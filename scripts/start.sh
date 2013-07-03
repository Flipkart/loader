cd ../
mvn clean compile package;
java -cp  loader-agent/target/*:loader-agent/target/lib/* perf.agent.LoaderAgentService loader-agent/config/loader-agent.yml > log &
sleep 5;
java -cp monitoring-service/target/*:monitoring-service/target/lib/* server.monitor.MonitoringService monitoring-service/config/monitoring-service.yml > log &
java -cp loader-server/target/*:loader-server/target/lib/*:/usr/share/loader-server/platformLibs/* perf.server.LoaderServerService loader-server/config/loader-server.yml > log &
cd -
