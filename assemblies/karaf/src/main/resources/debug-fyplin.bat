@echo off
cd /d %~dp0

set KARAF_TITLE=Fyplin

set KARAF_DEBUG=true
set JAVA_DEBUG_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8762

set JAVA_MIN_MEM=256M
set JAVA_MAX_MEM=1024M

set JAVA_META_SPACE=256M
set JAVA_MAX_META_SPACE=512M

call ./bin/karaf.bat
