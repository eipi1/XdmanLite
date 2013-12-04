aka="Not defined";
window.addEventListener("load", show_only_for_link, false);

function show_only_for_link() {
	//alert("Init");
	var menu = document.getElementById("contentAreaContextMenu");
	menu.addEventListener("popupshowing", link_listener, false);
	try{
		//alert("Creating...");
		Components.utils.import("resource://module/module.jsm");
		startInterceptor();
	}catch(e){
	}
}


function link_listener() {
  gContextMenu.showItem("db", gContextMenu.onLink);
  gContextMenu.showItem("flv",true);
}

function add_new_download(){
	sendLinkToXDM(gContextMenu.getLinkURL()+"");
}
function listFLV(){
	//alert(flvList);
	sendFLVToXDM(flvList);
}
function sendFLVToXDM(arr){
try{
	var client = new XMLHttpRequest();
	client.open("GET", "http://localhost:9614/flv", true);
	for(var i=0;i<arr.length;i++){
		client.setRequestHeader("link",arr[i]);
	}
	client.send();
	client.onreadystatechange = function() {
  		if(this.readyState == 4) {
    			if(this.status!=200){
				alert("Could not communicate with XDM. Make sure XDM is running");
			}
  		}
	};
}catch(error){alert(error);}
}
function sendLinkToXDM(link){
try{
	var client = new XMLHttpRequest();
	client.open("GET", "http://localhost:9614/link", true);
	client.setRequestHeader("link",link);
	client.send();
	client.onreadystatechange = function() {
  		if(this.readyState == 4) {
    			if(this.status!=200){
				alert("Could not communicate with XDM. Make sure XDM is running");
			}
  		}
	}
}catch(error){alert(error);}
}