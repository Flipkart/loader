#!/bin/sh
# Usage: create_loader-server_deb env (local/production) 
die()
{
      echo "Error: $1" >&2
      exit 1
}

PACKAGE=loader-server
LOADER_BASE_ROOT="../../"
CODE_ROOT="../"
LOADER_CORE_CODE_ROOT="../../loader-core"
PACKAGE_ROOT="./loader-server"
VERSION=0.1.12
ARCH=all

echo "Pre requisite to create this package : mvn clean compile package install -Dmaven.test.skip=true at loader2.0 level"
##remove existing directories
rm -rf $PACKAGE_ROOT
rm loader-server_*.deb

## Creating Required folders
mkdir -p $PACKAGE_ROOT/etc/$PACKAGE
mkdir -p $PACKAGE_ROOT/DEBIAN
mkdir -p $PACKAGE_ROOT/usr/share/$PACKAGE/lib
mkdir -p $PACKAGE_ROOT/usr/share/$PACKAGE/app
mkdir -p $PACKAGE_ROOT/usr/share/$PACKAGE/platformLibs
mkdir -p $PACKAGE_ROOT/etc/init.d/

## Copying content for packaging
cp $CODE_ROOT/config/loader-server.yml $PACKAGE_ROOT/etc/$PACKAGE/loader-server.yml
cp $CODE_ROOT/config/reportConfig.json $PACKAGE_ROOT/etc/$PACKAGE/reportConfig.json
cp $CODE_ROOT/config/dataFixers.json $PACKAGE_ROOT/etc/$PACKAGE/dataFixers.json
cp $PACKAGE.control $PACKAGE_ROOT/DEBIAN/control
cp $PACKAGE.postinst $PACKAGE_ROOT/DEBIAN/postinst
cp $PACKAGE.postrm $PACKAGE_ROOT/DEBIAN/postrm
cp $PACKAGE.preinst $PACKAGE_ROOT/DEBIAN/preinst
cp $PACKAGE.prerm $PACKAGE_ROOT/DEBIAN/prerm
cp $PACKAGE.init $PACKAGE_ROOT/etc/init.d/$PACKAGE

#### COPY CODE BASE
cp -R $CODE_ROOT/target/*.jar $PACKAGE_ROOT/usr/share/$PACKAGE/app/
cp -R $CODE_ROOT/target/lib/*.jar $PACKAGE_ROOT/usr/share/$PACKAGE/lib/
cp -R $LOADER_CORE_CODE_ROOT/target/platform.zip $PACKAGE_ROOT/usr/share/$PACKAGE/platformLibs/

## Creating Package
sed -i "s/<VERSION>/$VERSION/g" $PACKAGE_ROOT/DEBIAN/control
dpkg-deb -b $PACKAGE_ROOT
mv $PACKAGE_ROOT.deb ${PACKAGE_ROOT}_${VERSION}_${ARCH}.deb
