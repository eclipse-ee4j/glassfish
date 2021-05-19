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

package com.sun.enterprise.deployment;

import java.io.Serializable;

import org.glassfish.deployment.common.Descriptor;

/**
 * I am a pairing between a descriptor and a descriptor that has a JNDI name.
 *
 * @author Danny Coward
 */
public class NamedReferencePair implements Serializable {

    // Types of named reference pairs
    public static final int EJB = 1;
    public static final int EJB_REF = 2;
    public static final int RESOURCE_REF = 3;
    public static final int RESOURCE_ENV_REF = 4;

    private Descriptor referant;
    private NamedDescriptor referee;
    private int type;

    public static NamedReferencePair createEjbPair(EjbDescriptor referant, EjbDescriptor referee) {
        if (referant instanceof Descriptor) {
            // FIXME by srini - can we extract intf to avoid this
            return new NamedReferencePair((Descriptor) referant, referee, EJB);
        }
        return null;
    }


    public static NamedReferencePair createEjbRefPair(Descriptor referant, EjbReferenceDescriptor referee) {
        return new NamedReferencePair(referant, referee, EJB_REF);
    }


    public static NamedReferencePair createResourceRefPair(Descriptor referant, ResourceReferenceDescriptor referee) {
        return new NamedReferencePair(referant, referee, RESOURCE_REF);
    }


    public static NamedReferencePair createResourceEnvRefPair(Descriptor referant,
        ResourceEnvReferenceDescriptor referee) {
        return new NamedReferencePair(referant, referee, RESOURCE_ENV_REF);
    }


    /**
     * Construct a pairing between the given descriptor and the object
     * it has with a jndi name.
     */
    protected NamedReferencePair(Descriptor referant, NamedDescriptor referee, int type) {
        this.referant = referant;
        this.referee = referee;
        this.type = type;
    }


    /** Gets the descriptor with the named descriptor. */
    public Descriptor getReferant() {
        return this.referant;
    }


    /** Gets the named descriptor for the decriptor. */
    public NamedDescriptor getReferee() {
        return this.referee;
    }


    public String getPairTypeName() {
        switch (this.type) {
            case EJB:
                return "EJB";
            case EJB_REF:
                return "EJB REF";
            case RESOURCE_REF:
                return "RESOURCE REF";
            case RESOURCE_ENV_REF:
                return "RESOURCE ENV REF";
        }
        throw new IllegalStateException("unknown type = " + type);
    }


    public int getPairType() {
        return this.type;
    }


    /** My pretty format. */
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("NRP: ").append(referant.getName()).append(" -> ")
            .append(((Descriptor) referee).getName());
    }
}
