ps -ef | egrep -i "loader-agent" | grep -v grep | awk '{print $2}' | xargs kill -9
