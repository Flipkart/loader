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
	}
}

function renderRunPage(metadata){
	var insertHtml = "<label>Run Name:</label>&nbsp;&nbsp;" + 
            "<input type=\"text\" id=\"runName\" value=\"" + window.runSchema.runName + "\" class=\"bigInput\"/>";
    insertHtml = insertHtml + "<br/><br/><br/><br/>" + 
    		"<button id=\"updateRun\" onClick=\"updateRun()\">Update</button>" + 
    		"<button id=\"loadPart\" onClick=\"addLoadPart()\">Add LoadPart</button>" + 
    		"<button id=\"metricCollection\" onClick=\"addMetricCollection()\">Add Metrics Collection</button>" + 
    		"<button id=\"onDemandMetrics\" onClick=\"addOnDemandMetrics()\">Add OnDemand Metrics</button>";
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
	var insertHtml =  "<label>LoadPart Name: &nbsp;&nbsp;</label>" + 
			"<input type=\"text\" id=\"loadPartName\" value=\"" + window.runSchema.loadParts[metadata["loadPartIndex"]]["name"] + "\" class=\"bigInput\" />" + 
			"</br><label>Agents:</label>&nbsp;&nbsp;<input id=\"agents\" type=\"text\" value=\"" + window.runSchema.loadParts[metadata["loadPartIndex"]]["agents"] + "\" class=\"bigInput\"/><br/><br/>";
	insertHtml = insertHtml + 
		"<button id=\"addGroup\" onClick=\"addGroup()\">Add Group</button>" +
		"<button id=\"updateLoadPart\" onClick=\"updateLoadPart()\">Update</button>";
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
	var insertHtml = "<label>Group Name: &nbsp;&nbsp;</label>" + 
			"<input type=\"text\" id=\"groupName\" value=\"" + grp["name"] + "\" class=\"bigInput\" /><br/>" +
			"<label>Group Start Delay: &nbsp;&nbsp;</label>" + 
			"<input type=\"text\" id=\"groupStartDelay\" value=\"" + grp["groupStartDelay"] + "\" class=\"smallInput\"/>&nbsp;&nbsp;&nbsp" +
			"<label>Thread Start Delay: &nbsp;&nbsp;</label>" + 
			"<input type=\"text\" id=\"threadStartDelay\" value=\"" + grp["threadStartDelay"] + "\" class=\"smallInput\"/><br/>" +
			"<label>ThroughPut: &nbsp;&nbsp;</label>" + 
			"<input type=\"text\" id=\"throughput\" value=\"" + grp["throughput"] + "\" class=\"smallInput\"/>&nbsp;&nbsp;&nbsp" +
			"<label>Repeats: &nbsp;&nbsp;</label>" + 
			"<input type=\"text\" id=\"repeats\" value=\"" + grp["repeats"] + "\" class=\"smallInput\"/><br/>" +
			"<label>Duration(ms): &nbsp;&nbsp;</label>" + 
			"<input type=\"text\" id=\"duration\" value=\"" + grp["duration"] + "\" class=\"smallInput\"/>&nbsp;&nbsp;&nbsp" +
			"<label>Threads: &nbsp;&nbsp;</label>" + 
			"<input type=\"text\" id=\"threads\" value=\"" + grp["threads"] + "\" class=\"smallInput\" /><br/>" +
			"<label>Warm Up Time: &nbsp;&nbsp;</label>" + 
			"<input type=\"text\" id=\"warmUpTime\" value=\"" + grp["warmUpTime"] + "\" class=\"smallInput\"/>&nbsp;&nbsp;&nbsp" +
			"<label>Warm Up Repeats: &nbsp;&nbsp;</label>" + 
			"<input type=\"text\" id=\"warmUpRepeats\" value=\"" + grp["warmUpRepeats"] + "\" class=\"smallInput\"/><br/>" +
			"<label>Depends On: &nbsp;&nbsp;</label><br/></br>" + 
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
    insertHtml = insertHtml + "</select><br/><br/>";

	insertHtml = insertHtml + "<button id=\"updateGroup\" onClick=\"updateGroup()\">Update</button>" + 
		"<button id=\"addFunction\" onClick=\"addFunction()\">Add Function</button>" + 
		"<button id=\"addTimer\" onClick=\"addTimer()\">Add Timer</button>" +
		"<button id=\"addThreadResources\" onClick=\"addThreadResources()\">Add ThreadResources</button>" +
		"<button id=\"addCustomTimer\" onClick=\"addCustomTimer()\">Add Custom Timer</button>" + 
		"<button id=\"addCustomCounter\" onClick=\"addCustomCounter()\">Add Custom Counter</button>";
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
	grp["name"] = $("#groupName").val();
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
	var insertHtml =  "<label>Function Name:</label>&nbsp;&nbsp;" + 
		"<input type=\"text\" id=\"funcName\" value=\"" + func["functionalityName"] + "\" class=\"bigInput\"/><br/></br>" +
		"<label>Function Class:</label>&nbsp;&nbsp;" + 
		"<select id=\"functionList\" name=\"functionList\" onChange=\"getFunctionParameters()\"><option value=\"noclass\">Choose Function</option>";
		$.each(window.availableFunctions, function(index, f){
			if (f==func["functionClass"]) insertHtml = insertHtml + "<option value=\"" + f + "\" selected>" + f + "</option>";
			else insertHtml = insertHtml + "<option value=\"" + f + "\">" + f + "</option>";
		});
		insertHtml = insertHtml + "</select><br/></br>";
		if (func["dumpData"]=="true"){
			insertHtml = insertHtml + "<label>DumpData:</label><select id=\"dumpData\"><option value=\"false\">false</option>" +
				"<option value=\"true\" selected>true</option></select><br/><br/><div id=\"ips\"></div>" + 
				"<button id=\"updateFunction\" onClick=\"updateFunction()\">Update</button>";
		} else {
			insertHtml = insertHtml + "<label>DumpData:</label><select id=\"dumpData\"><option value=\"false\" selected>false</option>" +
				"<option value=\"true\">true</option></select><br/><br/><div id=\"ips\"></div>" + 
				"<button id=\"updateFunction\" onClick=\"updateFunction()\">Update</button>	";
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
	func["dumpData"] = $("#dumpData").val();
	$.each(window.inputParams, function(key, value){
		func["params"][key] = $("#"+key).val();
	});
	window.runSchema.loadParts[metadata["loadPartIndex"]]["load"]["groups"][metadata["groupIndex"]]["functions"][metadata["functionIndex"]]=func;
	createTree(window.runSchema);
	renderDisplayArea('function', window.selectedElementData);
}

function getFunctionParameters(){
	$("#ips").empty();
	var fName = $("#functionList").val();
	if (fName == "noclass") return;
	$.ajax({url: "/loader-server/functions/" + fName + "?classInfo=true",
      contentType: "application/json", 
      type:"GET",
      success: function(data) {
        var ip = data[0]["inputParameters"];
        window.inputParams = data[0]["inputParameters"];
        var insertHtml = "<table id=\"inputParameters\" width=\"80%\" align=\"center\">" +
						"<thead><tr><td colspan=\"2\"><strong>Input Parameters</strong><hr><br></td></tr></thead><tbody style=\"height:200px; overflow: scroll;\">";
		$.each(ip, function(k,v){
			console.log("v is",v);
			var redStar = "<span style=\"color:red\">*</span>";
			insertHtml = insertHtml + "<tr><td width=\"30%\"><b>" + v["name"];
			if(v["mandatory"]==true) insertHtml= insertHtml + redStar;
			var defaultVal = v["defaultValue"]?v["defaultValue"]:"";
			insertHtml = insertHtml + "</b></td><td width=\"70%\"><input type=\"text\" id=\"" + k + "\" value=\"Default: " + 
			defaultVal + "\" onfocus=\"inputFocus(this)\" onblur=\"inputBlur(this)\" style=\"width:99%;color:#888;\" /></td></tr>"; 
		});
		insertHtml = insertHtml + "</tbody></table>";
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

// function showSelectGroups(){
// 	if ($("#dependsOn").is(":checked")) {
// 		$("#existingGrps").removeAttr("hidden");
// 		$("#groupList").multiSelect();
// 	} else {
// 		$("#existingGrps").attr("hidden","true");
// 	}
// }

function createTree(data){ 
	console.log(data);
	$("#runTree").jstree({
			"json_data" : {
				"data" : [{
					"attr":{"id" : "node_" + data["runName"]},
					"data" : data["runName"], 
					"metadata" : { "name" : data["runName"], "nodeType" : "run"},    
					"children" : getLoadParts(data)
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
