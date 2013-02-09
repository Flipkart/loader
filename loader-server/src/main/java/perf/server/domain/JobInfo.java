package perf.server.domain;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 7/2/13
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobInfo {

    public static enum JOB_STATUS {
        RUNNING, PAUSED, COMPLETED, KILLED
    }
    private String jobId;
    private JOB_STATUS jobStatus;
    private Map<String,JOB_STATUS> agentsJobStatus;
    private Set<String> monitoringAgents;

    public JobInfo() {
        this.jobStatus = JOB_STATUS.RUNNING;
        this.monitoringAgents = new HashSet<String>();
        this.agentsJobStatus = new HashMap<String, JOB_STATUS>();
    }

    public String getJobId() {
        return jobId;
    }

    public JobInfo setJobId(String jobId) {
        this.jobId = jobId;
        return this;
    }

    public JOB_STATUS getJobStatus() {
        return jobStatus;
    }

    public JobInfo setJobStatus(JOB_STATUS jobStatus) {
        this.jobStatus = jobStatus;
        return this;
    }

    public Set<String> getMonitoringAgents() {
        return monitoringAgents;
    }

    public void setMonitoringAgents(Set<String> monitoringAgents) {
        this.monitoringAgents = monitoringAgents;
    }

    public JobInfo addMonitoringAgent(String agentIp) {
        monitoringAgents.add(agentIp);
        return this;
    }

    public Map<String, JOB_STATUS> getAgentsJobStatus() {
        return agentsJobStatus;
    }

    public JobInfo setAgentsJobStatus(Map<String, JOB_STATUS> agentsJobStatus) {
        this.agentsJobStatus = agentsJobStatus;
        return this;
    }

    public JobInfo jobRunningInAgent(String agentIp) {
        this.agentsJobStatus.put(agentIp, JOB_STATUS.RUNNING);
        return this;
    }

    public JobInfo jobCompletedInAgent(String agentIp) {
        this.agentsJobStatus.put(agentIp, JOB_STATUS.COMPLETED);
        if(!this.agentsJobStatus.containsValue(JOB_STATUS.RUNNING) &&
                !this.agentsJobStatus.containsValue(JOB_STATUS.PAUSED) &&
                !this.agentsJobStatus.containsValue(JOB_STATUS.KILLED))
            this.jobStatus = JOB_STATUS.COMPLETED;
        return this;
    }

    public JobInfo jobPausedInAgent(String agentIp) {
        this.agentsJobStatus.put(agentIp, JOB_STATUS.PAUSED);
        return this;
    }

    public JobInfo jobKilledInAgent(String agentIp) {
        this.agentsJobStatus.put(agentIp, JOB_STATUS.KILLED);
        return this;
    }
}
