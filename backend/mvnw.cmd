@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Begin all REM://
@echo off
@REM set title of command window
title %0
@REM enable echoing by setting MAVEN_BATCH_ECHO to 'on'
@if "%MAVEN_BATCH_ECHO%"=="" @echo off

@REM set %HOME% to equivalent of $HOME
if "%HOME%"=="" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

@REM Execute a user defined script before this one
if not "%MAVEN_SKIP_RC%"=="" goto skipRcPre
@REM check for pre script, once with legacy .bat ending and once with .cmd ending
if exist "%USERPROFILE%\mavenrc_pre.bat" call "%USERPROFILE%\mavenrc_pre.bat" %*
if exist "%USERPROFILE%\mavenrc_pre.cmd" call "%USERPROFILE%\mavenrc_pre.cmd" %*
:skipRcPre

@setlocal

set ERROR_CODE=0

@REM To isolate internal variables from possible post scripts, we use another setlocal
@setlocal

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%"=="" goto OkJHome

echo.
echo Error: JAVA_HOME not found in your environment. >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto init

echo.
echo Error: JAVA_HOME is set to an invalid directory. >&2
echo JAVA_HOME = "%JAVA_HOME%" >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.
goto error

@REM ==== END VALIDATION ====

:init

@REM Find the project base dir, i.e. the directory that contains the folder ".mvn".
@REM Fallback to current working directory if not found.

set MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
IF NOT "%MAVEN_PROJECTBASEDIR%"=="" goto endDetectBaseDir

set EXEC_DIR=%CD%
set WDIR=%EXEC_DIR%
:findBaseDir
IF EXIST "%WDIR%"\.mvn goto baseDirFound
cd ..
IF "%WDIR%"=="%CD%" goto baseDirNotFound
set WDIR=%CD%
goto findBaseDir

:baseDirFound
set MAVEN_PROJECTBASEDIR=%WDIR%
cd "%EXEC_DIR%"
goto endDetectBaseDir

:baseDirNotFound
set MAVEN_PROJECTBASEDIR=%EXEC_DIR%
cd "%EXEC_DIR%"

:endDetectBaseDir

IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties" goto wrapperNotFound

@REM Determine Maven distribution URL from wrapper properties
for /f "usebackq tokens=1,2 delims==" %%a in ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") do (
    if "%%a"=="distributionUrl" set DOWNLOAD_URL=%%b
)

if "%DOWNLOAD_URL%"=="" goto wrapperNotFound

@REM Download and extract Maven if not already present
set "MAVEN_HOME_DIR=%HOME%\.m2\wrapper\dists\apache-maven-3.9.6"

if exist "%MAVEN_HOME_DIR%\bin\mvn.cmd" goto runMaven

echo Downloading Maven from %DOWNLOAD_URL%...
mkdir "%MAVEN_HOME_DIR%" 2>nul

@REM Use PowerShell to download
powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%TEMP%\maven.zip' }"
if ERRORLEVEL 1 goto error

echo Extracting Maven...
powershell -Command "& { Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%HOME%\.m2\wrapper\dists' -Force }"
if ERRORLEVEL 1 goto error

del "%TEMP%\maven.zip" 2>nul

@REM The zip extracts to apache-maven-3.9.6 directory
if not exist "%MAVEN_HOME_DIR%\bin\mvn.cmd" (
    @REM Try to find it one level deeper
    for /d %%i in ("%HOME%\.m2\wrapper\dists\apache-maven-*") do (
        set "MAVEN_HOME_DIR=%%i"
    )
)

:runMaven
set "MAVEN_CMD=%MAVEN_HOME_DIR%\bin\mvn.cmd"

if not exist "%MAVEN_CMD%" (
    echo Error: Could not find Maven executable at %MAVEN_CMD%
    goto error
)

@REM Provide a "standardized" way to retrieve the CLI args that will
@REM work with both Windows and non-Windows executions.
set MAVEN_CMD_LINE_ARGS=%*

"%MAVEN_CMD%" %MAVEN_CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:wrapperNotFound
echo Error: Could not find maven-wrapper.properties
goto error

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%

if not "%MAVEN_BATCH_PAUSE%"=="" pause

cmd /C exit /B %ERROR_CODE%
