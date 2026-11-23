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

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

/**
 * The MessageGroup component is used to display a list of messages for the page
 * and all its components.
 */
@Component(type = "com.sun.webui.jsf.MessageGroup",
        family = "com.sun.webui.jsf.MessageGroup",
        displayName = "Message Group",
        tagName = "messageGroup",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_message_group",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_message_group_props")
        //CHECKSTYLE:ON
public final class MessageGroup extends UIComponentBase {

    /**
     * Set this attribute to true to display the detailed message.
     */
    @Property(name = "showDetail",
            displayName = "Show Detail Message",
            category = "Appearance")
    private boolean showDetail;

    /**
     * showDetail set flag.
     */
    private boolean showDetailSet;

    /**
     * Use the showGlobalOnly attribute to display only those messages that are
     * not associated with a component id. This attribute allows you to avoid
     * showing a component error twice if you use {@code webuijsf:message} and
     * {@code webuijsf:messageGroup} in the same page.
     */
    @Property(name = "showGlobalOnly",
            displayName = "Show Global Messages Only",
            category = "Behavior")
    private boolean showGlobalOnly;

    /**
     * showGlobalOnly set flag.
     */
    private boolean showGlobalOnlySet;

    /**
     * Set this attribute to true to display the summary message.
     */
    @Property(name = "showSummary",
            displayName = "Show Summary Message",
            category = "Appearance")
    private boolean showSummary;

    /**
     * showSummary set flag.
     */
    private boolean showSummarySet;

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
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool-tip if the mouse cursor hovers over the HTML
     * element.
     */
    @Property(name = "toolTip",
            displayName = "Tool Tip",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String toolTip;

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
     * Sets the title of the message group. If this attribute is not specified,
     * the default title "System Messages" will be used.
     */
    @Property(name = "title",
            displayName = "title",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String title;

    /**
     * Default constructor.
     */
    public MessageGroup() {
        super();
        setRendererType("com.sun.webui.jsf.MessageGroup");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.MessageGroup";
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

    /**
     * Set this attribute to true to display the detailed message.
     * @return {@code boolean}
     */
    public boolean isShowDetail() {
        if (this.showDetailSet) {
            return this.showDetail;
        }
        ValueExpression vb = getValueExpression("showDetail");
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
     * Set this attribute to true to display the detailed message.
     *
     * @see #isShowDetail()
     * @param newShowDetail showDetail
     */
    public void setShowDetail(final boolean newShowDetail) {
        this.showDetail = newShowDetail;
        this.showDetailSet = true;
    }

    /**
     * Use the showGlobalOnly attribute to display only those messages that are
     * not associated with a component id. This attribute allows you to avoid
     * showing a component error twice if you use {@code webuijsf:message}
     * and {@code webuijsf:messageGroup} in the same page.
     * @return {@code boolean}
     */
    public boolean isShowGlobalOnly() {
        if (this.showGlobalOnlySet) {
            return this.showGlobalOnly;
        }
        ValueExpression vb = getValueExpression("showGlobalOnly");
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
     * Use the showGlobalOnly attribute to display only those messages that are
     * not associated with a component id. This attribute allows you to avoid
     * showing a component error twice if you use {@code webuijsf:message}
     * and {@code webuijsf:messageGroup} in the same page.
     *
     * @see #isShowGlobalOnly()
     * @param newShowGlobalOnly showGlobalOnly
     */
    public void setShowGlobalOnly(final boolean newShowGlobalOnly) {
        this.showGlobalOnly = newShowGlobalOnly;
        this.showGlobalOnlySet = true;
    }

    /**
     * Set this attribute to true to display the summary message.
     * @return {@code boolean}
     */
    public boolean isShowSummary() {
        if (this.showSummarySet) {
            return this.showSummary;
        }
        ValueExpression vb = getValueExpression("showSummary");
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
     * Set this attribute to true to display the summary message.
     *
     * @see #isShowSummary()
     * @param newShowSummary showSummary
     */
    public void setShowSummary(final boolean newShowSummary) {
        this.showSummary = newShowSummary;
        this.showSummarySet = true;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
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
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool-tip if the mouse cursor hovers over the HTML
     * element.
     * @return String
     */
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

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool-tip if the mouse cursor hovers over the HTML
     * element.
     *
     * @see #getToolTip()
     * @param newToolTip tool-tip
     */
    public void setToolTip(final String newToolTip) {
        this.toolTip = newToolTip;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
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
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     *
     * @see #isVisible()
     * @param newVisible visible
     */
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    /**
     * Sets the title of the message group. If this attribute is not specified,
     * the default title "System Messages" will be used.
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
     * Sets the title of the message group. If this attribute is not specified,
     * the default title "System Messages" will be used.
     *
     * @see #getTitle()
     * @param newTitle title
     */
    public void setTitle(final String newTitle) {
        this.title = newTitle;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.showDetail = ((Boolean) values[1]);
        this.showDetailSet = ((Boolean) values[2]);
        this.showGlobalOnly = ((Boolean) values[3]);
        this.showGlobalOnlySet = ((Boolean) values[4]);
        this.showSummary = ((Boolean) values[5]);
        this.showSummarySet = ((Boolean) values[6]);
        this.style = (String) values[7];
        this.styleClass = (String) values[8];
        this.toolTip = (String) values[9];
        this.visible = ((Boolean) values[10]);
        this.visibleSet = ((Boolean) values[11]);
        this.title = (String) values[12];
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[13];
        values[0] = super.saveState(context);
        if (this.showDetail) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.showDetailSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.showGlobalOnly) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.showGlobalOnlySet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        if (this.showSummary) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        if (this.showSummarySet) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        values[7] = this.style;
        values[8] = this.styleClass;
        values[9] = this.toolTip;
        if (this.visible) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[11] = Boolean.TRUE;
        } else {
            values[11] = Boolean.FALSE;
        }
        values[12] = this.title;
        return values;
    }
}
