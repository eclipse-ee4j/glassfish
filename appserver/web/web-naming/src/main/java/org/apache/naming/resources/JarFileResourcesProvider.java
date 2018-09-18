/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.apache.naming.resources;

import java.util.jar.JarFile;

/**
 * This interface provides a list of jars to search resources.
 * It is an internal contract between resources and WebappClassLoader.
 * This should "not" be used for other purposes.
 *
 * @author Shing Wai Chan
 */
public interface JarFileResourcesProvider {
    public JarFile[] getJarFiles();
}
