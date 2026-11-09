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

package org.glassfish.admin.restconnector;

import org.glassfish.api.admin.config.ConfigExtension;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;

/**
 * RestConfig configuration. This defines a rest-config element.
 *
 * @author Ludovic Champenois
 *
 */
@Configured
public interface RestConfig extends ConfigExtension {

    String DEBUG = "debug";
    String INDENTLEVEL = "indentLevel";
    String SHOWHIDDENCOMMANDS = "showHiddenCommands";
    String WADLGENERATION = "wadlGeneration";
    String LOGRESPONSES = "logOutput";
    String LOGINPUTS = "logInput";
    String SHOWDEPRECATEDITEMS = "showDeprecatedItems";
    String SESSIONTOKENTIMEOUT = "sessionTokenTimeout";

    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getDebug();

    void setDebug(String debugFlag);

    @Attribute(defaultValue = "-1", dataType = Integer.class)
    String getIndentLevel();

    void setIndentLevel(String indentLevel);

    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getWadlGeneration();

    void setWadlGeneration(String wadlGeneration);

    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getShowHiddenCommands();

    void setShowHiddenCommands(String showHiddenCommands);

    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getLogOutput();

    void setLogOutput(String logOutput);

    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getLogInput();

    void setLogInput(String logInput);

    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getShowDeprecatedItems();

    void setShowDeprecatedItems(String showDeprecatedItems);

    @Attribute(defaultValue = "30", dataType = Integer.class)
    String getSessionTokenTimeout();

    void setSessionTokenTimeout(String timeout);
}
