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
package com.sun.webui.jsf.component;

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.model.FileChooserModel;
import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.model.ResourceItem;
import com.sun.webui.jsf.model.ResourceModel;
import com.sun.webui.jsf.model.ResourceModelException;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ClientSniffer;
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.jsf.validator.FileChooserFilterValidator;
import com.sun.webui.jsf.validator.FileChooserLookInValidator;
import com.sun.webui.jsf.validator.FileChooserSelectValidator;
import com.sun.webui.jsf.validator.FileChooserSortValidator;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.render.Renderer;
import jakarta.faces.validator.ValidatorException;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

// The file chooser poses several problems when trying to integrate
// with the JSF lifecycle. As an EditableValueHolder it must
// provide a value and allow it to be edited. This includes having a
// submittedValue, validation and model updating.
//
// However, it also supports several actions to allow the user to
// navigate a filesystem directory tree, including an initial directory
// called the look in field, a filter field to filter the directory
// contents, a sort menu allowing the user to sort the contents of
// the directory, a moveup button (displaye parent directory contents)
// and an open folder button to navigate a filesystem tree.
//
// In addition it must conform to SWAED guidelines in the appearance
// and behavior of the basic component as well as in context
// with other components.
//
// Handling the submittedValue
//
// The submittedValue is the sole means by which the component
// determines that it has selections. It is only at this time that
// the file chooser participates in the full JSF lifecycle.
//
// All other actions are intermediate and perform only the necessary
// validations associated with those actions.
//
// Actions
//
// OpenFolder
//
//    This serves as the action handler for the followin actions
//    - return key pressed in the look in field
//    - return key pressed in the filter field
//    - double clicking a folder in the list box
//    - return key pressed in the listbox with a folder selection
//    - the open folder button clicked
//
// MoveUp
//
//    - uses the current lookin field value. First ensures it is
//      valid. Must also ensure the validity of the filter field as well.
//
// Sort
//
//    - uses the current lookin field value. First ensures it is
//      valid. Must also ensure the validity of the filter field as well.
//
// LookInField
//
//    - Effectively validated on every request.
//
// Filter
//
//    - Effectively validated on every request.
//
// FIXME: The model is used as a persistant store and is being
// referenced in preference to the subcompoents which containt
// the current data. The model must be stateless and data
// must be obtained from the sub components and respect
// their validity by checking the submittedValue if the
// component is not valid and then the component's value
// if it is valid. The model should only be used to validate
// and obtain contents.
//
/**
 * The FileChooser component allows the user to select files and folders.
 */
@Component(type = "com.sun.webui.jsf.FileChooser",
        family = "com.sun.webui.jsf.FileChooser",
        displayName = "File Chooser", tagName = "fileChooser",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_file_chooser",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_file_chooser_props")
        //CHECKSTYLE:ON
public final class FileChooser extends WebuiInput implements NamingContainer {

    /**
     * Alphabetic sort field type.
     */
    public static final String ALPHABETIC = "alphabetic";

    /**
     * Alphabetic ascending sort.
     */
    public static final String ALPHABETIC_ASC = "alphabetica";

    /**
     * Alphabetic descending sort.
     */
    public static final String ALPHABETIC_DSC = "alphabeticd";

    /**
     * Sort "by size" field type.
     */
    public static final String SIZE = "size";

    /**
     * Size ascending sort.
     */
    public static final String SIZE_ASC = "sizea";

    /**
     * Size descending sort.
     */
    public static final String SIZE_DSC = "sized";

    /**
     * Sort "by last modified" field type.
     */
    public static final String LASTMODIFIED = "time";

    /**
     * Last modified ascending sort.
     */
    public static final String LASTMODIFIED_ASC = "timea";

    /**
     * Last modified descending sort.
     */
    public static final String LASTMODIFIED_DSC = "timed";

    /**
     * Server name facet.
     */
    public static final String FILECHOOSER_SERVERNAME_STATICTEXT_FACET
            = "serverNameText";

    /**
     * Server label facet.
     */
    public static final String FILECHOOSER_SERVERNAME_LABEL_FACET
            = "serverLabel";

    /**
     * Enter key press inline help facet.
     */
    public static final String FILECHOOSER_ENTERPRESS_HELP_FACET
            = "enterPressHelp";

    /**
     * Multi select inline help facet.
     */
    public static final String FILECHOOSER_MULTISELECT_HELP_FACET
            = "multiSelectHelp";

    /**
     * Look-in text field facet.
     */
    public static final String FILECHOOSER_LOOKIN_TEXTFIELD_FACET
            = "lookinField";

    /**
     * Look-in label field facet.
     */
    public static final String FILECHOOSER_LOOKIN_LABEL_FACET
            = "lookinLabel";

    /**
     * Label facet.
     */
    public static final String FILECHOOSER_LABEL_FACET
            = "fileChooserLabel";

    /**
     * Filter-on text field facet.
     */
    public static final String FILECHOOSER_FILTERON_TEXTFIELD_FACET
            = "filterField";

    /**
     * File chooser filter label facet.
     */
    public static final String FILECHOOSER_FILTER_LABEL_FACET
            = "filterLabel";

    /**
     * File chooser selected text field facet.
     */
    public static final String FILECHOOSER_SELECTED_TEXTFIELD_FACET
            = "selectedField";

    /**
     * File chooser select label facet.
     */
    public static final String FILECHOOSER_SELECT_LABEL_FACET
            = "selectedLabel";

    /**
     * File chooser uplevel button facet.
     */
    public static final String FILECHOOSER_UPLEVEL_BUTTON_FACET
            = "upButton";

    /**
     * File chooser open folder button facet.
     */
    public static final String FILECHOOSER_OPENFOLDER_BUTTON_FACET
            = "openButton";

    /**
     * File chooser sort menu facet.
     */
    public static final String FILECHOOSER_SORTMENU_FACET
            = "sortMenu";

    /**
     * File chooser sort label facet.
     */
    public static final String FILECHOOSER_SORT_LABEL_FACET
            = "sortLabel";

    /**
     * File chooser hidden button facet.
     */
    public static final String FILECHOOSER_HIDDEN_BUTTON_FACET
            = "hiddenButton";

    /**
     * File chooser list box facet.
     */
    public static final String FILECHOOSER_LISTBOX_FACET
            = "listEntries";

    /**
     * Referenced in FileChooserRenderer.
     */
    public static final String FILECHOOSER_HIDDENFIELD_ID
            = "_hiddenField";

    /**
     * Error string constant for Internal Exceptions.
     */
    private static final String NULLMODEL = "Null model value.";

    /**
     * Hyphen character constant.
     */
    public static final String HYPHEN = "-";

    /**
     * Flag for detecting a change in the look in field. Can't use value change
     * events because they happen too late. We could implement queueEvent and
     * watch events being queued from the sub components..
     */
    private boolean openFolderChanged;

    /**
     * filterChanged flag.
     */
    private boolean filterChanged;

    /**
     * The last open folder.
     */
    private String lastOpenFolder;

    /**
     * handling a special case where both files and folders can be chosen.
     */
    private boolean fileAndFolderChooser;

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     */
    @Property(name = "visible",
            displayName = "Visible",
            category = "Behavior")
    private boolean visible = true;

    /**
     * visible set flag.
     */
    private boolean visibleSet;

    /**
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     */
    @Property(name = "tabIndex",
            displayName = "Tab Index",
            category = "Accessibility",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int tabIndex = Integer.MIN_VALUE;

    /**
     * tabIndex set flag.
     */
    private boolean tabIndexSet;

    /**
     * Set this attribute to true to sort from the highest value to lowest
     * value, such as Z-A for alphabetic, or largest file to smallest for
     * sorting on file size. The default is to sort in ascending order.
     */
    @Property(name = "descending",
            displayName = "Descending",
            category = "Advanced")
    private boolean descending;

    /**
     * descending set flag.
     */
    private boolean descendingSet;

    /**
     * Indicates that activation of this component by the user is not currently
     * permitted.
     */
    @Property(name = "disabled",
            displayName = "Disabled",
            category = "Behavior")
    private boolean disabled;

    /**
     * disabled set flag.
     */
    private boolean disabledSet;

    /**
     * Use this attribute to configure the file chooser as a folder chooser. Set
     * the value to true for a folder chooser or false for a file chooser. The
     * default value is false.
     */
    @Property(name = "folderChooser",
            displayName = "Folder Chooser",
            category = "Appearance")
    private boolean folderChooser;

    /**
     * folderChooser set flag.
     */
    private boolean folderChooserSet;

    /**
     * Use this attribute to specify the initial folder to display in the Look
     * In text field. The contents of this folder will be displayed. Only
     * {@code java.io.File} or {@code java.lang.String} objects can be
     * bound to this attribute.
     */
    @Property(name = "lookin",
            displayName = "Lookin",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private Object lookin;

    /**
     * Specifies the model associated with the FileChooser. The model provides
     * the file chooser with content displayed in the file chooser's list. It
     * provides other services as defined
     * in{@code com.sun.webui.jsf.model.ResourceModel}. If the model
     * attribute is not assigned a value, a FileChooserModel is used as the
     * ResourceModel instance. A value binding assigned to this attribute must
     * return an instance of ResourceModel.
     */
    @Property(name = "model",
            displayName = "Model",
            shortDescription = "The model associated with the filechooser",
            isHidden = true,
            isAttribute = false)
    private com.sun.webui.jsf.model.ResourceModel model;

    /**
     * Set multiple to true to allow multiple files or folders to be selected
     * from the list. The default is false, which allows only one item to be
     * selected.
     */
    @Property(name = "multiple",
            displayName = "Multiple",
            category = "Appearance")
    private boolean multiple;

    /**
     * multiple set flag.
     */
    private boolean multipleSet;

    /**
     * <p>
     * If readOnly is set to true, the value of the component is rendered as
     * text, preceded by the label if one was defined.</p>
     */
    @Property(name = "readOnly",
            displayName = "Read-only",
            category = "Behavior")
    private boolean readOnly;

    /**
     * readOnly set flag.
     */
    private boolean readOnlySet;

    /**
     * The number of items to display in the list box. The value must be greater
     * than or equal to one. The default value is 12. Invalid values are ignored
     * and the value is set to 12.
     */
    @Property(name = "rows",
            displayName = "Rows",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    @SuppressWarnings("checkstyle:magicnumber")
    private int rows = 12;

    /**
     * rows set flag.
     */
    private boolean rowsSet;

    /**
     * Field to use to sort the list of files. Valid values are:
     * <ul><li>alphabetic - sort alphabetically</li>
     * <li>size - sort by file size</li>
     * <li>time - sort by last modified date</li></ul>
     * <p>
     * Note that these values are case sensitive. By default, the list is sorted
     * alphabetically.</p>
     */
    @Property(name = "sortField",
            displayName = "Sort Field",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.SortFieldEditor")
            //CHECKSTYLE:ON
    private String sortField = "alphabetic";

    /**
     * sortField set flag.
     */
    private boolean sortFieldSet;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
    private String style;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "styleClass",
            displayName = "CSS Style Class(es)",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StyleClassPropertyEditor")
            //CHECKSTYLE:ON
    private String styleClass;

    /**
     * Default constructor.
     */
    public FileChooser() {
        super();
        setRendererType("com.sun.webui.jsf.FileChooser");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.FileChooser";
    }

    /**
     * Get the escape character.
     * @return String
     */
    public String getEscapeChar() {
        return getModel().getEscapeChar();
    }

    /**
     * Get the delimiter character.
     * @return String
     */
    public String getDelimiterChar() {
        return getModel().getDelimiterChar();
    }

    /**
     * Return the current folder. The value of
     * {@code getModel().getCurrentDir()} is returned.
     * @return String
     */
    public String getCurrentFolder() {
        return getModel().getCurrentDir();
    }

    /**
     * Return the path element separator. The value of
     * {@code getModel().getSeparatorString()} is returned.
     * @return String
     */
    public String getSeparatorString() {
        return getModel().getSeparatorString();
    }

    /**
     * Return the current folder's parent folder. The value of
     * {@code getModel().getParentFolder()} is returned. If model is
     * {@code FileChooserModel} and there is no parent folder null is
     * returned.
     * @return String
     */
    public String getParentFolder() {
        return getModel().getParentFolder();
    }

    /**
     * Set flag to true if you want the fileChooser to be able to select both
     * files and folders. If this method is used neither the folderChooser
     * attribute nor the model API methods to set the chooser type should be
     * set.
     *
     * @param flag folder chooser flag
     */
    public void setFileAndFolderChooser(final boolean flag) {
        if (flag) {
            doSetFolderChooser(false);
        }
        this.fileAndFolderChooser = flag;
    }

    /**
     * Return true if both files and folders can be selected.
     * @return {@code boolean}
     */
    public boolean isFileAndFolderChooser() {
        return this.fileAndFolderChooser;
    }

    /**
     * Get the folder chooser flag value.
     * @return {@code boolean}
     */
    public boolean isFolderChooser() {
        if (isFileAndFolderChooser()) {
            return false;
        } else {
            return doIsFolderChooser();
        }
    }

    /**
     * Set the folder chooser flag value.
     * @param newChooser folder chooser
     */
    public void setFolderChooser(final boolean newChooser) {
        if (isFileAndFolderChooser()) {
            doSetFolderChooser(false);
        } else {
            doSetFolderChooser(newChooser);
        }
    }

    // - FileChooserModel must be stateless. (unless absolutely impossible
    //   which I don't think is the case.). This will allow the FileChooser
    //   to operate in a request scope. It means that data must be obtained
    //   from the sub components and not the model for things like the
    //   current directory.
    //
    // - There must be some mechanism like value change events from the
    //   subcomponents to indicate that data has changed that might
    //   affect a subcomponent after or before it has been validated.
    //   This is the case if the lookin field has changed, selections
    //   must be thrown away. (This is what the "lastOpenFolder" hack
    //   is about, as well as the directory changing due to a file
    //   selection.)
    //   One way to accomplish this is to intercept the queueEvents
    //   for the subcomponents since they bubble up the component hierachy
    //   and either do something based on seeing that event or
    //   if the PhaseId isn't ANYPHASE, make it ANYPHASE and use a
    //   value change listener. This is necessary so the immediate
    //   behavior can be realized.
    //
    // - Calls like getDirContent() should be parameterized with the
    //   the value of the look in field, since it is
    //   the real data store for that value. Likewise the file value
    //   and sort value.
    //
    // - If the model is null create the model.
    //   It will be created on every request unless it is bound a
    //   session scope backing bean. It appears that the Model object is
    //   actually serialized during restore/save state.
    //
    /**
     * Get the resource model.
     * @return ResourceModel
     */
    public ResourceModel getModel() {
        ResourceModel zModel = doGetModel();
        if (zModel == null) {
            log(NULLMODEL);
            zModel = new FileChooserModel();
            Object obj = getLookin();
            String currentDir = null;
            if (obj != null) {
                if (obj instanceof File) {
                    currentDir = ((File) obj).getAbsolutePath();
                } else if (obj instanceof String) {
                    currentDir = (String) obj;
                }
            }
            zModel.setCurrentDir(currentDir);
            setModel(zModel);
        }
        return zModel;
    }

    /**
     * Get the file system roots.
     * @return String[]
     */
    public String[] getRoots() {
        return getModel().getRoots();
    }

    /**
     * <p>
     * Override the default {@code UIComponentBase#processDecodes} processing to
     * perform the following steps.</p>
     * <p>
     * FileChooser obtains instances of the sub components and calls their
     * "processDecodes" methods before it calls its own "decode" method. After,
     * if FacesContext.getRenderResponse() returns true an error has occurred
     * and FileChooser should take its "failure" course. After calling the
     * processDecodes of the sub components, in the decode method of the
     * FileChooser obtain the submittedValues of the sub components and
     * synthesize a submitted value for the FileChooser and set its submitted
     * value which will cause the FileChooser to participate in the JSF life
     * cycle processing.
     *
     * <ul>
     * <li>If the {@code rendered} property of this {@link UIComponent} is
     * {@code false}, skip further processing.</li>
     * <li>Call the {@code processDecodes()} method of all facets of this
     * {@link FileChooser}, in the order determined by a call to
     * {@code getFacets().keySet().iterator()}.</li>
     *
     * <li>Call the {@code processDecodes()} method of all children of this
     * {@link FileChooser}, in the order determined by a call to
     * {@code getChildren().keySet().iterator()}.</li>
     *
     * </ul>
     *
     * @param context {@link FacesContext} for the current request
     *
     * @exception NullPointerException if {@code context} is
     * {@code null}
     */
    @Override
    public void processDecodes(final FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (!isRendered()) {
            return;
        }

        Iterator it = getFacets().keySet().iterator();
        while (it.hasNext()) {
            UIComponent facet = (UIComponent) getFacets().get(it.next());
            facet.processDecodes(context);
        }

        it = getChildren().iterator();
        while (it.hasNext()) {
            UIComponent child = (UIComponent) it.next();
            child.processDecodes(context);
        }

        // If the facet input components are immediate it can
        // be misleading if the chooser's submitted values, i.e.
        // the file selections, are full paths.
        //
        // If the filechooser's submittedValue is one or more
        // fully qualified paths, then these fields are irrelevant
        // and invalidating the chooser because any of them are
        // invalid is misleading.
        //
        // Policy decision.
        // This may be contrary to SWAED desires.
        // In an effort to not create new request protocols in
        // order to conditionally decode or validate facets, the
        // look in component, the filter component and the sort
        // component will always be validated as if they were immediate.
        //
        // The policy difference is that it may be that SWAED only
        // wants these values validated if explicitly commited by
        // virtue of pressing the return key in the in the look in
        // or filter text fields, or only when explicitly selecting a sort.
        // But that is very problematic when trying to integrate with the
        // JSF lifecycle.
        //
        // The chosen policy is also implemented in the javascript.
        //
        // If these components are immediate, they have already been validated.
        // All internally created facet fields are set as immediate.
        //
        // Typically JSF performs all validation even if one
        // validator fails.
        //
        boolean invalid = false;
        EditableValueHolder evh;

        // Need to do this first.
        // The policy is that if the look in field changes
        // then any selections have to be thrown away
        // since they may be relative to the last shown
        // directory.
        //
        // This is contrary to the feature of allowing the
        // selected file field to contain a full path to the
        // desired file.
        evh = (EditableValueHolder) getLookInTextField();
        if (evh != null && !evh.isImmediate()) {
            ((UIComponent) evh).processValidators(context);
        }
        invalid = invalid || !evh.isValid();

        // Do this second for the a similar reason.
        // These policies reflect the client behavior of
        // clearing the selected field when the filter field
        // and the lookin field are changed by hitting the
        // return key.
        //
        evh = (EditableValueHolder) getFilterTextField();
        if (evh != null && !evh.isImmediate()) {
            ((UIComponent) evh).processValidators(context);
        }
        invalid = invalid || !evh.isValid();

        // Checks to see if the the look in field or the
        // filter field have changed. If they have throw
        // away the selections.
        //
        // Can't use value change events because they
        // come too late.
        //
        // Need to also update the lookin field if a
        // selection implies a new folder location.
        //
        evh = (EditableValueHolder) getSelectedTextField();
        if (evh != null && !evh.isImmediate()) {
            ((UIComponent) evh).processValidators(context);
        }
        invalid = invalid || !evh.isValid();

        evh = (EditableValueHolder) getSortComponent();
        if (evh != null && !evh.isImmediate()) {
            ((UIComponent) evh).processValidators(context);
        }
        invalid = invalid || !evh.isValid();

        // Update the listbox state
        //
        evh = (EditableValueHolder) getListComponent();
        if (evh != null && !isImmediate()) {
            ((UIComponent) evh).processValidators(context);
        }
        invalid = invalid || !evh.isValid();

        // As noted above, if the submitted values are
        // full paths, then the prior validation may be misleading.
        decode(context);

        // If this component is immediate, then we would
        // validate it now. However if the other components aren't
        // immediate, it wouldn't make much sense.
        // But since we are treating the facet input components as
        // immediate, we're ok.
        if (isImmediate()) {
            // If the other fields are invalid don't perform the
            // validation. It's probably not useful since the
            // values may be derived from the other fields most of the time.
            // i.e. relative path values requiring the look in field value.
            if (!invalid) {
                validate(context);
            } else {
                setValid(false);
            }
        } else {
            // If any of the components were invalid set the
            // chooser invalid.
            if (invalid) {
                setValid(false);
            }
        }
        if (!isValid()) {
            context.renderResponse();
        }
    }

    /**
     * <p>
     * Override the default {@code UIComponentBase#processValidators} processing
     * to perform the following steps.</p>
     * <ul>
     * <li>If the {@code rendered} property of this {@link UIComponent} is
     * {@code false}, skip further processing.</li>
     * <li>Call the {@code processValidators()} method of all facets and
     * children of the fileChooser component except the list box. Then validate
     * the list box followed by the file chooser component itself. The list box
     * needs to be validated after the other components because its value
     * depends on user input to the other components.
     * </li>
     * </ul>
     *
     * @param context {@link FacesContext} for the current request
     *
     * @exception NullPointerException if {@code context} is
     * {@code null}
     */
    @Override
    public void processValidators(final FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (!isRendered()) {
            return;
        }

        // This has the potential of validating
        // developer defined facets twice. Once, above
        // because we treat them as immediate even if they
        // are not immediate.
        Iterator it = getFacetsAndChildren();
        Listbox lb = null;
        while (it.hasNext()) {
            UIComponent child = (UIComponent) it.next();
            if (child instanceof Listbox) {
                lb = (Listbox) child;
            } else {
                child.processValidators(context);
                if (child instanceof EditableValueHolder) {
                    if (!((EditableValueHolder) child).isValid()) {
                        setValid(false);
                    }
                }
            }
        }
        // This only needs to happen if a developer defined
        // facet is specified. But this is not a supported
        // facet, but do it anyway.
        if (lb != null) {
            lb.processValidators(context);
            if (!lb.isValid()) {
                setValid(false);
            }
        }

        try {
            if (!isImmediate()) {
                validate(context);
            }
        } catch (RuntimeException rte) {
            context.renderResponse();
        }

        if (!isValid()) {
            context.renderResponse();
        }
    }

    /**
     * <p>
     * Retrieve the submitted value with getSubmittedValue(). If this returns
     * null, exit without further processing. (This indicates that no value was
     * submitted for fileChooser.) Convert the submitted value into a "local
     * value" of the appropriate data type by calling
     * getConvertedValue(jakarta.faces.context.FacesContext, java.lang.Object).
     * Validate the property by calling
     * validateValue(jakarta.faces.context.FacesContext, java.lang.Object). If the
     * valid property of this component is still true, retrieve the previous
     * value of the component (with getValue()), store the new local value using
     * setValue(), and reset the submitted value to null. If the local value is
     * different from the previous value of this component, fire a
     * ValueChangeEvent to be broadcast to all interested listeners. processing
     * to perform the following steps.</p>
     * <ul>
     * <li>If the {@code rendered} property of this {@link UIComponent} is
     * {@code false}, skip further processing.</li>
     * <li>Call the {@code processUpdates()} method of all facets of this
     * {@link FileChooser}, in the order determined by a call to
     * {@code getFacets().keySet().iterator()}.</li>
     * </ul>
     *
     * @param context {@link FacesContext} for the current request
     *
     * @exception NullPointerException if {@code context} is
     * {@code null}
     */
    @Override
    public void validate(final FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        // If submittedValue is null, then no selections were
        // And this validate will never be called unless the
        // file chooser is immediate or we are processing a true
        // submit and not an intermediate action like openFolder.
        //
        Object submittedValue = getSubmittedValue();
        if (submittedValue == null) {
            return;
        }

        // enforce isMultiple here
        //
        if (!isMultiple() && ((String[]) submittedValue).length > 1) {
            FacesMessage fmsg = null;
            if (isFolderChooser()) {
                fmsg = createFacesMessage(
                        "filechooser.tooManyFileErrSum",
                        "filechooser.tooManyFileErrDet",
                        null, null);
            } else if (isFileAndFolderChooser()) {
                fmsg = createFacesMessage(
                        "filechooser.tooManyFileFolderErrSum",
                        "filechooser.tooManyFileFolderErrDet",
                        null, null);
                fmsg = createFacesMessage(
                        "filechooser.tooManyFolderErrSum",
                        "filechooser.tooManyFolderErrDet",
                        null, null);
            }
            context.addMessage(getClientId(context), fmsg);
            setValid(false);
            return;
        }

        // Get the current dir to see if it has changed
        // from the previous value.
        ResourceModel zModel = getModel();
        lastOpenFolder = zModel.getCurrentDir();

        Object newValue = null;
        try {
            newValue = getConvertedValue(context, submittedValue);
        } catch (ConverterException ce) {
            // display error message here
            setValid(false);
        }
        // Here's where it gets ugly.
        // If the openFolder has changed, call processValidators
        // on the look in field after setting its submittedValue
        // to the new value. If it turns out to be invalid.
        // null the submittedValue, and set the file chooser as invalid.
        // It should validate since it already is the current directory.
        if (!lastOpenFolder.equals(zModel.getCurrentDir())) {
            EditableValueHolder evh = (EditableValueHolder)
                    getLookInTextField();
            evh.setSubmittedValue(zModel.getCurrentDir());
            try {
                ((UIComponent) evh).processValidators(context);
            } catch (ValidatorException ve) {
                setValid(false);
                // Get the last known good value
                //
                evh.setSubmittedValue(lastOpenFolder);
                zModel.setCurrentDir(lastOpenFolder);
                return;
            }
        }

        // We have to handle a special case where the chooser does
        // not record a value event though a selection was submitted.
        // If the chooser is a file chooser and there is only one
        // selection and it is a folder,then this is not set as the
        // value and is the same as changing the look in field.
        // no need to validate as validate is done in the getConvertedValue()
        // method. If our value is valid, store the new value, erase the
        // "submitted" value, and emit a ValueChangeEvent if appropriate
        if (isValid()) {
            Object previous = getValue();
            setValue(newValue);
            setSubmittedValue(null);
            if (compareValues(previous, newValue)) {
                queueEvent(new ValueChangeEvent(this, previous, newValue));
            }
        }
    }

    // Currently previous and newValue are ResourceItem arrays.
    // Return true if values are different
    @Override
    protected boolean compareValues(final Object previous, final Object value) {
        // Let super take care of null cases
        //
        if (previous == null || value == null) {
            return super.compareValues(previous, value);
        }
        if (value instanceof Object[]) {
            // If the lengths aren't equal return true
            int length = Array.getLength(value);
            if (Array.getLength(previous) != length) {
                return true;
            }
            // Each element at index "i" in previous must be equal to the
            // elementa at index "i" in value.
            for (int i = 0; i < length; ++i) {

                Object newValue = Array.get(value, i);
                Object prevValue = Array.get(previous, i);

                if (newValue == null) {
                    if (prevValue == null) {
                        continue;
                    } else {
                        return true;
                    }
                }
                if (prevValue == null) {
                    return true;
                }

                if (!prevValue.equals(newValue)) {
                    return true;
                }
            }
            return false;
        }
        return super.compareValues(previous, value);
    }

    /**
     * This validation method is in addition to any that might be part of the
     * component when specified as a facet. Throw a ValidatorException with a
     * FacesMessage explaining what happened.
     *
     * Called from ChooserLookInValidator.
     * @param context faces context
     * @param component UI component
     * @param value value to validate
     * @throws ValidatorException if an error occurs
     */
    public void validateLookInComponent(final FacesContext context,
            final UIComponent component, final Object value)
            throws ValidatorException {

        // No need to check for null, getModel throws FacesException if null.
        ResourceModel zModel = getModel();

        // Assuming object value type is String.
        String lookInValue = (String) value;

        // For now assume this is called from
        // ChooserLookInValidator. It is a registered validator.
        // setCurrentDir throws exception, return its FacesMessage.
        // LookInValidator will throw ValidatorException
        // with that FacesMessage.
        // When it throws JSF will add that message
        // to the context, but with the component
        // id of the lookInField. Therefore, add it to the
        // context under the filechooser id since that is what
        // "displayAlert" and the renderer expects.
        //
        // The error strategy to be more integrated and well defined
        // so the renderer can do the right thing.
        try {
            zModel.setCurrentDir(lookInValue);
        } catch (ResourceModelException cme) {

            // First clear the submitted value so the last known
            // valid value is rendered.
            // This is part of the policy of the FileChooser.
            //
            ((EditableValueHolder) component).setSubmittedValue(null);
            FacesMessage fmsg = cme.getFacesMessage();
            context.addMessage(getClientId(context), fmsg);
            throw new ValidatorException(fmsg);
        }
    }

    /**
     * This validation method is in addition to any that might be part of the
     * component when specified as a facet. Throw a ValidatorException with a
     * FacesMessage explaining what happened.
     *
     * Called from ChooserFilterValidator.
     *
     * @param context faces context
     * @param component UI component
     * @param value value to validate
     * @throws ValidatorException if an error occurs
     */
    public void validateFilterComponent(final FacesContext context,
            final UIComponent component, final Object value)
            throws ValidatorException {

        // No need to check for null, getModel throws FacesException if null.
        ResourceModel zModel = getModel();

        // For now assume this is called from
        // ChooserFilterValidator. It is a registered validator.
        // setFilterValue throws exception, return its FacesMessage.
        // ChooserFilterValidator will throw ValidatorException
        // with that FacesMessage.
        // When it throws JSF will add that message
        // to the context, but with the component
        // id of the filterField. Therefore, add it to the
        // context under the filechooser id since that is what
        // "displayAlert" and the renderer expects.
        //
        // The error strategy needs to be more integrated and well defined
        // so the renderer can do the right thing.
        String filterValue = (String) value;

        // Get the current filter to see if it has changed
        // from the previous value.
        String lastFilter = zModel.getFilterValue();
        try {
            zModel.setFilterValue(filterValue);
        } catch (ResourceModelException cme) {
            ((EditableValueHolder) component).setSubmittedValue(null);
            FacesMessage fmsg = cme.getFacesMessage();
            context.addMessage(getClientId(context), fmsg);
            throw new ValidatorException(fmsg);
        }
        filterChanged = !lastFilter.equals(zModel.getFilterValue());
    }

    /**
     * This validation method is in addition to any that might be part of the
     * component if specified as a facet. Throw a ValidatorException with a
     * FacesMessage explaining what happened.
     *
     * Called from ChooserSortValidator.
     * @param context faces context
     * @param component UI component
     * @param value value to validate
     * @throws ValidatorException if an error occurs
     */
    public void validateSortComponent(final FacesContext context,
            final UIComponent component, final Object value)
            throws ValidatorException {

        // No need to check for null, getModel throws FacesException if null.
        ResourceModel zModel = getModel();

        // For now assume this is called from
        // ChooserSortValidator. It is a registered validator.
        // setSortValue throws exception, return its FacesMessage.
        // ChooserSortValidator will throw ValidatorException
        // with that FacesMessage.
        // When it throws JSF will add that message
        // to the context, but with the component
        // id of the sortField. Therefore, add it to the
        // context under the filechooser id since that is what
        // "displayAlert" and the renderer expects.
        //
        // The error strategy needs to be more integrated and well defined
        // so the renderer can do the right thing.
        String sortValue = (String) value;

        // Not sure is this is the right thing to do for a drop down.
        try {
            zModel.setSortValue(sortValue);
        } catch (ResourceModelException cme) {
            ((EditableValueHolder) component).setSubmittedValue(null);
            FacesMessage fmsg = cme.getFacesMessage();
            context.addMessage(getClientId(context), fmsg);
            throw new ValidatorException(fmsg);
        }
    }

    /**
     * This validation method is in addition to any that might be part of the
     * component if specified as a facet. Throw a ValidatorException with a
     * FacesMessage explaining what happened.
     *
     * Called from ChooserSelectValidator.
     * @param context faces context
     * @param component UI component
     * @param value value to validate
     * @throws ValidatorException if an error occurs
     */
    public void validateSelectComponent(final FacesContext context,
            final UIComponent component, final Object value)
            throws ValidatorException {

        // This validator is a strange validator.
        // It going to parse out the selection and set the
        // result as the submittedValue of the filechooser.
        //
        // This is due to the fact that the selections can be
        // submitted in two ways.
        //
        // If this selectFieldComponent doesn't exist, then
        // the selections are submitted differently and decoded
        // by the file chooser renderer as an array of selections
        // as a request parameter.
        //
        // If this select field component does exist it is
        // submitted as well. Since it can have user entered
        // data and not just selections from the list it must
        // take precedence in the request.
        //
        // Parse the comma separated selections.
        //
        // To enforce similar policies on the client, if the
        // look in field has changed or the filter has changed
        // Throw a ValidatorException with a null message
        // to simulate what happens on the client.
        // We may want a real message at some point.
        if (openFolderChanged || filterChanged) {
            ((EditableValueHolder) component).setSubmittedValue(null);
            ((EditableValueHolder) component).setValid(false);
            return;
        }

        if (value != null && ((String) value).length() > 0) {
            String[] selections = decodeSelections((String) value,
                    getEscapeChar(), getDelimiterChar());

            this.setSubmittedValue(selections);
        }
    }

    /**
     * Perform the following algorithm to update the model data associated with
     * this UIInput, if any, as appropriate.
     *
     * If the valid property of filechooser is false, take no further action. If
     * the localValueSet property of this component is false, take no further
     * action. If no ValueBinding for value exists, take no further action. Call
     * setValue() method of the ValueBinding to update the value that the
     * ValueBinding points at. If the setValue() method returns successfully: o
     * Clear the local value of this UIInput. o Set the localValueSet property
     * of this UIInput to false. If the setValue() method call fails: enqueue
     * an error message by calling addMessage() on the specified FacesContext
     * instance. o Set the valid property of this UIInput to false.
     *
     */
    @Override
    public void updateModel(final FacesContext context) {

        // Update the individual component values from the model
        // and then update the fileChooser component.
        if (context == null) {
            throw new NullPointerException();
        }

        if (!isValid() || !isLocalValueSet()) {
            return;
        }

        ValueExpression vb = getValueExpression("selected");
        if (vb != null) {
            try {
                vb.setValue(context.getELContext(), getLocalValue());
                setValue(null);
                setLocalValueSet(false);
            } catch (FacesException e) {
                FacesMessage message
                        = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                CONVERSION_MESSAGE_ID, e.getMessage());
                context.addMessage(getClientId(context), message);
                setValid(false);
            } catch (IllegalArgumentException e) {
                FacesMessage message
                        = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                CONVERSION_MESSAGE_ID, e.getMessage());
                context.addMessage(getClientId(context), message);
                setValid(false);
            } catch (Exception e) {
                FacesMessage message
                        = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                CONVERSION_MESSAGE_ID, e.getMessage());
                context.addMessage(getClientId(context), message);
                setValid(false);
            }
        }

    }

    // EditableValueMethods
    // this is the overriden method of UIInput.
    /**
     * Create a value for the fileChooser component based on the submitted
     * value, which are the user selections. The selections may be absolute or
     * relative paths. The result is an array of objects.
     * @param context faces context
     * @param component UI component
     * @param submittedValue value to convert
     * @return an object that reflects the value of the fileChooser component.
     * @throws ConverterException if an error occurs
     */
    public Object getConvertedValue(final FacesContext context,
            final UIComponent component, final Object submittedValue)
            throws ConverterException {

        // First defer to the renderer.
        //
        Renderer renderer = getRenderer(context);
        if (renderer != null) {
            return renderer.getConvertedValue(context, this, submittedValue);
        }
        return getConvertedValue(context, (FileChooser) component,
                submittedValue);
    }

    /**
     * Overloaded getConvertedValue called by our renderer.
     *
     * We have this method because the we want the implementation of
     * getConvertedValue to exist in the component and not solely in the
     * renderer. However JSF by convention defers to the renderer first in
     * getConvertedValue and if there isn't a renderer will
     * getConvetedValue(FacesContext, UIComponent, Object) calls this method.
     * Typically our renderer is registered as the renderer for this component.
     * Therefore it calls this method to obtain the the value when the other
     * getConvertedValue is called and tries execute its getConvertedValue.
     * @param context faces context
     * @param chooser file chooser
     * @param submittedValue value to convert
     * @return Object
     * @throws ConverterException if an error occurs
     */
    @SuppressWarnings({"unchecked", "checkstyle:MethodLength"})
    public Object getConvertedValue(final FacesContext context,
            final FileChooser chooser, final Object submittedValue)
            throws ConverterException {

        // No need to check for null, getModel throws FacesException if null.
        ResourceModel zModel = chooser.getModel();

        if (submittedValue == null) {
            return null;
        }

        if (!(submittedValue instanceof String[])) {
            String msg = "FileChooser getConvertedValue requires "
                    +
                    "String[] for its submittedValue.";
            log(msg);
            throw new ConverterException(msg);
        }

        // FIXME: We should strive to make this Object[] or ResourceItem[]
        // and try to convert anonymously, as needed.
        Object[] chooserValues = null;
        try {
            // Need to record any change to the current directory
            // from a selection. Unfortunately I haven't figured
            // out a way to do this in an elegant way.
            lastOpenFolder = zModel.getCurrentDir();

            // It really returns Object but for now assume File[]
            if (isFolderChooser()) {
                chooserValues = (Object[]) zModel.getSelectedContent((String[])
                        submittedValue,
                        true);
            } else if (isFileAndFolderChooser()) {
                Object[] fVals = (Object[]) zModel.getSelectedContent((String[])
                        submittedValue, false);
                Object[] dVals = (Object[]) zModel.getSelectedContent((String[])
                        submittedValue, true);
                int dsize = 0;
                if (dVals != null) {
                    dsize = dVals.length;
                }
                int fsize = 0;
                if (fVals != null) {
                    fsize = fVals.length;
                }
                if (fsize + dsize > 0) {
                    if (fVals instanceof File[]) {
                        chooserValues = new File[dsize + fsize];
                    } else {
                        chooserValues = new Object[dsize + fsize];
                    }
                    int k = 0;
                    for (int i = 0; i < dsize; i++) {
                        chooserValues[k++] = dVals[i];
                    }
                    for (int j = 0; j < fsize; j++) {
                        chooserValues[k++] = fVals[j];
                    }
                }
            } else {
                chooserValues = (Object[]) zModel.getSelectedContent((String[])
                        submittedValue, false);
            }
            // Set a flag if the directory has changed.
            openFolderChanged = !lastOpenFolder.equals(zModel.getCurrentDir());

        } catch (ResourceModelException cme) {
            FacesMessage fmsg = cme.getFacesMessage();
            context.addMessage(getClientId(context), fmsg);
            String msg;
            if (fmsg.getDetail() == null) {
                msg = fmsg.getSummary();
            } else {
                msg = fmsg.getDetail();
            }
            throw new ConverterException(msg);
        }

        // Need to coordinate this with "DB null values" strategy.
        if (chooserValues == null || chooserValues.length == 0) {
            return null;
        }

        // For now, if the developer chooses to use a custom model then
        // no converter will be supplied. The backing bean has to be
        // aware of what the model was and read the value of the component
        // accordingly.
        if (!(chooserValues instanceof File[])) {
            return chooserValues;
        }

        File[] realChooserValues = (File[]) chooserValues;

        // In case its a value binding
        boolean isMultiple = isMultiple();

        // Try and get a converter
        Object value = getValue();
        Converter converter = ((ValueHolder) chooser).getConverter();
        if (converter == null) {
            converter = getConverterFromValue(value);
        }

        // If there's no value binding or existing value,
        // convert if necessary or return File[] or File
        ValueExpression valueExpr = chooser.getValueExpression("value");
        Class vclazz = null;
        if (valueExpr != null) {
            vclazz = valueExpr.getType(context.getELContext());
        } else if (value != null) {
            vclazz = value.getClass();
        }

        // Default to File as native type.
        if (vclazz == null) {
            if (isMultiple) {
                if (converter == null) {
                    return realChooserValues;
                } else {
                    return convertFileArrayToObjectArray(context, converter,
                            realChooserValues);
                }
            } else {
                if (converter == null) {
                    return realChooserValues[0];
                } else {
                    return convertFileToObject(context, converter,
                            realChooserValues[0]);
                }
            }
        }

        if (isMultiple) {
            if (vclazz.isArray()) {
                // File[] and String[] special case
                if (converter == null) {
                    if (vclazz.getComponentType().isAssignableFrom(
                            java.io.File.class)) {
                        return realChooserValues;
                    } else if (vclazz.getComponentType().isAssignableFrom(
                            java.lang.String.class)) {
                        return convertFileArrayToStringArray(realChooserValues);
                    }
                } else {
                    // Convert to object with a converter.
                    return convertFileArrayToObjectArray(context, converter,
                            realChooserValues);
                }
            } else {
                List list = null;
                if (vclazz.isAssignableFrom(ArrayList.class)) {
                    list = new ArrayList();
                } else if (vclazz.isAssignableFrom(Vector.class)) {
                    list = new Vector();
                } else if (vclazz.isAssignableFrom(LinkedList.class)) {
                    list = new LinkedList();
                } else {
                    try {
                        list = (java.util.List) vclazz.newInstance();
                    } catch (Throwable t) {
                        throw new ConverterException("FileChooser is configured"
                                + " for multiple selection but the value is not"
                                + " bound to an assignable type.");
                    }
                }
                // Create the list of converted or File types.
                return convertFileArrayToList(context, converter,
                        realChooserValues, list);
            }
        } else {

            if (converter != null) {
                return converter.getAsObject(context, chooser,
                        convertFileToString(realChooserValues[0]));
            }

            if (vclazz.isAssignableFrom(java.io.File.class)) {
                return realChooserValues[0];
            } else if (vclazz.isAssignableFrom(java.lang.String.class)) {
                return convertFileToString(realChooserValues[0]);
            } else {
                return (Object) realChooserValues[0];
            }
        }
        // We shouldn't get here but if we do return null.
        return null;
    }

    // Converters
    //
    // FIXME: Some of these should be in the model. Especially if native type
    // of the model is more Opaque like ChooserItem
    // Then the ChooseItem would provide the converters.
    //
    /**
     * Get the convert for a given value.
     * @param value value to process
     * @return Converter
     */
    private Converter getConverterFromValue(final Object value) {

        if (value == null) {
            return null;
        }
        Converter converter = null;
        try {
            Class clazz = value.getClass();
            if (isMultiple()) {

                if (clazz.isArray()) {
                    clazz = clazz.getComponentType();
                } else if (value instanceof List
                        && !((List) value).isEmpty()) {
                    Object listItem = ((List) value).get(0);
                    if (listItem != null) {
                        clazz = listItem.getClass();
                    } else {
                        clazz = null;
                    }
                } else {
                    // Can't figure out for multiple return null
                    clazz = null;
                    log("Failed to obtain a class for the FileChooser multiple"
                            + " value.");
                    return null;
                }

            }
            if (clazz != null) {
                converter = ConversionUtilities.getConverterForClass(clazz);
            }

        } catch (Exception e) {
            // Proceed but log the error
            String msg
                    = "Failed to obtain a class for FileChooser value.";
            log(msg + "\nException: " + e.getStackTrace());
        }
        return converter;
    }

    /**
     * If converter is not null return an Object[] where each entry is converted
     * by applying the converted to each entry in fileArray. If converter is
     * null return fileArray. If fileArray is null return null;
     * @param context faces context
     * @param converter converter to use
     * @param fileArray array to convert
     * @return Object
     * @throws ConverterException if an error occurs
     */
    protected Object convertFileArrayToObjectArray(final FacesContext context,
            final Converter converter, final File[] fileArray)
            throws ConverterException {

        // What does it mean if fileArray.length == 0 ?
        if (fileArray == null) {
            return null;
        }
        Object[] objArray = new Object[fileArray.length];
        if (converter == null) {
            for (int i = 0; i < fileArray.length; ++i) {
                objArray[i] = fileArray[i];
            }
        } else {
            for (int i = 0; i < fileArray.length; ++i) {
                objArray[i] = converter.getAsObject(context, this,
                        convertFileToString(fileArray[i]));
            }
        }
        return objArray;
    }

    /**
     * Convert a file array to a list.
     * @param context faces context
     * @param converter converter to use
     * @param fileArray file array to convert
     * @param list destination list
     * @return List
     * @throws ConverterException if an error occurs
     */
    @SuppressWarnings("unchecked")
    protected List convertFileArrayToList(final FacesContext context,
            final Converter converter, final File[] fileArray,
            final List list) throws ConverterException {

        if (list == null) {
            return null;
        }
        if (converter != null) {
            for (int i = 0; i < fileArray.length; ++i) {
                list.add(converter.getAsObject(context, this,
                        convertFileToString(fileArray[i])));
            }
        } else {
            for (int i = 0; i < fileArray.length; ++i) {
                list.add(fileArray[i]);
            }
        }
        return list;
    }

    /**
     * If converter is not null return an Object that was converted by
     * converter. If converter is null, return file.
     * @param context faces context
     * @param converter converter to use
     * @param file file to convert
     * @return Object
     * @throws ConverterException if an error occurs
     */
    protected Object convertFileToObject(final FacesContext context,
            final Converter converter, final File file)
            throws ConverterException {

        if (converter == null) {
            return file;
        }
        String fileString = convertFileToString(file);
        if (fileString == null) {
            return null;
        }
        return converter.getAsObject(context, this, fileString);
    }

    /**
     * Convert a file to a string.
     * @param file file to convert
     * @return String
     */
    protected static String convertFileToString(final File file) {

        // using getAbsolutePath path for this is a policy
        // issue and need to make sure this is consistent
        // and expected.
        if (file != null) {
            return file.getAbsolutePath();
        }
        return null;
    }

    /**
     * Convert a file array to a string array.
     * @param fileArray array to convert
     * @return String[]
     */
    protected static String[] convertFileArrayToStringArray(
            final File[] fileArray) {

        if (fileArray == null) {
            return null;
        }
        String[] strArray = new String[fileArray.length];
        for (int i = 0; i < fileArray.length; ++i) {
            strArray[i] = convertFileToString(fileArray[i]);
        }
        return strArray;
    }


    /**
     * Convert a value to a string array.
     * @param context faces context
     * @param converter converter to use
     * @param value value to convert
     * @return String[]
     * @throws ConverterException if an error occurs
     */
    protected String[] convertValueToStringArray(final FacesContext context,
            final Converter converter, final Object value)
            throws ConverterException {

        // If there's no value just return null.
        if (value == null) {
            return null;
        }

        Class vclazz = value.getClass();
        if (!isMultiple()) {
            return new String[]{
                convertValueToString(context, converter, value)};
        } else {
            if (vclazz.isArray()) {
                return convertObjectArrayToStringArray(context, converter,
                        (Object[]) value);
            } else if (value instanceof List) {
                return convertObjectListToStringArray(context, converter,
                        (List) value);
            }
            String msg
                    = "FileChooser is configured for multiple selection "
                    + "but the value is not an assignable type.";
            log(msg);
            throw new ConverterException(msg);
        }
    }

    /**
     * Convert a value to a string.
     * @param context faces context
     * @param converter converter to use
     * @param value value to convert
     * @return String
     * @throws ConverterException if an error occurs
     */
    @SuppressWarnings("unchecked")
    protected String convertValueToString(final FacesContext context,
            final Converter converter, final Object value)
            throws ConverterException {

        if (value == null) {
            return null;
        }
        if (converter != null) {
            return converter.getAsString(context, this, value);
        } else if (value instanceof File) {
            return convertFileToString((File) value);
        } else if (value instanceof String) {
            return (String) value;
        } else {
            // Instead of bailing just return "toString".
            log("Resorting to object.toString() to convert single "
                    + "value to String.");
            return value.toString();
        }
    }

    /**
     * Convert an object array to a string array.
     * @param context faces context
     * @param converter converter to use
     * @param value value to convert
     * @return String[]
     * @throws ConverterException if an error occurs
     */
    @SuppressWarnings("unchecked")
    protected String[] convertObjectArrayToStringArray(
            final FacesContext context, final Converter converter,
            final Object[] value) throws ConverterException {

        if (value == null) {
            return null;
        }

        if (converter != null) {
            String[] strArray = new String[value.length];
            for (int i = 0; i < value.length; ++i) {
                strArray[i] = converter.getAsString(context, this, value[i]);
            }
            return strArray;
        } else {
            if (value instanceof File[]) {
                return convertFileArrayToStringArray((File[]) value);
            } else if (value instanceof String[]) {
                return (String[]) value;
            } else {
                // Instead of bailing just return "toString".
                //
                log("Resorting to object.toString() to convert multiple "
                        + "array value to String[].");
                String[] strArray = new String[value.length];
                for (int i = 0; i < value.length; ++i) {
                    strArray[i] = value[i].toString();
                }
                return strArray;
            }
        }
    }

    /**
     * Convert a list of object to a string array.
     * @param context faces context
     * @param converter converter to use
     * @param list list to convert
     * @return String[]
     * @throws ConverterException if an error occurs
     */
    @SuppressWarnings("unchecked")
    protected String[] convertObjectListToStringArray(
            final FacesContext context, final Converter converter,
            final List list) throws ConverterException {

        if (list == null) {
            return null;
        }

        if (converter != null) {
            String[] strArray = new String[list.size()];
            for (int i = 0; i < list.size(); ++i) {
                strArray[i] = converter.getAsString(context, this,
                        list.get(i));
            }
            return strArray;
        } else {
            // Try and find out what type we have by looking at the
            // first List value.
            // To be consistent
            String[] strArray = new String[list.size()];
            if (list.isEmpty()) {
                return strArray;
            }
            Object listItem = list.get(0);
            if (listItem instanceof File) {
                File[] fileArray = new File[list.size()];
                fileArray = (File[]) list.toArray(fileArray);
                return convertFileArrayToStringArray(fileArray);
            } else if (listItem instanceof String) {
                return (String[]) list.toArray(strArray);
            } else {
                // Instead of bailing just return "toString".
                log("Resorting to object.toString() to convert multiple "
                        + "list value to String[].");
                for (int i = 0; i < list.size(); ++i) {
                    strArray[i] = list.get(i).toString();
                }
                return strArray;
            }
        }
    }

    /**
     * This method returns a List box containing the list of resources selected
     * by the user.
     * @param fileList list box
     * @return ListBox
     */
    private Listbox populateListComponent(final Listbox fileList) {

        // No need to check for null, getModel throws FacesException if null.
        ResourceModel zModel = getModel();
        ResourceItem[] items;
        // If a folder chooser always disable files

        items = zModel.getFolderContent(zModel.getCurrentDir(),
                isFolderChooser(), false);

        // FIXME: Need more well defined data here for the
        // list options. We probably want files read only
        // in a folder chooser, folders read only or
        // disabled whichever allows a javascript event
        // to open a folder but not to select it for
        // submission.
        if (items != null && items.length != 0) {
            Option[] optList = new Option[items.length];
            for (int i = 0; i < items.length; i++) {
                optList[i] = new Option(items[i].getItemKey(),
                        items[i].getItemLabel());
                optList[i].setDisabled(items[i].isItemDisabled());
            }
            fileList.setItems(optList);
        } else {
            populateEmptyList(fileList);
        }
        return fileList;
    }

    /**
     * Populate the specified empty list box.
     * @param fileList list box
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private static void populateEmptyList(final Listbox fileList) {

        Theme theme = getTheme();

        // FIXME: Consider having a format string in the Theme
        int fileNameLen = Integer.parseInt(theme
                .getMessage("filechooser.fileNameLen"));
        int fileSizeLen = Integer.parseInt(theme
                .getMessage("filechooser.fileSizeLen"));
        int fileDateLen = Integer.parseInt(theme
                .getMessage("filechooser.fileDateLen"));

        // no files or directories exist
        // A line with "--------" has to be added to make the list box
        // have a width
        String label = "";
        String value = "0";
        // int len = fileNameLen + fileSizeLen + fileDateLen + 6;
        int len = fileNameLen + fileSizeLen + fileDateLen + 10;
        for (int i = 0; i < len; i++) {
            label += HYPHEN;
        }
        Option[] fileEntries = new Option[1];
        fileEntries[0] = new Option(value, label);
        fileEntries[0].setDisabled(true);
        fileList.setItems(fileEntries);
    }

    // Since the server text label does not use the for
    // attribute, neither the StaticText component or
    // label need to be placed in the facet map.
    /**
     * Return a component that implements the server name field. If a facet
     * named {@code serverNameText} is found that component is returned.
     * Otherwise a {@code StaticText} component is returned. It is assigned
     * the id
     * {@code getId() + "_serverNameText"}
     * <p>
     * If the facet is not defined then the returned {@code StaticText}
     * component is recreated every time this method is called.
     * </p>
     *
     * @return the server name field component
     */
    public UIComponent getServerNameText() {

        UIComponent facet = getFacet(FILECHOOSER_SERVERNAME_STATICTEXT_FACET);
        if (facet != null) {
            return facet;
        }

        StaticText child = new StaticText();
        child.setId(ComponentUtilities.createPrivateFacetId(this,
                FILECHOOSER_SERVERNAME_STATICTEXT_FACET));
        child.setParent(this);

        Theme theme = getTheme();
        child.setText(theme.getMessage("filechooser.lookinColumn"));
        child.setStyleClass(theme
                .getStyleClass(ThemeStyles.FILECHOOSER_NAME_TXT));

        // No need to check for null, getModel throws FacesException if null.
        //
        ResourceModel zModel = getModel();
        // Defaults to "localhost"
        //
        String serverName = zModel.getServerName();
        child.setText(serverName);
        return child;
    }

    /**
     * Return a component that implements the inline help for the filter text
     * field. If a facet named {@code enterPressHelp} is found that
     * component is returned. Otherwise a {@code HelpInline} component is
     * returned. It is assigned the id
     * {@code getId() + "_enterPressHelp"}
     * <p>
     * If the facet is not defined then the returned {@code StaticText}
     * component is created every time this method is called.
     * </p>
     *
     * @return the inline help component
     */
    public UIComponent getEnterInlineHelp() {

        UIComponent facet = getFacet(FILECHOOSER_ENTERPRESS_HELP_FACET);
        if (facet != null) {
            return facet;
        }

        HelpInline child = new HelpInline();
        child.setId(ComponentUtilities.createPrivateFacetId(this,
                FILECHOOSER_ENTERPRESS_HELP_FACET));
        child.setParent(this);

        Theme theme = getTheme();
        child.setText(theme.getMessage("filechooser.enterKeyHelp"));
        child.setType("field");
        return child;
    }

    /**
     * Return a component that implements the inline help for selecting multiple
     * rows from the listbox. If the {@code isMultiple} returns false, null
     * is returned.
     * If a facet named {@code multiSelectHelp} is found that component is
     * returned. Otherwise a {@code HelpInline} component is returned. It
     * is assigned the id
     * {@code getId() + "_multiSelectHelp"}
     * <p>
     * If the facet is not defined then the returned {@code HelpInline}
     * component is created every time this method is called.
     * </p>
     *
     * @return the inline help component
     */
    public UIComponent getMultiSelectHelp() {

        if (!isMultiple()) {
            return null;
        }

        UIComponent facet = getFacet(FILECHOOSER_MULTISELECT_HELP_FACET);
        if (facet != null) {
            return facet;
        }

        HelpInline child = new HelpInline();
        child.setId(ComponentUtilities.createPrivateFacetId(this,
                FILECHOOSER_MULTISELECT_HELP_FACET));
        child.setParent(this);

        Theme theme = getTheme();
        child.setText(theme.getMessage("filechooser.multiSelectHelp"));
        child.setType("field");

        return child;
    }

    /**
     * Return a component that implements the server name field label. If a
     * facet named {@code serverLabel} is found that component is returned.
     * Otherwise a {@code Label} component is returned. It is assigned the
     * id
     * {@code getId() + "_serverLabel"}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is created every time this method is called.
     * </p>
     *
     * @return the server name field label component
     */
    public UIComponent getServerNameLabel() {

        UIComponent facet = getFacet(FILECHOOSER_SERVERNAME_LABEL_FACET);
        if (facet != null) {
            return facet;
        }

        Label child = new Label();
        child.setId(ComponentUtilities.createPrivateFacetId(this,
                FILECHOOSER_SERVERNAME_LABEL_FACET));
        child.setParent(this);

        // Should be in theme
        //
        child.setLabelLevel(2);

        Theme theme = getTheme();
        child.setText(theme.getMessage("filechooser.serverPrompt"));
        child.setStyleClass(theme
                .getStyleClass(ThemeStyles.LABEL_LEVEL_TWO_TEXT));

        return child;
    }

    /**
     * Return a component that implements the title text. If a facet named
     * {@code fileChooserLabel} is found that component is returned.
     * Otherwise a {@code StaticText} component is returned. It is assigned
     * the id
     * {@code getId() + "_fileChooserLabel"}
     * <p>
     * If the facet is not defined then the returned {@code StaticText}
     * component is created every time this method is called.
     * </p>
     *
     * @return the FileChooser title component
     */
    public UIComponent getFileChooserTitle() {

        UIComponent facet = getFacet(FILECHOOSER_LABEL_FACET);
        if (facet != null) {
            return facet;
        }

        StaticText child = new StaticText();
        child.setId(ComponentUtilities.createPrivateFacetId(this,
                FILECHOOSER_LABEL_FACET));
        child.setParent(this);

        Theme theme = getTheme();
        child.setText(theme.getMessage("filechooser.title"));

        return child;
    }

    /**
     * Return a component that implements the look in input field. If a facet
     * named {@code lookinField} is found that component is returned.
     * Otherwise a {@code TextField} component is returned. It is assigned
     * the id
     * {@code getId() + "_lookinField"}
     * <p>
     * If the facet is not defined then the returned {@code TextField}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return the look in input field component
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public UIComponent getLookInTextField() {

        UIComponent facet = getFacet(FILECHOOSER_LOOKIN_TEXTFIELD_FACET);
        if (facet != null) {
            return facet;
        }

        TextField child = (TextField) ComponentUtilities.getPrivateFacet(this,
                FILECHOOSER_LOOKIN_TEXTFIELD_FACET, true);
        if (child == null) {
            child = new TextField();
            child.setId(ComponentUtilities.createPrivateFacetId(this,
                    FILECHOOSER_LOOKIN_TEXTFIELD_FACET));

            child.addValidator(new FileChooserLookInValidator());
            child.setSubmittedValue(getModel().getCurrentDir());
            ComponentUtilities.putPrivateFacet(this,
                    FILECHOOSER_LOOKIN_TEXTFIELD_FACET, child);
        }

        Theme theme = getTheme();
        child.setColumns(Integer.parseInt(theme
                .getMessage("filechooser.lookinColumn")));
        child.setStyleClass(
                theme.getStyleClass(
                        ThemeStyles.FILECHOOSER_NAME_TXT)
                        .concat(" ")
                        .concat(theme.getStyleClass(
                                ThemeStyles.FILECHOOSER_WIDTH_TXT)));

        int tindex = getTabIndex();
        if (tindex > 0 && tindex < 32767) {
            child.setTabIndex(tindex);
        }

        return child;
    }

    /**
     * Return a component that implements the look in input field label. If a
     * facet named {@code lookinLabel} is found that component is returned.
     * Otherwise a {@code Label} component is returned. It is assigned the
     * id
     * {@code getId() + "_lookinLabel"}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return the look in input field label component
     */
    public UIComponent getLookInLabel() {

        UIComponent facet = getFacet(FILECHOOSER_LOOKIN_LABEL_FACET);
        if (facet != null) {
            return facet;
        }

        Label child = (Label) ComponentUtilities.getPrivateFacet(this,
                FILECHOOSER_LOOKIN_LABEL_FACET, true);
        if (child == null) {
            child = new Label();
            child.setId(ComponentUtilities.createPrivateFacetId(this,
                    FILECHOOSER_LOOKIN_LABEL_FACET));

            // Should be in theme
            child.setLabelLevel(2);
            ComponentUtilities.putPrivateFacet(this,
                    FILECHOOSER_LOOKIN_LABEL_FACET, child);
        }

        Theme theme = getTheme();
        child.setText(theme.getMessage("filechooser.lookin"));
        child.setFor(getLookInTextField().getClientId(getFacesContext()));
        return child;
    }

    /**
     * Return a component that implements the filter input field. If a facet
     * named {@code filterField} is found that component is returned.
     * Otherwise a {@code TextField} component is returned. It is assigned
     * the id
     * {@code getId() + "_filterField"}
     * <p>
     * If the facet is not defined then the returned {@code TextField}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return the filter input field component
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public UIComponent getFilterTextField() {

        UIComponent facet = (TextField)
                getFacet(FILECHOOSER_FILTERON_TEXTFIELD_FACET);
        if (facet != null) {
            return facet;
        }

        TextField child = (TextField) ComponentUtilities.getPrivateFacet(this,
                FILECHOOSER_FILTERON_TEXTFIELD_FACET, true);
        if (child == null) {
            child = new TextField();
            child.setId(ComponentUtilities.createPrivateFacetId(this,
                    FILECHOOSER_FILTERON_TEXTFIELD_FACET));

            child.addValidator(new FileChooserFilterValidator());
            child.setSubmittedValue(getModel().getFilterValue());
            ComponentUtilities.putPrivateFacet(this,
                    FILECHOOSER_FILTERON_TEXTFIELD_FACET, child);
        }

        FacesContext context = FacesContext.getCurrentInstance();
        ClientSniffer sniffer = ClientSniffer.getInstance(context);

        // Needs to be in Theme.
        int size;
        if (sniffer.isNav6up()) {
            size = 32;
        } else {
            size = 18;
        }
        child.setColumns(size);

        int tindex = getTabIndex();
        if (tindex > 0 && tindex < 32767) {
            child.setTabIndex(tindex);
        }
        return child;
    }

    /**
     * Return a component that implements the filter input field label. If a
     * facet named {@code filterLabel} is found that component is returned.
     * Otherwise a {@code Label} component is returned. It is assigned the
     * id
     * {@code getId() + "_filterLabel"}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return the filter input field label component
     */
    public UIComponent getFilterLabel() {
        UIComponent facet = getFacet(FILECHOOSER_FILTER_LABEL_FACET);
        if (facet != null) {
            return facet;
        }

        Label child = (Label) ComponentUtilities.getPrivateFacet(this,
                FILECHOOSER_FILTER_LABEL_FACET, true);
        if (child == null) {
            child = new Label();
            child.setId(ComponentUtilities.createPrivateFacetId(this,
                    FILECHOOSER_FILTER_LABEL_FACET));

            // Should be in theme
            child.setLabelLevel(2);
            ComponentUtilities.putPrivateFacet(this,
                    FILECHOOSER_FILTER_LABEL_FACET, child);
        }

        Theme theme = getTheme();
        if (isFolderChooser()) {
            child.setText(
                    theme.getMessage("filechooser.folderFilter"));
        } else {
            child.setText(
                    theme.getMessage("filechooser.fileFilter"));
        }
        child.setFor(getFilterTextField().getClientId(getFacesContext()));
        return child;
    }

    /**
     * Return a component that implements the selected file(s) or folder(s)
     * input field. If a facet named {@code selectedField} is found that
     * component is returned. Otherwise a {@code TextField} component is
     * returned. It is assigned the id
     * {@code getId() + "_selectedField"}
     * <p>
     * If the facet is not defined then the returned {@code TextField}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return the select text field component. This text field displays the
     * list of selected items.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public UIComponent getSelectedTextField() {

        UIComponent facet;
        facet = getFacet(FILECHOOSER_SELECTED_TEXTFIELD_FACET);
        if (facet != null) {
            return facet;
        }

        TextField child = (TextField) ComponentUtilities.getPrivateFacet(this,
                FILECHOOSER_SELECTED_TEXTFIELD_FACET, true);
        if (child == null) {
            child = new TextField();
            child.setId(ComponentUtilities.createPrivateFacetId(this,
                    FILECHOOSER_SELECTED_TEXTFIELD_FACET));

            child.addValidator(new FileChooserSelectValidator());
            ComponentUtilities.putPrivateFacet(this,
                    FILECHOOSER_SELECTED_TEXTFIELD_FACET, child);
        }

        Theme theme = getTheme();
        if (isMultiple()) {
            child.setColumns(Integer.parseInt(theme
                    .getMessage("filechooser.multipleColumn")));
        } else {
            child.setColumns(Integer.parseInt(theme
                    .getMessage("filechooser.singleColumn")));
        }

        if (isFolderChooser()) {
            child.setStyleClass(theme
                    .getStyleClass(ThemeStyles.FILECHOOSER_FOLD_STYLE));
        } else {
            child.setStyleClass(theme
                    .getStyleClass(ThemeStyles.FILECHOOSER_FILE_STYLE));
        }

        int tindex = getTabIndex();
        if (tindex > 0 && tindex < 32767) {
            child.setTabIndex(tindex);
        }

        // FIXME: Need to add onblur handler.
        // This will be the only way to control the
        // chooser button correctly when a user enters a
        // value manually.
        return child;
    }

    // This label does not use the for attribute.
    // Therefore it does not necessarily have to be placed in the
    // facet map.
    /**
     * Return a component that implements the selected file(s) or folder(s)
     * input field label. If a facet named {@code selectedLabel} is found
     * that component is returned. Otherwise a {@code Label} component is
     * returned. It is assigned the id
     * {@code getId() + "_selectedLabel"}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return - returns the selected text field label component
     */
    public UIComponent getSelectLabel() {

        UIComponent facet = getFacet(FILECHOOSER_SELECT_LABEL_FACET);
        if (facet != null) {
            return facet;
        }

        Label child = (Label) ComponentUtilities.getPrivateFacet(this,
                FILECHOOSER_SELECT_LABEL_FACET, true);
        if (child == null) {
            child = new Label();
            child.setId(ComponentUtilities.createPrivateFacetId(this,
                    FILECHOOSER_SELECT_LABEL_FACET));

            // Should be in theme
            child.setLabelLevel(2);
            ComponentUtilities.putPrivateFacet(this,
                    FILECHOOSER_SELECT_LABEL_FACET, child);
        }

        Theme theme = getTheme();
        child.setFor(getSelectedTextField().getClientId(getFacesContext()));

        boolean ismultiple = isMultiple();
        String labelKey;
        if (isFolderChooser()) {
            if (ismultiple) {
                labelKey = "filechooser.selectedFolders";
            } else {
                labelKey = "filechooser.selectedFolder";
            }
        } else if (isFileAndFolderChooser()) {
            if (ismultiple) {
                labelKey = "filechooser.selectedFileAndFolders";
            } else {
                labelKey = "filechooser.selectedFileAndFolder";
            }
        } else {
            if (ismultiple) {
                labelKey = "filechooser.selectedFiles";
            } else {
                labelKey = "filechooser.selectedFile";
            }
        }
        child.setText(theme.getMessage(labelKey));
        return child;
    }

    /**
     * Return a component that implements the sort criteria menu. If a facet
     * named {@code sortMenu} is found that component is returned.
     * Otherwise a {@code DropDown} component is returned. It is assigned
     * the id
     * {@code getId() + "_sortMenu"}
     * <p>
     * If the facet is not defined then the returned {@code DropDown}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return the drop down sort menu component
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public UIComponent getSortComponent() {
        UIComponent facet = getFacet(FILECHOOSER_SORTMENU_FACET);
        if (facet != null) {
            return facet;

        }

        DropDown jdd = (DropDown) ComponentUtilities.getPrivateFacet(this,
                FILECHOOSER_SORTMENU_FACET, true);
        if (jdd == null) {
            jdd = new DropDown();
            jdd.setId(ComponentUtilities.createPrivateFacetId(this,
                    FILECHOOSER_SORTMENU_FACET));

            jdd.setSubmitForm(true);

            // Should be part of theme
            jdd.setLabelLevel(2);
            jdd.addValidator(new FileChooserSortValidator());
            jdd.setImmediate(true);

            ComponentUtilities.putPrivateFacet(this,
                    FILECHOOSER_SORTMENU_FACET, jdd);
        }

        // FIXME: These sort constants are a function of the
        // chooser model and should be obtained from
        // model. Sort should be some sort of sort class
        // from which one could get the "display" names
        // of the sort options.
        //
        // Possibly even have the model return
        // and Option array. This is a known model type
        // for our components.
        Theme theme = getTheme();
        Option[] sortFields = new Option[6];
        sortFields[0] = new Option(ALPHABETIC_ASC,
                theme.getMessage("filechooser.sortOption1"));
        sortFields[1] = new Option(ALPHABETIC_DSC,
                theme.getMessage("filechooser.sortOption4"));
        sortFields[2] = new Option(LASTMODIFIED_ASC,
                theme.getMessage("filechooser.sortOption2"));
        sortFields[3] = new Option(LASTMODIFIED_DSC,
                theme.getMessage("filechooser.sortOption5"));
        sortFields[4] = new Option(SIZE_ASC,
                theme.getMessage("filechooser.sortOption3"));
        sortFields[5] = new Option(SIZE_DSC,
                theme.getMessage("filechooser.sortOption6"));

        jdd.setItems(sortFields);
        // jdd.setLabel(theme.getMessage("filechooser.sortBy"));
        // jdd.setLabelLevel(2);
        jdd.setStyleClass(theme.getStyleClass(ThemeStyles.MENU_JUMP));

        int tindex = getTabIndex();
        if (tindex > 0 && tindex < 32767) {
            jdd.setTabIndex(tindex);
        }

        // No need to check for null, getModel throws FacesException if null.
        ResourceModel zModel = getModel();
        String sortVal = zModel.getSortValue();

        // first check if model's sortfield is set
        if (sortVal == null) {
            String sField = getSortField();
            if (isDescending()) {
                sField = sField.concat("d");
            } else {
                sField = sField.concat("a");
            }
            jdd.setSelected(sField);
            zModel.setSortValue(sField);
        } else {
            jdd.setSelected(sortVal);
        }
        return jdd;
    }

    /**
     * Return a component that implements the sort criteria menu. If a facet
     * named {@code sortMenu} is found that component is returned.
     * Otherwise a {@code DropDown} component is returned. It is assigned
     * the id
     * {@code getId() + "_sortMenu"}
     * <p>
     * If the facet is not defined then the returned {@code DropDown}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return the drop down sort menu component
     */
    public UIComponent getSortComponentLabel() {

        UIComponent facet = getFacet(FILECHOOSER_SORT_LABEL_FACET);
        if (facet != null) {
            return facet;
        }

        Label child = (Label) ComponentUtilities.getPrivateFacet(this,
                FILECHOOSER_SORT_LABEL_FACET, true);
        if (child == null) {
            child = new Label();
            child.setId(ComponentUtilities.createPrivateFacetId(this,
                    FILECHOOSER_SORT_LABEL_FACET));

            // Should be in theme
            child.setLabelLevel(2);
            ComponentUtilities.putPrivateFacet(this,
                    FILECHOOSER_SORT_LABEL_FACET, child);
        }

        Theme theme = getTheme();
        child.setText(
                theme.getMessage("filechooser.sortBy"));
        child.setFor(getSortComponent().getClientId(getFacesContext()));
        return child;
    }

    /**
     * Return a component that implements the list of files and folders. It is
     * assigned the id
     * {@code getId() + "_listEntries"}
     * <p>
     * The returned {@code Listbox} component is re-initialized every time
     * this method is called.
     * </p>
     *
     * @return the drop down sort menu component
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public UIComponent getListComponent() {

        Listbox fileList = (Listbox) ComponentUtilities.getPrivateFacet(this,
                FILECHOOSER_LISTBOX_FACET, true);
        if (fileList == null) {
            fileList = new Listbox();
            fileList.setId(ComponentUtilities.createPrivateFacetId(this,
                    FILECHOOSER_LISTBOX_FACET));

            // FIXME: This should be in Theme
            fileList.setMonospace(true);
            ComponentUtilities.putPrivateFacet(this,
                    FILECHOOSER_LISTBOX_FACET, fileList);
        }

        fileList.setRows(getRows());

        Theme theme = getTheme();
        if (isFolderChooser()) {
            fileList.setToolTip(theme
                    .getMessage("filechooser.listTitleFolder"));
        } else if (isFileAndFolderChooser()) {
            fileList.setToolTip(theme
                    .getMessage("filechooser.listTitleFileAndFolder"));
        } else {
            fileList.setToolTip(theme
                    .getMessage("filechooser.listTitleFile"));
        }

        fileList.setMultiple(isMultiple());
        fileList.setValue(null);

        int tindex = getTabIndex();
        if (tindex > 0 && tindex < 32767) {
            fileList.setTabIndex(tindex);
        }
        return populateListComponent(fileList);
    }

    /**
     * Return a component that implements the move up button. If a facet named
     * {@code upButton} is found that component is returned. Otherwise a
     * {@code Button} component is returned. It is assigned the id
     * {@code getId() + "_upButton"}
     * <p>
     * If the facet is not defined then the returned {@code Button}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @param newDisabled disabled Flag indicating button is disabled
     * @return the button component for moving up the folder hierarchy
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public UIComponent getUpLevelButton(final boolean newDisabled) {

        UIComponent facet = getFacet(FILECHOOSER_UPLEVEL_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }
        Theme theme = getTheme();

        Button child = (Button) ComponentUtilities.getPrivateFacet(this,
                FILECHOOSER_UPLEVEL_BUTTON_FACET, true);
        if (child == null) {
            child = new Button();
            child.setStyleClass(theme.getStyleClass(
                    ThemeStyles.FILECHOOSER_IMG_BTN));
            child.setId(ComponentUtilities.createPrivateFacetId(this,
                    FILECHOOSER_UPLEVEL_BUTTON_FACET));
            child.setIcon(ThemeImages.FC_UP_1LEVEL);
            child.setImmediate(true);
            ComponentUtilities.putPrivateFacet(this,
                    FILECHOOSER_UPLEVEL_BUTTON_FACET, child);
        }

        child.setText(theme.getMessage("filechooser.upOneLevel"));
        child.setToolTip(
                theme.getMessage("filechooser.upOneLevelTitle"));

        // Disabled should not be passed in.
        // This should either be determined solely on the client
        // or from the model, if there is a parent folder of the
        // current folder. The problem is ensuring that the
        // model has been updated to have the latest data
        // when this method is called.
        //
        child.setDisabled(newDisabled);

        int tindex = getTabIndex();
        if (tindex > 0 && tindex < 32767) {
            child.setTabIndex(tindex);
        }
        return child;
    }

    /**
     * Return a component that implements the open folder button. If a facet
     * named {@code openButton} is found that component is returned.
     * Otherwise a {@code Button} component is returned. It is assigned the
     * id {@code getId() + "_openButton"}
     * <p>
     * If the facet is not defined then the returned {@code Button}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return the OpenFolder button component.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public UIComponent getOpenFolderButton() {
        UIComponent facet = getFacet(FILECHOOSER_OPENFOLDER_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }
        Theme theme = getTheme();
        Button child = (Button) ComponentUtilities.getPrivateFacet(this,
                FILECHOOSER_OPENFOLDER_BUTTON_FACET, true);
        if (child == null) {
            child = new Button();
            child.setId(ComponentUtilities.createPrivateFacetId(this,
                    FILECHOOSER_OPENFOLDER_BUTTON_FACET));

            child.setDisabled(false);
            child.setImmediate(true);
            child.setIcon(ThemeImages.FC_OPEN_FOLDER);
            ComponentUtilities.putPrivateFacet(this,
                    FILECHOOSER_OPENFOLDER_BUTTON_FACET, child);
        }

        child.setText(theme.getMessage("filechooser.openFolder"));
        child.setToolTip(
                theme.getMessage("filechooser.openFolderTitle"));

        int tindex = getTabIndex();
        if (tindex > 0 && tindex < 32767) {
            child.setTabIndex(tindex);
        }
        return child;
    }

    // I don't think this is ever used on the client any more.
    // This method is referenced in the renderer but I don't think
    // the client javascript "clicks" it anymore.
    /**
     * Get a hidden button. In order to associate all user actions with an
     * ActionEvent and have a single ActionListener to listen for these events a
     * hidden button is being created to monitor changes in text filed values.
     * When a user enters data in a text field and hits enter a click of this
     * hidden button will be initiated using JS.
     *
     * @return the hidden button component.
     */
    public UIComponent getHiddenFCButton() {

        Button child = (Button) ComponentUtilities.getPrivateFacet(this,
                FILECHOOSER_HIDDEN_BUTTON_FACET, true);
        if (child == null) {
            child = new Button();
            child.setId(ComponentUtilities.createPrivateFacetId(this,
                    FILECHOOSER_HIDDEN_BUTTON_FACET));

            child.setPrimary(true);
            ComponentUtilities.putPrivateFacet(this,
                    FILECHOOSER_HIDDEN_BUTTON_FACET, child);
        }
        return child;
    }

    /**
     * This method handles the display of error messages.
     *
     * @param summary The error message summary
     * @param detail The error message detail
     * @param summaryArgs summary message arguments
     * @param detailArgs detail message arguments
     */
    public void displayAlert(final String summary, final String detail,
            final String[] summaryArgs, final String[] detailArgs) {

        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage fmsg = createFacesMessage(summary, detail,
                summaryArgs, detailArgs);
        context.addMessage(getClientId(context), fmsg);
    }

    // private convenience methods. Some may be useful for
    // general utilities.
    /**
     * Log an error only used during development time.
     * @param msg message to log
     */
    private static void log(final String msg) {
        if (LogUtil.fineEnabled(FileChooser.class)) {
            LogUtil.fine(FileChooser.class, msg);
        }
    }

    /**
     * Convenience function to get the current Theme.
     * @return String
     */
    private String getEncodedSelections() {

        Object value = getValue();
        Converter converter = getConverter();
        if (converter == null) {
            converter = getConverterFromValue(value);
        }
        String[] selections = null;
        try {
            selections = convertValueToStringArray(
                    FacesContext.getCurrentInstance(), converter, value);
        } catch (ConverterException ce) {
            log("Failed to convert and encode initial selections.");
        }
        return encodeSelections(selections, getEscapeChar(),
                getDelimiterChar());
    }

    /**
     * Utility method to get the theme.
     * @return Theme
     */
    private static Theme getTheme() {
        return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }

    /**
     * This method creates a FacesMessage.
     *
     * @param summary The error message summary
     * @param detail The error message detail
     * @param summaryArgs summary message arguments
     * @param detailArgs detail message arguments
     * @return FacesMessage
     */
    private static FacesMessage createFacesMessage(final String summary,
            final String detail, final String[] summaryArgs,
            final String[] detailArgs) {

        FacesContext context = FacesContext.getCurrentInstance();
        Theme theme = ThemeUtilities.getTheme(context);
        String summaryMsg = theme.getMessage(summary, summaryArgs);
        String detailMsg = theme.getMessage(detail, detailArgs);
        FacesMessage fmsg = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                summaryMsg, detailMsg);
        return fmsg;
    }

    @Override
    public ValueExpression getValueExpression(final String name) {
        if (name.equals("selected")) {
            return super.getValueExpression("value");
        }
        return super.getValueExpression(name);
    }

    @Override
    public void setValueExpression(final String name,
            final ValueExpression binding) {

        if (name.equals("selected")) {
            super.setValueExpression("value", binding);
            return;
        }
        super.setValueExpression(name, binding);
    }

    // Hide required
    @Property(name = "required", isHidden = true, isAttribute = false)
    @Override
    public boolean isRequired() {
        return super.isRequired();
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     * @return {@code boolean}
     */
    public boolean isVisible() {
        if (this.visibleSet) {
            return this.visible;
        }
        ValueExpression vb = getValueExpression("visible");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return this.visible;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     *
     * @see #isVisible()
     * @param newVisible visible
     */
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    /**
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     * @return int
     */
    public int getTabIndex() {
        if (this.tabIndexSet) {
            return this.tabIndex;
        }
        ValueExpression vb = getValueExpression("tabIndex");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     *
     * @see #getTabIndex()
     * @param newTabIndex tabIndex
     */
    public void setTabIndex(final int newTabIndex) {
        this.tabIndex = newTabIndex;
        this.tabIndexSet = true;
    }

    // Hide value
    @Property(name = "value", isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * Get the descending flag.
     * @return {@code boolean}
     */
    public boolean isDescending() {
        if (this.descendingSet) {
            return this.descending;
        }
        ValueExpression vb = getValueExpression("descending");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result != null) {
                return ((Boolean) result);
            }
        }
        // Return the default value.
        boolean defaultValue = descending;
        try {
            defaultValue = Boolean.parseBoolean(getTheme().getMessage(
                    "filechooser.descending"));
        } catch (Exception e) {
            log("Failed to obtain the default value from the theme."
                    + "Using the default value " + defaultValue + ".");
        }
        return defaultValue;
    }

    /**
     * Set descending to true to sort from the highest value to lowest value,
     * such as Z-A for alphabetic, or largest file to smallest for sorting on
     * file size. The default is to sort in ascending order.
     *
     * @see #isDescending()
     * @param newDescending descending
     */
    public void setDescending(final boolean newDescending) {
        this.descending = newDescending;
        this.descendingSet = true;
    }

    /**
     * Get the disabled flag value.
     * @return {@code boolean}
     */
    public boolean isDisabled() {
        if (this.disabledSet) {
            return this.disabled;
        }
        ValueExpression vb = getValueExpression("disabled");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return false;
    }

    /**
     * Indicates that activation of this component by the user is not currently
     * permitted.
     *
     * @see #isDisabled()
     * @param newDisabled disabled
     */
    public void setDisabled(final boolean newDisabled) {
        this.disabled = newDisabled;
        this.disabledSet = true;
    }

    /**
     * Get the folder chooser flag.
     * @return {@code boolean}
     */
    private boolean doIsFolderChooser() {
        if (this.folderChooserSet) {
            return this.folderChooser;
        }
        ValueExpression vb = getValueExpression("folderChooser");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return false;
    }

    /**
     * Use this attribute to configure the file chooser as a folder chooser. Set
     * the value to true for a folder chooser or false for a file chooser. The
     * default value is false.
     *
     * @see #isFolderChooser()
     * @param newFolderChooser folder chooser
     */
    private void doSetFolderChooser(final boolean newFolderChooser) {
        this.folderChooser = newFolderChooser;
        this.folderChooserSet = true;
    }

    /**
     * Get the look-in.
     * @return Object
     */
    public Object getLookin() {
        if (this.lookin != null) {
            return this.lookin;
        }
        ValueExpression vb = getValueExpression("lookin");
        if (vb != null) {
            return (Object) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use this attribute to specify the initial folder to display in the Look
     * In text field. The contents of this folder will be displayed. Only
     * {@code java.io.File} or {@code java.lang.String} objects can be
     * bound to this attribute.
     *
     * @see #getLookin()
     * @param newLookin look-in
     */
    public void setLookin(final Object newLookin) {
        this.lookin = newLookin;
    }

    /**
     * Get the model.
     * @return {@code com.sun.webui.jsf.model.ResourceModel}
     */
    private com.sun.webui.jsf.model.ResourceModel doGetModel() {
        if (this.model != null) {
            return this.model;
        }
        ValueExpression vb = getValueExpression("model");
        if (vb != null) {
            return (com.sun.webui.jsf.model.ResourceModel)
                    vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Specifies the model associated with the FileChooser. The model provides
     * the file chooser with content displayed in the file chooser's list. It
     * provides other services as defined
     * in{@code com.sun.webui.jsf.model.ResourceModel}. If the model
     * attribute is not assigned a value, a FileChooserModel is used as the
     * ResourceModel instance. A value binding assigned to this attribute must
     * return an instance of ResourceModel.
     *
     * @see #getModel()
     * @param newModel model
     */
    public void setModel(final com.sun.webui.jsf.model.ResourceModel newModel) {
        this.model = newModel;
    }

    /**
     * Get the multiple flag value.
     * @return {@code boolean}
     */
    public boolean isMultiple() {
        if (this.multipleSet) {
            return this.multiple;
        }
        ValueExpression vb = getValueExpression("multiple");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return false;
    }

    /**
     * Set multiple to true to allow multiple files or folders to be selected
     * from the list. The default is false, which allows only one item to be
     * selected.
     *
     * @see #isMultiple()
     * @param newMultiple multiple
     */
    public void setMultiple(final boolean newMultiple) {
        this.multiple = newMultiple;
        this.multipleSet = true;
    }

    /**
     * Get the read-only flag value.
     * @return {@code boolean}
     */
    public boolean isReadOnly() {
        if (this.readOnlySet) {
            return this.readOnly;
        }
        ValueExpression vb = getValueExpression("readOnly");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return false;
    }

    /**
     * If readOnly is set to true, the value of the component is rendered as
     * text, preceded by the label if one was defined.
     *
     * @see #isReadOnly()
     * @param newReadOnly read-only
     */
    public void setReadOnly(final boolean newReadOnly) {
        this.readOnly = newReadOnly;
        this.readOnlySet = true;
    }

    /**
     * Get rows.
     * @return int
     */
    public int getRows() {
        if (this.rowsSet) {
            return this.rows;
        }
        ValueExpression vb = getValueExpression("rows");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result != null && ((Integer) result) > 0) {
                return ((Integer) result);
            }
        }

        // Return the default.
        int defaultRows = rows;
        try {
            defaultRows = Integer.parseInt(getTheme().getMessage(
                    "filechooser.rows"));
            if (defaultRows < 1) {
                defaultRows = rows;
            }
        } catch (NumberFormatException e) {
            log("Failed to obtain the default value from the theme."
                    + "Using the default value " + defaultRows + ".");
        }
        return defaultRows;
    }

    /**
     * The number of items to display in the list box. The value must be greater
     * than or equal to one. The default value is 12. Invalid values are ignored
     * and the value is set to 12.
     *
     * @see #getRows()
     * @param newRows rows
     */
    public void setRows(final int newRows) {
        if (newRows < 1) {
            throw new IllegalArgumentException(getTheme().getMessage(
                    "filechooser.invalidRows"));
        }
        this.rows = newRows;
        this.rowsSet = true;
    }

    /**
     * This attribute represents the value of the fileChooser. Depending on the
     * value of the {@code folderChooser} attribute, the value of the
     * {@code selected} attribute can consist of selected files or folders
     * from the list box and/or paths to files or folders entered into the
     * Selected File field.
     * <p>
     * If the {@code multiple} attribute is true, the {@code selected}
     * attribute must be bound to one of the
     * following:
     * <ul>
     * <li>{@code java.io.File[]}</li>
     * <li>{@code java.lang.String[]}</li>
     * <li>a {@code java.util.List[]} such as {@code java.util.ArrayList},
     * or  {@code java.util.LinkedList}, or {@code java.util.Vector}
     * containing instances of {@code java.io.File} or
     * {@code java.lang.String}.</li>
     * </ul>
     * <p>
     * If the {@code multiple} attribute is false, the
     * {@code selected} attribute must be bound to one of the
     * following:
     * </p>
     * <ul>
     * <li>{@code java.io.File}</li>
     * <li>{@code java.lang.String}</li>
     * </ul>
     *
     * <p>
     * If a type other than these is contained in a list type or bound directly
     * to the {@code selected} attribute, then you must specify a converter
     * with the {@code converter} attribute.
     * </p>
     * @return Object
     */
    @Property(name = "selected",
            displayName = "Selected",
            shortDescription = "The selected file(s) or folder(s) name.",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor")
            //CHECKSTYLE:ON
    public Object getSelected() {
        return getValue();
    }

    /**
     * This attribute represents the value of the fileChooser. Depending on the
     * value of the {@code folderChooser} attribute, the value of the
     * {@code selected} attribute can consist of selected files or folders from
     * the list box and/or paths to files or folders entered into the Selected
     * File field.
     * <p>
     * If the {@code multiple} attribute is true, the {@code selected} attribute
     * must be bound to one of the
     * following:
     * <ul>
     * <li>{@code java.io.File[]}</li>
     * <li>{@code java.lang.String[]}</li>
     * <li>a {@code java.util.List[]} such as {@code java.util.ArrayList}, or
     * {@code java.util.LinkedList}, or {@code java.util.Vector} containing
     * instances of {@code java.io.File} or
     * {@code java.lang.String}.</li>
     * </ul>
     * <p>
     * If the {@code multiple} attribute is false, the {@code selected}
     * attribute must be bound to one of the
     * following:</p>
     * <ul>
     * <li>{@code java.io.File}</li>
     * <li>{@code java.lang.String}</li>
     * </ul>
     *
     * <p>
     * If a type other than these is contained in a list type or bound directly
     * to the {@code selected} attribute, then you must specify a converter with
     * the {@code converter} attribute.
     * </p>
     *
     * @see #getSelected()
     * @param newSelected selected
     */
    public void setSelected(final Object newSelected) {
        setValue(newSelected);
    }

    /**
     * Get the sort field.
     * @return String
     */
    public String getSortField() {
        if (this.sortFieldSet) {
            return this.sortField;
        }
        ValueExpression vb = getValueExpression("sortField");
        if (vb != null) {
            String result = (String) vb.getValue(getFacesContext()
                    .getELContext());
            if (result != null || result.trim().length() > 0) {
                result = result.trim();
                if (result.equals(ALPHABETIC) || result.equals(SIZE)
                        || result.equals(LASTMODIFIED)) {
                    return result;
                }
            }
        }

        // Return the default value.
        String defaultValue = getTheme().getMessage("filechooser.sortField");
        if (defaultValue == null || defaultValue.length() < 1) {
            defaultValue = sortField;
            log("Failed to obtain the default value from the theme."
                    + "Using the default value " + defaultValue + ".");
        }
        return defaultValue;
    }

    /**
     * Field to use to sort the list of files.Valid values are:
     * <ul>
     * <li>alphabetic - sort alphabetically</li>
     * <li>size - sort by file size</li>
     * <li>time - sort by last modified date</li>
     * </ul>
     * <p>
     * Note that these values are case sensitive. By default, the list is sorted
     * alphabetically.</p>
     *
     * @param newSortField sortField
     * @see #getSortField()
     */
    public void setSortField(final String newSortField) {
        if (newSortField == null) {
            throw new IllegalArgumentException(getTheme().getMessage(
                    "filechooser.nullSortField"));
        }
        String sort = newSortField.trim();
        if (sort.length() < 1) {
            throw new IllegalArgumentException(getTheme().getMessage(
                    "filechooser.whitespaceSortField"));
        }
        if (!(sort.equals(ALPHABETIC) || sort.equals(SIZE)
                || sort.equals(LASTMODIFIED))) {
            throw new IllegalArgumentException(getTheme().getMessage(
                    "filechooser.invalidSortField"));
        }
        this.sortField = sort;
        this.sortFieldSet = true;
    }

    /**
     * Get the style.
     * @return String
     */
    public String getStyle() {
        if (this.style != null) {
            return this.style;
        }
        ValueExpression vb = getValueExpression("style");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @see #getStyle()
     * @param newStyle style
     */
    public void setStyle(final String newStyle) {
        this.style = newStyle;
    }

    /**
     * Get the style class.
     * @return String
     */
    public String getStyleClass() {
        if (this.styleClass != null) {
            return this.styleClass;
        }
        ValueExpression vb = getValueExpression("styleClass");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @see #getStyleClass()
     * @param newStyleClass style
     */
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        if (state == null) {
            return;
        }
        Object[] values = (Object[]) ((Object[]) state)[0];
        super.restoreState(context, values[0]);
        this.descending = ((Boolean) values[1]);
        this.descendingSet = ((Boolean) values[2]);
        this.disabled = ((Boolean) values[3]);
        this.disabledSet = ((Boolean) values[4]);
        this.folderChooser = ((Boolean) values[5]);
        this.folderChooserSet = ((Boolean) values[6]);
        this.lookin = (Object) values[7];
        this.model = (com.sun.webui.jsf.model.ResourceModel) values[8];
        this.multiple = ((Boolean) values[9]);
        this.multipleSet = ((Boolean) values[10]);
        this.readOnly = ((Boolean) values[11]);
        this.readOnlySet = ((Boolean) values[12]);
        this.rows = ((Integer) values[13]);
        this.rowsSet = ((Boolean) values[14]);
        this.sortField = (String) values[15];
        this.style = (String) values[16];
        this.styleClass = (String) values[17];
        this.fileAndFolderChooser = ((Boolean) ((Object[]) state)[1]);
        //this.valueChangeListenerExpression =(MethodExpression)
        //        restoreAttachedState(_context, _values[2]);
        //this.validatorExpression = (MethodExpression)
        //        restoreAttachedState(_context, _values[3]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[18];
        values[0] = super.saveState(context);
        if (this.descending) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.descendingSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.disabled) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.disabledSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        if (this.folderChooser) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        if (this.folderChooserSet) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        values[7] = this.lookin;
        values[8] = this.model;
        if (this.multiple) {
            values[9] = Boolean.TRUE;
        } else {
            values[9] = Boolean.FALSE;
        }
        if (this.multipleSet) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        if (this.readOnly) {
            values[11] = Boolean.TRUE;
        } else {
            values[11] = Boolean.FALSE;
        }
        if (this.readOnlySet) {
            values[12] = Boolean.TRUE;
        } else {
            values[12] = Boolean.FALSE;
        }
        values[13] = this.rows;
        if (this.rowsSet) {
            values[14] = Boolean.TRUE;
        } else {
            values[14] = Boolean.FALSE;
        }
        values[15] = this.sortField;
        values[16] = this.style;
        values[17] = this.styleClass;
        Object[] values2 = new Object[2];
        values2[0] = values;
        if (this.fileAndFolderChooser) {
            values2[1] = Boolean.TRUE;
        } else {
            values2[1] = Boolean.FALSE;
        }
        //values[2] = saveAttachedState(_context,
        //      valueChangeListenerExpression);
        //values[3] = saveAttachedState(_context, validatorExpression);
        return values2;
    }

    /**
     * Encode the selectedFileField value. This is an escaped, comma separated
     * list of selected entries. This method is used to format an initial value
     * for the selectedFileField. It is only used on initial display (?), which
     * is true if processDecodes has not been called. If processDecodes has not
     * been called format and set the value of the selectedFileField. Do this by
     * setting the submittedValue not by the setValue method.
     *
     * This can get complicated since converters may be necessary to convert the
     * values to Strings.
     * @param selections selection to encode
     * @param escapeChar escape character
     * @param delimiter delimiter
     * @return String
     */
    private static String encodeSelections(final String[] selections,
            final String escapeChar, final String delimiter) {

        if (selections == null || selections.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder(
                escapeString(selections[0], escapeChar, delimiter));

        for (int i = 1; i < selections.length; ++i) {
            sb.append(delimiter);
            sb.append(escapeString(selections[i], escapeChar, delimiter));
        }

        return sb.toString();
    }

    /**
     * Decode the selections.
     * @param selections string to process
     * @param escapeChar escape character
     * @param delimiter delimiter
     * @return String[]
     */
    private static String[] decodeSelections(final String selections,
            final String escapeChar, final String delimiter) {

        if (selections == null) {
            return null;
        }

        // This has to be done character by character
        char del = delimiter.toCharArray()[0];
        char esc = escapeChar.toCharArray()[0];
        char[] charArray = selections.toCharArray();
        int escseen = 0;
        int ind = 0;
        int j = 0;
        ArrayList<String> strArray = new ArrayList<String>();
        for (int i = 0; i < selections.length(); ++i) {
            if (charArray[i] == del) {
                if (escseen % 2 == 0) {
                    strArray.add(ind++,
                            unescapeString(selections.substring(j, i),
                                    escapeChar, delimiter));
                    j = i + 1;
                }
            }
            if (charArray[i] == esc) {
                ++escseen;
            } else {
                escseen = 0;
            }
        }
        // Capture the last substring
        //
        strArray.add(ind, unescapeString(selections.substring(j),
                escapeChar, delimiter));
        return (String[]) strArray.toArray(new String[strArray.size()]);
    }

    // These should be made utility methods but I'm not sure
    // where they should go.
    /**
     * Escape a string.
     * @param str string to process
     * @param escapeChar escape character
     * @param delimiter delimiter
     * @return String
     */
    private static String escapeString(final String str,
            final String escapeChar, final String delimiter) {

        // Replace all escapeChar's with two escapeChar's
        // But if the escaape char is "\" need to escape it
        // in the regex since it is a special character.
        String escaped = escapeChar;
        if (escapeChar.equals("\\")) {
            escaped = escapeChar + escapeChar;
        }
        String regEx = escaped;
        String s0 = str.replaceAll(regEx, escaped + escaped);

        // Replace all delimiter characters with the
        // escapeChar and a delimiter.
        regEx = delimiter;
        s0 = s0.replaceAll(regEx, escaped + delimiter);
        return s0;
    }

    /**
     * Un-escape the specified string.
     * @param str string to process
     * @param escapeChar escape character
     * @param delimiter delimiter
     * @return String
     */
    private static String unescapeString(final String str,
            final String escapeChar, final String delimiter) {

        // Replace every escaped delimiter with just the
        // delimiter.
        // But if the escaape char is "\" need to escape it
        // in the regex since it is a special character.
        String escaped = escapeChar;
        if (escapeChar.equals("\\")) {
            escaped = escapeChar + escapeChar;
        }
        String regEx = escaped + delimiter;
        String s0 = str.replaceAll(regEx, delimiter);

        // Replace every two occurrences of the escape char  with one.
        regEx = escaped + escaped;
        s0 = s0.replaceAll(regEx, escaped);
        return s0;
    }
}
