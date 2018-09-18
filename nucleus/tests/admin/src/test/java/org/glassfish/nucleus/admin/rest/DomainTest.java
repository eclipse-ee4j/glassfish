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

package org.glassfish.nucleus.admin.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.ws.rs.core.Response;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

public class DomainTest extends RestTestBase {
    @Test
    public void testDomainGet() throws IOException {
        Map payload = new HashMap();
        Map<String, String> current = getEntityValues(get("/domain"));

        // Select a random locale so we're not setting the locale to its current value
        List<String> locales = new ArrayList<String>() {{
            add("en_US");
            add("en");
            add("de_DE");
            add("_GB");
            add("en_US_WIN");
            add("de__POSIX");
            add("fr__MAC");
        }};
        locales.remove(current.get("locale"));
        final int random = new Random().nextInt(locales.size());
        String newLocale = locales.get(random);

        payload.put("locale", newLocale);

        Response response = post("/domain", payload);
        assertTrue(isSuccess(response));

        // Reload the domain and make sure our new locale was saved
        Map<String, String> map = getEntityValues(this.get("/domain"));
        assertEquals(newLocale, map.get("locale"));
    }
}
