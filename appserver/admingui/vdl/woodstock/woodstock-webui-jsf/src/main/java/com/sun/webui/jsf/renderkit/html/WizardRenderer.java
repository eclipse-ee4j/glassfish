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
import com.sun.webui.jsf.component.Icon;
import com.sun.webui.jsf.component.Wizard;
import com.sun.webui.jsf.component.WizardStep;
import com.sun.webui.jsf.model.WizardStepListItem;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.ResponseStateManager;
import jakarta.json.JsonObject;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;

import static com.sun.webui.jsf.util.JavaScriptUtilities.getModuleName;
import static com.sun.webui.jsf.util.JavaScriptUtilities.renderInitScriptTag;
import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;
import static com.sun.webui.jsf.util.RenderingUtilities.getStyleClasses;
import static com.sun.webui.jsf.util.RenderingUtilities.renderComponent;
import static com.sun.webui.jsf.util.ThemeUtilities.getIcon;
import static com.sun.webui.jsf.util.ThemeUtilities.getTheme;

/**
 * Render a Wizard component. This renderer is responsible for rendering
 * children and directly renders the current WizardStep component.
 * The wizard is comprised of several layout areas:
 * <ul>
 * <li>Wizard title</li>
 * <li>A step list and step help pane appearing to the left of the main
 * body.</li>
 * <li>A step title appearing To the right of the steps and help pane and
 * above the main body</li>
 * <li>A step detail appearing below the step title.</li>
 * <li>The step body appearing below the step detail and to the right
 * of the step list and help pane containing the elements for the current
 * step.</li>
 * <li>Sequencing controls appearing below the main body.</li>
 * </ul>
 * <p>
 * There are also some decorative rules that
 * separate the different areas. {@code Scrollbars} are provided as
 * determined by the size of the content in each area.
 * </p>
 * <p>
 * Rendering the Wizard is abstracted into the following call
 * structure. Any method can be implemented to override
 * default implementation. It is important to understand the
 * layout context into which the new implementation is inserting
 * its markup. See the javadoc for default implementation for
 * rendered markup.
 * <p>
 *
 * encodeBegin(..)
 *    renderStart(...)
 *        renderWizardBegin(...)
 *            renderSkipLink(...) // private
 *
 * encodeChildren(...)
 *    // Currently does nothing.
 *
 * encodeEnd(...)
 *   renderEnd(...)
 *     renderWizard(...)
 *        renderTitle(...)
*           renderTitleBegin(...)
 *          renderTitleText(...)
 *          renderTitleEnd(...)
 *        renderStepsBar(..)
 *          renderTabsBar(...)
 *           or
 *          renderEmptyBar(..)
 *        renderStepsPane(...)
 *          renderStepListBegin(...)
 *          renderStepList(...)
 *          renderSubstep(...) // For each step
 *          renderStepNumber(...)
 *          renderStepSummary(...)
 *          or
 *          renderStep(...)
 *              renderStepNumber(...)
 *              renderStepSummary(...)
 *              or
 *              renderCurrentStep(...)
 *                 renderCurrentStepIndicator(...)
 *                 renderStep(...)
 *                     renderStepNumber(...)
 *                     renderStepSummary(...)
 *                 or
 *                 renderBranchStep(...)
 *                 renderPlaceholderText(...)
 *          renderStepListEnd(...)
 *          or
 *          renderStepHelp(...)
 *              renderStepHelpBegin(...)
 *              renderStepHelpText(...)
 *              renderStepHelpEnd(...)
 *        renderTask(context, component, theme, writer);
 *          renderTaskBegin(...)
 *          renderSkipAnchor(...)
 *          renderTaskHeader(...)
 *          renderStepTitle(...)
 *              renderStepTitleBegin(...)
 *              renderStepTitleLabelText(...)
 *              renderStepTitleText(...)
 *              renderStepTitleEnd(...)
 *          renderStepDetail(...)
 *              renderStepDetailBegin(...)
 *              renderStepDetailText(...)
 *              renderStepDetailEnd(...)
 *              renderStepTask(...)
 *          renderStepTaskBegin(...)
 *          // dispatch subview
 *          renderStepTaskEnd(...)
 *          renderTaskEnd(...)
 *        renderControlBar(...)
 *          renderControlBarBegin(...)
 *          renderControlBarSpacer(...)
 *          renderLeftControlBar(...)
 *          renderLeftControlBarBegin(...)
 *          renderPreviousControl(...)
 *          renderNextControl(...)
 *          or
 *          renderFinishControl(...)
 *          renderLeftControlBarEnd(...)
 *          renderRightControlBar(...)
 *          renderRightControlBarBegin(...)
 *              renderCancelControl(...)
 *              or
 *              renderCloseControl(...)
 *              renderRightControlBarEnd(...)
 *          renderControlBarEnd(...)
 *      renderWizardEnd(context, component, theme, writer);
 * <em>refer to HCI wizard guidelines for details.</em>
 */
@Renderer(@Renderer.Renders(componentFamily = "com.sun.webui.jsf.Wizard"))
public final class WizardRenderer extends AbstractRenderer {

    //CHECKSTYLE:OFF
    private static final String ANCHOR = "a";
    private static final String DIV = "div";
    private static final String IMG = "img";
    private static final String INPUT = "input";
    private static final String PARA = "p";
    private static final String SCRIPT = "script";
    private static final String SPAN = "span";
    private static final String TABLE = "table";
    private static final String TD = "td";
    private static final String TR = "tr";

    // Attributes
    private static final String ALIGN = "align";
    private static final String ALT = "alt";
    private static final String BORDER = "border";
    private static final String CELLPADDING = "cellpadding";
    private static final String CELLSPACING = "cellspacing";
    private static final String CLASS = "class";
    private static final String STYLE_CLASS = "styleClass";
    private static final String DISABLED = "disabled";
    private static final String HEIGHT = "height";
    private static final String HREF = "href";
    private static final String HSPACE = "hspace";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String NOWRAP = "nowrap";
    private static final String ONBLUR = "onblur";
    private static final String ONCLICK = "onclick";
    private static final String ONFOCUS = "onfocus";
    private static final String ONMOUSEOVER = "onmouseover";
    private static final String ONMOUSEOUT = "onmouseout";
    private static final String SRC = "src";
    private static final String STYLE = "style";
    private static final String TITLE = "title";
    private static final String TYPE = "type";
    private static final String VALIGN = "valign";
    private static final String VALUE = "valign";
    private static final String WIDTH = "width";

    // Values
    private static final String _1_PERCENT = "1%";
    private static final String _90_PERCENT = "90%";
    private static final String _99_PERCENT = "99%";
    private static final String _100_PERCENT = "100%";
    private static final String BOTTOM = "bottom";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String SUBMIT = "submit";
    private static final String TOP = "top";
    private static final String TEXT_JAVASCRIPT = "text/javascript";
    private static final String NBSP = "&nbsp;";
    // Constants
    private static final String STEPS_PANE_SUFFIX = "_stepspane";
    private static final String SPACE = " ";
    private static final String EMPTY_STRING = "";
    private static final String USCORE = "_";
    private static final String WIZARD_JSOBJECT_CLASS = "Wizard";
    private static final String SQUOTE = "'";
    private static final String CLOSEPOPUPJS = "{0}.closePopup();";
    // Wizard attributes
    private static final String TOOLTIP = "toolTip";
    private static final String SHOWCONTROLS = "showControls";
    private static final String SHOWSTEPSPANE = "showStepsPane";
    private static final String SHOWSTEPHELP = "showStepHelp";
    private static final String SHOWTITLE = "showTitle";
    private static final String ONPOPUPDISMISS = "onPopupDismiss";
    private static final String WIZARD_SKIP_LINK_ALT = "Wizard.skipLinkAlt";
    //CHECKSTYLE:OFF

    /**
     * Error message when not rendering a component of the right type.
     */
    private static final String MSG_COMPONENT_NOT_WIZARD =
            "WizardLayoutRenderer only renders Wizard components.";

    /**
     * Construct a {@code WizardRenderer}.
     */
    public WizardRenderer() {
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Render JS to close the popup window. Output JS specified
     * in the {@code onPopupDismiss} property.If the property is empty
     * render a call to the Wizard JS  object closePopup() function.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @return String
     * @throws java.io.IOException if an IO error occurs
     */
    protected String getWizardCloseJavaScript(final FacesContext context,
            final UIComponent component) throws IOException {

        String onPopupDismiss = (String) component.getAttributes().get(
                ONPOPUPDISMISS);

        if (onPopupDismiss == null || onPopupDismiss.length() == 0) {
            onPopupDismiss = MessageFormat.format(CLOSEPOPUPJS,
                    new Object[]{ getModuleName("wizard") });
        }
        return onPopupDismiss;
    }

    @Override
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        // Is component "isa" Wizard ?
        if (!(component instanceof Wizard)) {
            throw new IllegalArgumentException(MSG_COMPONENT_NOT_WIZARD);
        }

        Theme theme = getTheme(context);

        // Always render outer div tags for HTML element functions to be valid.
        // User may need to invoke
        // document.getElementById(id).closeAndForward(...)
        renderWizardBegin(context, component, theme, writer);
    }

    // We can use this to indicate that all body content has beed read
    // and use this point logically to set up Wizard rendering.
    // It is equivalent to encodeEnd and is called immediately before.
    //
    // We could consider this the Wizard Body and treat Step's as
    // children here.
    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) {
    }

    @Override
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Theme theme = getTheme(context);
        if (!((Wizard) component).isComplete()) {
            renderWizard(context, component, theme, writer);
        }
        renderWizardEnd(context, component, theme, writer);
    }

    /**
     * Render the beginning of the layout container for the entire Wizard.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderWizardBegin(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        String wizId = component.getClientId(context);

        // Enclose the whole wizard in a div
        // Not in Lockhart Wizard
        writer.startElement(DIV, component);
        writer.writeAttribute(ID, wizId, ID);

        String style = (String) component.getAttributes().get(STYLE);
        if (style != null) {
            writer.writeAttribute(STYLE, style, STYLE);
        }

        String styles = getStyleClasses(context,
                component, theme.getStyleClass(ThemeStyles.WIZARD));
        writer.writeAttribute(CLASS, styles, null);

        // Create a "skip" link that identifies the top
        // of the wizard. There will be a target anchor to the
        // main wizard body.
        //
        // Use clientId for the anchor text, so it should be
        // unique if there is  more than one wizard on a page.
        String toolTip = (String) component.getAttributes().get(TOOLTIP);
        if (toolTip == null) {
            toolTip = theme.getMessage(WIZARD_SKIP_LINK_ALT);
        }
        renderSkipLink(context, component, theme, writer,
                component.getClientId(context), toolTip);
    }

    /**
     * Render the five main areas of the wizard.<ul>
     * <li>Wizard Title</li>
     * <li>Wizard Steps Bar</li>
     * <li>Steps Pane</li>
     * <li>Task</li>
     * <li>Navigation Buttons</li>
     * </ul>
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderWizard(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        renderTitle(context, component, theme, writer);
        renderStepsBar(context, component, theme, writer);
        renderStepsPane(context, component, theme, writer);
        renderTask(context, component, theme, writer);
        renderControlBar(context, component, theme, writer);
    }

    /**
     * Render the end of the layout container for the entire Wizard.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderWizardEnd(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        // Enclose the whole wizard in a div
        writer.endElement(DIV);

        JsonObject initProps = JSON_BUILDER_FACTORY
                .createObjectBuilder()
                .add("id", component.getClientId(context))
                .add("facesViewState", ResponseStateManager.VIEW_STATE_PARAM)
                .build();

        renderInitScriptTag(writer, "wizard", initProps);
    }

    /**
     * Render the Wizard title and structural layout that surrounds the
     * title text.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderTitle(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        // Don't output anything if there is no title
        if (!show(component, SHOWTITLE)) {
            return;
        }

        renderTitleBegin(context, component, theme, writer);
        renderTitleText(context, component, writer);
        renderTitleEnd(context, component, theme, writer);
    }

    /**
     * Render the beginning of the layout container for the Wizard title.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderTitleBegin(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_TITLE_BAR), null);
    }

    /**
     * Render the actual Wizard title text
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderTitleText(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {

        Wizard wizard = (Wizard) component;
        UIComponent title = wizard.getTitleComponent();
        if (title != null) {
            renderComponent(title, context);
        }
    }

    /**
     * Render the end of the layout container for the Wizard title.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderTitleEnd(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        writer.endElement(DIV);
    }

    /**
     * Render the content of the steps bar.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepsBar(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        Wizard wizard = (Wizard) component;

        // See if the steps bar is owned by the application
        UIComponent stepsBar = wizard.getStepsBarComponent();
        if (stepsBar != null) {
            renderComponent(stepsBar, context);
            return;
        }

        // We own it. Therefore it's composed of a TabSet component
        // with possibly two tabs, Steps and Help. If the wizard
        // does not support Help then it's just Steps and therefore
        // there is no TabSet component.
        //
        // See if step help is turned off for the wizard
        // If no step help don't render any tabs, just an empty bar.
        if (wizard.hasStepHelp() && show(component, SHOWSTEPHELP)) {
            renderTabsBar(context, component, theme, writer);
        } else {
            renderEmptyBar(component, theme, writer);
        }
    }

    /**
     * Render the content of the steps pane layout container.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepsPane(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        if (!show(component, SHOWSTEPSPANE)) {
            return;
        }

        Wizard wizard = (Wizard) component;

        // See if the steps pane is owned by the application
        UIComponent stepsPane = wizard.getStepsPaneComponent();
        if (stepsPane != null) {
            renderComponent(stepsPane, context);
            return;
        }

        // Step tab always active if there is no help
        // Always returns true if no help is available
        if (wizard.isStepsTabActive()) {
            renderStepListBegin(context, component, theme, writer);
            // If no step help, include a steps pane title before rendering the
            // steps list.
            if (!wizard.hasStepHelp()) {
                renderStepsPaneTitle(context, component, theme, writer);
            }
            renderStepList(context, component, theme, writer);
            renderStepListEnd(context, component, theme, writer);
        } else {
            renderStepHelp(context, component, theme, writer);
        }
    }

    /**
     * Render the beginning of the layout container for the Wizard
     * Steps list.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepListBegin(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        Wizard wizard = (Wizard) component;
        String paneId =
                component.getClientId(context).concat(STEPS_PANE_SUFFIX);

        // Steps Pane DIV
        writer.startElement(DIV, component);

        writer.writeAttribute(ID, paneId, null);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP), null);
    }

    /**
     * Render the step list and help tabs in a bar.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderTabsBar(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        Wizard wizard = (Wizard) component;
        UIComponent tabs = wizard.getTabsComponent();
        if (tabs == null) {
            return;
        }

        // Tabs DIV
        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP_TAB), null);

        // Looks like tabs is outputting extra div.
        renderComponent(tabs, context);

        writer.endElement(DIV); // Tabs DIV
    }

    /**
     * Render an empty bar for the wizard with no help.
     *
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderEmptyBar(final UIComponent component,
            final Theme theme, final ResponseWriter writer) throws IOException {

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_BAR), null);
        writer.endElement(DIV);
    }

    /**
     * Render the steps pane title.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepsPaneTitle(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        Wizard wizard = (Wizard) component;
        UIComponent stepsPaneTitle = wizard.getStepsPaneTitleComponent();
        if (stepsPaneTitle == null) {
            return;
        }

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP_TITLE), null);
        renderComponent(stepsPaneTitle, context);
        writer.endElement(DIV);
    }

    /**
     * Render the step list for the Wizard.
     * If a UIComponent exists that represents the step list
     * render it. Other wise obtain a {@code Wizard.StepList}
     * iterator from the wizard. The iterator returns instances of
     * {@code WizardStepListItem}. It can be determined from
     * this object whether the step is the current step, a sub-step
     * or a branch. A component to render for each step can be
     * obtained from the wizard component.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepList(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        Wizard wizard = (Wizard) component;

        // If there is a component responsible for the complete
        // list render it.
        UIComponent stepList = wizard.getStepListComponent();
        if (stepList != null) {
            renderComponent(stepList, context);
            return;
        }

        writer.startElement(TABLE, component);
        writer.writeAttribute(BORDER, Integer.toString(0), null);
        writer.writeAttribute(CELLSPACING, Integer.toString(0), null);
        writer.writeAttribute(CELLPADDING, Integer.toString(0), null);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP_TABLE), null);

        Iterator stepListIterator = wizard.getStepListIterator();
        while (stepListIterator.hasNext()) {

            WizardStepListItem stepItem =
                    (WizardStepListItem) stepListIterator.next();

            if (stepItem.isSubstep()) {
                renderSubstep(context, component, theme, writer, stepItem);
            } else if (stepItem.isCurrentStep()) {
                renderCurrentStep(context, component, theme, writer, stepItem);
            } else if (stepItem.isBranch()) {
                renderBranchStep(context, component, theme, writer, stepItem);
            } else {
                renderStep(context, component, theme, writer, stepItem);
            }
        }
        writer.endElement(TABLE);
    }

    /**
     * Render the indicator that identifies a step as being the current step.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderCurrentStepIndicator(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        Wizard wizard = (Wizard) component;
        UIComponent stepIndicator = wizard.getStepIndicatorComponent();
        if (stepIndicator != null) {
            renderComponent(stepIndicator, context);
        }

    }

    /**
     * Render a sub step. Typically this has a step number of the form
     * "n.m" where "n" is the previous step number and "m" is the sub step.
     * It is indented from the normal step number.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStepListItem} information about this
     * sub step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderSubstep(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStepListItem step)
            throws IOException {

        boolean isCurrentStep = step.isCurrentStep();

        String stepTextStyle;
        if (isCurrentStep) {
            stepTextStyle = theme.getStyleClass(
                    ThemeStyles.WIZARD_STEP_CURRENT_TEXT);
        } else {
            stepTextStyle = theme.getStyleClass(ThemeStyles.WIZARD_STEP_LINK);
        }

        writer.startElement(TR, component);

        // The first cell may have the current step indicator.
        // It is different from a normal step in that the
        // step number is not part of the same cell.
        if (isCurrentStep) {
            writer.startElement(TD, component);
            writer.writeAttribute(VALIGN, TOP, null);
            writer.writeAttribute(ALIGN, RIGHT, null);
            writer.writeAttribute(NOWRAP, NOWRAP, null);

            writer.startElement(DIV, component);
            writer.writeAttribute(CLASS,
                    theme.getStyleClass(ThemeStyles.WIZARD_STEP_ARROW_DIV),
                    null);

            renderCurrentStepIndicator(context, component, theme, writer);

            writer.endElement(DIV);
            writer.endElement(TD);
        } else {
            // An empty cell if its not the current step.
            writer.startElement(TD, component);
            writer.writeAttribute(VALIGN, TOP, null);
            writer.endElement(TD);
        }

        // The cell for the step nummber. It has a different
        // style if it is the current step.
        writer.startElement(TD, component);
        writer.writeAttribute(VALIGN, TOP, null);

        writer.startElement(TABLE, component);
        writer.writeAttribute(BORDER, Integer.toString(0), null);
        writer.writeAttribute(CELLSPACING, Integer.toString(0), null);
        writer.writeAttribute(CELLPADDING, Integer.toString(0), null);

        writer.startElement(TR, component);
        writer.startElement(TD, component);
        writer.writeAttribute(VALIGN, TOP, null);

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP_TEXT_DIV), null);

        renderStepNumber(context, component, theme, writer, step,
                stepTextStyle);

        writer.endElement(DIV);
        writer.endElement(TD);

        // Cell for the step summary
        //
        writer.startElement(TD, component);
        writer.writeAttribute(VALIGN, TOP, null);

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP_TEXT_DIV), null);

        renderStepSummary(context, component, theme, writer, step,
                stepTextStyle);

        writer.endElement(DIV);

        writer.endElement(TD);
        writer.endElement(TR);
        writer.endElement(TABLE);

        writer.endElement(TD);
        writer.endElement(TR);
    }

    /**
     * Render a Branch step with place holder text.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStepListItem} information about this
     * branch step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderBranchStep(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStepListItem step)
            throws IOException {

        // Start a branch with an emtpy cell
        writer.startElement(TR, component);
        writer.startElement(TD, component);

        writer.writeAttribute(VALIGN, TOP, null);
        writer.writeAttribute(NOWRAP, NOWRAP, null);

        writer.write(NBSP);

        writer.endElement(TD);

        writer.startElement(TD, component);
        writer.writeAttribute(VALIGN, TOP, null);

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP_TEXT_DIV), null);

        // This will be placeholder text for a branch step
        renderStepPlaceholderText(context, component, theme, writer, step);

        writer.endElement(DIV);
        writer.endElement(TD);
        writer.endElement(TR);
    }

    /**
     * Render the current step in the step list. This step typically appears
     * with an icon to the left of the step number, indicating the current step.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStepListItem} information about this
     * branch step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderCurrentStep(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStepListItem step)
            throws IOException {

        // Begin step row
        writer.startElement(TR, component);

        // Current Step Indicator Cell
        // Should this markup be part of renderCurrentStepIndicator ?
        // But WIZARD_STEP_ARROW_DIV needs to include the number
        writer.startElement(TD, component);
        writer.writeAttribute(VALIGN, TOP, null);
        writer.writeAttribute(NOWRAP, NOWRAP, null);

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP_ARROW_DIV), null);

        renderCurrentStepIndicator(context, component, theme, writer);

        // How about formatter methods, stepNumberFormat(stepNumber)
        // How about WizardStepPane.renderStep(stepNumber, WizardStep.text)
        // How about WizardStepPane.renderBranch(WizardStep.branch)
        // COMPONENT TEXT : stepNumber "1."
        renderStepNumber(context, component, theme, writer, step,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP_CURRENT_TEXT));

        writer.endElement(DIV);
        writer.endElement(TD);

        // Should this markup be part of renderStepSummary ?
        writer.startElement(TD, component);
        writer.writeAttribute(VALIGN, TOP, null);

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP_TEXT_DIV), null);

        // APPLICATION TEXT : "Type number of users");
        renderStepSummary(context, component, theme, writer, step,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP_CURRENT_TEXT));

        writer.endElement(DIV);
        writer.endElement(TD);
        writer.endElement(TR);
    }

    /**
     * Render a step's sequence number in the step list.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStepListItem} the information about this
     * step.
     * @param styleClass The styleClass for this component
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepNumber(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStepListItem step,
            final String styleClass) throws IOException {

        UIComponent number = ((Wizard) component).getStepNumberComponent(
                step.getStep(), step.getStepNumberString());
        if (number == null) {
            // Should log or throw something here.
            return;
        }
        number.getAttributes().put(STYLE_CLASS, styleClass);
        renderComponent(number, context);
    }

    /**
     * Render a step's summary in the step list.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStepListItem} information about this
     * step.
     * @param styleClass CSS class
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepSummary(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStepListItem step,
            final String styleClass) throws IOException {

        UIComponent text = ((Wizard) component).getStepSummaryComponent(
                step.getStep());
        if (text == null) {
            return;
        }
        text.getAttributes().put(STYLE_CLASS, styleClass);
        renderComponent(text, context);
    }

    /**
     * Render a branch step's placeholder text in the step list.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStepListItem} information about this
     * step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepPlaceholderText(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStepListItem step)
            throws IOException {

        UIComponent text = ((Wizard) component).getStepPlaceholderTextComponent(
                step.getStep());
        if (text == null) {
            return;
        }
        text.getAttributes().put(STYLE_CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP_TEXT));
        renderComponent(text, context);
    }

    /**
     * Render a step in the step list.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStepListItem} information about this
     * step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStep(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStepListItem step)
            throws IOException {

        writer.startElement(TR, component);
        writer.startElement(TD, component);

        writer.writeAttribute(VALIGN, TOP, null);

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP_NUMBER_DIV), null);

        renderStepNumber(context, component, theme, writer, step,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP_LINK));

        writer.endElement(DIV);
        writer.endElement(TD);

        writer.startElement(TD, component);
        writer.writeAttribute(VALIGN, TOP, null);

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP_TEXT_DIV), null);

        // This will be placeholder text for a branch step
        //
        renderStepSummary(context, component, theme, writer, step,
                theme.getStyleClass(ThemeStyles.WIZARD_STEP_LINK));

        writer.endElement(DIV);
        writer.endElement(TD);
        writer.endElement(TR);
    }

    /**
     * Render the help for the current Step in the Wizard.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepHelp(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        UIComponent stepHelp = ((Wizard) component).getStepHelpComponent();
        if (stepHelp != null) {
            renderComponent(stepHelp, context);
        }

        WizardStep step = ((Wizard) component).getCurrentStep();
        renderStepHelpBegin(context, component, theme, writer, step);
        renderStepHelpText(context, component, theme, writer, step);
        renderStepHelpEnd(context, component, theme, writer, step);
    }

    /**
     * Render the beginning of the layout for the step help.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} the current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepHelpBegin(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_HELP_DIV), null);

        // Probably shouldn't include the para
        writer.startElement(PARA, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_HELP_TEXT), null);
    }

    /**
     * Render the step help text.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} the current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepHelpText(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        // Assumes current step.
        UIComponent stepHelp = ((Wizard) component).getStepHelpTextComponent();
        if (stepHelp != null) {
            renderComponent(stepHelp, context);
        }
    }

    /**
     * Render the end of the layout for the step help.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} the current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepHelpEnd(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        writer.endElement(PARA);
        writer.endElement(DIV);
    }

    /**
     * Render the end of the layout container for the Wizard
     * Steps list.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepListEnd(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        // Steps Pane DIV
        writer.endElement(DIV);
    }

    /**
     * Render the step task. This includes a task header of title and
     * step detail or instruction, and the components that make up the
     * current wizard step.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderTask(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        UIComponent task = ((Wizard) component).getTaskComponent();
        if (task != null) {
            renderComponent(task, context);
            return;
        }
        WizardStep step = ((Wizard) component).getCurrentStep();
        if (step == null) {
            // Should log
            return;
        }

        renderTaskBegin(context, component, theme, writer);
        renderTaskHeader(context, component, theme, writer, step);
        renderStepTask(context, component, theme, writer, step);
        renderTaskEnd(context, component, theme, writer);
    }

    /**
     * Render the beginning of the layout container step task area.
     * This includes an anchor from the beginning of the steps pane.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderTaskBegin(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        String idandclass = theme.getStyleClass(ThemeStyles.WIZARD_BODY);

        // WizBdy DIV
        writer.startElement(DIV, component);
        writer.writeAttribute(ID,
                component.getClientId(context).concat(USCORE)
                        .concat(idandclass),
                null);
        writer.writeAttribute(CLASS, idandclass, null);

        // The skip link anchor
        renderSkipAnchor(context, component, theme, writer,
                component.getClientId(context));
    }

    /**
     * Render the end of the layout container for the Wizard step task area.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderTaskEnd(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        // WizBdy DIV
        writer.endElement(DIV);
    }

    /**
     * Render the step title and step detail for the current step.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderTaskHeader(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        UIComponent taskHeader = ((Wizard) component).getTaskHeaderComponent();
        if (taskHeader != null) {
            renderComponent(taskHeader, context);
            return;
        }
        renderStepTitle(context, component, theme, writer, step);
        renderStepDetail(context, component, theme, writer, step);
    }

    /**
     * Render the step title label.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepTitleLabelText(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        UIComponent titleLabel =
                ((Wizard) component).getStepTitleLabelTextComponent();
        if (titleLabel != null) {
            titleLabel.getAttributes().put(STYLE_CLASS,
                    theme.getStyleClass(ThemeStyles.WIZARD_SUB_TITLE_TEXT));
            renderComponent(titleLabel, context);
        }
    }

    /**
     * Render the step title.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepTitle(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        UIComponent stepTitle = ((Wizard) component).getStepTitleComponent();
        if (stepTitle != null) {
            renderComponent(stepTitle, context);
            return;
        }

        renderStepTitleBegin(context, component, theme, writer, step);
        renderStepTitleLabelText(context, component, theme, writer, step);
        renderStepTitleText(context, component, theme, writer, step);
        renderStepTitleEnd(context, component, theme, writer, step);
    }

    /**
     * Render the beginning of the layout for the step title.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepTitleBegin(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_SUB_TITLE_DIV), null);
    }

    /**
     * Render the step title text.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepTitleText(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        UIComponent stepTitle = ((Wizard) component)
                .getStepTitleTextComponent();
        if (stepTitle != null) {
            stepTitle.getAttributes().put(STYLE_CLASS,
                    theme.getStyleClass(ThemeStyles.WIZARD_SUB_TITLE_TEXT));
            renderComponent(stepTitle, context);
        }
    }

    /**
     * Render the end of the layout for the step title.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepTitleEnd(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        writer.endElement(DIV);
    }

    /**
     * Render the step detail for the current step.
     * Each step may have optional detail text that appears
     * below the current step title.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepDetail(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        UIComponent stepDetail = ((Wizard) component).getStepDetailComponent();
        if (stepDetail != null) {
            renderComponent(stepDetail, context);
            return;
        }
        renderStepDetailBegin(context, component, theme, writer, step);
        renderStepDetailText(context, component, theme, writer, step);
        renderStepDetailEnd(context, component, theme, writer, step);
    }

    /**
     * Render the beginning of the layout container for the step detail.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepDetailBegin(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_CONTENT_HELP_TEXT),
                null);
    }

    /**
     * Render the step detail text.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepDetailText(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        UIComponent stepDetail =
                ((Wizard) component).getStepDetailTextComponent();
        if (stepDetail != null) {
            renderComponent(stepDetail, context);
        }
    }

    /**
     * Render the end of the layout container for the step detail.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepDetailEnd(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        writer.endElement(DIV);
    }

    /**
     * Render the beginning of the layout container for the step components.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepTaskBegin(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_TASK), null);
    }

    /**
     * Render the step components.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepTask(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        // This probably over kill ?
        // Wrapper for a WizardStep ?
        UIComponent task = ((Wizard) component).getTaskStepComponent();
        if (task != null) {
            renderComponent(task, context);
            return;
        }

        renderStepTaskBegin(context, component, theme, writer, step);
        renderStepTaskComponents(context, component, theme, writer, step);
        renderStepTaskEnd(context, component, theme, writer, step);
    }

    /**
     * Render the end of the layout container for the step components
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepTaskEnd(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        writer.endElement(DIV);
    }

    /**
     * Render the components and layout for the navigation control area.
     * This area contains the Previous, Next, Finish, Cancel and Close
     * buttons.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param theme theme in use
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderControlBar(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        if (!show(component, SHOWCONTROLS)) {
            return;
        }

        UIComponent controlBar = ((Wizard) component).getControlBarComponent();
        if (controlBar != null) {
            renderComponent(controlBar, context);
            return;
        }
        renderControlBarBegin(context, component, theme, writer);
        renderControlBarSpacer(context, component, theme, writer);

        // layout container for the left and right sections in the control area.
        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_BUTTON_BOTTOM), null);

        renderLeftControlBar(context, component, theme, writer);
        renderRightControlBar(context, component, theme, writer);

        writer.endElement(DIV);

        renderControlBarEnd(context, component, theme, writer);
    }

    /**
     * Render the beginning of the layout container for the control area.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderControlBarBegin(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer)
            throws IOException {

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_BOTTOM), null);
    }

    /**
     * Render the end of the layout container for the control area.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderControlBarEnd(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer)
            throws IOException {

        writer.endElement(DIV);
    }

    /**
     * Render spacer for the control area.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderControlBarSpacer(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_BOTTOM_SPACER), null);
        writer.endElement(DIV);
    }

    /**
     * Render the beginning of the layout container for the left side of
     * the control area. The area containing the sequencing controls is
     * split into two sections: Left and Right.
     *
     * The left side of the control area contains the Previous,
     * Next or Finish controls. The right area contains the
     * Cancel or Close control.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderLeftControlBar(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        Wizard wizard = (Wizard) component;

        UIComponent leftControlBar = wizard.getLeftControlBarComponent();
        if (leftControlBar != null) {
            renderComponent(leftControlBar, context);
            return;
        }

        renderLeftControlBarBegin(context, component, theme, writer);
        if (wizard.hasPrevious()) {
            renderPreviousControl(context, component, theme, writer);
        }

        if (wizard.hasNext()) {
            renderNextControl(context, component, theme, writer);
        } else if (wizard.hasFinish()) {
            renderFinishControl(context, component, theme, writer);
        }
        renderLeftControlBarEnd(context, component, theme, writer);
    }

    /**
     * Render the beginning of the layout container for the
     * left side of the control area.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderLeftControlBarBegin(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_LEFT), null);
    }

    /**
     * Render the control that directs the Wizard to the previous step.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderPreviousControl(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        Wizard wizard = (Wizard) component;
        UIComponent control = wizard.getPreviousComponent();
        if (control == null) {
            // Log this
            return;
        }
        renderControl(context, theme, control);
    }

    /**
     * Render the control that directs the Wizard to the next step.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderNextControl(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        Wizard wizard = (Wizard) component;
        UIComponent nextControl = wizard.getNextComponent();
        if (nextControl == null) {
            // Log this
            return;
        }
        renderControl(context, theme, nextControl);
    }

    /**
     * Render the control that directs the Wizard to the perform task.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderFinishControl(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        Wizard wizard = (Wizard) component;
        UIComponent control = wizard.getFinishComponent();
        if (control == null) {
            // Log this
            return;
        }
        renderControl(context, theme, control);
    }

    /**
     * Render the end of the layout container for the
     * left side of the control area.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderLeftControlBarEnd(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        writer.endElement(DIV);
    }

    /**
     * Render the beginning of the layout container for the
     * right side of the control area.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderRightControlBar(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        Wizard wizard = (Wizard) component;

        UIComponent rightControlBar = wizard.getRightControlBarComponent();
        if (rightControlBar != null) {
            renderComponent(rightControlBar, context);
            return;
        }

        renderRightControlBarBegin(context, component, theme, writer);

        if (wizard.hasCancel()) {
            renderCancelControl(context, component, theme, writer);
        } else if (wizard.hasClose()) {
            renderCloseControl(context, component, theme, writer);
        }

        renderRightControlBarEnd(context, component, theme, writer);
    }

    /**
     * Render the beginning of the layout container for the
     * right side of the control area.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderRightControlBarBegin(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.WIZARD_RIGHT), null);
    }

    /**
     * Render the control that cancels the Wizard task.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderCancelControl(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        Wizard wizard = (Wizard) component;
        UIComponent control = wizard.getCancelComponent();
        if (control == null) {
            // Log this
            return;
        }
        renderControl(context, theme, control);
    }

    /**
     * Render the control that directs the Wizard to the end the
     * Wizard task.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderCloseControl(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        Wizard wizard = (Wizard) component;
        UIComponent control = wizard.getCloseComponent();
        if (control == null) {
            // Log this
            return;
        }
        renderControl(context, theme, control);
    }

    /**
     * Render the end of the layout container for the
     * right side of the control area.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderRightControlBarEnd(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        writer.endElement(DIV);
    }

    /**
     * Create an invisible "skip" link to an anchor as an accessibility feature
     * to navigate to the main task area in a Wizard from the steps pane.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param alt the text that will appear to screen readers.
     */
    private void renderSkipLink(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final String link, final String alt)
            throws IOException {

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.SKIP_WHITE), null);
        writer.startElement(ANCHOR, component);
        writer.writeAttribute(HREF, "#" + link, null);

        // Can this be done with a style selector ?
        // This is an invisible rule, used mainly for the alt
        renderRule(context, component, theme, writer, 1, 1, alt);

        writer.endElement(ANCHOR);
        writer.endElement(DIV);
    }

    /**
     * The anchor target of the "skip" link.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param anchor the text that will appear to screen readers, specified as
     * {@code link} in a previous {@code renderSkipLink} call.
     */
    private void renderSkipAnchor(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final String anchor)
            throws IOException {

        writer.startElement(DIV, component);
        writer.writeAttribute(CLASS,
                theme.getStyleClass(ThemeStyles.SKIP_WHITE), null);
        writer.startElement(ANCHOR, component);
        writer.writeAttribute(NAME, anchor, null);
        writer.writeAttribute(ID, anchor, null);
        writer.endElement(ANCHOR);
        writer.endElement(DIV);
    }

    /**
     * Render a rule as separator line.
     *
     * NOTE: Need to determine if CSS can be used for this purpose.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param width the width of the rule.
     * @param height the height of the rule.
     * @param alt the text that will appear if the dot image cannot be
     * displayed.
     */
    private void renderRule(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final int width, final int height,
            final String alt) throws IOException {

        Icon icon = getIcon(theme, ThemeImages.DOT);

        // Originally alt was written out if it was "", does this
        // still make sense ?
        if (alt != null) {
            icon.setAlt(alt);
        }
        icon.setHeight(height);
        icon.setWidth(width);
        renderComponent(icon, context);
    }

    /**
     * Return true if the feature has been configured to be displayed,
     * false otherwise.
     *
     * @param component the {@code UIComponent} being rendered.
     * @param feature the attribute that controls a feature.
     */
    private boolean show(final UIComponent component, final String feature) {

        // Don't show controls if turned off
        // Default is true
        return true;
    }

    /**
     * Render the current WizardStep component.
     *
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStep} current step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepTaskComponents(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStep step)
            throws IOException {

        // This could be a component tree
        // Children of the step, an iterator, a jsp page, ...
        renderComponent(step, context);
    }

    // This isn't perfect
    /**
     * Merge the specified styles.
     * @param styles styles to be merged
     * @param newStyle styles to be merged
     * @return String
     */
    private static String mergeStyle(final String styles,
            final String newStyle) {

        if (newStyle == null) {
            return styles;
        }
        if (styles == null) {
            return newStyle;
        }
        StringBuilder sb = new StringBuilder(styles);
        String[] splitStyles = styles.split(SPACE);
        for (int i = 0; i < splitStyles.length; ++i) {
            if (splitStyles[i].equals(newStyle)) {
                return sb.toString();
            }
        }
        sb.append(SPACE).append(newStyle);
        return sb.toString();
    }

    /**
     * Render the control.
     * @param context faces context
     * @param theme the current theme
     * @param control control component
     * @throws IOException if an IO error occurs
     */
    private void renderControl(final FacesContext context, final Theme theme,
            final UIComponent control) throws IOException {

        renderComponent(control, context);
    }

    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        // Enforce NPE requirements in the Javadocs
        if (context == null || component == null) {
            throw new NullPointerException();
        }
    }

    // Deprecations, which we may decide can just be removed.
    // However, if not, these deprecated methods should probably provide the
    // redundant spans to maintain the original behavior.
    //
    // Deprecate these method since we were adding redundant spans
    // around static text. The problem is that using these methods
    // will yield NO wizard style for the span.
    //
    /**
     * Render a step's sequence number in the step list.
     *
     * @deprecated See {@link renderStepNumber(FacesContext,UIComponent,Theme,
     * ResponseWriter,WizardStepListItem, String styleClass)}
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStepListItem} the information about this step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepNumber(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStepListItem step)
            throws IOException {

        UIComponent number = ((Wizard) component).getStepNumberComponent(
                step.getStep(), step.getStepNumberString());
        if (number == null) {
            // Should log or throw something here.
            return;
        }
        renderComponent(number, context);
    }

    /**
     * Render a step's summary in the step list.
     *
     * @deprecated See {@link renderStepSummary(FacesContext,UIComponent,
     * Theme,ResponseWriter, WizardStepListItem, String styleClass)}
     * @param context {@code FacesContext} for the current request.
     * @param component {@code UIComponent} a Wizard or Wizard subclass.
     * @param theme {@code Theme} to use for style, images, and text.
     * @param writer {@code ResponseWriter} write the response using this
     * writer.
     * @param step {@code WizardStepListItem} information about this step.
     * @throws java.io.IOException if an IO error occurs
     */
    protected void renderStepSummary(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final WizardStepListItem step)
            throws IOException {

        UIComponent text = ((Wizard) component).getStepSummaryComponent(
                step.getStep());
        if (text == null) {
            return;
        }
        renderComponent(text, context);
    }
}
