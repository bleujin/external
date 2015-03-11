@echo off

:start
set HOMEDIR=%cd%
set JAVA_GC_ARGS=-Xms128m -Xmx128m -server
set PRG_ARGS=-config:%HOMEDIR%\resource\config\es-config.xml -action:shutdown

CALL common.bat