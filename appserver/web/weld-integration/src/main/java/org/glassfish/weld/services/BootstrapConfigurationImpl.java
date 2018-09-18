/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld.services;

import org.jboss.weld.bootstrap.spi.BootstrapConfiguration;

/**
 * @author <a href="mailto:j.j.snyder@oracle.com">JJ Snyder</a>
 */
public class BootstrapConfigurationImpl implements BootstrapConfiguration {
    @Override
    public boolean isConcurrentDeploymentEnabled() {
        // GlassFish was failing several tck tests when this is true.  Those tests include:
        // org.jboss.cdi.tck.tests.deployment.packaging.ear.MultiWebModuleWithExtensionTest
        // org.jboss.cdi.tck.tests.deployment.packaging.ear.SingleWebModuleWithExtensionTest

        return false;
    }

    @Override
    public int getPreloaderThreadPoolSize() {
        return 0;
    }

    @Override
    public void cleanup() {
    }


  @Override
  public boolean isNonPortableModeEnabled() {
    return false;
  }
}
