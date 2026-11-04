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
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;

/**
 * The HelpWindow component displays a link that opens a popup window for a help
 * system.
 */
@Component(type = "com.sun.webui.jsf.HelpWindow",
        family = "com.sun.webui.jsf.HelpWindow",
        displayName = "Help Window",
        tagName = "helpWindow",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_help_window",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_help_window_props")
        //CHECKSTYLE:ON
public final class HelpWindow extends IconHyperlink {

    /**
     * Default JSP path.
     */
    public static final String DEFAULT_JSP_PATH
            = "/com_sun_webui_jsf/help/";

    /**
     * Navigation JSP.
     */
    public static final String DEFAULT_NAVIGATION_JSP = "navigator.jsp";

    /**
     * Status JSP.
     */
    public static final String DEFAULT_STATUS_JSP = "status.jsp";

    /**
     * Button nav JSP.
     */
    public static final String DEFAULT_BUTTONNAV_JSP = "buttonnav.jsp";

    /**
     * Button frame JSP.
     */
    public static final String DEFAULT_BUTTONFRAME_JSP = "buttonFrame.jsp";

    /**
     * TIP JSP.
     */
    public static final String DEFAULT_TIPS_FILE = "tips.jsp";

    /**
     * The help file to be displayed in the help window content frame when the
     * help link is clicked. The value can be a relative path or a file
     * name.
     */
    @Property(name = "helpFile",
            displayName = "Help File")
    private String helpFile = null;

    /**
     * The context relative path to the help set to be displayed. This attribute
     * overrides any value set for the helpSetPath property in the application's
     * HelpBackingBean instance.
     */
    @Property(name = "helpSetPath",
            displayName = "Help Set Path")
    private String helpSetPath = null;

    /**
     * Set linkIcon to true to display the default icon in front of the text for
     * the help window link. The icon is useful in inline help links to the help
     * window. By default the value is false.
     */
    @Property(name = "linkIcon", displayName = "Link Icon")
    private boolean linkIcon = false;

    /**
     * linkIcon set flag.
     */
    private boolean linkIconSet = false;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)")
    private String style = null;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "styleClass",
            displayName = "CSS Style Class(es)")
    private String styleClass = null;

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
            displayName = "Visible")
    private boolean visible = false;

    /**
     * visible set flag.
     */
    private boolean visibleSet = false;

    /**
     * The text to display in the browser window frame for the help window. This
     * text is rendered in the HTML title element.
     */
    @Property(name = "windowTitle",
            displayName = "Help Window Title")
    private String windowTitle = null;

    /**
     * Creates a new instance of HelpWindow.
     */
    public HelpWindow() {
        super();
        setRendererType("com.sun.webui.jsf.HelpWindow");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.HelpWindow";
    }

    @Override
    public ValueExpression getValueExpression(final String name) {
        if (name.equals("linkText")) {
            return super.getValueExpression("text");
        }
        return super.getValueExpression(name);
    }

    @Override
    public void setValueExpression(final String name,
            final ValueExpression binding) {

        if (name.equals("linkText")) {
            super.setValueExpression("text", binding);
            return;
        }
        super.setValueExpression(name, binding);
    }

    // Hide actionExpression
    @Property(name = "actionExpression",
            isHidden = true,
            isAttribute = false)
    @Override
    public MethodExpression getActionExpression() {
        return super.getActionExpression();
    }

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     * @return String
     */
    @Property(name = "onDblClick",
            isHidden = false,
            isAttribute = true)
    @Override
    public String getOnDblClick() {
        return super.getOnDblClick();
    }

    // Hide actionListenerExpression
    @Property(name = "actionListenerExpression",
            isHidden = true,
            isAttribute = false)
    @Override
    public MethodExpression getActionListenerExpression() {
        return super.getActionListenerExpression();
    }

    // Hide Align
    @Property(name = "Align",
            isHidden = true,
            isAttribute = false)
    @Override
    public String getAlign() {
        return super.getAlign();
    }

    // Hide alt
    @Property(name = "alt",
            isHidden = true,
            isAttribute = false)
    @Override
    public String getAlt() {
        return super.getAlt();
    }

    // Hide border
    @Property(name = "border",
            isHidden = true,
            isAttribute = false)
    @Override
    public int getBorder() {
        return super.getBorder();
    }

    // Hide height
    @Property(name = "height",
            isHidden = true,
            isAttribute = false)
    @Override
    public int getHeight() {
        return super.getHeight();
    }

    // Hide hspace
    @Property(name = "hspace",
            isHidden = true,
            isAttribute = false)
    @Override
    public int getHspace() {
        return super.getHspace();
    }

    // Hide icon
    @Property(name = "icon",
            isHidden = true,
            isAttribute = false)
    @Override
    public String getIcon() {
        if (isLinkIcon()) {
            // return the default help window link icon
            return ThemeImages.HREF_LINK;
        }
        // don't display an icon
        return ThemeImages.DOT;
    }

    // Hide imageURL
    @Property(name = "imageURL",
            isHidden = true,
            isAttribute = false)
    @Override
    public String getImageURL() {
        return super.getImageURL();
    }

    // Hide immediate
    @Property(name = "immediate",
            isHidden = true,
            isAttribute = false)
    @Override
    public boolean isImmediate() {
        return super.isImmediate();
    }

    // Hide target
    @Property(name = "target",
            isHidden = false,
            isAttribute = true)
    @Override
    public String getTarget() {
        if (super.getTarget() != null) {
            return super.getTarget();
        }
        return "help_window";
    }

    @Override
    public void setTarget(final String value) {
        super.setTarget(value);
    }

    // Hide text
    @Property(name = "text",
            isHidden = true,
            isAttribute = false)
    @Override
    public Object getText() {
        if (super.getText() != null) {
            return super.getText();
        }
        Theme t = ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
        return t.getMessage("help.help");
    }

    // hide textPosition
    @Property(name = "textPosition",
            isHidden = true,
            isAttribute = false)
    @Override
    public String getTextPosition() {
        return super.getTextPosition();
    }

    // Hide type
    @Property(name = "type",
            isHidden = true,
            isAttribute = false)
    @Override
    public String getType() {
        return super.getType();
    }

    // Hide url
    @Property(name = "url",
            isHidden = true,
            isAttribute = false)
    @Override
    public String getUrl() {
        if (super.getUrl() != null) {
            return super.getUrl();
        }
        // Every request to a JSF based JSP page
        // should be checked to ensure that it invokes
        // "ViewHandler.getActionURL(FacesContext ctx, String viewId)".
        // This is the *only* way this should be implemented as it fixes
        // the bug,
        // and follows JSF's design allowing the ViewHandler to translate
        // the url as required.
        String jspPath = DEFAULT_JSP_PATH;
        FacesContext context = FacesContext.getCurrentInstance();

        // Path prefix if set should be done from the JavaHelpBackingBean
        // only.
        // Having two different places to set the same info is confusing.
        // By default
        // it is assumed that the path is set to
        // <appcontext>/com_sun_webui_jsf/help.
        // If developer wants the path prefix to be set to anything else
        // the default
        // should be overidden by supplying a managed bean property for
        // "jspPath".
        // The assumtion here is that all help related data reside in the same
        // place for a given app.
        Application app = context.getApplication();
        ValueExpression vb
                = app.getExpressionFactory()
                        .createValueExpression(getFacesContext().getELContext(),
                                "#{JavaHelpBean.jspPath}", Object.class);

        if (vb.getValue(context.getELContext()) != null) {
            jspPath = ((String) vb.getValue(context.getELContext()))
                    .concat(jspPath);
        }

        StringBuilder url = new StringBuilder(jspPath);
        url.append("helpwindow.jsp");

        // renderer will assign the required request parameters and after
        // invoking handler.getActionUrl()
        return url.toString();
    }

    // Hide urlLang
    @Property(name = "urlLang",
            isHidden = true,
            isAttribute = false)
    @Override
    public String getUrlLang() {
        return super.getUrlLang();
    }

    // Hide value
    @Property(name = "value",
            isHidden = true,
            isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    // Hide vspace
    @Property(name = "vspace",
            isHidden = true,
            isAttribute = false)
    @Override
    public int getVspace() {
        return super.getVspace();
    }

    /**
     * Get the help file.
     * @return String
     */
    public String getHelpFile() {
        if (this.helpFile != null) {
            return this.helpFile;
        }
        ValueExpression vb = getValueExpression("helpFile");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The help file to be displayed in the help window content frame when the
     * help link is clicked. The value can be a relative path or a file
     * name.
     *
     * @see #getHelpFile()
     * @param newHelpFile helpFile
     */
    public void setHelpFile(final String newHelpFile) {
        this.helpFile = newHelpFile;
    }

    /**
     * Get the help set path.
     * @return String
     */
    public String getHelpSetPath() {
        if (this.helpSetPath != null) {
            return this.helpSetPath;
        }
        ValueExpression vb = getValueExpression("helpSetPath");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The context relative path to the help set to be displayed. This attribute
     * overrides any value set for the helpSetPath property in the application's
     * HelpBackingBean instance.
     *
     * @see #getHelpSetPath()
     * @param newHelpSetPath helpSetPath
     */
    public void setHelpSetPath(final String newHelpSetPath) {
        this.helpSetPath = newHelpSetPath;
    }

    /**
     * Get the linkIcon flag value.
     * @return {@code boolean}
     */
    public boolean isLinkIcon() {
        if (this.linkIconSet) {
            return this.linkIcon;
        }
        ValueExpression vb = getValueExpression("linkIcon");
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
     * Set linkIcon to true to display the default icon in front of the text for
     * the help window link. The icon is useful in inline help links to the help
     * window. By default the value is false.
     *
     * @see #isLinkIcon()
     * @param newLinkIcon linkIcon
     */
    public void setLinkIcon(final boolean newLinkIcon) {
        this.linkIcon = newLinkIcon;
        this.linkIconSet = true;
    }

    /**
     * The text to display for the hyperlink that opens the help window.
     * @return String
     */
    @Property(name = "linkText",
            displayName = "Link Text")
    public String getLinkText() {
        return (String) getText();
    }

    /**
     * The text to display for the hyperlink that opens the help window.
     *
     * @see #getLinkText()
     * @param linkText linkText
     */
    public void setLinkText(final String linkText) {
        setText((Object) linkText);
    }

    @Override
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

    @Override
    public void setStyle(final String newStyle) {
        this.style = newStyle;
    }

    @Override
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

    @Override
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

    @Override
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
    @Override
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    /**
     * Get the window title.
     * @return String
     */
    public String getWindowTitle() {
        if (this.windowTitle != null) {
            return this.windowTitle;
        }
        ValueExpression vb = getValueExpression("windowTitle");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        // else return the default help window title.
        Theme theme = ThemeUtilities
                .getTheme(FacesContext.getCurrentInstance());
        return theme.getMessage("help.help");
    }

    /**
     * The text to display in the browser window frame for the help window. This
     * text is rendered in the HTML title element.
     *
     * @see #getWindowTitle()
     * @param newWindowTitle windowTitle
     */
    public void setWindowTitle(final String newWindowTitle) {
        this.windowTitle = newWindowTitle;
    }

    @Override
    public String getOnClick() {
        String clickHandler = super.getOnClick();
        if (clickHandler != null && clickHandler.length() > 0) {
            return clickHandler;
        } else {
            StringBuilder onClick = new StringBuilder("javascript: ");
            onClick.append("var win = window.open('','")
                    .append(getTarget())
                    .append("','height=500,")
                    //CHECKSTYLE:OFF
                    .append("width=750,top='+((screen.height-(screen.height/1.618))-")
                    .append("(500/2))+',left='+((screen.width-750)/2)+',resizable');")
                    //CHECKSTYLE:ON
                    .append("win.focus()");
            return onClick.toString();
        }
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.helpFile = (String) values[1];
        this.helpSetPath = (String) values[2];
        this.linkIcon = ((Boolean) values[3]);
        this.linkIconSet = ((Boolean) values[4]);
        this.style = (String) values[5];
        this.styleClass = (String) values[6];
        this.visible = ((Boolean) values[7]);
        this.visibleSet = ((Boolean) values[8]);
        this.windowTitle = (String) values[9];
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[10];
        values[0] = super.saveState(context);
        values[1] = this.helpFile;
        values[2] = this.helpSetPath;
        if (this.linkIcon) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.linkIconSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        values[5] = this.style;
        values[6] = this.styleClass;
        if (this.visible) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        values[9] = this.windowTitle;
        return values;
    }
}
