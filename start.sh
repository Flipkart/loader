mvn clean compile package;
java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=4444 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -cp loader-server/target/loader-server-1.0-SNAPSHOT.jar:loader-server/target/lib/* perf.server.LoaderServerService loader-server/config/loader-server.yml &>> log &
java -cp  loader-agent/target/loader-agent-1.0-SNAPSHOT.jar:loader-agent/target/lib/* perf.agent.LoaderAgentService loader-agent/config/loader-agent.yml &>> log &
java -cp monitoring-service/target/loader-monitoring-1.0-SNAPSHOT.jar:loader-monitoring/target/lib/* server.monitor.MonitoringService loader-monitoring/config/monitoring-service.yml &>> log &
