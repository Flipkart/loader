function getQueryParams(sParam){
	var queryString = window.location.search.substring(2);
	var queryParams = queryString.split('&');
	for (var i = 0; i < queryParams.length; i++){
        var sParameterName = queryParams[i].split('=');
		console.log(sParameterName[0]);
        if (sParameterName[0] == sParam){
				//console.log('matched');
            return sParameterName[1];
        }
    }
}
function getJobDetails(){
	var jobUrl = "/loader-server/jobs/" + getQueryParams('jobid');   //replace this with previous one
	console.log(jobUrl);
        $.ajax({url: jobUrl,
			contentType: "application/json", 
			dataType:"json",
			type:"GET",
			success: function(data){
				$.each(data, function(key,value){
					//console.log(key);
					switch(key){
						case "runName":
							$("#jobname").attr("value",value);
							console.log(key);
							break;
						case "jobId":
							$("#jobid").attr("value",value);
							break;
						case "jobStatus":
							$("#status").attr("value",value);
							break;
					    case "agentsJobStatus":
                            $.each(value, function(agentIp,agentsJobStatus){
                                agentHealth = agentsJobStatus["inStress"] == true ? "InStress(" + JSON.stringify(agentsJobStatus["healthStatus"]) + ")" : "OK";
                                rowColor = agentsJobStatus["inStress"] == true ? "red" : "#00FF00";
                                $("#agents").append("<tr bgcolor="+rowColor+"><td>"+agentIp+"</td><td>"+agentsJobStatus["job_status"]+"</td><td>"+agentHealth+"</td></tr>");
                            });
					        break;

					}
				});
				window["jobAgents"] = data["agentsJobStatus"];
				console.log("in getJobDetails", window);
				if ($("#status").attr("value")=="RUNNING"){
					$("#play").attr("disabled","disabled");
					$("#pause").removeAttr("disabled");
					$("#stop").removeAttr("disabled");
				} else {
					$("#play").removeAttr("disabled");
					$("#pause").attr("disabled","disabled");
					$("#stop").attr("disabled","disabled");
				}
			},
			error: function(e){
				console.log("Error");
				},
			complete: function(xhr, status){
				console.log(status);
				}
		});
}
