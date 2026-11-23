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
 * The ThemeLicnks component is used to create references to theme resources on
 * a page in a portlet environment, where the Head component cannot be used.
 */
@Component(type = "com.sun.webui.jsf.ThemeLinks",
        family = "com.sun.webui.jsf.ThemeLinks",
        displayName = "ThemeLinks",
        tagName = "themeLinks",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_theme_links",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_theme_links_props")
        //CHECKSTYLE:OFF
public final class ThemeLinks extends UIComponentBase {

    /**
     * Flag (true or false) indicating that DOJO debugging is enabled. The
     * default value is false.
     */
    @Property(name = "debug",
            displayName = "Enable Dojo Debugging",
            category = "Advanced")
    private boolean debug;

    /**
     * debug set flag.
     */
    private boolean debugSet;

    /**
     * Flag (true or false) indicating that component JavaScript should be
     * output in page. The default value is true.
     */
    @Property(name = "javaScript",
            displayName = "Include Component JavaScript",
            category = "Advanced")
    private boolean javaScript = true;

    /**
     * javaScript set flag.
     */
    private boolean javaScriptSet;

    /**
     * Flag (true or false) indicating that DOJO should search for dojoType
     * widget tags. Page load time is proportional to the number of nodes on the
     * page. The default value is false.
     */
    @Property(name = "parseWidgets",
            displayName = "Parse Dojo Widgets",
            category = "Advanced")
    private boolean parseWidgets;

    /**
     * parseWidgets set flag.
     */
    private boolean parseWidgetsSet;

    /**
     * If set to true, a link element with a reference to the theme style-sheet
     * resource is rendered.
     */
    @Property(name = "styleSheet",
            displayName = "Include StyleSheet Link",
            category = "Advanced")
    private boolean styleSheet;

    /**
     * styleSheet set flag.
     */
    private boolean styleSheetSet;

    /**
     * If set to true, the theme style-sheet contents will be rendered inline
     * instead of being linked to.
     */
    @Property(name = "styleSheetInline",
            displayName = "Include StyleSheet Definitions Inline",
            category = "Advanced")
    private boolean styleSheetInline;

    /**
     * styleSheetInline set flag.
     */
    private boolean styleSheetInlineSet;

    /**
     * Holds value of property styleSheetLink.
     */
    private boolean styleSheetLink = true;

    /**
     * Default constructor.
     */
    public ThemeLinks() {
        super();
        setRendererType("com.sun.webui.jsf.ThemeLinks");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.ThemeLinks";
    }

    /**
     * Getter for property styleSheetLink.
     *
     * @return Value of property styleSheetLink.
     */
    public boolean isStyleSheetLink() {
        return this.styleSheetLink;
    }

    /**
     * Setter for property styleSheetLink.
     *
     * @param newStyleSheetLink New value of property styleSheetLink.
     */
    public void setStyleSheetLink(final boolean newStyleSheetLink) {
        this.styleSheetLink = newStyleSheetLink;
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
     * Test flag indicating that DOJO debugging is enabled.
     * @return {@code boolean}
     */
    public boolean isDebug() {
        if (this.debugSet) {
            return this.debug;
        }
        ValueExpression vb = getValueExpression("debug");
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
     * Set flag indicating that DOJO debugging is enabled.
     * @param newDebug debug
     */
    public void setDebug(final boolean newDebug) {
        this.debug = newDebug;
        this.debugSet = true;
    }

    /**
     * Test flag indicating that component JavaScript should be output in page.
     * @return {@code boolean}
     */
    public boolean isJavaScript() {
        if (this.javaScriptSet) {
            return this.javaScript;
        }
        ValueExpression vb = getValueExpression("javaScript");
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
     * Set flag indicating that component JavaScript should be output in page.
     * @param newSavaScript javaScript
     */
    public void setJavaScript(final boolean newSavaScript) {
        this.javaScript = newSavaScript;
        this.javaScriptSet = true;
    }

    /**
     * Test flag indicating that DOJO should search for dojoType widget tags.
     * @return {@code boolean}
     */
    public boolean isParseWidgets() {
        if (this.parseWidgetsSet) {
            return this.parseWidgets;
        }
        ValueExpression vb = getValueExpression("parseWidgets");
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
     * Set flag indicating that DOJO should search for dojoType widget tags.
     * @param newParseWidgets parseWidgets
     */
    public void setParseWidgets(final boolean newParseWidgets) {
        this.parseWidgets = newParseWidgets;
        this.parseWidgetsSet = true;
    }

    /**
     * If set to true, a link element with a reference to the theme style-sheet
     * resource is rendered.
     * @return {@code boolean}
     */
    public boolean isStyleSheet() {
        if (this.styleSheetSet) {
            return this.styleSheet;
        }
        ValueExpression vb = getValueExpression("styleSheet");
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
     * If set to true, a link element with a reference to the theme style-sheet
     * resource is rendered.
     *
     * @see #isStyleSheet()
     * @param newStyleSheet styleSheet
     */
    public void setStyleSheet(final boolean newStyleSheet) {
        this.styleSheet = newStyleSheet;
        this.styleSheetSet = true;
    }

    /**
     * If set to true, the theme style-sheet contents will be rendered inline
     * instead of being linked to.
     *
     * @return {@code boolean}
     */
    public boolean isStyleSheetInline() {
        if (this.styleSheetInlineSet) {
            return this.styleSheetInline;
        }
        ValueExpression vb = getValueExpression("styleSheetInline");
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
     * If set to true, the theme style-sheet contents will be rendered inline
     * instead of being linked to.
     *
     * @see #isStyleSheetInline()
     * @param newStyleSheetInline styleSheetInline
     */
    public void setStyleSheetInline(final boolean newStyleSheetInline) {
        this.styleSheetInline = newStyleSheetInline;
        this.styleSheetInlineSet = true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object _values[] = (Object[]) state;
        super.restoreState(context, _values[0]);
        this.javaScript = ((Boolean) _values[1]);
        this.javaScriptSet = ((Boolean) _values[2]);
        this.styleSheet = ((Boolean) _values[3]);
        this.styleSheetSet = ((Boolean) _values[4]);
        this.styleSheetInline = ((Boolean) _values[5]);
        this.styleSheetInlineSet = ((Boolean) _values[6]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object values[] = new Object[7];
        values[0] = super.saveState(context);
        if (this.javaScript) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.javaScriptSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.styleSheet) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.styleSheetSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        if (this.styleSheetInline) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        if (this.styleSheetInlineSet) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        return values;
    }
}
