#!/bin/env bash

# Printing help prompt if neccessary
if [[ "$1" == "-h" ]] || [[ "$1" == "--help" ]]; then
	echo \
	"Simple logger, that can be used for formatting log messages.
	Usadge:
		logger.sh <logging level> <message content>
	Options:
		-h, --help    Display help prompt.
	Available logging levels:
		1 - INFO
		2 - ERROR
		3 - DEBUG
	Generated messages format:
		[<timestamp>][<logging level>] <message content>
	Exit codes:
		0 - All operations successfull.
		1 - Invalid arguemnts given."

	exit 0
fi

# Checking if number of given argument is valid
if [[ $# -ne 2 ]]; then
	echo "$Invalid number of arguments given."
	exit 1
fi

# Assigning arguments values to dedicated variables
logging_level="$1"
message_content="$2"

# Adding timestampt to log message
message="[$(date +'%Y-%m-%d %H:%M:%S')]"

# Adding logging level indicator to log message according to given argument
case "${logging_level}" in
	"1")
		message="${message}[INFO]"
		;;
	"2")
		message="${message}[ERROR]"
		;;
	"3")
		message="${message}[DEBUG]"
		;;
	*)
		echo "Specyfied logging level invalid."
		exit 1
		;;
esac

# Adding given message content to generated log message
message="${message} ${message_content}"

# Printing log message
echo "${message}"
