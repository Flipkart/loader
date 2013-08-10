function searchJobs(){
	var runName = $("#runName").val();
	var partialJobId = $("#jobId").val();
	var jobStatus=$("#jobStatus").val();
	var activeIn = $("#activeIn").val();
	var searchJobsUrl="/loader-server/jobs?";
	if (runName !== "" && typeof runName !== 'undefined' ) searchJobsUrl+=("&runName=" + runName);
	if (partialJobId !== "" && typeof runName !== 'undefined') searchJobsUrl+=("&jobId=" + partialJobId);
	if (jobStatus !== "" && typeof runName !== 'undefined') searchJobsUrl+=("&jobStatus=" + jobStatus);
	//if (activeIn !== "") searchJobsUrl+=("&activeIn=" + activeIn);
	console.log(searchJobsUrl);
	$.ajax({
		url: searchJobsUrl,
		contentType: "application/json", 
		dataType:"json",
		type:"GET",
		async:false,
		success: function(jobs){
			$.each(jobs, function(index, job){
				job["startTime"] = job["startTime"]==null?"Yet to start":new Date(job["startTime"]).toLocaleString();
				job["endTime"] = job["endTime"]==null?"Yet to Finish":new Date(job["endTime"]).toLocaleString();
				switch(job["jobStatus"]){
					case "QUEUED": job["rowClass"]="warning";
						break;
					case "RUNNING": job["rowClass"] = "success";
						break;
					case "COMPLETED": job["rowClass"] = "info";
						break;
					case "FAILED_TO_START": job["rowClass"] = "error";
						break;

				}
				job["runUrl"] = "/updaterun.html?&runName=" + job["runName"];
				job["jobUrl"] = "/jobdetails.html?&jobId=" + job["jobId"];
			});
			window.jobsList = jobs;
		},
		error: function(err){
			console.log(err);
		},
		complete: function(xhr, status){
			console.log("complete");
		}
	});
}

function searchJobsListModel(){
	var self = this;
	self.fetch = function(){
		searchJobs();
		return window.jobsList;
	};
	self.jobs = ko.observableArray(self.fetch());
	self.getJobsList = function(){
		$("#jobsList").find('tbody').empty();
		self.jobs(self.fetch());
		var resort = true;
       	$("#jobsList").trigger("update", [resort]);
       	var sorting = [[2,1],[0,0]];
        $("#jobsList").trigger("sorton", [sorting]);
		console.log(self);
	};
}

ko.bindingHandlers.sortTable = {
    init: function(element, valueAccessor) {
        setTimeout( function() {
            $(element).addClass('tablesorter');
            $(element).tablesorter({
            	theme: "bootstrap",
            	widthFixed: "false",
            	headerTemplate: '{content} {icon}',
            	widgets: ['uitheme'],
            })
            .tablesorterPager({
            	container: $(".pager"),
            	updateArrows: true,
            	page : 0,
            	size : 10,
            	removeRows: false,
            	output: '{page}/{totalPages}',
            	cssNext: '.next',
            	cssPrev: '.prev',
            	cssFirst: '.first',
            	cssLast: '.last',
            	cssGoto: '.gotoPage',
            	cssPageDisplay: '.pagedisplay', 
            	cssPageSize: '.pagesize',
            });
        }, 0);
    }
};
