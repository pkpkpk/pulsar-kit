#!/usr/bin/env node

eval("require('{{PACKAGE_PATH}}/target/{{PATH_IDENT}}/main/main.js')");

const out = {};
if ({{PATH_IDENT}}.main?.activate) out.activate = {{PATH_IDENT}}.main.activate;
if ({{PATH_IDENT}}.main?.deactivate) out.deactivate = {{PATH_IDENT}}.main.deactivate;
if ({{PATH_IDENT}}.main?.serialize) out.serialize = {{PATH_IDENT}}.main.serialize;

module.exports = out;
