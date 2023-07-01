#!/bin/bash

# Utilities used during script runtime
logger="./Utilities/logger.sh"
package_installer="./Utilities/package_installer.sh"
startup_script_preparer="./Utilities/startup_script_preparer.sh"

# Printing help prompt if neccessary
if [[ "$1" == "-h" ]] || [[ "$1" == "--help" ]]; then
	echo \
	"Performs defined set of actions to setup the machine.
	It requires root privileges to execute correctly.
	Should be executed only when CWD matches exact script location (related to used relative paths).
	Usadge:
		setup_basic_usadge_machine.sh
	Options:
		-h, --help    Display help prompt.
	Exit codes:
		0 - All operations successfull.
		1 - Invalid arguemnts given.
		2 - Script not launched by root.
		3 - One of performed actions failed."

	exit 0
fi

# Checking if number of given argument is valid
if [[ $# -ne 0 ]]; then
	$logger 2 "$Invalid number of arguments given."
	exit 1
fi

# Checking if script was launched by root
if [[ "$(whoami)" != "root" ]]
then
	$logger 2 "Script was not launched by root - aborting."
	exit 2
fi

# Logging
$logger 1 "Setting up basic usadge machine..."

# Setting up the machine
$logger 1 "Setting timezone to Europe/Warsaw..."
timedatectl set-timezone Europe/Warsaw || { $logger 2 "Failed to update timezone - exit code ${$?}."; exit 3; }
$logger 1 "Timezone updated successfully."

$logger 1 "Setting keyboard layout to Polish..."
localectl set-keymap pl || { $logger 2 "Failed to update keyboard layout - exit code ${$?}."; exit 3; }
$logger 1 "Keyboard layout updated successfully."

$logger 1 "Updating list of available packages..."
$logger 3 "Apt std-out"
apt update || { $logger 2 "Failed to update package list - exit code ${$?}."; exit 3; }
$logger 1 "Package list updated successfully."

$logger 1 "Upgrading the system..."
$logger 3 "Apt std-out"
apt upgrade	|| { $logger 2 "Failed to upgrade the system - exit code ${$?}."; exit 3; }
$logger 1 "System successfully upgraded."

$logger 1 "Installing additional packages..."
$package_installer "./Package_lists/general_use_machine.txt" || { $logger 2 "Failed to install specyfied packages - exit code ${$?}."; exit 3; }
$logger 1 "Packages installed successfully."

$logger 1 "Adding 'user' user to 'sudo' group..."
sudo adduser user sudo	> /dev/null || { $logger 2 "Failed to update 'sudo' group - exit code ${$?}."; exit 3; }
$logger 1 "'sudo' group updated successfully."

$logger 1 "Creating a mount points for VBox shared folders..."
mkdir -p /home/user/Shared/Main_share /home/user/Shared/Git_repositories || { $logger 2 "Failed to create mount points - exit code ${$?}."; exit 3; }
$logger 1 "Mount points successfully created."

$logger 1 "Preparing 'general_use_machine.sh' script to mount VBox shared folders on every system startup ..."
$startup_script_preparer "./Startup_scripts/General_use_machine/vbox_mount.sh" || { $logger 2 "Failed to prepare specyfied script - exit code ${$?}."; exit 3; }
$logger 1 "Script successfully prepared"

# Info message at the end
$logger 1 "All actions performed successfully."
