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
import jakarta.security.auth.message.callback.PasswordValidationCallback;
import jakarta.security.auth.message.callback.GroupPrincipalCallback;
import jakarta.resource.spi.work.SecurityContext;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;


public class MySecurityContext  extends SecurityContext {

    protected String userName;
    protected String password;
    protected String principalName;
    protected boolean translationRequired;
    protected Subject subject;
    protected boolean expectSuccess = true;
    protected boolean expectPVSuccess = true;

    public MySecurityContext(String userName, String password, String principalName, boolean translationRequired, boolean expectSuccess, boolean expectPasswordValidationSuccess) {
        this.userName = userName;
        this.password = password;
        this.principalName = principalName;
        this.translationRequired = translationRequired;
        this.expectSuccess = expectSuccess;
        this.expectPVSuccess = expectPasswordValidationSuccess;
    }

    public boolean isTranslationRequired() {
        return translationRequired;
    }

    public void setupSecurityContext(CallbackHandler callbackHandler, Subject execSubject, Subject serviceSubject) {

        //execSubject.getPublicCredentials().add(new Group("employee"));
        List<Callback> callbacks = new ArrayList<Callback>();


        CallerPrincipalCallback cpc = new CallerPrincipalCallback(execSubject, new PrincipalImpl(principalName));
        debug("setting caller principal callback with principal : " + principalName);
        callbacks.add(cpc);

/*
        GroupPrincipalCallback gpc = new GroupPrincipalCallback(execSubject, null);
        callbacks.add(gpc);
*/

        PasswordValidationCallback pvc = null;

        if (!translationRequired) {
            pvc = new PasswordValidationCallback(execSubject, userName,
                    password.toCharArray());
            debug("setting password validation callback with user [ " + userName + " ] + password [ " + password + " ]");
            callbacks.add(pvc);
        }

        addCallbackHandlers(callbacks, execSubject);

        Callback callbackArray[] = new Callback[callbacks.size()];
        try {
            callbackHandler.handle(callbacks.toArray(callbackArray));

        } catch (UnsupportedCallbackException e) {
            debug("exception occured : " + e.getMessage());
            e.printStackTrace();
            if(expectSuccess){
                throw new Error("Container has thrown UnsupportedCallbackException");
            }
        } catch (IOException e) {
            e.printStackTrace();
            debug("exception occured : " + e.getMessage());
            if(expectSuccess){
                throw new Error("Container has thrown IOException while handling callbacks");
            }
        }

        if (!translationRequired) {
            if (!pvc.getResult()) {
                debug("Password validation callback failure for user : " + userName);
                //throw new RuntimeException("Password validation callback failed for user " + userName);
                //TODO need to throw exception later (once spec defines it) and fail setup security context
                if(expectPVSuccess){
                    throw new Error("Password validation callback failed for user " + userName);
                }
            } else {
                debug("Password validation callback succeded for user : " + userName);
                if(!expectPVSuccess){
                    throw new Error("Password validation callback failed for user " + userName);
                }
            }
        }
    }

    protected void addCallbackHandlers(List<Callback> callbacks, Subject execSubject) {
        //do nothing
        //hook to test Dupilcate Inflow Context behavior
    }

    public Subject getSubject() {
        if (translationRequired) {
            if (subject == null) {
                subject = new Subject();
                subject.getPrincipals().add(new PrincipalImpl(principalName));
                debug("setting translation required for principal : " + principalName);
            }
            return subject;
        } else {
            return null;
        }
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
