function searchRuns(){
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

var searchRunsViewModel = function(){
	var self = this;
	self.selectedGroupName = ko.observable("default");
	self.selectedTeamName = ko.observable("default");
	self.fetchAvailableGroups = function(){
		var groups = [];
		$.each(window.existingRuns, function(k,v){
			groups.push(k);
		});
		return groups;
	}
	self.availableGroups = ko.observableArray(self.fetchAvailableGroups());
	self.availableTeams = ko.computed(function(){
		var grpName = self.selectedGroupName();
		var allTeams  = window.existingRuns[grpName]["teams"];
		var teams = [];
		$.each(allTeams, function(k, v){
			teams.push(k);
		})
		return teams;
	});
	self.startIndex = ko.observable("0");
	self.tableRows = ko.computed(function(){
		var runs = window.existingRuns[self.selectedGroupName()]["teams"][self.selectedTeamName()]["runs"];
		var rows = [];
		$.each(runs, function(index, run){
			rows.push(new searchRunRowViewModel(run, self.selectedGroupName(), self.selectedTeamName()));
		});
		return rows;
	});
	self.slicedTableRows = ko.computed(function(){
		return self.tableRows().slice(self.startIndex()*7, self.startIndex()*7+7>self.tableRows().length?self.tableRows().length:self.startIndex()*7+7);
	});
	self.noOfPages = ko.computed(function(){
		var gname = self.selectedGroupName();
		var tname = self.selectedTeamName();
		return Math.ceil(self.tableRows().length/7);
	});
	self.execute = function(data, even){
		console.log("execute", data, even);
		executeRun(data["runName"]);
	};
	self.update = function(data, even){
		console.log("update", data, even);
		window.location.href = "/updaterun.html?&runName=" + data["runName"];

	};
	self.clone = function(data, even){
		console.log("clone", data, even);
		cloneRun(data["runName"]);
	};
	self.del = function(data, even){
		console.log("del", data, even);
		$("#delRunButt").on('click', function(){
			console.log("going to delete ", data["runName"]);
			deleteRun(data["runName"]);
		})
		$("#deleteRun").modal();
		//deleteRun(data["runName"]);
	};
}	

var searchRunRowViewModel = function(rName, gName, tName){
	var self = this;
	self.runName = rName;
	self.updateUrl = "updaterun.html?&runName=" + rName;
	self.groupName = gName;
	self.teamName = tName;
}

ko.bindingHandlers.pager = {
	init: function(element, valueAccessor, allBindingsAccessor, viewModel){
		//console.log(element, valueAccessor, allBindingsAccessor, viewModel);
		var pages = viewModel.noOfPages();
		console.log(pages);
		var options = {
			currentPage : 1,
			totalPages : pages,
			onPageClicked: function(e, originalEvent, type, page){
				viewModel.startIndex(page-1);
				$('.dropdown-toggle').dropdown();
			}
		}
		//$("#pager").bootstrapPaginator(options);
		if(pages>0){
			$("#pager").bootstrapPaginator(options);
			$('.dropdown-toggle').dropdown();
		}
	},
	update: function(element, valueAccessor, allBindingsAccessor, viewModel){
		$("#pager").bootstrapPaginator('destroy');
		var pages = viewModel.noOfPages();
		var options = {
			currentPage : 1,
			totalPages : pages,
			onPageClicked: function(e, originalEvent, type, page){
				viewModel.startIndex(page-1);
				$('.dropdown-toggle').dropdown();
			}
		}
		if(pages>0){
			$("#pager").bootstrapPaginator(options);
			$('.dropdown-toggle').dropdown();
		}
	}
}


