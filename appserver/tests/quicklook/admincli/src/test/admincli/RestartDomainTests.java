/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.admincli;

import org.testng.annotations.Test;
import org.testng.Assert;

import java.io.*;
import java.net.*;
import java.util.*;

public class RestartDomainTests {

    private BufferedReader in = null;
    private String outPutFile=null, expectedOutPut=null, testErr=null;
    String host=System.getProperty("http.host");
    String port=System.getProperty("http.port");

    @Test
    public void restartDomainTest() throws Exception {
    outPutFile = "admincli-restart.output";
    expectedOutPut = "Command restart-domain executed successfully.";
    testErr = "Restart domain failed.";
    parseTestResults(outPutFile, expectedOutPut, testErr);
    }

    public void parseTestResults(String outPutFile, String expectedOutPut, String testErr) throws Exception {
       boolean result=false;
       File dir1 = new File (".");
       String fileName = dir1.getCanonicalPath()+"/"+outPutFile;
       this.expectedOutPut = expectedOutPut;
       this.testErr = testErr;
       //System.out.println("Parsing output file: "+fileName );
       try {
           in = new BufferedReader(new FileReader(fileName));
       } catch (FileNotFoundException e) {
           System.out.println("Could not open file " + fileName + " for reading ");
       }

       if(in != null) {
           String line;
           String testLine = null;
           try {
              while (( line = in.readLine() )  != null ) {
                //System.out.println("The line read is: " + line);
                if(line.indexOf(expectedOutPut)!=-1){
                  result=true;
                  testLine = line;
                  //System.out.println(testLine);
        }
          }
              Assert.assertEquals(result, true, testErr);
           }catch(Exception e){
              e.printStackTrace();
              throw new Exception(e);
         }
       }
    }
}
