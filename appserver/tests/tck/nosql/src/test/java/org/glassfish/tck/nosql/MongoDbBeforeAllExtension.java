package org.glassfish.tck.nosql;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.MongoDBContainer;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Order(10)
public class MongoDbBeforeAllExtension implements BeforeAllCallback, AfterAllCallback {

    private static MongoDBContainer mongodb;

    public static final String MP_CONFIG_CONTENT_KEY = "glassfish.tck.nosql.mongodb.config";

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {

        var dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        assumeTrue(dockerAvailable, "Docker is not available on this environment");

        if (mongodb == null) {
            mongodb = new MongoDBContainer("mongo:latest");
            mongodb.start();

            String mongoDbHost = mongodb.getHost() + ":" + mongodb.getFirstMappedPort();
            System.out.println("Started MongoDB in Docker at " + mongoDbHost);

            String config = "jnosql.document.database=tck\n" +
                            "jnosql.mongodb.host=" + mongoDbHost + "\n";
            System.setProperty(MP_CONFIG_CONTENT_KEY, config);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        // We keep it running across test classes for performance, Maven will kill it at the end
    }

}
