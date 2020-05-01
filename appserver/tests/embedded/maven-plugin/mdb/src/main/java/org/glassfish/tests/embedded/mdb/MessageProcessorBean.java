/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.mdb;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bhavanishankar@dev.java.net
 */

@MessageDriven(mappedName = "jms/TestQueue", activationConfig = {
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
})
public class MessageProcessorBean implements MessageListener {

    private static Logger logger = Logger.getAnonymousLogger();

    public void onMessage(Message message) {
        try {
            String msg = ((TextMessage) message).getText();
            logger.log(Level.INFO, "Message Received [" + msg + "]");
            FileOutputStream os = new FileOutputStream(new File(
                    System.getProperty("java.io.tmpdir"), "embedded_mdb_onmessage.txt"));
            os.write(msg.getBytes());
            os.flush();
            os.close();
        } catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
    }
}
