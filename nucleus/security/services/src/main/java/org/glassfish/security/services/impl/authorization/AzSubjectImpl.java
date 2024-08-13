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

import javax.security.auth.Subject;

import org.glassfish.security.services.api.authorization.AzSubject;

public final class AzSubjectImpl extends AzAttributesImpl implements AzSubject {

    final private Subject subject;

    /**
     * Constructor
     *
     * @param subject The represented subject
     * @throws IllegalArgumentException Given subject was null
     */
    public AzSubjectImpl( Subject subject )  {
        super(NAME);

        if ( null == subject ) {
            throw new IllegalArgumentException("Illegal null Subject.");
        }
        this.subject = subject;
    }


    /**
     * Determines the <code>{@link javax.security.auth.Subject}</code>
     * represented by this AzSubject.
     * @return The represented Subject.
     */
    @Override
    public final Subject getSubject() {
        return subject;
    }


    @Override
    public String toString() {
        return subject.toString();
    }
}
