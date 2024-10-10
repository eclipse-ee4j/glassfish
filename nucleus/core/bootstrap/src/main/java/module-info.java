/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation
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

/**
 * @author David Matejcek
 */
module org.glassfish.main.bootstrap {

    requires java.base;
    requires java.logging;

    // felix framework is supplied by other jpms modules which don't overlap osgi.core
    requires static org.apache.felix.framework;
    // logging annotations are used just by maven compiler plugin and they are not used in runtime.
    requires static org.glassfish.annotation.processing.logging;
    requires org.glassfish.main.jdke;

    exports com.sun.enterprise.glassfish.bootstrap.cfg;
    exports com.sun.enterprise.glassfish.bootstrap.launch;
}
