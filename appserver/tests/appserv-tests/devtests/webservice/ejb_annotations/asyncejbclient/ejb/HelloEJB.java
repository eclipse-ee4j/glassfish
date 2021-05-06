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

package ejb;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jakarta.ejb.Stateless;
import jakarta.xml.ws.WebServiceRef;
import jakarta.xml.ws.AsyncHandler;
import jakarta.xml.ws.Response;

import endpoint.SayHelloResponse;

@Stateless
public class HelloEJB implements Hello {


   @WebServiceRef
   WebServiceEJBService webService;

    public String invokeSync(String string) {
        WebServiceEJB ejb = webService.getWebServiceEJBPort();
        return ejb.sayHello("SYNC CALL" + string);
   }

   public String invokeAsyncPoll(String msg) {
       try {
            WebServiceEJB ejb = webService.getWebServiceEJBPort();
            Response<SayHelloResponse> resp = ejb.sayHelloAsync("ASYNC POLL CALL" + msg);
            Thread.sleep (2000);
            SayHelloResponse out = resp.get();
            return(out.getReturn());
       } catch(Throwable t) {
            return(t.getMessage());
       }
   }

   public String invokeAsyncCallBack(String msg) {
        try {
System.out.println("VIJ - invoking async call back");
            WebServiceEJB ejb = webService.getWebServiceEJBPort();
            MyCallBackHandler cbh = new MyCallBackHandler();
            Future<?> response =
                ejb.sayHelloAsync("ASYNC CALL BACK CALL" + msg, cbh);
            Thread.sleep (2000);
            SayHelloResponse out = cbh.getResponse ();
            return(out.getReturn());
        } catch(Throwable t) {
            return(t.getMessage());
        }
   }

   // The actual call back handler
   private class MyCallBackHandler implements
                    AsyncHandler<SayHelloResponse> {
        private SayHelloResponse output;
        public void handleResponse (Response<SayHelloResponse> response) {
            try {
                output = response.get ();
            } catch (ExecutionException e) {
                e.printStackTrace ();
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }

        SayHelloResponse getResponse (){
            return output;
        }
    }
}
