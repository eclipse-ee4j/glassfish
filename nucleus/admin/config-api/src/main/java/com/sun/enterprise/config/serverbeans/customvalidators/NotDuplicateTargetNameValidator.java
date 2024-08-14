/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Server;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.glassfish.api.admin.config.Named;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.Dom;

/**
 * Validation logic for NotDuplicateTargetName constraint
 *
 * @author Joe Di Pol
 */
public class NotDuplicateTargetNameValidator implements ConstraintValidator<NotDuplicateTargetName, Named> {

    @Override
    public void initialize(NotDuplicateTargetName constraintAnnotation) {
    }

    @Override
    public boolean isValid(Named bean, ConstraintValidatorContext context) {
        if (bean == null) {
            return true;
        }

        Dom beanDom = Dom.unwrap(bean);
        if (beanDom == null) {
            return true;
        }

        ServiceLocator locator = beanDom.getHabitat();
        if (locator == null) {
            return true;
        }

        Domain domain = locator.getService(Domain.class);
        if (domain == null) {
            return true;
        }

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

        if ((checkCluster && domain.getClusterNamed(name) != null) || (checkConfig && domain.getConfigNamed(name) != null)
                || (checkNode && domain.getNodeNamed(name) != null) || (checkServer && domain.getServerNamed(name) != null)) {
            return false;
        } else {
            return true;
        }
    }
}
