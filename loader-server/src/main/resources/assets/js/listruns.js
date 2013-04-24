function getListedRuns(){
	var runsUrl = "/loader-server/runs"
	$(document).ready(function(e) {
        $.ajax({url: runsUrl,
			contentType: "application/json", 
			dataType:"json",
			type:"GET",
			success: function(runs) {
				var insertHtml = "<tr><td><div id=\"cssmenu\"><ul>";
				for(var k=0; k<runs.length; k++){
					insertHtml = insertHtml + "<li><a id=\"" + runs[k] + "\" href='#'><span>" + runs[k] + "</span></a></li>";
					//$("#runsList").append("<tr style=\"border:1px solid black\"><td><button class=\"editbtn\">" + runs[k] + "</button></td></tr>");
				} 
				insertHtml = insertHtml + "</ul></div></td></tr>";
				$("#runsList").append(insertHtml);
				$('#cssmenu a').click(function(e){
					e.preventDefault();
					//alert("u clicked" + e.target.id);
					console.log(e.target.id);
					var id=e.target.id;
					//console.log(id);
					var workflowUrl = "/loader-server/runs/" + id;
					console.log(workflowUrl)
					returnTree(workflowUrl);
				});
				},
			error: function(e){
				console.log("Error");
				},
			complete: function(xhr, status){
				console.log(status);
				}
				});
			console.log("Done with ajax");
    });
}