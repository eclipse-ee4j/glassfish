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

package com.sun.enterprise.admin.remote;

import com.sun.enterprise.universal.collections.ManifestUtils;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 *
 * @author bnevins
 */
class PlainTextManager implements ResponseManager {
    private static final LocalStringsImpl strings = new LocalStringsImpl(PlainTextManager.class);

    PlainTextManager(String response) throws RemoteException {
        this.response = response;
    }

    public void process() throws RemoteException {
        // format:
        // "PlainTextActionReporterSUCCESS..."
        // or
        // "PlainTextActionReporterFAILURE..."
        String good = MAGIC + SUCCESS;
        String bad = MAGIC + FAILURE;

        response = ManifestUtils.decode(response);

        if (response.startsWith(good)) {
            throw new RemoteSuccessException(response.substring(good.length()));
        } else if (response.startsWith(bad)) {
            throw new RemoteSuccessException(response.substring(bad.length()));
        } else {
            throw new RemoteFailureException(strings.get("unknownFormat", response));
        }
    }

    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";
    private static final String MAGIC = "PlainTextActionReporter";
    private String response;
}
