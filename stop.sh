ps -ef | egrep -i "loader|monitoring" | grep -v grep | awk '{print $2}' | xargs kill -9
