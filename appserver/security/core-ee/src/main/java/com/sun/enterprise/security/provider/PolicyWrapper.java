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
 * PolicyWrapper.java
 *
 * @author Harpreet Singh (harpreet.singh@sun.com)
 * @author Ron Monzillo
 * @version
5B
 * Created on May 23, 2002, 1:56 PM
 */

package com.sun.enterprise.security.provider;

/**
 * This class is a wrapper around the default jdk policy file 
 * implementation. PolicyWrapper is installed as the JRE policy object
 * It multiplexes policy decisions to the context specific instance of
 * com.sun.enterprise.security.provider.PolicyFile.
 * Although this Policy provider is implemented using another Policy class,
 * this class is not a "delegating Policy provider" as defined by JACC, and
 * as such it SHOULD not be configured using the JACC system property
 * jakarta.security.jacc.policy.provider.
 * @author Harpreet Singh (harpreet.singh@sun.com)  
 * @author Jean-Francois Arcand
 * @author Ron Monzillo
 *
 */
public class PolicyWrapper extends BasePolicyWrapper {
    
    // override to change the implementation of PolicyFile
    /** gets the underlying PolicyFile implementation
     * can be overridden by Subclass
     */
    @Override
    protected java.security.Policy getNewPolicy() {
	return (java.security.Policy) new sun.security.provider.PolicyFile();
    }
}

