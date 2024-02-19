/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.adapter;

/** A package-private class that holds the state of the admin adapter.
 *  It also acts as a lock that needs to be synchronized externally.
 *  Note that this class is not thread-safe on its own.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish V3
 */
enum AdapterState {

    UNINITIAZED("state.uninitialized", "The Admin Console Adapter is not yet initialized."),
    AUTHENTICATING("state.authenticating", "Authentication required before the Admin Console can be installed."),
    PERMISSION_NEEDED("state.permissionNeeded", "The Admin Console requires your permission before it can be downloaded or installed."),
    PERMISSION_GRANTED("state.permissionGranted", "The Admin Console has your permission to downloaded and install."),
    CANCELED("state.canceled", "The Admin Console installation has been canceled."),
    DOWNLOADING("state.downloading", "The Admin Console Web Application is downloading..."),
    DOWNLOADED("state.downloaded", "The Admin Console Web Application has been downloaded."),
    EXPANDING("state.expanding", "The Admin Console war file is expanding..."),
    EXPANDED("state.expanded", "The Admin Console war file has been expanded."),
    INSTALLING("state.installing", "The Admin Console is installing..."),
    APPLICATION_INSTALLED_BUT_NOT_LOADED("state.installedNotLoaded", "The Admin Console is already installed, but not yet loaded."),
    APPLICATION_LOADING("state.loading", "The Admin Console is starting. Please wait."),
    APPLICATION_LOADED("state.loaded", "The Admin Console application is loaded."),
    APPLICATION_NOT_INSTALLED("state.notInstalled", "The Admin Console Application is not yet installed."),
    APPLICATION_PREPARE_UPGRADE("state.prepareRedeploy", "Preparing to upgrade Admin Console Application..."),
    APPLICATION_BACKUP_FALED("state.backupFailed", "Cannot backup previous version of __admingui"),
    APPLICATION_CLEANUP_FALED("state.cleanupFailed", "Exception while cleaning previous instance of admin GUI"),
    APPLICATION_BACKUP_CLEANING("state.cleaningBackup", "Cleaning up temporary backup file..."),
    APPLICATION_BACKUP_CLEANED("state.backupCleaned", "Temporary backup file removed"),
    APPLICATION_RESTORE("state.restore", "Restoring previously deployed Admin Console..."),
    APPLICATION_UPGRADE_FALED("state.upgradeFailed", "Cannot upgrade Admin Console."),
    WELCOME_TO("state.welcometo", "Welcome to ");


    private final String desc;
    private final String i18nKey;

    private AdapterState(String i18nKey, String desc) {
        this.i18nKey = i18nKey;
        this.desc = desc;
    }


    /**
     * This is the key that should be used to retrieve the localized message from a properties file.
     */
    public String getI18NKey() {
        return i18nKey;
    }

    @Override
    public String toString() {
        return (desc);
    }
}
