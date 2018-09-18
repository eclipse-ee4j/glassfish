/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.test.util.results;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
public class ResultsProducer {
    private XMLStreamReader parser;
    private int count = 0;
    private int pass = 0;
    private int fail = 0;
    private int didNotRun = 0;
    private boolean done = false;
    private StringBuilder buffer;
    private File input;

    public ResultsProducer(String inputFile) throws XMLStreamException, FileNotFoundException {
        input = new File(inputFile);
        parser = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(input));
    }

    private void produce() throws XMLStreamException, IOException {
        buffer = new StringBuilder();
        while (hasNext()) {
            readTestCase();
        }
        parser.close();
        line();
        format("PASSED", pass);
        format("FAILED", fail);
        format("DID NOT RUN", didNotRun);
        format("TOTAL", count);
        line();
        System.out.println(buffer);
        FileWriter writer = new FileWriter(new File(input.getParentFile(), "count.txt"));
        writer.write(buffer.toString());
        writer.flush();
        writer.close();
        if (fail != 0) {
            System.err.println("All Tests NOT passed, so returning UNSUCCESS status.");
            System.exit(1);
        }

    }

    private void line() {
        buffer.append("**********************\n");
    }

    private void format(final String result, final int count) {
        buffer.append(String.format("* %-12s %5d *\n", result, count));
    }

    private void readTestCase() throws XMLStreamException {
        skipNonStartElements();
        if (hasNext()) {
            if ("testcase".equals(parser.getLocalName())) {
                process(new TestCase(read("name"),
                    read("status", "value")));
            } else {
                next();
                readTestCase();
            }
        }
    }

    private void process(final TestCase test) {
        count++;
        if(ReporterConstants.PASS.equals(test.getStatus())) {
            pass++;
        } else if(ReporterConstants.FAIL.equals(test.getStatus())) {
            fail++;
        } else if(ReporterConstants.DID_NOT_RUN.equals(test.getStatus())) {
            didNotRun++;
        }
    }

    private String read(final String name) throws XMLStreamException {
        skipTo(name);
        return hasNext() ? parser.getElementText().trim() : null;
    }

    private String read(final String name, final String attr) throws XMLStreamException {
        skipTo(name);
        return hasNext() ? parser.getAttributeValue(null, attr).trim() : null;
    }

    private void skipTo(String name) throws XMLStreamException {
        while (hasNext() && !name.equals(parser.getLocalName())) {
            skipNonStartElements();
        }
    }

    private void skipNonStartElements() throws XMLStreamException {
        while (hasNext() && next() != XMLStreamConstants.START_ELEMENT) {
        }
    }

    private boolean hasNext() {
        return !done && parser.getLocation().getLineNumber() >= 0;
    }

    private int next() throws XMLStreamException {
        final int event = parser.next();
        if (event == XMLStreamConstants.END_DOCUMENT) {
            parser.close();
            done = true;
        }
        return event;
    }

    public static void main(String[] args) throws XMLStreamException, IOException {
        if (args.length < 1) {
            System.err.println("Please specify the input file name");
            return;
        }
        final ResultsProducer producer = new ResultsProducer(args[0]);
        producer.produce();
    }
}
