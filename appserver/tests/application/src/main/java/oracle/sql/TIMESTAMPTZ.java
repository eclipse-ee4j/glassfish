/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package oracle.sql;

/**
 * Stand-in for the class of the same name in the Oracle JDBC driver.
 * <p>
 * {@code Oracle9Platform} of the EclipseLink Oracle extension references this class directly in
 * its static initializer, so the class must be visible to the class loader which defined
 * {@code Oracle9Platform}. Only the name matters for that; no member of the real class is used
 * before a database connection is opened.
 * <p>
 * The Oracle JDBC driver cannot be redistributed with GlassFish, so tests verifying that
 * GlassFish makes driver classes visible to EclipseLink use this stub instead.
 */
public class TIMESTAMPTZ {
}
