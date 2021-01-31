/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.jvnet.hk2.annotations.Contract;
import org.glassfish.api.deployment.DeploymentContext;

/**
 * Composite handlers are just like any other ArchiveHandler except they get a chance at looking at the archive before
 * the archive handlers do
 *
 * The main reason for this tag interface is that some archive handlers might be tricked into thinking a composite
 * archive is theirs when in fact they only own a part of it.
 *
 * For instance, take a war file inside an ear file. and asssume that the war file contains some .jsp files. The archive
 * handler responsible for handling the war file could be fooled into thinking the ear file is a war file since it
 * contains jsp files, yet in reality, it only owns one of the sub archive bundled inside the composite ear file.
 *
 * @author Jerome Dochez
 */
@Contract
public interface CompositeHandler extends ArchiveHandler {

    public boolean accept(ReadableArchive source, String entryName);

    public void initCompositeMetaData(DeploymentContext context);
}
