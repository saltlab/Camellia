window.xhr = new XMLHttpRequest();
window.buffer = new Array();

var traceCounter = 0;

function send(value) {
	window.buffer.push(value);
    if (window.buffer.length > 10) {
        sendReally();
    }
}

function sendReally() {
    if (window.buffer.length > 0) { 
    	window.xhr.open('POST', document.location.href + '?slicinginformation', false);
    	window.xhr.send('['+(window.buffer).toString()+']');
    	window.buffer = new Array();
    }
}
