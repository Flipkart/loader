function searchJobs(){
	var runName = $("#runName").val();
	var partialJobId = $("#jobId").val();
	var jobStatus=$("#jobStatus").val();
	var activeIn = $("#activeIn").val();
	var searchJobsUrl="/loader-server/jobs?";
	if (runName !== "") searchJobsUrl+=("&runName=" + runName);
	if (partialJobId !== "") searchJobsUrl+=("&jobId=" + partialJobId);
	if (jobStatus !== "") searchJobsUrl+=("&jobStatus=" + jobStatus);
	//if (activeIn !== "") searchJobsUrl+=("&activeIn=" + activeIn);
	console.log(searchJobsUrl);
	$.ajax({
		url: searchJobsUrl,
		contentType: "application/json", 
		dataType:"json",
		type:"GET",
		success: function(jobs){
			var insertHtml = "<table width=\"60%\" border=\"1px\" align=\"center\">";
			$.each(jobs, function(index, job){
				console.log(job);
				insertHtml = insertHtml + "<tr><td><a href=/job_details.html?&jobid=" + job["jobId"] + ">" + job["jobId"] +"</a></td></tr>";
			});
			insertHtml = insertHtml + "</table>";
			$("#jobsList").empty();
			$("#jobsList").append(insertHtml);
		},
		error: function(err){
			console.log(err);
		},
		complete: function(xhr, status){
			console.log("complete");
		}
	});
}