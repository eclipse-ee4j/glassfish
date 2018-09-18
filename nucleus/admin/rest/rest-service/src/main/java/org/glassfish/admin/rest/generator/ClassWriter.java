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

package org.glassfish.admin.rest.generator;

import java.util.List;

/**
 * @author Mitesh Meswani
 */
public interface ClassWriter {

    void createGetCommandResourcePaths(List<CommandResourceMetaData> commandMetaData);

    void createGetCommandResource(String commandResourceClassName, String resourcePath);

    void createCommandResourceConstructor(String commandResourceClassName, String commandName, String httpMethod, boolean linkedToParent, CommandResourceMetaData.ParameterMetaData[] commandParams, String commandDisplayName, String commandAction);

    void createCustomResourceMapping(String resourceClassName, String mappingPath);

    /**
     * Called to end creation of class being written by this writer
     */
    void done();

    void createGetDeleteCommand(String commandName);

    void createGetPostCommand(String commandName);

    void createGetChildResource(String path, String childResourceClassName);

    void createGetChildResourceForListResources(String keyAttributeName, String childResourceClassName);

    void createGetPostCommandForCollectionLeafResource(String postCommandName);

    void createGetDeleteCommandForCollectionLeafResource(String postCommandName);

    void createGetDisplayNameForCollectionLeafResource(String displayName);
}
