/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.appclient.client.acc.config.util;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.glassfish.appclient.client.acc.config.ClientContainer;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.junit.jupiter.api.Test;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author tjquinn
 */
public class XMLTest {

    private static final String[] SAMPLE_XML_PATH = {"/sun-acc.xml", "/glassfish-acc.xml"};

    private static final String FIRST_HOST = "glassfish.dev.java.net";
    private static final int FIRST_PORT = 3701;
    private static final String SECOND_HOST = "other.dev.java.net";
    private static final int SECOND_PORT = 4701;

    private static final String FIRST_PROP_NAME = "firstProp";
    private static final String FIRST_PROP_VALUE = "firstValue";

    private static final String SECOND_PROP_NAME = "secondProp";
    private static final String SECOND_PROP_VALUE = "secondValue";

    @Test
    public void testProps() throws Exception {
        for (String sampleXMLPath : SAMPLE_XML_PATH) {
            System.out.println("Testing with " + sampleXMLPath);
            ClientContainer cc = readConfig(sampleXMLPath);
            Properties props = XML.toProperties(cc.getProperty());
            assertEquals(FIRST_PROP_VALUE, props.getProperty(FIRST_PROP_NAME),
                "property value mismatch for first property from " + sampleXMLPath);
            assertEquals(SECOND_PROP_VALUE, props.getProperty(SECOND_PROP_NAME),
                "property value mismatch for second property from " + sampleXMLPath);
        }
    }

    @Test
    public void testReadSampleXML() throws Exception {
        for (String sampleXMLPath : SAMPLE_XML_PATH) {
            System.out.println("  Testing with " + sampleXMLPath);
            ClientContainer cc = readConfig(sampleXMLPath);
            List<TargetServer> servers = cc.getTargetServer();
            assertAll("target servers did not read correctly from " + sampleXMLPath,
                () -> assertEquals(FIRST_HOST, servers.get(0).getAddress()),
                () -> assertEquals(FIRST_PORT, servers.get(0).getPort()),
                () -> assertEquals(SECOND_HOST, servers.get(1).getAddress()),
                () -> assertEquals(SECOND_PORT, servers.get(1).getPort())
            );
        }
    }


    private static ClientContainer readConfig(final String configPath) throws Exception {
        assertNotNull(configPath, "cannot locate test file " + configPath);
        try (InputStream is = XMLTest.class.getResourceAsStream(configPath)) {
            JAXBContext jc = JAXBContext.newInstance(ClientContainer.class );
            Unmarshaller u = jc.createUnmarshaller();
            final SAXSource src = setUpToUseLocalDTDs(is);
            return (ClientContainer) u.unmarshal(src);
        }
    }


    private static SAXSource setUpToUseLocalDTDs(final InputStream is) throws Exception {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        SAXParser saxParser = parserFactory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        InputSource inSrc = new InputSource(is);
        xmlReader.setEntityResolver(new LocalEntityResolver());
        SAXSource saxSource = new SAXSource(xmlReader,inSrc);
        return saxSource;
    }

    /**
     * Resolves entity references against local files if possible.
     * <p>
     * This is here primarily to allow us to find the local copy of the
     * glassfish- or sun-application-client DTD without going out over the network.
     */
    private static class LocalEntityResolver implements EntityResolver {

        private enum ACC_INFO {
            SUN_ACC(
                "-//Sun Microsystems Inc.//DTD Application Server 8.0 Application Client Container//EN",
                "dtds/sun-application-client-container_1_2.dtd"),
            GLASSFISH_ACC(
                "-//GlassFish.org//DTD GlassFish Application Server 3.1 Application Client Container//EN",
                "dtds/glassfish-application-client-container_1_3.dtd");

            private final String publicID;
            private final URI uri;

            ACC_INFO(final String publicID, final String systemIDSuffix) {
                this.publicID = publicID;
                uri = URI.create(LOCAL_PATH_PREFIX + systemIDSuffix);
            }
        }

        private static final String LOCAL_PATH_PREFIX = "/glassfish/lib/";

        private static final Map<String, String> publicIdToLocalPathMap = initPublicIdToLocalPathMap();

        private static Map<String,String> initPublicIdToLocalPathMap() {
            final Map<String,String> result = new HashMap<>();
            for (ACC_INFO accInfo : ACC_INFO.values()) {
                result.put(accInfo.publicID, accInfo.uri.toASCIIString());
            }
            return result;
        }

        /*
         * The deployment/dtds module should be in the dependencies, so the
         * entries in that JAR should be accessible on the class path.
         */
        @Override
        public InputSource resolveEntity(String publicId, String systemId){
            final String localPath = publicIdToLocalPathMap.get(publicId);
            if (localPath == null) {
                return null;
            }

            /*
             * The next line works because the pom for this project extracted
             * the DTD from the deployment/dtds dtds.zip file and placed it into
             * a temporary directory which we used for generating the JAXB classes.
             * The pom also adds that same temporary directory to the test-time
             * class path, which allows this class to find that locally-extracted
             * copy of the DTD.
             */
            final InputStream is = getClass().getResourceAsStream(localPath);
            if (is == null) {
                System.err.println("Found map entry for public Id but could not find the local path " + localPath);
                return null;
            }
            return new InputSource(is);
        }
    }
}
