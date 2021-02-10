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

package org.glassfish.api.web;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.jvnet.hk2.annotations.Contract;

/**
 * The TldProvider provides an interface to get jar URL with tlds and corresponding tld entries.
 *
 * @author Shing Wai Chan
 */
@Contract
public interface TldProvider {

    /**
     * Gets the name of this TldProvider
     */
    String getName();

    /**
     * Gets a mapping from JAR files to their TLD resources.
     */
    Map<URI, List<String>> getTldMap();

    /**
     * Gets a mapping from JAR files to their TLD resources that are known to contain listener declarations
     */
    Map<URI, List<String>> getTldListenerMap();

}
