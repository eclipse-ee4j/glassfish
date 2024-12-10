/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.config.support;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.StreamReaderDelegate;

/**
 * {@link XMLStreamReader} wrapper that cuts off sub-trees.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class XMLStreamReaderFilter extends StreamReaderDelegate implements AutoCloseable {
    XMLStreamReaderFilter(XMLStreamReader reader) {
        super(reader);
    }

    XMLStreamReaderFilter() {
    }

    public int next() throws XMLStreamException {
        while (true) {
            int r = super.next();
            if (r != START_ELEMENT || !filterOut())
                return r;
            skipTree();
        }
    }

    public int nextTag() throws XMLStreamException {
        while (true) {
            // Fix for issue 9127
            // The following call to super.nextTag() is replaced with thisNextTag()
            int r = thisNextTag();
            if (r != START_ELEMENT || !filterOut())
                return r;
            skipTree();
        }
    }

    // Fix for issue 9127
    // This method is a modified version of the super.nextTag()
    // In addition to all other event types skipped in super.nextTag() in search for
    // START_ELEMENT this method also includes DTD eventType in the skip-list
    private int thisNextTag() throws XMLStreamException {
        int eventType = super.next();
        while ((eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) // skip whitespace
                || (eventType == XMLStreamConstants.CDATA && isWhiteSpace())
                // skip whitespace
                || eventType == XMLStreamConstants.SPACE || eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                || eventType == XMLStreamConstants.COMMENT || eventType == XMLStreamConstants.DTD) {
            eventType = super.next();
        }

        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException("found: " + getEventTypeString(eventType) + ", expected "
                    + getEventTypeString(XMLStreamConstants.START_ELEMENT) + " or " + getEventTypeString(XMLStreamConstants.END_ELEMENT));
        }

        return eventType;

    }

    final static String getEventTypeString(int eventType) {
        switch (eventType) {
        case XMLEvent.START_ELEMENT:
            return "START_ELEMENT";
        case XMLEvent.END_ELEMENT:
            return "END_ELEMENT";
        case XMLEvent.PROCESSING_INSTRUCTION:
            return "PROCESSING_INSTRUCTION";
        case XMLEvent.CHARACTERS:
            return "CHARACTERS";
        case XMLEvent.COMMENT:
            return "COMMENT";
        case XMLEvent.START_DOCUMENT:
            return "START_DOCUMENT";
        case XMLEvent.END_DOCUMENT:
            return "END_DOCUMENT";
        case XMLEvent.ENTITY_REFERENCE:
            return "ENTITY_REFERENCE";
        case XMLEvent.ATTRIBUTE:
            return "ATTRIBUTE";
        case XMLEvent.DTD:
            return "DTD";
        case XMLEvent.CDATA:
            return "CDATA";
        case XMLEvent.SPACE:
            return "SPACE";
        }
        return "UNKNOWN_EVENT_TYPE, " + String.valueOf(eventType);
    }

    /**
     * Skips a whole subtree, and return with the cursor pointing to the end element of the skipped subtree.
     */
    private void skipTree() throws XMLStreamException {
        int depth = 1;

        while (depth > 0) {
            // nextTag may cause problems.  We are just throwing it all away so
            // next() is fine...
            int r = super.next();

            if (r == START_ELEMENT) {
                depth++;
            } else if (r == END_ELEMENT) {
                depth--;
            }
            // else ignore everything else...
        }
    }

    /**
     * Called when the parser is at the start element state, to decide if we are to skip the current element or not.
     */
    abstract boolean filterOut() throws XMLStreamException;
}
