#!/bin/sh

KARAF_TITLE="Fyplin"

KARAF_DEBUG="true"
JAVA_DEBUG_OPTS="agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8762"

JAVA_MIN_MEM="128M"
JAVA_MAX_MEM="512M"

JAVA_META_SPACE="128M"
JAVA_MAX_META_SPACE="256M"

sh "bin/karaf"
