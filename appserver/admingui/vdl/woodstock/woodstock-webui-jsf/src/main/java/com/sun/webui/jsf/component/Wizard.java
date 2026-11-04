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
import com.sun.webui.jsf.event.WizardActionListener;
import com.sun.webui.jsf.event.WizardEvent;
import com.sun.webui.jsf.event.WizardEventListener;
import com.sun.webui.jsf.model.WizardModel;
import com.sun.webui.jsf.model.WizardModelBase;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.PhaseId;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

// On Sub Components
//
// There are many sub components used by the wizard.
// Facets will eventually be supported for many at various levels.
// For example a facet for the complete StepsPane could
// be specified and rendered as a whole. Or just the
// steps/help tabset or individual tabs may be facets.
// It's not clear on exactly how much of this flexibility
// will be useful. When this is supported it would also be useful to have
// a "WizardStepsPane" component and renderer, vs. implicit code that
// creates the pane and the WizardRenderer renders it.
//
// For example having a facet for a button seems like a good
// idea, but what does the wizard do, does it register its
// action listener as well or expect any existing action listeners
// to propagate events and how does the wizard deal with this
// situation ? Does it remove action listeners, subsitute its own
// and forward to other registered listeners ?
// The common use for providing facets for buttons will be to
// specify custom javascript for client side behavior but providing
// facets for this seems very cumbersome to the average user that
// just wants to add some simple javascript to the next button.
// These details need to be addressed.
//
// As for other subcomponents, many are reused and may not be
// offered as facets. These reused sub components are mainly for
// use in the steps list, assuming a facet is not provided for
// the list. For example the static text visual features all resuse
// a single static text component.
// However the step links are not resused.
//
// Speaking of the step links, these need some serious management.
// In general sub components that need decoding are added as
// children to the wizard and filtered out to obtain the WizardStep
// children as necessary.
// Links need to be pruned from the facet map on a regular basis
// since some may only be relevant once and then never used again.
//
// Currently the typical use of the wizard tag is to specify all
// the wizard steps in the jsp page. This is a real drag on performance
// since the tag handler is generated, it just reads in all steps whether
// or not they are needed or not. Serious pruning could be accomplished
// if branches that are not taken, are not read as children.
// It would also be beneficial to have a renderer for the WizardStep
// and not the WizardRenderer itself, but this is not as straightforward
// as it might appear. Mainly because the steps list must be drawn before
// the actual step, so there needs to be a renderer that understands
// how to render a WizardStep for use by the StepsPane and a renderer
// to render the step in the body of the wizard.

/**
 * The Wizard component manages a sequence of
 * {@link WizardStep WizardStep} components.
 * <p>
 * The component delegates to a {@link WizardModel WizardModel} instance,
 * {@link WizardModelBase WizardModelBase} by default, to control the
 * life cycle and the navigation through the sequence of
 * {@link WizardStep WizardStep} components.
 * </p>
 * <p>
 * The {@code Wizard} component {@link #broadcast(FacesEvent) broadcasts}
 * {@link WizardEvent WizardEvents} to
 * {@link WizardEventListener WizardEventListeners} existing on the
 * current {@link WizardStep WizardStep} and the {@code Wizard} instance.
 * The events are then forwarded to the model based on the listener's response
 * to the event.
 * </p>
 * <h3>Wizard events</h3>
 * The following events are broadcast by the wizard. There are two events
 * that are only broadcast to the
 * {@link wizardEventListener WizardEventListener} specified
 * with the {@code eventListener} property on the {@code Wizard}
 * component:
 * <p>
 * <ul>
 * <li>{@code WizardEvent.START} is broadcast once, when the
 * wizard is first rendered during a wizard session. It is broadcast during
 * {@link #encodeBegin(FacesContext) encodeBegin} before any rendering has
 * begun.
 * </li>
 * <li>{@code WizardEvent.COMPLETE} is broadcast once when the wizard
 * session has completed. It
 * is broadcast during {@link #encodeEnd(FacesContext) encodeEnd} after the
 * last response for this wizard session has been written and the wizard
 * model {@link WizardModelBase#complete complete} method has been
 * called to inform the model it will no longer be referenced during this
 * wizard session. The next time this {@code Wizard} instance conducts
 * a wizard session, the {@code WizardEvent.START} event is broadcast.
 * </li>
 * </ul>
 * <p>
 * The following events may be broadcast during the INVOKE_APPLICATION phase to
 * the {@link WizardEventListener eventListener} specified by the
 * {@code eventListener} property of the {@code Wizard}
 * component and the current {@link WizardStep WizardStep} in which the
 * event has occurred, and to the wizard model.
 * </p>
 * <ul>
 * <li>{@code WizardEvent.NEXT} is broadcast when the user clicks the
 * next button.
 * </li>
 * <li>{@code WizardEvent.PREVIOUS} is broadcast when the user clicks
 * the previous button.
 * </li>
 * <li>{@code WizardEvent.GOTOSTEP} is broadcast when the user clicks
 * a previous step link in the step list.
 * </li>
 * <li>{@code WizardEvent.HELPTAB} is broadcast when the user clicks the
 * help tab, when there is step help available.
 * </li>
 * <li>{@code WizardEvent.STEPSTAB} is broadcast when the user clicks the
 * steps tab, when there is step help available.
 * </li>
 * <li>{@code WizardEvent.CANCEL} is broadcast when the user clicks the
 * cancel button.
 * </li>
 * <li>{@code WizardEvent.CLOSE} is broadcast when the user clicks the
 * close button of a results page.
 * </li>
 * <li>{@code WizardEvent.FINISH} is broadcast when the user clicks the
 * finish button on the last page of the wizard.
 * </li>
 * </ul>
 * <p>
 * When the application's {@code WizardEventListener} is invoked it may
 * return {@code true} to continue the current wizard session or
 * {@code false} to prevent the wizard from proceeding, thereby
 * remaining on the current step. If the listener returns {@code true}
 * the event is forwarded to the wizard model.
 * If the listener throws an exception the
 * {@code WizardEvent.CANCEL} event is forwarded to the wizard model
 * and the wizard will proceed to complete the wizard session
 * eventually broadcasting the {@code WizardEvent.COMPLETE} event.
 */
@Component(type = "com.sun.webui.jsf.Wizard",
        family = "com.sun.webui.jsf.Wizard",
        displayName = "Wizard",
        tagName = "wizard",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_wizard",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_wizard_props")
        //CHECKSTYLE:ON
public class Wizard extends UIComponentBase implements NamingContainer {

    //CHECKSTYLE:OFF
    // Constants
    private static final String MODEL = "model";
    private static final Boolean WIZARD_TRUE = Boolean.TRUE;
    private static final Boolean WIZARD_FALSE = Boolean.FALSE;

    // Facets
    private static final String RESULTS_FACET = "results";
    private static final String NEXT_FACET = "next";
    private static final String PREVIOUS_FACET = "previous";
    private static final String FINISH_FACET = "finish";
    private static final String CANCEL_FACET = "cancel";
    private static final String CLOSE_FACET = "close";
    private static final String TABS_FACET = "tabs";
    private static final String TITLE_FACET = "title";
    private static final String INDICATOR_FACET = "stepIndicator";
    private static final String STEPS_PANE_TITLE = "stepsPaneTitle";

    // Other Constants
    private static final String USCORE = "_";
    private static final String STEP_TEXT = "stptxt";
    private static final String STEP_NUM = "stpnum";
    private static final String STEP_LINK = "stplnk";
    private static final String STEP_HELP = "stphlp";
    private static final String STEP_TITLE = "stpttl";
    private static final String STEP_TITLE_LABEL = "stpttllbl";
    private static final String STEP_DETAIL = "stpdtl";
    private static final String STEP_SUMMARY = "stpsmmy";
    private static final String STEP_PLACEHLDR = "stpplhld";
    private static final String STEP_TAB = "stptb";
    private static final String HELP_TAB = "hlptb";
    private static final String NUM = "num";

    // Attributes
    private static final String DISABLED_ATTR = "disabled";
    private static final String IMMEDIATE_ATTR = "immediate";
    private static final String MINI_ATTR = "mini";
    private static final String ESCAPE_ATTR = "escape";
    private static final String ONCLICK_ATTR = "onClick";

    // Wizard Theme Text
    private static final String WIZARD_CANCEL = "Wizard.cancel";
    private static final String WIZARD_CLOSE = "Wizard.close";
    private static final String WIZARD_FINISH = "Wizard.finish";
    private static final String WIZARD_NEXT = "Wizard.next";
    private static final String WIZARD_PREVIOUS = "Wizard.previous";
    private static final String WIZARD_STEP_TITLE_LABEL
            = "Wizard.stepTitleLabel";
    private static final String WIZARD_STEP_TAB = "Wizard.stepTab";
    private static final String WIZARD_HELP_TAB = "Wizard.helpTab";
    private static final String WIZARD_SKIP_LINK_ALT
            = "Wizard.skipLinkAlt";
    private static final String WIZARD_CURRENT_STEP_ALT
            = "Wizard.currentStepAlt";
    private static final String WIZARD_TAB_TOOLTIP
            = "Wizard.tabToolTip";
    private static final String WIZARD_PLACEHOLDER_TEXT
            = "Wizard.placeholderText";
    private static final String WIZARD_STEPS_PANE_TITLE
            = "Wizard.stepsPaneTitle";
    //CHECKSTYLE:ON

    // One of
    // WizardEvent.NOEVENT
    // WizardEvent.CANCEL
    // WizardEvent.CLOSE
    // WizardEvent.FINISH
    // WizardEvent.GOTOSTEP
    // WizardEvent.HELPTAB
    // WizardEvent.NEXT
    // WizardEvent.PREVIOUS
    // WizardEvent.STEPSTAB
    // We want to begin with the START event.
    // Ensure that this state will only be START
    // at the first rendering. Every other cycle, this
    // must be set to a different event, especially NOEVENT
    // when the request is from a component with a wizard step.
    //
    // This member is not transient in case this is server side
    // state saving but it is not preserved during save/restore state.
    // Not sure if this matters for server side state saving, since
    // this component is not necessarily serializable.
    /**
     * Initially {@code WizardEvent.START} and subsequently the
     * event related to a user action.
     */
    private int event = WizardEvent.START;

    // The actual button instance
    /**
     * Maintains the component instance that triggers an event during the
     * request processing. Not saved or restored.
     */
    private transient UIComponent eventSource;

    /**
     * Maintains the id of the {@link WizardStep WizardStep} represented by a
     * previous step link, during request processing. Not saved or restored.
     */
    private transient String gotoStepId;

    /**
     * Maintains the state of the tabs in the steps pane. This member is saved
     * and restored.
     */
    private boolean stepTabActive = true;

    /**
     * The eventListener attribute is used to specify an object to handle an
     * event that is triggered when a user activates a component in the wizard.
     * The eventListener attribute value must be a JavaServer Faces EL
     * expression that resolves to an instance of
     * {@code com.sun.webui.jsf.event.WizardEventListener}.
     * <p>
     * The return value of the wizard component's call to the event listener's
     * handleEvent() method controls the processing of the current step that is
     * being performed, and determines whether the next step or a previous step,
     * etc. can be navigated to.
     * </p>
     * See the <a href="#EventListeners">Event Listeners</a> section also.
     */
    @Property(name = "eventListener", displayName = "Wizard Event Listener")
    private WizardEventListener eventListener = null;

    /**
     * The wizard is being targeted to a popup window. Default is {@code true}.
     * Set this property to {@code false} if the wizard is to appear within a
     * main browser window.
     */
    @Property(name = "isPopup", displayName = "isPopup",
            isHidden = true, isAttribute = false)
    private boolean isPopup = false;

    /**
     * isPopup set flag.
     */
    private boolean isPopupSet = false;

    /**
     * The {@code model} property is a value binding that resolves to an
     * instance of {@code WizardModel}. This instance is an alternative to the
     * default {@code WizardModelBase} instance that controls the flow of steps
     * in the wizard.
     */
    @Property(name = "model", displayName = "Wizard Model", isHidden = true)
    private com.sun.webui.jsf.model.WizardModel model = null;

    /**
     * Scripting code executed when the Cancel button is clicked.
     */
    @Property(name = "onCancel", displayName = "Cancel Script")
    private String onCancel = null;

    /**
     * Scripting code executed when the Close button is clicked.
     */
    @Property(name = "onClose", displayName = "Close Script")
    private String onClose = null;

    /**
     * Scripting code executed when the Finish button is clicked.
     */
    @Property(name = "onFinish", displayName = "Finish Script")
    private String onFinish = null;

    /**
     * Scripting code executed when the Help tab is clicked.
     */
    @Property(name = "onHelpTab", displayName = "Help Tab Script")
    private String onHelpTab = null;

    /**
     * Scripting code executed when the Next button is clicked.
     */
    @Property(name = "onNext", displayName = "Next Script")
    private String onNext = null;

    /**
     * Scripting code that is invoked when the wizard popup is dismissed. If the
     * wizard is not in a popup, the onPopupDismiss attribute is ignored. The
     * scripting code must specify what happens in the browser when the window
     * is closed. For example, the form of the parent window that opened the
     * popup should be submitted, and the browser might be redirected, or the
     * display refreshed to reflect the task completed by the user. These
     * activities provide feedback to the user.
     */
    @Property(name = "onPopupDismiss", displayName = "onPopupDismiss")
    private String onPopupDismiss = null;

    /**
     * Scripting code executed when the Previous button is clicked.
     */
    @Property(name = "onPrevious", displayName = "Previous Script")
    private String onPrevious = null;

    /**
     * Scripting code executed when a Step link is clicked.
     */
    @Property(name = "onStepLink", displayName = "Step Link Script")
    private String onStepLink = null;

    /**
     * Scripting code executed when the Steps tab is clicked.
     */
    @Property(name = "onStepsTab", displayName = "Steps Tab Script")
    private String onStepsTab = null;

    /**
     * Use the steps attribute to specify the wizard steps programmatically,
     * instead of using the {@code webuijsf:wizardStep} tags in the JSP. The
     * steps attribute must be specified as an {@code ArrayList} or {@code List}
     * of {@code WizardStep}, {@code WizardBranch}, {@code WizardBranchSteps},
     * or {@code WizardSubstepBranch} components.
     */
    @Property(name = "steps",
            displayName = "Wizard Steps",
            category = "Data",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor")
            //CHECKSTYLE:ON
    private Object steps = null;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style", displayName = "CSS Style(s)")
    private String style = null;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "styleClass", displayName = "CSS Style Class(es)")
    private String styleClass = null;

    /**
     * The text to be displayed as the title for the wizard. The title is
     * displayed in the top line, and extends the full width of the wizard
     * window.
     *
     * @see #getTitle()
     */
    @Property(name = "title", displayName = "Wizard Title")
    private String title = null;

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     */
    @Property(name = "visible", displayName = "Visible")
    private boolean visible = false;

    /**
     * visible set flag.
     */
    private boolean visibleSet = false;

    /**
     * Construct a {@code Wizard} instance. Sets renderer type to
     * {@code com.sun.webui.jsf.Wizard}.
     */
    public Wizard() {
        super();
        setRendererType("com.sun.webui.jsf.Wizard");
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.Wizard"}.
     * @return String
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Wizard";
    }

    // Need to ensure that wizardModel methods are not
    // called before WizardModelBase is initialized.
    // Need to work out what this means if the model
    // is supplied by the developer.
    // Looks like "isComplete" is the first call made into
    // the wizard from the renderer, since it wants to know
    // if the wizard is closing in the encodeBegin.
    /**
     * Overrides {@code super.getModel()} to provide a default
     * {@link WizardModel WizardModel} instance. If {@code super.getModel()}
     * returns null, create an instance of
     * {@link WizardModelBase WizardModelBase} and call {@code super.setModel()}
     * with the instance.
     *
     * @return WizardModel
     */
    public WizardModel getModel() {
        WizardModel wm = doGetModel();
        if (wm == null) {
            wm = new WizardModelBase();
            setModel(wm);
        }
        return wm;
    }

    /**
     * The {@code model} property is a value binding that resolves to an
     * instance of {@code WizardModel}. This instance is an alternative to the
     * default {@code WizardModelBase} instance that controls the flow of steps
     * in the wizard.
     *
     * @return WizardModel
     */
    private WizardModel doGetModel() {
        if (this.model != null) {
            return this.model;
        }
        ValueExpression vb = getValueExpression("model");
        if (vb != null) {
            return (WizardModel) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Convenience routine to determine if a sub component or other JSF event
     * caused the life-cycle to proceed to the RENDER_RESPONSE phase
     * prematurely.
     *
     * @return {@code boolean}
     */
    private boolean prematureRender() {
        return getFacesContext().getRenderResponse();
    }

    // This actually should call the renderer's decode method
    // to be JSF compliant and allow an alternate WizardRenderer
    // to decode something that it expects.
    /**
     * Called by {@link #processDecodes(FacesContext) processDecodes} during
     * APPLY_REQUEST_VALUES phase. Unlike many components, this method does not
     * defer to the renderer since there is nothing in the response to decode by
     * the {@code Wizard} component directly.
     */
    @Override
    public void decode(final FacesContext context) {

        // In case the renderer has a decode method
        // The current renderer doesn't decode anything.
        super.decode(context);

        // Always queue an event so that broadcast is
        // always called.
        // I think we have to do this in case the form is
        // submitted from a component on the step.
        WizardEvent wizardEvent = new WizardEvent(this);
        wizardEvent.setPhaseId(PhaseId.INVOKE_APPLICATION);
        queueEvent(wizardEvent);
    }

    /**
     * The wizard controls the decoding of the current wizard step. The wizard
     * must call {@code processDecodes()} for its controls before the current
     * step, to ensure that any control events are issued before processing the
     * step.
     *
     * @exception NullPointerException
     * @param context This FacesContext for this request.
     */
    @Override
    public void processDecodes(final FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        if (!isRendered()) {
            return;
        }

        // Currently there is nothing for a Wizard to decode.
        // Eventually there may be some hidden fields
        // associated with the Wizard, like an identifier that
        // the Wizard uses to cache its state in the Session
        // etc.
        try {
            decode(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }
        decodeControls(context);
        decodeStep(context);
    }

    /**
     * Invoke the {@code processDecodes()}method on the wizard's controls. In
     * effect, all children that are not instances of
     * {@link WizardStep WizardStep}.
     *
     * @param context This FacesContext for this request.
     */
    protected void decodeControls(final FacesContext context) {

        Iterator kids = getFacetsAndChildren();
        while (kids.hasNext()) {
            WizardStep currentStep = null;
            UIComponent kid = (UIComponent) kids.next();
            if (kid instanceof WizardStep) {
                continue;
            }
            kid.processDecodes(context);
        }
    }

    /**
     * Invoke the wizard model's {@link WizardModelBase#decode(int,boolean)
     * decode} method to determine if the wizard should invoke
     * {@code processDecodes()} on the current {@link WizardStep
     * WizardStep}.
     *
     * @param context This FacesContext for this request.
     */
    protected void decodeStep(final FacesContext context) {
        WizardModel wizardModel = getModel();
        if (wizardModel.decode(event, prematureRender())) {
            wizardModel.getCurrentStep().processDecodes(context);
        }
    }

    /**
     * The wizard controls the validating of the current wizard step. The wizard
     * must call {@code processValidators()} for its controls before the current
     * step, to ensure that any control events are issued before processing the
     * step. Call the {@link #validateControls(FacesContext) validateControls}
     * method, and then {@link #validateStep(FacesContext) validateStep}.
     *
     * @exception NullPointerException
     * @param context This FacesContext for this request.
     */
    @Override
    public void processValidators(final FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        validateControls(context);
        validateStep(context);
    }

    /**
     * Invoke the {@code processValidators()} method on the wizard's controls.
     * In effect, all children that are not instances of
     * {@link WizardStep WizardStep}.
     *
     * @param context This FacesContext for this request.
     */
    protected void validateControls(final FacesContext context) {

        // Process all the facets and children of this component
        Iterator kids = getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            if (kid instanceof WizardStep) {
                continue;
            }
            kid.processValidators(context);
        }
    }

    /**
     * Invoke the wizard model's {@link WizardModelBase#validate(int,boolean)
     * validate} method to determine if the wizard should invoke
     * {@code processValidators()} on the current step. If a component in a step
     * does not validate, it will be assumed that
     * {@code FacesContext.renderResponse}has been called which in turn will
     * cause {@link #prematureRender() prematureRender} to return {@code true}.
     *
     * @param context This FacesContext for this request.
     */
    protected void validateStep(final FacesContext context) {
        WizardModel wizardModel = getModel();
        if (wizardModel.validate(event, prematureRender())) {
            wizardModel.getCurrentStep().processValidators(context);
        }
    }

    /**
     * The wizard controls the updating of the current wizard step. The wizard
     * must call {@code processUpdates()} for its controls before the current
     * step, to ensure that any control events are issued before processing the
     * step. Call the {@link #updateControls(FacesContext) updateControls}
     * method, and then {@link #updateStep(FacesContext) updateStep}.
     *
     * @exception NullPointerException
     * @param context This FacesContext for this request.
     */
    @Override
    public void processUpdates(final FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        updateControls(context);
        updateStep(context);
    }

    /**
     * Invoke the {@code processUpdates()}method on the wizard's controls. In
     * effect, all children that are not instances of
     * {@link WizardStep WizardStep}
     *
     * @param context This FacesContext for this request.
     */
    protected void updateControls(final FacesContext context) {

        // Process all facets and children of this component
        Iterator kids = getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            if (kid instanceof WizardStep) {
                continue;
            }
            kid.processUpdates(context);
        }
    }

    /**
     * Invoke the wizard model's {@link WizardModelBase#update(int,boolean)
     * update} method to determine if the wizard should invoke
     * {@code processUpdates()} on the current step..
     *
     * @param context This FacesContext for this request.
     */
    protected void updateStep(final FacesContext context) {

        WizardModel wizardModel = getModel();
        if (wizardModel.update(event, prematureRender())) {
            wizardModel.getCurrentStep().processUpdates(context);
        }
    }

    /**
     * Begin the rendering of the wizard. If the wizard is rendering for the
     * first time, the {@code event} member is {@code WizardEvent.START}, call
     * {@link #broadcastStartEvent() broadcastStartEvent} and then set
     * {@code event} to {@code WizardEvent.NOEVENT} to indicate that the wizard
     * has rendered at least once. Then call the renderer's {@code encodeBegin}
     * method.
     *
     * @param context This FacesContext for this request.
     * @throws IOException if an IO error occurs
     */
    @Override
    public void encodeBegin(final FacesContext context) throws IOException {

        if (context == null) {
            throw new NullPointerException();
        }

        if (!isRendered()) {
            return;
        }

        // Let the application know, that the wizard is about to
        // render, only the first time.
        // The application cannot abort the event.
        // Set the event to NOEVENT in case it is
        // server side state saving, so that the next cycle
        // won't broadcst the START event again.
        //
        // In client side state saving this is ensured during
        // processRestoreState
        if (event == WizardEvent.START) {
            broadcastStartEvent();
            event = WizardEvent.NOEVENT;
        }

        String rendererType = getRendererType();
        if (rendererType != null) {
            getRenderer(context).encodeBegin(context, this);
        }
    }

    /**
     * Begin the rendering of the wizard child components. This really means
     * that the WizardStep children are available. Call the
     * {@link WizardModelBase#initialize(Wizard) initialize)} method. Then call
     * the renderer's {@code encodeChildren} method.
     *
     * @param context This FacesContext for this request.
     * @throws IOException if an IO error occurs
     */
    @Override
    public void encodeChildren(final FacesContext context) throws IOException {

        if (context == null) {
            throw new NullPointerException();
        }

        // This is the only point where we can truly initialize
        // the WizardModelBase model when wizard steps are child tags.
        // However, this is called twice in client state saving.
        // It is called in restoreState to establish the state
        // before the wizard was rendered to the client.
        //
        // In fact is must be processed twice.
        // Once to restore, and once to reconstruct the
        // the tree based on any changes that might have
        // occured during the request processing, and
        // invoke application phase i.e. branches taken,
        // steps removed, etc.
        //
        getModel().initialize(this);

        if (!isRendered()) {
            return;
        }

        String rendererType = getRendererType();
        if (rendererType != null) {
            getRenderer(context).encodeChildren(context, this);
        }

    }

    /**
     * Complete the rendering of the wizard. Call the renderer's
     * {@code encodeChildren} method. If the wizard model's
     * {@link WizardModelBase#isComplete() isComplete} method returns true, call
     * the model's {@link WizardModelBase#complete()
     * complete} method to inform the model this wizard instance will no longer
     * invoke methods on that model instance during this wizard session.
     *
     * @param context This FacesContext for this request.
     * @throws IOException if an IO error occurs
     */
    @Override
    public void encodeEnd(final FacesContext context) throws IOException {

        if (context == null) {
            throw new NullPointerException();
        }

        if (!isRendered()) {
            return;
        }

        String rendererType = getRendererType();
        if (rendererType != null) {
            getRenderer(context).encodeEnd(context, this);
        }

        // If in server side state saving, this wizard instance is not
        // destroyed evertime the wizard ends, since the view roots
        // are cached.
        //
        // The event must be reset to START if this session
        // is completing, so that the next cycle, which will be the "first"
        // for that wizard session, sends the START event. Otherwise
        // set the NOEVENT state.
        //
        // In client side state saving this is ensured by
        // processRestoreState.
        if (isComplete()) {
            getModel().complete();
            // Let the application know that the wizard has completed
            // Should we delete the children here ? CR 6333872
            //
            broadcastCompleteEvent();
            event = WizardEvent.START;
        } else {
            event = WizardEvent.NOEVENT;
        }
    }

    /**
     * Returns an iterator of {@link WizardStepListItem WizardStepListItem}
     * instances that represent the steps for this wizard.
     * {@link WizardStepListItem WizardStepListItem} instances encapsulate
     * information about a step that is useful for rendering a list of steps.
     *
     * @return Iterator
     */
    public Iterator getStepListIterator() {
        return getModel().getWizardStepList().iterator();
    }

    /**
     * Return true if the wizard has completed all its steps.
     *
     * @return {@code boolean}
     */
    public boolean isComplete() {
        return getModel().isComplete();
    }

    /**
     * Return true if step is the current step, else false.
     *
     * @param step The step to check.
     * @return {@code boolean}
     */
    public boolean isCurrentStep(final WizardStep step) {
        return getModel().isCurrentStep(step);
    }

    /**
     * Return the step currently being performed.
     *
     * @return WizardStep
     */
    public WizardStep getCurrentStep() {
        return getModel().getCurrentStep();
    }

    /**
     * Return true if the wizard has help for steps. Typically, if false is
     * returned the wizard steps pane does not display Steps and Help tabs, just
     * the step list.
     *
     * @return {@code boolean}
     */
    public boolean hasStepHelp() {
        return getModel().hasStepHelp();
    }

    /**
     * Return true if the close button should be rendered for the current step,
     * else false. Typically this method returns true only for the results step.
     *
     * @return {@code boolean}
     */
    public boolean hasClose() {
        WizardModel wm = getModel();
        return wm.hasClose(wm.getCurrentStep());
    }

    /**
     * Return true if the next button should be rendered for the current step,
     * else false. Typically this method returns true for all steps except for
     * the finish or results steps.
     *
     * @return {@code boolean}
     */
    public boolean hasNext() {
        WizardModel wm = getModel();
        return wm.hasNext(wm.getCurrentStep());
    }

    /**
     * Return true if the previous button should be rendered for the current
     * step, else false. Typically this method returns true for all steps except
     * for the results steps.
     *
     * @return {@code boolean}
     */
    public boolean hasPrevious() {
        WizardModel wm = getModel();
        return wm.hasPrevious(wm.getCurrentStep());
    }

    /**
     * Return true if the finish button should be rendered for the current step,
     * else false. Typically this method returns true only for the finish step.
     *
     * @return {@code boolean}
     */
    public boolean hasFinish() {
        WizardModel wm = getModel();
        return wm.hasFinish(wm.getCurrentStep());
    }

    /**
     * Return true if the cancel button should be rendered for the current step,
     * else false. Typically this method returns true for all steps except for
     * the results step.
     *
     * @return {@code boolean}
     */
    public boolean hasCancel() {
        WizardModel wm = getModel();
        return wm.hasCancel(wm.getCurrentStep());
    }

    /**
     * Return true if the close button should be disabled for the current step,
     * else false.
     *
     * @return {@code boolean}
     */
    public boolean isCloseDisabled() {
        WizardModel wm = getModel();
        return wm.isCloseDisabled(wm.getCurrentStep());
    }

    /**
     * Return true if the next button should be disabled for the current step,
     * else false.
     *
     * @return {@code boolean}
     */
    public boolean isNextDisabled() {
        WizardModel wm = getModel();
        return wm.isNextDisabled(wm.getCurrentStep());
    }

    /**
     * Return true if the previous button should be disabled for the current
     * step, else false. Typically the first step of a sequence should return
     * true, since there usually isn't a step before the first step.
     *
     * @return {@code boolean}
     */
    public boolean isPreviousDisabled() {
        WizardModel wm = getModel();
        return wm.isPreviousDisabled(wm.getCurrentStep());
    }

    /**
     * Return true if the finish button should be disabled for the current step,
     * else false.
     *
     * @return {@code boolean}
     */
    public boolean isFinishDisabled() {
        WizardModel wm = getModel();
        return wm.isFinishDisabled(wm.getCurrentStep());
    }

    /**
     * Return true if the cancel button should be disabled for the current step,
     * else false.
     *
     * @return {@code boolean}
     */
    public boolean isCancelDisabled() {
        WizardModel wm = getModel();
        return wm.isCancelDisabled(wm.getCurrentStep());
    }

    /**
     * Returns true if the steps pane has Steps and Help tabs and the Steps tab
     * is selected. If the wizard has no step help return true.
     *
     * @return {@code boolean}
     */
    public boolean isStepsTabActive() {
        return stepTabActive || !hasStepHelp();
    }

    /**
     * Return the number of the current Step, to be used in a steps list.
     *
     * @return String
     */
    public String getCurrentStepNumberString() {
        // Due to the implementation of WizardStepListBase
        // this value is only valid if the WizardStepListBase
        // instance that is returned has been iterated over at
        // least once. Need to fix this.
        //
        return getModel().getWizardStepList().
                getCurrentStepNumberString();
    }

    // Sub components and action listeners.
    //
    // While it may appear that facets are supported, they
    // are not yet documented in the tld doc. More work
    // needs to be done to understand the handling of these
    // facets.
    /**
     * Return the {@code UIComponent} that will be used for the Cancel button.
     *
     * @return UIComponent
     */
    public UIComponent getCancelComponent() {
        return getButtonComponent(CANCEL_FACET, WIZARD_CANCEL,
                isCancelDisabled(),
                WizardEvent.CANCEL, true,
                getJavaScript(WizardEvent.CANCEL), false);
    }

    /**
     * Return the {@code UIComponent} that will be used for the Close button.
     *
     * @return UIComponent
     */
    public UIComponent getCloseComponent() {
        return getButtonComponent(CLOSE_FACET, WIZARD_CLOSE,
                isCloseDisabled(),
                WizardEvent.CLOSE, true,
                getJavaScript(WizardEvent.CLOSE), true);
    }

    /**
     * Return the {@code UIComponent} that will be used for the Finish button.
     *
     * @return UIComponent
     */
    public UIComponent getFinishComponent() {
        return getButtonComponent(FINISH_FACET, WIZARD_FINISH,
                isFinishDisabled(),
                WizardEvent.FINISH, true,
                getJavaScript(WizardEvent.FINISH), true);
    }

    /**
     * Return the {@code UIComponent} that will be used for the Next button.
     *
     * @return UIComponent
     */
    public UIComponent getNextComponent() {
        return getButtonComponent(NEXT_FACET, WIZARD_NEXT,
                isNextDisabled(),
                WizardEvent.NEXT, true,
                getJavaScript(WizardEvent.NEXT), true);
    }

    /**
     * Return the {@code UIComponent} that will be used for the Previous button.
     *
     * @return UIComponent
     */
    public UIComponent getPreviousComponent() {
        return getButtonComponent(PREVIOUS_FACET, WIZARD_PREVIOUS,
                isPreviousDisabled(),
                WizardEvent.PREVIOUS, true,
                getJavaScript(WizardEvent.PREVIOUS), false);
    }

    /**
     * If a facet by the name of {@code facetName} is returned by
     * {@code ComponentUtilities.getPrivateFacet}, return it. If not return an
     * initialized {@link Button Button} as appropriate for the specified
     * {@code facetName}.
     * <p>
     * {@code facetName} is one of {@code previous}, {@code next}, finish},
     * {@code cancel} or {@code close}.
     * </p>
     * <p>
     * The button's id: {@code this.getId() + "_" + facetName}
     * <p>
     * The {@code actionListener} is also registered with the facet component if
     * found and sets its disabled state as deemed by the wizard.
     * </p>
     * This is a private facet.
     *
     * @deprecated See {@link #getButtonComponent}
     *
     * @param facetName the role for the button to return.
     * @param textKey the theme text for the button text.
     * @param disabled true if this button should be disabled, else false.
     * @param wizardEvent the WizardEvent to broadcast.
     * @param immediate the button immediate value.
     * @param javascript the onclick JavaScript routine name for this button
     * @return UIComponent
     */
    protected UIComponent getButtonComponent(final String facetName,
            final String textKey, final boolean disabled, final int wizardEvent,
            final boolean immediate, final String javascript) {

        // Primary button only.
        return getButtonComponent(facetName,
                textKey, disabled, wizardEvent, immediate, javascript, true);
    }

    // Need to create this method because the previous original method
    // cannot be changed and buttons must be created either primary
    // or not. The primary buttons are the Next, Finish, and Close
    // buttons.
    /**
     * If a facet by the name of {@code facetName} is returned by
     * {@code ComponentUtilities.getPrivateFacet}, re-initialize it and return
     * it. If not create and return an initialized {@link Button Button} as
     * appropriate for the specified {@code facetName}.
     * <p>
     * {@code facetName} is one of {@code previous}, {@code next}, finish},
     * {@code cancel} or {@code close}.
     * </p>
     * <p>
     * The button's id: {@code this.getId() + "_" + facetName}
     * <p>
     * A {@link WizardActionListener WizardActionListener} is also registered
     * with the facet component if created and sets its disabled state as deemed
     * by the wizard.
     * </p>
     * This is a private facet.
     *
     * @param facetName the role for the button to return.
     * @param textKey the theme text for the button text.
     * @param disabled true if this button should be disabled, else false.
     * @param wizardEvent the WizardEvent for the listener to process.
     * @param immediate the button immediate value.
     * @param javascript the onclick JavaScript routine name for this button
     * @param primary make the button a primary button, if true.
     * @return UIComponent
     */
    protected UIComponent getButtonComponent(final String facetName,
            final String textKey, final boolean disabled,
            final int wizardEvent, final boolean immediate,
            final String javascript, final boolean primary) {

        // We know its a Button
        Button button = (Button) ComponentUtilities
                .getPrivateFacet(this, facetName, true);
        if (button == null) {

            button = new Button();
            button.setId(
                    ComponentUtilities.createPrivateFacetId(this, facetName));
            button.setImmediate(immediate);
            button.setPrimary(primary);
            button.addActionListener(
                    new WizardActionListener(getId(), wizardEvent));

            ComponentUtilities.putPrivateFacet(this, facetName, button);
        }

        Theme theme = ThemeUtilities.getTheme(getFacesContext());

        // Set every time for locale changes
        button.setText(theme.getMessage(textKey));
        button.setDisabled(disabled);

        if (javascript != null) {
            button.setOnClick(javascript);
        }
        return button;
    }

    // FIXME: Need to return the component unconditionally
    // and let the caller determine if "hasStepHelp" has
    // any influence.
    /**
     * Return a component that is rendered in the Steps pane. If the wizard
     * steps do not provide help, null is returned.
     *
     * @return UIComponent
     */
    public UIComponent getTabsComponent() {

        // need to determine if there will be a tab component
        // If there is not help support there will be just
        // a steps list.
        // There is no help support if no step has help, or
        // the help has been turned off for the wizard.
        if (getModel().hasStepHelp()) {
            return getTabsComponent(TABS_FACET);
        }
        return null;
    }

    /**
     * If a facet by the name of {@code facetName} is returned by
     * {@code ComponentUtilities.getPrivateFacet}, re-initialize it and return
     * it. If not, create and return an initialized {@link TabSet TabSet} as
     * appropriate for the tabs in the steps pane.
     * <p>
     * The tabset's id is: {@code this.getId() + "_" + facetName}
     * <p>
     * This is a private facet.
     * </p>
     *
     * @param facetName the tab set facet to return.
     * @return UIComponent
     */
    protected UIComponent getTabsComponent(final String facetName) {

        Theme theme = ThemeUtilities.getTheme(getFacesContext());

        // We know it's a TabSet
        TabSet tabSet = (TabSet) ComponentUtilities
                .getPrivateFacet(this, facetName, true);
        if (tabSet == null) {

            tabSet = new TabSet();
            // Does this have any effect ?
            tabSet.setImmediate(true);
            tabSet.setId(
                    ComponentUtilities.createPrivateFacetId(this, facetName));
            // Mini tabs
            tabSet.setMini(true);

            List<UIComponent> tabs = tabSet.getChildren();

            Tab steptab = getTab(theme, STEP_TAB, WIZARD_STEP_TAB,
                    WIZARD_TAB_TOOLTIP,
                    WizardEvent.STEPSTAB, stepTabActive, true,
                    getJavaScript(WizardEvent.STEPSTAB));
            tabs.add(steptab);

            Tab helptab = getTab(theme, HELP_TAB, WIZARD_HELP_TAB,
                    WIZARD_TAB_TOOLTIP,
                    WizardEvent.HELPTAB, !stepTabActive, true,
                    getJavaScript(WizardEvent.HELPTAB));

            tabs.add(helptab);
            String selectedId;
            if (stepTabActive) {
                selectedId = steptab.getId();
            } else {
                selectedId = helptab.getId();
            }
            tabSet.setSelected(selectedId);
            ComponentUtilities.putPrivateFacet(this, facetName, tabSet);
            return tabSet;
        }

        // Update the existing TabSet private facet
        List tabs = tabSet.getChildren();
        Iterator itabs = tabs.iterator();
        String stepsId = ComponentUtilities.createPrivateFacetId(this,
                STEP_TAB);
        String helpId = ComponentUtilities.createPrivateFacetId(this,
                HELP_TAB);
        while (itabs.hasNext()) {
            UIComponent tab = (UIComponent) itabs.next();
            if (!(tab instanceof Tab)) {
                continue;
            }
            String tabid = tab.getId();
            if (stepsId.equals(tabid)) {
                initTab((Tab) tab, theme, WIZARD_STEP_TAB,
                        WIZARD_TAB_TOOLTIP, stepTabActive, true,
                        getJavaScript(WizardEvent.STEPSTAB));
            } else if (helpId.equals(tabid)) {
                initTab((Tab) tab, theme, WIZARD_HELP_TAB,
                        WIZARD_TAB_TOOLTIP, !stepTabActive, true,
                        getJavaScript(WizardEvent.HELPTAB));
            }
        }
        String selectedId;
        if (stepTabActive) {
            selectedId = stepsId;
        } else {
            selectedId = helpId;
        }
        tabSet.setSelected(selectedId);
        return tabSet;
    }

    /**
     * Return a {@link Tab Tab} component initialized with the parameters.
     * <p>
     * The id is formed from {@code this.getId() + "_" + facetName}. Facet name
     * is {@code stptb} or {@code hlptb}.
     * </p>
     * <p>
     * A {@link WizardActionListener WizardActionListener} is registered with
     * the component.
     * </p>
     *
     * @param theme Theme instance to obtain text for labels.
     * @param facetName Append this to the tab's id.
     * @param text The tab's text.
     * @param toolTip The tab's tooltip.
     * @param wizardEvent the WizardEvent for the listener to process.
     * @param selected The value is true if this tab is initially selected.
     * @param immediate the tab immediate value.
     * @param javascript the onclick JavaScript routine name for this tab
     * @return Tab
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    protected Tab getTab(final Theme theme, final String facetName,
            final String text, final String toolTip, final int wizardEvent,
            final boolean selected, final boolean immediate,
            final String javascript) {

        Tab tab = new Tab();
        tab.setId(ComponentUtilities.createPrivateFacetId(this, facetName));
        tab.addActionListener(new WizardActionListener(getId(), wizardEvent));
        initTab(tab, theme, text, toolTip, selected, immediate, javascript);
        return tab;
    }

    /**
     * Initialize a tab.
     *
     * @param tab The Tab instance to initialize.
     * @param theme Theme instance to obtain text for labels.
     * @param text The tab's text.
     * @param toolTip The tab's tool tip.
     * @param selected The value is true if this tab is initially selected.
     * @param immediate the tab immediate value.
     * @param javascript the onclick JavaScript routine name for this tab
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    private void initTab(final Tab tab, final Theme theme, final String text,
            final String toolTip, final boolean selected,
            final boolean immediate, final String javascript) {

        String zText = theme.getMessage(text);
        tab.setText(zText);
        if (selected) {
            tab.setToolTip(theme.getMessage(toolTip, new Object[]{zText}));
        } else {
            tab.setToolTip(zText);
        }
        tab.setImmediate(immediate);
        if (javascript != null) {
            tab.setOnClick(javascript);
        }
    }

    /**
     * Return a component that implements the current step indicator icon. If a
     * facet named {@code stepIndicator} is found that component is returned.
     * Otherwise an {@link Icon Icon} component is returned. It is assigned the
     * id {@code getId() + "_stepIndicator"}
     * <p>
     * If the facet is not defined then the returned {@link Icon Icon} component
     * is re-initialized every time this method is called. The icon image is
     * {@code ThemeImages.WIZARD_ARROW}
     * </p>
     *
     * @return a current step indicator icon
     */
    public UIComponent getStepIndicatorComponent() {

        UIComponent child = (UIComponent) getFacet(INDICATOR_FACET);
        if (child != null) {
            return child;
        }

        Theme theme = ThemeUtilities.getTheme(getFacesContext());

        // It needs to be created every time in case it
        // is localized.
        Icon icon = ThemeUtilities.getIcon(theme, ThemeImages.WIZARD_ARROW);
        String id = ComponentUtilities
                .createPrivateFacetId(this, INDICATOR_FACET);
        icon.setId(id);
        icon.setParent(this);
        // Should be part of Theme
        //CHECKSTYLE:OFF
        icon.setHspace(4);
        //CHECKSTYLE:ON

        String toolTip = theme.getMessage(WIZARD_CURRENT_STEP_ALT);
        icon.setToolTip(toolTip);
        icon.setAlt(toolTip);
        return icon;
    }

    // This component is a child and not a facet.
    // I'm not exactly sure why, but I was reluctant to change it
    // during the "private" facet edits.
    /**
     * Return an existing or create a {@link Hyperlink Hyperlink} component to
     * be used as a step link in the steps list. The id for the created
     * component is: {@code step.getId() + "_stplnk" + "_" + stepId().} The
     * appended step id is used to identify the step the wizard should navigate
     * to. An {@link WizardActionListener WizardActionListener} is added to the
     * component if it is created.
     *
     * @param id The component id as described above.
     * @param text The Hylerlink text, typically WizardStep.getSummaryText.
     * @param wizardEvent the WizardEvent for the listener to process.
     * @param immediate the component's immediate value.
     * @param javascript the onclick JavaScript routine name for this component
     * @return UIComponent
     */
    public UIComponent getSteplinkComponent(final String id, final String text,
            final int wizardEvent, final boolean immediate,
            final String javascript) {

        // Need a way to prune these hyperlink chidren when the
        // step list changes drastically
        //
        // We know it's a hyperlink
        Hyperlink hlink = (Hyperlink) ComponentUtilities
                .findChild(this, id, null);
        if (hlink == null) {
            hlink = new Hyperlink();
            hlink.setId(id);
            hlink.addActionListener(new WizardActionListener(
                    getId(), wizardEvent));
            hlink.setImmediate(immediate);
            getChildren().add(hlink);
        }

        hlink.setText(text);
        if (javascript != null) {
            hlink.setOnClick(javascript);
        }
        return hlink;
    }

    // These components are potential facet candidates.
    // Not currently documented, the WizardRenderer does
    // attempt to aquire them and render them. But its not
    // clear if these are really necessary or useful.
    //
    /**
     * Return a component to represent the left pane in the wizard that
     * typically contains the steps list. This {@code Wizard} implementation
     * returns null.
     *
     * @return UIComponent
     */
    public UIComponent getStepsPaneComponent() {
        return null;
    }

    /**
     * Return a component to represent the wizards's top bar that typically
     * contains the Steps and Help tabs. This {@code Wizard} implementation
     * returns null.
     *
     * @return UIComponent
     */
    public UIComponent getStepsBarComponent() {
        return null;
    }

    /**
     * Return a that represents the steps list, rendered in the Steps tab. This
     * {@code Wizard} implementation returns null.
     *
     * @return UIComponent
     */
    public UIComponent getStepListComponent() {
        return null;
    }

    /**
     * Return a component that represents the step help for the current step,
     * rendered in the Help tab. This {@code Wizard} implementation returns
     * null.
     *
     * @return UIComponent
     */
    public UIComponent getStepHelpComponent() {
        return null;
    }

    /**
     * Return a component that represents the step title. The component is
     * rendered in the wizard above the step's components. This {@code Wizard}
     * implementation returns null.
     *
     * @return UIComponent
     */
    public UIComponent getStepTitleComponent() {
        return null;
    }

    /**
     * Return a component that represents the step detail or step instruction,
     * rendered below the step title. This {@code Wizard} implementation returns
     * null.
     *
     * @return UIComponent
     */
    public UIComponent getStepDetailComponent() {
        return null;
    }

    /**
     * Return a component that represents the components that comprise the
     * current wizard step. This {@code Wizard} implementation returns null.
     *
     * @return UIComponent
     */
    public UIComponent getTaskStepComponent() {
        return null;
    }

    /**
     * Return a component that represents the entire right pane including the
     * step title, detail and components. This {@code Wizard} implementation
     * returns null.
     *
     * @return UIComponent
     */
    public UIComponent getTaskComponent() {
        return null;
    }

    /**
     * Return a component that represents the step title and detail for the
     * current step. This {@code Wizard} implementation returns null.
     *
     * @return UIComponent
     */
    public UIComponent getTaskHeaderComponent() {
        return null;
    }

    /**
     * Return a component that represents the control bar or navigation
     * controls, rendered after the step's components. This {@code Wizard}
     * implementation returns null.
     *
     * @return UIComponent
     */
    public UIComponent getControlBarComponent() {
        return null;
    }

    /**
     * Return a component that is rendered for the controls justified to the
     * left below the step's components. Typically these controls are the
     * previous and next or finish buttons. This {@code Wizard} implementation
     * returns null.
     *
     * @return UIComponent
     */
    public UIComponent getLeftControlBarComponent() {
        return null;
    }

    /**
     * Return a component that is rendered for the controls justified to the
     * right, below the step's components. Typically the control is the cancel
     * or close buttons. This {@code Wizard} implementation returns null.
     *
     * @return UIComponent
     */
    public UIComponent getRightControlBarComponent() {
        return null;
    }

    // Having facets for some of these StaticText elements
    // would be problematic.
    // Do we just accept the facet component as it is with
    // the expectation that the content is correct ?
    // Or should we overwrite the content ?
    // For example using a facet for the static text step
    // number. We would look for only one facet and replace
    // the content with a particular step.
    // Or the component that represents the previous step link.
    // Its content would change, like id and content.
    /**
     * Return the component to render the step title label. The id of this
     * component is: {@code getCurrentStep().getId() + "_stpttllbl"}. This
     * component also includes the step number. Typically renders "Step 1:".
     * <p>
     * This {@code Wizard} implementation returns a
     * {@link StaticText StaticText} component that is created and initialized
     * every time this method is called.
     * </p>
     *
     * @return UIComponent
     */
    public UIComponent getStepTitleLabelTextComponent() {
        Theme theme = ThemeUtilities.getTheme(getFacesContext());
        return getStepStaticTextComponent(getCurrentStep(), STEP_TITLE_LABEL,
                theme.getMessage(WIZARD_STEP_TITLE_LABEL,
                        new Object[]{getCurrentStepNumberString()}));
    }

    /**
     * Return the component to render the step title text. The id of this
     * component is: {@code getCurrentStep().getId() + "_stpttl"}. This
     * component typically renders after the step title label.
     * <p>
     * This {@code Wizard} implementation returns a
     * {@link StaticText StaticText} component that is created and initialized
     * every time this method is called.
     * </p>
     *
     * @return UIComponent
     */
    public UIComponent getStepTitleTextComponent() {
        WizardStep step = getCurrentStep();
        return getStepStaticTextComponent(step, STEP_TITLE, step.getTitle());
    }

    /**
     * Return the component to render the step detail text. The id of this
     * component is: {@code getCurrentStep().getId() + "_stpdtl"}. This
     * component typically renders below the step title.
     * <p>
     * This {@code Wizard} implementation returns a
     * {@link StaticText StaticText} component that is created and initialized
     * every time this method is called.
     * </p>
     *
     * @return UIComponent
     */
    public UIComponent getStepDetailTextComponent() {
        WizardStep step = getCurrentStep();
        return getStepStaticTextComponent(step, STEP_DETAIL, step.getDetail());
    }

    /**
     * Return the component to render for the step summary text that appears in
     * the step list.
     * <p>
     * This {@code Wizard} implementation returns either:
     * <p>
     * <ul>
     * <li>a {@link StaticText StaticText} component for the current and future
     * steps, created and initialized every time this method. The component id
     * is: {@code step.getId() + "_stpsmmy"}.
     * </li>
     * <li>a {@link Hyperlink Hyperlink} component for visited steps. The
     * component is re-initialized every time this method is called. The
     * component id is: {@code step.getId() + "_stplnk" + "_" + step.getId()}.
     * The right most "step.getId()" text is interpreted in event handlers to
     * identify the step to navigate to.
     * </li>
     * </ul>
     * </p>
     *
     * @param step The step being rendered.
     * @return UIComponent
     */
    public UIComponent getStepSummaryComponent(final WizardStep step) {
        String stepId = step.getId();
        if (getModel().canGotoStep(step)) {
            String id = ComponentUtilities.createPrivateFacetId(this,
                    STEP_LINK);
            id = id.concat(USCORE).concat(stepId);
            return getSteplinkComponent(id, step.getSummary(),
                    WizardEvent.GOTOSTEP, true,
                    getJavaScript(WizardEvent.GOTOSTEP));
        } else {
            return getStepStaticTextComponent(step, STEP_SUMMARY,
                    step.getSummary());
        }
    }

    /**
     * Return the component to render for a branch step's placeholder text that
     * appears in the step list. The component id is:
     * {@code step.getId() + "_stpplhld"}.
     * <p>
     * This {@code Wizard} implementation returns a
     * {@link StaticText StaticText} component that is created and initialized
     * every time this method is called.
     * </p>
     *
     * @param step The step being rendered.
     * @return UIComponent
     */
    public UIComponent getStepPlaceholderTextComponent(final WizardStep step) {

        Theme theme = ThemeUtilities.getTheme(getFacesContext());
        String placeholderText = ((WizardBranch) step).getPlaceholderText();

        return getStepStaticTextComponent(step, STEP_PLACEHLDR,
                theme.getMessage(WIZARD_PLACEHOLDER_TEXT,
                        new Object[]{placeholderText}));
    }

    /**
     * Return the component to render for the step number that appears in the
     * step list.
     * <p>
     * This {@code Wizard} implementation returns either:
     * <p>
     * <ul>
     * <li>a {@link StaticText StaticText} component for the current and future
     * steps, created and initialized every time this method is called. The id
     * of this component is: {@code step.getId() + "_stpnum"}.
     * </li>
     * <li>a {@link Hyperlink Hyperlink} component for visited steps. The
     * component is re-initialized every time this method is called. The
     * component id is:
     * {@code step.getId() + "_num" + "_stplnk" + "_" + step.getId()}. The right
     * most "step.getId()" text is interpreted in event handlers to identify the
     * step to navigate to.
     * </li>
     * </ul>
     * </p>
     *
     * @param step The step being rendered.
     * @param numberString The step number.
     * @return UIComponent
     */
    public UIComponent getStepNumberComponent(final WizardStep step,
            final String numberString) {

        String stepId = step.getId();
        if (getModel().canGotoStep(step)) {
            String numStpLnk = NUM.concat(USCORE).concat(STEP_LINK);
            String id = ComponentUtilities.createPrivateFacetId(this,
                    numStpLnk);
            id = id.concat(USCORE).concat(stepId);
            return getSteplinkComponent(id, numberString,
                    WizardEvent.GOTOSTEP, true,
                    getJavaScript(WizardEvent.GOTOSTEP));
        } else {
            return getStepStaticTextComponent(step, STEP_NUM, numberString);
        }
    }

    /**
     * Return the component to render for the step help text for the current
     * step in the steps pane help tab. The id of this component is:
     * {@code step.getId() + "_stphlp"}.
     * <p>
     * This {@code Wizard} implementation returns a
     * {@link StaticText StaticText} component that is created and initialized
     * every time this method is called.
     * </p>
     *
     * @return UIComponent
     */
    public UIComponent getStepHelpTextComponent() {

        WizardStep step = getCurrentStep();
        UIComponent stepHelp = getStepStaticTextComponent(getCurrentStep(),
                STEP_HELP, step.getHelp());

        // Allow HTML markup
        if (stepHelp != null) {
            stepHelp.getAttributes().put(ESCAPE_ATTR, WIZARD_FALSE);
        }
        return stepHelp;
    }

    /**
     * Return the component to render for the wizard title. The id of this
     * component is: {@code this.getId() + "_title"}.
     * <p>
     * This {@code Wizard} implementation returns a
     * {@link StaticText StaticText} component that is created and initialized
     * every time this method is called.
     * </p>
     *
     * @return UIComponent
     */
    public UIComponent getTitleComponent() {
        return getStaticTextComponent(TITLE_FACET, getTitle());
    }

    /**
     * Return the component to render for the steps pane title text when there
     * is no step help and therefore no tabs. The id of this component is:
     * {@code step().getId() + "_stepsPaneTitle"}.
     * <p>
     * This {@code Wizard} implementation returns a
     * {@link StaticText StaticText} component that is created and initialized
     * every time this method is called.
     * </p>
     *
     * @return UIComponent
     */
    public UIComponent getStepsPaneTitleComponent() {
        Theme theme = ThemeUtilities.getTheme(getFacesContext());
        String paneTitle = theme.getMessage(WIZARD_STEPS_PANE_TITLE);
        UIComponent stepsPaneTitle = getStaticTextComponent(
                STEPS_PANE_TITLE, paneTitle);
        return stepsPaneTitle;
    }

    /**
     * Return a component for various wizard text features. If a facet exists by
     * the name of {@code facetName} return it. Otherwise create, initialize and
     * return a {@link StaticText StaticText} component every time this method
     * is called. The component id is: {@code this.getId() + "_" + facetName}.
     *
     * <em>The facet names are not publicly documented facets.</em>
     *
     * @param facetName The name of a facet to return.
     * @param text The text to assign to the text property of a created
     * component.
     * @return UIComponent
     */
    protected UIComponent getStaticTextComponent(final String facetName,
            final String text) {

        UIComponent child = (UIComponent) getFacet(facetName);
        if (child != null) {
            return child;
        }

        StaticText txt = new StaticText();
        String id = ComponentUtilities.createPrivateFacetId(this, facetName);
        txt.setId(id);
        txt.setText(text);
        txt.setParent(this);

        return txt;
    }

    /**
     * Return a component for rendering text associated with a step. The
     * component id is: {@code step.getId() + "_" + facetName}
     * <p>
     * This {@code Wizard} implementation returns a
     * {@link StaticText StaticText} component that is created and initialized
     * every time this method is called.
     * </p>
     * <em>The facet names are not publicly documented facets.</em>
     *
     * @param step the WizardStep
     * @param facetName The facetName of the component to return.
     * @param stepText The text to assign to the text property of a created
     * component.
     * @return UIComponent
     */
    protected UIComponent getStepStaticTextComponent(final WizardStep step,
            final String facetName, final String stepText) {

        StaticText text = new StaticText();
        text.setId(ComponentUtilities.createPrivateFacetId(step, facetName));
        text.setParent(this);
        text.setText(stepText);
        return text;
    }

    /**
     * Return a StaticText component for rendering various wizard text. The
     * default implementation returns a new StaticText component instance, each
     * time this method is called.
     *
     * @deprecated
     * @return UIComponent
     */
    protected UIComponent getStaticTextComponent() {
        return new StaticText();
    }

    /**
     * Return a component for rendering text associated with a step. Create,
     * initialize and return a StaticText component every time this method is
     * called.
     *
     * @param id The id of the component to return.
     * @param stepText The text to assign to the text property of a created
     * component.
     *
     * @deprecated replaced by {@link #getStepStaticTextComponent(WizardStep,
     * String) getStaticTextComponent}
     * @return UIComponent
     */
    protected UIComponent getStepStaticTextComponent(final String id,
            final String stepText) {

        StaticText text = new StaticText();
        text.setId(id);
        text.setParent(this);
        text.setText(stepText);
        return text;
    }

    // Event handling
    /**
     * Return the {@code Wizard} instance ancestor of {@code child} identified
     * by {@code wizardId}.
     *
     * @param child A descendant of the desired Wizard.
     * @param wizardId The id of a Wizard ancestor.
     * @return Wizard
     */
    public static Wizard getWizard(final UIComponent child,
            final String wizardId) {

        return (Wizard) findAncestor(child, wizardId);
    }

    /**
     * Return the {@code UIComponent} instance ancestor of {@code descendant}
     * identified by {@code ancestorId}.
     *
     * @param descendant A descendant of the desired UIComponent.
     * @param ancestorId The id of a Wizard ancestor.
     * @return UIComponent
     */
    private static UIComponent findAncestor(final UIComponent descendant,
            final String ancestorId) {

        if (ancestorId == null || descendant == null) {
            return null;
        }
        UIComponent parent = descendant.getParent();
        while (parent != null) {
            if (ancestorId.equals(parent.getId())) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * Called from {@link WizardActionListener WizardActionListener} to
     * establish the event state of the wizard.
     *
     * @param source The UIComponent originating the even.
     * @param evt One of:
     * <ul>
     * <li>{@link WizardEvent#CANCEL WizardEvent.CANCEL}</li>
     * <li>{@link WizardEvent#CLOSE WizardEvent.CLOSE}</li>
     * <li>{@link WizardEvent#FINISH WizardEvent.FINISH}</li>
     * <li>{@link WizardEvent#GOTOSTEP WizardEvent.GOTOSTEP}</li>
     * <li>{@link WizardEvent#HELPTAB WizardEvent.HELPTAB}</li>
     * <li>{@link WizardEvent#NEXT WizardEvent.NEXT}</li>
     * <li>{@link WizardEvent#PREVIOUS WizardEvent.PREVIOUS}</li>
     * <li>{@link WizardEvent#STEPSTAB WizardEvent.STEPSTAB}</li>
     * </ul>
     * @throws AbortProcessingException if an error occurs
     */
    public void broadcastEvent(final UIComponent source, final int evt)
            throws AbortProcessingException {

        this.event = evt;
        this.eventSource = source;
        gotoStepId = null;

        switch (evt) {
            case WizardEvent.NEXT:
            case WizardEvent.PREVIOUS:
            case WizardEvent.CANCEL:
            case WizardEvent.CLOSE:
            case WizardEvent.FINISH:
                // Nothing special
                break;
            case WizardEvent.GOTOSTEP:
                // Need to identify the component id suffix by capturing
                // the text after "STEP_LINK + USCORE"
                String id = source.getId();
                String suffix = STEP_LINK.concat(USCORE);
                gotoStepId = id.substring(id.lastIndexOf(suffix)
                        + suffix.length());
                break;
            case WizardEvent.HELPTAB:
                stepTabActive = false;
                break;
            case WizardEvent.STEPSTAB:
                stepTabActive = true;
                break;
            default:
                break;
        }

        // Don't want the application's action listener to fire here
        // so we defeat the render complete of an immediate action.
        throw new AbortProcessingException();
    }

    /**
     * Send a {@link WizardEvent#START WizardEvent.START} event to the
     * component's {@link WizardEventListener WizardEventListener}. This event
     * will be sent in the encodeBegin method, before rendering is begun. The
     * return value from the listener's handleEvent and exceptions are ignored.
     */
    protected void broadcastStartEvent() {
        WizardEventListener listener = getEventListener();
        if (listener != null) {
            WizardEvent wizardEvent = new WizardEvent(this,
                    null, WizardEvent.START, null, null);
            try {
                listener.handleEvent(wizardEvent);
            } catch (AbortProcessingException e) {
                // ignore, nothing to do
                // FIXME: Log the exception.
            }
        }
    }

    /**
     * Send a {@link WizardEvent#COMPLETE WizardEvent.COMPLETE} event to the
     * component's {@link WizardEventListener WizardEventListener}. This event
     * will be sent in the encodeEnd method, after the renderer's encodeEnd
     * method returns. The return value from the listener's handleEvent and
     * exceptions are ignored.
     */
    protected void broadcastCompleteEvent() {
        WizardEventListener listener = getEventListener();
        if (listener != null) {
            WizardEvent wizardEvent = new WizardEvent(this,
                    null, WizardEvent.COMPLETE, null, null);
            try {
                listener.handleEvent(wizardEvent);
            } catch (Exception e) {
                // ignore, nothing to do
                // FIXME: Log the exception.
            }
        }

    }

    /**
     * This method broadcasts a {@link WizardEvent WizardEvent} event that is
     * queued as a result of an immediate {@code ActionEvent} from one of the
     * wizard's navigation components. If {@code facesEvent} is not an instance
     * of {@link WizardEvent WizardEvent} the method just returns.
     * <p>
     * If {@code facesEvent} is an instance of {@link WizardEvent WizardEvent} a
     * new {@link WizardEvent WizardEvent} is constructed with addtional
     * information based on the navigation event and the wizard state,
     * including:
     * </p>
     * <p>
     * <ul>
     * <li>the wizard instance as the source of the event
     * {@link WizardEvent#getSource() WizardEvent.getSource}</li>
     * <li>the current step, which may be null
     * {@link WizardEvent#getStep() WizardEvent.getStep}</li>
     * <li>the button or link component instance that triggered the original
     * immediate action event
     * {@link WizardEvent#getEventSource() WizardEvent.getEventSource}</li>
     * <li>the {@link WizardEvent WizardEvent} constant that identifies the
     * event {@link WizardEvent#getEvent() WizardEvent.getEvent}.</li>
     * <li>the next {@link WizardStep WizardStep} component id if the event is
     * {@code WizardEvent.GOTOSTEP}
     * {@link WizardEvent#getGotoStepId() WizardEvent.getgetGotoStepId},
     * otherwise null.</li>
     * </ul>
     * </p>
     * <p>
     * The event is then broadcast to
     * {@link WizardEventListener WizardEventListeners} configured on the
     * current step and the wizard, and then forwarded to the wizard model in
     * the following manner:
     * <p>
     * <ol>
     * <li>If the current step is not null and its
     * {@link WizardStep#getEventListener() getEventListener} method does not
     * return null, the
     * {@link WizardEventListener#handleEvent(WizardEvent) handleEvent} method
     * is called on the returned instance.
     * </li>
     * <li>If the step listener's handleEvent method does not throw an exception
     * the wizard's {@link Wizard#getEventListener() getEventListner} is called
     * and if it returns a non null instance, its
     * {@link WizardEventListener#handleEvent(WizardEvent) handleEvent} method
     * is called.
     * </li>
     * <li>If no exception occurs and the last listener called returns
     * {@code true}, the event is forwarded to the wizard model. Note that if
     * both listeners exist both listeners are sent the event, even if the
     * step's listener returns false. This means that if the wizard's listener
     * returns {@code true}, since it is called last, the event is forwarded to
     * the wizard model. If the last listener called returns {@code false} the
     * event is not forwarded to the model and the current step will be
     * redisplayed.
     * </li>
     * </ol>
     * If either listener's
     * {@link WizardEventListener#handleEvent(WizardEvent) handleEvent} method
     * throws an exception the event is changed to
     * {@link WizardEvent#CANCEL WizardEvent.CANCEL} and forwarded to the wizard
     * model, effectively ending the wizard session, as if the user had clicked
     * the cancel button.
     *
     * @param facesEvent must be an instance of {@link WizardEvent WizardEvent}
     */
    @Override
    public void broadcast(final FacesEvent facesEvent)
            throws AbortProcessingException {

        // Perform standard superclass processing
        // Iterates over "listeners".
        if (facesEvent instanceof WizardEvent) {

            boolean result = true;
            WizardStep step = getCurrentStep();
            // Check for null step.
            // Shouldn't happen.
            // FIXME: Should log this situation
            WizardEventListener listener;
            if (step == null) {
                listener = null;
            } else {
                listener = step.getEventListener();
            }

            WizardEvent wizardEvent = new WizardEvent(this,
                    eventSource, event, step, gotoStepId);
            try {
                if (listener != null) {
                    result = listener.handleEvent(wizardEvent);
                }
                listener = getEventListener();
                if (listener != null) {
                    result = listener.handleEvent(wizardEvent);
                }
            } catch (AbortProcessingException e) {
                result = true;
                // Make sure we update the wizard's idea of the event.
                event = WizardEvent.CANCEL;
                wizardEvent.setEvent(event);
            }
            // If there are no listeners, always let the model
            // handle the event.
            if (result) {
                getModel().handleEvent(wizardEvent);
            }
        }

    }

    // This should be in the renderer
    // WizardStep js takes precedence over Wizard js.
    // This makes sense if Wizard js is an alternative to
    // assigning js for every step. If the developer wants
    // the Wizard js also, then they can incorporate it into the
    // step js.
    /**
     * Return the JS method for the {@code javaScriptEvent}. This is effectively
     * the {@code "onclick"} handler for the buttons and hyperlinks. The wizard
     * step's JS takes precedence over the wizard's JS.
     * @param javaScriptEvent event id
     * @return String
     */
    private String getJavaScript(final int javaScriptEvent) {

        String js = null;
        WizardStep step = getCurrentStep();
        switch (javaScriptEvent) {
            case WizardEvent.NEXT:
                js = step.getOnNext();
                if (js == null) {
                    js = getOnNext();
                }
                break;
            case WizardEvent.PREVIOUS:
                js = step.getOnPrevious();
                if (js == null) {
                    js = getOnPrevious();
                }
                break;
            case WizardEvent.CANCEL:
                js = step.getOnCancel();
                if (js == null) {
                    js = getOnCancel();
                }
                break;
            case WizardEvent.CLOSE:
                js = step.getOnClose();
                if (js == null) {
                    js = getOnClose();
                }
                break;
            case WizardEvent.FINISH:
                js = step.getOnFinish();
                if (js == null) {
                    js = getOnFinish();
                }
                break;
            case WizardEvent.GOTOSTEP:
                js = step.getOnStepLink();
                if (js == null) {
                    js = getOnStepLink();
                }
                break;
            case WizardEvent.HELPTAB:
                js = step.getOnHelpTab();
                if (js == null) {
                    js = getOnHelpTab();
                }
                break;
            case WizardEvent.STEPSTAB:
                js = step.getOnStepsTab();
                if (js == null) {
                    js = getOnStepsTab();
                }
                break;
            default:break;
        }
        return js;
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

    /**
     * This implementation invokes {@code super.setRendered}.
     * @param rendered rendered flag
     */
    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * The eventListener attribute is used to specify an object to handle an
     * event that is triggered when a user activates a component in the wizard.
     * The eventListener attribute value must be a JavaServer Faces EL
     * expression that resolves to an instance of
     * {@code com.sun.webui.jsf.event.WizardEventListener}.
     * <p>
     * The return value of the wizard component's call to the event listener's
     * handleEvent() method controls the processing of the current step that is
     * being performed, and determines whether the next step or a previous step,
     * etc. can be navigated to.
     * </p>
     * See the <a href="#EventListeners">Event Listeners</a> section also.
     *
     * @return WizardEventListener
     */
    public WizardEventListener getEventListener() {
        if (this.eventListener != null) {
            return this.eventListener;
        }
        ValueExpression vb = getValueExpression("eventListener");
        if (vb != null) {
            return (WizardEventListener) vb
                    .getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The eventListener attribute is used to specify an object to handle an
     * event that is triggered when a user activates a component in the wizard.
     * The eventListener attribute value must be a JavaServer Faces EL
     * expression that resolves to an instance of
     * {@code com.sun.webui.jsf.event.WizardEventListener}.
     * <p>
     * The return value of the wizard component's call to the event listener's
     * handleEvent() method controls the processing of the current step that is
     * being performed, and determines whether the next step or a previous step,
     * etc. can be navigated to.
     * </p>
     * See the <a href="#EventListeners">Event Listeners</a> section also.
     *
     * @see #getEventListener()
     * @param newEventListener eventListener
     */
    public void setEventListener(final WizardEventListener newEventListener) {
        this.eventListener = newEventListener;
    }

    /**
     * The wizard is being targeted to a popup window. Default is {@code true}.
     * Set this property to {@code false} if the wizard is to appear within a
     * main browser window.
     *
     * @return {@code boolean}
     */
    public boolean isIsPopup() {
        if (this.isPopupSet) {
            return this.isPopup;
        }
        ValueExpression vb = getValueExpression("isPopup");
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
     * The wizard is being targeted to a popup window.Default is {@code true}.
     * Set this property to {@code false} if the wizard is to appear within a
     * main browser window.
     *
     * @param newIsPopup isPopup
     * @see #isIsPopup()
     */
    public void setIsPopup(final boolean newIsPopup) {
        this.isPopup = newIsPopup;
        this.isPopupSet = true;
    }

    /**
     * The {@code model} property is a value binding that resolves to an
     * instance of {@code WizardModel}.This instance is an alternative to the
     * default {@code WizardModelBase} instance that controls the flow of steps
     * in the wizard.
     *
     * @param newModel model
     * @see #getModel()
     */
    public void setModel(final WizardModel newModel) {
        this.model = newModel;
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
     * Scripting code that is invoked when the wizard popup is dismissed. If the
     * wizard is not in a popup, the onPopupDismiss attribute is ignored. The
     * scripting code must specify what happens in the browser when the window
     * is closed. For example, the form of the parent window that opened the
     * popup should be submitted, and the browser might be redirected, or the
     * display refreshed to reflect the task completed by the user. These
     * activities provide feedback to the user.
     *
     * @return String
     */
    public String getOnPopupDismiss() {
        if (this.onPopupDismiss != null) {
            return this.onPopupDismiss;
        }
        ValueExpression vb = getValueExpression("onPopupDismiss");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code that is invoked when the wizard popup is dismissed. If the
     * wizard is not in a popup, the onPopupDismiss attribute is ignored. The
     * scripting code must specify what happens in the browser when the window
     * is closed. For example, the form of the parent window that opened the
     * popup should be submitted, and the browser might be redirected, or the
     * display refreshed to reflect the task completed by the user. These
     * activities provide feedback to the user.
     *
     * @see #getOnPopupDismiss()
     * @param newOnPopupDismiss onPopupDismiss
     */
    public void setOnPopupDismiss(final String newOnPopupDismiss) {
        this.onPopupDismiss = newOnPopupDismiss;
    }

    /**
     * Scripting code executed when the Previous button is clicked.
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
     * Scripting code executed when the Previous button is clicked.
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
     * Use the steps attribute to specify the wizard steps programmatically,
     * instead of using the {@code webuijsf:wizardStep} tags in the JSP. The
     * steps attribute must be specified as an {@code ArrayList} or {@code List}
     * of {@code WizardStep}, {@code WizardBranch}, {@code WizardBranchSteps},
     * or {@code WizardSubstepBranch} components.
     *
     * @return Object
     */
    public Object getSteps() {
        if (this.steps != null) {
            return this.steps;
        }
        ValueExpression vb = getValueExpression("steps");
        if (vb != null) {
            return (Object) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use the steps attribute to specify the wizard steps programmatically,
     * instead of using the {@code webuijsf:wizardStep} tags in the JSP. The
     * steps attribute must be specified as an {@code ArrayList} or {@code List}
     * of {@code WizardStep}, {@code WizardBranch}, {@code WizardBranchSteps},
     * or {@code WizardSubstepBranch} components.
     *
     * @see #getSteps()
     * @param newSteps steps
     */
    public void setSteps(final Object newSteps) {
        this.steps = newSteps;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     *
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
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     *
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
     * @param newStyleClass styleClass
     */
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

    /**
     * The text to be displayed as the title for the wizard. The title is
     * displayed in the top line, and extends the full width of the wizard
     * window.
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
     * The text to be displayed as the title for the wizard. The title is
     * displayed in the top line, and extends the full width of the wizard
     * window.
     *
     * @see #getTitle()
     * @param newTitle title
     */
    public void setTitle(final String newTitle) {
        this.title = newTitle;
    }

    /**
     * Indicates whether the component is viewable by the user in the rendered
     * HTML page. Returns {@code true} by default and the HTML markup for the
     * component HTML is rendered in the page and the component is visible to
     * the user. If {@code false} is returned the the HTML markup for the
     * component is present in the rendered page, but the component is not
     * visible to the user.
     *
     * @see #setVisible
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
        return true;
    }

    /**
     * Indicate whether the component should be viewable by the user in the
     * rendered HTML page. If set to {@code false
     * }, the HTML markup for the component is present in the rendered page, but
     * the component is viewable to the user. By default, the HTML markup for
     * the component is included in the rendered page and is visible to the
     * user. If {@code visible} is {@code false}, the component is not viewable
     * to the user, but the HTML markup is included in the rendered page and can
     * still be processed on subsequent form submissions because the HTML is
     * present.
     *
     * @see #isVisible
     * @param newVisible visible
     */
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    /**
     * This implementation calls super.processRestortState and then calls
     * getModel().initialize(). This is needed because the order of state
     * restoration does not restore Wizard children until after the model is
     * restored. The model must rebuild the step children when they are actual
     * children of the Wizard, since they are new instances.
     */
    @Override
    public void processRestoreState(final FacesContext context,
            final Object state) {

        super.processRestoreState(context, state);

        // Need to do this because all children now have
        // new id's. Children are restored in UIComponentBase
        // processRestoreState.
        getModel().initialize(this);

        // Always set event to NOEVENT here
        // in case we are in a request from a component
        // within the wizard step page. Doing this here ensures that
        // the START event is never set except the first
        // render cycle. In server side state saving, the
        // event member is preserved.
        event = WizardEvent.NOEVENT;
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
        this.eventListener = (WizardEventListener)
                restoreAttachedState(context, values[1]);
        this.isPopup = ((Boolean) values[2]);
        this.isPopupSet = ((Boolean) values[3]);
        this.model = (WizardModel) restoreAttachedState(context, values[4]);
        this.onCancel = (String) values[5];
        this.onClose = (String) values[6];
        this.onFinish = (String) values[7];
        this.onHelpTab = (String) values[8];
        this.onNext = (String) values[9];
        this.onPopupDismiss = (String) values[10];
        this.onPrevious = (String) values[11];
        this.onStepLink = (String) values[12];
        this.onStepsTab = (String) values[13];
        this.steps = (Object) restoreAttachedState(context, values[14]);
        this.style = (String) values[15];
        this.styleClass = (String) values[16];
        this.title = (String) values[17];
        this.visible = ((Boolean) values[18]);
        this.visibleSet = ((Boolean) values[19]);
        this.stepTabActive = ((Boolean) values[20]);
    }

    /**
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[21];
        values[0] = super.saveState(context);
        values[1] = saveAttachedState(context, this.eventListener);
        if (this.isPopup) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.isPopupSet) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        // It must implement WizardModel which is a StateHolder if
        // there is no valuebinding.
        values[4] = saveAttachedState(context, this.model);
        values[5] = this.onCancel;
        values[6] = this.onClose;
        values[7] = this.onFinish;
        values[8] = this.onHelpTab;
        values[9] = this.onNext;
        values[10] = this.onPopupDismiss;
        values[11] = this.onPrevious;
        values[12] = this.onStepLink;
        values[13] = this.onStepsTab;
        values[14] = saveAttachedState(context, this.steps);
        values[15] = this.style;
        values[16] = this.styleClass;
        values[17] = this.title;
        if (this.visible) {
            values[18] = Boolean.TRUE;
        } else {
            values[18] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[19] = Boolean.TRUE;
        } else {
            values[19] = Boolean.FALSE;
        }
        if (this.stepTabActive) {
            values[20] = Boolean.TRUE;
        } else {
            values[20] = Boolean.FALSE;
        }
        return values;
    }
}
