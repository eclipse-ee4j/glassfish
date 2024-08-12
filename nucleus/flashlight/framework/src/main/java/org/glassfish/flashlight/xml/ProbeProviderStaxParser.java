/*
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.xml;

import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.glassfish.flashlight.FlashlightLoggerInfo;

import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static org.glassfish.flashlight.FlashlightLoggerInfo.NO_PROVIDER_IDENTIFIED_FROM_XML;
import static org.glassfish.flashlight.xml.XmlConstants.METHOD;
import static org.glassfish.flashlight.xml.XmlConstants.MODULE_NAME;
import static org.glassfish.flashlight.xml.XmlConstants.MODULE_PROVIDER_NAME;
import static org.glassfish.flashlight.xml.XmlConstants.PROBE;
import static org.glassfish.flashlight.xml.XmlConstants.PROBE_HIDDEN;
import static org.glassfish.flashlight.xml.XmlConstants.PROBE_NAME;
import static org.glassfish.flashlight.xml.XmlConstants.PROBE_PARAM;
import static org.glassfish.flashlight.xml.XmlConstants.PROBE_PARAM_NAME;
import static org.glassfish.flashlight.xml.XmlConstants.PROBE_PARAM_TYPE;
import static org.glassfish.flashlight.xml.XmlConstants.PROBE_PROFILE_NAMES;
import static org.glassfish.flashlight.xml.XmlConstants.PROBE_PROVIDER;
import static org.glassfish.flashlight.xml.XmlConstants.PROBE_PROVIDER_CLASS;
import static org.glassfish.flashlight.xml.XmlConstants.PROBE_PROVIDER_NAME;
import static org.glassfish.flashlight.xml.XmlConstants.PROBE_SELF;
import static org.glassfish.flashlight.xml.XmlConstants.PROBE_STATEFUL;
import static org.glassfish.flashlight.xml.XmlConstants.PROBE_STATEFUL_EXCEPTION;
import static org.glassfish.flashlight.xml.XmlConstants.PROBE_STATEFUL_RETURN;

/**
 * Read the XML file, parse it and return a list of ProbeProvider objects
 * @author bnevins
 */
public class ProbeProviderStaxParser extends StaxParser{

    private static final Logger logger =
        FlashlightLoggerInfo.getLogger();
    public final static LocalStringManagerImpl localStrings =
                            new LocalStringManagerImpl(ProbeProviderStaxParser.class);

    public ProbeProviderStaxParser(File f) throws XMLStreamException {
        super(f);
    }

    public ProbeProviderStaxParser(InputStream in) throws XMLStreamException {
        super(in);
    }

    public List<Provider> getProviders() {
        if(providers == null) {
            try {
                read();
            }
            catch(Exception ex) {
                // normal
                close();
            }
        }
        if (providers.isEmpty()) {
            // this line snatched from the previous implementation (DOM)
            logger.log(Level.SEVERE, NO_PROVIDER_IDENTIFIED_FROM_XML);
        }

        return providers;
    }

    @Override
    protected void read() throws XMLStreamException, EndDocumentException{
        providers = new ArrayList<Provider>();
        // move past the root -- "probe-providers".
        skipPast("probe-providers");

        while(true) {
            providers.add(parseProbeProvider());
        }
    }

    private Provider parseProbeProvider() throws XMLStreamException {
        if(!parser.getLocalName().equals(PROBE_PROVIDER)) {
            String errStr = localStrings.getLocalString("invalidStartElement",
                                "START_ELEMENT is supposed to be {0}" +
                                ", found: {1}", PROBE_PROVIDER, parser.getLocalName());
            throw new XMLStreamException(errStr);
        }

        Map<String,String> atts = parseAttributes();
        List<XmlProbe> probes = parseProbes();

        return new Provider(atts.get(MODULE_PROVIDER_NAME),
                            atts.get(MODULE_NAME),
                            atts.get(PROBE_PROVIDER_NAME),
                            atts.get(PROBE_PROVIDER_CLASS),
                            probes );
    }

    private List<XmlProbe> parseProbes() throws XMLStreamException {
        List<XmlProbe> probes = new ArrayList<XmlProbe>();

        boolean done = false;

        // Prime the pump here, and advance to the next start. Note that further advances
        // will be done as the elements within a probe are handled, not at this level.
        try {
            nextStart();
        }
        catch (EndDocumentException ex) {
            // ignore -- this must be the last START_ELEMENT in the doc
            // that's normal
            done = true;
        }

        while(!done) {
            // If we've been advanced to the end of the document, we' done...
            if (parser.getEventType() == END_DOCUMENT)
                break;
            if(parser.getLocalName().equals(PROBE))
                probes.add(parseProbe());  // Note that this will advance to the next probe if there is one
            else
                done = true;  // Done at this level, bounce out.
        }
        return probes;
    }

    private XmlProbe parseProbe() throws XMLStreamException {
        if(!parser.getLocalName().equals(PROBE)) {
            String errStr = localStrings.getLocalString("invalidStartElement",
                                "START_ELEMENT is supposed to be {0}" +
                                ", found: {1}", PROBE, parser.getLocalName());
            throw new XMLStreamException(errStr);
        }

        // for some unknown reason method is an element not an attribute
        // Solution -- use the last item if there are more than one

        List<XmlProbeParam> params = new ArrayList<XmlProbeParam>();
        Map<String,String> atts = parseAttributes();
        String method = null;
        String name = atts.get(PROBE_NAME);
        boolean self = Boolean.parseBoolean(atts.get(PROBE_SELF));
        boolean hidden = Boolean.parseBoolean(atts.get(PROBE_HIDDEN));
        boolean stateful = Boolean.parseBoolean(atts.get(PROBE_STATEFUL));
        boolean statefulReturn = Boolean.parseBoolean(atts.get(PROBE_STATEFUL_RETURN));
        boolean statefulException = Boolean.parseBoolean(atts.get(PROBE_STATEFUL_EXCEPTION));
        String profileNames = atts.get(PROBE_PROFILE_NAMES);
        boolean done = false;
        while(!done) {
            try {
                nextStart();
                String localName = parser.getLocalName();

                if(localName.equals(METHOD))
                    method = parser.getElementText();
                else if(localName.equals(PROBE_PARAM))
                    params.add(parseParam());
                else
                    done = true;
            }
            catch (EndDocumentException ex) {
                // ignore -- possibly normal -- but stop!
                done = true;
            }
        }
        return new XmlProbe(name, method, params, self, hidden, stateful, statefulReturn, statefulException, profileNames);
    }
    private XmlProbeParam parseParam() throws XMLStreamException {
        if(!parser.getLocalName().equals(PROBE_PARAM)){
            String errStr = localStrings.getLocalString("invalidStartElement",
                                "START_ELEMENT is supposed to be {0}" +
                                ", found: {1}", PROBE_PARAM, parser.getLocalName());
            throw new XMLStreamException(errStr);
        }

        Map<String,String> atts = parseAttributes();

        return new XmlProbeParam(atts.get(PROBE_PARAM_NAME), atts.get(PROBE_PARAM_TYPE));
    }

    private List<Provider> providers = null;
}

