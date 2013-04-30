package perf.server.domain;

import java.util.*;

public class Job {

    public static enum JOB_STATUS {
        QUEUED, RUNNING, PAUSED, COMPLETED, KILLED, FAILED_TO_START;
    }

    private String jobId;
    private String runName;
    private Date startTime, endTime;
    private JOB_STATUS jobStatus;
    private Map<String,AgentJobStatus> agentsJobStatus;
    private Set<String> monitoringAgents;

    public static class AgentJobStatus {
        private String agentIp;
        private boolean inStress;
        private JOB_STATUS job_status;
        private Map<String, Object> healthStatus;

        public String getAgentIp() {
            return agentIp;
        }

        public AgentJobStatus setAgentIp(String agentIp) {
            this.agentIp = agentIp;
            return this;
        }

        public JOB_STATUS getJob_status() {
            return job_status;
        }

        public AgentJobStatus setJob_status(JOB_STATUS job_status) {
            this.job_status = job_status;
            return this;
        }

        public Map<String, Object> getHealthStatus() {
            return healthStatus;
        }

        public AgentJobStatus setHealthStatus(Map<String, Object> healthStatus) {
            if(healthStatus != null) {
                this.healthStatus = healthStatus;
                this.inStress = (Boolean)this.healthStatus.remove("inStress");
            }
            return this;
        }

        public boolean isInStress() {
            return inStress;
        }

        public AgentJobStatus setInStress(boolean inStress) {
            this.inStress = inStress;
            return this;
        }
    }

    public Job() {
        this.jobStatus = JOB_STATUS.QUEUED;
        this.monitoringAgents = new HashSet<String>();
        this.agentsJobStatus = new LinkedHashMap<String, AgentJobStatus>();
    }

    public String getJobId() {
        return jobId;
    }

    public Job setJobId(String jobId) {
        this.jobId = jobId;
        return this;
    }

    public String getRunName() {
        return runName;
    }

    public Job setRunName(String runName) {
        this.runName = runName;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Job setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Job setEndTime(Date endTime) {
        this.endTime = endTime;
        return this;
    }

    public JOB_STATUS getJobStatus() {
        return jobStatus;
    }

    public Job setJobStatus(JOB_STATUS jobStatus) {
        this.jobStatus = jobStatus;
        return this;
    }

    public Set<String> getMonitoringAgents() {
        return monitoringAgents;
    }

    public void setMonitoringAgents(Set<String> monitoringAgents) {
        this.monitoringAgents = monitoringAgents;
    }

    public Job addMonitoringAgent(String agentIp) {
        monitoringAgents.add(agentIp);
        return this;
    }


    public Job jobRunningInAgent(String agentIp) {
        this.jobStatus = JOB_STATUS.RUNNING;
        this.agentsJobStatus.put(agentIp, new AgentJobStatus().setAgentIp(agentIp).setJob_status(JOB_STATUS.RUNNING));
        return this;
    }

    public Job jobCompletedInAgent(String agentIp) {
        JOB_STATUS jobStatusInAgent = this.agentsJobStatus.get(agentIp).getJob_status();
        if(jobStatusInAgent != JOB_STATUS.KILLED)
            this.agentsJobStatus.get(agentIp).setJob_status(JOB_STATUS.COMPLETED);

        if(!this.agentsJobStatus.containsValue(JOB_STATUS.RUNNING) &&
                !this.agentsJobStatus.containsValue(JOB_STATUS.PAUSED))
            this.jobStatus = JOB_STATUS.COMPLETED;

        return this;
    }

    public Job jobPausedInAgent(String agentIp) {
        this.agentsJobStatus.get(agentIp).setJob_status(JOB_STATUS.PAUSED);
        return this;
    }

    public Job jobKilledInAgent(String agentIp) {
        JOB_STATUS jobStatusInAgent = this.agentsJobStatus.get(agentIp).getJob_status();
        if(jobStatusInAgent != JOB_STATUS.COMPLETED)
            this.agentsJobStatus.get(agentIp).setJob_status(JOB_STATUS.KILLED);

        if(!this.agentsJobStatus.containsValue(JOB_STATUS.RUNNING) &&
                !this.agentsJobStatus.containsValue(JOB_STATUS.PAUSED))
            this.jobStatus = JOB_STATUS.COMPLETED;
        return this;
    }

    public boolean completed() {
        return this.getJobStatus().equals(Job.JOB_STATUS.COMPLETED) ||
                        this.getJobStatus().equals(Job.JOB_STATUS.KILLED);
    }

    public Map<String, AgentJobStatus> getAgentsJobStatus() {
        return agentsJobStatus;
    }

    public Job setAgentsJobStatus(Map<String, AgentJobStatus> agentsJobStatus) {
        this.agentsJobStatus = agentsJobStatus;
        return this;
    }
}
