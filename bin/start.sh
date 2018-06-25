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
function startService() {
    checkPid
    if [ ${psid} -ne 0 ];then
        echo "=============================================================="
        echo "warn: ${APP_NAME} already started! (pid=${psid})"
        echo "=============================================================="
    else
        echo "Starting ${APP_NAME} ... "
        nohup java -Dfile.encoding=utf-8 -jar ${APP_NAME} --spring.config.location=${CWD}/../application.properties >/dev/null 2>&1 &
        checkPid
        if [ ${psid} -ne 0 ]; then
            echo "Start service ${APP_NAME}(pid=${psid}) [OK]"
        else
            echo "Start service ${APP_NAME}(pid=${psid}) [Failure]"
        fi
    fi
}

function main
{
    startService
}

main "$@"
