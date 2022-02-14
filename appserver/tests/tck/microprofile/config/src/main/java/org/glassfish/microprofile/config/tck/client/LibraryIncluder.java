package org.glassfish.microprofile.config.tck.client;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 *  This extension adds certain libraries to deployed Arquillian apps.
 *
 *  Libraries added:
 *      - Hamcrest, to prevent ClassNotFoundExceptions when running hamcrest tests
 */
public class LibraryIncluder implements ApplicationArchiveProcessor {

    private static final Logger LOGGER = Logger.getLogger(LibraryIncluder.class.getName());

    @Override
    public void process(Archive<?> archive, TestClass testClass) {

        // Only process web archives
        if (!(archive instanceof WebArchive)) return;
        final var webArchive = (WebArchive) archive;

        try {
            // Add Hamcrest
            webArchive.addAsLibraries(resolveDependency("org.hamcrest:hamcrest:2.2"));
        } catch (Exception e) {
            LOGGER.log(SEVERE, "Error adding dependencies", e);
        }
    }

    private static File[] resolveDependency(String coordinates) {
        return Maven.resolver()
                .resolve(coordinates)
                .withoutTransitivity().asFile();
    }
}
