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
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

/**
 * The PanelGroup component is used to arrange a group of components.
 */
@Component(type = "com.sun.webui.jsf.PanelGroup",
        family = "com.sun.webui.jsf.PanelGroup",
        displayName = "Group Panel",
        instanceName = "groupPanel",
        tagName = "panelGroup",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_group_panel",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_panel_group_props")
        //CHECKSTYLE:ON
public final class PanelGroup extends UIComponentBase
        implements NamingContainer {

    /**
     * Separator facet.
     */
    public static final String SEPARATOR_FACET = "separator";

    /**
     * By default, the panelGroup component is rendered on the same line as the
     * component that comes before it and the component that follows, in a flow
     * layout. If the block attribute is set to true, the panelGroup component
     * is rendered on its own line. The components before it and after it are on
     * different lines. The block attribute has no effect on the panelGroup
     * component's children.
     */
    @Property(name = "block",
            displayName = "Block",
            category = "Appearance")
    private boolean block;

    /**
     * block set flag.
     */
    private boolean blockSet;

    /**
     * The string of characters or HTML element that should be inserted between
     * each component that is a child of this component. To specify an HTML
     * element, use the character entities &amp;lt; and &amp;gt; to produce the
     * &lt; and &gt; characters. You can use a block element such as &lt;p&gt;
     * or &lt;br&gt; to force each component to be rendered on a separate line.
     * If the separator attribute is not specified, the components are rendered
     * with a single space between them.
     */
    @Property(name = "separator",
            displayName = "Separator",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String separator;

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
     * Default constructor.
     */
    public PanelGroup() {
        super();
        setRendererType("com.sun.webui.jsf.PanelGroup");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.PanelGroup";
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
     * By default, the panelGroup component is rendered on the same line as the
     * component that comes before it and the component that follows, in a flow
     * layout. If the block attribute is set to true, the panelGroup component
     * is rendered on its own line. The components before it and after it are on
     * different lines. The block attribute has no effect on the panelGroup
     * component's children.
     * @return {@code boolean}
     */
    public boolean isBlock() {
        if (this.blockSet) {
            return this.block;
        }
        ValueExpression vb = getValueExpression("block");
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
     * By default, the panelGroup component is rendered on the same line as the
     * component that comes before it and the component that follows, in a flow
     * layout. If the block attribute is set to true, the panelGroup component
     * is rendered on its own line. The components before it and after it are on
     * different lines. The block attribute has no effect on the panelGroup
     * component's children.
     *
     * @see #isBlock()
     * @param newBlock block
     */
    public void setBlock(final boolean newBlock) {
        this.block = newBlock;
        this.blockSet = true;
    }

    /**
     * The string of characters or HTML element that should be inserted between
     * each component that is a child of this component. To specify an HTML
     * element, use the character entities &amp;lt; and &amp;gt; to produce the
     * &lt; and &gt; characters. You can use a block element such as &lt;p&gt;
     * or &lt;br&gt; to force each component to be rendered on a separate line.
     * If the separator attribute is not specified, the components are rendered
     * with a single space between them.
     * @return String
     */
    public String getSeparator() {
        if (this.separator != null) {
            return this.separator;
        }
        ValueExpression vb = getValueExpression("separator");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The string of characters or HTML element that should be inserted between
     * each component that is a child of this component. To specify an HTML
     * element, use the character entities &amp;lt; and &amp;gt; to produce the
     * &lt; and &gt; characters. You can use a block element such as &lt;p&gt;
     * or &lt;br&gt; to force each component to be rendered on a separate line.
     * If the separator attribute is not specified, the components are rendered
     * with a single space between them.
     *
     * @see #getSeparator()
     * @param newSeparator separator
     */
    public void setSeparator(final String newSeparator) {
        this.separator = newSeparator;
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

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.block = ((Boolean) values[1]);
        this.blockSet = ((Boolean) values[2]);
        this.separator = (String) values[3];
        this.style = (String) values[4];
        this.styleClass = (String) values[5];
        this.visible = ((Boolean) values[6]);
        this.visibleSet = ((Boolean) values[7]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[8];
        values[0] = super.saveState(context);
        if (this.block) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.blockSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        values[3] = this.separator;
        values[4] = this.style;
        values[5] = this.styleClass;
        if (this.visible) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        return values;
    }
}
