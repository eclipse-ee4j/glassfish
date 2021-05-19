/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.tools.apt;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

/**
 * State file for annotaton processor, 1 per service type, maintains
 * the list of implementors of that service type.
 *
 */
final class ServiceFileInfo {

    private final String serviceName;
    private Set<String> implementors;
    private Writer writer = null;

    public ServiceFileInfo(String serviceName, Set<String> initialImplementors) {
        this.serviceName = serviceName;
        this.implementors = initialImplementors;
    }

    public boolean isDirty() {
        return writer!=null;
    }

    public void createFile(ProcessingEnvironment env) throws IOException {
        // create the file at this time.
        if (writer==null) {
            writer = env.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "META-INF/services", serviceName).openWriter();
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public Set<String> getImplementors() {
        return implementors;
    }

    public Writer getWriter() {
        return writer;
    }
}
