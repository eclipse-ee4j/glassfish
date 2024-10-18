@echo off
REM
REM  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
goto :main

:seekJavaOnPath
for /f %%J in ("java.exe") do set JAVA=%%~$PATH:J
goto :EOF

:chooseJava
rem
rem Looks for Java at AS_JAVA, JAVA_HOME or in the path.
rem Sets javaSearchType to tell which was used to look for java,
rem javaSearchTarget to the location (or path), and
rem JAVA to the found java executable.

    if "%AS_JAVA%x" == "x" goto :checkJAVA_HOME
       set javaSearchType=AS_JAVA
       set javaSearchTarget="%AS_JAVA%"
       set JAVA=%AS_JAVA%\bin\java.exe
       for %%a in ("%AS_JAVA%") do set ACCJavaHome=%%~sa
       goto :verifyJava

:checkJAVA_HOME
    if "%JAVA_HOME%x" == "x" goto :checkPATH
       set javaSearchType=JAVA_HOME
       set javaSearchTarget="%JAVA_HOME%"
       set JAVA=%JAVA_HOME%\bin\java.exe
       for %%a in ("%JAVA_HOME%") do set ACCJavaHome=%%~sa
       goto :verifyJava

:checkPATH
    set JAVA=java
    call :seekJavaOnPath
    set javaSearchType=PATH
    set javaSearchTarget="%PATH%"

:verifyJava
rem
rem Make sure java really exists where we were told to look.  If not
rem display how we tried to find it and then try to run it, letting the shell
rem issue the error so we don't have to do i18n of our own message from the script.
    if EXIST "%JAVA%" goto :EOF
    echo
    echo %javaSearchType%=%javaSearchTarget%
    echo
    "%JAVA%"
    exit/b %ERRORLEVEL%
goto :EOF

:main
set AS_INSTALL="%~dp0.."
call "%AS_INSTALL%\config\asenv.bat"
call :chooseJava

set ACC_MODULEPATH="%AS_INSTALL%\lib\bootstrap"
set ACC_INSTALL_LIB="%AS_INSTALL%\lib\install\applications";
set ACC_CLASSPATH="%AS_INSTALL%\lib\gf-client.jar:%AS_INSTALL%\lib\appclient\weld-se-shaded.jar:%AS_INSTALL%\admin-cli.jar:%AS_DERBY_INSTALL%\lib\*:%ACC_INSTALL_LIB%\__ds_jdbc_ra\jdbc-ra-ds.jar:%ACC_INSTALL_LIB%\__cp_jdbc_ra\jdbc-ra-cp.jar:%ACC_INSTALL_LIB%\__xa_jdbc_ra\jdbc-ra-xa.jar:%ACC_INSTALL_LIB%\__dm_jdbc_ra\jdbc-ra-dm.jar:%ACC_INSTALL_LIB%\jmsra\imqjmsra.jar:%AS_IMQ_LIB%\imq.jar:%AS_IMQ_LIB%\imqadmin.jar:%AS_IMQ_LIB%\imqutil.jar:%AS_IMQ_LIB%\fscontext.jar:%AS_INSTALL%\modules\*"

%JAVA% %ASADMIN_JVM_OPTIONS% --module-path "%ACC_MODULEPATH%" --add-modules ALL-MODULE-PATH -cp "%ACC_CLASSPATH%" org.glassfish.appclient.client.CLIBootstrap %*
