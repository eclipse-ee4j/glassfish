@echo off
REM
REM  Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
REM
REM  This program and the accompanying materials are made available under the
REM  terms of the Eclipse Public License v. 2.0, which is available at
REM  http://www.eclipse.org/legal/epl-2.0.
REM
REM  This Source Code may also be made available under the following Secondary
REM  Licenses when the conditions for such availability set forth in the
REM  Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
REM  version 2 with the GNU Classpath Exception, which is available at
REM  https://www.gnu.org/software/classpath/license.html.
REM
REM  SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
REM



set AS_INSTALL_LIB=%~dp0..\modules
set JSP_IMPL=%AS_INSTALL_LIB%\wasp.jar
set EL_IMPL=%AS_INSTALL_LIB%\jakarta.el.jar;%AS_INSTALL_LIB%\jakarta.el-api.jar
set JSTL_IMPL=%AS_INSTALL_LIB%\jakarta.servlet.jsp.jstl.jar
set AS_LIB=%~dp0..\lib
set JAVAEE_API=%AS_LIB%\javaee.jar

java -cp "%JSP_IMPL%;%JAVAEE_API%;%AS_LIB%" org.apache.jasper.JspC -sysClasspath "%JSP_IMPL%;%EL_IMPL%;%JSTL_IMPL%;%JAVAEE_API%;%AS_LIB%" -schemas "/schemas/" -dtds "/dtds/" %*
