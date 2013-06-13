
function returnTimerGraphs(url, grpIndex, timerIndex, chart1StartIndex, chart2StartIndex){
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
      	var metrices = {}
      	if (typeof window.stats[grpIndex][timerIndex] != 'undefined' || window.stats[grpIndex][timerIndex] != null){
      		metrices = metrics(false, url, grpIndex, timerIndex, chart1StartIndex, chart2StartIndex);
      		window.stats[grpIndex][timerIndex]["metrices"]= metrices;
      	} else {
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
        	metrices = metrics(true, url, grpIndex, timerIndex, 0, 0);
        	window.stats[grpIndex][timerIndex]["metrices"]= metrices;
        }
        plotGraphs(grpIndex, timerIndex);
        nv.utils.windowResize(chart1.update);
  		nv.utils.windowResize(chart2.update);
  		chart1.dispatch.on('stateChange', function(e) { nv.log('New State:', JSON.stringify(e)); });
  		chart2.dispatch.on('stateChange', function(e) { nv.log('New State:', JSON.stringify(e)); });
  		return chart1
    });

function plotGraphs(grpIndex, timerIndex){
	getCounters(window["counterUrls"][grpIndex][timerIndex], grpIndex, timerIndex);
	chart1Name = "#chart" + grpIndex + timerIndex + "1";
	chart2Name = "#chart" + grpIndex + timerIndex + "2";
	// console.log("Lets see chart1Name:" + chart1Name);
	//console.log("chart I am plotting", window.stats[grpIndex][timerIndex]["metrices"]["chart1"]);
	d3.select(chart1Name + " svg")
      	.datum(window.stats[grpIndex][timerIndex]["metrices"]["chart1"])
    	.transition().duration(500)
      	.call(chart1);
    d3.select(chart2Name + " svg")
  		.datum(window.stats[grpIndex][timerIndex]["metrices"]["chart2"])
		.transition().duration(500)
		.call(chart2);
}


function metrics(initialize, url, grpIndex, timerIndex, c1Index, c2Index) {
 	 //var dumpMean, dumpThroughPut, overAllMean, overAllThroughPut, fiftieth, seventyFifth,
	  //ninetieth, nintyFifth, nintyEighth, nintyNinth;

	  // Getting the initial data
	  if(initialize) {
	  	initializeMetrics(url, grpIndex, timerIndex);
	  	//populateMetrics(url, grpIndex, timerIndex);
	  }
	  var last1Index = c1Index+300< window.stats[grpIndex][timerIndex]["statsqueues"]["dumpMean"].length?c1Index+300:window.stats[grpIndex][timerIndex]["statsqueues"]["dumpMean"].length-5;
	  var last2Index = c2Index+300< window.stats[grpIndex][timerIndex]["statsqueues"]["dumpThroughPut"].length?c2Index+300:window.stats[grpIndex][timerIndex]["statsqueues"]["dumpThroughPut"].length-5;
	  console.log("last1Index", last1Index, "last2Index", last2Index);
	  return { "chart1":[
    					{values: window.stats[grpIndex][timerIndex]["statsqueues"]["dumpMean"].slice(c1Index, last1Index),key: "Dump Mean",color: "#ff7f0e"},
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["overAllMean"].slice(c1Index, last1Index),key: "Over All Mean",color: "#a02c2c",},
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["fiftieth"].slice(c1Index, last1Index),key: "50Th%",color: "#B40404"},
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["seventyFifth"].slice(c1Index, last1Index),key: "75Th%",color: "#0B610B"	},
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["ninetieth"].slice(c1Index, last1Index),key: "90Th%",color: "#0B0B61"},
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["nintyFifth"].slice(c1Index, last1Index),key: "95Th%",color: "#FE9A2E"},
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["nintyEighth"].slice(c1Index, last1Index),key: "98Th%",color: "#0E0D0D"}
	  					],
			   "chart2":[
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["dumpThroughPut"].slice(c2Index, last2Index),key: "Dump Throughput",color: "#2ca02c"},
						{values: window.stats[grpIndex][timerIndex]["statsqueues"]["overAllThroughPut"].slice(c2Index, last2Index),key: "Over All ThroughPut",color: "#DF01D7",}
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
			  window.sliderLength=lines.length-300>0?lines.length-300:0;
			  for( var i=0; i<lines.length-1; i++){
			  	if (lines[i]=="") continue;
			  	try {
			  		//console.log("line", lines[i]);
			  		var dataJson = $.parseJSON(lines[i]);
			  		//var dataJson = lines[i];
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

function getCounters(counterUrls, grpIndex, timerIndex){
	$.each(counterUrls, function(key, value){
		var cnt=0;
		$.ajax({
			url: value,
			type: "GET",
			contentType: "text/plain",
			async:false,
			success: function(data){
				var counterJson = $.parseJSON(data);
				cnt=counterJson["count"];
			},
			error: function(data){

			},
			complete: function(xhr,status){
				console.log("Selecting :", "#" + key + grpIndex + timerIndex);
				$("#" + key + grpIndex + timerIndex).empty();
				$("#" + key + grpIndex + timerIndex).append("<strong>" + key + "</strong>" + ":&nbsp;&nbsp" + cnt);
			}
		});
	});
}
}

