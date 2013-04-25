function createJob(runName){
	var jobData = "{\"runName\" : \"" + runName + "\"}";
	var jobPostUrl = "/loader-server/jobs"
	$.ajax({
		url:jobPostUrl,
		type:"post",
		contentType:"application/json",
		data: jobData,
		success: function(){
			console.log("Job Submitted");},
		error: function(){
			console.log("Job Submission Failed");},
		complete: function(){
			console.log("Job Created, Successfully");
			$("#success").dialog("Job Created!!");
		}
	});
}