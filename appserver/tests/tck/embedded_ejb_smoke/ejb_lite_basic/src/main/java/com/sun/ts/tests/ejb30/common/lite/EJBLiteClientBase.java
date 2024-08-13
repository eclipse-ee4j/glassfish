/*
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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
 * $Id$
 */
package com.sun.ts.tests.ejb30.common.lite;

import com.sun.ts.lib.harness.ServiceEETest;
import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.common.vehicle.ejbliteshare.EJBLiteClientIF;
import com.sun.ts.tests.ejb30.common.helper.Helper;

import jakarta.ejb.EJBException;
import jakarta.ejb.embeddable.EJBContainer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;

public class EJBLiteClientBase extends ServiceEETest
    implements EJBLiteClientIF {

  /////////////////////////////////////////////////////////////////////
  // properties that may communicate with runtime environment (vehicles) and
  // may be configured as managed-property
  /////////////////////////////////////////////////////////////////////
  /**
   * status and reason indicate test status and may be configured as
   * managed-property. They must be updated by subclass test clients, typically
   * indirectly via invoking pass or fail method.
   */
  private String status;

  private String reason;

  /**
   * the current testName (without _from_<vehicle> suffix). When running in
   * vehicles, this field is initialized either by container (ejblitejsf
   * vehicle) or by runner class. When running as standalone client, need to
   * initialize it in setup method
   */
  private String testName;

  /**
   * If annotation injections on this class are supported in the runtime
   * environment. For example, in a servlet vehicle, this class will be
   * reflectively instantiated by the test app, and so any annotations will be
   * ignored.
   */
  private Boolean injectionSupported;

  /**
   * used in portable global jndi name.
   */
  private String moduleName;

  private EJBContainer container;

  private javax.naming.Context context;

  /**
   * Maps the lookup name of EJB injections to their portable global jndi names.
   */
  private Map<String, String> jndiMapping = new HashMap<String, String>();

  /**
   * Additional embeddable ejb modules that are outside current classpath. Its
   * getter method (protected) is intended for subclass to retrieve it. Its
   * setter method is declared in EJBLiteClientIF interface.
   */
  private File[] additionalModules;

  /////////////////////////////////////////////////////////////////////
  // internal fields
  /////////////////////////////////////////////////////////////////////
  /** a StringBuilder for reason property */
  private StringBuilder reasonBuffer;

  /**
   * number of times this class has been used to run test. When used as vehicle,
   * instances of this class (actually its concrete subclass) are either not
   * shared across multiple tests (e.g. ejblitejsf, ejblitejsp vehicles), or
   * internal state must be reset (e.g., ejbliteservlet vehicle). This field
   * must equal either 0 or 1.
   */
  private byte accessCount;

  /**
   * indicates this class is now inside a vehicle, e.g., jsf managed bean,
   * servlet, servlet filter, etc. This field is not initialized util
   * this.runTestInVehicle() is called.
   */
  private boolean inVehicle;

  /////////////////////////////////////////////////////////////////////
  // property accessors
  /////////////////////////////////////////////////////////////////////

  @Override
public String getTestName() {
    return testName;
  }

  @Override
public void setTestName(String testName) {
    this.testName = testName;
  }

  /**
   * Typically invoked by containers or vehicles to run the actual test method.
   * This method must not be invoked more than once for each test. Subclass test
   * client must not invoke this method.
   */
  @Override
public String getStatus() {
    if (accessCount == 0) {
      runTestInVehicle();
    } else {
      passOrFail(false,
          "This class has been incorrectly shared across multiple tests: ",
          this.toString(), ", timesCalled=", String.valueOf(accessCount));
    }
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Similar to getStatus() method, and subclass test client should not invoke
   * it. However, this method is used by both standalone and vehicle clients.
   */
  @Override
public String getReason() {
    if (reason == null) {
      if (reasonBuffer == null) {
        reason = "WARNING: both reason and reasonBuffer is null.";
      } else {
        reason = reasonBuffer.toString();
      }
    }
    return this.reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  @Override
public String getModuleName() {
    return moduleName;
  }

  @Override
public void setModuleName(String mn) {
    if (mn.startsWith("/")) {
      this.moduleName = mn.substring(1);
    } else {
      this.moduleName = mn;
    }
  }

  @Override
public EJBContainer getContainer() {
    return container;
  }

  @Override
public void setContainer(EJBContainer container) {
    this.container = container;
  }

  @Override
public javax.naming.Context getContext() {
    if (context != null) {
      return context;
    }
    javax.naming.Context ctx = null;
    try {
      ctx = new javax.naming.InitialContext();
    } catch (NamingException e) {
      throw new RuntimeException(e);
    }
    return ctx;
  }

  @Override
public void setContext(javax.naming.Context context) {
    this.context = context;
  }

  /////////////////////////////////////////////////////////////////////
  // harness-required methods
  /////////////////////////////////////////////////////////////////////
  /**
   * setup method is invoked by harness prior to each test method. If
   * getInjectionSupported() returns false, need to initialize those injected
   * fields here. When running inside vehicles, setup(null, null) are called
   * (see runTestInVehicle method)
   */
  public void setup(String[] args, Properties p) {
    if (!inVehicle && (testName == null) && p != null) {
      String s = p.getProperty("testName"); // strip off _from_<vehicle>
      testName = s.substring(0, s.indexOf("_from_"));
      String m = p.getProperty("vehicle_archive_name");
      setModuleName(m);
      Helper.getLogger()
          .fine("testName from test properties: " + s
              + ", setting testName field to the short name: " + testName
              + ". set moduleName to vehicle_archive_name property: " + m);
    }
  }

  /**
   * cleanup method is invoked by harness after each test method. For vehicles,
   * this methos is invoked only by servlet filter.
   */
  public void cleanup() {
    if (!inVehicle) {
      Helper.getLogger()
          .info("The following events occurred before test passed or failed: "
              + getReason());
    }
    nuke();
  }

  /////////////////////////////////////////////////////////////////////
  // EJBLiteClientIF methods
  /////////////////////////////////////////////////////////////////////
  @Override
public void runTestInVehicle() {
    inVehicle = true;
    try {
      setup(null, null);
      Method testMethod = getClass().getMethod(testName);
      testMethod.invoke(this, (Object[]) null);
      passOrFail(true);
    } catch (NoSuchMethodException e) {
      passOrFail(false, "Failed to get test method " + testName, e.toString());
    } catch (IllegalAccessException e) {
      passOrFail(false,
          "Failed to access test method (is it public?) " + testName,
          e.toString());
    } catch (InvocationTargetException e) {
      // If it's EJBException with a cause, just include the cause in the
      // message
      String msg = "Failed with exception ";
      Throwable root = e.getCause();
      if (root == null) {
        root = e;
      } else if (root instanceof EJBException) {
        Throwable cause2 = root.getCause();
        if (cause2 != null) {
          root = cause2;
          msg = "Failed with EJBException caused by ";
        }
      }
      passOrFail(false, msg, TestUtil.printStackTraceToString(root));
    } catch (Exception e) {
      passOrFail(false, "Unexpected exception for test " + testName,
          TestUtil.printStackTraceToString(e));
    } // finally { skip cleanup
  }

  /**
   * When running inside a vehicle, the vehicle runner class should call
   * setInjectionSupported(true|false) to explicitly initialize it. Its default
   * value is null. For ejblitejsf vehicle, it is set in faces-config.xml as a
   * managed-property. For standalone client, injectionSupported field is not
   * initialized. But if ejb spec requires injection support, we need to
   * initialize it from a property in ts.jte.
   */
  @Override
public Boolean getInjectionSupported() {
    return injectionSupported;
  }

  @Override
public void setInjectionSupported(Boolean injectionSupported) {
    this.injectionSupported = injectionSupported;
  }

  @Override
public Map<String, String> getJndiMapping() {
    return jndiMapping;
  }

  @Override
public void setContextClassLoader() {
  }

  @Override
public Map<String, Object> getContainerInitProperties() {
    return null;
  }

  @Override
public void setAdditionalModules(File[] additionalModules) {
    this.additionalModules = additionalModules;
  }

  /////////////////////////////////////////////////////////////////////
  // convenience methods
  /////////////////////////////////////////////////////////////////////

  /**
   * In embeddable usage, only the portable global jndi name lookup is
   * supported. In JavaEE or web environment, the following lookupName format
   * can be used:
   *
   * x will be expanded to java:comp/env/x java:comp/env/x
   * java:global/app-name/module-name/bean-name!FQN
   * java:app/module-name/bean-name!FQN java:module/bean-name!FQN
   *
   * beanName and beanInterface is to be used in embed mode to create a
   * java:global name
   */
  protected Object lookup(String lookupName, String beanName,
      Class<?> beanInterface) {
    String nameNormalized = lookupName;

    if (container == null) {
      // in JavaEE or Web environment
      if (!lookupName.startsWith("java:")) {
        nameNormalized = JAVA_COMP_ENV_PREFIX + lookupName;
      }
    } else {
      // embaddable usage, only look up with portable global jndi name
      if (!lookupName.startsWith(JAVA_GLOBAL_PREFIX)) {
        if (beanName == null) {
          // type-level @EJB injections are stored in jndiMapping
          String s = getJndiMapping().get(lookupName);
          if (s == null) {
            s = getJndiMapping().get(JAVA_COMP_ENV_PREFIX + lookupName);
          }

          if (s == null) {
            throw new RuntimeException(String.format(
                "Lookup name (%s) does not start with %s, beanName is %s, and not in jndiMapping %s",
                lookupName, JAVA_GLOBAL_PREFIX, beanName, getJndiMapping()));
          }
          nameNormalized = s;
          Helper.getLogger()
              .info("Retrieved the global jndi name from jndiMapping: "
                  + lookupName + " : " + nameNormalized);
        } else {
          nameNormalized = JAVA_GLOBAL_PREFIX + EJBEMBED_JAR_NAME_BASE + "/"
              + beanName;
          if (beanInterface != null) {
            nameNormalized = nameNormalized + "!" + beanInterface.getName();
          }
        }
      }
    }
    try {
      return getContext().lookup(nameNormalized);
    } catch (javax.naming.NamingException e) {
      throw new RuntimeException(e);
    }
  }

  protected File[] getAdditionalModules() {
    return additionalModules;
  }

  protected StringBuilder getReasonBuffer() {
    if (reasonBuffer == null) {
      reasonBuffer = new StringBuilder(); // single-threaded access
    }
    return reasonBuffer;
  }

  protected void appendReason(Object... oo) {
    for (Object o : oo) {
      getReasonBuffer().append(o).append(System.getProperty("line.separator"));
    }
  }

  protected void assertEquals(final String messagePrefix, final Object expected,
      final Object actual) throws RuntimeException {
    Helper.assertEquals(messagePrefix, expected, actual, getReasonBuffer());
  }

  protected void assertNotEquals(final String messagePrefix,
      final Object expected, final Object actual) throws RuntimeException {
    Helper.assertNotEquals(messagePrefix, expected, actual, getReasonBuffer());
  }

  protected void assertGreaterThan(final String messagePrefix, long arg1,
      long arg2) throws RuntimeException {
    Helper.assertGreaterThan(messagePrefix, arg1, arg2, getReasonBuffer());
  }

  /**
   * A central place to clear up state. Note that we cannot call it in cleanup()
   * since vehicle tests (at least jsf) test status is written to jsp page after
   * cleanup method. We cannot call it in setup either, since in vehicle tests
   * containers inject to these fields upon instantiate and we don't want to
   * remove these field values. In vehicle tests this test is instantiated once
   * for each test, and so it should not matter if we leave the state behind.
   * When running in standalone, this test client should also be instantiated
   * once per test.
   */
  private void nuke() {
    accessCount = 0;
    injectionSupported = null;
    status = null;
    reason = null;
    testName = null;
    inVehicle = false;
    reasonBuffer = null;

    moduleName = null;
    container = null;
    context = null;
    jndiMapping = null;
    additionalModules = null;
  }

  private void passOrFail(boolean passOrFail, String... why) {
    this.status = (passOrFail ? TEST_PASSED : TEST_FAILED) + "testName="
        + testName;
    appendReason((Object[]) why);
    if (reasonBuffer != null) {
      this.reason = reasonBuffer.toString();
    }
    this.accessCount++;
  }

}
