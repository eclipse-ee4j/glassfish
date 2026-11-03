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
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.jsf.util.ValueType;
import com.sun.webui.jsf.util.ValueTypeEvaluator;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.ConverterException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * The OrderableList component creates a list with buttons allowing the user to
 * change the order of the list items.
 *
 * <p>
 * This tag renders an OrderableList component. Use this component when web
 * application users need to create and modify a list of strings. The
 * application user can add new strings by typing them into the textfield and
 * clicking the "moveUp" button, and remove them by selecting one or more items
 * from the list and clicking the "Remove" button.</p>
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
 * <li>{@code moveUpButton}: use this facet to specify a custom component
 * for the moveUp button.</li>
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
@Component(type = "com.sun.webui.jsf.OrderableList",
        family = "com.sun.webui.jsf.OrderableList",
        displayName = "Orderable List", tagName = "orderableList",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_orderable_list",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_orderable_list_props")
        //CHECKSTYLE:ON
public final class OrderableList extends WebuiInput
        implements ListManager, NamingContainer {

    /**
     * The component id for the moveUp button.
     */
    public static final String MOVEUP_BUTTON_ID = "_moveUpButton";

    /**
     * The facet name for the moveUp button.
     */
    public static final String MOVEUP_BUTTON_FACET = "moveUpButton";

    /**
     * The move up button text key.
     */
    public static final String MOVEUP_TEXT_KEY = "OrderableList.moveUp";

    /**
     * The component id for the moveDown button.
     */
    public static final String MOVEDOWN_BUTTON_ID = "_moveDownButton";

    /**
     * The facet name for the moveDown button.
     */
    public static final String MOVEDOWN_BUTTON_FACET = "moveDownButton";

    /**
     * The move down button text key.
     */
    public static final String MOVEDOWN_TEXT_KEY = "OrderableList.moveDown";

    /**
     * The component id for the moveTop button.
     */
    public static final String MOVETOP_BUTTON_ID = "_moveTopButton";

    /**
     * The facet name for the moveTop button.
     */
    public static final String MOVETOP_BUTTON_FACET = "moveTopButton";

    /**
     * The move top button text key.
     */
    public static final String MOVETOP_TEXT_KEY = "OrderableList.moveTop";

    /**
     * The component id for the moveBottom button.
     */
    public static final String MOVEBOTTOM_BUTTON_ID = "_moveBottomButton";

    /**
     * The facet name for the moveBottom button.
     */
    public static final String MOVEBOTTOM_BUTTON_FACET = "moveBottomButton";

    /**
     * The move bottom button text key.
     */
    public static final String MOVEBOTTOM_TEXT_KEY = "OrderableList.moveBottom";

    /**
     * The component ID for the label.
     */
    public static final String LABEL_ID = "_label";

    /**
     * The facet name for the label.
     */
    public static final String LABEL_FACET = "label";

    /**
     * The default label text message key.
     */
    public static final String LABEL_TEXT_KEY
            = "OrderableList.defaultListLabel";

    /**
     * The component ID for the read only text field.
     */
    public static final String READ_ONLY_ID = "_readOnly";

    /**
     * The facet name for the readOnly text field.
     */
    public static final String READ_ONLY_FACET = "readOnly";

    /**
     * The name for the footer facet.
     */
    public static final String FOOTER_FACET = "footer";

    /**
     * String representing "return false" printed at the end of the JS
     * event handlers.
     */
    public static final String RETURN = "return false;";

    /**
     * Name of the JavaScript function which moves elements up.
     */
    public static final String MOVEUP_FUNCTION = ".moveUp(); ";

    /**
     * Name of the JavaScript function which moves elements down.
     */
    public static final String MOVEDOWN_FUNCTION = ".moveDown();";

    /**
     * Name of the JavaScript function which moves elements to the top.
     */
    public static final String MOVETOP_FUNCTION = ".moveTop(); ";

    /**
     * Name of the JavaScript function which moves elements to the bottom.
     */
    public static final String MOVEBOTTOM_FUNCTION = ".moveBottom();";

    /**
     * Name of the JavaScript function that updates the buttons.
     */
    public static final String UPDATEBUTTONS_FUNCTION = ".updateButtons(); ";

    /**
     * Name of the JavaScript function that handles changes on the available
     * list.
     */
    public static final String ONCHANGE_FUNCTION = ".onChange(); ";

    // FIXME: Should be in Theme
    /**
     * Read only separator string.
     */
    private static final String READ_ONLY_SEPARATOR = ", ";

    /**
     * Holds the ValueType of this component.
     */
    private ValueTypeEvaluator valueTypeEvaluator = null;

    /**
     * list items.
     */
    private ArrayList<Object> listItems = null;

    /**
     * Theme.
     */
    private transient Theme theme = null;

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Flag indicating that activation of this component by the user is not
     * currently permitted.
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
     * If set, a label is rendered adjacent to the component with the value of
     * this attribute as the label text.
     */
    @Property(name = "label",
            displayName = "List Label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String label = null;

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
     * If this attribute is true, the label is rendered above the component. If
     * it is false, the label is rendered next to the component. The default is
     * false.
     */
    @Property(name = "labelOnTop",
            displayName = "Label on Top",
            category = "Appearance")
    private boolean labelOnTop = false;

    /**
     * labelOnTop set flag.
     */
    private boolean labelOnTopSet = false;

    /**
     * If this attribute is true, the Move to Top and Move to Bottom buttons are
     * shown. The default is false.
     */
    @Property(name = "moveTopBottom",
            displayName = "Move Top and Bottom",
            category = "Appearance")
    private boolean moveTopBottom = false;

    /**
     * moveTopBottom set flag.
     */
    private boolean moveTopBottomSet = false;

    /**
     * Flag indicating that the application user can make select more than one
     * option from the listbox.
     */
    @Property(name = "multiple", displayName = "Multiple", category = "Data")
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
     * The number of rows to display, which determines the length of the
     * rendered listbox. The default value is 6.
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
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
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
     * Position of this element in the tabbing order for the current document.
     * The tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The tabIndex value must be an integer
     * between 0 and 32767.
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
     * Default constructor.
     * Construct a new {@code OrderableListBase}.
     */
    public OrderableList() {
        super();
        valueTypeEvaluator = new ValueTypeEvaluator(this);
        setRendererType("com.sun.webui.jsf.OrderableList");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.OrderableList";
    }

    /**
     * Return a component that implements the move up button.If a facet named
     * {@code moveUpButton} is found that component is returned. Otherwise a
     * {@code Button} component is returned. It is assigned the id
     * {@code getId() + "_moveUpButton"}
     * <p>
     * If the facet is not defined then the returned {@code Button} component is
     * re-initialized every time this method is called.
     * </p>
     *
     * @param context faces context
     * @return a move up button component
     */
    public UIComponent getMoveUpButtonComponent(final FacesContext context) {
        if (DEBUG) {
            log("getMoveUpButtonComponent()");
        }
        return getButtonFacet(MOVEUP_BUTTON_FACET, false,
                getTheme().getMessage(MOVEUP_TEXT_KEY), MOVEUP_FUNCTION);
    }

    /**
     * Return a component that implements the move down button. If a facet named
     * {@code moveDownButton} is found that component is returned.
     * Otherwise a {@code Button} component is returned. It is assigned the
     * id
     * {@code getId() + "_moveDownButton"}
     * <p>
     * If the facet is not defined then the returned {@code Button}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @param context faces context
     * @return a move down button component
     */
    public UIComponent getMoveDownButtonComponent(final FacesContext context) {
        if (DEBUG) {
            log("getMoveDownButtonComponent()");
        }
        return getButtonFacet(MOVEDOWN_BUTTON_FACET, false,
                getTheme().getMessage(MOVEDOWN_TEXT_KEY), MOVEDOWN_FUNCTION);
    }

    /**
     * Return a component that implements the move to top button. If a facet
     * named {@code moveToTop} is found that component is returned.
     * Otherwise a {@code Button} component is returned. It is assigned the
     * id
     * {@code getId() + "_moveToTop"}
     * <p>
     * If the facet is not defined then the returned {@code Button}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @param context faces context
     * @return a move to top button component
     */
    public UIComponent getMoveTopButtonComponent(final FacesContext context) {
        if (DEBUG) {
            log("getMoveTopButtonComponent()");
        }
        return getButtonFacet(MOVETOP_BUTTON_FACET, false,
                getTheme().getMessage(MOVETOP_TEXT_KEY), MOVETOP_FUNCTION);
    }

    /**
     * Return a component that implements the move to bottom button. If a facet
     * named {@code moveToBottom} is found that component is returned.
     * Otherwise a {@code Button} component is returned. It is assigned the
     * id
     * {@code getId() + "_moveToBottom"}
     * <p>
     * If the facet is not defined then the returned {@code Button}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @param context faces context
     * @return a move to bottom button component
     */
    public UIComponent getMoveBottomButtonComponent(
            final FacesContext context) {
        if (DEBUG) {
            log("getMoveBottomButtonComponent()");
        }
        return getButtonFacet(MOVEBOTTOM_BUTTON_FACET, false,
                getTheme().getMessage(MOVEBOTTOM_TEXT_KEY),
                MOVEBOTTOM_FUNCTION);
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
     * @param onclickFunction the JS function name for the onclick event
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
        }

        initButtonFacet(button, primary, text, onclickFunction);
        ComponentUtilities.putPrivateFacet(this, facetName, button);
        return button;
    }

    /**
     * Initialize a {@code Button} facet.
     *
     * @param button the Button instance
     * @param primary if false the button is not a primary button
     * @param text the button text
     * @param onclickFunction the JS function name for the onclick event
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
        buff.append("document.getElementById('")
                .append(getClientId(getFacesContext()))
                .append("')")
                .append(onclickFunction)
                .append(RETURN);
        button.setOnClick(buff.toString());

        // NOTE: the original behavior would have set this
        // on the developer defined facet. It was determined that
        // a developer defined facet should not be modified.
        button.setDisabled(isDisabled());
    }

    // Labels
    /**
     * Return a component that implements the header label. If a facet named
     * {@code label} is found that component is returned. Otherwise a
     * {@code Label} component is returned. It is assigned the id
     * {@code getId() + "_label"}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return a header label component
     */
    public UIComponent getHeaderComponent() {
        if (DEBUG) {
            log("getHeaderComponent()");
        }
        String labelString = getLabel();
        if (labelString == null || labelString.length() == 0) {
            labelString = getTheme().getMessage(LABEL_TEXT_KEY);
        }
        return getLabelFacet(LABEL_FACET, labelString, this);
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
        Label listLabel = (Label) ComponentUtilities
                .getPrivateFacet(this, facetName, true);

        if (listLabel == null) {
            if (DEBUG) {
                log("create Label");
            }
            listLabel = new Label();
            listLabel.setId(ComponentUtilities.createPrivateFacetId(this,
                    facetName));
        }
        initLabelFacet(listLabel, text, forComponent
                .getClientId(getFacesContext()));
        ComponentUtilities.putPrivateFacet(this, facetName, listLabel);
        return listLabel;
    }

    /**
     * Initialize a label facet.
     *
     * @param labelComp the Label instance
     * @param labelText the label text.
     * @param forComponentId the component instance this label is for
     */
    private void initLabelFacet(final Label labelComp, final String labelText,
            final String forComponentId) {

        if (DEBUG) {
            log("initLabelFacet()");
        }
        if (labelText == null || labelText.length() < 1) {
            // FIXME - maybe print a default?
            // A Theme default value.
            labelComp.setText("");
        } else {
            labelComp.setText(labelText);
        }

        labelComp.setLabelLevel(getLabelLevel());
        labelComp.setFor(forComponentId);
    }

    @Override
    public String getPrimaryElementID(final FacesContext context) {
        return getLabeledElementId(context);
    }

    @Override
    public String getLabeledElementId(final FacesContext context) {
        return getClientId(context).concat(ListSelector.LIST_ID);
    }

    @Override
    public String getFocusElementId(final FacesContext context) {
        // This should be the id of the selected Option in the
        // select list.
        // For now return the same as for the label.
        return getLabeledElementId(context);
    }

    /**
     * Retrieve an Iterator of ListSelector.ListItem, to be used by the
     * renderer.
     *
     * @param context faces context
     * @param ruler ruler flag
     * @return an Iterator over {@link ListItem}.
     * @throws jakarta.faces.FacesException
     */
    @Override
    public Iterator getListItems(final FacesContext context,
            final boolean ruler) throws FacesException {

        if (DEBUG) {
            log("getListItems()");
        }

        listItems = new ArrayList<Object>();

        Object submittedValue = getSubmittedValue();
        if (submittedValue != null && submittedValue instanceof String[]) {
            ListItem listItem;
            String[] values = (String[]) submittedValue;
            for (int counter = 0; counter < values.length; ++counter) {
                if (DEBUG) {
                    log("Adding listItem " + values[counter]);
                }
                listItem = new ListItem(values[counter], values[counter]);
                listItem.setValue(values[counter]);
                listItems.add(listItem);
            }
            return listItems.iterator();
        }

        Object listItemsObject = getList();
        if (listItemsObject == null) {
            if (DEBUG) {
                log("\tNo list items!");
            }
            // do nothing...
        } else if (valueTypeEvaluator.getValueType() == ValueType.LIST) {
            Iterator items = ((java.util.List) listItemsObject).iterator();
            Object item;
            ListItem listItem;
            while (items.hasNext()) {
                item = items.next();
                listItems.add(createListItem(this, item));
            }
        } else if (valueTypeEvaluator.getValueType() == ValueType.ARRAY) {

            if (DEBUG) {
                log("\tFound array value");
            }

            // The strings variable represents the strings entered by
            // the user, as an array.
            Object[] listObjects = (Object[]) listItemsObject;

            for (int counter = 0; counter < listObjects.length; ++counter) {
                listItems.add(createListItem(this, listObjects[counter]));
            }
        } else {
            String msg = getTheme().getMessage("OrderableList.invalidListType");
            throw new FacesException(msg);
        }

        return listItems.iterator();
    }

    /**
     * Enforce non null values. This is OK, since Converter returns null on null
     * input. And secondly this is equivalent to SelectItem and therefore Option
     * which do not allow null values.
     *
     * However we have to be wary of values that are "". But if the null case is
     * out of the way the this should work OK.
     * @param comp UI component
     * @param value initial value
     * @return ListItem
     */
    private ListItem createListItem(final UIComponent comp,
            final Object value) {

        // Do not allow null values
        if (value == null) {
            throw new NullPointerException(
                    "OrderableList ListItems cannot have null values");
        }
        if (DEBUG) {
            log("createListItem()");
        }
        String listLabel = ConversionUtilities.convertValueToString(comp,
                value);
        if (DEBUG) {
            log("\tLabel is " + listLabel);
        }
        ListItem listItem = new ListItem(value, listLabel);
        if (DEBUG) {
            log("\tCreated ListItem");
        }
        listItem.setValue(listLabel);
        return listItem;
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
            if (DEBUG) {
                log("List item value " + String.valueOf(values[counter]));
            }
            ++counter;
        }
        return values;
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
        if (rawValues.length == 0) {
            if (ConversionUtilities.renderedNull(this)) {
                return null;
            }
        }

        Object cValue = null;
        try {
            if (valueTypeEvaluator.getValueType() == ValueType.ARRAY) {
                if (DEBUG) {
                    log("\tComponent value is an array");
                }
                cValue = ConversionUtilities.convertValueToArray(this,
                        rawValues, context);
            } else if (valueTypeEvaluator.getValueType() == ValueType.LIST) {
                // This case is not supported yet!
                if (DEBUG) {
                    log("\tComponent value is a list");
                }
                /* Until this is fixed throw exception saying it is
                unsupported
                cValue = ConversionUtilities.convertValueToList
                (this, rawValues, context);
                 */
                throw new FacesException("List is not a supported value.");
            }
        } catch (FacesException ex) {
            if (DEBUG) {
                LogUtil.finest(null, ex);
            }
        }
        return cValue;
    }

    /**
     * Return a string suitable for displaying the value in read only mode.The
     * default is to separate the list values with a comma.
     *
     * @param context The FacesContext
     * @return String
     * @throws jakarta.faces.FacesException If the list items cannot be processed
     */
    protected String getValueAsReadOnly(final FacesContext context)
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

    /**
     * Return a component that implements the read only value of this
     * OrderableList. If a facet named {@code readOnly} is found that
     * component is returned. Otherwise a {@code StaticText} component is
     * returned. It is assigned the id
     * {@code getId() + "_readOnly"}
     * <p>
     * If the facet is not defined then the returned {@code StaticText}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return a component that represents the read only value of this
     * OrderableList
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

    /**
     * Log a message to the standard out.
     * @param msg message to log
     */
    private void log(final String msg) {
        LogUtil.finest(OrderableList.class.getName() + "::" + msg);
    }

    /**
     * Get the theme.
     * @return Theme
     */
    private Theme getTheme() {
        return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int getRows() {
        int listRows = doGetRows();
        if (listRows < 1) {
            listRows = 12;
            setRows(12);
        }
        return listRows;
    }

    @Override
    public String getOnChange() {
        return null;
    }

    /**
     * Return {@code true} if the new value is different from the previous
     * value.
     *
     * This only implements a compareValues for value if it is an Array. If
     * value is not an Array, defer to super.compareValues. The assumption is
     * that the ordering of the elements between the previous value and the new
     * value is determined in the same manner.
     *
     * Another assumption is that the two object arguments are of the same type,
     * both arrays of both not arrays.
     *
     * @param previous old value of this component (if any)
     * @param value new value of this component (if any)
     * @return {@code boolean}
     */
    @Override
    protected boolean compareValues(final Object previous, final Object value) {

        // Let super take care of null cases
        //
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
    public boolean mainListSubmits() {
        return false;
    }

    @Override
    public ValueExpression getValueExpression(final String name) {
        if (name.equals("list")) {
            return super.getValueExpression("value");
        }
        return super.getValueExpression(name);
    }

    /**
     * Set the {@code ValueExpression} stored for the specified name (if
     * any), respecting any property aliases.
     *
     * @param name Name of value binding to set
     * @param binding ValueExpression to set, or null to remove
     */
    @Override
    public void setValueExpression(final String name,
            final ValueExpression binding) {

        if (name.equals("list")) {
            super.setValueExpression("value", binding);
            return;
        }
        super.setValueExpression(name, binding);
    }

    // Hide value
    @Property(name = "value", isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * Flag indicating that activation of this component by the user is not
     * currently permitted.
     */
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
     * Flag indicating that activation of this component by the user is not
     * currently permitted.
     *
     * @see #isDisabled()
     * @param newDisabled disabled
     */
    public void setDisabled(final boolean newDisabled) {
        this.disabled = newDisabled;
        this.disabledSet = true;
    }

    /**
     * If set, a label is rendered adjacent to the component with the value of
     * this attribute as the label text.
     * @return String
     */
    public String getLabel() {
        if (this.label != null) {
            return this.label;
        }
        ValueExpression vb = getValueExpression("label");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * If set, a label is rendered adjacent to the component with the value of
     * this attribute as the label text.
     *
     * @see #getLabel()
     * @param newLabel label
     */
    public void setLabel(final String newLabel) {
        this.label = newLabel;
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
     * If this attribute is true, the label is rendered above the component. If
     * it is false, the label is rendered next to the component. The default is
     * false.
     * @return {@code boolean}
     */
    public boolean isLabelOnTop() {
        if (this.labelOnTopSet) {
            return this.labelOnTop;
        }
        ValueExpression vb = getValueExpression("labelOnTop");
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
     * If this attribute is true, the label is rendered above the component. If
     * it is false, the label is rendered next to the component. The default is
     * false.
     *
     * @see #isLabelOnTop()
     * @param newLabelOnTop labelOnTop
     */
    public void setLabelOnTop(final boolean newLabelOnTop) {
        this.labelOnTop = newLabelOnTop;
        this.labelOnTopSet = true;
    }

    /**
     * The object that represents the list of items. The attribute's value must
     * be a JavaServer Faces EL expression that evaluates to an array of Objects
     * or to a {@code java.util.List}.
     * @return Object
     */
    @Property(name = "list", displayName = "List", category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor")
            //CHECKSTYLE:ON
    public Object getList() {
        return getValue();
    }

    /**
     * The object that represents the list of items. The attribute's value must
     * be a JavaServer Faces EL expression that evaluates to an array of Objects
     * or to a {@code java.util.List}.
     *
     * @see #getList()
     * @param newList list
     */
    public void setList(final Object newList) {
        setValue(newList);
    }

    /**
     * If this attribute is true, the Move to Top and Move to Bottom buttons are
     * shown. The default is false.
     * @return {@code boolean}
     */
    public boolean isMoveTopBottom() {
        if (this.moveTopBottomSet) {
            return this.moveTopBottom;
        }
        ValueExpression vb = getValueExpression("moveTopBottom");
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
     * If this attribute is true, the Move to Top and Move to Bottom buttons are
     * shown. The default is false.
     *
     * @see #isMoveTopBottom()
     * @param newMoveTopBottom moveTopBottom
     */
    public void setMoveTopBottom(final boolean newMoveTopBottom) {
        this.moveTopBottom = newMoveTopBottom;
        this.moveTopBottomSet = true;
    }

    /**
     * Flag indicating that the application user can make select more than one
     * option from the listbox.
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
     * Flag indicating that the application user can make select more than one
     * option from the listbox.
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
     * The number of rows to display, which determines the length of the
     * rendered listbox. The default value is 6.
     * @return int
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private int doGetRows() {
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
        return 12;
    }

    /**
     * The number of rows to display, which determines the length of the
     * rendered listbox. The default value is 6.
     *
     * @see #getRows()
     * @param newRows newRows
     */
    public void setRows(final int newRows) {
        this.rows = newRows;
        this.rowsSet = true;
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
     * Position of this element in the tabbing order for the current document.
     * The tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The tabIndex value must be an integer
     * between 0 and 32767.
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
     * text will display as a tool tip if the mouse cursor hovers over the HTML
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

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.disabled = ((Boolean) values[1]);
        this.disabledSet = ((Boolean) values[2]);
        this.label = (String) values[3];
        this.labelLevel = ((Integer) values[4]);
        this.labelLevelSet = ((Boolean) values[5]);
        this.labelOnTop = ((Boolean) values[6]);
        this.labelOnTopSet = ((Boolean) values[7]);
        this.moveTopBottom = ((Boolean) values[8]);
        this.moveTopBottomSet = ((Boolean) values[9]);
        this.multiple = ((Boolean) values[10]);
        this.multipleSet = ((Boolean) values[11]);
        this.readOnly = ((Boolean) values[12]);
        this.readOnlySet = ((Boolean) values[13]);
        this.rows = ((Integer) values[14]);
        this.rowsSet = ((Boolean) values[15]);
        this.style = (String) values[16];
        this.styleClass = (String) values[17];
        this.tabIndex = ((Integer) values[18]);
        this.tabIndexSet = ((Boolean) values[19]);
        this.toolTip = (String) values[20];
        this.visible = ((Boolean) values[21]);
        this.visibleSet = ((Boolean) values[22]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[23];
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
        values[3] = this.label;
        values[4] = this.labelLevel;
        if (this.labelLevelSet) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        if (this.labelOnTop) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        if (this.labelOnTopSet) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        if (this.moveTopBottom) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        if (this.moveTopBottomSet) {
            values[9] = Boolean.TRUE;
        } else {
            values[9] = Boolean.FALSE;
        }
        if (this.multiple) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        if (this.multipleSet) {
            values[11] = Boolean.TRUE;
        } else {
            values[11] = Boolean.FALSE;
        }
        if (this.readOnly) {
            values[12] = Boolean.TRUE;
        } else {
            values[12] = Boolean.FALSE;
        }
        if (this.readOnlySet) {
            values[13] = Boolean.TRUE;
        } else {
            values[13] = Boolean.FALSE;
        }
        values[14] = this.rows;
        if (this.rowsSet) {
            values[15] = Boolean.TRUE;
        } else {
            values[15] = Boolean.FALSE;
        }
        values[16] = this.style;
        values[17] = this.styleClass;
        values[18] = this.tabIndex;
        if (this.rowsSet) {
            values[19] = Boolean.TRUE;
        } else {
            values[19] = Boolean.FALSE;
        }
        values[20] = this.toolTip;
        if (this.visible) {
            values[21] = Boolean.TRUE;
        } else {
            values[21] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[22] = Boolean.TRUE;
        } else {
            values[22] = Boolean.FALSE;
        }
        return values;
    }
}
