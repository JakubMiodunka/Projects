#!/bin/env bash

# Utilities used during script runtime
logger="./Utilities/logger.sh"

# Printing help prompt if neccessary
if [[ "$1" == "-h" ]] || [[ "$1" == "--help" ]]; then
	echo \
	"Makes the given script run on every system startup.
	Usadge:
		startup_script_preparer.sh <script desired to be launched on every system startup>
	Options:
		-h, --help    Display help prompt. 
	Exit codes:
		0 - All operations successfull.
		1 - Invalid arguemnts given.
		2 - Failed to copy given file to /etc/init.d/.
		3 - Error while updating the script privileges.
		4 - Error during system update using update-rc.d."

	exit 0
fi

# Checking if number of given argument is valid
if [[ $# -ne 1 ]]; then
	$logger 2 "$Invalid number of arguments given."
	exit 1
fi

# Assigning argument value to dedicated variable
# and performing quick validation
script_src="$1"

if [[ ! -f  "${script_src}" ]]; then
	$logger 2 "'${script_src}' does not exists."
	exit 1
fi

# Coping specyfied script to /etc/init.d/
$logger 1 "Coping '${script_src}' to '/etc/init.d/'..."

script_dest="/etc/init.d/$(basename "${script_src}")"
if [[ -f  "${script_dest}" ]]; then
	$logger 2 "'${script_dest}' already exist - aborting to prevent data overwitting."
	exit 2
fi

cp "${script_src}" "${script_dest}"
operation_status=$?
if [[ $operation_status -ne 0 ]]; then
	$logger 2 "Copying failed."
	exit 2
fi	
	
$logger 1 "File copiend successfully."

# Making copied file executable
$logger 1 "Making '${script_dest}' executable..."

chmod +x "${script_dest}"
operation_status=$?
if [[ $operation_status -ne 0 ]]; then
	$logger 2 "Failed to update privileges."
	exit 3
fi

$logger 1 "Privileges updated successfully."

# Updating the system to run the script on startup
$logger 1 "Updating the system to run the script on startup..."

sudo update-rc.d "$(basename ${script_dest})" defaults
#update-rc.d "$(basename ${script_dest})" defaults		# Works when 'sudo' is not installed
operation_status=$?
if [[ $operation_status -ne 0 ]]; then
	$logger 2 "Failed to update the system."
	exit 4
fi	

$logger 1 "System updated successfully."