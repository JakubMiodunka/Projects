#!/bin/env bash

### BEGIN INIT INFO
# Provides: vbox_mount.sh
# Required-Start: $all
# Required-Stop: $all
# Default-Start: 2 3 4 5
# Default-Stop: 0 1 6
# Short-Description: Mounts VirtualBox shared folders
### END INIT INFO

mount -t vboxsf Main_share /home/user/Shared/Main_share
mount -t vboxsf Git_repositories /home/user/Shared/Git_repositories
