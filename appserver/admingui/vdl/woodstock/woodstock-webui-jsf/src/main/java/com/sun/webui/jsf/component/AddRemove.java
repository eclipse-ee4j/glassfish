/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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
import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.model.OptionGroup;
import com.sun.webui.jsf.model.Separator;
import com.sun.webui.jsf.model.list.ListItem;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.JavaScriptUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.MessageUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.text.Collator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

/**
 * The AddRemove component is used to construct a list of selected items.
 *
 * Use the AddRemove component when the web application user makes selections
 * from a list and they need to see the currently selected items displayed
 * together, and/or they need to reorder the selected items.
 */
@Component(
        type = "com.sun.webui.jsf.AddRemove",
        family = "com.sun.webui.jsf.AddRemove",
        displayName = "Add Remove List",
        instanceName = "addRemoveList",
        tagName = "addRemove",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_add_remove_list",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_addremovelist_props")
        //CHECKSTYLE:ON
public class AddRemove extends ListSelector implements ListManager {

    /**
     * The component id for the ADD button.
     */
    public static final String ADD_BUTTON_ID = "_addButton";

    /**
     * The facet name of the add button.
     */
    public static final String ADD_BUTTON_FACET = "addButton";

    /**
     * The component id for the ADD ALL button.
     */
    public static final String ADDALL_BUTTON_ID = "_addAllButton";

    /**
     * The facet name of the Add All button.
     */
    public static final String ADDALL_BUTTON_FACET = "addAllButton";

    /**
     * The component ID for the remove button.
     */
    public static final String REMOVE_BUTTON_ID = "_removeButton";

    /**
     * The facet name of the remove button.
     */
    public static final String REMOVE_BUTTON_FACET = "removeButton";

    /**
     * The component ID for the remove all button.
     */
    public static final String REMOVEALL_BUTTON_ID = "_removeAllButton";

    /**
     * The facet name of the "Remove All" button.
     */
    public static final String REMOVEALL_BUTTON_FACET = "removeAllButton";

    /**
     * The component ID for the move up button.
     */
    public static final String MOVEUP_BUTTON_ID = "_moveUpButton";

    /**
     * The facet name of the "Move Up" button.
     */
    public static final String MOVEUP_BUTTON_FACET = "moveUpButton";

    /**
     * The component ID for the move down button.
     */
    public static final String MOVEDOWN_BUTTON_ID = "_moveDownButton";

    /**
     * The facet name of the "Move Down" button.
     */
    public static final String MOVEDOWN_BUTTON_FACET = "moveDownButton";

    /**
     * The component ID for the items list.
     */
    public static final String AVAILABLE_LABEL_ID = "_availableLabel";

    /**
     * The facet name of the label over the "Available" list.
     */
    public static final String AVAILABLE_LABEL_FACET = "availableLabel";

    /**
     * The facet name of the label read-only case.
     */
    public static final String READ_ONLY_LABEL_FACET = "readonlyLabel";

    /**
     * The component ID for the selected list.
     */
    public static final String SELECTED_LABEL_ID = "_selectedLabel";

    /**
     * The facet name of the label over the "Selected" list.
     */
    public static final String SELECTED_LABEL_FACET = "selectedLabel";

    /**
     * Facet name for the header facet.
     */
    public static final String HEADER_FACET = "header";

    /**
     * The facet name of the header (component label).
     */
    public static final String HEADER_ID = "_header";

    /**
     * Facet name for the footer facet.
     */
    public static final String FOOTER_FACET = "footer";

    /**
     * The id of the label component that functions as the label above the
     * available list.
     */
    public static final String AVAILABLE_ID = "_available";

    /**
     * The available label text key.
     */
    public static final String AVAILABLE_TEXT_KEY = "AddRemove.available";

    /**
     * The ID of the component that functions as the label above the "Selected"
     * list.
     */
    public static final String SELECTED_ID = "_selected";

    /**
     * The selected label text key.
     */
    public static final String SELECTED_TEXT_KEY = "AddRemove.selected";

    /**
     * The ID of the component read-only case.
     */
    public static final String READONLY_ID = "_readonly";

    /**
     * String representing "return false" printed at the end of the JS
     * event handlers.
     */
    public static final String RETURN = "return false;";

    /**
     * Name of the JavaScript function which is responsible for adding elements
     * from the available list to the selected list.
     */
    public static final String ADD_FUNCTION = ".add(); ";

    /**
     * Add button text key.
     */
    public static final String ADD_TEXT_KEY = "AddRemove.add";

    /**
     * Add button text key, vertical layout.
     */
    public static final String ADDVERTICAL_TEXT_KEY
            = "AddRemove.addVertical";

    /**
     * Name of the JavaScript function which is responsible for selecting all
     * the available items.
     */
    public static final String ADDALL_FUNCTION = ".addAll();";

    /**
     * Add all button text key.
     */
    public static final String ADDALL_TEXT_KEY = "AddRemove.addAll";

    /**
     * Add all button text key, vertical layout.
     */
    public static final String ADDALLVERTICAL_TEXT_KEY
            = "AddRemove.addAllVertical";

    /**
     * Name of the JavaScript function which removes items from the selected
     * list.
     */
    public static final String REMOVE_FUNCTION = ".remove(); ";

    /**
     * Remove button text key.
     */
    public static final String REMOVE_TEXT_KEY = "AddRemove.remove";

    /**
     * Remove button text key, vertical layout.
     */
    public static final String REMOVEVERTICAL_TEXT_KEY
            = "AddRemove.removeVertical";

    /**
     * Name of the JavaScript function which removes all the items from the
     * selected list.
     */
    public static final String REMOVEALL_FUNCTION = ".removeAll(); ";

    /**
     * Remove all button text key.
     */
    public static final String REMOVEALL_TEXT_KEY = "AddRemove.removeAll";

    /**
     * Remove all button text key, vertical layout.
     */
    public static final String REMOVEALLVERTICAL_TEXT_KEY
            = "AddRemove.removeAllVertical";

    /**
     * Name of the JavaScript function which moves elements up.
     */
    public static final String MOVEUP_FUNCTION = ".moveUp(); ";

    /**
     * Move up button text key.
     */
    public static final String MOVEUP_TEXT_KEY = "AddRemove.moveUp";

    /**
     * Name of the JavaScript function which moves elements down.
     */
    public static final String MOVEDOWN_FUNCTION = ".moveDown();";

    /**
     * Move down button text key.
     */
    public static final String MOVEDOWN_TEXT_KEY = "AddRemove.moveDown";

    /**
     * Name of the JavaScript function that updates the buttons.
     */
    public static final String UPDATEBUTTONS_FUNCTION = ".updateButtons(); ";

    /**
     * Name of the JavaScript function that handles changes on the available
     * list.
     */
    public static final String AVAILABLE_ONCHANGE_FUNCTION
            = ".availableOnChange(); ";

    /**
     * Name of the JavaScript function which handles changes to the selected
     * list.
     */
    public static final String SELECTED_ONCHANGE_FUNCTION
            = ".selectedOnChange(); ";

    /**
     * The name of the JavaScript function used to hook up the correct add and
     * remove functions when the component allows items to be added to the
     * selected items list more than once.
     */
    public static final String MULTIPLEADDITIONS_FUNCTION
            = ".allowMultipleAdditions()";

    /**
     * Spacer string.
     */
    public static final String SPACER_STRING = "_";

    /**
     * Key string.
     */
    private static final String KEY_STRING = "a";

    /**
     * Dup string.
     */
    private static final String DUP_STRING = "1";

    /**
     * The string used as a separator between the selected values.
     */
    public static final String SEPARATOR_VALUE = "com.sun.webui.jsf.separator";

    /**
     * The label level key. It is used to overwrite the Label component's
     * default value.
     */
    public static final String ADDREMOVE_LABEL_LEVEL
            = "AddRemove.labelLevel";

    /**
     * The available items.
     */
    private TreeMap<String, ListItem> availableItems = null;

    /**
     * The selected items.
     */
    private TreeMap<String, ListItem> selectedItems = null;

    /**
     * Text collator.
     */
    private Collator collator = null;

    /**
     * All values.
     */
    private String allValues = "";

    /**
     * Selected values.
     */
    private String selectedValues = "";

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * The label for the list of available items.
     */
    @Property(
            name = "availableItemsLabel",
            displayName = "Available Items label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String availableItemsLabel = null;

    /**
     * If duplicateSelections is set to true, items in the available list are
     * not removed when they are added to the selected list. The user is
     * permitted to add an available item more than once to the list of selected
     * items. The list of selected items would then contain duplicate
     * entries.
     */
    @Property(name = "duplicateSelections",
            displayName = "Allow Duplicate Selections",
            category = "Data")
    private boolean duplicateSelections = false;

    /**
     * DuplicateSelections set flag.
     */
    private boolean duplicateSelectionsSet = false;

    /**
     * Show the Move Up and Move Down buttons.
     */
    @Property(name = "moveButtons",
            displayName = "Move Buttons",
            category = "Appearance")
    private boolean moveButtons = false;

    /**
     * moveButtons set flag.
     */
    private boolean moveButtonsSet = false;

    /**
     * Show the Add All and Remove All buttons.
     */
    @Property(name = "selectAll",
            displayName = "Select All Buttons",
            category = "Appearance")
    private boolean selectAll = false;

    /**
     * selectAll set flag.
     */
    private boolean selectAllSet = false;

    /**
     * The label for the list of selected items.
     */
    @Property(name = "selectedItemsLabel",
            displayName = "Selected Items label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String selectedItemsLabel = null;

    /**
     * If sorted is set to true, the items on the available list are shown in
     * alphabetical order. The items on the selected options list are also shown
     * in alphabetical order, unless the moveButtons attribute is true, in which
     * case the user is expected to order the elements.
     */
    @Property(name = "sorted",
            displayName = "Sorted",
            category = "Data")
    private boolean sorted = false;

    /**
     * sorted set flag.
     */
    private boolean sortedSet = false;

    /**
     * Use the vertical layout instead of the default horizontal layout. The
     * vertical layout displays the available items list above the selected
     * items list.
     */
    @Property(name = "vertical",
            displayName = "Vertical layout",
            category = "Appearance")
    private boolean vertical = false;

    /**
     * vertical set flag.
     */
    private boolean verticalSet = false;

    /**
     * Constructor for the AddRemove component.
     */
    public AddRemove() {
        setMultiple(true);
        setRendererType("com.sun.webui.jsf.AddRemove");
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.AddRemove"}.
     * @return String
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.AddRemove";
    }

    /**
     * Get the number of rows to display (the default is 12).
     *
     * @return the number of rows to display
     */
    @Override
    public int getRows() {
        int rows = super.getRows();
        if (rows < 1) {
            // FIXME: Should be in Theme
            //CHECKSTYLE:OFF
            rows = 12;
            // CHECKSTYLE:ON
            // Don't alter the Bean value.
            //super.setRows(rows);
        }
        return rows;
    }

    /**
     * Get the separator string that is used to separate the selected values on
     * the client. The default value is "|". When the AddRemove component is
     * decoded, the value is taken from a hidden variable whose value is a list
     * of the values of all the options in the list representing the selected
     * items. Consider a case where the AddRemove has a list of options
     * including "one" and "two".
     * Assume that these two options are disabled. If the separator string is
     * set to "|", then the value of the hidden variable will be |1|2|.
     *
     * You will only need to set this variable if the string representation of
     * one of the option values contain the character "|". If you do need to
     * change from the default, bear in mind that the value of the hidden
     * component is sent as part of the body of the HTTP request body. Make sure
     * to select a character that does not change the syntax of the request.
     *
     * @return The separator string.
     */
    public String getSeparator() {
        // FIXME: Either should be in Theme or configurable.
        return "|";
    }

    /**
     * Returns an iterator over the selected items. This function will return
     * one separator element {@code com.sun.web.ui.separator} in addition to the
     * selected items even if the selected list is empty.
     *
     * @return an iterator over the selected items
     */
    public Iterator getSelectedItems() {
        FacesContext context = FacesContext.getCurrentInstance();
        // Initialize selectedItems and selectedValues.
        Iterator itr = getListItems(context, true);
        return selectedItems.values().iterator();
    }

    /**
     * This function returns a String consisting of the String representation of
     * the values of all the available Options, separated by the separator
     * String (see getSeparator()).
     *
     * @return String consisting of the String representation of the
     * values of all the available Options, separated by the separator String
     */
    public String getAllValues() {
        return allValues;
    }

    /**
     * This function returns a String consisting of the String representation of
     * the values of the selected Options, separated by the separator String.
     *
     * @return a String consisting of the String representation of the values of
     * the selected Options, separated by the separator String
     */
    public String getSelectedValues() {
        FacesContext context = FacesContext.getCurrentInstance();
        // Initialize selectedItems and selectedValues.
        Iterator itr = getListItems(context, true);
        return selectedValues;
    }

    // Buttons
    /**
     * Get or create the ADD button. Retrieves the component specified by the
     * addButton facet (if there is one) or creates a new Button component.
     *
     * @return A UI Component for the Add button
     * @param context The FacesContext for the request
     * @deprecated See getAddButtonComponent();
     */
    public UIComponent getAddButtonComponent(final FacesContext context) {
        return getAddButtonComponent();
    }

    /**
     * Return a component that implements the add button. If a facet named
     * {@code addButton} is found that component is returned. Otherwise a
     * {@code Button} component is returned. It is assigned the id
     * {@code getId() + "_addButton"}
     * <p>
     * If the facet is not defined then the returned {@code Button} component is
     * re-initialized every time this method is called.
     * </p>
     *
     * @return an add button component
     */
    public UIComponent getAddButtonComponent() {
        if (DEBUG) {
            log("getAddButtonComponent()");
        }
        String msgKey;
        if (isVertical()) {
            msgKey = ADDVERTICAL_TEXT_KEY;
        } else {
            msgKey = ADD_TEXT_KEY;
        }
        return getButtonFacet(ADD_BUTTON_FACET, false,
                getTheme().getMessage(msgKey), ADD_FUNCTION);
    }

    /**
     * Return a component that implements the add all button. If a facet named
     * {@code addAllButton} is found that component is returned. Otherwise a
     * {@code Button} component is returned. It is assigned the id
     * {@code getId() + "_addAllButton"}
     * <p>
     * If the facet is not defined then the returned {@code Button} component is
     * re-initialized every time this method is called.
     * </p>
     *
     * @return an add all button component
     */
    public UIComponent getAddAllButtonComponent() {
        if (DEBUG) {
            log("getAddAllButtonComponent()");
        }
        String msgKey;
        if (isVertical()) {
            msgKey = ADDALLVERTICAL_TEXT_KEY;
        } else {
            msgKey = ADDALL_TEXT_KEY;
        }
        return getButtonFacet(ADDALL_BUTTON_FACET, false,
                getTheme().getMessage(msgKey), ADDALL_FUNCTION);
    }

    /**
     * Return a component that implements the remove button. If a facet named
     * {@code removeButton} is found that component is returned. Otherwise a
     * {@code Button} component is returned. It is assigned the id
     * {@code getId() + "_removeButton"}
     *
     * If the facet is not defined then the returned {@code Button} component is
     * re-initialized every time this method is called.
     *
     * @return a remove button component
     */
    public UIComponent getRemoveButtonComponent() {
        if (DEBUG) {
            log("getRemoveButtonComponent()");
        }
        String msgKey;
        if (isVertical()) {
            msgKey = REMOVEVERTICAL_TEXT_KEY;
        } else {
            msgKey = REMOVE_TEXT_KEY;
        }
        return getButtonFacet(REMOVE_BUTTON_FACET, false,
                getTheme().getMessage(msgKey), REMOVE_FUNCTION);
    }

    /**
     * Return a component that implements the remove all button. If a facet
     * named {@code removeAllButton} is found that component is returned.
     * Otherwise a {@code Button} component is returned. It is assigned the id
     * {@code getId() + "_removeAllButton"}
     *
     * If the facet is not defined then the returned {@code Button} component is
     * re-initialized every time this method is called.
     *
     * @return a remove all button component
     */
    public UIComponent getRemoveAllButtonComponent() {
        if (DEBUG) {
            log("getRemoveAllButtonComponent()");
        }
        String msgKey;
        if (isVertical()) {
            msgKey = REMOVEALLVERTICAL_TEXT_KEY;
        } else {
            msgKey = REMOVEALL_TEXT_KEY;
        }
        return getButtonFacet(REMOVEALL_BUTTON_FACET, false,
                getTheme().getMessage(msgKey), REMOVEALL_FUNCTION);
    }

    /**
     * Return a component that implements the move up button. If a facet named
     * {@code moveUpButton} is found that component is returned. Otherwise a
     * {@code Button} component is returned. It is assigned the id
     * {@code getId() + "_moveUpButton"}
     * If the facet is not defined then the returned {@code Button} component is
     * re-initialized every time this method is called.
     *
     * @return a move up button component
     */
    public UIComponent getMoveUpButtonComponent() {
        if (DEBUG) {
            log("getMoveUpButtonComponent()");
        }
        return getButtonFacet(MOVEUP_BUTTON_FACET, false,
                getTheme().getMessage(MOVEUP_TEXT_KEY), MOVEUP_FUNCTION);
    }

    /**
     * Return a component that implements the move down button. If a facet named
     * {@code moveDownButton} is found that component is returned. Otherwise a
     * {@code Button} component is returned. It is assigned the id
     * {@code getId() + "_moveDownButton"}
     * <p>
     * If the facet is not defined then the returned {@code Button} component is
     * re-initialized every time this method is called.
     * </p>
     *
     * @return a move down button component
     */
    public UIComponent getMoveDownButtonComponent() {
        if (DEBUG) {
            log("getMoveDownButtonComponent()");
        }
        return getButtonFacet(MOVEDOWN_BUTTON_FACET, false,
                getTheme().getMessage(MOVEDOWN_TEXT_KEY), MOVEDOWN_FUNCTION);
    }

    /**
     * Return a component that implements a button facet. If a facet named
     * {@code facetName} is found that component is returned. Otherwise a
     * {@code Button} component is returned. It is assigned the id
     * {@code getId() + "_<facetName>"}
     * <p>
     * If the facet is not defined then the returned {@code Button} component is
     * re-initialized every time this method is called.
     * </p>
     *
     * @param facetName the name of the facet to return or create
     * @param primary if false the button is not a primary button
     * @param text the button text
     * @param onclickFunction the JS function name for the {@code onclick} event
     *
     * @return a button facet component
     */
    private UIComponent getButtonFacet(final String facetName,
            final boolean primary, final String text,
            final String onclickFunction) {

        if (DEBUG) {
            log("getButtonFacet() " + facetName);
        }
        // Check if the page author has defined the facet
        UIComponent buttonComponent = getFacet(facetName);
        if (buttonComponent != null) {
            if (DEBUG) {
                log("\tFound facet");
            }
            return buttonComponent;
        }

        // Return the private facet or create one, but initialize
        // it every time
        //
        // We know it's a Button
        Button button = (Button) ComponentUtilities.getPrivateFacet(this,
                facetName, true);
        if (button == null) {
            if (DEBUG) {
                log("create Button");
            }
            button = new Button();
            button.setId(ComponentUtilities.createPrivateFacetId(this,
                    facetName));
            ComponentUtilities.putPrivateFacet(this, facetName, button);
        }
        initButtonFacet(button, primary, text, onclickFunction);
        return button;
    }

    /**
     * Initialize a {@code Button} facet.
     *
     * @param button the Button instance
     * @param primary if false the button is not a primary button
     * @param text the button text
     * @param onclickFunction the JS function name for the {@code onclick} event
     */
    private void initButtonFacet(final Button button, final boolean primary,
            final String text, final String onclickFunction) {

        button.setPrimary(primary);
        button.setText(text);

        int tindex = getTabIndex();
        if (tindex > 0) {
            button.setTabIndex(tindex);
        }

        StringBuilder buff = new StringBuilder();
        buff.append(JavaScriptUtilities.getDomNode(getFacesContext(), this))
                .append(onclickFunction).append(RETURN);
        button.setOnClick(buff.toString());

        // NOTE: the original behavior would have set this
        // on the developer defined facet. It was determined that
        // a developer defined facet should not be modified.
        button.setDisabled(isDisabled());
    }

    /**
     * Return a component that implements a label for the available list. If a
     * facet named {@code availableLabel} is found that component is returned.
     * Otherwise a {@code Label} component is returned. It is assigned the id
     * {@code getId() + "_availableLabel"}
     * <p>
     * If the facet is not defined then the returned {@code Label} component is
     * re-initialized every time this method is called.
     * </p>
     *
     * @return an available list label facet component
     */
    public UIComponent getAvailableLabelComponent() {

        if (DEBUG) {
            log("getAvailableLabelComponent()");
        }
        // Prepare to call getLabelFacet so that it can be
        // re-initialized if it exists or created if it doesn't.
        // Preparing this information before we know if there
        // is a developer defined facet, is a hit, but there
        // is less likelyhood of a developer defined facet, so
        // optimize for the majority case.

        // This extensibility should be accomplished by the
        // developer providing
        // an AVAILABLE_LABEL_FACET. So that "getAvailableItemsLabel"
        // would not have to be expressed as an AddRemove attribute.
        // But in general it is more work for the developer.
        //
        // Alternatively, there could have been a way to
        // allow the developer to override the default
        // message key "AddRemove.available" with the text
        // that they desired, like an "messageBundle" attribute.
        // We could have used "param" tags for this, for example.
        String labelString = getAvailableItemsLabel();
        if (labelString == null || labelString.length() == 0) {
            labelString = getTheme()
                    .getMessage(AVAILABLE_TEXT_KEY);
        }

        String styleClass = getTheme()
                .getStyleClass(ThemeStyles.ADDREMOVE_LABEL2);
        String forId = getLabelFacetForId(AVAILABLE_ID);

        return getLabelFacet(AVAILABLE_LABEL_FACET, labelString,
                styleClass, forId);
    }

    /**
     * Return a component that implements a label for the selected list. If a
     * facet named {@code selectedLabel} is found that component is returned.
     * Otherwise a {@code Label} component is returned. It is assigned the id
     * {@code getId() + "_selectedLabel"}
     * <p>
     * If the facet is not defined then the returned {@code Label} component is
     * re-initialized every time this method is called.
     * </p>
     *
     * @return a selected list label facet component
     */
    public UIComponent getSelectedLabelComponent() {

        if (DEBUG) {
            log("getSelectedLabelComponent()");
        }
        // Prepare to call getLabelFacet so that it can be
        // re-initialized if it exists or created if it doesn't.
        // Preparing this information before we know if there
        // is a developer defined facet, is a hit, but there
        // is less likelyhood of a developer defined facet, so
        // optimize for the majority case.

        // This extensibility should be accomplished by the
        // developer providing
        // an AVAILABLE_LABEL_FACET. So that "getAvailableItemsLabel"
        // would not have to be expressed as an AddRemove attribute.
        // But in general it is more work for the developer.
        //
        // Alternatively, there could have been a way to
        // allow the developer to override the default
        // message key "AddRemove.selected" with the text
        // that they desired, like an "messageBundle" attribute.
        // We could have used "param" tags for this, for example.
        String labelString = getSelectedItemsLabel();
        if (labelString == null || labelString.length() == 0) {
            labelString = getTheme().getMessage(SELECTED_TEXT_KEY);
        }

        String styleClass = getTheme()
                .getStyleClass(ThemeStyles.ADDREMOVE_LABEL2);

        // The problem here is that the indicators will work
        // properly and render in the appropriate place, but the
        // Label for attribut of the label will point to the
        // available list. The selected list really needs to be
        // a component and not renderer as a select in
        // AddRemoveRenderer.
        String forId = getClientId(FacesContext.getCurrentInstance());

        return getLabelFacet(SELECTED_LABEL_FACET, labelString,
                styleClass, forId);
    }

    /**
     * Return a component that implements a label for the readOnly selected
     * list. If a facet named {@code selectedLabel} is found that component is
     * returned. Otherwise a {@code Label} component is returned. It is assigned
     * the id {@code getId() + "_selectedLabel"}
     * <p>
     * If the facet is not defined then the returned {@code Label} component is
     * re-initialized every time this method is called.
     * </p>
     *
     * @return a selected list label facet component
     */
    public UIComponent getReadOnlyLabelComponent() {

        if (DEBUG) {
            log("getReadOnlyLabelComponent()");
        }
        // Prepare to call getLabelFacet so that it can be
        // re-initialized if it exists or created if it doesn't.
        // Preparing this information before we know if there
        // is a developer defined facet, is a hit, but there
        // is less likelyhood of a developer defined facet, so
        // optimize for the majority case.

        // This extensibility should be accomplished by the
        // developer providing
        // an AVAILABLE_LABEL_FACET. So that "getAvailableItemsLabel"
        // would not have to be expressed as an AddRemove attribute.
        // But in general it is more work for the developer.
        //
        // Alternatively, there could have been a way to
        // allow the developer to override the default
        // message key "AddRemove.selected" with the text
        // that they desired, like an "messageBundle" attribute.
        // We could have used "param" tags for this, for example.
        String labelString = getSelectedItemsLabel();
        if (labelString == null || labelString.length() == 0) {
            labelString  = getTheme().getMessage(SELECTED_TEXT_KEY);
        }

        String styleClass = getTheme()
                .getStyleClass(ThemeStyles.ADDREMOVE_LABEL2_READONLY);

        return getLabelFacet(READ_ONLY_LABEL_FACET, labelString,
                styleClass, null);
    }

    /**
     * Return a component that implements a label for the AddRemove component.
     * If a facet named {@code header} is found that component is returned.
     * Otherwise a {@code Label} component is returned. It is assigned the id
     * {@code getId() + "_header"}
     * <p>
     * If the facet is not defined then the returned {@code Label} component is
     * re-initialized every time this method is called.
     * </p>
     *
     * @return a header list label facet component
     */
    public UIComponent getHeaderComponent() {

        if (DEBUG) {
            log("getHeaderComponent()");
        }
        String labelString = getLabel();
        String styleClass
                = getTheme().getStyleClass(ThemeStyles.ADDREMOVE_LABEL);
        String forId = getClientId(
                FacesContext.getCurrentInstance()).concat(AVAILABLE_ID);

        return getLabelFacet(HEADER_FACET, labelString,
                styleClass, forId);
    }

    /**
     * Return a component that implements a label for the facetName role. If a
     * facet named facetName is found that component is returned. Otherwise a
     * {@code Label} component is returned. It is assigned the id
     * {@code getId() + "facetName"}
     * <p>
     * If the facet is not defined then the returned {@code Label} component is
     * re-initialized every time this method is called.
     * </p>
     *
     * @param facetName the name of the facet to return or create
     * @param labelText the text for the label
     * @param styleClass the label styleClass
     * @param forId the component id that this facet labels
     *
     * @return a label facet component
     */
    private UIComponent getLabelFacet(final String facetName,
            final String labelText, final String styleClass,
            final String forId) {

        if (DEBUG) {
            log("getLabelFacet() for " + facetName);
        }
        // Check if the page author has defined a label facet
        UIComponent labelComponent = getFacet(facetName);
        if (labelComponent != null) {
            if (DEBUG) {
                log("\tFound facet.");
            }
            return labelComponent;
        }

        // There was an implicit policy for the HEADER_FACET
        // that if getLabel() returned null then no label facet
        // was returned or created. Follow that here if
        // labelText is null, return null. For callers of this
        // method in this file, labelText will only be null if getLabel
        // returns null or empty string, when called by getHeaderComponent.
        if (labelText == null) {
            return null;
        }

        // Return the private facet or create one, but initialize
        // it every time
        //
        // We know it's a Label
        Label label = (Label) ComponentUtilities.getPrivateFacet(this,
                facetName, true);
        if (label == null) {
            label = new Label();
            label.setId(ComponentUtilities.createPrivateFacetId(this,
                    facetName));
            ComponentUtilities.putPrivateFacet(this, facetName, label);
        }
        initLabelFacet(label, facetName, labelText, styleClass, forId);

        return label;
    }

    /**
     * Initialize a {@code Label} component for the role of {@code facetName}.
     * If forId is null, {@code setLabeledComponent} is called with {@code this}
     * as the parameter.
     *
     * @param label the Button instance
     * @param facetName the name of the facet to return or create
     * @param labelText the text for the label
     * @param styleClass the label styleClass
     * @param forId the component id that this facet labels
     */
    protected void initLabelFacet(final Label label, final String facetName,
            final String labelText, final String styleClass,
            final String forId) {

        if (DEBUG) {
            log("initLabelFacet()");
        }
        if (label == null) {
            return;
        }

        // Not sure why this is done.
        String text = labelText;
        if (text == null || text.length() < 1) {
            // TODO - maybe print a default?
            text = new String();
        }

        // By default, the Label component sets the label level to a default
        // value. Since we don't want any level to be set, we need to set the
        // label level to a value outside of the valid range.
        label.setLabelLevel(Integer.parseInt(getTheme().getMessage(
                ADDREMOVE_LABEL_LEVEL)));
        label.setText(text);
        label.setStyleClass(styleClass);

        // This policy is based on the original behavior.
        // For the available and selected facets, forId is set
        // For the header facet, setLabeledComponent is called.
        // Not sure how valid that is in the general case.
        if (forId != null) {
            label.setFor(forId);
        }
    }

    /**
     * Return an id for a label facet. The format of the id is
     * getClientId() + idSuffix
     * @param idSuffix id suffix
     * @return String
     */
    private String getLabelFacetForId(final String idSuffix) {

        // Note that the id returned here does not have the form
        // of an AddRemove child element id but the form
        // of a form element child id.
        //
        // "form1:addremoveid_idsuffix"
        //
        // The renderer MUST render the HTML element referred to by idSuffix
        // in the same manner.
        //
        // FIXME - what should we show here?
        return getClientId(
                FacesContext.getCurrentInstance()).concat(idSuffix);
    }

    /**
     * This implementation returns the client id + {@code _available}.
     * @param context faces context
     * @return String
     */
    @Override
    public String getPrimaryElementID(final FacesContext context) {
        // Don't return getLabeledElementId here in case
        // callers can't handle null when isReadOnly is true.
        return this.getClientId(context).concat(AVAILABLE_ID);
    }

    /**
     * This implementation returns {@code null} if read-only, or else
     * the client id + {@code _available}.
     * @param context faces context
     * @return String
     */
    @Override
    public String getLabeledElementId(final FacesContext context) {
        if (isReadOnly()) {
            return null;
        } else {
            return this.getClientId(context).concat(AVAILABLE_ID);
        }
    }

    /**
     * This implementation invokes {@link #getLabeledElementId}.
     * @param context faces context
     * @return String
     */
    @Override
    public String getFocusElementId(final FacesContext context) {
        return getLabeledElementId(context);
    }

    /**
     * Short-hand to get the theme.
     * @return Theme
     */
    private static Theme getTheme() {
        return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }

    /**
     * This implementation processes the selections and returns an iterator.
     *
     * @param context faces context
     * @param rulerAtEnd the end of the options. The role of the blank item is
     * to guarantee that the width of the lists do not change when items are
     * moved from one to the other.
     * @return Iterator
     * @throws FacesException if an error occurs
     */
    @Override
    public Iterator getListItems(final FacesContext context,
            final boolean rulerAtEnd) throws FacesException {

        if (DEBUG) {
            log("getListItems()");
        }
        Locale locale = context.getViewRoot().getLocale();
        if (DEBUG) {
            log("\tLocale is " + locale.toString());
        }
        collator = Collator.getInstance(locale);
        collator.setStrength(Collator.IDENTICAL);

        availableItems = new TreeMap<String, ListItem>(collator);
        selectedItems = new TreeMap<String, ListItem>(collator);

        // Retrieve the current selections. If there are selected
        // objects, mark the corresponding items as selected.
        processOptions(context, collator, locale, rulerAtEnd);

        // We construct a string representation of all values (whether
        // they are selected or not) before we remove the selected
        // items in the processSelections step
        allValues = constructValueString(availableItems);

        processSelections();

        // We construct a string representation of the selected values
        // only
        selectedValues
                = constructValueString(selectedItems, SEPARATOR_VALUE);

        return availableItems.values().iterator();
    }

    /**
     * Evaluates the list of available Options, creating a ListItem for each
     * one.
     *
     * @param context The FacesContext
     * @param zCollator text collator
     * @param locale locale to use
     * @param rulerAtEnd the end of the options. The role of the blank item is
     * to guarantee that the width of the lists do not change when items are
     * moved from one to the other.
     */
    protected void processOptions(final FacesContext context,
            final Collator zCollator, final Locale locale,
            final boolean rulerAtEnd) {

        if (DEBUG) {
            log("processOptions()");
        }
        Option[] options = getOptions();
        int length = options.length;

        ListItem listItem;
        String label;
        String lastKey = "";
        String longestString = "";
        StringBuilder unsortedKeyBuffer = new StringBuilder();
        boolean isSorted = isSorted();

        for (int counter = 0; counter < length; ++counter) {

            if (options[counter] instanceof OptionGroup) {
                String msg = MessageUtil.getMessage(
                        "com.sun.webui.jsf.resources.LogMessages",
                        "AddRemove.noGrouping");
                log(msg);
                continue;
            }
            if (options[counter] instanceof Separator) {
                String msg = MessageUtil.getMessage(
                        "com.sun.webui.jsf.resources.LogMessages",
                        "AddRemove.noGrouping");
                log(msg);
                continue;
            }
            // Convert the option to a list item
            listItem = createListItem(options[counter]);

            label = listItem.getLabel();
            if (label.length() > longestString.length()) {
                longestString = label;
            }

            if (isSorted) {
                availableItems.put(label, listItem);
                if (zCollator.compare(label, lastKey) > 0) {
                    lastKey = label;
                }
            } else {
                // If the page author does not want the list items to be
                // sorted (alphabetically by locale), then they're
                // supposed to be sorted by the order they were added.
                // Maps are not guaranteed to return items in the order
                // they were added, so we have to create this order
                // artificially. We do that by creating a successively
                // longer key for each element. (a, aa, aaa...).
                unsortedKeyBuffer.append(KEY_STRING);
                availableItems.put(unsortedKeyBuffer.toString(), listItem);
                lastKey = unsortedKeyBuffer.toString();
            }
        }

        if (rulerAtEnd) {

            // It looks the like "5" is extra padding or a margin
            // between the last letter of an item and the list
            // box border. This should be a Theme property.
            // SPACER_STRING should also be a Theme property
            //CHECKSTYLE:OFF
            int seplength = longestString.length() + 5;
            //CHECKSTYLE:ON
            StringBuilder labelBuffer = new StringBuilder(seplength);

            for (int counter = 0; counter < seplength; ++counter) {
                labelBuffer.append(SPACER_STRING);
            }
            ListItem item = new ListItem(labelBuffer.toString());
            item.setDisabled(true);
            item.setValue(SEPARATOR_VALUE);
            if (isSorted) {
                lastKey = lastKey.concat(KEY_STRING);
                availableItems.put(lastKey, item);
                lastKey = lastKey.concat(KEY_STRING);
                selectedItems.put(lastKey, item);
            } else {
                unsortedKeyBuffer.append(KEY_STRING);
                availableItems.put(unsortedKeyBuffer.toString(), item);
                unsortedKeyBuffer.append(KEY_STRING);
                selectedItems.put(unsortedKeyBuffer.toString(), item);
            }
        }

        if (DEBUG) {
            log("AvailableItems keys");
            Iterator iterator = availableItems.keySet().iterator();
            while (iterator.hasNext()) {
                log("next key " + iterator.next().toString());
            }
        }
    }

    /**
     * Build the value string from the specified tree map.
     * @param map tree map
     * @return String
     */
    private String constructValueString(final TreeMap map) {
        return constructValueString(map, null);
    }

    /**
     * Build the value string from the specified tree map and filter.
     * @param map tree map
     * @param filter filter
     * @return String
     */
    private String constructValueString(final TreeMap map,
            final String filter) {

        // Set up the "All values" string. This is rendered as a
        // hidden input on the client side, and is used to
        StringBuilder valuesBuffer = new StringBuilder();
        Iterator values = map.values().iterator();
        ListItem listItem;
        String separator = getSeparator();
        valuesBuffer.append(separator);
        while (values.hasNext()) {
            listItem = (ListItem) (values.next());
            if (filter != null && listItem.getValue().equals(filter)) {
                continue;
            }
            valuesBuffer.append(listItem.getValue());
            valuesBuffer.append(separator);
        }
        return valuesBuffer.toString();
    }

    /**
     * Retrieve an Iterator of ListSelector.ListItem representing the selected
     * selections only. This method is used by the renderer, to create the
     * options of the list of selected items. It is also used when calculating a
     * string representation of the value of the component.
     *
     * @return An Iterator over the selected ListItem
     */
    public Iterator getSelectedListItems() {
        return selectedItems.values().iterator();
    }

    /**
     * This implementation iterates over the available items and marks the
     * selected ones.
     * @param list items to mark as selected
     * @param processed flag to indicate if the values are already processed
     */
    @Override
    @SuppressWarnings("checkstyle:methodlength")
    protected void markSelectedListItems(final java.util.List list,
            final boolean processed) {

        if (DEBUG) {
            log("markSelectedListItems()");
        }
        // The "selected" variable is an iteration over the selected
        // items

        // CR 6359071
        // Drive the comparisons from the selected list vs. the
        // available list. This results in the resulting mapped
        // selected list reflecting the order of the original
        // selected list.
        Iterator selected = list.iterator();

        boolean allowDups = isDuplicateSelections();

        // The selected items are sorted if "isSorted" is true and
        // "isMoveButtons" is false. If "isMoveButtons" is true then
        // the selected items are not sorted even if "isSorted" is true.
        // They appear as they were inserted.
        // If "isSorted" is false and "isMoveButtons" is false, the selected
        // items will appear as they were inserted.
        boolean isSorted = isSorted() && !isMoveButtons();

        // Use the HashMap "removeItems" to record the selected
        // items that must be removed from the available items.
        // This allows us to not use the available item keys in the
        // selectedItems list, enabling the
        // selectedItems to be sorted as inserted.
        Set<String> removeItems = new HashSet<String>();

        // Devise a key to use for the selectedItems. Use the same
        // strategy as used for available items. Create an increasing
        // String of the letter KEY_STRING as selected items are recorded.
        // If sorting, use the available item key.
        String selectedKey = "";

        while (selected.hasNext()) {

            Object selectedValue = selected.next();

            // The "keys" are the keys of the options on the available map
            // Need to "rewind" for every selected item.
            Iterator<String> keys = availableItems.keySet().iterator();

            // Does the current listItem match the selected value?
            boolean match = false;

            while (keys.hasNext()) {

                String key = keys.next();
                // The next object from the available map
                //
                Object nextItem = availableItems.get(key);
                ListItem listItem = null;

                // If we get an exception just log it and continue.
                // It's cheaper this way than testing with "instanceof".
                try {
                    listItem = (ListItem) nextItem;
                } catch (Exception e) {
                    log("An available item was not a ListItem.");
                    continue;
                }

                if (DEBUG) {
                    log("Now processing ListItem "
                            + listItem.getValue());
                    log("\tSelected object value: "
                            + String.valueOf(selectedValue));
                    log("\tSelected object type: "
                            + selectedValue.getClass().getName());
                    if (processed) {
                        log("\tMatching the values by "
                                + "object.equals()");
                    } else {
                        log("\tMatching the values by string"
                                + "comparison on converted values.");
                    }
                }

                if (processed) {
                    match = listItem.getValueObject().equals(selectedValue);
                } else {
                    // Recall that "processed" means that we compare using the
                    // actual value of this component, and this case means that
                    // we compare from the submitted values. In other words, in
                    // this scenario, the selectedValue is an already converted
                    // String.
                    match = selectedValue.toString()
                            .equals(listItem.getValue());
                }

                // Note that elements in the selected list that do
                // not match will not appear in the "selectedItems"
                // TreeMap.
                if (!match) {
                    continue;
                }

                if (DEBUG) {
                    log("\tListItem and selected item match");
                }
                // Ensure that the selectedItems are sorted appropriately.
                // Use the sort order of the available items if sorted
                // and the insertion order if not.
                if (isSorted) {
                    selectedKey = key;
                } else {
                    selectedKey = selectedKey.concat(KEY_STRING);
                }

                // See if we have a dup. If dups are allowed
                // create a new unique key for the dup and add it
                // to the selectedItems.
                // If not a dup, add it to the removeItems map
                // and add it to the selectedItems.
                if (removeItems.contains(key)) {
                    if (allowDups) {
                        // In case users are allowed to add the same
                        // item more than once, use this complicated
                        // procedure.
                        // The assumption is that "1" comes before "a".
                        //
                        if (DEBUG) {
                            log("\tAdding duplicate "
                                    + "and creating unique key.");
                        }
                        String key2 = selectedKey.concat(DUP_STRING);
                        while (selectedItems.containsKey(key2)) {
                            key2 = key2.concat(DUP_STRING);
                        }
                        selectedItems.put(key2, listItem);
                    } else {
                        if (DEBUG) {
                            log("\tDuplicates not allowed "
                                    + "ignoring this duplicate selected item.");
                        }
                    }
                } else {
                    // Add the found key to the removeItems map
                    // and add to the selectedItems.
                    removeItems.add(key);
                    selectedItems.put(selectedKey, listItem);
                }

                // We have a match break the loop
                break;
            }
            if (DEBUG) {
                if (!match) {
                    log("\tSelected value "
                            + String.valueOf(selectedValue)
                            + " not present on the list of options.");
                }
            }
        }

        if (!allowDups) {
            if (DEBUG) {
                log("\tRemove the selected items from "
                        + "the available items");
            }
            Iterator<String> keys = removeItems.iterator();
            String key;
            while (keys.hasNext()) {
                key = keys.next();
                availableItems.remove(key);
            }
        }
    }

    /**
     * This implementation returns {@code false}.
     * @return {@code boolean}
     */
    @Override
    public boolean mainListSubmits() {
        return false;
    }

    // Hide value
    /**
     * This implementation invokes {@code super.getValue}.
     * @return Object
     */
    @Property(name = "value", isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    // Hide labelLevel
    /**
     * This implementation invokes {@code super.getLabelLevel}.
     * @return int
     */
    @Property(name = "labelLevel", isHidden = true, isAttribute = false)
    @Override
    public int getLabelLevel() {
        return super.getLabelLevel();
    }

    /**
     * This implementation invokes {@code super.isSeparators}.
     * @return {@code boolean}
     */
    // Hide separators
    @Property(name = "separators", isHidden = true, isAttribute = false)
    @Override
    public boolean isSeparators() {
        return super.isSeparators();
    }

    /**
     * This implementation invokes {@code super.getOnBlur}.
     * @return String
     */
    // Hide onBlur
    @Property(name = "onBlur", isHidden = true, isAttribute = false)
    @Override
    public String getOnBlur() {
        return super.getOnBlur();
    }

    /**
     * This implementation invokes {@code super.getOnChange}.
     * @return String
     */
    // Hide onChange
    @Property(name = "onChange", isHidden = true, isAttribute = false)
    @Override
    public String getOnChange() {
        return super.getOnChange();
    }

    /**
     * This implementation invokes {@code super.getOnClick}.
     * @return String
     */
    // Hide onClick
    @Property(name = "onClick", isHidden = true, isAttribute = false)
    @Override
    public String getOnClick() {
        return super.getOnClick();
    }

    /**
     * This implementation invokes {@code super.getOnDblClick}.
     * @return String
     */
    // Hide onDblClick
    @Property(name = "onDblClick", isHidden = true, isAttribute = false)
    @Override
    public String getOnDblClick() {
        return super.getOnDblClick();
    }

    /**
     * This implementation invokes {@code super.getOnFocus}.
     * @return String
     */
    // Hide onFocus
    @Property(name = "onFocus", isHidden = true, isAttribute = false)
    @Override
    public String getOnFocus() {
        return super.getOnFocus();
    }

    /**
     * This implementation invokes {@code super.getOnKeyDown}.
     * @return String
     */
    // Hide onKeyDown
    @Property(name = "onKeyDown", isHidden = true, isAttribute = false)
    @Override
    public String getOnKeyDown() {
        return super.getOnKeyDown();
    }

    /**
     * This implementation invokes {@code super.getOnKeyPress}.
     * @return String
     */
    // Hide onKeyPress
    @Property(name = "onKeyPress", isHidden = true, isAttribute = false)
    @Override
    public String getOnKeyPress() {
        return super.getOnKeyPress();
    }

    /**
     * This implementation invokes {@code super.getOnKeyUp}.
     * @return String
     */
    // Hide onKeyUp
    @Property(name = "onKeyUp", isHidden = true, isAttribute = false)
    @Override
    public String getOnKeyUp() {
        return super.getOnKeyUp();
    }

    /**
     * This implementation invokes {@code super.getOnMouseDown}.
     * @return String
     */
    // Hide onMouseDown
    @Property(name = "onMouseDown", isHidden = true, isAttribute = false)
    @Override
    public String getOnMouseDown() {
        return super.getOnMouseDown();
    }

    /**
     * This implementation invokes {@code super.getOnMouseMove}.
     * @return String
     */
    // Hide onMouseMove
    @Property(name = "onMouseMove", isHidden = true, isAttribute = false)
    @Override
    public String getOnMouseMove() {
        return super.getOnMouseMove();
    }

    /**
     * This implementation invokes {@code super.getOnMouseOut}.
     * @return String
     */
    // Hide onMouseOut
    @Property(name = "onMouseOut", isHidden = true, isAttribute = false)
    @Override
    public String getOnMouseOut() {
        return super.getOnMouseOut();
    }

    /**
     * This implementation invokes {@code super.getOnMouseOver}.
     * @return String
     */
    // Hide onMouseOver
    @Property(name = "onMouseOver", isHidden = true, isAttribute = false)
    @Override
    public String getOnMouseOver() {
        return super.getOnMouseOver();
    }

    /**
     * This implementation invokes {@code super.getOnMouseUp}.
     * @return String
     */
    // Hide onMouseUp
    @Property(name = "onMouseUp", isHidden = true, isAttribute = false)
    @Override
    public String getOnMouseUp() {
        return super.getOnMouseUp();
    }

    /**
     * This implementation invokes {@code super.getOnSelect}.
     * @return String
     */
    // Hide onSelect
    @Property(name = "onSelect", isHidden = true, isAttribute = false)
    @Override
    public String getOnSelect() {
        return super.getOnSelect();
    }

    /**
     * The label for the list of available items.
     * @return String
     */
    public String getAvailableItemsLabel() {
        if (this.availableItemsLabel != null) {
            return this.availableItemsLabel;
        }
        ValueExpression vb = getValueExpression("availableItemsLabel");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The label for the list of available items.
     *
     * @param newAvailableItemsLabel available items label
     * @see #getAvailableItemsLabel()
     */
    public void setAvailableItemsLabel(final String newAvailableItemsLabel) {
        this.availableItemsLabel = newAvailableItemsLabel;
    }

    /**
     * If duplicateSelections is set to true, items in the available list are
     * not removed when they are added to the selected list. The user is
     * permitted to add an available item more than once to the list of selected
     * items. The list of selected items would then contain duplicate
     * entries.
     * @return {@code boolean}
     */
    public boolean isDuplicateSelections() {
        if (this.duplicateSelectionsSet) {
            return this.duplicateSelections;
        }
        ValueExpression vb = getValueExpression("duplicateSelections");
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
     * If duplicateSelections is set to true, items in the available list are
     * not removed when they are added to the selected list. The user is
     * permitted to add an available item more than once to the list of selected
     * items. The list of selected items would then contain duplicate
     * entries.
     * @param newDuplicateSelections duplicate selections value
     *
     * @see #isDuplicateSelections()
     */
    public void setDuplicateSelections(final boolean newDuplicateSelections) {
        this.duplicateSelections = newDuplicateSelections;
        this.duplicateSelectionsSet = true;
    }

    /**
     * Show the Move Up and Move Down buttons.
     * @return {@code boolean}
     */
    public boolean isMoveButtons() {
        if (this.moveButtonsSet) {
            return this.moveButtons;
        }
        ValueExpression vb = getValueExpression("moveButtons");
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
     * Show the Move Up and Move Down buttons.
     *
     * @param newMoveButtons move buttons value
     * @see #isMoveButtons()
     */
    public void setMoveButtons(final boolean newMoveButtons) {
        this.moveButtons = newMoveButtons;
        this.moveButtonsSet = true;
    }

    /**
     * Show the Add All and Remove All buttons.
     * @return {@code boolean}
     */
    public boolean isSelectAll() {
        if (this.selectAllSet) {
            return this.selectAll;
        }
        ValueExpression vb = getValueExpression("selectAll");
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
     * Show the Add All and Remove All buttons.
     *
     * @param newSelectAll select all value
     * @see #isSelectAll()
     */
    public void setSelectAll(final boolean newSelectAll) {
        this.selectAll = newSelectAll;
        this.selectAllSet = true;
    }

    /**
     * The label for the list of selected items.
     * @return String
     */
    public String getSelectedItemsLabel() {
        if (this.selectedItemsLabel != null) {
            return this.selectedItemsLabel;
        }
        ValueExpression vb = getValueExpression("selectedItemsLabel");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The label for the list of selected items.
     * @param newSelectedItemsLabel selected items label
     *
     * @see #getSelectedItemsLabel()
     */
    public void setSelectedItemsLabel(final String newSelectedItemsLabel) {
        this.selectedItemsLabel = newSelectedItemsLabel;
    }

    /**
     * If sorted is set to true, the items on the available list are shown in
     * alphabetical order.The items on the selected options list are also shown
     * in alphabetical order, unless the moveButtons attribute is true, in which
     * case the user is expected to order the elements.
     *
     * @return {@code boolean}
     */
    public boolean isSorted() {
        if (this.sortedSet) {
            return this.sorted;
        }
        ValueExpression vb = getValueExpression("sorted");
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
     * If sorted is set to true, the items on the available list are shown in
     * alphabetical order. The items on the selected options list are also shown
     * in alphabetical order, unless the moveButtons attribute is true, in which
     * case the user is expected to order the elements.
     *
     * @param newSorted sorted value
     * @see #isSorted()
     */
    public void setSorted(final boolean newSorted) {
        this.sorted = newSorted;
        this.sortedSet = true;
    }

    /**
     * Use the vertical layout instead of the default horizontal layout.The
     * vertical layout displays the available items list above the selected
     * items list.
     *
     * @return {@code boolean}
     */
    public boolean isVertical() {
        if (this.verticalSet) {
            return this.vertical;
        }
        ValueExpression vb = getValueExpression("vertical");
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
     * Use the vertical layout instead of the default horizontal layout. The
     * vertical layout displays the available items list above the selected
     * items list.
     *
     * @param newVertical vertical value
     * @see #isVertical()
     */
    public void setVertical(final boolean newVertical) {
        this.vertical = newVertical;
        this.verticalSet = true;
    }

    /**
     * This implementation restores the state of all properties.
     * @param context faces context
     * @param state state object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.availableItemsLabel = (String) values[1];
        this.duplicateSelections = ((Boolean) values[2]);
        this.duplicateSelectionsSet = ((Boolean) values[3]);
        this.moveButtons = ((Boolean) values[4]);
        this.moveButtonsSet = ((Boolean) values[5]);
        this.selectAll = ((Boolean) values[6]);
        this.selectAllSet = ((Boolean) values[7]);
        this.selectedItemsLabel = (String) values[8];
        this.sorted = ((Boolean) values[9]);
        this.sortedSet = ((Boolean) values[10]);
        this.vertical = ((Boolean) values[11]);
        this.verticalSet = ((Boolean) values[12]);
    }

    /**
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[13];
        values[0] = super.saveState(context);
        values[1] = this.availableItemsLabel;
        if (this.duplicateSelections) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.duplicateSelectionsSet) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.moveButtons) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        if (this.moveButtonsSet) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        if (this.selectAll) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        if (this.selectAllSet) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        values[8] = this.selectedItemsLabel;
        if (this.sorted) {
            values[9] = Boolean.TRUE;
        } else {
            values[9] = Boolean.FALSE;
        }
        if (this.sortedSet) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        if (this.vertical) {
            values[11] = Boolean.TRUE;
        } else {
            values[11] = Boolean.FALSE;
        }
        if (this.verticalSet) {
            values[12] = Boolean.TRUE;
        } else {
            values[12] = Boolean.FALSE;
        }
        return values;
    }

    /**
     * Private method for development time error detecting.
     *
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(AddRemove.class.getName() + "::" + msg);
    }
}
