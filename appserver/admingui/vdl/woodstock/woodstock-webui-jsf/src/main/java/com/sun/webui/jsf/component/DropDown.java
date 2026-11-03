/*
 * Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation. All rights reserved.
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
import com.sun.webui.jsf.el.DropDownMethodExpression;
import com.sun.webui.jsf.event.MethodExprActionListener;
import com.sun.webui.jsf.util.LogUtil;

import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.component.ActionSource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.PhaseId;

import java.util.Iterator;
import java.util.Map;

/**
 * The DropDown component is used to display a drop down menu to allow users to
 * select one or more items from a list.
 */
@Component(type = "com.sun.webui.jsf.DropDown",
        family = "com.sun.webui.jsf.DropDown",
        displayName = "Drop Down List", tagName = "dropDown",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_dropdown_list",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_drop_down_props")
        //CHECKSTYLE:ON
public final class DropDown extends ListSelector implements ActionSource {

    /**
     * Submit id.
     */
    public static final String SUBMIT = "_submitter";

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * fireAction flag.
     */
    private boolean fireAction = false;

    /**
     * action expression.
     */
    private MethodExpression actionExpression;

   /**
     * The actionListenerExpression attribute is used to specify a method to
     * handle an action event that is triggered when this component is activated
     * by the user. The actionListenerExpression attribute value must be a
     * JavaServer Faces EL expression that resolves to a method in a backing
     * bean. The method must take a single parameter that is an ActionEvent, and
     * its return type must be {@code void}. The class that defines the
     * method must implement the {@code java.io.Serializable} interface or
     * {@code jakarta.faces.component.StateHolder} interface.
     *
     * <p>
     * The actionListenerExpression method is only invoked when the submitForm
     * attribute is true.
     * </p>
     */
    @Property(name = "actionListenerExpression",
            isHidden = true,
            isAttribute = true,
            displayName = "Action Listener Expression",
            category = "Advanced")
    @Property.Method(
            signature = "void processAction(jakarta.faces.event.ActionEvent)")
    private MethodExpression actionListenerExpression;

    /**
     * If this flag is set to true, then the component is always rendered with
     * no initial selection. By default, the component displays the selection
     * that was made in the last submit of the page. This value should be set to
     * true when the drop down is used for navigation.
     */
    @Property(name = "forgetValue",
            displayName = "Do not display selected value",
            category = "Advanced", isHidden = true)
    private boolean forgetValue = false;

    /**
     * forgetValue set flag.
     */
    private boolean forgetValueSet = false;

    /**
     * When this attribute is set to true, the value of the menu selection is
     * used as the action, to determine which page is displayed next according
     * to the registered navigation rules. Use this attribute instead of the
     * action attribute when the drop down is used for navigation. When you set
     * navigateToValue to true, you must also set submitForm to true.
     */
    @Property(name = "navigateToValue",
            displayName = "Navigate to Component Value",
            category = "Advanced",
            isHidden = true)
    private boolean navigateToValue = false;

    /**
     * navigateToValue set flag.
     */
    private boolean navigateToValueSet = false;

    /**
     * When the submitForm attribute is set to true, the form is immediately
     * submitted when the user changes the selection in the drop down list.
     */
    @Property(name = "submitForm",
            displayName = "Submit the Page on Change",
            isHidden = true)
    private boolean submitForm = false;

    /**
     * submitForm set flag.
     */
    private boolean submitFormSet = false;

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool-tip if the mouse cursor hovers over the HTML
     * element.
     */
    @Property(name = "toolTip",
            displayName = "Tool Tip",
            category = "Behavior")
    private String toolTip = null;

    /**
     * Default constructor.
     */
    public DropDown() {
        super();
        setRendererType("com.sun.webui.jsf.DropDown");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.DropDown";
    }

    @Override
    public void setRows(final int displayRows) {
        setRows(1);
    }

    /**
     * Getter for property multiple.
     *
     * @return Value of property multiple
     */
    public boolean getMultiple() {
        return false;
    }

    /**
     * Setter for property multiple.
     *
     * @param multiple New value of property multiple
     */
    @Override
    public void setMultiple(final boolean multiple) {
        super.setMultiple(false);
    }

    @Override
    public void addActionListener(final ActionListener listener) {
        addFacesListener(listener);
    }

    @Override
    public ActionListener[] getActionListeners() {
        ActionListener[] listeners
                = (ActionListener[]) getFacesListeners(ActionListener.class);
        return listeners;
    }

    @Override
    public void removeActionListener(final ActionListener listener) {
        removeFacesListener(listener);
    }

    @Override
    public void processDecodes(final FacesContext context) {
        if (DEBUG) {
            log("processDecodes()");
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        // Decode the component and any children
        // Do we really want to decode any children?
        // Process all facets and children of this component
        Iterator childComponents = getFacetsAndChildren();
        while (childComponents.hasNext()) {
            UIComponent comp = (UIComponent) childComponents.next();
            comp.processDecodes(context);
        }

        // Get the value of this component
        try {
            decode(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }

        // Testing isSubmitter() alone is deceiving here.
        // The component may have been the submitter but its
        // submittedValue may still be null.
        // This true in the case of an OptionTitle list option
        // It submits but is treated as if there was no submit
        // by not setting the submittedValue. It doesn't explicitly
        // set it to null, in case the caller of the renderer decode
        // method, set it to something special, for example if the
        // DropDown is used as a sub component.
        //
        // However because this component did submit the form
        // we still need to call setLastClientElement
        // so after calling is submitter, still check submittedValue.
        // and return if it is null.
        //
        // Also not that the submittedValue check has been added to
        // validate, as in UIInput's validate method.
        boolean isSubmitter = isSubmitter(context);

        // Should we fire an action?
        //
        // Check submittedValue. Let the code fall through to
        // validate for an immediate action and it will just return.
        fireAction = isSubmitter && isSubmitForm()
                && getSubmittedValue() != null;

        // If we are supposed to fire an action and navigate to the value
        // of the component, we get the submitted value now and pass
        // it to the DropDownMethodBinding.
        if (fireAction && isNavigateToValue()) {
            if (DEBUG) {
                log("\tHanding navigateToValue...");
            }
            MethodExpression mb = getActionExpression();
            if (DEBUG) {
                if (mb != null) {
                    log("\tMethod binding is " + mb.toString());
                } else {
                    log("\tMethod binding is null");
                }
            }

            if (mb instanceof DropDownMethodExpression) {
                String outcome = null;
                Object values = getSubmittedValue();
                if (values instanceof String[]) {
                    String[] stringValues = (String[]) values;
                    if (stringValues.length > 0) {
                        outcome = stringValues[0];
                        if (DEBUG) {
                            log("Outcome is " + outcome);
                        }
                    }
                }

                ((DropDownMethodExpression) mb).setValue(outcome);
                if (DEBUG) {
                    log("\tNavigate to " + outcome);
                }
            }
        }

        // Next, if the component is immediate, we validate the component
        if (isImmediate()) {
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
    }

    @Override
    public void broadcast(final FacesEvent event)
            throws AbortProcessingException {

        // Perform standard superclass processing
        super.broadcast(event);

        if (isSubmitForm() && (event instanceof ActionEvent)) {
            FacesContext context = getFacesContext();
            // Invoke the default ActionListener
            ActionListener listener
                    = context.getApplication().getActionListener();
            if (listener != null) {
                listener.processAction((ActionEvent) event);
            }
        }
    }

    @Override
    public void queueEvent(final FacesEvent event) {
        // If this is an action event, we set the phase according to whether
        // the component is immediate or not.
        if (isSubmitForm()) {
            if (event instanceof ActionEvent) {
                if (isImmediate()) {
                    event.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
                } else {
                    event.setPhaseId(PhaseId.INVOKE_APPLICATION);
                }
            }
        }
        super.queueEvent(event);
    }

    /**
     * Test if the request component is a submitter.
     * @param context faces context
     * @return {@code boolean}
     */
    private boolean isSubmitter(final FacesContext context) {

        if (DEBUG) {
            log("isSubmitter()");
        }
        String compID = getClientId(context).concat(SUBMIT);
        Map requestParameters
                = context.getExternalContext().getRequestParameterMap();

        String submitter = (String) requestParameters.get(compID);
        if (DEBUG) {
            log("\tValue of submitter field " + submitter);
        }
        if (submitter != null) {
            return submitter.equals("true");
        }
        return false;
    }

    @Override
    public void validate(final FacesContext context) {
        // Submitted value == null means "the component was not submitted
        // at all";  validation should not continue
        Object submittedValue = getSubmittedValue();
        if (submittedValue == null) {
            return;
        }

        super.validate(context);
        if (isValid() && fireAction) {
            if (DEBUG) {
                log("\tQueue the component event");
            }
            // queue an event
            queueEvent(new ActionEvent(this));
        }
    }

    // Hide onSelect
    @Property(name = "onSelect", isHidden = true, isAttribute = false)
    @Override
    public String getOnSelect() {
        return super.getOnSelect();
    }

    // Hide rows
    @Property(name = "rows", isHidden = true, isAttribute = false)
    @Override
    public int getRows() {
        return 1;
    }

    // Hide value
    @Property(name = "value", isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * The actionExpression attribute is used to specify the action to take when
     * this component is activated by the user. The value of the
     * actionExpression attribute must be one of the following:
     * <ul>
     * <li>an outcome string, used to indicate which page to display next, as
     * defined by a navigation rule in the application configuration resource
     * file {@code (faces-config.xml)}.</li>
     * <li>a JavaServer Faces EL expression that resolves to a backing bean
     * method. The method must take no parameters and return an outcome string.
     * The class that defines the method must implement the
     * {@code java.io.Serializable} interface or
     * {@code jakarta.faces.component.StateHolder} interface.</li>
     * </ul>
     *
     * When you use the actionExpression attribute in the DropDown component,
     * you must also set the submitForm attribute to true.
     */
    @Property(name = "actionExpression",
            isHidden = true,
            displayName = "Action Expression")
    @Property.Method(signature = "java.lang.String action()")
    @Override
    public MethodExpression getActionExpression() {
        if (this.actionExpression == null && isNavigateToValue()) {
            setActionExpression(new DropDownMethodExpression());
        }
        return this.actionExpression;
    }

    /**
     * The actionExpression attribute is used to specify the action to take when
     * this component is activated by the user.The value of the actionExpression
     * attribute must be one of the following:
     * <ul>
     * <li>an outcome string, used to indicate which page to display next, as
     * defined by a navigation rule in the application configuration resource
     * file {@code (faces-config.xml)}.</li>
     * <li>a JavaServer Faces EL expression that resolves to a backing bean
     * method. The method must take no parameters and return an outcome string.
     * The class that defines the method must implement the
     * {@code java.io.Serializable} interface or
     * {@code jakarta.faces.component.StateHolder} interface.</li>
     * </ul>
     *
     * When you use the actionExpression attribute in the DropDown component,
     * you must also set the submitForm attribute to true.
     *
     * @param me new action expression
     */
    @Override
    public void setActionExpression(final MethodExpression me) {
        this.actionExpression = me;
    }

    /**
     * The actionListenerExpression attribute is used to specify a method to
     * handle an action event that is triggered when this component is activated
     * by the user. The actionListenerExpression attribute value must be a
     * JavaServer Faces EL expression that resolves to a method in a backing
     * bean. The method must take a single parameter that is an ActionEvent, and
     * its return type must be {@code void}. The class that defines the
     * method must implement the {@code java.io.Serializable} interface or
     * {@code jakarta.faces.component.StateHolder} interface.
     *
     * <p>
     * The actionListenerExpression method is only invoked when the submitForm
     * attribute is true.
     * </p>
     * @return MethodExpression
     */
    public MethodExpression getActionListenerExpression() {
        return this.actionListenerExpression;
    }

    /**
     * The actionListenerExpression attribute is used to specify a method to
     * handle an action event that is triggered when this component is activated
     * by the user. The actionListenerExpression attribute value must be a
     * JavaServer Faces EL expression that resolves to a method in a backing
     * bean. The method must take a single parameter that is an ActionEvent, and
     * its return type must be {@code void}. The class that defines the
     * method must implement the {@code java.io.Serializable} interface or
     * {@code jakarta.faces.component.StateHolder} interface.
     *
     * <p>
     * The actionListenerExpression method is only invoked when the submitForm
     * attribute is true.
     * </p>
     * @param newActionListenerExpression actionListenerExpression
     */
    public void setActionListenerExpression(
            final MethodExpression newActionListenerExpression) {

        //call thru
        ActionListener[] curActionListeners = getActionListeners();
        // see if we need to remove existing actionListener.
        // only need to if this.actionListenerExpression != null
        // (since curMethodExpression won't be null)
        if (null != curActionListeners
                && this.actionListenerExpression != null) {
            for (ActionListener curActionListener1 : curActionListeners) {
                if (curActionListener1 instanceof MethodExprActionListener) {
                    MethodExprActionListener curActionListener =
                            (MethodExprActionListener) curActionListener1;
                    MethodExpression curMethodExpression =
                            curActionListener.getMethodExpression();
                    if (this.actionListenerExpression
                            .equals(curMethodExpression)) {
                        removeActionListener(curActionListener);
                        break;
                    }
                }
            }
        }
        if (newActionListenerExpression == null) {
            this.actionListenerExpression = null;
        } else {
            this.actionListenerExpression = newActionListenerExpression;
            addActionListener(new MethodExprActionListener(
                            this.actionListenerExpression));
        }
    }

    /**
     * If this flag is set to true, then the component is always rendered with
     * no initial selection. By default, the component displays the selection
     * that was made in the last submit of the page. This value should be set to
     * true when the drop down is used for navigation.
     * @return {@code boolean}
     */
    public boolean isForgetValue() {
        if (this.forgetValueSet) {
            return this.forgetValue;
        }
        ValueExpression vb = getValueExpression("forgetValue");
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
     * If this flag is set to true, then the component is always rendered with
     * no initial selection. By default, the component displays the selection
     * that was made in the last submit of the page. This value should be set to
     * true when the drop down is used for navigation.
     *
     * @see #isForgetValue()
     * @param newForgetValue forgetValue
     */
    public void setForgetValue(final boolean newForgetValue) {
        this.forgetValue = newForgetValue;
        this.forgetValueSet = true;
    }

    /**
     * When this attribute is set to true, the value of the menu selection is
     * used as the action, to determine which page is displayed next according
     * to the registered navigation rules. Use this attribute instead of the
     * action attribute when the drop down is used for navigation. When you set
     * navigateToValue to true, you must also set submitForm to true.
     * @return {@code boolean}
     */
    public boolean isNavigateToValue() {
        if (this.navigateToValueSet) {
            return this.navigateToValue;
        }
        ValueExpression vb = getValueExpression("navigateToValue");
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
     * When this attribute is set to true, the value of the menu selection is
     * used as the action, to determine which page is displayed next according
     * to the registered navigation rules. Use this attribute instead of the
     * action attribute when the drop down is used for navigation. When you set
     * navigateToValue to true, you must also set submitForm to true.
     *
     * @see #isNavigateToValue()
     * @param newNavigateToValue navigateToValue
     */
    public void setNavigateToValue(final boolean newNavigateToValue) {
        this.navigateToValue = newNavigateToValue;
        this.navigateToValueSet = true;
    }

    /**
     * When the submitForm attribute is set to true, the form is immediately
     * submitted when the user changes the selection in the drop down list.
     * @return {@code boolean}
     */
    public boolean isSubmitForm() {
        if (this.submitFormSet) {
            return this.submitForm;
        }
        ValueExpression vb = getValueExpression("submitForm");
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
     * When the submitForm attribute is set to true, the form is immediately
     * submitted when the user changes the selection in the drop down list.
     *
     * @see #isSubmitForm()
     * @param newSubmitForm submitForm
     */
    public void setSubmitForm(final boolean newSubmitForm) {
        this.submitForm = newSubmitForm;
        this.submitFormSet = true;
    }

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

    @Override
    public void setToolTip(final String newToolTip) {
        this.toolTip = newToolTip;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[8];
        values[0] = super.saveState(context);
        if (this.forgetValue) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.forgetValueSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.navigateToValue) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.navigateToValueSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        if (this.submitForm) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        if (this.submitFormSet) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        values[7] = this.toolTip;
        Object[] values2 = new Object[4];
        values2[0] = values;
        values2[1] = null;
        values2[2] = saveAttachedState(context, actionExpression);
        values2[3] = saveAttachedState(context, actionListenerExpression);
        return (values2);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values2 = (Object[]) state;
        Object[] values = (Object[]) values2[0];
        super.restoreState(context, values[0]);
        this.forgetValue = ((Boolean) values[1]);
        this.forgetValueSet = ((Boolean) values[2]);
        this.navigateToValue = ((Boolean) values[3]);
        this.navigateToValueSet = ((Boolean) values[4]);
        this.submitForm = ((Boolean) values[5]);
        this.submitFormSet = ((Boolean) values[6]);
        this.toolTip = (String) values[7];
        actionExpression = (MethodExpression)
                restoreAttachedState(context, values2[2]);
        actionListenerExpression = (MethodExpression)
                restoreAttachedState(context, values2[3]);
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
