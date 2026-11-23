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
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import java.io.IOException;

/**
 * RowColumnRenderer renders a HTML table. The contents are rendered by a
 * subclass during the renderCellContent method, implemented in a subclass. An
 * example of the HTML produced when the subclass renderer is a
 * {@link RadionButtonGroup}.
 * <p>
 * &lt;table class=" RbGrp" style=""&gt; &lt;tbody&gt; &lt;tr
 * class="RbGrpRwOd"&gt; &lt;td class="RbGrpCpt"&gt; &lt;label class="RbGrpLbl"
 * for="form1:_id1_0_input" title="rbgrp-tooltip"
 * id="form1:_id1_caption"&gt;Radio Button Group&lt;/label&gt; &lt;/td&gt;
 * &lt;td class="RbGrpClEv"&gt; &lt;input tabindex="1" class="Rb" value="Server"
 * name="form1:_id1" id="form1:_id1_0_input" type="radio"&gt; &lt;img
 * class="RbImg" src="tree_server.gif" id="form1:_id1_0_image"&gt; &lt;label
 * class="RbLbl LblLev3Txt" for="form1:_id1_0_input"
 * id="form1:_id1_0_label"&gt;Server&lt;/label&gt; &lt;/td&gt; &lt;td
 * class="RbGrpClOd"&gt; &lt;input tabindex="1" class="Rb" value="Volume"
 * name="form1:_id1" id="form1:_id1_1_input" type="radio"&gt; &lt;img
 * class="RbImg" src="volumegroup_tree.gif" id="form1:_id1_1_image"&gt;
 * &lt;label class="RbLbl LblLev3Txt" for="form1:_id1_1_input"
 * id="form1:_id1_1_label"&gt;Volume&lt;/label&gt; &lt;/td&gt; &lt;/tr&gt;
 * &lt;tr class="RbGrpRwEv"&gt; &lt;td&gt; &lt;/td&gt; &lt;td
 * class="RbGrpClEv"&gt; &lt;input tabindex="1" class="Rb" checked="checked"
 * value="Pool" name="form1:_id1" id="form1:_id1_2_input" type="radio"&gt;
 * &lt;img class="RbImg" src="pool_tree.gif" id="form1:_id1_2_image"&gt;
 * &lt;label class="RbLbl LblLev3Txt" for="form1:_id1_2_input"
 * id="form1:_id1_2_label"&gt;Pool&lt;/label&gt; &lt;/td&gt; &lt;td
 * class="RbGrpClEv"&gt; &lt;/td&gt; &lt;/tr&gt; &lt;/tbody&gt; &lt;/table&gt;
 * </p>
 * The style class selectors are not output by {@code RowColumnRenderer}
 * but by a subclass.
 */
abstract class RowColumnRenderer extends AbstractRenderer {

    /**
     * Constant indicating a TABLE element style selector is desired.
     */
    protected static final int TABLE_STYLE = 0;

    /**
     * Constant indicating a TD element style is desired. This selector is used
     * for the TD element that contains the group label.
     */
    protected static final int CAPTION_STYLE = 1;

    /**
     * Constant indicating a TR element style selector is desired. The selector
     * is applied to an odd row.
     */
    protected static final int ROWEVEN_STYLE = 2;

    /**
     * Constant indicating a TR element style selector is desired. The selector
     * is applied to an even row.
     */
    protected static final int ROWODD_STYLE = 3;

    /**
     * Constant indicating a TD element style selector is desired. The selector
     * is applied to an even column.
     */
    protected static final int CELLEVEN_STYLE = 4;

    /**
     * Constant indicating a TD element style selector is desired. The selector
     * is applied to an odd column.
     */
    protected static final int CELLODD_STYLE = 5;

    /**
     * Create a RowColumnRenderer instance.
     */
    RowColumnRenderer() {
        super();
    }

    /**
     * Called from a subclass when rendering is to begin.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code RadioButtonGroup} component rendered
     * @param theme the current theme
     * @param writer {@code ResponseWriter} to which the HTML is rendered
     * @param rows the number of rows to render
     * @param columns the number of columns to render
     * @throws IOException if an IO error occurs
     */
    protected void renderRowColumnLayout(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final int rows, final int columns)
            throws IOException {

        writer.startElement(HTMLElements.TABLE, component);
        writer.writeAttribute(HTMLAttributes.ID,
                component.getClientId(context), HTMLAttributes.ID);

        // Set the CSS table style
        writeStyleAttribute(component, writer, null);

        // Set the class attribute.
        // Include the hidden attribute
        String styles = RenderingUtilities.getStyleClasses(context, component,
                getRowColumnStyle(theme, TABLE_STYLE));
        if (styles != null) {
            writer.writeAttribute(HTMLAttributes.CLASS, styles, null);
        }

        // Assume contained elements inherit from these
        // span attributes
        addStringAttributes(context, component, writer,
                I18N_ATTRIBUTES);

        // mbohm 6300361,6300362
        // commenting out this call to addStringAttributes
        // see RadioButtonGroupRenderer.getSelectorComponent and
        // CheckboxGroupRenderer.getSelectorComponent
        // addStringAttributes(context, component, writer,
        // EVENTS_ATTRIBUTES);
        renderRows(context, component, theme, writer, rows, columns);
        writer.endElement(HTMLElements.TABLE);

    }

    /**
     * Render the rows of the table.
     * @param context faces context
     * @param component UI component
     * @param theme the current theme
     * @param writer writer to use
     * @param rows rows to render
     * @param columns column index
     * @throws IOException if an IO error occur
     */
    private void renderRows(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final int rows, final int columns)
            throws IOException {

        // Create a cell for the caption
        int row;
        writer.startElement(HTMLElements.TR, component);
        writer.writeAttribute(HTMLAttributes.CLASS,
                getRowColumnStyle(theme, ROWODD_STYLE), null);

        writer.startElement(HTMLElements.TD, component);
        String style = getRowColumnStyle(theme, CAPTION_STYLE);
        if (style != null) {
            writer.writeAttribute(HTMLAttributes.CLASS, style, null);
        }
        renderCaption(context, component, theme, writer);
        writer.endElement(HTMLElements.TD);

        int itemN = 0;
        for (row = 1; row <= rows; ++row) {

            for (int column = 0; column < columns; ++column) {

                writer.startElement(HTMLElements.TD, component);

                // Use "1" based cells so that the first cell is odd
                // vs. "0th" cell which would be even
                //
                // Don't inlcude the hidden attribute
                String styles;
                if ((column & 0x00000001) == 0) {
                    styles = getRowColumnStyle(theme, CELLEVEN_STYLE);
                } else {
                    styles = getRowColumnStyle(theme, CELLODD_STYLE);
                }
                if (styles != null) {
                    writer.writeAttribute(HTMLAttributes.CLASS, styles, null);
                }

                renderCellContent(context, component, theme, writer, itemN);
                ++itemN;

                writer.endElement(HTMLElements.TD);
            }
            writer.endElement(HTMLElements.TR);

            // Don't start any more rows if the loop is ending
            if (row + 1 <= rows) {
                writer.startElement(HTMLElements.TR, component);
                // Use "1" based rows so that the first row is odd
                // vs. "0th" row which would be even
                String styles;
                if ((row & 0x00000001) == 0) {
                    styles = getRowColumnStyle(theme, ROWEVEN_STYLE);
                } else {
                    styles = getRowColumnStyle(theme, ROWODD_STYLE);
                }
                if (styles != null) {
                    writer.writeAttribute(HTMLAttributes.CLASS, styles,
                            HTMLAttributes.CLASS);
                }
                writer.startElement(HTMLElements.TD, component);
                writer.endElement(HTMLElements.TD);
            }

        }

        // FIXME
        // Need to know if rows can ever be zero.
        // If rows is 0 then this is the only time this
        // row needs to be terminated.
        if (rows == 0) {
            // End the row started outside the loop
            writer.endElement(HTMLElements.TR);
        }
    }

    /**
     * Implemented by a subclass. Called when subclass should render the
     * contents of the cell for the {@code itemN}'th renderer cell.
     *
     * @param context {@code FacesContext} for the current request
     * @param component component being rendered
     * @param theme the current theme
     * @param writer {@code ResponseWriter} to which the HTML is rendered
     * @param itemN the nth cell to be rendered.
     * @throws IOException if an IO error occurs
     */
    protected abstract void renderCellContent(FacesContext context,
            UIComponent component, Theme theme, ResponseWriter writer,
            int itemN) throws IOException;

    /**
     * Implemented by a subclass. Called when the subclass should render the
     * CAPTION element for the table.
     *
     * @param context {@code FacesContext} for the current request
     * @param component component being rendered
     * @param theme the current theme
     * @param writer {@code ResponseWriter} to which the HTML is rendered
     * @throws IOException if an IO error occurs
     */
    protected abstract void renderCaption(FacesContext context,
            UIComponent component, Theme theme, ResponseWriter writer)
            throws IOException;

    /**
     * Get the style class for a structural element of the table.
     * {@code styleCode} is one of.
     *
     * <ul>
     * <li>{@code TABLE_STYLE}</li>
     * <li>{@code CAPTION_STYLE}</li>
     * <li>{@code ROWEVEN_STYLE}</li>
     * <li>{@code ROWODD_STYLE}</li>
     * <li>{@code CELLEVEN_STYLE}</li>
     * <li>{@code CELLODD_STYLE}</li>
     * </ul>
     *
     *
     * @param theme the current theme
     * @param styleCode one the predefined constants.
     * @return String
     */
    protected abstract String getRowColumnStyle(Theme theme,
            int styleCode);

    /**
     * Write the style attribute.
     * @param component UI component
     * @param writer writer to use
     * @param style CSS style
     * @throws IOException if an IO error occurs
     */
    private void writeStyleAttribute(final UIComponent component,
            final ResponseWriter writer, final String style)
            throws IOException {

        StringBuilder styleBuf = new StringBuilder();
        String compStyle = (String) component.getAttributes().get("style");
        if (compStyle != null) {
            styleBuf.append(compStyle);
        }
        if (style != null) {
            if (styleBuf.length() != 0) {
                styleBuf.append(" ");
            }
            styleBuf.append(style);
        }
        if (styleBuf.length() != 0) {
            writer.writeAttribute(HTMLAttributes.STYLE, styleBuf.toString(),
                    HTMLAttributes.STYLE);
        }
    }
}
