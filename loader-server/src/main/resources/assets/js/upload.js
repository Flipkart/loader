function initializeUpload(){
    $('#jarupload').fileupload({
        url: "/loader-server/resourceTypes/udfLibs",
        dataType: 'json',
        autoUpload: false,
        acceptFileTypes: /(\.|\/)(jar)$/i,
    }).on('fileuploadadd', function (e, data) {
        $("#uploadJarButton").on('click', function () {
            var $this = $(this),
            data = $this.data();
            data.submit().always(function () {
            //$this.remove();
            });
        });
        $("#uploadJarButton").data(data);
        $("#jarLoc").append("<p>" + data.files[0].name + "</p>");
        $("#jarProgress").removeAttr("hidden");
    }).on('fileuploadprogressall', function (e, data) {
        var progress = parseInt(data.loaded / data.total * 100, 10);
        $('#jarProgress .bar').css(
            'width',
            progress + '%'
        );
    }).on('fileuploaddone', function (e, data) {
        //Alerting all the classes that got added
        $("#jarAlert").removeAttr("hidden");
        var insertHtml = "<br/><h4><strong>Successfully added following UDFS</strong></h4>";
        $.each(data["result"], function(key, value){
            insertHtml = insertHtml + "<p>" + key + "</p>";
        });

        //removing the jar name
        $("#jarLoc").empty();
        //clearing the progress bar
        $('#jarProgress .bar').css(
            'width',
            0 + '%'
        );
        //$("#jarProgress").attr("hidden","true");
        //Adding the message
        $("#jarMsg").empty();
        $("#jarMsg").append(insertHtml);
    }).on('fileuploadfail', function (e, data) {
        console.log("data in fileuploadfail", data);
    });

    $('#resupload').fileupload({
        url: "/loader-server/resourceTypes/inputFiles",
        dataType: 'json',
        autoUpload: false,
        acceptFileTypes: /(\.|\/)(jar)$/i,
    }).on('fileuploadadd', function (e, data) {
        $("#uploadResButton").on('click', function () {
            var $this = $(this),
            data = $this.data();
            data.submit().always(function () {
            //$this.remove();
            });
        });
        $("#uploadResButton").data(data);
        $("#resLoc").append("<p>" + data.files[0].name + "</p>");
        $("#resProgress").removeAttr("hidden");
    }).on('fileuploadprogressall', function (e, data) {
        var progress = parseInt(data.loaded / data.total * 100, 10);
        $('#resProgress .bar').css(
            'width',
            progress + '%'
        );
    }).on('fileuploaddone', function (e, data) {
        //Alerting all the classes that got added
        $("#resAlert").removeAttr("hidden");
        var insertHtml = "";
        $.each(data["result"], function(key, value){
            insertHtml = insertHtml + "<h4><strong>" + value + "</strong></h4>";
        });

        //removing the jar name
        $("#resLoc").empty();
        //clearing the progress bar
        $('#resProgress .bar').css(
            'width',
            0 + '%'
        );
        //$("#jarProgress").attr("hidden","true");
        //Adding the message
        $("#resMsg").empty();
        $("#resMsg").append(insertHtml);
    }).on('fileuploadfail', function (e, data) {
        console.log("data in fileuploadfail", data);
    }).bind('fileuploadsubmit', function (e, data) {
    // The example input, doesn't have to be part of the upload form:
    var input = $('#resourceName');
    data.formData = {"resourceName": input.val()};
    if (!data.formData.resourceName) {
      input.focus();
      $("#resAlert").removeAttr("hidden");
      $("#resMsg").empty();
      $("#resMsg").append("<h4>Please Enter a Name for your resource!!</h4>")
      return false;
    }
    });
}