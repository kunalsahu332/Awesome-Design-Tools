#!/usr/bin/env node
// Extracts tools from the repo README into the widget's bundled tools.json.
// Usage: node android-widget/scripts/extract-tools.js
const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..', '..');
const readme = fs.readFileSync(path.join(root, 'README.md'), 'utf8');
const out = path.join(__dirname, '..', 'app', 'src', 'main', 'assets', 'tools.json');

const tools = [];
let category = null;
for (const line of readme.split('\n')) {
  const h = line.match(/^###\s+(.+?)\s*$/);
  if (h) {
    category = /addendum/i.test(h[1]) ? null : h[1].trim();
    continue;
  }
  if (!category) continue;
  const m = line.match(/^\*\s+\[([^\]]+)\]\((https?:\/\/[^)\s]+)\)\s*[—–-]\s*(.+)$/);
  if (!m) continue;
  const description = m[3]
    .replace(/\[!\[[^\]]*\]\([^)]*\)\]\([^)]*\)/g, '') // linked badges
    .replace(/!\[[^\]]*\]\([^)]*\)/g, '')            // badges
    .replace(/\[\]\([^)]*\)/g, '')                   // empty links
    .replace(/\[([^\]]+)\]\([^)]*\)/g, '$1')         // inline links
    .replace(/<[^>]+>/g, '')
    .replace(/\s+/g, ' ')
    .trim();
  tools.push({ name: m[1].trim(), url: m[2], category, description });
}

fs.mkdirSync(path.dirname(out), { recursive: true });
fs.writeFileSync(out, JSON.stringify(tools, null, 1) + '\n');
console.log(`Wrote ${tools.length} tools across ${new Set(tools.map(t => t.category)).size} categories to ${path.relative(root, out)}`);
