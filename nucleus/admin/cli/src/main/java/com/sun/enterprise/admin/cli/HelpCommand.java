/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.cli;

import jakarta.inject.Inject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * The help command will display the help text for all the commands and their options
 */
@Service(name = "help")
@PerLookup
public class HelpCommand extends CLICommand {
    @Inject
    private ServiceLocator habitat;

    private static final int DEFAULT_PAGE_LENGTH = 50;
    private static final int NO_PAGE_LENGTH = -1;
    private static final String DEFAULT_HELP_PAGE = "help";

    private static final LocalStringsImpl strings = new LocalStringsImpl(HelpCommand.class);

    @Param(name = "command-name", primary = true, optional = true)
    private String cmd;

    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {
        try {
            new More(getPageLength(), getSource(), getDestination(), getUserInput(), getUserOutput(), getQuitChar(), getPrompt());
        } catch (IOException ioe) {
            throw new CommandException(ioe);
        }
        return 0;
    }

    private String getCommandName() {
        return cmd != null ? cmd : DEFAULT_HELP_PAGE;
    }

    private Writer getDestination() {
        return new OutputStreamWriter(System.out, Charset.defaultCharset());
    }

    private int getPageLength() {
        if (programOpts.isInteractive()) {
            return DEFAULT_PAGE_LENGTH;
        }
        return NO_PAGE_LENGTH;
    }

    private String getPrompt() {
        return strings.get("ManpagePrompt");
    }

    private String getQuitChar() {
        return strings.get("ManpageQuit");
    }

    private Reader getSource() throws CommandException, CommandValidationException {
        CLICommand command = CLICommand.getCommand(habitat, getCommandName());
        Reader r = command.getManPage();
        if (r == null) {
            throw new CommandException(strings.get("ManpageMissing", getCommandName()));
        }
        return expandManPage(r);
    }

    private Reader getUserInput() {
        return new InputStreamReader(System.in, Charset.defaultCharset());
    }

    private Writer getUserOutput() {
        return new OutputStreamWriter(System.err, Charset.defaultCharset());
    }
}
