/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.persistence.ejb.entitybean.container;

import com.sun.ejb.Container;
import com.sun.ejb.ContainerFactory;
import com.sun.ejb.containers.BaseContainerFactory;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.security.SecurityManager;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.ejb.config.EjbContainer;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.runtime.IASEjbExtraDescriptors;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;

@Service(name = "EntityContainerFactory")
@PerLookup
public final class EntityContainerFactory extends BaseContainerFactory
        implements PostConstruct, ContainerFactory {

  @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config serverConfig;

    private EjbContainer ejbContainerDesc;

    private static final Logger _logger =
        LogDomains.getLogger(EntityContainerFactory.class, LogDomains.EJB_LOGGER);

    public void postConstruct() {
        ejbContainerDesc = serverConfig.getExtensionByType(EjbContainer.class);
    }

    public Container createContainer(EjbDescriptor ejbDescriptor,
                     ClassLoader loader,
                     DeploymentContext deployContext)
            throws Exception {
        EntityContainer container = null;
        SecurityManager sm = getSecurityManager(ejbDescriptor);

        // instantiate container class
      // EjbApplication got this ContainerFactory by ejbDescriptor type
      // hence we can always cast
      assert ejbDescriptor instanceof EjbEntityDescriptor;
      if (((EjbEntityDescriptor)ejbDescriptor).getIASEjbExtraDescriptors()
              .isIsReadOnlyBean()) {

        container = new ReadOnlyBeanContainer (ejbDescriptor, loader, sm);
      } else {
        String commitOption = null;
        IASEjbExtraDescriptors iased = ((EjbEntityDescriptor)ejbDescriptor).
                getIASEjbExtraDescriptors();
        if (iased != null) {
          commitOption = iased.getCommitOption();
        }
        if (commitOption == null) {
          commitOption = ejbContainerDesc.getCommitOption();
        }
        if (commitOption.equals("A")) {
          _logger.log(Level.WARNING,
                  "entitybean.container.commit_option_A_not_supported",
                  new Object []{ejbDescriptor.getName()}
          );
          container = new EntityContainer(ejbDescriptor, loader, sm);
        } else if (commitOption.equals("C")) {
          _logger.log(Level.FINE, "Using commit option C for: "
                  + ejbDescriptor.getName());
          container = new CommitCEntityContainer(ejbDescriptor, loader, sm);
        } else {
          _logger.log(Level.FINE,"Using commit option B for: " +
                  ejbDescriptor.getName());
          container = new EntityContainer(ejbDescriptor, loader, sm);
        }
      }
      container.initializeHome();
      return container;
    }
}
