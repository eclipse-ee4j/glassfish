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

package com.sun.enterprise.config.serverbeans.customvalidators;

import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Domain;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.glassfish.api.admin.config.Named;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;

/**
 * Validation logic for NotDuplicateTargetName constraint
 *
 * @author Joe Di Pol
 */
public class NotDuplicateTargetNameValidator implements ConstraintValidator<NotDuplicateTargetName, Named> {

    Domain domain = null;

    @Override
    public void initialize(NotDuplicateTargetName constraintAnnotation) {
        ServiceLocator locator = ServiceLocatorFactory.getInstance().find("default");
        if (locator == null)
            return;

        ConfigBeansUtilities cbu = locator.getService(ConfigBeansUtilities.class);
        if (cbu == null)
            return;

        domain = cbu.getDomain();
    }

    @Override
    public boolean isValid(Named bean, ConstraintValidatorContext context) {

        // When we search for name clashes we typically do not check the
        // type of object being created since that check has already been
        // done by the config framework, and checking for duplicates is not cheap
        // We use these booleans for readability
        boolean checkCluster = true;
        boolean checkConfig = true;
        boolean checkNode = true;
        boolean checkServer = true;

        if (bean instanceof Server) {
            checkServer = false;
        } else if (bean instanceof Cluster) {
            checkCluster = false;
        } else if (bean instanceof Config) {
            checkConfig = false;
        } else if (bean instanceof Node) {
            checkNode = false;
        } else {
            // Unknown bean type. In this case we just go ahead and check
            // against Server, Cluster, Config, and Node
        }

        String name = bean.getName();

        if (domain == null) {
            return true;
        }

        if ((checkCluster && domain.getClusterNamed(name) != null) || (checkConfig && domain.getConfigNamed(name) != null)
                || (checkNode && domain.getNodeNamed(name) != null) || (checkServer && domain.getServerNamed(name) != null)) {
            return false;
        } else {
            return true;
        }
    }
}
