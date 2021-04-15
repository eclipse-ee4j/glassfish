/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.remote.reader;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author martinmares
 */
public class ProprietaryReaderFactory {

    private static final List<ProprietaryReader> proprietaryReaders;
    static {
        proprietaryReaders = new ArrayList<ProprietaryReader>(7);
        proprietaryReaders.add(new StringProprietaryReader());
        ActionReportJsonProprietaryReader rdr = new ActionReportJsonProprietaryReader();
        proprietaryReaders.add(rdr);
        proprietaryReaders.add(new ParamsWithPayloadJsonProprietaryReader(rdr));
        proprietaryReaders.add(new AdminCommandStateJsonProprietaryReader());
        proprietaryReaders.add(new MultipartProprietaryReader(rdr));
        proprietaryReaders.add(new ProgressStatusDTOJsonProprietaryReader());
        proprietaryReaders.add(new ProgressStatusEventJsonProprietaryReader());
    }

    public static <T> ProprietaryReader<T> getReader(final Class<T> type, final String mediaType) {
        for (ProprietaryReader pr : proprietaryReaders) {
            if (pr.isReadable(type, mediaType)) {
                return pr;
            }
        }
        return null;
    }

}
