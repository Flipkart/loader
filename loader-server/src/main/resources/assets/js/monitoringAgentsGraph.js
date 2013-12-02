/*
*   graphPlotScheme will return me {"cpu":{ "title":"CPU", "charts":["total","user","app"]}}
*/

function getGraphPlotScheme(){
	$.ajax({
		url: "/loader-server/admin/config/report/job",
		type: "GET",
		contentType: "application/json", 
		dataType:"json",
		async: false,
		success: function(scheme){
			window["graphSceme"] = scheme;
			//console.log("success:", scheme);
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
		async: false,
		success: function(monitorResources){
			window["metricsBeingMonitored"] = monitorResources;
			//console.log("monitoringResources:",monitorResources);
		},
		error: function(err){

		},
		complete: function(xhr, status){
			console.log("Got all the resources being monitored");
		}
	});
}

function getGraphsData(jobId){
	getGraphPlotScheme();
	getMonitoringStats(jobId);
	window["monitoringStats"] = {};
	var metricsToBePlot = window["metricsBeingMonitored"];
	//console.log("metricsToBePlot:", metricsToBePlot);
	$.each(metricsToBePlot, function(index, metric){
		var agentName = metric["agent"];
		var resources = metric["resources"];
		window["monitoringStats"][agentName]={};
		//console.log("agentName:", agentName);
		//console.log("resources", resources);
		$.each(resources, function(index, resource){
			window["monitoringStats"][agentName][resource] = {};
			$.ajax({
				url: "/loader-server/jobs/" + jobId + "/monitoringStats/agents/" + agentName + "/resources/" + resource,
				type: "GET",
				contentType: "text/plain",
				async: false,
				success: function(resourceData){
					//console.log("resourceData: ", resourceData);
					var type = getResourceType(resource);
					var attributesToPlot = new Array();
					$.each(window["graphSceme"]["chartResources"][type]["charts"], function(index, chart){
						//console.log("chart:", chart["keysToPlot"]);
						attributesToPlot=attributesToPlot.concat(chart["keysToPlot"]);
						//console.log("attributesToPlot :", attributesToPlot);
					});
					//console.log("type :", type);
					//console.log("attributesToPlot :", attributesToPlot);
					var resourceDataLines = resourceData.split('\n');
					//console.log("resourceDataLines:", resourceDataLines);
					$.each(resourceDataLines, function(lineIndex, resourceDataLine){
						//console.log("resourceDataLine:", resourceDataLine);
						if (resourceDataLine!==""){ 
							$.each(attributesToPlot, function(metricIndex, attr){
								if(!window["monitoringStats"][agentName][resource][attr]) window["monitoringStats"][agentName][resource][attr]=new Array();
								try {
									var resourceDataJson = $.parseJSON(resourceDataLine);
									window["monitoringStats"][agentName][resource][attr].push({x: new Date(resourceDataJson[0]["time"]), y: resourceDataJson[0]["metrics"][attr]});
								} catch (err){
									console.log("Err in parsing", resourceDataLine);
								} 
							});
						}
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
	//console.log("monitoringStats:", window["monitoringStats"]);
}

function getResourceType(resource){
	if(resource.indexOf("jmx") !== -1) return "jmx";
	//if(resource.indexOf("cpu") !== -1) return "cpu.total";
	if(resource.indexOf("memory") !== -1) return "memory";
	if(resource.indexOf("sockets") !== -1) return "sockets";
	if(resource.indexOf("diskspace") !== -1) return "diskspace.root";
	if(resource.indexOf("mysql") !== -1) return "mysql";
	if(resource.indexOf('agentHealth') !== -1) return "agentHealth";
	if(resource.indexOf("redis") !== -1) return "redis";
	return resource;

}

function addGraphHolders(){
	var agentResources = window["metricsBeingMonitored"];
	$.each(agentResources, function(agentResIndex, agentResource){
		var agentName = agentResource["agent"];
		var resources = agentResource["resources"];
		var h3Id = ("monitor_" + agentName).replace(/\./g,"_");
		var insertHtml = "<h3 id=\"" + h3Id + "\" class=\"collapsible\">" + agentName + "</h3>" ;
		insertHtml = insertHtml + "<div id=\"" + h3Id + "Container\" class=\"container\" style=\"position:relative\">";
		insertHtml = insertHtml + "<div class=\"content\" style=\"position:relative\">";
		insertHtml = insertHtml + "<table width=\"100%\"><thead><tr><td colspan=\"2\">Agent:  " + agentName + "</td></tr></thead><tbody>";
		$.each(resources, function(resIndex, resource){
			insertHtml = insertHtml + "<tr><td colspan=\"2\">Resource:  " + resource + "</td></tr>";
			var type = getResourceType(resource);
			var totalGraphs = window["graphSceme"]["chartResources"][type]["charts"].length;
			for(var k=0; k<totalGraphs;){
				var divId = "agent_" + agentName + "_" + resource + "_chart" + k;
				divId = divId.replace(/\./g,"_");
				var style="width:50%;float:left;";
				if (k==0) style = "width:50%;float:left;position:relative"
				insertHtml = insertHtml + "<tr><td><div id=\"" + divId + "\" class=\"chart\" style=\"" + style + "\"><svg style=\"height: 350px;min-height:350px\"></svg></div>";
				k++;
				divId = "agent_" + agentName + "_" + resource + "_chart" + k;
				divId = divId.replace(/\./g,"_");
				insertHtml = insertHtml + "<div id=\"" + divId + "\" class=\"chart\" style=\"" + style + "\"><svg style=\"height: 350px;min-height:350px\"></svg></div></td></tr>";
				k++;
			}
		});
		insertHtml = insertHtml + "</tbody></table></div></div>";
		//console.log("insertHtml:", insertHtml);
		$("#monitoringGraphs").append(insertHtml);
	});
}

function plotGraphs(jobId){
	getGraphsData(jobId);
	addGraphHolders();
	var agentResources = window["metricsBeingMonitored"];
	$.each(agentResources, function(agentResIndex, agentResource){
		var agentName = agentResource["agent"];
		var resources = agentResource["resources"];
		$.each(resources, function(resIndex, resource){
			var type = getResourceType(resource);
			var charts = window["graphSceme"]["chartResources"][type]["charts"];
			$.each(charts, function(chartIndex, chart){
				formatTime = d3.time.format("%H:%M"),
				formatMinutes = function(d) { return formatTime(new Date(d)); };
				var chart1;
				nv.addGraph(function() {
  					chart1 = nv.models.lineChart();
  					//chart1.x(function(d,i) {  return i });
  					//chart1.y(function(d,i){ return i/1000});
					chart1.xAxis
  						//.ticks(d3.time.seconds, 60)
    					.tickFormat(function(d) { return d3.time.format('%H:%M')(new Date(d)); });

  					chart1.yAxis
      					.axisLabel('Time (ms)')
      					.tickFormat(d3.format('.1s'));
      				var chart1PlaceHolder = "#agent_" + agentName + "_" + resource + "_chart" + chartIndex;
      				chart1PlaceHolder = chart1PlaceHolder.replace(/\./g,"_");
      				//console.log("chart1PlaceHolder", chart1PlaceHolder);
      				d3.select(chart1PlaceHolder + " svg")
      					.datum(getChartData(agentName, resource, chart))
    					.transition().duration(500)
      					.call(chart1);
      				nv.utils.windowResize(chart1.update);
  					chart1.dispatch.on('stateChange', function(e) { nv.log('New State:', JSON.stringify(e)); });
  					return chart1;
  				});
  			});
		});
	});
}


function getChartData(agentName, resource, chart){
	var returnData = new Array();
	$.each(chart["keysToPlot"], function(attrIndex, attr){
		returnData.push({values: window["monitoringStats"][agentName][resource][attr], key: attr, color: pickColor(attrIndex)});
	});
	//console.log("returnData", returnData);
	return returnData;
}

function pickColor(index){
	var colors = ["#ff7f0e","#a02c2c","#B40404","#0B610B", "#0B0B61", "#FE9A2E", "#0E0D0D", "#2ca02c", "#DF01D7"];
	return colors[index%9];
}

