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

package org.glassfish.deployment.client;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author tjquinn
 */
public class CommandXMLResultParser {

    static DFDeploymentStatus parse(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory pf = SAXParserFactory.newInstance();
        pf.setValidating(true);
        pf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        SAXParser parser = pf.newSAXParser();
        
        
        
        DFDeploymentStatus topStatus = null;
        ResultHandler rh = new ResultHandler();
        parser.parse(is, rh);
        
        topStatus = rh.getTopStatus();
        
        return topStatus;
    }
    
    private static DFDeploymentStatus.Status exitCodeToStatus(String exitCodeText) {
        return DFDeploymentStatus.Status.valueOf(exitCodeText);
    }
    
    private static class ResultHandler extends DefaultHandler {

        private DFDeploymentStatus topStatus;
        
        /** currentLevel will always point to the depl status we are currently working on */
        private DFDeploymentStatus currentLevel;

        private String attrToText(Attributes attrs, String attrName) {
            return attrs.getValue(attrName);
        }
        
        private DFDeploymentStatus getTopStatus() {
            return topStatus;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (qName.equals("action-report")) {
                /*
                 * If this is the first action-report then the resulting
                 * DFDeploymentStatus will be the top-level one as well as the
                 * current-level one.
                 */
                if (topStatus == null) { 
                    currentLevel = topStatus = new DFDeploymentStatus();
                } else {
                    /*
                     * This is a nested action-report, so add it as a sub-stage
                     * to the current level DFDeploymentStatus.
                     */
                    addLevel();
//                    DFDeploymentStatus newLevel = new DFDeploymentStatus();
//                    currentLevel.addSubStage(newLevel);
//                    currentLevel = newLevel;
                }
                currentLevel.setStageStatus(exitCodeToStatus(attrToText(attributes, "exit-code")));
                currentLevel.setStageDescription(attrToText(attributes, "description"));
                String failureCause = attrToText(attributes, "failure-cause");
                if (failureCause != null) {
                    currentLevel.setStageStatusMessage(failureCause);
                }
            } else if (qName.equals("message-part")) {
                /*
                 * The "message" attribute may not be present if the operation succeeded.
                 */
                addLevel();
                String msg = attrToText(attributes, "message");
                if (msg != null) {
                    String origMsg = currentLevel.getStageStatusMessage();
                    msg = currentLevel.getStageStatusMessage() + (origMsg != null && origMsg.length() > 0 ? " " : "") + msg;
                    currentLevel.setStageStatusMessage(msg);
                }
            } else if (qName.equals("property")) {
                currentLevel.addProperty(attrToText(attributes, "name"), attrToText(attributes, "value"));
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("action-report")) {
                popLevel();
            } else if (qName.equals("message-part")) {
                popLevel();
            }
        }

        private void addLevel() {
            DFDeploymentStatus newLevel = new DFDeploymentStatus();
            currentLevel.addSubStage(newLevel);
            currentLevel = newLevel;
        }

        private void popLevel() {
            currentLevel = currentLevel.getParent();
        }
    }
}
