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

package com.sun.enterprise.web.deploy;

import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.SecurityRole;
import com.sun.enterprise.web.WebModule;

import java.util.Enumeration;
/**
 * Decorator of class <code>org.apache.catalina.deploy.SecurityConstraint</code>
 *
 * @author Jean-Francois Arcand
 */
public class SecurityConstraintDecorator 
                    extends org.apache.catalina.deploy.SecurityConstraint {

    private SecurityConstraint securityConstraint;
    
    public SecurityConstraintDecorator(SecurityConstraint securityConstraint,
                                       WebModule webModule){
        this.securityConstraint = securityConstraint;
        
        if (securityConstraint.getAuthorizationConstraint() != null){
            setAuthConstraint(true);
            Enumeration enumeration = securityConstraint
                            .getAuthorizationConstraint().getSecurityRoles();

            SecurityRole securityRole;
            while (enumeration.hasMoreElements()){
                securityRole = (SecurityRole)enumeration.nextElement();
                super.addAuthRole(securityRole.getName());
                if ( !securityRole.getName().equals("*")){
                    webModule.addSecurityRole(securityRole.getName());
                }
            }
            setDisplayName(securityConstraint.getAuthorizationConstraint().getName());
        }
 
        if (securityConstraint.getUserDataConstraint() != null){
            setUserConstraint(securityConstraint.getUserDataConstraint()
                                                    .getTransportGuarantee());
        }
        
    }

}



