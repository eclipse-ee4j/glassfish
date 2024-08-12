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

package com.sun.ts.tests.common.vehicle.ejbliteshare;

import jakarta.ejb.embeddable.EJBContainer;

import java.io.File;
import java.util.Map;

public interface EJBLiteClientIF {
    public static final String TEST_PASSED = "[TEST PASSED] ";

    public static final String TEST_FAILED = "[TEST FAILED] ";

    public static final String JAVA_GLOBAL_PREFIX = "java:global/";

    public static final String JAVA_COMP_ENV_PREFIX = "java:comp/env/";

    public static final String ADDITIONAL_MODULES_KEY = "-additionalModule";

    public static final String EJBEMBED_JAR_NAME_BASE = "ejbembed_vehicle_ejb";

    public void setInjectionSupported(Boolean injectionSupported);

    public Boolean getInjectionSupported();

    public void runTestInVehicle();

    public String getTestName();

    public void setTestName(String testName);

    public String getStatus();

    public String getReason();

    public String getModuleName();

    public void setModuleName(String mn);

    public Map<String, String> getJndiMapping();

    public EJBContainer getContainer();

    public void setContainer(EJBContainer container);

    public javax.naming.Context getContext();

    public void setAdditionalModules(File[] additionalModules);

    public void setContext(javax.naming.Context context);

    /**
     * Subclass client can override this method to customize the container creation. The default implementation returns null in
     * EJBLiteClientBase. Since the method must be invoked prior to container creation, way ahead of actual test method, this
     * customization is only possible at test client level, not at test method level.
     */
    public Map<String, Object> getContainerInitProperties();

    /**
     * This method is called by test client to set context ClassLoader to include additional classes and ejb modules. This method is
     * called prior to creating EJBContainer. The default implementation does nothing and makes no change to the context ClassLoader
     * in the current thread. Subclass client may choose to override this method to provide for additional ejb modules and classes.
     */
    public void setContextClassLoader();
}
