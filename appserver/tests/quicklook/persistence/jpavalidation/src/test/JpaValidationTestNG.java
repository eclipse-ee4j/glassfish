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

package test.jpa.jpavalidation;


import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.*;
import org.testng.Assert;

import java.io.*;
import java.net.*;
import java.util.*;

public class JpaValidationTestNG {

    private String strContextRoot="/jpavalidation";

    static String result = "";
    String host=System.getProperty("http.host");
    String port=System.getProperty("http.port");

    @Test(groups = { "init" })
    public void initialize() throws Exception{
        boolean result=false;

        try{

          result = test("initialize");
      Assert.assertEquals(result, true, "Unexpected Results");

        }catch(Exception e){

      e.printStackTrace();
      throw new Exception(e);

        }
    }

    @Test(dependsOnGroups = { "init.*" })
    public void validatePersist() throws Exception{
        boolean result=false;

        try{

            result = test("validatePersist");
          Assert.assertEquals(result, true,"Unexpected Results");

        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    @Test(dependsOnMethods = { "validatePersist" })
    public void validateUpdate() throws Exception{
        boolean result=false;

        try{

            result = test("validateUpdate");
          Assert.assertEquals(result, true,"Unexpected Results");

        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    @Test(dependsOnMethods = { "validateUpdate" })
    public void validateRemove() throws Exception{
        boolean result=false;

        try{

            result = test("validateRemove");
          Assert.assertEquals(result, true,"Unexpected Results");

        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    @Test(dependsOnMethods = { "validateRemove" })
    public void verify() throws Exception{
        boolean result=false;

        try{

            result = test("verify");
          Assert.assertEquals(result, true,"Unexpected Results");

        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    private boolean test(String c) throws Exception {
        String EXPECTED_RESPONSE = c + ":pass";
        boolean result=false;
        String url = "http://" + host + ":" + port + strContextRoot +
                     "/test?tc=" + c;
        // System.out.println("url="+url);

        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();
        int code = conn.getResponseCode();
        if (code != 200) {
            System.err.println("Unexpected return code: " + code);
    } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = input.readLine()) != null) {
          if (line.contains(EXPECTED_RESPONSE)) {
                result = true;
        break;
          }
        }

        }
    return result;
    }

    public static void echo(String msg) {
        System.out.println(msg);
    }

}
