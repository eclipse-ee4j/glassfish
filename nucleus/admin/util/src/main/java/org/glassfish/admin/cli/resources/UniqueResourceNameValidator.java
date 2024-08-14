/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.cli.resources;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;

/**
 * Created with IntelliJ IDEA. User: naman Date: 2/8/12 Time: 4:11 PM To change this template use File | Settings | File
 * Templates.
 */
public class UniqueResourceNameValidator implements ConstraintValidator<UniqueResourceNameConstraint, Resource> {

    Domain domain = null;

    public void initialize(final UniqueResourceNameConstraint constraint) {

        /*ServiceLocator locator = ServiceLocatorFactory.getInstance().find("default");
        if (locator == null) return;

        ConfigBeansUtilities cbu = locator.getService(ConfigBeansUtilities.class);
        if (cbu == null) return;

        domain = cbu.getDomain();*/

        ServiceLocator habitat = Globals.getDefaultHabitat();
        if (habitat == null)
            return; // due to: http://java.net/jira/browse/GLASSFISH-19043

        domain = habitat.getService(Domain.class);

    }

    @Override
    public boolean isValid(final Resource resource, final ConstraintValidatorContext constraintValidatorContext) {
        if (domain != null) {
            if (resource.getParent().getParent() instanceof Domain) {
                for (Resource res : domain.getResources().getResources()) {
                    if (resource.getIdentity().equals(res.getIdentity())) {
                        Class[] resourceInterfaces = resource.getClass().getInterfaces();
                        Class[] resInterfaces = res.getClass().getInterfaces();

                        for (Class resourceClass : resourceInterfaces) {
                            for (Class resClass : resInterfaces) {
                                if (resClass.getName().equals(resourceClass.getName())) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
