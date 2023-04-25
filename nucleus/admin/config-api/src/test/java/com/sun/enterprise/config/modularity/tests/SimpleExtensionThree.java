/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.modularity.tests;

import com.sun.enterprise.config.modularity.annotation.CustomConfiguration;
import com.sun.enterprise.config.modularity.customization.ConfigBeanDefaultValue;

import java.util.ArrayList;
import java.util.List;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * @author Masoud Kalali
 */
@Configured
@CustomConfiguration(baseConfigurationFileName = "simple-ext-type-one.xml", usesOnTheFlyConfigGeneration = true)
public interface SimpleExtensionThree extends SimpleConfigExtensionExtensionPoint, PropertyBag {

    static List<ConfigBeanDefaultValue> getDefaultValues(String runtimeType) {
        // decide what to do depending on the runtime...
        ConfigBeanDefaultValue defaultValue =
                new ConfigBeanDefaultValue(
                        "domain",
                        "some.class.name",
                        "<xml-doc></xml-doc>",
                        false,
                        null);
        List<ConfigBeanDefaultValue> values = new ArrayList<>();
        values.add(defaultValue);
        return values;
    }
}
