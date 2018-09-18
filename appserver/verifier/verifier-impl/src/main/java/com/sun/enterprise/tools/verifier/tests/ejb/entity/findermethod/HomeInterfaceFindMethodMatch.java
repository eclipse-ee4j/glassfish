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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.findermethod;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.lang.reflect.Method;
import java.util.Arrays;

/** 
 * find<METHOD>(...) methods test.  
 *
 *   Home interface contains all find<METHOD>(...) methods declared in the home 
 *   interface.  
 *
 *   Each finder method must be named ``find<METHOD>'' (e.g. findLargeAccounts),
 *   and it must match one of the ejbFind<METHOD> methods defined in the 
 *   enterprise Bean class (e.g. ejbFindLargeAccounts). The matching 
 *   ejbFind<METHOD> method must have the same number and types of arguments. 
 *   (Note that the return type may be different.) 
 *
 */
public class HomeInterfaceFindMethodMatch extends EjbTest implements EjbCheck { 

    Result result = null;
    ComponentNameConstructor compName = null;

    /** 
     * find<METHOD>(...) methods test.  
     *
     *   Home interface contains all find<METHOD>(...) methods declared in the home 
     *   interface.  
     *
     *   Each finder method must be named ``find<METHOD>'' (e.g. findLargeAccounts),
     *   and it must match one of the ejbFind<METHOD> methods defined in the 
     *   enterprise Bean class (e.g. ejbFindLargeAccounts). The matching 
     *   ejbFind<METHOD> method must have the same number and types of arguments. 
     *   (Note that the return type may be different.) 
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();
        if (descriptor instanceof EjbEntityDescriptor &&
                ((EjbEntityDescriptor)descriptor).getPersistenceType().equals(EjbEntityDescriptor.BEAN_PERSISTENCE)) {
            if(descriptor.getHomeClassName() != null && !"".equals(descriptor.getHomeClassName()))
                commonToBothInterfaces(descriptor.getHomeClassName(), descriptor.getRemoteClassName(), descriptor);

            if(descriptor.getLocalHomeClassName() != null && !"".equals(descriptor.getLocalHomeClassName()))
                commonToBothInterfaces(descriptor.getLocalHomeClassName(), descriptor.getLocalClassName(), descriptor);

        } else {
            addNaDetails(result, compName);
            result.notApplicable("This test is only applicable entity beans with bean managed persistence.");
        }

        return result;
    }

    /**
     * This method is responsible for the logic of the test. It is called for both local and remote interfaces.
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @param home for the Home interface of the Ejb. 
     */

    private void commonToBothInterfaces(String home, String remote, EjbDescriptor descriptor) {
        Class [] methodParameterTypes;
        Class [] ejbFinderMethodParameterTypes;
        int ejbFinderMethodLoopCounter = 0;
        try {
            // retrieve the home interface methods
            VerifierTestContext context = getVerifierContext();
            ClassLoader jcl = context.getClassLoader();
            Class homeInterfaceClass = Class.forName(home, false, getVerifierContext().getClassLoader());
            Class remoteInterfaceClass = Class.forName(remote, false, getVerifierContext().getClassLoader());
            Class EJBClass = Class.forName(descriptor.getEjbClassName(), false, getVerifierContext().getClassLoader());
            Method [] homeInterfaceMethods = homeInterfaceClass.getMethods();
            Method [] ejbFinderMethods = EJBClass.getMethods();
            int z;
            // Note: this test will be done in the testing on the
            // Home Interface class. i.e.
            // also need to check that matching signatures and exceptions exist
            // between home interface and EJB Class,
            // i.e.
            // Each finder method must be named ``find<METHOD>''
            //  (e.g. findLargeAccounts), and it must match one of the
            // ejbFind<METHOD> methods defined in the enterprise Bean
            // class (e.g. ejbFindLargeAccounts). The matching ejbFind<METHOD>
            // method must have the same number and types of arguments.
            // (Note that the return type may be different.)

            for (int i=0; i< homeInterfaceMethods.length; i++) {
                if (homeInterfaceMethods[i].getName().startsWith("find")) {
                    // clear these from last time thru loop
                    // find matching "ejbFind<METHOD>(...)" in bean class
                    for (z=0; z< ejbFinderMethods.length; z++) {
                        if (ejbFinderMethods[z].getName().startsWith("ejbFind")) {
                            // check rest of string to see if findAccount matches
                            // ejbFindAccount
                            if (homeInterfaceMethods[i].getName().toUpperCase().equals
                                    (ejbFinderMethods[z].getName().toUpperCase().substring(3))) {
                                // found one, see if it matches same number and types
                                // of arguments,
                                methodParameterTypes = homeInterfaceMethods[i].getParameterTypes();
                                ejbFinderMethodParameterTypes = ejbFinderMethods[z].getParameterTypes();

                                boolean returnTypeMatch = checkReturnType(homeInterfaceMethods[i], ejbFinderMethods[z],
                                        remoteInterfaceClass, descriptor);

                                if (!returnTypeMatch) {
                                    addErrorDetails(result, compName);
                                    result.failed(smh.getLocalString
                                            (getClass().getName() + ".failReturnType",
                                                    "For Home Interface [ {0} ] Method [ {1} ] return type [ {2} ] ",
                                                    new Object[] {homeInterfaceClass.getName(), homeInterfaceMethods[i].getName(), homeInterfaceMethods[i].getReturnType().getName()}));
                                    result.addErrorDetails(smh.getLocalString
                                            (getClass().getName() + ".failReturnType1",
                                                    "Error: does not match with return type [ {0} ] of corresponding ejbFind<METHOD>(...).",
                                                    new Object[] {ejbFinderMethods[z].getReturnType().getName()}));

                                }
                                if (!Arrays.equals(methodParameterTypes,ejbFinderMethodParameterTypes)) {

                                    addErrorDetails(result, compName);
                                    result.failed(smh.getLocalString
                                            (getClass().getName() + ".debug1",
                                                    "For Home Interface [ {0} ] Method [ {1} ]",
                                                    new Object[] {homeInterfaceClass.getName(),homeInterfaceMethods[i].getName()}));
                                    result.addErrorDetails(smh.getLocalString
                                            (getClass().getName() + ".failed",
                                                    "Error: A corresponding [ {0} ] method was found, but the parameters did not match.",
                                                    new Object[] {"ejb"+homeInterfaceMethods[i].getName().toUpperCase().substring(0,1)+homeInterfaceMethods[i].getName().substring(1)}));
                                }

                                // used to display output below
                                ejbFinderMethodLoopCounter = z;
                                break;
                            }// if check to see if findAccount matches ejbFindAccount
                        } // if check to see if startsWith("ejbFind")
                    }// for all the business methods within the bean class, loop

                    if (z==ejbFinderMethods.length) {
                        // set status to FAILED, 'cause there is not even an
                        // find method to begin with, regardless of its parameters
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString
                                (getClass().getName() + ".debug1",
                                        "For Home Interface [ {0} ] Method [ {1} ]",
                                        new Object[] {homeInterfaceClass.getName(),homeInterfaceMethods[i].getName()}));
                        result.addErrorDetails(smh.getLocalString
                                (getClass().getName() + ".failed1",
                                        "Error: No corresponding ejbFind<METHOD>(...)  method was found." ));
                    }

                    if(result.getStatus()!=Result.FAILED){
                        addGoodDetails(result, compName);
                        //result.passed()
                        result.passed(smh.getLocalString
                                (getClass().getName() + ".debug1",
                                        "For Home Interface [ {0} ] Method [ {1} ]",
                                        new Object[] {homeInterfaceClass.getName(),homeInterfaceMethods[i].getName()}));
                        result.addGoodDetails(smh.getLocalString
                                (getClass().getName() + ".passed",
                                        "The corresponding [ {0} ] method with matching parameters was found.",
                                        new Object[] {ejbFinderMethods[ejbFinderMethodLoopCounter].getName()}));

                    }

                } // if the home interface found a "find" method
            }// for all the methods within the home interface class, loop

        } catch (ClassNotFoundException e) {
            Verifier.debug(e);
            result.addErrorDetails(smh.getLocalString
                    ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName.toString()}));
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failedException",
                            "Error: Home interface [ {0} ] or EJB Class [ {1} ] does not exist or is not loadable.",
                            new Object[] {descriptor.getHomeClassName(),descriptor.getEjbClassName()}));
//            return false;
        }
    }

    private boolean checkReturnType(Method homeFinderMethod, Method beanFinderMethod, Class remote, EjbDescriptor descriptor){
        Class homeMethodtype = homeFinderMethod.getReturnType();
        Class beanMethodType = beanFinderMethod.getReturnType();

        if (homeMethodtype.getName().equals(remote.getName())) {
            return beanMethodType.getName().equals(((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName());
        } else return homeMethodtype.getName().equals(beanMethodType.getName());
    }
}
