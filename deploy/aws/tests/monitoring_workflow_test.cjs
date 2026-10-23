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

console.log('Monitoring workflow checks passed');
