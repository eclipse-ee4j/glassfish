@echo off
REM
REM Copyright (c) 2025 Contributors to the Eclipse Foundation
REM
REM This program and the accompanying materials are made available under the
REM terms of the Eclipse Public License v. 2.0, which is available at
REM http://www.eclipse.org/legal/epl-2.0.
REM
REM This Source Code may also be made available under the following Secondary
REM Licenses when the conditions for such availability set forth in the
REM Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
REM version 2 with the GNU Classpath Exception, which is available at
REM https://www.gnu.org/software/classpath/license.html.
REM
REM SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

REM Resolve AS_INSTALL from script location
call :resolveAsInstall "%~dp0.."
call :loadAsenv
call :chooseJava
exit /b 0

:resolveAsInstall
set "AS_INSTALL=%~f1"
exit /b 0

:loadAsenv
if exist "%AS_INSTALL%\config\asenv.bat" (
    call "%AS_INSTALL%\config\asenv.bat"
) else (
    echo Error: asenv.bat not found in %AS_INSTALL%\config
    exit /b 1
)
exit /b 0

:chooseJava
if defined AS_JAVA (
    set "JAVA=%AS_JAVA%\bin\java.exe"
    set "javaSearchType=AS_JAVA"
    set "javaSearchTarget=%AS_JAVA%"
) else if defined JAVA_HOME (
    set "JAVA=%JAVA_HOME%\bin\java.exe"
    set "javaSearchType=JAVA_HOME"
    set "javaSearchTarget=%JAVA_HOME%"
) else (
    for %%i in (java.exe) do set "JAVA=%%~$PATH;i"
    set "javaSearchType=PATH"
    set "javaSearchTarget=%PATH%"
)

if not exist "%JAVA%" (
    echo.
    echo The java command "%JAVA%" is not executable.
    echo It was configured as %javaSearchType%="%javaSearchTarget%"
    exit /b 1
)
exit /b 0
