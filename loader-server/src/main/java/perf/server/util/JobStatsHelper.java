package perf.server.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.open.perf.domain.Group;
import com.open.perf.domain.GroupFunction;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import perf.server.cache.JobsCache;
import perf.server.config.AgentConfig;
import perf.server.config.JobFSConfig;
import perf.server.config.MonitoringAgentConfig;
import perf.server.domain.Job;
import perf.server.domain.LoadPart;
import perf.server.domain.PerformanceRun;

import com.open.perf.util.FileHelper;

/**
 * Helper operation to persist and retrieve job related stats
 */
public class JobStatsHelper {
    private JobFSConfig jobFSConfig;
    private static JobStatsHelper instance;
    private static ObjectMapper objectMapper = ObjectMapperUtil.instance();
    private static Logger log = LoggerFactory.getLogger(JobStatsHelper.class);


    public JobStatsHelper(JobFSConfig jobFSConfig, AgentConfig agentConfig, MonitoringAgentConfig monitoringAgentConfig) {
        this.jobFSConfig = jobFSConfig;
    }

    public static JobStatsHelper build(JobFSConfig jobFSConfig, AgentConfig agentConfig, MonitoringAgentConfig monitoringAgentConfig) {
        if(instance == null)
            instance = new JobStatsHelper(jobFSConfig, agentConfig, monitoringAgentConfig);
        return instance;
    }

    public static JobStatsHelper instance() {
        return instance;
    }

    public void persistJobStatsComingFromAgent(String jobId, String agentIp, String relatedFilePath, InputStream jobStatsStream) throws IOException {
        log.info(jobId + " " + agentIp + " " + relatedFilePath);
        // Remove job Id from relative path as loader-server has already created path

        relatedFilePath = relatedFilePath.replace("/"+jobId + "/","");
        String groupName = relatedFilePath.split("\\/")[0];

        // Persist the stats in temporary file. As we have to read and write the stats at two places, same input stream can't be used twice.
        String tmpPath = "/tmp/"+jobId+"-"+System.nanoTime()+".txt";
        FileHelper.persistStream(jobStatsStream, tmpPath, true);

        //TBD Move Following Code to be executed in request queue mode by a daemon thread.
        String[] jobStatsPaths = new String[] {
                jobFSConfig.getJobGroupStatsPath(jobId, groupName)
                        + relatedFilePath.replace(groupName,"")
                        + File.separator + "agents"
                        + File.separator + agentIp
                        + File.separator + "data",

                jobFSConfig.getJobGroupStatsPath(jobId, groupName)
                        + relatedFilePath.replace(groupName,"")
                        + File.separator + "agents"
                        + File.separator + "combined"
                        + File.separator + "data",
        };

        // Persisting stats at agent and combined path
        for(String jobStatsPath : jobStatsPaths) {
            FileInputStream fis = new FileInputStream(tmpPath);

            FileHelper.createFilePath(jobStatsPath);
            FileHelper.persistStream(fis, jobStatsPath, true);
            fis.close();
        }

        FileHelper.remove(tmpPath);
    }

    public void persistJobHealthStatusComingFromAgent(String jobId, String agentIp, InputStream jobHealthStatusStream) throws IOException {
        String jobHealthStatusFile = jobFSConfig.getJobHealthStatusFile(jobId, agentIp);
        FileHelper.createFilePath(jobHealthStatusFile);
        FileHelper.persistStream(jobHealthStatusStream, jobHealthStatusFile, true);
        FileHelper.persistStream(jobHealthStatusStream, jobHealthStatusFile + ".last", false);
    }

    public static class MetricStatsMeta {

        private String name;
        private List<String> agents;

        public List<String> getAgents() {
            return agents;
        }

        public void setAgents(List<String> agents) {
            this.agents = agents;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class GroupStatsMeta {
        private String groupName;
        private List<MetricStatsMeta> timers;
        private List<MetricStatsMeta> counters;

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public List<MetricStatsMeta> getTimers() {
            return timers;
        }

        public void setTimers(List<MetricStatsMeta> timers) {
            this.timers = timers;
        }

        public List<MetricStatsMeta> getCounters() {
            return counters;
        }

        public void setCounters(List<MetricStatsMeta> counters) {
            this.counters = counters;
        }
    }

    /**
     * Get Meta information about all the metrics present for a job
     * @param jobId
     * @return
     */
    public List<GroupStatsMeta> getJobMetricsStatsMeta(String jobId) throws ExecutionException, IOException {
        Job job = JobsCache.getJob(jobId);
        PerformanceRun performanceRun = getPerformanceRun(job.getRunName());
        Set<Group> allGroups = new LinkedHashSet<Group>();
        for(LoadPart loadPart : performanceRun.getLoadParts()) {
            allGroups.addAll(loadPart.getLoad().getGroups());
        }

        List<GroupStatsMeta> groups = new ArrayList<GroupStatsMeta>();

        for(Group group : allGroups) {
            File groupPath = new File(jobFSConfig.getJobGroupStatsPath(jobId, group.getName()));
            if(groupPath.exists()) {
                GroupStatsMeta groupStatsMeta = new GroupStatsMeta();
                groupStatsMeta.setGroupName(groupPath.getName());
                boolean groupHasStats = false;

                List<MetricStatsMeta> groupTimers = new LinkedList<MetricStatsMeta>();
                List<MetricStatsMeta> groupCounters = new LinkedList<MetricStatsMeta>();

                for(GroupFunction groupFunction : group.getFunctions()) {
                    if(groupFunction.isDumpData()) {
                        String functionName = groupFunction.getFunctionalityName();

                        // Fetch Timers
                        File timersPath = new File(jobFSConfig.getJobGroupTimersStatsPath(jobId, group.getName()));
                        if(timersPath.exists()) {
                            for(File timerPath : timersPath.listFiles()) {
                                if(functionName.equals(timerPath.getName())) {
                                    // I am using timer name here as function name
                                    MetricStatsMeta metricStatsMeta = new MetricStatsMeta();
                                    metricStatsMeta.setName(timerPath.getName());

                                    File agentsPath = new File(timerPath.getAbsoluteFile() + File.separator + "agents");

                                    List<String> allAgents = Arrays.asList(agentsPath.list());
                                    List<String> agentsHavingData = new ArrayList<String>();

                                    for(String agent : allAgents) {
                                        if(new File(jobFSConfig.getJobFunctionStatsFile(jobId,
                                                groupPath.getName(),
                                                "timers",
                                                timerPath.getName(),
                                                agent)).exists()) {
                                            agentsHavingData.add(agent);
                                            groupHasStats = true;
                                        }
                                    }

                                    metricStatsMeta.setAgents(agentsHavingData);
                                    groupTimers.add(metricStatsMeta);

                                }
                            }
                        }

                        // Fetch Counters
                        File countersPath = new File(jobFSConfig.getJobGroupCountersStatsPath(jobId, group.getName()));
                        if(countersPath.exists()) {
                            for(File counterPath : countersPath.listFiles()) {
                                if(counterPath.getName().equals(functionName + "_count")
                                        || counterPath.getName().equals(functionName + "_error")
                                        || counterPath.getName().equals(functionName + "_failure")
                                        || counterPath.getName().equals(functionName + "_skip")) {
                                    // I am using counter name here as function name
                                    MetricStatsMeta metricStatsMeta = new MetricStatsMeta();
                                    metricStatsMeta.setName(counterPath.getName());

                                    File agentsPath = new File(counterPath.getAbsoluteFile() + File.separator + "agents");

                                    List<String> allAgents = Arrays.asList(agentsPath.list());
                                    List<String> agentsHavingData = new ArrayList<String>();

                                    for(String agent : allAgents) {
                                        if(new File(jobFSConfig.getJobFunctionStatsFile(jobId,
                                                groupPath.getName(),
                                                "counters",
                                                counterPath.getName(),
                                                agent)).exists()) {
                                            agentsHavingData.add(agent);
                                            groupHasStats = true;
                                        }
                                    }

                                    metricStatsMeta.setAgents(agentsHavingData);
                                    groupCounters.add(metricStatsMeta);

                                }
                            }
                        }

                    }
                }
                groupStatsMeta.setTimers(groupTimers);
                groupStatsMeta.setCounters(groupCounters);
                if(groupHasStats)
                    groups.add(groupStatsMeta);
            }
        }
        return groups;
    }

    /**
     * Return Input stream representing real time Group Configuration Change
     * @param jobId
     * @return
     */
    public InputStream getJobGroupConf(String jobId, String groupName, String agent, boolean last) throws FileNotFoundException {
        File statsFile = new File(jobFSConfig.getJobGroupConfFile(jobId, groupName, agent));
        log.info(statsFile.getAbsolutePath());
        if(!statsFile.exists())
            throw new WebApplicationException(ResponseBuilder.response(Response.Status.NOT_FOUND, String.format("Real Time Group Conf Not Received Yet for Group %s",groupName)));

        if(last)
            statsFile = new File(statsFile.getAbsoluteFile()+".last");
        return new FileInputStream(statsFile.getAbsoluteFile());
    }


    /**
     * Get Stats for specific metric for a job
     * @param jobId
     * @param groupName
     * @param metricType
     * @param metricName
     * @param agent
     * @param last
     * @return
     * @throws FileNotFoundException
     */
    public InputStream getJobMetricStats(String jobId, String groupName, String metricType, String metricName, String agent, boolean last) throws FileNotFoundException {
        File statsFile = new File(jobFSConfig.getJobFunctionStatsFile(jobId, groupName, metricType, metricName, agent));
        log.info(statsFile.getAbsolutePath());
        if(!statsFile.exists())
            throw new WebApplicationException(ResponseBuilder.response(Response.Status.NOT_FOUND, String.format("Stats for %s %s Not computed yet",metricType,metricName)));

        if(last)
            statsFile = new File(statsFile.getAbsoluteFile()+".last");
        return new FileInputStream(statsFile.getAbsoluteFile());
    }

    public static class MonitoringAgentStats {
        private String agent;
        private List<String> resources;

        public String getAgent() {
            return agent;
        }

        public void setAgent(String agent) {
            this.agent = agent;
        }

        public List<String> getResources() {
            return resources;
        }

        public void setResources(List<String> resources) {
            this.resources = resources;
        }
    }

    /**
     * Get Meta Information about all the monitoring metrics stored for a Job
     * @param jobId
     * @return
     */
    public List<MonitoringAgentStats> getJobMonitoringStatsMeta(String jobId) {
        List<MonitoringAgentStats> monitoringStats = new ArrayList<MonitoringAgentStats>();
        File monitoringResourcesPath = new File(jobFSConfig.getJobMonitoringStatsPath(jobId));
        if(monitoringResourcesPath.exists()) {
            for(File agentPath : new File(monitoringResourcesPath.getAbsolutePath() + File.separator + "agents").listFiles()) {
                MonitoringAgentStats monitoringAgentStats = new MonitoringAgentStats();
                monitoringAgentStats.setAgent(agentPath.getName());
                List<String> resources = new ArrayList<String>();
                for(File resourceFile : new File(agentPath.getAbsolutePath() + File.separator + "Resources").listFiles()) {
                    if(resourceFile.getName().endsWith(".txt"))
                        resources.add(resourceFile.getName().replace(".txt", ""));
                }
                monitoringAgentStats.setResources(resources);
                monitoringStats.add(monitoringAgentStats);
            }
        }
        return monitoringStats;

    }

    /**
     * Get collected resource stats that is being monitored for a job
     * @param jobId
     * @param agent
     * @param resourceName
     * @param last
     * @return
     * @throws FileNotFoundException
     */
    public InputStream getJobMonitoringResourceStats(String jobId, String agent, String resourceName, Boolean last) throws FileNotFoundException {
        File statsFile = new File(jobFSConfig.getJobResourceMonitoringFile(jobId, agent, resourceName));
        log.info(statsFile.getAbsolutePath());
        if(!statsFile.exists())
            throw new WebApplicationException(ResponseBuilder.response(Response.Status.NOT_FOUND, String.format("Monitoring Stats for %s %s Not collected yet",agent,resourceName)));

        if(last)
            statsFile = new File(statsFile.getAbsoluteFile()+".last");
        return new FileInputStream(statsFile.getAbsoluteFile());

    }

    /**
     * Returns metric key value collected for a Resource
     * @param jobId
     * @param agent
     * @param resourceName
     * @return
     */
    public Set<String> getJobMonitoringResourceMeta(String jobId, String agent, String resourceName) throws IOException {
        File statsFile = new File(jobFSConfig.getJobResourceMonitoringFile(jobId, agent, resourceName));
        log.info(statsFile.getAbsolutePath());
        if(!statsFile.exists())
            throw new WebApplicationException(ResponseBuilder.response(Response.Status.NOT_FOUND, String.format("Monitoring Stats for %s %s Not collected yet",agent,resourceName)));

        Map<String, Object> resourceLastInstance = objectMapper.readValue(new File(statsFile.getAbsolutePath()+".last"), Map.class);
        Map<String, Object> metricValueMap = (Map<String, Object>) resourceLastInstance.get("metrics");
        return metricValueMap.keySet();
    }

    /**
     * This function really doesn't belong here. Just added here for the time being.
     * @param runName
     * @return
     */
    public PerformanceRun getPerformanceRun(String runName) throws IOException {
        return objectMapper.readValue(new File(jobFSConfig.getRunFile(runName)), PerformanceRun.class);
    }
}