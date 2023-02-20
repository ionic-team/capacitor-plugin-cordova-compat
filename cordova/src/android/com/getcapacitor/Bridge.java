package com.getcapacitor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.HashMap;
import java.util.Map;

public class Bridge {

    public static final String CAPACITOR_HTTP_SCHEME = "http";
    public static final int DEFAULT_ANDROID_WEBVIEW_VERSION = 60;
    public static final int MINIMUM_ANDROID_WEBVIEW_VERSION = 55;

    private CapConfig config;
    // A reference to the main activity for the app
    private final Activity context;
    // A map of Plugin Id's to PluginHandle's
    private Map<String, PluginHandle> plugins = new HashMap<>();
    // Stored plugin calls that we're keeping around to call again someday
    private Map<String, PluginCall> savedCalls = new HashMap<>();

    // Our Handler for posting plugin calls. Created from the ThreadHandler
    private Handler taskHandler = null;
    // Store a plugin that started a new activity, in case we need to resume
    // the app and return that data back
    private PluginCall pluginCallForLastActivity;
    /**
     * Create the Bridge with a reference to the main {@link Activity} for the
     * app
     * @param context
     */
    public Bridge(Activity context) {
        this.context = context;
        this.config = config != null ? config : CapConfig.loadDefault(this.context);
    }
    /**
     * Get a retained plugin call
     * @param callbackId the callbackId to use to lookup the call with
     * @return the stored call
     */
    public PluginCall getSavedCall(String callbackId) {
        return this.savedCalls.get(callbackId);
    }

    public void startActivityForPluginWithResult(PluginCall call, Intent intent, int requestCode) {
        pluginCallForLastActivity = call;
        getActivity().startActivityForResult(intent, requestCode);
    }

    /**
     * Get the activity for the app
     * @return
     */
    public Activity getActivity() { return this.context; }

    public Context getContext() {return this.context.getApplicationContext();}

    public CapConfig getConfig() {
        return this.config;
    }

    /**
     * Release a retained call
     * @param call
     */
    public void releaseCall(PluginCall call) {
        this.savedCalls.remove(call.getCallbackId());
    }

    public void execute(Runnable runnable) {
        taskHandler.post(runnable);
    }

    public PluginHandle getPlugin(String pluginId) {
        return this.plugins.get(pluginId);
    }

    /**
     * Register a plugin class
     * @param pluginInstance a class inheriting from Plugin
     */
    // public void registerPlugin(Class<? extends Plugin> pluginClass) {
    public void registerPlugin(Plugin pluginInstance) {
        Class<? extends Plugin> pluginClass = pluginInstance.getClass();
        CapacitorPlugin pluginAnnotation = pluginClass.getAnnotation(CapacitorPlugin.class);

        if (pluginAnnotation == null) {
            Log.e("Bridge", "CapacitorPlugin doesn't have the @CapacitorPlugin annotation. Please add it");
            return;
        }

        String pluginId = pluginClass.getSimpleName();

        // Use the supplied name as the id if available
        if (!pluginAnnotation.name().equals("")) {
            pluginId = pluginAnnotation.name();
        }
        try {
            this.plugins.put(pluginId, new PluginHandle(this, pluginInstance));
        } catch (Exception ex) {
            Log.e("Bridge", "CapacitorPlugin " + pluginClass.getName() +
                    " is invalid. Ensure the @CapacitorPlugin annotation exists on the plugin class and" +
                    " the class extends Plugin");
        }
    }


}