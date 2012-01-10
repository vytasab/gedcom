
// Global parameters
G['globWidth'] = 640;
G['globHeight'] = 480;
G['direction'] = 'BT'; // ... parents: -1, me: 0, children: +1, ...
G['vertGapBetweenGenerations'] = 40;
G['margin'] = 10;
//G['rootId'] = 2;
//G['level'] = 0;
G['tempWidth'] = 0;
G['tempHeight'] = 0;
G['baseWmin'] = G['globWidth']/2+00;
G['baseWmax'] = G['globWidth']/2;
G['baseHmin'] = G['globHeight']/2+00;
G['baseHmax'] = G['globHeight']/2;


// Pauses JavaScript execution
// AC17-5/vsh init
function pauseInMillis(millis) {
    var date = new Date();
    var curDate = null;
    do { curDate = new Date(); }
    while(curDate-date < millis);
}

// Defines a string dimension when style = "font-family: Verdana, font-size: 10px";
// google-gr: [How to get the height and width of a " Text" which has been set the font and size ?]
// AC16-4/vsh init
var getDimension = function(s){
    var span = document.createElement("span");
    span.style.cssText = "font-family: Verdana, font-size: 10px";
    span.innerHTML = s;
    document.body.appendChild(span);
    var width = span.offsetWidth;
    var height = span.offsetHeight;
    document.body.removeChild(span);
    return {w:width,h:height};
}


// google-gr: [Obtaining an image natural resolution]
// AC16-4/vsh init; Google Chrome works improperly
Raphael.fn.drawImage = function(url, x, y, w_or_h, size) {
    var img = new Image();
    img.style.display = "none";
    img.src = url;
    if (img.height * img.width == 0) {
        // AC16-4/vsh Google Chrome workaround
        return this.image(url, x, y, size, size);
    } else if (w_or_h == "w") {
        var adjustedHeight = img.height/(parseFloat(img.width/size));
        //alert("w img.height " + img.height);
        //alert("w img.width " + img.width);
        //alert("adjustedHeight " + adjustedHeigth);
        return this.image(url, x, y, size, adjustedHeight);
    } else {
        //alert("h img.height " + img.height);
        //alert("h img.width " + img.width);
        var adjustedWidth = img.width/(parseFloat(img.height/size));
        return this.image(url, x, y, adjustedWidth, size);
    }
}

// google-gr: [Obtaining an image natural resolution]
// AC16-4/vsh init; Google Chrome works improperly
// relPosition: init, leftParent, rightParent, centerParent
Raphael.fn.drawPerson = function(personId, relPosition) {
    var pKey = "p"+personId;
    var whGenderM = getDimension('♂');
    var whGenderF = getDimension('♀');
    var gWidth = Math.max(whGenderM.w, whGenderF.w);
    var gHeight = Math.max(whGenderM.h, whGenderF.h);
    var gender = (recs[pKey]["gender"]=="M") ? '♂' : '♀';
    var whNameGivn = getDimension(recs[pKey]["nameGivn"]);
    var whNameSurn = getDimension(recs[pKey]["nameSurn"]);
    var whBD = getDimension('* '+recs[pKey]["bd"]);
    var whBP = getDimension('  '+recs[pKey]["bp"]);
    var whDD = getDimension('+ '+recs[pKey]["dd"]);
    var whDP = getDimension('  '+recs[pKey]["dp"]);
    var pWidth = Math.max(whNameGivn.w, whNameSurn.w, whBD.w, whBP.w, whDD.w, whDP.w) + gWidth;
    var pHeight = Math.max(whNameGivn.h, whNameSurn.h, whBD.h, whBP.h, whDD.h, whDP.h) + 0*gHeight;
    if (G.direction == 'BT') {
        if (relPosition == 'init') { 
            var x = G['baseWmin'] - 1*(pWidth)/2;
            var y = G['baseHmin'] - 1*pHeight*2;
        } else if (relPosition == 'leftParent') {
            var fKey = "f"+recs[pKey]["familyId"];  //alert("fKey =" + fKey);
            var f = G._f; // get Family obj
            var x = f.x + f.width/2 - pWidth - 1*G.margin;
            var y = f.y - pHeight*2;
        } else if (relPosition == 'rightParent') {
            var fKey = "f"+recs[pKey]["familyId"];  //alert("fKey =" + fKey);
            var f = G._f; // get Family obj
            var x = f.x + f.width/2 + 1*G.margin;
            var y = f.y - pHeight*2;
        } else if (relPosition == 'spouse') {
            var x = recs[pKey].x + G.margin;
            var y = recs[pKey].y + G.margin;
        } else if (relPosition == 'centerParent') {
            
        } else {
            alert("unsupported relPosition=|" + relPosition + "|"); 
        }
        var text1 = this.text(x, y, gender+' '+recs[pKey].nameGivn)
            .attr({fill:'#000000', 'font-family':'Verdana', 'font-size':'10px', 'stroke-width':1,'text-anchor':'start'});
        recs[pKey].r[recs[pKey]['r'].length] = text1;  //alert("pKey].r[(recs[pKey].r).length] = " + recs[pKey].r[0]);
        var text2 = this.text(x, y+pHeight, recs[pKey].nameSurn)
            .attr({fill:'#000000', 'font-family':'Verdana', 'font-size':'10px', 'stroke-width':1,'text-anchor':'start'});
        recs[pKey].r[recs[pKey]['r'].length] = text2;  //alert("recs[pKey].r[recs[pKey]['r'].length] = " +  recs[pKey].r[recs[pKey]['r'].length-1]);

        var text3 = this.text(x, y, '* '+recs[pKey]["bd"])
            .attr({fill:'#000000', 'font-family':'Verdana', 'font-size':'10px', 'stroke-width':1,'text-anchor':'start'});
        recs[pKey].r[recs[pKey]['r'].length] = text3;  alert("recs[pKey].r[recs[pKey]['r'].length] = " +  recs[pKey].r[recs[pKey]['r'].length-1]);
        var text4 = this.text(x, y, '  '+recs[pKey]["bp"])
            .attr({fill:'#000000', 'font-family':'Verdana', 'font-size':'10px', 'stroke-width':1,'text-anchor':'start'});
        recs[pKey].r[recs[pKey]['r'].length] = text4;

        var text5 = this.text(x, y, '+ '+recs[pKey]["dd"])
            .attr({fill:'#000000', 'font-family':'Verdana', 'font-size':'10px', 'stroke-width':1,'text-anchor':'start'});
        recs[pKey].r[recs[pKey]['r'].length] = text5;
        var text6 = this.text(x, y, '  '+recs[pKey]["dp"])
            .attr({fill:'#000000', 'font-family':'Verdana', 'font-size':'10px', 'stroke-width':1,'text-anchor':'start'});
        recs[pKey].r[recs[pKey]['r'].length] = text6;


        var rect1 = this.rect(x-1*G.margin,y-1*G.margin,(pWidth+2*G.margin),(2*pHeight+G.margin*1),5);
        recs[pKey].r[recs[pKey]['r'].length] = rect1;  alert("recs[pKey]['r'].length = " +  recs[pKey]['r'].length);
        //var c = this.circle(G['globWidth']-40, 40, 20);
        rect1.node.onclick = function() { //alert("c.node.onclick");
            rect1.attr("fill", "#fff");
            location.assign ( G.app+"rest/person/" + recs[pKey].id);
        };
        rect1.node.onmouseover = function() { //alert("rect1.node.onmouseover");
            rect1.attr({fill: "#fff", opacity: 0.5});
            rect1.attr("title", "click mouse to change current person");
        };
        rect1.node.onmouseout = function() { //alert("rect1.node.onmouseout");
            rect1.attr({fill: "#fff", opacity: 0.5});
        };
        recs[pKey]["x"] = x - G.margin;
        recs[pKey]["width"] = pWidth+2*G.margin;
        recs[pKey]["y"] = y - G.margin;
        recs[pKey]["height"] = 2*pHeight+G.margin/1;
        updateGlobVars(x-1*G['margin'], y-1*G['margin'], (pWidth+2*G.margin), (2*pHeight+G.margin/2));
    } else { alert("direction must be 'BT' [BottomTop] only"); }
    //showGlobVars();
    //alert("familyId=" + ('familyId' in recs[pKey]));
    if ('familyId' in recs[pKey]) { // the person has parents
        this.drawFamily(recs[pKey]/*['familyId'], recs[pKey]['id']*/, 'forParents');
        var f = recs['f'+recs[pKey]['familyId']];  //alert(f);
        var p = recs[pKey];
        var fmw = f.x + f.width/2;
        var fmh = f.y + f.height + G.margin/2;
        var plw = p.x + p.width/2;
        var plh = p.y;
        //alert('M'+fmw+' '+fmh+'L'+plw+' '+plh);
        var path1 = this.path('M'+fmw+' '+fmh+'L'+plw+' '+plh);
        recs[pKey].r[recs[pKey]['r'].length] = path1;  //alert("recs[pKey]['r'].length = " +  recs[pKey]['r'].length);
    }
    if (('fd' in recs[pKey]) && (relPosition != 'spouse')) { // the person is/was maried at leas once
        /**/
                var familyIdsString = ""+recs[pKey].fd;  // !!! ==> ""+...  is IMPORTANT
        //        alert("personIdsString=|"+personIdsString+"|");
                var familyIds = [];
        //        alert("personIdsString.indexOf(',') =" + personIdsString.indexOf(','));
                if (familyIdsString.indexOf(" ") < 0) {
                    familyIds[0] = familyIdsString;
        //            alert("personIds[0]="+personIds[0]);
                } else {
        //           alert("zzz="+personIdsString.split(","));
        //           alert("array="+new Array(personIdsString.split(",")));
                   familyIds = familyIdsString.split(" ");
                }//        alert("personIds="+personIds);
        //        alert("personIds.length="+personIds.length);
                for (j = 0; j < familyIds.length; j++){
                    //alert(" = " + familyIds[j]);
                    var fdKey = "f"+familyIds[j];
                    var fd = recs[fdKey];  //alert(fd);
                    var spouseId = (('mother' in fd) && (personId == fd.mother)) ? (('father' in fd) ? fd.father : '0') :
                    (('father' in fd) && (personId == fd.father)) ? (('mother' in fd) ? fd.mother : '0') : '0';
                    // alert("spouseId="+spouseId);
                    //-- Set spouse person position
                    var sPerson = recs['p'+spouseId]
                    sPerson.x = recs[pKey].x + recs[pKey].width;  // x - G.margin;
                    //recs[pKey]["width"] = pWidth+2*G.margin;
                    sPerson.y = recs[pKey]["y"]; // = y - G.margin;
                    //recs[pKey]["height"] = 2*pHeight+G.margin/1;
                    paper.drawPerson(spouseId, 'spouse');
                }
        //        alert("...[]");
        /**/
        /**/
        this.drawFamily(recs[pKey], 'forChildren');
        //var f = recs['f'+recs[pKey]['familyId']];  //alert(f);
        //var p = recs[pKey];
        //var fmw = f.x + f.width/2;
        //var fmh = f.y + f.height + G.margin/2;
        //var plw = p.x + p.width/2;
        //var plh = p.y;
        // //alert('M'+fmw+' '+fmh+'L'+plw+' '+plh);
        //var path1 = this.path('M'+fmw+' '+fmh+'L'+plw+' '+plh);
        //recs[pKey].r[recs[pKey]['r'].length] = path1;  //alert("recs[pKey]['r'].length = " +  recs[pKey]['r'].length);
        /**/
    }
}

// AC28-2/vsh init
Raphael.fn.drawFamily = function(p /* Person obj */, option /* forParents forChildren */) {
    if (option == 'forParents') { 
    var fKey = "f"+p.familyId;  //alert("fKey =" + fKey);
    if (!isEventInFamilyHW(p.familyId)) {
        var fWidth = 30;
        var fHeight = 5;
        var x = p.x + p.width/2 - fWidth/2;
        var y = p.y - fHeight - 1*G.vertGapBetweenGenerations;
        var fRect = this.rect(x-1*G.margin,y-1*G.margin,(fWidth+2*G.margin),(2*fHeight+G.margin/1),0);
        fRect.attr({fill: "#f66", stroke: "#f00", opacity: 0.5});
        recs[fKey]["x"] = x - G.margin;
        recs[fKey]["width"] = fWidth+2*G.margin;
        recs[fKey]["y"] = y - G.margin;
        recs[fKey]["height"] = 2*fHeight+G.margin/2;
        updateGlobVars(x-1*G.margin, y-1*G.margin, (fWidth+2*G.margin), (2*fHeight+G.margin/2));
        var f = recs[fKey];
        G._f = f;
        if (('father' in f) && ('mother' in f)) {
            // put mother & father over fam box
            this.drawPerson(f.father, 'leftParent');
            this.drawPerson(f.mother, /*'rightParent'*/ 'spouse');
        } else if ('father' in f) {
            this.drawPerson(f.father, 'centerParent');
        } else if ('mother' in f) {
            this.drawPerson(f.mother, 'centerParent');
        } else { alert("no father and mother set in Family"); }
        delete G._f;
    }
    //G['baseWmin'] = Math.min(G['baseWmin'], x);
    //G['baseWmax'] = Math.max(G['baseWmax'], x);
    //G['baseHmin'] = Math.min(G['baseHmin'], y);
    //G['baseHmax'] = Math.max(G['baseHmax'], y);
    } else if (option == 'forChildren') {
        //alert("option == 'forChildren'");
    } else {
        alert("A 'drawFamily' function 'else' needs to be written !")
    }
}

// AC28-2/vsh init
isEventInFamilyHW = function(familyId) {
    return ('fe' in recs["f"+familyId])
}

// AC29-3/vsh init
Raphael.fn.drawTestCircle = function() {
    var c = this.circle(G['globWidth']-40, 40, 20);
    c.node.onclick = function() { //alert("c.node.onclick");
        c.attr("fill", "#f00");
         LoadNewPage ();
    }
    // ! neveikia c.node.click = function (event) { alert("c.click"); attr({fill: "blue"}); };
    // ! neveikia c.mouseover = function () { alert("c.mouseover"); c.attr({fill: "green"}); };
    c.node.onmouseover = function() { //alert("c.node.onmouseover");
        c.attr({fill: "green"});
        c.attr("title", "Bandymas, aaaaa bbbbbb sssss ccccc fffff")
    };
    // ! neveikia c.mouseout = function () { alert("c.mouseout"); c.attr({fill: "#eee"}); };
    c.node.onmouseout = function() { //alert("c.node.onmouseout");
        c.attr({fill: "#fff"});
    };
}

// update global variables
// AC28-2/vsh init
updateGlobVars = function(x, y, width, height) {
    G['baseWmin'] = Math.min(G['baseWmin'], x);
    G['baseWmax'] = Math.max(G['baseWmax'], x);
    G['baseHmin'] = Math.min(G['baseHmin'], y);
    G['baseHmax'] = Math.max(G['baseHmax'], y);
}

// show global variables
// AC29-3/vsh init
showGlobVars = function() {
    alert("baseWmin= "+ G['baseWmin'] +
    "; baseWmax= "+ G['baseWmax'] +
    "; baseHmin= "+ G['baseHmin'] +
    "; baseHmax= "+ G['baseHmax'] + ";");
}

//logger = function(message) {
//    //alert(document.getElementById("logger").firstChild.nodeValue);
//    document.getElementById("logger").firstChild.nodeValue = message;
//};

function LoadNewPage () {
   location.assign ( G.app+"rest/person/6");
};

// initialize a generation objects position global variables
// AC29-3/vsh init
InitGlobVars = function() {
    for (var i = G['gMin']; i <= G['gMax']; i++) {
        G['g'+i+'_wMin'] = 0;
        G['g'+i+'_wMax'] = 0;
        G['g'+i+'_hMin'] = 0;
        G['g'+i+'_hMax'] = 0;
//        G['g.'+i+'.w.min'] = 0;
//        G['g.'+i+'.w.max'] = 0;
//        G['g.'+i+'.h.min'] = 0;
//        G['g.'+i+'.h.max'] = 0;
    }
};

// ------------------------------------------------------------------------------------

    InitGlobVars();

    paper = Raphael(document.getElementById("canvas"), G['globWidth'], G['globHeight']);

    var rect = paper.rect(0, 0, G['globWidth'], G['globHeight']);

    //var rec = paper.rect(10, 10, 10, 10);
    //var cir = paper.circle(10, 10, 10);
    //rec.insertAfter(cir); // neveikia!

     // DO NOT delete
     var imageURI =
    //"vsh.jpg";
    //"http://vshmixweb.vytastax.staxapps.net/images/2-James_Gosling.jpg";
    "http://vshmixweb.vytastax.staxapps.net/images/2-James_Strachan.png";
    paper.drawImage(imageURI, 5, 5, 'h', 90);
    //

    paper.drawTestCircle();

    //alert(G['g.size']);
    //var x = G["g0"];
    //alert(x[0]);
    //alert(G["g0"]);
    //alert(G[g.0']);
//    G['rootId'] = G['g0'][0];
//    paper.drawPerson(G['rootId']);
//    G['level'] += 1;
    //G['tempWidth'] = 0;
    //G['tempHeight'] = 0;

//    paper.drawPerson(G.g0, 'init');
    paper.drawPerson(G.rootId, 'init');
/*
    //for (var i = G['g.min']; i >= G['g.max']; i--) {
    for (i = 0; i >= G['gMin']; i--) {
//        alert("i="+i);
        var personIdsString = ""+G['g'+i];  // !!! ==> ""+G[...  is IMPORTANT
//        alert("personIdsString=|"+personIdsString+"|");
        var personIds = [];
//        alert("personIdsString.indexOf(',') =" + personIdsString.indexOf(','));
        if (personIdsString.indexOf(",") < 0) {
            personIds[0] = personIdsString;
//            alert("personIds[0]="+personIds[0]);
        } else {
//           alert("zzz="+personIdsString.split(","));
//           alert("array="+new Array(personIdsString.split(",")));
           //personIds = personIdsString.split(",");
        }//        alert("personIds="+personIds);
//        alert("personIds.length="+personIds.length);
        for (j = 0; j < personIds.length; j++){
//            alert(" = " + personIds[j]);
            paper.drawPerson(personIds[j], 'init');
        }
//        alert("...[]");
    }
*/


//    paper.drawPerson(1, /*x*/100, /*y*/120);
//    paper.drawPerson(2, /*x*/200, /*y*/120);
//    paper.drawPerson(3, /*x*/300, /*y*/120);

//    alert("1");
    //paper.print(100, 100, "Test string", paper.getFont("Verdana", 80), 30);
//    paper.logger("2");
//
//    var r = paper.set();
//    alert("3");
    //var log = paper.print(10, 50, "print", paper.getFont("Verdana"), 30); //.attr({fill: "#fad"});
//    alert("4");
//     log[0].attr({fill: "#f00"});
//    alert("log: " + log);


//    //var path = paper.path("M100 100L200 200");
//    var path = paper.path("M100 100L200 100");
//    //paper.logger("path.getTotalLength(): " + path.getTotalLength());


window.onload = function (paper) {
    //alert("window.onload");
};
