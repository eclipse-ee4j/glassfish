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

import com.sun.webui.jsf.model.Markup;

import jakarta.el.ValueExpression;
import jakarta.faces.application.Application;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.convert.Converter;
import jakarta.faces.render.Renderer;

import java.io.IOException;
import java.util.Map;

import static com.sun.webui.jsf.util.RenderingUtilities.renderComponent;
import static com.sun.webui.jsf.util.RenderingUtilities.renderStyleClass;

/**
 * Abstract base class for concrete implementations of
 * {@code jakarta.faces.render.Renderer} for JavaServer Faces component libraries.
 */
public abstract class AbstractRenderer extends Renderer {

    /**
     * Base name of the resource bundle we will use for localization.
     */
    protected static final String BUNDLE
            = "com.sun.webui.jsf.renderkit.html.Bundle";

    /**
     * The list of attribute names in the HTML 4.01 Specification that
     * correspond to the entity type <em>%events;</em>.
     */
    public static final String[] EVENTS_ATTRIBUTES = {
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
        "onKeyUp",
    };

    /**
     * The list of attribute names in the HTML 4.01 Specification that
     * correspond to the entity type <em>%i18n;</em>.
     */
    public static final String[] I18N_ATTRIBUTES = {
        "dir",
        "lang",
    };

    /**
     * Core attributes that are simple pass through.
     */
    private static final String[] CORE_ATTRIBUTES = {
        "style",
        "title",
    };

    /**
     * Decode any new state of the specified {@code UIComponent} from the
     * request contained in the specified {@code FacesContext}, and store that
     * state on the {@code UIComponent}.
     *
     * The default implementation calls {@code setSubmittedValue()} on
     * components that implement EditableValueHolder (i.e. input fields).
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code UIComponent} to be decoded
     *
     * @exception NullPointerException if {@code context} or {@code component}
     * is {@code null}
     */
    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        // Enforce NPE requirements in the Javadocs
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

        // Save submitted value on EditableValueHolder components
        // unless they are disabled or read only
        if (component instanceof EditableValueHolder) {
            setSubmittedValue(context, component);
        }

    }

    /**
     * Render the beginning of the specified {@code UIComponent} to the output
     * stream or writer associated with the response we are creating.
     *
     * The default implementation calls {@code renderStart()} and
     * {@code renderAttributes()}
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code UIComponent} to be decoded
     *
     * @exception NullPointerException if {@code context} or {@code component}
     * is {@code null}
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void encodeBegin(final FacesContext context,
            final UIComponent component) throws IOException {

        // Enforce NPE requirements in the Javadocs
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

        /*
        if (LogUtil.finestEnabled()) {
        LogUtil.finest("encodeBegin(id=" + component.getId() +
        ", family=" + component.getFamily() +
        ", rendererType=" + component.getRendererType() + ")");
        }
         */
        // Render the element and attributes for this component
        if (component.isRendered()) {
            ResponseWriter writer = context.getResponseWriter();
            renderStart(context, component, writer);
            renderAttributes(context, component, writer);
        }

    }

    /**
     * Render the children of the specified {@code UIComponent} to the output
     * stream or writer associated with the response we are creating.
     *
     * The default implementation iterates through the children of this
     * component and renders them.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code UIComponent} to be decoded
     * @throws NullPointerException if {@code context} or {@code component}
     * is {@code null}
     * @throws IOException if an input/output error occurs
     */
    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {

        // We shouldn't bother with a default implementation - this is exactly
        // what happens when you rendersChildren = false. Why duplicate the
        // code here?

        // Enforce NPE requirements in the Javadocs
        if (context == null || component == null) {
            throw new NullPointerException();
        }

        /*
        if (LogUtil.finestEnabled()) {
        LogUtil.finest("encodeChildren(id=" + component.getId() +
        ", family=" + component.getFamily() +
        ", rendererType=" + component.getRendererType() + ")");
        }
         */
        if (component.isRendered()) {
            for (UIComponent kid : component.getChildren()) {
                renderComponent(kid, context);
            }
        }
    }

    /**
     * Render the ending of the specified {@code UIComponent} to the output
     * stream or writer associated with the response we are creating.
     *
     * The default implementation calls {@code renderEnd()}.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code UIComponent} to be decoded
     * @throws NullPointerException if {@code context} or {@code component}
     * is {@code null}
     * @throws IOException if an input/output error occurs
     */
    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        // Enforce NPE requirements in the Javadocs
        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }

        /*
        if (LogUtil.finestEnabled()) {
        LogUtil.finest("encodeEnd(id=" + component.getId() +
        ", family=" + component.getFamily() +
        ", rendererType=" + component.getRendererType() + ")");
        }
         */
        // Render the element closing for this component
        if (component.isRendered()) {
            ResponseWriter writer = context.getResponseWriter();
            renderEnd(context, component, writer);
        }

    }

    /**
     * Render any {@code boolean} attributes on the specified list that have
     * {@code true} values on the corresponding attribute of the specified
     * {@code UIComponent}. Attribute names are converted to lower case in the
     * rendered output.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code EditableValueHolder} component whose submitted
     * value is to be stored
     * @param writer {@code ResponseWriter} to which the element start should be
     * rendered
     * @param names List of attribute names to be passed through
     * @throws IOException if an input/output error occurs
     */
    protected void addBooleanAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer,
            final String[] names) throws IOException {

        if (names == null) {
            return;
        }
        Map attributes = component.getAttributes();
        boolean flag;
        Object value;
        for (String name : names) {
            value = attributes.get(name);
            if (value != null) {
                if (value instanceof String) {
                    flag = Boolean.parseBoolean((String) value);
                } else {
                    flag = Boolean.parseBoolean(value.toString());
                }
                if (flag) {
                    writer.writeAttribute(name.toLowerCase(),
                            name.toLowerCase(), name);
                }
            }
        }

    }

    /**
     * Render the "core" set of attributes for this {@code UIComponent}. The
     * default implementation conditionally generates the following attributes
     * with values as specified.
     * <ul>
     * <li><strong>id</strong> - If this component has a non-{@code null}
     * {@code id} property, and the identifier does not start with
     * {@code UIViewRoot.UNIQUE_ID_PREFIX}, render the {@code clientId}.</li>
     * <li><strong>class</strong> - If this component has a non-{@code null}
     * {@code styleClass} attribute, render its value, combined with the syles
     * parameter (if any).</li>
     * <li><strong>style</strong> - If this component has a non-{@code null}
     * {@code style} attribute, render its value.</li>
     * <li><strong>title</strong> - If this component has a non-{@code null}
     * {@code title} attribute, render its value.</li>
     * </ul>
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code EditableValueHolder} component whose submitted
     * value is to be stored
     * @param writer {@code ResponseWriter} to which the element start should be
     * rendered
     * @param styles Space-separated list of CSS style classes to add to the
     * {@code class} attribute, or {@code null} for none
     *
     * @exception IOException if an input/output error occurs
     */
    protected void addCoreAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer,
            final String styles) throws IOException {

        String id = component.getId();
        writer.writeAttribute("id", component.getClientId(context), "id");

        renderStyleClass(context, writer, component, styles);
        addStringAttributes(context, component, writer, CORE_ATTRIBUTES);

    }

    /**
     * Render any Integer attributes on the specified list that do not have
     * Integer.MIN_VALUE values on the corresponding attribute of the specified
     * {@code UIComponent}. Attribute names are converted to lower case in the
     * rendered output.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code EditableValueHolder} component whose submitted
     * value is to be stored
     * @param writer {@code ResponseWriter} to which the element start should be
     * rendered
     * @param names List of attribute names to be passed through
     * @throws IOException if an input/output error occurs
     */
    protected void addIntegerAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer,
            final String[] names) throws IOException {

        if (names == null) {
            return;
        }
        Map attributes = component.getAttributes();
        boolean flag;
        Object value;
        for (String name : names) {
            value = attributes.get(name);
            if ((value != null) && (value instanceof Integer)) {
                Integer ivalue = (Integer) value;
                if (!(ivalue == Integer.MIN_VALUE)) {
                    writer.writeAttribute(name.toLowerCase(), ivalue, name);
                }
            }
        }

    }

    /**
     * Add any attributes on the specified list directly to the specified
     * {@code ResponseWriter} for which the specified {@code UIComponent} has a
     * non-{@code null} String value. This method may be used to "pass through"
     * commonly used attribute name/value pairs with a minimum of code.
     * Attribute names are converted to lower case in the rendered output.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code EditableValueHolder} component whose submitted
     * value is to be stored
     * @param writer {@code ResponseWriter} to which the element start should be
     * rendered
     * @param names List of attribute names to be passed through
     *
     * @exception IOException if an input/output error occurs
     */
    protected static void addStringAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer,
            final String[] names) throws IOException {

        if (names == null) {
            return;
        }
        Map attributes = component.getAttributes();
        Object value;
        for (String name : names) {
            value = attributes.get(name);
            if (value != null) {
                if (value instanceof String) {
                    writer.writeAttribute(name.toLowerCase(),
                            (String) value, name);
                } else {
                    writer.writeAttribute(name.toLowerCase(),
                            value.toString(), name);
                }
            }
        }
    }

    /**
     * Get the {@code Application} instance for this web application.
     * @return Application
     */
    protected Application getApplication() {
        return getFacesContext().getApplication();
    }

    /**
     * Return the value to be stored, as an Object that has been converted from
     * the String representation (if necessary), or {@code null} if the String
     * representation is null.
     *
     * @param context FacesContext for the current request
     * @param component Component whose value is being processed (must be a
     * component that implements ValueHolder
     * @param value String representation of the value
     * @return Object
     */
    protected final Object getAsObject(final FacesContext context,
            final UIComponent component, final String value) {

        if (value == null) {
            return null;
        }
        Converter converter = ((ValueHolder) component).getConverter();
        if (converter == null) {
            ValueExpression vb = component.getValueExpression("value");
            if (vb != null) {
                Class clazz = vb.getType(context.getELContext());
                if (clazz != null) {
                    converter = getApplication().createConverter(clazz);
                }
            }
        }
        if (converter != null) {
            return converter.getAsObject(context, component, value);
        } else {
            return value;
        }

    }

    /**
     * Get the value to be rendered, as a String (converted if necessary).
     * @param context FacesContext for the current request
     * @param component Component whose value is to be retrieved (must be a
     * component that implements ValueHolder)
     * @return converted value, pr {@code null} if the value is null
     */
    @SuppressWarnings("unchecked")
    protected final String getAsString(final FacesContext context,
            final UIComponent component) {

        if (component == null) {
            return null;
        }
        if (component instanceof EditableValueHolder) {
            Object submittedValue = ((EditableValueHolder) component)
                    .getSubmittedValue();
            if (submittedValue != null) {
                return (String) submittedValue;
            }
        }
        Object value = ((ValueHolder) component).getValue();
        if (value == null) {
            return null;
        }
        Converter converter = ((ValueHolder) component).getConverter();
        if (converter == null) {
            if (value instanceof String) {
                return (String) value;
            }
            converter = getApplication().createConverter(value.getClass());
        }
        if (converter != null) {
            return converter.getAsString(context, component, value);
        } else {
            return value.toString();
        }
    }

    /**
     * Get the {@code ExternalContext} instance for the current request.
     * @return ExternalContext
     */
    protected final ExternalContext getExternalContext() {
        return (FacesContext.getCurrentInstance().getExternalContext());
    }

    /**
     * Get the {@code FacesContext} instance for the current request.
     * @return FacesContext
     */
    protected final FacesContext getFacesContext() {
        return (FacesContext.getCurrentInstance());
    }

    /**
     * Retrieve the submitted value from the request parameters for this
     * request.The default implementation retrieves the parameter value that
     * corresponds to the client identifier of this component.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code UIComponent} whose submitted value is to be
     * retrieved
     * @return Object
     */
    protected Object getSubmittedValue(final FacesContext context,
            final UIComponent component) {

        String clientId = component.getClientId(context);
        Map parameters = context.getExternalContext().getRequestParameterMap();
        return parameters.get(clientId);
    }

    /**
     * Test if the specified component is disabled.
     *
     * @param component {@code UIComponent} to be checked
     * @return {@code true} if disabled, {@code false} otherwise
     */
    protected final boolean isDisabled(final UIComponent component) {

        Object disabled = component.getAttributes().get("disabled");
        if (disabled == null) {
            return (false);
        }
        if (disabled instanceof String) {
            return (Boolean.parseBoolean((String) disabled));
        } else {
            return (disabled.equals(Boolean.TRUE));
        }
    }

    /**
     * Test if the specified component is read.
     *
     * @param component {@code UIComponent} to be checked
     * @return {@code true} if ready, {@code false} otherwise
     */
    protected final boolean isReadOnly(final UIComponent component) {

        Object readonly = component.getAttributes().get("readonly");
        if (readonly == null) {
            return (false);
        }
        if (readonly instanceof String) {
            return (Boolean.parseBoolean((String) readonly));
        } else {
            return (readonly.equals(Boolean.TRUE));
        }

    }

    /**
     * Render the element attributes for the generated markup related to this
     * component. Simple renderers that create a single markup element for this
     * component should override this method and include calls to to
     * {@code writeAttribute()} and {@code writeURIAttribute} on the specified
     * {@code ResponseWriter}.
     *
     * The default implementation does nothing.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code EditableValueHolder} component whose submitted
     * value is to be stored
     * @param writer {@code ResponseWriter} to which the element start should be
     * rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAttributes(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {
    }

    /**
     * Render the element end for the generated markup related to this
     * component. Simple renderers that create a single markup element for this
     * component should override this method and include a call to
     * {@code endElement()} on the specified {@code ResponseWriter}.
     *
     * The default implementation does nothing.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code EditableValueHolder} component whose submitted
     * value is to be stored
     * @param writer {@code ResponseWriter} to which the element start should be
     * rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderEnd(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {
    }

    /**
     * Render the specified markup to the current response.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code UIComponent} associated with this markup
     * @param writer {@code ResponseWriter} to which the markup should be
     * rendered
     * @param markup {@link Markup} to be rendered
     * @throws java.io.IOException if an input/output error occurs
     */
    protected void renderMarkup(final FacesContext context,
            final UIComponent component, final ResponseWriter writer,
            final Markup markup) throws IOException {

        writer.write(markup.getMarkup());

    }

    /**
     * Render the element start for the generated markup related to this
     * component. Simple renderers that create a single markup element for this
     * component should override this method and include a call to
     * {@code startElement()} on the specified {@code ResponseWriter}.
     *
     * The default implementation does nothing.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code EditableValueHolder} component whose submitted
     * value is to be stored
     * @param writer {@code ResponseWriter} to which the element start should be
     * rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderStart(final FacesContext context,
            final UIComponent component, final ResponseWriter writer)
            throws IOException {
    }

    /**
     * If a submitted value was included on this request, store it in the
     * component as appropriate.
     *
     * The default implementation determines whether this component implements
     * {@code EditableValueHolder}. If so, it checks for a request parameter
     * with the same name as the {@code clientId} of this {@code UIComponent}.
     * If there is such a parameter, its value is passed (as a String) to the
     * {@code setSubmittedValue()} method on the {@code EditableValueHolder}
     * component.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code EditableValueHolder} component whose submitted
     * value is to be stored
     */
    protected void setSubmittedValue(final FacesContext context,
            final UIComponent component) {

        if (!(component instanceof EditableValueHolder)) {
            return;
        }
        component.getAttributes().put("submittedValue",
                getSubmittedValue(context, component));
    }
}
