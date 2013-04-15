<b>Overview<b>
========
loader2.0 is a distributed load generation platform with server side monitoring.<br>
<br><b>High level Feature List</b>
* 1) <b>Centralized Deployment/Hosted Solution :</b> Loader2.0 comes along with support of UI and REST services for all
features. It allows teams/companies to have a centralised deployment of platform instead of every user having to deploy
locally and trigger performance runs locally. Users can create their performance runs on UI, can trigger runs, monitor 
them and visualize reports in near real time.
<br><br>
* 2) <b>Scaling Load using Distributed Load Generation Capability :</b> Loader2.0 deployment works as server-agent 
model. User can define work load on server UI and also mention how many agents should be used to generate the load. 
This feature is extremely important when user need to generate in the order of 10s-100s of 1000s of requests per second
, which is not possible if you generate from single machine.
<br><br>
* 3) <b>Customizable/extendable Workload :</b> Users are free to used Out of the box functions like HTTPGet, HTTPPost, HTTPDelete 
etc to build their performance workload. But if such functions are not enough/suitable to simulate you use case. 
Loader comes along with SDK which allows user to write their own specific performance functions. These functions 
can then be clubbed in 'jar' and deployed on the loader instance. and now you can use your own functions and build 
performance workflow.
<br><br>
* 4) <b>Server Side and App stats Monitoring :</b> Loader deployment comes along with monitoring-agent deployment. This
monitoring-agent allows user to monitor/collect system metrics like cpu, memory, processes, network usage etc while the
your application is under performance test. In addition to monitoring system stats of box where your application under 
test is deployed, monitoring-agent also allows user to monitor application stats like stats exposed by JMX, mysql
connections etc.
<br><br>
* 5) <b>Live Reporting :</b> While Load generation activity is going on you can monitor both Load stats like throughput,
mean response times, standard deviations, percentiles and monitoring stats like cpu, memory , jmx, network usage etc in LIVE.

