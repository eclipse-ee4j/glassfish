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
 * The Message component is used to display error and warning messages for
 * another component.
 */
@Component(type = "com.sun.webui.jsf.Message",
        family = "com.sun.webui.jsf.Message",
        displayName = "Message", tagName = "message",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_message",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_message_props")
        //CHECKSTYLE:ON
public final class Message extends UIComponentBase {

    /**
     * Description of the image rendered by this component. The alt text can be
     * used by screen readers and in tool tips, and when image display is turned
     * off in the web browser.
     */
    @Property(name = "alt",
            displayName = "Alt Text",
            category = "Accessibility",
            isHidden = true,
            isAttribute = false,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String alt;

    /**
     * Identifier for the component associated with this message component.
     */
    @Property(name = "for",
            displayName = "Input Component",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.InputComponentIdsEditor")
            //CHECKSTYLE:ON
    private String forComponent;

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
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     */
    @Property(name = "tabIndex",
            displayName = "Tab Index",
            category = "Accessibility",
            isHidden = true,
            isAttribute = false,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int tabIndex = Integer.MIN_VALUE;

    /**
     * tabIndex set flag.
     */
    private boolean tabIndexSet;

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the
     * HTML code for the component is present in the page, but the component
     * is hidden with style attributes. By default, visible is set to true, so
     * HTML for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     */
    @Property(name = "visible", displayName = "Visible", category = "Behavior")
    private boolean visible;

    /**
     * visible set flag.
     */
    private boolean visibleSet;

    /**
     * Default constructor.
     */
    public Message() {
        super();
        setRendererType("com.sun.webui.jsf.Message");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Message";
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

    /**
     * Description of the image rendered by this component.The alt text can be
     * used by screen readers and in tool tips, and when image display is turned
     * off in the web browser.
     *
     * @return String
     */
    public String getAlt() {
        if (this.alt != null) {
            return this.alt;
        }
        ValueExpression vb = getValueExpression("alt");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Description of the image rendered by this component.The alt text can be
     * used by screen readers and in tool tips, and when image display is turned
     * off in the web browser.
     *
     * @param newAlt alt
     * @see #getAlt()
     */
    public void setAlt(final String newAlt) {
        this.alt = newAlt;
    }

    /**
     * Identifier for the component associated with this message component.
     * @return String
     */
    public String getFor() {
        if (this.forComponent != null) {
            return this.forComponent;
        }
        ValueExpression vb = getValueExpression("for");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Identifier for the component associated with this message component.
     * @see #getFor()
     * @param newFormComponent forComponent
     */
    public void setFor(final String newFormComponent) {
        this.forComponent = newFormComponent;
    }

    /**
     * Set this attribute to true to display the detailed message.
     * @see #isShowDetail()
     * @param newShowDetail showDetail
     */
    public void setShowDetail(final boolean newShowDetail) {
        this.showDetail = newShowDetail;
        this.showDetailSet = true;
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
        return true;
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
        return false;
    }

    /**
     * Set this attribute to true to display the summary message.
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
     * @see #getStyleClass()
     * @param newStyleClass styleClass
     */
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

    /**
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive
     * focus when the tab key is pressed. The value must be an integer
     * between 0 and 32767.
     * @return int
     */
    public int getTabIndex() {
        if (this.tabIndexSet) {
            return this.tabIndex;
        }
        ValueExpression vb = getValueExpression("tabIndex");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive
     * focus when the tab key is pressed. The value must be an integer
     * between 0 and 32767.
     * @see #getTabIndex()
     * @param newTabIndex tabIndex
     */
    public void setTabIndex(final int newTabIndex) {
        this.tabIndex = newTabIndex;
        this.tabIndexSet = true;
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
     * viewable by the user in the rendered HTML page. If set to false, the
     * HTML code for the component is present in the page, but the component
     * is hidden with style attributes. By default, visible is set to true, so
     * HTML for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     * @see #isVisible()
     * @param newVisible visible
     */
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.alt = (String) values[1];
        this.forComponent = (String) values[2];
        this.showDetail = ((Boolean) values[3]);
        this.showDetailSet = ((Boolean) values[4]);
        this.showSummary = ((Boolean) values[5]);
        this.showSummarySet = ((Boolean) values[6]);
        this.style = (String) values[7];
        this.styleClass = (String) values[8];
        this.tabIndex = ((Integer) values[9]);
        this.tabIndexSet = ((Boolean) values[10]);
        this.visible = ((Boolean) values[11]);
        this.visibleSet = ((Boolean) values[12]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[13];
        values[0] = super.saveState(context);
        values[1] = this.alt;
        values[2] = this.forComponent;
        if (this.showDetail) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.showDetailSet) {
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
        values[9] = this.tabIndex;
        if (this.tabIndexSet) {
            values[10] = Boolean.TRUE;
        } else {
            values[10] = Boolean.FALSE;
        }
        if (this.visible) {
            values[11] = Boolean.TRUE;
        } else {
            values[11] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[12] = Boolean.TRUE;
        } else {
            values[12] = Boolean.FALSE;
        }
        return values;
    }
}
