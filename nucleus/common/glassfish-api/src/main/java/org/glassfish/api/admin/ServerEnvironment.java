/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin;

import com.sun.enterprise.module.bootstrap.StartupContext;

import java.io.File;

import org.jvnet.hk2.annotations.Contract;

/**
 * Allow access to the environment under which GlassFish operates.
 *
 * TODO : dochez : this needs to be reconciled with ServerContext and simplified...
 *
 * @author Jerome Dochez
 */
@Contract
public interface ServerEnvironment {
    enum Status {
        starting, started, stopping, stopped
    }

    /** folder where the compiled JSP pages reside */
    String kCompileJspDirName = "jsp";
    String DEFAULT_INSTANCE_NAME = "default-instance-name";

    /**
     * @return the instance root
     */
    File getInstanceRoot();

    /**
     * return the startup context used to initialize this runtime
     */
    StartupContext getStartupContext();

    /**
     *
     */
    File getConfigDirPath();

    /**
     * Gets the directory for hosting user-provided jar files. Normally {@code ROOT/lib}
     */
    File getLibPath();

    /**
     * Gets the directory to store deployed applications Normally {@code ROOT/applications}
     */
    File getApplicationRepositoryPath();

    /**
     * Gets the directory to store generated stuff. Normally {@code ROOT/generated}
     */
    File getApplicationStubPath();

    /**
     * Returns the path for compiled JSP Pages from an application that is deployed on this instance. By default all such
     * compiled JSPs should lie in the same folder.
     */
    File getApplicationCompileJspPath();

    File getApplicationGeneratedXMLPath();

    File getApplicationEJBStubPath();

    File getApplicationPolicyFilePath();

    /**
     * Gets the directory to store external alternate deployment descriptor Normally {@code ROOT/generated/altdd}
     */
    File getApplicationAltDDPath();

    /**
     * A keystore which is locked with a fixed-key. This is the "security-by-obfuscation" carried over from V2.
     *
     * @return File representing the store containing the real master password
     */
    File getMasterPasswordFile();

    /**
     * A Java KeyStore which is locked by administrator's master password.
     *
     * @return File representing the JKS which is server's keystore in developer-product case
     */
    File getJKS();

    /**
     * The truststore used by the server.
     *
     * @return File for the truststore
     */
    File getTrustStore();

    /**
     * @return the server status
     */
    Status getStatus();

    /**
     * Returns the process type of this instance.
     *
     * @return the instance process type
     */
    RuntimeType getRuntimeType();

    /**
     * Every server has a name that can be found in the server element in domain.xml
     *
     * @return the name of this server i.e. "my" name
     */
    String getInstanceName();

    /**
     * @return true if this is a single or clustered instance server managed by DAS, false otherwise.
     */
    boolean isInstance();

    /**
     * @return true if this is an domain admin server, false otherwise.
     */
    boolean isDas();

    /**
     * @return true if this is an embedded server, false otherwise.
     */
    boolean isEmbedded();
}
