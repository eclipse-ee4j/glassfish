package org.glassfish.main.test.app.ejb;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.RandomGenerator;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.lang.System.Logger.Level.INFO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccessEJBWebTest {
    private static final System.Logger LOG = System.getLogger(AccessEJBWebTest.class.getName());
    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();
    private static final String APP_NAME = "echoservice";

    @BeforeAll
    static void deploy() {
        File war = createDeployment();
        try {
            AsadminResult result = ASADMIN.exec("deploy", "--contextroot", "/", "--name", APP_NAME,
                    war.getAbsolutePath());
            assertThat(result, AsadminResultMatcher.asadminOK());
        } finally {
            war.delete();
        }
    }

    @AfterAll
    static void undeploy() {
        AsadminResult result = ASADMIN.exec("undeploy", APP_NAME);
        assertThat(result, AsadminResultMatcher.asadminOK());
    }

    @Test
    void testAccessLocalEJBByCDI() throws Exception {
        String message = RandomGenerator.generateRandomString();
        HttpURLConnection connection = GlassFishTestEnvironment
                .openConnection(8080, "/local_ejb_cdi?message=" + message);
        connection.setRequestMethod("GET");
        assertAll(
                () -> assertEquals(200, connection.getResponseCode()),
                () -> assertEquals(message, readResponseContent(connection))
        );
    }

    @Test
    void testAccessLocalEJBByJNDI() throws Exception {
        String message = RandomGenerator.generateRandomString();
        HttpURLConnection connection = GlassFishTestEnvironment
                .openConnection(8080, "/local_ejb_jndi?message=" + message);
        connection.setRequestMethod("GET");
        assertAll(
                () -> assertEquals(200, connection.getResponseCode()),
                () -> assertEquals(message, readResponseContent(connection))
        );
    }

    @Test
    void testAccessRemoteEJBByCDI() throws Exception {
        String message = RandomGenerator.generateRandomString();
        HttpURLConnection connection = GlassFishTestEnvironment
                .openConnection(8080, "/remote_ejb_cdi?message=" + message);
        connection.setRequestMethod("GET");
        assertAll(
                () -> assertEquals(200, connection.getResponseCode()),
                () -> assertEquals(message, readResponseContent(connection))
        );
    }

    @Test
    void testAccessRemoteEJBByJNDI() throws Exception {
        String message = RandomGenerator.generateRandomString();
        HttpURLConnection connection = GlassFishTestEnvironment
                .openConnection(8080, "/remote_ejb_jndi?message=" + message);
        connection.setRequestMethod("GET");
        assertAll(
                () -> assertEquals(200, connection.getResponseCode()),
                () -> assertEquals(message, readResponseContent(connection))
        );
    }

    private String readResponseContent(HttpURLConnection connection) throws Exception {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private static File createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, APP_NAME + ".war")
                .addClasses(EchoService.class, EchoServiceLocal.class, EchoServiceRemote.class, EchoServiceEJB.class)
                .addClasses(AccessLocalEJBByCDIServlet.class, AccessLocalEJBByJNDIServlet.class)
                .addClasses(AccessRemoteEJBByCDIServlet.class, AccessRemoteEJBByJNDIServlet.class);
        LOG.log(INFO, war.toString(true));
        try {
            File tempFile = File.createTempFile(APP_NAME, ".war");
            war.as(ZipExporter.class).exportTo(tempFile, true);
            return tempFile;
        } catch (IOException e) {
            throw new IllegalStateException("Deployment failed - cannot load the input archive!", e);
        }
    }
}
