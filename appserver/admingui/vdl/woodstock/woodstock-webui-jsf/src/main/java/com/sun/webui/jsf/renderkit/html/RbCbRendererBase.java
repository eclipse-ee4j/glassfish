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
package com.sun.webui.jsf.renderkit.html;

import com.sun.webui.jsf.component.ImageComponent;
import com.sun.webui.jsf.component.Label;
import com.sun.webui.jsf.component.RbCbSelector;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.ConverterException;

import java.io.IOException;

import static com.sun.webui.jsf.util.ConversionUtilities.convertValueToString;
import static com.sun.webui.jsf.util.ConversionUtilities.setRenderedValue;
import static com.sun.webui.jsf.util.RenderingUtilities.getStyleClasses;
import static com.sun.webui.jsf.util.RenderingUtilities.renderComponent;

/**
 * <p>
 * The {@code RbCbRendererBase} class is the abstract base class for
 * {@link com.sun.webui.jsf.renderkit.html.RadioButtonRenderer} and
 * {@link com.sun.webui.jsf.renderkit.html.CheckboxRenderer}.
 * </p>
 * <p>
 * {@code RbCbRendererBase} provides encoding functionality for the
 * {@code RadioButtonRenderer} and {@code CheckboxRenderer}. This includes an
 * implementation of {@code getConvertedValue}, and a method called
 * {@code renderSelection} which a subclass calls to render either a
 * {@code Checkbox} or a {@code RadioButton} component at the appropriate
 * time.<br/>
 * The renderer subclass must implement
 * <p>
 * <ul>
 * <li>{@code isSelected} in order for this class to generically render either
 * component</li>
 * <li>{@code getStyle} so the specific subclass can specify the appropriate
 * {@code ThemeStyle} constants.</li>
 * </ul>
 * </p>
 * <h3>Decoding</h3>
 * <p>
 * See {@link com.sun.webui.jsf.renderkit.html.RadioButtonRenderer} and
 * {@link com.sun.webui.jsf.renderkit.html.CheckboxRenderer} for details on
 * decoding requests.
 * </p>
 * <h3>Encoding</h3>
 * <p>
 * The renderer emits the following HTML elements.
 * <ul>
 * <li> An INPUT element of type specified by the subclass in
 * {@code renderSelection}
 * </li>
 * <li> An optional {@link com.sun.webui.jsf.component.ImageComponent} component
 * is rendered for each INPUT element
 * </li>
 * <li> An optional {@link com.sun.webui.jsf.component.Label} component is
 * rendered for each INPUT element
 * </li>
 * </ul>
 * </p>
 * <p>
 * The ID attributes for HTML elements are constructed as follows, where
 * &lt;cid&gt; is the {@code clientId} of the component being rendered.
 * <p>
 * <ul>
 * <li> &lt;cid&gt; for the INPUT element
 * </li>
 * <li> &lt;cid&gt;_image for the image component
 * </li>
 * <li> &lt;cid&gt;_label for the label component
 * </li>
 * </ul>
 * <h1>Encoding the INPUT element</h1>
 * <p>
 * If the {@code name} property of the component is {@code null} the name
 * attribute of the INPUT element will be set to the value of the component's
 * {@code clientId} and the control will not be part of a group, and behave as
 * an individual control.<br/>
 * If the {@code name} property is not {@code null} then its value is used as
 * the value of the name attribute of the HTML INPUT element and the control
 * will behave as part of a group.
 * </p>
 * <p>
 * The {@code ConversionUtilities.getValueAsString} method is called with the
 * value of the component's {@code selectedValue} property and the result is
 * used as the value of the HTML INPUT element's value attribute. The
 * {@code String} value that is returned may be the actual value of the
 * {@code selectedValue} property or the result of a conversion of a developer
 * defined object value to a {@code String} or "true" if the
 * {@code selectedValue} property was null or never assigned a value.<em>The
 * components {@link com.sun.webui.jsf.component.RadioButton} and
 * {@link com.sun.webui.jsf.component.Checkbox} implement the behavior of
 * returning "true" when {@code selectedValue} is null. Therefore if the
 * component parameter is not one of these classes then this behavior may
 * vary.</em>
 * </p>
 * <p>
 * If {@code isSelected} returns {@code true} the the value of the HTML INPUT
 * element's checked attribute is set to "checked", otherwise the checked
 * attribute is not rendered.
 * </p>
 * <p>
 * The following component properties are obtained and rendered in turn and
 * equivalent to the HTML INPUT element's attributes of the same name, but
 * rendered in all lowercase.
 * <ul>
 * <li>disabled</li>
 * <li>readOnly</li>
 * <li>tabIndex</li>
 * <li>style</li>
 * </ul>
 * The component's {@code toolTip} property if not null is rendered as the value
 * of the HTML INPUT element's title attribute.<br/>
 * The HTML INPUT element's class attribute is set to the component's
 * {@code styleClass} property appended with the value returned from a call to
 * the {@code getStyle} method.
 * </p>
 * <h1>Rendering the image component</h1>
 * <p>
 * The renderer calls the component's {@code getImageComponent} method to obtain
 * an instance of a {@link com.sun.webui.jsf.component.ImageComponent}
 * component. If null is returned, no image will appear with the control. If a
 * non null instance is returned, the appropriate disabled or enabled style
 * class returned by {@code getStyle} is appended to the image's
 * {@code styleClass} property. {@code RenderingUtilities.renderComponent} is
 * called to render the component.<br/>
 * </p>
 * <p>
 * If an image is rendered it appears to the immediate left of the control.
 * </p>
 * <h1>Encoding the label component</h1>
 * <p>
 * The renderer calls the component's {@code getLabelComponent} method to obtain
 * an instance of a {@link com.sun.webui.jsf.component.Label} component. If null
 * is returned, no label will appear with the control. If a non null instance is
 * returned, the appropriate disabled or enabled style class returned by
 * {@code getStyle} is appended to the label's {@code styleClass} property.
 * {@code RenderingUtilities.renderComponent} is called to render the
 * component.
 * </p>
 */
abstract class RbCbRendererBase extends AbstractRenderer {

    /**
     * The define constant indicating the style class for an INPUT element.
     */
    protected static final int INPUT = 0;

    /**
     * The define constant indicating the style class for a disabled INPUT
     * element.
     */
    protected static final int INPUT_DIS = 1;

    /**
     * The define constant indicating the style class for the LABEL element.
     */
    protected static final int LABEL = 2;

    /**
     * The define constant indicating the style class for a disabled LABEL
     * element.
     */
    protected static final int LABEL_DIS = 3;

    /**
     * The define constant indicating the style class for the IMG element.
     */
    protected static final int IMAGE = 4;

    /**
     * The define constant indicating the style class for a disabled IMG
     * element.
     */
    protected static final int IMAGE_DIS = 5;

    /**
     * The define constant indicating the style class for for the containing
     * span element.
     */
    protected static final int SPAN = 6;

    /**
     * The define constant indicating the style class for for the containing
     * span element, when disabled.
     */
    protected static final int SPAN_DIS = 7;

    /**
     * Input element.
     */
    private static final String INPUT_ELEM = "input";

    /**
     * span element.
     */
    private static final String SPAN_ELEM = "span";

    /**
     * checked attribute.
     */
    private static final String CHECKED_ATTR = "checked";

    /**
     * disabled attribute.
     */
    private static final String DISABLED_ATTR = "disabled";

    /**
     * class attribute.
     */
    private static final String CLASS_ATTR = "class";

    /**
     * id attribute.
     */
    private static final String ID_ATTR = "id";

    /**
     * name attribute.
     */
    private static final String NAME_ATTR = "name";

    /**
     * readonly attribute.
     */
    private static final String READONLY_ATTR = "readonly";

    /**
     * readonly cc attribute.
     */
    private static final String READONLY_CC_ATTR = "readOnly";

    /**
     * style attribute.
     */
    private static final String STYLE_ATTR = "style";

    /**
     * style class attribute.
     */
    private static final String STYLECLASS_ATTR = "styleClass";

    /**
     * tabindex attribute.
     */
    private static final String TABINDEX_ATTR = "tabindex";

    /**
     * tabindex cc attribute.
     */
    private static final String TABINDEX_CC_ATTR = "tabIndex";

    /**
     * title attribute.
     */
    private static final String TITLE_ATTR = "title";

    /**
     * tooltip attribute.
     */
    private static final String TOOLTIP_ATTR = "toolTip";

    /**
     * type attribute.
     */
    private static final String TYPE_ATTR = "type";

    /**
     * value attribute.
     */
    private static final String VALUE_ATTR = "value";

    /**
     * span suffix.
     */
    private static final String SPAN_SUFFIX = "_span";

    /**
     * The list of attribute names for Rb and Cb.
     */
    public static final String[] RBCB_EVENTS_ATTRIBUTES = {
        "onFocus",
        "onBlur",
        "onClick",
        "onDblClick",
        "onChange",
        "onMouseDown",
        "onMouseUp",
        "onMouseOver",
        "onMouseMove",
        "onMouseOut",
        "onKeyPress",
        "onKeyDown",
        "onKeyUp"
    };

    /**
     * Creates a new instance of RbCbRendererBase.
     */
    RbCbRendererBase() {
        super();
    }

    /**
     * The getStyle method is implemented by subclasses to return the actual CSS
     * style class name for the given structural element of the rendered
     * component.
     *
     * @param theme Theme for the request we are processing.
     * @param styleCode one of the previously defined constants.
     * @return String
     */
    protected abstract String getStyle(Theme theme, int styleCode);

    /**
     * Implemented in the subclass to determine if the {@code item} is the
     * currently selected control.
     *
     * @param component UI component
     * @param context faces context
     * @return {@code boolean}
     */
    protected abstract boolean isSelected(FacesContext context,
            UIComponent component);

    /**
     * This implementation is empty.
     * @param context faces context
     * @param component UI component
     * @throws IOException if an IO error occurs
     */
    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component)
            throws IOException {
    }

    /**
     * Render a radio button or a checkbox.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     * @param theme the current theme
     * @param writer {@code ResponseWriter} to which the HTML will be output
     * @param type the INPUT element type attribute value.
     * @throws IOException if an error occurs
     */
    protected void renderSelection(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final String type)
            throws IOException {

        // Contain the radio button components within a span element
        // assigning the style and styleClass attribute to its
        // style and class attributes.
        writer.startElement(SPAN_ELEM, component);
        writer.writeAttribute(ID_ATTR,
                component.getClientId(context).concat(SPAN_SUFFIX), null);

        // Transfer explicit style attribute value to the span's style
        String prop = (String) ((RbCbSelector) component).getStyle();
        if (prop != null) {
            writer.writeAttribute(STYLE_ATTR, prop, STYLE_ATTR);
        }

        // Merge the standard style class with the styleClass
        // attribute
        String styleClass;
        if (((RbCbSelector) component).isDisabled()) {
            styleClass = getStyle(theme, SPAN_DIS);
        } else {
            styleClass = getStyle(theme, SPAN);
        }
        styleClass = getStyleClasses(context, component, styleClass);
        if (styleClass != null) {
            writer.writeAttribute(CLASS_ATTR, styleClass, null);
        }

        renderInput(context, component, theme, writer, type);
        renderImage(context, component, theme, writer);
        renderLabel(context, component, theme, writer);
        writer.endElement(SPAN_ELEM);
    }

    /**
     * Called from renderSelection to render an INPUT element of type
     * {@code type} for the specified {@code component}.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be rendered.
     * @param theme the current theme
     * @param writer {@code ResponseWriter} to which the HTML will be output
     * @param type the INPUT element type attribute value.
     * @throws IOException if an error occurs
     */
    @SuppressWarnings("checkstyle:magicnumber")
    protected void renderInput(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer, final String type) throws IOException {

        RbCbSelector rbcbSelector = (RbCbSelector) component;
        String componentId = component.getClientId(context);

        writer.startElement(INPUT_ELEM, component);
        writer.writeAttribute(TYPE_ATTR, type, null);

        // Set the control name to the radiobutton group id
        // and create a unique id from the radiobutton group id.
        writer.writeAttribute(ID_ATTR, componentId, ID_ATTR);

        // If name is not set use the component's clientId
        boolean inGroup = true;
        String prop = rbcbSelector.getName();
        if (prop == null) {
            prop = componentId;
            inGroup = false;
        }
        writer.writeAttribute(NAME_ATTR, prop, NAME_ATTR);

        // If the selectedValue is Boolean and the component is part
        // of a group, "name != null", then set the value of the value
        // attribute to "component.getClientId()".
        Object selectedValue = rbcbSelector.getSelectedValue();
        prop = convertValueToString(component, selectedValue);

        // Need to check immediate conditions
        // submittedValue will be non null if immediate is true on
        // some action component or a component on the page was invalid
        String[] subValue = (String[]) rbcbSelector.getSubmittedValue();
        if (subValue == null) {
            Object selected = rbcbSelector.getSelected();
            if (isSelected(context, component)) {
                writer.writeAttribute(CHECKED_ATTR, CHECKED_ATTR, null);
            }
            // A component can't be selected if "getSelected" returns null
            // Remember that the rendered value was null.
            setRenderedValue(component, selected);
        } else if (subValue.length != 0 && subValue[0].length() != 0) {
            // if the submittedValue is a 0 length array or the
            // first element is "" then the control is unchecked.

            // The submitted value has the String value of the
            // selectedValue property. Just compare the submittedValue
            // to it to determine if it is checked.
            //
            // Assume that the RENDERED_VALUE_STATE is the same
            // as the last rendering.
            if (prop != null && prop.equals(subValue[0])) {
                writer.writeAttribute(CHECKED_ATTR, CHECKED_ATTR, null);
            }
        }

        // If not ingroup prop has String version of selectedValue
        if (inGroup && (selectedValue instanceof Boolean)) {
            prop = componentId;
        }
        writer.writeAttribute(VALUE_ATTR, prop, null);

        boolean readonly = rbcbSelector.isReadOnly();
        if (readonly) {
            writer.writeAttribute(READONLY_ATTR, READONLY_ATTR,
                    READONLY_CC_ATTR);
        }

        String styleClass = null;
        boolean disabled = rbcbSelector.isDisabled();
        if (disabled) {
            writer.writeAttribute(DISABLED_ATTR, DISABLED_ATTR, DISABLED_ATTR);
            styleClass = getStyle(theme, INPUT_DIS);
        } else {
            styleClass = getStyle(theme, INPUT);
        }

        prop = rbcbSelector.getToolTip();
        if (prop != null) {
            writer.writeAttribute(TITLE_ATTR, prop, TOOLTIP_ATTR);
        }

        // Output the component's event attributes
        // Probably want the 'no auto submit javascript at some point'
        addStringAttributes(context, component, writer,
                RBCB_EVENTS_ATTRIBUTES);

        int tabIndex = rbcbSelector.getTabIndex();
        if (tabIndex > 0 && tabIndex < 32767) {
            writer.writeAttribute(TABINDEX_ATTR,
                    String.valueOf(tabIndex), TABINDEX_CC_ATTR);
        }

        writer.endElement(INPUT_ELEM);
    }

    /**
     * Called from renderSelection to render an IMG element for the specified
     * {@code item}control.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     * @param theme the current theme
     * @param writer {@code ResponseWriter} to which the HTML will be output
     * @throws IOException if an error occurs
     */
    protected void renderImage(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        UIComponent imageComponent = getImageComponent(context, component,
                theme);
        if (imageComponent != null) {
            renderComponent(imageComponent, context);
        }
    }

    /**
     * There is a serious issue creating child components for rendering
     * purposes. They must be updated to reflect the application state. This can
     * happen in two ways. Literal property values in the current component
     * being rendered that are intended for the child component may have been
     * changed by the application. Properties intended for the child component
     * may be binding expressions in which case the value binding must be
     * assigned to the property in the child component. Since both these values
     * must be updated in the child component since the application can change
     * them at any time, it makes sense to just always update the child with the
     * value obtained from the getter for the property vs. obtaining and
     * assigning the ValueBinding.
     *
     * Also child creation should occur in the component and the process of this
     * creation should produce a facet so that the renderer just asks for the
     * facet. It may originate from the component or the developer.
     *
     * @param context faces context
     * @param component UI component
     * @param theme theme in-use
     * @return UIComponent
     * @throws IOException if an IO error occurs
     */
    private UIComponent getImageComponent(final FacesContext context,
            final UIComponent component, final Theme theme) throws IOException {

        RbCbSelector rbcbComponent = (RbCbSelector) component;
        ImageComponent imageComponent
                = (ImageComponent) rbcbComponent.getImageComponent();
        if (imageComponent == null) {
            return null;
        }

        // Need to apply disabled class
        String styleClass;
        if (rbcbComponent.isDisabled()) {
            styleClass = getStyle(theme, IMAGE_DIS);
        } else {
            styleClass = getStyle(theme, IMAGE);
        }
        styleClass = getStyleClasses(context, imageComponent, styleClass);
        if (styleClass != null) {
            imageComponent.setStyleClass(styleClass);
        }
        return imageComponent;
    }

    /**
     * Called from {@code renderSelection} to render a LABEL.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     * @param theme the current theme
     * @param writer {@code ResponseWriter} to which the HTML will be output
     * @throws IOException if an IO error occurs
     */
    protected void renderLabel(final FacesContext context,
            final UIComponent component, final Theme theme,
            final ResponseWriter writer) throws IOException {

        UIComponent labelComponent = getLabelComponent(context, component,
                theme);
        if (labelComponent != null) {
            renderComponent(labelComponent, context);
        }
    }

    /**
     * Get the label component.
     * @param context faces context
     * @param component UI component
     * @param theme the current theme
     * @return UIComponent
     * @throws IOException if an IO error occurs
     */
    private UIComponent getLabelComponent(final FacesContext context,
            final UIComponent component, final Theme theme) throws IOException {

        RbCbSelector rbcbComponent = (RbCbSelector) component;
        Label labelComponent = (Label) rbcbComponent.getLabelComponent();
        if (labelComponent == null) {
            return null;
        }

        // Need to apply disabled class
        String styleClass;
        if (rbcbComponent.isDisabled()) {
            styleClass = getStyle(theme, LABEL_DIS);
        } else {
            styleClass = getStyle(theme, LABEL);
        }
        styleClass = getStyleClasses(context, labelComponent, styleClass);
        if (styleClass != null) {
            labelComponent.setStyleClass(styleClass);
        }
        return labelComponent;
    }

    /**
     * This implementation converts the selected of the rbcb component.
     * @param context faces context
     * @param component UI component
     * @param submittedValue submitted value to convert
     * @return Object
     * @throws ConverterException if a conversion error occurs
     */
    @Override
    public Object getConvertedValue(final FacesContext context,
            final UIComponent component, final Object submittedValue)
            throws ConverterException {

        // I know this looks odd but it gives an opportunity
        // for an alternative renderer for Checkbox and RadioButton
        // to provide a converter.
        return ((RbCbSelector) component).getConvertedValue(context,
                (RbCbSelector) component, submittedValue);
    }
}
