/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.readers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Rajeshwar Patil
 */
public class XmlInputObject extends InputObject {

    /** The Character '&'. */
    public static final Character AMP = Character.valueOf('&');

    /** The Character '''. */
    public static final Character APOS = Character.valueOf('\'');

    /** The Character '!'. */
    public static final Character BANG = Character.valueOf('!');

    /** The Character '='. */
    public static final Character EQ = Character.valueOf('=');

    /** The Character '>'. */
    public static final Character GT = Character.valueOf('>');

    /** The Character '<'. */
    public static final Character LT = Character.valueOf('<');

    /** The Character '?'. */
    public static final Character QUEST = Character.valueOf('?');

    /** The Character '"'. */
    public static final Character QUOT = Character.valueOf('"');

    /** The Character '/'. */
    public static final Character SLASH = Character.valueOf('/');

    private final XmlInputReader xmlReader;

    /**
     * Construct a XmlInputObjectfrom a input stream.
     *
     * @param inputstream an input stream
     * @exception InputException If there is a syntax error in the source input stream or a duplicate key.
     */
    public XmlInputObject(InputStream inputstream) throws InputException, IOException {
        this(new String(inputstream.readAllBytes(), UTF_8));
    }

    /**
     * Construct a XmlInputObjectfrom a XML text string.
     *
     * @param source A XML text string
     * @exception InputException If there is a syntax error in the source string or a duplicated key.
     */
    public XmlInputObject(String source) throws InputException {
        this(new XmlInputReader(source));
    }

    public XmlInputObject(XmlInputReader xmlReader) throws InputException {
        this.xmlReader = xmlReader;
        map = new HashMap();
    }

    /**
     * Construct and returns a map of input key-value pairs
     *
     * @throws InputException If there is a syntax error in the source string or a duplicated key.
     */
    @Override
    public Map initializeMap() throws InputException {
        while (xmlReader.more() && xmlReader.skipPast("<")) {
            parse(xmlReader, this, null);
        }
        return map;
    }

    public Map getMap() throws InputException {
        return map;
    }

    /**
     * Scan the content following the named tag, attaching it to the context.
     *
     * @param x The XmlInputReader containing the source string.
     * @param context The XmlInputObject that will include the new material.
     * @param name The tag name.
     * @return true if the close tag is processed.
     * @throws InputException
     */
    private static boolean parse(XmlInputReader reader, XmlInputObject context, String name) throws InputException {
        char character;
        int i;
        String n;
        XmlInputObject subContext;
        String string;
        Object token;

        // Test for and skip past these forms:
        // <!-- ... -->   <!   ...   >   <![  ... ]]>   <?   ...  ?>
        // Report errors for these forms:   <>   <=   <<

        token = reader.nextToken();

        // <!
        if (BANG.equals(token)) {
            character = reader.next();
            if (character == '-') {
                if (reader.next() == '-') {
                    reader.skipPast("-->");
                    return false;
                }
                reader.back();
            } else if (character == '[') {
                token = reader.nextToken();
                if ("CDATA".equals(token)) {
                    if (reader.next() == '[') {
                        string = reader.nextCDATA();
                        if (string.length() > 0) {
                            context.put("content", string);
                        }
                        return false;
                    }
                }
                throw reader.error("Expected 'CDATA['");
            }

            i = 1;
            do {
                token = reader.nextMeta();
                if (token == null) {
                    throw reader.error("Missing '>' after '<!'.");
                } else if (LT.equals(token)) {
                    i += 1;
                } else if (GT.equals(token)) {
                    i -= 1;
                }
            } while (i > 0);
            return false;
        } else if (QUEST.equals(token)) {
            // <?
            reader.skipPast("?>");
            return false;
        } else if (SLASH.equals(token)) {
            // Close tag </
            token = reader.nextToken();
            if (name == null) {
                throw reader.error("Mismatched close tag " + token);
            }
            if (!name.equals(token)) {
                throw reader.error("Mismatched " + name + " and " + token);
            }
            if (!GT.equals(reader.nextToken())) {
                throw reader.error("Misshaped close tag");
            }
            return true;

        } else if (token instanceof Character) {
            throw reader.error("Misshaped tag");

            // Open tag <
        } else {
            n = (String) token;
            token = null;
            subContext = new XmlInputObject(reader);
            for (;;) {
                if (token == null) {
                    token = reader.nextToken();
                }

                // attribute = value
                if (token instanceof String) {
                    string = (String) token;
                    token = reader.nextToken();
                    if (EQ.equals(token)) {
                        token = reader.nextToken();
                        if (!(token instanceof String)) {
                            throw reader.error("Missing value");
                        }
                        subContext.put(string, InputObject.stringToValue((String) token));
                        token = null;
                    } else {
                        subContext.put(string, "");
                    }

                    // Empty tag <.../>
                } else if (SLASH.equals(token)) {
                    if (!reader.nextToken().equals(GT)) {
                        throw reader.error("Misshaped tag");
                    }
                    context.putMap(n, subContext.getMap());
                    return false;

                    // Content, between <...> and </...>
                } else if (GT.equals(token)) {
                    for (;;) {
                        token = reader.nextContent();
                        if (token == null) {
                            if (n != null) {
                                throw reader.error("Unclosed tag " + n);
                            }
                            return false;
                        } else if (token instanceof String) {
                            string = (String) token;
                            if (string.length() > 0) {
                                subContext.put("content", InputObject.stringToValue(string));
                            }

                            // Nested element
                        } else if (LT.equals(token)) {
                            if (parse(reader, subContext, n)) {
                                if (subContext.length() == 0) {
                                    context.put(n, "");
                                } else if (subContext.length() == 1 && subContext.get("content") != null) {
                                    context.put(n, subContext.get("content"));
                                } else {
                                    context.putMap(n, subContext.getMap());
                                }
                                return false;
                            }
                        }
                    }
                } else {
                    throw reader.error("Misshaped tag");
                }
            }
        }
    }
}
