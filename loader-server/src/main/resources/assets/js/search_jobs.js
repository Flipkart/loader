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
			$("#jobsList").removeAttr("hidden");
			displayResults();
			//displayList(0);
			sortAndDisplay();
		},
		error: function(err){
			console.log(err);
		},
		complete: function(xhr, status){
			console.log("complete");
		}
	});
}

function sortList(sortOption){
	window.jobList.sort(function(job1, job2){
		if (sortOption=="runName"){
			if (job1["runName"]>job2["runName"]) return 1;
			if(job1["runName"]<job2["runName"]) return -1;
			return 0;
		} else {
			if (sortOption=="startTime"){
				return job2["startTime"] - job1["startTime"];
			} else {
				return job2["endTime"] - job1["endTime"] 
			}	
		}
	});
}

function displayResults(){
	if (window.jobList.length==0) return;
	var pageNumbers =  (window.jobList.length/5)>>0;
	if (window.jobList.length%5!=0) pageNumbers=pageNumbers +1;
	//console.log("visible", visible, pageNumbers);
	$("#jobsList").jPaginator({
		nbPages: pageNumbers,
		nbVisible: 5,
		marginPx: 5,
		widthPx: 25,
		selectedPage: 0,
		overBtnLeft: '#jobsList_o_left',
		overBtnRight: '#jobsList_o_right',
		maxBtnLeft:'#jobsList_m_left', 
    	maxBtnRight:'#jobsList_m_right',
    	withSlider:false,
		//minSlidesForSlider:5,
		onPageClicked: function(a, num){
			console.log("a is", a, "num is", num);
			displayList(num*5);
		}
	})
}

function displayList(index){
	var color = {
		"COMPLETED": "#006400",
		"RUNNING": "#FF0000"
	}
	var startIndex = index,
		endIndex = index +5<window.jobList.length?index+5:window.jobList.length;
	var insertHtml = "<table width=\"80%\" border=\"1px\" align=\"center\">";
	insertHtml = insertHtml + "<thead><tr><td style=\"width:5%;text-align:center\"><label><strong>#.</strong></label></td>";
	insertHtml = insertHtml + "<td style=\"width:30%;text-align:center\"><label><strong>Job Id</strong></label></td>" + 
	"<td style=\"width:20%;text-align:center\"><label><strong>RUN Name</strong></label></td><td style=\"width:15%;text-align:center\"><label><strong>Start Time</strong></label></td><td style=\"width:15%;text-align:center\"><label><strong>End Time</strong></label></td><td style=\"width:15%;text-align:center\"><label><strong>Status</strong></label></td></tr></thead><tbody>";
	$.each(window.jobList.slice(startIndex, endIndex), function(jobIndex, job){
		console.log(job);
		var slNo = startIndex + jobIndex + 1;
		insertHtml = insertHtml + "<tr><td>" + slNo + "</td><td><a href=/job_details.html?&jobid=" + job["jobId"] + ">" + job["jobId"] +"</a></td><td><a href=\"/workflow.html?&workflow=" + job["runName"] + "\">" + job["runName"] + "</a></td><td>" + (new Date(job["startTime"])).toString()+ "</td><td>" + (new Date(job["startTime"])).toString() + "</td><td style=\"color:" + color[job["jobStatus"]] + "\">" + job["jobStatus"] + "</td></tr>";
	});
	insertHtml = insertHtml + "</tbody></table>";
	$("#jobsListDetails").empty();
	$("#jobsListDetails").append(insertHtml);
}

function sortAndDisplay(){
	var opt = $("#sortOptions").val();
	console.log("option is", opt);
	sortList(opt);
	displayList(0);
}



