function getAgents(){
	var getAgentsUrl = "/loader-server/agents";
	$(document).ready(function(e){
		$.ajax({
			url: getAgentsUrl,
			contentType: "application/json", 
			dataType:"json",
			type:"GET",
			async: false,
			success: function(agents){
				ko.applyBindings(new agentsViewModel(agents));
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

function agentRow(agentDets){
	var self = this;
	var memory = Math.floor(agentDets["attributes"]["memory"]/1000000000);
	var rowClass="info";
	switch(agentDets["status"]){
		case "FREE" :
			rowClass="info";
			break;
		case "NOT_REACHABLE" :
			rowClass="error";
			break;
		case "BUSY":
			rowClass="success";
			break;
		case "DISABLED":
			rowClass="warning";
		}
	self.agent = agentDets["ip"];
	self.sysDetails = agentDets["attributes"]["linux"] + ", " + agentDets["attributes"]["architecture"]+ ", " + agentDets["attributes"]["processors"] + " Cores, " + memory+ " Gb";
	self.agentStatus = agentDets["status"];
	self.runningJobs = agentDets["runningJobs"].join();
	self.rwClass = rowClass;
}

function agentsViewModel(agents){
	var self = this;
	var agentList = []
	$.each(agents, function(k,v){
		console.log("creating row");
		agentList.push(new agentRow(v));
	});
	self.agentRows = ko.observableArray(agentList);
	console.log("agents", self.agentRows);
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
