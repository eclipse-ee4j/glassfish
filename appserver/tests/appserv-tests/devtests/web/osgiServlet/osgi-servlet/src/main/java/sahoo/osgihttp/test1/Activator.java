/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package sahoo.osgihttp.test1;

import jakarta.servlet.http.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.Filter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.HttpContext;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

        private ServiceTracker httpServiceTracker;

        public void start(BundleContext context) throws Exception {
                httpServiceTracker = new HttpServiceTracker(context);
                httpServiceTracker.open();
        }

        public void stop(BundleContext context) throws Exception {
                httpServiceTracker.close();
                httpServiceTracker = null;
        }

        private class HttpServiceTracker extends ServiceTracker {

                public HttpServiceTracker(BundleContext context) throws Exception {
                        super(context, context.createFilter("(&(objectClass=" +
                                                                org.osgi.service.http.HttpService.class.getName() +
                                                                ")(VirtualServer=server))"), null);
                }

                public Object addingService(ServiceReference reference) {
                        HttpService httpService = (HttpService) context.getService(reference);
                        try {
                                httpService.registerServlet("/aa/bb", new HelloWorldServlet1(), null, null);
                                httpService.registerServlet("/aa", new HelloWorldServlet2(), null, null);
                                System.out.println("Registered servlet1 with mapping /aa/bb and servlet2 with mapping /aa");
                                test();
                                httpService.unregister("/aa/bb");
                                System.out.println("Unregistered servlet1");
                                test();

                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                        return httpService;
                }
        }

        void test() {
            try {
                final String urlstr = "http://localhost:8080/osgi/aa/bb";
                URL source = null;
                String inputLine = new String();
                StringBuffer resbuf = new StringBuffer();
                BufferedReader in = null;
                source = new URL(urlstr);
                in = new BufferedReader(new InputStreamReader(source.openStream()));
                while ((inputLine = in.readLine()) != null) {
                        resbuf.append(inputLine);
                }
                in.close();
                System.out.println(resbuf.toString());
            } catch(Exception e) {
                System.out.println(e);
            }
        }
}
