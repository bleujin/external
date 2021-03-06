@echo off

setlocal enabledelayedexpansion


:test
set /a "TESTPORT=%RANDOM%+3000"
netstat -an | findstr ":%TESTPORT% "
if %ERRORLEVEL%==0 goto test

rem for %%? in ("%~dp0..") do set HOMEDIR=%%~f?
set HOMEDIR=%cd%
IF not exist "%JAVA_HOME%\jre" (
	set JAVA_HOME="C:\java\jdk6_45"
)
set JAVA_BIN="%JAVA_HOME%\bin\java"
set CP=./;
rem set JAVA_ARGS=-Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 -Djava.util.logging.config.file=%HOMEDIR%\resource\log4j.properties -Dsun.nio.ch.bugLevel=""
set JAVA_ARGS=-Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 -Djava.util.logging.config.file=.\resource\log4j.properties -Dsun.nio.ch.bugLevel="" 
set JMX_ARGS=-Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=%TESTPORT% 
set MAIN_JAR=es_0.1.jar
IF not DEFINED JAVA_GC_ARGS (
	set JAVA_GC_ARGS=-Xms512m -Xmx1024m -server
)

if not exist "%JAVA_HOME%\jre" goto no_java


@echo. running script for Niss Server

rem confirm setted vars 
@echo. == Settted Vars ==
@echo. HOMEDIR=%HOMEDIR%
@echo. JAVA_HOME=%JAVA_HOME%
@echo. JAVA_BIN=%JAVA_BIN%
@echo. CLASSPATH=%CP%
@echo. JAVA_ARGS=%JAVA_ARGS%
@echo. JMX_ARGS=%JMX_ARGS%
@echo. JAVA_GC_ARGS=%JAVA_GC_ARGS%
@echo. PRG_ARGS=%PRG_ARGS% %*

@echo. %JAVA_BIN% %JAVA_GC_ARGS% %JMX_ARGS% %JAVA_ARGS% -jar %MAIN_JAR% %PRG_ARGS% %*
start "" CALL %JAVA_BIN% %JAVA_GC_ARGS% %JMX_ARGS% %JAVA_ARGS% -jar %MAIN_JAR% %PRG_ARGS% %*

goto end

:no_java
@echo. This install script requires the parameter to specify Java location
@echo. The Java run-time files tools.jar and jvm.dll must exist under that location
goto error_exit


:error_exit
@echo .
@echo . Failed to run AradonServer

:end
@echo.
@pause
