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
	}
}

function renderRunPage(metadata){
	var insertHtml = "<div id=\"runSchema\" class=\"runSchema\"><label><strong>Run Name</strong>:</label>" + 
            "<input type=\"text\" id=\"runName\" value=\"" + window.runSchema.runName + "\" class=\"bigInput\"/></div>";
    insertHtml = insertHtml + "<br/><br/><br/><br/>" + "<div id=\"runSchemaButton\">" + 
    		"<button id=\"updateRun\" onClick=\"updateRun()\">Update</button>" + 
    		"<button id=\"addLoadPart\" onClick=\"addLoadPart()\">Add LoadPart</button></div>";
    $("#displayArea").empty();
    $("#displayArea").append(insertHtml);
    console.log("selecting", "#node_" + window.runSchema.runName);
    $("#runTree").bind("reselect.jstree", function(){
    	$("#runTree").jstree("select_node","#node_" + window.runSchema.runName);
    }); 
}

function updateRun(){
	window.runSchema.runName = $("#runName").val();
	createTree(window.runSchema);
	renderDisplayArea('run',{});
}

function addLoadPart(){
	var loadPart = {"name":"loadPart" + window.runSchema.loadParts.length,
					"agents":1,
					"load":{"groups" : new Array()}
					};
	window.runSchema.loadParts.push(loadPart);
	createTree(window.runSchema);
	renderDisplayArea('loadPart', {"loadPartIndex": window.runSchema.loadParts.length-1} );
}

function renderLoadPartPage(metadata){
	var insertHtml =  "<div id=\"loadPart\" class=\"loadPart\"><label><strong>LoadPart Name</strong>:</label>" + 
			"<input type=\"text\" id=\"loadPartName\" value=\"" + window.runSchema.loadParts[metadata["loadPartIndex"]]["name"] + "\" class=\"bigInput\" />" + 
			"</br></br><label><strong>Agents</strong>:</label><input id=\"agents\" type=\"text\" value=\"" + window.runSchema.loadParts[metadata["loadPartIndex"]]["agents"] + "\" class=\"bigInput\"/></div></br></br></br>";
	insertHtml = insertHtml + "<div id=\"loadPartButton\">" + 
		"<button id=\"addGroup\" onClick=\"addGroup()\">Add Group</button>" +
		"<button id=\"updateLoadPart\" onClick=\"updateLoadPart()\">Update</button>" + 
		"<button id=\"deleteLoadPart\" onClick=\"deleteLoadPart()\">Delete</button></div>";
	$("#displayArea").empty();
    $("#displayArea").append(insertHtml);
    console.log("selecting", "#node_" + window.runSchema.loadParts[metadata["loadPartIndex"]]["name"]);
    $("#runTree").bind("reselect.jstree", function(){
    	$("#runTree").jstree("select_node","#node_" + window.runSchema.loadParts[metadata["loadPartIndex"]]["name"]);
    }); 
}

function updateLoadPart(){
	var loadPartData = window.selectedElementData;
	var loadPart = window.runSchema["loadParts"][loadPartData["loadPartIndex"]];
	loadPart["name"] = $("#loadPartName").val();
	loadPart["agents"] = $("#agents").val();
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
        "warmUpTime":-1,
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
	var insertHtml = "<div class=\"groupDetails\"><div id=\"groupName\" class=\"groupName\"><label><strong>Group Name</strong>:</label>" + 
			"<input type=\"text\" id=\"groupNameText\" value=\"" + grp["name"] + "\" class=\"bigInput\" /></div><br/></br></br>" +
			"<div class=\"groupConfig\"><label><strong>Group Start Delay</strong>:</label>" + 
			"<input type=\"text\" id=\"groupStartDelay\" value=\"" + grp["groupStartDelay"] + "\" class=\"smallInput\"/>" +
			"<label><strong>Thread Start Delay</strong>:</label>" + 
			"<input type=\"text\" id=\"threadStartDelay\" value=\"" + grp["threadStartDelay"] + "\" class=\"smallInput\"/><br/>" +
			"</div></br></br><div class=\"groupConfig\"><label><strong>ThroughPut</strong>:</label>" + 
			"<input type=\"text\" id=\"throughput\" value=\"" + grp["throughput"] + "\" class=\"smallInput\"/>&nbsp;&nbsp;&nbsp" +
			"<label><strong>Repeats</strong>:</label>" + 
			"<input type=\"text\" id=\"repeats\" value=\"" + grp["repeats"] + "\" class=\"smallInput\"/><br/>" +
			"</div></br></br><div class=\"groupConfig\"><label><strong>Duration</strong>(ms):</label>" + 
			"<input type=\"text\" id=\"duration\" value=\"" + grp["duration"] + "\" class=\"smallInput\"/>&nbsp;&nbsp;&nbsp" +
			"<label><strong>Threads</strong>:</label>" + 
			"<input type=\"text\" id=\"threads\" value=\"" + grp["threads"] + "\" class=\"smallInput\" /><br/>" +
			"</div></br></br><div class=\"groupConfig\"><label><strong>Warm Up Time:</strong></label>" + 
			"<input type=\"text\" id=\"warmUpTime\" value=\"" + grp["warmUpTime"] + "\" class=\"smallInput\"/>&nbsp;&nbsp;&nbsp" +
			"<label><strong>Warm Up Repeats</strong>:</label>" + 
			"<input type=\"text\" id=\"warmUpRepeats\" value=\"" + grp["warmUpRepeats"] + "\" class=\"smallInput\"/><br/>" +
			"</div></br></br><div id=\"multiSelect\" class=\"multiSelect\"><label><strong>Depends On</strong>:</label><br/></br>" + 
			"<select multiple=\"multiple\" name=\"groupList\" id=\"groupList\">";
				$.each(window.runSchema.loadParts[metadata["loadPartIndex"]]["load"]["groups"], function(index, gr){
					if (index != metadata["groupIndex"]) {
						if (window.runSchema.loadParts[metadata["loadPartIndex"]]["load"]["groups"][metadata["groupIndex"]]["dependOnGroups"].indexOf(gr["name"])>-1){
							console.log("already in array");
							insertHtml = insertHtml + "<option value=\"" + gr["name"] + "\" selected>" + gr["name"] + "</option>";
						} else {
							insertHtml = insertHtml + "<option value=\"" + gr["name"] + "\">" + gr["name"] + "</option>";
						}
					}
				});
    insertHtml = insertHtml + "</select></div><br/><br/>";

	insertHtml = insertHtml + "<div class=\"groupDetailsButton\">" + 
		"<button id=\"updateGroup\" onClick=\"updateGroup()\">Update</button>" + 
		"<button id=\"addFunction\" onClick=\"addFunction()\">Add Function</button>" + 
		"<button id=\"addTimer\" onClick=\"addTimer()\">Add Timer</button>" +
		"<button id=\"deleteGroup\" onClick=\"deleteGroup()\">Delete</button></div></div>";
	//console.log(insertHtml);
	$("#displayArea").empty();
    $("#displayArea").append(insertHtml);
    $("#groupList").multiSelect();
    console.log("selecting", "#node_" + metadata["loadPartIndex"] + "_" + grp["name"]);
    $("#runTree").bind("reselect.jstree", function(){
    	$("#runTree").jstree("select_node","#node_" + metadata["loadPartIndex"] + "_" + grp["name"]);
    }); 
    
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
	grp["warmUpTime"] = $("#warmUpTime").val();
	grp["warmUpRepeats"] = $("#warmUpRepeats").val();
	//if ($("#dependsOn").is(":checked")) {
		console.log("groupList", $("#groupList").val());
		grp["dependOnGroups"] = $("#groupList").val();
	//}
	window.runSchema.loadParts[grpData["loadPartIndex"]]["load"]["groups"][grpData["groupIndex"]]=grp;
	createTree(window.runSchema);
	renderDisplayArea('group', window.selectedElementData);

}

function deleteGroup(){
	var grpData = window.selectedElementData;
	//var grp = window.runSchema.loadParts[grpData["loadPartIndex"]]["load"]["groups"][grpData["groupIndex"]];
	window.runSchema.loadParts[grpData["loadPartIndex"]]["load"]["groups"].splice(grpData["groupIndex"],1);
	createTree(window.runSchema);
	renderDisplayArea('loadPart', window.selectedElementData);
}

function addFunction(){
	console.log("inside");
	var grpData = window.selectedElementData;
	var funct = {
		"functionalityName":"function" + window.runSchema.loadParts[grpData["loadPartIndex"]]["load"]["groups"][grpData["groupIndex"]]["functions"].length,
		"functionClass": "noclass",
		"dumpData":"false",
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
	var insertHtml =  "<div id=\"functionPage\"><div class=\"functions\"><label><strong>Function Name</strong>:</label>" + 
		"<input type=\"text\" id=\"funcName\" value=\"" + func["functionalityName"] + "\" class=\"bigInput\"/><br/></br></br>" +
		"<label><strong>Function Class</strong>:</label>" + 
		"<select id=\"functionList\" name=\"functionList\" onChange=\"getFunctionParameters()\" class=\"selectOption\"><option value=\"noclass\">Choose Function</option>";
		$.each(window.availableFunctions, function(index, f){
			if (f==func["functionClass"]) insertHtml = insertHtml + "<option value=\"" + f + "\" selected>" + f + "</option>";
			else insertHtml = insertHtml + "<option value=\"" + f + "\">" + f + "</option>";
		});
		insertHtml = insertHtml + "</select></br></br></br>";
		if (func["dumpData"]=="true"){
			insertHtml = insertHtml + "<div id=\"dumpData\"><label><strong>DumpData</strong>:</label><select id=\"dumpDataSel\" class=\"selectOption\"><option value=\"false\">false</option>" +
				"<option value=\"true\" selected>true</option></select></div><br/><br/><div id=\"ips\"></div></div><div class=\"functionsButton\">" + 
				"<button id=\"updateFunction\" onClick=\"updateFunction()\">Update</button>" + 
				"<button id=\"deleteFunction\" onClick=\"deleteFunction()\">Delete</button></div></div>"	;
		} else {
			insertHtml = insertHtml + "<div id=\"dumpData\"><label><strong>DumpData</strong>:</label><select id=\"dumpDataSel\" class=\"selectOption\"><option value=\"false\" selected>false</option>" +
				"<option value=\"true\">true</option></select></div><br/><br/><div id=\"ips\"></div></div><div class=\"functionsButton\">" + 
				"<button id=\"updateFunction\" onClick=\"updateFunction()\">Update</button>	" + 
				"<button id=\"deleteFunction\" onClick=\"deleteFunction()\">Delete</button></div></div>";
		}
	$("#displayArea").empty();
    $("#displayArea").append(insertHtml);
    getFunctionParameters();
    $("#runTree").bind("reselect.jstree", function(){
    	$("#runTree").jstree("select_node","#node_" + metadata["loadPartIndex"] + "_" + metadata["groupIndex"]+ "_" + func["functionalityName"]);
    }); 
}

function updateFunction(){
	var metadata = window.selectedElementData;
	console.log("meta is", metadata);
	var func = window.runSchema.loadParts[metadata["loadPartIndex"]]["load"]["groups"][metadata["groupIndex"]]["functions"][metadata["functionIndex"]];
	console.log("function is", func);
	func["functionalityName"] = $("#funcName").val();
	func["functionClass"] = $("#functionList").val();
	func["dumpData"] = $("#dumpDataSel").val();
	$.each(window.inputParams, function(key, value){
		func["params"][key] = $("#"+key).val();
	});
	window.runSchema.loadParts[metadata["loadPartIndex"]]["load"]["groups"][metadata["groupIndex"]]["functions"][metadata["functionIndex"]]=func;
	console.log("creating tree");
	createTree(window.runSchema);
	console.log("done creating tree, rendering");
	renderDisplayArea('function', window.selectedElementData);
	console.log("rendering done");
}

function deleteFunction(){
	var metadata = window.selectedElementData;
	window.runSchema.loadParts[metadata["loadPartIndex"]]["load"]["groups"][metadata["groupIndex"]]["functions"].splice(metadata["functionIndex"],1);
	createTree(window.runSchema);
	renderDisplayArea('group', window.selectedElementData);
}

function getFunctionParameters(){
	var metadata = window.selectedElementData;
	var func = window.runSchema.loadParts[metadata["loadPartIndex"]]["load"]["groups"][metadata["groupIndex"]]["functions"][metadata["functionIndex"]];
	console.log("func is", func);
	$("#ips").empty();
	var fName = $("#functionList").val();
	if (fName == "noclass") return;
	$.ajax({url: "/loader-server/functions/" + fName + "?classInfo=true",
      contentType: "application/json", 
      type:"GET",
      success: function(data) {
        var ip = data[0]["inputParameters"];
        window.inputParams = data[0]["inputParameters"];
        var insertHtml = "<table id=\"inputParameters\" width=\"100%\" align=\"left\">" +
						"<thead><tr><td colspan=\"2\"><strong>Input Parameters</strong><hr><br></td></tr></thead><tbody style=\"height:200px; overflow: scroll;\">";
		$.each(ip, function(k,v){
			console.log("v is",v);
			console.log("k is", k);
			console.log("func is", func);
			var redStar = "<span style=\"color:red\">*</span>";
			insertHtml = insertHtml + "<tr><td width=\"30%\"><b>" + v["name"];
			if(v["mandatory"]==true) insertHtml= insertHtml + redStar;
			var defaultVal = v["defaultValue"]?v["defaultValue"]:"";
			defaultVal=func["params"][v["name"]]?func["params"][v["name"]]:defaultVal;
			insertHtml = insertHtml + "</b></td><td width=\"70%\"><input type=\"text\" id=\"" + k + "\" value=\"" + 
			defaultVal + "\" onfocus=\"inputFocus(this)\" onblur=\"inputBlur(this)\" style=\"width:99%;color:#888;\" /></td></tr>"; 
		});
		insertHtml = insertHtml + "</tbody></table></br></br>";
		$("#ips").empty();
		$("#ips").append(insertHtml);
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
		"name":"monitor-" + window.runSchema.metricCollections.length,
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
	var insertHtml = "<div class=\"metrics\"><div class=\"metricsPage\"><label><strong>Agent IP</strong></label>" + 
		"<input type=\"text\" id=\"agent\" class=\"bigInput\" value=\"" + 
		metric["agent"] + "\"/></br></br><div id=\"resources\"></div></div></br></br></br>";
	insertHtml = insertHtml + "<div class=\"metricsPageButton\"><button id=\"updateFunction\" onClick=\"updateMetricCollection()\">Update</button>" +
		"<button id=\"updateFunction\" onClick=\"deleteMetricCollection()\">Delete</button></div></div>";
	$("#displayArea").empty();
    $("#displayArea").append(insertHtml);
    getResources(metric);
    $("#runTree").bind("reselect.jstree", function(){
    	$("#runTree").jstree("select_node","#node_" + metric["name"]);
    }); 
}

function renderMonitoringAgentsAddPage(metadata){
	var insertHtml = "<div class=\"metricCollection\"><button id=\"metricCollection\" onClick=\"addMetricCollection()\">Add Metrics Collection</button></div>";
	$("#displayArea").empty();
    $("#displayArea").append(insertHtml);
    $("#runTree").bind("reselect.jstree", function(){
    	$("#runTree").jstree("select_node","#node_monitoringAgents");
    });
}

function updateMetricCollection(){
	var metricData = window.selectedElementData;
	var metric = window.runSchema.metricCollections[metricData["metricCollectionIndex"]];
	metric["agent"] = $("#agent").val();
	metric["collectionInfo"]["resources"].length=0;
	$("#resources input:checked").each(function(){
		metric["collectionInfo"]["resources"].push($(this).attr('id'));
	})
	window.runSchema.metricCollections[metricData["metricCollectionIndex"]]=metric;
	createTree(window.runSchema);
	renderDisplayArea('metricCollection',window.selectedElementData);
}

function deleteMetricCollection(){
	window.runSchema.metricCollections.splice(window.selectedElementData["metricCollectionIndex"],1);
	createTree(window.runSchema);
	renderDisplayArea('monitoringAgents',{});
}

function getResources(metric){
	var insertHtml = "";
	$.ajax({
		"url":"http://" + metric["agent"] + ":7777/monitoring-service/resources",
		"contentType": "application/json", 
      	"type":"GET",
      	"async":false,
      	success: function(data){
      		window.resourceData = data;
      	},
      	error: function(err){
      		console.log("in error",err);
      		//insertHtml = insertHtml + "<p>No monitoringServer running on given IP</p>";
      	}, 
      	complete: function(xhr, status){
      		if(xhr.status==200){
      			var cnt=1;
      			$.each(window.resourceData, function(index, resource){
      				console.log("testing", metric["collectionInfo"]["resources"]);
      				if(metric["collectionInfo"]["resources"].indexOf(resource)> -1){
      					console.log("found", resource);
      					insertHtml = insertHtml + "<input type=\"checkbox\" id=\"" + resource + 
      					"\" checked=\"true\"/>&nbsp;&nbsp;<label><strong>" + resource + "</strong></label>";
      				} else {
      					insertHtml = insertHtml + "<input type=\"checkbox\" id=\"" + resource + 
      					"\" />&nbsp;&nbsp;<label><strong>" + resource + "</strong></label>";
      				}
      				if(cnt%3==0)insertHtml = insertHtml + "</br></br>";
      				cnt = cnt+1;
      			});
      		} else {
      			insertHtml =insertHtml + "<p>No monitoringServer running on given IP</p>";
      		}
      	}

	});
	$("#resources").empty();
	$("#resources").append(insertHtml);
}

function createTree(data){ 
	console.log(data);
	$("#runTree").jstree({
			"json_data" : {
				"data" : [{
					"attr":{"id" : "node_" + data["runName"]},
					"data" : data["runName"], 
					"metadata" : { "name" : data["runName"], "nodeType" : "run"},    
					"children" : getChildren(data)
                	}],
            	"progressive_render" : true,
			},
			"plugins" : [ "themes", "json_data", "ui", "cookies"],
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
		console.log(data.rslt.obj.data());
		renderDisplayArea(data.rslt.obj.data("nodeType"), data.rslt.obj.data());
		window.selectedElementData = data.rslt.obj.data();
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
	if(typeof lps == 'undefined') return [{"attr":{"id": "node_monitoringAgents"}, "data": "MonitoringAgents", "metadata": {"nodeType":"monitoringAgents"}, "children": mas}]; 
	lps.push({"attr":{"id": "node_monitoringAgents"}, "data": "MonitoringAgents", "metadata": {"nodeType":"monitoringAgents"}, "children": mas});
	return lps;
}

function getLoadParts(data){
	var loadPartsName = new Array();
	var loadParts = data["loadParts"];
	if (loadParts.length==0) return undefined;
	for (var k=0; k<loadParts.length; k++){
		var agents = loadParts[k]["agents"];
		loadPartsName[k]={"attr" :{"id": "node_" + loadParts[k]["name"]},"data": loadParts[k]["name"], "metadata" : {"nodeType":"loadPart", "loadPartIndex": k}, "children": getGroupList(loadParts[k], k)};
	}
	console.log("loadpartsname:", loadPartsName);
	return loadPartsName;
}

function getMetricCollectors(data){
	var metricsAgents = new Array();
	var metricsCollectors = data["metricCollections"];
	if(metricsCollectors.length==0) return undefined;
	for( var k=0; k<metricsCollectors.length;k++){
		metricsAgents[k]={"attr":{"id": "node_" + metricsCollectors[k]["name"]}, "data": metricsCollectors[k]["name"], "metadata": {"nodeType":"metricCollection", "metricCollectionIndex":k}};
	}
	return metricsAgents;
}

function getGroupList(data, loadPartIndex){
	var groupName = new Array();
	var groups = data["load"]["groups"];
	if (groups.length==0) return undefined;
	for(var i=0;i<groups.length;i++){
		groupName[i]={ "attr":{"id": "node_" + loadPartIndex + "_" + groups[i]["name"]},"data": groups[i]["name"], "metadata" :{"nodeType":"group", "loadPartIndex": loadPartIndex, "groupIndex": i} ,"children" : getFunctionList(groups[i], loadPartIndex ,i)};
	}
	console.log("returning from getGroupList");
	return groupName;
}

function getFunctionList(group, loadPartIndex, groupIndex){
	var functionName = new Array();
	var functions = group["functions"];
	if (functions.length==0) return undefined;
	for(var i=0;i<functions.length; i++){
		functionName[i]={"attr":{"id":"node_" + loadPartIndex + "_" + groupIndex + "_" + functions[i]["functionalityName"]}, "metadata": {"nodeType":"function", "loadPartIndex":loadPartIndex, "groupIndex": groupIndex, "functionIndex":i}, "data": functions[i]["functionalityName"]};
	}
	console.log("returning from getFunctionList");
	return functionName;
}

function createRun(){
	var classes = new Array();
	$.each(window.runSchema["loadParts"], function(lpIndex, loadPart){
		$.each(loadPart["load"]["groups"], function(grpIndex, group){
			$.each(group["functions"], function(funcIndex, funct){
				classes.push(funct["functionClass"]);
			});
		}); 
	});
	window.runSchema["classes"] = classes;
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
      		$("#success").empty();
      		switch (xhr.status){
      			case 201:
      				$("#success").append("<p>Run Created, Successfully!!</p>");
					$("#success").dialog();
					break;
				case 409:
					$("#success").append("<p>RunName Conflict, Please Change RunName!!</p>");
					$("#success").dialog();
					break;
				case 400:
					$("#success").append("<p>Run Schema looks bad, Some JSON Parsing error!!</p>");
					$("#success").dialog();
					break;
				default :
					$("#success").append("<p>Something Went wrong, Can not create Run!!</p>");
					$("#success").dialog();
			}
      	}
	})
}
