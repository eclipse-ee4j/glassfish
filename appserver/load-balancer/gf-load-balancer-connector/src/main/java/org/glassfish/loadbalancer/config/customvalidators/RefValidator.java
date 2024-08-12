/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.loadbalancer.config.customvalidators;

import com.sun.enterprise.config.serverbeans.ClusterRef;
import com.sun.enterprise.config.serverbeans.ServerRef;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.util.List;

import org.glassfish.loadbalancer.config.LbConfig;

/**
 * A simple constraint that validates lb-config children.
 * @author yamini
 */
public class RefValidator
    implements ConstraintValidator<RefConstraint, LbConfig>, Payload {

    @Override
    public void initialize(final RefConstraint constraint) {
    }

    @Override
    public boolean isValid(final LbConfig bean,
        final ConstraintValidatorContext constraintValidatorContext) {
        if (bean == null) return true;

        List<ServerRef> sRefs = bean.getRefs(ServerRef.class);
        List<ClusterRef> cRefs = bean.getRefs(ClusterRef.class);

        if (sRefs.size() > 0 && cRefs.size() > 0) {
            return false;
        }
        return true;
    }
}

