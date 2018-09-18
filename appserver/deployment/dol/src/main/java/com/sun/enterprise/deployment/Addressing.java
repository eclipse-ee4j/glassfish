/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * This class describes addressing element from webservices.xml .
 *
 * @author Bhakti Mehta
 *
 */
public class Addressing extends Descriptor {

    private boolean enabled;

    private boolean required;

    private String responses ;

    /**
     * copy constructor.
     */
    public Addressing(Addressing other) {
        super(other);
        enabled = other.enabled;
        required = other.required;
        responses = other.responses;
    }

    public Addressing() {
    }

    public Addressing(boolean enabled, boolean req, String resp) {
        this.enabled = enabled;
        this.required = req;
        this.responses = resp;
    }

    public String getResponses() {
        return responses;
    }

    public void setResponses(String responses) {
        this.responses = responses;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


     /**
     * @return a string describing the values I hold
     */
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("\nAddressing enabled = ").append(enabled).append(
            " required = ").append(required).append(" responses = ").append(responses);

    }
}
