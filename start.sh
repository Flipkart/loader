mvn clean compile package;
java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=4444 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -cp loader-server/target/loader-server-1.0-SNAPSHOT.jar:loader-server/target/lib/*:/usr/share/loader-server/platformLibs/* perf.server.LoaderServerService loader-server/config/loader-server.yml &>> log &
java -cp  loader-agent/target/loader-agent-1.0-SNAPSHOT.jar:loader-agent/target/lib/* perf.agent.LoaderAgentService loader-agent/config/loader-agent.yml &>> log &
java -cp monitoring-service/target/monitoring-service-1.0-SNAPSHOT.jar:monitoring-service/target/lib/* server.monitor.MonitoringService monitoring-service/config/monitoring-service.yml &>> log &
