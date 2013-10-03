cd ../
[ -d log ] || mkdir log
java -cp monitoring-service/target/*:monitoring-service/target/lib/* com.flipkart.server.monitor.MonitoringService monitoring-service/config/monitoring-service.yml > log/monitoring-service.log 2>&1 &
cd -
