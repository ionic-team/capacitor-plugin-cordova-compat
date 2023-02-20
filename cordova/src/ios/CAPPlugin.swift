@objc public class CAPPlugin: CDVPlugin {
    private var config: CAPPluginConfig?
    
    func getConfig(_ pluginName: String) -> CAPPluginConfig {
        if let config = config {
            return config
        } else {
            guard let configUrl = Bundle.main.url(forResource: "capacitor.config", withExtension: "json", subdirectory: "www"),
                  let configData = try? Data(contentsOf: configUrl),
                  let json = try? JSONSerialization.jsonObject(with: configData) as? [String: [String: Any]],
                  let pluginConfig = json?["plugins"]?[pluginName] as? [String: Any],
                  let pluginConfigJsObject = JSTypes.coerceDictionaryToJSObject(pluginConfig)
            else {
                let config = CAPPluginConfig(config: JSObject())
                self.config = config
                return config
            }
            let config = CAPPluginConfig(config: pluginConfigJsObject)
            self.config = config
            return config
        }
    }
}
