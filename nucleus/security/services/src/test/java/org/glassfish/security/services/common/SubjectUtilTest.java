/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.security.services.common;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;

import org.glassfish.security.common.PrincipalImpl;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class SubjectUtilTest {

    private static final String USERNAME = "john";
    private static final String USERNAME2 = "John";
    private static final String[] GROUPS = {"g1", "g2"};

    @Test
    public void testUserNameUtil() {
        Subject sub = createSub(USERNAME, GROUPS);
        List<String> usernames = SubjectUtil.getUsernamesFromSubject(sub);
        assertThat(usernames, hasSize(1));
    }


    @Test
    public void testGroupNameUtil() {
        Subject sub = createSub(USERNAME, GROUPS);
        List<String> groupnames = SubjectUtil.getGroupnamesFromSubject(sub);
        assertThat(groupnames, hasSize(2));
    }

    @Test
    public void testUserNameUtil_empty() {
        Subject sub = createSub(null, GROUPS);
        List<String> usernames = SubjectUtil.getUsernamesFromSubject(sub);
        assertThat(usernames, hasSize(0));
    }


    @Test
    public void testGroupNameUtil_empty() {
        Subject sub = createSub(USERNAME, null);
        List<String> groupnames = SubjectUtil.getGroupnamesFromSubject(sub);
        assertThat(groupnames, hasSize(0));

    }

    @Test
    public void testUserNameUtil_multi() {
        Subject sub = createSub(USERNAME, GROUPS);
        sub.getPrincipals().add(new PrincipalImpl(USERNAME2));
        List<String> usernames = SubjectUtil.getUsernamesFromSubject(sub);
        assertThat(usernames, hasSize(2));
    }

    private static Subject createSub(String username, String[] groups) {
        Set<Principal> pset = new HashSet<>();
        if (username != null) {
            Principal u = new PrincipalImpl(username);
            pset.add(u);
        }

        if (groups != null) {
            for (String g : groups) {
                if (g != null) {
                    Principal p = new org.glassfish.security.common.Group(g);
                    pset.add(p);
                }
            }
        }

        Set<?> prvSet = new HashSet<>();
        Set<?> pubSet = new HashSet<>();
        Subject sub = new Subject(false, pset, pubSet, prvSet);
        return sub;
    }
}
