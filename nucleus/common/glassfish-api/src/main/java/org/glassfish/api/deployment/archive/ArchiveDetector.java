/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.deployment.archive;

import jakarta.inject.Singleton;

import java.io.IOException;

import org.jvnet.hk2.annotations.Contract;

/**
 * {@link ArchiveHandler}s are considered part of container implementation, hence are not available until the
 * corresponding container is setup. On the other hand, ArchiveDetectors are pretty light weight and used for selecting
 * the appropriate ArchiveHandler. ArchiveDetectors are supposed to be part of the connector module of a container. Each
 * detector has a rank as returned by {@link #rank()} which can be used to order the detectors as archive detection.
 *
 * <p/>
 * This is a container pluggability interface.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Contract
@Singleton
public interface ArchiveDetector {
    // TODO(Sahoo): Should we merge handle & getArchiveHandler methods into one method?

    /**
     * Since archive detection logic is typically executed at a very early stage of deployment, it is mainly heuristic. So
     * some detectors can incorrectly recognize archives that they actually don't support. e.g., take a war file inside an
     * ear file. and asssume that the war file contains some .jsp files. The archive detector responsible for handling the
     * war file could be fooled into thinking the ear file is a war file since it contains jsp files, yet in reality, it
     * only owns one of the sub archive bundled inside the composite ear file. To deal with such situations, each detector
     * can specify a rank which can be used to order the detectors. Since detectors can come from separate authors, rank of
     * a detector must be configurable in an installation.
     *
     * The order in which detectors are used during archive detection is based on the rank. Lower the integer value as
     * returned by this method, earlier it is used during detection.
     *
     * @return the rank of this detector
     */
    int rank();

    /**
     * This method is used to detect the archive type. If this detector can recognize the given archive, then it must return
     * true.
     *
     *
     * @param archive
     * @return
     * @throws IOException
     */
    boolean handles(ReadableArchive archive) throws IOException;

    /**
     * Return a ArchiveHandler that can handle the archive recognised by this ArchiveDetector.
     *
     * @return
     */
    ArchiveHandler getArchiveHandler();

    /**
     * Returns the type of the deployment unit or archive or module whichever way you want to call what's being depoyed.
     * Each archive handler is responsible for only one type of archive and the type of the archive is represented by
     * {@link ArchiveType}.
     *
     * @return the type of the archive or deployment unit that can be detected by this detector
     */
    ArchiveType getArchiveType();
}
