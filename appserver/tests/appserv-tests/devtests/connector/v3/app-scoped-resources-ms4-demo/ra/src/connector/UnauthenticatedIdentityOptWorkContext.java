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

import org.glassfish.security.common.PrincipalImpl;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.Subject;
import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.resource.spi.work.SecurityContext;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;


public class UnauthenticatedIdentityOptWorkContext extends SecurityContext {

    private String userName;
    private String password;
    private String principalName;
    private boolean translationRequired;
    private Subject subject;
    private boolean principal;

    public UnauthenticatedIdentityOptWorkContext(boolean translationRequired, boolean principal, String principalName, String userName, String password) {
        this.translationRequired = translationRequired;
        this.principal = principal;
        this.userName = userName;
        this.password = password;
        this.principalName = principalName;
    }

    public boolean isTranslationRequired() {
        return translationRequired;
    }

    public void setupSecurityContext(CallbackHandler callbackHandler, Subject execSubject, Subject serviceSubject) {

        List<Callback> callbacks = new ArrayList<Callback>();

        //if (!translationRequired) {
            if (principal) {
                CallerPrincipalCallback cpc = null;
                if(principalName != null){
                    cpc = new CallerPrincipalCallback(execSubject, new PrincipalImpl(principalName));
                    callbacks.add(cpc);
                    debug("setting caller principal callback with principal : " + principalName);
                }else{
                    execSubject.getPrincipals().add(new PrincipalImpl(principalName));
                    debug("setting principal for execSubject : " + principalName);
                }
            } else {
                //empty execSubject
                //do nothing
            }
        //}
        addCallbackHandlers(callbacks, execSubject);

        Callback callbackArray[] = new Callback[callbacks.size()];
        try {
            callbackHandler.handle(callbacks.toArray(callbackArray));

        } catch (UnsupportedCallbackException e) {
            debug("exception occured : " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            debug("exception occured : " + e.getMessage());
        }
            //debug("Password validation callback succeded for user : " + userName);
    }

    protected void addCallbackHandlers(List<Callback> callbacks, Subject execSubject) {
        //do nothing
        //hook to test Dupilcate Inflow Context behavior
    }

    public Subject getSubject() {
        //if (translationRequired) {
            if (subject == null) {
                subject = new Subject();
                subject.getPrincipals().add(new PrincipalImpl(principalName));
                debug("setting translation required for principal : " + principalName);
            }
            return subject;
        /*} else {
            return null;
        }*/
    }

    public String toString() {
        StringBuffer toString = new StringBuffer("{");
        toString.append("userName : " + userName);
        toString.append(", password : " + password);
        toString.append(", principalName : " + principalName);
        toString.append(", translationRequired : " + translationRequired);
        toString.append("}");
        return toString.toString();
    }

    public void debug(String message) {
        System.out.println("JSR-322 [RA] [MySecurityContext]: " + message);
    }

}
