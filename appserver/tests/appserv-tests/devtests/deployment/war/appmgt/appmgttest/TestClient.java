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

package appmgttest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Command-line args should be:
 *   URL to use in accessing the servlet
 *   expect positive result (true or false)
 *   one or more of the two following formats:
 *     -env name(class)=value//"desc"
 *     -param name=value//"desc"
 *
 * @author tjquinn
 */
public class TestClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new TestClient().run(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void run(String[] args) {

        String url = args[0];
        boolean testPositive = (Boolean.valueOf(args[1])).booleanValue();
        try {
            log("Test: devtests/deployment/war/appmgt");
            final Map<String,EnvEntryInfo> envs = new HashMap<String,EnvEntryInfo>();
            final Map<String,ParamInfo> params = new HashMap<String,ParamInfo>();
            int code = invokeServlet(url, envs, params);

            /*
             * We always expect the servlet to respond.
             */
            report(code, true);

            boolean entireTestPassed = true;

            String nextTestType = null;
            for (int i = 2; i < args.length; i++) {
                final String arg = args[i];
                if (arg.startsWith("-")) {
                    nextTestType = arg;
                } else {
                    if (nextTestType.equals("-env")) {
                        EnvEntryInfo target = EnvEntryInfo.parseBrief(arg);
                        EnvEntryInfo match = envs.get(target.name());
                        entireTestPassed &= reportCheck(target, match, "env-entry", target.name());
                    } else if (nextTestType.equals("-param")) {
                        ParamInfo target = ParamInfo.parseBrief(arg);
                        ParamInfo match = params.get(target.name());
                        entireTestPassed &= reportCheck(target, match, "context-param", target.name());
                    }
                }
            }
            if (entireTestPassed == testPositive) {
                pass();
            } else {
                fail();
            }

        } catch (IOException ex) {
            if (testPositive) {
                ex.printStackTrace();
                fail();
            } else {
                log("Caught EXPECTED IOException: " + ex);
                pass();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private boolean reportCheck(Object target, Object match, final String testType, final String targetName) {
        boolean result;
        if (match == null) {
            System.err.println("No matching " + testType + " for target name " + targetName);
            result = false;
        } else if ( ! match.equals(target)) {
            System.err.println("Target " + target.toString() + " != match " + match.toString());
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    private int invokeServlet(final String url, final Map<String,EnvEntryInfo> envs,
            final Map<String,ParamInfo> params) throws Exception {
        log("Invoking URL = " + url);
        URL u = new URL(url);
        HttpURLConnection c1 = (HttpURLConnection)u.openConnection();
        int code = c1.getResponseCode();
        if (code == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(c1.getInputStream()));
            String line;
            System.out.println("From servlet:");
            while ((line = br.readLine()) != null) {
                System.out.println("  " + line);
                if (line.startsWith("-env")) {
                    final EnvEntryInfo env = EnvEntryInfo.parseBrief(line.substring("-env".length() + 1));
                    envs.put(env.name(), env);
                } else if (line.startsWith("-param")) {
                    final ParamInfo param = ParamInfo.parseBrief(line.substring("-param".length() + 1));
                    params.put(param.name(), param);
                } else {
                    System.err.println("Unrecognized response line from servlet - continuing:");
                    System.err.println(">>" + line);
                }
            }

            System.out.println("servlet done");
            br.close();
        }
        return code;
    }

    private void report(int code, boolean testPositive) {
        if (testPositive) { //expect return code 200
            if(code != 200) {
                log("Incorrect return code: " + code);
                fail();
            } else {
                log("Correct return code: " + code);
            }
        } else {
            if(code != 200) { //expect return code !200
                log("Correct return code: " + code);
            } else {
                log("Incorrect return code: " + code);
                fail();
            }
        }
    }

    private void log(String message) {
        System.err.println("[war.client.Client]:: " + message);
    }

    private void pass() {
        log("PASSED: devtests/deployment/war/appmgt");
        System.exit(0);
    }

    private void fail() {
        log("FAILED: devtests/deployment/war/appmgt");
        System.exit(1);
    }
}
