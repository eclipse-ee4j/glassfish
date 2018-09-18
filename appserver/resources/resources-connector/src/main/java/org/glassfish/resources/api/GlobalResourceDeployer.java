/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * ResourceDeployer.java
 *
 * Created on December 12, 2003, 12:35 PM
 */

package org.glassfish.resources.api;

import org.jvnet.hk2.config.types.Property;

/**
 * @author Rob Ruyak
 */
public abstract class GlobalResourceDeployer {

    /**
     * Return an the element property names as an array of strings.
     *
     * @param props An array of ElementProperty objects.
     * @return The names within the element as an array of strings.
     */
    String[] getPropNamesAsStrArr(Property[] props) {
        if (props == null) {
            return null;
        } else {
            String[] result = new String[props.length];
            for (int i = 0; i < props.length; i++) {
                result[i] = props[i].getName();
            }
            return result;
        }
    }

    /**
     * Return an the element property values as an array of strings.
     *
     * @param props An array of ElementProperty objects.
     * @return The values within the element as an array of strings.
     */
    String[] getPropValuesAsStrArr(Property[] props) {
        if (props == null) {
            return null;
        } else {
            String[] result = new String[props.length];
            for (int i = 0; i < props.length; i++) {
                result[i] = props[i].getValue();
            }
            return result;
        }
    }
}
