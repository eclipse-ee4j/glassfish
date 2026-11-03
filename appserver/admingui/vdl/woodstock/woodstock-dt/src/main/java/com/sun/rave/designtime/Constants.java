/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.rave.designtime;

/**
 * Placeholder added to pass compilation since the {@code com.sun.rave.}
 * classes are not available.
 */
public final class Constants {

    /**
     * Cannot be instanciated.
     */
    private Constants() {
    }

    /**
     * Property descriptor.
     */
    public static final class PropertyDescriptor {

        /**
         * Property category descriptor.
         */
        public static final String CATEGORY = "propertyCagetoryDescriptor";

        /**
         * Property attribute descriptor.
         */
        public static final String ATTRIBUTE_DESCRIPTOR =
                "propertyAttributeDescriptor";

        /**
         * Cannot be instanciated.
         */
        private PropertyDescriptor() {
        }
    }

    /**
     * Bean descriptor.
     */
    public static final class BeanDescriptor {

        /**
         * Bean instance name.
         */
        public static final String INSTANCE_NAME = "beanInstanceName";

        /**
         * Bean facet descriptors.
         */
        public static final String FACET_DESCRIPTORS = "beanFacetDescriptors";

        /**
         * Bean help key.
         */
        public static final String HELP_KEY = "beanHelpKey";

        /**
         * Bean container flag.
         */
        public static final String IS_CONTAINER = "beanIsContainer";

        /**
         * Bean help key properties.
         */
        public static final String PROPERTIES_HELP_KEY =
                "beanPropertiesHelpKey";

        /**
         * Bean categories property.
         */
        public static final String PROPERTY_CATEGORIES =
                "beanPropertyCategories";

        /**
         * Bean tag name.
         */
        public static final String TAG_NAME = "beanTagName";

        /**
         * Bean tag lib prefix.
         */
        public static final String TAGLIB_PREFIX = "beanTagLibPrefix";

        /**
         * Bean tag lib URI.
         */
        public static final String TAGLIB_URI = "beanTagLibUri";

        /**
         * Cannot be instanciated.
         */
        private BeanDescriptor() {
        }
    }

    /**
     * Event set descriptor.
     */
    public static final class EventSetDescriptor {

        /**
         * Event set property.
         */
        public static final String BINDING_PROPERTY = "eventSetProperty";

        /**
         * Cannot be instanciated.
         */
        private EventSetDescriptor() {
        }
    }
}
