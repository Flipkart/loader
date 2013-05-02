function getAgents(){
	var getAgentsUrl = "/loader-server/agents";
	$(document).ready(function(e){
		$.ajax({
			url: getAgentsUrl,
			contentType: "application/json", 
			dataType:"json",
			type:"GET",
			success: function(agents){
				console.log(agents);
				var insertHtml="<tr>"
				$.each(agents, function(k,v){
					console.log(k);
					var imgSrc="images/green.png";
					switch(v["status"]){
						case "RUNNING" :
							imgSrc="images/green.png";
							break;
						case "NOT_REACHABLE" :
							imgSrc="images/cross.png";
							break;
						case "BUSY":
							imgSrc="images/red.png";
							break;
						case "DISABLED":
							imgSrc="images/disabled.png";
			
					}
					insertHtml = insertHtml + "<td><input type=\"checkbox\" id=\"" + v["ip"] + "\" name=\"" + v["ip"] + "\" /><label for=\"\">" + v["ip"] + "</label></td>";
					insertHtml = insertHtml + "<td>" + v["attributes"]["env"] + "</td>";
					insertHtml = insertHtml + "<td><b><u>OS:</u></b>" + v["attributes"]["linux"] + "<br><b><u>Architecture:</u></b>" + v["attributes"]["architecture"] + "<br>";
					insertHtml = insertHtml + "<b><u>Memory:</u></b>" + v["attributes"]["memory"] + "<br><b><u>Processors:</u></b>" + v["attributes"]["processors"] + "</td>";
					insertHtml = insertHtml + "<td><img src=\"" + imgSrc + "\" width=\"12px\" height=\"12px\"/>" + v["status"] + "</td>";
				});
				insertHtml = insertHtml + "</tr>";
				$("#agentsList").append(insertHtml);
			},
			error: function(err){
				console.log("Error");
			},
			complete: function(xhr, status){
				console.log("Ajax Complete");
			}
		});

	});
}


function enableAgents(){
	$("input:checkbox").each(function(){
		var $this = $(this);
		if($this.is(":checked")){
			$.ajax({
				url: "/loader-server/agents/" + $this.attr("id") + "/enable",
				contentType: "application/json", 
				dataType:"json",
				type:"PUT",
				success: function(e){
			//$("#enable" + agentIp).attr("disabled", true);
			//$("#disable" + agentIp).removeAttr("disabled");
				},
				error: function(e){
					console.log("Error");
				},
				complete: function(xhr, status){
					console.log("Complete");
					location.reload();
				}
			});
		}
	});
}

function disableAgents(){
	$("input:checkbox").each(function(){
		var $this = $(this);
		if($this.is(":checked")){
			$.ajax({
				url: "/loader-server/agents/" + $this.attr("id") + "/disable",
				contentType: "application/json", 
				dataType:"json",
				type:"PUT",
				success: function(e){
				//$("#disable" + agentIp).attr("disabled", true);
				//$("#enable" + agentIp).removeAttr("disabled");
				},
				error: function(e){
					console.log("Error");
				},
				complete: function(xhr, status){
					location.reload();
					console.log("Complete");
				}
			});
		}
	});
}
