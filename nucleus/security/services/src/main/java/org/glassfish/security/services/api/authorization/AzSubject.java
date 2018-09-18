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

package org.glassfish.security.services.api.authorization;


import javax.security.auth.Subject;

/**
 * Extends the AzAttributes interface to provide a type-safe interface for Subject attributes.
 */
public interface AzSubject extends AzAttributes {

    /**
     * The name of this collection. For example, this name may be used to
     * qualify attributes by collection type in XACML.
     */
    String NAME = "SUBJECT";


    /**
     * Determines the <code>{@link javax.security.auth.Subject}</code>
     * represented by this AzSubject.
     * @return The represented Subject
     */
    Subject getSubject() ;
}
