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
 * $Id: WizardSubstepBranch.java,v 1.1.20.1 2009-12-29 03:06:26 jyeary Exp $
 */
package com.sun.webui.jsf.component;

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.event.WizardEventListener;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.context.FacesContext;

/**
 * The WizardSubstepBranch component represents a conditional branch in a wizard
 * step sequence. The steps that comprise a sub step branch are always followed
 * by the same sequence of steps, in the main sequence, whether the sub step
 * branch is taken or not. The steps in a sub step branch are numbered
 * differently that the steps in the main sequence. For example if the sub step
 * branch occurs at step 2, then the sub steps are numbered as 2.1, 2.2, etc..
 */
@Component(type = "com.sun.webui.jsf.WizardSubstepBranch",
        family = "com.sun.webui.jsf.WizardSubstepBranch",
        displayName = "WizardSubstepBranch",
        tagName = "wizardSubstepBranch",
        //CHECKSTYLE:OFF
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_wizard_substep_branch",
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_wizard_substep_branch_props")
        //CHECKSTYLE:ON
public final class WizardSubstepBranch extends WizardStep
        implements NamingContainer {

    /**
     * The taken attribute is used to evaluate whether the steps of this branch
     * sequence are displayed. If taken is true, the child components included
     * in the {@code webuijsf:wizardStep} tags within this
     * {@code webuijsf:wizardSubstepBranch} tag are rendered. The taken
     * attribute should be a JavaServer Faces EL expression that uses the user's
     * response in a previous step to determine whether the branch sequence
     * should be rendered.
     */
    @Property(name = "taken")
    private boolean taken = false;

    /**
     * Taken set flag.
     */
    private boolean takenSet = false;

    /**
     * Construct a new {@code WizardSubstepBranchBase}.
     */
    public WizardSubstepBranch() {
        super();
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.WizardSubstepBranch";
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
     * The taken attribute is used to evaluate whether the steps of this branch
     * sequence are displayed.If taken is true, the child components included in
     * the {@code webuijsf:wizardStep} tags within this
     * {@code webuijsf:wizardSubstepBranch} tag are rendered. The taken
     * attribute should be a JavaServer Faces EL expression that uses the user's
     * response in a previous step to determine whether the branch sequence
     * should be rendered.
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
     * The taken attribute is used to evaluate whether the steps of this branch
     * sequence are displayed.If taken is true, the child components included in
     * the {@code webuijsf:wizardStep} tags within this
     * {@code webuijsf:wizardSubstepBranch} tag are rendered. The taken
     * attribute should be a JavaServer Faces EL expression that uses the user's
     * response in a previous step to determine whether the branch sequence
     * should be rendered.
     *
     * @param newTaken taken
     * @see #isTaken()
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
        this.taken = ((Boolean) values[1]);
        this.takenSet = ((Boolean) values[2]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[3];
        values[0] = super.saveState(context);
        if (this.taken) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.takenSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        return values;
    }
}
