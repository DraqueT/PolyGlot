#!/bin/bash

basedir=$(dirname $(readlink -f $0))

java_version() {
  local result
  local java_cmd
  if [[ -n $(type -p java) ]]
  then
    java_cmd=java
  elif [[ (-n "$JAVA_HOME") && (-x "$JAVA_HOME/bin/java") ]]
  then
    java_cmd="$JAVA_HOME/bin/java"
  fi
  local IFS=$'\n'
  if [[ -z $java_cmd ]]
  then
    echo "java could not be found"
    exit 1
  else
    # remove \r for Cygwin
    local lines=$("$java_cmd" -Xms32M -Xmx32M -version 2>&1 | tr '\r' '\n')
    for line in $lines; do
      if [[ (-z $result) && ($line = *"version \""*) ]]
      then
        local ver=$(echo $line | sed -e 's/.*version "\(.*\)"\(.*\)/\1/; 1q')
        # on macOS, sed doesn't support '?'
        if [[ $ver = "1."* ]]
        then
          result=$(echo $ver | sed -e 's/1\.\([0-9]*\)\(.*\)/\1/; 1q')
        else
          result=$(echo $ver | sed -e 's/\([0-9]*\)\(.*\)/\1/; 1q')
        fi
      fi
    done
  fi
  echo "$result"
}

# Works for Debian-based system if installed via openjfx package
# Could be extended to support other distribution
JFX_DIR="/usr/share/openjfx/lib/"

if [[ "$(java_version)" -ge 9 && -d "$JFX_DIR" ]]
then
  JAVA_OPTS="--module-path $JFX_DIR --add-modules=ALL-MODULE-PATH"
else
  JAVA_OPTS=""
fi

java $JAVA_OPTS -jar $basedir/PolyGlot.jar
