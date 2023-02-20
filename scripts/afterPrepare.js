const fs = require('fs');
const path = require('path');

module.exports = function(ctx) {
  return new Promise(async (resolve) => {
    // console.log(ctx);
    const conf = await loadExtConfig(ctx.opts.projectRoot);
    const assetPaths = [...ctx.opts.paths];
    const promises = [];
    for (const assetPath of assetPaths) {
      const item = new Promise((subResolve, subReject) => {
        const thePath = path.join(assetPath, 'capacitor.config.json');
        fs.writeFile(thePath, JSON.stringify(conf.extConfig, null, 2), {encoding: 'utf-8'}, (err) => {
          if (err) {
            subReject(err);
          }
          // console.log(`Wrote ${thePath}`);
          subResolve();
        });
      });
      promises.push(item);
    }
    if (promises.length > 0) {
      Promise.all(promises).then(() => {
        resolve();
      }).catch((error) => {
        reject(error);
      })
    } else {
      resolve();
    }
  }).then(() => {
    console.log('[Capacitor-Compat] Synced config options.');
  })
};

const CONFIG_FILE_NAME_TS = 'capacitor.config.ts';
const CONFIG_FILE_NAME_JS = 'capacitor.config.js';
const CONFIG_FILE_NAME_JSON = 'capacitor.config.json';

async function pathExists(filePath) {
  try {
    await fs.access(filePath, fs.constants.F_OK);
  } catch (e) {
    return false;
  }

  return true;
}

async function loadExtConfig(rootDir) {
  const extConfigFilePathTS = path.resolve(rootDir, CONFIG_FILE_NAME_TS);

  if (await pathExists(extConfigFilePathTS)) {
    return loadExtConfigTS(rootDir, CONFIG_FILE_NAME_TS, extConfigFilePathTS);
  }

  const extConfigFilePathJS = path.resolve(rootDir, CONFIG_FILE_NAME_JS);

  if (await pathExists(extConfigFilePathJS)) {
    return loadExtConfigJS(rootDir, CONFIG_FILE_NAME_JS, extConfigFilePathJS);
  }

  const extConfigFilePath = path.resolve(rootDir, CONFIG_FILE_NAME_JSON);

  let jsonRead = {};

  try {
    jsonRead = JSON.parse(fs.readFileSync(extConfigFilePath));
  } catch(e) {
    // ignore
  }

  return {
    extConfigType: 'json',
    extConfigName: CONFIG_FILE_NAME_JSON,
    extConfigFilePath: extConfigFilePath,
    extConfig: jsonRead,
  };
}

function resolveNode(
  root,
  ...pathSegments
) {
  try {
    return require.resolve(pathSegments.join('/'), { paths: [root] });
  } catch (e) {
    return null;
  }
}

const requireTS = (ts, p) => {
  const id = path.resolve(p);

  delete require.cache[id];

  require.extensions['.ts'] = (
    module,
    fileName,
  ) => {
    let sourceText = readFileSync(fileName, 'utf8');

    if (fileName.endsWith('.ts')) {
      const tsResults = ts.transpileModule(sourceText, {
        fileName,
        compilerOptions: {
          module: ts.ModuleKind.CommonJS,
          moduleResolution: ts.ModuleResolutionKind.NodeJs,
          esModuleInterop: true,
          strict: true,
          target: ts.ScriptTarget.ES2017,
        },
        reportDiagnostics: true,
      });
      sourceText = tsResults.outputText;
    } else {
      // quick hack to turn a modern es module
      // into and old school commonjs module
      sourceText = sourceText.replace(/export\s+\w+\s+(\w+)/gm, 'exports.$1');
    }

    module._compile?.(sourceText, fileName);
  };

  const m = require(id); // eslint-disable-line @typescript-eslint/no-var-requires

  delete require.extensions['.ts'];

  return m;
};

async function loadExtConfigTS(
  rootDir,
  extConfigName,
  extConfigFilePath,
) {
  try {
    const tsPath = resolveNode(rootDir, 'typescript');

    if (!tsPath) {
      console.error(
        'Could not find installation of TypeScript.\n' +
          `To use ${extConfigName} files, you must install TypeScript in your project, e.g. w/ 'npm install -D typescript'`,
      );
    }

    const ts = require(tsPath);
    const extConfigObject = requireTS(ts, extConfigFilePath);
    const extConfig = extConfigObject.default ?? extConfigObject;

    return {
      extConfigType: 'ts',
      extConfigName,
      extConfigFilePath: extConfigFilePath,
      extConfig,
    };
  } catch (e) {
    console.error(`Parsing ${extConfigName} failed.\n\n${e.stack ?? e}`);
    throw e;
  }
}

async function loadExtConfigJS(
  rootDir,
  extConfigName,
  extConfigFilePath,
 ) {
  try {
    return {
      extConfigType: 'js',
      extConfigName,
      extConfigFilePath: extConfigFilePath,
      extConfig: require(extConfigFilePath),
    };
  } catch (e) {
    console.error(`Parsing ${extConfigName} failed.\n\n${e.stack ?? e}`);
  }
}
