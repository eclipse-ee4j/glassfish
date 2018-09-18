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

/*
 * EARFileUsesClassPath.java
 *
 * Created on August 30, 2004, 9:15 AM
 */

package com.sun.enterprise.tools.verifier.tests.app;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.tools.verifier.Result;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 *
 * @author  ss141213
 * An ear file should not contain Class-Path reference.
 * This as per J2EE 1.4 spec section#8.2. Contents from spec is given below...
 * A JAR format file (such as a .jar file, .war file, or .rar file)
 * can reference a .jar file by naming the referenced .jar file in a Class-Path header
 * in the referencing Jar file s Manifest file.
 */

public class EARFileUsesClassPath  extends ApplicationTest implements AppCheck {
    public Result check(Application descriptor){
        Result result = getInitializedResult();
        result.setStatus(Result.FAILED);
        try{
            Manifest manifest=getVerifierContext().getAbstractArchive().getManifest();
            String cp=null;
            if (manifest!=null)
                cp=manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
            if(cp==null || cp.length()==0){
                result.passed(smh.getLocalString(getClass().getName() + ".passed",
                    "Manifest file of this EAR file does not contain any Class-Path entry."));
            }else{
                result.failed(smh.getLocalString(getClass().getName() + ".failed",
                    "Manifest file of this EAR file contains [ {0} ] as the Class-Path entry.",
                    new Object[]{cp}));
            }
        }catch(IOException e){
            result.addErrorDetails(e.toString());
        }
        return result;
    }
}
