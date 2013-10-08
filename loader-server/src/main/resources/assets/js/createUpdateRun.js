function renderDisplayArea(elementType, metadata){
	switch(elementType){
		case 'run':
			renderRunPage(metadata);
			break;
		case 'loadPart':
			renderLoadPartPage(metadata);
			break;
		case 'group':
			renderGroupPage(metadata);
			break;
		case 'function':
			renderFunctionPage(metadata);
			break;
		case 'metricCollection':
			renderMetricCollectionPage(metadata);
			break;
		case 'monitoringAgents':
			renderMonitoringAgentsAddPage(metadata);
			break;
		case 'timers':
			renderTimersPage(metadata);
			break;
	}
}
var runJsonViewModel = function(){
	var self=this;
	self.run = ko.observable(window.runSchema);
	self.runJson = ko.computed(function(){
		return JSON.stringify(self.run(), undefined, 4);
	});
}

var runSchemaViewModel = function(runSchema){
	var self = this;
	self.runName = ko.observable(runSchema["runName"]);
	var availableGroups = [];
	$.each(window.existingBus, function(k,v){
		availableGroups.push(k);
	})
	self.businessUnit = ko.observableArray(availableGroups);
	self.selectedBu = ko.observable(runSchema["businessUnit"]);
	self.team = ko.computed(function(){
		var selGrp = self.selectedBu();
		var teams = [];
		$.each(window.existingBus[selGrp]["teams"], function(k,v){
			teams.push(k);
		})
		return teams;
	});
	self.selectedTeam = ko.observable(runSchema["team"]);
	self.onBuChange = function(){
		self.selectedBu($("#bu").val());
	}
	console.log("runSchemaViewModel", self);
}

var loadPartViewModel = function(loadPart){
	var self = this;
	self.lPart = ko.observable(loadPart);
	self.loadPartName = ko.computed(function(){
		console.log(self.lPart());
		return self.lPart().name;
	});
	self.agents = ko.computed(function(){
		return self.lPart().agents;
	});
	self.availableInputResources = ko.observableArray(window.availableInputResources);
	self.useInputResources = ko.computed(function(){
		return self.lPart()["inputFileResources"];
	});
}

var groupViewModel = function(grp){
	var self = this;
	self.group = ko.observable(grp)
	self.loadPartIndex = ko.observable(0);
	self.groupName = ko.computed(function(){
		return self.group()["name"];
	});
	self.groupStartDelay = ko.computed(function(){
		return self.group()["groupStartDelay"];
	});
	self.threadStartDelay = ko.computed(function(){
		return self.group()["threadStartDelay"];
	});
	self.throughput = ko.computed(function(){
		return self.group()["throughput"];
	});
	self.repeats = ko.computed(function(){
		return self.group()["repeats"];
	});
	self.duration = ko.computed(function(){
		return self.group()["duration"];
	});
	self.threads = ko.computed(function(){
		return self.group()["threads"];
	});
	self.warmUpRepeats = ko.computed(function(){
		return self.group()["warmUpRepeats"];
	});
	//fix the select wala part
	self.availableGroups = ko.computed(function(){
		var grpName = self.group()["name"];
		var availGrps = [];
		$.each(window.runSchema.loadParts[self.loadPartIndex()]["load"]["groups"], function(index, grp){
			console.log(grp);
			console.log("matching", grp["name"], grpName);
			if(grp["name"]!== grpName) availGrps.push(grp["name"]);
		})
		return availGrps;
	});
	self.dependsOn = ko.computed(function(){
		return self.group()["dependOnGroups"];
	});
}

var functionViewModel = function(funct){
	var self = this;
	self.curFunction = ko.observable(funct);
	self.functionName = ko.computed(function(){
		return self.curFunction()["functionalityName"];
	});
	//self.selectedFunction = ko.observable(funct["functionClass"]=="noclass"?"Choose Class":funct["functionClass"]);
	self.selectedFunction = ko.computed(function(){
		var f = self.curFunction();
		return f["functionClass"]=="noclass"?"Choose Class":f["functionClass"];
	});
	self.availableFunctionClass = ko.computed(function(){
		var f = self.curFunction();
		return window["availableFunctions"];
	});
	self.dumpData = ko.computed(function(){
		return self.curFunction()["dumpData"]?"true":"false";
	});
	self.dumpOpts = ko.observableArray(["false", "true"])
	self.inputParameters = ko.computed(function(){
		console.log("Calling computed");
		var f = self.curFunction();
		var fname = f["functionClass"]=="noclass"?"Choose Class":f["functionClass"];
		getFunctionParameters(fname);
		$.each(window.inputParams, function (index, param){
			console.log("f[\"params\"][param[\"key\"]]", f["params"][param.key]);
			if(f["params"][param.key]!== undefined && typeof f["params"][param.key]!== 'undefined' && f["params"][param.key]!==""){ console.log("changing value");window.inputParams[index].val=f["params"][param.key];} 
		});
		console.log("returning", window.inputParams);
		return window.inputParams;
	});
	self.onchange = function(){
		console.log("calling onchange")
		var f = self.curFunction();
		f["functionClass"] = $("#functionClass").val();
		console.log("setting", $("#functionClass").val());
		self.curFunction(f);
	}
}

var timersViewModel = function(groupTimers) {
	console.log("i got groupTimers",groupTimers);
	var self = this;
	self.timers = ko.observableArray(groupTimers);
	self.removeTimer = function(data, event){
		console.log("in cut", data);
		var timersData = window.selectedElementData;
		self.timers.remove(data);
		window.runSchema.loadParts[timersData["loadPartIndex"]]["load"]["groups"][timersData["groupIndex"]]["timers"]=self.timers();

	}
}

var inputParamViewModel = function(k, v){
	var self = this;
	self.key = k;
	self.val = v;
}

var metricsCollectionViewModel = function(metric){
	var self = this;
	self.currMetric = ko.observable(metric);
	self.agent = ko.computed(function(){
		return self.currMetric().agent;
	});
	self.selectedOpts = ko.computed(function(){
		console.log("seleOpts returning", self.currMetric()["collectionInfo"]["resources"]);
		return self.currMetric()["collectionInfo"]["resources"];
	});
	self.opts = ko.computed(function(){
		var curSel = self.currMetric()["collectionInfo"]["resources"];
		getResources();
		console.log("returning opts", window.availableMetrices);
		return window.availableMetrices;
	});
	self.findResources = function(){
		console.log("finding resources");
		var metric = self.currMetric();
		metric["agent"] = $("#agent").val();
		self.currMetric(metric);
	}
}


function renderRunPage(metadata){
    $(".runSchemaElement").css("display","none");
	$("#run").removeAttr("style");
	if(window.models["run"]["firstview"]){
		window.models.run.instance = new runSchemaViewModel(window.runSchema);
		ko.applyBindings(window.models.run.instance, $("#run")[0]);
		window.models["run"]["firstview"]=false;
	}
	window.models.run.instance.runName(window.runSchema.runName);
    $("#runTree").bind("reselect.jstree", function(){
    	$("#runTree").jstree("select_node","#node_" + window.runSchema.runName);
    }); 
}

function updateRun(){
	window.runSchema.runName = $("#runName").val();
	window.runSchema.businessUnit = $("#bu").val();
	window.runSchema.team = $("#team").val();
	createTree(window.runSchema);
	renderDisplayArea('run',window["runSchema"]);
}

function updateRunGrpTeam(){
	window.runSchema.businessUnit = $("#bu").val();
	window.runSchema.team = $("#team").val();
	createTree(window.runSchema);
	renderDisplayArea('run',window["runSchema"]);
}

function addLoadPart(){
	var loadPart = {"name":"loadPart" + window.runSchema.loadParts.length,
					"agents":1,
					"load":{"groups" : new Array()},
					"inputFileResources":new Array()
					};
	window.runSchema.loadParts.push(loadPart);
	createTree(window.runSchema);
	renderDisplayArea('loadPart', {"loadPartIndex": window.runSchema.loadParts.length-1} );
}

function renderLoadPartPage(metadata){
	var loadPart = window.runSchema.loadParts[metadata["loadPartIndex"]];
    $(".runSchemaElement").css("display","none");
	$("#loadPart").removeAttr("style");
	if(window.models["loadPart"]["firstview"]){
		window.models.loadPart.instance = new loadPartViewModel(loadPart);
		ko.applyBindings(window.models.loadPart.instance, $("#loadPart")[0]);
		window.models["loadPart"]["firstview"]=false;
	} else {
		window.models.loadPart.instance.lPart(loadPart);
	}
    $("#runTree").bind("reselect.jstree", function(){
    	$("#runTree").jstree("select_node","#node_" + window.runSchema.loadParts[metadata["loadPartIndex"]]["name"]);
    }); 
}

function updateLoadPart(){
	var loadPartData = window.selectedElementData;
	console.log("loadPartData",loadPartData);
	var loadPart = window.runSchema["loadParts"][loadPartData["loadPartIndex"]];
	console.log("loadPart", loadPart);
	loadPart["name"] = $("#loadPartName").val();
	loadPart["agents"] = $("#agents").val();
	if($("#inputResourceList").val()!=null) loadPart["inputFileResources"]=$("#inputResourceList").val();
	console.log("input",$("#inputResourceList").val().join());
	window.runSchema["loadParts"][loadPartData["loadPartIndex"]] = loadPart;
	createTree(window.runSchema);
	renderDisplayArea('loadPart', window.selectedElementData);
}

function deleteLoadPart(){
	var loadPartData = window.selectedElementData;
	var loadPart = window.runSchema["loadParts"][loadPartData["loadPartIndex"]];
	window.runSchema["loadParts"].splice(loadPartData["loadPartIndex"],1);
	createTree(window.runSchema);
	renderDisplayArea('run', window.selectedElementData);
}

function addGroup(){
	var loadPartData = window.selectedElementData;
	var group = {
		"name":"group" + window.runSchema.loadParts[loadPartData["loadPartIndex"]]["load"]["groups"].length,
        "groupStartDelay":0,
        "threadStartDelay":0,
        "throughput":-1.0,
        "repeats":1,
        "duration":-1,
        "threads":1,
        "warmUpRepeats":-1,
        "functions": new Array(),
        "dependOnGroups": new Array(),
        "params" : {},
        "timers" : new Array(),
        "threadResources" : new Array(),
        "customTimers" : new Array(),
        "customCounters" : new Array()
	}	
	window.runSchema.loadParts[loadPartData["loadPartIndex"]]["load"]["groups"].push(group);
	createTree(window.runSchema);
	renderDisplayArea('group', {"loadPartIndex": loadPartData["loadPartIndex"], 
		"groupIndex":window.runSchema.loadParts[loadPartData["loadPartIndex"]]["load"]["groups"].length-1});
}

function renderGroupPage(metadata){
	var grp = window.runSchema.loadParts[metadata["loadPartIndex"]]["load"]["groups"][metadata["groupIndex"]];
    $(".runSchemaElement").css("display","none");
	$("#group").removeAttr("style");
    if(window.models["group"]["firstview"]){
		window.models.group.instance = new groupViewModel(grp);
		window.models.group.instance.loadPartIndex(metadata["loadPartIndex"]);
		ko.applyBindings(window.models.group.instance, $("#group")[0]);
		window.models["group"]["firstview"]=false;
	} else {
		window.models.group.instance.group(grp);
		window.models.group.instance.loadPartIndex(metadata["loadPartIndex"]);
	}
    $("#runTree").bind("reselect.jstree", function(){
    	$("#runTree").jstree("select_node","#node_" + metadata["loadPartIndex"] + "_" + grp["name"]);
    }); 
    $("#groupList").multiSelect();
    
}

function updateGroup(){
	var grpData = window.selectedElementData;
	var grp = window.runSchema.loadParts[grpData["loadPartIndex"]]["load"]["groups"][grpData["groupIndex"]];
	grp["name"] = $("#groupNameText").val();
	grp["groupStartDelay"] = $("#groupStartDelay").val();
	grp["threadStartDelay"] = $("#threadStartDelay").val();
	grp["throughput"] = $("#throughput").val();
	grp["repeats"] = $("#repeats").val();
	grp["duration"] = $("#duration").val();
	grp["threads"] = $("#threads").val();
	grp["warmUpRepeats"] = $("#warmUpRepeats").val();
	grp["dependOnGroups"].length=0;
	if($("#groupList").val()!=null) grp["dependOnGroups"].push($("#groupList").val().join());
	window.runSchema.loadParts[grpData["loadPartIndex"]]["load"]["groups"][grpData["groupIndex"]]=grp;
	createTree(window.runSchema);
	renderDisplayArea('group', window.selectedElementData);

}

function deleteGroup(){
	var grpData = window.selectedElementData;
	window.runSchema.loadParts[grpData["loadPartIndex"]]["load"]["groups"].splice(grpData["groupIndex"],1);
	createTree(window.runSchema);
	renderDisplayArea('loadPart', window.selectedElementData);
}

function addFunction(){
	var grpData = window.selectedElementData;
	var funct = {
		"functionalityName":"function" + window.runSchema.loadParts[grpData["loadPartIndex"]]["load"]["groups"][grpData["groupIndex"]]["functions"].length,
		"functionClass": "noclass",
		"dumpData":false,
		"params":{}
	} 
	window.runSchema.loadParts[grpData["loadPartIndex"]]["load"]["groups"][grpData["groupIndex"]]["functions"].push(funct);
	createTree(window.runSchema);
	renderDisplayArea('function', 
		{"loadPartIndex":grpData["loadPartIndex"],"groupIndex":grpData["groupIndex"],
		"functionIndex":window.runSchema.loadParts[grpData["loadPartIndex"]]["load"]["groups"][grpData["groupIndex"]]["functions"].length-1});
}

function renderFunctionPage(metadata){
	var func = window.runSchema.loadParts[metadata["loadPartIndex"]]["load"]["groups"][metadata["groupIndex"]]["functions"][metadata["functionIndex"]];
	console.log("this is func", func);
    $(".runSchemaElement").css("display","none");
	$("#function").removeAttr("style");
	if(window.models["function"]["firstview"]){
		window.models["function"].instance = new functionViewModel(func);
		ko.applyBindings(window.models["function"].instance, $("#function")[0]);
		window.models["function"]["firstview"]=false;
	} else {
		window.models["function"].instance.curFunction(func);
		//window.models["function"].instance.selectedFunction(
		//	func["functionClass"]=="noclass"?"Choose Class":func["functionClass"]);
	}
    $("#runTree").bind("reselect.jstree", function(){
    	console.log("selecting the funcion node");
    	$("#runTree").jstree("select_node","#node_" + metadata["loadPartIndex"] + "_" + metadata["groupIndex"]+ "_" + func["functionalityName"]);
    }); 
}

function updateFunction(){
	var metadata = window.selectedElementData;
	var func = window.runSchema.loadParts[metadata["loadPartIndex"]]["load"]["groups"][metadata["groupIndex"]]["functions"][metadata["functionIndex"]];
	func["functionalityName"] = $("#funcName").val();
	func["functionClass"] = $("#functionClass").val();
	func["dumpData"] = $("#dumpDataSel").val()=="true"?true:false;
	console.log("window.inputParams is", window.inputParams);
	$.each(window.inputParams, function(index, param){
		func["params"][param.key] = $("#"+param.key).val();
	});
	console.log("paramstable after update", func["params"]);
	window.runSchema.loadParts[metadata["loadPartIndex"]]["load"]["groups"][metadata["groupIndex"]]["functions"][metadata["functionIndex"]]=func;
	createTree(window.runSchema);
	renderDisplayArea('function', window.selectedElementData);
}

function deleteFunction(){
	var metadata = window.selectedElementData;
	window.runSchema.loadParts[metadata["loadPartIndex"]]["load"]["groups"][metadata["groupIndex"]]["functions"].splice(metadata["functionIndex"],1);
	createTree(window.runSchema);
	renderDisplayArea('group', window.selectedElementData);
}

function addTimer(){
	var grpData = window.selectedElementData;
	if(window.runSchema.loadParts[grpData["loadPartIndex"]]["load"]["groups"][grpData["groupIndex"]]["timers"].length == 0) {
		var timer = {
			"name":"Timer-" + window.runSchema.loadParts[grpData["loadPartIndex"]]["load"]["groups"][grpData["groupIndex"]]["timers"].length,
			"duration" : -1,
			"threads" : -1,
			"throughput" : -1
		}	
		window.runSchema.loadParts[grpData["loadPartIndex"]]["load"]["groups"][grpData["groupIndex"]]["timers"].push(timer);
	}
	createTree(window.runSchema);
	renderDisplayArea('timers',{"loadPartIndex":grpData["loadPartIndex"],"groupIndex":grpData["groupIndex"]});
}

function plusTimer(){
	var timersData = window.selectedElementData;
	var timer = {
		"name":"Timer-" + window.runSchema.loadParts[timersData["loadPartIndex"]]["load"]["groups"][timersData["groupIndex"]]["timers"].length,
		"duration" : -1,
		"threads" : -1,
		"throughput" : -1
	}	
	window.runSchema.loadParts[timersData["loadPartIndex"]]["load"]["groups"][timersData["groupIndex"]]["timers"].push(timer);
	renderDisplayArea('timers',{"loadPartIndex":timersData["loadPartIndex"],"groupIndex":timersData["groupIndex"]});	
}


function renderTimersPage(metadata){
	var timers = window.runSchema.loadParts[metadata["loadPartIndex"]]["load"]["groups"][metadata["groupIndex"]]["timers"];
	$(".runSchemaElement").css("display","none");
	$("#timers").removeAttr("style");
	if(window.models["timers"]["firstview"]){
		window.models["timers"].instance = new timersViewModel(timers);
		ko.applyBindings(window.models["timers"].instance, $("#timers")[0]);
		window.models["timers"]["firstview"]=false;
	} else {
		window.models["timers"].instance.timers(timers);
	}
	$("#runTree").bind("reselect.jstree", function(){
    	console.log("selecting the funcion node");
    	$("#runTree").jstree("select_node","#node_" + metadata["loadPartIndex"] + "_" + metadata["groupIndex"]+ "_timers");
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
			window.inputParams.push(new inputParamViewModel(k,defaultVal)); 
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

function addMetricCollection(){
	var metricCollector = {
		"agent":"127.0.0.1",
		"collectionInfo":{
			"resources":new Array(),
			"lastHowManyInstances":1,
			"publishUrl":"http://" + window.location.hostname + ":9999/loader-server/jobs/{jobId}/monitoringStats",
			"forHowLong":0,
            "interval":20000
		}

	}
	window.runSchema.metricCollections.push(metricCollector);
	createTree(window.runSchema);
	renderDisplayArea('metricCollection',{"nodeType":"metricCollection", "metricCollectionIndex":window.runSchema.metricCollections.length-1});
}

function renderMetricCollectionPage(metadata){
	var metric = window.runSchema.metricCollections[metadata["metricCollectionIndex"]];
	$(".runSchemaElement").css("display","none");
	$("#monitoringAgent").removeAttr("style");
	if(window.models["metricCollection"]["firstview"]){
		window.models["metricCollection"].instance = new metricsCollectionViewModel(metric);
		ko.applyBindings(window.models["metricCollection"].instance, $("#monitoringAgent")[0]);
		window.models["metricCollection"]["firstview"]=false;
	} else {
		window.models["metricCollection"].instance.currMetric(metric);
	}
    $("#runTree").bind("reselect.jstree", function(){
    	$("#runTree").jstree("select_node","#node_" + metric["agent"].replace(/\./g,"_"));
    }); 
}

function renderMonitoringAgentsAddPage(metadata){
    $(".runSchemaElement").css("display","none");
	$("#metricCollection").removeAttr("style");
    $("#runTree").bind("reselect.jstree", function(){
    	$("#runTree").jstree("select_node","#node_monitoringAgents");
    });
}

function updateMetricCollection(){
	var metricData = window.selectedElementData;
	var metric = window.runSchema.metricCollections[metricData["metricCollectionIndex"]];
	metric["agent"] = $("#agent").val();
	metric["collectionInfo"]["resources"].length=0;
	metric["collectionInfo"]["resources"] = $("#resources").val();
	console.log("Update Metric", metric);
	window.runSchema.metricCollections[metricData["metricCollectionIndex"]]=metric;
	createTree(window.runSchema);
	renderDisplayArea('metricCollection',window.selectedElementData);
}

function deleteMetricCollection(){
	window.runSchema.metricCollections.splice(window.selectedElementData["metricCollectionIndex"],1);
	createTree(window.runSchema);
	renderDisplayArea('monitoringAgents',{});
}

function getResources(){
	var metricData = window.selectedElementData;
	var metric = window.runSchema.metricCollections[metricData["metricCollectionIndex"]];
	console.log("Getting agent value", $("#agent").val());
	$.ajax({
		"url":"http://" + $("#agent").val() + ":7777/monitoring-service/resources",
		"contentType": "application/json", 
      	"type":"GET",
      	"async":false,
      	success: function(data){
      		window.resourceData = data;
      	},
      	error: function(err){
      		console.log("in error",err);
      		window.availableMetrices = [];
      	}, 
      	complete: function(xhr, status){
      		window.availableMetrices = [];
      		if(xhr.status==200){
      			window.availableMetrices = window.resourceData;
      		}
      		console.log("returning", window.availableMetrices);
      	}

	});
}

function createTree(data){ 
	//console.log(data);
	$("#runTree").jstree({
			"json_data" : {
				"data" : [{
					"attr":{"id" : "node_" + data["runName"], "rel":"run"},
					"data" : data["runName"], 
					"metadata" : { "name" : data["runName"], "nodeType" : "run"},    
					"children" : getChildren(data)
                	}],
            	"progressive_render" : true,
			},
			"dnd" : {
				"drop_target" : false,
            	"drag_target" : false
			},
			"types": {
				"valid_children":["run"],
				"types": {
					"run":{
						"icon":{
							"image":"../img/run.png"
						},
						"valid_children":["loadPart", "monitoringAgents"]
					},
					"loadPart":{
						"icon":{
							"image":"../img/loadpart.png"
						},
						"valid_children":["group"]
					},
					"group":{
						"icon":{
							"image":"../img/group.png"
						},
						"valid_children":["function"]
					},
					"function":{
						"icon":{
							"image":"../img/function.png"
						},
						"valid_children":[]
					},
					"timers":{
						"icon":{
							"image":"../img/function.png"
						},
						"valid_children":[]
					},
					"monitoringAgents":{
						"icon":{
							"image":"../img/monagents.png"
						},
						"valid_children":["metricCollection"]
					},
					"metricCollection":{
						"icon":{
							"image":"../img/metriccol.png"
						},
						"valid_children":[]
					}
				}
			},
			"plugins" : [ "themes", "json_data", "ui", "cookies", "dnd","types"],
			"cookies" : {
							"save_selected":true,
							"save_opened":true,
							"auto_save":true,
							"cookie_options":{}
						},
			"ui" : {
					"selected_parent_open":true
					}
	}).bind("select_node.jstree", function(event, data){
		//console.log(data.rslt.obj.data());
		window.selectedElementData = data.rslt.obj.data();
		renderDisplayArea(data.rslt.obj.data("nodeType"), data.rslt.obj.data());
		
	});
	$("#runTree").bind("loaded.jstree", function (event, data) {
        $("#runTree").jstree("open_all");
    });
    $("#runTree").bind("refresh.jstree", function (event, data) {
        $("#runTree").jstree("open_all");
    });
}

function getChildren(data){
	var lps= getLoadParts(data);
	var mas = getMetricCollectors(data);
	if(typeof lps == 'undefined') {
		if (typeof mas == 'undefined'){
			return [{"attr":{"id": "node_monitoringAgents", "rel": "monitoringAgents"}, "data": "MonitoringAgents", "metadata": {"nodeType":"monitoringAgents"}}]; 
		} else {
			return [{"attr":{"id": "node_monitoringAgents", "rel": "monitoringAgents"}, "data": "MonitoringAgents", "metadata": {"nodeType":"monitoringAgents"}, "children": mas}]; 
		}
	}
	if (typeof mas == 'undefined'){
		lps.push({"attr":{"id": "node_monitoringAgents", "rel":"monitoringAgents"}, "data": "MonitoringAgents", "metadata": {"nodeType":"monitoringAgents"}});
	} else {
		lps.push({"attr":{"id": "node_monitoringAgents", "rel":"monitoringAgents"}, "data": "MonitoringAgents", "metadata": {"nodeType":"monitoringAgents"}, "children": mas});
	}
	return lps;
}

function getLoadParts(data){
	var loadPartsName = new Array();
	var loadParts = data["loadParts"];
	if (loadParts.length==0) return undefined;
	for (var k=0; k<loadParts.length; k++){
		var agents = loadParts[k]["agents"];
		loadPartsName[k]={"attr" :{"id": "node_" + loadParts[k]["name"], "rel":"loadPart"}, "data": loadParts[k]["name"], "metadata" : {"nodeType":"loadPart", "loadPartIndex": k}, "children": getGroupList(loadParts[k], k)};
	}
	console.log("loadpartsname:", loadPartsName);
	return loadPartsName;
}

function getMetricCollectors(data){
	var metricsAgents = new Array();
	var metricsCollectors = data["metricCollections"];
	if(metricsCollectors.length==0) return undefined;
	for( var k=0; k<metricsCollectors.length;k++){
		metricsAgents[k]={"attr":{"id": "node_" + metricsCollectors[k]["agent"].replace(/\./g,"_"), "rel":"metricCollection"}, "data": metricsCollectors[k]["agent"], "metadata": {"nodeType":"metricCollection", "metricCollectionIndex":k}};
	}
	return metricsAgents;
}

function getGroupList(data, loadPartIndex){
	var groupName = new Array();
	var groups = data["load"]["groups"];
	if (groups.length==0) return undefined;
	for(var i=0;i<groups.length;i++){
		groupName[i]={ "attr":{"id": "node_" + loadPartIndex + "_" + groups[i]["name"], "rel":"group"},"data": groups[i]["name"], "metadata" :{"nodeType":"group", "loadPartIndex": loadPartIndex, "groupIndex": i} ,"children" : getFunctionList(groups[i], loadPartIndex ,i)};
	}
	console.log("returning from getGroupList");
	return groupName;
}

function getFunctionList(group, loadPartIndex, groupIndex){
	var functionName = new Array();
	var functions = group["functions"];
	if (functions.length==0 && group["timers"].length ==0) return undefined;
	for(var i=0;i<functions.length; i++){
		functionName[i]={"attr":{"id":"node_" + loadPartIndex + "_" + groupIndex + "_" + functions[i]["functionalityName"], "rel":"function"}, "type":"function","metadata": {"nodeType":"function", "loadPartIndex":loadPartIndex, "groupIndex": groupIndex, "functionIndex":i}, "data": functions[i]["functionalityName"]};
	}
	if(group["timers"].length>0){
		functionName[functions.length] = {"attr":{"id":"node_" + loadPartIndex + "_" + groupIndex + "_" + "timers", "rel":"timers"}, "type":"timers", "metadata": {"nodeType":"timers", "loadPartIndex":loadPartIndex, "groupIndex": groupIndex}, "data":"timers"};
	}
	console.log("returning from getFunctionList");
	return functionName;
}

function createRun(){
	var isValid = true;
	if(window.selectedView=='json'){
		window.runSchema = $.parseJSON($("#runJson").val());
	}
	$.each(window.runSchema["loadParts"], function(lpIndex, loadPart){
		var classes = new Array();
		$.each(loadPart["load"]["groups"], function(grpIndex, group){
			$.each(group["functions"], function(funcIndex, funct){
				if (funct["functionClass"]=="noclass") {
					isValid = false;
					return;
				}
				if(classes.indexOf(funct["functionClass"])==-1){
					classes.push(funct["functionClass"]);
				}
			});
		});
		window.runSchema["loadParts"][lpIndex]["classes"] =  classes;
	});

	if (!isValid){
		$("#alertMsg").empty();
  	    $("#alertMsg").removeClass("alert-success");
        $("#alertMsg").addClass("alert-error");
        $("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"reload()\">&times;</button>");
		$("#alertMsg").append("<h4>Error!!</h4> Function with no class exists!!");
		$("#alertMsg").css("display", "block");
		return;
	}
	//window.runSchema["classes"] = classes;
	console.log("sending", JSON.stringify(window.runSchema));
	$.ajax({
		url:"loader-server/runs",
		contentType: "application/json", 
      	type:"POST",
      	processData:false,
      	data: JSON.stringify(window.runSchema),
      	success: function(data){
      		console.log(data);
      	},
      	error: function(err){
      		console.log(err);
      	},
      	complete: function(xhr, status){
      		console.log("COMPLETE",xhr);
      		$("#success").empty();
      		switch (xhr.status){
      			case 201:
      				$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-error");
        		 	$("#alertMsg").addClass("alert-success");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"goToUpdate()\">&times;</button>");
					$("#alertMsg").append("<h4>Success!!</h4> Run Created successfully!!");
					$("#alertMsg").append("<br>Redirecting to update run page...");
					$("#alertMsg").css("display", "block");
					setTimeout(function(){goToUpdate();},5000);
					break;
				case 409:
					$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-success");
        		 	$("#alertMsg").addClass("alert-error");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"returnToPage()\">&times;</button>");
					$("#alertMsg").append("<h4>Error!!</h4> Run name conflict!!");
					$("#alertMsg").css("display", "block");
					break;
				case 400:
					$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-success");
        		 	$("#alertMsg").addClass("alert-error");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"returnToPage()\">&times;</button>");
					$("#alertMsg").append("<h4>Error!!</h4> Invalid options!!");
					$("#alertMsg").css("display", "block");
				default :
					$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-success");
        		 	$("#alertMsg").addClass("alert-error");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"returnToPage()\">&times;</button>");
					$("#alertMsg").append("<h4>Error!!</h4> Run creation failed!!");
					$("#alertMsg").css("display", "block");
			 }
      	}
	})
}

function updateRunSchema(){
	var isValid = true;
	if(window.selectedView=='json'){
		window.runSchema = $.parseJSON($("#runJson").val());
	}
	$.each(window.runSchema["loadParts"], function(lpIndex, loadPart){
		var classes = new Array();
		$.each(loadPart["load"]["groups"], function(grpIndex, group){
			$.each(group["functions"], function(funcIndex, funct){
				if (funct["functionClass"]=="noclass") {
					isValid = false;
					return;
				}
				if(classes.indexOf(funct["functionClass"])==-1){
					classes.push(funct["functionClass"]);
				}
			});
		});
		window.runSchema["loadParts"][lpIndex]["classes"] =  classes;
	});

	if (!isValid){
		// $("#success").append("<p>U have function with no class, Can't create run!!</p>");
		// $("#success").dialog();
		return;
	}
	$.ajax({
		url:"loader-server/runs/" + runSchema["runName"],
		contentType: "application/json", 
      	type:"PUT",
      	processData:false,
      	data: JSON.stringify(window.runSchema),
      	success: function(data){
      		console.log(data);
      	},
      	error: function(err){
      		console.log(err);
      	},
      	complete: function(xhr, status){
      		$("#success").empty();
      		switch (xhr.status){
      			case 204:
      				$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-error");
        		 	$("#alertMsg").addClass("alert-success");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"reload()\">&times;</button>");
					$("#alertMsg").append("<h4>Success!!</h4> Run Updated successfully!!");
					$("#alertMsg").css("display", "block");
					break;
				case 400:
					$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-success");
        		 	$("#alertMsg").addClass("alert-error");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"returnToPage()\">&times;</button>");
					$("#alertMsg").append("<h4>Error!!</h4> Invalid options!!");
					$("#alertMsg").css("display", "block");
					break;
				default :
					$("#alertMsg").empty();
  	                $("#alertMsg").removeClass("alert-success");
        		 	$("#alertMsg").addClass("alert-error");
        			$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"returnToPage()\">&times;</button>");
					$("#alertMsg").append("<h4>Error!!</h4> Run Update failed!!");
					$("#alertMsg").css("display", "block");
			}
      	}
	})
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
			window.existingBus = data;
		},
		error: function(){
			console.log("Error in getting businessUnits");
		},
		complete: function(xhr, status){
			switch(xhr.status){
				case 200 : 
					break;
				default :
					window.existingBus = {};
			}
		}
	});
}

function showUI(){
	$(".col-wrap").removeAttr('style');
	$("#json").css('display','none');
	window.selectedView='ui';
}

function showJson(){
	$("#json").removeAttr('style');
	$(".col-wrap").css('display','none');
	window.selectedView='json';
	if(window.models["jsonView"]["firstview"]){
		window.models.jsonView.instance = new runJsonViewModel();
		window.models["jsonView"]["firstview"]=false;
		ko.applyBindings(window.models.jsonView.instance, $("#json")[0]);
	} else {
		window.models.jsonView.instance.run(window.runSchema);
	}
}

function syntaxHighlight(json) {
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
        var cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });
}

function returnToPage(){
	//location.reload();
	$("#alertBox").append("<div id=\"alertMsg\" class=\"alert\" style=\"display: none\"></div>");
}

function execRun(){
	executeRun(window.runSchema.runName);
}

function goToUpdate(){
	window.location = "/updaterun.html?&runName=" + window.runSchema.runName;
}


