function getBusinessUnits(){
	var runName = $("#runName").val();
	var bu = $("#buName").val();
	var team = $("#team").val();
	var searchUrl = "/loader-server/businessUnits";
	$.ajax({
		url: searchUrl,
		contentType: "application/json", 
		dataType:"json",
		type:"GET",
		async:false,
		success: function(data){
			window.existingRuns = data;
		},
		error: function(){
			console.log("Error in getting runs");
		},
		complete: function(xhr, status){
			switch(xhr.status){
				case 200 : 
					break;
				default :
					window.existingRuns = {};
			}
		}
	});
}

function getFunctionParameters(functionName){
	if (typeof functionName == 'undefined' || functionName == 'undefined' || functionName == "Choose Class") { 
		window.inputParams = [];
		console.log("sending blank");
		return;
	};
	console.log("making ajax call");
	$.ajax({url: "/loader-server/functions/" + functionName + "?classInfo=true",
      contentType: "application/json", 
      type:"GET",
      async:false,
      success: function(data) {
        var ip = data[0]["inputParameters"];
        window.inputParams= [];
		$.each(ip, function(k,v){
			var defaultVal = v["defaultValue"]?v["defaultValue"]:"";
			window.inputParams.push(new inputParamViewModel(k, defaultVal)); 
		});
      },
      error: function(e){
        console.log("Error");
      },
      complete: function(xhr, status){
        console.log(status);
      }
    });
}

function getAvailableFunctions(){
	$.ajax({
	    url: "/loader-server/functions",
	    contentType: "application/json", 
	    type:"GET",
	    async: false,
	    success: function(data) {
	    	console.log('got the funcs', data);
	      	window.availableFunctions = ["Choose Class"];
	        window.availableFunctions = window.availableFunctions.concat(data);
	    },
	    error: function(e){
	       	console.log("Error");
	    },
	    complete: function(xhr, status){
	        console.log(status);
	    }
    });
}


var quickBuilderViewModel = function(){
	var self = this;
	self.selectedGroup = ko.observable("default");
	//self.selectedTeam = ko.observable("default");
	var availableGroups = [];
	$.each(window.existingRuns, function(k,v){
		availableGroups.push(k);
	})
	self.group = ko.observableArray(availableGroups);
	self.team = ko.computed(function(){
		var selGrp = self.selectedGroup();
		var teams = [];
		$.each(window.existingRuns[selGrp]["teams"], function(k,v){
			teams.push(k);
		})
		return teams;
	});
	console.log("functions",window.availableFunctions);
	self.functions = ko.observableArray(window.availableFunctions);
	self.selectedFunction = ko.observable("Choose Class");
	self.inputParameters = ko.computed(function(){
		var curFunc = self.selectedFunction();
		getFunctionParameters(curFunc);
		console.log("returning", window.inputParams);
		return window.inputParams;
	});
	self.onfunctionChange = function(){
		self.selectedFunction($("#functionClass").val());
	}
	self.onBuChange = function(){
		self.selectedGroup($("#grp").val());
	}
}

var inputParamViewModel = function(k,v){
	var self = this;
	self.key = ko.observable(k);
	self.val = ko.observable(v);
}

function createRun(){
	var runSchema = {};
	var params = {};
	var allParams = $("input.inputParamVal[type=text]");
	$.each(allParams, function(index, p){
		params[$(this).attr('id')]=$(this).val();
	})
	runSchema["runName"]=$("#runName").val();
	runSchema["businessUnit"]=$("#grp").val();
	runSchema["team"]=$("#team").val();
	runSchema["loadParts"]=[]
	runSchema["onDemandMetricCollections"]=[]
	runSchema["metricCollections"]=[]
	runSchema["loadParts"].push({
		"name":"loadPart0",
		"agents": 1,
		"classes":[$("#functionClass").val()],
		"inputFileResources": [],
		"load":{
			"groups":[{
				"name":"group0",
				"groupStartDelay":0,
				"threadStartDelay":0,
				"throughput":$("#throughput").val(),
				"repeats":$("#repeats").val(),
				"duration":$("#duration").val(),
				"threads":$("#threads").val(),
				"warmUpRepeats":$("#warmupRepeats").val(),
				"functions":[{
					"functionalityName":"function0",
					"functionClass":$("#functionClass").val(),
					"dumpData":true,
					"params":params
				}],
				"dependOnGroups":[],
				"params":{},
				"timers":[],
				"threadResources":[],
				"customTimers":[],
				"customCounters":[]
			}]
		}
	})
	console.log("runSchema",runSchema);
	$.ajax({
		url:"loader-server/runs",
		contentType: "application/json", 
      	type:"POST",
      	processData:false,
      	data: JSON.stringify(runSchema),
      	success: function(data){
      		console.log(data);
      	},
      	error: function(err){
      		console.log(err);
      	},
      	complete: function(xhr, status){
      		console.log("COMPLETE",xhr);
      		switch(xhr.status){
      			case 201:
      				$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-error");
        		 	$("#alertMsg").addClass("alert-success");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"reload()\">&times;</button>");
					$("#alertMsg").append("<h4>Success!!</h4> Run Created successfully!!");
					$("#alertMsg").css("display", "block");
					break;
				case 409:
					$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-success");
        		 	$("#alertMsg").addClass("alert-error");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"reload()\">&times;</button>");
					$("#alertMsg").append("<h4>Error!!</h4> Run name conflict!!");
					$("#alertMsg").css("display", "block");
					break;
				case 400:
					$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-success");
        		 	$("#alertMsg").addClass("alert-error");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"reload()\">&times;</button>");
					$("#alertMsg").append("<h4>Error!!</h4> Invalid options!!");
					$("#alertMsg").css("display", "block");
				default :
					$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-success");
        		 	$("#alertMsg").addClass("alert-error");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"reload()\">&times;</button>");
					$("#alertMsg").append("<h4>Error!!</h4> Run creation failed!!");
					$("#alertMsg").css("display", "block");
      		}
      	}
    });
}

function reload(){
	location.reload();
}
