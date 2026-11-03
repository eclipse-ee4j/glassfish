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
package com.sun.webui.jsf.faces;

import com.sun.faces.annotation.Resolver;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeJavascript;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.theme.ThemeTemplates;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.PropertyNotWritableException;
import jakarta.faces.context.FacesContext;

import java.lang.reflect.Field;

/**
 * {@code PropertyResolver} implementation that passes calls to
 * {@code getValue()},{@code getType()}, {@code isReadOnly()}, and
 * {@code setValue()} to the underlying {@link Theme} instance.
 *
 * Examples of supported expressions:
 * <ul>
 * <li>{@code #{themeStyles.CONTENT_MARGIN}}
 * This expression binds to a value in the {@link Theme} corresponding to the
 * CONTENT_MARGIN constant in ThemeStyles.</li>
 * <li> {@code #{themeImages.ALERT_ERROR_LARGE}}
 * This expression binds to a value in the {@link Theme} corresponding to the
 * ALERT_ERROR_LARGE constant in ThemeImages.</li>
 * <li>{@code #{themeJavascript.DOJO}}
 * This expression binds to the value of the {@link Theme} value corresponding
 * to the DOJO constant in ThemeJavascript.</li>
 * <li>{@code #{themeMessages['EditableList.invalidRemove']}}
 * This expression binds to a value in the {@link Theme} corresponding to the
 * EditableList.invalidRemove key in messages.properties.</li>
 * <li>{@code #{themeTemplates.PROGRESSBAR}}
 * This expression binds to a value in the {@link Theme} corresponding to the
 * PROGRESSBAR constant in ThemeTemplates.</li>
 * </ul>
 */
@Resolver
public final class ThemeELResolver extends ELResolver {

    /**
     * Theme images key.
     */
    private static final String THEME_IMAGES = "themeImages";

    /**
     * Theme JS key.
     */
    private static final String THEME_JAVASCRIPT = "themeJavascript";

    /**
     * Theme messages key.
     */
    private static final String THEME_MESSAGES = "themeMessages";

    /**
     * Theme CSS styles key.
     */
    private static final String THEME_STYLES = "themeStyles";

    /**
     * Theme templates key.
     */
    private static final String THEME_TEMPLATES = "themeTemplates";

    /**
     * Default constructor.
     */
    public ThemeELResolver() {
    }

    @Override
    public Object getValue(final ELContext context, final Object base,
            final Object property) {

        if (property == null) {
            throw new PropertyNotFoundException("Property cannot be null.");
        }
        if (context == null) {
            throw new NullPointerException();
        }

        Object result = null;
        if (base != null) {
            // Resolve the given property associated with base object.
            if (base instanceof Images) {
                result = ((Images) base).getValue(property.toString());
                context.setPropertyResolved(true);
            } else if (base instanceof Javascript) {
                result = ((Javascript) base).getValue(property.toString());
                context.setPropertyResolved(true);
            } else if (base instanceof Messages) {
                result = ((Messages) base).getValue(property.toString());
                context.setPropertyResolved(true);
            } else if (base instanceof Styles) {
                result = ((Styles) base).getValue(property.toString());
                context.setPropertyResolved(true);
            } else if (base instanceof Templates) {
                result = ((Templates) base).getValue(property.toString());
                context.setPropertyResolved(true);
            }
        } else {
            // Variable resolution is a special case of property resolution
            // where the base is null.
            if (THEME_IMAGES.equals(property)) {
                result = new Images();
                context.setPropertyResolved(true);
            } else if (THEME_JAVASCRIPT.equals(property)) {
                result = new Javascript();
                context.setPropertyResolved(true);
            } else if (THEME_MESSAGES.equals(property)) {
                result = new Messages();
                context.setPropertyResolved(true);
            } else if (THEME_STYLES.equals(property)) {
                result = new Styles();
                context.setPropertyResolved(true);
            } else if (THEME_TEMPLATES.equals(property)) {
                result = new Templates();
                context.setPropertyResolved(true);
            }
        }
        return result;
    }

    @Override
    public void setValue(final ELContext context, final Object base,
            final Object property, final Object value) {

        // Variable resolution is a special case of property resolution
        // where the base is null.
        if (base != null) {
            return;
        }
        if (property == null) {
            throw new PropertyNotFoundException("Property cannot be null.");
        }
        if (base != null) {
            // Regardless of the base object, all properties are read only.
            if (base instanceof Images
                    || base instanceof Javascript
                    || base instanceof Messages
                    || base instanceof Styles
                    || base instanceof Templates) {
                throw new PropertyNotWritableException(property.toString());
            }
        } else {
            // Variable resolution is a special case of property resolution
            // where the base is null. Thus, We need to provide an object to
            // resolve the next property.
            if (THEME_IMAGES.equals(property)
                    || THEME_JAVASCRIPT.equals(property)
                    || THEME_MESSAGES.equals(property)
                    || THEME_STYLES.equals(property)
                    || THEME_TEMPLATES.equals(property)) {
                throw new PropertyNotWritableException(property.toString());
            }
        }
    }

    @Override
    public boolean isReadOnly(final ELContext context, final Object base,
            final Object property) {

        if (property == null) {
            throw new PropertyNotFoundException("Property cannot be null.");
        }
        if (context == null) {
            throw new NullPointerException();
        }
        boolean result = false;
        if (base != null) {
            // Regardless of the base object, all properties are read only.
            if (base instanceof Images
                    || base instanceof Javascript
                    || base instanceof Messages
                    || base instanceof Styles
                    || base instanceof Templates) {
                result = true;
                context.setPropertyResolved(true);
            }
        } else {
            // Variable resolution is a special case of property resolution
            // where the base is null. Thus, We need to provide an object to
            // resolve the next property.
            if (THEME_IMAGES.equals(property)
                    || THEME_JAVASCRIPT.equals(property)
                    || THEME_MESSAGES.equals(property)
                    || THEME_STYLES.equals(property)
                    || THEME_TEMPLATES.equals(property)) {
                result = true;
                context.setPropertyResolved(true);
            }
        }
        return result;
    }

    @Override
    public Class getType(final ELContext context, final Object base,
            final Object property) {

        if (property == null) {
            throw new PropertyNotFoundException("Property cannot be null.");
        }
        if (context == null) {
            throw new NullPointerException();
        }
        Class result = null;
        if (base != null) {
            // Regardless of the base object, all properties are Strings.
            if (base instanceof Images
                    || base instanceof Javascript
                    || base instanceof Messages
                    || base instanceof Styles
                    || base instanceof Templates) {
                result = String.class;
                context.setPropertyResolved(true);
            }
        } else {
            // Variable resolution is a special case of property resolution
            // where the base is null. Thus, We need to provide an object to
            // resolve the next property.
            if (THEME_IMAGES.equals(property)
                    || THEME_JAVASCRIPT.equals(property)
                    || THEME_MESSAGES.equals(property)
                    || THEME_STYLES.equals(property)
                    || THEME_TEMPLATES.equals(property)) {
                result = String.class;
                context.setPropertyResolved(true);
            }
        }
        return result;
    }

    @Override
    public Class getCommonPropertyType(final ELContext context,
            final Object base) {

        // Variable resolution is a special case of property resolution
        // where the base is null. Thus, if the object is not null, we're not
        // meant to resolve it.
        if (base != null) {
            return null;
        }
        return String.class;
    }

    /**
     * Private class to resolve ThemeImage constants.
     */
    private static final class Images {

        /**
         * Height suffix.
         */
        private static final String HEIGHT_SUFFIX = "_HEIGHT";

        /**
         * Width suffix.
         */
        private static final String WIDTH_SUFFIX = "_WIDTH";

        /**
         * Alternate suffix.
         */
        private static final String ALT_SUFFIX = "_ALT";

        /**
         * Resolve ThemeImages constants.
         *
         * @param property The ThemeImages constant.
         * @return Object
         */
        public Object getValue(final String property) {
            if (property == null) {
                throw new PropertyNotFoundException(
                        "Property cannot be null.");
            }

            Object result;
            Theme theme = ThemeUtilities
                    .getTheme(FacesContext.getCurrentInstance());

            try {
                Field field = ThemeImages.class.getField(property);
                String value = field.get(null).toString();

                // Since there are a few diffierent types of properties, we need
                // to resolve each a bit differently.
                if (property.endsWith(ALT_SUFFIX)) {
                    // Resolve image alt strings.
                    result = theme.getMessage(theme.getImageString(value));
                } else if (property.endsWith(HEIGHT_SUFFIX)) {
                    // Resolve image height properties.
                    result = theme.getImageString(value);
                } else if (property.endsWith(WIDTH_SUFFIX)) {
                    // Resolve image width properties.
                    result = theme.getImageString(value);
                } else {
                    // Resolve image paths.
                    result = theme.getImage(value).getPath();
                }
            } catch (IllegalAccessException e) {
                // Try to resolve as resource key, bypassing ThemeImages.
                result = theme.getImageString(property);
            } catch (IllegalArgumentException e) {
                // Try to resolve as resource key, bypassing ThemeImages.
                result = theme.getImageString(property);
            } catch (NoSuchFieldException e) {
                // Try to resolve as resource key, bypassing ThemeImages.
                result = theme.getImageString(property);
            } catch (SecurityException e) {
                // Try to resolve as resource key, bypassing ThemeImages.
                result = theme.getImageString(property);
            }
            return result;
        }
    }

    /**
     * Private class to resolve ThemeJavascript constants.
     */
    private static final class Javascript {

        /**
         * JS prefix.
         */
        private static final String JS_PREFIX = "JS_PREFIX";

        /**
         * Module path prefix.
         */
        private static final String MODULE_PATH = "MODULE_PATH";

        /**
         * Module prefix.
         */
        private static final String MODULE_PREFIX = "MODULE_PREFIX";

        /**
         * Resolve ThemeJavascript constants.
         *
         * @param property The ThemeJavascript constant.
         * @return Object
         */
        public Object getValue(final String property) {
            if (property == null) {
                throw new PropertyNotFoundException("Property cannot be null.");
            }

            Object result;
            Theme theme = ThemeUtilities
                    .getTheme(FacesContext.getCurrentInstance());

            try {
                Field field = ThemeJavascript.class.getField(property);
                String value = field.get(null).toString();

                // This is a special case where the theme path is not prefixed.
                if (JS_PREFIX.equals(property)
                        || MODULE_PATH.equals(property)
                        || MODULE_PREFIX.equals(property)) {
                    result = theme.getJSString(value);
                } else {
                    // Resolve Javascript file path.
                    result = theme.getPathToJSFile(value);
                }
            } catch (IllegalAccessException e) {
                // Try to resolve as resource key, bypassing ThemeJavascript.
                result = theme.getJSString(property);
            } catch (IllegalArgumentException e) {
                // Try to resolve as resource key, bypassing ThemeJavascript.
                result = theme.getJSString(property);
            } catch (NoSuchFieldException e) {
                // Try to resolve as resource key, bypassing ThemeJavascript.
                result = theme.getJSString(property);
            } catch (SecurityException e) {
                // Try to resolve as resource key, bypassing ThemeJavascript.
                result = theme.getJSString(property);
            }
            return result;
        }
    }

    /**
     * Private class to resolve messages.properties keys.
     */
    private static final class Messages {

        /**
         * Resolve messages.properties keys.
         *
         * @param property The messages.properties key.
         * @return Object
         */
        public Object getValue(final String property) {
            if (property == null) {
                throw new PropertyNotFoundException("Property cannot be null.");
            }

            Object result;
            Theme theme = ThemeUtilities
                    .getTheme(FacesContext.getCurrentInstance());

            // Resolve resource bundle string.
            result = theme.getMessage(property);
            return result;
        }
    }

    /**
     * Private class to resolve ThemeStyles constants.
     */
    private static final class Styles {

        /**
         * Resolve ThemeStyles constants.
         *
         * @param property The ThemeStyles constant.
         * @return Object
         */
        public Object getValue(final String property) {
            if (property == null) {
                throw new PropertyNotFoundException("Property cannot be null.");
            }

            Object result;
            Theme theme = ThemeUtilities
                    .getTheme(FacesContext.getCurrentInstance());

            try {
                // Resolve the style selector.
                Field field = ThemeStyles.class.getField(property);
                result = theme.getStyleClass(field.get(null).toString());
            } catch (IllegalAccessException e) {
                // Try to resolve as resource key, bypassing ThemeStyles.
                result = theme.getStyleClass(property);
            } catch (IllegalArgumentException e) {
                // Try to resolve as resource key, bypassing ThemeStyles.
                result = theme.getStyleClass(property);
            } catch (NoSuchFieldException e) {
                // Try to resolve as resource key, bypassing ThemeStyles.
                result = theme.getStyleClass(property);
            } catch (SecurityException e) {
                // Try to resolve as resource key, bypassing ThemeStyles.
                result = theme.getStyleClass(property);
            }
            return result;
        }
    }

    /**
     * Private class to resolve ThemeTemplates constants.
     */
    private static final class Templates {

        /**
         * Resolve ThemeTemplates constants.
         *
         * @param property The ThemeTemplates constant.
         * @return Object
         */
        public Object getValue(final String property) {
            if (property == null) {
                throw new PropertyNotFoundException("Property cannot be null.");
            }

            Object result;
            Theme theme = ThemeUtilities
                    .getTheme(FacesContext.getCurrentInstance());

            try {
                // Resolve the HTML template path.
                Field field = ThemeTemplates.class.getField(property);
                result = theme.getPathToTemplate(field.get(null).toString());
            } catch (IllegalAccessException e) {
                // Try to resolve as resource key, bypassing ThemeTemplates.
                result = theme.getPathToTemplate(property);
            } catch (IllegalArgumentException e) {
                // Try to resolve as resource key, bypassing ThemeTemplates.
                result = theme.getPathToTemplate(property);
            } catch (NoSuchFieldException e) {
                // Try to resolve as resource key, bypassing ThemeTemplates.
                result = theme.getPathToTemplate(property);
            } catch (SecurityException e) {
                // Try to resolve as resource key, bypassing ThemeTemplates.
                result = theme.getPathToTemplate(property);
            }
            return result;
        }
    }
}
