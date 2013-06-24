mkdir -p /var/log/loader/
mkdir -p /var/log/loader-server/jobs
mkdir -p /var/log/loader-server/runs
mkdir -p /var/log/loader-agent/jobs
mkdir -p /usr/share/loader-agent
mkdir -p /usr/share/loader-agent/config
mkdir -p /usr/share/loader-agent/libs
mkdir -p /usr/share/loader-agent/platformLibs
mkdir -p /usr/share/loader-server/agents
mkdir -p /usr/share/loader-server/config
mkdir -p /usr/share/loader-server/platformLibs
mkdir -p /usr/share/loader-server/libs
echo "[]" > /var/log/loader-agent/jobs/runningJobs
echo "[]" > /var/log/loader-server/jobs/runningJobs
echo "[]" > /var/log/loader-server/jobs/queuedJobs
touch /usr/share/loader-server/config/classLibMapping.properties
touch /usr/share/loader-agent/config/mapping.properties
cp ../loader-server/config/reportConfig.yml /var/log/loader-server/report/reportConfig.yml
sudo chown -R `whoami`:`whoami` /var/log/loader* /usr/share/loader*