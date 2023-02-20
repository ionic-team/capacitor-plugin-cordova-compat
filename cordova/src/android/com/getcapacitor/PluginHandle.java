package com.getcapacitor;

import com.getcapacitor.annotation.CapacitorPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * PluginHandle is an instance of a plugin that has been registered
 * and indexed. Think of it as a Plugin instance with extra metadata goodies
 */
public class PluginHandle {
    private final Bridge bridge;
    private final Class<? extends Plugin> pluginClass;

    private Map<String, PluginMethodHandle> pluginMethods = new HashMap<>();

    private final String pluginId;

    private CapacitorPlugin pluginAnnotation;
    private Plugin instance;

    public PluginHandle(Bridge bridge, Plugin plugin) throws Exception {
        Class<? extends Plugin> pluginClass = plugin.getClass();

        this.bridge = bridge;
        this.pluginClass = pluginClass;
        this.instance = plugin;
        this.instance.setPluginHandle(this);
        this.instance.setBridge(this.bridge);

        CapacitorPlugin pluginAnnotation = pluginClass.getAnnotation(CapacitorPlugin.class);
        if (pluginAnnotation == null) {
            throw new Exception("No @CapacitorPlugin annotation found for plugin " + pluginClass.getName());
        }

        if (!pluginAnnotation.name().equals("")) {
            this.pluginId = pluginAnnotation.name();
        } else {
            this.pluginId = pluginClass.getSimpleName();
        }

        this.pluginAnnotation = pluginAnnotation;

        this.indexMethods(pluginClass);

        this.instance.load();

        this.load();
    }

    public Class<? extends Plugin> getPluginClass() {
        return pluginClass;
    }

    public String getId() {
        return this.pluginId;
    }
    public CapacitorPlugin getPluginAnnotation() { return this.pluginAnnotation; }
    public Plugin getInstance() {
        return this.instance;
    }

    public Collection<PluginMethodHandle> getMethods() {
        return this.pluginMethods.values();
    }

    public Plugin load() throws Exception {
        if(this.instance != null) {
            return this.instance;
        }

        this.instance.load();
        return this.instance;
    }

    /**
     * Call a method on a plugin.
     * @param methodName the name of the method to call
     * @param call the constructed PluginCall with parameters from the caller
     * @throws Exception if no method was found on that plugin
     */
    public void invoke(String methodName, PluginCall call) throws Exception {
        if(this.instance == null) {
            // Can throw PluginLoadException
            this.load();
        }

        PluginMethodHandle methodMeta = pluginMethods.get(methodName);
        if(methodMeta == null) {
            throw new Exception("No method " + methodName + " found for plugin " + pluginClass.getName());
        }

        try {
            methodMeta.getMethod().invoke(this.instance, call);
        } catch(InvocationTargetException | IllegalAccessException ex) {
            throw new Exception("Unable to invoke method " + methodName + " on plugin " + pluginClass.getName(), ex);
        }
    }

    /**
     * Index all the known callable methods for a plugin for faster
     * invocation later
     */
    private void indexMethods(Class<? extends Plugin> plugin) {
        //Method[] methods = pluginClass.getDeclaredMethods();
        Method[] methods = pluginClass.getMethods();

        for(Method methodReflect: methods) {
            PluginMethod method = methodReflect.getAnnotation(PluginMethod.class);

            if(method == null) {
                continue;
            }

            PluginMethodHandle methodMeta = new PluginMethodHandle(methodReflect, method);
            pluginMethods.put(methodReflect.getName(), methodMeta);
        }
    }
}