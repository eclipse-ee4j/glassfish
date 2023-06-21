/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.server.core.jws;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;

/**
 * Abstracts the OSGi configuration information so it can be served easily
 * via JNLP to Java Web Start.
 *
 * @author tjquinn
 */
class LoaderConfigContent {

    private static final String OSGI_CONFIG_FILE_PATH = "config/osgi.properties";
    private final String content;

    LoaderConfigContent(final File installDir) throws FileNotFoundException, IOException {
        content = loadContent(configFileURI(installDir));
    }

    String content() {
        return content;
    }

    private String loadContent(final URI configFileURI) throws FileNotFoundException, IOException {
        return Files.readString(new File(configFileURI).toPath());
    }

    private URI configFileURI(final File installDir) {
        return installDir.toURI().resolve(OSGI_CONFIG_FILE_PATH);
    }
}
