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

package org.glassfish.admingui.common.plugin;

import java.net.URL;

import org.glassfish.api.admingui.ConsoleProvider;
import org.jvnet.hk2.annotations.Service;


/**
 *  <p>This is a noop file just to help test out the {@link ConsoleProvider}
 * and <code>ConsolePluginService</code> files.</p>
 *
 *  @author Ken Paulsen        (ken.paulsen@sun.com)
 */
@Service
public class CommonUtilPlugin implements ConsoleProvider {

    public URL getConfiguration() { return null; }
}
