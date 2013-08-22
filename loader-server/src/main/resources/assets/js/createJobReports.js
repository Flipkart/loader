var graphReports = function(){
	var self = this;
	self.createGroupArray = function(){
		var grpList = [];
		$.each(window["groups"], function(grpIndex,group){
			console.log("group", group);
			if(group["timers"]!=null && typeof(group["timers"])!= 'undefined'){
				console.log("group[\"timers\"]", group["timers"]);
				var timers = [];
				$.each(group["timers"], function(timerIndex, timer){
					timers.push({"timerName":timer["name"], "chartName1":"chart-"+grpIndex+"-"+timerIndex + "-1",
						"chartName2":"chart-"+grpIndex+"-"+timerIndex + "-2", "timerDivId":"timer-" + grpIndex + "-" + timerIndex});
				});
				grpList.push({"groupName":group["groupName"], "timers":timers, "groupDivId":"group-" + grpIndex});
			}
		});
		return grpList;
	}
	self.createMonAgentsArray = function(){
		return [];
	}
	self.jobGroups = ko.observableArray(self.createGroupArray());
	self.monAgents = ko.observableArray(self.createMonAgentsArray());
}

function getJobStats(){
	var jobId = getQueryParams("jobId");
	$.ajax({
		url: "/loader-server/jobs/" + jobId + "/jobStats",
		type: "GET",
		contentType: "application/json",
		async: false,
		success: function(jobStats){
							console.log("jobStats",jobStats);
			window["groups"] = jobStats;
			window["stats"] = new Array();
			window["groupsURLS"] = [];
			window["graphsState"] = [];
			$.each(jobStats, function(index, group){
							console.log("group",group);
				var groupUrls = {};
				var groupGraphsState = {};
				var timers = group["timers"];
				if( typeof(timers) != 'undefined' && timers != null){
					groupUrls["timerUrls"]=[];
					groupGraphsState["timerGraphsState"]=[];
					groupUrls["counterUrls"]=[];
					$.each(timers, function(timerIndex, timer){
													console.log("timer", timer);
						groupGraphsState["timerGraphsState"].push(false);
						groupUrls["timerUrls"].push("/loader-server/jobs/" + jobId + "/jobStats/groups/" + group["groupName"] + "/timers/" + timer["name"] + "/agents/combined");
						groupUrls["counterUrls"].push({ "count":"/loader-server/jobs/" + jobId + "/jobStats/groups/" + group["groupName"] + "/counters/" + timer["name"] + "_count/agents/combined?last=true",
														"error":"/loader-server/jobs/" + jobId + "/jobStats/groups/" + group["groupName"] + "/counters/" + timer["name"] + "_error/agents/combined?last=true",
														"skip":"/loader-server/jobs/" + jobId + "/jobStats/groups/" + group["groupName"] + "/counters/" + timer["name"] + "_skip/agents/combined?last=true",
														"failure":"/loader-server/jobs/" + jobId + "/jobStats/groups/" + group["groupName"] + "/counters/" + timer["name"] + "_failure/agents/combined?last=true"});
					});
					window["groupsURLS"].push(groupUrls);
					window["graphsState"].push(groupGraphsState);
					window["stats"][index]=new Array();
				}
			});
			console.log("window[\"groupsURLS\"]", window["groupsURLS"]);
		},
		error: function(){

		},
		complete: function(){

		}
	});
}

function getMonitoringStats(){
	var jobId = getQueryParams("jobId");
	$.ajax({
		url: "/loader-server/jobs/" + jobId + "/monitoringStats",
		type: "GET",
		contentType: "application/json",
		async: false,
		success: function(monResources){
			window["monitorResources"] = monResources;
		},
		error: function(){

		},
		complete: function(){

		}
	});
}

function getGraphPlotScheme(){
  $.ajax({
    url: "/loader-server/admin/config/report/job",
    type: "GET",
    contentType: "application/json",
    dataType:"json",
    async: false,
    success: function(scheme){
      window["graphSceme"] = scheme;
    },
    error: function(err){

    },
    complete: function(xhr, status){
      console.log("Got the plotting scheme");
    }
  });
}

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
    return undefined;
}

function createGroupTree(){
	$("#timerTree").jstree({
		"plugins":["themes", "json_data", "checkbox", "ui"],
		"json_data":{
			"data":[{
				"attr":{"id" : "node_graphs", "rel":"graphs"},
				"data" : "Graphs", 
				"metadata" : { "name" : "Graphs", "nodeType" : "graphs"},    
				"children" : getGraphsChildren()
			}],
		"checkbox":{
			"override_ui":true,
		},
		"progressive_render" : true,
		}
	}).bind("check_node.jstree", function(event, data){
		console.log("You checked ", event, data);
		console.log("metadata", data.rslt.obj.data());
		switch(data.rslt.obj.data("nodeType")){
			case "graphs":
				plotGraphs();
				break;
			case "group":
				plotGroupGraphs(data.rslt.obj.data("groupIndex"));
				break;
			case "timer":
				plotTimerGraph(data.rslt.obj.data("groupIndex"), data.rslt.obj.data("timerIndex"));
				break;
		}

	}).bind("uncheck_node.jstree", function(event, data){
		console.log("You unchecked ", event, data);
		switch(data.rslt.obj.data("nodeType")){
			case "graphs":
				hideGraphs();
				break;
			case "group":
				hideGroupGraphs(data.rslt.obj.data("groupIndex"));
				break;
			case "timer":
				hideTimerGraph(data.rslt.obj.data("groupIndex"), data.rslt.obj.data("timerIndex"));
				break;
		}
	});
	$("#timerTree").bind("loaded.jstree", function (event, data) {
        $("#timerTree").jstree("open_all");
        $.jstree._reference("#timerTree").check_node("#node_"+window["groups"][0]["groupName"]);  
    });
    $("#timerTree").bind("refresh.jstree", function (event, data) {
        $("#timerTree").jstree("open_all");
        $.jstree._reference('#timerTree').check_node("#node_"+window["groups"][0]["groupName"]);  
    });
}

function getGraphsChildren(){
	if(window["groups"]==undefined) return undefined;
	var children = []
	$.each(window["groups"], function(index, group){
		children.push({"attr":{"id":"node_" + group["groupName"],"rel":"group"}, "metadata":{ "nodeType":"group", "groupIndex":index },"data": group["groupName"], "children": getTimersChildren(group["timers"], index)});
	});
	return children;
}

function getTimersChildren(timers, groupIndex){
	if(timers==null || timers.length == 0) return undefined;
	var children = []
	$.each(timers, function(index, timer){
		children.push({"attr":{"id":"node_" + timer["name"], "rel":"timer"},"metadata":{"nodeType":"timer", "groupIndex":groupIndex, "timerIndex": index},"data": timer["name"]});
	})
	return children;
}

function plotTimerGraph(groupIndex, timerIndex){
	$("#timerGraphs").show();
	$("#group-" + groupIndex).show();
	if(!window["graphsState"][groupIndex][timerIndex]){
		returnTimerGraphs(window["groupsURLS"][groupIndex]["timerUrls"][timerIndex], groupIndex, timerIndex, 0, 0);
		window["graphsState"][groupIndex][timerIndex]=true;
	} 
	var divId = "#timer-"+groupIndex + "-" + timerIndex;
	$(divId).show();
}

function plotGroupGraphs(groupIndex){
	$("#timerGraphs").show();
	$("#group-" + groupIndex).show();
	$.each(window["groups"][groupIndex]["timers"], function(timerIndex, timer){
		plotTimerGraph(groupIndex, timerIndex);
	});
}

function plotGraphs() {
	$("#timerGraphs").show();
	$.each(window["groups"], function(groupIndex, group){
		plotGroupGraphs(groupIndex);
	})
}

function hideTimerGraph(groupIndex, timerIndex){
	var divId = "#timer-"+groupIndex + "-" + timerIndex;
	$(divId).hide();
}

function hideGroupGraphs(groupIndex){
	if(window["groups"][groupIndex]["timers"]!=null){
		$.each(window["groups"][groupIndex]["timers"], function(timerIndex, timer){
			hideTimerGraph(groupIndex, timerIndex);
		});
	}
	var divId = "#group-" + groupIndex;
	$(divId).hide();
}

function hideGraphs(){
	$.each(window["groups"], function(groupIndex, group){
		hideGroupGraphs(groupIndex);
	})
	$("#timerGraphs").hide();
}

function createMonitoringTree(){
	$("#monitoringTree").jstree({
		"plugins":["themes", "json_data", "checkbox", "ui"],
		"json_data":{
			"data":[{
				"attr":{"id" : "node_graphs", "rel":"monag"},
				"data" : "MonitoringAgents", 
				"metadata" : { "name" : "MonitoringAgents", "nodeType" : "monag"},    
				"children" : getMonitoringChildren()
			}],
		"checkbox":{
			"override_ui":true,


		},
		"progressive_render" : true,
		}
	}).bind("check_node.jstree", function(event, data){
		console.log("You checked ", event, data);
	}).bind("uncheck_node.jstree", function(event, data){
		console.log("You unchecked ", event, data);
	});
}

function getMonitoringChildren(){
	if(window["monitorResources"]==undefined) return undefined;
	var children = [];
	$.each(window["monitorResources"], function(index, monRes){
		children.push({"attr":{"id":"node_"+monRes["agent"], "rel":"agent"}, "data":monRes["agent"], "children": getResourcesChildren(monRes)});
	});
	return children;
}

function getResourcesChildren(monRes){
	if(monRes["resources"]==null) return undefined;
	var children = [];
	$.each(monRes["resources"], function(index, resource){
		children.push({"attr":{"id":"node_"+resource, "rel":"resource"}, "data":resource});
	})
	return children;
}
