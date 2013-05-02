function createRun(){
	window.completeRun = {};
	window.completeRun["runName"] = "unnamed";
	window.completeRun["loadParts"] = new Array();
	window.completeRun["onDemandMetricCollections"] = new Array();
	window.completeRun["metricCollections"] = new Array();
	var insertHtml = "<tr><td><strong><label>Run Name</label></strong>&nbsp;&nbsp<input type=\"text\" id=\"runName\"/></td></tr>";
	insertHtml = insertHtml + "<tr><td><button id=\"addLoadPart\" onClick=\"addLoadPart()\">Add LoadPart</button>&nbsp;&nbsp";
	insertHtml = insertHtml + "<button id=\"onDemandMetrics\">On Demand Metrics</button>&nbsp;&nbsp";
	insertHtml = insertHtml + "<button id=\"metrics\">Metrics</button></td></tr>";
	$("#displayDetails").empty();
	$("#displayDetails").append(insertHtml);
	createTree(window.completeRun);
}

function saveRunName(){
	window.completeRun["runName"] = $("#runName").val();
	createTree(window.completeRun);
}

function addLoadPart(){
	saveRunName();
	var index = window.completeRun["loadParts"].length
	var loadPart = {};
	loadPart["name"] = "loadPart" + index;
	loadPart["agents"] = "0";
	loadPart["load"] = {"groups": new Array()};
	window.completeRun["loadParts"].push(loadPart);
	var insertHtml = "<tr><td colspan=\"2\"><strong><label>LoadPart Name</label></strong>&nbsp;&nbsp<input type=\"text\" id=\"loadPartName\" /></td></tr>";
    insertHtml = insertHtml + "<tr><td><strong><label>Agents</label></strong>&nbsp;&nbsp<input type=\"text\" id=\"agents\"/></td></tr>";
    insertHtml = insertHtml + "<tr><td><button id=\"addGroup\" onClick=\"addGroup(" + index + ")\">Add Group</button></td></tr>"; 
    $("#displayDetails").empty();
	$("#displayDetails").append(insertHtml);
	createTree(window.completeRun);
}

function saveLoadPart(index){
	var loadPart = window.completeRun["loadParts"][index];
	loadPart["name"] = $("#loadPartName").val();
	loadPart["agents"] = $("#agents").val();
	window.completeRun["loadParts"][index] = loadPart;
	createTree(window.completeRun);
}

function addGroup(index){
	saveLoadPart(index);
	var group = {};
	group["name"] = "testGroup";
	group["groupStartDelay"] = "0";
	group["threadStartDelay"] = "0";
	group["throughput"] ="-1";
	group["repeats"]="0";
	group["duration"]="-1";
	group["threads"]="1";
	group["warmUpTime"]="-1";
	group["warmUpRepeats"]="-1";
	group["functions"] = new Array();
	group["dependOnGroups"] = new Array();
	group["params"]= new Array();
	group["timers"] = new Array();
	group["threadResources"]= new Array();
	group["customTimers"] = new Array();
	group["customCounters"] = new Array();
	window.completeRun["loadParts"][index]["load"]["groups"].push(group);
	displayGroupPage(index, window.completeRun["loadParts"][index]["load"]["groups"].length -1);
}


function displayGroupPage(loadPartIndex, groupIndex){
	var group = window.completeRun["loadParts"][loadPartIndex]["load"]["groups"][groupIndex];
	var insertHtml = "<tr><td colspan=\"2\"><strong><label>Group Name</label></strong>&nbsp;&nbsp<input type=\"text\" id=\"groupName\" value=\"" + group["name"] + "\"/></td></tr>";
	insertHtml = insertHtml + "<tr><td><strong><label>Group Start Delay</label></strong>&nbsp;&nbsp<input type=\"text\" id=\"groupStartDelay\" value=\"" + group["groupStartDelay"] + "\" /></td>";
	insertHtml = insertHtml + "<td><strong><label>Thread Start Delay</label></strong>&nbsp;&nbsp<input type=\"text\" id=\"threadStartDelay\" value=\"" + group["threadStartDelay"] + "\" /></td></tr>";
	insertHtml = insertHtml + "<tr><td><strong><label>Throughput</label></strong>&nbsp;&nbsp<input type=\"text\" id=\"throughput\" value=\"" + group["throughput"] + "\" /></td>";
	insertHtml = insertHtml + "<td><strong><label>Repeats</label></strong>&nbsp;&nbsp<input type=\"text\" id=\"repeats\" value=\"" + group["repeats"] + "\" /></td></tr>";
	insertHtml = insertHtml + "<tr><td><strong><label>Duration</label></strong>&nbsp;&nbsp<input type=\"text\" id=\"duration\" value=\"" + group["duration"] + "\" /></td>";
	insertHtml = insertHtml + "<td><strong><label>Threads</label></strong>&nbsp;&nbsp<input type=\"text\" id=\"threads\" value=\"" + group["threads"] + "\" /></td></tr>";
	insertHtml = insertHtml + "<tr><td><strong><label>Warmup Repeats</label></strong>&nbsp;&nbsp<input type=\"\" id=\"warmupRepeats\" value=\"" + group["warmUpRepeats"] + "\" /></td>";
	insertHtml = insertHtml + "<td><strong><label>Warmup Time</label></strong>&nbsp;&nbsp<input type=\"\" id=\"warmupTime\" value=\"" + group["warmUpTime"] + "\" /></td></tr>";
	insertHtml = insertHtml + "<tr><td><button id=\"addFunction\" onClick=\"addFunction(" + loadPartIndex + "," + groupIndex + "\")>Add Function</button>";
	$("#displayDetails").empty();
	$("#displayDetails").append(insertHtml);
	createTree(window.completeRun);
}








