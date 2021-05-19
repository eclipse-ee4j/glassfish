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

package com.sun.enterprise.admin.servermgmt;

import java.util.BitSet;

/**
 */
public interface DomainsManager {
    /**
     * In SE/EE we need an admin user/password that the DAS can use to authenticate to Node Agents and servers in the
     * domain. This is not the case in PE; hence this flag -- DomainConfig.K_FLAG_START_DOMAIN_NEEDS_ADMIN_USER
     *
     * In SE/EE we need an extra non secure http port to host the Lockhart components which is controlled by --
     * DomainConfig.K_FLAG_CREATE_DOMAIN_NEEDS_ALTERNATE_ADMIN_PORT
     *
     * @return flags toggling SE/EE specific behavior.
     */
    public BitSet getDomainFlags();

    /**
     * SE/EE supports NSS as its native SSL database. NSS is capable of supporting multiple slots (e.g. for different SSL
     * hardware devices, smartcards, etc). Each device needs a specific password which the CLI must prompt for.
     */
    public String[] getExtraPasswordOptions(DomainConfig config) throws DomainException;

    /**
     * Deletes a domain identified by the given name. (Should we stop the DAS and instances administered by this domain
     * before deleting the domain?)
     *
     * @param domainConfig
     * @throws DomainException This exception is thrown if
     * <ul>
     * - the domain doesnot exist. - an exception occurred while deleting the domain.
     * </ul>
     */
    public void deleteDomain(DomainConfig domainConfig) throws DomainException;

    /**
     * Starts the Domain Administration Server (DAS) that administers the given domain.
     *
     * @param startParams
     * @throws DomainException
     */
    /*
    public void startDomain(DomainConfig domainConfig)
        throws DomainException;
    */
    /**
     * Stops the Domain Administration Server (DAS) that administers the given domain.
     *
     * @param domainConfig
     * @throws DomainException
     */

    /*
    public void stopDomain(DomainConfig domainConfig)
        throws DomainException;
    */
    /**
     * Lists all the domains.
     */
    public String[] listDomains(DomainConfig domainConfig) throws DomainException;

    /**
     * Lists all the domains and their status
     */
    /*
    public String[] listDomainsAndStatus(DomainConfig domainConfig)
        throws DomainException;
    */
    /**
     * Changes the master password for the domain
     */
    /*
    public void changeMasterPassword(DomainConfig domainConfig)
        throws DomainException;
    */
    public void validateDomain(DomainConfig domainConfig, boolean domainExists) throws DomainException;

    public void validateMasterPassword(DomainConfig config) throws DomainException;

    //public void validateAdminUserAndPassword(DomainConfig domainConfig) throws DomainException;

    /*
    public InstancesManager getInstancesManager(RepositoryConfig config);
    */
    /**
     * Stops the Domain Administration Server (DAS) that administers the given domain.
     *
     * @param domainConfig
     * @throws DomainException
     */
    /*
    public void stopDomainForcibly(DomainConfig domainConfig, int timeout)
        throws DomainException;
     */
}
