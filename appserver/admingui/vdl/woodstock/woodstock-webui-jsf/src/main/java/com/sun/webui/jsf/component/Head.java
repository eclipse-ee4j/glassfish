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
 * The Head component is used to provide information to be used in the head
 * element.
 */
@Component(type = "com.sun.webui.jsf.Head",
        family = "com.sun.webui.jsf.Head",
        displayName = "Head",
        tagName = "head",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_head",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_head_props")
        //CHECKSTYLE:ON
public final class Head extends UIComponentBase {

    /**
     * Flag (true or false) indicating that DOJO debugging is enabled. The
     * default value is false.
     */
    @Property(name = "debug",
            displayName = "Enable Dojo Debugging",
            category = "Advanced")
    private boolean debug = false;

    /**
     * debug set flag.
     */
    private boolean debugSet = false;

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
    private boolean javaScriptSet = false;

    /**
     * Flag (true or false) indicating that DODO should search for dojoType
     * widget tags. Page load time is proportional to the number of nodes on the
     * page. The default value is false.
     */
    @Property(name = "parseWidgets",
            displayName = "Parse Dojo Widgets",
            category = "Advanced")
    private boolean parseWidgets = false;

    /**
     * parseWidgets set flag.
     */
    private boolean parseWidgetsSet = false;

    /**
     * Flag (true or false) indicating if meta data should be rendered. The
     * default value is true.
     */
    @Property(name = "meta",
            displayName = "Render Meta Data",
            category = "Advanced")
    private boolean meta = false;

    /**
     * meta set flag.
     */
    private boolean metaSet = false;

    /**
     * Flag (true or false) indicating that a default HTML base tag should be
     * shown or not. Changing this attribute could cause {@code webuijsf:anchor}
     * to not work properly. The default value is false.
     */
    @Property(name = "defaultBase",
            displayName = "Default Base",
            category = "Appearance")
    private boolean defaultBase = false;

    /**
     * defaultBase set flag.
     */
    private boolean defaultBaseSet = false;

    /**
     * A space separated list of URL's that contains meta data information about
     * the page.
     */
    @Property(name = "profile",
            displayName = "Profile",
            category = "Advanced")
    private String profile = null;

    /**
     * Title of the document to be displayed in the browser title bar.
     */
    @Property(name = "title",
            displayName = "title",
            category = "Appearance",
            isDefault = true)
    private String title = null;

    /**
     * Construct a new {@code Head}.
     */
    public Head() {
        super();
        setRendererType("com.sun.webui.jsf.Head");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Head";
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
     * @param newJavaScript javaScript
     */
    public void setJavaScript(final boolean newJavaScript) {
        this.javaScript = newJavaScript;
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
     * Test flag indicating if the default meta data should be rendered.
     * @return {@code boolean}
     */
    public boolean isMeta() {
        if (this.metaSet) {
            return this.meta;
        }
        ValueExpression vb = getValueExpression("meta");
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
     * Set flag indicating if the default meta data should be rendered.
     * @param newMeta meta
     */
    public void setMeta(final boolean newMeta) {
        this.meta = newMeta;
        this.metaSet = true;
    }

    /**
     * Flag (true or false) indicating that a default HTML base tag should be
     * shown or not. Changing this attribute could cause {@code webuijsf:anchor}
     * to not work properly. The default value is false.
     *
     * @return {@code boolean}
     */
    public boolean isDefaultBase() {
        if (this.defaultBaseSet) {
            return this.defaultBase;
        }
        ValueExpression vb = getValueExpression("defaultBase");
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
     * Flag (true or false) indicating that a default HTML base tag should be
     * shown or not. Changing this attribute could cause {@code webuijsf:anchor}
     * to not work properly. The default value is false.
     *
     * @see #isDefaultBase()
     * @param newDefaultBase defaultBase
     */
    public void setDefaultBase(final boolean newDefaultBase) {
        this.defaultBase = newDefaultBase;
        this.defaultBaseSet = true;
    }

    /**
     * A space separated list of URL's that contains meta data information about
     * the page.
     * @return String
     */
    public String getProfile() {
        if (this.profile != null) {
            return this.profile;
        }
        ValueExpression vb = getValueExpression("profile");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * A space separated list of URL's that contains meta data information about
     * the page.
     *
     * @see #getProfile()
     * @param newProfile profile
     */
    public void setProfile(final String newProfile) {
        this.profile = newProfile;
    }

    /**
     * Title of the document to be displayed in the browser title bar.
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
     * Title of the document to be displayed in the browser title bar.
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
        this.defaultBase = ((Boolean) values[1]);
        this.defaultBaseSet = ((Boolean) values[2]);
        this.profile = (String) values[3];
        this.title = (String) values[4];
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[5];
        values[0] = super.saveState(context);
        if (this.defaultBase) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.defaultBaseSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        values[3] = this.profile;
        values[4] = this.title;
        return values;
    }
}
