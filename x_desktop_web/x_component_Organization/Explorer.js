MWF.xApplication.Organization.Explorer = new Class({
	Extends: MWF.widget.Common,
	Implements: [Options, Events],
	options: {
		"style": "default"
	},
	
	initialize: function(node, actions, options){
		this.setOptions(options);
		
		this.path = "/x_component_Organization/$Explorer/";
		this.cssPath = "/x_component_Organization/$Explorer/"+this.options.style+"/css.wcss";
		this._loadCss();
		
		this.actions = actions;
		this.node = $(node);
		
		this.loaddingElement = false;
		this.groups = [];
		this.isElementLoaded = false;
		this.loadElementQueue = 0;
		
		this.deleteElements = [];
	},
	clear: function(){
		this.loaddingElement = false;
		this.isElementLoaded = false;
		this.loadElementQueue = 0;
		this.chartNode.empty();
	},
	load: function(){
		this.loadLayout();
		this.loadChart();
	},
	loadLayout: function(){
		this.chartAreaNode = new Element("div", {"styles": this.css.chartAreaNode}).inject(this.node);
		this.propertyAreaNode = new Element("div", {"styles": this.css.propertyAreaNode}).inject(this.node);
		
		this.resizeBarNode = new Element("div", {"styles": this.css.resizeBarNode}).inject(this.propertyAreaNode);
		this.propertyNode = new Element("div", {"styles": this.css.propertyNode}).inject(this.propertyAreaNode);
		
		this.propertyTitleNode = new Element("div", {"styles": this.css.propertyTitleNode}).inject(this.propertyNode);
		this.propertyContentNode = new Element("div", {"styles": this.css.propertyContentNode}).inject(this.propertyNode);
		
		this.loadToolbar();
		this.chartScrollNode = new Element("div", {"styles": this.css.chartScrollNode}).inject(this.chartAreaNode);
		this.chartNode = new Element("div", {"styles": this.css.chartNode}).inject(this.chartScrollNode);
		
		this.resizePropertyContentNode();
		this.app.addEvent("resize", function(){this.resizePropertyContentNode();}.bind(this));
		
		MWF.require("MWF.widget.ScrollBar", function(){
			var _self = this;
			new MWF.widget.ScrollBar(this.chartScrollNode, {
				"style":"xApp_Organization_Explorer", 
				"where": "before", 
				"distance": 100, 
				"friction": 4,	
				"axis": {"x": false, "y": true},
				"onScroll": function(y){
					var scrollSize = _self.chartScrollNode.getScrollSize();
					var clientSize = _self.chartScrollNode.getSize();
					var scrollHeight = scrollSize.y-clientSize.y;
					if (y+200>scrollHeight) {
						if (!_self.isElementLoaded) _self.loadElements();
					}
				}
			});
			new MWF.widget.ScrollBar(this.propertyContentNode, {
				"style":"xApp_Organization_Explorer", "where": "before", "distance": 100, "friction": 4,	"axis": {"x": false, "y": true}
			});
		}.bind(this));
		
		this.propertyResize = new Drag(this.resizeBarNode,{
			"snap": 1,
			"onStart": function(el, e){
				var x = e.event.clientX;
				var y = e.event.clientY;
				el.store("position", {"x": x, "y": y});
				
				var size = this.chartAreaNode.getSize();
				el.store("initialWidth", size.x);
			}.bind(this),
			"onDrag": function(el, e){
				var x = e.event.clientX;
//				var y = e.event.y;
				var bodySize = this.node.getSize();
				var position = el.retrieve("position");
				var initialWidth = el.retrieve("initialWidth").toFloat();
				var dx = position.x.toFloat()-x.toFloat();
				
				var width = initialWidth-dx;
				if (width> bodySize.x/1.5) width = bodySize.x/1.5;
				if (width<400) width = 400;
				this.chartAreaNode.setStyle("width", width+1);
				this.propertyAreaNode.setStyle("margin-left", width);
			}.bind(this)
		});
	},
	
	getPageNodeCount: function(){
		var size = this.chartScrollNode.getSize();
		count = (size.y/46).toInt()+5;
		return count;
	},
	getLastLoadedElementId: function(){
		return (this.groups.length) ? this.groups[this.groups.length-1].data.id : "";
	},
	
	loadChart: function(){
		this.loadElements();
		this.app.addEvent("resize", function(){
			if (this.groups.length<this.getPageNodeCount()){
				this.loadElements(true);
			}
		}.bind(this));
	},
	loadElements: function(addToNext){
		if (!this.isElementLoaded){
			if (!this.loaddingElement){
				this.loaddingElement = true;
				this.actions.listGroupNext(this.getLastLoadedElementId(), this.getPageNodeCount(), function(json){
					if (json.data.length){
						this.loadChartContent(json.data);
						this.loaddingElement = false;
						
						if (json.data.length<count){
							this.isElementLoaded = true;
							this.app.notice(this.app.lp.groupLoaded, "ok", this.chartScrollNode, {"x": "center", "y": "bottom"});
						}else{
							if (this.loadElementQueue>0){
								this.loadElementQueue--;
								this.loadElements();
							}
						}
					}else{
						if (!this.groups.length){
							this.setNoElementNoticeArea();
						}else{
							this.app.notice(this.app.lp.groupLoaded, "ok", this.chartScrollNode, {"x": "center", "y": "bottom"});
						}
						this.isElementLoaded = true;
						this.loaddingElement = false;
					}
					
				}.bind(this));
			}else{
				if (addToNext) this.loadElementQueue++;
			}
		}
	},
	loadChartContent: function(data){
		data.each(function(itemData){
			var item = new MWF.xApplication.Organization.Explorer.Element(itemData, this);
			this.groups.push(item);
			item.load();
		}.bind(this));
	},
	
	resizePropertyContentNode: function(){
		var size = this.node.getSize();
		var tSize = this.propertyTitleNode.getSize();
		var mtt = this.propertyTitleNode.getStyle("margin-top").toFloat();
		var mbt = this.propertyTitleNode.getStyle("margin-bottom").toFloat();
		var mtc = this.propertyContentNode.getStyle("margin-top").toFloat();
		var mbc = this.propertyContentNode.getStyle("margin-bottom").toFloat();		
		var height = size.y-tSize.y-mtt-mbt-mtc-mbc;
		this.propertyContentNode.setStyle("height", height);
		
		tSize = this.toolbarNode.getSize();
		mtt = this.toolbarNode.getStyle("margin-top").toFloat();
		mbt = this.toolbarNode.getStyle("margin-bottom").toFloat();
		mtc = this.toolbarNode.getStyle("margin-top").toFloat();
		mbc = this.toolbarNode.getStyle("margin-bottom").toFloat();		
		height = size.y-tSize.y-mtt-mbt-mtc-mbc;
		this.chartScrollNode.setStyle("height", height);
	},
	
	loadToolbar: function(){
		this.toolbarNode = new Element("div", {"styles": this.css.toolbarNode}).inject(this.chartAreaNode);
		this.addTopElementNode = new Element("div", {"styles": this.css.addTopElementNode}).inject(this.toolbarNode);
		this.addTopElementNode.addEvent("click", function(){
			this.addTopElement();
		}.bind(this));
		this.createSearchNode();
	},
	createSearchNode: function(){
		this.searchNode = new Element("div", {"styles": this.css.searchNode}).inject(this.toolbarNode);
		
		this.searchButtonNode = new Element("div", {
			"styles": this.css.searchButtonNode,
			"title": this.app.lp.search
		}).inject(this.searchNode);
		
		this.searchButtonNode.addEvent("click", function(){
			this.searchOrg();
		}.bind(this));
		
		this.searchInputAreaNode = new Element("div", {
			"styles": this.css.searchInputAreaNode
		}).inject(this.searchNode);
		
		this.searchInputBoxNode = new Element("div", {
			"styles": this.css.searchInputBoxNode
		}).inject(this.searchInputAreaNode);
		
		this.searchInputNode = new Element("input", {
			"type": "text",
			"value": this.app.lp.searchText,
			"styles": this.css.searchInputNode,
			"x-webkit-speech": "1"
		}).inject(this.searchInputBoxNode);
		var _self = this;
		this.searchInputNode.addEvents({
			"focus": function(){
				if (this.value==_self.app.lp.searchText) this.set("value", "");
			},
			"blur": function(){if (!this.value) this.set("value", _self.app.lp.searchText);},
			"keydown": function(e){
				if (e.code==13){
					this.searchOrg();
					e.preventDefault();
				}
			}.bind(this),
			"selectstart": function(e){
				e.preventDefault();
			},
			"change": function(){
				var key = this.searchInputNode.get("value");
				if (!key || key==this.app.lp.searchText) {
					if (this.currentItem){
						if (this.currentItem.unSelected()){
							this.clear();
							this.loadElements();
						}else{
							this.app.notice(this.app.lp.groupSave, "error", this.propertyContentNode);
						}
					} 
				}
			}.bind(this)
		});
		this.searchButtonNode.addEvent("click", function(){this.searchOrg();}.bind(this));
	},
	searchOrg: function(){

		var key = this.searchInputNode.get("value");
		if (key){
			if (key!=this.app.lp.searchText){
				var isSearchElement = true;
				if (this.currentItem) isSearchElement = this.currentItem.unSelected();
				if (isSearchElement){
					this.actions.listGroupByKey(function(json){
						if (this.currentItem) this.currentItem.unSelected();
						this.clear();
						json.data.each(function(itemData){
							var item = new MWF.xApplication.Organization.Explorer.Element(itemData, this);
							item.load();
						}.bind(this));
					}.bind(this), null, key);
				}else{
					this.app.notice(this.app.lp.groupSave, "error", this.propertyContentNode);
				}
			}else{
				if (this.currentItem) isSearchElement = this.currentItem.unSelected();
				if (isSearchElement){
					this.clear();
					this.loadElements();
				}else{
					this.app.notice(this.app.lp.groupSave, "error", this.propertyContentNode);
				}
			}
		}else{
			if (this.currentItem) isSearchElement = this.currentItem.unSelected();
			if (isSearchElement){
				this.clear();
				this.loadElements();
			}else{
				this.app.notice(this.app.lp.groupSave, "error", this.propertyContentNode);
			}
		}
	},
	addTopElement: function(){
		var isNewElement = true;
		if (this.currentItem) isNewElement = this.currentItem.unSelected();
		if (isNewElement){
			var newElementData = {
				"personList": [],
				"groupList": [],
				"id": "",
				"name": ""
			};
			var item = new MWF.xApplication.Organization.Explorer.Element(newElementData, this);
			item.load();
			item.selected();
			item.editBaseInfor();
			
			(new Fx.Scroll(this.chartScrollNode)).toElement(item.node);
		}else{
			this.app.notice(this.app.lp.groupSave, "error", this.propertyContentNode);
		}
	},
	checkDeleteElements: function(){
		if (this.deleteElements.length){
			if (!this.deleteElementsNode){
				this.deleteElementsNode = new Element("div", {
					"styles": this.css.deleteElementsNode,
					"text": this.app.lp.deleteElements
				}).inject(this.node);
				this.deleteElementsNode.position({
					relativeTo: this.chartScrollNode,
				    position: "centerTop",
				    edge: "centerTop"
				});
				this.deleteElementsNode.addEvent("click", function(e){
					this.deleteSelectedElements(e);
				}.bind(this));
			}
		}else{
			if (this.deleteElementsNode){
				this.deleteElementsNode.destroy();
				this.deleteElementsNode = null;
				delete this.deleteElementsNode;
			}
		}
	},
	deleteSelectedElements: function(e){
		var _self = this;
		this.app.confirm("infor", e, this.app.lp.deleteGroupsTitle, this.app.lp.deleteGroupsConfirm, 300, 120, function(){
			var deleted = [];
			var doCount = 0;

			_self.deleteElements.each(function(group){
				group["delete"](function(){
					deleted.push(group);
					doCount++;
					if (_self.deleteElements.length==doCount){
						_self.deleteElements = _self.deleteElements.filter(function(item, index){
							return !deleted.contains(item);
						});
						_self.checkDeleteElements();
					}
				}, function(){
					doCount++;
					if (_self.deleteElements.length==doCount){
						_self.deleteElements = _self.deleteElements.filter(function(item, index){
							return !deleted.contains(item);
						});
						_self.checkDeleteElements();
					}
				});
			});
			this.close();
		}, function(){
			this.close();
		});
	}
	
});

MWF.xApplication.Organization.Explorer.Element = new Class({
	initialize: function(data, explorer){
		this.data = data;
		if (this.data.personList) this.data.personList = data.personList.filter(function(item){return item;});
		if (this.data.groupList) this.data.groupList = data.groupList.filter(function(item){return item;});
		this.explorer = explorer;
		this.chartNode = this.explorer.chartNode;
		this.initStyle();
		this.selectedPersons = [];
		this.selectedElements = [];
		this.isEdit = false;
		this.deleteSelected = false;
	},
	initStyle: function(){
		this.style = this.explorer.css.groupItem;
	},
	load: function(){
		this.node = new Element("div", {"styles": this.style.node}).inject(this.chartNode);
		this.contentNode = new Element("div", {"styles": this.style.contentNode}).inject(this.node);
		this.childNode = new Element("div", {"styles": this.style.childNode}).inject(this.node);
		
		this.flagNode = new Element("div", {"styles": this.style.flagNode}).inject(this.contentNode);
		this.iconNode = new Element("div", {"styles": this.style.iconNode}).inject(this.contentNode);
		this.actionNode = new Element("div", {"styles": this.style.actionNode}).inject(this.contentNode);
		
		this.textNode = new Element("div", {"styles": this.style.textNode}).inject(this.contentNode);
		this.textNode.set({
			"text": this.data.name
		});
		this.node.inject(this.chartNode);
		
		this.addActions();
		this.setEvent();
	},
	addActions: function(){
		this.deleteNode = new Element("div", {"styles": this.style.actionDeleteNode}).inject(this.actionNode);
//		this.addNode = new Element("div", {"styles": this.style.actionAddNode}).inject(this.actionNode);
		this.deleteNode.addEvent("click", function(e){
			if (!this.deleteSelected){
				this.deleteNode.setStyles(this.style.actionDeleteNode_selected);
				this.contentNode.setStyles(this.style.contentNode_selected);
				
				this.explorer.deleteElements.push(this);
				this.deleteSelected = true;
				
				this.explorer.checkDeleteElements();
			}else{
				this.deleteNode.setStyles(this.style.actionDeleteNode);
				this.contentNode.setStyles(this.style.contentNode);
				
				this.explorer.deleteElements.erase(this);
				this.deleteSelected = false;
				this.explorer.checkDeleteElements();
			}
			e.stopPropagation();
		}.bind(this));
	},
	setEvent: function(){
		this.contentNode.addEvents({
			"mouseover": function(e){
				if (this.explorer.currentItem!=this){
					this.flagNode.setStyles(this.style.flagNodeOver);
				}
				if (!this.deleteSelected) this.actionNode.fade("in");
			}.bind(this),
			"mouseout": function(e){
				if (this.explorer.currentItem!=this){
					this.flagNode.setStyles(this.style.flagNode);
				}
				if (!this.deleteSelected) this.actionNode.fade("out");
			}.bind(this),
			"click": function(e){
				if (this.explorer.currentItem){
					if (this.explorer.currentItem.unSelected()){
						this.selected();
					}else{
						this.explorer.app.notice(this.explorer.app.lp.groupLoaded, "error", this.propertyContentNode);
					}
				}else{
					this.selected();
				}
			}.bind(this)
		});
	},
	selected: function(){
		this.explorer.currentItem = this;
		this.flagNode.setStyles(this.style.flagNodeSelected);
		this.showItemProperty();
	},
	unSelected: function(){
		if (this.isEdit) return false;
		this.explorer.currentItem = null;
		this.flagNode.setStyles(this.style.flagNode);
		this.clearItemProperty();
		return true;
	},
	clearItemProperty: function(){
		this.explorer.propertyTitleNode.empty();
		this.explorer.propertyContentNode.empty();
	},

    showItemProperty: function(){
        this.explorer.propertyTitleNode.set("text", this.data.name);
        this.showItemPropertyBase();
        this.showItemPropertyPerson();
        this.showItemPropertyGroup();
//		this.showItemPropertyBase();
//		this.showItemPropertyDuty();
//		this.showItemPropertyAttribute();
    },

	showItemPropertyBase: function(){
		this.propertyBaseNode = new Element("div", {
			"styles": this.style.propertyInforNode
		}).inject(this.explorer.propertyContentNode);
		
		this.baseActionNode = new Element("div", {
			"styles": this.style.propertyInforActionNode
		}).inject(this.propertyBaseNode);
		this.propertyBaseTextNode = new Element("div", {
			"styles": this.style.propertyInforTextNode,
			"text": this.explorer.app.lp.groupBaseText
		}).inject(this.propertyBaseNode);
		
		this.createEditBaseNode();
		
		this.propertyBaseContentNode = new Element("div", {
			"styles": this.style.propertyInforContentNode
		}).inject(this.propertyBaseNode);
		
		var html = "<table cellspacing='0' cellpadding='0' border='0' width='95%' align='center'>";
		html += "<tr><td class='formTitle'>"+this.explorer.app.lp.groupName+"</td><td id='formGroupName'></td></tr>";
		html += "</table>";
		this.propertyBaseContentNode.set("html", html);
		this.propertyBaseContentNode.getElements("td.formTitle").setStyles(this.style.propertyBaseContentTdTitle);
		
		this.groupNameInput = new MWF.xApplication.Organization.GroupExplorer.Input(this.propertyBaseContentNode.getElement("#formGroupName"), this.data.name, this.explorer.css.formInput);
	},
	createEditBaseNode: function(){
		this.editBaseNode = new Element("button", {
			"styles": this.style.editBaseNode,
			"text": this.explorer.app.lp.edit,
			"events": {"click": this.editBaseInfor.bind(this)}
		}).inject(this.baseActionNode);
	},
	createCancelBaseNode: function(){
		this.cancelBaseNode = new Element("button", {
			"styles": this.style.cancelBaseNode,
			"text": this.explorer.app.lp.cancel,
			"events": {"click": this.cancelBaseInfor.bind(this)}
		}).inject(this.baseActionNode);
	},
	createSaveBaseNode: function(){
		this.saveBaseNode = new Element("button", {
			"styles": this.style.saveBaseNode,
			"text": this.explorer.app.lp.save,
			"events": {"click": this.saveBaseInfor.bind(this)}
		}).inject(this.baseActionNode);
	},
	editBaseInfor: function(){
		this.baseActionNode.empty();
		this.editBaseNode = null;
		this.createCancelBaseNode();
		this.createSaveBaseNode();
		
		this.editMode();
	},
	cancelBaseInfor: function(){
		if (this.data.name){
			this.baseActionNode.empty();
			this.cancelBaseNode = null;
			this.saveBaseNode = null;
			this.createEditBaseNode();
			
			this.readMode();
		}else{
			this.destroy();
		}
	},
	saveBaseInfor: function(){
		if (!this.groupNameInput.input.get("value")){
			this.explorer.app.notice(this.explorer.app.lp.inputGroupName, "error", this.explorer.propertyContentNode);
			return false;
		}
		this.propertyBaseNode.mask({
			"style": {
				"opacity": 0.7,
				"background-color": "#999"
			}
		});
		this.save(function(){
			this.baseActionNode.empty();
			this.cancelBaseNode = null;
			this.saveBaseNode = null;
			this.createEditBaseNode();
			
			this.readMode();
			
			this.propertyBaseNode.unmask();
		}.bind(this), function(xhr, text, error){
			var errorText = error;
			if (xhr) errorText = xhr.responseText;
			this.explorer.app.notice("request json error: "+errorText, "error");
			this.propertyBaseNode.unmask();
		}.bind(this));
	},
	editMode: function(){
		this.groupNameInput.editMode();
		this.isEdit = true;
	},
	readMode: function(){
		this.groupNameInput.readMode();
		this.isEdit = false;
	},
	save: function(callback, cancel){
		this.data.name = this.groupNameInput.input.get("value");
		
		this.explorer.actions.saveGroup(this.data, function(json){
			this.textNode.set("text", this.data.name);
			this.data.id = json.data.id;
			this.groupNameInput.save();
			
			if (callback) callback();
		}.bind(this), function(xhr, text, error){
			if (cancel) cancel(xhr, text, error);
		}.bind(this));
	},
	
	showItemPropertyPerson: function(){
		this.propertyPersonNode = new Element("div", {
			"styles": this.style.propertyInforNode
		}).inject(this.explorer.propertyContentNode);
		
		this.personActionNode = new Element("div", {
			"styles": this.style.propertyInforActionNode
		}).inject(this.propertyPersonNode);
		this.propertyPersonTextNode = new Element("div", {
			"styles": this.style.propertyInforTextNode,
			"text": this.explorer.app.lp.groupMemberPersonText
		}).inject(this.propertyPersonNode);
	//	this.createEditBaseNode();
		
		this.propertyPersonContentNode = new Element("div", {
			"styles": this.style.propertyInforContentNode
		}).inject(this.propertyPersonNode);

		this.createDeletePersonNode();
		this.createAddPersonNode();
		
		this.listPerson();
	},
	createAddPersonNode: function(){
		this.addPersonNode = new Element("button", {
			"styles": this.style.addActionNode,
			"text": this.explorer.app.lp.add,
			"events": {"click": this.addPerson.bind(this)}
		}).inject(this.personActionNode);
	},
	createDeletePersonNode: function(){
		this.deletePersonNode = new Element("button", {
			"styles": this.style.deleteActionNode_desable,
			"text": this.explorer.app.lp["delete"],
			"disable": true
		}).inject(this.personActionNode);
	},
	addPerson: function(){
        MWF.xDesktop.requireApp("Organization", "Selector.Person", function(){
			var selector = new MWF.xApplication.Organization.Selector.Person(this.explorer.app.content,{
				"values": this.data.personList,
				"onComplete": function(items){
					var ids = [];
					items.each(function(item){
						ids.push(item.data.id);
					});
					this.data.personList = ids;
					
					this.explorer.actions.saveGroup(this.data, function(){
						this.listPerson();
					}.bind(this));
				}.bind(this)
			});
			selector.load();
		}.bind(this));
	},
	listPerson: function(){
		var html = "<table cellspacing='0' cellpadding='5' border='0' width='95%' align='center' style='line-height:normal'>";
		html += "<tr><th style='width:20px'></th>";
		html += "<th style='border-right: 1px solid #FFF'>"+this.explorer.app.lp.personEmployee+"</th>";
		html += "<th style='border-right: 1px solid #FFF'>"+this.explorer.app.lp.personDisplay+"</th>";
		html += "<th style='border-right: 1px solid #FFF'>"+this.explorer.app.lp.personMail+"</th>";
		html += "<th>"+this.explorer.app.lp.personPhone+"</th></tr>";
		html += "</table>";
		
		this.propertyPersonContentNode.set("html", html);
		this.propertyPersonContentNode.getElements("th").setStyles(this.style.propertyContentTdTitle);
		
		this.data.personList.each(function(id){
			this.explorer.actions.getPerson(function(json){
				new MWF.xApplication.Organization.GroupExplorer.PersonMember(this.propertyPersonContentNode.getElement("table").getFirst(), json.data, this, this.explorer.css.map);
			}.bind(this), null, id, false);
		}.bind(this));
//		this.explorer.actions.listCompanyDuty(function(json){
//			json.data.each(function(item){
//				new MWF.xApplication.Organization.CompanyDuty(this.propertyDutyContentNode.getElement("table").getFirst(), item, this, this.explorer.css.map);
//			}.bind(this));
//		}.bind(this), null, this.data.id);
	},
	checkDeletePersonAction: function(){

		if (this.selectedPersons.length){
			if (this.deletePersonNode.get("disable")){
				this.deletePersonNode.set({
					"styles": this.style.deleteActionNode
				});
				this.deletePersonNode.removeProperty("disable");
				this.deletePersonNode.addEvent("click", function(e){this.deletePerson(e);}.bind(this));
			}
		}else{
			if (!this.deletePersonNode.get("disable")){
				this.deletePersonNode.set({
					"styles": this.style.deleteActionNode_desable,
					"disable": true
				});
				this.deletePersonNode.removeEvents("click");
			}
		}
	},
	deletePerson: function(e){
		var _self = this;
		this.explorer.app.confirm("infor", e, this.explorer.app.lp.deleteGroupMemberTitle, this.explorer.app.lp.deleteGroupPerson, 300, 120, function(){
			var deleteIds = [];
			_self.selectedPersons.each(function(item){
				this.data.personList = this.data.personList.erase(item.data.id);
			}.bind(_self));
			_self.explorer.actions.saveGroup(_self.data, function(){
				this.listPerson();
			}.bind(_self));
			this.close();
		}, function(){
			this.close();
		});
	},
	
	showItemPropertyGroup: function(){
		this.propertyGroupNode = new Element("div", {
			"styles": this.style.propertyInforNode
		}).inject(this.explorer.propertyContentNode);
		
		this.groupActionNode = new Element("div", {
			"styles": this.style.propertyInforActionNode
		}).inject(this.propertyGroupNode);
		this.propertyGroupTextNode = new Element("div", {
			"styles": this.style.propertyInforTextNode,
			"text": this.explorer.app.lp.groupMemberGroupText
		}).inject(this.propertyGroupNode);
	//	this.createEditBaseNode();
		
		this.propertyGroupContentNode = new Element("div", {
			"styles": this.style.propertyInforContentNode
		}).inject(this.propertyGroupNode);

		this.createDeleteGroupNode();
		this.createAddGroupNode();
		
		this.listGroup();
	},
	createAddGroupNode: function(){
		this.addGroupNode = new Element("button", {
			"styles": this.style.addActionNode,
			"text": this.explorer.app.lp.add,
			"events": {"click": this.addGroup.bind(this)}
		}).inject(this.groupActionNode);
	},
	createDeleteGroupNode: function(){
		this.deleteGroupNode = new Element("button", {
			"styles": this.style.deleteActionNode_desable,
			"text": this.explorer.app.lp["delete"],
			"disable": true
		}).inject(this.groupActionNode);
	},
	addGroup: function(){
        MWF.xDesktop.requireApp("Organization", "Selector.Group", function(){
			var selector = new MWF.xApplication.Organization.Selector.Group(this.explorer.app.content,{
				"values": this.data.groupList,
				"onComplete": function(items){
					var ids = [];
					items.each(function(item){
						ids.push(item.data.id);
					});
					this.data.groupList = ids;
					
					this.explorer.actions.saveGroup(this.data, function(){
						this.listGroup();
					}.bind(this));
				}.bind(this)
			});
			selector.load();
		}.bind(this));
	},
	listGroup: function(){
		var html = "<table cellspacing='0' cellpadding='5' border='0' width='95%' align='center' style='line-height:normal'>";
		html += "<tr><th style='width:20px'></th>";
		html += "<th style='width:25%; border-right: 1px solid #FFF'>"+this.explorer.app.lp.groupName+"</th>";
		html += "<th style='border-right: 1px solid #FFF'>"+this.explorer.app.lp.groupDescription+"</th>";
		html += "</table>";
		
		this.propertyGroupContentNode.set("html", html);
		this.propertyGroupContentNode.getElements("th").setStyles(this.style.propertyContentTdTitle);
		
		this.data.groupList.each(function(id){
			this.explorer.actions.getGroup(function(json){
				new MWF.xApplication.Organization.GroupExplorer.GroupMember(this.propertyGroupContentNode.getElement("table").getFirst(), json.data, this, this.explorer.css.map);
			}.bind(this), null, id);
		}.bind(this));
//		this.explorer.actions.listCompanyDuty(function(json){
//			json.data.each(function(item){
//				new MWF.xApplication.Organization.CompanyDuty(this.propertyDutyContentNode.getElement("table").getFirst(), item, this, this.explorer.css.map);
//			}.bind(this));
//		}.bind(this), null, this.data.id);
	},
	checkDeleteGroupAction: function(){

		if (this.selectedGroups.length){
			if (this.deleteGroupNode.get("disable")){
				this.deleteGroupNode.set({
					"styles": this.style.deleteActionNode
				});
				this.deleteGroupNode.removeProperty("disable");
				this.deleteGroupNode.addEvent("click", function(e){this.deleteGroupMember(e);}.bind(this));
			}
		}else{
			if (!this.deleteGroupNode.get("disable")){
				this.deleteGroupNode.set({
					"styles": this.style.deleteActionNode_desable,
					"disable": true
				});
				this.deleteGroupNode.removeEvents("click");
			}
		}
	},
	destroy: function(){
		this.explorer.currentItem = null;
		this.clearItemProperty();
		this.node.destroy();
		delete this;
	},
	"delete": function(success, failure){
		this.explorer.actions.deleteGroup(this.data.id, function(){
			this.destroy();
			if (success) success();
		}.bind(this), function(xhr, text, error){
			var errorText = error;
			if (xhr) errorText = xhr.responseText;
			MWF.xDesktop.notice("error", {x: "right", y:"top"}, "request json error: "+errorText);
			
			if (failure) failure();
		});
	},
	deleteGroupMember: function(e){
		var _self = this;
		this.explorer.app.confirm("infor", e, this.explorer.app.lp.deleteGroupMemberTitle, this.explorer.app.lp.deleteGroupGroup, 300, 120, function(){
			var deleteIds = [];
			_self.selectedGroups.each(function(item){
				this.data.groupList = this.data.groupList.erase(item.data.id);
			}.bind(_self));
			_self.explorer.actions.saveGroup(_self.data, function(){
				this.listGroup();
			}.bind(_self));
			this.close();
		}, function(){
			this.close();
		});
	}
});

MWF.xApplication.Organization.GroupExplorer.PersonMember = new Class({
	initialize: function(container, data, item, style){
		this.container = $(container);
		this.data = data;
		this.style = style;
		this.item = item;
		this.selected = false;
		this.load();
	},
	load: function(){
		this.node = new Element("tr", {
			"styles": this.style.contentNode
		}).inject(this.container);
		
		this.selectNode = new Element("td", {
			"styles": this.style.selectNode
		}).inject(this.node);
		
		this.employeeNode = new Element("td", {
			"styles": this.style.valueNode,
			"text": this.data.employee || ""
		}).inject(this.node);
		
		this.displayNode = new Element("td", {
			"styles": this.style.valueNode,
			"text": this.data.display
		}).inject(this.node);
		
		this.mailNode = new Element("td", {
			"styles": this.style.valueNode,
			"text": this.data.mail
		}).inject(this.node);
		
		this.phoneNode = new Element("td", {
			"styles": this.style.valueNode,
			"text": this.data.mobile
		}).inject(this.node);
		
		this.setEvent();
	},
	setEvent: function(){
		this.selectNode.addEvent("click", function(){
			this.selectNodeClick();
		}.bind(this));
	},
	selectNodeClick: function(){
		if (!this.selected){
			this.selected = true;
			this.selectNode.setStyles(this.style.selectNode_selected);
			this.node.setStyles(this.style.contentNode_selected);
			this.item.selectedPersons.push(this);
			this.item.checkDeletePersonAction();
		}else{
			this.selected = false;
			this.selectNode.setStyles(this.style.selectNode);
			this.node.setStyles(this.style.contentNode);
			this.item.selectedPersons.erase(this);
			this.item.checkDeletePersonAction();
		}
	}
	
});
MWF.xApplication.Organization.GroupExplorer.GroupMember = new Class({
	initialize: function(container, data, item, style){
		this.container = $(container);
		this.data = data;
		this.style = style;
		this.item = item;
		this.selected = false;
		this.load();
	},
	load: function(){
		this.node = new Element("tr", {
			"styles": this.style.contentNode
		}).inject(this.container);
		
		this.selectNode = new Element("td", {
			"styles": this.style.selectNode
		}).inject(this.node);
		
		this.nameNode = new Element("td", {
			"styles": this.style.valueNode,
			"text": this.data.name || ""
		}).inject(this.node);
		
		this.descriptionNode = new Element("td", {
			"styles": this.style.valueNode,
			"text": this.data.description || ""
		}).inject(this.node);
		
		this.setEvent();
	},
	setEvent: function(){
		this.selectNode.addEvent("click", function(){
			this.selectNodeClick();
		}.bind(this));
	},
	selectNodeClick: function(){
		if (!this.selected){
			this.selected = true;
			this.selectNode.setStyles(this.style.selectNode_selected);
			this.node.setStyles(this.style.contentNode_selected);
			this.item.selectedGroups.push(this);
			this.item.checkDeleteGroupAction();
		}else{
			this.selected = false;
			this.selectNode.setStyles(this.style.selectNode);
			this.node.setStyles(this.style.contentNode);
			this.item.selectedGroups.erase(this);
			this.item.checkDeleteGroupAction();
		}
	}
	
});

MWF.xApplication.Organization.GroupExplorer.Input = new Class({
	Implements: [Events],
	initialize: function(node, value, style){
		this.node = $(node);
		this.value = value || "";
		this.style = style;
		this.load();
	},
	load: function(){
		this.content = new Element("div", {
			"styles": this.style.content,
			"text": this.value
		}).inject(this.node);
	},
	editMode: function(){
		this.content.empty();
		this.input = new Element("input",{
			"styles": this.style.input,
			"value": this.value
		}).inject(this.content);
		
		this.input.addEvents({
			"focus": function(){
				this.input.setStyles(this.style.input_focus);
			}.bind(this),
			"blur": function(){
				this.input.setStyles(this.style.input);
			}.bind(this)
		});
		
	},
	readMode: function(){
		this.content.empty();
		this.input = null;
		this.content.set("text", this.value);
	},
	save: function(){
		if (this.input) this.value = this.input.get("value");
		return this.value;
	}
});