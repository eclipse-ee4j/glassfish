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

import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;

/**
 * The SkipLink component is used to create a single-pixel transparent image
 * (not visible within the browser page) which is hyperlinked to an anchor
 * beyond the section to skip.
 */
@Component(type = "com.sun.webui.jsf.SkipHyperlink",
        family = "com.sun.webui.jsf.SkipHyperlink",
        displayName = "Skip Hyperlink",
        tagName = "skipHyperlink",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_skip_hyperlink",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_skip_hyperlink_props")
        //CHECKSTYLE:ON
public final class SkipHyperlink extends WebuiCommand {

    /**
     * Use the description attribute to provide text that describes the purpose
     * of the skip hyperlink. The description should indicate which section is
     * skipped when the link is clicked. The text is rendered as the alt text
     * for the image.
     */
    @Property(name = "description",
            displayName = "Description",
            category = "Appearance")
    private String description;

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
     * Construct a new {@code SkipHyperlink}.
     */
    public SkipHyperlink() {
        super();
        setRendererType("com.sun.webui.jsf.SkipHyperlink");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.SkipHyperlink";
    }

    // Hide actionExpression
    @Property(name = "actionExpression",
            isHidden = true,
            isAttribute = false)
    @Override
    public MethodExpression getActionExpression() {
        return super.getActionExpression();
    }

    // Hide actionListenerExpression
    @Property(name = "actionListenerExpression",
            isHidden = true,
            isAttribute = false)
    @Override
    public MethodExpression getActionListenerExpression() {
        return super.getActionListenerExpression();
    }

    // Hide immediate
    @Property(name = "immediate",
            isHidden = true,
            isAttribute = false)
    @Override
    public boolean isImmediate() {
        return super.isImmediate();
    }

    // Hide Value
    @Property(name = "value",
            isHidden = true,
            isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * Use the description attribute to provide text that describes the purpose
     * of the skip hyperlink. The description should indicate which section is
     * skipped when the link is clicked. The text is rendered as the alt text
     * for the image.
     * @return String
     */
    public String getDescription() {
        if (this.description != null) {
            return this.description;
        }
        ValueExpression vb = getValueExpression("description");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use the description attribute to provide text that describes the purpose
     * of the skip hyperlink. The description should indicate which section is
     * skipped when the link is clicked. The text is rendered as the alt text
     * for the image.
     *
     * @see #getDescription()
     * @param newDescription description
     */
    public void setDescription(final String newDescription) {
        this.description = newDescription;
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
     * @see #getStyleClass()
     * @param newStyleClass styleClass
     */
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

    /**
     * Position of this element in the tabbing order of the current document.
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
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
     * Tabbing order determines the sequence in which elements receive focus
     * when the tab key is pressed. The value must be an integer between 0 and
     * 32767.
     *
     * @see #getTabIndex()
     * @param newTabIndex tabIndex
     */
    public void setTabIndex(final int newTabIndex) {
        this.tabIndex = newTabIndex;
        this.tabIndexSet = true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.description = (String) values[1];
        this.style = (String) values[2];
        this.styleClass = (String) values[3];
        this.tabIndex = ((Integer) values[4]);
        this.tabIndexSet = ((Boolean) values[5]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[6];
        values[0] = super.saveState(context);
        values[1] = this.description;
        values[2] = this.style;
        values[3] = this.styleClass;
        values[4] = this.tabIndex;
        if (this.tabIndexSet) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        return values;
    }
}
