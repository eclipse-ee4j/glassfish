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
 * OptPkgDependencySatisfied.java
 *
 * Created on August 30, 2004, 9:39 AM
 */

package com.sun.enterprise.tools.verifier.tests.app;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.apiscan.packaging.ExtensionRef;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author  ss141213
 * This checks to see that all the installed optional packages that this ear depends on are indeed
 * available in the system or not. 
 */
public class OptPkgDependencySatisfied  extends ApplicationTest implements AppCheck {
    public Result check(Application descriptor){
        Result result = getInitializedResult();
        try{
            String earURI=getAbstractArchiveUri(descriptor);
            com.sun.enterprise.tools.verifier.apiscan.packaging.Archive jar=new com.sun.enterprise.tools.verifier.apiscan.packaging.Archive(new File(earURI));
            //See we do not care about bundled opt packages, as for an ear file
            //there should not be any Class-Path entry.
            com.sun.enterprise.tools.verifier.apiscan.packaging.ExtensionRef[] extRefs=jar.getExtensionRefs();
            com.sun.enterprise.tools.verifier.apiscan.packaging.Archive[] allOptPkgs=com.sun.enterprise.tools.verifier.apiscan.packaging.Archive.getAllOptPkgsInstalledInJRE();
            ArrayList<ExtensionRef> notFounds=new ArrayList<ExtensionRef>();
            for(int i=0;i<extRefs.length;++i){
                ExtensionRef ref=extRefs[i];
                boolean found=false;
                for(int j=0;j<allOptPkgs.length;++j){
                    if(ref.isSatisfiedBy(allOptPkgs[j])) {
                        found=true;
                        break;
                    }
                }
                if(!found) notFounds.add(ref);
            }//for

            if(notFounds.isEmpty()){
                result.passed(smh.getLocalString(getClass().getName() + ".passed",
                                             "All opt package dependency satisfied for this ear file."));
                result.passed("");
            }else{
                result.failed(smh.getLocalString(getClass().getName() + ".failed","Some dependencies could not be satisfied for this ear file. See info below..."));
                for(Iterator i=notFounds.iterator();i.hasNext();){
                    result.addErrorDetails(i.next().toString());
                }
            }
        }catch(IOException e){
            result.failed(e.toString());
        }
        return result;
    }
}
