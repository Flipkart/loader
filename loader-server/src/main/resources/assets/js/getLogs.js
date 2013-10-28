var logsViewModel = function(){
  var self = this;
  self.fetchAgents = function(){
    var agents = getQueryParams("agents").split(",");
    return agents;
  }
  self.agentsOption = ko.observableArray(self.fetchAgents());
  self.linenumbers = ko.observable(1000);
}

function getLogs(){
  var jobId = getQueryParams("jobId");
  var lines = $("#linenumbers").val();
  var textToGrep = $("#grepText").val();
  console.log("lines", lines);
  console.log("textToGrep", textToGrep);
  var agent = $("#agents").val();
  if (agent=="127.0.0.1") agent=window.location.hostname;
  if (typeof lines == 'undefined') lines=1000;
  if (typeof textToGrep == 'undefined') textToGrep = "";
  $.ajax({
    url: "http://" + agent +":8888/loader-agent/jobs/" + jobId + "/log?&lines=" + lines + "&grep=" + textToGrep,
        type: "GET",
        contentType:"text/plain",
        //dataType:"jsonp",
        success: function(data){
          $("#logsArea").val(data);
        }
  });
}

function getQueryParams(sParam){
  var queryString = window.location.search.substring(2);
  var queryParams = queryString.split('&');
  for (var i = 0; i < queryParams.length; i++){
        var sParameterName = queryParams[i].split('=');
    //console.log(sParameterName[0]);
        if (sParameterName[0] == sParam){
        //console.log('matched');
            return sParameterName[1];
        }
    }
    return undefined;
}