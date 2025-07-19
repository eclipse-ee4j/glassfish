/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

/**
 * The KeyStoreManager implementation for a node.
 * This is used by the change-master-password command which will change the master password for a node
 *
 * @author Bhakti Mehta
 */
public class NodeKeystoreManager extends KeystoreManager {

    public void encryptKeystore(RepositoryConfig config, String oldPassword, String newPassword) throws RepositoryException {
        super.changeSSLCertificateDatabasePassword(config, oldPassword, newPassword);
    }
}
