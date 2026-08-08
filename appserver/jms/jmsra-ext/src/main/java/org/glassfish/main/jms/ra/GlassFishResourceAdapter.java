/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

package org.glassfish.main.jms.ra;

import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.auth.AccessController;
import com.sun.messaging.jmq.jmsserver.auth.file.JMQFileUserRepository;
import com.sun.messaging.jmq.jmsserver.auth.usermgr.PasswdDB;
import com.sun.messaging.jmq.jmsserver.comm.CommGlobals;
import com.sun.messaging.jms.blc.LifecycleManagedBroker;
import com.sun.messaging.jms.ra.ResourceAdapter;

import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ResourceAdapterInternalException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GlassFish specific extension for OpenMQ ResourceAdapter.
 */
public class GlassFishResourceAdapter extends ResourceAdapter {

    private static final Logger LOG = Logger.getLogger(GlassFishResourceAdapter.class.getName());

    @Override
    public synchronized void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
        boolean synchronizePasswordRepository = shouldSynchronizePasswordRepository();
        super.start(bootstrapContext);
        if (!synchronizePasswordRepository) {
            return;
        }
        synchronizeAdminPasswordRepository();
    }

    private boolean shouldSynchronizePasswordRepository() {
        if (BROKER_TYPE_REMOTE.equals(getBrokerType())) {
            return false;
        }

        LifecycleManagedBroker lifecycleManagedBroker = getLifecycleManagedBroker();
        if (lifecycleManagedBroker == null) {
            return false;
        }

        String brokerVarDir = lifecycleManagedBroker.getBrokerVarDir();
        String brokerInstanceName = lifecycleManagedBroker.getBrokerInstanceName();
        Path brokerInstanceDir = Path.of(brokerVarDir, CommGlobals.INSTANCES_HOME_DIRECTORY, brokerInstanceName);
        return !Files.exists(brokerInstanceDir);
    }

    // synchronized because PasswdDB relies on static setPasswordFileName
    private synchronized void synchronizeAdminPasswordRepository() throws ResourceAdapterInternalException {
        final String oldPasswordFileName = PasswdDB.getPasswordFileName();
        try {
            String passwordFileName = Globals.getConfig().getProperty(
                AccessController.PROP_USER_REPOSITORY_PREFIX + JMQFileUserRepository.PROP_FILENAME_SUFFIX,
                JMQFileUserRepository.DEFAULT_PW_FILENAME);
            Path passwordFile = Path.of(Globals.getInstanceEtcDir(), passwordFileName);
            PasswdDB.setPasswordFileName(passwordFile.toAbsolutePath().toString());
            PasswdDB passwdDB = new PasswdDB();
            passwdDB.updateUser(getAdminUsername(), getAdminPassword(), Boolean.TRUE);

            LOG.log(Level.INFO, "Synchronized OpenMQ admin password repository at {0}",
                passwordFile.toAbsolutePath());
        } catch (Exception e) {
            throw new ResourceAdapterInternalException(
                "Failed to synchronize OpenMQ admin password repository", e);
        } finally {
            PasswdDB.setPasswordFileName(oldPasswordFileName);
        }
    }
}
