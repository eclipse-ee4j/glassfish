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

/*
 * Constants.java
 *
 * Created on January 21, 2004, 11:31 PM
 */

package com.sun.enterprise.backup;

/**
 *
 * @author  bnevins
 */
public interface Constants
{
    final static String    loggingResourceBundle = "com.sun.enterprise.backup.LocalStrings";
    final static String    exceptionResourceBundle = "/com/sun/enterprise/backup/LocalStrings.properties";
    final static String    BACKUP_DIR = "backups";
    final static String    OSGI_CACHE = "osgi-cache";
    final static String    PROPS_USER_NAME = "user.name";
    final static String    PROPS_TIMESTAMP_MSEC = "timestamp.msec";
    final static String    PROPS_TIMESTAMP_HUMAN = "timestamp.human";
    final static String    PROPS_DOMAINS_DIR = "domains.dir";
    final static String    PROPS_DOMAIN_DIR = "domain.dir";
    final static String    PROPS_DOMAIN_NAME = "domain.name";
    final static String    PROPS_BACKUP_FILE = "backup.file";
    final static String    PROPS_DESCRIPTION = "description";
    final static String    PROPS_HEADER = "Backup Status";
    final static String    PROPS_VERSION = "version";
    final static String    PROPS_TYPE = "type";
    final static String    BACKUP_CONFIG = "backupConfig";
    final static String    PROPS_FILENAME = "backup.properties";
    final static String    CONFIG_ONLY ="configOnly";
    final static String    FULL ="full";
    final static String    CONFIG_DIR="config";
    final static String    NO_CONFIG=" ";
}
