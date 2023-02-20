import Foundation

protocol CDVConverter: CAPPlugin {
    func createCall(_ command: CDVInvokedUrlCommand) -> CAPPluginCall
}

extension CDVConverter {
    func createCall(_ command: CDVInvokedUrlCommand) -> CAPPluginCall {
        let commandDelegate = self.commandDelegate
        let callbackId = command.callbackId
        
        var theOptions = command.arguments.count > 0 ? command.arguments[0] as? [AnyHashable : Any] : nil
        if theOptions == nil {
            theOptions = [AnyHashable : Any]()
        }
        
        
        let theErrorHandler = { (error: CAPPluginCallError?) in
            let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_ERROR,
                messageAs: error?.message
            )
            commandDelegate!.send(
                pluginResult,
                callbackId: callbackId
            )
        }
        let theSuccessHandler = { (result: CAPPluginCallResult?, call: CAPPluginCall?) in
            let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: result?.data
            )
            commandDelegate!.send(
                pluginResult,
                callbackId: callbackId
            )
        }
        
        return CAPPluginCall(callbackId: callbackId, options: theOptions!, success: theSuccessHandler, error: theErrorHandler)
    }
}

extension CAPPlugin: CDVConverter {}
