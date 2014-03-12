function makeRequest(url) {
	var http_request = false;
	if (window.XMLHttpRequest) { // Mozilla, Safari,...
		http_request = new XMLHttpRequest();
		if (http_request.overrideMimeType)
			http_request.overrideMimeType('text/xml');
	} else if (window.ActiveXObject) { // IE
		try { http_request = new ActiveXObject("Msxml2.XMLHTTP"); } catch (e) {
			try { http_request = new ActiveXObject("Microsoft.XMLHTTP"); } catch (e) {}
		}
	}
	if (!http_request) {
		alert('Giving up :( Cannot create an XMLHTTP instance');
		return false;
	}
	http_request.onreadystatechange = function() { 	try { alertContents(http_request); } catch(e) {} };
	http_request.open('GET', url, true);
	http_request.send(null);

	var AjaxVal = "";
	var isAjaxing = false;
	var md5 = "";
	var ss_cur = 0;
	var ss_play = 1;
	var ss_pid  = new Array();
	var ss_ttl  = new Array();
	var ss_src  = new Array();
	var ss_date = new Array();
	var ss_desc = new Array();
	var ss_loaded = false;
	var ss_smaller = false;
	var ss_awaits = 1;
}