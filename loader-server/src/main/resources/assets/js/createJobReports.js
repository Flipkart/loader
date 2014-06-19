ko.bindingHandlers.graphsInitialization = {
    update: function(element, valueAccessor, allBindings, viewModel, bindingContext){
        var value = valueAccessor();
        var valueUnWrapperd = ko.unwrap(value);
        if(valueUnWrapperd) {
            viewModel.initializeGraphs($(element));
        }
    }
}

ko.bindingHandlers.addSlider = {
    update: function(element, valueAccessor, allBindings, viewModel, bindingContext){
        var value = valueAccessor();
        var valueUnWrapperd = ko.unwrap(value);
        if(valueUnWrapperd){
            bindingContext.$parent.addSlider(element);
        }
    }
}

ko.bindingHandlers.addCounterSlider = {
    update: function(element, valueAccessor, allBindings, viewModel, bindingContext){
        var value = valueAccessor();
        var valueUnWrapperd = ko.unwrap(value);
        if(valueUnWrapperd){
            bindingContext.$data.addSlider(element);
        }
    }
}

function getQueryParams(sParam) {
    var queryString = window.location.search.substring(2);
    var queryParams = queryString.split('&');
    for (var i = 0; i < queryParams.length; i++) {
        var sParameterName = queryParams[i].split('=');
        if (sParameterName[0] == sParam) {
            return sParameterName[1];
        }
    }
    return undefined;
}

function getJobStats() {
    var jobId = getQueryParams("jobId");
    $.ajax({
        url: "/loader-server/jobs/" + jobId + "/jobStats",
        type: "GET",
        contentType: "application/json",
        async: false,
        success: function(jobStats) {
            window.viewModel = new groupsGraphViewModel(jobStats);
            ko.applyBindings(window.viewModel, $("#groupGraphs")[0]);
            createGroupsTree(jobStats);
        },
        error: function() {

        },
        complete: function() {
        }
    });
}

function getMonitoringStats(){
    var jobId = getQueryParams("jobId");
    $.ajax({
        url: "/loader-server/jobs/" + jobId + "/monitoringStats",
        type: "GET",
        contentType: "application/json",
        async: false,
        success: function(monStats) {
            window.monViewModel = new monitorGraphsViewModel(monStats);
            ko.applyBindings(window.monViewModel, $("#monGraphs")[0]);
            //createGroupsTree(jobStats);
            createMonitoringTree(monStats);
        },
        error: function() {

        },
        complete: function() {
        }
    });
}

function createGroupsTree(jobStats) {
    $("#timerTree").jstree({
        "plugins": ["themes", "json_data", "checkbox", "ui", "types"],
        "types": {
            "types": {
                "graphs": {
                    "icon": {
                        "image": "../img/graphs.png"
                    },
                },
                "group": {
                    "icon": {
                        "image": "../img/group.png"
                    }
                },
                "function": {
                    "icon": {
                        "image": "../img/group.png"
                    }
                },
                "timers": {
                    "icon": {
                        "image": "../img/group.png"
                    }
                },
                "counters": {
                    "icon": {
                        "image": "../img/group.png"
                    }
                },
                "histograms": {
                    "icon": {
                        "image": "../img/group.png"
                    }
                },
                "timer": {
                    "icon": {
                        "image": "../img/function.png"
                    }
                },
                "counter": {
                    "icon": {
                        "image": "../img/group.png"
                    }
                },
                "histogram": {
                    "icon": {
                        "image": "../img/group.png"
                    }
                }
            }
        },
        "json_data": {
            "data": [{
                "attr": {
                    "id": "node_graphs",
                    "rel": "graphs"
                },
                "data": "Graphs",
                "metadata": {
                    "name": "Graphs",
                    "nodeType": "graphs",
                    "id": "node_graphs"
                },
                "children": getGraphsChildren(jobStats)
            }],
            "checkbox": {
                "override_ui": true,
            },
            "progressive_render": true,
        }
    }).bind("check_node.jstree", function(event, data) {
        updateStateOnCheck();
        switch (data.rslt.obj.data("nodeType")) {
            case "graphs":
                window.viewModel.showGraphs();
                break;
            case "group":
                var groupIndex = data.rslt.obj.data("groupIndex");
                var model = window.viewModel.groups()[groupIndex];
                window.viewModel.isVisible(true);
                model.showGroupGraphs();
                break;
            case "function":
                var groupIndex = data.rslt.obj.data("groupIndex");
                var functionIndex = data.rslt.obj.data("functionIndex");
                window.viewModel.isVisible(true);
                window.viewModel.groups()[groupIndex].isVisible(true);
                var model = window.viewModel.groups()[groupIndex].functions()[functionIndex];
                model.showFuncGraphs();
                break;
            case "timers":
                var groupIndex = data.rslt.obj.data("groupIndex");
                var functionIndex = data.rslt.obj.data("functionIndex");
                window.viewModel.isVisible(true);
                window.viewModel.groups()[groupIndex].isVisible(true);
                window.viewModel.groups()[groupIndex].functions()[functionIndex].isVisible(true);
                window.viewModel.groups()[groupIndex].functions()[functionIndex].timersVisible(true); 
                var model = window.viewModel.groups()[groupIndex].functions()[functionIndex];
                model.showFuncTimersGraphs();
                break;
            case "counters":
                var groupIndex = data.rslt.obj.data("groupIndex");
                var functionIndex = data.rslt.obj.data("functionIndex");
                window.viewModel.isVisible(true);
                window.viewModel.groups()[groupIndex].isVisible(true);
                window.viewModel.groups()[groupIndex].functions()[functionIndex].isVisible(true);
                window.viewModel.groups()[groupIndex].functions()[functionIndex].countersVisible(true); 
                var model = window.viewModel.groups()[groupIndex].functions()[functionIndex];
                model.showFuncCounterGraphs();
                break;
            case "histograms":
                var groupIndex = data.rslt.obj.data("groupIndex");
                var functionIndex = data.rslt.obj.data("functionIndex");
                window.viewModel.isVisible(true);
                window.viewModel.groups()[groupIndex].isVisible(true);
                window.viewModel.groups()[groupIndex].functions()[functionIndex].isVisible(true);
                window.viewModel.groups()[groupIndex].functions()[functionIndex].histogramsVisible(true); 
                var model = window.viewModel.groups()[groupIndex].functions()[functionIndex];
                model.showFuncHistogramGraphs();
                break;
            case "timer":
                var groupIndex = data.rslt.obj.data("groupIndex");
                var functionIndex = data.rslt.obj.data("functionIndex");
                var timerIndex = data.rslt.obj.data("keyIndex");
                window.viewModel.isVisible(true);
                window.viewModel.groups()[groupIndex].isVisible(true);
                window.viewModel.groups()[groupIndex].functions()[functionIndex].isVisible(true);
                window.viewModel.groups()[groupIndex].functions()[functionIndex].timersVisible(true); 
                var model = window.viewModel.groups()[groupIndex].functions()[functionIndex].timers()[timerIndex];
                model.showTimerGraphs();
                break;
            case "counter":
                break;
            case "histogram":
                var groupIndex = data.rslt.obj.data("groupIndex");
                var functionIndex = data.rslt.obj.data("functionIndex");
                var histIndex = data.rslt.obj.data("keyIndex");
                window.viewModel.isVisible(true);
                window.viewModel.groups()[groupIndex].isVisible(true);
                window.viewModel.groups()[groupIndex].functions()[functionIndex].isVisible(true);
                window.viewModel.groups()[groupIndex].functions()[functionIndex].histogramsVisible(true); 
                var model = window.viewModel.groups()[groupIndex].functions()[functionIndex].histograms()[histIndex];
                model.showHistogramGraphs();
                break;
        }
    }).bind("uncheck_node.jstree", function(event, data) {
        updateStateOnUnCheck();
        switch (data.rslt.obj.data("nodeType")) {
            case "graphs":
                //window.viewModel.isVisible(false);
                window.viewModel.hideGraphs();
                break;
            case "group":
                var groupIndex = data.rslt.obj.data("groupIndex");
                var model = window.viewModel.groups()[groupIndex];
                model.hideGroupGraphs();
                //model.isVisible(false);
                break;
            case "function":
                var groupIndex = data.rslt.obj.data("groupIndex");
                var functionIndex = data.rslt.obj.data("functionIndex");
                var model = window.viewModel.groups()[groupIndex].functions()[functionIndex];
                model.hideFuncGraphs();
                //model.isVisible(false);
                break;
            case "timers":
                var groupIndex = data.rslt.obj.data("groupIndex");
                var functionIndex = data.rslt.obj.data("functionIndex");
                var model = window.viewModel.groups()[groupIndex].functions()[functionIndex];
                //model.timersVisible(false);
                model.hideFuncTimersGraphs();
                break;
            case "counters":
                var groupIndex = data.rslt.obj.data("groupIndex");
                var functionIndex = data.rslt.obj.data("functionIndex");
                var model = window.viewModel.groups()[groupIndex].functions()[functionIndex];
                model.hideFuncCounterGraphs();
                //model.countersVisible(false);
                break;
            case "histograms":
                var groupIndex = data.rslt.obj.data("groupIndex");
                var functionIndex = data.rslt.obj.data("functionIndex");
                var model = window.viewModel.groups()[groupIndex].functions()[functionIndex];
                //model.timersVisible(false);
                model.hideFuncHistogramGraphs();
                break;
            case "timer":
                var groupIndex = data.rslt.obj.data("groupIndex");
                var functionIndex = data.rslt.obj.data("functionIndex");
                var timerIndex = data.rslt.obj.data("keyIndex");
                var model = window.viewModel.groups()[groupIndex].functions()[functionIndex].timers()[timerIndex];
                //model.isVisible(false);
                model.hideTimerGraphs();
                break;
            case "counter":
                break;
            case "histogram":
                var groupIndex = data.rslt.obj.data("groupIndex");
                var functionIndex = data.rslt.obj.data("functionIndex");
                var hisIndex = data.rslt.obj.data("keyIndex");
                var model = window.viewModel.groups()[groupIndex].functions()[functionIndex].histograms()[hisIndex];
                //model.isVisible(false);
                model.hideHistogramGraphs();
                break;

        }
    });
    $("#timerTree").bind("loaded.jstree", function(event, data) {
        $("#timerTree").jstree("open_all");
        checkTimerNodes();
        //$.jstree._reference("#timerTree").check_node("#node_"+window["groups"][0]["groupName"]);  
    });
    $("#timerTree").bind("refresh.jstree", function(event, data) {
        $("#timerTree").jstree("open_all");
        checkTimerNodes();
        //$.jstree._reference('#timerTree').check_node("#node_"+window["groups"][0]["groupName"]);  
    });
}

function getGraphsChildren(jobStats) {
    if (jobStats == undefined || jobStats.length == 0) return undefined;
    var children = []
    $.each(jobStats, function(index, group) {
        children.push({
            "attr": {
                "id": "node_" + group["groupName"],
                "rel": "group"
            },
            "metadata": {
                "nodeType": "group",
                "groupIndex": index,
                "id": "node_" + group["groupName"]
            },
            "data": group["groupName"],
            "children": getGroupChildren(group["functions"], index)
        });
    });
    return children;
}

function getGroupChildren(groupStats, groupIndex) {
    if (groupStats == undefined) return undefined;
    var children = [];
    var count = 0;
    $.each(groupStats, function(key, val) {
        children.push({
            "attr": {
                "id": "node_" + val["functionName"]+ "_" + groupIndex,
                "rel": "function"
            },
            "metadata": {
                "nodeType": "function",
                "groupIndex": groupIndex,
                "functionIndex": count,
            },
            "data": val["functionName"],
            "children": getFunctionChildren(val["metrics"], groupIndex, count)
        });
        count=count+1;
    });
    return children;
}

function getFunctionChildren(functionStats, groupIndex, functionIndex) {
    if (functionStats == undefined) return undefined;
    var children = [];
    var metricIndex = 0;
    $.each(functionStats, function(key, val) {
        children.push({
            "attr": {
                "id": "node_" + key + "_" + groupIndex + "_" + functionIndex,
                "rel": key
            },
            "metadata": {
                "nodeType": key,
                "groupIndex":groupIndex,
                "functionIndex": functionIndex,
                "metricIndex":metricIndex
            },
            "data": key,
            "children": getMetricsChildren(val, getMetricType(key), groupIndex, functionIndex, metricIndex)
        });
        metricIndex = metricIndex +1;
    });
    return children;
}

function getMetricsChildren(metricStats, type, groupIndex, functionIndex, metricIndex) {
    if (metricStats == undefined) return undefined;
    var children = [];
    if (type == "counter") {
        return undefined;
    }
    $.each(metricStats, function(index, metric) {
            children.push({
                "attr": {
                    "id": "node_" + metric["name"]+ "_" + groupIndex + "_" + functionIndex,
                    "rel": type
                },
                "metadata": {
                    "nodeType": type,
                    "groupIndex":groupIndex,
                    "functionIndex":functionIndex,
                    "metricIndex":metricIndex,
                    "keyIndex":index
                },
                "data": metric["name"]
            });
    });
    return children;
}

function getMetricType(parentType) {
    switch (parentType) {
        case "counters":
            return "counter";
        case "histograms":
            return "histogram";
        case "timers":
            return "timer";
    }
}

function getGraphPlotScheme() {
    $.ajax({
        url: "/loader-server/admin/config/report/job",
        type: "GET",
        contentType: "application/json",
        dataType: "json",
        async: false,
        success: function(scheme) {
            window["graphSceme"] = scheme;
        },
        error: function(err) {

        },
        complete: function(xhr, status) {
        }
    });
}

function createMonitoringTree(monStats) {
    $("#monitoringTree").jstree({
        "plugins": ["themes", "json_data", "checkbox", "ui", "types"],
        "types": {
            "types": {
                "monag": {
                    "icon": {
                        "image": "../img/monagents.png"
                    },
                },
                "agent": {
                    "icon": {
                        "image": "../img/metriccol.png"
                    }
                },
                "resource": {
                    "icon": {
                        "image": "../img/timer.png"
                    }
                }
            }
        },
        "json_data": {
            "data": [{
                "attr": {
                    "id": "node_graphs",
                    "rel": "monag"
                },
                "data": "MonitoringAgents",
                "metadata": {
                    "name": "MonitoringAgents",
                    "nodeType": "monag"
                },
                "children": getMonitoringChildren(monStats)
            }],
            "checkbox": {
                "override_ui": true,
            },
            "progressive_render": true,
        }
    }).bind("check_node.jstree", function(event, data) {
        updateStateOnCheck();
        switch (data.rslt.obj.data("nodeType")) {
            case "agent":
                //plotMonAgentGraphs(data.rslt.obj.data("agentIndex"));
                var agentIndex = data.rslt.obj.data("agentIndex");
                window["monViewModel"].isVisible(true);
                window["monViewModel"].monAgents()[agentIndex].showAgentGraphs();
                break;
            case "resource":
                //plotResourceGraphs(data.rslt.obj.data("agentIndex"), data.rslt.obj.data("resourceIndex"));
                var agentIndex = data.rslt.obj.data("agentIndex");
                var resourceIndex = data.rslt.obj.data("resourceIndex");
                window["monViewModel"].isVisible(true);
                window["monViewModel"].monAgents()[agentIndex].isVisible(true);
                window["monViewModel"].monAgents()[agentIndex].resources()[resourceIndex].showResourceGraphs();
                break;
            case "monag":
                window["monViewModel"].showMonitorGraphs();
        }
        setVisible();
    }).bind("uncheck_node.jstree", function(event, data) {
        updateStateOnUnCheck();
        switch (data.rslt.obj.data("nodeType")) {
            case "agent":
                var agentIndex = data.rslt.obj.data("agentIndex");
                window["monViewModel"].monAgents()[agentIndex].hideAgentGraphs();
                break;
            case "resource":
                var agentIndex = data.rslt.obj.data("agentIndex");
                var resourceIndex = data.rslt.obj.data("resourceIndex");
                window["monViewModel"].monAgents()[agentIndex].resources()[resourceIndex].hideResourceGraphs();
                break;
            case "monag":
                window["monViewModel"].hideMonitorGraphs();
        }
        setVisible();
    });

    $("#monitoringTree").bind("loaded.jstree", function(event, data) {
        $("#monitoringTree").jstree("open_all");
        checkTimerNodes();
        checkMonNodes();
    });
    $("#monitoringTree").bind("refresh.jstree", function(event, data) {
        $("#monitoringTree").jstree("open_all");
        checkTimerNodes();
        checkMonNodes();
    });

    function setVisible(){
        var anyAgentVisible = false;
        $.each(window["monViewModel"].monAgents(), function(index, ag){
            var anyResourceVisible = false;
            $.each(ag.resources(), function(ind, res){
                if(res.isVisible()){
                    anyResourceVisible=true;
                }
            });
            if(!anyResourceVisible) ag.isVisible(false);
            if(ag.isVisible()){
                anyAgentVisible=true;
            }
        });
        if(!anyAgentVisible) window["monViewModel"].isVisible(false);
    }
}

function getMonitoringChildren(monStats) {
    if (monStats == undefined || monStats.length == 0) return undefined;
    var children = [];
    $.each(monStats, function(index, monRes) {
        children.push({
            "attr": {
                "id": "node_" + monRes["agent"].replace(/\./g, "_"),
                "rel": "agent"
            },
            "metadata": {
                "nodeType": "agent",
                "agentIndex": index
            },
            "data": monRes["agent"],
            "children": getResourcesChildren(monStats, monRes, index)
        });
    });
    return children;
}

function getResourcesChildren(monStats, monRes, agentIndex) {
    if (monRes["resources"] == null || monRes["resources"] == undefined || monRes["resources"].length == 0) return undefined;
    var children = [];
    $.each(monRes["resources"], function(index, resource) {
        children.push({
            "attr": {
                "id": "node_" + monStats[agentIndex]["agent"].replace(/\./g, "_") + "_" + resource,
                "rel": "resource"
            },
            "metadata": {
                "nodeType": "resource",
                "agentIndex": agentIndex,
                "resourceIndex": index
            },
            "data": resource
        });
    })
    return children;
}

function getResourceType(resource) {
    if (resource.indexOf("jmx") !== -1) return "jmx";
    if (resource.indexOf("memory") !== -1) return "memory";
    if (resource.indexOf("sockets") !== -1) return "sockets";
    if (resource.indexOf("diskspace") !== -1) return "diskspace.root";
    if (resource.indexOf("mysql") !== -1) return "mysql";
    if (resource.indexOf('agentHealth') !== -1) return "agentHealth";
    if (resource.indexOf("redis") !== -1) return "redis";
    return resource;
}

var groupsGraphViewModel = function(jobStats) {
    var self = this;
    self.getGroups = function() {
        var groups = [];
        $.each(jobStats, function(index, group) {
            groups.push(new groupGraphViewModel(group));
        });
        return groups;
    }
    self.groups = ko.observableArray(self.getGroups());
    self.isVisible = ko.observable(false);
    self.showGraphs = function(){
        self.isVisible(true);
        var grps = self.groups();
        $.each(grps, function(index , grp){
            grp.showGroupGraphs();
        });      
    }
    self.hideGraphs = function(){
        self.isVisible(false);
        var grps = self.groups();
        $.each(grps, function(index , grp){
            grp.hideGroupGraphs();
        });   
    }
}

var groupGraphViewModel = function(group) {
    var self = this;
    var groupUrl = "/loader-server/jobs/" + getQueryParams("jobId") + "/jobStats/groups/" + group["groupName"];
    self.getFunctions = function() {
        var functions = [];
        $.each(group["functions"], function(k, v) {
            try {
                functions.push(new functionGraphViewModel(v, groupUrl));
            } catch(err) {
                console.log("ERROR: functionGraphViewModel creation failed with " + err);
            }
        });
        return functions;
    }
    self.groupName = ko.observable(group["groupName"]);
    self.functions = ko.observableArray(self.getFunctions());
    self.isVisible = ko.observable(false);
    self.showGroupGraphs = function(){
        self.isVisible(true);
        var funcs = self.functions();
        $.each(funcs, function(index, func){
            func.showFuncGraphs();
        });
    }
    self.hideGroupGraphs = function(){
        self.isVisible(false);
        var funcs = self.functions();
        $.each(funcs, function(index, func){
            func.hideFuncGraphs();
        });
    }
}

var functionGraphViewModel = function(func, groupUrl) {
    var self = this;
    var functionUrl = groupUrl + "/functions/" + func["functionName"];
    self.functionName = func["functionName"];
    self.getTimers = function() {
        var timers = [];
        if(func["metrics"]["timers"]==undefined) return timers;
        $.each(func["metrics"]["timers"], function(index, timer) {
            try {
                timers.push(new timerGraphViewModel(timer, functionUrl));
            } catch (err){
                console.log("ERROR: Creation of timerGraphViewModel failed with " + err);
            }
        });
        return timers;
    }
    
    self.getHistograms = function(){
        var histos = [];
        if(func["metrics"]["histograms"]==undefined) return histos;
        $.each(func["metrics"]["histograms"], function(index, histo){
            try {
                histos.push(new histogramGraphViewModel(histo, functionUrl));
            } catch (err){
                console.log("ERROR: Creation of histogramGraphViewModel failed with " + err);
            }
        });
        return histos;
    }
    self.timers = ko.observableArray(self.getTimers());
    try {
        self.counters = ko.observableArray([new countersGraphViewModel(func["metrics"]["counters"], functionUrl)]);
    } catch (err){
        console.log("ERROR: Creation of countersGraphViewModel failed with " + err);
    }
    self.histograms = ko.observableArray(self.getHistograms());
    self.isVisible = ko.observable(false);
    self.timersVisible = ko.observable(false);
    self.countersVisible = ko.observable(false);
    self.histogramsVisible = ko.observable(false);
    self.showFuncGraphs = function(){
        self.isVisible(true);
        self.showFuncTimersGraphs();
        self.showFuncCounterGraphs();
        self.showFuncHistogramGraphs();
    }
    self.hideFuncGraphs = function(){
        self.isVisible(false);
        self.hideFuncTimersGraphs();
        self.hideFuncCounterGraphs();
        self.hideFuncHistogramGraphs();
    }
    self.showFuncTimersGraphs = function(){
        self.timersVisible(true);
        var tmrs =  self.timers();
        $.each(tmrs, function(index, tm){
            tm.showTimerGraphs();
        });
    }
    self.hideFuncTimersGraphs = function(){
        self.timersVisible(false);
        var tmrs =  self.timers();
        $.each(tmrs, function(index, tm){
            tm.hideTimerGraphs();
        });
    }
    self.showFuncCounterGraphs = function(){
        self.countersVisible(true);
        var counters = self.counters();
        counters[0].showCounterGraphs();

    }
    self.hideFuncCounterGraphs = function(){
        self.countersVisible(false);
        var counters = self.counters();
        counters[0].hideCounterGraphs();
    }
    self.showFuncHistogramGraphs = function(){
        self.histogramsVisible(true);
        var histos = self.histograms();
        $.each(histos, function(index, h){
            h.showHistogramGraphs();
        });
    }
    self.hideFuncHistogramGraphs = function(){
        self.histogramsVisible(false);
        var histos = self.histograms();
        $.each(histos, function(index, h){
            h.hideHistogramGraphs();
        });
    }
}
var histogramGraphViewModel = function(histogram, functionUrl){
    var self = this;
    self.histogramName = "Histogram-" + histogram["name"];
    self.availableAgents = ko.observableArray(histogram["agents"]);
    self.selectedAgent = ko.observable("combined");
    self.dataAvailable = ko.observable(false);
    self.histogramUrl = ko.computed(function(){
        return functionUrl + "/histograms/" + histogram["name"] + "/agents/" + self.selectedAgent();
    });
    var chartScheme = window["graphSceme"]["chartResources"]["histogram"]["charts"];
    self.chartRows = ko.observableArray(new Array(Math.floor((chartScheme.length + 1) / 2)));
    self.isVisible = ko.observable(false);
    self.chartsData = {};
    self.dataLength = ko.observable(0);
    self.totalCharts = ko.observable(chartScheme.length);
    self.sliderVisible = ko.observable(false);
    self.fetchAndParse = function() {
        var timeSeries = {};
        $.ajax({
            url: self.histogramUrl(),
            type: "GET",
            contentType: "application/json",
            async: false,
            success: function(histogramStats) {
                var dataLines = histogramStats.split('\n');
                self.dataLength(dataLines.length);
                if(dataLines.length>100){
                    self.sliderVisible(true);
                }
                for(var i=0;i<dataLines.length;i++){
                    if (dataLines[i]=="") continue;
                    var lineJson = $.parseJSON(dataLines[i]);
                    $.each(lineJson, function(key, value) {
                        if(key != "time"){
                            if (timeSeries[key] == undefined) timeSeries[key] = new Array();
                            timeSeries[key].push({x: new Date(lineJson["time"]), y: value});
                        }
                    });
                }
            },
            error: function() {
            },
            complete: function() {
            }
        });
        return timeSeries;
    }
    self.createIndexesMap = function(){
        var map = [];
        for(var i=0;i<chartScheme.length;i++) map.push({"sliderStartIndex": 0});
        return map;
    }
    self.chartIndexes = self.createIndexesMap();
    
    self.hide = function() {
        $(this).hide();
    }
    self.onAgentChange = function(data, event) {
        var agent = self.selectedAgent();
        if (self.chartsData[agent] == undefined) {
            self.chartsData[agent] = divideInCharts(self.fetchAndParse(),window["graphSceme"]["chartResources"]["histogram"]["charts"]);
        }
        self.plot($(event.target));
    }
    self.refresh = function(data, event) {
        var agent = self.selectedAgent();
        $.each(self.chartsData, function(k,v){
            self.chartsData[k] = undefined;
        });
        self.chartsData[agent] = divideInCharts(self.fetchAndParse(),window["graphSceme"]["chartResources"]["histogram"]["charts"]);
        self.plot($(event.target)[0]);
    }
    self.initializeGraphs = function(currElement){
        var agent = self.selectedAgent();
        self.chartsData[agent] = divideInCharts(self.fetchAndParse(),window["graphSceme"]["chartResources"]["histogram"]["charts"]);
        self.plot(currElement);
    }
    self.plot = function(currElement){
        var chartScheme = window["graphSceme"]["chartResources"]["histogram"]["charts"];
        var charts = self.chartsData[self.selectedAgent()];
        var tmpCharts = [];
        $.each(charts, function(index, chart){
            var tmpChart = [];
            var startIndex = self.chartIndexes[index].sliderStartIndex;
            var lastIndex = startIndex + 100> self.dataLength()?self.dataLength():startIndex + 100;
            $.each(chart, function(ind, line){
                tmpChart.push({"key":line["key"],"color":line["color"], "values": line["values"].slice(startIndex, lastIndex)});
            });
            tmpCharts.push(tmpChart);
        }); 
        $.each(tmpCharts, function(index, tmpChart){
            plotGraph(tmpChart, $($(currElement).parents(".histogramsGraphs")[0]).find("svg")[index], chartScheme[index]["xLegend"], chartScheme[index]["yLegend"]);
        });  
    }
    self.showHistogramGraphs = function(){
        self.isVisible(true);
    }
    self.hideHistogramGraphs = function(){
        self.isVisible(false);
    }
    self.addSlider = function(element){
        var st = Math.ceil(self.dataLength()/100);
        var options={
            min: 0,
            max: self.dataLength()-100,
            step:st,
            stop: function(event, ui){
                var k = $(event.target).attr('id');
                self.chartIndexes[k].sliderStartIndex=ui.value;
                self.updatePlot(k, event);
            }
        }
        $(element).slider(options);
    }

    self.updatePlot = function(k, event){
        var chartScheme = window["graphSceme"]["chartResources"]["histogram"]["charts"];
        var charts = self.chartsData[self.selectedAgent()];
        var tmpChart = [];
        var startIndex = self.chartIndexes[k].sliderStartIndex;
        var lastIndex = startIndex + 100> self.dataLength()?self.dataLength():startIndex + 100;
        $.each(charts[k], function(ind, line){
            tmpChart.push({"key":line["key"],"color":line["color"], "values": line["values"].slice(startIndex, lastIndex)});
        });
        var currElement = $(event.target)[0];
        plotGraph(tmpChart, $($(currElement).parents(".histogramsGraphs")[0]).find("svg")[k], chartScheme[k]["xLegend"], chartScheme[k]["yLegend"]);
    }

}
var timerGraphViewModel = function(timer, functionUrl) {
    var self = this;
    self.timerName = "Timer-" + timer["name"];
    self.availableAgents = ko.observableArray(timer["agents"]);
    self.selectedAgent = ko.observable("combined");
    self.dataAvailable = ko.observable(false);
    self.timerDataUrl = ko.computed(function() {
        return functionUrl + "/timers/" + timer["name"] + "/agents/" + self.selectedAgent();
    });
    var chartScheme = window["graphSceme"]["chartResources"]["timer"]["charts"];
    self.chartRows = ko.observableArray(new Array(Math.floor((chartScheme.length + 1) / 2)));
    self.isVisible = ko.observable(false);
    self.chartsData = {};
    self.dataLength = ko.observable(0);
    self.totalCharts = ko.observable(chartScheme.length);
    self.sliderVisible = ko.observable(false);

    self.fetchAndParse = function() {
        var timeSeries = {};
        $.ajax({
            url: self.timerDataUrl(),
            type: "GET",
            contentType: "application/json",
            async: false,
            success: function(timerStats) {
                var dataLines = timerStats.split('\n');
                self.dataLength(dataLines.length);
                if(dataLines.length>100){
                    self.sliderVisible(true);
                }
                for(var i=0;i<dataLines.length;i++){
                    if (dataLines[i]=="") continue;
                    var lineJson = $.parseJSON(dataLines[i]);
                    $.each(lineJson, function(key, value) {
                        if(key != "time"){
                            if (timeSeries[key] == undefined) timeSeries[key] = new Array();
                            timeSeries[key].push({x: new Date(lineJson["time"]), y: value});
                        }
                    });
                }
            },
            error: function() {
            },
            complete: function() {
            }
        });
        return timeSeries;
    }

    self.createIndexesMap = function(){
        var map = [];
        for(var i=0;i<chartScheme.length;i++) map.push({"sliderStartIndex": 0});
        return map;
    }
    self.chartIndexes = self.createIndexesMap();
    
    self.hide = function() {
        $(this).hide();
    }
    self.onAgentChange = function(data, event) {
        var agent = self.selectedAgent();
        if (self.chartsData[agent] == undefined) {
            self.chartsData[agent] = divideInCharts(self.fetchAndParse(),window["graphSceme"]["chartResources"]["timer"]["charts"]);
        }
        self.plot($(event.target));
    }
    self.refresh = function(data, event) {
        var agent = self.selectedAgent();
        $.each(self.chartsData, function(k,v){
            self.chartsData[k] = undefined;
        });
        self.chartsData[agent] = divideInCharts(self.fetchAndParse(),window["graphSceme"]["chartResources"]["timer"]["charts"]);
        self.plot($(event.target)[0]);
    }
    self.initializeGraphs = function(currElement){
        var agent = self.selectedAgent();
        self.chartsData[agent] = divideInCharts(self.fetchAndParse(),window["graphSceme"]["chartResources"]["timer"]["charts"]);
        self.plot(currElement);
    }
    self.plot = function(currElement){
        var chartScheme = window["graphSceme"]["chartResources"]["timer"]["charts"];
        var charts = self.chartsData[self.selectedAgent()];
        var tmpCharts = [];
        $.each(charts, function(index, chart){
            var tmpChart = [];
            var startIndex = self.chartIndexes[index].sliderStartIndex;
            var lastIndex = startIndex + 100> self.dataLength()?self.dataLength():startIndex + 100;
            $.each(chart, function(ind, line){
                tmpChart.push({"key":line["key"],"color":line["color"], "values": line["values"].slice(startIndex, lastIndex)});
            });
            tmpCharts.push(tmpChart);
        }); 
        $.each(tmpCharts, function(index, tmpChart){
            plotGraph(tmpChart, $($(currElement).parents(".timerGraphs")[0]).find("svg")[index], chartScheme[index]["xLegend"], chartScheme[index]["yLegend"]);
        });  
    }
    self.showTimerGraphs = function(){
        self.isVisible(true);
    }
    self.hideTimerGraphs = function(){
        self.isVisible(false);
    }
    self.addSlider = function(element){
        var st = Math.ceil(self.dataLength()/100);
        var options={
            min: 0,
            max: self.dataLength()-100,
            step:st,
            stop: function(event, ui){
                var k = $(event.target).attr('id');
                self.chartIndexes[k].sliderStartIndex=ui.value;
                self.updatePlot(k, event);
            }
        }
        $(element).slider(options);
    }

    self.updatePlot = function(k, event){
        var chartScheme = window["graphSceme"]["chartResources"]["timer"]["charts"];
        var charts = self.chartsData[self.selectedAgent()];
        var tmpChart = [];
        var startIndex = self.chartIndexes[k].sliderStartIndex;
        var lastIndex = startIndex + 100> self.dataLength()?self.dataLength():startIndex + 100;
        $.each(charts[k], function(ind, line){
            tmpChart.push({"key":line["key"],"color":line["color"], "values": line["values"].slice(startIndex, lastIndex)});
        });
        var currElement = $(event.target)[0];
        plotGraph(tmpChart, $($(currElement).parents(".timerGraphs")[0]).find("svg")[k], chartScheme[k]["xLegend"], chartScheme[k]["yLegend"]);
    }
}

var countersGraphViewModel = function(counters, functionUrl){
    var self = this;
    self.counters = counters;
    self.isVisible = ko.observable(false);
    self.availableAgents = counters[0]["agents"];
    self.selectedAgent = ko.observable("combined");
    self.counterUrls = ko.computed(function(){
        var urls = [];
        $.each(self.counters, function(index, ctr){
            urls.push({"name": ctr["name"], "url":functionUrl + "/counters/" + ctr["name"] + "/agents/" + self.selectedAgent()});
        });
        return urls;
    });
    self.dataLength = ko.observable(0);
    self.countersData = {};
    self.showCustomCounters = ko.observable(false);
    self.sliderVisible = ko.computed(function(){
        if (self.dataLength()>100) return true;
        else return false;
    });
    self.chartIndexes = [{"sliderStartIndex":0},{"sliderStartIndex":0}]
    self.fetchAndParse = function(){
        self.countersData[self.selectedAgent()] = {};
        $.each(self.counterUrls(), function(index, ctrUrl){
            $.ajax({
                url: ctrUrl["url"],
                type: "GET",
                contentType: "application/json",
                async: false,
                success: function(counterStats) {
                    var dataLines = counterStats.split('\n');
                    self.dataLength(dataLines.length);
                    var ctrData = [];
                    for(var i=0;i<dataLines.length;i++){
                        if (dataLines[i]=="") continue;
                        var lineJson = $.parseJSON(dataLines[i]);   
                        ctrData.push({x: new Date(lineJson["time"]), y: lineJson["count"]});
                    }
                    self.countersData[self.selectedAgent()][ctrUrl["name"]] = ctrData;
                },
                error: function() {
                },
                complete: function() {
                }
            });   
            pushTimeStamps = false;
        });
        return self.countersData;
    }
    self.onAgentChange = function(data, event){
        if(self.countersData[self.selectedAgent()]==undefined){
            self.fetchAndParse();
        } 
        self.plot($(event.target)[0]);
    }
    self.refresh = function(data, event){
        self.countersData={};
        self.fetchAndParse();
        self.plot($(event.target)[0]);
    }
    self.plot = function(currElement, chartIndex){
        var agent = self.selectedAgent();
        var data = self.countersData[agent];
        var chart1=[];
        var chart2=[];
        var colors = ["#2ca02c", "#d62728", "#DF01D7", "#08088A", "#FF0000", "#ff7f0e", "#1f77b4"];
        var col1=0, col2=0;
        $.each(data, function(key, val){
            if(key=="error" || key=="skip" || key=="failure" || key=="count"){
                chart1.push({"key":key,"color":colors[col1],"values":val.slice(self.chartIndexes[0]["sliderStartIndex"], 
                    self.chartIndexes[0]["sliderStartIndex"]+100>self.dataLength()?self.dataLength():self.chartIndexes[0]["sliderStartIndex"]+100)});
                col1=col1+1;
                col1=col1%colors.length;  
            } else {
                chart2.push({"key":key,"color":colors[col2],"values":val.slice(self.chartIndexes[1]["sliderStartIndex"], 
                    self.chartIndexes[1]["sliderStartIndex"]+100>self.dataLength()?self.dataLength():self.chartIndexes[1]["sliderStartIndex"]+100)});
                col2=col2+1;
                col2=col2%colors.length;  
            }
        });
        if(chartIndex==1){
            plotGraph(chart1, $($(currElement).parents(".counterGraphs")[0]).find("svg")[0], "Time (HH:MM)", "Count");
        }
        else if(chartIndex==2) {
            plotGraph(chart2, $($(currElement).parents(".counterGraphs")[0]).find("svg")[1], "Time (HH:MM)", "Count");
        }
        else {
            plotGraph(chart1, $($(currElement).parents(".counterGraphs")[0]).find("svg")[0], "Time (HH:MM)", "Count");
            if(chart2.length>0){
                self.showCustomCounters(true);
                plotGraph(chart2, $($(currElement).parents(".counterGraphs")[0]).find("svg")[1], "Time (HH:MM)", "Count");
            }
        }
    }
    self.updatePlot = function(k, event){
        self.plot($(event.target, k));
    }
    self.addSlider = function(element){
        var st = Math.ceil(self.dataLength()/100);
        var options={
            min: 0,
            max: self.dataLength()-100,
            step:st,
            stop: function(event, ui){
                var k = $(event.target).attr('id');
                self.chartIndexes[k].sliderStartIndex=ui.value;
                self.updatePlot(k, event);
            }
        }
        $(element).slider(options);
    }
    self.showCounterGraphs = function(){
        self.isVisible(true);
    }
    self.hideCounterGraphs = function(){
        self.isVisible(false);
    }
    self.initializeGraphs = function(currElement){
        if(self.countersData[self.selectedAgent()]==undefined){
            self.fetchAndParse();
        }
        self.plot(currElement);
    }
}

function divideInCharts(data, chartScheme) {
    var timeSeriesData = [];
    $.each(chartScheme, function(index, chart){
        var keysToPlot = chart["keysToPlot"];
        var linesToPlot = [];
        $.each(keysToPlot, function(ind, k){
            var ke = k["key"];
            var tmp = data[ke];
            if(tmp!=undefined){
                var tmpJson = { 
                    "values": tmp,
                    "key": k["name"],
                    "color": k["color"]
                }
                linesToPlot.push(tmpJson);
            }
         });
        timeSeriesData.push(linesToPlot);
    });
    return timeSeriesData;
}

var monitorGraphsViewModel = function(monStats) {
    var self = this;
    self.monStatsUrl = "/loader-server/jobs/" + getQueryParams("jobId") + "/monitoringStats"
    self.isVisible = ko.observable(false);
    self.getMonAgents = function(){
        var agents = [];
        $.each(monStats, function(index, agent){
            agents.push(new monAgentGraphsViewModel(agent, self.monStatsUrl));
        });
        return agents;
    }
    self.monAgents  = ko.observableArray(self.getMonAgents());
    self.showMonitorGraphs = function(){
        self.isVisible(true);
        $.each(self.monAgents(), function(index, monAgent){
            monAgent.showAgentGraphs();
        });
    }
    self.hideMonitorGraphs = function(){
        self.isVisible(false);
        $.each(self.monAgents(), function(index, monAgent){
            monAgent.hideAgentGraphs();
        });
    }
}

var monAgentGraphsViewModel = function(agent, url){
    var self = this;
    self.agentName = agent["agent"];
    self.agentMonStatsUrl = url + "/agents/" + self.agentName;
    self.getResources = function(){
        var res = [];
        $.each(agent["resources"],function(index, r){
            res.push(new resourceGraphsViewModel(r, self.agentMonStatsUrl));
        });
        return res;
    }
    self.resources = ko.observableArray(self.getResources());
    self.isVisible = ko.observable(false);
    self.showAgentGraphs = function(){
        self.isVisible(true);
        $.each(self.resources(), function(index, res){
            res.showResourceGraphs();
        });
    }
    self.hideAgentGraphs = function(){
        self.isVisible(false);
        $.each(self.resources(), function(index, res){
            res.hideResourceGraphs();
        });
    }
}

var resourceGraphsViewModel = function(resource, url){
    var self = this;
    self.resourceName = resource;
    self.resourceUrl = url + "/resources/" + self.resourceName;
    self.resourceData = {};
    self.resourceMapsData = [];
    self.resourcePlotScheme = window["graphSceme"]["chartResources"][self.resourceName];
    self.dataLength = ko.observable(0);
    self.sliderVisible = ko.computed(function(){
        if(self.dataLength()>100) return true;
        return false;
    });
    self.isVisible = ko.observable(false);
    self.totalSvgRows = ko.observableArray(new Array(Math.floor((self.resourcePlotScheme["charts"].length + 1) / 2)));
    self.totalSvgs = ko.observable(self.resourcePlotScheme["charts"].length);
    self.createIndexesMap = function(){
        var map=[];
        for(var k=0;k<self.totalSvgs();k++){
            map.push({"sliderStartIndex":0});
        }
        return map;
    }
    self.chartIndexes = self.createIndexesMap();
    self.fetchAndParse = function(){
        $.ajax({
            url: self.resourceUrl,
            type: "GET",
            contentType: "application/json",
            async: false,
            success: function(resStats) {
                var dataLines = resStats.split('\n');
                self.dataLength(dataLines.length);
                for(var i=0;i<dataLines.length;i++){
                    if (dataLines[i]=="") continue;
                    var lineJson = $.parseJSON(dataLines[i]);
                    $.each(lineJson[0]["metrics"], function(key, val){
                        if(self.resourceData[key]==undefined) self.resourceData[key] = [];
                        self.resourceData[key].push({x: new Date(lineJson[0]["time"]), y: val});
                    })
                }
                $.each(self.resourcePlotScheme["charts"], function(index, plot){
                    var tmpData = {};
                    $.each(plot["keysToPlot"], function(i,k){
                        if(k["isRegex"]){
                            var pat = new RegExp(k["key"], "i");
                            $.each(self.resourceData, function(resKey, valList){
                                if(pat.test(resKey)){
                                    tmpData[resKey] = self.resourceData[resKey];
                                }
                            });
                        } else {
                            tmpData[k["key"]] = self.resourceData[k["key"]];
                        }
                    });
                    self.resourceMapsData.push(tmpData);
                });
                console.log("this is resMapData",self.resourceMapsData);
            },
            error: function() {
            },
            complete: function() {
            }
        });    
    }
    self.refresh = function(data, event){
        self.fetchAndParse();
        self.plotAll($(event.target));
    };
    self.plot = function(k, currElement){
        var startIndex = self.chartIndexes[k]["sliderStartIndex"];
        var lastIndex = startIndex + 100>self.dataLength()?self.dataLength():startIndex + 100;
        var tmpChart = [];
        $.each(self.resourcePlotScheme["charts"][k]["keysToPlot"], function(index, keyToPlot){
            if(!keyToPlot["isRegex"]){
                tmpChart.push({"key":keyToPlot["name"],"values":self.resourceMapsData[k][keyToPlot["key"]].slice(startIndex, lastIndex),"color":keyToPlot["color"]});
            } else {
                var pat = new RegExp(keyToPlot["key"],"i");
                $.each(self.resourceMapsData[k], function(resKey, listVal){
                    if(pat.test(resKey)){
                        tmpChart.push({"key":resKey,"values":listVal.slice(startIndex, lastIndex),"color":getRandomColor()});
                    }
                });
            }
        });
        plotGraph(tmpChart, $($(currElement).parents(".resourceGraphs")[0]).find("svg")[k], 
            self.resourcePlotScheme["charts"][k]["xLegend"], self.resourcePlotScheme["charts"][k]["yLegend"],".2s");
    };
    self.plotAll = function(currElement){
        if(self.resourceMapsData.length==0) self.fetchAndParse();
        for(var k=0;k<self.totalSvgs();k++){
            self.plot(k, currElement);
        }
    }
    self.initializeGraphs = function(currElement){
        self.plotAll(currElement);
    }
    self.addSlider = function(element){
        var st = Math.ceil(self.dataLength()/100);
        var options={
            min: 0,
            max: self.dataLength()-100,
            step:st,
            stop: function(event, ui){
                var k = $(event.target).attr('id');
                self.chartIndexes[k].sliderStartIndex=ui.value;
                self.plot(k, $(event.target));
            }
        }
        $(element).slider(options);
    }

    self.showResourceGraphs = function(){
        self.isVisible(true);
    }

    self.hideResourceGraphs = function(){
        self.isVisible(false);
    }
}

function plotGraph(data, svgElement, xAxisLabel, yAxisLabel, tickFormat){
    if(tickFormat==undefined) tickFormat=",.2f";
    var chart;
    formatTime = d3.time.format("%H:%M"),
    formatMinutes = function(d) { return formatTime(new Date(d)); };
    nv.addGraph(function() {
        chart = nv.models.lineChart().
            margin({left: 80});
        
        chart.xAxis
            .axisLabel(xAxisLabel)
            .tickFormat(function(d) { return d3.time.format('%H:%M')(new Date(d)); });

        chart.yAxis
            .axisLabel(yAxisLabel)
            .tickFormat(d3.format(tickFormat));

        d3.select(svgElement)
        .datum(data)
        .transition()
        .duration(500)
        .call(chart);

        nv.utils.windowResize(chart.update);
        chart.dispatch.on('stateChange', function(e) { nv.log('New State:', JSON.stringify(e)); });
        return chart;
    });
}

function checkTimerNodes(){
    var selTimerList = getQueryParams('timerNodes');
    if(selTimerList != undefined && selTimerList!=""){
        selTimerList = selTimerList.split(",");
        $.each(selTimerList, function(index, timer){
            $.jstree._reference('#timerTree').check_node("#"+timer);
        });
    }
}

function checkMonNodes(){
    var selMonResList = getQueryParams('monNodes');
    if(selMonResList!=undefined && selMonResList!=""){
        selMonResList = selMonResList.split(",");
        $.each(selMonResList, function(index, monRes){
            $.jstree._reference('#monitoringTree').check_node("#"+monRes);
        });
    }
}

function updateStateOnCheck(){
    var selectedTimerNodes = $.jstree._reference('#timerTree').get_checked(null, 'get_all');
    var selectedMonNodes = $.jstree._reference('#monitoringTree').get_checked(null, 'get_all');
    var selTimerList = [];
    $.each(selectedTimerNodes, function(index, node){
        selTimerList.push($(node).attr('id'));
    });
    if(getQueryParams('timerNodes')!=undefined){
        $.each(getQueryParams('timerNodes').split(","), function(index, node){
            if(selTimerList.indexOf(node)==-1) selTimerList.push(node);
        });
    }
    var selMonResList = [];
    $.each(selectedMonNodes, function(index, node){
        selMonResList.push($(node).attr('id'));
    });
    if(getQueryParams('monNodes')!=undefined){
        $.each(getQueryParams('monNodes').split(","), function(index, node){
            if(selMonResList.indexOf(node)==-1) selMonResList.push(node);
        });
    }
    var autoRefresh = getQueryParams("autoRefresh");
    var interval = getQueryParams("interval");
    if(autoRefresh==undefined) autoRefresh = "false";
    if(interval==undefined) interval = "60000";
    var link = window.location.origin+ "/graphreports.html" + "?&jobId=" + getQueryParams("jobId")+ "&autoRefresh="+ autoRefresh + "&interval=" + interval + "&monNodes=" + selMonResList.join() + "&timerNodes=" + selTimerList.join() ;
    history.replaceState(null, null, link);
}

function updateStateOnUnCheck(){
    var selectedTimerNodes = $.jstree._reference('#timerTree').get_checked(null, 'get_all');
    var selectedMonNodes = $.jstree._reference('#monitoringTree').get_checked(null, 'get_all');
    var selTimerList = [];
    $.each(selectedTimerNodes, function(index, node){
        selTimerList.push($(node).attr('id'));
    });
    
    var selMonResList = [];
    $.each(selectedMonNodes, function(index, node){
        selMonResList.push($(node).attr('id'));
    });
    var autoRefresh = getQueryParams("autoRefresh");
    var interval = getQueryParams("interval");
    if(autoRefresh==undefined) autoRefresh = "false";
    if(interval==undefined) interval = "60000";
    var link = window.location.origin+ "/graphreports.html" + "?&jobId=" + getQueryParams("jobId")+ "&autoRefresh="+ autoRefresh + "&interval=" + interval  + "&monNodes=" + selMonResList.join() + "&timerNodes=" + selTimerList.join();
    history.replaceState(null, null, link);
}

function getRandomColor(){
    var colors = ["#ff7f0e", "#1f77b4", "#2ca02c", "#d62728", "#DF01D7", "#08088A", "#FF0000"];
    return colors[Math.floor((Math.random()*6))];
}
