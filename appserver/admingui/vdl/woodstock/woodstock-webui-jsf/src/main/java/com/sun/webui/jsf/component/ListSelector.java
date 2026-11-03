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

import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.model.Option;
import com.sun.webui.jsf.model.OptionGroup;
import com.sun.webui.jsf.model.OptionTitle;
import com.sun.webui.jsf.model.Separator;
import com.sun.webui.jsf.model.list.EndGroup;
import com.sun.webui.jsf.model.list.ListItem;
import com.sun.webui.jsf.model.list.StartGroup;
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.MessageUtil;
import com.sun.webui.jsf.util.ValueType;
import com.sun.webui.jsf.util.ValueTypeEvaluator;

import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.beans.Beans;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Base component for UI components that allow the user to make a selection from
 * a list of options using an HTML select element.
 */
public class ListSelector extends Selector implements ListManager,
        NamingContainer {

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Holds the options for this component.
     */
    private ArrayList<Object> listItems = null;

    /**
     * ReadOnly facet.
     */
    private static final String READONLY_FACET = "readOnly";

    /**
     * Label facet.
     */
    private static final String LABEL_FACET = "label";

    /**
     * Value id.
     */
    public static final String VALUE_ID = "_list_value";

    /**
     * Value label id.
     */
    public static final String VALUE_LABEL_ID = "_hiddenlabel";

    /**
     * List id.
     */
    public static final String LIST_ID = "_list";

    /**
     * separator length.
     */
    private int separatorLength = 0;

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
     * The number of items to display. The default value is 12.
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
     * Flag indicating that items corresponding to
     * {@code com.sun.webui.jsf.model.Option} that are defined inside a
     * {@code com.sun.webui.jsf.model.OptionGroup} should be surrounded by
     * separators inside the list. The default value is true. If false, no
     * separators are shown. To manually specify the location of separators, set
     * this flag to false and place instances of
     * {@code com.sun.webui.jsf.model.Separator} between the relevant
     * {@code com.sun.webui.jsf.model.Option} instances when specifying the
     * {@code items} attribute.
     */
    @Property(name = "separators",
            displayName = "Separators",
            category = "Appearance")
    private boolean separators = false;

    /**
     * separators set.
     */
    private boolean separatorsSet = false;

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
     * Creates a new instance of ListSelector.
     */
    public ListSelector() {
        setRendererType("com.sun.webui.jsf.ListSelectorRenderer");
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.ListSelector"}.
     * @return String
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.ListSelector";
    }

    /**
     * Check that this component has a value binding that matches the value of
     * the "multiple" attribute.
     *
     * @param context The FacesContext of the request
     */
    public void checkSelectionModel(final FacesContext context) {

        if (DEBUG) {
            log("checkSelectionModel()");
            log("\tComponent multiple = " + String.valueOf(isMultiple()));
            log("\tValueType " + getValueTypeEvaluator()
                    .getValueType().toString());
        }

        if (isMultiple()
                && getValueTypeEvaluator().getValueType() != ValueType.ARRAY) {

            if (DEBUG) {
                log("\tMultiple selection enabled for non-array value");
            }
            Object[] params = new Object[]{
                this.toString()
            };
            String msg = MessageUtil
                    .getMessage("com.sun.webui.jsf.resources.LogMessages",
                    "Selector.multipleError",
                    params);
            throw new RuntimeException(msg);
        }
    }

    /**
     * Retrieve an Iterator of ListSelector.ListItem, to be used by the
     * renderer.
     * This implementation retrieves the current selections.
     * @param context faces context
     * @param rulerAtEnd ruler flag
     * @return an Iterator over {@link ListItem}.
     */
    @Override
    public Iterator getListItems(final FacesContext context,
            final boolean rulerAtEnd) throws FacesException {

        if (DEBUG) {
            log("getListItems()");
        }
        listItems = new ArrayList<Object>();
        separatorLength = 0;

        // Retrieve the current selections. If there are selected
        // objects, mark the corresponding items as selected.
        processOptions(getOptions());
        processSelections();
        return listItems.iterator();
    }

    /**
     * Retrieve an Iterator of ListSelector.ListItem, to be used when evaluating
     * the list items. If the list items are needed by the renderer, use
     * getListItems(context, rulerAtEnd) instead.
     *
     * @return an Iterator over {@link ListItem}.
     */
    public Iterator getListItems() {
        if (DEBUG) {
            log("getListItems()");
        }
        if (listItems != null) {
            return listItems.iterator();
        }
        listItems = new ArrayList<Object>();
        processOptions(getOptions());
        return listItems.iterator();
    }

    /**
     * This method resets the options. Use this only if you need to add or
     * remove options after the component has been rendered once. public void
     * resetOptions() { listItems = null; }
     * @return int
     */
    public int getSeparatorLength() {
        return separatorLength;
    }

    /**
     * Processes the component's SelectItems. Constructs an ArrayList of
     * Selector.Options.
     *
     * <ul>
     * <li>General algorithm copied from the RI, except that I modified the
     * class casts for readability. I don't think the algorithm is correct
     * though, need to verify. </li>
     * <li>The list of allowed data types must match the spec. </li>
     * <li>This code will have to be replaced when switching to Selection.</li>
     * </ul>
     * @return Option[]
     */
    protected Option[] getOptions() {

        Option[] options;
        Object optionsObject = getItems();

        // TODO - add some error reporting...
        if (optionsObject instanceof Option[]) {
            options = (Option[]) optionsObject;
        } else if (optionsObject instanceof Collection) {
            Object[] objects = ((Collection) optionsObject).toArray();
            int numObjects = objects.length;
            options = new Option[numObjects];
            for (int counter = 0; counter < numObjects; ++counter) {
                options[counter] = (Option) objects[counter];
            }
        } else if (optionsObject instanceof Map) {
            Collection itemsCollection = ((Map) optionsObject).values();
            options = (Option[]) (itemsCollection.toArray());
        } else {
            // The items attribute has not been specified
            // do nothing
            options = new Option[0];
        }
        return options;
    }

    /**
     * Process the specified options.
     * @param options options to process
     */
    @SuppressWarnings("checkstyle:magicnumber")
    protected void processOptions(final Option[] options) {

        if (DEBUG) {
            log("processOptions()");
        }
        int length = options.length;

        for (int counter = 0; counter < length; ++counter) {
            if (options[counter] instanceof OptionGroup) {
                OptionGroup selectionGroup
                        = (OptionGroup) options[counter];
                String groupLabel = selectionGroup.getLabel();

                if (DEBUG) {
                    log("\tFound SelectionGroup");
                    log("\tLabel is " + groupLabel);
                }

                if ((groupLabel.length() * 1.5) > separatorLength) {
                    // FIXME - needs to be dependent on the
                    // browser if not the OS... ARRGGH.
                    separatorLength = (int) (groupLabel.length() * 1.5);
                }

                listItems.add(new StartGroup(groupLabel));
                processOptions(selectionGroup.getOptions());
                listItems.add(new EndGroup());
            } else if (options[counter] instanceof Separator) {
                listItems.add(options[counter]);
            } else {
                listItems.add(createListItem(options[counter]));
            }
        }
    }

    /**
     * Retrieve the current selections and compare them with the list items.
     */
    protected void processSelections() {
        if (DEBUG) {
            log("processSelections()");
        }
        // For the "immediate" case:
        Object value = getSubmittedValue();

        if (value != null) {
            if (DEBUG) {
                log("Found submitted value");
            }
            if (value instanceof String[]) {
                if (DEBUG) {
                    log("found submitted value (string array)");
                }
                String[] obj = (String[]) value;
                ArrayList<String> list = new ArrayList<String>(obj.length);
                for (int counter = 0; counter < obj.length; ++counter) {
                    list.add(obj[counter]);
                    if (DEBUG) {
                        log("\tAdded " + obj[counter]);
                    }
                }
                markSelectedListItems(list, false);
                return;
            }
            throw new IllegalArgumentException("Illegal submitted value");
        }

        // For the first time and "non-immediate" case:
        if (DEBUG) {
            log("No submitted values, use actual value");
        }
        ValueTypeEvaluator valueTypeEvaluator = getValueTypeEvaluator();
        // Covers List cases
        if (valueTypeEvaluator.getValueType() == ValueType.NONE
                || valueTypeEvaluator.getValueType() == ValueType.INVALID) {
            if (DEBUG) {
                log("\tNo value");
            }
            markSelectedListItems(new ArrayList(), true);
            return;
        }

        value = getValue();

        if (DEBUG) {
            if (value == null) {
                log("\t actual value is null");
            } else {
                log("\t actual value is of type "
                        + value.getClass().getName());
            }
        }
        if (value == null) {
            if (DEBUG) {
                log("\tNo value");
            }
            markSelectedListItems(new ArrayList(), true);
            return;
        }

        // Covers Object array
        ArrayList<Object> list = new ArrayList<Object>();
        if (valueTypeEvaluator.getValueType() == ValueType.ARRAY) {
            int length = Array.getLength(value);
            for (int counter = 0; counter < length; ++counter) {
                list.add(Array.get(value, counter));
                if (DEBUG) {
                    log(String.valueOf(Array.get(value, counter)));
                }
            }
            markSelectedListItems(list, true);
            return;
        }

        // Covers Object array
        list.add(value);
        if (DEBUG) {
            log("\tAdded object " + String.valueOf(value));
        }
        markSelectedListItems(list, true);
    }

    /**
     * Marks options corresponding to objects listed as values of this
     * components as selected.
     *
     * @param list A list representation of the selected values
     * @param processed If true, compare the values object by object (this is
     * done if we compare the value of the object with with the list items). If
     * false, perform a string comparison of the string representation of the
     * submitted value of the component with the string representation of the
     * value from the list items (this is done if we compare the submitted
     * values with the list items).
     */
    protected void markSelectedListItems(final java.util.List list,
            final boolean processed) {

        if (DEBUG) {
            log("markSelectedListItems()");
        }
        ListItem option;
        Object nextItem;
        Iterator items = listItems.iterator();
        Iterator selected;

        while (items.hasNext()) {
            nextItem = items.next();
            // If the next item is a selection group, we continue.
            // Need to check this with the guidelines, perhaps
            // you can select options too...
            if (!(nextItem instanceof ListItem)) {
                continue;
            }

            option = (ListItem) nextItem;

            // By default, the option will not be marked as selected
            option.setSelected(false);

            if (DEBUG) {
                log("\tItem value: " + option.getValue());
                log("\tItem type: "
                        + option.getValueObject().getClass().getName());
            }

            // There are no more selected items, continue with the
            // next option
            if (list.isEmpty()) {
                if (DEBUG) {
                    log("No more selected items");
                }
                continue;
            }

            // There are still selected items to account for
            selected = list.iterator();
            while (selected.hasNext()) {
                if (processed) {
                    Object o = selected.next();
                    if (DEBUG) {
                        log("\tSelected object value: " + String.valueOf(o));
                        log("\tSelected object type: "
                                + o.getClass().getName());
                    }
                    if (option.getValueObject().equals(o)) {
                        if (DEBUG) {
                            log("\tFound a match: " + String.valueOf(o));
                        }
                        option.setSelected(true);
                        list.remove(o);
                        break;
                    }
                } else {
                    String s = (String) selected.next();
                    if (s.equals(option.getValue())) {
                        if (DEBUG) {
                            log("\tFound a match: " + s);
                        }
                        option.setSelected(true);
                        list.remove(s);
                        break;
                    }
                }
            }
        }

        // At this point the selected list should be empty.
        if (!list.isEmpty() && !Beans.isDesignTime()) {
            String msg = MessageUtil.getMessage(
                    "com.sun.webui.jsf.resources.LogMessages",
                    "List.badValue",
                    new Object[]{
                        getClientId(FacesContext.getCurrentInstance())
                    });
            log(msg);
        }
    }

    /**
     * Add an option to the list.
     * @param si separator item
     * @return ListItem
     */
    @SuppressWarnings("checkstyle:magicnumber")
    protected ListItem createListItem(final Option si) {

        if (DEBUG) {
            log("createListItem()");
        }
        String label = si.getLabel();
        String valueString = ConversionUtilities
                .convertValueToString(this, si.getValue());

        if (label == null) {
            label = valueString;
        }
        if (DEBUG) {
            log("Label is " + label);
        }
        if ((label.length() * 1.5) > separatorLength) {
            separatorLength = (int) (label.length() * 1.5);
        }
        ListItem listItem = new ListItem(si.getValue(), label,
                si.getDescription(),
                si.isDisabled());

        listItem.setValue(valueString);
        if (si instanceof OptionTitle) {
            listItem.setTitle(true);
        }
        return listItem;

    }

    /**
     * Return a component that implements the label for this ListSelector. If a
     * facet named {@code label} is found that component is returned.
     * Otherwise a {@code Label} component is returned. It is assigned the
     * id
     * {@code getId() + "_label"}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return a label component for this ListSelector
     */
    public UIComponent getLabelComponent() {

        if (DEBUG) {
            log("getLabelComponent()");
        }
        // Check if the page author has defined the facet
        //
        UIComponent labelComponent = getFacet(LABEL_FACET);
        if (labelComponent != null) {
            if (DEBUG) {
                log("\tFound facet");
            }
            return labelComponent;
        }

        // We need to allow an empty string label since this
        // could mean that there is value binding and a
        // message bundle hasn't loaded yet, but there
        // is a value binding since the jakarta.el never returns
        // null for a String binding.
        String labelString = getLabel();
        if (labelString == null) {
            return null;
        }

        // Return the private facet or create one, but initialize
        // it every time
        //
        // We know it's a Label
        Label label = (Label) ComponentUtilities.getPrivateFacet(this,
                LABEL_FACET, true);
        if (label == null) {
            if (DEBUG) {
                log("create Label");
            }
            label = new Label();
            label.setId(ComponentUtilities.createPrivateFacetId(this,
                    LABEL_FACET));
        }
        initLabelFacet(label, labelString, this.getClientId(getFacesContext()));

        ComponentUtilities.putPrivateFacet(this, LABEL_FACET, label);

        return label;
    }

    /**
     * Initialize a label facet.
     *
     * @param label the Label instance
     * @param labelText the label text.
     * @param forComponentId the component instance this label is for
     */
    private void initLabelFacet(final Label label, final String labelText,
            final String forComponentId) {

        if (DEBUG) {
            log("initLabelFacet()");
        }
        if (labelText == null || labelText.length() < 1) {
            // TODO - maybe print a default?
            // A Theme default value.
            label.setText("");
        } else {
            label.setText(labelText);
        }
        label.setLabelLevel(getLabelLevel());
        if (!isReadOnly()) {
            label.setFor(forComponentId);
        }
    }

    /**
     * This implementation returns a component that implements the read only
     * value of this ListSelector. If a facet named {@code readOnly} is found
     * that component is returned. Otherwise a {@code StaticText} component is
     * returned. It is assigned the id {@code getId() + "_readOnly"}
     * <p>
     * If the facet is not defined then the returned {@code StaticText}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return a component that represents the read only value of this
     * ListSelector
     */
    @Override
    public UIComponent getReadOnlyValueComponent() {

        if (DEBUG) {
            log("getReadOnlyValueComponent()");
        }
        // Check if the page author has defined the facet
        //
        UIComponent textComponent = getFacet(READONLY_FACET);
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
                READONLY_FACET));
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
     *
     * This implementation get the value (the object representing the
     * selection(s)) of this component as a String array.
     *
     * @param context The FacesContext of the request
     */
    @Override
    public String[] getValueAsStringArray(final FacesContext context) {

        String[] values = null;
        Object value = getSubmittedValue();
        if (value != null) {
            if (value instanceof String[]) {
                return (String[]) value;
            } else if (value instanceof String) {
                values = new String[1];
                values[0] = (String) value;
                return values;
            }
        }

        value = getValue();
        if (value == null) {
            return new String[0];
        }

        // No submitted value found - look for
        ValueTypeEvaluator valueTypeEvaluator = getValueTypeEvaluator();
        if (valueTypeEvaluator.getValueType() == ValueType.NONE) {
            return new String[0];
        }
        if (valueTypeEvaluator.getValueType() == ValueType.INVALID) {
            return new String[0];
        }

        int counter;
        if (valueTypeEvaluator.getValueType() == ValueType.LIST) {
            java.util.List list = (java.util.List) value;
            counter = list.size();
            values = new String[counter];
            Iterator valueIterator = ((java.util.List) value).iterator();
            String valueString;
            counter = 0;
            while (valueIterator.hasNext()) {
                valueString = ConversionUtilities
                        .convertValueToString(this, valueIterator.next());
                values[counter++] = valueString;
            }
        } else if (valueTypeEvaluator.getValueType() == ValueType.ARRAY) {

            counter = Array.getLength(value);
            values = new String[counter];
            Object valueObject;
            String valueString;

            for (int i = 0; i < counter; ++i) {
                valueObject = Array.get(value, i);
                valueString = ConversionUtilities
                                .convertValueToString(this, valueObject);
                values[i] = valueString;
            }
        } else if (valueTypeEvaluator.getValueType() == ValueType.OBJECT) {

            values = new String[1];
            values[0] = ConversionUtilities.convertValueToString(this, value);
        }

        return values;
    }

    /**
     * Returns the absolute ID of an HTML element suitable for use as the value
     * of an HTML LABEL element's {@code for} attribute. If the
     * {@code ComplexComponent} has sub-components, and one of the
     * sub-components is the target of a label, if that sub-component is a
     * {@code ComplexComponent}, then {@code getLabeledElementId} must
     * called on the sub-component and the value returned. The value returned by
     * this method call may or may not resolve to a component instance. This
     * implementation returns the id of the select list that will have the
     * "LIST_ID" suffix. IF there is not label, then the select list id will be
     * the component's client id.
     *
     * @param context The FacesContext used for the request
     * @return An absolute id suitable for the value of an HTML LABEL element's
     * {@code for} attribute.
     */
    @Override
    public String getLabeledElementId(final FacesContext context) {

        // If this component has a label either as a facet or
        // an attribute, return the id of the select list
        // that will have the "LIST_ID" suffix. IF there is not
        // label, then the select list id will be the component's
        // client id.
        //
        // To ensure we get the right answer call getLabelComponent.
        // This checks for a developer facet or the private label facet.
        // It also checks the label attribute. This is better than
        // relying on "getLabeledComponent" having been called
        // like this method used to do.
        String clntId = this.getClientId(context);
        UIComponent labelComp = getLabelComponent();
        if (labelComp == null) {
            return clntId;
        } else {
            return clntId.concat(LIST_ID);
        }
    }

    /**
     * Returns the id of an HTML element suitable to receive the focus. If the
     * {@code ComplexComponent} has sub-components, and one of the
     * sub-components is to receive the focus, if that sub-component is a
     * {@code ComplexComponent}, then {@code getFocusElementId} must
     * called on the sub-component and the value returned. The value returned by
     * this method call may or may not resolve to a component instance.
     * <p>
     * This implementations returns the value of
     * {@code getLabeledElementId}.
     * </p>
     *
     * @param context The FacesContext used for the request
     */
    @Override
    public String getFocusElementId(final FacesContext context) {
        // For now just return the same id that is used for label.
        return getLabeledElementId(context);
    }

    /**
     * This implementation invokes {@link #getLabeledElementId}.
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
        return getLabeledElementId(context);
    }

    /**
     * Return a string suitable for displaying the value in read only mode.The
     * default is to separate the list values with a comma.
     *
     * @param context The FacesContext
     * @param separator separator character
     * @return String
     */
    public String getValueAsReadOnly(final FacesContext context,
            final String separator) {

        // remove me when the interface method goes.
        return "FIX ME!";
    }

    /**
     * This implementation returns {@code true}.
     * @return {@code boolean}
     */
    @Override
    public boolean mainListSubmits() {
        return true;
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
     * This implementation returns the value if set, otherwise evaluates the
     * expression {@code "rows"}.
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
        return 12;
    }

    /**
     * The number of items to display. The default value is 12.
     *
     * @see #getRows()
     * @param newRows rows
     */
    public void setRows(final int newRows) {
        this.rows = newRows;
        this.rowsSet = true;
    }

    /**
     * Flag indicating that items corresponding to
     * {@code com.sun.webui.jsf.model.Option} that are defined inside a
     * {@code com.sun.webui.jsf.model.OptionGroup} should be surrounded by
     * separators inside the list. The default value is true. If false, no
     * separators are shown. To manually specify the location of separators, set
     * this flag to false and place instances of
     * {@code com.sun.webui.jsf.model.Separator} between the relevant
     * {@code com.sun.webui.jsf.model.Option} instances when specifying the
     * {@code items} attribute.
     * @return {@code boolean}
     */
    public boolean isSeparators() {
        if (this.separatorsSet) {
            return this.separators;
        }
        ValueExpression vb = getValueExpression("separators");
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
     * Flag indicating that items corresponding to
     * {@code com.sun.webui.jsf.model.Option} that are defined inside a
     * {@code com.sun.webui.jsf.model.OptionGroup} should be surrounded by
     * separators inside the list. The default value is true. If false, no
     * separators are shown. To manually specify the location of separators, set
     * this flag to false and place instances of
     * {@code com.sun.webui.jsf.model.Separator} between the relevant
     * {@code com.sun.webui.jsf.model.Option} instances when specifying the
     * {@code items} attribute.
     *
     * @see #isSeparators()
     * @param newSeparators separators
     */
    public void setSeparators(final boolean newSeparators) {
        this.separators = newSeparators;
        this.separatorsSet = true;
    }

    /**
     * This implementation returns the value if set, otherwise evaluates the
     * expression {@code "visible"}.
     * @return {@code boolean}
     */
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
     * This implementation saves the new value and marks the field as set.
     * @param newVisible visible
     */
    @Override
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
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
        this.labelOnTop = ((Boolean) values[1]);
        this.labelOnTopSet = ((Boolean) values[2]);
        this.rows = ((Integer) values[3]);
        this.rowsSet = ((Boolean) values[4]);
        this.separators = ((Boolean) values[5]);
        this.separatorsSet = ((Boolean) values[6]);
        this.visible = ((Boolean) values[7]);
        this.visibleSet = ((Boolean) values[8]);
    }

    /**
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[9];
        values[0] = super.saveState(context);
        if (this.labelOnTop) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.labelOnTopSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        values[3] = this.rows;
        if (this.rowsSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        if (this.separators) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        if (this.separatorsSet) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        if (this.visible) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
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
