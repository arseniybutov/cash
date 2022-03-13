#!/bin/sh

PORT=6304
ACTION=audit

run_command() {
  curl -d "$1" -X POST "http://localhost:$PORT/$ACTION/"
}

run_command "$*"
