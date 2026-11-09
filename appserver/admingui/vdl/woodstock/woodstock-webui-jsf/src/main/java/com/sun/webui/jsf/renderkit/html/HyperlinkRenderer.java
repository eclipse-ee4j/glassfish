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
package com.sun.webui.jsf.renderkit.html;

import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.Hyperlink;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIParameter;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ActionEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.sun.webui.jsf.util.ConversionUtilities.convertValueToString;
import static com.sun.webui.jsf.util.JavaScriptUtilities.renderCall;
import static com.sun.webui.jsf.util.JavaScriptUtilities.renderCalls;
import static com.sun.webui.jsf.util.RenderingUtilities.renderURLAttribute;
import static com.sun.webui.jsf.util.ThemeUtilities.getTheme;

/**
 * This class is responsible for rendering the {@link Hyperlink} component for
 * the HTML Render Kit.
 * <p>
 * The {@link Hyperlink} component can be used as an anchor, a plain hyperlink
 * or a hyperlink that submits the form depending on how the properties are
 * filled out for the component
 * </p>
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.Hyperlink"))
public class HyperlinkRenderer extends AbstractRenderer {

    /**
     * The set of {@code boolean} pass-through attributes to be rendered. Note:
     * if you add a {@code boolean} here and you want it rendered if the
     * hyperlink is disabled then you must fix the renderer to work properly!
     */
    private static final String[] BOOLEAN_ATTRIBUTES = {
        "disabled",
    };

    /**
     * The set of integer pass-through attributes to be rendered.
     */
    private static final String[] INT_ATTRIBUTES = {
        "tabIndex",
    };

    /**
     * The set of String pass-through attributes to be rendered.
     */
    private static final String[] STRING_ATTRIBUTES = {
        "onBlur",
        "onFocus",
        "onDblClick",
        "onKeyDown",
        "onKeyPress",
        "onMouseUp",
        "onKeyUp",
        "onMouseDown",
        "onMouseMove",
        "onMouseOut",
        "onMouseOver",
    };

    /**
     * The log message to be displayed if name and/or value attribute is null.
     */
    private static final String PARAM_ERROR
            = "Hyperlink UIParameter child attribute name and/or value not set,"
            + " id = ";

    /**
     * This implementation returns {@code true}.
     * @return {@code boolean}
     */
    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * This implementation decodes the link from the user input.
     * @param context faces context
     * @param component UI component
     */
    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        // Enforce NPE requirements in the Javadocs
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }
        Hyperlink link = (Hyperlink) component;

        if (isSubmitLink(link)) {
            String paramId = this.getSubmittedParameterId(context, component);
            String value = (String) context.getExternalContext()
                    .getRequestParameterMap().get(paramId);

            if ((value == null)
                    || !value.equals(component.getClientId(context))) {
                return;
            }

            // add the event to the queue so we know that a command happened.
            // this should automatically take care of actionlisteners and
            // actions
            link.queueEvent(new ActionEvent(link));
        }
    }

    /**
     * Returns the identifier for the parameter that corresponds to the hidden
     * field used to pass the value of the component that submitted the page.
     *
     * @param context faces context
     * @param component UI component
     * @return String
     */
    protected String getSubmittedParameterId(final FacesContext context,
            final UIComponent component) {

        return component.getClientId(context) + "_submittedField";
    }

    /**
     * This implementation is empty.
     * @param context faces context
     * @param component UI component
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {
    }

    /**
     * This implementation is empty.
     * @param context faces context
     * @param component UI component
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    @Override
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {
    }

    /**
     * This implementation is empty.
     * @param context faces context
     * @param component UI component
     * @throws IOException if an IO error occurs
     */
    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {
    }

    /**
     * This implementation is empty.
     * @param context faces context
     * @param component UI component
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        renderLink(context, component, writer);
    }

    /**
     * Finish render attributes.
     * @param context faces context
     * @param component UI component
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    protected void finishRenderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Hyperlink link = (Hyperlink) component;

        // Set up local variables we will need
        String label = convertValueToString(component, link.getText());
        if (label != null) {
            writer.writeText(label, null);
        }
    }

    /**
     * Render the link.
     * @param context faces context
     * @param component UI component
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    protected void renderLink(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Hyperlink link = (Hyperlink) component;
        if (!link.isDisabled()) {
            // Start the appropriate element
            writer.startElement("a", link);
        } else {
            writer.startElement("span", link);
        }

        // Set up local variables we will need
        String label = convertValueToString(component, link.getText());

        String url = link.getUrl();
        String target = link.getTarget();
        String tooltip = link.getToolTip();
        String onclick = link.getOnClick();
        String urlLang = link.getUrlLang();

        // Render core and pass through attributes as necessary
        String sb = getStyles(context, link);

        addCoreAttributes(context, component, writer, sb);
        addIntegerAttributes(context, component, writer, INT_ATTRIBUTES);
        addStringAttributes(context, component, writer, STRING_ATTRIBUTES);

        if (!link.isDisabled()) {
            // no such thing as disabling a span so we must do this here.
            addBooleanAttributes(context, component, writer,
                    BOOLEAN_ATTRIBUTES);

            // writeout href for the a tag:
            if (url != null) {
                // URL is not empty, check and see if it's an anchor, mailto,
                // or javascript -- see bugtraq #6306848 & #6322887.
                if (url.startsWith("#")
                        || url.startsWith("mailto:")
                        || url.startsWith("javascript:")) {
                    writer.writeURIAttribute("href", url, "url");
                } else {
                    // Append context path to relative URLs -- bugtraq #6306727.
                    // Invoke the getCorrectURL method so that subclassed
                    // components may implement their own solution for
                    // prepending the right context
                    url = getCorrectURL(context, link, url);
                    renderURLAttribute(context, writer, component, "href", url,
                            "url");
                }
                if (onclick != null) {
                    writer.writeAttribute("onclick", onclick, "onclick");
                }
            } else {
                UIComponent form
                        = ComponentUtilities.getForm(context, component);
                if (form != null) {
                    List<String> params = new ArrayList<String>();
                    for (UIComponent kid : component.getChildren()) {
                        if (!(kid instanceof UIParameter)) {
                            continue;
                        }
                        String name = (String) kid.getAttributes().get("name");
                        String value = (String) kid.getAttributes()
                                .get("value");
                        if (name == null || value == null) {
                            log(PARAM_ERROR + kid.getId());
                            continue;
                        }
                        params.add(name);
                        params.add(value);
                    }
                    String formClientId = form.getClientId(context);
                    StringBuilder buff = new StringBuilder();
                    buff.append(renderCalls(onclick,
                            // ws_hyperlink_submit
                            renderCall("hyperlink_submit", "this", formClientId,
                                    params)));
                    writer.writeAttribute("onclick", buff.toString(), null);
                    writer.writeAttribute("href", "#", null);
                }
            }

            if (null != target) {
                writer.writeAttribute("target", target, null);
            }
            if (null != tooltip) {
                writer.writeAttribute("title", tooltip, null);
            }
            if (null != urlLang) {
                writer.writeAttribute("hreflang", urlLang, "urlLang");
            }
        }

        // for hyperlink, this will encodeChildren as well, but not for
        // subclasses unless they explicitly do it!
        finishRenderAttributes(context, component, writer);

        renderChildren(context, component);

        // End the appropriate element
        if (!link.isDisabled()) {
            writer.endElement("a");
        } else {
            // no need to render params for disabled link
            writer.endElement("span");
        }
    }

    /**
     * This method is called by renderEnd.It is provided so renderer that
     * extend HyperlinkRenderer (such as TabRenderer) may override it in order
     * to prevent children from always being rendered.
     *
     * @param context The current FacesContext.
     * @param component The current component.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderChildren(final FacesContext context,
            final UIComponent component) throws IOException {

        super.encodeChildren(context, component);
    }

    /**
     * This function returns the style classes necessary to display the
     * {@link Hyperlink} component as it's state indicates.
     *
     * @param context The current FacesContext.
     * @param component The current component.
     * @return the style classes needed to display the current state of the
     * component
     */
    protected String getStyles(final FacesContext context,
            final UIComponent component) {

        Hyperlink link = (Hyperlink) component;
        StringBuilder sb = new StringBuilder();
        Theme theme = getTheme(context);
        if (link.isDisabled()) {
            sb.append(" ");
            sb.append(theme.getStyleClass(ThemeStyles.LINK_DISABLED));
        }
        if (sb.length() > 0) {
            return sb.toString();
        }
        return null;
    }

    /**
     * This method returns the most appropriate URL under the circumstances.In
     * some cases {@code viewhandler.getActionURL()} needs to be invoked while
     * in other cases {@code viewhandler.getResourceURL()} needs to be
     * invoked.The hyperlink renderer, by default, will always use latter while
     * generating the complete URL. Subclasses of this renderer can do it their
     * own way.
     *
     * @param context The current FacesContext.
     * @param component The current component.
     * @param url input URL
     * @return String
     */
    protected String getCorrectURL(final FacesContext context,
            final UIComponent component, final String url) {

        if (url == null) {
            return null;
        }
        return context.getApplication()
                .getViewHandler()
                .getResourceURL(context, url);
    }

    /**
     * Test if the given link is a submit link.
     *
     * @param h link to test
     * @return {@code true} if a submit link, {@code false} otherwise
     */
    private static boolean isSubmitLink(final Hyperlink h) {
        return (h.getUrl() == null);
    }

    /**
     * Log an error.
     *
     * @param msg error message to log
     */
    private static void log(final String msg) {
        if (LogUtil.fineEnabled(HyperlinkRenderer.class)) {
            LogUtil.fine(HyperlinkRenderer.class, msg);
        }
    }
}
