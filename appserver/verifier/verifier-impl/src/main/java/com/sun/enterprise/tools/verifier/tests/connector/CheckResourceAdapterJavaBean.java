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
 * CheckResourceAdapterJavaBean.java
 *
 * Created on August 29, 2002
 */

package com.sun.enterprise.tools.verifier.tests.connector;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.tools.verifier.tests.*;
import java.util.*;
import java.beans.*;

/**
 * Test that "resourceadapter-class" is a Java Bean.
 *
 * @author Anisha Malhotra 
 * @version 
 */
public class CheckResourceAdapterJavaBean
extends ConnectorTest 
implements ConnectorCheck 
{

  /** <p>
   * Test that "resourceadapter-class" is a Java Bean.
   * </p>
   *
   * @param descriptor deployment descriptor for the rar file
   * @return result object containing the result of the individual test
   * performed
   */
  public Result check(ConnectorDescriptor descriptor) {

    Result result = getInitializedResult();
    ComponentNameConstructor compName = 
      getVerifierContext().getComponentNameConstructor();

    String impl = descriptor.getResourceAdapterClass();
    if(impl.equals(""))
    {
      if(descriptor.getInBoundDefined())
      {
        // resourceadapter-class cannot be null
        result.addErrorDetails(smh.getLocalString
            ("tests.componentNameConstructor",
             "For [ {0} ]",
             new Object[] {compName.toString()}));
        result.failed(smh.getLocalString
            (getClass().getName() + ".failed1",
             "resourceadapter-class cannot be empty if the resource" + 
             " adapter provides inbound communication"));
      }
      else
      {
          result.addNaDetails(smh.getLocalString
              ("tests.componentNameConstructor",
               "For [ {0} ]",
               new Object[] {compName.toString()}));
          result.notApplicable(smh.getLocalString
              ("com.sun.enterprise.tools.verifier.tests.connector.resourceadapter.notApp",
               "resourceadapter-class is not specified."));
      }
      return result;
    }
    VerifierTestContext context = getVerifierContext();
    Class implClass = null;
    try
    {
      implClass = Class.forName(impl, false, getVerifierContext().getClassLoader());
    }
    catch(ClassNotFoundException e)
    {
      result.addErrorDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.failed(smh.getLocalString
          ("com.sun.enterprise.tools.verifier.tests.connector.CheckResourceAdapter.nonexist",
           "Error: The class [ {0} ] as defined under resourceadapter-class in the deployment descriptor does not exist",
           new Object[] {impl}));
      return result;
    }
    Set configProps = descriptor.getConfigProperties();
    boolean oneFailed = false;
    Iterator propIter = configProps.iterator();
    BeanInfo bi = null;
    try
    {
      bi = Introspector.getBeanInfo(implClass, Object.class);
    } 
    catch (IntrospectionException ie) {
      oneFailed = true;
      result.addErrorDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));
      result.failed(smh.getLocalString
          (getClass().getName() + ".failed",
           "Error: The resourceadapter-class [ {0} ] is not JavaBeans compliant",
           new Object[] {impl} ));
      return result;
    }

    PropertyDescriptor[] properties = bi.getPropertyDescriptors();
    Hashtable<String, PropertyDescriptor> props = new Hashtable<String, PropertyDescriptor>();
    for(int i=0;i<properties.length;i++)
    {
      props.put(properties[i].getName(), properties[i]);
    }
    while(propIter.hasNext()) 
    {
      EnvironmentProperty envProp = (EnvironmentProperty) propIter.next();
      String name = Introspector.decapitalize(envProp.getName());
      String type = envProp.getType();
      PropertyDescriptor propDesc = props.get(name);
      if(propDesc != null)
      {
        if (propDesc.getReadMethod()==null || propDesc.getWriteMethod()==null)
        {
          oneFailed = true;
          result.addErrorDetails(smh.getLocalString
              ("tests.componentNameConstructor",
               "For [ {0} ]",
               new Object[] {compName.toString()}));
          result.failed(smh.getLocalString
              (getClass().getName() + ".failed1",
               "Error: The resourceadapter-class [ {0} ] does not provide accessor methods for [ {1} ].",
               new Object[] {impl, name} ));
          return result;
        }
      }
      else
      {
        oneFailed = true;
        result.addErrorDetails(smh.getLocalString
            ("tests.componentNameConstructor",
             "For [ {0} ]",
             new Object[] {compName.toString()}));
        result.failed(smh.getLocalString
            (getClass().getName() + ".failed1",
             "Error: The resourceadapter-class [ {0} ] does not provide accessor methods for [ {1} ].",
             new Object[] {impl, name} ));
        return result;
      }
    }
    if(!oneFailed)
    {
      result.addGoodDetails(smh.getLocalString
          ("tests.componentNameConstructor",
           "For [ {0} ]",
           new Object[] {compName.toString()}));	
      result.passed(smh.getLocalString(getClass().getName() + ".passed",
            "Success: resourceadapter-class is a Java Bean"));                     
    }
    return result;
  }
}
