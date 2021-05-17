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

package org.glassfish.ha.common;

/**
 * @author Mahesh Kannan
 */
public class HACookieManager {

    private static final HACookieManager _manager = new HACookieManager();

    private static InheritableThreadLocal<HACookieInfo> _haCookieInfo
        = new InheritableThreadLocal<HACookieInfo>() {

        @Override
        protected HACookieInfo childValue(HACookieInfo parentValue) {
            return (parentValue != null)
                ? new HACookieInfo(parentValue.getNewReplicaCookie(), parentValue.getOldReplicaCookie())
                : new HACookieInfo(null, null);
        }

        @Override
        protected HACookieInfo initialValue() {
            return new HACookieInfo(null, null);
        }
    };

    public static HACookieInfo getCurrent() {
        return _haCookieInfo.get();
    }

    public static HACookieInfo setCurrrent(HACookieInfo info) {
        HACookieInfo oldInfo = _haCookieInfo.get();
        _haCookieInfo.set(info);

        return oldInfo;
    }

    public static HACookieInfo reset() {
        _haCookieInfo.get().reset();
        return _haCookieInfo.get();
    }

}
