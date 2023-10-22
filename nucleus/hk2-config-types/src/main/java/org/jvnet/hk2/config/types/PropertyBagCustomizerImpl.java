/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.jvnet.hk2.annotations.Contract;

/**
 * @author jwells
 *
 */
@Singleton
@Contract
@Named(PropertyBagCustomizer.DEFAULT_IMPLEMENTATION)
public class PropertyBagCustomizerImpl implements PropertyBagCustomizer {

    /* (non-Javadoc)
     * @see org.jvnet.hk2.config.types.PropertyBagCustomizer#getProperty(org.jvnet.hk2.config.types.PropertyBag, java.lang.String)
     */
    @Override
    public Property getProperty(PropertyBag me, String name) {
        return me.lookupProperty(name);
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.config.types.PropertyBagCustomizer#getPropertyValue(org.jvnet.hk2.config.types.PropertyBag, java.lang.String)
     */
    @Override
    public String getPropertyValue(PropertyBag me, String name) {
        return getPropertyValue(me,name,null);
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.config.types.PropertyBagCustomizer#getPropertyValue(org.jvnet.hk2.config.types.PropertyBag, java.lang.String, java.lang.String)
     */
    @Override
    public String getPropertyValue(PropertyBag me, String name,
            String defaultValue) {
        Property prop = getProperty(me, name);
        if (prop != null) {
            return prop.getValue();
        }
        return defaultValue;
    }

}
