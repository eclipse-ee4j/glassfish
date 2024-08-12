/*
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

import com.sun.enterprise.util.StringUtils;

import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.glassfish.config.support.DomainXmlPreParser.DomainXmlPreParserException;

import static org.glassfish.config.support.Constants.CLUSTER;
import static org.glassfish.config.support.Constants.CONFIG;
import static org.glassfish.config.support.Constants.NAME;
import static org.glassfish.config.support.Constants.SERVER;

/**
 * The "pre-parser" goes through the domain.xml and finds all the 'special' elements that should be retained. This
 * filter class just checks the elements against those lists...
 *
 * @author Byron Nevins
 */
class InstanceReaderFilter extends ServerReaderFilter {

    InstanceReaderFilter(String theServerName, URL theDomainXml, XMLInputFactory theXif)
            throws XMLStreamException, DomainXmlPreParserException {

        super(theDomainXml, theXif);
        instanceName = theServerName;
        dxpp = new DomainXmlPreParser(domainXml, xif, instanceName);
    }

    /**
     * This method is called for every element. We are very interested in server, config and cluster. We will only filter
     * out config and server and cluster elements never other elements We use this as a handy hook to get info about other
     * elements -- which really is a side-effect.
     *
     * @return true to NOT parse this sub-tree
     * @throws XMLStreamException
     */
    @Override
    final boolean filterOut() throws XMLStreamException {
        try {
            XMLStreamReader reader = getParent();
            String elementName = reader.getLocalName();

            if (!StringUtils.ok(elementName))
                return true; // famous last words:  "this can never happen" ;-)

            // possibly filter out from these 3 kinds of elements
            if (elementName.equals(SERVER))
                return handleServer(reader);

            if (elementName.equals(CONFIG))
                return handleConfig(reader);

            if (elementName.equals(CLUSTER))
                return handleCluster(reader);

            // keep everything else
            return false;
        } catch (Exception e) {
            // I don't trust the XML parser code in the JDK -- it likes to throw
            // unchecked exceptions!!
            throw new XMLStreamException(Strings.get("InstanceReaderFilter.UnknownException", e.toString()), e);
        }
    }

    @Override
    final String configWasFound() {
        // preparser already threw an exception if the config wasn't found
        return null;
    }

    /**
     * @return true if we want to filter out this server element
     */
    private boolean handleServer(XMLStreamReader r) {
        String name = r.getAttributeValue(null, NAME);

        if (StringUtils.ok(name) && dxpp.getServerNames().contains(name))
            return false;

        return true;
    }

    /**
     * @return true if we want to filter out this config element
     */
    private boolean handleConfig(XMLStreamReader reader) {
        String name = reader.getAttributeValue(null, NAME);

        if (dxpp.getConfigName().equals(name))
            return false;

        return true;
    }

    /**
     * Note that dxpp.getClusterName() will definitely return null for stand-alone instances. This is normal.
     *
     * @return true if we want to filter out this cluster element
     */
    private boolean handleCluster(XMLStreamReader reader) {
        String name = reader.getAttributeValue(null, NAME);
        String myCluster = dxpp.getClusterName();

        if (StringUtils.ok(myCluster) && myCluster.equals(name))
            return false;

        return true;
    }

    private final DomainXmlPreParser dxpp;
    private final String instanceName;
}
