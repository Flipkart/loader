#!/bin/sh
# Usage: create_fk-alert-service_deb env (local/production) 
die()
{
      echo "Error: $1" >&2
      exit 1
}

APP=diamond
PACKAGE=diamond-redis-conf
PACKAGE_ROOT=./diamond-redis-conf
CONFIG_ROOT=../../../diamond/collector-config/
VERSION=0.0.1
ARCH=all

rm -rf $PACKAGE_ROOT

#mv $PACKAGE_ROOT/config/fk-alert-service.yml $PACKAGE_ROOT/etc/fk-alert-service/config/fk-alert-service.yml
mkdir -p $PACKAGE_ROOT/etc/$APP/collectors
mkdir -p $PACKAGE_ROOT/DEBIAN

cp $CONFIG_ROOT/RedisCollector.conf $PACKAGE_ROOT/etc/$APP/collectors/RedisCollector.conf
cp $PACKAGE.control $PACKAGE_ROOT/DEBIAN/control
cp $PACKAGE.postinst $PACKAGE_ROOT/DEBIAN/postinst
cp $PACKAGE.postrm $PACKAGE_ROOT/DEBIAN/postrm
cp $PACKAGE.preinst $PACKAGE_ROOT/DEBIAN/preinst
cp $PACKAGE.prerm $PACKAGE_ROOT/DEBIAN/prerm

sed -i "s/<VERSION>/$VERSION/g" $PACKAGE_ROOT/DEBIAN/control
dpkg-deb -b $PACKAGE_ROOT
mv $PACKAGE_ROOT.deb ${PACKAGE_ROOT}_${VERSION}_${ARCH}.deb
