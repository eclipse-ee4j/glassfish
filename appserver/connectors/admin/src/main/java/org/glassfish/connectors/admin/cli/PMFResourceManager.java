/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.resource.ResourceException;

import java.util.Properties;

import org.glassfish.api.I18n;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.admin.cli.ResourceConstants;
import org.glassfish.resources.admin.cli.ResourceManager;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;

/**
 * PMF Resource is removed from v3. <BR>
 * Keeping a ResourceManager so as to provide warning message when
 * older versions of sun-resources.xml is used.<BR>
 */
@Service(name= ServerTags.PERSISTENCE_MANAGER_FACTORY_RESOURCE)
@PerLookup
@I18n("create.pmf.resource")
public class PMFResourceManager implements ResourceManager {

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(PMFResourceManager.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceStatus create(Resources resources, ResourceAttributes attributes, Properties properties,
        String target) throws Exception {
        return new ResourceStatus(ResourceStatus.WARNING, getWarningMessage(attributes));
    }

    private String getWarningMessage(ResourceAttributes attributes) {
        //we do not support pmf-resource any more.
        String jndiName = attributes.getString(ResourceConstants.JNDI_NAME);
        String jdbcResourceJndiName= attributes.getString(ResourceConstants.JDBC_RESOURCE_JNDI_NAME);
        String defaultMsg = "persistence-manager-factory-resource is not supported any more. Instead, " +
                "use the jdbc-resource [ {0} ] referred by the persistence-manager-factory-resource [ {1} ] " +
                "in the application(s).";
        return I18N.getLocalString("create.pmf.resource.not.supported", defaultMsg, jdbcResourceJndiName, jndiName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource createConfigBean(Resources resources, ResourceAttributes attributes, Properties properties,
        boolean validate) throws Exception {
        throw new ResourceException(getWarningMessage(attributes));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResourceType() {
        return ServerTags.PERSISTENCE_MANAGER_FACTORY_RESOURCE;
    }
}
