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

package com.sun.enterprise.web.connector.extension;

import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Host;

/**
 * Listener used to receive events from Catalina when a <code>Context</code>
 * is removed or when a <code>Host</code> is removed.
 *
 * @author Jean-Francois Arcand
 */
public class CatalinaListener  implements ContainerListener{

    public void containerEvent(ContainerEvent event) {
        if (Container.REMOVE_CHILD_EVENT.equals(event.getType()) ) {
            Context context;
            String contextPath;
            Host host;

            Object container = event.getData();
            if ( container instanceof Context) {
                context = (Context)container;

                if (!context.hasConstraints() &&
                        context.findFilterDefs().length == 0 ){
                    contextPath = context.getPath();
                    host = (Host)context.getParent();
                    String[] names = host.getNetworkListenerNames();
                    for (String name : names) {
                        removeContextPath(name, contextPath);
                    }
                }
            }
        }
    }


    /**
     * Remove from the <code>FileCache</code> all entries related to
     * the <code>Context</code> path.
     * @param id the <code>FileCacheFactory</code> id
     * @param contextPath the <code>Context</code> path
     */
    private void removeContextPath(String id, String contextPath) {
        // FIXME: I can't spot where Grizzly is registering mbeans, and this code
        // tries to invoke it and fails during the undeployment.
        // Commented out for now for the sake of JavaOne demo.

//        ArrayList<GrizzlyConfig> list =
//                GrizzlyConfig.getGrizzlyConfigInstances();
//        for(GrizzlyConfig config: list){
//            if (config.getPort() == port){
//                config.invokeGrizzly("removeCacheEntry",
//                        new Object[]{contextPath},
//                        new String[]{"java.lang.String"});
//            }
//        }
    }
}

