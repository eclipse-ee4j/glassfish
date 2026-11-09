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

 /*
 * $Id: WizardStep.java,v 1.1.20.1 2009-12-29 03:06:26 jyeary Exp $
 */
package com.sun.webui.jsf.component;

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.event.WizardEventListener;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

/**
 * The WizardStep component represents a single step in a Wizard component step
 * sequence. The components to obtain user data also known collectively as the
 * step task, are specified as children of the WizardStep component.
 */
@Component(type = "com.sun.webui.jsf.WizardStep",
        family = "com.sun.webui.jsf.WizardStep",
        displayName = "Wizard Step",
        tagName = "wizardStep",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_wizard_step",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_wizard_step_props")
        //CHECKSTYLE:ON
public class WizardStep extends UIComponentBase implements NamingContainer {

    /**
     * The detail attribute supplies the text that is to be displayed in the
     * Step Instructions area, before the input components of the Step Content
     * pane. Typically you would provide one or two sentences that describe what
     * the step does, or tell the user how to interact with the step.
     */
    @Property(name = "detail")
    private String detail;

    /**
     * The eventListener attribute is used to specify an object to handle an
     * event that is triggered when a user activates a component in the step.
     * The eventListener attribute value must be a JavaServer Faces EL
     * expression that resolves to an instance of
     * {@code com.sun.webui.jsf.event.WizardEventListener}.
     * <p>
     * The return value of the wizard component's call to the event listener's
     * {@code handleEvent()} method controls the processing of the current step,
     * and determines whether the next step or a previous step, etc. can be
     * navigated to.
     * </p>
     * <p>
     * See the <a href="wizard.html#EventListeners">Event Listeners</a> section
     * in the {@code webuijsf:wizard} tag documentation for more information.
     * </p>
     */
    @Property(name = "eventListener", displayName = "Wizard Event Listener")
    private WizardEventListener eventListener;

    /**
     * Set the finish attribute to true when the wizard step represents the
     * Finish step. For wizards with three or more steps, the Finish step should
     * be the Review Selections page. The finish attribute causes the Finish
     * button to be displayed. The Finish step performs the wizard task when the
     * user clicks the Finish button.
     */
    @Property(name = "finish")
    private boolean finish;

    /**
     * finish set flag.
     */
    private boolean finishSet;

    /**
     * Descriptive text that provides detailed help to the user for this step.
     * The amount of text specified is unlimited but is typically only a few
     * short paragraphs. The content can contain HTML markup for formatting.
     * Note that you must use the character entity references {@code &amp;lt;}
     * and {@code &amp;gt;} to create the &lt; and &gt; characters for HTML
     * elements in the help text.
     */
    @Property(name = "help")
    private String help;

    /**
     * Scripting code executed when the Cancel button is clicked.
     */
    @Property(name = "onCancel", displayName = "Cancel Script")
    private String onCancel;

    /**
     * Scripting code executed when the Close button is clicked.
     */
    @Property(name = "onClose", displayName = "Close Script")
    private String onClose;

    /**
     * Scripting code executed when the Finish button is clicked.
     */
    @Property(name = "onFinish", displayName = "Finish Script")
    private String onFinish;

    /**
     * Scripting code executed when the Help tab is clicked.
     */
    @Property(name = "onHelpTab", displayName = "Help Tab Script")
    private String onHelpTab;

    /**
     * Scripting code executed when the Next button is clicked.
     */
    @Property(name = "onNext", displayName = "Next Script")
    private String onNext;

    /**
     * Scripting code executed when the Next button is clicked.
     */
    @Property(name = "onPrevious", displayName = "Previous Script")
    private String onPrevious;

    /**
     * Scripting code executed when a Step link is clicked.
     */
    @Property(name = "onStepLink", displayName = "Step Link Script")
    private String onStepLink;

    /**
     * Scripting code executed when the Steps tab is clicked.
     */
    @Property(name = "onStepsTab", displayName = "Steps Tab Script")
    private String onStepsTab;

    /**
     * Set the results attribute to true when the wizard step represents the
     * View Results page. This page should be used after the wizard task is
     * completed, to display information related to the task, including failure
     * information if appropriate. This attribute causes the Close button to be
     * displayed on the View Results page.
     */
    @Property(name = "results")
    private boolean results;

    /**
     * results set flag.
     */
    private boolean resultsSet;

    /**
     * A brief description of this step, to be used in the numbered list of
     * steps in the Steps pane.
     */
    @Property(name = "summary")
    private String summary;

    /**
     * A descriptive title to be displayed as the Step Title in the Step Content
     * pane. The Step Title consists of the step number followed by the value of
     * the title attribute. The value of the title attribute could be the same
     * as the value of the summary attribute, or could provide a more detailed
     * description.
     */
    @Property(name = "title")
    private String title;

    /**
     * Construct a new {@code WizardStep}.
     */
    public WizardStep() {
        super();
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.WizardStep"}.
     * @return String
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.WizardStep";
    }

    /**
     * This implementation invokes {@code super.setId}.
     * @param id component id
     */
    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    // Hide rendered
    /**
     * This implementation invokes {@code super.isRendered}.
     * @return {@code boolean}
     */
    @Property(isHidden = true, isAttribute = false)
    @Override
    public boolean isRendered() {
        return super.isRendered();
    }

    /**
     * The detail attribute supplies the text that is to be displayed in the
     * Step Instructions area, before the input components of the Step Content
     * pane.Typically you would provide one or two sentences that describe what
     * the step does, or tell the user how to interact with the step.
     *
     * @return String
     */
    public String getDetail() {
        if (this.detail != null) {
            return this.detail;
        }
        ValueExpression vb = getValueExpression("detail");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The detail attribute supplies the text that is to be displayed in the
     * Step Instructions area, before the input components of the Step Content
     * pane. Typically you would provide one or two sentences that describe what
     * the step does, or tell the user how to interact with the step.
     *
     * @see #getDetail()
     * @param newDetail detail
     */
    public void setDetail(final String newDetail) {
        this.detail = newDetail;
    }

    /**
     * The eventListener attribute is used to specify an object to handle an
     * event that is triggered when a user activates a component in the step.The
     * eventListener attribute value must be a JavaServer Faces EL expression
     * that resolves to an instance of
     * {@code com.sun.webui.jsf.event.WizardEventListener}. The return value of
     * the wizard component's call to the event listener's {@code handleEvent()}
     * method controls the processing of the current step, and determines
     * whether the next step or a previous step, etc. can be navigated to.
     *
     * See the <a href="wizard.html#EventListeners">Event Listeners</a> section
     * in the {@code webuijsf:wizard} tag documentation for more information.
     *
     * @return WizardEventListener
     */
    public WizardEventListener getEventListener() {
        if (this.eventListener != null) {
            return this.eventListener;
        }
        ValueExpression vb = getValueExpression("eventListener");
        if (vb != null) {
            return (WizardEventListener) vb.getValue(getFacesContext()
                    .getELContext());
        }
        return null;
    }

    /**
     * The eventListener attribute is used to specify an object to handle an
     * event that is triggered when a user activates a component in the step.The
     * eventListener attribute value must be a JavaServer Faces EL expression
     * that resolves to an instance of
     * {@code com.sun.webui.jsf.event.WizardEventListener}. The return value of
     * the wizard component's call to the event listener's {@code handleEvent()}
     * method controls the processing of the current step, and determines
     * whether the next step or a previous step, etc. can be navigated to.
     *
     * See the <a href="wizard.html#EventListeners">Event Listeners</a> section
     * in the {@code webuijsf:wizard} tag documentation for more information.
     *
     * @param newEventListener event listener
     * @see #getEventListener()
     */
    public void setEventListener(final WizardEventListener newEventListener) {
        this.eventListener = newEventListener;
    }

    /**
     * Set the finish attribute to true when the wizard step represents the
     * Finish step.For wizards with three or more steps, the Finish step should
     * be the Review Selections page. The finish attribute causes the Finish
     * button to be displayed. The Finish step performs the wizard task when the
     * user clicks the Finish button.
     *
     * @return {@code boolean}
     */
    public boolean isFinish() {
        if (this.finishSet) {
            return this.finish;
        }
        ValueExpression vb = getValueExpression("finish");
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
     * Set the finish attribute to true when the wizard step represents the
     * Finish step. For wizards with three or more steps, the Finish step should
     * be the Review Selections page. The finish attribute causes the Finish
     * button to be displayed. The Finish step performs the wizard task when the
     * user clicks the Finish button.
     *
     * @see #isFinish()
     * @param newFinish finish
     */
    public void setFinish(final boolean newFinish) {
        this.finish = newFinish;
        this.finishSet = true;
    }

    /**
     * Descriptive text that provides detailed help to the user for this step.
     * The amount of text specified is unlimited but is typically only a few
     * short paragraphs. The content can contain HTML markup for formatting.
     * Note that you must use the character entity references {@code &amp;lt;}
     * and {@code &amp;gt;} to create the &lt; and &gt; characters for HTML
     * elements in the help text.
     *
     * @return String
     */
    public String getHelp() {
        if (this.help != null) {
            return this.help;
        }
        ValueExpression vb = getValueExpression("help");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Descriptive text that provides detailed help to the user for this step.
     * The amount of text specified is unlimited but is typically only a few
     * short paragraphs. The content can contain HTML markup for formatting.
     * Note that you must use the character entity references {@code &amp;lt;}
     * and {@code &amp;gt;} to create the &lt; and &gt; characters for HTML
     * elements in the help text.
     *
     * @see #getHelp()
     * @param newHelp help
     */
    public void setHelp(final String newHelp) {
        this.help = newHelp;
    }

    /**
     * Scripting code executed when the Cancel button is clicked.
     *
     * @return String
     */
    public String getOnCancel() {
        if (this.onCancel != null) {
            return this.onCancel;
        }
        ValueExpression vb = getValueExpression("onCancel");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the Cancel button is clicked.
     *
     * @see #getOnCancel()
     * @param newOnCancel onCancel
     */
    public void setOnCancel(final String newOnCancel) {
        this.onCancel = newOnCancel;
    }

    /**
     * Scripting code executed when the Close button is clicked.
     *
     * @return String
     */
    public String getOnClose() {
        if (this.onClose != null) {
            return this.onClose;
        }
        ValueExpression vb = getValueExpression("onClose");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the Close button is clicked.
     *
     * @see #getOnClose()
     * @param newOnClose onClose
     */
    public void setOnClose(final String newOnClose) {
        this.onClose = newOnClose;
    }

    /**
     * Scripting code executed when the Finish button is clicked.
     *
     * @return String
     */
    public String getOnFinish() {
        if (this.onFinish != null) {
            return this.onFinish;
        }
        ValueExpression vb = getValueExpression("onFinish");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the Finish button is clicked.
     *
     * @see #getOnFinish()
     * @param newOnFinish onFinish
     */
    public void setOnFinish(final String newOnFinish) {
        this.onFinish = newOnFinish;
    }

    /**
     * Scripting code executed when the Help tab is clicked.
     *
     * @return String
     */
    public String getOnHelpTab() {
        if (this.onHelpTab != null) {
            return this.onHelpTab;
        }
        ValueExpression vb = getValueExpression("onHelpTab");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the Help tab is clicked.
     *
     * @see #getOnHelpTab()
     * @param newOnHelpTab onHelpTab
     */
    public void setOnHelpTab(final String newOnHelpTab) {
        this.onHelpTab = newOnHelpTab;
    }

    /**
     * Scripting code executed when the Next button is clicked.
     *
     * @return String
     */
    public String getOnNext() {
        if (this.onNext != null) {
            return this.onNext;
        }
        ValueExpression vb = getValueExpression("onNext");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the Next button is clicked.
     *
     * @see #getOnNext()
     * @param newOnNext onNext
     */
    public void setOnNext(final String newOnNext) {
        this.onNext = newOnNext;
    }

    /**
     * Scripting code executed when the Next button is clicked.
     *
     * @return String
     */
    public String getOnPrevious() {
        if (this.onPrevious != null) {
            return this.onPrevious;
        }
        ValueExpression vb = getValueExpression("onPrevious");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the Next button is clicked.
     *
     * @see #getOnPrevious()
     * @param newOnPrevious onPrevious
     */
    public void setOnPrevious(final String newOnPrevious) {
        this.onPrevious = newOnPrevious;
    }

    /**
     * Scripting code executed when a Step link is clicked.
     *
     * @return String
     */
    public String getOnStepLink() {
        if (this.onStepLink != null) {
            return this.onStepLink;
        }
        ValueExpression vb = getValueExpression("onStepLink");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when a Step link is clicked.
     *
     * @see #getOnStepLink()
     * @param newOnStepLink onStepLink
     */
    public void setOnStepLink(final String newOnStepLink) {
        this.onStepLink = newOnStepLink;
    }

    /**
     * Scripting code executed when the Steps tab is clicked.
     *
     * @return String
     */
    public String getOnStepsTab() {
        if (this.onStepsTab != null) {
            return this.onStepsTab;
        }
        ValueExpression vb = getValueExpression("onStepsTab");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the Steps tab is clicked.
     *
     * @see #getOnStepsTab()
     * @param newOnStepsTab onStepsTab
     */
    public void setOnStepsTab(final String newOnStepsTab) {
        this.onStepsTab = newOnStepsTab;
    }

    /**
     * Set the results attribute to true when the wizard step represents the
     * View Results page. This page should be used after the wizard task is
     * completed, to display information related to the task, including failure
     * information if appropriate. This attribute causes the Close button to be
     * displayed on the View Results page.
     *
     * @return {@code boolean}
     */
    public boolean isResults() {
        if (this.resultsSet) {
            return this.results;
        }
        ValueExpression vb = getValueExpression("results");
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
     * Set the results attribute to true when the wizard step represents the
     * View Results page. This page should be used after the wizard task is
     * completed, to display information related to the task, including failure
     * information if appropriate. This attribute causes the Close button to be
     * displayed on the View Results page.
     *
     * @see #isResults()
     * @param newResults results
     */
    public void setResults(final boolean newResults) {
        this.results = newResults;
        this.resultsSet = true;
    }

    /**
     * A brief description of this step, to be used in the numbered list of
     * steps in the Steps pane.
     *
     * @return String
     */
    public String getSummary() {
        if (this.summary != null) {
            return this.summary;
        }
        ValueExpression vb = getValueExpression("summary");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * A brief description of this step, to be used in the numbered list of
     * steps in the Steps pane.
     *
     * @see #getSummary()
     * @param newSummary summary
     */
    public void setSummary(final String newSummary) {
        this.summary = newSummary;
    }

    /**
     * A descriptive title to be displayed as the Step Title in the Step Content
     * pane. The Step Title consists of the step number followed by the value of
     * the title attribute. The value of the title attribute could be the same
     * as the value of the summary attribute, or could provide a more detailed
     * description.
     *
     * @return String
     */
    public String getTitle() {
        if (this.title != null) {
            return this.title;
        }
        ValueExpression vb = getValueExpression("title");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * A descriptive title to be displayed as the Step Title in the Step Content
     * pane. The Step Title consists of the step number followed by the value of
     * the title attribute. The value of the title attribute could be the same
     * as the value of the summary attribute, or could provide a more detailed
     * description.
     *
     * @see #getTitle()
     * @param newTitle title
     */
    public void setTitle(final String newTitle) {
        this.title = newTitle;
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
        this.detail = (String) values[1];
        this.eventListener = (WizardEventListener) values[2];
        this.finish = ((Boolean) values[3]);
        this.finishSet = ((Boolean) values[4]);
        this.help = (String) values[5];
        this.onCancel = (String) values[6];
        this.onClose = (String) values[7];
        this.onFinish = (String) values[8];
        this.onHelpTab = (String) values[9];
        this.onNext = (String) values[10];
        this.onPrevious = (String) values[11];
        this.onStepLink = (String) values[12];
        this.onStepsTab = (String) values[13];
        this.results = ((Boolean) values[14]);
        this.resultsSet = ((Boolean) values[15]);
        this.summary = (String) values[16];
        this.title = (String) values[17];
    }

    /**
     * This implementation saves the state of all properties.
     *
     * @param context faces context
     * @return Object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[18];
        values[0] = super.saveState(context);
        values[1] = this.detail;
        values[2] = this.eventListener;
        if (this.finish) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.finishSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        values[5] = this.help;
        values[6] = this.onCancel;
        values[7] = this.onClose;
        values[8] = this.onFinish;
        values[9] = this.onHelpTab;
        values[10] = this.onNext;
        values[11] = this.onPrevious;
        values[12] = this.onStepLink;
        values[13] = this.onStepsTab;
        if (this.results) {
            values[14] = Boolean.TRUE;
        } else {
            values[14] = Boolean.FALSE;
        }
        if (this.resultsSet) {
            values[15] = Boolean.TRUE;
        } else {
            values[15] = Boolean.FALSE;
        }
        values[16] = this.summary;
        values[17] = this.title;
        return values;
    }
}
