/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.persistence.jpa;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.InstrumentableClassLoader;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.persistence.common.DatabaseConstants;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.ClassTransformer;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Implementation of ProviderContainerContractInfo while running on server.
 * @author Mitesh Meswani
 */
public class ServerProviderContainerContractInfo extends ProviderContainerContractInfoBase {

       private final DeploymentContext deploymentContext;
       private final ClassLoader finalClassLoader;
       private ValidatorFactory validatorFactory;
       boolean isDas;

       public ServerProviderContainerContractInfo(DeploymentContext deploymentContext, ConnectorRuntime connectorRuntime, boolean isDas) {
           super(connectorRuntime, deploymentContext);
           this.deploymentContext = deploymentContext;
           // Cache finalClassLoader as deploymentContext.getFinalClassLoader() is expected to be called only once during deployment.
           this.finalClassLoader = deploymentContext.getFinalClassLoader();
           this.isDas = isDas;
       }

      @Override
      public ClassLoader getClassLoader() {
           return finalClassLoader;
       }

       @Override
       public ClassLoader getTempClassloader() {
           return ( (InstrumentableClassLoader)deploymentContext.getClassLoader() ).copy();
       }

       @Override
       public void addTransformer(final ClassTransformer transformer) {
           // Bridge between java.lang.instrument.ClassFileTransformer that DeploymentContext accepts
           // and jakarta.persistence.spi.ClassTransformer that JPA supplies.
           deploymentContext.addTransformer(new ClassFileTransformer() {
               public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                       ProtectionDomain protectionDomain, byte[] classfileBuffer)
                       throws IllegalClassFormatException {
                   return transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
               }
           });
       }

        @Override
        public String getApplicationLocation() {
           // Get source for current bundle. If it has not parent, it is the top level application
           // else continue traversing up till we find one with not parent.
           ReadableArchive archive = deploymentContext.getSource();
           boolean appRootFound = false;
           while (!appRootFound) {
               ReadableArchive parentArchive = archive.getParentArchive();
               if(parentArchive != null) {
                   archive = parentArchive;
               } else {
                   appRootFound = true;
               }
           }
           return archive.getURI().getPath();
       }

       @Override
       public ValidatorFactory getValidatorFactory() {
           // TODO Once discussion about BeanValidation in JavaEE is done, ValidatorFactory should be available from deployment context
           // We only create one validator factory per bundle.
           if (validatorFactory == null) {
               validatorFactory = Validation.buildDefaultValidatorFactory();
           }

           return validatorFactory;
       }

       @Override
       public boolean isJava2DBRequired() {
           OpsParams params = deploymentContext.getCommandParameters(OpsParams.class);
           // We only do java2db while being deployed on DAS. We do not do java2DB on load of an application or being deployed on an instance of a cluster
           return params.origin.isDeploy() && isDas;
       }

       @Override
       public DeploymentContext getDeploymentContext() {
           return deploymentContext;
       }

       @Override
       public void registerEMF(String unitName, String persistenceRootUri, RootDeploymentDescriptor containingBundle, EntityManagerFactory emf) {
           // We register the EMF into the bundle that declared the corresponding PU. This limits visibility of the emf
           // to containing module.
           // See EMFWrapper.lookupEntityManagerFactory() for corresponding look up logic
           if (containingBundle.isApplication()) {
               // ear level pu
               assert containingBundle instanceof Application;
               Application.class.cast(containingBundle).addEntityManagerFactory(
                       unitName, persistenceRootUri, emf);
           } else {
               assert containingBundle instanceof BundleDescriptor;
               BundleDescriptor.class.cast(containingBundle).addEntityManagerFactory(
                       unitName, emf);
           }
       }

       @Override
       public String getJTADataSourceOverride() {
           return deploymentContext.getTransientAppMetaData(DatabaseConstants.JTA_DATASOURCE_JNDI_NAME_OVERRIDE, String.class);
       }
}

