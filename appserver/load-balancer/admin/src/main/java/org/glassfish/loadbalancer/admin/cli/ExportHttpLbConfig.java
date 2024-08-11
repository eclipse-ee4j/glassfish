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

package org.glassfish.loadbalancer.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.loadbalancer.admin.cli.helper.LbConfigHelper;
import org.glassfish.loadbalancer.admin.cli.reader.api.LoadbalancerReader;
import org.glassfish.loadbalancer.admin.cli.reader.impl.LoadbalancerReaderImpl;
import org.glassfish.loadbalancer.config.LbConfig;
import org.glassfish.loadbalancer.config.LoadBalancer;
import org.jvnet.hk2.annotations.Service;

/**
 * Export load-balancer xml
 *
 * @author Kshitiz Saxena
 */
@Service(name = "export-http-lb-config")
@PerLookup
@I18n("export.http.lb.config")
@RestEndpoints({
    @RestEndpoint(configBean=LbConfig.class,
        opType=RestEndpoint.OpType.POST, // TODO: Should probable be GET
        path="export-http-lb-config",
        description="export-http-lb-config"),
    @RestEndpoint(configBean=LoadBalancer.class,
        opType=RestEndpoint.OpType.POST, // TODO: Should probable be GET
        path="export-http-lb-config",
        description="export-http-lb-config",
        params={
            @RestParam(name="lbname", value="$parent")
        })
})
public class ExportHttpLbConfig implements AdminCommand {

    @Param(name = "lbtargets", separator = ',', optional = true)
    List<String> target;
    @Param(name = "config", optional = true)
    String lbConfigName;
    @Param(name = "lbname", optional = true)
    String lbName;
    @Param(name = "retrievefile", optional = true, defaultValue = "false")
    boolean retrieveFile;
    @Param(name = "file_name", optional = true, primary = true)
    String fileName;
    @Param(name = "property", optional = true, separator = ':')
    Properties properties;
    @Inject
    Domain domain;
    @Inject
    ApplicationRegistry appRegistry;
    @Inject
    ServerEnvironment env;

    private static final String DEFAULT_LB_XML_FILE_NAME =
            "loadbalancer.xml";

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        try {
            String msg = process(context);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            report.setMessage(msg);
        } catch (Throwable t) {
            String msg = LbLogUtil.getStringManager().getString("ExportHttpLbConfigFailed", t.getMessage());
            LbLogUtil.getLogger().log(Level.WARNING, msg);
            if (LbLogUtil.getLogger().isLoggable(Level.FINE)) {
                LbLogUtil.getLogger().log(Level.FINE, "Exception when exporting http lb config", t);
            }
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(t.getMessage());
            report.setFailureCause(t);
        }
    }

    public String process(AdminCommandContext context) throws Exception {

        LoadbalancerReader lbr = null;
        if (lbName != null && lbConfigName == null && target == null) {
            LoadBalancer lb = LbConfigHelper.getLoadBalancer(domain, lbName);
            lbr = LbConfigHelper.getLbReader(domain, appRegistry, lb.getLbConfigName());
        } else if (lbConfigName != null && lbName == null && target == null) {
            lbr = LbConfigHelper.getLbReader(domain, appRegistry, lbConfigName);
        } else if (target != null && lbName == null && lbConfigName == null) {
            Set<String> clusters = new HashSet<String>();
            clusters.addAll(target);
            lbr = new LoadbalancerReaderImpl(domain, appRegistry, clusters, properties);
        } else {
            String msg = LbLogUtil.getStringManager().getString("ExportHttpLbConfigInvalidArgs");
            throw new Exception(msg);
        }

        if (fileName == null) {
            String configName = lbr.getName();
            if (configName != null) {
                fileName = DEFAULT_LB_XML_FILE_NAME + "." + configName;
            } else {
                fileName = DEFAULT_LB_XML_FILE_NAME;
            }
        }

        File lbConfigFile = new File(fileName);
        if (!lbConfigFile.isAbsolute() && !retrieveFile) {
            File loadbalancerDir = new File(env.getInstanceRoot(),
                    "load-balancer");
            if (!loadbalancerDir.exists()) {
                boolean isMkdirSuccess = loadbalancerDir.mkdir();
                if(!isMkdirSuccess){
                    String msg = LbLogUtil.getStringManager().getString(
                            "directoryCreationFailed");
                    throw new Exception(msg);
                }
            }
            lbConfigFile = new File(loadbalancerDir, fileName);
        }

            File tmpLbXmlFile = null;
            if (retrieveFile) {
                tmpLbXmlFile = File.createTempFile("load-balancer", ".xml");
                tmpLbXmlFile.deleteOnExit();
            } else {
                if (lbConfigFile.exists()) {
                    String msg = LbLogUtil.getStringManager().getString(
                            "FileExists", lbConfigFile.getPath());
                    throw new Exception(msg);
                }

                if (!(lbConfigFile.getParentFile().exists())) {
                    String msg = LbLogUtil.getStringManager().getString(
                            "ParentFileMissing", lbConfigFile.getParent());
                    throw new Exception(msg);
                }
                tmpLbXmlFile = lbConfigFile;
            }

            FileOutputStream fo = null;

            try {
                fo = new FileOutputStream(tmpLbXmlFile);
                LbConfigHelper.exportXml(lbr, fo);
                if (retrieveFile) {
                    retrieveLbConfig(context, lbConfigFile, tmpLbXmlFile);
                }
                LbConfig lbConfig = lbr.getLbConfig();
                //Check for the case when lbtargets are provided
                //In such a case, lbconfig will be null
                if(lbConfig != null){
                    lbConfig.setLastExported();
                }
                String msg = LbLogUtil.getStringManager().getString(
                        "GeneratedFileLocation", lbConfigFile.toString());
                return msg;
            } finally {
                if (fo != null) {
                    fo.close();
                    fo = null;
                }
            }
        }


    private void retrieveLbConfig(AdminCommandContext context, File lbConfigFile,
                                  File tmpLbXmlFile) throws Exception {
        File localFile = lbConfigFile;
        Properties props = new Properties();
        File parent = localFile.getParentFile();
        if (parent == null) {
            parent = localFile;
        }
        props.setProperty("file-xfer-root", parent.getPath().replace('\\', '/'));
        URI parentURI = parent.toURI();
        try {
            context.getOutboundPayload().attachFile(
                    "text/xml",
                    parentURI.relativize(localFile.toURI()),
                    "sync-load-balancer-xml",
                    props,
                    tmpLbXmlFile);
        } catch (IOException ex) {
            String msg = LbLogUtil.getStringManager().getString(
                    "RetrieveFailed", lbConfigFile.getAbsolutePath());
            throw new Exception(msg, ex);
        }
    }
}
