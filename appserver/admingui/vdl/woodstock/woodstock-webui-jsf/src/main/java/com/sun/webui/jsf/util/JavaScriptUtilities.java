/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package com.sun.webui.jsf.util;

import com.sun.webui.jsf.theme.ThemeJavascript;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;
import static com.sun.webui.jsf.util.JsonUtilities.JSON_WRITER_FACTORY;
import static com.sun.webui.jsf.util.JsonUtilities.writeJsonObject;

/**
 * This class provides common methods for rendering JavaScript includes, default
 * properties, etc.
 */
public final class JavaScriptUtilities {

    /**
     * Cannot be instanciated.
     */
    private JavaScriptUtilities() {
    }

    // TODO
    // remove getDomNode
    // remove getModuleName
    // create enum for helper methods (to give some typing)

    /**
     * Render the DOJO config object.
     * Note: Must be rendered before including {@code dojo.js} in the page.
     *
     * @param debug Enable JavaScript debugging.
     * @param parseWidgets Enable searching of dojoType widget tags.
     * @param writer writer to use
     * @throws java.io.IOException if an input/output error occurs.
     */
    public static void renderHeaderScriptTags(final boolean debug,
            final boolean parseWidgets, final ResponseWriter writer)
            throws IOException {

        JsonObject path = JSON_BUILDER_FACTORY
                .createObjectBuilder()
                .add("webui/suntheme",
                        "../../com/sun/webui/jsf/suntheme/javascript")
                .build();
        JsonObject json = JSON_BUILDER_FACTORY
                .createObjectBuilder()
                .add("isDebug", debug)
                .add("debugAtAllCosts", debug)
                .add("parseWidgets", parseWidgets)
                .add("async", true)
                .add("paths", path)
                .build();

        StringWriter buff = new StringWriter();
        buff.append("var dojoConfig=");
        JsonWriter jsonWriter = JSON_WRITER_FACTORY.createWriter(buff);
        jsonWriter.writeObject(json);
        buff.append(";\n");
        renderScripTag(writer, buff.toString());

        renderInclude(writer, ThemeJavascript.DOJO);
        renderInclude(writer, ThemeJavascript.HELPER);
        renderInclude(writer, ThemeJavascript.PROTOTYPE);
        renderInclude(writer, ThemeJavascript.JSFX);

        // Render global include.
        String[] jsFiles = getTheme().getGlobalJSFiles();
        if (jsFiles == null) {
            return;
        }
        for (String jsFile : jsFiles) {
            Object file = jsFile;
            if (file == null) {
                continue;
            }
            writer.startElement("script", null);
            writer.writeAttribute("type", "text/javascript", null);
            writer.writeURIAttribute("src", file.toString(), null);
            writer.endElement("script");
            writer.write("\n");
        }
    }

    /**
     * Returns JavaScript to obtain the DOM node associated with the given
     * component.When complex components are rendered, a DOM object
     * corresponding to the component is created.To manipulate the component on
     * the client side, we will invoke functions on the DOM object.
     *
     * Providing a component, with a client id of {@code form1:btn1}, will
     * return {@code document.getElementById('form1:btn1')}. This JavaScript
     * obtains the DOM object associated the HTML element. Thus, we can disable
     * a button using
     * {@code document.getElementById('form1:btn1').disable(true);}
     *
     * @param context The current FacesContext.
     * @param component The current component being rendered.
     * @deprecated removed soon
     * @return String
     */
    public static String getDomNode(final FacesContext context,
            final UIComponent component) {

        return new StringBuilder()
                .append("document.getElementById('")
                .append(component.getClientId(context))
                .append("')")
                .toString();
    }

    /**
     * Returns a string comprised of a theme prefix and the given module
     * name.
     *
     * @param name The module to append to the theme prefix.
     * @deprecated removed soon
     * @return fully qualified module name
     */
    public static String getModuleName(final String name) {
        return new StringBuilder()
                .append("webui/suntheme")
                .append("/")
                .append(name)
                .toString();
    }

    /**
     * Render the JS call to {@code ws_init} in the page, including enclosing
     * script tags.
     *
     * @param writer ResponseWriter to which the component should be rendered.
     * @param moduleName name of the init module
     * @param properties init properties
     * @param extraCalls extra calls
     *
     * @exception IOException if an input/output error occurs.
     */
    public static void renderInitScriptTag(final ResponseWriter writer,
            final String moduleName, final JsonObject properties,
            final String... extraCalls) throws IOException {

        if (properties == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(renderInitCall(moduleName, properties));
        if (extraCalls != null) {
            for (String extraCall : extraCalls) {
                sb.append(extraCall);
                sb.append("\n");
            }
        }
        renderScripTag(writer, sb.toString());
    }

    /**
     * Render the JS call to {@code ws_init} in the page, including enclosing
     * script tags.
     *
     * @param writer ResponseWriter to which the component should be rendered.
     * @param moduleName name of the init module
     * @param properties init properties
     *
     * @exception IOException if an input/output error occurs.
     */
    public static void renderInitScriptTag(final ResponseWriter writer,
            final String moduleName, final JsonObject properties)
            throws IOException {

        renderInitScriptTag(writer, moduleName, properties, (String[]) null);
    }

    /**
     * Render the JS call to {@code ws_init_elt} in the page.
     *
     * @param moduleName name of the init module
     * @param properties init properties
     * @return String rendered call
     *
     * @throws IllegalArgumentException if properties is {@code null}
     */
    public static String renderInitCall(final String moduleName,
            final JsonObject properties) throws IllegalArgumentException {

        if (properties == null) {
            throw new IllegalArgumentException("event type is null");
        }
        return renderCall("init_elt", moduleName, properties);
    }

    /**
     * Render the JS call to {@code ws_on...} for a given event.
     *
     * @param moduleName module name that handles the event
     * @param extraCall The existing attribute value to append JS to.
     * @param eventType The JS event to invoke.
     * @return String rendered attribute value
     * @throws IllegalArgumentException if event type is {@code null}
     */
    public static String renderEventCall(final String moduleName,
            final String extraCall, final String eventType) {

        if (eventType == null) {
            throw new IllegalArgumentException("event type is null");
        }
        return renderCalls(
                renderCall(eventType, moduleName, "this"),
                extraCall);
    }

    /**
     * Render two JS calls, make sure they are separated by a {@code ';'}.
     *
     * @param firstCall first call, may be {@code null}
     * @param secondCall second call, may be {@code null}
     * @return String rendered calls
     */
    public static String renderCalls(final String firstCall,
            final String secondCall) {

        StringBuilder sb = new StringBuilder();
        if (firstCall != null) {
            sb.append(firstCall);
            if (!firstCall.endsWith(";")) {
                sb.append(";");
            }
        }
        if (secondCall != null) {
            sb.append(secondCall);
        }
        return sb.toString();
    }

    /**
     * Render a JS call to a method prefixed with {@code ws_}.
     *
     * @param methodName method call to render
     * @param arguments method arguments
     * @return String rendered attribute value
     */
    public static String renderCall(final String methodName,
            final Object... arguments) {

        if (methodName == null) {
            throw new IllegalArgumentException("method name is null");
        }
        StringBuilder buff = new StringBuilder();
        buff.append("ws_")
                .append(methodName)
                .append("(");
        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                if (i > 0) {
                    buff.append(",");
                }
                if (arguments[i] == null) {
                    buff.append("null");
                } else if (arguments[i] instanceof JsonObject) {
                    StringWriter jsonWriter = new StringWriter();
                    writeJsonObject((JsonObject) arguments[i], jsonWriter);
                    buff.append(jsonWriter.toString());
                } else if (arguments[i] instanceof List) {
                    if (((List) arguments[i]).isEmpty()) {
                        buff.append("null");
                        continue;
                    }
                    buff.append("[");
                    Iterator it = ((List) arguments[i]).iterator();
                    while (it.hasNext()) {
                          buff.append("'")
                            .append(String.valueOf(it.next()))
                            .append("'");
                        if (it.hasNext()) {
                            buff.append(",");
                        }
                    }
                    buff.append("]");
                } else if (arguments[i] instanceof String
                        && "this".equals((String) arguments[i])) {
                    buff.append("this");
                } else {
                    buff.append("'")
                            .append(String.valueOf(arguments[i]))
                            .append("'");
                }
            }
        }
        return buff.append(");")
                .toString();
    }

    /**
     * Render JavaScript in the page, including enclosing script tags.
     *
     * @param writer ResponseWriter to which the component should be rendered.
     * @param jsCode The JS code string nested within the scrip tag
     *
     * @exception IOException if an input/output error occurs.
     */
    public static void renderScripTag(final ResponseWriter writer,
            final String jsCode) throws IOException {

        if (jsCode == null) {
            return;
        }
        writer.startElement("script", null);
        writer.writeAttribute("type", "text/javascript", null);
        writer.write("\n");
        writer.write(jsCode);
        writer.endElement("script");
        writer.write("\n");
    }

    /**
     * Render given JavaScript file in page, including script tags.
     *
     * Note: JavaScript includes must be output in page prior to instantiating
     * widgets. This can be done via the head, themeLinks, or portalTheme tags,
     * but not via any other component renderer. Thus, this method is declared
     * private to ovoid misuse.
     *
     * If JavaScript includes are output by renderers, timing issues can occur
     * when client-side widgets and server-side components are rendered in the
     * same page. For example, button HTML may be rendered as a JSON property
     * (the child of a widget), which also contains a JavaScript include. In
     * this scenario, their buttons in the page may not initialize correctly
     * because the widget has not added the JavaScript include, yet. See CR
     * 6517246.
     *
     * @param writer The current ResponseWriter.
     * @param file The JavaScript file to include.
     * @throws IOException if an IO error occurs
     */
    private static void renderInclude(final ResponseWriter writer,
            final String file) throws IOException {

        if (file == null) {
            return;
        }

        String jsFile = getTheme().getPathToJSFile(file);
        if (jsFile == null) {
            return;
        }

        writer.startElement("script", null);
        writer.writeAttribute("type", "text/javascript", null);
        writer.writeURIAttribute("src", jsFile, null);
        writer.endElement("script");
        writer.write("\n");
    }

    /**
     * Helper method to get Theme objects.
     * @return Theme
     */
    private static Theme getTheme() {
        return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }
}
