@echo off
REM
REM  Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

setlocal
set _AS_INSTALL=%~dp0..
call "%_AS_INSTALL%\config\asenv.bat"
REM
REM  Run with the user-specified Java, if any.
REM
if "%AS_JAVA%x" == "x" goto UsePath
set JAVA="%AS_JAVA%\bin\java"
goto run
:UsePath
set JAVA=java
:run
set _AS_INSTALL_LIB=%_AS_INSTALL%\lib
%JAVA% -classpath "%_AS_INSTALL_LIB%\gf-client.jar" org.glassfish.appclient.client.packageappclient.PackageAppClient %*
