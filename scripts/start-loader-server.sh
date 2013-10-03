cd ../
[ -d log ] || mkdir log
java -cp loader-server/target/*:loader-server/target/lib/*:/usr/share/loader-server/platformLibs/* com.flipkart.perf.server.LoaderServerService loader-server/config/loader-server.yml >> log/loader-server.log 2>&1 &
cd -
