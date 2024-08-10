/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.deployment.descriptor;

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.OrderedSet;

import java.util.Set;

import org.glassfish.deployment.common.Descriptor;

/**
 * This class holds a set of activation config properties.
 *
 * @author Kenneth Saks
 */

public final class ActivationConfigDescriptor extends Descriptor {

    // Set of EnvironmentProperty entries
    private Set<EnvironmentProperty> activationConfig;

    public ActivationConfigDescriptor()
    {
        activationConfig = new OrderedSet<EnvironmentProperty>();
    }

    public ActivationConfigDescriptor(ActivationConfigDescriptor other) {
        activationConfig = new OrderedSet<EnvironmentProperty>(other.activationConfig);
     }

    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("Activation Config : ").append(activationConfig);
    }

    public Set<EnvironmentProperty> getActivationConfig() {
        return activationConfig;
    }
}

