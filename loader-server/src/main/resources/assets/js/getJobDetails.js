function getQueryParams(sParam){
	var queryString = window.location.search.substring(2);
	var queryParams = queryString.split('&');
	for (var i = 0; i < queryParams.length; i++){
        var sParameterName = queryParams[i].split('=');
		console.log(sParameterName[0]);
        if (sParameterName[0] == sParam){
				//console.log('matched');
            return sParameterName[1];
        }
    }
}
function getJobDetails(){
	var jobUrl = "/loader-server/jobs/" + getQueryParams('jobId');   //replace this with previous one
	console.log(jobUrl);
        $.ajax({
        	url: jobUrl,
			contentType: "application/json", 
			dataType:"json",
			type:"GET",
			success: function(data){
				console.log("creating the view");
				window.viewModel = new jobDetailViewModel(data);
				window.viewModel.remarks.subscribe(function(remarks){
				var jobId = getQueryParams("jobId");
				jobUrl = "loader-server/jobs/" + jobId + "/remarks";
					$.ajax({
						url: jobUrl,
						contentType: "application/json", 
						dataType:"json",
						type:"PUT",
						data: remarks,
						async:false,
						success: function(data){
							window.existingRuns = data;
						},	
						error: function(){
							console.log("Error in getting runs");
						},
						complete: function(xhr, status){
							//location.reload();
						}
					});
				});
				ko.applyBindings(window.viewModel);
			},
			error: function(e){
				console.log("Error");
				},
			complete: function(xhr, status){
				console.log(status);
				}
		});
}

var jobDetailViewModel = function(jobDetails){
	var agentList = [];
	window["jobAgents"] = [];
	$.each(jobDetails["agentsJobStatus"], function(k,v){
		if(v["inStress"] == false){
			v["health"] = "OK";
			v["rowClass"] = "info";
		} else {
			v["health"] = "In Stress";
			v["rowClass"] = "error";
		}
		console.log("job_status is ", v["job_status"]);
		if(v["job_status"]=="RUNNING"){
			v["stopAgentJobClass"]="btn";
		} else {
			v["stopAgentJobClass"] = "btn disabled";
		}
		v.killOnAgent = function(data, event){
			console.log("in kill on Agent",data);
			var ip = data["agentIp"];
			var jobId = 
			$.ajax({
				url: "loader-server/jobs/" + getQueryParams('jobId') + "/agents/" + ip + "/kill",
				contentType: "application/json", 
				dataType:"json",
				type:"PUT",
				async: false,
				success: function(agents){
				},
				error: function(err){
					console.log("Error");
				},
				complete: function(xhr, status){
					if(xhr.status==204) {
          				$("#alertMsg").empty();
  	       				$("#alertMsg").removeClass("alert-error");
        				$("#alertMsg").addClass("alert-success");
        				$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"reload()\">&times;</button>");
						$("#alertMsg").append("<h4>Success!!</h4> Job Killed Successfully On Agent " + ip + " !!");
						$("#alertMsg").css("display", "block");
        			} else {
          				$("#alertMsg").empty();
  	        			$("#alertMsg").removeClass("alert-success");
        				$("#alertMsg").addClass("alert-error");
        				$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"reload()\">&times;</button>");
						$("#alertMsg").append("<h4>Error!!</h4> Job Kill Failed On Agent " + ip + " !!");
						$("#alertMsg").css("display", "block");
        			}
				}
			});
		}
		agentList.push(ko.observable(v));
		window["jobAgents"].push(k);
	});
	console.log("agents used", window["jobAgents"]);
	var status = "Complete",
		labelClass = "label-info",
		disableClass = "btn disabled",
		logBtnCls = "btn",
		grfBtnClass = "btn",
		runBtnClass="btn";
		delBtnClass = "btn"
	switch(jobDetails["jobStatus"]){
		case 'RUNNING':
			labelClass = "label-success",
			status = "Running",
			disableClass = "btn";
			logBtnCls = "btn";
			grfBtnClass="btn";
			runBtnClass="btn disabled";
			delBtnClass="btn disabled";
			break;
		case 'FAILED_TO_START':
			labelClass = "label-important",
			status = "Failed",
			disableClass = "btn disabled";
			logBtnCls = "btn disabled";
			grfBtnClass="btn disabled";
			runBtnClass="btn";
			delBtnClass="btn";
			break;
		case 'QUEUED':
			labelClass = "label-warning",
			status = "Queued",
			disableClass = "btn disabled";
			logBtnCls = "btn disabled";
			grfBtnClass="btn disabled";
			runBtnClass="btn disabled";
			delBtnClass="btn disabled";
			break;
	}
	var self = this;
	self.runName = jobDetails["runName"];
	self.jobId = jobDetails["jobId"] + "  (  <span class=\"label " + labelClass + "\">" + status+ "</span>  )";
	self.startTime = jobDetails["startTime"]==null?"Yet to start":new Date(jobDetails["startTime"]).toLocaleString();
	self.endTime = jobDetails["endTime"]==null?"Yet to Finish":new Date(jobDetails["endTime"]).toLocaleString();
	self.jobStatus = jobDetails["jobStatus"];
	self.stopBtnClass = disableClass;
	self.logsBtnClass = logBtnCls;
	self.deleteBtnClass = delBtnClass;
	self.graphsBtnClass = grfBtnClass;
	self.agents = ko.observableArray(agentList);
	self.reRunBtnClass = runBtnClass;
	self.remarks = ko.observable(jobDetails["remarks"]);
	self.runUrl = "/updaterun.html?&runName=" + jobDetails["runName"];
	console.log("self", self);
}

function stopJob(){
	var jobId = getQueryParams("jobId");
	$.ajax({
      url: "loader-server/jobs/" + jobId + "/kill",
      contentType: "application/json", 
      dataType:"json",
      type:"PUT",
      complete: function(xhr, status){
        if(xhr.status==204) {
          	$("#alertMsg").empty();
  	       	$("#alertMsg").removeClass("alert-error");
        	$("#alertMsg").addClass("alert-success");
        	$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"reload()\">&times;</button>");
			$("#alertMsg").append("<h4>Success!!</h4> Job Killed Successfully!!");
			$("#alertMsg").css("display", "block");
        } else {
          	$("#alertMsg").empty();
  	        $("#alertMsg").removeClass("alert-success");
        	$("#alertMsg").addClass("alert-error");
        	$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"reload()\">&times;</button>");
			$("#alertMsg").append("<h4>Error!!</h4> Job Kill Failed!!");
			$("#alertMsg").css("display", "block");
        }
      }
  	});
}

function reRun(){
	executeRun($("#runName").text());
}

function deleteJob(){
	var jobId = getQueryParams("jobId");
	$.ajax({
		url: "loader-server/jobs/" + jobId,
      	contentType: "application/json", 
      	dataType:"json",
      	type:"DELETE",
      	complete: function(xhr, status){
        	if(xhr.status==204) {
          		window.location.href="/jobsearch.html";
        	} else {
          		$("#alertMsg").empty();
  	        	$("#alertMsg").removeClass("alert-success");
        		$("#alertMsg").addClass("alert-error");
        		$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"reload()\">&times;</button>");
				$("#alertMsg").append("<h4>Error!!</h4> Job Deletion Failed!!");
				$("#alertMsg").css("display", "block");
        	}
        }
  	});	
}
