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

package com.sun.webui.jsf.util;

import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;

import java.lang.reflect.Array;
import java.util.HashMap;

/**
 * The ConversionUtilities class provides utility method for converting values
 * to and from Strings. Use this class if your component processes input from
 * the user, or displays a converted value.
 */
public final class ConversionUtilities {

    /**
     * Cannot be instanciated.
     */
    private ConversionUtilities() {
    }

    /**
     * Rendered table null values key.
     */
    private static final String RENDERED_TABLE_NULL_VALUES
            = "_RENDERED_TABLE_NULL_VALUES_";

    /**
     * Rendered null value key.
     */
    private static final String RENDERED_NULL_VALUE = "_RENDERED_NULL_VALUE_";

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Convert the values of a component with a single (non-list, non-array)
     * value. Use this method if
     * <ul>
     * <li>the component always binds the user input to a single object (e.g. a
     * text field component); or </li>
     * <li>to handle the single object case when the component may bind the user
     * input to a single object <em>or</em> to a collection of objects (e.g. a
     * list component). Use a ValueTypeEvaluator to evaluate the value binding
     * type. </li>
     * </ul>
     *
     * @param component The component whose value is getting converted
     * @param rawValue The submitted value of the component
     * @param context The FacesContext of the request
     * @throws ConverterException if the conversion fails
     * @return An Object representing the converted value. If rawValue is
     * {@code null} return {@code null}.
     * @see ValueTypeEvaluator
     */
    public static Object convertValueToObject(final UIComponent component,
            final String rawValue, final FacesContext context)
            throws ConverterException {

        if (DEBUG) {
            log("convertValueToObject()");
        }

        // Optimization based on
        // jakarta.faces.convert.Converter getAsObject.
        // It says:
        // return null if the value to convert is
        // null otherwise the result of the conversion
        if (rawValue == null || !(component instanceof ValueHolder)) {
            return rawValue;
        }

        ValueHolder valueHolder = ((ValueHolder) component);
        Converter converter = valueHolder.getConverter();
        if (converter == null) {

            Class clazz;
            // Determine the type of the component's value object
            ValueExpression valueBinding =
                    component.getValueExpression("value");
            if (valueBinding == null) {
                Object value = valueHolder.getValue();
                if (value == null) {
                    return rawValue;
                }
                clazz = value.getClass();
            } else {
                clazz = valueBinding.getType(context.getELContext());
            }

            // You can't register a default converter for
            // String/Object for the whole app (as opposed to for the
            // individual component). In this case we just
            // return the String.
            if (clazz == null
                    || clazz.equals(String.class)
                    || clazz.equals(Object.class)) {
                return rawValue;
            }

            // Try to get a converter
            converter = getConverterForClass(clazz);
            if (converter == null) {
                return rawValue;
            }
        }
        if (DEBUG) {
            log("Raw value was: " + rawValue);
            log("Converted value is: "
                    + converter.getAsObject(context, component, rawValue));
        }
        return converter.getAsObject(context, component, rawValue);
    }

    /**
     * Convert a String array of submitted values to the appropriate
     * type of Array for the value Object. This method assumes that
     * the value binding for the value of the component has been
     * determined to be an array (and as a consequence that the
     * component implements ValueHolder).
     *
     * <p>To evaluate the valueBinding, use the ValueTypeEvaluator
     * class.</p>
     * @param component The component whose submitted values are to be
     * converted
     * @param rawValues The submitted value of the component
     * @param context The FacesContext of the request
     * @see ValueTypeEvaluator
     * @throws ConverterException if the conversion fails
     * @return An array of converted values
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static Object convertValueToArray(final UIComponent component,
            final String[] rawValues, final FacesContext context)
            throws ConverterException {

        if (DEBUG) {
            log("::convertValueToArray()");
        }

        // By definition Converter returns null if the value to
        // convert is null. Do so here.
        //
        if (rawValues == null) {
            return null;
        }

        // Get the class of the array members. We expect that the
        // component's value binding for its value has been determined
        // to be an array, as this is a condition of invoking this
        // method.
        Class clazz;

        // Get any converter specified by the page author
        Converter converter = ((ValueHolder) component).getConverter();

        try {
            // Try to obtain the actual value type by obtaining
            // the value binding's real value instance and determining its
            // type, if the component's defined value type is Object.
            //
            ValueExpression vb = component.getValueExpression("value");
            Class valueClass = vb.getType(context.getELContext());
            if (Object.class.equals(valueClass)) {
                Object value = vb.getValue(context.getELContext());
                if (value != null) {
                    valueClass = value.getClass();
                }
            }
            clazz = valueClass.getComponentType();
        } catch (Exception ex) {
            // This may fail because we don't have a valuebinding (the
            // developer may have used the binding attribute)
            Object value = ((ValueHolder) component).getValue();
            if (value == null) {
                // Now we're on thin ice. If there is a converter, we'll
                // try to set this as an object array; if not, we'll just
                // go for String.
                if (converter != null) {
                    if (DEBUG) {
                        log("\tNo class info, converter present"
                                + " - using object...");
                    }
                    clazz = Object.class;
                } else {
                    if (DEBUG) {
                        log("\tNo class info, no converter - using String...");
                    }
                    clazz = String.class;
                }

            } else {
                clazz = value.getClass().getComponentType();
                if (DEBUG) {
                    log("\tClass is " + clazz.getName());
                }
            }
        }

        // If the array members are Strings, no conversion is
        // necessary
        if (clazz.equals(String.class)) {
            if (DEBUG) {
                log("\tArray class is String, no conversion necessary");
                log("\tValues are ");
                for (int counter = 0; counter < rawValues.length; ++counter) {
                    log("\t" + rawValues[counter]);
                }
            }
            return rawValues;
        }

        // We know rawValues is not null
        int arraySize;
        arraySize = rawValues.length;
        if (DEBUG) {
            log("\tNumber of values is " + String.valueOf(arraySize));
        }

        Object valueArray = Array.newInstance(clazz, arraySize);

        // If there are no new values, return an empty array
        if (arraySize == 0) {
            if (DEBUG) {
                log("\tEmpty value array, return new empty array");
                log("\tof type " + valueArray.toString());
            }
            return valueArray;
        }

        // Populate the array by converting each of the raw values

        // If there is no converter, look for the default converter
        if (converter == null) {
            if (DEBUG) {
                log("\tAttempt to get a default converter");
            }
            converter = getConverterForClass(clazz);
        } else if (DEBUG) {
            log("\tRetrieved converter attached to component");
        }

        int counter;
        if (converter == null) {
            if (DEBUG) {
                log("\tNo converter found");
            }
            if (clazz.equals(Object.class)) {
                if (DEBUG) {
                    log("\tArray class is object, return the String array");
                    log("\tValues are\n");
                    for (counter = 0; counter < rawValues.length; ++counter) {
                        log("\n" + rawValues[counter]);
                    }
                }
                return rawValues;
            }

            // Failed to deal with submitted data. Throw an
            // exception.
            String valueString = "";
            for (counter = 0; counter < rawValues.length; counter++) {
                valueString = valueString + " " + rawValues[counter];
            }

            String message = "Could not find converter for " + valueString;
            throw new ConverterException(message);
        }

        if (clazz.isPrimitive()) {
            for (counter = 0; counter < arraySize; ++counter) {
                addPrimitiveToArray(component,
                        context,
                        converter,
                        clazz,
                        valueArray,
                        counter,
                        rawValues[counter]);
            }
        } else {
            for (counter = 0; counter < arraySize; ++counter) {
                Array.set(valueArray, counter, converter.getAsObject(context,
                        (UIComponent) component, rawValues[counter]));
            }
        }
        return valueArray;
    }

    /**
     * Add the given primitive value to the given array.
     * @param component UI component
     * @param context faces context
     * @param converter converter
     * @param clazz primitive class
     * @param valueArray array to add to
     * @param arrayIndex array index where to add
     * @param rawValue raw value to convert
     */
    private static void addPrimitiveToArray(final UIComponent component,
            final FacesContext context, final Converter converter,
            final Class clazz, final Object valueArray, final int arrayIndex,
            final String rawValue) {

        Object valueObject =
                converter.getAsObject(context, component, rawValue);
        if (clazz.equals(Boolean.TYPE)) {
            boolean value = ((Boolean) valueObject);
            Array.setBoolean(valueArray, arrayIndex, value);
        } else if (clazz.equals(Byte.TYPE)) {
            byte value = ((Byte) valueObject);
            Array.setByte(valueArray, arrayIndex, value);
        } else if (clazz.equals(Double.TYPE)) {
            double value = ((Double) valueObject);
            Array.setDouble(valueArray, arrayIndex, value);
        } else if (clazz.equals(Float.TYPE)) {
            float value = ((Float) valueObject);
            Array.setFloat(valueArray, arrayIndex, value);
        } else if (clazz.equals(Integer.TYPE)) {
            int value = ((Integer) valueObject);
            Array.setInt(valueArray, arrayIndex, value);
        } else if (clazz.equals(Character.TYPE)) {
            char value = ((Character) valueObject);
            Array.setChar(valueArray, arrayIndex, value);
        } else if (clazz.equals(Short.TYPE)) {
            short value = ((Short) valueObject);
            Array.setShort(valueArray, arrayIndex, value);
        } else if (clazz.equals(Long.TYPE)) {
            long value = ((Long) valueObject);
            Array.setLong(valueArray, arrayIndex, value);
        }
    }

    /**
     * Converts an Object (which may or may not be the value of the component)
     * to a String using the converter associated with the component. This
     * method can be used to convert the value of the component, or the value of
     * an Object associated with the component, such as the objects representing
     * the options for a list box or a check box group.
     *
     * @param component The component that needs to display the value as a
     * String
     * @param realValue The object that the component is to display
     * @return If converting the Object to a String fails
     * @throws ConverterException if the conversion fails
     */
    @SuppressWarnings("unchecked")
    public static String convertValueToString(final UIComponent component,
            final Object realValue) throws ConverterException {

        if (DEBUG) {
            log("convertValueToString(UIComponent, Object)");
        }

        // The way the RI algorithm is written, it ends up returning
        // and empty string if the realValue is null and there is no
        // converter, and null if there is a converter (the converter
        // is never applied). I don't think that's right, but I'm
        // not sure what the right thing to do is. I return an empty
        // string for now.
        if (realValue == null) {
            return new String();
        }
        if (realValue instanceof String) {
            return (String) realValue;
        }
        if (!(component instanceof ValueHolder)) {
            return String.valueOf(realValue);
        }

        Converter converter = ((ValueHolder) component).getConverter();

        // Case 1: no converter specified for the component. Try
        // getting a default converter, and if that fails, invoke
        // the .toString() method.
        if (converter == null) {

            // if converter attribute set, try to acquire a converter
            // using its class type. (avk note: this is the comment from
            // the RI - not sure what it's supposed to mean)
            converter = getConverterForClass(realValue.getClass());

            // if there is no default converter available for this identifier,
            // assume the model type to be String. Otherwise proceed to case 2.
            if (converter == null) {
                return String.valueOf(realValue);
            }
        }

        // Case 2: we have found a converter.
        FacesContext context = FacesContext.getCurrentInstance();
        return converter.getAsString(context, component, realValue);
    }

    /**
     * This method retrieves an appropriate converter based on the
     * type of an object.
     * @param converterClass The name of the converter class
     * @return An instance of the appropriate converter type
     */
    public static Converter getConverterForClass(final Class converterClass) {
        if (converterClass == null) {
            return null;
        }
        try {
            ApplicationFactory aFactory =
                    (ApplicationFactory) FactoryFinder.getFactory(
                    FactoryFinder.APPLICATION_FACTORY);
            Application application = aFactory.getApplication();
            return (application.createConverter(converterClass));
        } catch (FacesException e) {
            return (null);
        }
    }

    /**
     * Log a message to the standard output.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(ConversionUtilities.class.getSimpleName() + "::"
                + msg);
    }

    /**
     * Return the converted value of submittedValue.If submittedValue is null,
     * return null.If submittedValue is "", check the rendered value.If the the
     * value that was rendered was null, return null else continue to convert.
     *
     * @param context faces context
     * @param submittedValue value
     * @param component UI component
     * @return Object
     * @throws ConverterException if an conversion error occurs
     */
    public static Object convertRenderedValue(final FacesContext context,
            final Object submittedValue, final UIComponent component)
            throws ConverterException {

        Converter converter = ((ValueHolder) component).getConverter();

        // If the component has a converter we can't assume that
        // "" should be returned if "" was rendered or "" was rendered
        // for null.
        if (converter == null) {
            // See if we rendered null.
            // If we rendered null and the submitted value was ""
            // return null
            if (renderedNull(component) && submittedValue instanceof String
                    && ((String) submittedValue).length() == 0) {
                return null;
            }
        }
        // If submittedValue is null, convertValueToObject returns null
        // as does Converter by definition.
        return ConversionUtilities.convertValueToObject(component,
                (String) submittedValue, context);
    }

    /**
     * Record the value being rendered.
     *
     * @param component The component being rendered.
     * @param value The value being rendered.
     */
    public static void setRenderedValue(final UIComponent component,
            final Object value) {

        // First remove the attribute.
        // Need to do this because a null value does nothing.
        // Therefore the last value specified will remain.
        //
        component.getAttributes().remove(RENDERED_NULL_VALUE);

        // If the value is null, put barfs.
        // So getRenderedValue will return null, if there is no
        // RENDERED_NULL_VALUE property. Interpret this to mean that
        // "null" was saved. I'd rather not but as long as the
        // explicit property is not sought outside of these methods
        // then it shouldn't be a problem.
        //

        if (value == null) {
            component.getAttributes().put(RENDERED_NULL_VALUE, Boolean.TRUE);
        }
    }

    /**
     * Return true if the stored rendered value on the specified
     * component was null.
     * @param component UI component
     * @return {@code true} if rendered the value, {@code false} otherwise
     */
    public static boolean renderedNull(final UIComponent component) {
        return (Boolean) component.getAttributes()
                .get(RENDERED_NULL_VALUE) != null;
    }

    /**
     * Remove the stored rendered value from the specified component.
     * @param component UI component
     */
    public static void removeRenderedValue(final UIComponent component) {
        component.getAttributes().remove(RENDERED_NULL_VALUE);
    }

    /**
     * Used to preserve the rendered value when a component is
     * used within a table. Since there is only one component
     * instance when used in a table column the rendered value
     * must be maintained for each "virtual" component instance
     * for the rows in the column.
     *
     * @param context The current FacesContext for this request.
     * @param component The component that is appearing in the table.
     */
    @SuppressWarnings("unchecked")
    public static void saveRenderedValueState(final FacesContext context,
            final UIComponent component) {

        boolean renderedNullValue = renderedNull(component);
        HashMap rv = (HashMap) component.getAttributes()
                .get(RENDERED_TABLE_NULL_VALUES);

        if (rv == null) {
            if (renderedNullValue) {
                rv = new HashMap();
                component.getAttributes().put(RENDERED_TABLE_NULL_VALUES, rv);
                rv.put(component.getClientId(context), null);
                component.getAttributes().remove(RENDERED_NULL_VALUE);
            }
        } else if (!renderedNullValue) {
            rv.remove(component.getClientId(context));
        } else {
            rv.put(component.getClientId(context), null);
            removeRenderedValue(component);
        }
    }

    /**
     * Used to restore the rendered value when a component is
     * used within a table. Since there is only one component
     * instance when used in a table column the rendered value
     * must be maintained and restored for each "virtual" component
     * instance for the rows in the column.
     *
     * @param context The current FacesContext for this request.
     * @param component The component that is appearing in the table.
     */
    public static void restoreRenderedValueState(final FacesContext context,
            final UIComponent component) {

        HashMap rv = (HashMap) component.getAttributes().get(
                RENDERED_TABLE_NULL_VALUES);
        if (rv != null) {
            if (rv.containsKey(component.getClientId(context))) {
                setRenderedValue(component, null);
            }
        }
    }

    /**
     * Remove the storage for the "virtual" for the specified
     * component used to save the rendered value for the "virtual"
     * instances of this component when used in a table.
     * @param component UI component
     */
    public static void removeSavedRenderedValueState(
            final UIComponent component) {

        component.getAttributes().remove(RENDERED_TABLE_NULL_VALUES);
    }
}
