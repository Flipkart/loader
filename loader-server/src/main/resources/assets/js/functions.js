function getAllFunctions(){
	$.ajax({
		url: "/loader-server/functions",
		contentType: "application/json", 
		type:"GET",
		async: false,
		success: function(data){
			console.log("creating the view");
			ko.applyBindings(new functionsViewModel(data));
		},
		error: function(e){
			console.log("Error");
		},
		complete: function(xhr, status){
			console.log(status);
		}
	})
}

var functionsViewModel = function(functionsList){
	var self = this;
	self.functions = ko.observableArray(functionsList);
	self.functionsPerPkg = ko.computed(function(){
		var map = {};
		var availableFunctions= self.functions();
		$.each(availableFunctions, function(index, func){
			var pkgName = func.substring(0,func.lastIndexOf("."));
			if(map[pkgName]==undefined)
				map[pkgName] = new Array();
			map[pkgName].push({"functionName":func.substring(func.lastIndexOf(".")+1,func.length)});
		});
		return map;
	})
	self.packages = ko.computed(function(){
		var packages = [];
		var availableFunctions= self.functions();
		var tmpPkgNames = [];
		$.each(availableFunctions, function(index, func){
			if(tmpPkgNames.indexOf(func.substring(0,func.lastIndexOf(".")))==-1){
				packages.push({"packageName":func.substring(0,func.lastIndexOf("."))});
				tmpPkgNames.push(func.substring(0,func.lastIndexOf(".")));
			}
		});
		return packages;
	});
	self.pkgFuncPair = ko.observable({"selPkgName": self.packages()[0]["packageName"], 
		"selFuncName": self.functionsPerPkg()[self.packages()[0]["packageName"]][0]["functionName"]})
	//self.selectedPackage = ko.observable(self.packages()[0]["packageName"]);
	self.packageFunctions = ko.computed(function(){
		var selPkg = self.pkgFuncPair()["selPkgName"];
		return self.functionsPerPkg()[selPkg];
	});
	//self.selectedFunction = ko.observable(self.packageFunctions()[0]["functionName"]);
	self.functionDetails = ko.computed(function(){
		var funcName = self.pkgFuncPair()["selFuncName"];
		var pkgName = self.pkgFuncPair()["selPkgName"];
		var clsName = pkgName + "." + funcName;
		var funcDets = {}
		$.ajax({
			url: "/loader-server/functions/" + clsName + "?classInfo=true",
			contentType: "application/json", 
			type:"GET",
			async: false,
			success: function(data){
				funcDets["function"] = data[0]["function"];	
				funcDets["description"] = data[0]["description"][0];
				funcDets["inputParameters"]= new Array();
				funcDets["outputParameters"] = new Array();
				$.each(data[0]["inputParameters"], function(k, v){
					funcDets["inputParameters"].push(v);
				});
				$.each(data[0]["outputParameters"], function(k, v){
					funcDets["outputParameters"].push(v);
				});
			},
			error: function(e){
				console.log("Error");
			},
			complete: function(xhr, status){
				console.log(status);
			}
		});
		console.log("returning", funcDets);
		return funcDets;
	});
	self.onPkgClick = function(pkg, event){
		$("#packages .row-fluid").css("background-color","white");
		$("#packages .row-fluid a").css("color","#0088cc");
		$(event.target).parent().css("background-color","#0088cc");
		$(event.target).css("color","white");
		self.pkgFuncPair({"selPkgName": pkg["packageName"], 
			"selFuncName": self.functionsPerPkg()[pkg["packageName"]][0]["functionName"]});
		$("#functions .row-fluid a").first().click();

	}
	self.onFunctionClick = function(func, event){
		$("#functions .row-fluid").css("background-color","white");
		$("#functions .row-fluid a").css("color","#0088cc");
		$(event.target).parent().css("background-color","#0088cc");
		$(event.target).css("color","white");
		self.pkgFuncPair({"selPkgName": self.pkgFuncPair()["selPkgName"], "selFuncName": func["functionName"]});
	}
}