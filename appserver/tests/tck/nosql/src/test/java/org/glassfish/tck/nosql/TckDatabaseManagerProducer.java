/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package org.glassfish.tck.nosql;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.semistructured.DatabaseConfiguration;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DatabaseManagerFactory;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;

/**
 * Producer for DatabaseManager used in Jakarta NoSQL TCK tests.
 */
@ApplicationScoped
public class TckDatabaseManagerProducer {

    @Produces
    @Database(DatabaseType.DOCUMENT)
    public DatabaseManager getManager() {
        DatabaseConfiguration configuration = DatabaseConfiguration.getConfiguration();

        Properties properties = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("META-INF/microprofile-config.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            // ignore
        }

        String host = properties.getProperty("jnosql.mongodb.host");
        if (host == null || host.isBlank()) {
            host = System.getProperty("jnosql.mongodb.host", "localhost:27017");
        }

        Settings settings = Settings.builder()
                .put("jnosql.mongodb.host", host)
                .build();

        DatabaseManagerFactory factory = configuration.apply(settings);
        return factory.apply("tck");
    }
}
