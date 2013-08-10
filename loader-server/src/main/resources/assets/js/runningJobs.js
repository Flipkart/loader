function getRunningJobs(){
	var runningJobsUrl = "/loader-server/jobs";
	$(document).ready(function(e) {
        $.ajax({url: runningJobsUrl,
			contentType: "application/json", 
			dataType:"json",
			type:"GET",
			success: function(jobs) {
				//console.log(runningJobsUrl);
				$.each(jobs, function(index, job){
					var agents="";
					$.each(job["agentsJobStatus"], function(k,v){
						agents=agents + "," + k;
					})
					job["agents"] = agents;
					var startTime = new Date(job["startTime"]);
					if (startTime > new Date(1)){
						startTime = startTime.toLocaleString();
					} else {
						startTime = "Yet To Start";
					}
					job["startTime"] = startTime;
					job["runUrl"] = "/updaterun.html?&runName=" + job["runName"];
					job["jobUrl"] = "/jobdetails.html?&jobId=" + job["jobId"];
					// var insertHtml = "<tbody><tr><td><a href=\"/jobdetails.html?&jobid=" + jobId + "\">" + jobId + "</a></td><td><a href=\"updaterun.html?&runname=" + jobName +"\">" + jobName + "</a></td><td>" + startTime + "</td><td>" + status + "</td><td>" + agents + "</td></tbody>";
					// $("#jobsList").append(insertHtml);
				})
				ko.applyBindings(new jobViewModel(jobs));
				},
			error: function(e){
				console.log("Error");
				},
			complete: function(xhr, status){
				console.log(status);
				}
				});
			console.log("Done with ajax");
    });
}

function jobViewModel(allJobs){
	var self = this;
	self.jobs = ko.observableArray(allJobs);
}