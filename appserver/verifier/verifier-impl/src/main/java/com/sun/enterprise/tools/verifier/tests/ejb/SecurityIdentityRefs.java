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

package com.sun.enterprise.tools.verifier.tests.ejb;

import com.sun.enterprise.deployment.RunAsIdentityDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.security.common.Role;

import java.util.Iterator;
import java.util.Set;

/** 
 * Security role references test.
 * The Bean provider must declare all of the enterprise's bean references 
 * to security roles as specified in section 15.2.1.3 of the Moscone spec.
 * Role names must be mapped to names within the jar.
 */
public class SecurityIdentityRefs extends EjbTest { 


  /** 
   * Security role references test.
   * The Bean provider must declare all of the enterprise's bean references
   * to security roles as specified in section 15.2.1.3 of the Moscone spec.
   * Role names must be mapped to names within the jar.
   *
   * @param descriptor the Enterprise Java Bean deployment descriptor
   *
   * @return <code>Result</code> the results for this assertion
   */
  public Result check(EjbDescriptor descriptor) {

    Result result = getInitializedResult();
    ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
if (descriptor.getUsesCallerIdentity()){
        result.addNaDetails(smh.getLocalString
            ("tests.componentNameConstructor",
             "For [ {0} ]",
             new Object[] {compName.toString()}));
        result.notApplicable(smh.getLocalString(
              "com.sun.enterprise.tools.verifier.tests.ejb.SecurityIdentityRefs.notApplicable3",
              "Bean [ {0} ] does not specify a run-as identity",
              new Object[] {descriptor.getName()}));
        return result;
    }
    RunAsIdentityDescriptor identity = descriptor.getRunAsIdentity();
    if (identity == null) {
      result.addNaDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.notApplicable(smh.getLocalString(
            "com.sun.enterprise.tools.verifier.tests.ejb.SecurityIdentityRefs.notApplicable2",
            "Bean [ {0} ] does not specify a security identity",
            new Object[] {descriptor.getName()}));                    
      return result;
    }

    EjbBundleDescriptorImpl bundleDescriptor = descriptor.getEjbBundleDescriptor();
    Set roles = bundleDescriptor.getRoles();
    Iterator roleIterator = roles.iterator();
    while (roleIterator.hasNext()) {
      Role role = (Role) roleIterator.next();
      if (role.getName().equals(identity.getRoleName())) {
        result.addGoodDetails(smh.getLocalString
            ("tests.componentNameConstructor",
             "For [ {0} ]",
             new Object[] {compName.toString()}));
        result.passed(smh.getLocalString(
              "com.sun.enterprise.tools.verifier.tests.ejb.SecurityIdentityRefs.passed",
              "Security identity run-as specified identity [ {0} ] role is found in the list of roles",
              new Object[] {role.getName()}));        
        return result;                
      }
    }
    result.addErrorDetails(smh.getLocalString
        ("tests.componentNameConstructor",
         "For [ {0} ]",
         new Object[] {compName.toString()}));
    result.failed(smh.getLocalString(
          "com.sun.enterprise.tools.verifier.tests.ejb.SecurityIdentityRefs.failed",
          "Security identity run-as specified identity [ {0} ] role is not valid",
          new Object[] {identity.getRoleName()}));        
    return result;                
  }

}
