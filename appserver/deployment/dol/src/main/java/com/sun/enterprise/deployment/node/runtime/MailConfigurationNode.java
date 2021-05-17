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

package com.sun.enterprise.deployment.node.runtime;

import com.sun.enterprise.deployment.MailConfiguration;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

/**
 * This node handles the runtime deployment descriptor tag
 * mail-configuration.  Or at least it would, if there were
 * such a tag.  As far as I can tell, there isn't, and this
 * class is never used.
 */
public class MailConfigurationNode extends DeploymentDescriptorNode {

    private String name = null;
    private String mail_from = null;
    private String mail_host = null;

    /**
     * receives notification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        if (RuntimeTagNames.NAME.equals(element.getQName())) {
            name = value;
        } else  if (RuntimeTagNames.MAIL_FROM.equals(element.getQName())) {
            mail_from = value;
        } else  if (RuntimeTagNames.MAIL_HOST.equals(element.getQName())) {
            mail_host = value;
        }
    }

    /**
     * notification of the end of XML parsing for this node
     */
    public void postParsing() {
        getParentNode().addDescriptor(new MailConfiguration(name, mail_from, mail_host));
    }
}
