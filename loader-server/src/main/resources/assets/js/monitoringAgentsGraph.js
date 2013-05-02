/*
*   graphPlotScheme will return me {"cpu":{ "title":"CPU", "charts":["total","user","app"]}}
*/

function getGraphPlotScheme(){
	$.ajax({
		url: "/loader-server/admin/config/report/job",
		type: "GET",
		contentType: "application/json", 
		dataType:"json",
		success: function(scheme){
			window["graphSceme"] = scheme;
			console.log("success:", scheme);
		},
		error: function(err){

		},
		complete: function(xhr, status){
			console.log("Got the plotting scheme");
		}
	});
}

function getMonitoringStats(jobId){
	$.ajax({
		url: "/loader-server/jobs/" + jobId + "/monitoringStats",
		type: "GET",
		contentType: "application/json", 
		dataType:"json",
		success: function(monitorResources){
			window["metricsBeingMonitored"] = monitorResources;
		},
		error: function(err){

		},
		complete: function(xhr, status){
			console.log("Got all the resources being monitored");
		}
	});
}

function plotTheGraphs(jobId){
	getGraphPlotScheme();
	getMonitoringStats(jobId);
	var metricsToBePlot = window["metricsBeingMonitored"];
	$.each(metricsToBePlot, function(index, metric){
		var agentName = metric["agent"];
		var resources = metric["resources"];
		$.each(resources, function(index, resource){
			$.ajax({
				url: "/loader-server/jobs/" + jobId + "/monitoringStats/agents/" + agentName + "/resources/" + resource;
				type: "GET",
				contentType: "text/plain",
				dataType:"json",
				success: function(resourceData){
					var type = getResourceType(resource);
					var attributesToPlot = new Array();
					$.each(window["graphSceme"]["type"]["charts"], function(index, chart){
						attributesToPlot.concat(chart["keysToPlot"]);
					});
					var resourceDataLines = resourceData.split("\n");
					$.each(resourceDataLines, function(lineIndex, resourceDataLine){
						$.each(attributesToPlot, function(metricIndex, attr){
							if(!window["monitoringStats"][agentName][resource][attr]) window["monitoringStats"][agentName][resource][attr]=new Array();
							var resourceDataJson = $.parseJson(resourceDataLine);
							window["monitoringStats"][agentName][resource][attr].push(resourceDataJson["metrics"][attr]);
						});
					});
				},
				error: function(err){
					console.log(err);
				},
				complete: function(xhr, status){
					console.log("Done fetching data");
				}
			});
		});
	});
	console.log("monitoringStats:", window["monitoringStats"]);
}

function getResourceType(resource){
	if(resource.indexOf("jmx") !== -1) return "jmx";
	if(resource.indexOf("cpu") !== -1) return "cpu";
	if(resource.indexOf("memory") !== -1) return "memory";
	if(resource.indexOf("sockets") !== -1) return "sockets";

}









