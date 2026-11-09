/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
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
import com.sun.webui.jsf.util.CookieUtils;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.RenderingUtilities;

import jakarta.el.ELException;
import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.event.ValueChangeListener;
import jakarta.faces.validator.Validator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Tree component is used to display a tree structure in the rendered HTML
 * page.
 */
@Component(type = "com.sun.webui.jsf.Tree",
        family = "com.sun.webui.jsf.Tree",
        displayName = "Tree", tagName = "tree",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_tree",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_tree_props")
        //CHECKSTYLE:ON
public final class Tree extends TreeNode implements EditableValueHolder {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = -3186310519238174661L;

    /**
     * This is the {@link com.sun.webui.theme.Theme} key used to retrieve the
     * JavaScript needed for this component.
     *
     * @see com.sun.webui.theme.Theme#getPathToJSFile(String)
     */
    public static final String JAVA_SCRIPT_THEME_KEY = "tree";

    /**
     * This is the suffix appended to the client id when forming a request
     * attribute key. The value associated with the generated key indicates
     * which node should be selected. The renderer uses this information to
     * generate JavaScript to select this node, overriding the previous
     * selection.
     */
    public static final String SELECTED_SUFFIX = "_select";

    /**
     * This is the suffix appended to the client id to form the key to the
     * cookie Map needed to retrieve the tree selection.
     */
    public static final String COOKIE_SUFFIX = "-hi";

    /**
     * This is the suffix appended to the client id to form the key to the
     * cookie Map needed to retrieve the node that may need to be expanded
     * (because it was just selected).
     */
    public static final String COOKIE_SUFFIX_EXPAND = "-expand";

    /**
     * String constant representing the content facet name.
     */
    public static final String TREE_CONTENT_FACET_NAME = "content";

    /**
     * String constant representing the image facet name.
     */
    public static final String TREE_IMAGE_FACET_NAME = "image";

    /**
     * Converter.
     */
    private Converter converter;

    /**
     * The set of {@link Validator}s associated with this
     * {@code UIComponent}.
     */
    private List<Validator> validators;

    /**
     * The submittedValue value of this component.
     */
    private Object submittedValue;

    /**
     * Toggle indicating validity of this component.
     */
    private boolean valid = true;

    /**
     * The "localValueSet" state for this component.
     */
    private boolean localValueSet;

    /**
     * The value of the {@code Tree}. This should be a String representing
     * the client id of the selected {@code TreeNode}.
     */
    private Object value;

    /**
     * Set the clientSide attribute to true to specify that the Tree component
     * should run on the client. By default, this attribute is false, so the
     * Tree component interacts with the server. In a client-side tree,
     * expanding and collapsing of the tree nodes happens only in the browser.
     * In a server-side tree, a request is made to the server each time the tree
     * nodes are expanded or collapsed. If you use the actionListener attribute
     * to fire events, you must use a server side tree so that the event can be
     * processed.
     */
    @Property(name = "clientSide",
            displayName = "ClientSide",
            category = "Behavior")
    private boolean clientSide;

    /**
     * clientSide set flag.
     */
    private boolean clientSideSet;

    /**
     * Flag indicating that folder / container nodes will automatically expand
     * when they are selected. This attribute is true by default. If you want a
     * tree's container nodes to expand only when the handle icons are clicked,
     * set expandOnSelect to false.
     */
    @Property(name = "expandOnSelect",
            displayName = "Expand On Select",
            category = "Behavior")
    private boolean expandOnSelect;

    /**
     * expandOnSelect set.
     */
    private boolean expandOnSelectSet;

    /**
     * Flag indicating that event handling for this component should be handled
     * immediately (in Apply Request Values phase) rather than waiting until
     * Invoke Application phase. May be desired for this component when required
     * is true (although most likely not).
     */
    @Property(name = "immediate",
            displayName = "Immediate",
            category = "Advanced")
    private boolean immediate;

    /**
     * immediate set.
     */
    private boolean immediateSet;

    /**
     * Flag indicating that the user must select a value for this tree. Default
     * value is false.
     */
    @Property(name = "required",
            displayName = "Required",
            category = "Behavior")
    private boolean required;

    /**
     * requiredSet flag.
     */
    private boolean requiredSet;

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
    private boolean visible;

    /**
     * visible set flag.
     */
    private boolean visibleSet;

    /**
     * Create a new instance.
     */
    public Tree() {
        super();
        setRendererType("com.sun.webui.jsf.Tree");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Tree";
    }

    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    @Property(isHidden = true, isAttribute = true)
    @Override
    public String getTarget() {
        return super.getTarget();
    }

    @Property(isHidden = true, isAttribute = true)
    @Override
    public String getUrl() {
        return super.getUrl();
    }

    @Property(isHidden = true, isAttribute = true)
    @Override
    public String getImageURL() {
        return super.getImageURL();
    }

    @Property(isHidden = true, isAttribute = true)
    @Override
    public MethodExpression getActionListenerExpression() {
        return super.getActionListenerExpression();
    }

    @Property(isHidden = true, isAttribute = true)
    @Override
    public MethodExpression getActionExpression() {
        return super.getActionExpression();
    }

    // Hide expanded
    @Property(isHidden = true, isAttribute = false)
    @Override
    public boolean isExpanded() {
        return super.isExpanded();
    }

    /**
     * Returns the id of the selected tree node. Should be cast to a String and
     * nothing else.
     * @return String
     */
    @Property(name = "selected",
            displayName = "Selected",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.TreeNodeIdsEditor")
            //CHECKSTYLE:ON
    public String getSelected() {
        return (String) getValue();
    }

    /**
     * Set the clientSide attribute to true to specify that the Tree component
     * should run on the client. By default, this attribute is false, so the
     * Tree component interacts with the server. In a client-side tree,
     * expanding and collapsing of the tree nodes happens only in the browser.
     * In a server-side tree, a request is made to the server each time the tree
     * nodes are expanded or collapsed. If you use the actionListener attribute
     * to fire events, you must use a server side tree so that the event can be
     * processed.
     * @return {@code boolean}
     */
    public boolean isClientSide() {
        if (this.clientSideSet) {
            return this.clientSide;
        }
        ValueExpression vb = getValueExpression("clientSide");
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
     * Set the clientSide attribute to true to specify that the Tree component
     * should run on the client.By default, this attribute is false, so the Tree
     * component interacts with the server. In a client-side tree, expanding and
     * collapsing of the tree nodes happens only in the browser. In a
     * server-side tree, a request is made to the server each time the tree
     * nodes are expanded or collapsed. If you use the actionListener attribute
     * to fire events, you must use a server side tree so that the event can be
     * processed.
     *
     * @param newClientSide clientSide
     * @see #isClientSide()
     */
    public void setClientSide(final boolean newClientSide) {
        this.clientSide = newClientSide;
        this.clientSideSet = true;
    }

    /**
     * Flag indicating that folder / container nodes will automatically expand
     * when they are selected. This attribute is true by default. If you want a
     * tree's container nodes to expand only when the handle icons are clicked,
     * set expandOnSelect to false.
     * @return {@code boolean}
     */
    public boolean isExpandOnSelect() {
        if (this.expandOnSelectSet) {
            return this.expandOnSelect;
        }
        ValueExpression vb = getValueExpression("expandOnSelect");
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
     * Flag indicating that folder / container nodes will automatically expand
     * when they are selected.This attribute is true by default. If you want a
 tree's container nodes to expand only when the handle icons are clicked,
 set expandOnSelect to false.
     *
     * @param newExpandOnSelect expandOnSelect
     * @see #isExpandOnSelect()
     */
    public void setExpandOnSelect(final boolean newExpandOnSelect) {
        this.expandOnSelect = newExpandOnSelect;
        this.expandOnSelectSet = true;
    }

    @Override
    public boolean isImmediate() {
        if (this.immediateSet) {
            return this.immediate;
        }
        ValueExpression vb = getValueExpression("immediate");
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

    @Override
    public void setImmediate(final boolean newImmediate) {
        this.immediate = newImmediate;
        this.immediateSet = true;
    }

    @Override
    public boolean isRequired() {
        if (this.requiredSet) {
            return this.required;
        }
        ValueExpression vb = getValueExpression("required");
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

    @Override
    public void setRequired(final boolean newRequired) {
        this.required = newRequired;
        this.requiredSet = true;
    }

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

    @Override
    public void setStyle(final String newStyle) {
        this.style = newStyle;
    }

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

    @Override
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
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

    @Override
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Specify the id of the selected tree node. Should specify a String object.
     * Also note that this should NOT be the client ID of the selected node.
     *
     * @see #getSelected()
     * @param newSelected selected
     */
    public void setSelected(final String newSelected) {
        if (newSelected == null || newSelected.length() == 0) {
            FacesContext context = FacesContext.getCurrentInstance();
            Cookie cookie = CookieUtils.getCookieValue(context,
                    getClientId(context) + COOKIE_SUFFIX);
            if (cookie != null) {
                if (!RenderingUtilities.isPortlet(context)) {
                    ExternalContext extCtx = context.getExternalContext();
                    HttpServletResponse res
                            = (HttpServletResponse) extCtx.getResponse();
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    res.addCookie(cookie);
                    setValue(null);
                }
            }
        }
        setValue(newSelected);
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

    @Override
    public Converter getConverter() {
        return converter;
    }

    @Override
    public void setConverter(final Converter newConverted) {
        converter = newConverted;
        // Do nothing... throw exception?
    }

    @Override
    public Object getLocalValue() {
        return value;
    }

    @Override
    public Object getValue() {
        if (value != null) {
            return value;
        }
        ValueExpression vb = getValueExpression("value");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            return result;
        }
        return null;
    }

    @Override
    public void setValue(final Object newValue) {
        value = newValue;

        // Mark the local value as set.
        setLocalValueSet(true);
    }

    @Override
    public Object getSubmittedValue() {
        return submittedValue;
    }

    @Override
    public void setSubmittedValue(final Object newSubmittedValue) {
        submittedValue = newSubmittedValue;
    }

    @Override
    public boolean isLocalValueSet() {
        return localValueSet;
    }

    @Override
    public void setLocalValueSet(final boolean newLocalValueSet) {
        localValueSet = newLocalValueSet;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void setValid(final boolean newValid) {
        valid = newValid;
    }

    @Override
    public void addValidator(final Validator validator) {
        if (validator == null) {
            throw new NullPointerException();
        }
        if (validators == null) {
            validators = new ArrayList<Validator>();
        }
        validators.add(validator);
    }

    @Override
    public Validator[] getValidators() {
        if (validators == null) {
            return (new Validator[0]);
        }
        return ((Validator[]) validators.toArray(
                new Validator[validators.size()]));
    }

    @Override
    public void removeValidator(final Validator validator) {
        if (validators != null) {
            validators.remove(validator);
        }
    }

    @Override
    public void addValueChangeListener(final ValueChangeListener listener) {
        addFacesListener(listener);
    }

    @Override
    public ValueChangeListener[] getValueChangeListeners() {
        return (ValueChangeListener[])
                getFacesListeners(ValueChangeListener.class);
    }

    @Override
    public void removeValueChangeListener(final ValueChangeListener listener) {
        removeFacesListener(listener);
    }

    @Override
    public void decode(final FacesContext context) {
        setValid(true);
        super.decode(context);
    }

    @Override
    public void broadcast(final FacesEvent event)
            throws AbortProcessingException {

        // Perform standard superclass processing
        super.broadcast(event);
        if (event instanceof ValueChangeEvent) {
            for (ValueChangeListener listener : getValueChangeListeners()) {
                event.processListener(listener);
            }
        }
    }

    @Override
    public void processUpdates(final FacesContext context) {
        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        // Do the super stuff...
        super.processUpdates(context);

        // Save model stuff...
        try {
            updateModel(context);
        } catch (RuntimeException ex) {
            context.renderResponse();
            throw new RuntimeException(ex);
        }
    }

    /**
     * Perform the following algorithm to update the model data associated with
     * this component, if any, as appropriate.
     *
     * <ul>
     * <li>If the {@code valid} property of this component is
     * {@code false}, take no further action.</li>
     * <li>If the {@code localValueSet} property of this component is
     * {@code false}, take no further action.</li>
     * <li>If no {@code ValueExpression} for {@code value} exists, take
     * no further action.</li>
     * <li>Call {@code setValue()} method of the {@code ValueExpression}
     * to update the value that the {@code ValueExpression} points at.</li>
     * <li>If the {@code setValue()} method returns successfully:
     * <ul>
     * <li>Clear the local value of this component.</li>
     * <li>Set the {@code localValueSet} property of this component to
     * false.</li>
     * </ul>
     * </li>
     * <li>If the {@code setValue()} method call fails:
     * <ul>
     * <li>Queue an error message by calling {@code addMessage()} on
     * the specified {@code FacesContext} instance.</li>
     * <li>Set the {@code valid} property of this component to
     * {@code false}.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param context {@code FacesContext} for the request we are
     * processing.
     */
    public void updateModel(final FacesContext context) {
        // Sanity Checks...
        if (context == null) {
            throw new NullPointerException();
        }
        if (!isValid() || !isLocalValueSet()) {
            return;
        }
        ValueExpression valueExpression = getValueExpression("value");
        if (valueExpression != null) {
            try {
                valueExpression.setValue(context.getELContext(), getLocalValue());
                setValue(null);
                setLocalValueSet(false);
            } catch (ELException ex) {
                String messageStr = ex.getMessage();
                if (messageStr != null) {
                    FacesMessage message;
                    message = new FacesMessage(messageStr);
                    message.setSeverity(FacesMessage.SEVERITY_ERROR);
                    context.addMessage(getClientId(context), message);
                }
                setValid(false);
                if (LogUtil.configEnabled()) {
                    LogUtil.config("Unable to update Model!", ex);
                }
            }
        }
    }

    @Override
    public void processDecodes(final FacesContext context) {
        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }
        super.processDecodes(context);
        if (isImmediate()) {
            executeValidate(context);
        }
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

        super.processValidators(context);

        if (!isImmediate()) {
            executeValidate(context);
        }
    }

    /**
     * Executes validation logic.
     * @param context faces context
     */
    private void executeValidate(final FacesContext context) {
        try {
            validate(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }

        if (!isValid()) {
            context.renderResponse();
        }
    }

    /**
     * Perform the following algorithm to validate the local value of this
     * {@code UIInput}.
     *
     * <ul><li>Retrieve the submitted value with
     * {@code getSubmittedValue()}. If this returns null, exit without
     * further processing. (This indicates that no value was submitted for this
     * component.)</li>
     *
     * <li>Convert the submitted value into a "local value" of the appropriate
     * data type by calling {@code getConvertedValue}.</li>
     *
     * <li>Validate the property by calling {@code validateValue}.</li>
     *
     * <li>If the {@code valid} property of this component is still
     * {@code true}, retrieve the previous value of the component (with
     * {@code getValue()}), store the new local value using
     * {@code setValue()}, and reset the submitted value to null. If the
     * local value is different from the previous value of this component, fire
     * a {@code ValueChangeEvent} to be broadcast to all interested
     * listeners.</li></ul>
     *
     * @param context {@code FacesContext} for the current request.
     */
    public void validate(final FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        // Submitted value == null means "the component was not submitted
        // at all";  validation should not continue
        Object val = getSubmittedValue();
        if (val == null) {
            return;
        }

        Object newValue = val;

        // Validate the value (check for required for now)
        validateValue(context, newValue);

        // If our value is valid, store the new value, erase the
        // "submitted" value, and emit a ValueChangeEvent if appropriate
        if (isValid()) {
            Object previous = getValue();
            setValue(newValue);
            setSubmittedValue(null);
            if (isDifferent(previous, newValue)) {
                queueEvent(new ValueChangeEvent(this, previous, newValue));
            }
        }
    }

    /**
     * Return {@code true} if the objects are not equal.
     *
     * @param val1 Value 1
     * @param val2 Value 2
     *
     * @return true if the 2 values are not equal
     */
    protected boolean isDifferent(final Object val1, final Object val2) {
        if (val1 == val2) {
            // Same object, they're equal
            return false;
        }
        if (val1 == null) {
            // Not equal, and one is null
            return true;
        }
        return !val1.equals(val2);
    }

    /**
     * Validate a specified value.
     * @param context faces context
     * @param newValue value to validate
     */
    protected void validateValue(final FacesContext context,
            final Object newValue) {

        if (!isValid()) {
            return;
        }
        if (isRequired() && ((newValue == null)
                || (newValue.toString().trim().equals("")))) {
            setValid(false);
        }
    }

    /**
     * This method accepts the {@link TreeNode} which is to be selected. The
     * previous {@link TreeNode} that was selected will unselected. No state is
     * saved with this operation, the state is maintained on the client.
     *
     * @deprecated Use #setValue(Object)
     *
     * @param treeNode The {@link TreeNode} to be selected.
     */
    public void selectTreeNode(final TreeNode treeNode) {
        setSelected(treeNode.getId());
    }

    /**
     * This method accepts the clientId of a {@link TreeNode} which is to be
     * selected. The previous {@link TreeNode} that was selected will
     * unselected. No state is saved with this operation, the state is
     * maintained on the client-side.
     *
     * @deprecated Use #setValue(Object)
     *
     * @param id The id of the {@link TreeNode} to be selected.
     */
    public void selectTreeNode(final String id) {
        setSelected(id);
    }

    /**
     * This method returns the {@link TreeNode} client ID that is selected
     * according the browser cookie. This method is generally only useful during
     * the decode process.
     *
     * @return The selected tree node (according to the cookie).
     */
    public String getCookieSelectedTreeNode() {
        FacesContext context = FacesContext.getCurrentInstance();

        // If look at cookies...
        Cookie cookie = CookieUtils.getCookieValue(context,
                getClientId(context) + COOKIE_SUFFIX);
        if (cookie != null) {
            return cookie.getValue();
        }

        // Not found, return null
        return null;
    }

    /**
     * This method will return the {@link TreeNode} client ID that is selected
     * according the browser cookie. This method is only useful during the
     * decode process as the cookie will typically be reset to null immediately
     * after the request is processed.
     *
     * @return The selected tree node (according to the cookie).
     */
    public String getCookieExpandNode() {
        FacesContext context = FacesContext.getCurrentInstance();
        Cookie cookie = CookieUtils.getCookieValue(context,
                getClientId(context) + COOKIE_SUFFIX_EXPAND);

        if (cookie != null) {
            return cookie.getValue();
        }

        // Not found, return null
        return null;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[18];
        values[0] = super.saveState(context);
        if (this.clientSide) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] =  Boolean.FALSE;
        }
        if (this.clientSideSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] =  Boolean.FALSE;
        }
        if (this.expandOnSelect) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] =  Boolean.FALSE;
        }
        if (this.expandOnSelectSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] =  Boolean.FALSE;
        }
        if (this.immediate) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] =  Boolean.FALSE;
        }
        if (this.immediateSet) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] =  Boolean.FALSE;
        }
        if (this.required) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] =  Boolean.FALSE;
        }
        if (this.requiredSet) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] =  Boolean.FALSE;
        }
        values[9] = this.style;
        values[10] = this.styleClass;
        if (this.visible) {
            values[11] = Boolean.TRUE;
        } else {
            values[11] =  Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[12] = Boolean.TRUE;
        } else {
            values[12] =  Boolean.FALSE;
        }
        values[13] = saveAttachedState(context, converter);
        values[14] = this.value;
        if (this.localValueSet) {
            values[15] = Boolean.TRUE;
        } else {
            values[15] =  Boolean.FALSE;
        }
        if (this.valid) {
            values[16] = Boolean.TRUE;
        } else {
            values[16] =  Boolean.FALSE;
        }
        values[17] = saveAttachedState(context, validators);
        return (values);
    }

    @Override
    @SuppressWarnings({"unchecked", "checkstyle:magicnumber"})
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.clientSide = ((Boolean) values[1]);
        this.clientSideSet = ((Boolean) values[2]);
        this.expandOnSelect = ((Boolean) values[3]);
        this.expandOnSelectSet = ((Boolean) values[4]);
        this.immediate = ((Boolean) values[5]);
        this.immediateSet = ((Boolean) values[6]);
        this.required = ((Boolean) values[7]);
        this.requiredSet = ((Boolean) values[8]);
        this.style = (String) values[9];
        this.styleClass = (String) values[10];
        this.visible = ((Boolean) values[11]);
        this.visibleSet = ((Boolean) values[12]);
        converter = (Converter) restoreAttachedState(context, values[13]);
        this.value = values[14];
        localValueSet = ((Boolean) values[15]);
        valid = ((Boolean) values[16]);

        List restoredValidators = (List)
                restoreAttachedState(context, values[17]);
        Iterator iter;
        if (restoredValidators != null) {
            // if there were some validators registered prior to this
            // method being invoked, merge them with the list to be
            // restored.
            if (null != validators) {
                iter = restoredValidators.iterator();
                while (iter.hasNext()) {
                    validators.add((Validator) iter.next());
                }
            } else {
                validators = restoredValidators;
            }
        }
    }

    @Override
    public void resetValue() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
