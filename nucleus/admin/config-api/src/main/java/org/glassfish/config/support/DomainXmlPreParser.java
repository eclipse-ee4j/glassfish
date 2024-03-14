/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.util.Utility;
import java.io.*;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.glassfish.config.support.Constants.*;

/**
 * It is incredibly complex and difficult to do "perfect-parsing" when the elements aren't in the right order. These 3
 * elements: clusters servers configs Need to be in that exact order. If they aren't in that order we MUST do a reparse
 * with the streaming parser. As of July 6,2010 the clusters ALWAYS appears after the other two so we have to do a
 * reparse 100% of the time anyways. Now (July 2010, GF 3.1)a new wrinkle has been added. Each instance wants to have
 * information about all other servers in its cluster. This makes the parsing with the old method so difficult and
 * complex that I came up with a new plan: ALWAYS parse twice. The first time through should be VERY fast because we are
 * skipping almost everything. I'm just picking out the minimal "look-ahead" info and saving it for the final parse.
 *
 * @author Byron Nevins
 */
class DomainXmlPreParser {

    static class DomainXmlPreParserException extends Exception {

        DomainXmlPreParserException(Throwable t) {
            super(t);
        }

        private DomainXmlPreParserException(String s) {
            super(s);
        }
    }

    DomainXmlPreParser(URL domainXml, XMLInputFactory xif, String instanceNameIn) throws DomainXmlPreParserException {
        if (domainXml == null || xif == null || !StringUtils.ok(instanceNameIn))
            throw new IllegalArgumentException();

        instanceName = instanceNameIn;
        try (InputStream in = domainXml.openStream();
                InputStreamReader isr = new InputStreamReader(in, Charset.defaultCharset().toString())) {
            reader = xif.createXMLStreamReader(domainXml.toExternalForm(), isr);
            parse();
            postProcess();
            validate();
        } catch (DomainXmlPreParserException e) {
            throw e;
        } catch (Exception e2) {
            throw new DomainXmlPreParserException(e2);
        } finally {
            cleanup();
        }
    }

    final String getClusterName() {
        if (!valid)
            return null;

        return cluster.name;
    }

    final List<String> getServerNames() {
        if (!valid)
            return null;

        return cluster.serverRefs;
    }

    final String getConfigName() {
        if (!valid)
            return null;

        return cluster.configRef;
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////   Everything below here is private   //////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    private void parse() throws XMLStreamException {
        while (reader.hasNext()) {
            if (reader.next() == START_ELEMENT) {
                handleElement();
            }
        }
    }

    private void cleanup() {
        // this code is so ugly that it lives here!!
        try {
            if (reader != null)
                reader.close();
            reader = null;
        } catch (Exception ex) {
            // ignore!
        }
    }

    private void postProcess() {
        // our instance is in either zero or one cluster.  Find it and set it.
        for (ClusterData cd : clusters) {
            for (String serverName : cd.serverRefs) {
                if (instanceName.equals(serverName)) {
                    cluster = cd;
                    return;
                }
            }
        }
        // if we get here that means the instance either
        // does not exist or it is stand-alone
        cluster = new ClusterData();
        cluster.configRef = serverConfigRef;
        cluster.serverRefs.add(instanceName);
    }

    private void validate() throws DomainXmlPreParserException {
        // 1. confirm that the server was located
        if (serverConfigRef == null)
            throw new DomainXmlPreParserException(Strings.get("dxpp.serverNotFound", instanceName));
        // 2. config-ref of server matches config-ref of cluster
        if (!serverConfigRef.equals(cluster.configRef))
            throw new DomainXmlPreParserException(Strings.get("dxpp.configrefnotmatch", instanceName, cluster.name));

        if (!configNames.contains(serverConfigRef))
            throw new DomainXmlPreParserException(Strings.get("dxpp.confignotfound", instanceName, serverConfigRef));

        valid = true;
    }

    private void handleElement() throws XMLStreamException {
        String name = reader.getLocalName();

        if (!StringUtils.ok(name))
            return;

        if (name.equals(SERVERS)) {
            handleServers();
        } else if (name.equals(CLUSTERS)) {
            handleClusters();
        } else if (name.equals(CONFIGS)) {
            handleConfigs();
        }
    }

    private void handleServers() throws XMLStreamException {
        // we are pointed at the servers element
        printf("FOUND SERVERS");

        while (skipToStartButNotPast(SERVER, SERVERS)) {
            handleServer();
        }
    }

    private void handleServer() {
        String name = getName();
        String configRef = getConfigRef();

        printf("SERVER: " + name + ", ref= " + configRef);

        if (instanceName.equals(name))
            serverConfigRef = configRef;
    }

    private void handleClusters() throws XMLStreamException {
        // we are pointed at the servers element
        printf("FOUND CLUSTERS");

        while (skipToStartButNotPast(CLUSTER, CLUSTERS)) {
            handleCluster();
        }
    }

    private void handleCluster() throws XMLStreamException {
        ClusterData cd = new ClusterData();
        cd.name = getName();
        cd.configRef = getConfigRef();
        handleServerRefs(cd);
        clusters.add(cd);
        printf(cd.toString());
    }

    private void handleServerRefs(ClusterData cd) throws XMLStreamException {

        while (skipToStartButNotPast(SERVER_REF, CLUSTER)) {
            cd.serverRefs.add(reader.getAttributeValue(null, REF));
        }
    }

    private void handleConfigs() throws XMLStreamException {
        // we are pointed at the configs element
        printf("FOUND CONFIGS");

        while (skipToStartButNotPast(CONFIG, CONFIGS)) {
            handleConfig();
        }
    }

    private void handleConfig() {
        String name = reader.getAttributeValue(null, NAME);
        printf("CONFIG: " + name);
        configNames.add(name);
    }

    private boolean skipToStartButNotPast(String startName, String stopName) throws XMLStreamException {
        if (!StringUtils.ok(startName) || !StringUtils.ok(stopName))
            throw new IllegalArgumentException();

        while (reader.hasNext()) {
            reader.next();
            // getLocalName() will throw an exception in many states.  Be careful!!
            if (reader.isStartElement() && startName.equals(reader.getLocalName()))
                return true;
            if (reader.isEndElement() && stopName.equals(reader.getLocalName()))
                return false;
        }
        return false;
    }

    private String getName() {
        return reader.getAttributeValue(null, NAME);
    }

    private String getConfigRef() {
        return reader.getAttributeValue(null, CONFIG_REF);
    }

    private static void printf(String s) {
        if (debug)
            System.out.println(s);
    }

    private XMLStreamReader reader;
    private List<ClusterData> clusters = new LinkedList<ClusterData>();
    private List<String> configNames = new LinkedList<String>();
    private ClusterData cluster;
    private String instanceName;
    private String serverConfigRef;
    private boolean valid = false;
    private final static boolean debug = Boolean.parseBoolean(Utility.getEnvOrProp("AS_DEBUG"));

    private static class ClusterData {

        String name;
        String configRef;
        List<String> serverRefs = new ArrayList<String>();

        @Override
        public String toString() {
            return "Cluster:name=" + name + ", config-ref=" + configRef + ", server-refs = " + serverRefs;
        }
    }
}
