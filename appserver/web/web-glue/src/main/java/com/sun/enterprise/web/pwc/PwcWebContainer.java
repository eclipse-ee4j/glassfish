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

package com.sun.enterprise.web.pwc;

import org.apache.catalina.Engine;
import org.apache.catalina.Logger;
import org.apache.catalina.startup.Embedded;

/*
 * Represents the production web container
 */
public class PwcWebContainer implements PwcWebContainerLifecycle {

    /**
     * The parent/top-level container in <code>_embedded</code> for virtual
     * servers.
     */
    private Engine _engine = null;

   /**
     * The embedded Catalina object.
     */
    private Embedded _embedded = null;


    /**
     * Has this component been started yet?
     */
    private boolean _started = false;

    public void onInitialization(String rootDir, String instanceName,
                                 boolean useNaming, Logger logger,
                                 String embeddedClassName)
        throws Exception {
        Class c = Class.forName(embeddedClassName);
        _embedded = (Embedded) c.newInstance();
        _embedded.setUseNaming(useNaming);
        _embedded.setLogger(logger);
        _engine = _embedded.createEngine();
        _embedded.addEngine(_engine);
    }

    public void onStartup()
        throws Exception {
        _started = true;

        _embedded.start();

    }

    public void onReady()
        throws Exception {
    }


    public void onShutdown()
        throws Exception {
    }


    public void onTermination()
        throws Exception {
        _started = false;
        _embedded.stop();
    }

    public Engine getEngine() {
        return _engine;
    }

    public Embedded getEmbedded() {
        return _embedded;
    }

}
