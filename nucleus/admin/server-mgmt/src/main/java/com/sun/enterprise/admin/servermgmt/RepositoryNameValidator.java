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

package com.sun.enterprise.admin.servermgmt;

import com.sun.enterprise.util.i18n.StringManager;

import java.io.ByteArrayInputStream;

import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.InputSource;

/**
 * Validates the repository name. A repository name must be a - valid file name, - valid xml CDATA value & - valid
 * javax.management.ObjectName property value.
 */
public class RepositoryNameValidator extends StringValidator {
    private static final String VALID_CHAR = "[^\\,\\/ \\&\\;\\`\\'\\\\\"\\|\\*\\!\\?\\~\\<\\>\\^\\(\\)\\[\\]\\{\\}\\$\\:\\%]*";

    private static final String IAS_NAME = "com.sun.appserv:name=";

    private static final String XML_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <xml>";

    private static final String XML_2 = "</xml>";

    /**
     * i18n strings manager object
     */
    private static final StringManager strMgr = StringManager.getManager(RepositoryNameValidator.class);

    /**
     * Constructs new RepositoryNameValidator object.
     *
     * @param name
     */
    public RepositoryNameValidator(String name) {
        super(name);
    }

    /**
     * Validates the given value for the given entry. This method first invokes its superclass's validate method and then
     * performs additional validations.
     *
     * @throws InvalidConfigException
     */
    public void validate(Object str) throws InvalidConfigException {
        super.validate(str);
        checkValidName((String) str);
        checkValidXmlToken((String) str);
        checkValidObjectNameToken((String) str);
    }

    public void checkValidName(String name) throws InvalidConfigException {
        if (!name.matches(VALID_CHAR)) {
            throw new InvalidConfigException(strMgr.getString("validator.invalid_value", getName(), name));
        }
    }

    /**
     * Implementation copied from com.sun.enterprise.admin.verifier.tests.StaticTest
     */
    public void checkValidXmlToken(String name) throws InvalidConfigException {
        try {
            //Construct a valid xml string
            String xml = XML_1 + name + XML_2;
            ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
            InputSource is = new InputSource(bais);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.parse(is);
        } catch (Exception e) {
            throw new InvalidConfigException(strMgr.getString("validator.invalid_value", getName(), name));
        }
    }

    public void checkValidObjectNameToken(String name) throws InvalidConfigException {
        try {
            new ObjectName(IAS_NAME + name);
        } catch (Exception e) {
            throw new InvalidConfigException(strMgr.getString("validator.invalid_value", getName(), name));
        }
    }
}
