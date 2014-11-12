# Setting Loader Server File system
echo "\n===============Setting up Loader Server file system================\n"
PKG=loader-server
CODE_ROOT=../$PKG

# Create Files and Folders
echo "****Creating Files and Folder Required for loade-server"
$CODE_ROOT/DEBIAN/$PKG.preinst

#Copy Required Files
echo "\n****Copying Config Files"
cp $CODE_ROOT/config/reportConfig.json /etc/$PKG/reportConfig.json
cp $CODE_ROOT/config/dataFixers.json /etc/$PKG/dataFixers.json
cp $CODE_ROOT/config/reportConfig.json /etc/$PKG/reportConfig.json

#Setting up Core Libraries
echo "\n****Copying loader-core libraries for loader-server"
cp -R ../loader-core/target/platform.zip /usr/share/$PKG/platformLibs/
unzip ../loader-core/target/platform.zip -d /usr/share/$PKG/platformLibs/

#Setting up Out of Box Perf Operations
echo "\n****Copying Out out Box Perf Operations"
cp ../loader-http-operations/target/loader-http-operations-*-jar-with-dependencies.jar /usr/share/loader-server/unDeployedLibs/
cp ../loader-common-operations/target/loader-common-operations-*-jar-with-dependencies.jar /usr/share/loader-server/unDeployedLibs/

#Setting Sample Runs
echo "\n****Setting up Sample Performance Run Schemas"
#mkdir -p /var/log/loader-server/runs/SampleHttpGetFor10000Times
cp -r ../loader-server/sample/runs/SampleHttpGetFor10000Times /var/log/loader-server/runs/
#mkdir -p /var/log/loader-server/runs/SampleHttpGetFor30Seconds
cp -r ../loader-server/sample/runs/SampleHttpGetFor30Seconds /var/log/loader-server/runs/

# Setting up loader agent
echo "\n===============Setting up Loader Agent file system================\n"
PKG=loader-agent
CODE_ROOT=../$PKG

# Create Files and Folders
echo "****Creating Files and Folder Required for loader-agent"
$CODE_ROOT/DEBIAN/$PKG.preinst
