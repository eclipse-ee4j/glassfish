/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import com.sun.ejte.ccl.reporter.*;
import com.sun.appserv.test.BaseDevTest;

/**
 * Unit test for HA SSO Failover.
 *
 */
public class WebTest extends BaseDevTest {

    static class SessionData {
        private String jsessionId;
        private String jsessionIdVersion;
        private String jreplica;
    }

    private static final String TEST_NAME = "ha-sso-failover";
    private static final String JSESSIONID = "JSESSIONID";
    private static final String JSESSIONIDVERSION = "JSESSIONIDVERSION";
    private static final String JREPLICA = "JREPLICA";
    private static final String JSESSIONIDSSO = "JSESSIONIDSSO";
    private static final String JSESSIONIDSSOVERSION = "JSESSIONIDSSOVERSION";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private int port1;
    private int port2;
    private String instancename1;
    private String contextRootPrefix;
    private String user;
    private String password;
    private Map<String, SessionData> app2Sd = new HashMap<String, SessionData>();
    private String ssoId;
    private String ssoIdVersion;
    private long ssoIdVersionNumber = -1L;

    public WebTest(String[] args) {
        host = args[0];
        port1 = Integer.parseInt(args[1]);
        port2 = Integer.parseInt(args[2]);
        instancename1 = args[3];
        contextRootPrefix = "/" + args[4];
        user = args[5];
        password = args[6];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for GlassFish Issue 1933");
        WebTest webTest = new WebTest(args);

        try {
            webTest.run();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public String getTestName() {
        return TEST_NAME;
    }

    public String getTestDescription() {
        return TEST_NAME;
    }

    public void run() throws Exception {
        /*
         * Access login.jsp
         */
        app2Sd.put("A", new SessionData());
        app2Sd.put("B", new SessionData());
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        try {
            sock = new Socket(host, new Integer(port1).intValue());
            os = sock.getOutputStream();
            String postData = "j_username=" + user
                + "&j_password=" + password;
            String post = "POST " + contextRootPrefix + "-a/j_security_check"
                + " HTTP/1.0\n"
                + "Content-Type: application/x-www-form-urlencoded\n"
                + "Content-length: " + postData.length() + "\n\n"
                + postData;
            System.out.println(post);
            os.write(post.getBytes());
            os.flush();

            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            String location = null;
            String cookie = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Location:")) {
                    location = line;
                } else if (line.startsWith("Set-Cookie:")
                        || line.startsWith("Set-cookie:")) {
                    cookie = line;
                    parseCookies(line, app2Sd.get("A"));
                }
            }

            if (ssoIdVersion != null) {
                long ssoVer = Long.valueOf(ssoIdVersion.substring(
                        JSESSIONIDSSOVERSION.length() + 1));
                if (ssoIdVersionNumber == -1 ||
                        ssoIdVersionNumber + 1 == ssoVer) {
                    ssoIdVersionNumber = ssoVer;
                } else {
                    throw new Exception("Version number does not match: " +
                           ssoIdVersionNumber + ", " + ssoVer);
                }
            }

            if (cookie == null) {
                throw new Exception("Missing Set-Cookie response header");
            } else if (location == null) {
                throw new Exception("Missing Location response header");
            }

            String redirect = location.substring("Location:".length()).trim();
            // follow the redirect
            int cA1 = go(port1, new URL(redirect).getPath(), "A");
            int cB1 = go(port1, contextRootPrefix + "-b/index.jsp", "B");

            // stop inst1
            asadmin("stop-local-instance", instancename1);

            int cB2 = go(port2, contextRootPrefix + "-b/index.jsp", "B");
            int cA2 = go(port2, contextRootPrefix + "-a/index.jsp", "A");

            if ((cA2 - cA1 != 1) && (cB2 - cB1 != 1)) {
                throw new Exception("count does not match: " + cA1 + ", " + cB1 + ", " + cA2 + ", " + cB2);
            }
        } finally {
            close(sock);
            close(os);
            close(br);
            close(is);
        }
    }

    /*
     * Access http://<host>:<port>/web-ha-sso-failover-<aName> .
     * @return the associated count value
     */
    private int go(int port, String path, String aName)
            throws Exception {

        int count = -1;
        String countPrefix = aName + ":" + user + ":";
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        try {
            sock = new Socket(host, port);
            os = sock.getOutputStream();
            String get = "GET " + path + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            StringBuilder sb = new StringBuilder("Cookie: ");
            sb.append(ssoId);
            if (ssoIdVersion != null) {
                sb.append(";" + ssoIdVersion);
            }
            SessionData data = app2Sd.get(aName);
            if (data.jsessionId != null) {
                sb.append(";" + data.jsessionId);
            }
            if (data.jsessionIdVersion != null) {
                sb.append(";" + data.jsessionIdVersion);
            }
            if (data.jreplica != null) {
                sb.append(";" + data.jreplica);
            }
            os.write(sb.toString().getBytes());
            System.out.println(sb);

            os.write("\n\n".getBytes());

            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            String cookieHeader = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Set-Cookie:") ||
                        line.startsWith("Set-cookie:")) {
                    parseCookies(line, app2Sd.get(aName));
                }
                int index = line.indexOf(countPrefix);
                if (index >= 0) {
                    count = Integer.parseInt(line.substring(index + countPrefix.length()));
                    break;
                }
            }
        } finally {
            close(sock);
            close(os);
            close(br);
            close(is);
        }

        if (count == -1) {
            throw new Exception("Failed to access index.jsp");
        }

        System.out.println("Count: " + countPrefix + count);

        return count;
    }

    private void parseCookies(String cookie, SessionData data) {
        String value = getSessionIdFromCookie(cookie, JSESSIONID);
        if (value != null) {
            data.jsessionId = value;
        }
        value = getSessionIdFromCookie(cookie, JSESSIONIDVERSION);
        if (value != null) {
            data.jsessionIdVersion = value;
        }
        value = getSessionIdFromCookie(cookie, JREPLICA);
        if (value != null) {
            data.jreplica = value;
        }
        value = getSessionIdFromCookie(cookie, JSESSIONIDSSO);
        if (value != null) {
            ssoId = value;
        }
        value = getSessionIdFromCookie(cookie, JSESSIONIDSSOVERSION);
        if (value != null) {
            ssoIdVersion = value;
        }
    }

    private String getSessionIdFromCookie(String cookie, String field) {

        String ret = null;

        int index = cookie.indexOf(field + "=");
        if (index != -1) {
            int endIndex = cookie.indexOf(';', index);
            if (endIndex != -1) {
                ret = cookie.substring(index, endIndex);
            } else {
                ret = cookie.substring(index);
            }
            ret = ret.trim();
        }

        return ret;
    }

    private void close(Socket sock) {
        try {
            if (sock != null) {
                sock.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }
}
