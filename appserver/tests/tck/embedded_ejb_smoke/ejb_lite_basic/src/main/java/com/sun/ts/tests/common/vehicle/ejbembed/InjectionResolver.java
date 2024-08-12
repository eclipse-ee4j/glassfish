/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.ts.tests.common.vehicle.ejbembed;

import com.sun.ts.tests.common.vehicle.ejbliteshare.EJBLiteClientIF;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBs;
import jakarta.ejb.embeddable.EJBContainer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Since JavaEE annotations are not required in ejb embeddable usage, this class
 * helps resolving @EJB and @PostConstruct in client classes when tests are
 * running in ejbembed vehicle.
 *
 * It resolves type-level, field and setter @EJB injections, and @PostConstruct
 * methods as well. This class constructs a portable jndi name from the metadata
 * in @EJB annotations.
 *
 * All client classes and their superclasses need to be scanned for @EJB
 * and @PostConstruct. The most general superclass should be processed first.
 * However, all @PostConstruct methods must be invoked after all @EJB injections
 * have been resolved and initialized.
 *
 * For type-level injections, name, beanName, and beanInterface are all
 * required, and so they are sufficient to construct the portable jndi name.
 * Then a mapping between JavaEE lookup name and portable global jndi name is
 * recorded, which can be consulted when test methods look up the ejb ref.
 *
 * For field and setter @EJB injection, all 3 @EJB attributes are optional.
 * beanInterface may be present or be inferred from the field or parameter type.
 * Obtaining beanName is complicated and requires searching all ejb bean
 * classes, parsing its component- defining annotations and parsing ejb-jar.xml.
 * This part is too much for our purpose.
 *
 * So we make it a rule for test writers that all client classes that are to be
 * run in ejbembed vehicle always use beanName attribute in @EJB injections.
 *
 * After a portable global jndi name is constructed, the field is initialized to
 * the lookup result, and the setter method is invoked, passing the lookup
 * result as the parameter.
 *
 * moduleName is set by various vehicles.
 */
public class InjectionResolver {
  private static final Logger logger = Logger
      .getLogger("com.sun.ts.tests.common.vehicle.ejbembed");

  private EJBContainer container;

  private EJBLiteClientIF client;

  private List<Method> postConstructMethods = new ArrayList<Method>();

  public InjectionResolver(EJBLiteClientIF client, EJBContainer container) {
    super();
    this.client = client;
    this.container = container;
  }

  public void resolve() {
    resolve0(client.getClass());
    invokePostConstructMethods();
  }

  public void resolve0(Class<? extends EJBLiteClientIF> cls) {
    Class<?> sup = cls.getSuperclass();
    if (sup != null && EJBLiteClientIF.class.isAssignableFrom(sup)) {
      resolve0((Class<? extends EJBLiteClientIF>) sup);
    }

    resolveTypeLevelInjections(cls);
    resolveFieldInjections(cls);
    resolveSetterInjections(cls);
    resolvePostConstruct(cls);

    logger.info("Resolved " + cls);
  }

  private void resolvePostConstruct(Class<? extends EJBLiteClientIF> cls) {
    Method[] methods = cls.getDeclaredMethods();
    for (Method m : methods) {
      PostConstruct pc = m.getAnnotation(PostConstruct.class);
      if (pc != null) {
        postConstructMethods.add(m);
      }
    }
  }

  private void invokePostConstructMethods() {
    for (Method m : postConstructMethods) {
      m.setAccessible(true);
      try {
        m.invoke(client);
        logger.info("Invoked PostConstruct method: " + m);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void resolveSetterInjections(Class<? extends EJBLiteClientIF> cls) {
    Method[] methods = cls.getDeclaredMethods();
    for (Method m : methods) {
      EJB b = m.getAnnotation(EJB.class);
      if (b == null) {
        continue;
      }

      logger.info("Resolving setter @EJB injection: " + b);

      String lookup = b.lookup(); // default value is ""
      if (lookup.length() > 0 && lookup.startsWith("java:global")) {
        logger.info("Got @EJB.lookup " + lookup);
      } else {
        Class<?> beanInterface = b.beanInterface();
        String beanName = b.beanName();
        if (beanInterface.equals(Object.class)) {
          Class<?>[] paramTypes = m.getParameterTypes();
          beanInterface = paramTypes[0];
        }
        if (beanName.length() == 0) {
          beanName = getBeanNameFromDescription(b.description());
        }
        if (beanName.length() == 0) {
          throw new RuntimeException(
              "beanName is not specified in @EJB injection on method "
                  + m.toString());
        }
        lookup = createGlobalJNDIName(beanInterface, beanName);
      }

      Object beanFromLookup = lookup(lookup);

      m.setAccessible(true);
      try {
        m.invoke(client, beanFromLookup);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void resolveFieldInjections(Class<? extends EJBLiteClientIF> cls) {
    Field[] fields = cls.getDeclaredFields();
    for (Field f : fields) {
      EJB b = f.getAnnotation(EJB.class);
      if (b == null) {
        continue;
      }

      logger.info("Resolving field @EJB injection: " + b);

      String lookup = b.lookup(); // default value is ""
      if (lookup.length() > 0 && lookup.startsWith("java:global")) {
        logger.info("Got @EJB.lookup " + lookup);
      } else {
        Class<?> beanInterface = b.beanInterface();
        String beanName = b.beanName();
        if (beanInterface.equals(Object.class)) {
          beanInterface = f.getType();
        }
        if (beanName.length() == 0) {
          beanName = getBeanNameFromDescription(b.description());
        }
        if (beanName.length() == 0) {
          throw new RuntimeException(
              "beanName is not specified in @EJB injection on field "
                  + f.toString());
        }
        lookup = createGlobalJNDIName(beanInterface, beanName);
      }

      Object beanFromLookup = lookup(lookup);

      f.setAccessible(true);
      try {
        f.set(client, beanFromLookup);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * EJB spec requires that lookup and beanName attributes cannot be both
   * present in the same @EJB. For those tests that specifically test lookup
   * attribute, beanName value has to be indirectly specified in description
   * attribute in the form: description="beanName=LookupBean" This method
   * extracts the beanName value from description attribute of @EJB
   */
  private String getBeanNameFromDescription(String description) {
    String[] tokens = description.split("=");
    String beanName = "";
    if (tokens.length == 2) {
      beanName = tokens[1];
      logger.info("Got beanName indirectly from description: " + beanName);
    }
    return beanName;
  }

  private void resolveTypeLevelInjections(
      Class<? extends EJBLiteClientIF> cls) {
    EJBs ejbs = cls.getAnnotation(EJBs.class);
    EJB ejb = cls.getAnnotation(EJB.class);

    if (ejbs != null) {
      for (EJB b : ejbs.value()) {
        resolveTypeLevelEJB(b);
      }
    }
    if (ejb != null) {
      resolveTypeLevelEJB(ejb);
    }
  }

  private void resolveTypeLevelEJB(EJB b) {
    // For type-level @EJB injections, all 3 attr are required
    logger.info("Resolving type-level @EJB injection: " + b);

    Class<?> beanInterface = b.beanInterface();
    String beanName = b.beanName();
    String name = b.name();
    String lookup = b.lookup(); // should be very rare to use lookup attr in
                                // type-level injections

    if (lookup.length() > 0 && lookup.startsWith("java:global")) {
      logger.info("Got @EJB.lookup " + lookup);
    } else {
      lookup = createGlobalJNDIName(beanInterface, beanName);
    }

    client.getJndiMapping().put(createJavaEELookupName(name), lookup);
  }

  private String createJavaEELookupName(String name) {
    return "java:comp/env/" + name;
  }

  // java:global[/<app-name>]/<module-name>/<bean-name>[!<fully-qualified-interface-name>]
  private String createGlobalJNDIName(Class<?> beanInterface, String beanName) {
    String result = EJBLiteClientIF.JAVA_GLOBAL_PREFIX;
    result += client.getModuleName() + "/" + beanName + "!"
        + beanInterface.getName();

    logger.info("Constructed portable global jndi name: " + result);
    return result;
  }

  private Object lookup(String lookupName) {
    Object result = null;
    javax.naming.Context context = container.getContext();
    try {
      result = context.lookup(lookupName);
    } catch (javax.naming.NamingException e) {
      throw new RuntimeException(e);
    }

    return result;
  }

}
