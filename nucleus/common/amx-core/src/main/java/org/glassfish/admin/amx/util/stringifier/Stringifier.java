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

package org.glassfish.admin.amx.util.stringifier;

/**
 * Convert an object to a String. The intent of this is to provide a flexible means
 * to control the string representation of an Object. The toString() routine has many
 * issues, including:
 * - appropriateness for end-user viewing (within a CLI for example)
 * - an object may not have implemented a toString() method
 * - the output of toString() may simply be unacceptable (eg class@eebc1933)
 * - it may be desirable to have many variations on the output
 * - modifying toString() requires modifying the orignal class; a Stringifier
 * or many of them can exist independently, making it easy to apply many different
 * types of formatting to the same class.
 * The intended use is generally to have a separate class implement Stringifier, rather
 * than the class to be stringified.
 */
public interface Stringifier {

    /**
     * Produce a String representation of an object. The actual output has no
     * other semantics; each Stringifier may choose to target a particular type
     * of user.
     * <p>
     * The resulting String should be suitable for display to a user.
     *
     * @param object the Object for which a String should be produced
     */
    String stringify(Object object);
}
