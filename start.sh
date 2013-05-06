mvn clean compile package;
java -cp  loader-agent/target/loader-agent-1.0-SNAPSHOT.jar:loader-agent/target/lib/* perf.agent.LoaderAgentService loader-agent/config/loader-agent.yml > log &
sleep 5;
java -cp monitoring-service/target/monitoring-service-1.0-SNAPSHOT.jar:monitoring-service/target/lib/* server.monitor.MonitoringService monitoring-service/config/monitoring-service.yml > log &
java -cp loader-server/target/loader-server-1.0-SNAPSHOT.jar:loader-server/target/lib/*:/usr/share/loader-server/platformLibs/* perf.server.LoaderServerService loader-server/config/loader-server.yml > log &
