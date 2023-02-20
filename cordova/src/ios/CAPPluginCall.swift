import Foundation

public typealias PluginCallErrorData = [String:Any]
public typealias PluginResultData = [String:Any]
typealias CAPPluginCallSuccessHandler = (CAPPluginCallResult?, CAPPluginCall?) -> Void
typealias CAPPluginCallErrorHandler = (CAPPluginCallError?) -> Void

@objc class CAPPluginCallResult: NSObject {
    var data: [String : Any?]?

    init(_ data: [String : Any?]? = nil) {
        self.data = data
    }
}

@objc class CAPPluginCallError: NSObject {
    var message: String?
    var error: Error?
    var data: [String : Any?]?

    init(message: String?, error: Error?, data: [String : Any?]?) {
        self.message = message
        self.error = error
        self.data = data
    }
}

@objc class CAPPluginCall: NSObject {
    private static let UNIMPLEMENTED = "not implemented"
    
    var isSaved = false
    var callbackId: String?
    var options: [AnyHashable : Any]
    var successHandler: CAPPluginCallSuccessHandler
    var errorHandler: CAPPluginCallErrorHandler
    
    init(callbackId: String? = nil, options: [AnyHashable : Any] = [AnyHashable : Any](), success: @escaping CAPPluginCallSuccessHandler, error: @escaping CAPPluginCallErrorHandler) {
        self.callbackId = callbackId
        self.options = options
        self.successHandler = success
        self.errorHandler = error
    }

    func save() {
        isSaved = true
    }

    func get<T>(_ key: String, _ ofType: T.Type, _ defaultValue: T? = nil) -> T? {
        return self.options[key] as? T ?? defaultValue
    }
    
    func getArray<T>(_ key: String, _ ofType: T.Type, _ defaultValue: [T]? = nil) -> [T]? {
      return self.options[key] as? [T] ?? defaultValue
    }
    
    func getBool(_ key: String, _ defaultValue: Bool? = nil) -> Bool? {
      return self.options[key] as? Bool ?? defaultValue
    }
    
    func getInt(_ key: String, _ defaultValue: Int? = nil) -> Int? {
      return self.options[key] as? Int ?? defaultValue
    }
    
    func getFloat(_ key: String, _ defaultValue: Float? = nil) -> Float? {
      return self.options[key] as? Float ?? defaultValue
    }
    
    func getDouble(_ key: String, _ defaultValue: Double? = nil) -> Double? {
      return self.options[key] as? Double ?? defaultValue
    }
    
    func getString(_ key: String) -> String? {
      return self.options[key] as? String
    }
    
    func getString(_ key: String, _ defaultValue: String) -> String {
      return self.options[key] as? String ?? defaultValue
    }
    
    func getDate(_ key: String, _ defaultValue: Date? = nil) -> Date? {
      guard let isoString = self.options[key] as? String else {
        return defaultValue
      }
      let dateFormatter = DateFormatter()
      dateFormatter.locale = Locale(identifier: "en_US_POSIX")
      dateFormatter.timeZone = TimeZone.autoupdatingCurrent
      dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
      return dateFormatter.date(from: isoString)
    }
    
    func getObject(_ key: String, defaultValue: JSObject? = nil) -> JSObject? {
      let obj = self.options[key] as? [String:Any]
      return obj != nil ? toJSObject(obj!) : defaultValue
    }
    
    func hasOption(_ key: String) -> Bool {
      return self.options.index(forKey: key) != nil
    }

    func success() {
      successHandler(CAPPluginCallResult(), self)
    }
    
    func success(_ data: PluginResultData = [:]) {
      successHandler(CAPPluginCallResult(data), self)
    }
    
    func resolve() {
      successHandler(CAPPluginCallResult(), self)
    }
    
    func resolve(_ data: PluginResultData = [:]) {
      successHandler(CAPPluginCallResult(data), self)
    }
    
    func error(_ message: String, _ error: Error? = nil, _ data: PluginCallErrorData = [:]) {
      errorHandler(CAPPluginCallError(message: message, error: error, data: data))
    }
    
    func reject(_ message: String, _ error: Error? = nil, _ data: PluginCallErrorData = [:]) {
      errorHandler(CAPPluginCallError(message: message, error: error, data: data))
    }

    func unimplemented() {
      errorHandler(CAPPluginCallError(message: CAPPluginCall.UNIMPLEMENTED, error: nil, data: [:]))
    }

    private func toJSObject(_ src: [String: Any]) -> JSObject {
      var obj = JSObject()
      src.keys.forEach { key in
        obj[key] = coerceToJSValue(src[key], formattingDates: false)
      }
      return obj
    }
}
