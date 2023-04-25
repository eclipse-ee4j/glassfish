/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.auth.realm;

import static com.sun.enterprise.util.Utility.isEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author Kumar
 */
public class GroupMapper {

    private Map<String, List<String>> groupMappingTable = new HashMap<>();

    public void parse(String mappingStr) {
        StringTokenizer tokenizer = new StringTokenizer(mappingStr, ";");
        while (tokenizer.hasMoreElements()) {
            String mapping = tokenizer.nextToken();
            String[] mappingGroups = mapping.split(",");
            String mappedGroup = null;
            int indexOfArrow = mapping.indexOf("->");
            if (indexOfArrow > 0 && mappingGroups != null && (mappingGroups.length > 0)) {
                String tmpGroup = mapping.substring(indexOfArrow + 2);
                mappedGroup = tmpGroup.trim();
            }

            validate(mappedGroup, mappingGroups);

            for (String grp : mappingGroups) {
                int aIndex = grp.indexOf("->");
                String theGroup = null;
                if (aIndex > 0) {
                    String tGrp = grp.substring(0, aIndex);
                    theGroup = tGrp.trim();
                } else {
                    theGroup = grp.trim();
                }

                List<String> mappedGroupList = groupMappingTable.get(theGroup);
                if (mappedGroupList == null) {
                    mappedGroupList = new ArrayList<String>();
                }
                mappedGroupList.add(mappedGroup);

                groupMappingTable.put(theGroup, mappedGroupList);
            }
        }
    }

    public void getMappedGroups(String group, List<String> targetMappedGroups) {
        if (targetMappedGroups == null) {
            throw new RuntimeException("result argument cannot be NULL");
        }

        List<String> sourceMappedGroups = groupMappingTable.get(group);
        if (isEmpty(sourceMappedGroups)) {
            return;
        }

        addUnique(targetMappedGroups, sourceMappedGroups);

        // Look for transitive closure
        List<String> result1 = new ArrayList<>();
        for (String mappedGroup : sourceMappedGroups) {
            getMappedGroups(group, mappedGroup, result1);
        }

        addUnique(targetMappedGroups, result1);
    }

    private void addUnique(List<String> dest, List<String> src) {
        for (String str : src) {
            if (!dest.contains(str)) {
                dest.add(str);
            }
        }
    }

    /**
     * @param args the command line arguments
     *
     * public static void main(String[] args) { // TODO code application logic here GroupMapper mapper = new GroupMapper();
     * mapper.parse(mappingStr); mapper.traverse(); }
     */

    private void getMappedGroups(String group, String str, List<String> result) {
        List<String> mappedGroups = groupMappingTable.get(str);
        if (isEmpty(mappedGroups)) {
            return;
        }

        if (mappedGroups.contains(group)) {
            throw new RuntimeException("Illegal Mapping: cycle detected with group'" + group);
        }

        addUnique(result, mappedGroups);

        for (String mappedGroup : mappedGroups) {
            getMappedGroups(group, mappedGroup, result);
        }
    }

    private void validate(String mappedGroup, String[] mappingGroups) {
        for (String mappingGroup : mappingGroups) {
            int aIndex = mappingGroup.indexOf("->");
            String theGroup = null;
            if (aIndex > 0) {
                theGroup = mappingGroup.substring(0, aIndex);
            } else {
                theGroup = mappingGroup;
            }

            if (theGroup.equals(mappedGroup)) {
                throw new RuntimeException("Illegal Mapping: Identity Mapping of group '" + theGroup + "' to '" + theGroup + "'");
            }
        }
    }
}
