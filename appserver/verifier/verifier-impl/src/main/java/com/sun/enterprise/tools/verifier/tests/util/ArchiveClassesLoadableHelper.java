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

package com.sun.enterprise.tools.verifier.tests.util;

import com.sun.enterprise.tools.verifier.apiscan.classfile.ClosureCompiler;
import com.sun.enterprise.tools.verifier.apiscan.classfile.ClosureCompilerImpl;
import com.sun.enterprise.tools.verifier.StringManagerHelper;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.*;
import java.io.File;

/**
 * This class is a helper around {@link ClosureCompiler}.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ArchiveClassesLoadableHelper {
    /**
     * This method is used to print the result in various
     * *ArchiveClassesLoadable tests.
     * @param cc a closure compiler which provides the necesasry information
     * @return a localized string which contains the details.
     */
    public static String getFailedResult(ClosureCompiler cc){
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Object o : cc.getFailed().entrySet()) {
            Map.Entry<String, List<String>> referencingPathToFailedList =
                    (Map.Entry<String, List<String>>)o;
            LocalStringManagerImpl smh = StringManagerHelper.getLocalStringsManager();
            String classes = "Failed to find following classes:";
            if (smh != null) {
                classes = smh.getLocalString(
                        ArchiveClassesLoadableHelper.class.getName() + ".classes",
                        classes);
            }
            sb.append(classes).append("\n[");
            for (Iterator<String> iii = referencingPathToFailedList.getValue().iterator();
                 iii.hasNext();) {
                sb.append("\n\t").append(iii.next());
                if(iii.hasNext()) sb.append(",");
            }
            sb.append("\n]");
            String referencingPath = referencingPathToFailedList.getKey();
            if(referencingPath.length()==0) continue; // skip if a top level class is not found
            String ref = "referenced in the following call stack :\n";
            String reference = "at";
            if (smh != null) {
               ref = smh.getLocalString(
                        ArchiveClassesLoadableHelper.class.getName() + ".ref",
                        ref);
                reference = smh.getLocalString(
                        ArchiveClassesLoadableHelper.class.getName() + ".reference",
                        reference);
            }
            StringTokenizer st = new StringTokenizer(referencingPath, File.separator);
            Stack<String> referencingClassStack = new Stack<String>();
            while(st.hasMoreTokens()) {
                referencingClassStack.push(st.nextToken());
            }
            if(!referencingClassStack.isEmpty())
                 sb.append("\n\n"+ref);
            while(!referencingClassStack.isEmpty()){
                sb.append("\n\t").append(reference).append(" ");
                sb.append(referencingClassStack.pop());
            }
            sb.append("\n\n");
        }
        return sb.toString();
    }
}

