/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import static com.sun.enterprise.util.StringUtils.ok;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.io.FileUtils;

/**
 * This class is responsible for handling the Remote Server response. Note that an unusul paradigm is used here. Success
 * is signaled by throwing a "success" exception. This breaks the overarching rule about Exceptions but is very useful
 * in CLI. CLI has the pattern of: Error: Throw an Exception Success: Don't throw an Exception The logic becomes
 * difficult. The command itself has to know how to print a success message properly instead of just putting such a
 * message inside an Exception object and throwing it. In such a system it is cleaner to do this: Error: throw failure
 * exception Success: throw success exception
 *
 * @author bnevins
 */
public class RemoteResponseManager implements ResponseManager {

    private static final LocalStringsImpl strings = new LocalStringsImpl(RemoteResponseManager.class);
    private static final int HTTP_SUCCESS_CODE = 200;

    private final int code;
    private final Logger logger;
    private final InputStream responseStream;
    private final String response;
    private Map<String, String> mainAtts = Collections.emptyMap();

    public RemoteResponseManager(InputStream in, int code, Logger logger) throws RemoteException, IOException {
        this.code = code;
        this.logger = logger;

        // make a copy of the stream.  O/w if Manifest.read() blows up -- the
        // data would be gone!
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtils.copy(in, baos);

        responseStream = new ByteArrayInputStream(baos.toByteArray());
        response = baos.toString();

        if (!ok(response)) {
            throw new RemoteFailureException(strings.get("emptyResponse"));
        }

        logger.finer("------- RAW RESPONSE  ---------");
        logger.finer(response);
        logger.finer("------- RAW RESPONSE  ---------");
    }

    @Override
    public void process() throws RemoteException {
        checkCode(); // Exception == Goodbye!
        try {
            handleManifest();
        } catch (RemoteFailureException e) {
            // Manifest obj was ok -- remote failure
            throw e;
        } catch (IOException e) {
            // ignore -- move on to Plain Text...
        }
        // put a try around this if another type of response is added...
        handlePlainText();
        throw new RemoteFailureException(strings.get("internal", response));
    }

    public Map<String, String> getMainAtts() {
        return mainAtts;
    }

    private void checkCode() throws RemoteFailureException {
        if (code != HTTP_SUCCESS_CODE) {
            throw new RemoteFailureException(strings.get("badHttpCode", code));
        }
    }

    private void handleManifest() throws RemoteException, IOException {
        ManifestManager mgr = new ManifestManager(responseStream, logger);
        mainAtts = mgr.getMainAtts();
        mgr.process();
    }

    private void handlePlainText() throws RemoteException {
        PlainTextManager mgr = new PlainTextManager(response);
        mgr.process();
    }
}
