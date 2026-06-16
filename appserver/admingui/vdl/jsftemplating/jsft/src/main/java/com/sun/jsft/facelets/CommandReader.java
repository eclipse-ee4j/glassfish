/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright (c) 2011 Ken Paulsen
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

package com.sun.jsft.facelets;

import com.sun.jsft.event.Command;
import com.sun.jsft.event.ELCommand;
import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This class is responsible for reading in all the commands for the given String. The String typically is passed in
 * from the body content of event.
 * </p>
 *
 * @author Ken Paulsen (kenapaulsen@gmail.com)
 */
public class CommandReader {

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public CommandReader(String str) {
        this(new ByteArrayInputStream(("{" + CommandReader.unwrap(str) + "}").getBytes()));
    }

    /**
     * <p>
     * Constructor.
     * </p>
     *
     * @param stream The <code>InputStream</code> for the {@link Command}.
     */
    protected CommandReader(InputStream stream) {
        _parser = new CommandParser(stream);
    }

    /**
     * <p>
     * The read method uses the {@link CommandParser} to parses the template. It populates a {@link LayoutDefinition}
     * structure, which is returned.
     * </p>
     *
     * @return The {@link LayoutDefinition}
     *
     * @throws IOException
     */
    public List<Command> read() throws IOException {
        // Start...
        _parser.open();

        try {
            // Populate the LayoutDefinition from the Document
            return readCommandList();
        } finally {
            _parser.close();
        }
    }

    /**
     *
     */
// FIXME: Parenthesis are not well supported.  When convertKeywords is called,
// FIXME: it receivces "(foo...)".  First foo() is not recognized because of
// FIXME: the leading '('.  Second, there is a good possibility that other
// FIXME: keywords may exist in the middle of the string.  Need to rethink how
// FIXME: this conversion is done.
    private Command readCommand() throws IOException {
        // Skip White Space...
        _parser.skipCommentsAndWhiteSpace(CommandParser.SIMPLE_WHITE_SPACE);

        // Read the next Command
        String commandLine = _parser.readUntil(new int[] { ';', '{', '}' }, true);

        // Read the children
        int ch = _parser.nextChar();
        List<Command> commandChildren = null;
        if (ch == '{') {
            // Read the Command Children
            commandChildren = readCommandList();
        } else if (ch == '}') {
            _parser.unread(ch);
        }

        // Check to see if there is a variable to store the result...
        String variable = null;
        int idx = indexOf((byte) '=', commandLine);
        if (idx != -1) {
            // We have a result variable, store it separately...
            variable = commandLine.substring(0, idx).trim();
            commandLine = commandLine.substring(++idx).trim();
        }

        // If "if" handle "else" if present
        Command elseCommand = null;
        if (commandLine.startsWith("if")) {
            // First convert "if" to the real if handler...
            commandLine = "jsft._if" + commandLine.substring(2);

            // Check the next few characters to see if they are "else"...
            _parser.skipCommentsAndWhiteSpace(CommandParser.SIMPLE_WHITE_SPACE);
            int next[] = new int[] { _parser.nextChar(), _parser.nextChar(), _parser.nextChar(), _parser.nextChar(), _parser.nextChar() };
            if ((next[0] == 'e') && (next[1] == 'l') && (next[2] == 's') && (next[3] == 'e') && (Character.isWhitespace((char) next[4]))) {
                // This is an else case, parse it...
                elseCommand = readCommand();
            } else {
                // Not an else, restore the parser state
                for (idx = 4; idx > -1; idx--) {
                    if (next[idx] != -1) {
                        _parser.unread(next[idx]);
                    }
                }
            }
        }

        // Create the Command
        Command command = null;
        if ((commandLine.length() > 0) || (commandChildren != null)) {
            command = new ELCommand(variable, convertKeywords(commandLine), commandChildren, elseCommand);
        }

        // Return the LayoutElement
        return command;
    }

    /**
     * <p>
     * This method replaces keywords with the "real" syntax for developer convenience.
     * </p>
     */
    private String convertKeywords(String exp) {
        if (exp == null) {
            return null;
        }

        // Get the key to lookup
        String key = exp;
        int paren = exp.indexOf(OPEN_PAREN);
        if (paren != -1) {
            key = exp.substring(0, paren);
            if (key.indexOf(".") != -1) {
                // '.' found, this is not a keyword...
                return exp;
            }
        }
        key = key.trim();

        // Check for mapping...
        String value = _reservedMappings.get(key);
        if (value != null) {
            exp = value + exp.substring(key.length());
        }

        return exp;
    }

    /**
     * <p>
     * This method looks for the given <code>char</code> in the given <code>String</code>. It will not match any values that
     * are found within parenthesis or quotes.
     * </p>
     */
    private int indexOf(byte ch, String str) {
        byte[] bytes = str.getBytes();

        int idx = 0;
        int insideChar = -1;
        for (byte curr : bytes) {
            if (insideChar == -1) {
                // Not inside anything...
                if (ch == curr) {
                    break;
                } else if (('\'' == curr) || ('"' == curr)) {
                    insideChar = curr;
                } else if (OPEN_PAREN == curr) {
                    insideChar = CLOSE_PAREN;
                } else if (OPEN_BRACKET == curr) {
                    insideChar = CLOSE_BRACKET;
                }
            } else if (insideChar == curr) {
                // Was inside something, ending now...
                insideChar = -1;
            }
            idx++;
        }

        // If we found it return it, otherwise return -1
        if (idx >= bytes.length) {
            idx = -1;
        }
        return idx;
    }

    /**
     * <p>
     * This method reads Commands until a closing '}' is encountered.
     * </p>
     */
    private List<Command> readCommandList() throws IOException {
        int ch = _parser.nextChar();
        List<Command> commands = new ArrayList<>();
        Command command = null;
        while (ch != '}') {
            // Make sure readCommand gets the full command line...
            if (ch != '{') {
                // We want to throw this char away...
                _parser.unread(ch);
            }

            // Read a Command
            command = readCommand();
            if (command != null) {
                commands.add(command);
            }

            // Skip White Space...
            _parser.skipCommentsAndWhiteSpace(CommandParser.SIMPLE_WHITE_SPACE);

            // Get the next char...
            ch = _parser.nextChar();
            if (ch == -1) {
                throw new IOException("Unexpected end of stream! Expected to find '}'.");
            }
        }

        // Return the Commands
        return commands;
    }

    /**
     * <p>
     * This function removes the containing CDATA tags, if found.
     * </p>
     */
    private static String unwrap(String str) {
        str = str.trim();
        if (str.startsWith(OPEN_CDATA)) {
            int endingIdx = str.lastIndexOf(CLOSE_CDATA);
            if (endingIdx != -1) {
                // Remove the CDATA wrapper
                str = str.substring(OPEN_CDATA.length(), endingIdx);
            }
        }
        return str;
    }

    // Map to hold shortcut mappings (e.g. keywords)
    private static Map<String, String> _reservedMappings = new HashMap<>(16);
    static {
        _reservedMappings.put("foreach", "jsft.foreach");
        _reservedMappings.put("for", "jsft._for");
        _reservedMappings.put("println", "jsft.println");
        _reservedMappings.put("write", "jsft.write");
        _reservedMappings.put("setAttribute", "jsft.setAttribute");
        _reservedMappings.put("responseComplete", "jsft.responseComplete");
        _reservedMappings.put("renderResponse", "jsft.renderResponse");
        _reservedMappings.put("printStackTrace", "jsft.printStackTrace");
        _reservedMappings.put("getNanoTime", "jsft.getNanoTime");
    }

    private static final char OPEN_BRACKET = '[';
    private static final char CLOSE_BRACKET = ']';
    private static final char OPEN_PAREN = '(';
    private static final char CLOSE_PAREN = ')';
    private static final String OPEN_CDATA = "<![CDATA[";
    private static final String CLOSE_CDATA = "]]>";

    private CommandParser _parser = null;
}
