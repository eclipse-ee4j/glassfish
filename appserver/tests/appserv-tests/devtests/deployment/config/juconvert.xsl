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
<xsl:output method="html"/>
<xsl:template match="/">
<xsl:for-each select="testsuites/testsuite">  
<xsl:choose>
<xsl:when test="@errors + @failures = 0">
[PASSED] </xsl:when>
<xsl:otherwise>
[FAILED] </xsl:otherwise>
</xsl:choose>
 <xsl:value-of select="properties/property[@name='ant.project.name']/@value"/> : <xsl:for-each select="testcase">
<xsl:value-of select="@name"/>, </xsl:for-each>

</xsl:for-each>

</xsl:template>

</xsl:stylesheet>
