/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.devtests.web.gendocschema;

import java.io.File;

import com.sun.appserv.test.BaseDevTest;

public class GenerateDocumentSchema extends BaseDevTest {
    public static void main(String[] args) {
        new GenerateDocumentSchema().run();
    }

    @Override
    protected String getTestName() {
        return "gen-doc-schema";
    }

    @Override
    protected String getTestDescription() {
        return "gen-doc-schema";
    }

    public void run() {
        try {
            report("generate-schema-doc", asadmin("generate-domain-schema"));
            report("verify-report-file",
                new File(System.getenv("S1AS_HOME"), "domains/domain1/config/index.html").exists());
        } finally {
            stat.printSummary();
        }
    }
}
