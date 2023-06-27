#!/bin/env bash

# Utilities used during script runtime
logger="./Utilities/logger.sh"


# Installs specyfied package using apt
# Args:
#	$1: Package name.
install_package() {
	# Assigning function argument to suitable variable
	local package="$1"
	
	# Logging
	$logger 1 "Installing '${package}' package..."
	
	# Attempting to install specyfied package using apt
	$logger 3 "Apt std-out:"
	apt install -y "${package}"
	
	# Checking if operation was successfull
	operation_status=$?
	if [[ $operation_status -ne 0 ]]; then
		$logger 2 "Failed to install '${package}' package."
		exit 3
	else
		$logger 1 "Package '${package}' successfully installed or was already installed."
	fi
}


# Printing help prompt if neccessary
if [[ "$1" == "-h" ]] || [[ "$1" == "--help" ]]; then
	echo \
	"Script, whitch installs all packages from provided list using apt.
	It requires root privileges to execute correctly.
	Usadge:
		package_installer.sh <package list file>
	Options:
		-h, --help    Display help prompt.
	Structure of package list file:
		Each package required to be installed should be placed in
		sepparate line. Name of the packages should be acceptable for apt.
	Exit codes:
		0 - All operations successfull.
		1 - Invalid arguemnts given.
		2 - Given package list file is invalid.
		3 - Failed to install particular package."

	exit 0
fi

# Checking if number of given argument is valid
if [[ $# -ne 1 ]]; then
	$logger 2 "$Invalid number of arguments given."
	exit 1
fi

# Assigning argument value to dedicated variable
package_list="$1"

# Validation of given packages list
if [[ ! -f "${package_list}" ]]; then
    $logger 2 "Given package list does not exist."
	exit 2
fi

if [[ $(wc -l <<< ${package_list}) -eq 0 ]]; then
	$logger 2 "Given package list is empty."
	exit 2
fi


# Installing packages mentioned in given package list
while read package; do
	# Making sure that package name doe not contain carriage return - it can cause 
	# problems as bash expects that end-of-line char is always and only a newline (\n).
	# In case that input file was created using Windows based editor, where each line
	# is ended with carriage return-newline combination (\r\n) such trimming is neccessary.
	package="$(tr -d "\r" <<< "${package}")"
	
	# Installing package
	install_package "${package}"
done < ${package_list}