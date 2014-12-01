
// http://developer.apple.com/internet/webcontent/xmlhttpreq.html
// http://developer.apple.com/internet/webcontent/XMLHttpRequestExample/example.html
// http://jibbering.com/2002/4/httprequest.html

// http://www.webdevelopersnotes.com/tutorials/javascript/global_local_variables_scope_javascript.php3

// loads sprecified GEDCOM object
// AC17-5/vsh init
function loadGedcomRec(recType, recId) {
    var urlPref = "http://localhost:8080/gedcom-web/rest/";
    // sample: "http://localhost:8080/gedcom-web/rest/person/1/xml";
    var url = urlPref+recType+"/"+recId+"/xml";
    //alert("url: " + url);
    loadXMLDoc(url);
}


// global flag
var isIE = false;
// global request and XML document objects
var req;

// Retrieve XML document (reusable generic function);
// parameter is URL string (relative or complete) to
// an .xml file whose Content-Type is a valid XML
// type, such as text/xml; XML source must be from
// same domain as HTML file
// AC17-5/vsh extracted from: http://developer.apple.com/internet/webcontent/XMLHttpRequestExample/example.html
function loadXMLDoc(url) {
    // branch for native XMLHttpRequest object
    if (window.XMLHttpRequest) {
        //alert("window.XMLHttpRequest");
        req = new XMLHttpRequest();
        req.onreadystatechange = processReqChange;
        req.open("GET", url, false);  // true -> asynchronous, false -> synchronous
        req.send(null);
    // branch for IE/Windows ActiveX version
    } else if (window.ActiveXObject) {
        //alert("window.ActiveXObject");
        isIE = true;
        req = new ActiveXObject("Microsoft.XMLHTTP");
        if (req) {
            req.onreadystatechange = processReqChange;
            req.open("GET", url, false);
            req.send();
        }
    }
}

// handle onreadystatechange event of req object
// AC17-5/vsh extracted from: http://developer.apple.com/internet/webcontent/XMLHttpRequestExample/example.html
function processReqChange() {
    // only if req shows "loaded"
    //alert("req.{status,readyState}: " + req.status + " " + req.readyState);
    if (req.readyState == 4) {
        // only if "OK"
        if (req.status == 200) {
            //alert("req.responseText: " + req.responseText);
            //alert("req.responseXML: " + req.responseXML);
            //clearTopicList();
            //buildTopicList();

            var doc = req.responseXML;  //alert("doc: " + doc);

            var recType = xpathString("/gedcom/@operation", doc, "operation");
            if (recType == "person") {
                var p = {};
                p["id"] = xpathString("//id", doc, 0);
                p["nameGivn"] = xpathString("//nameGivn", doc, "nerasta");
                p["nameSurn"] = xpathString("//nameSurn", doc, "nerasta");
                p["gender"] = xpathString("//gender", doc, "nerasta");
                recs["p."+p["id"]] = p;
                //alert("recs['p.3']['id']: |" +  recs["p."+p["id"]]["id"] + "|");
                //alert("recs['p.3']['nameGivn']: |" +  recs["p.3"]["nameGivn"] + "|");
            }
        } else {
            alert("There was a problem retrieving the XML data:\n" + req.statusText);
        }
    }
}

// extract first String usint
// AC17-5/vsh init
function xpathString(xpathExpression, xmlDoc, orUse) {
    var result = xmlDoc.evaluate(xpathExpression, xmlDoc.documentElement, null, XPathResult.STRING_TYPE, null);
    result = result.stringValue;
    if (result.length == 0) result = orUse;
    //alert("xpathString: |" +  xpathExpression + "| ==> |" + result + "|");
    return result;
}

// Pauses JavaScript execution
// AC17-5/vsh init
function pauseInMillis(millis) {
    var date = new Date();
    var curDate = null;
    do { curDate = new Date(); }
    while(curDate-date < millis);
}
