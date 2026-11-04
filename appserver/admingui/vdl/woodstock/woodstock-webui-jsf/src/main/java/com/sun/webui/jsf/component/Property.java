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
import com.sun.webui.jsf.util.ComponentUtilities;

import jakarta.el.ValueExpression;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;

import java.beans.Beans;
import java.util.Iterator;

/**
 * The {@code Property} component was written to be used within
 * the{@code PropertySheetSection} component, which is in turn used within the
 * context of a {@code PropertySheet} component. The{@code Property} component
 * allows you to encapsulate a logic "property" and help you lay it out on the
 * page. A "property" has a number of configuration options, including: the
 * property content; an optional label; the ability to stretch the property to
 * include the label area (in addition to the content area of the "property";
 * the ability to mark a property required; and the ability to associate help
 * text with the property to inform your end user how to interact with the
 * property.<p>
 * Help text can be provided for each property by supplying the{@code helpText}
 * attribute. This attribute may be a literal String or a {@code ValueBinding}
 * expression. The help text will appear below the content of the "property".
 * Optionally, the helpText may also be provided as a facet named "helpText".
 * This allows advanced users to have more control over the types of content
 * provided in the helpText area.</p><p>
 * The label may be provided via the {@code label} attribute. The label will be
 * rendered to the left of the content area of the "property". The label area
 * will not exist if the {@code overlapLabel} attribute is set to true.
 * Optionally advanced users may provide a label facet named "label". This
 * allows developers to have more control over the content of the label
 * area.</p><p>
 * The {@code labelAlign} attribute can use used to specify "left" or "right"
 * alignment of the label table cell.</p><p>
 * Setting the {@code noWrap} attribute to true specifies that the label should
 * not be wraped to a new line.</p><p>
 * The {@code overlapLabel} attribute causes the content of the property to be
 * stretched into the label area as well as the content area. This may be useful
 * for titles which should span the entire width, or other cases where you need
 * the whole width of the {@code PropertySheet}.</p><p>
 * The {@code requiredIndicator} attribute is only valid when you supply a label
 * via an attribute (not as a facet). When this attribute is marked
 * {@code true}, the label that is created will have its required attribute
 * marked true.</p><p>
 * For an example, please see the documentation for the {@code propertySheet}
 * Tag.</p>
 */
@Component(type = "com.sun.webui.jsf.Property",
        family = "com.sun.webui.jsf.Property",
        displayName = "Property", tagName = "property",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_property",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_property_props")
        //CHECKSTYLE:ON
public final class Property extends UIComponentBase
        implements ComplexComponent, NamingContainer {

    /**
     * Content facet.
     */
    public static final String CONTENT_FACET = "content";

    /**
     * Help text facet.
     */
    public static final String HELPTEXT_FACET = "helpText";

    /**
     * Label facet.
     */
    public static final String LABEL_FACET = "label";

    /**
     * Flag indicating that the user is not permitted to activate this
     * component, and that the component's value will not be submitted with the
     * form.
     */
    @com.sun.faces.annotation.Property(name = "disabled",
            displayName = "Disabled",
            category = "Behavior")
    private boolean disabled = false;

    /**
     * disabled set flag.
     */
    private boolean disabledSet = false;

    /**
     * The text specified with this attribute is displayed below the content of
     * the property in a small font. The value can be a literal String or a
     * ValueBinding expression. If you want greater control over the content
     * that is displayed in the help text area, use the helpText facet.
     */
    @com.sun.faces.annotation.Property(name = "helpText",
            displayName = "Help Text",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String helpText = null;

    /**
     * Use this attribute to specify the text of the label of this property. The
     * text is displayed in the column that is reserved for the label of this
     * property row. The attribute value can be a string or a value binding
     * expression. The label is associated with the first input element in the
     * content area of the property. To label a different component, use the
     * label facet instead.
     */
    @com.sun.faces.annotation.Property(name = "label",
            displayName = "Label",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String label = null;

    /**
     * Specifies the alignment for the property label. The label occupies a cell
     * in the first column of a table that is used to lay out the properties.
     * Set the labelAlign attribute to make the label align to the left or right
     * of the cell. The default alignment is left. This attribute applies to
     * labels that are specified with either the label attribute or the label
     * facet.
     */
    @com.sun.faces.annotation.Property(name = "labelAlign",
            displayName = "Label Alignment",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.HtmlHorizontalAlignEditor")
            //CHECKSTYLE:ON
    private String labelAlign = null;

    /**
     * Specifies that the label should not wrap around to another line, if set
     * to true. If the label is long, the label column in the table for the
     * property sheet section expands to accommodate the label without wrapping
     * to a new line. This attribute applies to labels that are specified with
     * either the label attribute or the label facet.
     */
    @com.sun.faces.annotation.Property(name = "noWrap",
            displayName = "Label No-Wrap",
            category = "Appearance")
    private boolean noWrap = false;

    /**
     * noWrap set flag.
     */
    private boolean noWrapSet = false;

    /**
     * Specifies that the content of the property should occupy the label area
     * as well as the content area, if set to true. The default value is false.
     * This attribute is useful for properties that require the entire width of
     * the property sheet.
     */
    @com.sun.faces.annotation.Property(name = "overlapLabel",
            displayName = "Overlap Label",
            category = "Appearance")
    private boolean overlapLabel = false;

    /**
     * overlapLabel set flag.
     */
    private boolean overlapLabelSet = false;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @com.sun.faces.annotation.Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
            //CHECKSTYLE:ON
    private String style = null;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @com.sun.faces.annotation.Property(name = "styleClass",
            displayName = "CSS Style Class(es)",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StyleClassPropertyEditor")
            //CHECKSTYLE:ON
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
    @com.sun.faces.annotation.Property(name = "visible",
            displayName = "Visible",
            category = "Behavior")
    private boolean visible = false;

    /**
     * Constructor.
     */
    public Property() {
        super();
        setRendererType("com.sun.webui.jsf.Property");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Property";
    }

    /**
     * Returns the absolute ID of an HTML element suitable for use as the value
     * of an HTML LABEL element's {@code for} attribute. If the
     * {@code ComplexComponent} has sub-components, and one of the
     * sub-components is the target of a label, if that sub-component is a
     * {@code ComplexComponent}, then {@code getLabeledElementId} must
     * called on the sub-component and the value returned. The value returned by
     * this method call may or may not resolve to a component instance.
     *
     * @param context The FacesContext used for the request
     * @return An absolute id suitable for the value of an HTML LABEL element's
     * {@code for} attribute.
     */
    @Override
    public String getLabeledElementId(final FacesContext context) {
        // Check for "content" facet first
        UIComponent contentFacet = getContentComponent();

        // The field component is the one that is labelled
        UIComponent labeledComponent;

        if (contentFacet == null) {
            // If there is no facet, assume that the content is specified
            // as a child of this component. Search for a
            // required ComplexComponent among the children
            labeledComponent = findLabeledComponent(this, true);
        } else {
            // If a facet has been specified, see if the facet is a required
            // ComplexComponent or search for a required ComplexComponent
            // among the children of the facet component
            labeledComponent = findLabeledComponent(contentFacet, false);
        }

        if (labeledComponent != null) {
            // NOTE: Don't use ComplexComponent here, the Label component will.
            if (Beans.isDesignTime()) {
                //6474235: recalculate clientId
                UIComponent resetIdComp = labeledComponent;
                while (resetIdComp != null) {
                    resetIdComp.setId(resetIdComp.getId());
                    resetIdComp = resetIdComp.getParent();
                }
            }
            if (labeledComponent instanceof ComplexComponent) {
                return ((ComplexComponent) labeledComponent)
                        .getLabeledElementId(context);
            } else {
                return labeledComponent.getClientId(context);
            }
        }
        return null;

    }

    /**
     * Returns the id of an HTML element suitable to receive the focus. If the
     * {@code ComplexComponent} has sub-components, and one of the
     * sub-components is to receive the focus, if that sub-component is a
     * {@code ComplexComponent}, then {@code getFocusElementId} must
     * called on the sub-component and the value returned. The value returned by
     * this method call may or may not resolve to a component instance.
     *
     * @param context The FacesContext used for the request
     */
    @Override
    public String getFocusElementId(final FacesContext context) {
        // Just return the same id as the labeled component for now.
        return getLabeledElementId(context);
    }

    /**
     * This method calculates the proper {@code UIComponent} that should be
     * used when the label property is used with this component.
     *
     * <p>
     * This method provides the implementation for
     * {@link com.sun.webui.jsf.component.ComplexComponent}</p>
     *
     * @param context The {@code FacesContext}.
     *
     * @return The {@code id} of the label target.
     *
     * @deprecated
     * @see #getLabeledElementId
     * @see #getFocusElementId
     */
    @Override
    public String getPrimaryElementID(final FacesContext context) {
        // Check for "content" facet first
        UIComponent contentFacet = getContentComponent();

        // The field component is the one that is labelled
        UIComponent labeledComponent;

        if (contentFacet == null) {
            // If there is no facet, assume that the content is specified
            // as a child of this component. Search for a
            // required EditableValueHolderamong the children
            //
            labeledComponent = findLabeledComponent(this, true);
        } else {
            // If a facet has been specified, see if the facet is a required
            // EditableValueHolder or search for a required EditableValueHolder
            // among the children of the facet component
            labeledComponent = findLabeledComponent(contentFacet, false);
        }

        if (labeledComponent != null) {
            // Return an absolute path (relative is harder to calculate)
            // NOTE: Label component does not fully support relative anyway,
            // NOTE: the ":" I'm adding isn't necessary... however, it doesn't
            // NOTE: hurt and if Label ever does support relative paths, the
            // NOTE: ":" prefix is needed to specify a full path.
            // NOTE:
            // NOTE: Don't use ComplexComponent here, the Label component will.
            if (Beans.isDesignTime()) {
                //6474235: recalculate clientId
                UIComponent resetIdComp = labeledComponent;
                while (resetIdComp != null) {
                    resetIdComp.setId(resetIdComp.getId());
                    resetIdComp = resetIdComp.getParent();
                }
            }
            return ":" + labeledComponent.getClientId(context);
        }
        return null;
    }

    /**
     * This method checks the component, children, and facets to see if any of
     * them are {@code EditableValueHolder}s. The first one found is
     * returned, null otherwise.
     *
     * @param comp The {@code UIComponent} to check.
     * @param skip Flag indicating the initial component should be ignored
     *
     * @return The first {@code EditableValueHolder}, null if not found.
     */
    private static UIComponent findLabeledComponent(final UIComponent comp,
            final boolean skip) {
        if (!skip) {
            // Check to see if comp is an EditableValueHolder
            if (comp instanceof EditableValueHolder) {
                return comp;
            }
        }

        // Next check children and facets
        Iterator it = comp.getFacetsAndChildren();
        while (it.hasNext()) {
            UIComponent c = findLabeledComponent((UIComponent) it.next(),
                    false);
            if (c != null) {
                return c;
            }
        }
        // Not found
        return null;
    }

    /**
     * Return the a component that represents the content of the property. If a
     * facet called {@code content} does not exist {@code null} is
     * returned.
     * @return UIComponent
     */
    public UIComponent getContentComponent() {
        return getFacet(CONTENT_FACET);
    }

    /**
     * Return a component that implements help text. If a facet named
     * {@code helpText} is found that component is returned. Otherwise a
     * {@code HelpInline} component is returned. It is assigned the id
     * {@code getId() + "_helpText"}
     * <p>
     * If the facet is not defined then the returned {@code HelpInline}
     * component is re-initialized every time this method is called.
     * </p>
     * <p>
     * If {@code getHelpeText} returns null, null is returned.
     * </p>
     *
     * @return a help text facet component
     */
    public UIComponent getHelpTextComponent() {
        UIComponent component = getFacet(HELPTEXT_FACET);
        if (component != null) {
            return component;
        }

        String propertyHelpText = getHelpText();
        if (propertyHelpText == null) {
            return null;
        }
        // Create one every time.
        component = (UIComponent) new HelpInline();
        // Assume helpText is literal
        ((HelpInline) component).setText(propertyHelpText);
        component.setId(
                ComponentUtilities.createPrivateFacetId(this, HELPTEXT_FACET));
        component.setParent(this);
        ((HelpInline) component).setType("field");

        return component;

    }

    /**
     * Return a component that implements a label. If a facet named
     * {@code label} is found that component is returned. Otherwise a
     * {@code Label} component is returned. It is assigned the id
     * {@code getId() + "_label"}
     * <p>
     * If the facet is not defined then the returned {@code Label}
     * component is re-initialized every time this method is called.
     * </p>
     *
     * @return a label facet component
     */
    public UIComponent getLabelComponent() {

        UIComponent component = getFacet(LABEL_FACET);
        if (component != null) {
            return component;
        }

        // If label is null, don't return any component.
        // This may need to be revisited but is the common
        // behavior of other components, rightly or wrongly
        String propLabel = getLabel();
        if (propLabel == null) {
            return null;
        }
        component = ComponentUtilities.getPrivateFacet(this,
                LABEL_FACET, true);
        if (component == null) {
            // This really should be done using JSF application
            // create component, and component type.
            component = (UIComponent) new Label();
            component.setId(ComponentUtilities.createPrivateFacetId(
                    this, LABEL_FACET));
            ComponentUtilities.putPrivateFacet(this, LABEL_FACET,
                    component);
        }

        ((Label) component).setText(propLabel);

        // Theme should be queried for the label level if
        // there isn't an attribute, which there should be.
        // The renderer verifies the value.
        //
        //((Label)component).setLabelLevel(getLabelLevel());
        // We need to set the for attribute for this label.
        // How do we choose which of the possibly several
        // properties to set it on. Easy. The Property component
        // should have a "for" attribute whose value is the
        // component to associate the label to.
        //
        // Currently there are heuristics implemented to try
        // find the labelled component, continue using that
        // for now.
        String id = getLabeledElementId(getFacesContext());
        ((Label) component).setFor(id);

        return component;
    }

    @com.sun.faces.annotation.Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    @com.sun.faces.annotation.Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * Flag indicating that the user is not permitted to activate this
     * component, and that the component's value will not be submitted with the
     * form.
     * @return {@code boolean}
     */
    public boolean isDisabled() {
        if (this.disabledSet) {
            return this.disabled;
        }
        ValueExpression vb = getValueExpression("disabled");
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
     * Flag indicating that the user is not permitted to activate this
     * component, and that the component's value will not be submitted with the
     * form.
     *
     * @see #isDisabled()
     * @param newDisabled disabled
     */
    public void setDisabled(final boolean newDisabled) {
        this.disabled = newDisabled;
        this.disabledSet = true;
    }

    /**
     * The text specified with this attribute is displayed below the content of
     * the property in a small font. The value can be a literal String or a
     * ValueBinding expression. If you want greater control over the content
     * that is displayed in the help text area, use the helpText facet.
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
     * The text specified with this attribute is displayed below the content of
     * the property in a small font. The value can be a literal String or a
     * ValueBinding expression. If you want greater control over the content
     * that is displayed in the help text area, use the helpText facet.
     *
     * @see #getHelpText()
     * @param newHelpText helpText
     */
    public void setHelpText(final String newHelpText) {
        this.helpText = newHelpText;
    }

    /**
     * Use this attribute to specify the text of the label of this property. The
     * text is displayed in the column that is reserved for the label of this
     * property row. The attribute value can be a string or a value binding
     * expression. The label is associated with the first input element in the
     * content area of the property. To label a different component, use the
     * label facet instead.
     * @return String
     */
    public String getLabel() {
        if (this.label != null) {
            return this.label;
        }
        ValueExpression vb = getValueExpression("label");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Use this attribute to specify the text of the label of this property. The
     * text is displayed in the column that is reserved for the label of this
     * property row. The attribute value can be a string or a value binding
     * expression. The label is associated with the first input element in the
     * content area of the property. To label a different component, use the
     * label facet instead.
     *
     * @see #getLabel()
     * @param newLabel label
     */
    public void setLabel(final String newLabel) {
        this.label = newLabel;
    }

    /**
     * Specifies the alignment for the property label. The label occupies a cell
     * in the first column of a table that is used to lay out the properties.
     * Set the labelAlign attribute to make the label align to the left or right
     * of the cell. The default alignment is left. This attribute applies to
     * labels that are specified with either the label attribute or the label
     * facet.
     * @return String
     */
    public String getLabelAlign() {
        if (this.labelAlign != null) {
            return this.labelAlign;
        }
        ValueExpression vb = getValueExpression("labelAlign");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Specifies the alignment for the property label. The label occupies a cell
     * in the first column of a table that is used to lay out the properties.
     * Set the labelAlign attribute to make the label align to the left or right
     * of the cell. The default alignment is left. This attribute applies to
     * labels that are specified with either the label attribute or the label
     * facet.
     *
     * @see #getLabelAlign()
     * @param newLabelAlign labelAlign
     */
    public void setLabelAlign(final String newLabelAlign) {
        this.labelAlign = newLabelAlign;
    }

    /**
     * Specifies that the label should not wrap around to another line, if set
     * to true. If the label is long, the label column in the table for the
     * property sheet section expands to accommodate the label without wrapping
     * to a new line. This attribute applies to labels that are specified with
     * either the label attribute or the label facet.
     * @return {@code boolean}
     */
    public boolean isNoWrap() {
        if (this.noWrapSet) {
            return this.noWrap;
        }
        ValueExpression vb = getValueExpression("noWrap");
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
     * Specifies that the label should not wrap around to another line, if set
     * to true. If the label is long, the label column in the table for the
     * property sheet section expands to accommodate the label without wrapping
     * to a new line. This attribute applies to labels that are specified with
     * either the label attribute or the label facet.
     *
     * @see #isNoWrap()
     * @param newNoWrap noWrap
     */
    public void setNoWrap(final boolean newNoWrap) {
        this.noWrap = newNoWrap;
        this.noWrapSet = true;
    }

    /**
     * Specifies that the content of the property should occupy the label area
     * as well as the content area, if set to true. The default value is false.
     * This attribute is useful for properties that require the entire width of
     * the property sheet.
     * @return {@code boolean}
     */
    public boolean isOverlapLabel() {
        if (this.overlapLabelSet) {
            return this.overlapLabel;
        }
        ValueExpression vb = getValueExpression("overlapLabel");
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
     * Specifies that the content of the property should occupy the label area
     * as well as the content area, if set to true. The default value is false.
     * This attribute is useful for properties that require the entire width of
     * the property sheet.
     *
     * @see #isOverlapLabel()
     * @param newOverlapLabel overlapLabel
     */
    public void setOverlapLabel(final boolean newOverlapLabel) {
        this.overlapLabel = newOverlapLabel;
        this.overlapLabelSet = true;
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
     * visible set flag.
     */
    private boolean visibleSet = false;

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
        this.disabled = ((Boolean) values[1]);
        this.disabledSet = ((Boolean) values[2]);
        this.helpText = (String) values[3];
        this.label = (String) values[4];
        this.labelAlign = (String) values[5];
        this.noWrap = ((Boolean) values[6]);
        this.noWrapSet = ((Boolean) values[7]);
        this.overlapLabel = ((Boolean) values[8]);
        this.overlapLabelSet = ((Boolean) values[9]);
        this.style = (String) values[10];
        this.styleClass = (String) values[11];
        this.visible = ((Boolean) values[12]);
        this.visibleSet = ((Boolean) values[13]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[14];
        values[0] = super.saveState(context);
        if (this.disabled) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.disabledSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        values[3] = this.helpText;
        values[4] = this.label;
        values[5] = this.labelAlign;
        if (this.noWrap) {
            values[6] = Boolean.TRUE;
        } else {
            values[6] = Boolean.FALSE;
        }
        if (this.noWrapSet) {
            values[7] = Boolean.TRUE;
        } else {
            values[7] = Boolean.FALSE;
        }
        if (this.overlapLabel) {
            values[8] = Boolean.TRUE;
        } else {
            values[8] = Boolean.FALSE;
        }
        if (this.overlapLabelSet) {
            values[9] = Boolean.TRUE;
        } else {
            values[9] = Boolean.FALSE;
        }
        values[10] = this.style;
        values[11] = this.styleClass;
        if (this.visible) {
            values[12] = Boolean.TRUE;
        } else {
            values[12] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[13] = Boolean.TRUE;
        } else {
            values[13] = Boolean.FALSE;
        }
        return values;
    }
}
