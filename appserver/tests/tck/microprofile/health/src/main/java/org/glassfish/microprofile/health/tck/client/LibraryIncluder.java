package org.glassfish.microprofile.health.tck.client;

import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 *  This extension adds certain libraries to deployed Arquillian apps.
 *
 *  Libraries added:
 *      - Hamcrest, to prevent ClassNotFoundExceptions when running hamcrest tests
 */
public class LibraryIncluder implements ApplicationArchiveProcessor {

    private static final Logger LOG = System.getLogger(LibraryIncluder.class.getName());

    @Override
    public void process(Archive<?> archive, TestClass testClass) {

        // Only process web archives
        if (!(archive instanceof WebArchive)) {
            return;
        }
        final var webArchive = (WebArchive) archive;
        webArchive.addAsLibraries(resolveDependency("org.hamcrest:hamcrest"));
        webArchive.addAsLibraries(resolveDependencyTransitive("org.eclipse.microprofile.health:microprofile-health-tck"));
        LOG.log(Level.INFO, () -> "webArchive:\n" + webArchive.toString(true));
    }

    private static File[] resolveDependency(String coordinates) {
        return Maven.configureResolver().workOffline().loadPomFromFile("pom.xml")
                .resolve(coordinates).withoutTransitivity()
                .asFile();
    }

    private static File[] resolveDependencyTransitive(String coordinates) {
        return Maven.configureResolver().workOffline().loadPomFromFile("pom.xml")
                .resolve(coordinates).withTransitivity()
                .asFile();
    }
}
