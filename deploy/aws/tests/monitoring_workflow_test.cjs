const assert = require('node:assert/strict');
const fs = require('node:fs');

const workflow = JSON.parse(fs.readFileSync('deploy/aws/monitoring/workflows/cinema-ai-alerts.json', 'utf8'));
const node = (name) => workflow.nodes.find((item) => item.name === name);

function evaluateExpression(expression, values) {
  assert.ok(expression.startsWith('={{ ') && expression.endsWith(' }}'));
  const $ = (name) => ({ first: () => ({ json: values[name] }) });
  return new Function('$', `return (${expression.slice(4, -3)});`)($);
}

function runCode(name, values) {
  const $ = (key) => ({ first: () => ({ json: values[key] }) });
  return new Function('$', node(name).parameters.jsCode)($);
}

const condition = node('Is Firing').parameters.conditions.conditions[0].leftValue;
assert.equal(evaluateExpression(condition, {
  'Normalize Alert': { status: 'firing' },
  'Slack Root Alert': { ok: true, channel: 'C123', ts: '123.456' },
}), 'firing');
assert.notEqual(evaluateExpression(condition, {
  'Normalize Alert': { status: 'firing' },
  'Slack Root Alert': { ok: false, error: 'channel_not_found' },
}), 'firing');
assert.notEqual(evaluateExpression(condition, {
  'Normalize Alert': { status: 'firing' },
  'Slack Root Alert': { ok: true, channel: 'C123' },
}), 'firing');
assert.notEqual(evaluateExpression(condition, {
  'Normalize Alert': { status: 'resolved' },
  'Slack Root Alert': { ok: true, channel: 'C123', ts: '123.456' },
}), 'firing');

const [reply] = runCode('Prepare Slack Thread Reply', {
  'OpenAI Incident Analysis': { output_text: '원인 후보' },
  'Slack Root Alert': { ok: true, channel: 'C123', ts: '123.456' },
});
assert.equal(reply.json.channel, 'C123');
assert.equal(reply.json.thread_ts, '123.456');
assert.match(reply.json.text, /원인 후보/);

const [fallback] = runCode('Prepare Slack Thread Reply', {
  'OpenAI Incident Analysis': { error: 'request failed' },
  'Slack Root Alert': { ok: true, channel: 'C123', ts: '123.456' },
});
assert.equal(fallback.json.thread_ts, '123.456');
assert.match(fallback.json.text, /AI 분석 실패/);

const deploymentSha = 'b557a7bdc2ddd03240f7dfb9d86fd9f972da1564';
const [context] = runCode('Select Exact Deployment Context', {
  'Normalize Alert': { status: 'firing', summary: 'API failure', version: deploymentSha },
  'Loki Error Logs': { data: { result: [{ values: [[
    '1',
    'level=ERROR version=' + deploymentSha + ' requestId=req-123 password=top-secret user@example.com\n' +
      '\tat com.example.reservation.ReservationService.reserve(ReservationService.java:42)',
  ]] }] } },
  'Prometheus Error Metrics': { data: { result: [] } },
});
assert.equal(context.json.version, deploymentSha);
assert.equal(context.json.lineNumber, 42);
assert.equal(context.json.sourceUrl,
  `https://raw.githubusercontent.com/strangehoon/cinema/${deploymentSha}/src/main/java/com/example/reservation/ReservationService.java`);
assert.match(context.json.logs, /requestId=req-123/);
assert.doesNotMatch(context.json.logs, /top-secret|user@example.com/);

const [nestedContext] = runCode('Select Exact Deployment Context', {
  'Normalize Alert': { status: 'firing', summary: 'API failure', version: deploymentSha },
  'Loki Error Logs': { data: { result: [{ values: [[
    '2',
    'level=ERROR version=' + deploymentSha + '\n' +
      '\tat com.example.reservation.ReservationService$Worker.run(ReservationService.java:47)',
  ]] }] } },
  'Prometheus Error Metrics': { data: { result: [] } },
});
assert.equal(nestedContext.json.sourceUrl, context.json.sourceUrl);
assert.equal(nestedContext.json.lineNumber, 47);

const [analysis] = runCode('Build Sanitized AI Prompt', {
  'Select Exact Deployment Context': context.json,
  'Read Deployed Source': { data: Array.from({ length: 50 }, (_, index) => `line ${index + 1}`).join('\n') },
});
assert.match(analysis.json.prompt, /42: line 42/);
assert.match(analysis.json.prompt, new RegExp(deploymentSha));
assert.doesNotMatch(analysis.json.prompt, /top-secret|user@example.com/);

const [unknownContext] = runCode('Select Exact Deployment Context', {
  'Normalize Alert': { status: 'firing', summary: 'API failure', version: '' },
  'Loki Error Logs': { data: { result: [] } },
  'Prometheus Error Metrics': { data: { result: [] } },
});
const [unknownAnalysis] = runCode('Build Sanitized AI Prompt', {
  'Select Exact Deployment Context': unknownContext.json,
  'Read Deployed Source': {},
});
assert.equal(unknownContext.json.sourceUrl, '');
assert.match(unknownAnalysis.json.prompt, /Do not infer code details/);

console.log('Monitoring workflow checks passed');
