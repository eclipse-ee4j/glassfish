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

package com.sun.enterprise.security.auth.realm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author Kumar
 */
public class GroupMapper {

    private Map<String, ArrayList<String>> groupMappingTable = new HashMap<String, ArrayList<String>>();

    public void parse(String mappingStr) {
        StringTokenizer tokenizer = new StringTokenizer(mappingStr, ";");
        while(tokenizer.hasMoreElements()) {
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
                ArrayList<String> mappedGroupList = groupMappingTable.get(theGroup);
                if (mappedGroupList == null) {
                    mappedGroupList = new ArrayList<String>();
                }
                mappedGroupList.add(mappedGroup);
                groupMappingTable.put(theGroup, mappedGroupList);
            }
        }
    }

    public void getMappedGroups(String group, ArrayList<String> result) {
        if (result == null) {
            throw new RuntimeException("result argument cannot be NULL");
        }
        ArrayList<String> mappedGrps = groupMappingTable.get(group);
        if (mappedGrps == null || mappedGrps.isEmpty()) {
            return;
        }
        addUnique(result,mappedGrps);
        //look for transitive closure
        ArrayList<String> result1 = new ArrayList<String>();
        for (String str : mappedGrps) {
            getMappedGroups(group, str,result1);
        }
        addUnique(result, result1);
    }

    private void addUnique(ArrayList<String> dest, ArrayList<String> src) {
        for (String str : src) {
            if (!dest.contains(str)) {
                dest.add(str);
            }
        }
    }
    /*
    public void traverse() {
        Iterator<String> it = groupMappingTable.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next();
            System.out.println();
            System.out.print( key + "<<<Is Mapped to>>>");
            ArrayList<String> list = new ArrayList<String>();
            getMappedGroups(key, list);
            if (list != null) {
                for (String str : list) {
                    System.out.print(str + ", ");
                }
            }
            System.out.println();
        }
    }*/
    /**
     * @param args the command line arguments

    public static void main(String[] args) {
        // TODO code application logic here
        GroupMapper mapper = new GroupMapper();
        mapper.parse(mappingStr);
        mapper.traverse();
    }*/

    private void getMappedGroups(String group, String str, ArrayList<String> result) {

        ArrayList<String> mappedGrps = groupMappingTable.get(str);
        if (mappedGrps == null || mappedGrps.isEmpty()) {
            return;
        }
        if (mappedGrps.contains(group)) {
            throw new RuntimeException("Illegal Mapping: cycle detected with group'" + group);
        }
        addUnique(result,mappedGrps);
        for (String str1 : mappedGrps) {
            getMappedGroups(group, str1,result);
        }
    }

    private void validate(String mappedGroup, String[] mappingGroups) {
        for (String str : mappingGroups) {
            int aIndex = str.indexOf("->");
            String theGroup = null;
            if (aIndex > 0) {
                theGroup = str.substring(0, aIndex);
            } else {
                theGroup = str;
            }
            if (theGroup.equals(mappedGroup)) {
                throw new RuntimeException("Illegal Mapping: Identity Mapping of group '" + theGroup + "' to '" + theGroup + "'");
            }
        }
    }
}
