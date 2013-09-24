#!/bin/sh
# Usage: create_monitoring-service_deb env (local/production) 
die()
{
      echo "Error: $1" >&2
      exit 1
}

PACKAGE=monitoring-service
LOADER_BASE_ROOT="../../"
CODE_ROOT="../"
PACKAGE_ROOT="./monitoring-service"
VERSION=0.1.6
ARCH=all

echo "Pre requisite to create this package : mvn clean compile package install -Dmaven.test.skip=true at loader2.0 level"
##remove existing directories
rm -rf $PACKAGE_ROOT
rm monitoring-service_*.deb

## Creating Required folders
mkdir -p $PACKAGE_ROOT/etc/$PACKAGE
mkdir -p $PACKAGE_ROOT/DEBIAN
mkdir -p $PACKAGE_ROOT/usr/share/$PACKAGE/lib
mkdir -p $PACKAGE_ROOT/usr/share/$PACKAGE/app
mkdir -p $PACKAGE_ROOT/etc/init.d/

## Copying content for packaging
cp $CODE_ROOT/config/monitoring-service.yml $PACKAGE_ROOT/etc/$PACKAGE/monitoring-service.yml
cp $PACKAGE.control $PACKAGE_ROOT/DEBIAN/control
cp $PACKAGE.postinst $PACKAGE_ROOT/DEBIAN/postinst
cp $PACKAGE.postrm $PACKAGE_ROOT/DEBIAN/postrm
cp $PACKAGE.preinst $PACKAGE_ROOT/DEBIAN/preinst
cp $PACKAGE.prerm $PACKAGE_ROOT/DEBIAN/prerm
cp $PACKAGE.init $PACKAGE_ROOT/etc/init.d/$PACKAGE

#### COPY CODE BASE
cp -R $CODE_ROOT/target/*.jar $PACKAGE_ROOT/usr/share/$PACKAGE/app/
cp -R $CODE_ROOT/target/lib/*.jar $PACKAGE_ROOT/usr/share/$PACKAGE/lib/

## Creating Package
sed -i "s/<VERSION>/$VERSION/g" $PACKAGE_ROOT/DEBIAN/control
dpkg-deb -b $PACKAGE_ROOT
mv $PACKAGE_ROOT.deb ${PACKAGE_ROOT}_${VERSION}_${ARCH}.deb
