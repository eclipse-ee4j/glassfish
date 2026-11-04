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
import com.sun.webui.jsf.component.Button;
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.FileChooser;
import com.sun.webui.jsf.component.HelpInline;
import com.sun.webui.jsf.component.Icon;
import com.sun.webui.jsf.component.Label;
import com.sun.webui.jsf.component.StaticText;
import com.sun.webui.jsf.component.TextField;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ClientSniffer;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.theme.Theme;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.ConverterException;
import jakarta.json.JsonObject;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static com.sun.webui.jsf.util.JavaScriptUtilities.getDomNode;
import static com.sun.webui.jsf.util.JavaScriptUtilities.renderInitScriptTag;
import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;
import static com.sun.webui.jsf.util.RenderingUtilities.renderComponent;
import static com.sun.webui.jsf.util.RenderingUtilities.renderStyleClass;
import static com.sun.webui.jsf.util.ThemeUtilities.getIcon;
import static com.sun.webui.jsf.util.ThemeUtilities.getTheme;

/**
 * Renderer for a {@link com.sun.webui.jsf.component.FileChooser} component.
 * <p>
 * The FileChooser renders the following logical elements to effect
 * the FileChooser behavior.
 * </p>
 * <ul>
 * <li>An input field to accept and display the current open folder path.</li>
 * <li>An input field to accept and display a string used to filter the
 * contents of the current open folder.</li>
 * <li>An input field to accept and display the files to select.</li>
 * <li>A list box to display the contents of the current open folder.</li>
 * <li>A drop down menu of sort options to control the order of the
 * open folder's contents.</li>
 * <li>An open folder button to open and display a selected folder in the
 * list box.</li>
 * <li>A move up button to navigate and display the currently open folder's
 * parent folder.</li>
 * </ul>
 * <p>
 * The expected submitted value is an array of Strings. If there are
 * selections a request parameter called {@code <clientId>:<id>_selections}
 * where {@code clientId} is the client id of the FileChooser component
 * and {@code id} is the component id of the FileChooser component.
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.FileChooser"))
public class FileChooserRenderer extends AbstractRenderer {

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * The "-" string constant.
     */
    public static final String HYFEN = "-";

    /**
     * Creates a new instance of FileChooserRenderer.
     */
    public FileChooserRenderer() {
    }

    /**
     * This implementation decodes the user input.
     * @param context faces context
     * @param component UI component
     */
    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        if (DEBUG) {
            log("decode(context, component)");
        }
        if (isDisabled(component) || isReadOnly(component)) {
            if (DEBUG) {
                log("component is readonly...");
            }
            return;
        }

        if (!(component instanceof FileChooser)) {
            throw new FacesException("FileChooserRenderer can only "
                    + "render FileChooser components.");
        }
        FileChooser chooser = (FileChooser) component;

        // If the submitted value is not null then the
        // selectFieldValidator has decodeed the selections and
        // set the file chooser's submitted value.
        // All future processing should occur as if the value was
        // decoded here.
        if (chooser.getSubmittedValue() == null) {
            decodeSubmittedValue(context, chooser);
        }
    }

    /**
     * The hidden field is encoded with an operation and the
     * selections separated by ","
     *
     * This must be reworked to not depend on an operation
     * and a delimiter. Use a select element so that named
     * array can returned in the request.
     * @param context faces context
     * @param chooser file chooser
     */
    private void decodeSubmittedValue(final FacesContext context,
            final FileChooser chooser) {

        // Look for the submitted dynamically created select element.
        // This will be set for the
        //
        // dblclicked - dblclick a file selection in file chooser mode
        // selectedFileEntered - selectFileField enter pressed
        //     in file mode and folder mode
        //
        // These are the selection actions
        // If not set no selection has been made and effectively
        // no change, as far as file choosers value.
        //
        // Probable want to always create the hidded select element
        // to reduce dependence on javascript.
        String selectionsId = chooser.getClientId(context) + ":"
                + chooser.getId() + "_selections";

        Map requestParameters =
                context.getExternalContext().getRequestParameterValuesMap();

        String[] selections = (String[]) requestParameters.get(selectionsId);
        if (selections != null) {
            chooser.setSubmittedValue(selections);
        } else {
            chooser.setSubmittedValue(null);
        }
    }

    /**
     * This implementation of getConvertedValue calls back into the component's
     * getConvertedValue.
     *
     * @param context faces context
     * @param component UI component
     * @param submittedValue value to convert
     * @return String
     * @throws ConverterException if a conversion error occurs
     */
    @Override
    public java.lang.Object getConvertedValue(final FacesContext context,
            final UIComponent component, final Object submittedValue)
            throws ConverterException {

        // This implementation of getConvertedValue calls back into
        // the component's getConvertedValue. We do this because
        // we are also writing the renderer and it is more convenient
        // and useful to place the real implementation in the
        // component. However if someone else writes a renderer
        // they may want to implement it. Therefore the component
        // will call the renderer's getConveredValue first if
        // a renderer is configured, which in effect will be a call
        // back to an overloaded FileChooser.getConvertedValue().
        //
        // public Object getConvertedValue(FacesContext, FileChooser, Object);
        if (!(component instanceof FileChooser)) {
            String msg = "Can only convert values for FileChooser component.";
            throw new ConverterException(msg);
        }
        return ((FileChooser) component).getConvertedValue(context,
                (FileChooser) component, submittedValue);
    }

    /**
     * This implementation is empty.
     * @param context faces context
     * @param component UI component
     * @throws IOException if an IO error occurs
     */
    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component)
            throws IOException {
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
     * This implementation renders the component.
     * @param context faces context
     * @param component UI component
     * @param writer writer to use
     * @throws IOException if an IO error occurs
     */
    @Override
    @SuppressWarnings("checkstyle:methodlength")
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        if (component == null) {
            return;
        }

        try {
            FileChooser chooser = null;
            if (component instanceof FileChooser) {
                chooser = (FileChooser) component;
            } else {
                String message = "Component " + component.toString()
                        + " has been associated with a FileChooser. "
                        + " This renderer can only be used by components "
                        + " that extend"
                        + " com.sun.webui.jsf.component.FileChooser.";
                throw new FacesException(message);
            }

            if (!chooser.isRendered()) {
                return;
            }

            Theme theme = getTheme(context);

            // start the div the entire filechooser is wrapped in
            writer.startElement("div", component);
            writer.writeAttribute("id", component.getClientId(context), null);

            String style = chooser.getStyle();
            if (style != null) {
                writer.writeAttribute("style", style, null);
            }

            renderStyleClass(context, writer, component, null);
            writer.writeText("\n", null);

            // render title in a table
            renderChooserTitle(context, chooser, writer, theme);

            // render file chooser in a div
            writer.startElement("div", component);
            writer.writeAttribute("class",
                    theme.getStyleClass(ThemeStyles.FILECHOOSER_CONMGN), null);
            writer.writeText("\n", null);
            writer.startElement("table", chooser);
            writer.writeAttribute("border", "0", "border");
            writer.writeAttribute("cellpadding", "0", "cellpadding");
            writer.writeAttribute("cellspacing", "0", "cellspacing");
            writer.writeAttribute("title", " ", "title");
            writer.writeText("\n", null);
            writer.startElement("tr", chooser);
            writer.startElement("td", chooser);

            // check if facets are available and if not add a default one.

            // renderEmptyLine(context, chooser, writer, null, 1, 20, theme);
            renderServerName(context, chooser, writer, theme);
            renderClearDiv(context, chooser, writer, theme);
            renderLookinTextField(context, chooser, writer, theme);
            renderClearDiv(context, chooser, writer, theme);
            renderFilterField(context, chooser, writer, theme);
            renderClearDiv(context, chooser, writer, theme);

            String helpMessage = theme.getMessage("filechooser.enterKeyHelp");
            renderInlineHelp(context, chooser, writer, helpMessage, theme);
            renderClearDiv(context, chooser, writer, theme);
            writer.endElement("td");
            writer.endElement("tr");
            writer.writeText("\n", null);

            writer.startElement("tr", chooser);
            writer.startElement("td", chooser);
            writer.startElement("hr", chooser);
            writer.writeAttribute("color", "#98a0a5", null);
            writer.writeAttribute("size", "1", null);
            writer.endElement("hr");
            writer.endElement("td");
            writer.endElement("tr");
            writer.writeText("\n", null);

            writer.startElement("tr", chooser);
            writer.startElement("td", chooser);
            renderButtons(context, chooser, writer, theme);
            renderSortFields(context, chooser, writer, theme);
            writer.endElement("td");
            writer.endElement("tr");
            writer.writeText("\n", null);

            writer.startElement("tr", chooser);
            writer.startElement("td", chooser);
            writer.startElement("div", chooser);
            writer.writeAttribute("class",
                    theme.getStyleClass(ThemeStyles.FILECHOOSER_LST_HDR), null);
            writer.startElement("div", chooser);
            writer.writeAttribute("class",
                    theme.getStyleClass(ThemeStyles.FILECHOOSER_NAME_HDR),
                    null);
            writer.write(theme.getMessage("filechooser.name_column_header"));
            writer.endElement("div");
            writer.writeText("\n", null);
            writer.startElement("div", chooser);
            writer.writeAttribute("class",
                    theme.getStyleClass(ThemeStyles.FILECHOOSER_SIZE_HDR),
                    null);
            writer.write(theme.getMessage("filechooser.size_column_header"));
            writer.endElement("div");
            writer.writeText("\n", null);
            writer.startElement("div", chooser);
            writer.writeAttribute("class",
                    theme.getStyleClass(ThemeStyles.FILECHOOSER_DATE_TIME_HDR),
                    null);
            writer.write(theme
                    .getMessage("filechooser.date_time_column_header"));
            writer.endElement("div");
            writer.writeText("\n", null);
            writer.endElement("div");
            writer.endElement("td");
            writer.endElement("tr");
            writer.writeText("\n", null);

            writer.startElement("tr", chooser);
            writer.startElement("td", chooser);
            renderFileList(context, chooser, writer, theme);
            writer.endElement("td");
            writer.endElement("tr");

            renderMultiSelectHelp(context, chooser, writer, theme);

            writer.startElement("tr", chooser);
            writer.startElement("td", chooser);
            writer.writeText("\n", null);
            renderSelectText(context, chooser, writer, theme);
            writer.endElement("td");
            writer.endElement("tr");

            writer.writeText("\n", null);
            writer.startElement("tr", chooser);
            writer.startElement("td", chooser);
            writer.startElement("hr", chooser);
            writer.writeAttribute("color", "#98a0a5", null);
            writer.writeAttribute("size", "1", null);
            writer.endElement("hr");
            writer.endElement("td");
            writer.endElement("tr");
            writer.writeText("\n", null);
            writer.endElement("table");

            writer.endElement("div");
            writer.endElement("div");

            // end of filechooser layout, now add the two hidden fields.
            String hiddenID = chooser.getClientId(context) + ":"
                    + chooser.getId() + FileChooser.FILECHOOSER_HIDDENFIELD_ID;
            writer.startElement("input", chooser);
            writer.writeAttribute("id", hiddenID, null);
            writer.writeAttribute("name", hiddenID, null);
            writer.writeAttribute("type", "hidden", null);
            writer.writeAttribute("value", "NOACTION", null);
            writer.endElement("input");

            Button hiddenButton = (Button) chooser.getHiddenFCButton();
            hiddenButton.setStyleClass(
                    theme.getStyleClass(ThemeStyles.HIDDEN));
            renderComponent(hiddenButton, context);

            // Render a hidden select to hold the currently selected
            // values.
            // Not sure if this really needs to done.
            // No change should be indicated by no submitted value
            // or if the selected file field contains exactly what it
            // did when it was rendered.
            //
            //renderSelectedSelections(context, chooser, writer, theme);

            // This may be useful at some point.
            // I was going to use this to detect entries that
            // where typed into the selected file field.
            // It would be a way to determine if an entry
            // was a full path. The problem is that you can't
            // detect when the file chooser is actually submitted
            // since the submit may have occured from an element
            // outside the file chooser. Then only way to do this
            // then would be onchange events and key press events
            // on the selected file field.
            //
            //renderRoots(context, chooser, writer, theme);
            renderJavaScript(chooser, context, writer, theme);
        } catch (Exception e) {
            throw new FacesException("Filechooser throws exception while"
                    + "rendering: " + e.getMessage());
        }
    }

    /**
     * Render the selected selections.
     * @param context faces context
     * @param chooser file chooser
     * @param writer writer to use
     * @throws Exception if an error occurs
     */
    private void renderSelectedSelections(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer)
            throws Exception {

        writer.startElement("select", chooser);
        String id = chooser.getClientId(context) + ":"
                + chooser.getId() + "_selections";
        writer.writeAttribute("id", id, null);
        writer.writeAttribute("name", id, null);
        writer.writeAttribute("style", "{display:none}", null);
        writer.writeAttribute("multiple", "multiple", null);

        // If this select is going to hold the current selections
        // need to get conversion hooked up to convert from the
        // app's type
        writer.endElement("select");
    }

    /**
     * Render the possible roots.
     * @param context faces context
     * @param chooser UI component
     * @param writer writer to use
     * @throws java.io.IOException if an IO error occurs
     */
    private void renderRoots(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer)
            throws IOException {

        String[] roots = chooser.getRoots();
        writer.startElement("select", chooser);
        String id = chooser.getClientId(context) + ":"
                + chooser.getId() + "_roots";
        writer.writeAttribute("id", id, null);
        writer.writeAttribute("name", id, null);
        writer.writeAttribute("style", "{display:none}", null);
        writer.writeAttribute("multiple", "multiple", null);

        for (int i = 0; i < roots.length; ++i) {
            writer.startElement("option", chooser);
            writer.writeAttribute("value", roots[i], null);
            writer.endElement("option");
        }

        // If this select is going to hold the current selections
        // need to get conversion hooked up to convert from the
        // app's type
        writer.endElement("select");
    }

    /**
     * This method appends the server name from where the files are being
     * listed to the file chooser layout.
     * @param context faces context
     * @param chooser UI component
     * @param writer writer to use
     * @param theme theme in use
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderServerName(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer,
            final Theme theme) throws IOException {

        UIOutput uio = (UIOutput) chooser.getServerNameText();
        Label label = (Label) chooser.getServerNameLabel();

        // If either not rendered, render neither.
        if (!uio.isRendered() || !label.isRendered()) {
            return;
        }

        writer.startElement("div", chooser);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.FILECHOOSER_SRV_DIV), null);

        // Render server name label
        writer.startElement("div", chooser);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.FILECHOOSER_LEV2_DIV), null);
        renderComponent(label, context);
        writer.endElement("div");
        writer.writeText("\n", null);

        // Render server name text
        renderComponent(uio, context);

        writer.endElement("div");
        writer.writeText("\n", null);
    }

    /**
     * This method renders the fileChooser label.
     * @param context faces context
     * @param chooser UI component
     * @param writer writer to use
     * @param theme theme in use
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderChooserTitle(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer,
            final Theme theme) throws IOException {

        // Append alert icon html.
        StaticText title = (StaticText) chooser.getFileChooserTitle();
        if (!title.isRendered()) {
            return;
        }
        writer.startElement("table", chooser);
        writer.writeAttribute("width", "100%", "width");
        writer.writeAttribute("border", "0", "border");
        writer.writeAttribute("cellpadding", "0", "cellpadding");
        writer.writeAttribute("cellspacing", "0", "cellspacing");
        writer.writeAttribute("title", "", "title");
        writer.startElement("tr", chooser);
        writer.writeAttribute("valign", "bottom", "valign");
        writer.startElement("td", chooser);
        writer.writeAttribute("valign", "bottom", "valign");
        writer.startElement("div", chooser);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.TITLE_TEXT_DIV), "class");
        writer.startElement("h1", chooser);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.TITLE_TEXT), "class");

        renderComponent(title, context);
        writer.endElement("h1");
        writer.endElement("div");
        writer.endElement("td");
        writer.endElement("tr");
        writer.endElement("table");
        writer.writeText("\n", null);
    }

    /**
     * This method renders help message in small font to
     * the file chooser layout.
     * @param context faces context
     * @param chooser UI component
     * @param writer writer to use
     * @param theme theme in use
     * @param messageKey message key to lookup
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderInlineHelp(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer,
            final String messageKey, final Theme theme)
            throws IOException {

        UIComponent help = chooser.getEnterInlineHelp();

        writer.startElement("div", chooser);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.FILECHOOSER_FLT_HLP_DIV), null);

        if (help.isRendered()) {
            renderComponent(help, context);
        }
        writer.writeText("\n", null);
        writer.endElement("div");
        writer.writeText("\n", null);
    }

    /**
     * This method appends a help message in small font to the file chooser
     * layout.
     *
     * @param context faces context
     * @param chooser UI component
     * @param writer writer to use
     * @param theme theme in use
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderLookinTextField(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer,
            final Theme theme) throws IOException {

        TextField lookinField = (TextField) chooser.getLookInTextField();
        if (!lookinField.isRendered()) {
            return;
        }

        writer.startElement("div", chooser);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.FILECHOOSER_LOOK_IN_DIV), null);
        writer.writeText("\n", null);

        writer.startElement("div", chooser);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.FILECHOOSER_LEV2_DIV), null);
        writer.writeText("\n", null);

        // render the lookin textfield label
        renderComponent(chooser.getLookInLabel(), context);
        writer.endElement("div");
        writer.writeText("\n", null);

        // render the lookin textfield
        setEnterKeyPressHandler(context, chooser, lookinField);

        renderComponent(lookinField, context);
        writer.endElement("div");
        writer.writeText("\n", null);
    }

    /**
     * This method appends a help message in small font to the file chooser
     * layout.
     *
     * @param context faces context
     * @param chooser UI component
     * @param writer writer to use
     * @param theme theme in use
     * @throws Exception if an IO error occurs
     */
    protected void renderFilterField(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer,
            final Theme theme) throws Exception {

        TextField filterOnField = (TextField) chooser.getFilterTextField();
        if (!filterOnField.isRendered()) {
            return;
        }
        writer.startElement("div", chooser);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.FILECHOOSER_FLT_DIV), null);
        writer.writeText("\n", null);

        writer.startElement("div", chooser);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.FILECHOOSER_LEV2_DIV), null);
        renderComponent(chooser.getFilterLabel(), context);
        writer.endElement("div");
        writer.writeText("\n", null);

        // render the lookin textfield
        setEnterKeyPressHandler(context, chooser, filterOnField);
        renderComponent(filterOnField, context);
        writer.endElement("div");
        writer.writeText("\n", null);
    }

    /**
     * This method renders the sort field drop down menu for the file chooser
     * component.
     *
     * @param context faces context
     * @param chooser UI component
     * @param writer writer to use
     * @param theme theme in use
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderSortFields(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer,
            final Theme theme) throws IOException {

        writer.startElement("div", chooser);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.FILECHOOSER_SORT_BY_DIV), null);

        writer.writeText("\n", null);
        Label sortLabel = (Label) chooser.getSortComponentLabel();
        DropDown jdd = (DropDown) chooser.getSortComponent();
        if (!jdd.isRendered()) {
            return;
        }
        if (sortLabel.isRendered()) {
            renderComponent(sortLabel, context);
        }
        renderComponent(jdd, context);
        writer.writeText("\n", null);
        writer.endElement("div");
        writer.writeText("\n", null);
    }

    /**
     * This method renders the list of files/folders in
     * a list box.
     * @param context faces context
     * @param chooser UI component
     * @param writer writer to use
     * @param theme theme in use
     * @throws Exception if an IO error occurs
     */
    protected void renderFileList(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer,
            final Theme theme) throws Exception {

        writer.startElement("div", chooser);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.FILECHOOSER_LST_DIV), null);

        UIComponent fileList = chooser.getListComponent();
        createJavaScriptForFileList(chooser, context, fileList);
        setEnterKeyPressHandler(context, chooser, fileList);
        renderComponent(fileList, context);

        writer.endElement("div");
        writer.writeText("\n", null);
    }

    /**
     * This method renders the multi select help if supplied.
     * @param context faces context
     * @param chooser UI component
     * @param writer writer to use
     * @param theme theme in use
     * @throws Exception if an IO error occurs
     */
    protected void renderMultiSelectHelp(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer,
            final Theme theme) throws Exception {

        HelpInline help = (HelpInline) chooser.getMultiSelectHelp();
        if (help != null && help.isRendered()) {
            writer.startElement("tr", chooser);
            writer.startElement("td", chooser);
            writer.startElement("div", chooser);
            writer.writeAttribute("class",
                    theme.getStyleClass(ThemeStyles.FILECHOOSER_MULT_HLP_DIV),
                    null);
            renderComponent(help, context);
            writer.endElement("div");
            renderClearDiv(context, chooser, writer, theme);
            writer.endElement("td");
            writer.endElement("tr");
        }
    }

    /**
     * This method renders the buttons used to traverse the file/folder
     * list.
     * @param context faces context
     * @param chooser UI component
     * @param writer writer to use
     * @param theme theme in use
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderButtons(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer,
            final Theme theme) throws IOException {

        writer.startElement("div", chooser);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.FILECHOOSER_BTN_GRP_DIV), null);

        renderUpLevelButton(context, chooser, writer, theme);
        renderOpenFolderButton(context, chooser, writer, theme);
        writer.endElement("div");
    }

    /**
     * This method renders the button to traverse the resource list
     * up one level.
     * @param context faces context
     * @param chooser UI component
     * @param writer writer to use
     * @param theme theme in use
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderUpLevelButton(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer,
            final Theme theme) throws IOException {

        writer.writeText("\n", null);
        Button child = (Button) chooser.getUpLevelButton(false);
        if (!child.isRendered()) {
            return;
        }
        StringBuilder jsBuffer = new StringBuilder();
        jsBuffer.append(getDomNode(context, chooser));
        jsBuffer.append(".moveUpButtonClicked();");
        child.setOnClick(jsBuffer.toString());

        renderComponent(child, context);
        writer.writeText("\n", null);
    }

    /**
     * This method renders the button to open a folder.
     * @param context faces context
     * @param chooser UI component
     * @param writer writer to use
     * @param theme theme in use
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderOpenFolderButton(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer,
            final Theme theme) throws IOException {

        writer.writeText("\n", null);
        Button child = (Button) chooser.getOpenFolderButton();
        if (!child.isRendered()) {
            return;
        }
        StringBuilder jsBuffer = new StringBuilder();
        jsBuffer.append(getDomNode(context, chooser));
        jsBuffer.append(".openFolderClicked();");
        child.setOnClick(jsBuffer.toString());
        renderComponent(child, context);
        writer.writeText("\n", null);
    }

    /**
     * This method renders the test field which lists the selected file(s) or
     * folder(s).It must satisfy the following UI properties: If a file is
     * selected in the list, display the name of the file in the Selected File
     * text field.If multiple files are selected, display all of the files names
     * separated by commas.Do not display folder names in the Selected File text
     * field.Replace existing text in the Selected File text field only when the
     * user selects a new file name from the list or types a new file name.No
     * other action should replace existing text in the Selected File text
     * field.Allow the user to specify a new path in the Selected File text
     * field.After the user enters the new path and hits enter, update the list
     * and the Look In text field to reflect the new path. If a user types a
     * path that begins with '/' or '\', take that as the full path name.
     * Otherwise, append what the user types to the path in the Look In text
     * field.
     *
     * Allow the user to specify a file name in the Selected File text field by
     * typing a full path, or without a path if the file is contained in the
     * current Look In folder. Once the user types in a valid file name and
     * presses the Enter key, the window should react appropriately: pop-up
     * windows should close and return the file name to the application while
     * inline file choosers should return the file name, clear the Selected file
     * text field, and either close or await additional user input at the
     * application designer's discretion. If the file chooser is in a pop-up
     * window, set the keyboard focus inside the Selected File text field when
     * the pop-up window initially comes up.
     *
     * @param context faces context
     * @param chooser component
     * @param writer writer to use
     * @param theme theme
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderSelectText(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer,
            final Theme theme) throws IOException {

        TextField selectedTextField
                = (TextField) chooser.getSelectedTextField();
        if (!selectedTextField.isRendered()) {
            return;
        }
        writer.startElement("div", chooser);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.FILECHOOSER_SEL_FILE_DIV),
                null);
        writer.writeText("\n", null);
        writer.startElement("div", chooser);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.FILECHOOSER_SEL_FILE_LEV2_DIV),
                null);

        renderComponent(chooser.getSelectLabel(), context);
        writer.endElement("div");
        setEnterKeyPressHandler(context, chooser, selectedTextField);
        renderComponent(selectedTextField, context);
        writer.endElement("div");

        renderClearDiv(context, chooser, writer, theme);
    }

    /**
     * This method renders JavaScript to initialize dom node.
     * @param context faces context
     * @param chooser UI component
     * @param writer writer to use
     * @param theme theme in use
     * @throws java.io.IOException if an IO error occurs
     */
    private void renderJavaScript(final FileChooser chooser,
            final FacesContext context, final ResponseWriter writer,
            final Theme theme) throws IOException {

        // boolean folderChooser = chooser.isFolderChooser();
        String chooserType;
        if (chooser.isFolderChooser()) {
            chooserType = "folderChooser";
        } else if (chooser.isFileAndFolderChooser()) {
            chooserType = "fileAndFolderChooser";
        } else {
            chooserType = "fileChooser";
        }

        // Arguments are
        // The client id
        // The chooser mode, folder chooser "true", file chooser "false"
        // The move up dir or the parent directory of the displayed
        // directory. This may be the same as the current directory.
        // The escape char, the string to use to escape the delimiter when
        // it appears in a file name.
        // The delimiter, the string to use to separate entries in
        // the selected file field.

        // Need to escape the escapeChar
        // There are other possible issue characters like "'"
        // and '"' but let's not deal with that now.
        String esc = chooser.getEscapeChar();
        if (esc.equals("\\")) {
            esc = "\\\\";
        }

        // getParentFolder can return null.
        // If null is returned, use getCurrentFolder().
        // FileChooserModel.getCurrentDir no longer returns null.
        // and will return the the root directory.
        String parentDir = chooser.getParentFolder();
        String currentFolder = chooser.getCurrentFolder();
        if (parentDir == null) {
            parentDir = currentFolder;
        }

        // If parentDir is still null, set it to empty string.
        // probably not FileChooserModel.
        String sep = chooser.getSeparatorString();
        if (parentDir == null) {
            parentDir = "";
        } else {
            // Need  to escape separator String
            //
            if (sep.equals("\\")) {
                sep = sep + sep;
                parentDir = parentDir.replaceAll(sep, sep + sep);
            }
        }

        sep = chooser.getSeparatorString();
        if (currentFolder == null) {
            currentFolder = "";
        } else {
            // Need  to escape separator String
            if (sep.equals("\\")) {
                sep = sep + sep;
                currentFolder = currentFolder.replaceAll(sep, sep + sep);
            }

        }

        // Append properties.
        String id = chooser.getClientId(context);
        StringWriter buff = new StringWriter();
        JsonObject initProps = JSON_BUILDER_FACTORY.createObjectBuilder()
                .add("id", id)
                .add("chooserType", chooserType)
                .add("parentFolder", parentDir)
                .add("separatorChar", sep)
                .add("escapeChar", esc)
                .add("delimiter", chooser.getDelimiterChar())
                .add("currentFolder", currentFolder)
                .build();

        // Render JavaScript.
        renderInitScriptTag(writer, "fileChooser", initProps);
    }

    /**
     * Create the JS for file list to handle {@code onchange}.
     * @param context faces context
     * @param chooser UI component
     * @param fileList file list
     */
    private void createJavaScriptForFileList(final FileChooser chooser,
            final FacesContext context, final UIComponent fileList) {

        String jsObject = getDomNode(context, chooser);

        // generate the JavaScript that will disable DBL clicks
        // on files in a folderchooser.
        StringBuilder dblClickBuffer = new StringBuilder();
        dblClickBuffer.append(jsObject);
        dblClickBuffer.append(".handleDblClick();");
        fileList.getAttributes().put("onDblClick", dblClickBuffer.toString());

        // The user gets to choose the child name, label, tooltip for
        // the control button that is placed at the bottom of the
        // filechooser tag. The following few lines of code disables
        // this button based on certain conditions. For example, the
        // button would be disabled if the user selects a file but
        // the choose button is meant to choose a folder.

        // The following code disabled the open folder button
        // based on the following situations:
        // a) user selects more than one from the list of files/folders
        // b) user selects a file
        // c) user does not select anything

        // This javascript also performs the following additional
        // function. Add the file/folder to the selected file/folder
        // textfield if its of the appropriate type and has been selected.

        StringBuilder jsBuffer = new StringBuilder();
        jsBuffer.append(jsObject);
        jsBuffer.append(".handleOnChange();");
        fileList.getAttributes().put("onChange", jsBuffer.toString());
    }

    /**
     * This method returns the string of size maxLen by padding the
     * appropriate amount of spaces next to str.
     * @param context faces context
     * @param chooser UI component
     * @param child UI component child
     */
    private void setEnterKeyPressHandler(final FacesContext context,
            final FileChooser chooser, final UIComponent child) {

        StringBuilder scriptBuffer = new StringBuilder();
        scriptBuffer.append("return ");
        scriptBuffer.append(getDomNode(context, chooser));
        scriptBuffer.append(".enterKeyPressed(this);");

        String js = getReturnKeyJavascriptWrapper(scriptBuffer.toString());
        scriptBuffer.setLength(0);
        scriptBuffer.append(js);
        child.getAttributes().put("onKeyPress", scriptBuffer.toString());
    }

    /**
     * Return the HTML equivalent of a single table row of empty space.
     * @param context faces context
     * @param chooser UI component
     * @param writer the writer to use
     * @param colSpan dot image colSpan
     * @param wd width
     * @param ht dot image ht
     * @param theme the theme in-use
     * @throws java.io.IOException if an IO error occurs
     */
    private void renderEmptyLine(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer,
            final String colSpan, final int wd, final int ht, final Theme theme)
            throws IOException {

        writer.startElement("tr", chooser);
        renderDotImage(writer, chooser, context, colSpan, wd, ht, theme);
        writer.endElement("tr");
        writer.writeText("\n", null);
    }

    /**
     * Return the HTML equivalent of a single table row of empty space.
     * @param context faces context
     * @param chooser UI component
     * @param writer the writer to use
     * @param theme the theme in-use
     * @throws java.io.IOException if an IO error occurs
     */
    private void renderClearDiv(final FacesContext context,
            final FileChooser chooser, final ResponseWriter writer,
            final Theme theme) throws IOException {

        writer.startElement("div", chooser);
        writer.writeAttribute("class",
                theme.getStyleClass(ThemeStyles.CLEAR), null);
        writer.endElement("div");
        writer.writeText("\n", null);
    }

    /**
     * Return the HTML equivalent of a single table data of empty space.
     * @param context faces context
     * @param chooser UI component
     * @param writer the writer to use
     * @param colSpan dot image colSpan
     * @param wd width
     * @param ht dot image ht
     * @param theme the theme in-use
     * @throws java.io.IOException if an IO error occurs
     */
    private void renderDotImage(final ResponseWriter writer,
            final FileChooser chooser, final FacesContext context,
            final String colSpan, final int wd, final int ht, final Theme theme)
            throws IOException {

            writer.startElement("td", chooser);
            if (colSpan != null) {
                writer.writeAttribute("colspan", colSpan, null);
            }

            Icon dot = getIcon(theme, ThemeImages.DOT);
            dot.setWidth(wd);
            dot.setHeight(ht);
            dot.setBorder(0);
            dot.setAlt("");
            renderComponent(dot, context);

            // close the td tag
            writer.endElement("td");
            writer.writeText("\n", null);
    }

    /**
     * Log an error - only used during development time.
     * @param msg log message
     */
    void log(final String msg) {
        if (LogUtil.fineEnabled(FileChooserRenderer.class)) {
            LogUtil.fine(FileChooserRenderer.class, msg);
        }
    }

    /**
     * Helper method to get JS to submit the "go" button when the user
     * clicks enter in the page field.
     *
     * @param body page body
     * @return The JS used to submit the "go" button.
     */
    private String getReturnKeyJavascriptWrapper(final String body) {
        ClientSniffer cs = ClientSniffer.getInstance(getFacesContext());

        // Get key code.
        String keyCode;
        if (cs.isNav()) {
            keyCode = "event.which";
        } else {
            keyCode = "event.keyCode";
        }

        // Append JS to capture the event.
        StringBuffer buff = new StringBuffer()
                .append("if (")
                .append(keyCode)
                .append("==13) {");

        // To prevent an auto-submit, Netscape 6.x and netscape 7.0 require
        // setting the cancelBubble property. However, Netscape 7.1,
        // Mozilla 1.x, IE 5.x for SunOS/Windows do not use this property.
        if (cs.isNav6() || cs.isNav70()) {
            buff.append("event.cancelBubble = true;");
        }

        buff.append(body).append("} else { return true; }");
        return buff.toString();
    }
}
