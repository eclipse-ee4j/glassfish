<?xml version="1.0" ?>
<!--

    Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.

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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	        xmlns:fo="http://www.w3.org/1999/XSL/Format"
		version="1.0" >
    <xsl:output method="text" indent="yes"/>
    
    <xsl:template match="/">
	<xsl:apply-templates />
    </xsl:template>
    <xsl:template match="static-verification">
          ---------------------------
          STATIC VERIFICATION RESULTS
          ---------------------------

	<xsl:apply-templates select="failure-count" />

        <xsl:apply-templates select="error" />
        
        <xsl:apply-templates select="application" />

        <xsl:apply-templates select="appclient" />
        
        <xsl:apply-templates select="ejb" />
	
        <xsl:apply-templates select="web" />
        
        <xsl:apply-templates select="connector" />
	
        <xsl:apply-templates select="other" />
        
          ----------------------------------
          END OF STATIC VERIFICATION RESULTS
          ----------------------------------
    </xsl:template>


    <!-- NOW LIST ALL THE PATTERN RULES-->
    <xsl:template match="application">

          -------------------------------------
          RESULTS FOR APPLICATION-RELATED TESTS
          -------------------------------------
	<xsl:apply-templates/>
    </xsl:template>


    <xsl:template match="appclient">

          -----------------------------------
          RESULTS FOR APPCLIENT-RELATED TESTS
          -----------------------------------
	<xsl:apply-templates/>
    </xsl:template>


    <xsl:template match="ejb">

          -----------------------------
          RESULTS FOR EJB-RELATED TESTS
          -----------------------------
	<xsl:apply-templates/>
    </xsl:template>

    
    <xsl:template match="web">

          -----------------------------
          RESULTS FOR WEB-RELATED TESTS
          -----------------------------
	<xsl:apply-templates/>
    </xsl:template>
    
    
    <xsl:template match="connector">

          -----------------------------------
          RESULTS FOR CONNECTOR-RELATED TESTS
          -----------------------------------
	<xsl:apply-templates/>
    </xsl:template>
    
    
    <xsl:template match="other">

          -----------------------------------
          RESULTS FOR OTHER XML-RELATED TESTS
          -----------------------------------
	<xsl:apply-templates/>
    </xsl:template>

    
    <xsl:template match="error">

          -----------------------------------------------------
          ERRORS THAT OCCURRED WHILE RUNNING STATIC VERIFICATION
          ----------------------------------------------------- 
	<xsl:apply-templates/>
    </xsl:template>


    <xsl:template match="error-name">
	Error Name : <xsl:value-of select="." />
    </xsl:template>


    <xsl:template match="error-description">
	Error Description : <xsl:value-of select="." />
    </xsl:template>


    <xsl:template match="failure-count">
	 ----------------------------------
	 NUMBER OF FAILURES/WARNINGS/ERRORS
	 ----------------------------------
	 # of Failures : <xsl:value-of select="failure-number" />
         # of Warnings : <xsl:value-of select="warning-number" />
	 # of Errors : <xsl:value-of select="error-number" />

    </xsl:template>

    
    <xsl:template match="failed">
	 --------------
	 FAILED TESTS : 
	 --------------
	<xsl:apply-templates/>
    </xsl:template>


    <xsl:template match="passed">
	 ---------------
	 PASSED TESTS :
	 ---------------
	<xsl:apply-templates/>
    </xsl:template>

    
    <xsl:template match="warning">
	 -----------
	 WARNINGS :
	 -----------
	<xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="not-applicable">
	 ---------------------
	 NOTAPPLICABLE TESTS :
	 ---------------------
	<xsl:apply-templates/>
    </xsl:template>

    
    <xsl:template match="test">
	 Test Name : <xsl:value-of select="test-name" />
	 Test Assertion : <xsl:value-of select="test-assertion" />
	 Test Description : <xsl:value-of select="test-description" />
    </xsl:template>

    
</xsl:stylesheet>
