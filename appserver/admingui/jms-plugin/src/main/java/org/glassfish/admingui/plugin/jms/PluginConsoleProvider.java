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

package org.glassfish.admingui.plugin.jms;

import java.net.URL;

import org.glassfish.api.admingui.ConsoleProvider;
import org.jvnet.hk2.annotations.Service;

/**
 *  <p> This class serves as a marker to indicate this OSGi bundle provides GUI
 *      content which is to be displayed in the GlassFish admin console.  The
 *      {@link #getConfiguration()} method should either return
 *      <code>(null)</code>, or a <code>URL</code> to the
 *      <code>console-config.xml</code> file.</p>
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
@Service
public class PluginConsoleProvider implements ConsoleProvider {

    /**
     * <p> Returns a <code>URL</code> to the <code>console-config.xml</code>
     *     file, or <code>null</code>.  If <code>null</code> is returned, the
     *     default ("META-INF/admingui/console-config.xml") will be used.</p>
     */
    public URL getConfiguration() {
        return null;
    }
}
