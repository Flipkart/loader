;(function() {
		
	window.jsPlumbDemo = { 
	
		init :function() {
						
			// setup some defaults for jsPlumb.	
			jsPlumb.importDefaults({
				Endpoint : ["Dot", {radius:2}],
				HoverPaintStyle : {strokeStyle:"#1e8151", lineWidth:2 },
				ConnectionOverlays : [
					[ "Arrow", { 
						location:1,
						id:"arrow",
	                    length:14,
	                    foldback:0.8
					} ],
	                [ "Label", { label:"FOO", id:"label", cssClass:"aLabel" }]
				]
			});
			
			var windows = $(".w");

            // initialise draggable elements.  
			jsPlumb.draggable(windows);

            // bind a click listener to each connection; the connection is deleted. you could of course
			// just do this: jsPlumb.bind("click", jsPlumb.detach), but I wanted to make it clear what was
			// happening.
			jsPlumb.bind("click", function(c) { 
				jsPlumb.detach(c); 
			});			
				
			// make each ".ep" div a source and give it some parameters to work with.  here we tell it
			// to use a Continuous anchor and the StateMachine connectors, and also we give it the
			// connector's paint style.  note that in this demo the strokeStyle is dynamically generated,
			// which prevents us from just setting a jsPlumb.Defaults.PaintStyle.  but that is what i
			// would recommend you do. Note also here that we use the 'filter' option to tell jsPlumb
			// which parts of the element should actually respond to a drag start.
			jsPlumb.makeSource(windows, {
				filter:".ep",				// only supported by jquery
				anchor:"Continuous",
				connector:[ "StateMachine", { curviness:20 } ],
				connectorStyle:{ strokeStyle:"#5c96bc", lineWidth:2, outlineColor:"transparent", outlineWidth:4 },
				maxConnections:5,
				onMaxConnections:function(info, e) {
					alert("Maximum connections (" + info.maxConnections + ") reached");
				}
			});			



            jsPlumb.bind("connection", function(info) {
				info.connection.getOverlay("label").setLabel(info.connection.id);
            });

			// initialise all '.w' elements as connection targets.
            jsPlumb.makeTarget(windows, {
				dropOptions:{ hoverClass:"dragHover" },
				anchor:"Continuous"				
			});
		}
	};
})();
function createScheduledWorkflowJson(){
	//validateAllFields();
	var workflow = {};
	workflow["name"] = $("#workflowName").val();
	workflow["schedulerType"] = $("#schedulerType").val();
	if(workflow["schedulerType"]=="CRON") workflow["cronExpression"] = $("#cronExp").val();
	if(window.startNow==true) {
		workflow["startNow"] = true;
		workflow["startAt"] = null;
	} else { 
		workflow["startAt"] = $("#startTime").val();
		workflow["startNow"] = false;
	}

	if(window.runForever==true) {
		workflow["runForEver"] = true; 
		workflow["endAt"] = null;
		workflow["repeat"] = 0;
	} else {
		workflow["runForEver"] = false;
		workflow["endAt"] = $("#endTime").val();
		if($("#repeats").val == undefined) workflow["repeats"] = 0;
		else workflow["repeats"] = $("#repeats").val;
	}
	if($("#interval").val()==undefined) workflow["interval"] = 0;
	else workflow["interval"] = $("#interval").val();
	//workflow["workflow"] = $.parseJSON($("#workflow").val());
	workflow["workflow"] = createWorkflow();
	return workflow;
}

function createScheduledWorkflow(){
    var workflow = createScheduledWorkflowJson();
	$.ajax({
		url: "/loader-server/scheduledWorkFlows",
		contentType: "application/json", 
		dataType:"json",
		type:"POST",
		async: false,
		data: JSON.stringify(workflow),
		success: function(msg){
			console.log(msg);
		},
		error: function(err){
			console.log("Error");
		},
		complete: function(xhr, status){
			switch(xhr.status){
				case 204:
      				$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-error");
        		 	$("#alertMsg").addClass("alert-success");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"goToUpdate()\">&times;</button>");
					$("#alertMsg").append("<h4>Success!!</h4> Workflow Created successfully!!");
					$("#alertMsg").css("display", "block");
					setTimeout(function(){goToUpdate();},3000);
					break;
				case 409:
					$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-success");
        		 	$("#alertMsg").addClass("alert-error");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"returnToPage()\">&times;</button>");
					$("#alertMsg").append("<h4>Error!!</h4> Workflow name conflict!!");
					$("#alertMsg").css("display", "block");
					break;
				default :
					$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-success");
        		 	$("#alertMsg").addClass("alert-error");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"returnToPage()\">&times;</button>");
					$("#alertMsg").append("<h4>Error!!</h4> Workflow creation failed!!");
					$("#alertMsg").css("display", "block");		
			}
		}
	});
}

function addBlock(){
	var runName = $("#runName").val();
	var blockId = runName + new Date().getTime();
	var blockName = "Block_" + window.count;
	window.count = window.count + 1;
	var div = "<div class=\"w\" id=\"" + blockId + "\"><div class=\"edit\" style=\"word-wrap:break-word\">" + blockName + "</div><div class=\"ep\"></div><div class=\"cross\"></div></div>";	
	$(".grid").append(div);
	window.numberOfBlocks = window.numberOfBlocks + 1;
	addAllEventHandlersToBlocks();
	addDataAndEventsToBlock(blockId, runName, blockName);
}

function addAllEventHandlersToBlocks(){
	var windows = $(".w");
	jsPlumb.draggable(windows,{
		containment:"parent"
	});
	jsPlumb.bind("click", function(c) { 
		jsPlumb.detach(c); 
	});	

	jsPlumb.makeSource(windows, {
		filter:".ep",				// only supported by jquery
		anchor:"Continuous",
		connector:[ "StateMachine", { curviness:20 } ],
		connectorStyle:{ strokeStyle:"#5c96bc", lineWidth:2, outlineColor:"transparent", outlineWidth:4 },
		maxConnections:50,
		onMaxConnections:function(info, e) {
				alert("Maximum connections (" + info.maxConnections + ") reached");
		}
	});
	jsPlumb.bind("connection", function(info) {
		info.connection.getOverlay("label").setLabel(info.connection.id);
    });
	jsPlumb.makeTarget(windows, {
		dropOptions:{ hoverClass:"dragHover" },
		anchor:"Continuous"				
	});
}

function addDataAndEventsToBlock(blockId, runName, blockName){

	$("#"+blockId + " .edit").data("blockName", blockName);
	$("#" + blockId).data("runName",runName);

	$("#"+blockId + " .cross").click(function(event) {
		console.log($(this));
		jsPlumb.detachAllConnections($(this).parent());
		$(this).parent().remove();
		window.numberOfBlocks = window.numberOfBlocks -1;
		event.stopPropagation();
	});
	$("#"+blockId).click(function(event) {
		console.log($(this), event);
		if($(window.scheduler["selectedBlock"])!=undefined) $(window.scheduler["selectedBlock"]).css('background-color','white');
		$(this).css('background-color',"#2e6f9a");
		window.scheduler["selectedBlock"] = this;
		event.stopPropagation();
	});
	$("#" + blockId + " .edit").editable(function(value,settings){
    	console.log(value);
    	console.log(settings);
    	$(this).data("blockName", value);
    	return(value);
    },{
    	tooltip : "Doubleclick to change blockname",
      	event : "dblclick",
      	style : "inherit"
    });
}

function createWorkflow(){
	var blocks = [];
	var blocksMap = {};
	$(".grid .w").each(function(idx, elem){
		if($(elem).attr("id")!="startBlock"){
			blocksMap[$(elem).attr("id")] = {
				"blockId": $(elem).attr("id"),
				"blockName": $(elem).children(".edit").data("blockName"),
				"runName": $(elem).data("runName"),
				"dependsOn": new Array(),
				"topPosition": parseInt($(elem).css("top"),10),
				"leftPosition": parseInt($(elem).css("left"),10)
			}
		}
	});

	$.each(jsPlumb.getConnections(), function(idx, connection){
		if(connection.sourceId!="startBlock")
			blocksMap[connection.targetId]["dependsOn"].push($("#" + connection.sourceId).children(".edit").data("blockName"));
	});
	console.log("Map created", blocksMap);
	$.each(blocksMap, function(k,v){
		blocks.push(v);
	});
	return blocks;
}

function createWorkflowDiagram(blocks){
	$("#grid").empty();
	$("#grid").append("<div id=\"render\"></div>");
	$("#grid").append("<div class=\"w\" id=\"startBlock\">Start<div class=\"ep\"></div></div>");
	$.each(blocks, function(index, block){
		var div = "<div class=\"w\" id=\"" + 
			block["blockId"] + "\" style=\"top:" + block["topPosition"] + "px;left:" + block["leftPosition"] + "px;\"><div class=\"edit\" style=\"word-wrap:break-word\">" + 
			block["blockName"] + "</div><div class=\"ep\"></div><div class=\"cross\"></div></div>";	
		$("#grid").append(div);
		addDataAndEventsToBlock(block["blockId"],block["runName"],block["blockName"]);
	});
	addAllEventHandlersToBlocks();
	var blockNameIdMap={};
	$.each(blocks, function(index,block){
		blockNameIdMap[block["blockName"]]=block["blockId"];
	})
	$.each(blocks, function(index,block){
		if(block["dependsOn"].length==0){
			console.log("connecting start with :" , block["blockId"]);
			jsPlumb.connect({
				source:'startBlock',
				target:block["blockId"]
			});
		} else {
			$.each(block["dependsOn"], function(index, blockName){
				console.log("connecting :" ,blockNameIdMap[blockName], block["blockId"]);
				jsPlumb.connect({
					source:blockNameIdMap[blockName],
					target:block["blockId"]
				})
			})
		}
	});
}

var runModel = function(){
	var self = this;
	var allBus = getAllBusinessUnits();
	self.getBuList = function(){
		var list = [];
		$.each(allBus, function(k,v){
			list.push(k);
		});
		return list;
	}
	self.bus = ko.observableArray(self.getBuList());
	self.selectedBu = ko.observable(self.bus()[0]);
	self.teams = ko.computed(function(){
		var list =[];
		var selBu = self.selectedBu();
		$.each(allBus[selBu]["teams"], function(k,v){
			list.push(k);
		});
		return list;
	});
	self.selectedTeam = ko.observable(self.teams()[0]);
	self.runs = ko.computed(function(){
		var list =[];
		var selBu = self.selectedBu();
		var selTeam = self.selectedTeam();
		return allBus[selBu]["teams"][selTeam]["runs"];
	})
	//self.selectedRun = ko.observable(self.runs()[0])
}
var schedulerInfoModel = function(){
	var self = this;
	//self.workflowNames = ko.observableArray(getAllWorkflows());
	self.workflowName = ko.observable(getQueryParams("workflowName"));
	self.workflowDetails = ko.computed(function(){
		var workflowName = self.workflowName();
		var details;
		$.ajax({
			url: "/loader-server/scheduledWorkFlows/" + workflowName,
			contentType: "application/json", 
			type:"GET",
			async: false,
			success: function(msg){
				details = msg;
			},
			error: function(err){
				console.log("Error");
			},
			complete: function(xhr, status){
				console.log(xhr,status);
			}	
		});
		return details;
	});
	self.schedulerType = ko.computed(function(){
		return self.workflowDetails()["schedulerType"];
	});
	self.cronExpression = ko.computed(function(){
		return self.workflowDetails()["cronExpression"];
	});
	self.startNow = ko.computed(function(){
		return self.workflowDetails()["startNow"];
	});
	self.runForEver = ko.computed(function(){
		return self.workflowDetails()["runForEver"];
	});
	self.startAt = ko.computed(function(){
		return self.workflowDetails()["startAt"];
	});
	self.endAt = ko.computed(function(){
		return self.workflowDetails()["endAt"];
	});
	self.repeats = ko.computed(function(){
		return self.workflowDetails()["repeats"];
	});
	self.interval = ko.computed(function(){
		return self.workflowDetails()["interval"];
	});
	self.cronExpressionStyle = ko.computed(function(){
		if (self.workflowDetails()["schedulerType"] == "SIMPLE") return 
	})
	self.disableCronExp = ko.computed(function(){
		var value = self.workflowDetails()["schedulerType"];
		if(value=="SIMPLE")
			return true;	
		return false;
	})
	self.startNowClass = ko.computed(function(){
		if(self.workflowDetails()["startNow"]==true) return "active";
		return "";
	})
	self.startAtClass = ko.computed(function(){
		if(self.workflowDetails()["startNow"]==true) return "";
		return "active";
	})
	self.runForEverClass = ko.computed(function(){
		if(self.workflowDetails()["runForEver"]==true) return "active";
		return "";
	})
	self.endAtClass = ko.computed(function(){
		if(self.workflowDetails()["runForEver"]==true) return "";
		return "active";
	})
	
	self.onChangeSchedulerType = function(){
		var value = $("#schedulerType").val();
		if(value=="SIMPLE"){
			//$("#cronExp").fadeTo(0, 0.7);
			$("#cronExp").attr("disabled","disabled");
		} else {
			$("#cronExp").removeAttr("disabled");
			//$("#cronExp").fadeTo(0, 1);
		}
	}
	self.onChangeWorkflowName = function(){
		createWorkflowDiagram(self.workflowDetails()["workflow"]);
	}
}

function updateScheduledWorkflow(){
	var workflow = createScheduledWorkflowJson();
	$.ajax({
		url: "/loader-server/scheduledWorkFlows/" + workflow["name"],
		contentType: "application/json", 
		dataType:"json",
		type:"PUT",
		async: false,
		data: JSON.stringify(workflow),
		success: function(msg){
			console.log(msg);
		},
		error: function(err){
			console.log("Error");
		},
		complete: function(xhr, status){
			switch(xhr.status){
				case 200:
      				$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-error");
        		 	$("#alertMsg").addClass("alert-success");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"goToUpdate()\">&times;</button>");
					$("#alertMsg").append("<h4>Success!!</h4> Workflow Updated successfully!!");
					$("#alertMsg").css("display", "block");
					setTimeout(function(){goToUpdate();},5000);
					break;
				default :
					$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-success");
        		 	$("#alertMsg").addClass("alert-error");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"returnToPage()\">&times;</button>");
					$("#alertMsg").append("<h4>Error!!</h4> Workflow update failed!!");
					$("#alertMsg").css("display", "block");		
			}
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

function getAllBusinessUnits(){
	bu = {};
	$.ajax({
		url: "/loader-server/businessUnits",
		contentType: "application/json", 
		dataType:"json",
		type:"GET",
		async: false,
		success: function(data){
			bu=data;
		},
		error: function(err){
			console.log("Error");
		},
		complete: function(xhr, status){
			console.log(xhr,status);
		}
	});
	return bu;
}

function executeWorkflow(){
	var workflowName = getQueryParams("workflowName");
	$.ajax({
		url: "/loader-server/scheduledWorkFlows/" + workflowName + "/execute",
		contentType: "application/json", 
		dataType:"json",
		type:"POST",
		async: false,
		data: "",
		success: function(msg){
			console.log(msg);
		},
		error: function(err){
			console.log("Error");
		},
		complete: function(xhr, status){
			switch(xhr.status){
				case 200:
      				$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-error");
        		 	$("#alertMsg").addClass("alert-success");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"goToUpdate()\">&times;</button>");
					$("#alertMsg").append("<h4>Success!!</h4> Workflow Executed successfully, Redirecting to running jobs page!!");
					$("#alertMsg").css("display", "block");
					setTimeout(function(){
						window.location="jobs.html";
					},3000);
					break;
				default :
					$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-success");
        		 	$("#alertMsg").addClass("alert-error");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"returnToPage()\">&times;</button>");
					$("#alertMsg").append("<h4>Error!!</h4> Workflow Execution failed!!");
					$("#alertMsg").css("display", "block");		
			}
		}
	});	
}

function goToUpdate(){
	window.location = "/updatescheduledworkflow.html?&workflowName=" + $("#workflowName").val();
}

function returnToPage(){
	location.reload();
}










