function getListedRuns(){
	var runsUrl = "/loader-server/runs"
	$(document).ready(function(e) {
        $.ajax({url: runsUrl,
			contentType: "application/json", 
			dataType:"json",
			type:"GET",
			success: function(runs) {
				var insertHtml = "<div id=\"cssmenu\"><ul>";
				for(var k=0; k<runs.length; k++){
					insertHtml = insertHtml + "<li><a id=\"" + runs[k] + "\" href='#' class=\"display\"><span>" + runs[k] + 
					"</span></a><ul><li><a id=\"" + runs[k]+ "execute\" href='#' class=\"execute\"><span>Execute</span></a></li>" + 
					"<li><a id=\"" + runs[k]+ "update\" href='#' class=\"update\"><span>Update</span></a></li>" +
					"<li><a id=\"" + runs[k]+ "delete\" href='#' class=\"delete\"><span>Delete</span></a></li>" +
					"<li><a id=\"" + runs[k]+ "clone\" href='#' class=\"clone\"><span>Clone</span></a></li>" +
					"</ul></li>";
					//$("#runsList").append("<tr style=\"border:1px solid black\"><td><button class=\"editbtn\">" + runs[k] + "</button></td></tr>");
				} 
				insertHtml = insertHtml + "</ul></div>";
				console.log("appending", insertHtml);
				$("#runsList").append(insertHtml);
				$('#cssmenu a').click(function(e){
				 	e.preventDefault();
				 	console.log("u selected anchor");
				 	var id=e.target.id;
				 	var myclass = $(this).attr("class");
				 	switch(myclass){
				 		case 'display':
				 			console.log("I am in display");
				 			break;
				 		case 'execute' :
				 			id = id.substring(0,id.indexOf("execute"));
				 			console.log(id);
				 			createJob(id);
				 			break;
				 		case 'delete' :
				 			id = id.substring(0,id.indexOf("delete"));
				 			console.log(id);
				 			deleteRun(id);
				 			break;
				 		case 'update' :
				 			id = id.substring(0,id.indexOf("update"));
				 			window.location = "/updateRun.html?&runName=" + id;
				 			break;
				 		case 'clone' :
				 			id = id.substring(0,id.indexOf("clone"));
				 			cloneRun(id);
				 			break;
				 	}
				})
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

function deleteRun(runName){
	var runUrl = "/loader-server/runs/" + runName;
	$.ajax({
		url:runUrl,
		contentType:"application/json",
		type:"DELETE",
		success: function(data){

		},
		error: function(data){

		},
		complete: function(xhr, status){
			if(xhr.status == 204){
				$("#success").append("<p>Run Deleted, Successfully!!</p>");
				$("#success").dialog({
					height:100,
					width:100,
					dialogClass:"dialogClass",
					position:{ my: "center", at: "center", of: window },
					close: function(){
						location.reload();
					}
				});
			} else {
				$("#success").append("<p>Run Deletion Failed!!</p>");
				$("#success").dialog({
					height:100,
					width:100,
					dialogClass:"dialogClass",
					position:{ my: "center", at: "center", of: window },
					close: function(){
						location.reload();
					}
				});
			}
		}
	})

}

function cloneRun(runName){
	$.ajax({
		url: "/loader-server/runs/"+runName,
		contentType: "application/json", 
		dataType: "json",
		type: "GET",
		success:function(runJson){
			window.runSchema = runJson;
		},
		error:function(err){
			console.log(err);
		},
		complete: function(xhr,status){
			if(xhr.status==200){
				$("#cloneRun").dialog({
					//autoOpen:false,
					height:150,
					width:200,
					position:{ my: "center", at: "center", of: window },
					//modal:true,
					dialogClass: "promptClass",
					buttons:{
						Clone:function(){
							var newRunName = $("#newRunName").val();
							$(this).dialog("close");
							if(typeof newRunName != 'undefined') {
								window.runSchema["runName"] = newRunName;
								$.ajax({
									url: "/loader-server/runs",
									contentType: "application/json",
									dataType: "json",
									type: "POST",
									processData:false,
      								data: JSON.stringify(window.runSchema),
									success: function(data){
										console.log("cloned the run");
									},
									error: function(err){
										console.log("error in cloning the run");
									},
									complete: function(xhr, status){
										//$(this).dialog("close");
										if (xhr.status == 201){
											$("#success").append("<p>Run Cloned Successfully!!</p>");
											$("#success").dialog({
												height:100,
												width:100,
												dialogClass:"dialogClass",
												position:{ my: "center", at: "center", of: window },
												close: function(){
													location.reload();
												}
											});
										} else {
											$("#success").append("<p>Run Cloning Failed!!</p>");
											$("#success").dialog({
												height:100,
												width:100,
												dialogClass:"dialogClass",
												position:{ my: "center", at: "center", of: window },
												close: function(){
													location.reload();
												}
											});
										}
									}
								});
							}
						},
						Cancel: function(){
							$(this).dialog( "close" );
						}
					}
				});
			} else {
				$("#success").append("<p>Run Cloning Failed!!</p>");
				$("#success").dialog({
					height:100,
					width:100,
					dialogClass:"dialogClass",
					position:{ my: "center", at: "center", of: window },
					close: function(){
						location.reload();
					}
				});
			}
		}

	})
}













