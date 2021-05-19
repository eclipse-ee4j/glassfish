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

package taglib;

import java.io.InputStream;
import java.io.IOException;

import jakarta.servlet.jsp.tagext.PageData;
import jakarta.servlet.jsp.tagext.TagLibraryValidator;
import jakarta.servlet.jsp.tagext.ValidationMessage;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MyValidator extends TagLibraryValidator {

    public ValidationMessage[] validate(String prefix, String uri,
                                        PageData page) {

        /*
        StringBuffer sb = new StringBuffer();

        sb.append("---------- Prefix=" + prefix + " URI=" + uri +
                  "----------\n");

        InputStream is = page.getInputStream();
        while (true) {
            try {
                int ch = is.read();
                if (ch < 0) {
                    break;
                }
                sb.append((char) ch);
            } catch (IOException e) {
                break;
            }
        }
        sb.append("-----------------------------------------------");
        System.out.println(sb.toString());
        */

        try {
            DefaultHandler h = new DefaultHandler();

            // parse the page
            SAXParserFactory f = SAXParserFactory.newInstance();
            f.setValidating(false);
            f.setNamespaceAware(true);
            SAXParser p = f.newSAXParser();
            p.parse(page.getInputStream(), h);

        } catch (SAXException ex) {
            return vmFromString(ex.toString());
        } catch (ParserConfigurationException ex) {
            return vmFromString(ex.toString());
        } catch (IOException ex) {
            return vmFromString(ex.toString());
        }

        return null;
    }

    // constructs a ValidationMessage[] from a single String and no ID
    private static ValidationMessage[] vmFromString(String message) {
        return new ValidationMessage[] {
            new ValidationMessage(null, message)
        };
    }

}
