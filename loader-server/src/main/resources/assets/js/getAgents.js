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
				var insertHtml="<ul>"
				$.each(agents, function(k,v){
					console.log(k);
					insertHtml = insertHtml + "<li><a id=\"" + v["ip"] + "\" href='#'>" + v["ip"] + "</a></li>";
				});
				insertHtml = insertHtml + "</ul>";
				$("#agentList").append(insertHtml);
				$("#agentList a").click(function(e){
					e.preventDefault();
					console.log(e.target.id);
					getAgentDetails(e.target.id, agents);
				});
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

function getAgentDetails(id, agentsData){
	console.log(agentsData);
	var agentDetails = agentsData[id];
	var systemDetails = agentDetails["attributes"];
	var imgSrc = "images/green.png";
	var canDisable = true;
	var canEnable = false;
	switch(agentDetails["status"]){
		case "RUNNING" :
			imgSrc="images/green.png";
			canDisable = true;
			canEnable=false;
			break;
		case "NOT_REACHABLE" :
			imgSrc="images/cross.png";
			canDisable=false;
			canEnable=false;
			break;
		case "BUSY":
			imgSrc="images/red.png";
			canEnable=false;
			canDisable=false;
			break;
		case "DISABLED":
			imgSrc="images/disabled.png";
			canEnable = true;
			canDisable=false;
	}
	var insertHtml = "<table width=\"100%\"><tr><td width=\"50%\"><label><strong><u>IP Address</u></strong></label></td>" +
						"<td width=\"50%\"><label><strong><u>Status</u></strong></label></td></tr>" + 
						"<tr><td><label>" + agentDetails["ip"] + "</label></td><td><img src=\"" + imgSrc + "\" width=\"12px\" height=\"12px\"/>&nbsp&nbsp" +
						"<label>" + agentDetails["status"] + "</label></td></tr><tr><td colspan=\"2\"><label><strong><u>" +
						"System Attributes</u></strong></label></td></tr><tr><td><label><strong><u>Network Env:</u></strong>&nbsp;&nbsp;" +
						systemDetails["env"] + "</label></td><td><label><strong><u>OS:</u></strong>&nbsp;&nbsp;" + systemDetails["linux"] + 
						"</label></td></tr><tr><td><label><strong><u>Architecture:</u></strong>&nbsp;&nbsp;" + systemDetails["architecture"] +
						"</label></td><td><label><strong><u>Processors:</u></strong>&nbsp&nbsp" + systemDetails["processors"] + "</label></td></tr>" +
						"<tr><td><label><strong><u>Memory:</u></strong>&nbsp&nbsp" + systemDetails["memory"] + "</label></td><td><label><strong><u>" +
						"Swap:</u></strong>&nbsp&nbsp" + systemDetails["swap"] + "</label></td></tr>";
	insertHtml = insertHtml + "<tr><td colspan=\"2\"><label><strong><u>Running Jobs:</u></strong></label><ul>";
	var runningJobs = agentDetails["runningJobs"];
	$.each(runningJobs, function(index, data){
		insertHtml = insertHtml + "<li><a href=\"/job_details.html?&jobid=" + runningJobs[index] + "\">" + runningJobs[index] + "</a></li>";
	});
	insertHtml = insertHtml + "</ul></td></tr><tr><tr><td colspan=\"2\"><button id=\"enable\" name=\"" + agentDetails["ip"] + "\" style=\"background:#505050; cursor:pointer\">" + 
					"Enable</button><button id=\"disable\" name=\"" + agentDetails["ip"] + "\" style=\"background:#505050; cursor:pointer\">Disable</button></td></tr></table>";
	$("#details").empty();
	$("#details").append(insertHtml);
	$("#details button").click(function(e){
					e.preventDefault();
					console.log(e.target.id, e.target.name);
					if(e.target.id=="disable") disableAgent(e.target.name);
					else enableAgent(e.target.name);
				});
	checkButtonStatus(canEnable, canDisable);
}

function checkButtonStatus(canEnable, canDisable){
	if(canEnable) $("#enable").removeAttr("disabled");
	else $("#enable").attr("disabled", true);
	if(canDisable) $("#disable").removeAttr("disabled");
	else $("#disable").attr("disabled", true);
}

function enableAgent(agentIp){
	$.ajax({
		url: "/loader-server/agents/" + agentIp + "/enable",
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

function disableAgent(agentIp){
	$.ajax({
		url: "/loader-server/agents/" + agentIp + "/disable",
		contentType: "application/json", 
		dataType:"json",
		type:"PUT",
		success: function(e){
			//$("#disable" + agentIp).attr("disabled", true);
			//$("#enable" + agentIp).removeAttr("disabled");
			location.reload();
		},
		error: function(e){
			console.log("Error");
		},
		complete: function(xhr, status){
			console.log("Complete");
		}
	});
}













