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

 /*
 * PagetitleRenderer.java
 */
package com.sun.webui.jsf.renderkit.html;

import com.sun.faces.annotation.Renderer;
import com.sun.webui.jsf.component.ContentPageTitle;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * Renders a Pagetitle component.
 */
@Renderer(
        @Renderer.Renders(
                componentFamily = "com.sun.webui.jsf.ContentPageTitle"))
public class ContentPageTitleRenderer extends jakarta.faces.render.Renderer {

    /**
     * Bottom id.
     */
    public static final String BOTTOM_ID = "_bottom";

    /**
     * Creates a new instance of PagetitleRenderer.
     */
    public ContentPageTitleRenderer() {
        // default constructor
    }

    /**
     * This implementation returns {@code true}.
     * @return {@code boolean}
     */
    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * This implementation renders the Pagetitle component start.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code EditableValueHolder} component whose
     * submitted value is to be stored
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void encodeBegin(final FacesContext context,
            final UIComponent component) throws IOException {

        ContentPageTitle pagetitle = (ContentPageTitle) component;
        Theme theme = ThemeUtilities.getTheme(context);
        ResponseWriter writer = context.getResponseWriter();
        String style = pagetitle.getStyle();

        writer.startElement("div", pagetitle);
        writer.writeAttribute("id",
                pagetitle.getClientId(context), "id");

        if (style != null) {
            writer.writeAttribute("style", style, null);
        }

        String styleClass = RenderingUtilities.getStyleClasses(context,
                component, null);
        if (styleClass != null) {
            writer.writeAttribute("class", styleClass, null);
        }

        startLayoutTable(writer, pagetitle, "bottom", true, null, "bottom");

        // render the page title itself
        renderPageTitle(context, pagetitle, writer, theme);

        // check for pageButtonsTop facet to use
        UIComponent facet = pagetitle.getFacet("pageButtonsTop");

        if (facet != null && facet.isRendered()) {
            renderPageButtons(context, pagetitle, writer,
                    theme.getStyleClass(ThemeStyles.TITLE_BUTTON_DIV), facet);
        }

        endLayoutTable(writer);
    }

    /**
     * This implementation renders any facets related to the top of the page
     * title will along with the children found in the body content.
     *
     * @param context The current FacesContext
     * @param component The Pagetitle component
     * @throws IOException if an IO error occurs
     */
    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {

        ContentPageTitle pagetitle = (ContentPageTitle) component;
        ResponseWriter writer = context.getResponseWriter();
        Theme theme = ThemeUtilities.getTheme(context);

        // render the page help if any
        if (pagetitle.getHelpText() != null
                || pagetitle.getFacet("pageHelp") != null) {
            renderPageHelp(context, pagetitle, theme, writer);
        }

        boolean showActions = pagetitle.getFacet("pageActions") != null;
        boolean showViews = pagetitle.getFacet("pageViews") != null;

        // render the page actions and views if any
        if (showActions || showViews) {
            String tdAlign = null;

            if (!showActions) {
                // actions not shown, first td align must be right for views
                tdAlign = "right";
            }

            startLayoutTable(writer, pagetitle, "bottom", true, tdAlign,
                    "bottom");

            if (showActions) {
                renderPageActions(context, pagetitle, theme, writer);
            }

            if (showViews) {
                if (showActions) {
                    // actions are displayed, start new td for views
                    writer.startElement("td", pagetitle);
                    writer.writeAttribute("align", "right", null);
                    writer.writeAttribute("valign", "bottom", null);
                    writer.writeAttribute("nowrap", "nowrap", null);
                }
                renderPageViews(context, pagetitle, theme, writer);
            }
            writer.endElement("td");
            endLayoutTable(writer);
        }

        // perform the normal encoding of all other children
        super.encodeChildren(context, component);
    }

    /**
     * Render the pageActions facet.
     *
     * @param context The current FacesContext
     * @param pagetitle The Pagetitle component
     * @param theme the current theme
     * @param writer The current ResponseWriter
     * @throws IOException if an IO error occurs
     */
    protected void renderPageActions(final FacesContext context,
            final ContentPageTitle pagetitle, final Theme theme,
            final ResponseWriter writer) throws IOException {

        UIComponent pageActions = pagetitle.getFacet("pageActions");

        if (pageActions == null) {
            return;
            //throw new NullPointerException("pageActions facet null");
        }

        writer.startElement("div", pagetitle);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.TITLE_ACTION_DIV),
                null);

        RenderingUtilities.renderComponent(pageActions, context);

        writer.endElement("div");
        writer.endElement("td");
    }

    /**
     * Render the pageViews facet.
     *
     * @param context The current FacesContext
     * @param pagetitle The Pagetitle component
     * @param theme the current theme
     * @param writer The current ResponseWriter
     * @throws IOException if an IO error occurs
     */
    protected void renderPageViews(final FacesContext context,
            final ContentPageTitle pagetitle, final Theme theme,
            final ResponseWriter writer) throws IOException {

        UIComponent pageViews = pagetitle.getFacet("pageViews");

        if (pageViews == null) {
            //throw new NullPointerException("pageViews facet null");
            return;
        }

        writer.startElement("div", pagetitle);
        writer.writeAttribute("class", theme.getStyleClass(
                ThemeStyles.TITLE_VIEW_DIV), null);

        RenderingUtilities.renderComponent(pageViews, context);

        writer.endElement("div");
    }

    /**
     * Convenience method to output the start of a layout table.
     *
     * @param writer The current ResponseWriter
     * @param pagetitle The Pagetitle component
     * @param firstRowValign Value to use for the first table row's valign
     * @param noWrapTd nowrap attribute for the table cell
     * @param firstTdAlign Value to use for the first table div's align
     * @param firstTdValign Value to use for the first table div's valign
     * @throws IOException if an IO error occurs
     */
    private void startLayoutTable(final ResponseWriter writer,
            final ContentPageTitle pagetitle, final String firstRowValign,
            final boolean noWrapTd, final String firstTdAlign,
            final String firstTdValign) throws IOException {

        writer.startElement("table", pagetitle);
        writer.writeAttribute("border", "0", null);
        writer.writeAttribute("width", "100%", null);
        writer.writeAttribute("cellpadding", "0", null);
        writer.writeAttribute("cellspacing", "0", null);
        writer.startElement("tr", pagetitle);
        if (firstRowValign != null) {
            writer.writeAttribute("valign", firstRowValign, null);
        }
        writer.startElement("td", pagetitle);
        if (noWrapTd) {
            writer.writeAttribute("nowrap", "nowrap", null);
        }
        if (firstTdAlign != null) {
            writer.writeAttribute("align", firstTdAlign, null);
        }
        if (firstTdValign != null) {
            writer.writeAttribute("valign", firstTdValign, null);
        }
    }

    /**
     * Convenience method to output the end of a layout table.
     *
     * @param writer The current ResponseWriter
     * @throws IOException if an IO error occurs
     */
    private void endLayoutTable(final ResponseWriter writer)
            throws IOException {

        writer.endElement("tr");
        writer.endElement("table");
    }

    /**
     * Render the page title.
     *
     * @param context The current FacesContext
     * @param pagetitle The Pagetitle component
     * @param writer The current ResponseWriter
     * @param theme The current Theme object
     * @throws IOException if an IO error occurs
     */
    protected void renderPageTitle(final FacesContext context,
            final ContentPageTitle pagetitle, final ResponseWriter writer,
            final Theme theme) throws IOException {

        writer.startElement("div", pagetitle);
        String style = theme.getStyleClass(ThemeStyles.TITLE_TEXT_DIV);
        writer.writeAttribute("class", style, null);

        if (pagetitle.getFacet("pageTitle") != null) {
            // if the developer spec'd a pageTitle facet, render it
            RenderingUtilities.renderComponent(pagetitle.getFacet("pageTitle"),
                    context);
        } else {
            String title = pagetitle.getTitle();
            if (title != null) {
                writer.startElement("h1", pagetitle);
                style = theme.getStyleClass(ThemeStyles.TITLE_TEXT);
                writer.writeAttribute("class", style, null);
                writer.write(title);
                writer.endElement("h1");
            }
        }
        writer.endElement("div");
        writer.endElement("td");
    }

    /**
     * Render the page help facet.
     *
     * @param context The current FacesContext
     * @param pagetitle The Pagetitle component
     * @param theme theme
     * @param writer The current ResponseWriter
     * @throws IOException if an IO error occurs
     */
    protected void renderPageHelp(final FacesContext context,
            final ContentPageTitle pagetitle, final Theme theme,
            final ResponseWriter writer) throws IOException {

        startLayoutTable(writer, pagetitle, null, false, null, null);

        writer.startElement("div", pagetitle);
        String style = theme.getStyleClass(ThemeStyles.TITLE_HELP_DIV);
        writer.writeAttribute("class", style, null);

        UIComponent helpFacet = pagetitle.getFacet("pageHelp");

        if (helpFacet != null) {
            RenderingUtilities.renderComponent(helpFacet, context);
        } else {
            writer.startElement("div", pagetitle);
            style = theme.getStyleClass(ThemeStyles.HELP_PAGE_TEXT);
            writer.writeAttribute("class", style, null);
            writer.write(pagetitle.getHelpText());
            writer.endElement("div");
        }
        writer.endElement("div");
        writer.endElement("td");
        endLayoutTable(writer);
    }

    /**
     * Render the pageButtons facet.
     *
     * @param context The current FacesContext
     * @param pagetitle The Pagetitle component
     * @param writer The current ResponseWriter
     * @param divStyle The style class name to use for the div enclosing the
     * button facet
     * @param buttonFacet button component
     * @throws IOException if an IO error occurs
     */
    protected void renderPageButtons(final FacesContext context,
            final ContentPageTitle pagetitle, final ResponseWriter writer,
            final String divStyle, final UIComponent buttonFacet)
            throws IOException {

        if (buttonFacet == null) {
            // throw new NullPointerException("pageButtons facet null");
            return;
        }

        writer.startElement("td", pagetitle);
        writer.writeAttribute("align", "right", null);
        writer.writeAttribute("nowrap", "nowrap", null);
        writer.writeAttribute("valign", "bottom", null);
        writer.startElement("div", pagetitle);
        writer.writeAttribute("class", divStyle, null);
        RenderingUtilities.renderComponent(buttonFacet, context);
        writer.endElement("div");
        writer.endElement("td");
    }

    /**
     * Render the Pagetitle component end.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code EditableValueHolder} component whose
     * submitted value is to be stored
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        ContentPageTitle pagetitle = (ContentPageTitle) component;
        Theme theme = ThemeUtilities.getTheme(context);
        ResponseWriter writer = context.getResponseWriter();
        // check for bottom page buttons facet
        UIComponent facet = pagetitle.getFacet("pageButtonsBottom");

        // test if the bottom page buttons need to be rendered
        if (facet != null && facet.isRendered()) {
            if (pagetitle.isSeparator()) {
                renderPageSeparator(context, pagetitle, theme, writer);
            }
            writer.startElement("table", pagetitle);
            writer.writeAttribute("border", "0", null);
            writer.writeAttribute("width", "100%", null);
            writer.writeAttribute("cellpadding", "0", null);
            writer.writeAttribute("cellspacing", "0", null);

            writer.startElement("tr", pagetitle);
            String style
                    = theme.getStyleClass(ThemeStyles.TITLE_BUTTON_BOTTOM_DIV);

            renderPageButtons(context, pagetitle, writer, style, facet);
            endLayoutTable(writer);
        }
        writer.endElement("div");
    }

    /**
     * Render the page separator.
     *
     * @param context The current FacesContext
     * @param pagetitle The Pagetitle component
     * @param theme the current theme
     * @param writer The current ResponseWriter
     * @exception IOException if an input/output error occurs
     */
    protected void renderPageSeparator(final FacesContext context,
            final ContentPageTitle pagetitle, final Theme theme,
            final ResponseWriter writer) throws IOException {

        UIComponent separatorFacet = pagetitle.getBottomPageSeparator();
        if (separatorFacet != null) {
            RenderingUtilities.renderComponent(separatorFacet, context);
        }
    }

    /**
     * Helper function to render a transparent spacer image.
     *
     * TO DO - move to AbstractRenderer
     *
     * @param writer The current ResponseWriter
     * @param pagetitle The Pagetitle component
     * @param src The value to use for image src attribute
     * @param height The value to use for the image height attribute
     * @param width The value to use for the image width attribute
     * @throws IOException if an IO error occurs
     */
    private void writeDotImage(final ResponseWriter writer,
            final ContentPageTitle pagetitle, final String src,
            final int height, final int width)
            throws IOException {

        writer.startElement("img", pagetitle);
        writer.writeAttribute("src", src, null);
        writer.writeAttribute("alt", "", null);
        writer.writeAttribute("border", "0", null);
        writer.writeAttribute("height", height, null);
        writer.writeAttribute("width", width, null);
        writer.endElement("img");
    }
}
