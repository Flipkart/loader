ko.bindingHandlers.combobox = {
	init: function(element, valueAccessor, allBindingAccessor, viewModel){
		//console.log("initializing combobox");
		var value = valueAccessor();
        var valueUnWrapperd = ko.unwrap(value);
        $(element).combobox();	
	},
	update: function(element, valueAccessor, allBindingAccessor, viewModel){
		var selFun = viewModel.selectedFunction();
		var value = valueAccessor();
        var valueUnWrapperd = ko.unwrap(value);
		//if(valueUnWrapperd){
			$(element).children("option").each(function(index, opt){
		  		if($(opt).attr("value") == selFun){ 
		  			$(opt).attr("selected",true);
		  		}
		  		else { 
		  			$(opt).removeAttr("selected"); 
		  		}
			});
		$(element).combobox('refresh');
		//}
	}
}

ko.bindingHandlers.multiSelect = {
	init: function(element, valueAccessor, allBindingAccessor, viewModel){
		var selabHdr = "<div class='custom-header'><label><strong>Available Groups</strong></label>";
		var selcnHdr = "<div class='custom-header'><label><strong>Depends On Groups</strong></label>";
		switch($(element).attr('id')){
			case  "inputResourceList":
				selabHdr = "<div class='custom-header'><label><strong>Available InputFile Resources</strong></label></div>";
			  	selcnHdr = "<div class='custom-header'><label><strong>InputFile Resources in Use</strong></label>";
				break;
			case  "histogramsList":
				selabHdr = "<div class='custom-header'><label><strong>Available Histograms</strong></label></div>";
			  	selcnHdr = "<div class='custom-header'><label><strong>Plot Histograms</strong></label>";
				break;
			case "customCountersList":
				selabHdr = "<div class='custom-header'><label><strong>Available Custom Counters</strong></label></div>";
			  	selcnHdr = "<div class='custom-header'><label><strong>Plot Custom Counters</strong></label>";
				break;
			case "customTimersList":
				selabHdr = "<div class='custom-header'><label><strong>Available Custom Timers</strong></label></div>";
			  	selcnHdr = "<div class='custom-header'><label><strong>Plot Custom Timers</strong></label>";
				break;
		}
		$(element).multiSelect({
			selectableHeader: selabHdr,
			selectionHeader: selcnHdr
		});
	},
	update: function(element, valueAccessor, allBindingAccessor, viewModel){
		var value = valueAccessor();
        var valueUnWrapperd = ko.unwrap(value);
        //if(valueUnWrapperd){
			$(element).multiSelect('refresh');
		//}
	}
}

function getBusinessUnits(){	
	var searchUrl = "/loader-server/businessUnits";
	window.existingBus = [];
	window.existingTeams = {};
	$.ajax({
		url: searchUrl,
		contentType: "application/json", 
		dataType:"json",
		type:"GET",
		async:false,
		success: function(data){
			$.each(data, function(k,v){
				window.existingBus.push(k);
				var tmp = [];
				$.each(v["teams"], function(key,val){
					tmp.push(key);
				});
				window.existingTeams[k]=tmp;
			});
		},
		error: function(){
			//console.log("Error in getting businessUnits");
		},
		complete: function(xhr, status){
			switch(xhr.status){
				case 200 : 
					break;
				default :
					window.existingBus = [];
			}
			////console.log(window.existingBus,window.existingTeams);
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

function getAllFunctionClasses(){
	$.ajax({
	    url: "/loader-server/functions",
	    contentType: "application/json", 
	    type:"GET",
	    async:false,
	    success: function(data) {
	      	window.availableFunctions = ["Choose Class"];
	        window.availableFunctions = window.availableFunctions.concat(data);
	    },
	    error: function(e){
	        //console.log("Error");
	    },
	    complete: function(xhr, status){
	        //console.log(status);
	    }
    });
}



var quickBuilderViewModel = function(){
	var self = this;
	self.runName = ko.observable("TestRun");
	self.selectedBu = ko.observable("sample");
	self.availableBus = window.existingBus;
	self.availableTeams = ko.computed(function(){
		return window.existingTeams[self.selectedBu()];
	});
	self.selectedTeam = ko.observable("sample");
	self.threads = ko.observable(1);
	self.throughput = ko.observable(-1)
	self.repeats = ko.observable(1);
	self.duration = ko.observable(-1);
	self.warmUpRepeats = ko.observable(-1);
	self.availableFunctions = window.availableFunctions;
	self.selectedFunction = ko.observable("Choose Class");
	self.createdAt = "" + (new Date().getTime() + Math.floor(Math.random()*1000));
	self.accordionId = "accordionId_" +  self.createdAt;
	self.InputParameterId = "InputParameterId_" + self.createdAt;
	self.InputParameterIdHref = "#" + self.InputParameterId;
	self.histAccordionId = "histAccordionId_" + self.createdAt;
	self.histId = "histId_" + self.createdAt;
	self.histIdHref = "#" + self.histId; 
	self.timerAccordionId = "timerAccordionId_" + self.createdAt;
	self.timerId = "timerId_" + self.createdAt;
	self.timerIdHref = "#" + self.timerId;
	self.counterAccordionId = "counterAccordionId_" + self.createdAt;
	self.counterId = "counterId_" + self.createdAt;
	self.counterIdHref = "#" + self.counterId;
	self.isVisible = ko.observable(true);
	self.availableParameters = ko.computed(function(){
		var functionName = self.selectedFunction();
		if (typeof functionName == 'undefined' || functionName == undefined || functionName == "Choose Class") { 
			return [];
		}
		var inputParams= [];
		var params = {};
		$.ajax({url: "/loader-server/functions/" + functionName + "?classInfo=true",
    		contentType: "application/json", 
      		type:"GET",
      		async:false,
      		success: function(data) {
        		var ip = data[0]["inputParameters"];
				$.each(ip, function(k,v){
					inputParams.push(new inputParamViewModel(v)); 
				});
				params["inputParameters"] = ko.observableArray(inputParams);
				params["histograms"] = ko.observableArray(data[0]["customHistograms"]);
				params["customCounters"] = ko.observableArray(data[0]["customCounters"]);
				params["customTimers"] = ko.observableArray(data[0]["customTimers"]);
      		},
      		error: function(e){
        		//console.log("Error");
      		}
    	});
    	return params;
	});
	self.selectedHistograms = ko.observableArray([]);
	self.selectedCustomTimers = ko.observableArray([]);
	self.selectedCustomCounters = ko.observableArray([]);
}

var inputParamViewModel = function(inputParam){
	var self = this;
	self.key = inputParam["name"];
	self.isScalar = inputParam["type"]=="SCALER"?true:false;
	self.isHashMap = inputParam["type"]=="MAP"?true:false;
	self.isList = inputParam["type"]=="LIST"?true:false;
	self.showButton = ko.computed(function(){
		return !self.isScalar;
	});
	self.getScalar = function(){
		if(!self.isScalar) return "";
		if(inputParam["defaultValue"]==null || inputParam["defaultValue"]==undefined) return "";
		return inputParam["defaultValue"];
	}
	self.getList = function(){
		if(!self.isList) return [];
		var list = $.parseJSON(inputParam["defaultValue"]);
		var params = [];
		$.each(list, function(index, param){
			params.push({"keyValue": ko.observable(param)});
		});
		return params;
	}
	self.getMap = function(){
		if(!self.isHashMap) return [];
		var map =  $.parseJSON(inputParam["defaultValue"]);
		var params = [];
		$.each(map, function(k,v){
			params.push({"name": ko.observable(k), "keyValue":ko.observable(v)});
		});
		return params;
	}
	self.scalarValue = ko.observable(self.getScalar());
	self.listValue = ko.observableArray(self.getList());
	self.mapValue = ko.observableArray(self.getMap());
	self.addListElement = function(){
		self.listValue.push({"keyValue": ko.observable("")});
	}
	self.addMapElement = function(){
		self.mapValue.push({"name":ko.observable(""), "keyValue":ko.observable("")})
	}
	self.addElement= function(){
		if(self.isList) self.addListElement();
		else self.addMapElement();
	}
	self.removeFromMap = function(elem){
		self.mapValue.remove(elem);
	}
	self.removeFromList = function(elem){
		self.listValue.remove(elem);
	}
	self.returnScalar = function(){
		return self.scalarValue();
	}
	self.returnList = function(){
		var paramList = self.listValue();
		var result = [];
		$.each(paramList, function(index, elem){
			result.push(elem.keyValue());
		});
		return result;
	}
	self.returnMap = function(){
		var mapList = self.mapValue();
		var result = {};
		$.each(mapList, function(ind, elem){
			result["\"" + elem.name() + "\""] = elem.keyValue();
		});
		return result;
	}
	self.val = function(){
		if(self.isScalar) return self.returnScalar();
		if(self.isList) return self.returnList();
		if(self.isHashMap) return self.returnMap();
	}
}

function createRun(){
	var runSchema = {};
	var model = window.viewModel;
	var agentTags = [model.selectedBu()];
	if(agentTags.indexOf(model.selectedTeam())==-1) agentTags.push(model.selectedTeam());
	var params = {};
	if(model.availableParameters().inputParameters!=undefined){
		$.each(model.availableParameters().inputParameters(), function(paramIndex, param){
			params[param.key] = param.val();
		});
	}
	runSchema["runName"]=model.runName();
	runSchema["businessUnit"]=model.selectedBu();
	runSchema["team"]=model.selectedTeam();
	runSchema["loadParts"]=[]
	runSchema["onDemandMetricCollections"]=[]
	runSchema["metricCollections"]=[]
	runSchema["description"] = "Run created by Quick builder."
	runSchema["loadParts"].push({
		"name":"loadPart0",
		"agents": 1,
		"classes":[model.selectedFunction()],
		"inputFileResources": [],
		"agentTags":agentTags,
		"load":{
			"logLevel": "INFO",
			"setupGroup": null,
			"tearDownGroup": null,
			"dataGenerators": {},
			"groups":[{
				"name":"group0",
				"groupStartDelay":0,
				"threadStartDelay":0,
				"throughput":model.throughput(),
				"repeats":model.repeats(),
				"duration":model.duration(),
				"threads":model.threads(),
				"warmUpRepeats":model.warmUpRepeats(),
				"functions":[{
					"functionalityName":"function0",
					"functionClass":model.selectedFunction(),
					"dumpData":true,
					"params":params,
					"customTimers": model.selectedCustomTimers(),
					"customCounters": model.selectedCustomCounters,
					"customHistograms": model.selectedHistograms()
				}],
				"dependOnGroups":[],
				"params":{},
				"timers":[],
				"threadResources":[],
				"dataGenerators":{}
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
