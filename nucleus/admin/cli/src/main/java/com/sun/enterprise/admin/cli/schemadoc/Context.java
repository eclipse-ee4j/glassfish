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

package com.sun.enterprise.admin.cli.schemadoc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Context {
    private Boolean showSubclasses;
    private Boolean showDeprecated;
    private File docDir;
    private Map<String, ClassDef> classDefs = new HashMap<String, ClassDef>();
    private String rootClassName;

    public Context(Map<String, ClassDef> classDefs, File docDir, Boolean showDeprecated, Boolean showSubclasses, String className) {
        this.classDefs = classDefs;
        this.docDir = docDir;
        this.showDeprecated = showDeprecated;
        this.showSubclasses = showSubclasses;
        rootClassName = className;
    }

    public Map<String, ClassDef> getClassDefs() {
        return classDefs;
    }

    public File getDocDir() {
        return docDir;
    }

    public Boolean getShowDeprecated() {
        return showDeprecated;
    }

    public Boolean getShowSubclasses() {
        return showSubclasses;
    }

    public String getRootClassName() {
        return rootClassName;
    }
}
