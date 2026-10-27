const assert = require('node:assert/strict');
const fs = require('node:fs');

const alloy = fs.readFileSync('deploy/aws/app/alloy.alloy', 'utf8');
const compose = fs.readFileSync('deploy/aws/monitoring/compose.yml', 'utf8');
const endpoint = alloy.match(/url = "(http:\/\/[^\"]+\/loki\/api\/v1\/push)"/);
assert.ok(endpoint, 'Alloy must have a Loki push endpoint');

const { hostname, port } = new URL(endpoint[1]);
const loki = compose.split(/^  loki:\s*$/m)[1]?.split(/^  n8n:\s*$/m)[0];
assert.ok(loki, 'Monitoring Compose must define Loki');
assert.ok(
  loki.includes(`- "${hostname}:${port}:${port}"`),
  `Loki must publish ${hostname}:${port} so Alloy on the app EC2 can reach it`,
);

console.log('Monitoring network checks passed');
