<!--

    Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/">
        <table border="1">
            <tr bgcolor="#9acd32">
                <th>Status</th>
                <th>Test</th>
                <th>Description</th>
            </tr>
            <xsl:for-each select="tests/test">
                <tr>
                    <xsl:choose>
                        <xsl:when test="result/@status = 'PASSED'">
                            <td bgcolor="green">[PASSED] </td>
                        </xsl:when>
                        <xsl:otherwise>
                            <td bgcolor="red">[FAILED] </td>
                        </xsl:otherwise>
                    </xsl:choose>
                    <td><xsl:value-of select="@name"/> </td>
                    <td><xsl:value-of select="@description"/></td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>
</xsl:stylesheet>
