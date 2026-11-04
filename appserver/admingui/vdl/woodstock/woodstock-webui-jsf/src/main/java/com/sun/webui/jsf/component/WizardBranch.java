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
 * $Id: WizardBranch.java,v 1.1.20.1 2009-12-29 03:06:25 jyeary Exp $
 */
package com.sun.webui.jsf.component;

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.event.WizardEventListener;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.context.FacesContext;

/**
 * The WizardBranch represents a step in a Wizard sequence that indicates
 * that the steps following the branch, cannot be determined until the branch is
 * reached. Until that time, place holder text appears in the step list
 * at the location of the WizardBranch, that describes the conditions
 * that determine the steps that follow.
 */
@Component(type = "com.sun.webui.jsf.WizardBranch",
        family = "com.sun.webui.jsf.WizardBranch",
        displayName = "WizardBranch",
        tagName = "wizardBranch",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_wizard_branch",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_wizard_branch_props")
        //CHECKSTYLE:ON
public final class WizardBranch extends WizardStep implements NamingContainer {

    /**
     * Text that describes to users what happens when they make a selection in
     * the step that sets up the branch. This text is displayed in the Steps
     * pane when that step is initially displayed, before the user proceeds
     * through the step.
     */
    @Property(name = "placeholderText")
    private String placeholderText = null;

    /**
     * The taken attribute is used to evaluate whether the steps of the branch
     * are displayed. If taken is true, the branch is followed, and the child
     * {@code webuijsf:wizardBranchSteps} tags are evaluated. The taken
     * attribute should be a JavaServer Faces EL expression that could use the
     * user's response in a previous step to determine whether the branch should
     * be followed.
     */
    @Property(name = "taken")
    private boolean taken = false;

    /**
     * Taken set flag.
     */
    private boolean takenSet = false;

    /**
     * Construct a new {@code WizardBranchBase}.
     */
    public WizardBranch() {
        super();
        // No renderertype
        //setRendererType("com.sun.webui.jsf.WizardBranch");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.WizardBranch";
    }

    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    // Hide detail
    @Property(isHidden = true, isAttribute = false)
    @Override
    public String getDetail() {
        return super.getDetail();
    }

    // Hide eventListener
    @Property(isHidden = true, isAttribute = false)
    @Override
    public WizardEventListener getEventListener() {
        return super.getEventListener();
    }

    // Hide finish
    @Property(isHidden = true, isAttribute = false)
    @Override
    public boolean isFinish() {
        return super.isFinish();
    }

    // Hide help
    @Property(isHidden = true, isAttribute = false)
    @Override
    public String getHelp() {
        return super.getHelp();
    }

    // Hide onCancel
    @Property(isHidden = true, isAttribute = false)
    @Override
    public String getOnCancel() {
        return super.getOnCancel();
    }

    // Hide onClose
    @Property(isHidden = true, isAttribute = false)
    @Override
    public String getOnClose() {
        return super.getOnClose();
    }

    // Hide onFinish
    @Property(isHidden = true, isAttribute = false)
    @Override
    public String getOnFinish() {
        return super.getOnFinish();
    }

    // Hide onHelpTab
    @Property(isHidden = true, isAttribute = false)
    @Override
    public String getOnHelpTab() {
        return super.getOnHelpTab();
    }

    // Hide onNext
    @Property(isHidden = true, isAttribute = false)
    @Override
    public String getOnNext() {
        return super.getOnNext();
    }

    // Hide onPrevious
    @Property(isHidden = true, isAttribute = false)
    @Override
    public String getOnPrevious() {
        return super.getOnPrevious();
    }

    // Hide onStepLink
    @Property(isHidden = true, isAttribute = false)
    @Override
    public String getOnStepLink() {
        return super.getOnStepLink();
    }

    // Hide onStepsTab
    @Property(isHidden = true, isAttribute = false)
    @Override
    public String getOnStepsTab() {
        return super.getOnStepsTab();
    }

    // Hide results
    @Property(isHidden = true, isAttribute = false)
    @Override
    public boolean isResults() {
        return super.isResults();
    }

    // Hide summary
    @Property(isHidden = true, isAttribute = false)
    @Override
    public String getSummary() {
        return super.getSummary();
    }

    // Hide title
    @Property(isHidden = true, isAttribute = false)
    @Override
    public String getTitle() {
        return super.getTitle();
    }

    /**
     * Text that describes to users what happens when they make a selection in
     * the step that sets up the branch.  This text is displayed in the Steps
     * pane when that step is initially displayed, before the user proceeds
     * through the step.
     * @return String
     */
    public String getPlaceholderText() {
        if (this.placeholderText != null) {
            return this.placeholderText;
        }
        ValueExpression vb = getValueExpression("placeholderText");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Text that describes to users what happens when they make a selection in
     * the step that sets up the branch.  This text is displayed in the Steps
     * pane when that step is initially displayed, before the user proceeds
     * through the step.
     * @see #getPlaceholderText()
     * @param newPlaceholderText placeholderText
     */
    public void setPlaceholderText(final String newPlaceholderText) {
        this.placeholderText = newPlaceholderText;
    }

    /**
     * The taken attribute is used to evaluate whether the steps of the branch
     * are displayed.If taken is true, the branch is followed, and the child
     * {@code webuijsf:wizardBranchSteps} tags are evaluated. The taken
     * attribute should be a JavaServer Faces EL expression that could use the
     * user's response in a previous step to determine whether the branch should
     * be followed.
     *
     * @return {@code boolean}
     */
    public boolean isTaken() {
        if (this.takenSet) {
            return this.taken;
        }
        ValueExpression vb = getValueExpression("taken");
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
     * The taken attribute is used to evaluate whether the steps of
     * the branch are displayed. If taken is true, the branch is followed, and
     * the child {@code webuijsf:wizardBranchSteps} tags are evaluated.
     * The taken attribute should be a JavaServer Faces EL expression that
     * could use the user's response in a previous step to determine whether
     * the branch should be followed.
     * @see #isTaken()
     * @param newTaken taken
     */
    public void setTaken(final boolean newTaken) {
        this.taken = newTaken;
        this.takenSet = true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.placeholderText = (String) values[1];
        this.taken = ((Boolean) values[2]);
        this.takenSet = ((Boolean) values[3]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[4];
        values[0] = super.saveState(context);
        values[1] = this.placeholderText;
        if (this.taken) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.takenSet) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        return values;
    }
}
