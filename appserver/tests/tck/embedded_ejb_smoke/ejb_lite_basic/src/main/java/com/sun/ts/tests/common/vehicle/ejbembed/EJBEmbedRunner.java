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

import com.sun.javatest.Status;
import com.sun.ts.lib.util.TestUtil;
import com.sun.ts.tests.common.vehicle.VehicleRunnable;
import com.sun.ts.tests.common.vehicle.ejbliteshare.EJBLiteClientIF;
import com.sun.ts.tests.common.vehicle.ejbliteshare.ReasonableStatus;

import jakarta.ejb.embeddable.EJBContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import static com.sun.ts.tests.common.vehicle.ejbliteshare.EJBLiteClientIF.TEST_PASSED;

public class EJBEmbedRunner implements VehicleRunnable {

  private static final Logger logger = Logger
      .getLogger("com.sun.ts.tests.common.vehicle.ejbembed");

  @Override
public Status run(String[] args, Properties props) {
    int statusCode = Status.NOT_RUN;
    String passOrFail = null;
    String reason = null;
    EJBLiteClientIF client = null;

    try {
      client = createClient(args, props);
      passOrFail = client.getStatus();
      reason = client.getReason();

      if (passOrFail.startsWith(TEST_PASSED)) {
        statusCode = Status.PASSED;
      } else {
        statusCode = Status.FAILED;
      }

    } catch (Exception e) {
      statusCode = Status.FAILED;
      reason = TestUtil.printStackTraceToString(e);
    } finally {
      if (client != null) {
        client.getContainer().close();
      }
    }
    return new ReasonableStatus(statusCode, reason);
  }

  private File[] parseAdditionalModules(String[] args) {
    if (args == null) {
      return null;
    }
    List<String> mods = new ArrayList<String>();
    for (int i = 0; i < args.length; i++) {
      if (EJBLiteClientIF.ADDITIONAL_MODULES_KEY.equals(args[i])) {
        mods.add(args[++i]);
      }
    }
    File[] files = new File[mods.size()];
    for (int i = 0; i < mods.size(); i++) {
      files[i] = new File(mods.get(i));
    }
    return files;
  }

  private EJBLiteClientIF createClient(String[] args, Properties props)
      throws Exception {
    Class<?> c = Class.forName(props.getProperty("test_classname"));
    EJBLiteClientIF client = (EJBLiteClientIF) c.newInstance();

    client.setAdditionalModules(parseAdditionalModules(args));
    client.setTestName(props.getProperty("testName"));
    client.setInjectionSupported(false);
    client.setModuleName(EJBLiteClientIF.EJBEMBED_JAR_NAME_BASE);
    client.setContextClassLoader();

    Map<String, Object> containerInitProps = client
        .getContainerInitProperties();
    EJBContainer container = (containerInitProps == null
        ? EJBContainer.createEJBContainer()
        : EJBContainer.createEJBContainer(containerInitProps));
    javax.naming.Context context = container.getContext();

    client.setContainer(container);
    client.setContext(context);

    InjectionResolver injectionResolver = new InjectionResolver(client,
        container);
    injectionResolver.resolve();

    return client;
  }
}
