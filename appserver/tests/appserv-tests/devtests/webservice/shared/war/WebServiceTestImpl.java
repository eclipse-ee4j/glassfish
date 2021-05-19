/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.webservice;

import java.rmi.*;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

public class WebServiceTestImpl implements ServiceLifecycle, WebServiceTest {

    WebServiceTest delegate;

    public void destroy() {
        System.out.println("Driver servlet destroyed");
    }

    public void init(Object context) {
        ServletEndpointContext seContext = (ServletEndpointContext) context;
        String testClassName = seContext.getServletContext().getInitParameter("testclassname");
        if (testClassName==null) {
            System.out.println("Error : no delegate servlet provided for test");
            return;
        }
        try {
            Class clazz = Class.forName(testClassName);
            if (clazz==null) {
                System.out.println("Error : cannot load delegate " + testClassName);
                return;
            }
            Object o = clazz.newInstance();
            if (o instanceof WebServiceTest) {
             delegate = (WebServiceTest) o;
            } else {
             System.out.println("Error : delegate not of type WebServiceTest");
            }
        } catch(ClassNotFoundException cnfe) {
            System.out.println("Error : cannot load delegate " + testClassName);
        } catch(InstantiationException ie) {
            System.out.println("Error : cannot instantiate " + testClassName);
        } catch(Exception e) {
            System.out.println("Error : cannot load delegate " + testClassName + " " + e.getMessage());
        }
    }

    public String doTest(String[] params) throws RemoteException {

        if (delegate!=null) {
            return delegate.doTest(params);
        } else {
            throw new RemoteException("No delegate for test harness");
        }
    }
}
