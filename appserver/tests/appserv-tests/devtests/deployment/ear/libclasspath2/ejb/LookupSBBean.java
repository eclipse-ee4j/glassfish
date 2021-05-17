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

package libclasspath2.ejb;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import jakarta.ejb.*;
import libclasspath2.ResourceHelper;

/**
 * This is the bean class for the LookupSBBean enterprise bean.
 * Created Nov 11, 2005 2:18:56 PM
 * @author tjquinn
 */
@Stateless()
public class LookupSBBean implements LookupSBRemote {

    public ResourceHelper.Result runTests(String[] tests, ResourceHelper.TestType testType) throws IOException {
        return ResourceHelper.checkAll(tests, testType);
    }
}
