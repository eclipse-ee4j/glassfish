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

package connector;

import jakarta.resource.spi.work.SecurityContext;
import javax.security.auth.Subject;
import jakarta.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;


public class MyPlainSICWithGPC extends SecurityContext {

        private String groups[];
        public MyPlainSICWithGPC(String[] groupNames){
            this.groups = groupNames;
        }


        public void setupSecurityContext(CallbackHandler callbackHandler, Subject execSubject, Subject serviceSubject) {


            List<Callback> callbacks = new ArrayList<Callback>();

            GroupPrincipalCallback gpc = new GroupPrincipalCallback(execSubject, groups);

            debug("setting group principal callback with group : " + groups);
            callbacks.add(gpc);

            addCallbackHandlers(callbacks, execSubject);

            Callback callbackArray[] = new Callback[callbacks.size()];
            try{
                callbackHandler.handle(callbacks.toArray(callbackArray));

            }catch(UnsupportedCallbackException e){
                debug("exception occured : " + e.getMessage());
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
                debug("exception occured : " + e.getMessage());
            }
        }

        protected void addCallbackHandlers(List<Callback> callbacks, Subject execSubject) {
            //do nothing
            //hook to test Dupilcate Inflow Context behavior
        }

        public String toString(){
            StringBuffer toString = new StringBuffer("{");
            for(String group : groups){
                toString.append(", groups : " + group);
            }
            toString.append("}");
            return toString.toString();
        }

        public void debug(String message){
            System.out.println("JSR-322 [RA] [MyPlainSICWithGPC]: " + message);
        }

}
