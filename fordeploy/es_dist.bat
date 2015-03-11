@echo off


:start
set HOMEDIR=%cd%
set JAVA_GC_ARGS=-Xms1024m -Xmx1592m -server
set PRG_ARGS=-config:%HOMEDIR%\resource\config\es-dist-config.xml

CALL common.bat
