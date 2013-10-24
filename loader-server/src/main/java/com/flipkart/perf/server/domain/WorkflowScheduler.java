package com.flipkart.perf.server.domain;

import com.flipkart.perf.server.util.ObjectMapperUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created with IntelliJ IDEA.
 * User: shwet.shashank
 * Date: 11/10/13
 * Time: 10:55 AM
 * To change this template use File | Settings | File Templates.
 */


public class WorkflowScheduler {
    private static  SchedulerFactory SCHEDULER_FACTORY;
    private static  Scheduler SCHEDULER;
    private static  ObjectMapper mapper;
    //private ScheduledWorkflow scheduledWorkflow;


    public static void initialize() throws SchedulerException {
        SCHEDULER_FACTORY = new StdSchedulerFactory();
        SCHEDULER = SCHEDULER_FACTORY.getScheduler();
        mapper = ObjectMapperUtil.instance();
        SCHEDULER.start();
    }


    public static String createSchedule(ScheduledWorkflow scheduledWorkflow) throws SchedulerException, IOException, ParseException {
        //Check if already an instance running for given workflow.
        JobDetail j = newJob(ScheduledWorkflowJobCreator.class).
                withIdentity("LoaderWorkflow", scheduledWorkflow.getName()).
                usingJobData("workflow", mapper.writeValueAsString(scheduledWorkflow.getWorkflow())).
                usingJobData("workflowName", scheduledWorkflow.getName()).
                build();
        if(SCHEDULER.checkExists(j.getKey()))
            return "{\"message\":\"One instance of this workflow already running!! Please wait.\"}";
        Trigger t = null;
        ScheduleBuilder schBuilder = null;
        if(scheduledWorkflow.getSchedulerType().toUpperCase().equals("CRON")){
           CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(scheduledWorkflow.getCronExpression());
           schBuilder = cronScheduleBuilder;

        } else {
            SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
            if(scheduledWorkflow.isRunForEver()){
                scheduleBuilder.repeatForever();
            }  else {
                if(scheduledWorkflow.getRepeats()>0){
                    scheduleBuilder.withRepeatCount(scheduledWorkflow.getRepeats());
                }
            }
            if(scheduledWorkflow.getInterval()>0) scheduleBuilder.withIntervalInSeconds(scheduledWorkflow.getInterval()/1000);
            schBuilder = scheduleBuilder;
        }
        TriggerBuilder triggerBuilder= newTrigger();
        triggerBuilder.
                withIdentity("LoaderWorkflowTrigger", scheduledWorkflow.getName()).
                withSchedule(schBuilder).forJob(j.getKey());
        if(scheduledWorkflow.isStartNow()){
            triggerBuilder.startNow();
        }  else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            if(scheduledWorkflow.getStartAt()!=null){
                triggerBuilder.startAt(dateFormat.parse(scheduledWorkflow.getStartAt()));
            }
            if(scheduledWorkflow.getEndAt()!=null){
                triggerBuilder.endAt(dateFormat.parse(scheduledWorkflow.getEndAt()));
            }
        }
        t = triggerBuilder.build();
        SCHEDULER.scheduleJob(j,t);
        return "{\"message\":\"Workflow Scheduled.\"}";
    }

    public static ArrayList<String> allRunningWorkflows() throws SchedulerException {
        ArrayList<JobExecutionContext> jobExecutionContexts = (ArrayList<JobExecutionContext>)
                SCHEDULER.getCurrentlyExecutingJobs();
        ArrayList<String> list = new ArrayList<String>();
        for(JobExecutionContext context: jobExecutionContexts){
            list.add(context.getJobDetail().getKey().getName());
        }
        return list;
    }

    public static void removeWorkflow(String workflowName) throws SchedulerException {
        SCHEDULER.deleteJob(jobKey("LoaderWorkflow",workflowName));
    }
}