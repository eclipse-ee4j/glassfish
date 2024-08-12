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

package org.glassfish.appclient.client.acc;


import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Implements ReadableArchive for the https protocol to support
 * launches of app clients using Java Web Start.
 * <p>
 * Although the JARs are stored as JARs in the Java Web Start cache,
 * Java Web Start hides the actual location where the cached JAR resides.
 * So this implementation does not rely on the JARs location but uses
 * URLs to access the archive itself and its elements.
 *
 * @author tjquinn
 */
@Service(name="https")
@PerLookup
public class HTTPSInputArchive extends HTTPInputArchive {

}
