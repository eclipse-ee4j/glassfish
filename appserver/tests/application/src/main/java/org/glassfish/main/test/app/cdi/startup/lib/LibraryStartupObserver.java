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

package org.glassfish.main.test.app.cdi.startup.lib;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Observes {@link Startup} from a library packaged in {@code WEB-INF/lib} and looks up
 * a data source the web application defines with {@code @DataSourceDefinition}.
 */
@ApplicationScoped
public class LibraryStartupObserver {

    public static final String DATA_SOURCE_NAME = "java:app/jdbc/startupObserverDS";

    private volatile String status = "Startup event not observed";

    void onStartup(@Observes Startup event) {
        status = lookupDataSource();
    }

    public String getStatus() {
        return status;
    }

    public static String lookupDataSource() {
        try {
            Object dataSource = new InitialContext().lookup(DATA_SOURCE_NAME);
            return dataSource instanceof DataSource ? "OK" : "Not a DataSource: " + dataSource;
        } catch (NamingException e) {
            return e.toString();
        }
    }
}
