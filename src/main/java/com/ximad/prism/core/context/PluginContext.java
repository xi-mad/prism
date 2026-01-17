package com.ximad.prism.core.context;

/**
 * 插件执行作用域 - 用于在调用链中捕获当前正在执行的插件 Bean 名称
 */
public class PluginContext {
    public static final ScopedValue<String> CURRENT_PLUGIN_NAME = ScopedValue.newInstance();
}
