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
import com.sun.webui.html.HTMLAttributes;
import com.sun.webui.html.HTMLElements;
import com.sun.webui.jsf.component.Button;
import com.sun.webui.jsf.component.IFrame;
import com.sun.webui.jsf.component.Icon;
import com.sun.webui.jsf.component.ImageComponent;
import com.sun.webui.jsf.component.VersionPage;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * Renders a version page.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.VersionPage"))
public final class VersionPageRenderer extends AbstractRenderer {

    /**
     * Default height and width for the image.
     */
    private static final String DEFAULT_VERSIONINFO_HEIGHT = "330";

    /**
     * Creates a new instance of VersionPageRenderer.
     */
    public VersionPageRenderer() {
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        if (context == null || component == null || writer == null) {
            throw new NullPointerException();
        }

        VersionPage versionPage = (VersionPage) component;
        // Get the theme
        Theme theme = ThemeUtilities.getTheme(context);

        String style = versionPage.getStyle();
        writer.startElement(HTMLElements.DIV, versionPage);
        writer.writeAttribute(HTMLAttributes.ID,
                versionPage.getClientId(context), HTMLAttributes.ID);

        if (style != null) {
            writer.writeAttribute(HTMLAttributes.STYLE, style, null);
        }
        String versionBodyStyle = theme.getStyleClass(ThemeStyles.VERSION_BODY);
        String styleClass = RenderingUtilities.getStyleClasses(context,
                component, versionBodyStyle);
        if (styleClass != null) {
            writer.writeAttribute(HTMLAttributes.CLASS, styleClass, null);
        }

        // Render the masthead
        renderVersionMasthead(context, versionPage, writer, theme);

        style = theme.getStyleClass(ThemeStyles.VERSION_MARGIN);
        writer.startElement(HTMLElements.DIV, versionPage);
        writer.writeAttribute(HTMLAttributes.CLASS, style, null);

        // Render the version number, legal notice, close button
        renderVersionInformation(context, versionPage, writer, theme);
        writer.endElement(HTMLElements.DIV);
        renderCloseButton(context, versionPage, writer, theme);
        writer.endElement(HTMLElements.DIV);
    }

    /**
     * Renders the version masthead at the top of the version page.
     *
     * @param context The current FacesContext
     * @param versionPage The VersionPage object to use
     * @param writer The current ResponseWriter
     * @param theme The current theme
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderVersionMasthead(final FacesContext context,
            final VersionPage versionPage, final ResponseWriter writer,
            final Theme theme) throws IOException {

        // Get the facet with the product name image, logo, etc.
        UIComponent mhFacet = versionPage.getFacet("identityContent");

        if (mhFacet != null) {
            // Render the contents of the facet
            RenderingUtilities.renderComponent(mhFacet, context);
        } else {
            renderVersionMastheadImages(context, versionPage, theme, writer);
        }
    }

    /**
     * Renders the images in the version page masthead.
     *
     * @param context The current FacesContext
     * @param versionPage The VersionPage object to use
     * @param corplogo The java logo icon to render
     * @param writer The current ResponseWriter
     * @param theme The current theme
     * @deprecated
     * @see #renderVersionMastheadImages(FacesContext, VersionPage, Theme,
     * ResponseWriter)
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderVersionMastheadImages(final FacesContext context,
            final VersionPage versionPage, final Icon corplogo,
            final ResponseWriter writer, final Theme theme)
            throws IOException {

        renderVersionMastheadImages(context, versionPage, theme, writer);
    }

    /**
     * Render the images in the verion page masthead. Calls
     * {@code getProductImage} and {@code getCorporateLogo} to obtain
     * images for the masthead. This method lays out the surround HTML even if
     * there is no product image. However if there is no corporate logo, the
     * surrounding HTML is not rendered.
     *
     * @param context The current FacesContext
     * @param versionPage The VersionPage object to use
     * @param theme The current theme
     * @param writer The current ResponseWriter
     * @exception IOException if an input/output error occurs
     */
    protected void renderVersionMastheadImages(final FacesContext context,
            final VersionPage versionPage, final Theme theme,
            final ResponseWriter writer) throws IOException {

        // Render the version brand image
        String style = theme.getStyleClass(ThemeStyles.VERSION_MASTHEAD_BODY);
        writer.startElement(HTMLElements.DIV, versionPage);
        writer.writeAttribute(HTMLAttributes.CLASS, style, null);
        writer.writeText("\n", null);

        // Render the product name image
        style = theme.getStyleClass(ThemeStyles.VERSION_PRODUCT_TD);
        writer.startElement(HTMLElements.DIV, versionPage);
        writer.writeAttribute(HTMLAttributes.CLASS, style, null);

        style = theme.getStyleClass(ThemeStyles.VERSION_PRODUCT_DIV);
        writer.startElement(HTMLElements.DIV, versionPage);
        writer.writeAttribute(HTMLAttributes.CLASS, style, null);

        // Instantiate the productname image and render it
        String id = versionPage.getId();
        int size = 0;
        String altText = null;

        UIComponent image = getProductImage(context, versionPage, theme);
        if (image != null) {
            RenderingUtilities.renderComponent(image, context);
        }

        writer.endElement(HTMLElements.DIV);
        writer.endElement(HTMLElements.DIV);

        // Render the Corporate logo
        //
        image = getCorporateLogo(context, versionPage, theme);
        if (image != null) {

            style = theme.getStyleClass(ThemeStyles.VERSION_LOGO_TD);
            writer.startElement(HTMLElements.DIV, versionPage);
            writer.writeAttribute(HTMLAttributes.CLASS, style, null);

            style = theme.getStyleClass(ThemeStyles.VERSION_LOGO_DIV);
            writer.startElement(HTMLElements.DIV, versionPage);
            writer.writeAttribute(HTMLAttributes.CLASS, style, null);

            RenderingUtilities.renderComponent(image, context);

            writer.endElement(HTMLElements.DIV);
            writer.endElement(HTMLElements.DIV);
        }

        writer.endElement(HTMLElements.DIV);
    }

    /**
     * Renders the version information based on the attributes specified.
     *
     * @param context The current FacesContext
     * @param versionPage The VersionPage object to use
     * @param writer The current ResponseWriter
     * @param theme The current theme
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderVersionInformation(final FacesContext context,
            final VersionPage versionPage, final ResponseWriter writer,
            final Theme theme) throws IOException {

        String file = versionPage.getVersionInformationFile();
        if (file != null) {
            renderVersionInformationFile(context, versionPage, writer);
        } else {
            renderVersionInformationInline(context, versionPage,
                    writer, theme);
        }
    }

    /**
     * Renders the Version information contained within an HTML file.
     *
     * @param context The current FacesContext
     * @param versionPage The VersionPage object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderVersionInformationFile(final FacesContext context,
            final VersionPage versionPage, final ResponseWriter writer)
            throws IOException {

        IFrame iframe = new IFrame();
        iframe.setId(versionPage.getId()
                + "_versionInformationIFrame");
        iframe.setAlign("left");
        iframe.setHeight(DEFAULT_VERSIONINFO_HEIGHT);
        iframe.setWidth("100%");
        iframe.setScrolling("auto");
        iframe.setUrl(versionPage.getVersionInformationFile());
        RenderingUtilities.renderComponent(iframe, context);
    }

    /**
     * Renders the Version information inline in the version page.
     *
     * @param context The current FacesContext
     * @param versionPage The VersionPage object to use
     * @param writer The current ResponseWriter
     * @param theme The current theme
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderVersionInformationInline(final FacesContext context,
            final VersionPage versionPage, final ResponseWriter writer,
            final Theme theme) throws IOException {

        String text;

        // Render the version number
        String style = theme.getStyleClass(ThemeStyles.VERSION_HEADER_TEXT);
        writer.startElement(HTMLElements.DIV, versionPage);
        writer.writeAttribute(HTMLAttributes.CLASS, style, null);
        text = versionPage.getVersionString();
        if (text != null) {
            writer.writeText(text, "versionString");
        }
        writer.endElement(HTMLElements.DIV);

        // Render the legal notice
        style = theme.getStyleClass(ThemeStyles.VERSION_TEXT);
        writer.startElement(HTMLElements.DIV, versionPage);
        writer.writeAttribute(HTMLAttributes.CLASS, style, null);
        text = versionPage.getCopyrightString();
        if (text != null) {
            // Do not escape (i.e. use writeText), allow <br>'s other HTML
            writer.write(text);
        }
        writer.endElement(HTMLElements.DIV);

    }

    /**
     * Renders the Close button at the bottom of the version page.
     *
     * @param context The current FacesContext
     * @param versionPage The VersionPage object to use
     * @param writer The current ResponseWriter
     * @param theme The current theme
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderCloseButton(final FacesContext context,
            final VersionPage versionPage, final ResponseWriter writer,
            final Theme theme) throws IOException {

        // Instantiate a button
        Button button = new Button();

        button.setId(versionPage.getId()
                + "_versionPageCloseButton");
        button.setText(ThemeUtilities.getTheme(context).
                getMessage("Version.closeButton"));
        button.setPrimary(true);
        button.setOnClick("javascript: parent.close(); return false;");

        String style
                = theme.getStyleClass(ThemeStyles.VERSION_BUTTON_MARGIN_DIV);
        writer.startElement(HTMLElements.DIV, versionPage);
        writer.writeAttribute(HTMLAttributes.CLASS, style, null);

        RenderingUtilities.renderComponent(button, context);

        writer.endElement(HTMLElements.DIV);

        // FIXME: This must not be done here. When the focus changes
        // are put back this must be changed to use
        // RenderingUtilities.setRequestFocusElementId()
        writer.startElement(HTMLElements.SCRIPT, versionPage);
        writer.writeAttribute(HTMLAttributes.TYPE,
                "text/javascript", null);
        writer.write("document.forms[0]." + button.getClientId(context)
                + ".focus()");
        writer.endElement(HTMLElements.SCRIPT);
    }

    /**
     * Returns a component suitable for the corporate logo. This implementation
     * returns an ImageComponent as defined in the theme by
     * {@code ThemeImages.VERSION_CORPLOGO} by calling
     * {@code ThemeUtilities.getImage}. If this theme property is not defined,
     * null is returned. Return a UIComponent suitable to render for the brand
     * image. If the {@code brandImage} facet exists return it, otherwise if the
     * {@code masthead.getBrandImageURL()} exists create a component initialized
     * with appropriate values and return it.
     * <p>
     * In this implementation, if a value for {@code getBrandImageUrl} is not
     * specified it returns an {@code Icon} component, by calling
     * {@code ThemeUtilities.getIcon} with the
     * {@code ThemeImages.MASTHEAD_CORPLOGO} key. If there is no image for this
     * key, return {@code null}
     * </p>
     *
     * @param context faces context
     * @param versionPage version page component
     * @param theme the current theme
     * @return UIComponent
     */
    protected UIComponent getCorporateLogo(final FacesContext context,
            final VersionPage versionPage, final Theme theme) {

        // Check if the javalogo image is defined in the theme
        // If not, assume were using the defaulttheme...
        Icon corpLogo = null;
        try {
            // If there is no path then there is no default.
            //
            String imagePath = theme.getImagePath(ThemeImages.VERSION_CORPLOGO);
            if (imagePath == null) {
                return null;
            }
            corpLogo = ThemeUtilities.getIcon(theme,
                    ThemeImages.VERSION_CORPLOGO);
            if (corpLogo != null) {
                corpLogo.setId(versionPage.getId()
                        + "_versionPageJavaLogo");
                corpLogo.setParent(versionPage);
            }
        } catch (Exception e) {
            // Don't care.
        }
        return corpLogo;
    }

    /**
     * Returns a component suitable for the product image. This implementations
     * returns an {@code ImageComponent} created from the
     * {@code versionPage} properties. If
     * {@code versionPage.getProductImageURL} returns {@code null} or
     * an empty string, null is returned.
     * @param context faces context
     * @param versionPage VersionPage component
     * @param theme the current theme
     * @return UIComponent
     */
    protected UIComponent getProductImage(final FacesContext context,
            final VersionPage versionPage, final Theme theme) {

        String imageAttr = versionPage.getProductImageURL();
        if (imageAttr == null || imageAttr.trim().length() == 0) {
            return null;
        }

        ImageComponent image = new ImageComponent();
        image.setId(versionPage.getId() + "_versionPageProductImage");
        image.setUrl(imageAttr);
        imageAttr = versionPage.getProductImageDescription();
        if (imageAttr != null && imageAttr.trim().length() != 0) {
            image.setAlt(imageAttr);
        }
        int dim = versionPage.getProductImageHeight();
        if (dim > 0) {
            image.setHeight(dim);
        }
        dim = versionPage.getProductImageWidth();
        if (dim > 0) {
            image.setWidth(dim);
        }
        image.setBorder(0);
        return image;
    }
}
