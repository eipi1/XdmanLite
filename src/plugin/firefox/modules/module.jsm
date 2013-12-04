Components.utils.import("resource://gre/modules/XPCOMUtils.jsm");
var EXPORTED_SYMBOLS = ["startInterceptor", "bar","flvList"];

var flvList=new Array();

var arr=[<FILE_EXT>];
var javaPath="<JAVA_PATH>";
var xdmanJar="<XDM_PATH>";
	
function shouldCapture(file){
	var str=file.toUpperCase();
	for(var i=0;i<arr.length;i++){
		if((str.match("\\."+arr[i]+"$")!=null)||(str.match("\\."+arr[i]+"\\/\\?")!=null))
			return true;
	}
	return false;
}
 
function foo() {
  return "foo";
}

function startInterceptor(){
	if(init==0){
		new myObserver();
		init++;
	}else{
		return;
	}
}
 
var bar = {
  name : "bar",
  size : 3
};
 
var dummy = "dummy";

var init=0;

function myObserver()
{
  this.register();
}
myObserver.prototype = {
  observe: function(subject, topic, data) {
	subject.QueryInterface(Components.interfaces.nsIHttpChannel);
    	var url = subject.URI.spec;
     	if(topic=="http-on-modify-request"){
		var method=subject.requestMethod+"";
		if(method.toLowerCase()!="get"){
			return;
		}
		if(shouldCapture(url+"")){
			var file=Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
			file.initWithPath(javaPath);
			var process=Components.classes["@mozilla.org/process/util;1"].createInstance(Components.interfaces.nsIProcess);
			process.init(file);
			var args=["-jar",xdmanJar,"-u",url+"","-m"];
			try{
				var cookie=subject.getRequestHeader("Cookie")+"";
				args.push("-c");
				args.push(cookie);
			}catch(e){}
			try{
				var ref=subject.getRequestHeader("Referer")+"";
				args.push("-r");
				args.push(ref);
			}catch(e){}
			process.run(false,args,args.length);
			try{
				subject.setRequestHeader("xdm-skip","true",true);
			}catch(e){}
		}
	}else if(topic=="http-on-examine-response"){
		try{
			var type=subject.getResponseHeader("Content-Type")+"";
			if(type.toLowerCase().indexOf("video/")>=0){
			            flvList.push(url+"");
                                          }
		}catch(e){}
	}
  },
  register: function() {
    var observerService = Components.classes["@mozilla.org/observer-service;1"]
                          .getService(Components.interfaces.nsIObserverService);
    observerService.addObserver(this, "http-on-examine-response", false);
    observerService.addObserver(this, "http-on-modify-request", false);
  },
  unregister: function() {
    var observerService = Components.classes["@mozilla.org/observer-service;1"]
                            .getService(Components.interfaces.nsIObserverService);
    observerService.removeObserver(this, "http-on-examine-response");
    observerService.removeObserver(this, "http-on-modify-request");
  }
}