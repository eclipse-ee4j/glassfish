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

package com.sun.enterprise.admin.servermgmt.cli;

import com.sun.enterprise.util.HostAndPort;
import com.sun.enterprise.util.io.DomainDirs;
import com.sun.enterprise.util.io.ServerDirs;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;

/**
 * A class that's supposed to capture all the behavior common to operation on a "local" domain. It's supposed to act as
 * the abstract base class that provides more functionality to the commands that operate on a local domain.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @author Byron Nevins (bnevins@dev.java.net)
 */
public abstract class LocalDomainCommand extends LocalServerCommand {
    @Param(name = "domaindir", optional = true)
    protected String domainDirParam;
    // subclasses decide whether it's optional, required, or not allowed
    //@Param(name = "domain_name", primary = true, optional = true)
    private String userArgDomainName;
    // the key for the Domain Root in the main attributes of the
    // manifest returned by the __locations command
    private static final String DOMAIN_ROOT_KEY = "Domain-Root";
    private DomainDirs dd;

    /*
     * The prepare method must ensure that the superclass' implementation of
     * the method is called.
     * The reason we override here is that we can get into trouble with layers
     * of NPE possibilities.  So here the ServerDirs object is initialized
     * right away.  It will return null for all non-boolean method calls.  But
     * we never have to do a null-check on the ServerDirs object itself.
     * ServerDirs is 100% immutable.  A new one will be made later if needed.
     */
    @Override
    protected void prepare() throws CommandException, CommandValidationException {
        super.prepare();
        setServerDirs(new ServerDirs()); // do-nothing ServerDirs object...
    }

    @Override
    protected void validate() throws CommandException, CommandValidationException {

        initDomain();
    }

    /**
     * Loads the list of admin addresses of a particular server parsed from the domain.xml.
     *
     * @param domainXml
     * @return list of HostAndPort objects with admin server address. Never null but can be empty.
     */
    protected final List<HostAndPort> loadAdminAddresses(File domainXml) {
        return loadAdminAddresses(domainXml, "server");
    }

    /**
     * @return admin endpoint of the domain.
     */
    @Override
    protected final HostAndPort getReachableAdminAddress() {
        return getReachableAdminAddress(() -> loadAdminAddresses(getDomainXml(), "server"));
    }

    protected final File getDomainsDir() {
        return dd.getDomainsDir();
    }

    protected final File getDomainRootDir() {
        return dd.getDomainDir();
    }

    protected final String getDomainName() {
        // can't just use "dd" since it may be half-baked right now!
        if (dd != null && dd.isValid()) {
            return dd.getDomainName();
        }
        return userArgDomainName; // might be and is ok to be null
    }

    /**
     * We need this so that @Param values for domainname can be remembered later when the ServerDirs object is made.
     *
     * @param name the user-specified domain name.
     */
    protected final void setDomainName(String name) {
        dd = null;
        userArgDomainName = name;
    }

    protected void initDomain() throws CommandException {
        try {
            File domainsDirFile = null;

            if (ok(domainDirParam)) {
                domainsDirFile = new File(domainDirParam);
            }

            dd = new DomainDirs(domainsDirFile, getDomainName());
            setServerDirs(dd.getServerDirs());
        } catch (Exception e) {
            throw new CommandException(e.getMessage(), e);
        }
        setLocalPassword();
    }

    protected boolean isThisDAS(File ourDir) {
        return isThisServer(ourDir, DOMAIN_ROOT_KEY);
    }

    /**
     * @param endpoints
     * @return space separated list of endpoints including HTTP/HTTPS protocol prefix.
     */
    protected static String toHttpList(List<HostAndPort> endpoints) {
        return endpoints.stream()
        .map(h -> (h.isSecure() ? "https://" : "http://") + h.getHost() + ':' + h.getPort())
        .collect(Collectors.joining(" "));
    }
}
