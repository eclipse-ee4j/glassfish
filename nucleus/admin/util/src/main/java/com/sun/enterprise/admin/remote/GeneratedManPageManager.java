/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.universal.NameValue;
import com.sun.enterprise.universal.glassfish.AdminCommandResponse;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author bnevins
 */
class GeneratedManPageManager implements ResponseManager {
    GeneratedManPageManager(AdminCommandResponse response) {
        this.response = response;
    }

    public void process() throws RemoteException {
        String usageText = response.getValue(AdminCommandResponse.SYNOPSIS);

        if (usageText == null) {
            // this is one way to figure out there was an error!
            throw new RemoteFailureException("XXXXXXXXXXXXXXXXXXXX", response.getMainMessage());
        }
        setName();
        setSynopsis();
        setParamsAndOperands();
        StringBuilder sb = new StringBuilder();
        sb.append(EOL);
        printName(sb);
        printSynopsis(sb);
        printParams(sb);
        printOperands(sb);
        throw new RemoteSuccessException(sb.toString());
    }

    private void setName() {
        name = displayInProperLen(response.getMainMessage());
    }

    private void printName(StringBuilder sb) {
        sb.append("NAME :").append(EOL);
        sb.append(name).append(EOL);
    }

    private void setSynopsis() {
        synopsis = response.getValue(AdminCommandResponse.SYNOPSIS);

        if (synopsis.startsWith("Usage: ")) {
            synopsis = synopsis.substring(7);
        }
        // this looks too horrible - go with one long line...
        //synopsis = displayInProperLen(synopsis);
    }

    private void printSynopsis(StringBuilder sb) {
        sb.append("SYNOPSIS :").append(EOL);
        sb.append(TAB).append(synopsis).append(EOL);
        sb.append(EOL);
    }

    private void setParamsAndOperands() {
        List<NameValue<String, String>> list = response.getMainKeys();

        for (NameValue<String, String> nv : list) {
            String name = nv.getName();
            if (name.equals(AdminCommandResponse.SYNOPSIS))
                continue;
            if (name.endsWith("operand"))
                operands.add(nv);
            else
                params.add(nv);
        }
    }

    private void printParams(StringBuilder sb) {
        sb.append("OPTIONS :").append(EOL);

        for (NameValue<String, String> nv : params) {
            sb.append(TAB + "--").append(nv.getName()).append(EOL);
            sb.append(displayInProperLen(nv.getValue()));
            sb.append(EOL);
        }
    }

    private void printOperands(StringBuilder sb) {

        sb.append("OPERANDS :").append(EOL);

        for (NameValue<String, String> nv : operands) {
            String key = nv.getName();
            // peel off "_operand"
            key = key.substring(0, key.length() - 8);
            String value = nv.getValue();
            sb.append(displayInProperLen(key + " - " + value));
            sb.append(EOL);
        }
    }

    // bnevins -- original code reused, this looks painful to change...
    private String displayInProperLen(String strToDisplay) {
        int index = 0;
        StringBuilder sb = new StringBuilder();

        for (int ii = 0; ii + 70 < strToDisplay.length(); ii += 70) {
            index = ii + 70;
            String subStr = strToDisplay.substring(ii, index + 1);
            if (subStr.endsWith(" ") || subStr.endsWith(",") || subStr.endsWith(".") || subStr.endsWith("-")) {
                sb.append(TAB + subStr + EOL);
                ii++;
                index++;
            } else {
                sb.append(TAB + strToDisplay.substring(ii, index) + "-" + EOL);
            }
        }
        if (index < strToDisplay.length()) {
            sb.append(TAB + strToDisplay.substring(index) + EOL);
        }

        return sb.toString();
    }

    AdminCommandResponse response;
    private String name;
    private String synopsis;
    List<NameValue<String, String>> params = new LinkedList<NameValue<String, String>>();
    List<NameValue<String, String>> operands = new LinkedList<NameValue<String, String>>();
    private static final String TAB = "    ";
    private static final String EOL = System.getProperty("line.separator");
}
/*
 --------  RESPONSE DUMP         --------------
Signature-Version: 1.0
keys: GeneratedHelp;connectionpoolid;enabled;description;jndi_name_ope
 rand;property;target;SYNOPSYS
enabled_value: Determines whether the JDBC resource is enabled at runt
 ime. The default value is true.
SYNOPSYS_name: SYNOPSYS
target_name: target
message: create-jdbc-resource - creates a JDBC resource with the speci
 fied JNDI name
connectionpoolid_value: The name of the JDBC connection pool. If two o
 r more JDBC resource elements point to the same connection pool eleme
 nt, they use the same pool connection at runtime.
description_value: Text providing descriptive details about the JDBC r
 esource.
property_value:
jndi_name_operand_name: jndi_name_operand
enabled_name: enabled
exit-code: SUCCESS
property_name: property
GeneratedHelp_value: true
SYNOPSYS_value: Usage: create-jdbc-resource --connectionpoolid=connect
 ionpoolid [--enabled=true] [--description=description] [--property=pr
 operty] [--target=server] jndi_name
GeneratedHelp_name: GeneratedHelp
connectionpoolid_name: connectionpoolid
description_name: description
jndi_name_operand_value: The JNDI name of this JDBC resource.
target_value:


----------------------------------------------
NAME :
        create-jdbc-resource - creates a JDBC resource with the specified JNDI
        name

SYNOPSIS :
        create-jdbc-resource --connectionpoolid=connectionpoolid [--enabled=true] [--description=description] [--property=property] [--target=server] jndi_name

OPTIONS :
        --GeneratedHelp
        true

        --connectionpoolid
        The name of the JDBC connection pool. If two or more JDBC resource ele-
        ments point to the same connection pool element, they use the same poo-
        l connection at runtime.

        --enabled
        Determines whether the JDBC resource is enabled at runtime. The defaul-
        t value is true.

        --description
        Text providing descriptive details about the JDBC resource.

        --property

        --target

OPERANDS :
        jndi_name - The JNDI name of this JDBC resource.

Command create-jdbc-resource executed successfully.
 */
