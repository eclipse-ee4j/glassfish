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

package org.glassfish.admin.rest.generator.client;

import org.glassfish.api.admin.CommandModel;
import org.jvnet.hk2.config.ConfigModel;

/**
 *
 * @author jdlee
 */
public interface ClientClassWriter {

    void generateGetSegment(String tagName);

    void generateCommandMethod(String methodName, String httpMethod, String resourcePath, CommandModel cm);

    String generateMethodBody(CommandModel cm, String httpMethod, String resourcePath, boolean includeOptional, boolean needsMultiPart);

    void generateGettersAndSetters(String type, String methodName, String fieldName);

    void createGetChildResource(ConfigModel model, String elementName, String childResourceClassName);

    void generateCollectionLeafResourceGetter(String className);

    void generateRestLeafGetter(String className);

    void done();
}
