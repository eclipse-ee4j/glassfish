/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.security.integration.bankadmin.daomanager;

import java.util.Properties;
import javax.naming.*;
import jakarta.ejb.SessionContext;
import jakarta.ejb.CreateException;
import javax.naming.NamingException;
import jakarta.ejb.EJBObject;
import jakarta.ejb.EJBHome;
import javax.rmi.PortableRemoteObject;

import java.util.logging.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Title: BeanTestClient implementing RASClient interface
 * Description: JavaEE test client for admincontroller session bean
 * @author Deepa Singh
 * @version 1.0
 */

public class BeanTestClient
{
  private Properties beanprops=null;
  private javax.naming.Context jndi=null;
  private CustomerRemoteHome ejbHome=null;
  private CustomerRemote ejbObject;
  private static Logger logger = Logger.getLogger("bank.admin");
  private static ConsoleHandler ch = new ConsoleHandler();
  private static SimpleReporterAdapter stat=new SimpleReporterAdapter("appserv-tests");


  //bean specific variables
  String customerID="singhd";

  public BeanTestClient(Properties p)
  {
    this.beanprops=p;
  }

  public void setupJNDIContext(Properties p)
  {
      try
      {
          jndi=new javax.naming.InitialContext();
      }catch(Exception e)
      {
          e.printStackTrace();
      }
  }

  public EJBHome getHomeInterface()
  {
    try
    {
      Object obj=(CustomerRemoteHome)jndi.lookup("java:comp/env/ejb/CustomerBean");
      ejbHome=(CustomerRemoteHome)PortableRemoteObject.narrow(obj,com.sun.s1peqe.security.integration.bankadmin.daomanager.CustomerRemoteHome.class);
      System.out.println("Home interface of Customer Bean"+ejbHome.getClass().getName());
    }catch(Throwable e)
    {
        e.printStackTrace();
    }
    return (EJBHome)ejbHome;
  }


  public EJBObject getRemoteInterface(EJBHome ejbHome) {
      System.out.println("inside getting remote interface");
      try{
          ejbObject=((CustomerRemoteHome)ejbHome).createCustomer(customerID,customerID);
      System.out.println("Remote interface of Customer Bean"+ejbObject.getClass().getName());

      }catch(jakarta.ejb.DuplicateKeyException e) {
          System.out.println("Exception:Customer already exists");
      }
      catch (Throwable e) {
          e.printStackTrace();
      }
      return (EJBObject)ejbObject;
  }

  public void runTestClient()
  {
      try
      {
          Object obj=(CustomerRemoteHome)jndi.lookup("java:comp/env/ejb/CustomerBean");
          ejbHome=(CustomerRemoteHome)PortableRemoteObject.narrow(obj,com.sun.s1peqe.security.integration.bankadmin.daomanager.CustomerRemoteHome.class);
          System.out.println("Home interface of Customer Bean"+ejbHome.getClass().getName());
      }catch(Throwable e)
      {
          e.printStackTrace();
      }
      System.out.println("inside getting remote interface");
      try{
          try{ejbObject=((CustomerRemoteHome)ejbHome).findByPrimaryKey(customerID);
          }catch(jakarta.ejb.ObjectNotFoundException e){System.out.println("customer does not exist");}
          if(ejbObject==null)
          {
              System.out.println("Creating customer..."+customerID);
              ejbObject=((CustomerRemoteHome)ejbHome).createCustomer(customerID,customerID);
          }
          System.out.println("Remote interface of Customer Bean"+ejbObject.getClass().getName());
          boolean ret=ejbObject.TestCallerInRole();
          if(ret==true)
              stat.addStatus("JACC:cmp_cmp isCallerInRole",stat.PASS);
          else if(ret==false)
              stat.addStatus("JACC:cmp_cmp isCallerInRole",stat.FAIL);
          else
          {
              stat.addStatus("JACC:cmp_cmp isCallerInRole",stat.FAIL);
              System.out.println("Test did not get run");
          }

      }catch (Throwable e) {
          e.printStackTrace();
      }

      try {
          System.out.println("created customer from client"+customerID);
          boolean ret=ejbObject.TestCallerInRole();
          if(ret==true)
              stat.addStatus("JACC:cmp_cmp isCallerInRole",stat.PASS);
          else if(ret==false)
              stat.addStatus("JACC:cmp_cmp isCallerInRole",stat.FAIL);
          else
          {
            stat.addStatus("JACC:cmp_cmp isCallerInRole",stat.FAIL);
            System.out.println("Test did not get run");
          }

      }catch(Throwable e) {
          stat.addStatus("JACC:cmp_cmp isCallerInRole",stat.FAIL);
          e.printStackTrace();
      }
  }

  public void cleanupTests(){
      stat.printSummary("cmp_cmpID");
  }

  public void run() {
      try {
          logger.info("inside run method");
          stat.addDescription("This test suite exercises isCallerInRole API as unit test");
          setupJNDIContext(beanprops);
          runTestClient();
          cleanupTests();
      }
      catch(Throwable e) {
          e.printStackTrace();
      }
  }

  public static void main(String args[]) {
      Properties p=null;// we are testing locally only, no properties to test
      BeanTestClient testClient=new BeanTestClient(p);
      testClient.run();

  }

}
