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

package org.glassfish.orb.http.client;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.naming.ConfigurationException;
import javax.naming.Context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpInitialContextFactoryTest {

    // Held, so that the logger the factory looks up is this one.
    private static final Logger LOGGER = Logger.getLogger(HttpInitialContextFactory.class.getName());

    private static List<String> warningsWhile(Hashtable<String, Object> environment) throws Exception {
        List<String> warnings = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                    warnings.add(record.getMessage());
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        LOGGER.addHandler(handler);
        try {
            ((HttpNamingContext) new HttpInitialContextFactory().getInitialContext(environment)).close();
        } finally {
            LOGGER.removeHandler(handler);
        }
        return warnings;
    }

    @Test
    @DisplayName("a bearer token over plain HTTP is warned about, as a password is")
    void aBearerTokenOverPlainHttpIsWarnedAbout() throws Exception {
        Hashtable<String, Object> environment = new Hashtable<>();
        environment.put(Context.PROVIDER_URL, "http://localhost:8080/glassfish-services");
        environment.put(HttpInitialContextFactory.PROP_BEARER_TOKEN, "secret-token");

        List<String> warnings = warningsWhile(environment);

        assertEquals(1, warnings.size(), warnings.toString());
        assertTrue(warnings.get(0).contains("bearer token"), warnings.get(0));
    }

    @Test
    @DisplayName("a bearer token over https is not")
    void aBearerTokenOverHttpsIsNotWarnedAbout() throws Exception {
        Hashtable<String, Object> environment = new Hashtable<>();
        environment.put(Context.PROVIDER_URL, "https://localhost:8181/glassfish-services");
        environment.put(HttpInitialContextFactory.PROP_BEARER_TOKEN, "secret-token");

        assertEquals(List.of(), warningsWhile(environment));
    }

    @Test
    @DisplayName("a provider URL whose host java.net.URI cannot read is refused, not turned into 'null'")
    void aHostWithAnUnderscoreIsRefused() {
        Hashtable<String, Object> environment = new Hashtable<>();
        environment.put(Context.PROVIDER_URL, "https://my_host:8181/glassfish-services");

        assertThrows(ConfigurationException.class,
                () -> new HttpInitialContextFactory().getInitialContext(environment));
    }
}
