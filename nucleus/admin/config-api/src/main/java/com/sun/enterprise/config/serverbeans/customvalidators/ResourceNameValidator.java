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


import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class ResourceNameValidator
    implements ConstraintValidator<ResourceNameConstraint, Resource> {

    public void initialize(final ResourceNameConstraint constraint) {
    }

    @Override
    public boolean isValid(final Resource resource,
        final ConstraintValidatorContext constraintValidatorContext) {
        if(resource.getParent().getParent() instanceof Domain){
            if(resource instanceof BindableResource){
                if(((BindableResource)resource).getJndiName().contains(":")){
                    return false;
                }
            }else if(resource instanceof ResourcePool){
                if(((ResourcePool)resource).getName().contains(":")){
                    return false;
                }
            }
        }
        return true;
    }
}

