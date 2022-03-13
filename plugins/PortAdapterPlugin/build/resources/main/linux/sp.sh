#!/bin/sh

is_running() {
    pgrep -f "$1" > /dev/null 2>&1
    return $?
}

is_socat_running() {
  is_running "socat.*$1.*$2"
  return $?
}

is_serialproxy_running() {
  is_running "serialproxy .*?($1|$2)"
  return $?
}

do_start() {
    DISPLAY=:0
    export DISPLAY=:0

    is_socat_running "$2" "$3"
    case "$?" in
        0)
            echo "socat $2/$3 is already running"
            ;;
        1)
            sudo su -c "socat -d -d pty,raw,mode=666,echo=0,LINK=$2 pty,raw,echo=0,mode=666,LINK=$3 > /dev/null 2>&1 &"
            sleep 3
            ;;
    esac

    is_socat_running "$2" "$3"
    case "$?" in
        0) echo "socat $2/$3 has been started" ;;
        1) echo "ERROR: socat $2/$3 failed to start" ;;
    esac

    is_serialproxy_running "$2" "$1"
    case "$?" in
        0) echo "serialproxy $2 $1 is already running" && exit 0 ;;
        1)
            sudo su -c "./serialproxy $2 $1 $4 > /dev/null 2>&1 &"
        ;;
    esac

    is_serialproxy_running "$2" "$1"
    case "$?" in
        0) echo "serialproxy has been started $2 $1 $4" ;;
        1) echo "ERROR: serialproxy $2 $1 $4 failed to start" ;;
    esac

}

do_stop() {
    for pid in $(pgrep -f "serialproxy .*?($2|$1)"); do
        sudo kill -s TERM "$pid"
    done
    is_serialproxy_running "$2" "$1"
    if [ "$?" -eq 0 ]; then
        echo "serialproxy $2 $1 is still running. Sending KILL signal..."
        for pid in $(pgrep -f "serialproxy .*?($2|$1)"); do
            sudo kill -s KILL "$pid"
        done
    else
        echo "serialproxy $2 $1 has been stopped"
    fi
}

case $1 in
    start)
        do_start "$2" "$3" "$4" "$5";;
    stop)
        do_stop "$2" "$3";;
    restart)
        do_stop "$2" "$3"
        do_start "$2" "$3" "$4" "$5"
        ;;
    status)
        is_socat_running "$3" "$4"
        [ "$?" -eq 0 ] && echo "Socat $3/$4 is running" || echo "Socat $3/$4 is NOT running"
        is_serialproxy_running "$2" "$1"
        [ "$?" -eq 0 ] && echo "serialproxy $2 $1 is running" || echo "serialproxy $2 $1 is NOT running"
        ;;
    *)
        echo "Usage: $(basename "$0") {start|stop|restart|status}"
        echo "start <device_port> <virtual_port_1> <virtual_port_2> <baud_rate>"
        echo "restart <device_port> <virtual_port_1> <virtual_port_2> <baud_rate>"
        echo "stop <device_port> <virtual_port_1>"
        echo "status <device_port> <virtual_port_1> <virtual_port_2>"
        exit 1
        ;;
esac
