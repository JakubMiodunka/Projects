#!/bin/env bash

### BEGIN INIT INFO
# Provides: scriptname
# Required-Start: $all
# Required-Stop: $all
# Default-Start: 2 3 4 5
# Default-Stop: 0 1 6
# Short-Description: Start scriptname at boot time
### END INIT INFO

mount -t vboxsf Main_share /home/user/Shared
