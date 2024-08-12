/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.modularity.parser;

import com.sun.enterprise.config.util.ConfigApiLoggerInfo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDocument;

/**
 * populate the a DomDocument from the given configuration snippet file containing a config bean configuration.
 *
 * @author Bhakti Mehta
 * @author Masoud Kalali
 */
public class ConfigurationPopulator {

    private final static Logger LOG = ConfigApiLoggerInfo.getLogger();
    private final DomDocument doc;
    private final ConfigBeanProxy parent;
    private final String xmlContent;

    public ConfigurationPopulator(String xmlContent, DomDocument doc, ConfigBeanProxy parent) {
        this.xmlContent = xmlContent;
        this.doc = doc;
        this.parent = parent;
    }

    public void run(ConfigParser parser) {
        try {
            InputStream is = new ByteArrayInputStream(xmlContent.getBytes());
            XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(is, "utf-8");
            parser.parse(reader, doc, Dom.unwrap((ConfigBeanProxy) parent));
        } catch (XMLStreamException e) {
            LOG.log(Level.SEVERE, ConfigApiLoggerInfo.DEFAULT_CFG_READ_FAILED, e);
        }
    }
}
