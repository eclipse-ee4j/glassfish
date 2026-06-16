/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout.template;

import java.io.IOException;

/**
 * <p>
 * This interface provides a way to process "custom" parser commands. These commands are in the format: "&lt;![custom
 * command name] ...". They must be registered with the TemplateParser to be recognized. See
 * {@link TemplateReader#setCustomParserCommand(String, CustomParserCommand)}.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public interface CustomParserCommand {

    /**
     * <p>
     * This method processes a "custom" command. These are commands that start with a !. When this method receives control,
     * the <code>name</code> (i.e. the token after the '!' character) has already been read. It is passed via the
     * <code>name</code> parameter.
     * </p>
     *
     * <p>
     * The {@link ProcessingContext} and {@link ProcessingContextEnvironment} are both available.
     * </p>
     */
    void process(ProcessingContext ctx, ProcessingContextEnvironment env, String name) throws IOException;
}
