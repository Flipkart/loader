ps -ef | egrep -i "monitoring" | grep -v grep | awk '{print $2}' | xargs kill -9
