/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.v3.admin;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.util.Map;
import java.util.Properties;

import org.glassfish.admin.payload.PayloadFilesManager;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.Payload;
import org.jvnet.hk2.component.MultiMap;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Encapsulates handling of files uploaded to the server in the payload
 * of the incoming HTTP request.
 * <p>
 * Extracts any such files from the payload into a temporary directory
 * under the domain's applications directory.  (Putting them there allows
 * the deployment processing to rename the uploaded archive to another location
 * under the applications directory, rather than having to copy them.)
 */
class UploadedFilesManager {
    private static final Logger LOG = System.getLogger(UploadedFilesManager.class.getName());

    private final ActionReport report;
    /**
     * maps option names as sent with each uploaded file to the corresponding
     * extracted files
     */
    private MultiMap<String, File> optionNameToFileMap;

    /*
     * PFM needs to be a field so it is not gc-ed before the
     * UploadedFilesManager is closed.
     */
    private PayloadFilesManager.Temp payloadFilesMgr = null;

    UploadedFilesManager(final ActionReport report, final Payload.Inbound inboundPayload, String applicationRoot)
        throws IOException, Exception {
        this.report = report;
        extractFiles(inboundPayload, applicationRoot);
    }


    MultiMap<String, File> optionNameToFileMap() {
        return optionNameToFileMap;
    }


    public void close() {
        if (payloadFilesMgr != null) {
            payloadFilesMgr.cleanup();
        }
    }


    private void extractFiles(final Payload.Inbound inboundPayload, String applicationRoot) throws Exception {
        if (inboundPayload == null) {
            return;
        }

        final File uniqueSubdirUnderApplications = chooseTempDirParent(applicationRoot);
        payloadFilesMgr = new PayloadFilesManager.Temp(uniqueSubdirUnderApplications, report);

        /*
         * Extract the files into the temp directory.
         */
        final Map<File, Properties> payloadFiles = payloadFilesMgr.processPartsExtended(inboundPayload);

        /*
         * Prepare the map of command options names to corresponding
         * uploaded files.
         */
        optionNameToFileMap = new MultiMap<>();
        for (Map.Entry<File, Properties> e : payloadFiles.entrySet()) {
            final String optionName = e.getValue().getProperty("data-request-name");
            if (optionName != null) {
                LOG.log(DEBUG, () -> "UploadedFilesManager: map " + optionName + " to " + e.getKey());
                optionNameToFileMap.add(optionName, e.getKey());
            }
        }
    }


    private File chooseTempDirParent(String applicationRoot) throws IOException {
        final File appRoot = new File(applicationRoot);

        /*
         * Apparently during embedded runs the applications directory
         * might not be present already. Create it if needed.
         */
        if (!appRoot.isDirectory()) {
            if (!appRoot.exists() && !appRoot.mkdirs()) {
                throw new IOException(
                    "Could not create the directory " + appRoot + "; no further information is available.");
            }
        }

        return appRoot;
    }
}
