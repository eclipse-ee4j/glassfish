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
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;

/**
 * The JobStatus component is used to show the number of jobs currently running.
 */
@Component(type = "com.sun.webui.jsf.JobStatus",
        family = "com.sun.webui.jsf.JobStatus",
        displayName = "Job Status",
        tagName = "jobStatus",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_job_status",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_job_status_props")
        //CHECKSTYLE:ON
public final class JobStatus extends ImageHyperlink {

    /**
     * The number of currently executing jobs, displayed next to the job label.
     */
    @Property(name = "numJobs",
            displayName = "Number of Jobs")
    private int numJobs = Integer.MIN_VALUE;

    /**
     * numJobs set flag.
     */
    private boolean numJobsSet = false;

    /**
     * Default constructor.
     */
    public JobStatus() {
        super();
        setRendererType("com.sun.webui.jsf.JobStatus");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.JobStatus";
    }

    @Override
    public String getIcon() {
        String icon = super.getIcon();
        if (icon == null) {
            icon = ThemeImages.MASTHEAD_STATUS_ICON;
        }
        return icon;
    }

    @Override
    public String getStyleClass() {
        String styleClass = super.getStyleClass();
        if (styleClass == null) {
            styleClass = ThemeStyles.MASTHEAD_PROGRESS_LINK;
        }
        Theme theme = ThemeUtilities.getTheme(
                FacesContext.getCurrentInstance());
        return theme.getStyleClass(styleClass);
    }

    @Override
    public boolean isDisabled() {
        if (getNumJobs() == 0) {
            // always disable the hyperlink when 0 jobs
            return true;
        }
        return super.isDisabled();
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int getHeight() {
        return 17;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int getWidth() {
        return 17;
    }

    @Override
    public int getBorder() {
        return 0;
    }

    @Override
    public String getAlign() {
        return "middle";
    }

    @Override
    public String getAlt() {
        String alt = super.getAlt();
        if (alt == null) {
            Theme theme = ThemeUtilities
                    .getTheme(FacesContext.getCurrentInstance());
            alt = theme.getMessage("masthead.tasksRunningAltText");
        }
        return alt;
    }

    /**
     * Specifies the position of the image with respect to its context. Valid
     * values are: bottom (the default); middle; top; left; right.
     * @return String
     */
    @Property(name = "onDblClick",
            isHidden = false,
            isAttribute = true)
    @Override
    public String getOnDblClick() {
        return super.getOnDblClick();
    }

    // Hide textPosition
    @Property(name = "textPosition", isHidden = true, isAttribute = false)
    @Override
    public String getTextPosition() {
        return super.getTextPosition();
    }

    /**
     * Get the number of jobs.
     * @return int
     */
    public int getNumJobs() {
        if (this.numJobsSet) {
            return this.numJobs;
        }
        ValueExpression vb = getValueExpression("numJobs");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return 0;
    }

    /**
     * The number of currently executing jobs, displayed next to the job
     * label.
     *
     * @see #getNumJobs()
     * @param newNumJobs numJobs
     */
    public void setNumJobs(final int newNumJobs) {
        this.numJobs = newNumJobs;
        this.numJobsSet = true;
    }

    @Override
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.numJobs = ((Integer) values[1]);
        this.numJobsSet = ((Boolean) values[2]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[3];
        values[0] = super.saveState(context);
        values[1] = this.numJobs;
        if (this.numJobsSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        return values;
    }
}
