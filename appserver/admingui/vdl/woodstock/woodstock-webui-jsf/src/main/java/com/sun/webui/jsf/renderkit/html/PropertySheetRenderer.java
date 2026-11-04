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

import com.sun.webui.html.HTMLAttributes;
import com.sun.webui.html.HTMLElements;
import com.sun.webui.jsf.component.Anchor;
import com.sun.webui.jsf.component.Icon;
import com.sun.webui.jsf.component.IconHyperlink;
import com.sun.webui.jsf.component.Legend;
import com.sun.webui.jsf.component.PropertySheet;
import com.sun.webui.jsf.component.PropertySheetSection;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Renders a PropertySheet component.
 */
@com.sun.faces.annotation.Renderer(
        @com.sun.faces.annotation.Renderer.Renders(
                componentFamily = "com.sun.webui.jsf.PropertySheet"))
public final class PropertySheetRenderer extends jakarta.faces.render.Renderer {

    /**
     * Jump to section tool-tip.
     */
    public static final String JUMPTOSECTIONTOOLTIP
            = "propertySheet.jumpToSectionTooltip";

    /**
     * Jump to top tool-tip.
     */
    public static final String JUMPTOTOPTOOLTIP
            = "propertySheet.jumpToTopTooltip";

    /**
     * Jump to top property.
     */
    public static final String JUMPTOTOP
            = "propertySheet.jumpToTop";

    /**
     * Creates a new instance of PropertySheetRenderer.
     */
    public PropertySheetRenderer() {
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        if (context == null || component == null) {
            throw new NullPointerException();
        }
        if (!component.isRendered()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        PropertySheet propertySheet = (PropertySheet) component;

        // Get the theme
        Theme theme = ThemeUtilities.getTheme(context);

        writer.startElement(HTMLElements.DIV, component);
        writer.writeAttribute(HTMLAttributes.ID,
                component.getClientId(context), "id");

        String propValue = RenderingUtilities.getStyleClasses(context,
                component, theme.getStyleClass(ThemeStyles.PROPERTY_SHEET));
        writer.writeAttribute(HTMLAttributes.CLASS, propValue, null);

        propValue = propertySheet.getStyle();
        if (propValue != null) {
            writer.writeAttribute(HTMLAttributes.STYLE, propValue,
                    HTMLAttributes.STYLE);
        }

        renderJumpLinks(context, propertySheet, theme, writer);
        renderRequiredFieldsLegend(context, propertySheet, theme, writer);
        renderPropertySheetSections(context, propertySheet, theme, writer);
        writer.endElement(HTMLElements.DIV);
    }

    /**
     * Render the property sheet sections.
     *
     * @param context The current FacesContext
     * @param propertySheet The PropertySheet object to render
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderPropertySheetSections(final FacesContext context,
            final PropertySheet propertySheet, final Theme theme,
            final ResponseWriter writer) throws IOException {

        // From propertysheetsection template
        //
        // <!-- Separator before Section (except first one, unless jumplinks
        // are rendered) -->
        // This has to be done here since we know if jumpllinks were
        // rendered, not the section. There was probably a request map
        // attribute set to convey this. We control the spacer too.
        //
        List sections = propertySheet.getVisibleSections();
        boolean haveJumpLinks = propertySheet.isJumpLinks()
                && sections.size() > 1;
        boolean renderSpacer = false;
        for (Object section : sections) {

            renderAnchor(context, (PropertySheetSection) section, writer);

            // Orginally the spacer came after the section's
            // opening div. Let's do it before. It should be equivalent.
            // And the PropertySheet should control separators.
            //
            // If there are jumplinks render a spacer if there is more
            // than one section.
            // If there are no jumplinks, render a spacer unless
            // it is the first section.
            if (haveJumpLinks || renderSpacer) {
                renderSpacer(context, (PropertySheetSection) section, theme,
                        writer);
            } else {
                renderSpacer = true;
            }
            RenderingUtilities.renderComponent((UIComponent) section, context);

            if (haveJumpLinks && sections.size() > 1) {
                renderJumpToTopLink(context, propertySheet, theme, writer);
            }
        }
    }

    /**
     * Render a required fields legend. If
     * {@code propertySheet.getRequiredFields} returns null a spacer is
     * rendered.
     *
     * @param context The current FacesContext
     * @param propertySheet The PropertySheet object to render
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    protected void renderRequiredFieldsLegend(final FacesContext context,
            final PropertySheet propertySheet, final Theme theme,
            final ResponseWriter writer) throws IOException {

        // This should be a facet.
        String requiredFields = propertySheet.getRequiredFields();
        // Why isn't this boolean ?
        if (requiredFields != null
                && requiredFields.equalsIgnoreCase("true")) {
            Legend legend = new Legend();
            legend.setId(propertySheet.getId() + "_legend");
            legend.setStyleClass(
                    theme.getStyleClass(ThemeStyles.CONTENT_REQUIRED_DIV));
            RenderingUtilities.renderComponent(legend, context);
        } else {
            // FIXME : Needs to be theme.
            Icon spacer = ThemeUtilities.getIcon(theme, ThemeImages.DOT);
            spacer.setHeight(20);
            spacer.setWidth(1);
            RenderingUtilities.renderComponent(spacer, context);
        }
    }

    /**
     * Render a set of jump links.
     *
     * @param context The current FacesContext
     * @param propertySheet The PropertySheet object to render
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    protected void renderJumpLinks(final FacesContext context,
            final PropertySheet propertySheet, final Theme theme,
            final ResponseWriter writer) throws IOException {

        // Don't render any jump links if they are not requested.
        if (!propertySheet.isJumpLinks()) {
            return;
        }
        // Or if there are no visible sections
        List sections = propertySheet.getVisibleSections();
        int numSections = sections.size();
        if (numSections <= 1) {
            return;
        }

        // There seems to be a distinction if there are 4, 5 to 9,
        // and greater than 9 property sheet sections. This should be a
        // theme configurable parameter.
        //
        // If there are less than 5 sections, there will be
        // 2 jump links per row.
        // If there are greater than 5 sections there will be
        // 3 junmp links per row.
        // If there are more than 10 sections, there will be
        // 4 jump links per row.
        //
        // Determine the number of sections
        //
        // Start the layout for the property sheet sections
        // jump link area
        int jumpLinksPerRow;
        if (numSections < 5) {
            jumpLinksPerRow = 5;
        } else if (numSections > 4 && numSections < 10) {
            jumpLinksPerRow = 3;
        } else {
            jumpLinksPerRow = 4;
        }

        // Start a div for the jump links table
        writer.startElement(HTMLElements.DIV, propertySheet);
        writer.writeAttribute(HTMLAttributes.CLASS,
                theme.getStyleClass(ThemeStyles.CONTENT_JUMP_SECTION_DIV),
                null);

        writer.startElement(HTMLElements.TABLE, propertySheet);
        writer.writeAttribute(HTMLAttributes.BORDER, 0, null);
        writer.writeAttribute(HTMLAttributes.CELLSPACING, 0, null);
        writer.writeAttribute(HTMLAttributes.CELLPADDING, 0, null);
        writer.writeAttribute(HTMLAttributes.TITLE, "", null);

        // Optimize, just get the needed selectors once.
        String jumpLinkDivStyle
                = theme.getStyleClass(ThemeStyles.CONTENT_JUMP_LINK_DIV);
        String jumpLinkStyle
                = theme.getStyleClass(ThemeStyles.JUMP_LINK);

        Iterator sectionIterator = sections.iterator();
        while (sectionIterator.hasNext()) {

            writer.startElement(HTMLElements.TR, propertySheet);
            for (int i = 0; i < jumpLinksPerRow; ++i) {

                PropertySheetSection section
                        = (PropertySheetSection) sectionIterator.next();

                writer.startElement(HTMLElements.TD, propertySheet);
                writer.startElement(HTMLElements.SPAN, propertySheet);
                writer.writeAttribute(HTMLAttributes.CLASS, jumpLinkDivStyle,
                        null);

                IconHyperlink jumpLink = new IconHyperlink();
                jumpLink.setId(section.getId() + "_jumpLink");
                jumpLink.setParent(propertySheet);
                jumpLink.setIcon(ThemeImages.HREF_ANCHOR);
                jumpLink.setBorder(0);
                // Shouldn't this come from the section ?
                String propValue = theme.getMessage(JUMPTOSECTIONTOOLTIP);
                if (propValue != null) {
                    jumpLink.setAlt(propValue);
                    jumpLink.setToolTip(propValue);
                }

                propValue = section.getLabel();
                if (propValue != null) {
                    jumpLink.setText(propValue);
                }
                jumpLink.setUrl("#_" + section.getId());
                jumpLink.setStyleClass(jumpLinkStyle);

                // Render the jump link
                RenderingUtilities.renderComponent(jumpLink, context);

                writer.endElement(HTMLElements.SPAN);
                writer.endElement(HTMLElements.TD);

                // If we haven't created enough cells, we should.
                if (!sectionIterator.hasNext()) {
                    while (++i < jumpLinksPerRow) {
                        writer.startElement(HTMLElements.TD, propertySheet);
                        writer.startElement(HTMLElements.SPAN, propertySheet);
                        writer.writeAttribute(HTMLAttributes.CLASS,
                                jumpLinkDivStyle, null);
                        writer.endElement(HTMLElements.SPAN);
                        writer.endElement(HTMLElements.TD);
                    }
                    break;
                }
            }
            writer.endElement(HTMLElements.TR);
        }
        writer.endElement(HTMLElements.TABLE);
        writer.endElement(HTMLElements.DIV);
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {

        if (context == null || component == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Render an anchor to the section.
     *
     * @param context The current FacesContext
     * @param propertySheetSection The PropertySheetSection about to be
     * rendered.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    private void renderAnchor(final FacesContext context,
            final PropertySheetSection propertySheetSection,
            final ResponseWriter writer) throws IOException {

        Anchor anchor = new Anchor();
        anchor.setParent(propertySheetSection);
        anchor.setId("_" + propertySheetSection.getId());
        RenderingUtilities.renderComponent(anchor, context);
    }

    /**
     * Render a spacer before the section.
     *
     * @param context The current FacesContext
     * @param propertySheetSection The PropertySheetSection about to be
     * rendered.
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    private void renderSpacer(final FacesContext context,
            final PropertySheetSection propertySheetSection,
            final Theme theme, final ResponseWriter writer)
            throws IOException {

        Icon spacer = ThemeUtilities.getIcon(theme, ThemeImages.DOT);
        writer.startElement(HTMLElements.DIV, propertySheetSection);
        writer.writeAttribute(HTMLAttributes.CLASS,
                theme.getStyleClass(ThemeStyles.CONTENT_LIN), null);

        spacer.setId(propertySheetSection.getId() + "_dot1");
        spacer.setParent(propertySheetSection);
        spacer.setHeight(1);
        spacer.setWidth(1);
        RenderingUtilities.renderComponent(spacer, context);
        writer.endElement(HTMLElements.DIV);
    }

    /**
     * Render the back to top link.
     *
     * @param context The current FacesContext
     * @param propertySheet The PropertySheet being rendered
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    private void renderJumpToTopLink(final FacesContext context,
            final PropertySheet propertySheet, final Theme theme,
            final ResponseWriter writer) throws IOException {

        writer.startElement(HTMLElements.DIV, propertySheet);
        writer.writeAttribute(HTMLAttributes.CLASS,
                theme.getStyleClass(ThemeStyles.CONTENT_JUMP_TOP_DIV), null);

        // Should be facets ?
        IconHyperlink jumpLink = new IconHyperlink();
        jumpLink.setIcon(ThemeImages.HREF_TOP);
        jumpLink.setBorder(0);
        // Shouldn't this come from the section ?
        String propValue = theme.getMessage(JUMPTOTOPTOOLTIP);
        if (propValue != null) {
            jumpLink.setAlt(propValue);
            jumpLink.setToolTip(propValue);
        }
        propValue = theme.getMessage(JUMPTOTOP);
        if (propValue != null) {
            jumpLink.setText(propValue);
        }
        jumpLink.setUrl("#");
        jumpLink.setStyleClass(theme.getStyleClass(
                ThemeStyles.JUMP_TOP_LINK));

        // Render the jump link
        RenderingUtilities.renderComponent(jumpLink, context);
        writer.endElement(HTMLElements.DIV);
    }
}
