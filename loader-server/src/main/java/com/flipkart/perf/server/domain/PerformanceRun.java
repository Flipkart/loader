package com.flipkart.perf.server.domain;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.flipkart.perf.common.util.FileHelper;

import com.flipkart.perf.server.cache.JobsCache;
import com.flipkart.perf.server.config.JobFSConfig;
import com.flipkart.perf.server.config.LoaderServerConfiguration;
import com.flipkart.perf.server.exception.InvalidJobStateException;
import com.flipkart.perf.server.util.ObjectMapperUtil;
import com.flipkart.perf.server.util.ResponseBuilder;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;

/**
 * Represents a Performance Run
 */
public class PerformanceRun {
    private String businessUnit = "default";
    private String team = "default";
    private String runName;
    private List<LoadPart> loadParts;
    private List<OnDemandMetricCollection> onDemandMetricCollections;
    private List<MetricCollection> metricCollections;
    private String description = "";
    private int currentVersion = 1;
    private int version = 1;

    private static final JobFSConfig  jobFSConfig = LoaderServerConfiguration.instance().getJobFSConfig();
    private static Logger logger = LoggerFactory.getLogger(PerformanceRun.class);
    private static final ObjectMapper mapper = ObjectMapperUtil.instance();

    public String getRunName() {
        return runName;
    }

    public PerformanceRun setRunName(String runName) {
        this.runName = runName;
        return this;
    }

    public List<LoadPart> getLoadParts() {
        return loadParts;
    }

    public PerformanceRun setLoadParts(List<LoadPart> loadParts) {
        this.loadParts = loadParts;
        return this;
    }

    public List<OnDemandMetricCollection> getOnDemandMetricCollections() {
        return onDemandMetricCollections;
    }

    public PerformanceRun setOnDemandMetricCollections(List<OnDemandMetricCollection> onDemandMetricCollections) {
        this.onDemandMetricCollections = onDemandMetricCollections;
        return this;
    }

    public List<MetricCollection> getMetricCollections() {
        return metricCollections;
    }

    public PerformanceRun setMetricCollections(List<MetricCollection> metricCollections) {
        this.metricCollections = metricCollections;
        return this;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int agentsNeeded() {
        int totalAgentsNeeded = 0;
        for(LoadPart loadPart : this.getLoadParts()) {
            totalAgentsNeeded += loadPart.getAgents();
        }
        return totalAgentsNeeded;
    }

    public boolean exists() {
        return new File(jobFSConfig.getRunPath(runName)).exists();
    }

    public void create() throws IOException {
        persist();
        BusinessUnit businessUnit = BusinessUnit.build(this.businessUnit);
        businessUnit.addRuns(this.team, Arrays.asList(new String[]{this.runName})).persist();
    }

    public void update(PerformanceRun newRun) throws IOException {
        BusinessUnit businessUnit = BusinessUnit.build(this.getBusinessUnit());
        businessUnit.getTeam(this.getTeam()).getRuns().remove(this.getRunName());
        businessUnit.persist();

        newRun.setVersion(latestVersion(newRun.runName) +  1);
        newRun.persist();
        businessUnit = BusinessUnit.build(newRun.businessUnit);
        businessUnit.addRuns(newRun.team, Arrays.asList(new String[]{newRun.runName})).persist();
    }

    /**
     * Return Latest version of given run
     * @param runName
     * @return
     * @throws IOException
     */
    public static int latestVersion(String runName) throws IOException {
        File runMetaFile = new File(jobFSConfig.getRunMetaInfoFile(runName));
        if(runMetaFile.exists()) {
            RunMeta runMeta = mapper.readValue(new File(jobFSConfig.getRunMetaInfoFile(runName)), RunMeta.class);
            return runMeta.getLatestVersion();
        }
        else {
            return 0;
        }
    }

    public void persist() throws IOException {
        String runFile = jobFSConfig.getRunFile(runName, version);
        FileHelper.createFilePath(runFile);
        ObjectMapper mapper = ObjectMapperUtil.instance();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(runFile), this);

        // Updating the Version
        RunMeta runMeta = new RunMeta();
        if(version > 1){
            runMeta = mapper.readValue(new File(jobFSConfig.getRunMetaInfoFile(runName)), RunMeta.class);
            runMeta.setLatestVersion(version);

        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(jobFSConfig.getRunMetaInfoFile(runName)), runMeta);
    }

    public String getBusinessUnit() {
        return businessUnit;
    }

    public PerformanceRun setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
        return this;
    }

    public String getTeam() {
        return team;
    }

    public PerformanceRun setTeam(String team) {
        this.team = team;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static PerformanceRun runExistsOrException(String runName) throws IOException {
        return runExistsOrException(runName, "latest");
    }

    public static PerformanceRun runExistsOrException(String runName, String version) throws IOException {
        if(!new File(jobFSConfig.getRunPath(runName)).exists()) {
            throw new WebApplicationException(ResponseBuilder.runNameDoesNotExist(runName));
        }

        if(!new File(jobFSConfig.getRunFile(runName, runVersion(runName, version))).exists()) {
            throw new WebApplicationException(ResponseBuilder.runNameDoesNotExist(runName + " Version " + version));
        }

        return mapper.
                readValue(new File(jobFSConfig.getRunFile(runName, runVersion(runName, version))), PerformanceRun.class);
    }

    public static int runVersion(String runName, String version) throws IOException {
        if(version == null)
            version = "LATEST";
        if("LATEST".equalsIgnoreCase(version)) {
            return latestVersion(runName);
        }
        else {
            return Integer.parseInt(version);
        }
    }

    public void delete() throws IOException {
        File runJobsFile = new File(jobFSConfig.getRunAllJobsFile(this.runName));
        if(runJobsFile.exists())  {
            BufferedReader jobReader = null;
            try {
                jobReader = new BufferedReader(new InputStreamReader(new FileInputStream(runJobsFile)));
                String jobId;
                while((jobId = jobReader.readLine()) != null) {
                    if(!jobId.trim().equals("")) {
                        Job job = JobsCache.getJob(jobId);
                        job.delete();
                    }
                }
            }
            catch (IOException e) {
                logger.error("Error while deleting Job details for run "+this.runName,e);
            } catch (ExecutionException e) {
                logger.error("Error while deleting Job details for run " + this.runName, e);
            } catch (InvalidJobStateException e) {
                logger.error("Error while deleting Job details for run " + this.runName, e);
            } finally {
                if(jobReader != null)
                    jobReader.close();
            }
        }

        FileHelper.remove(jobFSConfig.getRunPath(runName));
        BusinessUnit businessUnit = BusinessUnit.build(this.getBusinessUnit());
        businessUnit.getTeam(this.team).getRuns().remove(this.runName);
        businessUnit.persist();
    }

    public static List<String> versions(String runName) {
        File runVersionsPath = new File(jobFSConfig.getRunVersionsPath(runName));
        if(runVersionsPath.exists()) {
            return Arrays.asList(runVersionsPath.list());
        }
        return new ArrayList<String>();  //To change body of created methods use File | Settings | File Templates.
    }
}
