/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.JavaConfig;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static com.sun.enterprise.admin.servermgmt.SLogger.JVM_OPTION_UPGRADE_FAILURE;
import static com.sun.enterprise.admin.servermgmt.SLogger.getLogger;

/**
 * Change the jvm-options from v2 to v3
 *
 * @author Byron Nevins
 */
@Service
@PerLookup
public class V2ToV3ConfigUpgrade implements ConfigurationUpgrade, PostConstruct {

    @Inject
    Configs configs;

    /**
     * Report the JavaConfig beans for each config.
     * <p>
     * Lets the caller command prepare access checks for security authorization.
     *
     * @return
     */
    public Collection<JavaConfig> getJavaConfigs() {
        final Collection<JavaConfig> result = new ArrayList<JavaConfig>();
        for (Config c : configs.getConfig()) {
            if (c.getJavaConfig() != null) {
                result.add(c.getJavaConfig());
            }
        }
        return result;
    }

    @Override
    public void postConstruct() {
        // the 'prevent' defense
        if (configs == null || configs.getConfig() == null || configs.getConfig().isEmpty()) {
            return;
        }

        try {
            for (Config c : configs.getConfig()) {
                JavaConfig jc = c.getJavaConfig();
                if (jc == null) {
                    continue;
                }

                // fix issues where each new config gets 2x, 3x, 4x the data
                newJvmOptions.clear();
                oldJvmOptions = Collections.unmodifiableList(jc.getJvmOptions());
                doAdditions("server-config".equals(c.getName()));
                doRemovals();
                ConfigSupport.apply(new JavaConfigChanger(), jc);
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, JVM_OPTION_UPGRADE_FAILURE, e);
            throw new RuntimeException(e);
        }
    }

    private void doRemovals() {
        // copy options from old to new.  Don't add items on the removal list
        // note that the remove list also has all the items we just added with
        // doAdditions() so that we don't get duplicate messes.
        for (String s : oldJvmOptions) {
            if (!shouldRemove(s))
                newJvmOptions.add(s);
        }
    }

    private void doAdditions(boolean isDas) {
        // add new options
        doAdditionsFrom(ADD_LIST);
        if (isDas) {
            doAdditionsFrom(ADD_LIST_DAS);
        } else {
            doAdditionsFrom(ADD_LIST_NOT_DAS);
        }
    }

    private void doAdditionsFrom(String[] strings) {
        newJvmOptions.addAll(Arrays.asList(strings));
    }

    private boolean shouldRemove(String option) {
        if (!ok(option))
            return true;

        for (String s : REMOVAL_LIST)
            if (option.startsWith(s))
                return true;

        return false;
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    private List<String> oldJvmOptions = null;
    private final List<String> newJvmOptions = new ArrayList<String>();
    private static final String[] BASE_REMOVAL_LIST = new String[] { "-Djavax.management.builder.initial",
            "-Dsun.rmi.dgc.server.gcInterval", "-Dsun.rmi.dgc.client.gcInterval", "-Dcom.sun.enterprise.taglibs",
            "-Dcom.sun.enterprise.taglisteners", "-XX:LogFile", };
    // these are added to all configs
    private static final String[] ADD_LIST = new String[] { "-XX:+UnlockDiagnosticVMOptions", "-XX:+LogVMOutput",
            "-XX:LogFile=${com.sun.aas.instanceRoot}/logs/jvm.log", "-Djava.awt.headless=true", "-DANTLR_USE_DIRECT_CLASS_LOADING=true",
            "-Dosgi.shell.telnet.maxconn=1", "-Dosgi.shell.telnet.ip=127.0.0.1", "-Dgosh.args=--nointeractive",
            "-Dfelix.fileinstall.dir=${com.sun.aas.installRoot}/modules/autostart/", "-Dfelix.fileinstall.poll=5000",
            "-Dfelix.fileinstall.debug=3", "-Dfelix.fileinstall.bundles.new.start=true", "-Dfelix.fileinstall.bundles.startTransient=true",
            "-Dfelix.fileinstall.disableConfigSave=false", "-Dfelix.fileinstall.log.level=2",
            "-Djavax.management.builder.initial=com.sun.enterprise.v3.admin.AppServerMBeanServerBuilder",
            "-Dorg.glassfish.web.rfc2109_cookie_names_enforced=false" };
    // these are added to DAS only
    private static final String[] ADD_LIST_DAS = new String[] { "-Dosgi.shell.telnet.port=6666" };
    // these are added to instances
    private static final String[] ADD_LIST_NOT_DAS = new String[] { "-Dosgi.shell.telnet.port=${OSGI_SHELL_TELNET_PORT}" };
    private static final List<String> REMOVAL_LIST = new ArrayList<String>();

    static {
        Collections.addAll(REMOVAL_LIST, BASE_REMOVAL_LIST);
        Collections.addAll(REMOVAL_LIST, ADD_LIST);
    }

    private class JavaConfigChanger implements SingleConfigCode<JavaConfig> {

        @Override
        public Object run(JavaConfig jc) throws PropertyVetoException, TransactionFailure {
            jc.setJvmOptions(newJvmOptions);
            return jc;
        }
    }
}
