curl -X POST -H "Content-Type: multipart/form-data" -F "lib=@../target/platform.zip"  http://$1:$2/loader-server/resourceTypes/platformLibs
