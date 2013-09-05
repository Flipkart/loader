cd ../
[ -d log ] || mkdir log
java -cp  loader-agent/target/*:loader-agent/target/lib/* perf.agent.LoaderAgentService loader-agent/config/loader-agent.yml >> log/loader-agent.log 2>&1 &
cd -
