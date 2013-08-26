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
        $.ajax({url: jobUrl,
			contentType: "application/json", 
			dataType:"json",
			type:"GET",
			success: function(data){
				console.log("creating the view");
				ko.applyBindings(new jobDetailViewModel(data));
			},
			error: function(e){
				console.log("Error");
				},
			complete: function(xhr, status){
				console.log(status);
				}
		});
}

function jobDetailViewModel(jobDetails){
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
		agentList.push(ko.observable(v));
		window["jobAgents"].push(k);
	});
	console.log("agents used", window["jobAgents"]);
	var status = "Complete",
		labelClass = "label-info",
		disableClass = "btn disabled";
	switch(jobDetails["jobStatus"]){
		case 'RUNNING':
			labelClass = "label-success",
			status = "Running",
			disableClass = "btn";
			break;
		case 'FAILED_TO_START':
			labelClass = "label-important",
			status = "Failed",
			disableClass = "btn disabled";
			break;
		case 'QUEUED':
			labelClass = "label-warning",
			status = "Queued",
			disableClass = "btn disabled";
			break;
	}
	var self = this;
	self.runName = jobDetails["runName"];
	self.jobId = jobDetails["jobId"] + "  (  <span class=\"label " + labelClass + "\">" + status+ "</span>  )";
	self.startTime = jobDetails["startTime"]==null?"Yet to start":new Date(jobDetails["startTime"]).toLocaleString();
	self.endTime = jobDetails["endTime"]==null?"Yet to Finish":new Date(jobDetails["endTime"]).toLocaleString();
	self.jobStatus = jobDetails["jobStatus"];
	self.stopBtnClass = disableClass;
	self.agents = ko.observableArray(agentList);
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

