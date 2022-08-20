/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment;

import org.glassfish.deployment.common.Descriptor;

/**
 * This class defines a descriptor which can be described
 * with a description
 *
 * @author  Jerome Dochez
 */
public class DescribableDescriptor extends Descriptor {

    private static final long serialVersionUID = 1L;
    private String description;

    /** Creates new DescribableDescriptor */
    public DescribableDescriptor() {
    }


    @Override
    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public String getDescription() {
        return description;
    }
}
