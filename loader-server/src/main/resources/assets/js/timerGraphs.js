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
				//window.timersToPlot = {};
				window.timerUrls = new Array();
				for( var i=0; i<groups.length; i++){
					var groupJson = groups[i];
					var insertHtml = "<h3 class=\"collapsible\" id=\"section" + (i + 4) + "\">" + groupJson["groupName"] +  
					"<span></span></h3>" + 
					"<div id=\"section" + (i + 4) + "Container\" class=\"container\" style:\"position:relative\">";
					var timers = groupJson["timers"];
					window.timerLengths[i] = timers.length;
					for( var j=0; j<timers.length; j++){
						var timerName = timers[j]["name"];
						window.timerUrls.push("/loader-server/jobs/" + jobId + "/jobStats/groups/" + groupJson["groupName"] + "/timers/" + timerName + "/agents/combined") ;
						console.log("timer:" + timerName);
						var chartId = "chart" + i +j;
						console.log("chartId:" + chartId);
						insertHtml = insertHtml + "<div class=\"content\" style:\"position:relative\"><table width=\"100%\">" + "<tr><td colspan=\"2\">" + timerName + "</td></tr><tr><td><div id=\"" + chartId + "1\" style=\"width:50%;float:left;position:relative\"><svg style=\"height: 350px;min-height:350px\"></svg></div><div id=\"" + chartId + "2\" style=\"width:50%;float:left;\"><svg style=\"height: 350px; min-height:350px;\"></svg></div></td></tr></table></div>";
						//insertHtml = insertHtml + "<div class=\"content\"><p>" + timerName + "</p>";

					}
					insertHtml = insertHtml + "</div>";
					
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
				$('.collapsible').click(function(){
					var id = $(this).attr("id");
					console.log(id);
					$("#"+id+"Container").slideToggle();
				});
				console.log("timersToPlot:" +  window.timerUrls);
				window.stats = new Array();
				var cnt = 0;
				for (var k=0; k<window.groupCount; k++){
					window.stats[k] = new Array();
					for(var p=0;p<window.timerLengths[k];p++){
						returnTimerGraphs(window.timerUrls[cnt++], k,p);
					}
				}
				console.log("complete done");
			}
		});
}