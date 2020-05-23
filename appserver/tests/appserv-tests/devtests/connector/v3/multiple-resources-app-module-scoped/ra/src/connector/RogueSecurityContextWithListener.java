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

import javax.security.auth.callback.Callback;
import javax.security.auth.Subject;
import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.auth.message.callback.PasswordValidationCallback;
import java.util.List;

public class RogueSecurityContextWithListener extends MySecurityContextWithListener{
    public RogueSecurityContextWithListener(String userName, String password, String principalName) {
        super(userName, password, principalName, true, true, false); //with translationRequired
    }

    protected void addCallbackHandlers(List<Callback> callbacks, Subject execSubject) {

        //when translation required is ON, PasswordValidationCallback can't be used
        PasswordValidationCallback pvc = null;
        pvc = new PasswordValidationCallback(execSubject, userName, password.toCharArray());
            debug("setting password validation callback with user [ " + userName + " ] + password [ " + password + " ]");
            callbacks.add(pvc);
        debug("setting Password Principal Callback for : Case-II - translation required");

/*
        String principalName = "xyz";

        CallerPrincipalCallback cpc = new CallerPrincipalCallback(execSubject, new PrincipalImpl(principalName));
        debug("setting caller principal callback with principal : " + principalName);
*/
        callbacks.add(pvc);
    }

    public void debug(String message){
        System.out.println("JSR-322 [RA] [RogueSecurityContextWithListener]: " + message);
    }
}
