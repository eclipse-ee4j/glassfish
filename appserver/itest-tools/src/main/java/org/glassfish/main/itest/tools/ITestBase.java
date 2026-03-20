package org.glassfish.main.itest.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

import static java.lang.System.Logger.Level.INFO;
import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.HttpListenerType.HTTP;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
public class ITestBase {

    private static final System.Logger LOG = System.getLogger(ITestBase.class.getName());

    protected static final Asadmin ASADMIN = getAsadmin();

    @TempDir
    protected File tempDir;
    protected File warFile;
    protected String appName;

    protected String result = "";
    protected String host = "localhost";
    protected String port = GlassFishTestEnvironment.getPort(HTTP) + "";

    protected void doDeploy(WebArchive webArchive) throws Exception {
        LOG.log(INFO, webArchive.toString(true));

        appName = webArchive.getName();

        warFile = new File(tempDir, appName + ".war");
        webArchive.as(ZipExporter.class).exportTo(warFile, true);

        assertThat(ASADMIN.exec("deploy", "--target", "server", warFile.getAbsolutePath()), asadminOK());
    }

    protected boolean doTest(String testCase, String expectedResponse) throws Exception {
        boolean result = false;

        String url = "http://" + host + ":" + port + "/" + appName + "/test?testcase=" + testCase;
        System.out.println("******************** url=" + url);

        HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
        int code = conn.getResponseCode();
        if (code != 200) {
            System.err.println("Unexpected return code: " + code);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = input.readLine()) != null) {
                if (line.contains(expectedResponse)) {
                    result = true;
                    break;
                }
            }

        }

        return result;
    }

    protected static void echo(String msg) {
        System.out.println(msg);
    }

    @AfterAll
    protected void doCleanup() throws Exception {
        assertThat(ASADMIN.exec("undeploy", appName), asadminOK());
        TestUtilities.delete(warFile);
    }

}
