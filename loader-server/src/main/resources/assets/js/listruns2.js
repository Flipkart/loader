function getListedRuns(){
	var runsUrl = "/loader-server/runs";
	$(document).ready(function(e) {
        $.ajax({url: runsUrl,
			contentType: "application/json", 
			dataType:"json",
			type:"GET",
			success: function(runs) {
				window.listOfRuns = runs;
				var insertHtml = "";
				for(var k=0; k<runs.length; k++){
					insertHtml = insertHtml +  "<tr><td><h3 class=\"collapsible\" id=\"section" + k + "\">" + runs[k] +  
					"<span></span><h3>" + 
					"<div id=\"section" + k + "Container\" class=\"container\">";
					insertHtml = insertHtml + "<div class=\"content\"><table width=\"60%\" border=\"1\" align=\"center\" bordercolor=\"#000000\" style=\"min-height:300px\" style=\"\"><tr style=\"margin-top:10px\"><td width=\"30%\" bordercolor=\"#000000\"><div id=\"demo" + k + "\"></div></td><td><div id=\"display" + k + "\" align=\"center\"></div></td></tr></table></div></div></td></tr>";
				} 
				$("#workflows").append(insertHtml);
			},
			error: function(e){
				console.log("Error");
			},
			complete: function(xhr, status){
				console.log(window.listOfRuns);
				$('.container').slideUp();
				$('.collapsible').click(function(){
					var id = $(this).attr("id");
					console.log(id);
					$("#"+id+"Container").slideToggle();
				});
				for(var k=0; k<window.listOfRuns.length; k++){
					var workFlowName = window.listOfRuns[k];
					var workFlowUrl = "/loader-server/runs/" + workFlowName;
					returnTree(workFlowUrl, k);
				}
			}
		});
			console.log("Done with ajax");
    });
}