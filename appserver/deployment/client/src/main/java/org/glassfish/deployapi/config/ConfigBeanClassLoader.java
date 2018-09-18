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
 * ConfigBeanClassLoader.java
 *
 * Created on August 27, 2002, 3:31 PM
 */

package org.glassfish.deployapi.config;

import javax.enterprise.deploy.model.DeployableObject;

/**
 *
 * @author  dochez
 */
public class ConfigBeanClassLoader extends ClassLoader {
    
    DeployableObject deployableObject;
    
    /** Creates a new instance of ConfigBeanClassLoader */
    public ConfigBeanClassLoader(DeployableObject deployableObject) {
        this.deployableObject = deployableObject;
    }
    
    public Class findClass(String name) {
        return deployableObject.getClassFromScope(name);
    }
    
}
