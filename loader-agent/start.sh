mvn clean compile package;
java -cp  target/loader-agent-1.0-SNAPSHOT.jar:target/lib/* perf.agent.LoaderAgentService config/loader-agent.yml &>> log &
