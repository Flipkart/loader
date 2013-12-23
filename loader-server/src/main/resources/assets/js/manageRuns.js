function executeRun(runName){
	var jobData = "{\"runName\" : \"" + runName + "\"}";
	var jobPostUrl = "/loader-server/jobs"
	$.ajax({
		url:jobPostUrl,
		type:"post",
		contentType:"application/json",
		data: jobData,
		success: function(data){
			window.jobId = data["jobId"];
			},
		error: function(){
			console.log("Job Submission Failed");
			},
		complete: function(xhr, status){
			if(xhr.status==200){
				console.log("Job Created, Successfully");
				window.location.href = "/jobdetails.html?&jobId=" + window.jobId;
			} else {
				console.log("Job creation failed!!");
				$("#alertMsg").empty();
				$("#alertMsg").removeClass("alert-success");
				$("#alertMsg").addClass("alert-error");
				$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button>");
				$("#alertMsg").append("<h4>Error!!</h4> Job creation failed!!");
				$("#alertMsg").css("display", "block");
			}
		}
	});
}

function cloneRun(existingRunName){
 	$.ajax({
    	url: "/loader-server/runs/"+existingRunName,
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
          	$("#cloneRunButt").on('click', function(){
          		console.log($("#newRunName").val()); 
          		window.runSchema["runName"] = $("#newRunName").val();
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
  	                  	if (xhr.status == 201){
  	                  		//location.reload();
  	                  		$("#alertMsg").empty();
  	                  		$("#alertMsg").removeClass("alert-error");
        					        $("#alertMsg").addClass("alert-success");
        					        $("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"reload()\">&times;</button>");
							            $("#alertMsg").append("<h4>Success!!</h4> Run Cloned successfully!!");
							            $("#alertMsg").css("display", "block");
                      	} else {
                  			   $("#alertMsg").empty();
                  			   $("#alertMsg").removeClass("alert-success");
        					         $("#alertMsg").addClass("alert-error");
        					         $("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button>");
							             $("#alertMsg").append("<h4>Error!!</h4> Run Cloning failed!!");
							             $("#alertMsg").css("display", "block");
                    	  }
                	}
            	});
          	});
          	$("#cloneRun").modal();  
          } else {
          		$("#alertMsg").empty();
          		$("#alertMsg").removeClass("alert-success");
        		$("#alertMsg").addClass("alert-error");
        		$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button>");
				$("#alertMsg").append("<h4>Error!!</h4> Run Cloning failed!!");
				$("#alertMsg").css("display", "block");
          }
        }
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
        console.log("Run Deleted!!");
        //location.reload();
        $("#alertMsg").empty();
        $("#alertMsg").removeClass("alert-error");
        $("#alertMsg").addClass("alert-success");
        $("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"reload()\">&times;</button>");
		    $("#alertMsg").append("<h4>Success!!</h4> Run deleted successfully!!");
	     	$("#alertMsg").css("display", "block");
        setTimeout(reload(),3000);
      } else {
		    console.log("Run deletion failed!!");  
    		$("#alertMsg").empty();
    		$("#alertMsg").removeClass("alert-success");
    		$("#alertMsg").addClass("alert-error");
    		$("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button>");
    		$("#alertMsg").append("<h4>Error!!</h4> Run Deletion failed!!");
    		$("#alertMsg").css("display", "block");      
      }
    }
  })
}

function reload(){
	location.reload();
}

