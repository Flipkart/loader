function getFunctions(){
	$.ajax({
		url:"/loader-server/functions",
		contentType: "application/json", 
		dataType:"json",
		type:"GET",
		success: function(functions){
			console.log(functions);
			var tree = createNode(functions);
			console.log(tree);
			createPackageTree({"name":"/","nodes":tree});
		},
		error: function(err){
			console.log("Error");
		},
		complete: function(xhr, status){
			console.log("Complete");
		}

	});
}

function createNode(data, parentName){
	if(data.length==0) return [];
	var retData = new Array();
	var intrdata = {};
	for(var k=0;k<data.length;k++){
		if(data[k].indexOf('.')==-1){
			intrdata[data[k]] = [];
			console.log("its a leaf:" + data[k]);
			continue;
		}
		var key = data[k].substring(0,data[k].indexOf('.'));
		console.log("key:" + key);
		if(!intrdata[key]) intrdata[key] = new Array();
		intrdata[key].push(data[k].substring(data[k].indexOf('.')+1, data[k].length));
	}
	console.log("data:" + intrdata);
	
	$.each(intrdata, function(k,v){
		var fullName = !parentName?k:parentName + "." + k;
		retData.push({"name":k, "fullName": fullName, "nodes": createNode(v,fullName)});
	});
	return retData;
}

function createPackageTree(tree){
	console.log("Tree:",tree);
	$("#display").jstree({
		"json_data":{
				"data":[{
					"data": tree["name"],
					//"children": [{"data":"first", "metadata":{"random":"1234"}},{"data":"second", "metadata":{"random":"12345"}}],
					"children": getChildren(tree["nodes"]),
					"metadata": {"nodeType":"node", "fullName":tree["fullName"]}
					}],
				"progressive_render" : true
		},
		"plugins" : [ "themes", "json_data", "ui"]
	}).bind("select_node.jstree", function(event, data){
		if(data.rslt.obj.data("nodeType")!=="leaf") return;
		var fullName = data.rslt.obj.data("fullName");
		$.ajax({
			url: "/loader-server/functions/" + fullName + "?classInfo=true",
			contentType: "application/json", 
			dataType:"json",
			type:"GET",
			success: function(functionData){
				var functionDetails = functionData[0];
				var inputParameters = functionDetails["inputParameters"];
				var outputParameters = functionDetails["outputParameters"];
				var desc = functionDetails["description"];
				var descTable = "<table id=\"description\" width=\"80%\" align=\"center\">" + 
					"<thead style=\"text-align:left\"><tr><td><strong>Description</strong><hr><br></td></tr></thead>" + 
					"<tbody style=\"height:100px; overflow: scroll;\"><tr><td><p>" + functionDetails["description"] + "</p></td></tr></tbody></table><hr>";
				var redStar = "<span style=\"color:red\">*</span>";
				var insertHtml = "<table id=\"inputParameters\" width=\"80%\" align=\"center\">" +
						"<thead><tr><td colspan=\"2\"><strong>Input Parameters</strong><hr><br></td></tr></thead><tbody style=\"height:200px; overflow: scroll;\">";
				$.each(inputParameters, function(k,v){
					console.log("v is",v);
					insertHtml = insertHtml + "<tr><td width=\"30%\"><b>" + v["name"];
					if(v["mandatory"]==true) insertHtml= insertHtml + redStar;
					var defaultVal = v["defaultValue"]?v["defaultValue"]:"";
					insertHtml = insertHtml + "</b></td><td width=\"70%\"><input type=\"text\" value=\"Default: " + 
						defaultVal + "\" onfocus=\"inputFocus(this)\" onblur=\"inputBlur(this)\" style=\"width:99%;color:#888;\" /></td></tr>"; 
				});
				insertHtml = insertHtml + "</tbody></table><hr>";
				var outputParamTable = "<table id=\"outputParameter\" width=\"80%\" align=\"center\">" + 
					"<thead><tr><td colspan=\"2\"><strong>Output Parameters</strong><hr><br></td></tr></thead><tbody style=\"height:100px; overflow: scroll;\">";
				$.each(outputParameters, function(k,v){
					outputParamTable = outputParamTable + "<tr><td width=\"30%\"><b>" + v["name"] + "</b></td>" +
						"<td><label>" + v["description"] + "</label></td></tr>"
				});
				outputParamTable = outputParamTable + "</tbody></table>";
				console.log(descTable + insertHtml + outputParamTable);
				$("#details").empty();
				$("#details").append(descTable + insertHtml + outputParamTable);
			},
			error: function(err){
				console.log(err);
			},
			complete: function(xhr, status){
				console.log("Completed!");
			}

		});
	});
}

function getChildren(subTree){
	console.log("subTree:" + subTree);
	var children = new Array();
	for( var index=0; index < subTree.length; index++){
		if(subTree[index]["nodes"].length==0){
			children.push({"data": subTree[index]["name"], "metadata": {"nodeType": "leaf", "fullName": subTree[index]["fullName"]}});
		} else {
			children.push({"data": subTree[index]["name"], "metadata": {"nodeType":"node", "fullName": subTree[index]["fullName"]}, "children": getChildren(subTree[index]["nodes"])});
		}
	}
	console.log("children:" ,children);
	return children;
}