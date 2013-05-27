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
			window.jobList = jobs;
			window.nextCounter = 0;
			displayResults();
		},
		error: function(err){
			console.log(err);
		},
		complete: function(xhr, status){
			console.log("complete");
		}
	});
}

function displayResults(){
	var begin = window.nextCounter*10;
	var last = begin + 10 > window.jobList.length?window.jobList.length:begin + 10;
	var insertHtml = "<table width=\"60%\" border=\"1px\" align=\"center\">";
			insertHtml = insertHtml + "<thead><tr><td style=\"width:50%;text-align:left\"><button id=\"previousList\" onClick=\"previousList()\">Previous</button></td>";
			insertHtml = insertHtml + "<td style=\"width:50%;text-align:right\"><button id=\"nextList\" onClick=\"nextList()\">Next</button></td></tr></thead><tbody>";
			$.each(window.jobList.slice(begin, last), function(index, job){
				console.log(job);
				insertHtml = insertHtml + "<tr><td colspan=\"2\"><a href=/job_details.html?&jobid=" + job["jobId"] + ">" + job["jobId"] +"</a></td></tr>";
			});
			insertHtml = insertHtml + "</tbody></table>";
			$("#jobsList").empty();
			$("#jobsList").append(insertHtml);
			if (window.nextCounter==0) $("#previousList").attr("disabled","true");
			if (window.nextCounter==((window.jobList.length/10)>>0)) $("#nextList").attr("disabled","true");
}

function previousList(){
	window.nextCounter = window.nextCounter -1;
	displayResults();
	if (window.nextCounter==0) $("#previousList").attr("disabled","true");
	if (window.nextCounter==((window.jobList.length/10)-1)) $("#nextList").removeAttr("disabled");
}

function nextList(){
	window.nextCounter = window.nextCounter + 1;
	displayResults();
	if (window.nextCounter==1) $("#previousList").removeAttr("disabled");
	if (window.nextCounter==(window.jobList.length/10)) $("#nextList").attr("disabled","true");
}






