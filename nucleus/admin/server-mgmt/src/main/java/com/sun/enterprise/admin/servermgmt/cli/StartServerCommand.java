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

package com.sun.enterprise.admin.servermgmt.cli;

import com.sun.enterprise.admin.launcher.GFLauncherException;
import com.sun.enterprise.universal.xml.MiniXmlParserException;
import java.util.List;
import org.glassfish.api.admin.RuntimeType;

/**
 * @author bnevins
 */
public interface StartServerCommand {

    /**
     * @return the type of "this" server
     */
    RuntimeType getType();

    /**
     * Create a launcher for the whatever type of server "we" are.
     */
    void createLauncher() throws GFLauncherException, MiniXmlParserException;

    List<String> getLauncherArgs();
}
