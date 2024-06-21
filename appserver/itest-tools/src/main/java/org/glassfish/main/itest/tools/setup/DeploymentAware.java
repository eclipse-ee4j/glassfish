/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.glassfish.main.itest.tools.setup;

import static java.lang.System.Logger.Level.INFO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Adds some helper methods as default interface methods related to creating deployments in tests.
 * @author Ondro Mihalyi
 */
public interface DeploymentAware {

    System.Logger getLogger();

    default File createDeploymentWar(WebArchive webArchive, String archiveFileBase) throws IOException {
        getLogger().log(INFO, webArchive.toString(true));

        File tmpDir = Files.createTempDirectory(archiveFileBase).toFile();
        File warFile = new File(tmpDir, archiveFileBase + ".war");
        webArchive.as(ZipExporter.class).exportTo(warFile, true);
        tmpDir.deleteOnExit();
        return warFile;
    }
}
