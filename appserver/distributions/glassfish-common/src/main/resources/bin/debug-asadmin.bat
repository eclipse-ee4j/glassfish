@echo off
REM
REM  Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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


REM  Always use JDK 1.6 or higher
REM  Depends on Java from ..\glassfish\config\asenv.bat
VERIFY OTHER 2>nul
setlocal ENABLEEXTENSIONS
if ERRORLEVEL 0 goto ok
echo "Unable to enable extensions"
exit /B 1
:ok
call "%~dp0..\glassfish\config\asenv.bat"
if "%AS_JAVA%x" == "x" goto UsePath
set JAVA="%AS_JAVA%\bin\java"
goto run
:UsePath
set JAVA=java
:run
%JAVA% -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=9008 -jar "%~dp0..\glassfish\lib\client\appserver-cli.jar" %*
