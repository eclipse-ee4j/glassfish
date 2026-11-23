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
import com.sun.faces.extensions.avatar.lifecycle.AsyncResponse;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

import java.util.Objects;

/**
 * The ProgressBar component is used to create a progress indicator.
 * <p>
 * Progress Bar component consists of three progress bar designs: "Simple
 * Determinate", "Indeterminate" and a "Busy" indicator. The "Simple
 * Determinate" and "Indeterminate" progress bars are represented as graphical
 * bars. The "Simple Determinate" progress bar is used to represent the
 * percentage of given task that has been completed. The "Indeterminate"
 * progress bar is used when estimates of task completion cannot be provided,
 * yet the task is being performed. Progress bars may also include a textual
 * description of the operation; a textual description of the current operation
 * status; and any related controls such as a "Pause", "Resume" and/or "Cancel"
 * buttons to halt the associated task or job. The "Busy" indicator is used when
 * space is very constrained.
 * </p>
 * <p>
 * In the rendered HTML page, the progressbar is created with
 * {@code &lt;div&gt;} elements. The progress consists of the following
 * areas:</p>
 * <ul>
 * <li>
 * <p>
 * An optional textual "operation description" element that describes the
 * overall operation being monitored.
 * </p><p>
 * A dynamic, graphical "progress animation" element that updates as the
 * operation progresses and has a default height of 14 pixels and width of 184
 * pixels.
 * </p><p>
 * An optional log message textarea that can be used to display log messages.
 * </p><p>
 * An optional control element that can be used to provide a stop control, pause
 * and resume controls, cancel control or the like (customizable by developer)
 * </p><p>
 * An optional textual "status" element that provides a dynamic, written
 * description of the current state of the operation.
 * </p>
 * </li>
 * </ul>
 */
@Component(type = "com.sun.webui.jsf.ProgressBar",
        family = "com.sun.webui.jsf.ProgressBar",
        displayName = "ProgressBar",
        tagName = "progressBar",
        tagRendererType = "com.sun.webui.jsf.widget.ProgressBar")
public final class ProgressBar extends jakarta.faces.component.UIOutput
        implements NamingContainer, Widget {

    /**
     * The facet name for the top text.
     */
    public static final String TOPTEXT_FACET = "progressTextTop";

    /**
     * The facet name for the bottom text.
     */
    public static final String BOTTOMTEXT_FACET = "progressTextBottom";

    /**
     * The facet name for the right control.
     */
    public static final String RIGHTTASK_CONTROL_FACET = "progressControlRight";

    /**
     * The facet name for the left control.
     */
    public static final String BOTTOMTASK_CONTROL_FACET =
            "progressControlBottom";

    // Task states
    /**
     * Task completed state.
     */
    public static final String TASK_COMPLETED = "completed";

    /**
     * Task paused state.
     */
    public static final String TASK_PAUSED = "paused";

    /**
     * Task stopped state.
     */
    public static final String TASK_STOPPED = "stopped";

    /**
     * Task running state.
     */
    public static final String TASK_RUNNING = "running";

    /**
     * Task resumed state.
     */
    public static final String TASK_RESUMED = "resumed";

    /**
     * Task canceled state.
     */
    public static final String TASK_CANCELED = "canceled";

    /**
     * Task failed state.
     */
    public static final String TASK_FAILED = "failed";

    /**
     * Task not started state.
     */
    public static final String TASK_NOT_STARTED = "not_started";

    // Types of ProgressBar

    /**
     * Determinate progress bar type.
     */
    public static final String DETERMINATE = "DETERMINATE";

    /**
     * Indeterminate progress bar type.
     */
    public static final String INDETERMINATE = "INDETERMINATE";

    /**
     * Busy progress bar type.
     */
    public static final String BUSY_INDICATOR = "BUSY";

    /**
     * The component id for the actions separator icon.
     */
    public static final String BUSY_ICON_ID = "_busyImage";

    /**
     * Flag indicating to turn off default Ajax functionality. Set ajaxify to
     * false when providing a different Ajax implementation.
     */
    @Property(name = "ajaxify",
            displayName = "Ajaxify",
            category = "Javascript")
    private boolean ajaxify = true;

    /**
     * ajaxify set flag.
     */
    private boolean ajaxifySet;

   /**
     * Alternative HTML template to be used by this component.
     */
    @Property(name = "htmlTemplate",
            displayName = "HTML Template",
            category = "Appearance")
    private String htmlTemplate;

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     */
    @Property(name = "toolTip",
            displayName = "toolTip Text",
            category = "Behavior")
    private String toolTip;

    /**
     * Text to describe the operation that is monitored by the progress bar.
     */
    @Property(name = "description",
            displayName = "description",
            category = "Appearance")
    private String description;

    /**
     * Text to be displayed along with an icon when the task fails.
     */
    @Property(name = "failedStateText",
            displayName = "Failed State Text",
            category = "Appearance")
    private String failedStateText;

    /**
     * Number of pixels for the height of the progress bar animation. The
     * default is 14.
     */
    @Property(name = "height",
            displayName = "Bar Height",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int height = Integer.MIN_VALUE;

    /**
     * height set flag.
     */
    private boolean heightSet;

    /**
     * Text to be displayed in a text area at the bottom of the progress bar
     * component.
     */
    @Property(name = "logMessage",
            displayName = "Log Message",
            category = "Appearance")
    private String logMessage;

    /**
     * Set to true to display the operation progress text superimposed on the
     * progress bar animation.
     */
    @Property(name = "overlayAnimation",
            displayName = "Is overlayAnimation",
            category = "Advanced")
    private boolean overlayAnimation;

    /**
     * overlayAnimation set flag.
     */
    private boolean overlayAnimationSet;

    /**
     * An integer that indicates the completion percentage of the task.
     */
    @Property(name = "progress",
            displayName = "Progress",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int progress = Integer.MIN_VALUE;

    /**
     * progress set flag.
     */
    private boolean progressSet;

    /**
     * URL to an image to use instead of the default image for the progress
     * indicator.
     */
    @Property(name = "progressImageUrl",
            displayName = "Image Url",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.SunWebUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String progressImageUrl;

    /**
     * The number of milliseconds between updates to the progress bar.
     */
    @Property(name = "refreshRate",
            displayName = "Refresh Rate",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int refreshRate = Integer.MIN_VALUE;

    /**
     * refreshRate set flag.
     */
    private boolean refreshRateSet;

    /**
     * Text to be displayed at the bottom of the progress bar, for the status of
     * the operation.
     */
    @Property(name = "status",
            displayName = "Status Text",
            category = "Appearance")
    private String status;

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
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
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
    private boolean tabIndexSet;

    /**
     * A string representing the state of the task associated with this progress
     * bar. Valid values are:
     * <ul>
     * <li>not_started</li>
     * <li>running</li>
     * <li>paused</li>
     * <li>resumed</li>
     * <li>stopped</li>
     * <li>canceled</li>
     * <li>failed</li>
     * <li>completed</li>
     * </ul>
     */
    @Property(name = "taskState",
            displayName = "Task State",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.ProgressBarStatesTypeEditor")
            //CHECKSTYLE:ON
    private String taskState;

    /**
     * Type of progress bar. Value must be one of the following:<br>
     * "DETERMINATE" for horizontal bar showing percent complete<br>
     * "INDETERMINATE" for horizontal bar without percent complete<br>
     * "BUSY" for simple activity indicator
     */
    @Property(name = "type",
            displayName = "ProgressBar Type",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.ProgressBarTypesEditor")
            //CHECKSTYLE:ON
    private String type;

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
    private boolean visible;

    /**
     * visible set flag.
     */
    private boolean visibleSet;

    /**
     * Number of pixels for the width of the progress bar animation. The default
     * is 184.
     */
    @Property(name = "width",
            displayName = "Bar Width",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int width = Integer.MIN_VALUE;

    /**
     * width set flag.
     */
    private boolean widthSet;

    /**
     * Creates a new instance of ProgressBar.
     */
    public ProgressBar() {
        super();
        setRendererType("com.sun.webui.jsf.widget.ProgressBar");
    }

    /**
     * Get the faces context.
     * @return FacesContext
     */
    public FacesContext getContext() {
        return getFacesContext();
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.ProgressBar";
    }

    @Override
    public String getRendererType() {
        if (AsyncResponse.isAjaxRequest()) {
            return "com.sun.webui.jsf.ajax.ProgressBar";
        } else {
            return super.getRendererType();
        }
    }

    @Override
    public String getWidgetType() {
        return "progressBar";
    }

    /**
     * Get the busy icon.
     *
     * @return The busy icon.
     */
    public UIComponent getBusyIcon() {
        Theme theme = ThemeUtilities.getTheme(getFacesContext());

        // Get child.
        Icon child = ThemeUtilities.getIcon(theme, ThemeImages.PROGRESS_BUSY);
        child.setId(BUSY_ICON_ID);
        child.setBorder(0);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * The component identifier for this component. This value must be unique
     * within the closest parent component that is a naming container.
     * @param id id
     */
    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    /**
     * Use the rendered attribute to indicate whether the HTML code for the
     * component should be included in the rendered HTML page. If set to false,
     * the rendered HTML page does not include the HTML for the component. If
     * the component is not rendered, it is also not processed on any subsequent
     * form submission.
     * @param rendered rendered
     */
    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    // Overwrite value annotation
    @Property(isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    // Hide converter
    @Property(isHidden = true, isAttribute = false)
    @Override
    public Converter getConverter() {
        return super.getConverter();
    }

    /**
     * This function creates one text-area component for displaying log
     * messages.
     *
     * @param component UI component
     * @return UIComponent
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public UIComponent getLogMsgComponent(final ProgressBar component) {
        // textArea for running log
        TextArea textArea = new TextArea();
        textArea.setParent(component);
        textArea.setId("logMsg");
        textArea.setRows(4);
        textArea.setColumns(62);
        return textArea;
    }

    /**
     * Test if default Ajax functionality should be turned off.
     *
     * @return {@code true} if ajax is turned on
     */
    public boolean isAjaxify() {
        if (this.ajaxifySet) {
            return this.ajaxify;
        }
        ValueExpression vb = getValueExpression("ajaxify");
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
     * Set flag indicating to turn off default Ajax functionality.
     *
     * @param newAjaxify new value
     */
    public void setAjaxify(final boolean newAjaxify) {
        this.ajaxify = newAjaxify;
        this.ajaxifySet = true;
    }

    /**
     * Get alternative HTML template to be used by this component.
     *
     * @return String
     */
    public String getHtmlTemplate() {
        if (this.htmlTemplate != null) {
            return this.htmlTemplate;
        }
        ValueExpression vb = getValueExpression("htmlTemplate");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Set alternative HTML template to be used by this component.
     *
     * @param newHtmlTemplate new value
     */
    public void setHtmlTemplate(final String newHtmlTemplate) {
        this.htmlTemplate = newHtmlTemplate;
    }

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool-tip if the mouse cursor hovers over the HTML
     * element.
     *
     * @return String
     */
    public String getToolTip() {
        if (this.toolTip != null) {
            return this.toolTip;
        }
        ValueExpression valueExpression = getValueExpression("toolTip");
        if (valueExpression != null) {
            return valueExpression.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Sets the value of the title attribute for the HTML element.The specified
     * text will display as a tooltip if the mouse cursor hovers over the HTML
     * element.
     *
     * @param newToolTip tool tip
     * @see #getToolTip()
     */
    public void setToolTip(final String newToolTip) {
        this.toolTip = newToolTip;
    }

    /**
     * Text to describe the operation that is monitored by the progress bar.
     *
     * @return String
     */
    public String getDescription() {
        if (this.description != null) {
            return this.description;
        }
        ValueExpression valueExpression = getValueExpression("description");
        if (valueExpression != null) {
            return valueExpression.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Text to describe the operation that is monitored by the progress bar.
     *
     * @param newDescription description
     * @see #getDescription()
     */
    public void setDescription(final String newDescription) {
        this.description = newDescription;
    }

    /**
     * Text to be displayed along with an icon when the task fails.
     *
     * @return String
     */
    public String getFailedStateText() {
        ValueExpression valueExpression = getValueExpression("failedStateText");
        // set the default text for failed state if failedStateText is null
        if (valueExpression == null && this.failedStateText == null) {
            this.failedStateText = ThemeUtilities
                    .getTheme(FacesContext.getCurrentInstance())
                    .getMessage("ProgressBar.failedText");
        }
        if (this.failedStateText != null) {
            return this.failedStateText;
        }

        if (valueExpression != null) {
            return valueExpression.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Text to be displayed along with an icon when the task fails.
     *
     * @param newFailedStateText new value
     * @see #getFailedStateText()
     */
    public void setFailedStateText(final String newFailedStateText) {
        this.failedStateText = newFailedStateText;
    }

    /**
     * Number of pixels for the height of the progress bar animation. The
     * default is 14.
     *
     * @return height
     */
    public int getHeight() {
        if (this.heightSet) {
            return this.height;
        }
        ValueExpression valueExpression = getValueExpression("height");
        if (valueExpression != null) {
            Integer result = valueExpression.getValue(getFacesContext().getELContext());
            if (result != null) {
                return result;
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Number of pixels for the height of the progress bar animation.The default
     * is 14.
     *
     * @param newHeight new value
     * @see #getHeight()
     */
    public void setHeight(final int newHeight) {
        this.height = newHeight;
        this.heightSet = true;
    }

    /**
     * Text to be displayed in a text area at the bottom of the progress bar
     * component.
     *
     * @return String
     */
    public String getLogMessage() {
        if (this.logMessage != null) {
            return this.logMessage;
        }
        ValueExpression valueExpression = getValueExpression("logMessage");
        if (valueExpression != null) {
            return valueExpression.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Text to be displayed in a text area at the bottom of the progress bar
     * component.
     *
     * @param newLogMessage new value
     * @see #getLogMessage()
     */
    public void setLogMessage(final String newLogMessage) {
        this.logMessage = newLogMessage;
    }

    /**
     * Set to true to display the operation progress text superimposed on the
     * progress bar animation.
     *
     * @return {@code boolean}
     */
    public boolean isOverlayAnimation() {
        if (this.overlayAnimationSet) {
            return this.overlayAnimation;
        }
        ValueExpression valueExpression = getValueExpression("overlayAnimation");
        if (valueExpression != null) {
            Boolean result = valueExpression.getValue(getFacesContext().getELContext());
            if (result != null) {
                return result;
            }
        }
        return false;
    }

    /**
     * Set to true to display the operation progress text superimposed on the
     * progress bar animation.
     *
     * @param newOverlayAnimation new value
     * @see #isOverlayAnimation()
     */
    public void setOverlayAnimation(final boolean newOverlayAnimation) {
        this.overlayAnimation = newOverlayAnimation;
        this.overlayAnimationSet = true;
    }

    /**
     * An integer that indicates the completion percentage of the task.
     *
     * @return progress value
     */
    public int getProgress() {
        if (this.progressSet) {
            return this.progress;
        }
        ValueExpression valueExpression = getValueExpression("progress");
        if (valueExpression != null) {
            Integer result = valueExpression.getValue(getFacesContext().getELContext());
            if (result != null) {
                return result;
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * An integer that indicates the completion percentage of the task.
     *
     * @param newProgress new value
     * @see #getProgress()
     */
    public void setProgress(final int newProgress) {
        this.progress = newProgress;
        this.progressSet = true;
    }

    /**
     * URL to an image to use instead of the default image for the progress
     * indicator.
     *
     * @return String
     */
    public String getProgressImageUrl() {
        if (this.progressImageUrl != null) {
            return this.progressImageUrl;
        }
        ValueExpression valueExpression = getValueExpression("progressImageUrl");
        if (valueExpression != null) {
            return valueExpression.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * URL to an image to use instead of the default image for the progress
     * indicator.
     *
     * @param newProgressImageUrl new value
     * @see #getProgressImageUrl()
     */
    public void setProgressImageUrl(final String newProgressImageUrl) {
        this.progressImageUrl = newProgressImageUrl;
    }

    /**
     * The number of milliseconds between updates to the progress bar.
     *
     * @return refresh rate
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public int getRefreshRate() {
        ValueExpression valueExpression = getValueExpression("refreshRate");
        //set the default refresh rate 3000 if refreshRate < 0
        if (valueExpression == null && this.refreshRate < 0) {
            this.refreshRate = 3000;
        }
        if (this.refreshRateSet) {
            return this.refreshRate;
        }
        if (valueExpression != null) {
            Integer result = valueExpression.getValue(getFacesContext().getELContext());
            if (result != null) {
                return result;
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * The number of milliseconds between updates to the progress bar.
     *
     * @param newRefreshRate new value
     * @see #getRefreshRate()
     */
    public void setRefreshRate(final int newRefreshRate) {
        this.refreshRate = newRefreshRate;
        this.refreshRateSet = true;
    }

    /**
     * Text to be displayed at the bottom of the progress bar, for the status of
     * the operation.
     *
     * @return status
     */
    public String getStatus() {
        if (this.status != null) {
            return this.status;
        }
        ValueExpression valueExpression = getValueExpression("status");
        if (valueExpression != null) {
            return valueExpression.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Text to be displayed at the bottom of the progress bar, for the status of
     * the operation.
     *
     * @param newStatus new value
     * @see #getStatus()
     */
    public void setStatus(final String newStatus) {
        this.status = newStatus;
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
        ValueExpression valueExpression = getValueExpression("style");
        if (valueExpression != null) {
            return valueExpression.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @param newStyle new value
     * @see #getStyle()
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
        ValueExpression valueExpression = getValueExpression("styleClass");
        if (valueExpression != null) {
            return valueExpression.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @param newStyleClass styleClass
     * @see #getStyleClass()
     */
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

    /**
     * Position of this element in the tabbing order of the current
     * document.Tabbing order determines the sequence in which elements receive
     * focus when the tab key is pressed. The value must be an integer between 0
     * and 32767.
     *
     * @return index
     */
    public int getTabIndex() {
        if (this.tabIndexSet) {
            return this.tabIndex;
        }
        ValueExpression valueExpression = getValueExpression("tabIndex");
        if (valueExpression != null) {
            Integer result = valueExpression.getValue(getFacesContext().getELContext());
            if (result != null) {
                return result;
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Position of this element in the tabbing order of the current
     * document.Tabbing order determines the sequence in which elements receive
     * focus when the tab key is pressed. The value must be an integer between 0
     * and 32767.
     *
     * @param newTabIndex tabIndex
     * @see #getTabIndex()
     */
    public void setTabIndex(final int newTabIndex) {
        this.tabIndex = newTabIndex;
        this.tabIndexSet = true;
    }

    /**
     * A string representing the state of the task associated with this progress
     * bar.Default value for taskState is not_started. Valid values are:
     * <ul>
     * <li>not_started</li>
     * <li>running</li>
     * <li>paused</li>
     * <li>resumed</li>
     * <li>stopped</li>
     * <li>canceled</li>
     * <li>failed</li>
     * <li>completed</li>
     * </ul>
     *
     * @return String
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public String getTaskState() {
        // set default state of task as not started and also if progress
        // is greater than zero and task state is not started then set
        // it to running state. Also if progress > 99 set taskState to
        // completed
        ValueExpression valueExpression = getValueExpression("taskState");
        if (this.taskState == null && valueExpression == null) {
            this.taskState = ProgressBar.TASK_NOT_STARTED;
        } else if (Objects.equals(this.taskState, ProgressBar.TASK_NOT_STARTED)
                && this.progress > 0) {
            this.taskState = ProgressBar.TASK_RUNNING;
        } else if (!(Objects.equals(this.taskState, ProgressBar.TASK_COMPLETED))
                && this.progress > 99) {
            this.taskState = ProgressBar.TASK_COMPLETED;
        }

        if (this.taskState != null) {
            return this.taskState;
        }
        return valueExpression.getValue(getFacesContext().getELContext());
    }

    /**
     * A string representing the state of the task associated with this progress
     * bar. Valid values are:
     * <ul>
     * <li>not_started</li>
     * <li>running</li>
     * <li>paused</li>
     * <li>resumed</li>
     * <li>stopped</li>
     * <li>canceled</li>
     * <li>failed</li>
     * <li>completed</li>
     * </ul>
     *
     * @param newTaskState new value
     * @see #getTaskState()
     */
    public void setTaskState(final String newTaskState) {
        this.taskState = newTaskState;
    }

    /**
     * Type of progress bar.
     * Value must be one of the following:
     * <ul>
     * <li>"DETERMINATE" for horizontal bar showing percent complete</li>
     * <li>"INDETERMINATE" for horizontal bar without percent complete</li>
     * <li>"BUSY" for simple activity indicator</li>
     * </ul>
     *
     * @return String
     */
    public String getType() {
        ValueExpression valueExpression = getValueExpression("type");
        //set type attribute to DETERMINATE if type is null
        if (valueExpression == null && this.type == null) {
            this.type = ProgressBar.DETERMINATE;
        }
        if (this.type != null) {
            return this.type;
        }
        return valueExpression.getValue(getFacesContext().getELContext());
    }

    /**
     * Type of progress bar.
     * Value must be one of the following:
     * <ul>
     * <li>"DETERMINATE" for horizontal bar showing percent complete</li>
     * <li>"INDETERMINATE" for horizontal bar without percent complete</li>
     * <li>"BUSY" for simple activity indicator</li>
     * </ul>
     *
     * @param newType new value
     * @see #getType()
     */
    public void setType(final String newType) {
        this.type = newType;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page.If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     *
     * @return {@code boolean}
     */
    public boolean isVisible() {
        if (this.visibleSet) {
            return this.visible;
        }
        ValueExpression valueExpression = getValueExpression("visible");
        if (valueExpression != null) {
            Boolean result = valueExpression.getValue(getFacesContext().getELContext());
            if (result != null) {
                return result;
            }
        }
        return true;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page.If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     *
     * @param newVisible new value
     * @see #isVisible()
     */
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    /**
     * Number of pixels for the width of the progress bar animation.The default
     * is 184.
     *
     * @return width
     */
    public int getWidth() {
        if (this.widthSet) {
            return this.width;
        }
        ValueExpression valueExpression = getValueExpression("width");
        if (valueExpression != null) {
            Integer result = valueExpression.getValue(getFacesContext().getELContext());
            if (result != null) {
                return result;
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Number of pixels for the width of the progress bar animation.The default
     * is 184.
     *
     * @param newWidth new value
     * @see #getWidth()
     */
    public void setWidth(final int newWidth) {
        this.width = newWidth;
        this.widthSet = true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.toolTip = (String) values[1];
        this.description = (String) values[2];
        this.failedStateText = (String) values[3];
        this.height = ((Integer) values[4]);
        this.heightSet = ((Boolean) values[5]);
        this.logMessage = (String) values[6];
        this.overlayAnimation = ((Boolean) values[7]);
        this.overlayAnimationSet = ((Boolean) values[8]);
        this.progress = ((Integer) values[9]);
        this.progressSet = ((Boolean) values[10]);
        this.progressImageUrl = (String) values[11];
        this.refreshRate = ((Integer) values[12]);
        this.refreshRateSet = ((Boolean) values[13]);
        this.status = (String) values[14];
        this.style = (String) values[15];
        this.styleClass = (String) values[16];
        this.tabIndex = ((Integer) values[17]);
        this.tabIndexSet = ((Boolean) values[18]);
        this.taskState = (String) values[19];
        this.type = (String) values[20];
        this.visible = ((Boolean) values[21]);
        this.visibleSet = ((Boolean) values[22]);
        this.width = ((Integer) values[23]);
        this.widthSet = ((Boolean) values[24]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[25];
        values[0] = super.saveState(context);
        values[1] = this.toolTip;
        values[2] = this.description;
        values[3] = this.failedStateText;
        values[4] = this.height;
        if (this.heightSet) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        values[6] = this.logMessage;
        if (this.overlayAnimation) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        if (this.overlayAnimationSet) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        values[9] = this.progress;
        if (this.progressSet) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        values[11] = this.progressImageUrl;
        values[12] = this.refreshRate;
        if (this.refreshRateSet) {
            values[13] = Boolean.TRUE;
        } else {
            values[13] = Boolean.FALSE;
        }
        values[14] = this.status;
        values[15] = this.style;
        values[16] = this.styleClass;
        values[17] = this.tabIndex;
        if (this.tabIndexSet) {
            values[18] = Boolean.TRUE;
        } else {
            values[18] = Boolean.FALSE;
        }
        values[19] = this.taskState;
        values[20] = this.type;
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
        values[23] = this.width;
        if (this.widthSet) {
            values[24] = Boolean.TRUE;
        } else {
            values[24] = Boolean.FALSE;
        }
        return values;
    }
}
