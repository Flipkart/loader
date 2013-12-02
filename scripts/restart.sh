## This script needs to be executed from scripts folder only
./stop-all.sh;
cd ..
mvn clean compile package
cd -
sleep 5
./start-all.sh;
