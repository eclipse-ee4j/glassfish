/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package eetimer;

import admin.AdminBaseDevTest;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimerTestBase extends AdminBaseDevTest {
    protected static final String ejb_jar_name = System.getProperty("ejb-jar-name");
    protected static final String ejb_jar_path = System.getProperty("ejb-jar-path");
    protected static final String cluster_name = System.getProperty("cluster-name");
    protected static final String instance_name_1 = System.getProperty("instance-name-1");
    protected static final String instance_name_2 = System.getProperty("instance-name-2");
    protected static final String instance_name_3 = System.getProperty("instance-name-3");

    protected static final Logger logger = Logger.getLogger(TimerTestBase.class.getName());

    @Override
    protected String getTestDescription() {
        return "devtests for ejb ee timer";
    }

    protected void deployEjbCreateTimers(String target) {
        AsadminReturn output = asadminWithOutput(
                "deploy", "--force", "true", "--target", target, ejb_jar_path);
        logger.log(Level.INFO, output.outAndErr);
    }

    protected void undeployEjb(String target) {
        String moduleName = ejb_jar_name.substring(0, ejb_jar_name.indexOf("."));
        AsadminReturn output = asadminWithOutput(
                "undeploy", "--target", target, moduleName);
        logger.log(Level.INFO, output.outAndErr);
    }

    /**
     * Parses list-timers output and save the result to a Map of instanceName:timerCount
     * @param output output from asadmin command list-timers
     * @return resultMap whose key is the instance name, and value is the number of timers.
     */
    protected Map<String, Integer> countInstanceTimers(String output) {
        Map<String, Integer> resultMap = new HashMap<String, Integer>();
        String[] lines = output.split(System.getProperty("line.separator"));
        //the first line is the asadmin command line itself.
        //If the output format changes in the future, need to change here too
        for(int i = 1; i < lines.length; i++) {
            String[] pair = lines[i].split(":");
            String k = pair[0].trim();
            String v = pair[1].trim();
            if(!k.isEmpty()) {
                resultMap.put(k, Integer.valueOf(v));
            }
        }
        logger.log(Level.INFO, "instance::timer map: {0}, from output: {1}",
                new Object[]{resultMap, output});
        return resultMap;
    }
}
