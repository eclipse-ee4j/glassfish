/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.ejb.remoteview;


import org.testng.annotations.*;
import org.testng.Assert;
import javax.naming.InitialContext;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import remoteview.*;

public class RemoteViewTestNG {
    private String appName="remoteview";

    @Test(groups = { "init" })
    public void helloRemote() throws Exception{
        boolean test_result = false;
        try {
            HelloHome helloHome = (HelloHome) new InitialContext().lookup("java:global/" + appName + "/HelloBean!remoteview.HelloHome");
            callHome(helloHome);

            Hello hello = (Hello) new InitialContext().lookup("HH#remoteview.Hello");
            Future<String> future = hello.helloAsync();
            hello.helloAsync();
            hello.helloAsync();
            hello.helloAsync();
            hello.fireAndForget();

            String result = future.get();
            //System.out.println("helloAsync() says : " + result);
            test_result = true;
        } catch(ExecutionException e) {
            test_result = false;
            System.out.println("Got async ExecutionException.");
            e.printStackTrace();
        }
        Assert.assertEquals(test_result, true,"Unexpected Results");
    }

    @Test(dependsOnGroups = { "init.*" })
    public void portableGlobal() throws Exception{
        boolean test_result = false;
        // Fully-qualified portable global
        try{
            HelloHome helloHome2 = (HelloHome) new InitialContext().lookup("java:global/" + appName + "/HelloBean!remoteview.HelloHome");
            callHome(helloHome2);

            Hello hello2 = (Hello) new InitialContext().lookup("java:global/" + appName + "/HelloBean!remoteview.Hello");
            callBusHome(hello2);
            test_result = true;
        } catch(Exception e) {
            test_result = false;
            System.err.println("Exception from portableGlobal");
            e.printStackTrace();
        }
        Assert.assertEquals(test_result, true,"Unexpected Results");
    }

    @Test(dependsOnGroups = { "init.*" })
    public void nonPortableGlobal() throws Exception{
        boolean test_result = false;
        // non-portable global
        try{
            HelloHome helloHome5 = (HelloHome) new InitialContext().lookup("HH");
            callHome(helloHome5);

            Hello hello5 = (Hello) new InitialContext().lookup("HH#remoteview.Hello");
            callBusHome(hello5);
            test_result = true;
        } catch(Exception e) {
            test_result = false;
            System.err.println("Exception from portableGlobal:");
            e.printStackTrace();
        }
        Assert.assertEquals(test_result, true,"Unexpected Results");
    }

    private static void callHome(HelloHome home) throws Exception {
        HelloRemote hr = home.create();
        System.out.println("2.x HelloRemote.hello() says " + hr.hello());
    }
    private static void callBusHome(Hello h) {
        String hret = h.hello();
        System.out.println("Hello.hello() says " + h.hello());
    }
}
