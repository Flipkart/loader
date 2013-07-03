##This script needs to executed from within scripts folder only
echo "mvn clean compile install package first"
cd ../
echo "Creating core loader Packages"
modules=("loader-server" "loader-agent" "monitoring-service")
for module in "${modules[@]}"
do
	cd ${module}/DEBIAN
 	./create_${module}_deb.sh	
	cd -
done

echo "Creating Diamond conf Packages"
cd monitoring-service/diamond/DEBIAN/
for conf in `ls`
do
	cd ${conf}
	./create_${conf}_deb.sh
	cd -
done

