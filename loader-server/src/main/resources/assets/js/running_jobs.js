function getRunningJobs(){
	var runningJobsUrl = "/loader-server/jobs";
	$(document).ready(function(e) {
        $.ajax({url: runningJobsUrl,
			contentType: "application/json", 
			dataType:"json",
			type:"GET",
			success: function(jobs) {
				console.log(runningJobsUrl);
				$.each(jobs, function(index, job){
					var jobName = job["runName"];
					var jobId = job["jobId"];
					var status = job["jobStatus"];
					$("#jobs").append("<tr><td><a href=/workflow.html?&workflow=" + jobName + ">" + jobName + "</a></td><td><a href=/job_details.html?&jobid=" + jobId + '>' + jobId + "</a></td><td>" + status + "</td></tr>");
				})
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