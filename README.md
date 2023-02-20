# Capacitor plugin Compatability for Cordova apps
#### Use your favorite Capacitor plugins in your cordova app!

> __Note:__ Capacitor plugins must explicitly support this compatability layer to be used with this system. Find out how to add that compatability to your plugin below.

## Usage:

1.  Run `npm i @capacitor/plugin-cordova-compat` in your cordova app.
2.  Run `cordova plugin add @capacitor/plugin-cordova-compat` in your cordova app.
3.  Install Capacitor plugins that have support for the compatability layer and enjoy!

## Using configuration for Capacitor Plugins:

1. Create a `capacitor.config.json` file in your project root with the following structure:
```json
{
  "plugins": {}
}
```
2. Add your configuration according to the plugins docs. For example the `Tester` plugin wants a word to log on start up so the config would look like this:
```json
{
  "plugins": {
    "Tester": {
      "wordToLogOnLoad": "Hello!!!"
    }
  }
}
```

## How to add cordova compatability to your Capacitor plugin:

1.  You need to find a few bits of info before proceding:
    1.   Find your exported name for your plugin. Normally this will be in your `src/index.ts` file and look similar to the below where the name is circled in <span style="color:red">__red__</span>. This will be replacing any `{{PLUGIN_EXPORT_NAME}}` you see in the files we will be creating in these instructions. You will also need a all lower case version to replace any `{{PLUGIN_EXPORT_NAME_LOWERCASE}}`. (ex. `Tester` and `tester` respecfully) [<img src="https://i.imgur.com/fz9l861.png">](https://i.imgur.com/fz9l861.png)

    2.  Find your current plugin version from your `package.json` files `version` property. This will be replacing any `{{PLUGIN_VERSION}}` you see in the files we will be creating in these instructions. (ex. `1.0.1`)

    3.  Find your android package name in your `android/src/main/AndroidManifest.xml` file, it will be a string in a `package="PACKAGE_HERE"`. This will be replacing any `{{ANDROID_PACKAGE}}` you see in the files we will be creating in these instructions. You will also need a path based version to replace any `{{ANDROID_PACKAGE_PATH}}`. (ex. `com.example.testerplugin` and `com/example/testerplugin` respecfully)

2.  Create a `cordova` folder in the root of the Capacitor plugin (where it's `package.json` is).

3.  Create two folders in the new `cordova` folder: `www` and `src`

4.  Create two folders in the new `cordova/src` folder: `android` and `ios`

5.  In the `cordova/src/android` folder create a `{{PLUGIN_EXPORT_NAME}}.gradle` file with the contents below. This is where any android dependencies or other gradle settings needed by the android side of your plugin will need to be copied/placed should you need any. 
```gradle
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {}
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

dependencies {
}

ext.postBuildExtras = {
}
```

6.  In the `cordova/www` folder create a `{{PLUGIN_EXPORT_NAME}}.js` file with the contents below (remember to do replacements). Modify the methods array to contain all the names of the methods your plugin exposes to its users.
```javascript
var exec = require('cordova/exec');

var PLUGIN_NAME = '{{PLUGIN_EXPORT_NAME}}Plugin';

const methods = [
  'echo'
];

const exportThis = methods.reduce((e, m) => {
  e[m] = args =>
    new Promise((resolve, reject) =>
      exec(resolve, reject, PLUGIN_NAME, m, args ? [args] : []),
    );
  return e;
}, {});

module.exports = exportThis;
```

7.  In the `cordova/src/ios` folder create a `{{PLUGIN_EXPORT_NAME}}Plugin.swift` file with the contents below (remember to do replacements). In this file you can see we have the example `echo` function using the implemention code of our Capacitor plugin but for cordova. You will need to create these shim function in the same style for your iOS plugin. (remember to use the `createCall` function to be able to return as you will be used to in Capacitor)
```swift
import Foundation

@objc({{PLUGIN_EXPORT_NAME}}Plugin)
public class {{PLUGIN_EXPORT_NAME}}Plugin: CAPPlugin {
    private lazy var implementation: {{PLUGIN_EXPORT_NAME}} = {{PLUGIN_EXPORT_NAME}}()

    // Called when plugin is loaded.
    override public func pluginInitialize() {
        super.pluginInitialize()
    }

    // Replace with your methods.
    @objc func echo(_ command: CDVInvokedUrlCommand) {
        // This line is needed to translate cordova to Capacitor API
        let call = createCall(command)
    
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }
}
```

8.  In the root of your plugins folder (where the package.json is) create a `plugin.xml` file with the content below replacing where needed and following any comments for extra instructions.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<plugin xmlns="http://apache.org/cordova/ns/plugins/1.0" id="{{PLUGIN_EXPORT_NAME_LOWERCASE}}-plugin" version="{{PLUGIN_VERSION}}">
    <name>{{PLUGIN_EXPORT_NAME_LOWERCASE}}-plugin</name>
    <description>{{PLUGIN_EXPORT_NAME_LOWERCASE}}-plugin</description>
    <license>SEE LICENSE</license>
    <keywords>capacitor,{{PLUGIN_EXPORT_NAME_LOWERCASE}},cordova</keywords>

    <js-module src="cordova/www/{{PLUGIN_EXPORT_NAME}}.js" name="{{PLUGIN_EXPORT_NAME_LOWERCASE}}plugin">
        <runs/>
        <clobbers target="{{PLUGIN_EXPORT_NAME}}"/>
    </js-module>
    <platform name="android">
        <config-file target="res/xml/config.xml" parent="/*">
            <feature name="{{PLUGIN_EXPORT_NAME}}Plugin">
                <param name="android-package" value="{{ANDROID_PACKAGE}}.{{PLUGIN_EXPORT_NAME}}Plugin"/>
                <param name="onload" value="true"/>
            </feature>
        </config-file>
        <config-file target="AndroidManifest.xml" parent="/*">
            <uses-permission android:name="android.permission.INTERNET"/>
        </config-file>
        <framework src="cordova/src/android/{{PLUGIN_EXPORT_NAME}}.gradle" custom="true" type="gradleReference" />

        <!-- add implementation and call code files from your capacitor plugin below. Example: -->

        <!-- Call code: -->
        <!-- <source-file src="android/src/main/java/{{ANDROID_PACKAGE_PATH}}/{{PLUGIN_EXPORT_NAME}}Plugin.java" target-dir="src/{{ANDROID_PACKAGE_PATH}}"/> -->

        <!-- Implementation code: -->
        <!-- <source-file src="android/src/main/java/{{ANDROID_PACKAGE_PATH}}/{{PLUGIN_EXPORT_NAME}}.java" target-dir="src/{{ANDROID_PACKAGE_PATH}}"/> -->
    </platform>

    <!-- ios -->
    <platform name="ios">
        <config-file target="config.xml" parent="/*">
            <feature name="{{PLUGIN_EXPORT_NAME}}Plugin">
                <param name="ios-package" value="{{PLUGIN_EXPORT_NAME}}Plugin" onload="true"/>
            </feature>
            <preference name="UseSwiftLanguageVersion" value="5"/>
        </config-file>
        <dependency id="cordova-plugin-add-swift-support" version="2.0.2"/>
        <source-file src="cordova/src/ios/{{PLUGIN_EXPORT_NAME}}Plugin.swift"/>

        <!-- add implementation files from your capacitor plugin below (do not include call code like android, ios needs the shim we create in the setup instructions). Example: -->

        <!-- Implementation code: -->
        <!-- <source-file src="ios/Plugin/{{PLUGIN_EXPORT_NAME}}.swift"/> -->
    </platform>

</plugin>
```

9.  In your `src/index.ts` where you found your `{{PLUGIN_EXPORT_NAME}}` you can make changes to make the cordova usage of the plugin the same as the usage of the capacitor side by making replacements to the below and using this as your `src/index.ts`. Note this is based on basic capacitor plugin creation, and if you are doing anything custom here you will need to add that in as well. 
```typescript
import { registerPlugin } from '@capacitor/core';

import type { {{PLUGIN_EXPORT_NAME}}Plugin } from './definitions';

let {{PLUGIN_EXPORT_NAME}}Impl;

if ((window as any).cordova !== undefined && (window as any).cordova.platformVersion !== '1.0.0') {
  {{PLUGIN_EXPORT_NAME}}Impl = new Proxy({}, {
    get(_target, property) {
      return (window as any).{{PLUGIN_EXPORT_NAME}}[property];
    }
  });
} else {
  {{PLUGIN_EXPORT_NAME}}Impl = registerPlugin<TesterPlugin>('Tester', {
    web: () => import('./web').then(m => new m.TesterWeb()),
  });
}

const {{PLUGIN_EXPORT_NAME}} = {{PLUGIN_EXPORT_NAME}}Impl as {{PLUGIN_EXPORT_NAME}}Plugin;

export * from './definitions';
export { {{PLUGIN_EXPORT_NAME}} };
```

10. You will need to add some properties to your `package.json` so cordova will reconize your plugin. Add the below to the end of your `package.json` while doing any replacements needed like the above steps.
```json
  "cordova": {
    "id": "{{PLUGIN_EXPORT_NAME_LOWERCASE}}-plugin",
    "platforms": [
      "android",
      "ios"
    ]
  },
  "engines": {
    "cordovaDependencies": {
      "3.0.0": {
        "cordova": ">100"
      }
    }
  }
```

11. Also in your `package.json` be sure to include the new files created in these steps in your package when published. For example if you use the `files` property in the `package.json` it would look like the below with the changes in this step.
```json
  "files": [
    ...,
    "cordova/",
    "plugin.xml"
  ],
```