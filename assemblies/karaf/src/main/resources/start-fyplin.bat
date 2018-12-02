@echo off
cd /d %~dp0

set KARAF_TITLE=Fyplin

set JAVA_MIN_MEM=128M
set JAVA_MAX_MEM=512M

set JAVA_META_SPACE=128M
set JAVA_MAX_META_SPACE=256M

call ./bin/karaf.bat
