/*
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

package org.glassfish.api;

/**
 * Interface for defining dynamic command parameter defaults. This is used with the 
 * defaultCalculator argument to the @Param annotation to implement a calculator 
 * for a dynamic default value.
 * 
 * @author tmueller
 */
public class ParamDefaultCalculator {
    /*
     * This method is called if the user has not
     * provided a value for it via the client. This object is given a chance to 
     * determine the default value for parameter. This method may return null 
     * if no default value can be computed. 
     */
    public String defaultValue(ExecutionContext context) {
        return null;
    };
    
}
