mvn clean compile package;
java -cp target/loader-server-1.0-SNAPSHOT.jar:target/lib/*:/usr/share/loader-server/platformLibs/* perf.server.LoaderServerService config/loader-server.yml &>> log &
