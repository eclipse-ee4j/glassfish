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

import jakarta.resource.spi.work.WorkContextLifecycleListener;


public class MySecurityContextWithListener extends MySecurityContext implements WorkContextLifecycleListener {

    public MySecurityContextWithListener(String userName, String password,
                                               String principalName, boolean translationRequired, boolean expectSuccess, boolean expectPVSuccess){
        super(userName, password, principalName, translationRequired, expectSuccess, expectPVSuccess);
    }

    public void contextSetupComplete() {
        debug("Context setup completed " + this.toString() );
        if(!expectSuccess){
            throw new Error("Container has completed context setup which is not expected");
        }
    }

    public void contextSetupFailed(String string) {
        debug("Context setup failed with the following message : " + string + " for security-inflow-context " +
                this.toString());
        if(expectSuccess){
            throw new Error("Container has not completed context setup");
        }
    }

    public void debug(String message){
        System.out.println("JSR-322 [RA] [MySecurityContextWithListener]: " + message);
    }

}
