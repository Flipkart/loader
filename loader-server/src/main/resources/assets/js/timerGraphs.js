function plotTimerGraphs(){
    var jobId = getQueryParams("jobid");
		$.ajax({
			url: "/loader-server/jobs/" + jobId + "/jobStats",
			type: "GET",
			contentType: "application/json",
			async: false,
			success: function(data){
				var groups = data;
				console.log(groups);
				window.groupCount = groups.length;
				window.timerLengths = new Array();
				window.stats = new Array();
				//window.timersToPlot = {};
				window.timerUrls = new Array();
				for( var i=0; i<groups.length; i++){
					var groupJson = groups[i];
					var insertHtml = "";
					var timers = groupJson["timers"];
					if( typeof(timers) != 'undefined' && timers != null){
						insertHtml = "<h3 class=\"collapsible\" id=\"section" + i + "\">" + groupJson["groupName"] +  
							"<span></span></h3>" + 
							"<div id=\"section" + i + "Container\" class=\"container\" style:\"position:relative\">";
						window.timerLengths[i] = timers.length;
						for( var j=0; j<timers.length; j++){
							var timerName = timers[j]["name"];
							window.timerUrls.push("/loader-server/jobs/" + jobId + "/jobStats/groups/" + groupJson["groupName"] + "/timers/" + timerName + "/agents/combined") ;
							console.log("timer:" + timerName);
							var chartId = "chart" + i +j;
							console.log("chartId:" + chartId);
							insertHtml = insertHtml + "<div class=\"content\" style:\"position:relative\"><table width=\"100%\">" + 
							"<tr><td colspan=\"2\">" + timerName + 
							"</td></tr><tr><td><div id=\"" + chartId + "1\" style=\"width:50%;float:left;position:relative\"><svg style=\"height: 350px;min-height:350px\"></svg><div id=\"slider-"+ i + "-" + j +"-0\" class=\"slider\" style=\"width:90%;margin:0 auto;\"></div>" + 
							"</div><div id=\"" + chartId + "2\" style=\"width:50%;float:left;\"><svg style=\"height: 350px; min-height:350px;\"></svg><div id=\"slider-"+ i + "-" + j +"-1\" class=\"slider\" style=\"width:90%;margin:0 auto;\"></div></div></td></tr></table></div>";
							//insertHtml = insertHtml + "<div class=\"content\"><p>" + timerName + "</p>";

						}
						insertHtml = insertHtml + "</div>";
					}
					$("#collapsibleMenu")
					.append(insertHtml);
				}
				console.log("success done");
			},
			error: function(err){
				console.log("Error");
			},
			complete: function(comp){
				console.log($('.container'));
				$('.container').slideUp();
				console.log("complete done");
			}
		});
}
