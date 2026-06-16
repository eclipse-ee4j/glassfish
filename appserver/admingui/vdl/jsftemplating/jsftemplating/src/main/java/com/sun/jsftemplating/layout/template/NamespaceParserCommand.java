/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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
 * This {@link CustomParserCommand} handles "namespace" declarations. The format of this command should be:
 * </p>
 *
 * <ul>
 * <li>&lt;!namespace "longname"="shortname" /&gt;</li>
 * </ul>
 *
 * <p>
 * For example:
 * </p>
 *
 * <ul>
 * <li>&lt;!namespace "http://java.sun.com/mojarra/scales"="sc" /&gt;</li>
 * </ul>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class NamespaceParserCommand implements CustomParserCommand {

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public NamespaceParserCommand() {
    }

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
    @Override
    public void process(ProcessingContext ctx, ProcessingContextEnvironment env, String name) throws IOException {
        // Get the reader
        TemplateReader reader = env.getReader();
        TemplateParser parser = reader.getTemplateParser();
        parser.skipCommentsAndWhiteSpace(TemplateParser.SIMPLE_WHITE_SPACE);
        int ch = parser.nextChar();
        if (ch == '>' || ch == '/') {
            // Nothing specified, throw exception!
            throw new IllegalArgumentException("Found an empty \"<!namespace />\" delcaration!  The long and" + " short namespace names must be provided.");
        }
        parser.unread(ch);

        // Next get the next NVP...
        NameValuePair nvp = parser.getNVP(null, true);

        // Make sure we read the WS after the data...
        parser.skipCommentsAndWhiteSpace(TemplateParser.SIMPLE_WHITE_SPACE);

        // Now make sure this is an end tag...
        ch = parser.nextChar();
        int ch2 = parser.nextChar();
        if (ch != '/' || ch2 != '>') {
            throw new IllegalArgumentException("[<!namespace " + nvp.getName() + "=" + nvp.getValue() + (char) ch + (char) ch2 + "] does not end with \"/>\"!");
        }
        reader.popTag(); // Don't look for end tag

        // Save the mapping...
        reader.setNamespace(nvp.getName(), nvp.getValue().toString());
    }
}
