function createTree(data){ 
	$("#demo").jstree({
			"json_data" : {
				"data" : [{
					"data" : data["runName"], 
					"metadata" : { "nodeType" : "workflow", "level":0},    
					"children" : getGroupList(data)
                	}],
            	"progressive_render" : true
			},
			"plugins" : [ "themes", "json_data", "ui"]
	}).bind("select_node.jstree", function(event, data){
		//alert(data.rslt.obj.data("nodeType"));
		console.log(window.data);
		switch(data.rslt.obj.data("nodeType")){
			case 'function' : 
				var grpName = data.rslt.obj.data("groupName");
				console.log($("#"+grpName));
				var index = 0;
				for ( var i=0; i<window.data["load"]["groups"].length; i++){
					if (window.data["load"]["groups"][i]["name"] == grpName) {
						index=i;
						break;
					}
				}
				console.log(window.data["load"]["groups"][index]);
				var funct = window.data["load"]["groups"][index]["functions"][data.rslt.obj.data("index")];
				//var disp = "";
				$("#display").empty();
				for( k in funct ){
					$("#display").append("<tr><td><label>"+ k + ":</label></td><td><input type=\"text\" readonly=\"readonly\" value=" +  funct[k] + " size=\"60\"></input></td></tr>");
				}
				//$("#display").append(disp);
				break;
			case 'group' :
				var group = window.data["load"]["groups"][data.rslt.obj.data("index")];
				$("#display").empty();
				for( k in group ){
					if (typeof group[k] === 'object') continue;
					$("#display").append("<tr><td><label>"+ k + ":</label></td><td><input type=\"text\" readonly=\"readonly\" value=" + group[k] + " size=\"60\"></input></td></tr>");
				}
		}   
	});
}

function getLoadParts(data){
	var loadPartsName = new Array();
	var loadParts = data["loadParts"];
	for ( var k=0; k<loadParts.length; k++){
		loadPartsName[i]={"data": loadParts[k]["name"], "children": getGroupList(loadParts[k])};
	}

}

function getGroupList(data){
	var groupName = new Array();
	var groups = data["load"]["groups"];
	//console.log(groups);
	for(var i=0;i<groups.length;i++){
		groupName[i]={ "data": groups[i]["name"], "metadata" : { "nodeType" : "group", "level": 1, "loadParts" : "name", "index":i},"children" : getFunctionList(data, groups[i]), "attr":{"id":groups[i]["name"]}};
	}
	return groupName;
}

function getFunctionList(data, group){
	var functionName = new Array();
	var functions = group["functions"];
	//console.log(group);
	for(var i=0;i<functions.length; i++){
		functionName[i]={"metadata": {"nodeType": "function", "level" : 2, "groupName": group["name"], "index":i}, "data": functions[i]["functionalityName"], "attr":{"id":functions[i]["functionalityName"], "index":i}};
	}
	return functionName;
}