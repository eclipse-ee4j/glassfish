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
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.beans.Beans;
import java.util.Iterator;
import java.util.List;

/**
 * The CommonTasksSection component is used to present a number of tasks that
 * might commonly be performed by the user.
 */
@Component(type = "com.sun.webui.jsf.CommonTasksSection",
        family = "com.sun.webui.jsf.CommonTasksSection",
        displayName = "Common Tasks Section",
        instanceName = "commonTasksSection",
        tagName = "commonTasksSection")
public final class CommonTasksSection
        extends jakarta.faces.component.UIComponentBase
        implements NamingContainer {

    /**
     * Inline help text facet.
     */
    public static final String HELP_INLINE_FACET = "help";

    /**
     * Section help.
     */
    public static final String SECTION_HELP = "commonTasks.sectionHelp";

    /**
     * UI component.
     */
    private UIComponent component;

    /**
     * The help text to be displayed for the common tasks section.
     */
    @Property(name = "helpText",
            displayName = "Inline help to be displayed",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String helpText;

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
     * The title text to be displayed for the common tasks section.
     */
    @Property(name = "title",
            displayName = "Common Tasks section Text",
            category = "Appearance",
            isDefault = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String title;

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
     * Specify the number of task columns to display in the common tasks
     * section.
     */
    @Property(name = "columns",
            displayName = "columns",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.IntegerPropertyEditor")
            //CHECKSTYLE:ON
    private int columns = Integer.MIN_VALUE;

    /**
     * columns set flag.
     */
    private boolean columnsSet;

    /**
     * Creates a new instance of CommonTasksSection.
     */
    public CommonTasksSection() {
        super();
        setRendererType("com.sun.webui.jsf.CommonTasksSection");

    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.CommonTasksSection";
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
     * The help text to be displayed for the common tasks section.
     * @return String
     */
    public String getHelpText() {
        if (this.helpText != null) {
            return this.helpText;
        }
        ValueExpression vb = getValueExpression("helpText");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The help text to be displayed for the common tasks section.
     *
     * @see #getHelpText()
     * @param newHelpText helpText
     */
    public void setHelpText(final String newHelpText) {
        this.helpText = newHelpText;
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
     * The title text to be displayed for the common tasks section.
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
     * The title text to be displayed for the common tasks section.
     *
     * @see #getTitle()
     * @param newTitle title
     */
    public void setTitle(final String newTitle) {
        this.title = newTitle;
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
     * Specify the number of task columns to display in the common tasks
     * section.
     * @return int
     */
    public int getColumns() {
        if (this.columnsSet) {
            return this.columns;
        }
        ValueExpression vb = getValueExpression("columns");
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
     * Specify the number of task columns to display in the common tasks
     * section.
     *
     * @see #getColumns()
     * @param newColumns columns
     */
    public void setColumns(final int newColumns) {
        this.columns = newColumns;
        this.columnsSet = true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.helpText = (String) values[1];
        this.style = (String) values[2];
        this.styleClass = (String) values[3];
        this.title = (String) values[4];
        this.visible = ((Boolean) values[5]);
        this.visibleSet = ((Boolean) values[6]);
        this.columns = ((Integer) values[7]);
        this.columnsSet = ((Boolean) values[8]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[9];
        values[0] = super.saveState(context);
        values[1] = this.helpText;
        values[2] = this.style;
        values[3] = this.styleClass;
        values[4] = this.title;
        if (this.visible) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        if (this.columnsSet) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        values[7] = this.columns;
        return values;
    }

    /**
     * Return the total number of
     * {@link com.sun.webui.jsf.component.CommonTask}s that are present in a
     * {@link CommonTasksSection} This gives the number of visible
     * {@link com.sun.webui.jsf.component.CommonTask} elements and does not take
     * into account the ones that have rendered or visible attribute set to
     * false.
     *
     * @return number of visible commonTask elements on the page
     */
    public int getCommonTaskCount() {
        int totalCount = 0;
        int tmp;
        CommonTasksGroup ctg;
        List children = this.getChildren();

        if (children.size() > 0) {
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i) instanceof CommonTasksGroup) {
                    ctg = (CommonTasksGroup) children.get(i);
                    // Get the number of commonTask elements for each
                    // commonTasksGroup
                    // that are to be rendered and are visible.
                    tmp = getSingleGroupTaskCount(ctg);
                    if (ctg.isRendered() && ctg.isVisible()) {
                        totalCount += tmp;
                    }
                } else {
                    // If some other component is put as a child of the
                    // commonTasksSection, we just take it as a single component
                    // and increment the total count by one.
                    totalCount++;
                }
            }
        }

        return totalCount;
    }

    /**
     * Get the help facet for the {@link CommonTasksSection}. If a developer
     * specified facet exists, use it or otherwise use the default facet.
     *
     * @param context The faces context.
     * @return An help component to be displayed below the title.
     */
    public UIComponent getHelp(final FacesContext context) {
        component = this.getFacet(HELP_INLINE_FACET);

        if (component != null) {
            return component;
        }
        Theme theme = ThemeUtilities.getTheme(context);
        HelpInline hil;
        UIComponent comp = ComponentUtilities.getPrivateFacet(this,
                HELP_INLINE_FACET, true);

        if (comp == null) {
            hil = new HelpInline();
            hil.setType("page");
            hil.setId(ComponentUtilities.createPrivateFacetId(this,
                    HELP_INLINE_FACET));
            ComponentUtilities.putPrivateFacet(this, HELP_INLINE_FACET, hil);
            comp = hil;
        }

        try {
            hil = (HelpInline) comp;
            if (getHelpText() == null) {
                hil.setText(theme.getMessage(SECTION_HELP));
            } else {
                hil.setText(getHelpText());
            }
        } catch (ClassCastException e) {
        }
        return comp;
    }

    /**
     * Returns the number of {@link commonTask} components contained in a
     * {@link commonTasksGroup}. Check whether at least one of the
     * {@link commonTask} for a particular {@link commonTasksGroup} is to be
     * rendered. Otherwise set the rendered attribute of that particular
     * {@link commonTasksGroup} to false.
     *
     * @param group The commonTasksGroup for which the number of commonTasks
     * should be calculated
     * @return The number of visible commonTask components.
     */
    private int getSingleGroupTaskCount(final CommonTasksGroup group) {
        int count = 0;
        boolean flag = false;
        Iterator it = group.getChildren().iterator();
        while (it.hasNext()) {
            component = (UIComponent) it.next();
            if (component.isRendered()) {
                count++;
                flag = true;
            }

            if (component instanceof CommonTask) {
                if (((CommonTask) component).isVisible()) {
                    flag = true;
                }
            }
        }
        if (!Beans.isDesignTime()) {
            if (!flag) {
                group.setRendered(false);
            }
        }
        return count;
    }
}
