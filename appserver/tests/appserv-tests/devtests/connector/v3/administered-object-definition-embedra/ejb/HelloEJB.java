/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.hello.session3;

import jakarta.ejb.Stateless;

import javax.naming.InitialContext;
import jakarta.resource.AdministeredObjectDefinitions;
import jakarta.resource.AdministeredObjectDefinition;

@AdministeredObjectDefinitions(
     value = {
          @AdministeredObjectDefinition(
                description="global-scope resource defined by @AdministeredObjectDefinition",
                name = "java:global/env/HelloEJB_ModByDD_AdminObject",
                interfaceName = "jakarta.jms.Destination",
                className = "connector.MyAdminObject",
                resourceAdapter="#aod-ra",
                properties = {"org.glassfish.admin-object.resType=connector.MyAdminObject"}
          ),
          @AdministeredObjectDefinition(
               description = "global-scope resource defined by @AdministeredObjectDefinition",
               name = "java:global/env/HelloEJB_Annotation_AdminObject",
               interfaceName = "jakarta.jms.Destination",
               className = "connector.MyAdminObject",
               resourceAdapter="#aod-ra",
               properties = {"org.glassfish.admin-object.resType=connector.MyAdminObject"}
          ),

          @AdministeredObjectDefinition(
               description = "application-scope resource defined by @AdministeredObjectDefinition",
               name = "java:app/env/HelloEJB_Annotation_AdminObject",
               interfaceName = "jakarta.jms.Destination",
               className = "connector.MyAdminObject",
               resourceAdapter="#aod-ra",
               properties = {"org.glassfish.admin-object.resType=connector.MyAdminObject"}
          ),

          @AdministeredObjectDefinition(
               description = "module-scope resource defined by @AdministeredObjectDefinition",
               name = "java:module/env/HelloEJB_Annotation_AdminObject",
               interfaceName = "jakarta.jms.Destination",
               className = "connector.MyAdminObject",
               resourceAdapter="#aod-ra",
               properties = {"org.glassfish.admin-object.resType=connector.MyAdminObject"}
          ),

          @AdministeredObjectDefinition(
               description = "component-scope resource defined by @AdministeredObjectDefinition",
               name = "java:comp/env/HelloEJB_Annotation_AdminObject",
               interfaceName = "jakarta.jms.Destination",
               className = "connector.MyAdminObject",
               resourceAdapter="#aod-ra",
               properties = {"org.glassfish.admin-object.resType=connector.MyAdminObject"}
          )

     }
)
@Stateless
public class HelloEJB implements Hello {

    public void hello() {

        // Connector-Resource-Definition through Annotation
        lookupAdminObject("java:global/env/Servlet_AdminObject", true);
        lookupAdminObject("java:app/env/Servlet_AdminObject", true);
        lookupAdminObject("java:module/env/Servlet_AdminObject", false);
        lookupAdminObject("java:comp/env/Servlet_AdminObject", false);

        lookupAdminObject("java:global/env/HelloStatefulEJB_Annotation_AdminObject", true);
        lookupAdminObject("java:app/env/HelloStatefulEJB_Annotation_AdminObject", true);
        lookupAdminObject("java:module/env/HelloStatefulEJB_Annotation_AdminObject", true);
        lookupAdminObject("java:comp/env/HelloStatefulEJB_Annotation_AdminObject", false);

        lookupAdminObject("java:global/env/HelloEJB_Annotation_AdminObject", true);
        lookupAdminObject("java:app/env/HelloEJB_Annotation_AdminObject", true);
        lookupAdminObject("java:module/env/HelloEJB_Annotation_AdminObject", true);
        lookupAdminObject("java:comp/env/HelloEJB_Annotation_AdminObject", true);

        // Connector-Resource-Definition through DD
        lookupAdminObject("java:global/env/EAR_AdminObject", true);
        lookupAdminObject("java:app/env/EAR_AdminObject", true);

        lookupAdminObject("java:global/env/Web_DD_AdminObject", true);
        lookupAdminObject("java:app/env/Web_DD_AdminObject", true);
        lookupAdminObject("java:module/env/Web_DD_AdminObject", false);
        lookupAdminObject("java:comp/env/Web_DD_AdminObject", false);

        lookupAdminObject("java:global/env/HelloStatefulEJB_DD_AdminObject", true);
        lookupAdminObject("java:app/env/HelloStatefulEJB_DD_AdminObject", true);
        lookupAdminObject("java:module/env/HelloStatefulEJB_DD_AdminObject", true);
        lookupAdminObject("java:comp/env/HelloStatefulEJB_DD_AdminObject", false);

        lookupAdminObject("java:global/env/HelloEJB_DD_AdminObject", true);
        lookupAdminObject("java:app/env/HelloEJB_DD_AdminObject", true);
        lookupAdminObject("java:module/env/HelloEJB_DD_AdminObject", true);
        lookupAdminObject("java:comp/env/HelloEJB_DD_AdminObject", true);

        System.out.println("In HelloEJB::hello()");
    }

    private void lookupAdminObject(String jndiName, boolean expectSuccess) throws RuntimeException{
        try {
            InitialContext ic = new InitialContext();
            Object ao = ic.lookup(jndiName);
            System.out.println("Stateless EJB: can access administered object : " + jndiName);
        } catch (Exception e) {
            if(expectSuccess){
                e.printStackTrace();
                throw new RuntimeException("Fail to access administered object: "+jndiName, e);
            }else{
                System.out.println("Stateless EJB cannot access administered object : " + jndiName);
            }
        }
    }


}
