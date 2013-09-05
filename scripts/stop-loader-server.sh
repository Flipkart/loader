ps -ef | egrep -i "loader-server" | grep -v grep | awk '{print $2}' | xargs kill -9
