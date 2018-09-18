/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.impl.authorization;

import org.glassfish.security.services.api.authorization.AzAttributes;
import org.glassfish.security.services.impl.common.AttributesImpl;

/**
 * <code>AzAttributesImpl</code> implements AzAttributes to provide an abstract
 * layer specifically for Authorization attributes.
 */
public abstract class AzAttributesImpl extends AttributesImpl implements AzAttributes {

    private final String name;


    /**
     * Copy constructor
     *
     * @param other The copy source
     */
    public AzAttributesImpl( AzAttributesImpl other ) {
        super( other );

        this.name = other.getName();
    }


    /**
     * Constructor
     * @param name The name of this collection.
     * @throws IllegalArgumentException Given name is null or empty
     */
    public AzAttributesImpl( String name ) {

        if ( null == name ) {
            throw new IllegalArgumentException( "Illegal null name given." );
        }

        name = name.trim();
        if ( name.isEmpty() ) {
            throw new IllegalArgumentException( "Illegal empty name given." );
        }

        this.name = name;
    }

    /**
     * Determines a name to denote this collection of attributes.
     * <p>
     * For example, this name may be used to qualify attributes by collection
     * type in XACML.
     *
     * @return The collection name
     * @see org.glassfish.security.services.api.authorization.AzAttributes#getName()
     */
    @Override
    public String getName() {
        return name;
    }
}
