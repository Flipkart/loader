ko.bindingHandlers.multiSelect = {
    init: function(element, valueAccessor, allBindingAccessor, viewModel){
        var selabHdr = "<div class='custom-header'><label><strong>Available Groups</strong></label>";
        var selcnHdr = "<div class='custom-header'><label><strong>Depends On Groups</strong></label>";
        switch($(element).attr('id')){
            case  "resources":
                selabHdr = "<div class='custom-header'><label><strong>Available Resources</strong></label></div>";
                selcnHdr = "<div class='custom-header'><label><strong>Monitored Resources</strong></label>";
                break;
            case  "inputResourceList":
                selabHdr = "<div class='custom-header'><label><strong>Available InputFile Resources</strong></label></div>";
                selcnHdr = "<div class='custom-header'><label><strong>InputFile Resources in Use</strong></label>";
                break;
            case  "histogramsList":
                selabHdr = "<div class='custom-header'><label><strong>Available Histograms</strong></label></div>";
                selcnHdr = "<div class='custom-header'><label><strong>Plot Histograms</strong></label>";
                break;
            case "customCountersList":
                selabHdr = "<div class='custom-header'><label><strong>Available Custom Counters</strong></label></div>";
                selcnHdr = "<div class='custom-header'><label><strong>Plot Custom Counters</strong></label>";
                break;
            case "customTimersList":
                selabHdr = "<div class='custom-header'><label><strong>Available Custom Timers</strong></label></div>";
                selcnHdr = "<div class='custom-header'><label><strong>Plot Custom Timers</strong></label>";
                break;
        }
        $(element).multiSelect({
            selectableHeader: selabHdr,
            selectionHeader: selcnHdr
        });
    },
    update: function(element, valueAccessor, allBindingAccessor, viewModel){
        var value = valueAccessor();
        var valueUnWrapperd = ko.unwrap(value);
        //if(valueUnWrapperd){
            $(element).multiSelect('refresh');
        //}
    }
}
ko.bindingHandlers.combobox = {
    init: function(element, valueAccessor, allBindingAccessor, viewModel){
        var value = valueAccessor();
        var valueUnWrapperd = ko.unwrap(value);
        if(valueUnWrapperd) $(element).combobox();  
    },
    update: function(element, valueAccessor, allBindingAccessor, viewModel){
        var selFun = viewModel.selectedFunction();
        var value = valueAccessor();
        var valueUnWrapperd = ko.unwrap(value);
        if(valueUnWrapperd){
            $(element).children("option").each(function(index, opt){
                if($(opt).attr("value") == selFun){ 
                    $(opt).attr("selected",true);
                }
                else { 
                    $(opt).removeAttr("selected"); 
                }
            });
        $(element).combobox('refresh');
        }
    }
}

ko.bindingHandlers.refreshTree = {
    update: function(element, valueAccessor, allBindingAccessor, viewModel){
        createTree();
        if(window.currentModel!=undefined) 
            selectNode(window.currentModel.nodeId);
        else 
            selectNode(window.viewModel.nodeId);
    }
}

ko.bindingHandlers.multiSel = {
    init: function(element, valueAccessor, allBindingAccessor, viewModel, bindingContext){
        var value = valueAccessor();
        var valueUnWrapperd = ko.unwrap(value);
    },
    update: function(element, valueAccessor, allBindingAccessor, viewModel, bindingContext){
        var value = valueAccessor();
        var valueUnWrapperd = ko.unwrap(value);
    }
}

ko.bindingHandlers.getGroups = {
    init:function(element, valueAccessor, allBindingAccessor, viewModel, bindingContext){
        var parent = bindingContext.$parent;
        var groupNames = [];
        $.each(parent.groups(), function(index, grp){
            if(grp.groupName()!=viewModel.groupName()) groupNames.push(grp.groupName());
        });
        viewModel.availableGroups(groupNames);
        var value = valueAccessor();
        var valueUnWrapperd = ko.unwrap(value);
        //if(valueUnWrapperd){
            var selabHdr = "<div class='custom-header'><label><strong>Available Groups</strong></label>";
            var selcnHdr = "<div class='custom-header'><label><strong>Depends On Groups</strong></label>";
            $(element).find("#availableGroup").multiSelect({
                selectableHeader: selabHdr,
                selectionHeader: selcnHdr
            });
        //}
    },
    update: function(element, valueAccessor, allBindingAccessor, viewModel, bindingContext){
        var parent = bindingContext.$parent;
        var groupNames = [];
        $.each(parent.groups(), function(index, grp){
            if(grp.groupName()!=viewModel.groupName()) groupNames.push(grp.groupName());
        });
        viewModel.availableGroups(groupNames);
        var value = valueAccessor();
        var valueUnWrapperd = ko.unwrap(value);
        //if(valueUnWrapperd){
            $(element).find("#availableGroup").multiSelect('refresh');
        //}
    }
}

function getInputResourceFiles(){
    $.ajax({
            url: "/loader-server/resourceTypes/inputFiles",
            contentType: "application/json", 
            type:"GET",
            async:false,
            success: function(data) {
                window.availableInputResources = data;
            },
            error: function(e){
            },
            complete: function(xhr, status){
            }
        });
}

function getAllFunctionClasses(){
    $.ajax({
        url: "/loader-server/functions",
        contentType: "application/json", 
        type:"GET",
        async:false,
        success: function(data) {
            window.availableFunctions = ["Choose Class"];
            window.availableFunctions = window.availableFunctions.concat(data);
        },
        error: function(e){
        },
        complete: function(xhr, status){
        }
    });
}

function getBusinessUnits(){    
    var searchUrl = "/loader-server/businessUnits";
    window.existingBus = [];
    window.existingTeams = {};
    $.ajax({
        url: searchUrl,
        contentType: "application/json", 
        dataType:"json",
        type:"GET",
        async:false,
        success: function(data){
            $.each(data, function(k,v){
                window.existingBus.push(k);
                var tmp = [];
                $.each(v["teams"], function(key,val){
                    tmp.push(key);
                });
                window.existingTeams[k]=tmp;
            });
        },
        error: function(){
        },
        complete: function(xhr, status){
            switch(xhr.status){
                case 200 : 
                    break;
                default :
                    window.existingBus = [];
            }
        }
    });
}

var runJsonViewModel = function(){
    var self=this;
    self.runName = ko.observable("RunSchema");
    self.selectedBu = ko.observable("Sample");
    self.desc = ko.observable("New RunSchema");
    self.loadPart = ko.observableArray([]);
    self.isVisible = ko.observable(false);
    self.loadPartsVisible = ko.observable(false);
    self.monAgentsVisible = ko.observable(false);
    self.availableBus = window.existingBus;
    self.nodeId = "node_" + (new Date().getTime() + Math.floor(Math.random()*1000));
    self.availableTeams = ko.computed(function(){
        return window.existingTeams[self.selectedBu()];
    });
    self.selectedTeam = ko.computed(function(){
        if(self.availableTeams()==undefined) return "Sample";
        return self.availableTeams()[0];
    });
    self.metricCollections = ko.observableArray([]);
    self.addLoadPart = function(){
        var lpmodel = new loadPartViewModel();
        self.loadPart.push(lpmodel);
        createTree();
        selectNode(lpmodel.nodeId);
    }
    self.deleteLoadPart = function(lp){
        self.loadPart.remove(lp);
        self.isVisible(true);
        createTree();

        selectNode(self.nodeId);
    }
    self.addMonitoringAgent = function(){
        var monModel = new monitoringViewModel();
        self.metricCollections.push(monModel);
        createTree();
        selectNode(monModel.nodeId);
    }

    self.deleteMonitoringAgent = function(monAg){
        self.metricCollections.remove(monAg);
        createTree();
        selectNode(self.nodeId);
    }
    self.isInitialized =false;
    self.isDirty = ko.computed(function(){
        self.runName();
        self.selectedBu();
        self.desc();
        self.loadPart();
        self.selectedTeam();
        if(!self.isInitialized){
            //self.isInitialized = true;
            return false;
        }
        return true;
    });
    self.hasChanged = function(){
        if(self.isDirty()) return true;
        var lpChanged = false;
        $.each(self.loadPart(), function(lIndex, loadPart){
            if(loadPart.hasChanged()){ 
                lpChanged = true; 
            }
        });
        if(lpChanged) return true;
        var mcChanged = false;
        $.each(self.metricCollections(), function(mIndex, metricCol){
            if(metricCol.hasChanged()){ mcChanged = true};
        });
        return mcChanged;
    }
}

var loadPartViewModel =  function(){
    var self = this;
    self.loadPartName = ko.observable("loadPart");
    self.createdAt = "" + (new Date().getTime() + Math.floor(Math.random()*1000));
    self.accordionId = "inputResAccordionId_" + self.createdAt;
    self.inputResourcesSelectId = "inputResSelectId_" + self.createdAt;
    self.inputResourcesSelectIdHref = "#" + self.inputResourcesSelectId;
    self.agents = ko.observable(1);
    self.availableInputResources = ko.observableArray(window.availableInputResources);
    self.dataGenerators = ko.observableArray([]);
    self.useInputResources = ko.observableArray([]);
    self.groups = ko.observableArray([]);
    self.logLevels = ko.observableArray(["DEBUG", "INFO", "WARNING"]);
    self.logLevel = ko.observable("INFO");
    var setup = new groupViewModel();
    setup.groupName("Setup")
    var tearDown =  new groupViewModel();
    tearDown.groupName("TearDown");
    self.setupVisible = ko.observable(false);
    self.tearDownVisible = ko.observable(false);
    self.setupGroup = ko.observable(setup);
    self.tearDownGroup = ko.observable(tearDown);
    self.isVisible = ko.observable(false);
    self.nodeId = "node_" + self.createdAt;
    self.dataGenAccordionId = "lpDataGenAccordionId_" + self.createdAt;
    self.dataGenId = "lpDataGenId_" + self.createdAt;
    self.dataGenIdHref = "#" + self.dataGenId;

    self.groupsName = ko.computed(function(){
        var gNames = [];
        if(self.groups()!=undefined){
            $.each(self.groups(), function(gIndex, grp){
                if(grp!=undefined)gNames.push(grp.groupName());
            });
        }
        return gNames;
    }); 
    self.addGroup = function(){
        var grpModel = new groupViewModel();
        self.groups.push(grpModel);
        createTree();
        selectNode(grpModel.nodeId);
    }
    self.deleteGroup = function(gp){
        self.groups.remove(gp);
        //self.isVisible(true);
        createTree();
        selectNode(self.nodeId);
    }
    self.addDataGenerator = function(){
        var dataGeneratorModel = new dataGeneratorViewModel();
        self.dataGenerators.push(dataGeneratorModel);
    }
    self.deleteDataGenerator = function(dg){
        self.dataGenerators.remove(dg);
    }
    self.isInitialized = false;
    self.isDirty = ko.computed(function(){
        self.loadPartName();
        self.agents();
        self.dataGenerators();
        self.useInputResources();
        self.groups();
        self.logLevel();
        self.setupGroup();
        self.tearDownGroup();
        if(!self.isInitialized){
            //self.isInitialized = true;
            return false;
        }
        return true;
    });
    self.hasChanged = function(){
        if(self.isDirty()) return true;
        if(self.setupGroup().hasChanged()) return true;
        if(self.tearDownGroup().hasChanged()) return true;
        var grpChanged = false;
        $.each(self.groups(), function(gIndex, grp){
            if(grp.hasChanged()){ grpChanged=true; }
        });
        if(grpChanged) return true;
        var dataGenChanged = false;
        $.each(self.dataGenerators(), function(dIndex, dataGen){
            if(dataGen.hasChanged()) { dataGenChanged = true;}
        });
        return dataGenChanged;
    }
}

var groupViewModel = function(){
    var self = this;
    self.createdAt = "" + (new Date().getTime() + Math.floor(Math.random()*1000));
    self.groupName = ko.observable("Group_" + self.createdAt.substring(self.createdAt.length-3));
    self.groupStartDelay = ko.observable(0);
    self.threadStartDelay = ko.observable(0);
    self.throughput = ko.observable(-1);
    self.repeats = ko.observable(-1);
    self.duration = ko.observable(-1);
    self.threads = ko.observable(1);
    self.warmUpRepeats = ko.observable(-1);
    self.availableGroups = ko.observableArray([]);
    self.accordionId = "accordionId_" + self.createdAt;
    self.dependsOnSelectId = "dependsOnSelectId_" + self.createdAt;
    self.dependsOnSelectIdHref = "#" + self.dependsOnSelectId;
    self.timersAccordionId = "timersAccordionId_" + self.createdAt;
    self.addTimersId = "addTimersId_" + self.createdAt;
    self.addTimersIdHref = "#" + self.addTimersId;
    self.dataGenAccordionId = "dataGenAccordionId_" + self.createdAt;
    self.dataGenId = "dataGenId_" + self.createdAt;
    self.dataGenIdHref = "#" + self.dataGenId;
    // self.availableGroups = ko.computed(function(){
    //  return self.otherGroups();
    // });
    self.dependsOn = ko.observableArray([]);
    self.functions = ko.observableArray([]);
    self.dataGenerators = ko.observableArray([]);
    self.timersVisible = ko.observable(false);
    self.isVisible = ko.observable(false);
    self.params = ko.observable({});
    self.timers = ko.observableArray([]);
    self.threadResources = ko.observableArray([]);
    self.nodeId = "node_" + self.createdAt;


    self.addFunction = function(){
        var funViewModel = new functionViewModel();
        //funViewModel.isVisible(true); 
        self.functions.push(funViewModel);
        //self.isVisible(false);
        createTree();
        selectNode(funViewModel.nodeId);
    }
    self.deleteFunction = function(fn){
        self.functions.remove(fn);
        //self.isVisible(true);
        createTree();
        selectNode(self.nodeId);
    }
    self.addTimer = function(){
        var timerModel = new timerViewModel();
        self.timers.push(timerModel);
    }
    self.deleteTimer = function(tm){
        self.timers.remove(tm);
    }
    self.addDataGenerator = function(){
        var dataGeneratorModel = new dataGeneratorViewModel();
        self.dataGenerators.push(dataGeneratorModel);
    }
    self.deleteDataGenerator = function(dg){
        self.dataGenerators.remove(dg);
    }
    self.isInitialized = false;
    self.isDirty = ko.computed(function(){
        self.groupName();
        self.groupStartDelay();
        self.threadStartDelay();
        self.throughput();
        self.repeats();
        self.duration();
        self.warmUpRepeats();
        self.dependsOn();
        self.threads();
        self.functions();
        self.dataGenerators();
        self.params();
        self.timers();
        self.threadResources();
        if(!self.isInitialized){
            //self.isInitialized = true;
            return false;
        }
        return true;
    });
    self.hasChanged = function(){
        if(self.isDirty()) return true;
        var funcChanged = false;
        $.each(self.functions(), function(fIndex, func){
            if(func.hasChanged()) funcChanged=true;
        });
        if(funcChanged) return true;
        var dataGenChanged = false;
        $.each(self.dataGenerators(), function(dIndex, dataGen){
            if(dataGen.hasChanged()) {dataGenChanged =true;}
        });
        if(dataGenChanged) return true;
        var timerChanged = false;
        $.each(self.timers(), function(tIndex, timer){
            if(timer.hasChanged()) timerChanged= true;
        });
        return timerChanged;
    }
}


var timerViewModel = function(){
    var self = this;
    self.timerName = ko.observable("New Timer");
    self.threads = ko.observable(1);
    self.throughput = ko.observable(-1);
    self.duration = ko.observable(-1);
    self.isInitialized = false;
    self.isDirty=ko.computed(function(){
        self.timerName();
        self.threads();
        self.duration();
        self.throughput();
        if(!self.isInitialized){
            //self.isInitialized = true;
            return false;
        }
        return true;
    });
    self.hasChanged = function(){
        return self.isDirty();
    }
}

var functionViewModel = function(){
    var self = this;
    self.createdAt = "" + (new Date().getTime() + Math.floor(Math.random()*1000));
    self.functionName = ko.observable("PerformanceFunction_" + self.createdAt.substring(self.createdAt.length-3));
    self.accordionId = "accordionId_" +  self.createdAt;
    self.InputParameterId = "InputParameterId_" + self.createdAt;
    self.InputParameterIdHref = "#" + self.InputParameterId;
    self.histAccordionId = "histAccordionId_" + self.createdAt;
    self.histId = "histId_" + self.createdAt;
    self.histIdHref = "#" + self.histId; 
    self.timerAccordionId = "timerAccordionId_" + self.createdAt;
    self.timerId = "timerId_" + self.createdAt;
    self.timerIdHref = "#" + self.timerId;
    self.counterAccordionId = "counterAccordionId_" + self.createdAt;
    self.counterId = "counterId_" + self.createdAt;
    self.counterIdHref = "#" + self.counterId;
    self.availableFunctionClass = window.availableFunctions;
    self.isVisible = ko.observable(false);
    self.selectedFunction = ko.observable(self.availableFunctionClass[0]);
    self.dumpOpts = [true, false];
    self.dumpData = ko.observable(false);
    self.nodeId = "node_" + self.createdAt;
    self.availableParameters = ko.computed(function(){
        var functionName = self.selectedFunction();
        var params = {};
        if (typeof functionName == 'undefined' || functionName == undefined || functionName == "Choose Class") { 
            params["inputParameters"] = ko.observableArray([]);
            params["histograms"] = ko.observableArray([]);
            params["customCounters"] = ko.observableArray([]);
            params["customTimers"] = ko.observableArray([]);
            return params;
        }
        var inputParams= [];
        $.ajax({url: "/loader-server/functions/" + functionName + "?classInfo=true",
            contentType: "application/json", 
            type:"GET",
            async:false,
            success: function(data) {
                var ip = data[0]["inputParameters"];
                $.each(ip, function(k,v){
                    inputParams.push(new inputParamViewModel(v)); 
                });
                params["inputParameters"] = ko.observableArray(inputParams);
                params["histograms"] = ko.observableArray(data[0]["customHistograms"]);
                params["customCounters"] = ko.observableArray(data[0]["customCounters"]);
                params["customTimers"] = ko.observableArray(data[0]["customTimers"]);
            },
            error: function(e){
            }
        });
        return params;
    });
    self.selectedHistograms = ko.observableArray([]);
    self.selectedCustomTimers = ko.observableArray([]);
    self.selectedCustomCounters = ko.observableArray([]);
    self.isInitialized = false;
    self.isDirty = ko.computed(function(){
        self.functionName();
        self.selectedFunction();
        self.dumpData();
        self.selectedHistograms();
        self.selectedCustomTimers();
        self.selectedCustomCounters();
        if(!self.isInitialized){
            //self.isInitialized = true;
            return false;
        }
        return true;
    });
    self.hasChanged = function(){
        if(self.isDirty()) return true;
        var paramChanged = false;
        $.each(self.availableParameters().inputParameters(), function(pIndex, inputParam){
            if(inputParam.hasChanged()) paramChanged= true;
        });
        return paramChanged;
    }
}

var inputParamViewModel = function(inputParam){
    var self = this;
    self.key = inputParam["name"];
    self.isScalar = inputParam["type"]=="SCALER"?true:false;
    self.isHashMap = inputParam["type"]=="MAP"?true:false;
    self.isList = inputParam["type"]=="LIST"?true:false;
    self.showButton = ko.computed(function(){
        return !self.isScalar;
    });
    self.getScalar = function(){
        if(!self.isScalar) return "";
        if(inputParam["defaultValue"]==null || inputParam["defaultValue"]==undefined) return "";
        return inputParam["defaultValue"];
    }
    self.getList = function(){
        if(!self.isList) return [];
        //var list = $.parseJSON(inputParam["defaultValue"]);
		var list = inputParam["defaultValue"];
        var params = [];
        if(list==null || list==undefined) return params;
        $.each(list, function(index, param){
            params.push({"keyValue": ko.observable(param)});
        });
        return params;
    }
    self.getMap = function(){
        if(!self.isHashMap) return [];
        //var map =  $.parseJSON(inputParam["defaultValue"]);
		var map = inputParam["defaultValue"];
        var params = [];
        if(map==null || map==undefined) return params;
        $.each(map, function(k,v){
            params.push({"name": ko.observable(k), "keyValue":ko.observable(v)});
        });
        return params;
    }
    self.scalarValue = ko.observable(self.getScalar());
    self.listValue = ko.observableArray(self.getList());
    self.mapValue = ko.observableArray(self.getMap());
    self.addListElement = function(){
        self.listValue.push({"keyValue": ko.observable("")});
    }
    self.addMapElement = function(){
        self.mapValue.push({"name":ko.observable(""), "keyValue":ko.observable("")})
    }
    self.addElement= function(){
        if(self.isList) self.addListElement();
        else self.addMapElement();
    }
    self.removeFromMap = function(elem){
        self.mapValue.remove(elem);
    }
    self.removeFromList = function(elem){
        self.listValue.remove(elem);
    }
    self.returnScalar = function(){
        return self.scalarValue();
    }
    self.returnList = function(){
        var paramList = self.listValue();
        var result = [];
        $.each(paramList, function(index, elem){
            result.push(elem.keyValue());
        });
        return result;
    }
    self.returnMap = function(){
        var mapList = self.mapValue();
        var result = {};
        $.each(mapList, function(ind, elem){
            result["\"" + elem.name() + "\""] = elem.keyValue();
        });
        return result;
    }
    self.val = function(){
        if(self.isScalar) return self.returnScalar();
        if(self.isList) return self.returnList();
        if(self.isHashMap) return self.returnMap();
    }
    self.isInitialized = false;
    self.isDirty = ko.computed(function(){
        self.scalarValue();
        self.listValue();
        self.mapValue();
        if(!self.isInitialized){
            //self.isInitialized = true;
            return false;
        }
        return true;
    });
    self.hasChanged = function(){
        if(self.isDirty()) return true;
    }
}

var dataGeneratorViewModel = function(){
    var self = this;
    self.generatorName = ko.observable("Data Generator");
    self.generatorType = ko.observable("Choose Type");
    var types = ["Choose Type","COUNTER", "FIXED_VALUE", "RANDOM_FLOAT", "RANDOM_NUMBER", "RANDOM_SELECTION", "RANDOM_STRING", "RANDOM_DISTRIBUTION", "CYCLIC_SELECTION", "USE_AND_REMOVE"];
    self.availableGeneratorTypes = ko.observableArray(types);
    self.isDefault = ko.computed(function(){
        if(self.generatorType()=="Choose Type" || self.generatorType()== "RANDOM_FLOAT") return false;
        return true;
    });
    self.isCounter = ko.computed(function(){
        if(self.generatorType()=="COUNTER") return true;
        return false;
    });
    self.isFixedValue = ko.computed(function(){
        if(self.generatorType()=="FIXED_VALUE") return true;
        return false;
    });
    self.isRandomFloat = ko.computed(function(){
        if(self.generatorType()=="RANDOM_FLOAT") return true;
        return false;
    });
    self.isRandomNumber = ko.computed(function(){
        if(self.generatorType()=="RANDOM_NUMBER") return true;
        return false;
    });
    self.isRandomSelection = ko.computed(function(){
        if(self.generatorType()=="RANDOM_SELECTION") return true;
        return false;
    });
    self.isRandomString = ko.computed(function(){
        if(self.generatorType()=="RANDOM_STRING") return true;
        return false;
    });
    self.isRandomDistribution = ko.computed(function(){
        if(self.generatorType()=="RANDOM_DISTRIBUTION") return true;
        return false;
    });
    self.isCyclicSelection = ko.computed(function(){
        if(self.generatorType()=="CYCLIC_SELECTION") return true;
        return false;
    });
    self.isUseAndRemove = ko.computed(function(){
        if(self.generatorType()=="USE_AND_REMOVE") return true;
        return false;
    });
    self.startValue = ko.observable(0);
    self.maxValue = ko.observable(2147483647);
    self.jump=ko.observable(1);
    self.availableStringTypes = ko.observableArray(["NUMERIC", "ALPHABETIC", "ALPHA_NUMERIC", "ANY"]);
    self.stringType = ko.observable("ALPHABETIC");
    self.stringLength = ko.observable(20);
    self.closedString = ko.observable("");
    self.distributionInfoList = ko.observableArray([]);
    self.selectionList = ko.observableArray([]);
    self.addtoDisInfoList = function(){
        self.distributionInfoList.push({"start":ko.observable(0), "end":ko.observable(100), "val":ko.observable(1)});
    }
    self.removeFromDisInfoList = function(info){
        self.distributionInfoList.remove(info);
    }
    self.addToSelectionList = function(){
        self.selectionList.push({"listValue":ko.observable("")})
    }
    self.removeFromSelectionList = function(elem){
        self.selectionList.remove(elem);
    }
    self.isInitialized = false;
    self.isDirty = ko.computed(function(){
        self.generatorName();
        self.generatorType();
        self.startValue();
        self.maxValue();
        self.jump();
        self.stringType();
        self.stringLength();
        self.closedString();
        self.distributionInfoList();
        self.selectionList();
        if(!self.isInitialized){
            //self.isInitialized = true;
            return false;
        }
        return true;
    });
    self.hasChanged = function(){
        if(self.isDirty()) return true; 
    }
}

var monitoringViewModel = function(){
    var self = this;
    self.createdAt = "" + (new Date().getTime() + Math.floor(Math.random()*1000));
    self.agent = ko.observable("127.0.0.1");
    self.availableResources = ko.computed(function(){
        var result =[];
        $.ajax({
            "url":"http://" + self.agent() + ":7777/monitoring-service/resources",
            "contentType": "application/json", 
            "type":"GET",
            "async":false,
            success: function(data){
                result = result.concat(data);
            },
            error: function(err){
            }
        });
        return result;
    });
    self.isVisible = ko.observable(false);
    self.nodeId = "node_" + self.createdAt;
    self.availableOnDemandCollectors = ko.computed(function(){
        var result =[];
        $.ajax({
            "url":"http://" + self.agent() + ":7777/monitoring-service/onDemandResources",
            "contentType": "application/json", 
            "type":"GET",
            "async":false,
            success: function(data){
                result = result.concat(data);   
            },
            error: function(){
            }
        });
        return result;
    });
    self.onDemandCollectors = ko.observableArray([]);
    self.addOnDemandCollector = function(){
        self.onDemandCollectors.push(new OnDemandCollector(self.availableOnDemandCollectors()));
    }
    self.removeOnDemandCollector = function(collector){
        self.onDemandCollectors.remove(collector);
    }
    self.selectedResources = ko.observableArray([]); 
    self.onDemandColId = "onDemandColId_" + self.createdAt;
    self.collectorId = "collectorId_" + self.createdAt;
    self.collectorIdHref = "#"+self.collectorId;
    self.defaultResourcesId = "defaultResourcesId_" + self.createdAt;
    self.monitorResourcesId = "monitorResourcesId_" + self.createdAt;
    self.monitorResourcesHref = "#" + self.monitorResourcesId;
    self.isInitialized = false;
    self.isDirty = ko.computed(function(){
        self.agent();
        self.onDemandCollectors();
        self.selectedResources();
        if(!self.isInitialized){
            //self.isInitialized = true;
            return false;
        }
        return true;
    });
    self.hasChanged = function(){
        if(self.isDirty()) return true;
        var odColChanged = false;
        $.each(self.onDemandCollectors(), function(oIndex, odCol){
            if(odCol.hasChanged()) odColChanged=true;
        });
        return odColChanged;
    }
}

var OnDemandCollector = function(collectors){
    var self = this;
    self.colName = ko.observable("Collector");
    self.availableCollectors = collectors;
    self.getCollectorNames = function(){
        var names = ["Choose Collector"];
        $.each(collectors, function(index, collector){
            names.push(collector["name"]);
        });
        return names;
    }
    self.collectorNames = ko.observableArray(self.getCollectorNames());
    self.selectedCollector = ko.observable("Choose Collector");
    self.collectorParameters = ko.computed(function(){
        var result = [];
        $.each(collectors, function(index, collector){
            if(collector["name"]==self.selectedCollector()){
                $.each(collector["requiredParams"], function(index, param){
                    result.push({"paramKey":param, "paramValue":ko.observable("")});
                });
            }
        });
        return result;
    });
    self.isDefault = ko.computed(function(){
        return self.selectedCollector()!="Choose Collector";
    })
    self.monClass = ko.computed(function(){
        var cls = "";
        $.each(collectors, function(index, collector){
            if(self.selectedCollector()==collector["name"]){
                cls = collector["klass"];
            } 
        });
        return cls;
    });
    self.interval = ko.observable(20);
    self.isInitialized = false;
    self.isDirty = ko.computed(function(){
        self.colName();
        self.selectedCollector();
        self.interval();
        if(!self.isInitialized){
            //self.isInitialized = true;
            return false;
        }
        return true;
    });
    self.hasChanged = function(){
        if(self.isDirty()) return true;
        return false;
    }
}


function createJsonFromView(){
    var runJson = {};
    var model = window.viewModel;
    runJson["runName"] = model.runName();
    runJson["businessUnit"] = model.selectedBu();
    runJson["team"] = model.selectedTeam();
    runJson["description"] = model.desc();
    runJson["loadParts"] = [];
    $.each(model.loadPart(), function(index, lpart){
        var lp = {};
        lp["name"] = lpart.loadPartName();
        lp["agents"] = lpart.agents();
        lp["classes"] = [];
        lp["inputFileResources"] = lpart.useInputResources();
        if(model.selectedBu()==model.selectedTeam())
            lp["agentTags"] = [model.selectedTeam()];
        else
            lp["agentTags"] = [model.selectedBu(), model.selectedTeam()];
        var load = {};
        load["logLevel"] = lpart.logLevel();
        if(lpart.setupGroup().functions().length==0)
            load["setupGroup"] = null;
        else
            load["setupGroup"] = getGroupJson(lpart.setupGroup(), lp);
        load["groups"] = [];
        $.each(lpart.groups(), function(grpIndex, grp){
            var gr={};
            gr["name"] = grp.groupName();
            gr["groupStartDelay"] = grp.groupStartDelay();
            gr["threadStartDelay"] = grp.threadStartDelay();
            gr["throughput"] = grp.throughput();
            gr["repeats"] = grp.repeats();
            gr["duration"] = grp.duration();
            gr["threads"] = grp.threads();
            gr["warmUpRepeats"] = grp.warmUpRepeats();
            gr["functions"] = [];
            $.each(grp.functions(), function(funcIndex, func){
                var fun = {};
                fun["functionalityName"] = func.functionName();
                fun["functionClass"] = func.selectedFunction();
                fun["dumpData"] = func.dumpData();
                fun["params"] = {};
                if(func.availableParameters().inputParameters!=undefined){
                    $.each(func.availableParameters().inputParameters(), function(paramIndex, param){
                        fun["params"][param.key] = param.val();
                    });
                }
                fun["customTimers"] = func.selectedCustomTimers();
                fun["customHistograms"] = func.selectedHistograms();
                fun["customCounters"] = func.selectedCustomCounters();
                if(lp["classes"].indexOf(fun["functionClass"])==-1) lp["classes"].push(fun["functionClass"]);
                gr["functions"].push(fun);
            });
            gr["dependOnGroups"] = grp.dependsOn();
            gr["params"] = grp.params();
            gr["timers"] = getTimerJson(grp.timers());
            gr["threadResources"] = grp.threadResources();
            gr["dataGenerators"] = getDataGeneratorsJson(grp.dataGenerators());
            load["groups"].push(gr);
        });
        if(lpart.tearDownGroup().functions().length==0)
            load["tearDownGroup"] = null;
        else
            load["tearDownGroup"] = getGroupJson(lpart.tearDownGroup(), lp);
        load["dataGenerators"] = getDataGeneratorsJson(lpart.dataGenerators());
        lp["load"] = load;
        runJson["loadParts"].push(lp);
    });
    runJson["onDemandMetricCollections"] = [];
    runJson["metricCollections"] = [];
    $.each(model.metricCollections(), function(index, collector){
        var monAg={};
        monAg["agent"] = collector.agent();
        monAg["collectionInfo"] = {};
        monAg["collectionInfo"]["resources"] = collector.selectedResources();
        monAg["collectionInfo"]["lastHowManyInstances"] = 1;
        monAg["collectionInfo"]["publishUrl"] = "http://" + window.location.hostname + ":9999/loader-server/jobs/{jobId}/monitoringStats";
        monAg["collectionInfo"]["forHowLong"] = 0;
        monAg["collectionInfo"]["interval"] = 20000;
        var collcs = {};
        collcs["agent"] = collector.agent();
        collcs["collectors"] = []
        $.each(collector.onDemandCollectors(), function(odIndex, odCollector){
            var onDemandCol = {};
            onDemandCol["name"]=odCollector.colName();
            onDemandCol["klass"]=odCollector.monClass();
            onDemandCol["interval"]=odCollector.interval();
            onDemandCol["params"] ={};
            $.each(odCollector.collectorParameters(), function(paramIndex, param){
                onDemandCol["params"][param.paramKey]=param.paramValue()==undefined?"":param.paramValue();
            });
            collcs["collectors"].push(onDemandCol);
            monAg["collectionInfo"]["resources"].push(odCollector.colName());
        });
        runJson["onDemandMetricCollections"].push(collcs);
        runJson["metricCollections"].push(monAg);
    });
    return runJson;
}

function getGroupJson(grpModel, loadPartJson){
    var grp = grpModel;
    var gr = {};
    gr["name"] = grp.groupName();
    gr["groupStartDelay"] = grp.groupStartDelay();
    gr["threadStartDelay"] = grp.threadStartDelay();
    gr["throughput"] = grp.throughput();
    gr["repeats"] = grp.repeats();
    gr["duration"] = grp.duration();
    gr["threads"] = grp.threads();
    gr["warmUpRepeats"] = grp.warmUpRepeats();
    gr["functions"] = [];
    $.each(grp.functions(), function(funcIndex, func){
        var fun = {};
        fun["functionalityName"] = func.functionName();
        fun["functionClass"] = func.selectedFunction();
        fun["dumpData"] = false;
        fun["params"] = {};
        if(func.availableParameters().inputParameters!=undefined){
            $.each(func.availableParameters().inputParameters(), function(paramIndex, param){
                fun["params"][param.key] = param.val();
            });
        }
        if(loadPartJson["classes"].indexOf(fun["functionClass"])==-1) loadPartJson["classes"].push(fun["functionClass"]);
        gr["functions"].push(fun);
    });
    gr["dependOnGroups"] = [];
    gr["params"] = grp.params();
    gr["timers"] = getTimerJson(grp.timers());
    gr["threadResources"] = grp.threadResources();
    gr["dataGenerators"] = getDataGeneratorsJson(grpModel.dataGenerators());
    return gr;
}

function getTimerJson(timersModel){
    var timers = [];
    $.each(timersModel, function(timerIndex, timer){
        var tm ={};
        tm["name"] = timer.timerName();
        tm["duration"] = timer.duration();
        tm["throughput"] = timer.throughput();
        tm["threads"] = timer.threads();
        timers.push(tm);
    });
    return timers;
}

function getDataGeneratorsJson(dataGenModelList){
    var json = {};
    $.each(dataGenModelList, function(index, model){
        var modelJson ={};
        modelJson["generatorName"] = model.generatorName();
        modelJson["generatorType"] = model.generatorType();
        modelJson["inputDetails"] = {};
        switch(model.generatorType()){
            case "COUNTER":
                modelJson["inputDetails"]["startValue"] = model.startValue();
                modelJson["inputDetails"]["jump"] = model.jump();
                modelJson["inputDetails"]["maxValue"] = model.maxValue();
                break;
            case "FIXED_VALUE":
                modelJson["inputDetails"]["value"] = model.startValue();
                break;
            case "RANDOM_FLOAT":
                break;
            case "RANDOM_NUMBER":
                modelJson["inputDetails"]["maxValue"] = model.maxValue();
                break;
            case "RANDOM_SELECTION":
                modelJson["inputDetails"]["selectionSet"] = [];
                $.each(model.selectionList(), function(index, elem){
                    modelJson["inputDetails"]["selectionSet"].push(elem.listValue());
                });
                break;
            case "USE_AND_REMOVE":
                modelJson["inputDetails"]["selectionSet"] = [];
                $.each(model.selectionList(), function(index, elem){
                    modelJson["inputDetails"]["selectionSet"].push(elem.listValue());
                });
                break;
            case "CYCLIC_SELECTION":
                modelJson["inputDetails"]["selectionSet"] = [];
                $.each(model.selectionList(), function(index, elem){
                    modelJson["inputDetails"]["selectionSet"].push(elem.listValue());
                });
                break;
            case "RANDOM_STRING":
                modelJson["inputDetails"]["type"] = model.stringType();
                modelJson["inputDetails"]["length"] = model.stringLength();
                modelJson["inputDetails"]["closedString"]= model.closedString();
                break;
            case "RANDOM_DISTRIBUTION":
                modelJson["inputDetails"]["distributionInfoList"]=[];
                $.each(model.distributionInfoList(), function(listIndex, elem){
                    modelJson["inputDetails"]["distributionInfoList"].push({"start":elem.start(),"end":elem.end(),"value":elem.val()});
                });
                break;
        }
        json[model.generatorName()] = modelJson;
    });
    return json;
}

function createTree(){ 
    var model = window.viewModel;
    $("#runTree").jstree({
            "json_data" : {
                "data" : [{
                    "attr":{"id" : model.nodeId, "rel":"run"},
                    "data" : model.runName(), 
                    "metadata" : { "name" : model.runName(), "nodeType" : "run"},    
                    "children" : getRunSchemaChildren(model)
                    }],
                "progressive_render" : true,
            },
            "contextmenu" :{
                "items": function(data){
                    var defaultVal = {
                        "rename": false,
                        "delete": false,
                        "edit": false,
                        "create": false
                    }
                    switch($(data[0]).data('nodeType')){
                        case 'run':
                            defaultVal["addLoadPart"] = {
                                    "label": "Add LoadPart",
                                    "action": function(){
                                        window.viewModel.addLoadPart();
                                    }
                                };
                            defaultVal["addMonitoringAgent"] = {
                                    "label": "Add Monitoring Agent",
                                    "action": function(){
                                        window.viewModel.addMonitoringAgent();
                                    }
                                };
                            return defaultVal;
                        case 'loadParts':
                            defaultVal["addLoadPart"] = {
                                "label": "Add LoadPart",
                                "action": function(){
                                    window.viewModel.addLoadPart();
                                }
                            }
                            return defaultVal;
                        case 'loadPart':
                            defaultVal["addGroup"]= {
                                "label": "Add Group",
                                "action": function(){
                                    var loadPartIndex = $(data[0]).data('loadPartIndex');
                                    window.viewModel.loadPart()[loadPartIndex].addGroup();
                                }
                            };
                            defaultVal["delete"] = {
                                "label": "Delete",
                                "action": function(){
                                    var loadPartIndex = $(data[0]).data('loadPartIndex');
                                    var loadPart = window.viewModel.loadPart()[loadPartIndex];
                                    window.viewModel.deleteLoadPart(loadPart);
                                }
                            };
                            return defaultVal;
                        case 'group':
                            defaultVal["addFunction"]={
                                "label": "Add Function",
                                "action": function(){
                                    var loadPartIndex = $(data[0]).data('loadPartIndex');
                                    var grpIndex = $(data[0]).data('groupIndex');
                                    window.viewModel.loadPart()[loadPartIndex].groups()[grpIndex].addFunction();
                                }
                            };
                            defaultVal["delete"]= {
                                "label": "Delete",
                                "action": function(){
                                    var loadPartIndex = $(data[0]).data('loadPartIndex');
                                    var grpIndex = $(data[0]).data('groupIndex');
                                    var grp = window.viewModel.loadPart()[loadPartIndex].groups()[grpIndex];
                                    window.viewModel.loadPart()[loadPartIndex].deleteGroup(grp);
                                }
                            }
                            return defaultVal;
                        case 'setupGroup':
                            defaultVal["addFunction"]={
                                "label": "Add Function",
                                "action": function(){
                                    var loadPartIndex = $(data[0]).data('loadPartIndex');
                                    window.viewModel.loadPart()[loadPartIndex].setupGroup().addFunction();
                                }
                            };
                            return defaultVal;
                        case 'tearDownGroup':
                            defaultVal["addFunction"]={
                                "label": "Add Function",
                                "action": function(){
                                    var loadPartIndex = $(data[0]).data('loadPartIndex');
                                    window.viewModel.loadPart()[loadPartIndex].tearDownGroup().addFunction();
                                }
                            };
                            return defaultVal;
                        case 'function':
                            defaultVal["delete"] = {
                                "label": "Delete",
                                "action": function(){
                                    var loadPartIndex = $(data[0]).data('loadPartIndex');
                                    var grpIndex = $(data[0]).data('groupIndex');
                                    var funcIndex = $(data[0]).data('functionIndex');
                                    var func = window.viewModel.loadPart()[loadPartIndex].groups()[grpIndex].functions()[funcIndex];
                                    window.viewModel.loadPart()[loadPartIndex].groups()[grpIndex].deleteFunction(func);
                                }
                            }
                            return defaultVal;
                        case 'setupFunction':
                            defaultVal["delete"] = {
                                "label": "Delete",
                                "action": function(){
                                    var loadPartIndex = $(data[0]).data('loadPartIndex');
                                    var funcIndex = $(data[0]).data('functionIndex');
                                    var func = window.viewModel.loadPart()[loadPartIndex].setupGroup().functions()[funcIndex];
                                    window.viewModel.loadPart()[loadPartIndex].setupGroup().deleteFunction(func);
                                }
                            }
                            return defaultVal;
                        case 'tearDownFunction':
                            defaultVal["delete"] = {
                                "label": "Delete",
                                "action": function(){
                                    var loadPartIndex = $(data[0]).data('loadPartIndex');
                                    var funcIndex = $(data[0]).data('functionIndex');
                                    var func = window.viewModel.loadPart()[loadPartIndex].tearDownGroup().functions()[funcIndex];
                                    window.viewModel.loadPart()[loadPartIndex].tearDownGroup().deleteFunction(func);
                                }
                            }
                            return defaultVal;
                        case 'monitoringAgents':
                            defaultVal["addMonitoringAgent"] = {
                                "label":"Add Monitoring Agent",
                                "action": function(){
                                    window.viewModel.addMonitoringAgent();
                                }
                            }
                            return defaultVal;
                        case 'metricCollection':
                            defaultVal["delete"] = {
                                "label": "Delete",
                                "action": function(){
                                    var metricCollectionIndex = $(data[0]).data('metricCollectionIndex');
                                    var metricCollector = window.viewModel.metricCollections()[metricCollectionIndex];
                                    window.viewModel.deleteMonitoringAgent(metricCollector);
                                }
                            }
                            return defaultVal;
                        default :
                            return {
                                "rename": false,
                                "delete": false,
                                "edit": false,
                                "create": false
                            }
                    }
                },
                "select_node" : true
            },
            "dnd" : {
                "drop_target" : false,
                "drag_target" : false
            },
            "types": {
                "valid_children":["run"],
                "types": {
                    "run":{
                        "icon":{
                            "image":"../img/run.png"
                        },
                        "valid_children":["loadPart", "monitoringAgents"]
                    },
                    "loadParts":{
                        "icon":{
                            "image":"../img/loadpart.png"
                        },
                        "valid_children":["loadPart"]
                    },
                    "loadPart":{
                        "icon":{
                            "image":"../img/loadpart.png"
                        },
                        "valid_children":["group"]
                    },
                    "group":{
                        "icon":{
                            "image":"../img/group.png"
                        },
                        "valid_children":["function"]
                    },
                    "function":{
                        "icon":{
                            "image":"../img/function.png"
                        },
                        "valid_children":[]
                    },
                    "monitoringAgents":{
                        "icon":{
                            "image":"../img/monagents.png"
                        },
                        "valid_children":["metricCollection"]
                    },
                    "metricCollection":{
                        "icon":{
                            "image":"../img/metriccol.png"
                        },
                        "valid_children":[]
                    },
                    "onDemandCol":{
                        "icon":{
                            "image":"../img/metriccol.png"
                        },
                        "valid_children":[]
                    }
                }
            },
            "plugins" : [ "themes", "json_data", "ui", "cookies", "dnd","types", "contextmenu"],
            "cookies" : {
                            "save_selected":true,
                            "save_opened":true,
                            "auto_save":true,
                            "cookie_options":{}
                        },
            "ui" : {
                    "selected_parent_open":true
                    }
    }).bind("select_node.jstree", function(event, data){
        hideAll();
        switch(data.rslt.obj.data("nodeType")){
            case "run":
                var model = window.viewModel;
                model.isVisible(true);
                window.currentModel = model;
                break;
            case "loadParts":
                var model = window.viewModel;
                model.loadPartsVisible(true);
                window.currentModel = model;
                break;
            case "loadPart":
                var loadPartIndex = data.rslt.obj.data("loadPartIndex");
                var model = window.viewModel.loadPart()[loadPartIndex];
                model.isVisible(true);
                window.currentModel = model;
                break;
            case "setupGroup":
                var loadPartIndex = data.rslt.obj.data("loadPartIndex");
                var model = window.viewModel.loadPart()[loadPartIndex];
                model.setupVisible(true);
                window.currentModel = model;
                break;
            case "group":
                var loadPartIndex = data.rslt.obj.data("loadPartIndex");
                var groupIndex = data.rslt.obj.data("groupIndex");
                var model = window.viewModel.loadPart()[loadPartIndex].groups()[groupIndex];
                model.isVisible(true);
                window.currentModel = model;
                break;
            case "tearDownGroup":
                var loadPartIndex = data.rslt.obj.data("loadPartIndex");
                var model = window.viewModel.loadPart()[loadPartIndex];
                model.tearDownVisible(true);
                window.currentModel = model;
                break;
            case "function":
                var loadPartIndex = data.rslt.obj.data("loadPartIndex");
                var groupIndex = data.rslt.obj.data("groupIndex");
                var functionIndex = data.rslt.obj.data("functionIndex");
                var model = window.viewModel.loadPart()[loadPartIndex].groups()[groupIndex].functions()[functionIndex];
                model.isVisible(true);
                window.currentModel = model;
                break;
            case "setupFunction":
                var loadPartIndex = data.rslt.obj.data("loadPartIndex");
                var functionIndex = data.rslt.obj.data("functionIndex");
                var model = window.viewModel.loadPart()[loadPartIndex].setupGroup().functions()[functionIndex];
                model.isVisible(true);
                window.currentModel = model;
                break;
            case "tearDownFunction":
                var loadPartIndex = data.rslt.obj.data("loadPartIndex");
                var functionIndex = data.rslt.obj.data("functionIndex");
                var model = window.viewModel.loadPart()[loadPartIndex].tearDownGroup().functions()[functionIndex];
                model.isVisible(true);
                window.currentModel = model;
                break;
            case "monitoringAgents":
                var model = window.viewModel;
                model.monAgentsVisible(true);
                window.currentModel = model;
                break;
            case "metricCollection":
                var metricCollectionIndex = data.rslt.obj.data("metricCollectionIndex");
                var model = window.viewModel.metricCollections()[metricCollectionIndex];
                model.isVisible(true);
                window.currentModel = model;
                break;
            case "onDemandCol":
                var model = window.viewModel;
                model.onDemandColsVisible(true);
                window.currentModel = model;
                break;
        }
        
    });
    $("#runTree").bind("loaded.jstree", function (event, data) {
        $("#runTree").jstree("open_all");
    });
    $("#runTree").bind("refresh.jstree", function (event, data) {
        $("#runTree").jstree("open_all");
    });
}

function hideAll(){
    var model = window.viewModel;
    model.isVisible(false);
    model.monAgentsVisible(false);
    model.loadPartsVisible(false);
    $.each(model.loadPart(), function(index, lPart){
        lPart.isVisible(false);
        $.each(lPart.groups(), function(index, grp){
            grp.isVisible(false);
            grp.timersVisible(false);
            $.each(grp.functions(), function(index, func){
                func.isVisible(false);
            });
        });
        lPart.setupVisible(false);
        $.each(lPart.setupGroup().functions(), function(index, func){
            func.isVisible(false);
        });
        lPart.tearDownVisible(false);
        $.each(lPart.tearDownGroup().functions(), function(index, func){
            func.isVisible(false);
        });
    });
    $.each(model.metricCollections(), function(index, metricCollector){
        metricCollector.isVisible(false);
    });
}

function getRunSchemaChildren(model){
    return [{
            "attr":{"id":"node_loadParts","rel":"loadParts"},
            "data":"LoadParts",
            "metadata":{"nodeType":"loadParts"},
            "children": getLoadParts(model)
        },{
            "attr":{"id":"node_monitoringAgents","rel":"monitoringAgents"},
            "data":"MonitoringAgents",
            "metadata":{"nodeType":"monitoringAgents"},
            "children": getMetricCollectors(model)
        }]
}

function getLoadParts(model){
    var loadPartsName = new Array();
    var loadParts = model.loadPart();
    if (loadParts.length==0) return undefined;
    for (var k=0; k<loadParts.length; k++){
        var grps = [{
            "attr":{"id":"setupGroup_" + k, "rel":"group"}, 
            "data" : loadParts[k].setupGroup().groupName(), 
            "metadata" : {"nodeType":"setupGroup", "loadPartIndex": k},
            "children": getFunctionListForFixedGroup(loadParts[k].setupGroup(), k, "setupFunction")
        }]
        grps= grps.concat(getGroupList(loadParts[k], k));
        grps.push({
            "attr":{"id":"tearDownGroup_" + k, "rel":"group"}, 
            "data": loadParts[k].tearDownGroup().groupName(), 
            "metadata" : {"nodeType":"tearDownGroup", "loadPartIndex": k},
            "children": getFunctionListForFixedGroup(loadParts[k].tearDownGroup(), k, "tearDownFunction")
        });
        loadPartsName[k]={"attr" :{"id": loadParts[k].nodeId, "rel":"loadPart"}, "data": loadParts[k].loadPartName(), "metadata" : {"nodeType":"loadPart", "loadPartIndex": k}, "children": grps};
    }
    return loadPartsName;
}

function getMetricCollectors(model){
    var metricsAgents = new Array();
    var metricsCollectors = model.metricCollections();
    if(metricsCollectors.length==0) return undefined;
    for( var k=0; k<metricsCollectors.length;k++){
        metricsAgents[k]={"attr":{"id": metricsCollectors[k].nodeId, "rel":"metricCollection"}, "data": metricsCollectors[k].agent(), "metadata": {"nodeType":"metricCollection", "metricCollectionIndex":k}};
    }
    return metricsAgents;
}

function getGroupList(model, loadPartIndex){
    var groupName = new Array();
    var groups = model.groups();
    if (groups.length==0) return undefined;
    for(var i=0;i<groups.length;i++){
        groupName[i]={ "attr":{"id": groups[i].nodeId, "rel":"group"},"data": groups[i].groupName(), "metadata" :{"nodeType":"group", "loadPartIndex": loadPartIndex, "groupIndex": i} ,"children" : getFunctionList(groups[i], loadPartIndex ,i)};
    }
    return groupName;
}

function getFunctionList(model, loadPartIndex, groupIndex){
    var functionName = new Array();
    var functions = model.functions();
    if (functions.length==0) return undefined;
    for(var i=0;i<functions.length; i++){
        functionName[i]={"attr":{"id":functions[i].nodeId, "rel":"function"}, "type":"function","metadata": {"nodeType":"function", "loadPartIndex":loadPartIndex, "groupIndex": groupIndex, "functionIndex":i}, "data": functions[i].functionName()};
    }
    return functionName;
}

function getFunctionListForFixedGroup(model, loadPartIndex, type){
    var funcName = [];
    var functions = model.functions();
    if(functions.length==0) return undefined;
    for(var i=0;i<functions.length;i++){
        funcName[i] = {
            "attr": {"id": functions[i].nodeId, "rel": "function"},
            "type": type,
            "metadata": {"nodeType": type, "loadPartIndex":loadPartIndex, "functionIndex":i}, 
            "data": functions[i].functionName()
        }
    }
    return funcName;
}

function selectNode(node){
    $("#runTree").bind("reselect.jstree", function(){
        $("#runTree").jstree("select_node","#" + node);
    });
}

function createRun(){
    window.createPressed = true;
    var runJson ={};
    if(window.selectedView=='json'){
        runJson = $.parseJSON($("#runJson").val());
        window.viewModel.runName(runJson["runName"]);
    } else {
        runJson = createJsonFromView();
    } 
    var result = checkValidity(runJson);
    var isValid = result["isValid"];
    var alertMsg = result["alertMessage"];
    if (!isValid){
        $("#alertMsg").empty();
        $("#alertMsg").removeClass("alert-success");
        $("#alertMsg").addClass("alert-error");
        $("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"reload()\">&times;</button>");
        $("#alertMsg").append("<h4>Error!!</h4> " +  alertMsg);
        $("#alertMsg").css("display", "block");
        return;
    }
    $.ajax({
        url:"loader-server/runs",
        contentType: "application/json", 
        type:"POST",
        processData:false,
        data: JSON.stringify(runJson),
        success: function(data){
        },
        error: function(err){
        },
        complete: function(xhr, status){
            $("#success").empty();
            switch (xhr.status){
                case 201:
                    $("#alertMsg").empty();
                    $("#alertMsg").removeClass("alert-error");
                    $("#alertMsg").addClass("alert-success");
                    $("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"goToUpdate()\">&times;</button>");
                    $("#alertMsg").append("<h4>Success!!</h4> Run Created successfully!!");
                    $("#alertMsg").append("<br>Redirecting to update run page...");
                    $("#alertMsg").css("display", "block");
                    setTimeout(function(){goToUpdate();},3000);
                    break;
                case 409:
                    $("#alertMsg").empty();
                    $("#alertMsg").removeClass("alert-success");
                    $("#alertMsg").addClass("alert-error");
                    $("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"returnToPage()\">&times;</button>");
                    $("#alertMsg").append("<h4>Error!!</h4> Run name conflict!!");
                    $("#alertMsg").css("display", "block");
                    break;
                case 400:
                    $("#alertMsg").empty();
                    $("#alertMsg").removeClass("alert-success");
                    $("#alertMsg").addClass("alert-error");
                    $("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"returnToPage()\">&times;</button>");
                    $("#alertMsg").append("<h4>Error!!</h4> Invalid options!!");
                    $("#alertMsg").css("display", "block");
                default :
                    $("#alertMsg").empty();
                    $("#alertMsg").removeClass("alert-success");
                    $("#alertMsg").addClass("alert-error");
                    $("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"returnToPage()\">&times;</button>");
                    $("#alertMsg").append("<h4>Error!!</h4> Run creation failed!!");
                    $("#alertMsg").css("display", "block");
             }
        }
    })
}

function checkValidity(runJson){
    var lpNames = [];
    var result = {"isValid":true,"alertMessage":""};
    $.each(runJson["loadParts"], function(lIndex, lPart){
        if(lpNames.indexOf(lPart["name"])==-1){
            lpNames.push(lPart["name"]);
        } else {
            result["isValid"]=false;
            result["alertMessage"]="Please use different names for loadparts.";
            return false;
        }
        var setupGroup = lPart["load"]["setupGroup"];
        if(setupGroup!=null){
            var funcNames = [];
            $.each(setupGroup["functions"], function(fIndex, func){
                if(funcNames.indexOf(func["functionalityName"])!=-1){
                    result["isValid"]=false;
                    result["alertMessage"]="Please use different names for functions in " + setupGroup["name"] + ".";
                    return false; 
                }
                if(func["functionalityName"].indexOf(" ")!=-1 || func["functionalityName"]==""){
                    result["isValid"]=false;
                    result["alertMessage"]="Invalid function name " + func["functionalityName"] + ", Blank/Space/Tabs not allowed in name.";
                    return false; 
                }
                if(func["functionClass"].indexOf("Choose")!=-1){
                    result["isValid"]=false;
                    result["alertMessage"]="Invalid function class in " + func["functionalityName"] + ", You need to select a class.";
                }
                funcNames.push(func["functionalityName"]);
            });    
        }
        var tearDownGroup = lPart["load"]["tearDownGroup"];
        if(tearDownGroup!=null){
            var funcNames = [];
            $.each(tearDownGroup["functions"], function(fIndex, func){
                if(funcNames.indexOf(func["functionalityName"])!=-1){
                    result["isValid"]=false;
                    result["alertMessage"]="Please use different names for functions in " + tearDownGroup["name"] + ".";
                    return false; 
                }
                if(func["functionalityName"].indexOf(" ")!=-1 || func["functionalityName"]==""){
                    result["isValid"]=false;
                    result["alertMessage"]="Invalid function name " + func["functionalityName"] + ", Blank/Space/Tabs not allowed in name.";
                    return false; 
                }
                if(func["functionClass"].indexOf("Choose")!=-1){
                    result["isValid"]=false;
                    result["alertMessage"]="Invalid function class in " + func["functionalityName"] + ", You need to select a class.";
                }
                funcNames.push(func["functionalityName"]);
            });    
        }
        var grpNames = [];
        $.each(lPart["load"]["groups"], function(gIndex, grp){
            if(grpNames.indexOf(grp["name"])!=-1){
                result["isValid"]=false;
                result["alertMessage"]="Please use different names for groups in " + lPart["name"] + ".";
                return false;
            }
            if(grp["name"].indexOf(" ")!=-1 || grp["name"]==""){
                result["isValid"]=false;
                result["alertMessage"]="Invalid group name " + grp["name"] + ", Blank/Space/tabs not allowed in name.";
                return false; 
            }
            grpNames.push(grp["name"]);
            var funcNames = [];
            $.each(grp["functions"], function(fIndex, func){
                if(funcNames.indexOf(func["functionalityName"])!=-1){
                    result["isValid"]=false;
                    result["alertMessage"]="Please use different names for functions in " + grp["name"] + ".";
                    return false; 
                }
                if(func["functionalityName"].indexOf(" ")!=-1 || func["functionalityName"]==""){
                    result["isValid"]=false;
                    result["alertMessage"]="Invalid function name " + func["functionalityName"] + ", Blank/Space/Tabs not allowed in name.";
                    return false; 
                }
                if(func["functionClass"].indexOf("Choose")!=-1){
                    result["isValid"]=false;
                    result["alertMessage"]="Invalid function class in " + func["functionalityName"] + ", You need to select a class.";
                }
                funcNames.push(func["functionalityName"]);
            });
        });
    });
    var colNames = [];
    $.each(runJson["metricCollections"], function(iindex, collector){
        if(colNames.indexOf(collector["agent"])!=-1){
            result["isValid"]=false;
            result["alertMessage"]="Do you really want two monitoring agent with same IP " + collector["agent"] + ", club them together";
            return false;
        }
    });
    var colNames = [];
    $.each(runJson["onDemandMetricCollections"], function(iindex, agent){
        var colNames = [];
        $.each(agent["collectors"], function(index, collector){
            if(colNames.indexOf(collector["name"])!=-1){
                result["isValid"]=false;
                result["alertMessage"]="You have two collectors with same name for agent " + agent["agent"] + ", You will loose data for one.";
                return false;
            }
        });
    });
    return result;
}

function goToUpdate(){
    window.location = "/updaterun.html?&runName=" + window.viewModel.runName();
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

function getRunSchema(){
    var runName = getQueryParams("runName");
    var runJson ={}
    $.ajax({
        url:"loader-server/runs/" + runName,
        contentType: "application/json", 
        type:"GET",
        async: false,
        success: function(data){
            runJson = data;
        }
    });
    ko.applyBindings(createViewFromJson(runJson));
}

function createViewFromJson(runJson){
    var runViewModel = new runJsonViewModel();
    runViewModel.runName(runJson["runName"]);
    runViewModel.selectedBu(runJson["businessUnit"]);
    runViewModel.desc(runJson["description"]);
    runViewModel.isVisible(true);
    $.each(runJson["loadParts"], function(lPartIndex, lPart){
        var loadPartModel = new loadPartViewModel();
        loadPartModel.loadPartName(lPart["name"]);
        loadPartModel.agents(lPart["agents"]);
        loadPartModel.useInputResources(lPart["inputFileResources"]);
        loadPartModel.logLevel(lPart["load"]["logLevel"]);
        loadPartModel.setupGroup(createGroupModel(lPart["load"]["setupGroup"], "setup"));
        loadPartModel.tearDownGroup(createGroupModel(lPart["load"]["tearDownGroup"], "tearDown"));
        $.each(lPart["load"]["groups"], function(grpIndex, grp){
            loadPartModel.groups.push(createGroupModel(grp));
        });
        $.each(lPart["load"]["dataGenerators"], function(key, dataGen){
            loadPartModel.dataGenerators.push(createDatagenModel(dataGen));
        });
        runViewModel.loadPart.push(loadPartModel);
    });
    $.each(runJson["metricCollections"], function(metricIndex, metricCollector){
        runViewModel.metricCollections.push(createMonitoringAgentModel(metricCollector, runJson["onDemandMetricCollections"]));
    });
    window.viewModel = runViewModel;
    return runViewModel;
    
}

function createGroupModel(grp, type){
    var grpModel = new groupViewModel();
    if(grp==null) {
        if(type=="setup") 
            grpModel.groupName("Setup");
        if(type=="tearDown")
            grpModel.groupName("TearDown");
        return grpModel
    }
    grpModel.groupName(grp["name"]);
    grpModel.groupStartDelay(grp["groupStartDelay"]);
    grpModel.threadStartDelay(grp["threadStartDelay"]);
    grpModel.throughput(grp["throughput"]);
    grpModel.repeats(grp["repeats"]);
    grpModel.duration(grp["duration"]);
    grpModel.threads(grp["threads"]);
    grpModel.warmUpRepeats(grp["warmUpRepeats"]);
    grpModel.dependsOn(grp["dependOnGroups"]);
    $.each(grp["functions"], function(funIndex, func){
        grpModel.functions.push(createFunctionModel(func));
    });
    $.each(grp["timers"], function(timerIndex, timer){
        grpModel.timers.push(createTimerModel(timer));
    });
    $.each(grp["dataGenerators"], function(key, dataGen){
        grpModel.dataGenerators.push(createDatagenModel(dataGen));
    });
    return grpModel;
}

function createFunctionModel(func){
    var funcModel = new functionViewModel();
    funcModel.functionName(func["functionalityName"]);
    funcModel.selectedFunction(func["functionClass"]);
    funcModel.dumpData(func["dumpData"]);
    funcModel.selectedHistograms(func["customHistograms"]);
    funcModel.selectedCustomTimers(func["customTimers"]);
    funcModel.selectedCustomCounters(func["customCounters"]);
    $.each(funcModel.availableParameters().inputParameters(), function(index, inputParam){
        if(inputParam.isScalar){
            if(func["params"][inputParam.key]!=undefined && func["params"][inputParam.key]!=null && $.type(func["params"][inputParam.key])==="string"){
                inputParam.scalarValue(func["params"][inputParam.key]);
            } else {
                inputParam.scalarValue("");
            }
        } else {
            if(inputParam.isList){
                var list = [];
                if(func["params"][inputParam.key]!=undefined && func["params"][inputParam.key]!=null && $.type(func["params"][inputParam.key])==="array"){
                    $.each(func["params"][inputParam.key], function(keyIndex, param){
                        list.push({"keyValue": ko.observable(param)});
                    });
                }
                inputParam.listValue(list);
            } else {
                var mapList = [];
                if(func["params"][inputParam.key]!=undefined && func["params"][inputParam.key]!=null && $.isPlainObject(func["params"][inputParam.key])){
                    $.each(func["params"][inputParam.key], function(k,v){
                        mapList.push({"name":ko.observable(k.replace(/"/g,"")), "keyValue":ko.observable(v)});
                    });
                }
                inputParam.mapValue(mapList);
            }
        } 
    });
    return funcModel;
}

function createTimerModel(timer){
    var timerModel = new timerViewModel();
    timerModel.timerName(timer["name"]);
    timerModel.threads(timer["threads"]);
    timerModel.throughput(timer["throughput"]);
    timerModel.duration(timer["duration"]);
    return timerModel;
}

function createDatagenModel(dataGen){
    var dataGenModel = new dataGeneratorViewModel();
    dataGenModel.generatorName(dataGen["generatorName"]);
    dataGenModel.generatorType(dataGen["generatorType"]);
    switch(dataGen["generatorType"]){
    case "COUNTER":
        dataGenModel.startValue(dataGen["inputDetails"]["startValue"]);
        dataGenModel.jump(dataGen["inputDetails"]["jump"]);
        dataGenModel.maxValue(dataGen["inputDetails"]["maxValue"]);
        break;
    case "FIXED_VALUE":
        dataGenModel.startValue(dataGen["inputDetails"]["value"]);
        break;
    case "RANDOM_NUMBER":
        dataGenModel.maxValue(dataGen["inputDetails"]["maxValue"]);
        break;
    case "RANDOM_SELECTION":
        var selectionList = [];
        $.each(dataGen["inputDetails"]["selectionSet"], function(index, selection){
            selectionList.push({"listValue": ko.observable(selection)});
        })
        dataGenModel.selectionList(selectionList);
        break;
    case "USE_AND_REMOVE":
        var selectionList = [];
        $.each(dataGen["inputDetails"]["selectionSet"], function(index, selection){
            selectionList.push({"listValue": ko.observable(selection)});
        })
        dataGenModel.selectionList(selectionList);
        break;
    case "CYCLIC_SELECTION":
        var selectionList = [];
        $.each(dataGen["inputDetails"]["selectionSet"], function(index, selection){
            selectionList.push({"listValue": ko.observable(selection)});
        })
        dataGenModel.selectionList(selectionList);
        break;
    case "RANDOM_STRING":
        dataGenModel.stringType(dataGen["inputDetails"]["type"]);
        dataGenModel.stringLength(dataGen["inputDetails"]["length"]);
        dataGenModel.closedString(dataGen["inputDetails"]["closedString"]);
        break;
    case "RANDOM_DISTRIBUTION":
        $.each(dataGen["inputDetails"]["distributionInfoList"], function(index, elem){
            dataGenModel.distributionInfoList.push({"start":ko.observable(elem["start"]),"end":ko.observable(elem["end"]),"val":ko.observable(elem["value"])})
        });
    }
    return dataGenModel;
}

function createMonitoringAgentModel(metricCollector, onDemandCollectors){
    var monitoringModel = new monitoringViewModel();
    monitoringModel.agent(metricCollector["agent"]);
    var selectedRes = [];
    $.each(metricCollector["collectionInfo"]["resources"], function(resIndex, res){
        if(monitoringModel.availableResources().indexOf(res)!=-1) selectedRes.push(res);
    })
    monitoringModel.selectedResources(selectedRes);
    $.each(onDemandCollectors, function(odIndex, odCollector){
        if(odCollector["agent"]==metricCollector["agent"]){
            $.each(odCollector["collectors"], function(index, collector){
                var odColModel = new OnDemandCollector(monitoringModel.availableOnDemandCollectors());
                odColModel.colName(collector["name"]);
                odColModel.selectedCollector(getCollectorType(collector["klass"], monitoringModel.availableOnDemandCollectors()));
                odColModel.interval(collector["interval"]);
                $.each(odColModel.collectorParameters(), function(pIndex, param){
                    param.paramValue(collector["params"][param.paramKey]);
                });
                monitoringModel.onDemandCollectors.push(odColModel);
            });
        }
    });
    return monitoringModel;
}

function getCollectorType(klass, collectors){
    var nme = "";
    $.each(collectors, function(index, collector){
        if(klass==collector["klass"]){
            nme = collector["name"];
        } 
    });
    return nme;
}

function updateRun(){
    window.updatePressed = true;
    var runJson ={};
    if(window.selectedView=='json'){
        runJson = $.parseJSON($("#runJson").val());
    } else {
        runJson = createJsonFromView();
    } 
    var result = checkValidity(runJson);
    var isValid = result["isValid"];
    var alertMsg = result["alertMessage"];
    if (!isValid){
        $("#alertMsg").empty();
        $("#alertMsg").removeClass("alert-success");
        $("#alertMsg").addClass("alert-error");
        $("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"reload()\">&times;</button>");
        $("#alertMsg").append("<h4>Error!!</h4> " +  alertMsg);
        $("#alertMsg").css("display", "block");
        return;
    }
    $.ajax({
        url:"loader-server/runs/" + getQueryParams("runName"),
        contentType: "application/json", 
        type:"PUT",
        processData:false,
        data: JSON.stringify(runJson),
        success: function(data){
        },
        error: function(err){
        },
        complete: function(xhr, status){
            $("#success").empty();
            initializeState(false);
            switch (xhr.status){
                case 204:
                    $("#alertMsg").empty();
                    $("#alertMsg").removeClass("alert-error");
                    $("#alertMsg").addClass("alert-success");
                    $("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"goToUpdate()\">&times;</button>");
                    $("#alertMsg").append("<h4>Success!!</h4> Run Updated successfully!!");
                    $("#alertMsg").css("display", "block");
                    setTimeout(function(){goToUpdate();},5000);
                    break;
                case 409:
                    $("#alertMsg").empty();
                    $("#alertMsg").removeClass("alert-success");
                    $("#alertMsg").addClass("alert-error");
                    $("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"returnToPage()\">&times;</button>");
                    $("#alertMsg").append("<h4>Error!!</h4> Run name conflict!!");
                    $("#alertMsg").css("display", "block");
                    break;
                case 400:
                    $("#alertMsg").empty();
                    $("#alertMsg").removeClass("alert-success");
                    $("#alertMsg").addClass("alert-error");
                    $("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"returnToPage()\">&times;</button>");
                    $("#alertMsg").append("<h4>Error!!</h4> Invalid options!!");
                    $("#alertMsg").css("display", "block");
                default :
                    $("#alertMsg").empty();
                    $("#alertMsg").removeClass("alert-success");
                    $("#alertMsg").addClass("alert-error");
                    $("#alertMsg").append("<button type=\"button\" class=\"close\" data-dismiss=\"alert\" onClick=\"returnToPage()\">&times;</button>");
                    $("#alertMsg").append("<h4>Error!!</h4> Run creation failed!!");
                    $("#alertMsg").css("display", "block");
            }
        }
    })
}

function execRun(){
    var runName =  getQueryParams("runName");
    executeRun(runName);
}

function initializeState(value){
    var model = window.viewModel;
    window.viewModel.isInitialized=value;
    $.each(window.viewModel.loadPart(), function(index, lPart){
        lPart.isInitialized=value;
        $.each(lPart.groups(), function(index, grp){
            grp.isInitialized=value;
            $.each(grp.functions(), function(index, func){
                func.isInitialized=value;
                $.each(func.availableParameters().inputParameters(), function(pIndex, inputParam){
                    inputParam.isInitialized=value;
                });
            });
            $.each(grp.dataGenerators(), function(dIndex, dataGen){
                dataGen.isInitialized=value;
            });
            $.each(grp.timers(), function(tIndex, timer){
                timer.isInitialized=value;
            });
        });
        $.each(lPart.dataGenerators(), function(dIndex, dataGen){
            dataGen.isInitialized=value;
        });
        lPart.setupGroup().isInitialized=value;
        $.each(lPart.setupGroup().functions(), function(index, func){
            func.isInitialized=value;
        });
        $.each(lPart.setupGroup().dataGenerators(), function(index, dataGen){
            dataGen.isInitialized=value;
        });
        lPart.tearDownGroup().isInitialized=value;
        $.each(lPart.tearDownGroup().functions(), function(index, func){
            func.isInitialized=value;
        });
        $.each(lPart.tearDownGroup().dataGenerators(), function(index, dataGen){
            dataGen.isInitialized=value;
        });
    });
    $.each(model.metricCollections(), function(index, metricCollector){
        metricCollector.isInitialized=value;
        $.each(metricCollector.onDemandCollectors(), function(oIndex, odCol){
            odCol.isInitialized=value;
        });
    });
}
