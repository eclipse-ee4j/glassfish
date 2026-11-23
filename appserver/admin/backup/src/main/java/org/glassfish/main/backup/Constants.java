/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

/*
 * Constants.java
 *
 * Created on January 21, 2004, 11:31 PM
 */

package org.glassfish.main.backup;

/**
 *
 * @author  bnevins
 */
public interface Constants
{
    String    loggingResourceBundle = "org.glassfish.main.backup.LocalStrings";
    String    exceptionResourceBundle = "/org/glassfish/main/backup/LocalStrings.properties";
    String    BACKUP_DIR = "backups";
    String    OSGI_CACHE = "osgi-cache";
    String    PROPS_USER_NAME = "user.name";
    String    PROPS_TIMESTAMP_MSEC = "timestamp.msec";
    String    PROPS_TIMESTAMP_HUMAN = "timestamp.human";
    String    PROPS_DOMAINS_DIR = "domains.dir";
    String    PROPS_DOMAIN_DIR = "domain.dir";
    String    PROPS_DOMAIN_NAME = "domain.name";
    String    PROPS_BACKUP_FILE = "backup.file";
    String    PROPS_DESCRIPTION = "description";
    String    PROPS_HEADER = "Backup Status";
    String    PROPS_VERSION = "version";
    String    PROPS_TYPE = "type";
    String    BACKUP_CONFIG = "backupConfig";
    String    PROPS_FILENAME = "backup.properties";
    String    CONFIG_ONLY ="configOnly";
    String    FULL ="full";
    String    CONFIG_DIR="config";
    String    NO_CONFIG=" ";
}
