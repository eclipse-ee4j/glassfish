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
import com.sun.webui.jsf.model.list.ListItem;
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.JavaScriptUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.MessageUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.jsf.validator.StringLengthValidator;
import com.sun.webui.theme.Theme;

import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * The EditableList component allows users to create and modify a list of
 * strings.
 * <p>
 * Use this component when web application users need to create and modify a
 * list of strings. The application user can add new strings by typing them into
 * the textfield and clicking the "Add" button, and remove them by selecting one
 * or more items from the list and clicking the "Remove" button.</p>
 *
 * <h4>Configuring the listbox tag</h4>
 *
 * <p>
 * Use the {@code list} attribute to bind the component to a model. The
 * value must be an EL expression that corresponds to a managed bean or a
 * property of a managed bean, and it must evaluate to an array of
 *  {@code java.lang.String}.
 * </p>
 *
 * <p>
 * To set the label of the textfield, use the {@code fieldLabel} attribute.
 * To set the label of the textfield, use the {@code listLabel} attribute.
 * To validate new items, use the {@code fieldValidator} attribute; to
 * validate the contents of the list once the user has finished adding and
 * removing items, specify a {@code labelValidator}.</p>
 *
 * <h4>Facets</h4>
 *
 * <ul>
 * <li>{@code fieldLabel}: use this facet to specify a custom component for
 * the textfield label.</li>
 * <li>{@code listLabel}: use this facet to specify a custom component for
 * the textfield label.</li>
 * <li>{@code field}: use this facet to specify a custom component for the
 * textfield.</li>
 * <li>{@code addButton}: use this facet to specify a custom component for
 * the add button.</li>
 * <li>{@code removeButton}: use this facet to specify a custom component
 * for the remove button.</li>
 * <li>{@code search}: use this facet to specify a custom component for the
 * search button. </li>
 * <li>{@code readOnly}: use this facet to specify a custom component for
 * display a readonly version of the component.</li>
 * <li>{@code header}: use this facet to specify a header, rendered in a
 * table row above the component.</li>
 * <li>{@code footer}: use this facet to specify a header, rendered in a
 * table row below the component.</li>
 * </ul>
 *
 * <h4>Client-side JavaScript functions</h4>
 *
 * <ul>
 * <li>NONE yet</li>
 * </ul>
 */
@Component(type = "com.sun.webui.jsf.EditableList",
        family = "com.sun.webui.jsf.EditableList",
        displayName = "Editable List", tagName = "editableList",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_editable_list",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_editable_list_props")
        //CHECKSTYLE:ON
public final class EditableList extends WebuiInput implements ListManager,
        NamingContainer {

    /**
     * The component id for the ADD button.
     */
    public static final String ADD_BUTTON_ID = "_addButton";

    /**
     * The add button facet name.
     */
    public static final String ADD_BUTTON_FACET = "addButton";

    /**
     * The add button text key.
     */
    public static final String ADD_BUTTON_TEXT_KEY = "EditableList.add";

    /**
     * The component ID for the remove button.
     */
    public static final String REMOVE_BUTTON_ID = "_removeButton";

    /**
     * The remove button facet name.
     */
    public static final String REMOVE_BUTTON_FACET = "removeButton";

    /**
     * The remove button text key.
     */
    public static final String REMOVE_BUTTON_TEXT_KEY = "EditableList.remove";

    /**
     * The component ID for the text field.
     */
    public static final String FIELD_ID = "_field";

    /**
     * The input field facet name.
     */
    public static final String FIELD_FACET = "field";

    /**
     * The text key for the StringLengthValidator "too long" message.
     */
    public static final String SLV_TOOLONG_KEY
            = "EditableList.itemTooLong";

    /**
     * The text key for the StringLengthValidator "too short" message.
     */
    public static final String SLV_TOOSHORT_KEY
            = "EditableList.fieldEmpty";

    /**
     * The component ID for the text field.
     */
    public static final String LIST_LABEL_ID = "_listLabel";

    /**
     * The list label facet name.
     */
    public static final String LIST_LABEL_FACET = "listLabel";

    /**
     * The list default label text key.
     */
    public static final String LIST_LABEL_TEXT_KEY
            = "EditableList.defaultListLabel";

    /**
     * The component ID for the text field.
     */
    public static final String FIELD_LABEL_ID = "_fieldLabel";

    /**
     * The input field label facet name.
     */
    public static final String FIELD_LABEL_FACET = "fieldLabel";

    /**
     * The default field label text key.
     */
    public static final String FIELD_LABEL_TEXT_KEY
            = "EditableList.defaultFieldLabel";

    /**
     * The component ID for the text field.
     */
    public static final String READ_ONLY_ID = "_readOnly";

    /**
     * The read only facet name.
     */
    public static final String READ_ONLY_FACET = "readOnly";

    /**
     * Facet name for the header facet.
     */
    public static final String HEADER_FACET = "header";

    /**
     * Facet name for the footer facet.
     */
    public static final String FOOTER_FACET = "footer";

    /**
     * Name of the JavaScript function which is responsible for adding elements
     * from the available list to the selected list.
     */
    public static final String ADD_FUNCTION = ".add(); ";

    /**
     * Name of the JavaScript function which is responsible for
     * enabling/disabling the add button.
     */
    public static final String ENABLE_ADD_FUNCTION = ".enableAdd(); ";

    /**
     * Name of the JavaScript function which is responsible for
     * enabling/disabling the add button.
     */
    public static final String SET_ADD_DISABLED_FUNCTION =
            ".setAddDisabled(false);";

    /**
     * Name of the JavaScript function which is responsible for
     * enabling/disabling the remove button.
     */
    public static final String ENABLE_REMOVE_FUNCTION = ".enableRemove(); ";

    /**
     * Name of the JavaScript function that updates the buttons.
     */
    public static final String UPDATE_BUTTONS_FUNCTION = ".updateButtons(); ";

    // FIXME: This should be part of the theme.
    /**
     * Read only separator string.
     */
    private static final String READ_ONLY_SEPARATOR = ", ";

    /**
     * Facet name for the search facet.
     */
    public static final String SEARCH_FACET = "search";

    // FIXME: This should be part of the theme.

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
    private static final String DUP_STRING = "\t";

    // FIXME: This should be part of the theme.

    /**
     * Min length.
     */
    private static final int MIN_LENGTH = 20;

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * ListItems.
     */
    private TreeMap<String, ListItem> listItems = null;

    /**
     * Text collator.
     */
    private Collator collator = null;

    /**
     * Theme in-use.
     */
    private transient Theme theme = null;

    /**
     * Selected value.
     */
    private String selectedValue = null;

    /**
     * values to remove.
     */
    private String[] valuesToRemove = null;

    /**
     * Flag indicating that the user is not permitted to activate this
     * component, and that the component's value will not be submitted with the
     * form.
     */
    @Property(name = "disabled",
            displayName = "Disabled",
            category = "Behavior")
    private boolean disabled = false;

    /**
     * disabled set flag.
     */
    private boolean disabledSet = false;

    /**
     * Text to be used as the label next to the input text field.
     */
    @Property(name = "fieldLabel",
            displayName = "Textfield Label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String fieldLabel = null;

    /**
     * Sets the style level for the generated labels. Valid values are 1
     * (largest), 2 and 3 (smallest). The default value is 2.
     */
    @Property(name = "labelLevel",
            displayName = "Label Level",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.LabelLevelsEditor")
            //CHECKSTYLE:ON
    private int labelLevel = Integer.MIN_VALUE;

    /**
     * labelLevel set flag.
     */
    private boolean labelLevelSet = false;

    /**
     * Text to be used as the label next to the list box.
     */
    @Property(name = "listLabel",
            displayName = "List Label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String listLabel = null;

    /**
     * Specifies the display order of the parts of this component. When set to
     * true, the listOnTop attribute causes the list box to be displayed above
     * the text input field. By default, the list box is displayed below the
     * input field.
     */
    @Property(name = "listOnTop",
            displayName = "Show List On Top",
            category = "Advanced")
    private boolean listOnTop = false;

    /**
     * listOnTop set flag.
     */
    private boolean listOnTopSet = false;

    /**
     * The maximum number of characters allowed for each string in the list.
     */
    @Property(name = "maxLength",
            displayName = "Maximum String Length",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int maxLength = Integer.MIN_VALUE;

    /**
     * maxLength set flag.
     */
    private boolean maxLengthSet = false;

    /**
     * Flag indicating that the application user can select more than one option
     * at a time in the list box.
     */
    @Property(name = "multiple",
            displayName = "Multiple",
            category = "Data")
    private boolean multiple = false;

    /**
     * multiple set flag.
     */
    private boolean multipleSet = false;

    /**
     * If this attribute is set to true, the value of the component is rendered
     * as text, preceded by the label if one was defined.
     */
    @Property(name = "readOnly",
            displayName = "Read-only",
            category = "Behavior")
    private boolean readOnly = false;

    /**
     * readOnly set flag.
     */
    private boolean readOnlySet = false;

    /**
     * The number of items to display, which determines the length of the
     * rendered list box. The default value is 6.
     */
    @Property(name = "rows",
            displayName = "Number of Items to Display",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int rows = Integer.MIN_VALUE;

    /**
     * rows set flag.
     */
    private boolean rowsSet = false;

    /**
     * Set sorted to true if the list items should be sorted in locale-specific
     * alphabetical order. The sorting is performed using a Collator configured
     * with the locale from the FacesContext.
     */
    @Property(name = "sorted",
            displayName = "Sorted",
            category = "Advanced")
    private boolean sorted = false;

    /**
     * sorted set flag.
     */
    private boolean sortedSet = false;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
            //CHECKSTYLE:ON
    private String style = null;

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
    private String styleClass = null;

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
    private boolean tabIndexSet = false;

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     */
    @Property(name = "toolTip",
            displayName = "Tool Tip",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String toolTip = null;

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
    private boolean visible = false;

    /**
     * visible set flag.
     */
    private boolean visibleSet = false;

    /**
     * A validator that will be applied to entries made into the textfield.
     * Specify this to be the {@code validate()} method of a
     * {@code jakarta.faces.validator.Validator}, or to another method with the
     * same argument structure and exceptions.
     */
    @Property(name = "fieldValidatorExpression",
            displayName = "Field Validator Expression",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.ValidatorPropertyEditor")
    //CHECKSTYLE:ON
    @Property.Method(
            //CHECKSTYLE:OFF
            signature = "void validate(jakarta.faces.context.FacesContext,jakarta.faces.component.UIComponent,java.lang.Object)")
    //CHECKSTYLE:ON
    private MethodExpression fieldValidatorExpression;

    /**
     * Default constructor.
     */
    public EditableList() {
        super();
        setRendererType("com.sun.webui.jsf.EditableList");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.EditableList";
    }

    // Hide converter
    @Property(name = "converter", isHidden = true, isAttribute = false)
    @Override
    public Converter getConverter() {
        return super.getConverter();
    }

    // Hide immediate
    @Property(name = "immediate", isHidden = true, isAttribute = false)
    @Override
    public boolean isImmediate() {
        return false;
    }

    // Hide value
    @Property(name = "value", isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * A validator which will be applied to the contents of the list (e.g. to
     * verify that the list has a minimum number of entries). Specify this to be
     * the {@code validate()} method of a
     * {@code jakarta.faces.validator.Validator}, or to another method with the
     * same argument structure and exceptions.
     *
     * @return MethodExpression
     */
    @Property(name = "listValidatorExpression",
            isHidden = false,
            displayName = "List Validator Expression",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.ValidatorPropertyEditor")
    //CHECKSTYLE:ON
    @Property.Method(
            //CHECKSTYLE:OFF
            signature = "void validate(jakarta.faces.context.FacesContext,jakarta.faces.component.UIComponent,java.lang.Object)")
    //CHECKSTYLE:ON
    public MethodExpression getListValidatorExpression() {
        return getValidatorExpression();
    }

    /**
     * A validator which will be applied to the contents of the list (e.g. to
     * verify that the list has a minimum number of entries). Specify this to be
     * the {@code validate()} method of a
     * {@code jakarta.faces.validator.Validator}, or to another method with the
     * same argument structure and exceptions.
     *
     * @see #getListValidatorExpression()
     * @param listValidator listValidator
     */
    public void setListValidatorExpression(
            final MethodExpression listValidator) {
        setValidatorExpression(listValidator);
    }

    @Property(name = "validatorExpression",
            isHidden = true,
            isAttribute = false)
    @Override
    public MethodExpression getValidatorExpression() {
        return super.getValidatorExpression();
    }

    /**
     * The object that represents the list. The list attribute must be an EL
     * expression that evaluates to an object of type{@code java.lang.String[]}.
     *
     * @return Object
     */
    @Property(name = "list",
            displayName = "List",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor")
    //CHECKSTYLE:ON
    public Object getList() {
        return getValue();
    }

    /**
     * The object that represents the list. The list attribute must be an EL
     * expression that evaluates to an object of type{@code java.lang.String[]}.
     *
     * @see #getList()
     * @param newList list
     */
    public void setList(final Object newList) {
        setValue(newList);
    }

    /**
     * Get the maximum length of the strings on the list. If the length is less
     * than 1, the default value of 15 is returned.
     *
     * @return An integer value for the maximum number of characters on the list
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public int getMaxLength() {
        int length;
        if (this.maxLengthSet) {
            length = this.maxLength;
        } else {
            ValueExpression vb = getValueExpression("maxLength");
            if (vb != null) {
                Object result = vb.getValue(
                        getFacesContext().getELContext());
                if (result == null) {
                    length = Integer.MIN_VALUE;
                } else {
                    length = ((Integer) result);
                }
            } else {
                length = 25;
            }
        }
        if (length < 1) {
            // FIXME: Should be part of Theme.
            length = 25;
            // Shouldn't reset the length, it clobbers a
            // developers value. Just return the default.
            //super.setMaxLength(length);
        }
        return length;
    }

    // Buttons
    /**
     * Return a component that implements the add button. If a facet named
     * {@code addButton} is found that component is returned. Otherwise a
     * {@code Button} component is returned. It is assigned the id
     * {@code getId() + "_addButton"}
     * <p>
     * If the facet is not defined then the returned {@code Button}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return an add button component
     */
    public UIComponent getAddButtonComponent() {
        if (DEBUG) {
            log("getAddButtonComponent()");
        }
        return getButtonFacet(ADD_BUTTON_FACET, false,
                getTheme().getMessage(ADD_BUTTON_TEXT_KEY), new AddListener());
    }

    /**
     * Return a component that implements the remove button. If a facet named
     * {@code removeButton} is found that component is returned. Otherwise
     * a {@code Button} component is returned. It is assigned the id
     * {@code getId() + "_removeButton"}
     * <p>
     * If the facet is not defined then the returned {@code Button}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return a remove button component
     */
    public UIComponent getRemoveButtonComponent() {
        if (DEBUG) {
            log("getRemoveButtonComponent()");
        }
        return getButtonFacet(REMOVE_BUTTON_FACET, false,
                getTheme().getMessage(REMOVE_BUTTON_TEXT_KEY),
                new RemoveListener());
    }

    /**
     * Return a component that implements a button facet. If a facet named
     * {@code facetName} is found that component is returned. Otherwise a
     * {@code Button} component is returned. It is assigned the id
     * {@code getId() + "_<facetName>"}
     * <p>
     * If the facet is not defined then the returned {@code Button}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @param facetName the name of the facet to return or create
     * @param primary if false the button is not a primary button
     * @param text the button text
     * @param actionListener the button's actionListener
     *
     * @return a button facet component
     */
    private UIComponent getButtonFacet(final String facetName,
            final boolean primary, final String text,
            final ActionListener actionListener) {

        if (DEBUG) {
            log("getButtonFacet() " + facetName);
        }
        // Check if the page author has defined the facet
        //
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
        //
        Button button = (Button) ComponentUtilities.getPrivateFacet(this,
                facetName, true);
        if (button == null) {
            if (DEBUG) {
                log("create Button");
            }
            button = new Button();
            button.setId(ComponentUtilities.createPrivateFacetId(this,
                    facetName));
            button.addActionListener(actionListener);
            ComponentUtilities.putPrivateFacet(this, facetName, button);
        }
        initButtonFacet(button, primary, text);
        return button;
    }

    /**
     * Initialize a {@code Button} facet.
     *
     * @param button the Button instance
     * @param primary primary flag
     * @param text the button text
     */
    private void initButtonFacet(final Button button, final boolean primary,
            final String text) {

        if (DEBUG) {
            log("initButtonFacet()");
        }
        button.setText(text);
        int tindex = getTabIndex();
        if (tindex > 0) {
            button.setTabIndex(tindex);
        }
        button.setImmediate(true);
        button.setPrimary(primary);
        button.setDisabled(isDisabled());
    }

    // Labels
    /**
     * Return a component that implements a label for the list. If a facet named
     * {@code listLabel} is found that component is returned. Otherwise a
     * {@code Label} component is returned. It is assigned the id
     * {@code getId() + "_listLabel"}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return a list label component
     */
    public UIComponent getListLabelComponent() {
        if (DEBUG) {
            log("getListLabelComponent()");
        }
        String labelString = getListLabel();
        if (labelString == null || labelString.length() == 0) {
            labelString = getTheme().getMessage(LIST_LABEL_TEXT_KEY);
        }
        return getLabelFacet(LIST_LABEL_FACET, labelString, this);
    }

    /**
     * Return a component that implements a label for the input field. If a
     * facet named {@code fieldLabel} is found that component is returned.
     * Otherwise a {@code Label} component is returned. It is assigned the
     * id
     * {@code getId() + "_fieldLabel"}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return a field label component
     */
    public UIComponent getFieldLabelComponent() {
        if (DEBUG) {
            log("getFieldLabelComponent()");
        }
        String labelString = getFieldLabel();
        if (labelString == null || labelString.length() == 0) {
            labelString = getTheme().getMessage(FIELD_LABEL_TEXT_KEY);
        }
        // This will cause two initializations of the private field
        // facet, in succession. The renderer is calling
        // getFieldLabelComponent()
        // getFieldComponent()
        //
        return getLabelFacet(FIELD_LABEL_FACET, labelString,
                getFieldComponent());
    }

    /**
     * Return a component that implements a label facet. If a facet named
     * {@code facetName} is found that component is returned. Otherwise a
     * {@code Label} component is returned. It is assigned the id
     * {@code getId() + "_<facetName>"}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @param facetName the name of the facet to return or create
     * @param text the label text
     * @param forComponent the component instance this label is for
     *
     * @return a label facet component
     */
    private UIComponent getLabelFacet(final String facetName, final String text,
            final UIComponent forComponent) {

        if (DEBUG) {
            log("getLabelFacet() " + facetName);
        }
        // Check if the page author has defined the facet
        UIComponent labelComponent = getFacet(facetName);
        if (labelComponent != null) {
            if (DEBUG) {
                log("\tFound facet");
            }
            return labelComponent;
        }

        // Return the private facet or create one, but initialize
        // it every time
        //
        // We know it's a Label
        Label label = (Label) ComponentUtilities.getPrivateFacet(this,
                facetName, true);
        if (label == null) {
            if (DEBUG) {
                log("create Label");
            }
            label = new Label();
            label.setId(ComponentUtilities.createPrivateFacetId(this,
                    facetName));
            ComponentUtilities.putPrivateFacet(this, facetName, label);
        }
        initLabelFacet(label, text, forComponent
                .getClientId(getFacesContext()));

        return label;
    }

    /**
     * Initialize a label facet.
     *
     * @param label the Label instance
     * @param labelString the label text.
     * @param forComponentId the client id of the component instance this label
     * is for
     */
    private void initLabelFacet(final Label label, final String labelString,
            final String forComponentId) {

        if (DEBUG) {
            log("initLabelFacet()");
        }
        if (labelString == null || labelString.length() < 1) {
            // TODO - maybe print a default?
            // A Theme default value.
            label.setText("");
        } else {
            label.setText(labelString);
        }
        label.setLabelLevel(getLabelLevel());
        if (!isReadOnly()) {
            label.setFor(forComponentId);
        }
    }

    // Other
    /**
     * Return the actual facet or private facet without re-initializing it, if
     * it exists. We sometimes want the component in the state that it was last
     * rendered with and not a newly initialized state, like during validation
     * processing or decoding. For example, you don't want to render with a 30
     * character StringLengthValidator and then validate against a 25 character
     * validator on the odd chance that getMaxLength() returns a different value
     * during decode.
     * @return UIComponent
     */
    private UIComponent getRenderedFieldComponent() {

        // Check if the page author has defined the facet
        UIComponent fieldComponent = getFacet(FIELD_FACET);
        if (fieldComponent != null) {
            if (DEBUG) {
                log("\tFound facet");
            }
            return fieldComponent;
        }

        // If the private facet does't exist
        // just call the normal "getFieldComponent", but
        // it should exist, since getRenderedFieldComponent is designed
        // to be used for lifecycle phases before rendereding, during
        // request processing.
        fieldComponent = ComponentUtilities.getPrivateFacet(this,
                FIELD_FACET, false);

        if (fieldComponent == null) {
            return getFieldComponent();
        }
        return fieldComponent;
    }

    /**
     * Return a component that implements an input field facet. If a facet named
     * {@code field} is found that component is returned. Otherwise a
     * {@code TextField} component is returned. It is assigned the id
     * {@code getId() + "_field"}
     * <p>
     * If the facet is not defined then the returned {@code TextField}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return an input field facet component
     */
    public UIComponent getFieldComponent() {

        if (DEBUG) {
            log("getFieldComponent()");
        }
        // Check if the page author has defined the facet
        UIComponent fieldComponent = getFacet(FIELD_FACET);
        if (fieldComponent != null) {
            if (DEBUG) {
                log("\tFound facet");
            }
            return fieldComponent;
        }

        // Return the private facet or create one, but initialize
        // it every time
        //
        // We know it's a TextField
        TextField field = (TextField) ComponentUtilities.getPrivateFacet(this,
                FIELD_FACET, true);
        if (field == null) {
            if (DEBUG) {
                log("create Field");
            }
            field = new TextField();
            field.setId(ComponentUtilities.createPrivateFacetId(this,
                    FIELD_FACET));
            field.setTrim(true);
            ComponentUtilities.putPrivateFacet(this, FIELD_FACET, field);

            // Add the validator ONCE !
            StringLengthValidator strl
                    = new StringLengthValidator(getMaxLength(), 1);

            Theme th = getTheme();
            strl.setTooLongMessage(th.getMessage(SLV_TOOLONG_KEY));
            strl.setTooShortMessage(th.getMessage(SLV_TOOSHORT_KEY));

            field.addValidator(strl);

        }
        initFieldFacet(field);
        return field;
    }

    /**
     * Initialize the field facet.
     * @param field facet to initialize
     */
    private void initFieldFacet(final TextField field) {
        if (DEBUG) {
            log("initFieldFacet()");
        }
        String jsObjectName = getJavaScriptObjectName();
        StringBuilder onkeypressBuffer = new StringBuilder();
        onkeypressBuffer.append("if(event.keyCode == 13) { ");
        onkeypressBuffer.append(jsObjectName);
        onkeypressBuffer.append(ADD_FUNCTION);
        onkeypressBuffer.append("return false; } ");

        field.setOnKeyPress(onkeypressBuffer.toString());

        StringBuilder onfocusBuffer = new StringBuilder();
        onfocusBuffer.append(jsObjectName);
        onfocusBuffer.append(SET_ADD_DISABLED_FUNCTION);
        onfocusBuffer.append("return false;");

        field.setOnFocus(onfocusBuffer.toString());

        StringBuilder onfocuslostBuffer = new StringBuilder();
        onfocuslostBuffer.append(jsObjectName);
        onfocuslostBuffer.append(ENABLE_ADD_FUNCTION);
        onfocuslostBuffer.append("return false;");

        field.setOnBlur(onfocuslostBuffer.toString());

        // FIXME: MIN_LENGTH should be part of Theme
        int columns = getMaxLength();
        if (columns < MIN_LENGTH) {
            columns = MIN_LENGTH;
        }
        field.setColumns(columns);

        int tindex = getTabIndex();
        if (tindex > 0) {
            field.setTabIndex(tindex);
        }

        field.setDisabled(isDisabled());

        // Now add an application field validator expression
        // Do this every time in case it was changed on the
        // EditableList.
        field.setValidatorExpression(getFieldValidatorExpression());
    }

    /**
     * Return a component that implements the read only value of this
     * EditableList. If a facet named {@code readOnly} is found that
     * component is returned. Otherwise a {@code StaticText} component is
     * returned. It is assigned the id
     * {@code getId() + "_readOnly"}
     * <p>
     * If the facet is not defined then the returned {@code StaticText}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return a component that represents the read only value of this
     * EditableList
     */
    @Override
    public UIComponent getReadOnlyValueComponent() {
        if (DEBUG) {
            log("getReadOnlyValueComponent()");
        }
        // Check if the page author has defined the facet
        //
        UIComponent textComponent = getFacet(READ_ONLY_FACET);
        if (textComponent != null) {
            if (DEBUG) {
                log("\tFound facet");
            }
            return textComponent;
        }

        // Just create it every time.
        //
        if (DEBUG) {
            log("create StaticText");
        }
        StaticText text = new StaticText();
        text.setId(ComponentUtilities.createPrivateFacetId(this,
                READ_ONLY_FACET));
        text.setParent(this);

        FacesContext context = FacesContext.getCurrentInstance();
        String readOnlyString = getValueAsReadOnly(context);
        if (readOnlyString == null || readOnlyString.length() < 1) {
            // TODO - maybe print a default?
            readOnlyString = new String();
        }
        text.setText(readOnlyString);
        return text;
    }

    // Readonly value
    /**
     * Return a string suitable for displaying the value in read only mode. The
     * default is to separate the list values with a comma.
     *
     * @param context The FacesContext
     * @return String
     * @throws jakarta.faces.FacesException If the list items cannot be processed
     */
    private String getValueAsReadOnly(final FacesContext context)
            throws FacesException {

        // The comma format READ_ONLY_SEPARATOR should be part of the theme
        // and/or configurable by the application
        StringBuilder valueBuffer = new StringBuilder();
        Iterator iterator = getListItems(context, false);

        while (iterator.hasNext()) {
            String string = ((ListItem) (iterator.next())).getLabel();
            // Do this with a boolean on getListItems instead
            if (string.contains("nbsp")) {
                continue;
            }
            valueBuffer.append(string);
            if (iterator.hasNext()) {
                valueBuffer.append(READ_ONLY_SEPARATOR);
            }
        }
        return valueBuffer.toString();
    }

    // The following methods overrides default behaviour that does not
    // make sense for this component
    @Override
    public void setConverter(final jakarta.faces.convert.Converter converter) {
        String msg = getTheme().getMessage("EditableList.noConversion");
        throw new RuntimeException(msg);
    }

    /**
     * Get the JS object name.
     * @return String
     */
    public String getJavaScriptObjectName() {
        return JavaScriptUtilities.getDomNode(getFacesContext(), this);
    }

    /**
     * Get the theme.
     * @return Theme
     */
    private Theme getTheme() {
        return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }

    @Override
    public String getOnChange() {
        StringBuilder onchangeBuffer = new StringBuilder();
        onchangeBuffer.append(getJavaScriptObjectName());
        onchangeBuffer.append(ENABLE_REMOVE_FUNCTION);
        return onchangeBuffer.toString();
    }

    @Override
    public String getLabeledElementId(final FacesContext context) {
        // If this component is marked read only then it is
        // not appropriate for a label's for attribute.
        //
        if (isReadOnly()) {
            return null;
        } else {
            return getClientId(context).concat(ListSelector.LIST_ID);
        }
    }

    @Override
    public String getFocusElementId(final FacesContext context) {
        // This is a little sketchy, because in this case we'd actually prefer
        // to return different values for focus and for the labelled component.
        // We should always label the list (that's the one that should have the
        // invalid icon if the list is empty, for example. But we should
        // probably also set the focus to the top input component which could
        // be either the field or the label. Ah well. I can get around this
        // if I implement some extra bits on the label.
        // TODO
        return getLabeledElementId(context);
    }

    /**
     * Implement this method so that it returns the DOM ID of the HTML element
     * which should receive focus when the component receives focus, and to
     * which a component label should apply. Usually, this is the first element
     * that accepts input.
     *
     * @param context The FacesContext for the request
     * @return The client id, also the JavaScript element id
     *
     * @deprecated
     * @see #getLabeledElementId
     * @see #getFocusElementId
     */
    @Override
    public String getPrimaryElementID(final FacesContext context) {
        // In case callers can't handle null do not
        // return getLabeledElementId here.
        //
        return getClientId(context).concat(ListSelector.LIST_ID);
    }

    /**
     * Getter for property valuesToRemove.
     *
     * @return Value of property valuesToRemove.
     */
    public String[] getValuesToRemove() {
        if (valuesToRemove == null) {
            return new String[0];
        }
        return this.valuesToRemove;
    }

    /**
     * Setter for property valuesToRemove.
     *
     * @param values New value of property valuesToRemove.
     */
    public void setValuesToRemove(final String[] values) {
        this.valuesToRemove = values;
    }

    /**
     * Retrieve an Iterator of ListSelector.ListItem, to be used by the
     * renderer.
     *
     * @param context faces context
     * @param rulerAtEnd ruler flag
     * @return an Iterator over {@link ListItem}.
     * @throws jakarta.faces.FacesException
     */
    @Override
    @SuppressWarnings("checkstyle:methodlength")
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
        listItems = new TreeMap<String, ListItem>(collator);

        // Are we sorting ?
        boolean zSorted = isSorted();

        // Keep a list of keys that have been seen
        // to support duplicates when sorting.
        //
        Map<String, String> keysSeen;
        if (zSorted) {
            keysSeen = new HashMap<String, String>();
        } else {
            keysSeen = null;
        }

        // We have to make sure that the long empty list item (whose
        // purpose is to guarantee that the size of the list stays
        // constant) is alwasy at the bottom of the list.  (=has the
        // highest key in the map). We do this by identifying the
        // longest key in the map, as long as the collator is
        // concerned and appending something at the end. (It's not
        // possible to use a constant for this, since an o with an
        // umlaut comes after z in Swedish, but before it in German,
        // for example).
        String lastKey = "";
        String[] currentValues = getCurrentValueAsStringArray();
        if (DEBUG) {
            log("\tValues are:");
            for (int i = 0; i < currentValues.length; ++i) {
                log(currentValues[i]);
            }
        }

        // The string currently being evaluated
        String currentString;

        // Two cases:
        // First case: the page author requested a sorted map (by
        // character), in which case we sort by the strings
        // themselves. The last key is set to the string that the
        // collator deems to be the last.
        // Second case: the list is sorted by the order they were
        // added to the map. We deal with that by generating a
        // successively longer key for each entry (maps do not
        // conserve the order the items were added). The last key
        // is set to the last key generated.
        ListItem listItem;

        // Keep track of the last ListItem that matches the
        // selectedValue. This is necessary so that the last
        // seen duplicate of the selectedValue is selected.
        // And so that only one match is selected.
        ListItem selectedItem = null;

        // If the page author does not want the list items to be
        // sorted (alphabetically by locale), then they're
        // supposed to be sorted by the order they were added.
        // Maps are not guaranteed to return items in the order
        // they were added, so we have to create this order
        // artificially. We do that by creating a successively
        // longer key for each element. (a, aa, aaa...).
        //
        // Only need this when not sorting
        StringBuilder unsortedKeyBuffer;
        if (zSorted) {
            unsortedKeyBuffer = null;
        } else {
            unsortedKeyBuffer = new StringBuilder(KEY_STRING);
        }

        for (int counter = 0; counter < currentValues.length; ++counter) {
            currentString = currentValues[counter];

            if (DEBUG) {
                log("Current string is " + currentString);
                log("SelectedValue is "
                        + String.valueOf(selectedValue));
            }

            if (currentString == null) {
                String msg = MessageUtil.getMessage(
                        "com.sun.webui.jsf.resources.LogMessages",
                        "EditableList.badValue",
                        new Object[]{
                            getClientId(context)
                        });
                throw new FacesException(msg);
            }

            listItem = new ListItem(currentString);
            listItem.setValue(currentString);

            // The selectedValue will be the last entry in the
            // submittedValues array (getCurrentValueAsStringArray)
            // if it is not null. It will also appear as the "last"
            // duplicate.
            //
            // Keep track of last ListItem that matches the selectedValue
            // and at the end mark the last one found as selected.
            if (currentString.equals(selectedValue)) {
                if (DEBUG) {
                    log("Selected value seen");
                }
                selectedItem = listItem;
            }
            if (zSorted) {
                // Since duplicates are allowed, if a duplicate occurs
                // the key must be unique but must sort next
                // to the duplicate. Add an increasing string of
                // spaces to the duplicate key which should sort the keys
                // next to each other.
                String key = currentString;
                if (keysSeen.containsKey(key)) {
                    String dupString = keysSeen.get(key);
                    dupString = dupString.concat(DUP_STRING);
                    key = key.concat(dupString);
                    keysSeen.put(currentString, dupString);
                } else {
                    keysSeen.put(key, DUP_STRING);
                }
                if (collator.compare(key, lastKey) > 0) {
                    lastKey = key;
                }
                listItems.put(key, listItem);
            } else {
                listItems.put(unsortedKeyBuffer.toString(), listItem);
                unsortedKeyBuffer.append(KEY_STRING);
            }
        }

        // selectedItem was the last seen entry that matched the
        // selectedValue
        if (selectedItem != null) {
            selectedItem.setSelected(true);
        }

        if (!zSorted) {
            lastKey = unsortedKeyBuffer.toString();
        }

        // rulerAtEnd will be true if the invoker needs a blank
        // disabled list option at the end. Typically this is
        // needed by the renderer, to guarantee that the widget
        // stays the same in size when items are added and removed.
        if (rulerAtEnd) {
            int length = getMaxLength();
            if (length < MIN_LENGTH) {
                length = MIN_LENGTH;
            }
            StringBuilder labelBuffer = new StringBuilder(length);
            for (int counter = 0; counter < length; ++counter) {
                labelBuffer.append(SPACER_STRING);
            }
            ListItem item = new ListItem(labelBuffer.toString());
            item.setDisabled(true);
            listItems.put(lastKey.concat(KEY_STRING), item);
        }
        return listItems.values().iterator();
    }

    /**
     * Get current values as string array.
     * @return String[]
     */
    private String[] getCurrentValueAsStringArray() {

        if (DEBUG) {
            log("getCurrentValueAsStringArray()");
        }
        Object value = getSubmittedValue();
        if (value == null) {
            if (DEBUG) {
                log("\tUsing regular value");
            }
            value = getValue();
        } else if (DEBUG) {
            log("\tUsing submitted value");
        }

        if (value == null) {
            return new String[0];
        }
        if (value instanceof String[]) {
            return (String[]) value;
        }

        String msg = MessageUtil.getMessage(
                "com.sun.webui.jsf.resources.LogMessages",
                "EditableList.badValue",
                new Object[]{
                    getClientId(FacesContext.getCurrentInstance())
                });
        throw new FacesException(msg);
    }

    /**
     * Log a message to the standard output.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(EditableList.class.getName() + "::" + msg);
    }

    @Override
    public Object getConvertedValue(final FacesContext context,
            final Object submittedValue) throws ConverterException {

        if (DEBUG) {
            log("getConvertedValue()");
        }

        if (!(submittedValue instanceof String[])) {
            throw new ConverterException(
                    "Submitted value must be a String array");
        }
        String[] rawValues = (String[]) submittedValue;

        // If there are no elements in rawValues nothing was submitted.
        // If null was rendered, return null
        //
        if (rawValues.length == 0) {
            if (ConversionUtilities.renderedNull(this)) {
                return null;
            }
        }
        return submittedValue;
    }

    @Override
    public void processValidators(final FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        // This component may be a developer defined facet.
        // It is explicitly validated during an Add action.
        // It must not be validated during a submit. The assumption
        // is that processValidators is being called during
        // a submit and not in an immediate context.
        // Compare the id of this component with the children
        // and facets and if it matches don't call its
        // processValidators method.
        //
        // Get the last rendered field component instead of
        // a newly initialized one.
        UIComponent field = getRenderedFieldComponent();
        String fieldId = field.getId();

        // Process all the facets and children of this component
        Iterator kids = getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            // We probably should ensure that fieldId is not
            // null, during getFieldComponent() even if
            // it is a developer defined facet.
            if (fieldId != null && fieldId.equals(kid.getId())) {
                continue;
            }
            kid.processValidators(context);
        }

        // Validate the EditableList
        checkValid(context);
    }

    /**
     * Process add action.
     */
    public void processAddAction() {
        if (DEBUG) {
            log("processAddAction()");
        }

        // If we are rendering prematurely don't do anything
        if (FacesContext.getCurrentInstance().getRenderResponse()) {
            return;
        }

        selectedValue = null;

        String[] values = getCurrentValueAsStringArray();

        Object value = getAddedObject();
        if (value == null) {
            return;
        }
        //TODO - fix this when implementing conversion for this component
        selectedValue = value.toString();

        int numValues = values.length;

        String[] newValues = new String[numValues + 1];
        int counter;
        for (counter = 0; counter < numValues; ++counter) {
            newValues[counter] = values[counter];
            if (DEBUG) {
                log("\tAdding " + newValues[counter]);
            }
        }
        newValues[counter] = selectedValue;
        if (DEBUG) {
            log("\tAdding " + newValues[counter]);
        }
        setSubmittedValue(newValues);
    }

    /**
     * Process remove action.
     */
    public void processRemoveAction() {
        if (DEBUG) {
            log("processRemoveAction()");
        }

        // If we are rendering prematurely don't do anything
        if (FacesContext.getCurrentInstance().getRenderResponse()) {
            return;
        }

        // Reset the selected value
        selectedValue = null;

        ArrayList<String> items = new ArrayList<String>();
        int counter;

        if (getValue() != null) {
            if (DEBUG) {
                log("\tList was not empty");
            }
            String[] strings = getCurrentValueAsStringArray();
            int length = strings.length;
            for (counter = 0; counter < length; ++counter) {
                items.add(strings[counter]);
                if (DEBUG) {
                    log("Added " + strings[counter]);
                }
            }
        }

        String[] toRemove = getValuesToRemove();
        for (counter = 0; counter < toRemove.length; ++counter) {
            items.remove(toRemove[counter]);
            if (DEBUG) {
                log("remove " + toRemove[counter]);
            }
        }

        String[] newValues = new String[items.size()];
        for (counter = 0; counter < items.size(); ++counter) {
            newValues[counter] = (String) (items.get(counter));
            if (DEBUG) {
                log("\tAdding back " + newValues[counter]);
            }
        }

        setValuesToRemove(null);
        setSubmittedValue(newValues);
    }

    /**
     * Validate.
     * @param context faces context
     */
    private void checkValid(final FacesContext context) {

        if (DEBUG) {
            log("checkValid()");
        }

        try {
            validate(context);
        } catch (RuntimeException e) {
            if (DEBUG) {
                log("Error during validation");
            }
            context.renderResponse();
            throw e;
        }

        if (!isValid()) {
            if (DEBUG) {
                log("Component is not valid");
            }
            context.renderResponse();
        }
    }

    /**
     * Get added object.
     * @return Object
     */
    private Object getAddedObject() {

        FacesContext context = FacesContext.getCurrentInstance();

        if (DEBUG) {
            log("\tAdd a new item");
        }

        // Need to get the field's value validated first
        // The field can't be immediate because we don't want
        // to validate it if the value is not going to be added.
        // For example in a real submit request.
        //
        // Get the last rendered Field component, not necessarily
        // a newly initialized one.
        EditableValueHolder field = (EditableValueHolder)
                getRenderedFieldComponent();

        // This is ok to do here.
        // We are currently after the APPLY_REQUEST_VALUES phase
        // and before the PROCESS_VALIDATIONS phase.
        // If the field were marked immediate, then the validation
        // would have occured before we get here. If not done
        // here it will be done in the next phase. But it needs
        // to be done here, sort a simulation of immediate
        // henavior. But we don't want the side effect of immediate
        // behavior from external immediate components.
        ((UIComponent) field).processValidators(context);

        if (!field.isValid()) {
            return null;
        }
        // Get the value from the field.
        Object value = field.getValue();

        // This is a policy of the EditableList.
        // An emptyString or null value cannot be added to the list.
        if (value == null
                || (value instanceof String
                && value.toString().length() == 0)) {

            field.setValid(false);
            context.renderResponse();

            if (DEBUG) {
                log("No value from the field");
            }

            String message = ThemeUtilities.getTheme(context).
                    getMessage("EditableList.fieldEmpty");
            context.addMessage(getClientId(context), new FacesMessage(message));
            return null;
        }
        // The new value was added so clear the value.
        // This set is questionable, if the field is a developer
        // defined facet. This will cause an update to the model
        // before the value change event.
        if (DEBUG) {
            log("\tFound new value: " + value);
        }
        field.setValue(null);

        return value;
    }

    @Override
    protected boolean compareValues(final Object previous,
            final Object value) {

        // Let super take care of null cases
        if (previous == null || value == null) {
            return super.compareValues(previous, value);
        }
        if (value instanceof Object[]) {
            // If the lengths aren't equal return true
            //
            int length = Array.getLength(value);
            if (Array.getLength(previous) != length) {
                return true;
            }
            // Each element at index "i" in previous must be equal to the
            // elementa at index "i" in value.
            for (int i = 0; i < length; ++i) {

                Object newValue = Array.get(value, i);
                Object prevValue = Array.get(previous, i);

                // This is probably not necessary since
                // an Option's value cannot be null
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

    @Override
    public String[] getValueAsStringArray(final FacesContext context) {

        if (DEBUG) {
            log("getValueAsStringArray)");
        }

        Iterator iterator = getListItems(context, false);
        int numItems = listItems.size();
        String[] values = new String[numItems];

        int counter = 0;
        while (counter < numItems) {
            values[counter] = ((ListItem) (iterator.next())).getValue();
            ++counter;
        }
        return values;
    }

    @Override
    public boolean mainListSubmits() {
        return true;
    }

    @Override
    public ValueExpression getValueExpression(final String name) {
        if (name.equals("list")) {
            return super.getValueExpression("value");
        }
        return super.getValueExpression(name);
    }

    @Override
    public void setValueExpression(final String name,
            final ValueExpression binding) {

        if (name.equals("list")) {
            super.setValueExpression("value", binding);
            return;
        }
        super.setValueExpression(name, binding);
    }

    @Override
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
     * Flag indicating that the user is not permitted to activate this
     * component, and that the component's value will not be submitted with the
     * form.
     *
     * @param newDisabled disabled
     * @see #isDisabled()
     */
    public void setDisabled(final boolean newDisabled) {
        this.disabled = newDisabled;
        this.disabledSet = true;
    }

    /**
     * Text to be used as the label next to the input text field.
     * @return String
     */
    public String getFieldLabel() {
        if (this.fieldLabel != null) {
            return this.fieldLabel;
        }
        ValueExpression vb = getValueExpression("fieldLabel");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Text to be used as the label next to the input text field.
     *
     * @see #getFieldLabel()
     * @param newFieldLabel fieldLabel
     */
    public void setFieldLabel(final String newFieldLabel) {
        this.fieldLabel = newFieldLabel;
    }

    /**
     * Sets the style level for the generated labels. Valid values are 1
     * (largest), 2 and 3 (smallest). The default value is 2.
     * @return int
     */
    public int getLabelLevel() {
        if (this.labelLevelSet) {
            return this.labelLevel;
        }
        ValueExpression vb = getValueExpression("labelLevel");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return 2;
    }

    /**
     * Sets the style level for the generated labels. Valid values are 1
     * (largest), 2 and 3 (smallest). The default value is 2.
     *
     * @see #getLabelLevel()
     * @param newLabelLevel labelLevel
     */
    public void setLabelLevel(final int newLabelLevel) {
        this.labelLevel = newLabelLevel;
        this.labelLevelSet = true;
    }

    /**
     * Text to be used as the label next to the list box.
     * @return String
     */
    public String getListLabel() {
        if (this.listLabel != null) {
            return this.listLabel;
        }
        ValueExpression vb = getValueExpression("listLabel");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Text to be used as the label next to the list box.
     *
     * @see #getListLabel()
     * @param newListLabel listLabel
     */
    public void setListLabel(final String newListLabel) {
        this.listLabel = newListLabel;
    }

    /**
     * Specifies the display order of the parts of this component. When set to
     * true, the listOnTop attribute causes the list box to be displayed above
     * the text input field. By default, the list box is displayed below the
     * input field.
     * @return {@code boolean}
     */
    public boolean isListOnTop() {
        if (this.listOnTopSet) {
            return this.listOnTop;
        }
        ValueExpression vb = getValueExpression("listOnTop");
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
     * Specifies the display order of the parts of this component. When set to
     * true, the listOnTop attribute causes the list box to be displayed above
     * the text input field. By default, the list box is displayed below the
     * input field.
     *
     * @see #isListOnTop()
     * @param newListOnTop listOnTop
     */
    public void setListOnTop(final boolean newListOnTop) {
        this.listOnTop = newListOnTop;
        this.listOnTopSet = true;
    }

    /**
     * The maximum number of characters allowed for each string in the list.
     *
     * @see #getMaxLength()
     * @param newMaxLength maxLength
     */
    public void setMaxLength(final int newMaxLength) {
        this.maxLength = newMaxLength;
        this.maxLengthSet = true;
    }

    @Override
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
     * Flag indicating that the application user can select more than one option
     * at a time in the list box.
     *
     * @see #isMultiple()
     * @param newMultiple multiple
     */
    public void setMultiple(final boolean newMultiple) {
        this.multiple = newMultiple;
        this.multipleSet = true;
    }

    /**
     * If this attribute is set to true, the value of the component is rendered
     * as text, preceded by the label if one was defined.
     * @return {@code boolean}
     */
    @Override
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
     * If this attribute is set to true, the value of the component is rendered
     * as text, preceded by the label if one was defined.
     *
     * @see #isReadOnly()
     * @param newReadOnly readOnly
     */
    public void setReadOnly(final boolean newReadOnly) {
        this.readOnly = newReadOnly;
        this.readOnlySet = true;
    }

    /**
     * The number of items to display, which determines the length of the
     * rendered list box. The default value is 6.
     * @return int
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int getRows() {
        if (this.rowsSet) {
            return this.rows;
        }
        ValueExpression vb = getValueExpression("rows");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return 6;
    }

    /**
     * The number of items to display, which determines the length of the
     * rendered list box. The default value is 6.
     *
     * @see #getRows()
     * @param newRows rows
     */
    public void setRows(final int newRows) {
        this.rows = newRows;
        this.rowsSet = true;
    }

    /**
     * Set sorted to true if the list items should be sorted in locale-specific
     * alphabetical order. The sorting is performed using a Collator configured
     * with the locale from the FacesContext.
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
     * Set this attribute to true if the list items should be sorted in
     * locale-specific alphabetical order. The sorting is performed using a
     * Collator configured with the locale from the FacesContext.
     *
     * @see #isSorted()
     * @param newSorted sorted
     */
    public void setSorted(final boolean newSorted) {
        this.sorted = newSorted;
        this.sortedSet = true;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     * @return String
     */
    @Override
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
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     * @return String
     */
    @Override
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
     * @param newStyleClass styleClass
     */
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

    /**
     * <p>e
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     * @return int
     */
    @Override
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

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     * @return String
     */
    @Override
    public String getToolTip() {
        if (this.toolTip != null) {
            return this.toolTip;
        }
        ValueExpression vb = getValueExpression("toolTip");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tooltip if the mouse cursor hovers over the HTML
     * element.
     *
     * @see #getToolTip()
     * @param newToolTip toolTip
     */
    public void setToolTip(final String newToolTip) {
        this.toolTip = newToolTip;
    }

    @Override
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
        return true;
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
     * A validator that will be applied to entries made into the textfield.
     * Specify this to be the {@code validate()} method of a
     * {@code jakarta.faces.validator.Validator}, or to another method with
     * the same argument structure and exceptions.
     * @return MethodExpression
     */
    public MethodExpression getFieldValidatorExpression() {
        return this.fieldValidatorExpression;
    }

    /**
     * A validator that will be applied to entries made into the textfield.
     * Specify this to be the {@code validate()} method of a
     * {@code jakarta.faces.validator.Validator}, or to another method with
     * the same argument structure and exceptions.
     *
     * @see #getFieldValidatorExpression()
     * @param newFieldValidatorExpression fieldValidatorExpression
     */
    public void setFieldValidatorExpression(
            final MethodExpression newFieldValidatorExpression) {
        this.fieldValidatorExpression = newFieldValidatorExpression;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    @Override
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.disabled = ((Boolean) values[1]);
        this.disabledSet = ((Boolean) values[2]);
        this.fieldLabel = (String) values[3];
        this.labelLevel = ((Integer) values[4]);
        this.labelLevelSet = ((Boolean) values[5]);
        this.listLabel = (String) values[6];
        this.listOnTop = ((Boolean) values[7]);
        this.listOnTopSet = ((Boolean) values[8]);
        this.maxLength = ((Integer) values[9]);
        this.maxLengthSet = ((Boolean) values[10]);
        this.multiple = ((Boolean) values[11]);
        this.multipleSet = ((Boolean) values[12]);
        this.readOnly = ((Boolean) values[13]);
        this.readOnlySet = ((Boolean) values[14]);
        this.rows = ((Integer) values[15]);
        this.rowsSet = ((Boolean) values[16]);
        this.sorted = ((Boolean) values[17]);
        this.sortedSet = ((Boolean) values[18]);
        this.style = (String) values[19];
        this.styleClass = (String) values[20];
        this.tabIndex = ((Integer) values[21]);
        this.tabIndexSet = ((Boolean) values[22]);
        this.toolTip = (String) values[23];
        this.visible = ((Boolean) values[24]);
        this.visibleSet = ((Boolean) values[25]);
        this.fieldValidatorExpression = (MethodExpression)
                restoreAttachedState(context, values[26]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[27];
        values[0] = super.saveState(context);
        if (this.disabled) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.disabledSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        values[3] = this.fieldLabel;
        values[4] = this.labelLevel;
        if (this.labelLevelSet) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        values[6] = this.listLabel;
        if (this.listOnTop) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        if (this.listOnTopSet) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        values[9] = this.maxLength;
        if (this.maxLengthSet) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        if (this.multiple) {
            values[11] = Boolean.TRUE;
        } else {
            values[11] = Boolean.FALSE;
        }
        if (this.multipleSet) {
            values[12] = Boolean.TRUE;
        } else {
            values[12] = Boolean.FALSE;
        }
        if (this.readOnly) {
            values[13] = Boolean.TRUE;
        } else {
            values[13] = Boolean.FALSE;
        }
        if (this.readOnlySet) {
            values[14] = Boolean.TRUE;
        } else {
            values[14] = Boolean.FALSE;
        }
        values[15] = this.rows;
        if (this.rowsSet) {
            values[16] = Boolean.TRUE;
        } else {
            values[16] = Boolean.FALSE;
        }
        if (this.sorted) {
            values[17] = Boolean.TRUE;
        } else {
            values[17] = Boolean.FALSE;
        }
        if (this.sortedSet) {
            values[18] = Boolean.TRUE;
        } else {
            values[18] = Boolean.FALSE;
        }
        values[19] = this.style;
        values[20] = this.styleClass;
        values[21] = this.tabIndex;
        if (this.tabIndexSet) {
            values[22] = Boolean.TRUE;
        } else {
            values[22] = Boolean.FALSE;
        }
        values[23] = this.toolTip;
        if (this.visible) {
            values[24] = Boolean.TRUE;
        } else {
            values[24] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[25] = Boolean.TRUE;
        } else {
            values[25] = Boolean.FALSE;
        }
        values[26] = saveAttachedState(context, fieldValidatorExpression);
        return values;
    }

    /**
     * Add listener.
     */
    private static final class AddListener
            implements ActionListener, Serializable {

        /**
         * Serialization UID.
         */
        private static final long serialVersionUID = -5204715231205041623L;

        @Override
        public void processAction(final ActionEvent event) {
            UIComponent comp = event.getComponent();
            comp = comp.getParent();
            if (comp instanceof EditableList) {
                ((EditableList) comp).processAddAction();
            }
        }
    }

    /**
     * Remove listener.
     */
    private static final class RemoveListener
            implements ActionListener, Serializable {

        /**
         * Serialization UID.
         */
        private static final long serialVersionUID = -7559203083988359751L;

        @Override
        public void processAction(final ActionEvent event) {
            UIComponent comp = event.getComponent();
            comp = comp.getParent();
            if (comp instanceof EditableList) {
                ((EditableList) comp).processRemoveAction();
            }
        }
    }
}
