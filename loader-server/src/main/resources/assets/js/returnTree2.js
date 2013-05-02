function createTree(data){ 
	console.log(data);
	$("#demo").jstree({
			"json_data" : {
				"data" : [{
					"data" : data["runName"], 
					"metadata" : { "name" : data["runName"], "displaydata": data["runName"]},    
					"children" : getLoadParts(data)
                	}],
            	"progressive_render" : true
			},
			"plugins" : [ "themes", "json_data", "ui"]
	}).bind("select_node.jstree", function(event, data){
		var display = data.rslt.obj.data("displaydata");
		$("#display").empty();
		for ( k in display ){
			$("#display").append("<tr><td><label>"+ k + ":</label></td><td><input type=\"text\" readonly=\"readonly\" value=" + display[k] + " size=\"60\"></input></td></tr>");
		}
	});
}

function getLoadParts(data){
	var loadPartsName = new Array();
	var loadParts = data["loadParts"];
	for (var k=0; k<loadParts.length; k++){
		var agents = loadParts[k]["agents"];
		var metaData = {"Name" : loadParts[k]["name"], "agents": agents};
		loadPartsName[k]={"data": loadParts[k]["name"], "metadata" : {"displaydata" :metaData, "name": "test"}, "children": getGroupList(loadParts[k])};
	}
	console.log("loadpartsname:", loadPartsName);
	return loadPartsName;
}

function getGroupList(data){
	if(!data["load"]["groups"]) return;
	var groupName = new Array();
	var groups = data["load"]["groups"];
	for(var i=0;i<groups.length;i++){
		var metaData = {};
		for(k in groups[i]){
			if(typeof groups[i][k] === 'object') continue;
			metaData[k] = groups[i][k];
		}
		groupName[i]={ "data": groups[i]["name"], "metadata" :{"displaydata" :metaData, "name": "test"} ,"children" : getFunctionList(groups[i])};
	}
	console.log("returning from getGroupList");
	return groupName;
}

function getFunctionList(group){
	var functionName = new Array();
	var functions = group["functions"];
	for(var i=0;i<functions.length; i++){
		var metaData = {};
		for (k in functions[i]){
			if(typeof functions[i][k] === 'object') continue;
			metaData[k] = functions[i][k];
		}
		functionName[i]={"metadata": {"displaydata" :metaData, "name": "test"}, "data": functions[i]["functionalityName"]};
	}
	console.log("returning from getFunctionList");
	return functionName;
}
