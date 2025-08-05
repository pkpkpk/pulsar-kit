#!/usr/bin/env node

eval("require('{{HOME}}/target/{{IDENT}}/main/main.js')");

const out = {};
if ({{IDENT}}.main?.activate) out.activate = {{IDENT}}.main.activate;
if ({{IDENT}}.main?.deactivate) out.deactivate = {{IDENT}}.main.deactivate;
if ({{IDENT}}.main?.serialize) out.serialize = {{IDENT}}.main.serialize;

module.exports = out;
