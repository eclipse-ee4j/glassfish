/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import javax.security.auth.Subject;
import java.security.Principal;

import org.glassfish.security.common.PrincipalImpl;
import org.glassfish.security.common.Group;

public class SubjectUtil {

    /**
     * Utility method to find the user names from a subject. The method assumes the user name is
     * represented by {@link org.glassfish.security.common.PrincipalImpl PrincipalImpl } inside the Subject's principal set.
     * @param subject the subject from which to find the user name
     * @return a list of strings representing the user name. The list may have more than one entry if the subject's principal set
     * contains more than one PrincipalImpl instances, or empty entry (i.e., anonymous user) if the subject's principal set contains no PrincipalImpl instances.
     */
    public static List<String> getUsernamesFromSubject(Subject subject) {

        List<String> userList = new ArrayList<>();

        Set<Principal> princSet = null;

        if (subject != null) {

            princSet = subject.getPrincipals();
            for (Principal p : princSet) {
                if ((p != null)
                    && (p.getClass().isAssignableFrom(PrincipalImpl.class)
                    || "weblogic.security.principal.WLSUserImpl".equals(p.getClass().getCanonicalName()))) {
                    String uName = p.getName();
                    userList.add(uName);
                }
            }
        }

        return userList;
    }


    /**
     * Utility method to find the group names from a subject. The method assumes the group name is
     * represented by {@link org.glassfish.security.common.Group Group } inside the Subject's principal set.
     * @param subject the subject from which to find the username
     * @return a list of strings representing the group names. The list may have more than one entry if the subject's principal set
     * contains more than one Group instances, or empty entry if the subject's principal set contains no Group instances.
     */
    public static List<String> getGroupnamesFromSubject(Subject subject) {

        List<String> groupList = new ArrayList<>();

        Set<Group> princSet = null;

        if (subject != null) {

            princSet = subject.getPrincipals(Group.class);
            for (PrincipalImpl g : princSet) {
                String gName = g.getName();
                groupList.add(gName);
            }
        }

        return groupList;
    }

}
