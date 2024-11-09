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

package test;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class ReportWatcher extends TestWatcher{

    private SimpleReporterAdapter stat;
    private String testId;

    public ReportWatcher(SimpleReporterAdapter stat, String testId) {
        this.stat=stat;
        this.testId=testId;
    }

    @Override
      protected void starting(Description description){
        stat.addDescription(testId + " " + description.getMethodName());
      }

      @Override
      protected void failed(Throwable e, Description description) {
          stat.addStatus(testId + " " + description.getMethodName() , stat.FAIL);
      }

      @Override
      protected void succeeded(Description description) {
          stat.addStatus(testId + " " + description.getMethodName() , stat.PASS);
      }

      protected void printSummary(){
        stat.printSummary();
      }
}
