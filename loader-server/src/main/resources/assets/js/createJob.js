function createJob(runName){
	var jobData = "{\"runName\" : \"" + runName + "\"}";
	var jobPostUrl = "/loader-server/jobs"
	$.ajax({
		url:jobPostUrl,
		type:"post",
		contentType:"application/json",
		data: jobData,
		success: function(data){
			console.log("Job Submitted");
			},
		error: function(){
			console.log("Job Submission Failed");
			},
		complete: function(xhr, status){
			if(xhr.status==200){
				console.log("Job Created, Successfully");
				$("#success").append("<p>Job Created, Successfully!!</p>");
				$("#success").dialog({
					height:100,
					width:100,
					dialogClass:"dialogClass",
					position:{ my: "center", at: "center", of: window },
					close: function(){
						var resp =xhr.responseText;
						var respJson = $.parseJSON(resp);
						window.location="/job_details.html?&jobid=" + respJson["jobId"];
					}
				});
			} else {
				$("#success").append("<p>Job Creation, Failed!!</p>");
				$("#success").dialog({
					height:100,
					width:100,
					dialogClass:"dialogClass",
					position:{ my: "center", at: "center", of: window },
					close: function(){
					}
				});
			}
		}
	});
}