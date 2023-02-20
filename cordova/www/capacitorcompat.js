var exec = require('cordova/exec');

var PLUGIN_NAME = 'CapacitorCompat';

const methods = [];

module.exports = methods.reduce((e, m) => {
  e[m] = args =>
    new Promise((resolve, reject) =>
      exec(resolve, reject, PLUGIN_NAME, m, args ? [args] : []),
    );
  return e;
}, {});