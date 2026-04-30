package com.sun.enterprise.deployment;

import jakarta.servlet.ServletContext;

import org.glassfish.api.event.EventTypes;

public record WebBundleRuntimeContext(
        WebBundleDescriptor webBundleDescriptor,
        ServletContext servletContext) {

    /** Used by the deployer and the web container */
    public static final EventTypes<WebBundleRuntimeContext> AFTER_SERVLET_CONTEXT_INITIALIZED_EVENT =
        EventTypes.create("After_Servlet_Context_Initialized", WebBundleRuntimeContext.class);

    public static final EventTypes<WebBundleRuntimeContext> AFTER_SERVLET_LOAD_INITIALIZED_EVENT =
        EventTypes.create("After_Servlet_Load_Initialized", WebBundleRuntimeContext.class);
}