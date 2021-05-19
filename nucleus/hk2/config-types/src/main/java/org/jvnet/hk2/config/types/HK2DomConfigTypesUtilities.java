/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config.types;

import java.util.LinkedList;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.config.HK2DomConfigUtilities;

/**
 * @author jwells
 *
 */
public class HK2DomConfigTypesUtilities {
    private final static String PROPERTY_GENERATED_INJECTOR_CLASS = "org.jvnet.hk2.config.types.PropertyInjector";
    private final static String CONFIG_INJECTOR_CLASS = "org.jvnet.hk2.config.ConfigInjector";
    private final static String NAME = "property";
    private final static String INJECTION_TARGET_QUALIFIER = "org.jvnet.hk2.config.InjectionTarget";
    private final static String REQUIRED = "required";
    private final static String OPTIONAL = "optional";
    private final static String STRING_DATATYPE = "datatype:java.lang.String";
    private final static String LEAF = "leaf";
    private final static String PROPERTY_CLASS = "org.jvnet.hk2.config.types.Property";

    private final static String NAME_FIELD = "@name";
    private final static String VALUE_FIELD = "@value";
    private final static String DESCRIPTION_FIELD = "@description";
    private final static String KEYED_AS = "keyed-as";
    private final static String TARGET = "target";
    private final static String KEY = "key";

    /**
     * This method enables the HK2 Dom based XML configuration parsing for
     * systems that do not use HK2 metadata files or use a non-default
     * name for HK2 metadata files, along with support for the types
     * provided in this module.  This method is idempotent, so that
     * if the services already are available in the locator they will
     * not get added again
     *
     * @param locator The non-null locator to add the hk2 dom based
     * configuration services to
     * @param loader The loader to use to classload the services added
     */
    public static void enableHK2DomConfigurationConfigTypes(ServiceLocator locator) {
        enableHK2DomConfigurationConfigTypes(locator, null);
    }


    /**
     * This method enables the HK2 Dom based XML configuration parsing for
     * systems that do not use HK2 metadata files or use a non-default
     * name for HK2 metadata files, along with support for the types
     * provided in this module.  This method is idempotent, so that
     * if the services already are available in the locator they will
     * not get added again
     *
     * @param locator The non-null locator to add the hk2 dom based
     * configuration services to
     * @param loader The loader to use to classload the services added
     */
    public static void enableHK2DomConfigurationConfigTypes(ServiceLocator locator, HK2Loader loader) {
        if (locator.getBestDescriptor(BuilderHelper.createContractFilter(PROPERTY_GENERATED_INJECTOR_CLASS)) != null) return;

        HK2DomConfigUtilities.enableHK2DomConfiguration(locator, loader);

        LinkedList<String> namedList = new LinkedList<String>();
        namedList.add(REQUIRED);
        namedList.add(STRING_DATATYPE);
        namedList.add(LEAF);

        LinkedList<String> valueList = new LinkedList<String>();
        valueList.add(REQUIRED);
        valueList.add(STRING_DATATYPE);
        valueList.add(LEAF);

        LinkedList<String> keyedAsList = new LinkedList<String>();
        keyedAsList.add(PROPERTY_CLASS);

        LinkedList<String> targetList = new LinkedList<String>();
        targetList.add(PROPERTY_CLASS);

        LinkedList<String> descriptionList = new LinkedList<String>();
        descriptionList.add(OPTIONAL);
        descriptionList.add(STRING_DATATYPE);
        descriptionList.add(LEAF);

        DescriptorImpl injectorDescriptor = BuilderHelper.link(PROPERTY_GENERATED_INJECTOR_CLASS).
                to(CONFIG_INJECTOR_CLASS).
                in(Singleton.class.getName()).
                named(NAME).
                qualifiedBy(INJECTION_TARGET_QUALIFIER).
                has(NAME_FIELD, namedList).
                has(VALUE_FIELD, valueList).
                has(KEYED_AS, keyedAsList).
                has(TARGET, targetList).
                has(DESCRIPTION_FIELD, descriptionList).
                has(KEY, NAME_FIELD).
                build();

        // A strangeness of using name from @Service
        injectorDescriptor.removeQualifier(Named.class.getName());

        if (loader != null) {
            injectorDescriptor.setLoader(loader);
        }

        ServiceLocatorUtilities.addOneDescriptor(locator, injectorDescriptor);
    }

}
