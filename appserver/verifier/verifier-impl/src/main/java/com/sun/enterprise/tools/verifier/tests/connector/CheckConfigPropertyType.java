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
 * CheckConfigProperyType.java
 *
 * Created on October 2, 2000, 3:25 PM
 */

package com.sun.enterprise.tools.verifier.tests.connector;

import java.util.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.tools.verifier.tests.*;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.tools.verifier.Result;
/**
 * Properties names defined in the resource adapter config-propery should
 * be of an acceptable type
 *
 * @author  Jerome Dochez
 * @version
 */
public class CheckConfigPropertyType extends ConnectorTest implements ConnectorCheck {

    /**
     * Property allowed type
     */
    private static Class[] allowedTypes = {
					java.lang.String.class,
					java.lang.Boolean.class,
					java.lang.Integer.class,
					java.lang.Double.class,
					java.lang.Byte.class,
					java.lang.Short.class,
					java.lang.Long.class,
					java.lang.Float.class,
					java.lang.Character.class,
					    };

    /** <p>
     * Properties names defined in the resource adapter config-propery should
     * be of an acceptable type
     * </p>
     *
     * @paramm descriptor deployment descriptor for the rar file
     * @return result object containing the result of the individual test
     * performed
     */
    public Result check(ConnectorDescriptor descriptor) {

        boolean oneFailed = false;

        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        //Set properties = descriptor.getConfigProperties();
        ConnectionDefDescriptor desc = descriptor.getConnectionDefinitionByCFType(null, true);
        Set properties = desc.getConfigProperties();
        if (properties.size()!=0) {
            Iterator iterator = properties.iterator();
            // let's add the propery name
            // HashSet hs = new HashSet();
            while (iterator.hasNext()) {
                EnvironmentProperty ep = (EnvironmentProperty) iterator.next();
                String type = ep.getType();
                if (type == null) {
                    result.addErrorDetails(smh.getLocalString
                            ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                    result.failed(smh.getLocalString(getClass().getName() + ".notdefined",
                            "Error: The configuration property named [ {0} ] has no type ",
                            new Object[] {ep.getName()}));
                    return result;
                }
                Class typeClass = null;
                // is it loadable ?
                try {
                    typeClass = Class.forName(type);
                } catch (Throwable t) {
                    result.addErrorDetails(smh.getLocalString
                            ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                    result.failed(smh.getLocalString(getClass().getName() + ".nonexist",
                            "Error: The type [ {0} ] of the configuration property named [ {1} ] cannot be loaded",
                            new Object[] {ep.getType(), ep.getName()}));
                    return result;
                }
                boolean allowedType = false;
                for (int i = 0; i < allowedTypes.length; i++) {
                    if (allowedTypes[i].equals(typeClass)) {
                        allowedType = true;
                        break;
                    }
                }
                if (!allowedType) {
                    oneFailed = true;
                    result.addErrorDetails(smh.getLocalString
                            ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                    result.failed(smh.getLocalString(getClass().getName() + ".failed",
                            "Error: The type [ {0} ] for the configuration property named [ {1} ] is not allowed",
                            new Object[] {ep.getType(), ep.getName()}));
                    return result;
                }
            }
            // for failure, result has been set before
            if (!oneFailed) {
                result.addGoodDetails(smh.getLocalString
                        ("tests.componentNameConstructor",
                                "For [ {0} ]",
                                new Object[] {compName.toString()}));
                result.passed(smh.getLocalString(getClass().getName() + ".passed",
                        "Success: all properties have an allowed type"));

            }
        } else {
            result.addNaDetails(smh.getLocalString
                    ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName.toString()}));
            result.notApplicable(smh.getLocalString(getClass().getName() + ".notApplicable",
                    "Not Applicable: There are no config-property element defined" ));

        }
        return result;
    }
}
