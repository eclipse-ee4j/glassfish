/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.main.itest.tools;

import jakarta.ws.rs.client.Client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.glassfish.admin.rest.client.ClientWrapper;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.glassfish.main.itest.tools.asadmin.StartServ;
import org.glassfish.main.jdke.security.KeyTool;

import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_FILENAME_DEFAULT;
import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_PASSWORD_DEFAULT;
import static com.sun.enterprise.util.SystemPropertyConstants.TRUSTSTORE_FILENAME_DEFAULT;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.glassfish.embeddable.GlassFishVariable.JAVA_HOME;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This class represents GlassFish installation outside test environment.
 * <p>
 * Ensures that the domain in executed before first test started, and that the domain stops
 * after tests are finished.
 *
 * @author David Matejcek
 * @author Ondro Mihalyi
 */
public class GlassFishTestEnvironment {
    private static final Logger LOG = Logger.getLogger(GlassFishTestEnvironment.class.getName());

    private static final File BASEDIR = detectBasedir();
    private static final File GF_ROOT = resolveGlassFishRoot();

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASSWORD = "admintest";

    private static final File ASADMIN = findAsadmin();
    private static final File STARTSERV = findStartServ();
    private static final File JARSIGNER = findJarSigner();
    private static final File PASSWORD_FILE_FOR_UPDATE = findPasswordFile("password_update.txt");
    private static final File PASSWORD_FILE = findPasswordFile("password.txt");

    private static final int ASADMIN_START_DOMAIN_TIMEOUT = 60_000;
    /** 1 day. Useful for debugging */
    private static final int ASADMIN_START_DOMAIN_TIMEOUT_FOR_DEBUG = 1000 * 60 * 60 * 24;

    private static HttpClient client;

    static {
        if (!isGlassFishRunningRemotely()) {
            LOG.log(Level.INFO, "Using basedir: {0}", BASEDIR);
            LOG.log(Level.INFO, "Expected GlassFish directory: {0}", GF_ROOT);
            changePassword();
            Thread hook = new Thread(() -> {
                getAsadmin().exec(30_000, "stop-domain", "--kill", "--force");
            });
            Runtime.getRuntime().addShutdownHook(hook);
            final int timeout = isStartDomainSuspendEnabled()
                    ? ASADMIN_START_DOMAIN_TIMEOUT_FOR_DEBUG : ASADMIN_START_DOMAIN_TIMEOUT;
            // This is the absolutely first start - if it fails, all other starts will fail too.
            // Note: --suspend implicitly enables --debug
            assertThat(getAsadmin().exec(timeout,"start-domain",
                    isStartDomainSuspendEnabled() ? "--suspend" : "--debug"), asadminOK());
        }
    }


    /**
     * @return the installation directory, usually referred as AS_INSTALL and named
     *         <code>glassfish</code> (without version number).
     */
    public static File getGlassFishDirectory() {
        return GF_ROOT;
    }


    /**
     * @return {@link Asadmin} command api for tests.
     */
    public static Asadmin getAsadmin() {
        return getAsadmin(true);
    }

    /**
     * @param terse true means suitable and minimized for easy parsing.
     * @return {@link Asadmin} command api for tests.
     */
    public static Asadmin getAsadmin(boolean terse) {
        return new Asadmin(ASADMIN, ADMIN_USER, PASSWORD_FILE, terse);
    }

    /**
     * @return {@link Asadmin} command api for tests.
     */
    public static StartServ getStartServ() {
        return new StartServ(STARTSERV);
    }


    /**
     * @return {@link Asadmin} command api for tests.
     */
    public static StartServ getStartServInTopLevelBin() {
        return new StartServ(findStartServ("../"));
    }


    public static JarSigner getJarSigner() {
        return new JarSigner(JARSIGNER);
    }

    /**
     * @return {@link Asadmin} command api for tests.
     */
    public static File getAppClient() {
        return new File(getGlassFishDirectory(), isWindows() ? "bin/appclient.bat" : "bin/appclient");
    }

    /**
     * @return project's target directory.
     */
    public static File getTargetDirectory() {
        return new File(BASEDIR, "target");
    }


    /**
     * @return domain1 directory absolute path
     */
    public static Path getDomain1Directory() {
        return GF_ROOT.toPath().resolve(Paths.get("domains", "domain1"));
    }


    public static KeyStore getDomain1KeyStore() {
        Path keystore = getDomain1Directory().resolve(Paths.get("config", KEYSTORE_FILENAME_DEFAULT));
        try {
            return new KeyTool(keystore.toFile(), KEYSTORE_PASSWORD_DEFAULT.toCharArray()).loadKeyStore();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    public static KeyStore getDomain1TrustStore() {
        Path cacerts = getDomain1Directory().resolve(Paths.get("config", TRUSTSTORE_FILENAME_DEFAULT));
        try {
            return new KeyTool(cacerts.toFile(), KEYSTORE_PASSWORD_DEFAULT.toCharArray()).loadKeyStore();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    public static int getPort(HttpListenerType listenerType) {
        return listenerType.getPort();
    }

    /**
     * Creates a {@link Client} instance for the domain administrator.
     * Caller is responsible for closing.
     *
     * @return new {@link Client} instance
     */
    public static ClientWrapper createClient() {
        return new ClientWrapper(new HashMap<>(), ADMIN_USER, ADMIN_PASSWORD);
    }


    /**
     * Creates a {@link HttpURLConnection} for the admin administrator.
     *
     * @param context - part of the url behind the <code>http://localhost:4848</code>
     * @return a new disconnected {@link HttpURLConnection}.
     * @throws IOException
     */
    public static HttpURLConnection openConnection(final String context) throws IOException {
        final HttpURLConnection connection = openConnection(false, 4848, context);
        connection.setAuthenticator(new DasAuthenticator());
        return connection;
    }


    /**
     * Creates an unencrypted {@link HttpURLConnection} for the given port and context.
     *
     * @param port
     * @param context - part of the url behind the <code>http://localhost:[port]</code>
     * @return a new disconnected {@link HttpURLConnection}.
     * @throws IOException
     */
    public static HttpURLConnection openConnection(final int port, final String context) throws IOException {
        return openConnection(false, port, context);
    }


    /**
     * Creates a {@link HttpURLConnection} for the given port and context.
     *
     * @param secured true for https, false for http
     * @param port
     * @param context - part of the url behind the <code>http://localhost:[port]</code>
     * @return a new disconnected {@link HttpURLConnection}.
     * @throws IOException
     */
    public static <T extends HttpURLConnection> T openConnection(final boolean secured, final int port, final String context)
        throws IOException {
        final String protocol = secured ? "https" : "http";
        @SuppressWarnings("unchecked")
        final T connection = (T) new URL(protocol + "://localhost:" + port + context).openConnection();
        if (System.getProperty("glassfish.suspend") != null) {
            connection.setReadTimeout(0);
            connection.setConnectTimeout(0);
        } else {
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(100);
        }
        connection.setRequestProperty("X-Requested-By", "JUnit5Test");
        return connection;
    }

    /**
     * Creates a {@link HttpResponse} for the default port using unsecured HTTP.
     *
     * @param context - part of the url behind the <code>http://localhost:[port]</code>
     * @return a new disconnected {@link HttpResponse}.
     * @throws IOException
     */
    public static HttpResponse<String> getHttpResource(final String context) throws Exception {
        String rootContext = context;
        if (context != null && !context.startsWith("/")) {
            rootContext = "/" + context;
        }

        return getHttpResource(false, 8080, rootContext);
    }

    /**
     * Creates a {@link HttpResponse} for the given port and context.
     *
     * @param secured true for https, false for http
     * @param port
     * @param context - part of the url behind the <code>http://localhost:[port]</code>
     * @return a new disconnected {@link HttpResponse}.
     * @throws IOException
     */
    public static HttpResponse<String> getHttpResource(final boolean secured, final int port, final String context)
        throws Exception {
        final String protocol = secured ? "https" : "http";
        URI uri = URI.create(protocol + "://localhost:" + port + context);
        final HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(15))
                .header("X-Requested-By", "JUnit5Test")
                .build();
        return newInsecureHttpClient().send(request, ofString(StandardCharsets.UTF_8));
    }

    public static URI webSocketUri(final int port, final String context) throws URISyntaxException {
        return webSocketUri(false, port, context);
    }

    public static URI webSocketUri(final boolean secured, final int port, final String context) throws URISyntaxException {
        final String protocol = secured ? "wss" : "ws";
        return new URI(protocol + "://localhost:" + port + context);
    }

    /**
     * Creates the unencrypted password file on the local file system and uses it to create the user
     * record in the file realm.
     *
     * @param realmName
     * @param user
     * @param password
     * @param groupNames
     */
    public static void createFileUser(String realmName, String user, String password, String... groupNames) {
        final Path passwordFile = doIO(() -> Files.createTempFile("pwd", "txt"));
        try {
            Files.writeString(passwordFile,
                "AS_ADMIN_PASSWORD=" + ADMIN_PASSWORD + "\nAS_ADMIN_USERPASSWORD=" + password + "\n",
                StandardOpenOption.APPEND);
            Asadmin asadmin = new Asadmin(ASADMIN, ADMIN_USER, passwordFile.toFile());
            assertThat(asadmin.exec("create-file-user", "--groups", String.join(",", groupNames), "--authrealmname",
                realmName, "--target", "server", user), asadminOK());
        } catch (IOException e) {
            throw new IllegalStateException("Could not create the temporary password file.", e);
        } finally {
            doIO(() -> Files.delete(passwordFile));
        }
    }

    /**
     * This will delete the jobs.xml file
     */
    public static void deleteJobsFile() {
        Path path = GF_ROOT.toPath().resolve(Paths.get("domains", "domain1", "config", "jobs.xml"));
        LOG.log(Level.CONFIG, "Deleting: " + path);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    /** Default is org.apache.derby.jdbc.ClientDataSource */
    public static void switchDerbyPoolToEmbededded() {
        final AsadminResult result = getAsadmin(true).exec(5_000, "set",
            "resources.jdbc-connection-pool.DerbyPool.datasource-classname=org.apache.derby.jdbc.EmbeddedDataSource",
            "resources.jdbc-connection-pool.DerbyPool.property.PortNumber=",
            "resources.jdbc-connection-pool.DerbyPool.property.serverName=",
            "resources.jdbc-connection-pool.DerbyPool.property.URL=");
        assertThat(result, asadminOK());
        // Just to see the result in log.
        assertThat(getAsadmin(true).exec(5_000, "get", "resources.jdbc-connection-pool.DerbyPool.*"), asadminOK());
    }

    /**
     * Useful for a heuristic inside Eclipse and other environments.
     *
     * @return Absolute path to the glassfish directory.
     */
    private static File detectBasedir() {
        final String basedir = System.getProperty("basedir");
        if (basedir != null) {
            return new File(basedir);
        }
        final File target = new File("target");
        if (target.exists()) {
            return target.getAbsoluteFile().getParentFile();
        }
        return new File(".").getAbsoluteFile().getParentFile();
    }


    private static File resolveGlassFishRoot() {
        final File gfDir = BASEDIR.toPath().resolve(Path.of("target", "glassfish9", "glassfish")).toFile();
        if (gfDir == null || !gfDir.exists()) {
            throw new IllegalStateException("The expected GlassFish home directory doesn't exist: " + gfDir);
        }
        return gfDir;
    }


    private static File findAsadmin() {
        return new File(GF_ROOT, "bin/asadmin.java");
    }

    private static File findStartServ(String... optionalPrefix) {
        String prefix = optionalPrefix.length > 0 ? optionalPrefix[0] : "";
        return new File(GF_ROOT, isWindows() ? prefix + "bin/startserv.bat" : prefix + "bin/startserv");
    }

    private static File findJarSigner() {
        return new File(System.getProperty(JAVA_HOME.getSystemPropertyName()), isWindows() ? "bin/jarsigner.exe" : "bin/jarsigner");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
    }


    private static File findPasswordFile(final String filename) {
        File output = new File(getTargetDirectory(), filename);
        if (output.exists()) {
            return output;
        }
        try {
            final Enumeration<URL> urls = Asadmin.class.getClassLoader().getResources(filename);
            if (urls == null || !urls.hasMoreElements()) {
                throw new IllegalStateException(filename + " not found");
            }
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try (InputStream input = url.openStream()) {
                    Files.copy(input, output.toPath());
                }
                return output;
            }
            throw new IllegalStateException(filename + " not found");
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }


    private static void changePassword() {
        final Asadmin asadmin = new Asadmin(ASADMIN, ADMIN_USER, PASSWORD_FILE_FOR_UPDATE);
        final AsadminResult result = asadmin.exec(20_000, "change-admin-password");
        if (result.isError()) {
            // probably changed by previous execution without maven clean
            System.out.println("Admin password NOT changed.");
        } else {
            System.out.println("Admin password changed.");
        }
    }


    private static boolean isStartDomainSuspendEnabled() {
        final String envValue = System.getenv("GLASSFISH_SUSPEND");
        return envValue == null ? Boolean.getBoolean("glassfish.suspend") : Boolean.parseBoolean(envValue);
    }

    public static boolean isGlassFishRunningRemotely() {
        final String envValue = System.getenv("GLASSFISH_REMOTE");
        return envValue == null ? Boolean.getBoolean("glassfish.remote") : Boolean.parseBoolean(envValue);
    }

    private static <T> T doIO(IOSupplier<T> action) {
        try {
            return action.execute();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


    private static void doIO(IOAction action) {
        try {
            action.execute();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static class DasAuthenticator extends Authenticator {

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(ADMIN_USER, ADMIN_PASSWORD.toCharArray());
        }
    }

    @FunctionalInterface
    private interface IOSupplier<T> {

        T execute() throws IOException;
    }

    @FunctionalInterface
    private interface IOAction {

        void execute() throws IOException;
    }

    private static HttpClient newInsecureHttpClient() throws KeyManagementException, NoSuchAlgorithmException {
        if (client == null) {
            // Java 17 doesn't allow to close http client, so we reuse a global one.
            // Once we start using Java 21, client should be created for every call and returned instance should be closed
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            client = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .connectTimeout(Duration.ofMillis(100))
                    .build();
        }
        return client;
    }

    // FIXME: add loading of the right certificate from keystore.
    private static final TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }


            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }


            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
    };
}
