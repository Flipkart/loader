
function returnTimerGraphs(url, grpIndex, timerIndex){
	var chart1, chart2;
	formatTime = d3.time.format("%H:%M"),
 	formatMinutes = function(d) { return formatTime(new Date(d)); };
	nv.addGraph(function() {
  		chart1 = nv.models.lineChart();
  		chart2 = nv.models.lineChart();
  		//chart1.x(function(d,i) { return i })

		chart1.xAxis
  			//.ticks(d3.time.minutes, 5)
  			.tickFormat(function(d) { return d3.time.format('%H:%M')(new Date(d)); });

  		chart1.yAxis
      		.axisLabel('Time (ms)')
      		.tickFormat(d3.format(',.2f'));

      	//chart2.x(function(d,i) { return i })


  		chart2.xAxis
		  	//.ticks(d3.time.minutes, 5)
  			.tickFormat(function(d) { return d3.time.format('%H:%M')(new Date(d)); });

  		chart2.yAxis
      		.axisLabel('Time (ms)')
      		.tickFormat(d3.format(',.2f'));
      	var statsQueues = {
      						"dumpMean": new Array(), 
      						"dumpThroughPut": new Array(),
      						"overAllMean": new Array(),
      						"overAllThroughPut": new Array(),
      						"fiftieth": new Array(),
      						"seventyFifth": new Array(),
      						"ninetieth": new Array(),
      						"nintyFifth": new Array(),
      						"nintyEighth": new Array(),
      						"nintyNinth": new Array()
      					};
        window.stats[grpIndex][timerIndex]={"statsqueues": statsQueues};
        var metrices = metrics(true, url, grpIndex, timerIndex);
        window.stats[grpIndex][timerIndex]["metrices"]= metrices;
        plotGraphs(url, grpIndex, timerIndex);

        nv.utils.windowResize(chart1.update);
  		nv.utils.windowResize(chart2.update);
  		chart1.dispatch.on('stateChange', function(e) { nv.log('New State:', JSON.stringify(e)); });
  		chart2.dispatch.on('stateChange', function(e) { nv.log('New State:', JSON.stringify(e)); });

  		return chart1

});

function plotGraphs(url, grpIndex, timerIndex){
	chart1Name = "#chart" + grpIndex + timerIndex + "1";
	chart2Name = "#chart" + grpIndex + timerIndex + "2";
	// console.log("Lets see chart1Name:" + chart1Name);
	console.log("chart I am plotting", window.stats[grpIndex][timerIndex]["metrices"]["chart1"]);
	d3.select(chart1Name + " svg")
      //.datum([]) //for testing noData
      	.datum(window.stats[grpIndex][timerIndex]["metrices"]["chart1"])
    	.transition().duration(500)
      	.call(chart1);

  	d3.select(chart2Name + " svg")
  		.datum(window.stats[grpIndex][timerIndex]["metrices"]["chart2"])
		.transition().duration(500)
		.call(chart2);

	cleanQueues();
		//console.log(dumpMean;
	// var metrices = metrics(false, url, grpIndex, timerIndex);
 //    window.stats[grpIndex][timerIndex]["metrices"]= metrices;
	//setInterval(function(){plotGraphs(url, grpIndex, timerIndex);}, 10000);
}

function metrics(initialize, url, grpIndex, timerIndex) {
 	 //var dumpMean, dumpThroughPut, overAllMean, overAllThroughPut, fiftieth, seventyFifth,
	  //ninetieth, nintyFifth, nintyEighth, nintyNinth;

	  // Getting the initial data
	  if(initialize) {
	  	initializeMetrics(url, grpIndex, timerIndex);
	  	//populateMetrics(url, grpIndex, timerIndex);
	  }	
	  return { "chart1":[
    					{values: window.stats[grpIndex][timerIndex]["statsqueues"]["dumpMean"],key: "Dump Mean",color: "#ff7f0e"},
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["overAllMean"],key: "Over All Mean",color: "#a02c2c",},
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["fiftieth"],key: "50Th%",color: "#B40404"},
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["seventyFifth"],key: "75Th%",color: "#0B610B"	},
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["ninetieth"],key: "90Th%",color: "#0B0B61"},
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["nintyFifth"],key: "95Th%",color: "#FE9A2E"},
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["nintyEighth"],key: "98Th%",color: "#0E0D0D"}
	  					],
			   "chart2":[
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["dumpThroughPut"],key: "Dump Throughput",color: "#2ca02c"},
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["overAllThroughPut"],key: "Over All ThroughPut",color: "#DF01D7",}
						]
			 };
}
function initializeMetrics(httpUrl, grpIndex, timerIndex){
	var parse =d3.time.format("%H:%S").parse
	$.ajax({
		  	url: httpUrl,
		  	type: "GET",
		  	contentType:"text/plain",
		  	async:false,
		  	success: function(data){
			  var lines = data.split('\n');
			  //var firstLine = $.parseJSON(lines[0]);
			  for( var i=0; i<lines.length-1; i++){
			  	if (lines[i]=="") continue;
			  	try {
			  		var dataJson = $.parseJSON(lines[i]);
					time = new Date(dataJson["time"]);
					window.stats[grpIndex][timerIndex]["statsqueues"]["dumpMean"].push({x: new Date(dataJson["time"]),y: dataJson["dumpMean"]});
					window.stats[grpIndex][timerIndex]["statsqueues"]["dumpThroughPut"].push({x: new Date(dataJson["time"]),y: dataJson["dumpThroughput"]});
					window.stats[grpIndex][timerIndex]["statsqueues"]["overAllMean"].push({x: new Date(dataJson["time"]), y: dataJson["overallMean"]});
					window.stats[grpIndex][timerIndex]["statsqueues"]["overAllThroughPut"].push({x: new Date(dataJson["time"]), y: dataJson["overAllThroughput"]});
					window.stats[grpIndex][timerIndex]["statsqueues"]["fiftieth"].push({x: new Date(dataJson["time"]), y: dataJson["fiftieth"]});
					window.stats[grpIndex][timerIndex]["statsqueues"]["seventyFifth"].push({x: new Date(dataJson["time"]), y: dataJson["seventyFifth"]});
					window.stats[grpIndex][timerIndex]["statsqueues"]["ninetieth"].push({x: new Date(dataJson["time"]), y: dataJson["ninetieth"]});
					window.stats[grpIndex][timerIndex]["statsqueues"]["nintyFifth"].push({x: new Date(dataJson["time"]), y: dataJson["ninetyFifth"]});
					window.stats[grpIndex][timerIndex]["statsqueues"]["nintyEighth"].push({x: new Date(dataJson["time"]), y: dataJson["ninetyEight"]});
					window.stats[grpIndex][timerIndex]["statsqueues"]["nintyNinth"].push({x: new Date(dataJson["time"]), y: dataJson["ninetyNinth"]});
				} catch (err){
					console.log("Err in parsing:",lines[i] );
				}
			  }
		  },
		  error: function(data){
			  console.log("NO Data Found");
		  },
		  complete: function(){
			  console.log("Initial Data fetch Complete");
		  }
	  	});
}

function cleanQueues(){
	window.stats[grpIndex][timerIndex]["statsqueues"]["dumpMean"].length = 0;
	window.stats[grpIndex][timerIndex]["statsqueues"]["dumpThroughPut"].length=0;
	window.stats[grpIndex][timerIndex]["statsqueues"]["overAllMean"].length=0;
	window.stats[grpIndex][timerIndex]["statsqueues"]["overAllThroughPut"].length=0;
	window.stats[grpIndex][timerIndex]["statsqueues"]["fiftieth"].length=0;
	window.stats[grpIndex][timerIndex]["statsqueues"]["seventyFifth"].length=0;
	window.stats[grpIndex][timerIndex]["statsqueues"]["ninetieth"].length=0;
	window.stats[grpIndex][timerIndex]["statsqueues"]["nintyFifth"].length=0;
	window.stats[grpIndex][timerIndex]["statsqueues"]["nintyEighth"].length=0;
	window.stats[grpIndex][timerIndex]["statsqueues"]["nintyNinth"].length=0;
}
}

