##This script needs to executed from within scripts folder only
cd ../
mvn clean compile install package
modules=("loader-server" "loader-agent" "monitoring-service")
for module in "${modules[@]}"
do
	cd ${module}/DEBIAN
 	./create_${module}_deb.sh	
	cd -
done
