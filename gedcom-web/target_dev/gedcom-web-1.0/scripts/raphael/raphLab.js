//// Each of the following examples create a canvas that is 320px wide by 200px high
//// Canvas is created at the viewport’s 10,50 coordinate
//// alert("before: var paper intialization");
//// var paper = Raphael(10, 50, 320, 200);
//// alert("before: var paper intialization");
//
//// Canvas is created at the top left corner of the #notepad element
//// (or its top right corner in dir="rtl" elements)
//// var paper = Raphael(document.getElementById("notepad"), 320, 200);
//
//// Same as above
//// var paper = Raphael("canvas", 320, 200);
//
//// Image dump
////var set = Raphael(
////["canvas", 320, 200,
////{type: "rect", x: 10, y: 10, width: 25, height: 25, stroke: "#f00"},
////{type: "text", x: 30, y: 40, text: "Dump" }
////])
//
////var url = "http://localhost:8080/gedcom-web/rest/person/1/xml";
////loadXMLDoc(url);
//////url = "/rest/person/2/xml";
//////loadXMLDoc(url);
//
////var url = "http://localhost:8080/gedcom-web/rest/person/1/xml";
////loadXMLDoc("http://localhost:8080/gedcom-web/rest/person/1/xml");
//
//
////var recs = {};
////
//loadGedcomRec("person", 2);
//alert("! recs['p.3']['nameGivn']: |" +  recs["p.3"]["nameGivn"] + "|");
//
//
////setTimeout("alert('---')",2000);
////pauseInMillis(1000);
////url = "/rest/person/2/xml";
////loadXMLDoc("http://localhost:8080/gedcom-web/rest/person/2/xml");
//
//
////var evaluator = new XPathEvaluator();
//////get first div
////var result = evaluator.evaluate("//nameGivn", document.documentElement, null,
////                 XPathResult.FIRST_ORDERED_NODE_TYPE, null);
////alert("First div ID is " + result.singleNodeValue.id);
//
//
//////get first div
////var result = evaluator.evaluate("//div", document.documentElement, null,
////                 XPathResult.FIRST_ORDERED_NODE_TYPE, null);
////alert("First div ID is " + result.singleNodeValue.id);
//
////alert("url: " + url);
//
//
//// alert("after: var paper intialization " + paper);
//// var c = paper.circle(50, 50, 40);
//// circle.attr("fill", "#f00");
//// circle..attr("stroke", "#fff");
//
//var people = [];
//
//function Person(id, nameGivn, nameSurn, gender) {
//  this.id = id;
//  this.nameGivn = nameGivn || "";
//  this.nameSurn = nameSurn || "";
//  this.gender = gender || "?";
//  //@OneToMany(mappedBy = "personevent", targetEntity = classOf[PersonEvent], cascade = Array(CascadeType.REMOVE))
//  //var personevents: java.util.Set[PersonEvent] = new java.util.HashSet[PersonEvent]()
//  //@OneToMany(mappedBy = "personattrib", targetEntity = classOf[PersonAttrib], cascade = Array(CascadeType.REMOVE))
//  //var personattribs: java.util.Set[PersonAttrib] = new java.util.HashSet[PersonAttrib]()
//  //@ManyToOne(fetch = FetchType.EAGER, optional = true)
//  //var family: Family = _
//  //@ManyToOne(fetch = FetchType.LAZY, optional = true)
//  //var submitter = ""  // User = _
//  }
//  people[0] = new Person (1, 'Vytautas', 'Šabanas', 'M');
//  people[1] = new Person (1, 'Statys', 'Šabanas', 'M');
//
//
//
//var globWidth = 640;
//var globHeight = 480;
//var paper
////var globWidth = 640;
////var globHeight = 480;
//
////Raphael.fn.logger = function(message) {
////    return this.text(globWidth/2, globHeight-10, message);
////};
//var  logger = function(message) {
//    //alert(document.getElementById("logger").firstChild.nodeValue);
//    document.getElementById("logger").firstChild.nodeValue = message;
//};
//    //alert(document.getElementById("logger").firstChild.nodeValue);
//
////Raphael.fn.logger = function(paper, message) {
////    //paper.text(globWidth/2, globHeight-10, "      ");
////    return this.text(globWidth/2, globHeight-10, message);
////};
////    /*var image =*/ return /*paper*/this.image(p.url, p.x, p.y, img.width, img.height);
//    //return this.image(url, x, y, img.width/2, img.height/2);
//
//
//    //alert(document);
//    //alert("include script: " + document.getElementById("canvas").id);
//
//    var paper = Raphael(document.getElementById("canvas"), globWidth, globHeight);
//    // var paper = Raphael("canvas", 640, 480);
//
//    //var rec = paper.rect(10, 10, 10, 10);
//    //var cir = paper.circle(10, 10, 10);
//    //rec.insertAfter(cir); // neveikia!
//
//    //var imageURI = "vsh.jpg";
//    var imageURI = "http://vshmixweb.vytastax.staxapps.net/images/2-James_Gosling.jpg";
//    //var imageURI = "http://vshmixweb.vytastax.staxapps.net/images/2-James_Strachan.png";
////    var img = document.getElementById("imageFiller");
////    img.style.display = "none";
////    paper.image(img.src, 0, 0, img.width, img.height);
//
//    //var image = paper.image("http://vshmixweb.vytastax.staxapps.net/images/2-James_Gosling.jpg", globWidth-100-5, 5, 100, 100);
//    //var image = paper.image(imageURI, globWidth-100-5, 5, 100, 100);
//    //var image = paper.drawImage(imageURI, globWidth-100-5, 5/*, 100, 100*/);
//    var image = paper.drawImage(imageURI, 5, 5, 'h', 60);
////    var image = paper.image("vsh.jpg", globWidth-100-5, 5, 100, 100);
//
//    var rect = paper.rect(0, 0, globWidth, globHeight);
//    //var rect1 = paper.rect(0, 0, globHeight, globWidth);
//    var c = paper.circle(300, 300, 20);
//    //alert("c.node: " + c.node);
//    c.node.onclick = function() { //alert("c.node.onclick");
//        c.attr("fill", "#f00");
//    }
//    // ! neveikia c.node.click = function (event) { alert("c.click"); attr({fill: "blue"}); };
//    // ! neveikia c.mouseover = function () { alert("c.mouseover"); c.attr({fill: "green"}); };
//    c.node.onmouseover = function() { //alert("c.node.onmouseover");
//        c.attr({fill: "green"});
//    };
//    // ! neveikia c.mouseout = function () { alert("c.mouseout"); c.attr({fill: "#eee"}); };
//    c.node.onmouseout = function() { //alert("c.node.onmouseout");
//        c.attr({fill: "#fff"});
//    };
//    //paper.logger("c.node: " + c.node);
//    //var text = paper.text(10, 100, "1234"/*"Ūžė miškas !!!"*/);
//
//    var whNameGivn = getDimension(people[0].nameGivn);
//    var nameGivn = paper.text(20+whNameGivn.w/2, 120, people[0].nameGivn)
//        .attr({fill:'#000000', 'font-family':'Verdana', 'font-size':'10px', 'stroke-width':1,'text-anchor':'start'});
//    var whNameSurn = getDimension(people[0].nameSurn);
//    var nameSurn = paper.text(20+whNameSurn.w/2, 120+whNameGivn.h, people[0].nameSurn)
//        .attr({fill:'#000000', 'font-family':'Verdana', 'font-size':'10px', 'stroke-width':1,'text-anchor':'start'});
//
//    //alert("recs['p.3'].nameGivn: |" +  recs["p.3"]["nameGivn"] + "|");
//    var whNameGivn = getDimension(recs["p.3"]["nameGivn"]);
//    var nameGivn = paper.text(20+whNameGivn.w/2, 220, recs["p.3"]["nameGivn"])
//        .attr({fill:'#000000', 'font-family':'Verdana', 'font-size':'10px', 'stroke-width':1,'text-anchor':'start'});
//    var whNameSurn = getDimension(recs["p.3"]["nameSurn"]);
//    var nameSurn = paper.text(20+whNameSurn.w/2, 220+whNameGivn.h, recs["p.3"]["nameSurn"])
//        .attr({fill:'#000000', 'font-family':'Verdana', 'font-size':'10px', 'stroke-width':1,'text-anchor':'start'});
//
//    // nameGivn.insertAfter(nameSurn);
//    logger("nameGivn width: " + nameGivn.getBBox().width);
//
//
////    alert("1");
//    //paper.print(100, 100, "Test string", paper.getFont("Verdana", 80), 30);
////    paper.logger("2");
////
////    var r = paper.set();
////    alert("3");
//    //var log = paper.print(10, 50, "print", paper.getFont("Verdana"), 30); //.attr({fill: "#fad"});
////    alert("4");
////     log[0].attr({fill: "#f00"});
////    alert("log: " + log);
//
//
//    //var path = paper.path("M100 100L200 200");
//    var path = paper.path("M100 100L200 100");
//    //paper.logger("path.getTotalLength(): " + path.getTotalLength());
//
//
//window.onload = function (paper) {
//    //alert("window.onload");
//};
