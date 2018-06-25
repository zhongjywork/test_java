#!/bin/bash

RUNNING_USER=admin
APP_NAME=../jeci-pts-1.0-SNAPSHOT.jar
CWD=$(dirname $(readlink -f $0))

#######################################################
# check process is existed
#######################################################
psid=0
function checkPid() {
    javaps=`${JAVA_HOME}/bin/jps -l | grep ${APP_NAME}`
    if [ -n "${javaps}" ]; then
        psid=`echo ${javaps} | awk '{print $1}'`
    else
        psid=0
    fi
}

#######################################################
# start jeci-pts service
#######################################################
function stopService() {
    checkPid
    if [ ${psid} -ne 0 ]; then
        echo -n "Stopping ${APP_NAME} ...(pid=${psid}) "
        kill -9 $psid
        if [ $? -eq 0 ]; then
            echo "[OK]"
        else
            echo "[Failed]"
        fi

        checkPid
        if [ ${psid} -ne 0 ]; then
            stopService
        fi
    else
        echo "================================"
        echo "warn: ${APP_NAME} is not running"
        echo "================================"
    fi
}

function main
{
    stopService
}

main "$@"
