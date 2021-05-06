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

import java.util.*;

import com.sun.enterprise.security.jauth.*;

public class TestCredential {

    String moduleClass;
    Map options;
    AuthPolicy requestPolicy;
    AuthPolicy responsePolicy;

    public TestCredential(String moduleClass,
                        Map options,
                        AuthPolicy requestPolicy,
                        AuthPolicy responsePolicy) {
        this.moduleClass = moduleClass;
        this.options = options;
        this.requestPolicy = requestPolicy;
        this.responsePolicy = responsePolicy;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TestCredential)) {
            return false;
        }
        TestCredential that = (TestCredential)o;

        if (this.moduleClass.equals(that.moduleClass) &&
            this.options.equals(that.options) &&
            (this.requestPolicy == that.requestPolicy ||
                (this.requestPolicy != null &&
                        this.requestPolicy.equals(that.requestPolicy))) &&
            (this.responsePolicy == that.responsePolicy ||
                (this.responsePolicy != null &&
                        this.responsePolicy.equals(that.responsePolicy)))) {
            return true;
        }

        return false;
    }

    public int hashCode() {
        return moduleClass.hashCode() + options.hashCode();
    }
}
