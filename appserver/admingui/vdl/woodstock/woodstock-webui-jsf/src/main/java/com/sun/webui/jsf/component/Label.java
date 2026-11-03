/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

import java.beans.Beans;
import java.util.Iterator;
import java.util.List;

/**
 * The Label component displays a label for a component.
 */
@Component(type = "com.sun.webui.jsf.Label",
        family = "com.sun.webui.jsf.Label",
        displayName = "Label",
        tagName = "label",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_label",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_label_props")
        //CHECKSTYLE:ON
public final class Label extends UIOutput implements NamingContainer {

    /**
     * Required suffix.
     */
    public static final String REQUIRED_ID = "_required";

    /**
     * Required facet.
     */
    public static final String REQUIRED_FACET = "required";

    /**
     * Error suffix.
     */
    public static final String ERROR_ID = "_error";

    /**
     * Error facet.
     */
    public static final String ERROR_FACET = "error";

    /**
     * Editable value.
     */
    private EditableValueHolder labeledComponent = null;

    /**
     * HTML element.
     */
    private String element = "span";

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Use this attribute to specify the labeled component. The value of the
     * attribute is the absolute client id of the component or the id of the
     * component to be labeled. Relative ids are no longer supported. If this
     * attribute is not specified, the {@code label} component tries to search
     * its children to see whether any of them can be used for evaluating the
     * value of this "for" attribute.
     */
    @Property(name = "for",
            displayName = "Input Component",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.InputComponentIdsEditor")
            //CHECKSTYLE:ON
    private String forComp = null;

    /**
     * Use the hideIndicators attribute to prevent display of the required and
     * invalid icons with the label. When the required attribute on the
     * component to be labeled is set to true, the required icon is displayed
     * next to the label. If the user submits the page with an invalid value for
     * the component, the invalid icon is displayed. This attribute is useful
     * when the component has more than one label, and only one label should
     * show the icons.
     */
    @Property(name = "hideIndicators",
            displayName = "Hide the Required and Invalid icons",
            category = "Advanced")
    private boolean hideIndicators = false;

    /**
     * hideIndicateors set flag.
     */
    private boolean hideIndicatorsSet = false;

    /**
     * Style level for this label, where lower values typically specify
     * progressively larger font sizes, and/or bolder font weights. Valid values
     * are 1, 2, and 3. The default label level is 2. Any label level outside
     * this range will result in no label level being added.
     */
    @Property(name = "labelLevel",
            displayName = "Style Level",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.LabelLevelsEditor")
            //CHECKSTYLE:ON
    private int labelLevel = Integer.MIN_VALUE;

    /**
     * labelLevel set flag.
     */
    private boolean labelLevelSet = false;

    /**
     * Scripting code executed when a mouse click occurs over this
     * component.
     */
    @Property(name = "onClick",
            displayName = "Click Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onClick = null;

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseDown",
            displayName = "Mouse Down Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseDown = null;

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     */
    @Property(name = "onMouseMove",
            displayName = "Mouse Move Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseMove = null;

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     */
    @Property(name = "onMouseOut",
            displayName = "Mouse Out Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseOut = null;

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     */
    @Property(name = "onMouseOver",
            displayName = "Mouse In Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseOver = null;

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseUp",
            displayName = "Mouse Up Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseUp = null;

    /**
     * Flag indicating that the labeled component should be marked as required.
     * It is only relevant if the labeled component is not a child of the label
     * tag. Set this flag to ensure that the required icon shows up the first
     * time the page is rendered.
     */
    @Property(name = "requiredIndicator",
            displayName = "Required Field Indicator",
            category = "Appearance")
    private boolean requiredIndicator = false;

    /**
     * requiredIndicator set flag.
     */
    private boolean requiredIndicatorSet = false;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
    private String style = null;

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
    private String styleClass = null;

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     */
    @Property(name = "toolTip",
            displayName = "Tool Tip",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String toolTip = null;

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
    private boolean visible = false;

    /**
     * visible set flag.
     */
    private boolean visibleSet = false;

    /**
     * Default constructor.
     */
    public Label() {
        super();
        setRendererType("com.sun.webui.jsf.Label");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Label";
    }

    @Property(name = "converter")
    @Override
    public void setConverter(final Converter converter) {
        super.setConverter(converter);
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

    // Hide value
    @Property(name = "value", isHidden = true, isAttribute = false)
    @Override
    public Object getValue() {
        return super.getValue();
    }

    /**
     * Set the labeled component to {@code comp}.If {@code comp} is null, the
     * labeled component is set to null. If {@code comp} is an instance of
     * {@code EditableValueHolder} the
     * labeled component is set to {@code comp} and {@code setFor} is
     * called with the value of {@code comp.getClientId}. Subsequent calls
     * to {@code getElement} will return "label".
     * If {@code comp} is not an {@code EditableValueHolder} the
     * labeled component is set to null, and subsequent calls to
     * {@code getElement} will return "label".
     *
     * @param comp UI component
     * @deprecated
     * @see #setFor
     */
    public void setLabeledComponent(final UIComponent comp) {
        if (DEBUG) {
            log("setLabeledComponent");
        }
        if (comp == null) {
            if (DEBUG) {
                log("component is null");
            }
            this.labeledComponent = null;
        } else if (comp instanceof EditableValueHolder) {
            if (DEBUG) {
                log("Component is EditableValueHolder");
            }
            this.labeledComponent = (EditableValueHolder) comp;
            if (!Beans.isDesignTime()) {
                this.setFor(comp.getClientId(
                        FacesContext.getCurrentInstance()));
            }
            element = "label";
        } else {
            if (DEBUG) {
                log("Component is not an EditableValueHolder");
            }
            if (LogUtil.infoEnabled(Label.class)) {
                FacesContext context = FacesContext.getCurrentInstance();
                LogUtil.info(Label.class, "Label.invalidFor",
                        new Object[]{getId(),
                            context.getViewRoot().getViewId(),
                            comp.getId()});
            }
            this.labeledComponent = null;
            element = "label";
        }
    }

    /**
     * Return the labeled component instance. If the labeled component has not
     * been set and {@code getFor} returns null, return the first
     * {@code EditableValueHolder} child of this {@code Label}
     * component.
     * If {@code getFor} does not return null and the value is a not an
     * absolute id, search for the labeled component using
     * {@code UIComponentBase.findComponent}. Otherwise search for the
     * labeled component from the view root using
     * {@code UIComponentBase.findComponent} (In this case the id is
     * ensured to have ":" prepended to the id to cause
     * {@code findComponent} to search from the view root).
     * {@code setLabeledComponent} is called to set the labeled component
     * to the component that is found or null.
     *
     * @return the labeled component instance or null
     * @deprecated
     * @see #getFor()
     */
    public EditableValueHolder getLabeledComponent() {
        if (DEBUG) {
            log("getLabeledComponent for label " + String.valueOf(getText()));
        }
        if (labeledComponent != null) {
            if (DEBUG) {
                log("Found component ");
            }
            if (DEBUG) {
                log(((UIComponent) labeledComponent).getId());
            }
            return labeledComponent;
        }
        if (DEBUG) {
            log("labelled component is null, try something else");
        }
        String id = getFor();

        if (DEBUG && id != null) {
            log("\tfor attribute set to " + id);
        }

        if (id == null) {
            if (DEBUG) {
                log("\tID is not set, find children ");
            }
            setLabeledComponent(findLabeledChild());
        } else {
            if (DEBUG) {
                log("\tID found");
            }
            // If the id is an absolute path, prefix it with ":"
            // to tell findComponent to do a search from the root
            if (id.contains(":") && !id.startsWith(":")) {
                id = ":" + id;
            }
            // Since Label is now a NamingContainer, findComponent
            // will treat it as the closest NamingContainer. Therefore
            // obtain the parent and call findComponent on it.
            // This makes the logic similar to getLabelComponentId by
            // treating a relative path id as a sibling.
            //
            // This use of parent is different from getLabelComponentId
            // since we are not requiring that the parent be a
            // NamingContainer.
            try {
                UIComponent parent = this.getParent();
                setLabeledComponent(parent.findComponent(id));
            } catch (Exception e) {
                if (DEBUG) {
                    log("\t ID is not found");
                }
            }
            element = "label";
        }
        return labeledComponent;
    }

    /**
     * Return the absolute client id of the labeled component. If the labeled
     * component is an instance of {@code ComplexComponent}, call
     * {@code getPrimaryElementID} on the labeled component and return that
     * id.
     * If the labeled component is not an instance of
     * {@code ComplexComponent}, call {@code getClientId} on the
     * labeled component and return that id.
     * If the labeled component has not been set, and the {@code getFor}
     * method returns a non null absolute id, return that id.
     * If {@code getFor} returns a relative id and the label's parent is a
     * {@code NamingContainer}, return an absolute client id constructed
     * from the label's parent client id and the id returned by the
     * {@code getFor} method. If the label's parent is not a
     * {@code NamingContainer} return the id returned by
     * {@code getFor}.
     * If {@code getFor} returns null, return null.
     *
     * @param context faces context
     * @return the client id of the labeled component or null.
     * @deprecated
     * @see com.sun.webui.jsf.util.RenderingUtilities#getLabeledElementId
     */
    public String getLabeledComponentId(final FacesContext context) {
        String id;
        if (labeledComponent != null) {
            if (labeledComponent instanceof ComplexComponent) {
                ComplexComponent compComp = (ComplexComponent) labeledComponent;
                id = compComp.getPrimaryElementID(context);
            } else {
                UIComponent comp = ((UIComponent) labeledComponent);
                id = comp.getClientId(context);
            }
        } else {
            id = getFor();
            // If the id is not null and is an absolute path
            // return the id, otherwise
            // assume it is a sibling if the Label has a parent.
            // and return the absolute path.
            //
            // This assumption should be stated in the doc for the
            // "for" attribute. I'm not sure what happens if the
            // relative path id is returned. Its possible that this
            // will not match any HTML element id, since most if
            // not all element id's will be absolute paths starting
            // with the form id. Wouldn't it be better to keep searching
            // for a Naming container ? For exmaple until recently
            // many of our own components could be parents but
            // were not NamingContainers.
            if (id != null && !id.contains(":")) {
                UIComponent comp = this.getParent();
                if (comp instanceof NamingContainer) {
                    id = comp.getClientId(context) + ":" + id;
                }
            }
        }
        return id;
    }

    /**
     * Find the labeled child.
     * @return UIComponent
     */
    private UIComponent findLabeledChild() {

        if (DEBUG) {
            log("findLabeledChild");
        }
        List kids = getChildren();
        if (DEBUG && kids.isEmpty()) {
            log("No children!");
        }
        for (int i = 0; i < kids.size(); i++) {
            Object kid = kids.get(i);
            if (kid instanceof EditableValueHolder) {
                if (DEBUG) {
                    log("Found good child " + kid.toString());
                }
                return (UIComponent) kid;
            }
        }
        if (DEBUG) {
            log("\tReturning null...");
        }
        return null;
    }

    /**
     * Return a component that implements a required icon. If a facet named
     * {@code required} is found that component is returned.
     * If a facet is not found an {@code Icon} component instance is
     * returned with the id
     * {@code getId() + "_required"}.
     * <p>
     * If a facet is not defined then the returned {@code Icon} component
     * is created every time this method is called.
     * </p>
     *
     * @param theme theme to use
     * @param context faces context
     * @return required facet or an Icon instance
     */
    public UIComponent getRequiredIcon(final Theme theme,
            final FacesContext context) {

        UIComponent comp = getFacet(REQUIRED_FACET);
        if (comp != null) {
            return comp;
        }
        Icon icon = ThemeUtilities.getIcon(theme,
                ThemeImages.LABEL_REQUIRED_ICON);
        icon.setId(
                ComponentUtilities.createPrivateFacetId(this, REQUIRED_FACET));
        icon.setParent(this);
        icon.setBorder(0);

        //icon.setLongDesc("FIXME: Required");
        return icon;
    }

    /**
     * Return a component that implements an error icon. If a facet named
     * {@code error} is found that component is returned.
     * If a facet is not found an {@code Icon} component instance is
     * returned with the id
     * {@code getId() + "_error"}.
     * <p>
     * If a facet is not defined then the returned {@code Icon} component
     * is created every time this method is called.
     * </p>
     *
     * @param theme theme to use
     * @param context faces context
     * @param valid valid flag
     * @return error facet or an Icon instance
     */
    public UIComponent getErrorIcon(final Theme theme,
            final FacesContext context, final boolean valid) {

        UIComponent comp = getFacet(ERROR_FACET);
        if (comp != null) {
            return comp;
        }

        Icon icon = ThemeUtilities.getIcon(theme,
                ThemeImages.LABEL_INVALID_ICON);
        icon.setId(
                ComponentUtilities.createPrivateFacetId(this, ERROR_FACET));
        icon.setParent(this);
        icon.setBorder(0);
        //icon.setLongDesc("FIXME: Invalid");

        if (valid) {
            icon.setIcon(ThemeImages.DOT);
            icon.setAlt("");
        } else if (labeledComponent != null) {
            String labeledCompID
                    = ((UIComponent) labeledComponent).getClientId(context);
            Iterator messages = context.getMessages(labeledCompID);
            FacesMessage fm;
            StringBuilder msgBuffer = new StringBuilder();
            while (messages.hasNext()) {
                fm = (FacesMessage) (messages.next());
                msgBuffer.append(fm.getDetail());
                msgBuffer.append(" ");
            }
            icon.setToolTip(msgBuffer.toString());
        }

        return icon;
    }

    /**
     * Return {@code span} if the label is not labeling another component,
     * else return {@code label}.
     * @return String
     */
    public String getElement() {
        return element;
    }

    /**
     * Log a message to the standard out.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(Label.class.getName() + "::" + msg);
    }

    /**
     * Return the label level. If the label level is less than 1 or greater than
     * 3, 2 is returned.
     * @return int
     */
    // These values need to be Theme based.
    @SuppressWarnings("checkstyle:magicnumber")
    public int getLabelLevel() {
        int level = doGetLabelLevel();
        if (level < 1 || level > 3) {
            level = 2;
            setLabelLevel(level);
        }
        return level;
    }

    @Override
    public ValueExpression getValueExpression(final String name) {
        if (name.equals("text")) {
            return super.getValueExpression("value");
        }
        return super.getValueExpression(name);
    }

    @Override
    public void setValueExpression(final String name,
            final ValueExpression binding) {

        if (name.equals("text")) {
            super.setValueExpression("value", binding);
            return;
        }
        super.setValueExpression(name, binding);
    }

    /**
     * Use this attribute to specify the labeled component. The value of the
     * attribute is the absolute client id of the component or the id of the
     * component to be labeled. Relative ids are no longer supported. If this
     * attribute is not specified, the {@code label} component tries to
     * search its children to see whether any of them can be used for evaluating
     * the value of this "for" attribute.
     * @return String
     */
    public String getFor() {
        if (this.forComp != null) {
            return this.forComp;
        }
        ValueExpression vb = getValueExpression("for");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use this attribute to specify the labeled component. The value of the
     * attribute is the absolute client id of the component or the id of the
     * component to be labeled. Relative ids are no longer supported. If this
     * attribute is not specified, the {@code label} component tries to
     * search its children to see whether any of them can be used for evaluating
     * the value of this "for" attribute.
     *
     * @see #getFor()
     * @param newForcomp forComp
     */
    public void setFor(final String newForcomp) {
        this.forComp = newForcomp;
    }

    /**
     * Use the hideIndicators attribute to prevent display of the required and
     * invalid icons with the label. When the required attribute on the
     * component to be labeled is set to true, the required icon is displayed
     * next to the label. If the user submits the page with an invalid value for
     * the component, the invalid icon is displayed. This attribute is useful
     * when the component has more than one label, and only one label should
     * show the icons.
     * @return {@code boolean}
     */
    public boolean isHideIndicators() {
        if (this.hideIndicatorsSet) {
            return this.hideIndicators;
        }
        ValueExpression vb = getValueExpression("hideIndicators");
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
     * Use the hideIndicators attribute to prevent display of the required and
     * invalid icons with the label. When the required attribute on the
     * component to be labeled is set to true, the required icon is displayed
     * next to the label. If the user submits the page with an invalid value for
     * the component, the invalid icon is displayed. This attribute is useful
     * when the component has more than one label, and only one label should
     * show the icons.
     *
     * @see #isHideIndicators()
     * @param newHideIndicators hideIndicators
     */
    public void setHideIndicators(final boolean newHideIndicators) {
        this.hideIndicators = newHideIndicators;
        this.hideIndicatorsSet = true;
    }

    /**
     * Style level for this label, where lower values typically specify
     * progressively larger font sizes, and/or bolder font weights. Valid values
     * are 1, 2, and 3. The default label level is 2. Any label level outside
     * this range will result in no label level being added.
     * @return int
     */
    public int doGetLabelLevel() {
        if (this.labelLevelSet) {
            return this.labelLevel;
        }
        ValueExpression vb = getValueExpression("labelLevel");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) result);
            }
        }
        return 2;
    }

    /**
     * Style level for this label, where lower values typically specify
     * progressively larger font sizes, and/or bolder font weights. Valid values
     * are 1, 2, and 3. The default label level is 2. Any label level outside
     * this range will result in no label level being added.
     *
     * @see #getLabelLevel()
     * @param newLabelLevel labelLevel
     */
    public void setLabelLevel(final int newLabelLevel) {
        this.labelLevel = newLabelLevel;
        this.labelLevelSet = true;
    }

    /**
     * Scripting code executed when a mouse click occurs over this
     * component.
     * @return String
     */
    public String getOnClick() {
        if (this.onClick != null) {
            return this.onClick;
        }
        ValueExpression vb = getValueExpression("onClick");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when a mouse click occurs over this
     * component.
     *
     * @see #getOnClick()
     * @param newOnClick onClick
     */
    public void setOnClick(final String newOnClick) {
        this.onClick = newOnClick;
    }

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     * @return String
     */
    public String getOnMouseDown() {
        if (this.onMouseDown != null) {
            return this.onMouseDown;
        }
        ValueExpression vb = getValueExpression("onMouseDown");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     *
     * @see #getOnMouseDown()
     * @param newOnMouseDown onMouseDown
     */
    public void setOnMouseDown(final String newOnMouseDown) {
        this.onMouseDown = newOnMouseDown;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     * @return String
     */
    public String getOnMouseMove() {
        if (this.onMouseMove != null) {
            return this.onMouseMove;
        }
        ValueExpression vb = getValueExpression("onMouseMove");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     *
     * @see #getOnMouseMove()
     * @param newOnMouseMove onMouseMove
     */
    public void setOnMouseMove(final String newOnMouseMove) {
        this.onMouseMove = newOnMouseMove;
    }

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     * @return String
     */
    public String getOnMouseOut() {
        if (this.onMouseOut != null) {
            return this.onMouseOut;
        }
        ValueExpression vb = getValueExpression("onMouseOut");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     *
     * @see #getOnMouseOut()
     * @param newOnMouseOut onMouseOut
     */
    public void setOnMouseOut(final String newOnMouseOut) {
        this.onMouseOut = newOnMouseOut;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     * @return String
     */
    public String getOnMouseOver() {
        if (this.onMouseOver != null) {
            return this.onMouseOver;
        }
        ValueExpression vb = getValueExpression("onMouseOver");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     *
     * @see #getOnMouseOver()
     * @param newOnMouseOver onMouseOver
     */
    public void setOnMouseOver(final String newOnMouseOver) {
        this.onMouseOver = newOnMouseOver;
    }

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     * @return {@code boolean}
     */
    public String getOnMouseUp() {
        if (this.onMouseUp != null) {
            return this.onMouseUp;
        }
        ValueExpression vb = getValueExpression("onMouseUp");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     *
     * @see #getOnMouseUp()
     * @param newOnMouseUp onMouseUp
     */
    public void setOnMouseUp(final String newOnMouseUp) {
        this.onMouseUp = newOnMouseUp;
    }

    /**
     * Flag indicating that the labeled component should be marked as required.
     * It is only relevant if the labeled component is not a child of the label
     * tag. Set this flag to ensure that the required icon shows up the first
     * time the page is rendered.
     * @return {@code boolean}
     */
    public boolean isRequiredIndicator() {
        if (this.requiredIndicatorSet) {
            return this.requiredIndicator;
        }
        ValueExpression vb = getValueExpression("requiredIndicator");
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
     * Flag indicating that the labeled component should be marked as required.
     * It is only relevant if the labeled component is not a child of the label
     * tag. Set this flag to ensure that the required icon shows up the first
     * time the page is rendered.
     *
     * @see #isRequiredIndicator()
     * @param newRequiredIndicator requiredIndicator
     */
    public void setRequiredIndicator(final boolean newRequiredIndicator) {
        this.requiredIndicator = newRequiredIndicator;
        this.requiredIndicatorSet = true;
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
     * The label text to be displayed for this label. This attribute can be set
     * to a literal string, to a value binding expression that corresponds to a
     * property of a managed bean, or to a value binding expression that
     * corresponds to a message from a resource bundle declared using
     * {@code f:loadBundle}.
     *
     * @return Object
     */
    @Property(name = "text",
            displayName = "Label Text",
            category = "Appearance",
            isDefault = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    public Object getText() {
        return getValue();
    }

    /**
     * The label text to be displayed for this label. This attribute can be set
     * to a literal string, to a value binding expression that corresponds to a
     * property of a managed bean, or to a value binding expression that
     * corresponds to a message from a resource bundle declared using
     * {@code f:loadBundle}.
     *
     * @see #getText()
     * @param newText text
     */
    public void setText(final Object newText) {
        setValue(newText);
    }

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
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
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     *
     * @see #getToolTip()
     * @param newToolTip tool tip
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

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.forComp = (String) values[1];
        this.hideIndicators = ((Boolean) values[2]);
        this.hideIndicatorsSet = ((Boolean) values[3]);
        this.labelLevel = ((Integer) values[4]);
        this.labelLevelSet = ((Boolean) values[5]);
        this.onClick = (String) values[6];
        this.onMouseDown = (String) values[7];
        this.onMouseMove = (String) values[8];
        this.onMouseOut = (String) values[9];
        this.onMouseOver = (String) values[10];
        this.onMouseUp = (String) values[11];
        this.requiredIndicator = ((Boolean) values[12]);
        this.requiredIndicatorSet = ((Boolean) values[13]);
        this.style = (String) values[14];
        this.styleClass = (String) values[15];
        this.toolTip = (String) values[16];
        this.visible = ((Boolean) values[17]);
        this.visibleSet = ((Boolean) values[18]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[19];
        values[0] = super.saveState(context);
        values[1] = this.forComp;
        if (this.hideIndicators) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        if (this.hideIndicatorsSet) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        values[4] = this.labelLevel;
        if (this.labelLevelSet) {
            values[5] = Boolean.TRUE;
        } else {
            values[5] = Boolean.FALSE;
        }
        values[6] = this.onClick;
        values[7] = this.onMouseDown;
        values[8] = this.onMouseMove;
        values[9] = this.onMouseOut;
        values[10] = this.onMouseOver;
        values[11] = this.onMouseUp;
        if (this.requiredIndicator) {
            values[12] = Boolean.TRUE;
        } else {
            values[12] = Boolean.FALSE;
        }
        if (this.requiredIndicatorSet) {
            values[13] = Boolean.TRUE;
        } else {
            values[13] = Boolean.FALSE;
        }
        values[14] = this.style;
        values[15] = this.styleClass;
        values[16] = this.toolTip;
        if (this.visible) {
            values[17] = Boolean.TRUE;
        } else {
            values[17] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[18] = Boolean.TRUE;
        } else {
            values[18] = Boolean.FALSE;
        }
        return values;
    }
}
