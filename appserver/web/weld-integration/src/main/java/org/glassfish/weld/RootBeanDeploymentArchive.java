/*
 * Copyright (c) 2022, 2022 Contributors to Eclipse Foundation.
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld;

import com.sun.enterprise.deployment.EjbDescriptor;

import java.util.Collection;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.weld.connector.WeldUtils;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;

import static java.util.Collections.emptyList;

/**
 * A root BDA represents the root of a module where a module is a war, ejb, rar, ear lib A root BDA of each module
 * follows accessibility of the module (can only see BDAs, including root ones, in accessible modules). A root BDA
 * contains no bean classes. All bdas of the module are visible to the root bda. And the root bda is visible to all bdas
 * of the module.
 *
 * <p>
 * (Alternatively creating one root BDA per deployment has the disadvantage that you need to be careful about
 * accessibility rules. If you allow every BDA to see the root BDA - return it from BDA.getBeanDeploymentArchives() -
 * and allow the root BDA to see all other BDAs - return all other BDAs from root BDA.getDeployemtArchive(). Due to
 * transitivity you make any BDA accessible to any other BDA and break the accessibility rules. One way is to only allow
 * the root BDA to see all the other BDAs (but not vice versa). This may work for the InjectionTarget case but may be a
 * limitation elsewhere.)
 *
 * @author <a href="mailto:j.j.snyder@oracle.com">JJ Snyder</a>
 */
public class RootBeanDeploymentArchive extends BeanDeploymentArchiveImpl {

    private BeanDeploymentArchiveImpl moduleBda;

    /**
     * @param archive - this constructor uses and closes the archive
     */
    public RootBeanDeploymentArchive(ReadableArchive archive, Collection<EjbDescriptor> ejbs, DeploymentContext deploymentContext) {
        this(archive, ejbs, deploymentContext, null);
    }

    /**
     * @param archive - this constructor uses and closes the archive
     */
    public RootBeanDeploymentArchive(ReadableArchive archive, Collection<EjbDescriptor> ejbs, DeploymentContext deploymentContext, String moduleBdaID) {
        super("root_" + archive.getName(), emptyList(), emptyList(), emptyList(), deploymentContext);
        createModuleBda(archive, ejbs, deploymentContext, moduleBdaID);
    }

    private void createModuleBda(ReadableArchive archive, Collection<EjbDescriptor> ejbs, DeploymentContext deploymentContext, String bdaId) {
        moduleBda = new BeanDeploymentArchiveImpl(archive, ejbs, deploymentContext, bdaId);

        // Set the beanDeploymentArchive visibility for the root
        for (BeanDeploymentArchive beanDeploymentArchive : moduleBda.getBeanDeploymentArchives()) {
            beanDeploymentArchive.getBeanDeploymentArchives().add(this);
            getBeanDeploymentArchives().add(beanDeploymentArchive);
        }

        moduleBda.getBeanDeploymentArchives().add(this);
        getBeanDeploymentArchives().add(moduleBda);
    }

    @Override
    public Collection<String> getBeanClasses() {
        return emptyList();
    }

    @Override
    public Collection<Class<?>> getBeanClassObjects() {
        return emptyList();
    }

    @Override
    public Collection<String> getModuleBeanClasses() {
        return emptyList();
    }

    @Override
    public Collection<Class<?>> getModuleBeanClassObjects() {
        return emptyList();
    }

    @Override
    public BeansXml getBeansXml() {
        return null;
    }

    @Override
    public WeldUtils.BDAType getBDAType() {
        //todo: this should return a root type
        return WeldUtils.BDAType.UNKNOWN;
    }

    @Override
    public ClassLoader getModuleClassLoaderForBDA() {
        return moduleBda.getModuleClassLoaderForBDA();
    }

    public BeanDeploymentArchive getModuleBda() {
        return moduleBda;
    }

    public WeldUtils.BDAType getModuleBDAType() {
        return moduleBda.getBDAType();
    }
}
