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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.cluster.SyncRequest;

import jakarta.inject.Inject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.admin.payload.PayloadImpl;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

/**
 * Usage:
 * export-sync-bundle --target cluster_std-alone-instance [--retrieve <false>]
 *[file_name]
 *
 * --target      Cluster or stand alone server instance (required)
 * --retrieve    When true, the zip file is downloaded under the specified file_name in local machine
 *                 When false, the zip file is exported under the the specified file_name on DAS
 *                 Default value is false. (optional)
 *
 * file_name    Specifies the file name and location of the synchronized content.
 * If file_name is not specified and --retrieve=false, the default value is
 * install-root/domains/domain_name/sync/<target>-sync-bundle.zip.
 * If file_name is not specified and --retrieve=true, the default value is
 * <target>-sync-bundle.zip.  (optional)
 *
 * @author Byron Nevins
 * @author Jennifer Chou
 */
@org.glassfish.api.admin.ExecuteOn(RuntimeType.DAS)
@Service(name = "export-sync-bundle")
@PerLookup
//@TargetType(value={CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE})
@I18n("export-sync-bundle")
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="export-sync-bundle",
        description="export-sync-bundle")
})
public class ExportSyncBundle implements AdminCommand {

    @Param(name="target", optional = false)
    private String cluster_instance;
    @Param(name="retrieve", optional = true, defaultValue="false")
    private boolean isRetrieve;
    @Param(optional = true, primary = true)
    String file_name;

    @Override
    public void execute(AdminCommandContext context) {
        report = context.getActionReport();
        report.setActionExitCode(ExitCode.SUCCESS);
        logger = context.getLogger();

        // we use our own private payload.  Don't use the one in the context!
        payload = PayloadImpl.Outbound.newInstance();


        try {
            if (!isValid())
                return;

            if (!setSyncBundleExportFile())
                return;

            syncRequest = new SyncRequest();
            syncRequest.instance = cluster_instance;

            if (!sync())
                return;

            // write to the das or temp file
            write();

            //all OK...download local file
            if (isRetrieve)
                pumpItOut(context);
        }
        catch (Exception e) {
            setError(Strings.get("export.sync.bundle.fail", e.toString()));
            logger.log(Level.SEVERE, Strings.get("export.sync.bundle.fail", e.toString()), e);
            return;
        }
    }

    private void pumpItOut(AdminCommandContext context) {
        String fileName = file_name != null && !file_name.isEmpty() ? file_name : getDefaultBundleName();
        File localFile = new File(fileName.replace('\\', '/'));
        Properties props = new Properties();
        File parent = localFile.getParentFile();
        if (parent == null) {
            parent = localFile;
        }
        props.setProperty("file-xfer-root", parent.getPath().replace('\\', '/'));
        URI parentURI = parent.toURI();
        try {
            context.getOutboundPayload().attachFile(
                    "application/octet-stream",
                    parentURI.relativize(localFile.toURI()),
                    "sync-bundle",
                    props,
                    syncBundleExport);
        } catch (IOException ex) {
            setError(Strings.get("export.sync.bundle.retrieveFailed", ex.getLocalizedMessage()));
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("fileURI: " +
                                parentURI.relativize(localFile.toURI()));
                logger.finer("file-xfer-root: " +
                                parent.getPath().replace('\\', '/'));
                logger.finer("file: " + syncBundleExport.getAbsolutePath());
            }
        }
    }

    private boolean sync() {
        for (String dir : ALL_DIRS) {
            syncRequest.dir = dir;

            if (!syncOne())
                return false;
        }

        return !hasError();
    }

    private void write() {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(syncBundleExport));
            payload.writeTo(out);
        } catch (IOException ex) {
            setError(Strings.get("export.sync.bundle.exportFailed",
                    syncBundleExport.getAbsolutePath(), ex.getLocalizedMessage()));
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    logger.warning(Strings.get("export.sync.bundle.closeStreamFailed",
                            syncBundleExport.getAbsolutePath(), ex.getLocalizedMessage()));
                }
            }
        }
        if (!isRetrieve) {
            if (syncBundleExport.isFile()) {
                report.setMessage(Strings.get("export.sync.bundle.success", syncBundleExport.getAbsolutePath()));
            } else {
                setError(Strings.get("export.sync.bundle.fail", syncBundleExport.getAbsolutePath()));
            }
        }
    }

    private boolean syncOne() {
        if (instance != null) {
            serverSynchronizer.synchronize(instance, syncRequest, payload, report, logger);
        }
        if (cluster != null) { // Use one of the clustered instances
            List<Server> slist = cluster.getInstances();
            if (slist != null && !slist.isEmpty()) {
                Server s = slist.get(0);
                serverSynchronizer.synchronize(s, syncRequest, payload, report, logger);
            }
        }

        // synchronize() will be set to FAILURE if there were problems
        return !hasError();
    }

    private File getDefaultBundle() {
        return new File(new File(env.getInstanceRoot(), "sync"), getDefaultBundleName());
    }

    private String getDefaultBundleName() {
        return cluster_instance + "-sync-bundle.zip";
    }

    private boolean isValid() {
        // verify the cluster or stand-alone server name corresponds to reality!
        if (servers != null)
            instance = servers.getServer(cluster_instance);
        if (clusters != null) {
            cluster = clusters.getCluster(cluster_instance);
            if (cluster != null) {
                List<Server> list = cluster.getInstances();
                if (list == null || list.isEmpty()) {
                    setError(Strings.get("sync.empty_cluster", cluster_instance));
                    return false;
                }
            }
        }
        if (instance == null && cluster == null) {
            setError(Strings.get("sync.unknown.instanceOrCluster", cluster_instance));
            return false;
        }

        return true;
    }

    private boolean setSyncBundleExportFile() {
        if (isRetrieve) {
            try {
                syncBundleExport = File.createTempFile("GlassFishSyncBundle", ".zip");
                syncBundleExport.deleteOnExit();
            } catch (Exception ex) {
                syncBundleExport = null;
                setError(Strings.get("sync.bad_temp_file", ex.getLocalizedMessage()));
                return false;
            }
        } else {
            File f = null;
            if (file_name != null && !file_name.isEmpty()) {
                f = new File(file_name);
                if (f.isDirectory()) {
                    //Existing directory specified, <target>-sync-bundle.zip is created in specified directory.
                    f = new File(f, getDefaultBundleName());
                }
            } else {
                //No operand specified, <target>-sync-bundle.zip is created in install-root/domains/domain_name/sync
                f = getDefaultBundle();
            }

            if (f.getParentFile() != null && !f.getParentFile().exists()) {
                if (!f.getParentFile().mkdirs()) {
                    setError(Strings.get("export.sync.bundle.createDirFailed", f.getParentFile().getPath()));
                    return false;
                }
            }
            syncBundleExport = SmartFile.sanitize(f);
        }
        return true;
    }

    private void setError(String msg) {
        report.setActionExitCode(ExitCode.FAILURE);
        report.setMessage(msg);
    }

    private boolean hasError() {
        return report.getActionExitCode() != ExitCode.SUCCESS;
    }
    @Inject @Optional
    private Servers servers;
    @Inject @Optional
    private Clusters clusters;
    @Inject
    private ServerSynchronizer serverSynchronizer;
    @Inject
    private ServerEnvironment env;
    private ActionReport report = null;
    private File syncBundleExport;
    private Logger logger = null;
    private Payload.Outbound payload = null;
    private Server instance;
    private Cluster cluster;
    private SyncRequest syncRequest = new SyncRequest();
    private static final String[] ALL_DIRS = new String[]{
        "config", "applications", "lib", "docroot", "config-specific"
    };
}
