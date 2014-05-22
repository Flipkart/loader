## This script needs to be executed from scripts folder only
./stop-all.sh;
cd ..
mvn clean compile package
cd -
sleep 5
./start-all.sh;
sleep 5
cd ../loader-core/script/
./deploy.sh localhost 9999
cd -
