// a family tree drawing
// B104-2/vsh started from gedcomLoader.js
/* naming conventions for assoc arrays:
 G - global parameters,
 g - gedcom Entities: g['x'+id] - x ={p-Person, f-family, pe- , pa- , fe- , ed- }}
 an Entity contains box params: x - horiz coord, y - vert coord, w - width, h - height 
*/
// Global parameters
// G.loggedIn = the value is set in serev Lift side (Forest.scala)
G.globWidth =  0*640 + 0*720 + 0*800 + 0 + 1*1280;
G.globHeight = 0*480 + 0*576 + 0*600 + 0 + 1*720;
G.locale = 'lt'; // actual value must be generated in Lift
G.direction = 'BT'; // tree dreawing direction: BT-BottomTop TB-TopBottom LR-LeftRight RL-RightLeft
G.vertGapBetweenGenerations = 200;
G.horizGapBetweenSiblings = 20;
G.margin = 10; // Family and Person box internal margin
// G.rootId = id; // a root Person id
// G.gSize = 0; // number of generations
// G.gMin = +999 // min generation
// G.gMax = -999 // max generation
// G['g'+generationLevel] = 'pId,...' // list of Person ids

// 2D size of tree
G.baseWmin = G.initX = G.globWidth/2+00;
G.baseWmax = G.globWidth/2;
G.baseHmin = G.initY = G.globHeight/2+00;
G.baseHmax = G.globHeight/2;
G.tempWidth = 0;
G.tempHeight = 0;

G.shownFam=[];
if (!G.shownFam.indexOf) {
    alert ("Your browser does not support indexOf method!");
}

// Development time tools:  Loging configuration
G.DIWE = '-DIWE'; //'-DIWE';
G.diwe = 'E';
function logD(msg) { if ('-D   '.indexOf(G.diwe) > 0) alert('DEBUG ' + msg); }
function logI(msg) { if ('_DI  '.indexOf(G.diwe) > 0) alert('INFO ' + msg); }
function logW(msg) { if ('_DIW '.indexOf(G.diwe) > 0) alert('WARN ' + msg); }
function logE(msg) { if ('_DIWE'.indexOf(G.diwe) > 0) alert('ERROR ' + msg); }

CSS={};


// Pauses JavaScript execution
// AC17-5/vsh init
function pauseInMillis(millis) {
    var date = new Date();
    var curDate = null;
    do { curDate = new Date(); }
    while(curDate-date < millis);
}


Object.extend = function(destination, source) {
// http://stackoverflow.com/questions/929776/merging-associative-arrays-javascript
// var arr1 = { robert: "bobby", john: "jack" };
// var arr2 = { elizabeth: "liz", jennifer: "jen" };
// var shortnames = Object.extend(arr1,arr2);
    for (var property in source) {
        if (source.hasOwnProperty(property)) {
            destination[property] = source[property];
        }
    }
    return destination;
};


function levenshtein(str1, str2) {
// http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance
    var l1 = str1.length, l2 = str2.length;
    if (Math.min(l1, l2) === 0) {
        return Math.max(l1, l2);
    }
    var i = 0, j = 0, d = [];
    for (i = 0 ; i <= l1 ; i++) {
        d[i] = [];
        d[i][0] = i;
    }
    for (j = 0 ; j <= l2 ; j++) {
        d[0][j] = j;
    }
    for (i = 1 ; i <= l1 ; i++) {
        for (j = 1 ; j <= l2 ; j++) {
            d[i][j] = Math.min(
                d[i - 1][j] + 1,
                d[i][j - 1] + 1, 
                d[i - 1][j - 1] + (str1.charAt(i - 1) === str2.charAt(j - 1) ? 0 : 1)
            );
        }
    }
    return d[l1][l2];
//var poros = [['Ašmys','Aschmies'],['Ašmys','Asmys'],['Ašmys','Asmis']];
//for (var i in poros ) alert(""+ levenshtein(poros[i][0], poros[i][1]);
}

var getPersonFamilyIdsArr = function(person) {
    var familyIds = [];
    if (('fd' in person)) { // the Person is/was maried at least once
        var familyIdsString = ""+person.fd;  // !!! ==> ""+...  is IMPORTANT
        //logD("familyIdsString=|"+familyIdsString+"|");
        //  var familyIds = [];
        //logD("familyIdsString.indexOf(',') =" + familyIdsString.indexOf(','));
        if (familyIdsString.indexOf(",") < 0) {
            familyIds[0] = familyIdsString;
            //logD("familyIds[0]="+familyIds[0]);
        } else {
            familyIds = familyIdsString.split(",");
        } // logD("familyIds="+familyIds);
        //logD("familyIds.length="+familyIds.length);
    }
    return familyIds;
}


var getMaxHusbWifeLines = function(person, fam) {
// Defines max of spouses lines genereted in sketchPerson() in Family family
// B527-5/vsh init
    //alert('fam = ' + fam)
    //var father = var p = g['p'+family.father];
    //return person.rLines;
    //return Math.max(g['p'+fam.father].rLines, g['p'+fam.mother].rLines);
    if (typeof fam == "undefined") return person.rLines;
    else  return Math.max((fam.father==0 ? 0 : g['p'+fam.father].rLines), (fam.mother==0 ? 0 : g['p'+fam.mother].rLines));
}


var getDimension = function(s) {
// Defines a string dimension when style = "font-family: Verdana, font-size: 10px";
// google-gr: [How to get the height and width of a " Text" which has been set the font and size ?]
// AC16-4/vsh init
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
        //logD("w img.height " + img.height);
        //logD("w img.width " + img.width);
        //logD("adjustedHeight " + adjustedHeigth);
        return this.image(url, x, y, size, adjustedHeight);
    } else {
        //logD("h img.height " + img.height);
        //logD("h img.width " + img.width);
        var adjustedWidth = img.width/(parseFloat(img.height/size));
        return this.image(url, x, y, adjustedWidth, size);
    }
}


// Return localized string value or 'stringName' itself
// B108-5/vsh init
function localeString(strName) {
    if (strName in L)
        return L[strName]/*[G.locale]*/;
    else
        return strName;
}


As = {}; 

// Defines possible set of actions for the Person
// B106-4/vsh init
// relPosition: init, leftParent, rightParent, centerParent
Raphael.fn.setActions4Sibling = function(personId) { //===S-i-b-l-i-n-g=========
    var pKey = "p"+personId;
    var p = g[pKey];
    As.a = {};
    // TODO: create and transfer to JS localized strings 
    var asb = getDimension(localeString('js_add_brother'));
    var ass = getDimension(localeString('js_add_sister'));
    var aff = getDimension(localeString('js_add_family'));
    //var afh = getDimension(localeString('js_add_husband'));
    //var afw = getDimension(localeString('js_add_wife'));
    var asc = getDimension(localeString('js_cancel'));
    As.w = Math.max(asb.w, ass.w, asc.w, aff.w/*, afh.w, afw.w*/);
    As.h = Math.max(asb.h, ass.h, asc.h, aff.h/*, afh.h, afw.h*/);
    As.x = /*p.x*/getDelta(p,'x') - As.w - 0.5*G.margin
    As.y = /*p.y*/getDelta(p,'y')
    As.cx = As.x;
    As.cy = As.y;
    
    //var rectActs = this.rect(As.x-1*G.margin-0, As.y-1*G.margin, As.w, As.h, 2);
    //rectActs.attr({fill: "white", "fill-opacity": 0.5, stroke: "#fff", "stroke-width": 2});
    //p.r[p.r.length] = rectActs;

    var fId = ('familyId' in p) ? (0+p.familyId) : '0';
    this.setAction4Sibling(personId, 'js_full_info', G.app+'rest/personView/'+personId);  // B217-4/vsh


    if (G.loggedIn && (personId == G.rootId)) { // D207-4/vsh
        if ('familyId' in p) {
            this.setAction4Sibling(personId, 'js_add_brother', G.app+'rest/'+personId+'/addBrotherToFamily/'+fId);
            this.setAction4Sibling(personId, 'js_add_sister', G.app+'rest/'+personId+'/addSisterToFamily/'+fId);
        }
        this.setAction4Sibling(personId, 'js_add_family', G.app+'rest/'+personId+'/addNewFamily/'+p.gender);
    }
    // B215-2/vsh this.setAction4Sibling(personId, 'js_go_home', G.app+'');
    //this.setAction4Sibling(personId, 'js_full_info', G.app+'rest/'+personId+'/fullInfo');  // B215-2/vsh
    this.setAction4Sibling(personId, 'js_cancel', '');
    
    G.topRect.attr({'fill':'#333', 'fill-opacity':'0.5'})
    var arm = 50; // Action rect margin
    G.topAcctionsRect = this.rect(As.x-arm, As.y-arm, arm+As.w+arm, arm+As.h*5+arm, 10).attr({'fill':'#ff9', 'fill-opacity':'0.5'});
    for (var i in As.a) { 
        As.a[i].show(); 
        As.a[i].toFront(); 
        //As.a[i].translate(getDelta(p,'x')+0, getDelta(p,'y')+0); 
    }
}


                /*
                 //G.topRect.attr({'fill':'#333', 'fill-opacity':'0.5'})
                 //G.topRect.goFront()
                 G.topRectDimmed = paper.rect(0, 0, scale*G.globWidth, scale*G.globHeight).attr({'fill':'#333', 'fill-opacity':'0.5'});
                 G.topRectDimmed.goFront()
                 for (var i in As.a) { As.a[i].goFront();}
                 
    for (var i in g) {
        logD('i = ' + i);
        var x = g[i]
        for (var j in x.r) {
            x.r[j].translate(deltaX, deltaY)
        }
    }

                 */


// Visualize an action for the Person
// B108-6-4/vsh init
Raphael.fn.setAction4Sibling = function(personId, actionName, restUrl) { //=====
    var pKey = "p"+personId;
    var p = g[pKey];
    var localeActionName = localeString(actionName);
    var actionCode = actionName.replace('.', '');
    var anAction = this.text(/*getDelta(personId,'x')*/As.cx, /*getDelta(personId,'y')*/As.cy, localeActionName)//.translate(getDelta(personId,'x'), getDelta(personId,'y'))
  //var anAction = this.text(As.cx, As.cy, localeActionName);
    anAction.attr("text-anchor","start");
    As.cx = As.cx;
    As.cy = As.cy + As.h + G.margin/2;
    As.a[actionCode] = anAction;
    
    var actionBox = this.rect(As.cx-G.margin/2, As.cy-As.h-1.25*G.margin, 
        As.w+1*G.margin, As.h+1*G.margin/4, 0)//.translate(getDelta(personId,'x'), getDelta(personId,'y'))
    As.init = {"fill":"#cff", "stroke":"#000", "opacity":"0.5", "stroke-width":"2"};
    actionBox.attr(As.init);
    As.a[actionCode+'Box'] = actionBox;

    actionBox.node.onmouseover = function() { //logD("anAction.node.onmouseover");
        As.mouseover = {"fill":"#cf9", "stroke":"#000", "opacity":0.5, "stroke-width":"3", "title":"click mouse to " + localeActionName};
        actionBox.attr(As.mouseover);
    };
    
    actionBox.node.onmouseout = function() { //logD("anAction.node.onmouseout");
        As.mouseout = {"fill":"#cff", "stroke":"#000", "opacity":0.5, "stroke-width":"1"};
        actionBox.attr(As.mouseout);
    };
    
    actionBox.node.onclick = function() { //logD("anAction.node.onclick");
        G.topAcctionsRect.remove()
        G.topRect.attr({fill: "270-#eeeeee-#dddddd", stroke: "#fff", opacity: 1})
        actionBox.attr("fill","#fff");
        if (restUrl == '') {
            for (var i in As.a) {
                As.a[i].remove();
            }
            p.arrowLeft.show();
            if ('arrowUp' in p) p.arrowUp.show();
        } else {    
            location.assign(restUrl);
        }
    };
}


Aa = {}; 

// Defines possible set of actions for the Person direct ancestors
// B110-1/vsh init
// relPosition: init, leftParent, rightParent, centerParent
Raphael.fn.setActions4Ancestor = function(personId) { //===A-n-c-e-s-t-o-r========
    var pKey = "p"+personId;
    var p = g[pKey];
    Aa.a = {};
    // TODO: create and transfer to JS localized strings 
    var aaf = getDimension(localeString('js_add_husband'));
    var aam = getDimension(localeString('js_add_wife'));
    var aac = getDimension(localeString('js_cancel'));
    Aa.w = Math.max(aaf.w, aam.w, aac.w);
    Aa.h = Math.max(aaf.h, aam.h, aac.h);
    //Aa.x = p.x - 2*G.margin - Aa.w;
    //Aa.y = p.y - p.h;  //Aa.h;
    Aa.x =/*p.x*/getDelta(p,'x') - 0.5*G.margin - Aa.w
    Aa.y = /*p.y*/getDelta(p,'y')


    Aa.cx = Aa.x;
    Aa.cy = Aa.y;
    
    //var rectActs = this.rect(Aa.x-1*G.margin-0, Aa.y-1*G.margin, Aa.w, Aa.h, 2);
    //rectActs.attr({fill: "white", "fill-opacity": 0.5, stroke: "#ffffff", "stroke-width": 2});
    //p.r[p.r.length] = rectActs;
    
    if ('familyId' in p) { // the person has parents
        var f = g['f'+p.familyId];
        if (f.father == 0) 
            this.setAction4Ancestor(personId, 'js_add_father', G.app+'rest/'+personId+'/addFatherToFamily/'+p.familyId);
        if (f.mother == 0) 
            this.setAction4Ancestor(personId, 'js_add_mother', G.app+'rest/'+personId+'/addMotherToFamily/'+p.familyId);
    } else {
        this.setAction4Ancestor(personId, 'js_add_father', G.app+'rest/'+personId+'/addFatherToFamily/'+0);
        this.setAction4Ancestor(personId, 'js_add_mother', G.app+'rest/'+personId+'/addMotherToFamily/'+0);
    }        
    // B215-2  this.setAction4Ancestor(personId, 'js_go_home', G.app+'');
    this.setAction4Ancestor(personId, 'js_cancel', '');
    
    //for (var i in Aa.a) { Aa.a[i].show(); }
    
    G.topRect.attr({'fill':'#333', 'fill-opacity':'0.5'})
    var arm = 50; // Action rect margin
    G.topAcctionsRect = this.rect(Aa.x-arm, Aa.y-arm, arm+Aa.w+arm, arm+Aa.h*5+arm, 10).attr({'fill':'#ff9', 'fill-opacity':'0.5'});
    for (var i in Aa.a) { 
        Aa.a[i].show(); 
        Aa.a[i].toFront(); 
        //As.a[i].translate(getDelta(p,'x')+0, getDelta(p,'y')+0); 
    }
    
}


// Visualize an action for the Person direct ancestor
// B110-1/vsh init
Raphael.fn.setAction4Ancestor = function(personId, actionName, restUrl) { //====
    var pKey = "p"+personId;
    var p = g[pKey];
    var localeActionName = localeString(actionName);
    var actionCode = actionName.replace('.', '');
    var anAction = this.text(Aa.cx, Aa.cy, localeActionName);
    anAction.attr("text-anchor","start");
    Aa.cx = Aa.cx;
    Aa.cy = Aa.cy + Aa.h + G.margin/2;
    Aa.a[actionCode] = anAction;
    
    var actionBox = this.rect(Aa.cx-G.margin/2, Aa.cy-Aa.h-1.25*G.margin, Aa.w+1*G.margin, Aa.h+1*G.margin/4, 0);
    Aa.init = {"fill":"#cff", "stroke":"#000", "opacity":"0.5", "stroke-width":"2"};
    actionBox.attr(Aa.init);
    Aa.a[actionCode+'Box'] = actionBox;

    actionBox.node.onmouseover = function() { //logD("anAction.node.onmouseover");
        Aa.mouseover = {"fill":"#cf9", "stroke":"#000", "opacity":0.5, "stroke-width":"3", "title":"click mouse to " + localeActionName};
        actionBox.attr(Aa.mouseover);
    };
    
    actionBox.node.onmouseout = function() { //logD("anAction.node.onmouseout");
        Aa.mouseout = {"fill":"#cff", "stroke":"#000", "opacity":0.5, "stroke-width":"1"};
        actionBox.attr(Aa.mouseout);
    };
    actionBox.node.onclick = function() { //logD("anAction.node.onclick");
        G.topAcctionsRect.remove()
        G.topRect.attr({fill: "270-#eeeeee-#dddddd", stroke: "#fff", opacity: 1})
        actionBox.attr("fill","#fff");
        actionBox.attr("fill","#fff");
        if (restUrl == '') {
            for (var i in Aa.a) {
                Aa.a[i].remove();
            }
            p.arrowUp.show();
            p.arrowLeft.show();
        } else {    
            location.assign(restUrl);
        }
    };
}

// find an object position (after forest move to upper left corner of canvas ---
// B127-4/vsh init
getDelta = function(p, x_or_y) {
    var realX = p.r[0].getBBox().x
    var realY = p.r[0].getBBox().y
    return (x_or_y == 'x') ? realX : realY;
};


// Defines a Person box w and h
// B123-7/vsh init
sketchPerson = function() { //================================================== 
    G.diwe_=G.diwe; //G.diwe = 'D';    //G.diwe=G.diwe_;
    for (var i in g) {
        if (i.indexOf('p') >= 0) {
            var p = g[i/*pKey*/];
            var gender = (p.gender=="M") ? '♂' : '♀';
            var whGenderM = getDimension('♂');
            var whGenderF = getDimension('♀');
            var gWidth = Math.max(whGenderM.w, whGenderF.w);
            var gHeight = Math.max(whGenderM.h, whGenderF.h);
            var whNameGivn = getDimension(p.nameGivn);
            var whNameSurn = getDimension(p.nameSurn);
            var whBD = {w:0,h:0};
            var whBP = {w:0,h:0};
            var whDD = {w:0,h:0};
            var whDP = {w:0,h:0};
            if (p.bd.trim().length > 0 && p.bp.trim().length > 0) {
                whBD = getDimension('* '+p.bd);
                whBP = getDimension('  '+p.bp);
            } else if (p.bd.trim().length > 0) {
                whBD = getDimension('* '+p.bd);
            } else if (p.bp.trim().length > 0) {
                whBP = getDimension('* '+p.bp);
            }
            if (p.dd.trim().length > 0 && p.dp.trim().length > 0) {
                whDD = getDimension('+ '+p.dd);
                whDP = getDimension('  '+p.dp);
            } else if (p.dd.trim().length > 0) {
                whDD = getDimension('+ '+p.dd);
            } else if (p.dp.trim().length > 0) {
                whDP = getDimension('+ '+p.dp);
            }
            /*var whBD = getDimension('* '+p.bd);
            var whBP = getDimension('  '+p.bp);
            var whDD = getDimension('+ '+p.dd);
            var whDP = getDimension('  '+p.dp);*/

            //var rLines = 6-0;
            var rLines = 2 + (whBD.w>0 ? 1 : 0) + (whBP.w>0 ? 1 : 0) + (whDD.w>0 ? 1 : 0) + (whDP.w>0 ? 1 : 0);  //alert(nLines);
            var textWidth = Math.max(whNameGivn.w, whNameSurn.w, whBD.w, whBP.w, whDD.w, whDP.w) + gWidth;
            var textLineHeight = Math.max(whNameGivn.h, whNameSurn.h, whBD.h, whBP.h, whDD.h, whDP.h) + 0*gHeight;
            p.rLines = rLines;
            p.w = textWidth /*+ 2*G.margin*/;
            p.h = /*rLines**/textLineHeight /*+ G.margin/2*/;
            //logD('--] sP personId w h rLines ' + i+'  '+p.w+' '+p.h+' '+p.rLines)
        }
    }
    G.diwe=G.diwe_;
}

// Draws a Person info
// AC16-4/vsh init
// relPosition: init, leftParent, rightParent, centerParent
Raphael.fn.drawPerson = function(personId, relPosition) { //==================== 
    G.diwe_=G.diwe; G.diwe = 'I';
    //logD('[-- dP personId relPosition "' + personId+'" '+relPosition)
    var pKey = "p"+personId;    //alert(pKey);
    var p = g[pKey];            //alert(p);
    if (p.r.length > 0)/*('x' in p)*/ { // the Person box is built now
        //logD('--](skip) dP personId relPosition ' + personId+' '+relPosition);
        return;
    }
    var gender = (p.gender=="M") ? '♂' : '♀';
    var nLines = p.rLines;
    
    textWidth = p.w /*- 2*G.margin*/;
    textLineHeight = p.h /*(p.h - G.margin/2)/nLines*/;
    
    if (G.direction == 'BT') {
        if (relPosition == 'personOnly') {
            var f = G._f;
            var nLines = p.rLines;    //alert('pe-init: nLines='+nLines);
            //var xfbbm = f.x + f.w/2 - 0*G.margin/* - f.childrenWidth/2 + f.wMean/2*/;  // Family Box
            //var yfbbm = f.y + f.h + G.vertGapBetweenGenerations;                       // Bottom Middle
            //var realXY = locateXpendBoxWise(p, /*'cLR',*/ xfbbm, yfbbm);
            var xptfc = 150; //f.x + f.w/2 - 0*G.margin/* - f.childrenWidth/2 + f.wMean/2*/;  //
            var yptfc = 50; //f.y + f.h + G.vertGapBetweenGenerations;                       //
            var realXY = locateXpendBox(p, 'cLR', xptfc, yptfc);
            var x = realXY.x;
            var y = realXY.y;
            //var x = G.initX - 1*textWidth/2;
            //var y = G.initY - 1*textLineHeight * nLines;
            p.y = y; // - G.margin;
            delete G._f;

//        } else if (relPosition == 'init') {
//            var f = G._f;
//            var nLines = getMaxHusbWifeLines(p, f);    //alert('pe-init: nLines='+nLines);
//            var x = G.initX - 1*textWidth/2;
//            var y = G.initY - 1*textLineHeight * nLines;
//            p.y = y - G.margin;
//            delete G._f;
//
//        } else if (relPosition == 'otherParent') {     //alert('pe-otherParent');
//
//        } else if (relPosition == 'leftParent') {      //alert('pe-leftParent');
//            var f = G._f; //-- get Family obj
//            var x = f.x + f.w/2 - textWidth;
//            var nLines = getMaxHusbWifeLines(p, f);    //alert('pe-leftParent: nLines='+nLines);
//            var y = f.y - nLines*textLineHeight;
//            //var y = f.y - /*nLines*/getMaxHusbWifeLines(p, f)*textLineHeight;
//            p.y = y + G.margin/2;
//
//        } else if (relPosition == 'rightParent') {     //alert('pe-rightParent');
//            var f = G._f;
//            var x = f.x + f.w/2 + 2*G.margin;
//            var nLines = getMaxHusbWifeLines(p, f);    //alert('pe-rightParent: nLines='+nLines);
//            var y = f.y - nLines*textLineHeight;
//            //var y = f.y - /*nLines*/getMaxHusbWifeLines(p, f)*textLineHeight;
//            p.y = y + G.margin/2;  // p.y = y - G.margin;
//
//        } else if (relPosition == 'centerParent') {  //alert('pe-centerParent');
//            var f = G._f;
//            var x = f.x + f.w/2 - (textWidth + 2*G.margin)/2 + 1*G.margin;
//            var nLines = getMaxHusbWifeLines(p, f);    //alert('pe-centerParent: nLines='+nLines);
//            var y = f.y - nLines*textLineHeight;
//            //var y = f.y - /*nLines*/getMaxHusbWifeLines(p, f)*textLineHeight;
//            p.y = y + G.margin/2;

        } else if (relPosition == 'spouse') {          //alert('pe-spouse');
            var sp = G._p // get spouse Person obj
            var nLines = getMaxHusbWifeLines(p, G._f); //alert('pe-spouse: nLines='+nLines);
            //var x = sp.x + sp.w + 0*G.margin;
            //var y = sp.y;
         //   f = g['f'+sp.familyId]; // !!! may be undefined
         //   var gg_wMin = G[f.generation+'_wMin'];
         //   var gg_wMax = G[f.generation+'_wMax'];
            var x = sp.x + sp.w + 2*G.margin;
            var y = sp.y;
            p.y = sp.y;

//        } else if (relPosition == 'child') {  //alert('pe-child');
//            var f = G._f;
//// TODO B528-6/vsh display the person all families
//            var nLines = getMaxHusbWifeLines(p, g['f'+getPersonFamilyIdsArr(p)[0]]); //alert('pe-child: nLines='+nLines);    // !!!
//
//            /* B...-./vsh deprecated
//            var realXY = locateBox(p, 'cLR', xa, ya);*/
//
//            /* B528-6/vsh deprecated
//            var xa = f.x + f.w/2 - G.margin - f.childrenWidth/2 + f.wMean/2;  // cLR
//            var ya = f.y + f.h + G.vertGapBetweenGenerations;
//            var realXY = locateXpendBox(p, 'cLR', xa, ya);*/
//
//            var xfbbm = f.x + f.w/2 - 0*G.margin/* - f.childrenWidth/2 + f.wMean/2*/;  // Family Box
//            var yfbbm = f.y + f.h + G.vertGapBetweenGenerations;                       // Bottom Middle
//            var realXY = locateXpendBoxWise(p, /*'cLR',*/ xfbbm, yfbbm);
//            var x = realXY.x;
//            var y = realXY.y;
//            p.y = y;
//            //G.diwe_=G.diwe; G.diwe = 'D';
//            //logD("--------child: p.x p.y f.x f.y "+p.nameGivn+" "+p.x+" "+p.y+"  || "+f.x+" "+f.y);
//            //G.diwe=G.diwe_;
//        } else {
//            G.diwe = 'E'
//            logE('Raphael.fn.drawPerson: unsupported relPosition=|' + relPosition + '|');
//            return;

        }     
        logD('dP finally: personId nLines ' +pKey+' '+nLines)
        p.x = x - 0*G.margin;
        updateGlobVars(p, p.x-1*G.margin, p.y-1*G.margin, p.w, p.h, 'drawPerson');

        CSS.font = {'font-family':'Verdana', 'font-size':'10px'};
        CSS.text = {'fill':'#000000', 'stroke-width':'1', 'text-anchor':'start'};
        CSS.male = CSS.female = CSS.sex = {'fill-opacity':'0.9', 'stroke':'#000', 'stroke-width':'1', 'text-anchor':'start'};
        CSS.male = {'fill':'270-#cff-#fff'};
        CSS.female = {'fill':'270-#fcf-#fff'}; 
        CSS.maleOver = {'fill':'90-#6ff-#fff'};
        CSS.femaleOver = {'fill':'90-#f6f-#fff'}; 
        //CSS.male = $.extend( CSS.sex, {'fill':'270-#cff-#fff'})
        //CSS.female = $.extend( CSS.sex, {'fill':'270-#fcf-#fff'})
        //CSS.male['fill'] = '270-#cff-#fff'
        //CSS.female['fill'] = '270-#fcf-#fff'

        CSS.gender = ((p.gender=='M') ?  CSS.male : CSS.female);
        CSS.genderOver = ((p.gender=='M') ?  CSS.maleOver : CSS.femaleOver);
        p.r[p.r.length] = '';  //placeholder for this person top box

        //var rect1 = this.rect(p.x-1*G.margin, p.y-1*G.margin, p.w, p.h, 5).attr(CSS.gender)
        //alert('rect1: nLines='+nLines);
        var rect1 = this.rect(p.x - G.margin, p.y - G.margin, p.w + 2*G.margin, p.h*nLines/*getMaxHusbWifeLines(p, f)*/ + G.margin, 5).attr(CSS.gender)

        p.r[0] = rect1;
        var text1 = this.text(p.x, p.y, gender+' '+p.nameGivn).attr(CSS.font).attr(CSS.text)
        p.r[p.r.length] = text1;   //logD("p.r[p.r.length] = " + p.r[p.r.length-1]);
        var text2 = this.text(p.x, p.y + textLineHeight, p.nameSurn).attr(CSS.font).attr(CSS.text)
        p.r[p.r.length] = text2;
        var text3 = this.text(p.x, p.y + 2*textLineHeight, ((p.bd==''&& p.bp=='') ? ' ' : '*')+' '+p.bd).attr(CSS.font).attr(CSS.text)
        p.r[p.r.length] = text3;
        var text4 = this.text(p.x, p.y + 3*textLineHeight, '  '+p.bp).attr(CSS.font).attr(CSS.text)
        p.r[p.r.length] = text4;
        var text5 = this.text(p.x, p.y + 4*textLineHeight, ((p.dd=='' && p.dp=='') ? ' ' : '+')+' '+p.dd).attr(CSS.font).attr(CSS.text)
        p.r[p.r.length] = text5;
        var text6 = this.text(p.x, p.y + 5*textLineHeight, ' '+p.dp).attr(CSS.font).attr(CSS.text)
        p.r[p.r.length] = text6;
        
        //rect1.toFront();
        //logD(""+p.x-1*G.margin+" "+p.y-1*G.margin+" "+p.w+" "+p.h+" "+5);
        //var rect1 = this.rect(p.x-1*G.margin, p.y-1*G.margin, p.w, p.h, 5).attr(CSS.gender)
        //p.r[0/*p.r.length*/] = rect1;

        rect1.node.onclick = function() { //logD("rect1.node.onclick");
            rect1.attr("fill", "#fff");
            location.assign ( G.app+"rest/person/" + p.id);
        };
        
        rect1.node.onmouseover = function() { //logD("rect1.node.onmouseover");
            rect1.attr(/*CSS.genderOver*/((p.gender=='M') ?  CSS.maleOver : CSS.femaleOver)) //.attr({fill: '90-#f6f-#fff', opacity: 0.5});
            rect1.attr("title", "click mouse to change current person");
        };
        rect1.node.onmouseout = function() { //logD("rect1.node.onmouseout");
            rect1.attr(/*CSS.gender*/((p.gender=='M') ?  CSS.male : CSS.female)) //.attr({fill: '270-#fcf-#fff', opacity: 0.5});
        };
        
        //if (G.loggedIn && (relPosition == 'init')) { // goLeft, goUp icon shape is (p.h X p.h)
        //if (G.loggedIn && (personId == G.rootId)) { // goLeft, goUp icon shape is (p.h X p.h)
        if ((personId == G.rootId)) { // D207-4/vsh // goLeft, goUp icon shape is (p.h X p.h)
            // goLeft icon drawing  S-i-b-l-i-n-g  S-i-b-l-i-n-g  S-i-b-l-i-n-g
            var xgli = p.x-p.h-1*G.margin-1;
            var ygli = p.y-1*G.margin;
            
            var arrowLeft = paper.drawImage( G.app+'images/goLeft.png', xgli, ygli, 'w', p.h);
            p.r[p.r.length] = arrowLeft;
            p.arrowLeft = arrowLeft; // !!!
            
//            var rect2 = this.rect(xgli, ygli, p.h, p.h, 1);
//            rect2.attr({fill: "ff3", "fill-opacity":"0", stroke: "#fff", "stroke-width":"1"});
//            p.r[p.r.length] = rect2;
            
            arrowLeft.node.onclick = function() { ///*logD*/alert("arrowLeft.node.onclick");
                 arrowLeft.hide();
                 if ('arrowUp' in p) p.arrowUp.hide(); 
                 paper.setActions4Sibling(p.id);
            };
            
            arrowLeft.node.onmouseover = function() {  //logD("arrowLeft.node.onmouseover");
                //arrowLeft.show()
                //rect2.attr({fill: "#ff0", opacity: 1});
                arrowLeft.attr("title", "click mouse to show available actions for the person sibling");
                //rect2.attr({fill: "#111", "fill-opacity":0, stroke: "#0ff", "stroke-width": 2});
            };
            arrowLeft.node.onmouseout = function() {  //logD("arrowLeft.node.onmouseout");
                //arrowLeft.hide();
                //rect2.attr({fill: "#fff", opacity: 0.5});
                 //rect2.attr('display', 'none');
                //rect2.attr({fill: "#000", "fill-opacity": 0, stroke: "#fff", "stroke-width": 2});
            };
            
            // goUp icon drawing  A-n-c-e-s-t-o-r  A-n-c-e-s-t-o-r  A-n-c-e-s-t-o-r  A-n-c-e-s-t-o-r
            var f = g['f'+p.familyId];
            if ((!('familyId' in p)) || (!(f.father > 0)) || (!(f.mother > 0))) {
                var xglj = p.x + 0*(p.w-p.h)/2 - G.margin;
                var yglj = p.y-p.h - 1.1*G.margin;
                
                var arrowUp = paper.drawImage( G.app+'images/goUp.png', xglj, yglj, 'w', p.h);
                p.r[p.r.length] = arrowUp;
                p.arrowUp = arrowUp; // !!!
                
//                var recta = this.rect(xglj, yglj, p.h, p.h, 1);
//                recta.attr({"fill":"white", "fill-opacity":"0", "stroke":"#fff", "stroke-width":"0"});
//                //recta.attr({"fill":"ff3",   "fill-opacity":"1", "stroke":"#fff", "stroke-width":"0"});
//                p.r[p.r.length] = recta;
                
                arrowUp.node.onclick = function() { ///*logD*/alert("arrowUp.node.onclick");
                    arrowLeft.hide();
                    arrowUp.hide();
                    paper.setActions4Ancestor(p.id);
                };
                arrowUp.node.onmouseover = function() { ///*logD*/alert("arrowUp.node.onmouseover");
                    //arrowUp.show()
                    //recta.attr({fill: "#ff0", opacity: 1});
                    arrowUp.attr("title", "click mouse to show available actions for the person ancestor");
                    //recta.attr({fill: "#111", "fill-opacity":"0", stroke: "#0ff", "stroke-width": 2});
                };
                arrowUp.node.onmouseout = function() { ///*logD*/alert("arrowUp.node.onmouseout");
                    //arrowUp.hide();
                    //recta.attr({fill: "#fff", opacity: 0.5});
                     //recta.attr('display', 'none');
                    //recta.attr({fill: "#000", "fill-opacity": 0, stroke: "#fff", "stroke-width": 2});
                };
            }
            
        }
    } else {
         //logD("direction must be 'BT' [BottomTop] only");
    }
    
/* BA20-4  moved below to draw the parents first
    if (('fd' in p)) { // the Person is/was maried at least once
        var familyIds = getPersonFamilyIdsArr(p);
        for (var j = 0; j < familyIds.length; j++){
            //logD(" = " + familyIds[j]);
            this.drawFamily(familyIds[j], personId, 0); // (familyId, skipsSpouseId, skipChilId)
        }
    }
*/

    //logD("familyId=" + ('familyId' in p));

//    if ('familyId' in p) { // the Person has parents
//        var fKey = 'f'+p.familyId;   //logD(f);
//        if (fKey == 'f1') alert('fKey=' + fKey);
//        if (fKey in g) {  // B130-7: opposite may happen because of forest show narrowing
//            if (fKey == 'f1') alert('fKey in g fKey=' + fKey);
//            var f = g[fKey];   //logD(f);
//// TODO     // nebaigta !!! var spouseId = (('mother' in f) && (personId == f.mother)) ? (('father' in fd) ? f.father : '0') :
//            //    (('father' in fd) && (personId == f.father)) ? (('mother' in fd) ? f.mother : '0') : '0';
//            /* BB07-1/vsh
//            this.drawFamily(p.familyId, 0, personId); // (familyId, skipsSpouseId, skipChilId)
//            */
//            //G.diwe_temp = G.diwe; G.diwe ='D'
//            var fmw = f.x + f.w/2;                                  //logD("f.x f.w "+f.x+" "+f.w);
//            var fmh = f.y + f.h*f.rLines + 0*G.margin/2;            //logD("f.y f.h "+f.y+" "+f.h);
//
//            //--var plw = p.x + p.w/2 - G.margin;
//            var plw = p.x + p.w/2 + 0*G.margin;
//
//            var plh = p.y - G.margin;                               //logD('M'+fmw+' '+fmh+'L'+plw+' '+plh);
//            if (fKey == 'f1') alert('fKey in g fKey=' + fKey + '------- ' + 'M'+fmw+' '+fmh+'L'+plw+' '+plh);
//            var path1 = this.path('M'+fmw+' '+fmh+'L'+plw+' '+plh);  // alert("drawPerson relPosition = " + relPosition);
//            if (fKey == 'f1') alert('fKey in g fKey=' + fKey + '=======');
//            p.r[p.r.length] = path1;
//            //logD("Raphael.fn.drawPerson: p.r[p.r.length-1] = " +  p.r[p.r.length-1]);
//            //G.diwe = G.diwe_temp;
//        }
//    }

    //logD('--] dP personId relPosition w h x y ' + personId+' '+relPosition+' '+p.w+' '+p.h+' '+p.x+' '+p.y)

    if (('fd' in p)) { // the Person is/was maried at least once
        var familyIds = getPersonFamilyIdsArr(p);
        for (var j = 0; j < familyIds.length; j++){
            logD(" = " + familyIds[j]);
            this.drawFamily(familyIds[j], personId, 0); // (familyId, skipsSpouseId, skipChilId)
        }
    }

    G.diwe=G.diwe_;
}


// Defines a Person box w and h
// B123-7/vsh init
sketchFamily = function() { //================================================== 
    G.diwe_=G.diwe; //G.diwe = 'D';
    for (var i in g) {
        if (i.indexOf('f') >= 0) {
            var f = g[i];
            //var rLines = 2;
            //var w = 30;  //var fWidth = 30;
            //var h = 5;   //var fHeight = 5;
            //f.w = w + 2*G.margin;
            //f.h = rLines*h + G.margin/2; // kad vert jungtys būtų vientisos
            var whMD = getDimension('oo  '+f.md);
            var whMP = getDimension('    '+f.mp);
            var whDD = getDimension('o-o '+f.dd);
            var whDP = getDimension('    '+f.dp);
            var rLines = Math.max((whMD.w>0 ? 1 : 0) +(whMP.w>0 ? 1 : 0) + (whDD.w>0 ? 1 : 0) + (whDP.w>0 ? 1 : 0), 1);
            var textWidth = Math.max(whMD.w, whMP.w, whDD.w, whDP.w);
            var textLineHeight = Math.max(whMD.h, whMP.h, whDD.h, whDP.h);
            f.rLines = rLines;
            f.w = textWidth /*+ 2*G.margin*/;
            f.h = /*rLines**/textLineHeight /*+ G.margin/2*/; // kad vert jungtys būtų vientisos
            //logD('--] sF fmilyId w h  ' + i+'  '+f.w+' '+f.h)
            
            if ('children' in f) {
                var childIdsString = ""+f.children;  // !!! ==> ""+...  is IMPORTANT
                // logD("childIdsString=|"+childIdsString+"|");
                var childIds = [];
                if (childIdsString.indexOf(',') < 0) {
                    childIds[0] = childIdsString; // logD("childIds[0]="+childIds[0]);
                } else {
                    childIds = childIdsString.split(','); // logD("childIds="+childIds); 
                } 
                //logD("childIds.length="+childIds.length);
        
                f.childrenWidth = 0;
                f.childrenNum = childIds.length;
                var siblSep = 0;
                for (var j = 0; j < childIds.length; j++){
                   if (!('p'+childIds[j] in g)) continue; //  B130-7: it may happen because of forest show narrowing
                   //logD(" = " + childIds[j]);
                   if ('w' in g['p'+childIds[j]]) {
                        //logD("childIds[j]="+childIds[j]);  // logFamily(familyId);
                        f.childrenWidth = f.childrenWidth + siblSep + g['p'+childIds[j]].w;
                        siblSep = G.horizGapBetweenSiblings;
                   } else {
                   }
                }
            }
            f.wMean = (f.childrenWidth - (f.childrenNum - 1)*G.horizGapBetweenSiblings)/f.childrenNum;
        }
    }
    G.diwe=G.diwe_;
}


Afs = {}; 

// Defines possible set of actions for the Person
// B106-4/vsh init
// relPosition: init, leftParent, rightParent, centerParent
Raphael.fn.setActions4FamilySpouse = function(familyId) { //===S-p-o-u-s-e======
    var fKey = "f"+familyId;
    var f = g[fKey];
    Afs.a = {};
    // TODO: create and transfer to JS localized strings 
    //var asb = getDimension(localeString('js_add_brother'));
    //var ass = getDimension(localeString('js_add_sister'));
    var afh = getDimension(localeString('js_add_husband'));
    var afw = getDimension(localeString('js_add_wife'));
    var asc = getDimension(localeString('js_cancel'));
    Afs.w = Math.max(/*asb.w, ass.w,*/ asc.w, afh.w, afw.w);
    Afs.h = Math.max(/*asb.h, ass.h,*/ asc.h, afh.h, afw.h);
    Afs.x = getDelta(f,'x') - 0.5*G.margin - As.w
    Afs.y = getDelta(f,'y')
    Afs.cx = Afs.x;
    Afs.cy = Afs.y;
    
    //var rectActs = this.rect(Afs.x-1*G.margin-0, Afs.y-1*G.margin, Afs.w, Afs.h, 2);
    //rectActs.attr({fill: "white", "fill-opacity": 0.5, stroke: "#fff", "stroke-width": 2});
    //f.r[f.r.length] = rectActs;

//    var fId = ('familyId' in p) ? (0+p.familyId) : '0';
    if (f.father == 0 || f.mother == 0) {
        var gender = ('father' in f) ? "F" : "M"
      //this.setAction4FamilySpouse(familyId, 'js_add_spouse', G.app+'rest/'+familyId+'/addSpouseToFamily/'+gender);
        this.setAction4FamilySpouse(familyId, 'js_add_spouse', G.app+'rest/'+G.rootId+'/addSpouseToFamily/'+familyId);
        this.setAction4FamilySpouse(familyId, 'js_go_home', G.app+'');
        this.setAction4FamilySpouse(familyId, 'js_cancel', '');
    }        
    
    //for (var i in Afs.a) { Afs.a[i].show(); }
    G.topRect.attr({'fill':'#333', 'fill-opacity':'0.5'})
    var arm = 50; // Action rect margin
    G.topAcctionsRect = this.rect(Afs.x-arm, Afs.y-arm, arm+Afs.w+arm, arm+Afs.h*5+arm, 10).attr({'fill':'#ff9', 'fill-opacity':'0.5'});
    for (var i in Afs.a) { 
        Afs.a[i].show(); 
        Afs.a[i].toFront(); 
        //As.a[i].translate(getDelta(p,'x')+0, getDelta(p,'y')+0); 
    }
    
}


// Visualize an action for the Person
// B108-6-4/vsh init
Raphael.fn.setAction4FamilySpouse = function(familyId, actionName, restUrl) { //=====
    var fKey = "f"+familyId;
    var f = g[fKey];
    var localeActionName = localeString(actionName);
    var actionCode = actionName.replace('.', '');
    var anAction = this.text(Afs.cx, Afs.cy, localeActionName)
    anAction.attr("text-anchor","start");
    Afs.cx = Afs.cx;
    Afs.cy = Afs.cy + Afs.h + G.margin/2;
    Afs.a[actionCode] = anAction;
    
    var actionBox = this.rect(Afs.cx-G.margin/2, Afs.cy-Afs.h-1.25*G.margin, 
        Afs.w+1*G.margin, Afs.h+1*G.margin/4, 0)
    Afs.init = {"fill":"#cff", "stroke":"#000", "opacity":"0.5", "stroke-width":"2"};
    actionBox.attr(Afs.init);
    Afs.a[actionCode+'Box'] = actionBox;

    actionBox.node.onmouseover = function() { //logD("anAction.node.onmouseover");
        Afs.mouseover = {"fill":"#cf9", "stroke":"#000", "opacity":0.5, "stroke-width":"3", "title":"click mouse to " + localeActionName};
        actionBox.attr(Afs.mouseover);
    };
    
    actionBox.node.onmouseout = function() { //logD("anAction.node.onmouseout");
        Afs.mouseout = {"fill":"#cff", "stroke":"#000", "opacity":0.5, "stroke-width":"1"};
        actionBox.attr(Afs.mouseout);
    };
    actionBox.node.onclick = function() { //logD("anAction.node.onclick");
        G.topAcctionsRect.remove()
        G.topRect.attr({fill: "270-#eeeeee-#dddddd", stroke: "#fff", opacity: 1})
        actionBox.attr("fill","#fff");
        if (restUrl == '') {
            for (var i in Afs.a) {
                Afs.a[i].remove();
            }
            if ('arrowLeft' in f) f.arrowLeft.show();
            if ('arrowDown' in f) f.arrowDown.show();
        } else {    
            location.assign(restUrl);
        }
    };
}


Afc = {}; 

// Defines possible set of actions for the Person direct ancestors
// B110-1/vsh init
// relPosition: init, leftParent, rightParent, centerParent
Raphael.fn.setActions4FamilyChildren = function(familyId) { //===C-h-i-l-d-r-e-n====
    var fKey = "f"+familyId;
    var f = g[fKey];
    Afc.a = {};
    // TODO: create and transfer to JS localized strings 
    var aaf = getDimension(localeString('js_add_son'));
    var aam = getDimension(localeString('js_add_daughter'));
    var aac = getDimension(localeString('js_cancel'));
    Afc.w = Math.max(aaf.w, aam.w, aac.w);
    Afc.h = Math.max(aaf.h, aam.h, aac.h);
    //Afc.x = p.x - 2*G.margin - Afc.w;
    //Afc.y = p.y - p.h;  //Afc.h;
    Afc.x =/*p.x*/getDelta(f,'x') - 0.5*G.margin - Afc.w
    Afc.y = /*p.y*/getDelta(f,'y')


    Afc.cx = Afc.x;
    Afc.cy = Afc.y;
    
    var rectActs = this.rect(Afc.x-1*G.margin-0, Afc.y-1*G.margin, Afc.w, Afc.h, 2);
    rectActs.attr({fill: "white", "fill-opacity": 0.5, stroke: "#ffffff", "stroke-width": 2});
    f.r[f.r.length] = rectActs;
    
    this.setAction4FamilyChildren(familyId, 'js_add_son', G.app+'rest/'+G.rootId+'/addSonToFamily/'+familyId);
    this.setAction4FamilyChildren(familyId, 'js_add_daughter', G.app+'rest/'+G.rootId+'/addDaughterToFamily/'+familyId);
    this.setAction4FamilyChildren(familyId, 'js_go_home', G.app+'');
    this.setAction4FamilyChildren(familyId, 'js_cancel', '');
    
    //for (var i in Afc.a) { Afc.a[i].show(); }
    G.topRect.attr({'fill':'#333', 'fill-opacity':'0.5'})
    var arm = 50; // Action rect margin
    G.topAcctionsRect = this.rect(Afc.x-arm, Afc.y-arm, arm+Afc.w+arm, arm+Afc.h*5+arm, 10).attr({'fill':'#ff9', 'fill-opacity':'0.5'});
    for (var i in Afc.a) { 
        Afc.a[i].show(); 
        Afc.a[i].toFront(); 
        //As.a[i].translate(getDelta(p,'x')+0, getDelta(p,'y')+0); 
    }
}


// Visualize an action for the Person direct ancestor
// B110-1/vsh init
Raphael.fn.setAction4FamilyChildren = function(familyId, actionName, restUrl) { //====
    var fKey = "f"+familyId;
    var f = g[fKey];
    var localeActionName = localeString(actionName);
    var actionCode = actionName.replace('.', '');
    var anAction = this.text(Afc.cx, Afc.cy, localeActionName);
    anAction.attr("text-anchor","start");
    Afc.cx = Afc.cx;
    Afc.cy = Afc.cy + Afc.h + G.margin/2;
    Afc.a[actionCode] = anAction;
    
    var actionBox = this.rect(Afc.cx-G.margin/2, Afc.cy-Afc.h-1.25*G.margin, Afc.w+1*G.margin, Afc.h+1*G.margin/4, 0);
    Afc.init = {"fill":"#cff", "stroke":"#000", "opacity":"0.5", "stroke-width":"2"};
    actionBox.attr(Afc.init);
    Afc.a[actionCode+'Box'] = actionBox;

    actionBox.node.onmouseover = function() { //logD("anAction.node.onmouseover");
        Afc.mouseover = {"fill":"#cf9", "stroke":"#000", "opacity":0.5, "stroke-width":"3", "title":"click mouse to " + localeActionName};
        actionBox.attr(Afc.mouseover);
    };
    
    actionBox.node.onmouseout = function() { //logD("anAction.node.onmouseout");
        Afc.mouseout = {"fill":"#cff", "stroke":"#000", "opacity":0.5, "stroke-width":"1"};
        actionBox.attr(Afc.mouseout);
    };
    actionBox.node.onclick = function() { //logD("anAction.node.onclick");
        G.topAcctionsRect.remove()
        G.topRect.attr({fill: "270-#eeeeee-#dddddd", stroke: "#fff", opacity: 1})
        actionBox.attr("fill","#fff");
        if (restUrl == '') {
            for (var i in Afc.a) {
                Afc.a[i].remove();
            }
            if ('arrowDown' in f) f.arrowDown.show();
            if ('arrowLeft' in f) f.arrowLeft.show(); 
        } else {    
            location.assign(restUrl);
        }
    };
}


// AC28-2/vsh init
Raphael.fn.drawFamily = function(familyId, skipSpouseId, skipChildId) {
    G.diwe_=G.diwe; //G.diwe = 'D';
    logD('--] dF familyId G.shownFam.length G.shownFam.indexOf(familyId)  ' + familyId+' '+G.shownFam.length+' '+G.shownFam.indexOf(familyId));
    if (G.shownFam.indexOf(familyId) >= 0) {          //alert(99999999999999999999);
        // the family has been shown now
        return;
    }
    G.shownFam[G.shownFam.length] = familyId
    //logD('[-- dF familyId skipSpouseId skipChildId ' + familyId+' '+skipSpouseId+' '+skipChildId);
    var fKey = "f"+familyId;
    var f = g[fKey];
    if (f.r.length > 0)/*('x' in f)*/ { // the Family box is built now
        //logD('--](skip) dF familyId skipSpouseId skipChildId w h x y ' + familyId+' '+skipSpouseId+' '+skipChildId+' '+f.w+' '+f.h+' '+f.x+' '+f.y);
        return;
    }
    CSS.font = {'font-family':'Verdana', 'font-size':'10px'};
    CSS.text = {'fill':'#000000', 'stroke-width':'1', 'text-anchor':'start'};
    CSS.familyBox = {'fill':"#ffffff", 'stroke':"#000", 'opacity':'1.0'};

    //var nLines = f.rLines;
    if ('fe' in g["f"+familyId]) {
        logE("Raphael.fn.drawFamily: the function 'if'  branch needs to be written !")
    } else {
        var w = f.w /*- 2*G.margin*/;
        var h = f.h /*(f.h - G.margin/2)/nLines*/; // kad vert jungtys būtų vientisos
        //textWidth = f.w - 2*G.margin;
        //textLineHeight = (f.h - G.margin/2)/nLines;

        if (skipChildId > 0) { // draw Family box above the Person (child)
            //alert("==>*<==  draw Family box above the Person (child)");
            // define free space for spouses
            var gg = 'g'+f.generation 
            var freeX = G[gg+'_wMax']
            var freeY = G[gg+'_hMax']

//            f.w = textWidth /*+ 2*G.margin*/;
//            f.h = /*nLines**/textLineHeight /*+ G.margin/2*/; // kad vert jungtys būtų vientisos

            var x, y;
            if (freeX == -99999) {
                var p = g['p'+skipChildId];
                x = p.x + f.childrenWidth/2 - f.w/2 - G.margin;
                y = p.y - h - G.vertGapBetweenGenerations;
            } else {
                freeX -= 0
                freeY -= G.margin/2
                if ((f.father > 0) && (f.mother > 0)) {  //alert("fa- ((f.father > 0) && (f.mother > 0))");
                    x = freeX + g['p'+f.mother].w - f.w/2 + 0*2*G.margin;
                    y = freeY;
                } else if (f.father > 0) {  //alert("fa- ((f.father > 0)");
                    x = freeX + g['p'+f.father].w - f.w/2 + 0*2*G.margin;
                    y = freeY;
                } else if (f.mother > 0) {  //alert("fa- (f.mother > 0)");
                    x = freeX + g['p'+f.mother].w - f.w/2 + 0*2*G.margin;
                    y = freeY;
                } else {  //alert("fa- else{} ");
                    G.diwe_=G.diwe; G.diwe = 'E';
                    logE("no father and mother set in Family"); 
                    G.diwe=G.diwe_;
                }
        //    var p = g['p'+skipChildId];
        //    //var x = p.x + (p.w - w)/2 - G.margin;   logD("p.x p.w "+p.x+" "+p.w);
        //    //var y = p.y - h - 1*G.vertGapBetweenGenerations;
        //    var x = p.x + f.childrenWidth/2 - f.w/2 - G.margin;
        //    var y = p.y - h - G.vertGapBetweenGenerations;
            }
            
//            var fRect = this.rect(x-1*G.margin,y-1*G.margin,(w+2*G.margin),(2*h+G.margin/1),0);
//            //-- do-not-delete  fRect.attr({fill: "#f66", stroke: "#f00", opacity: 0.5});
//            f.r[f.r.length] = fRect;  // logD("f.r[f.r.length] = "+f.r[f.r.length-1]);


            f.x = x /*- G.margin*/;
            f.y = y /*- G.margin*/;
        //var rect1 = this.rect(p.x-1*G.margin, p.y-1*G.margin, p.w, p.h, 5).attr(CSS.gender)
        ////////////////////////////////////////////////
            //var nLines = 2
            var textWidth = f.w - 2*G.margin;
            var textLineHeight = f.h /*(f.h - G.margin/2)/nLines*/;

            var fRect1 = this.rect(f.x - 0*2*G.margin, f.y - 0*G.margin,  f.w + 2*G.margin, f.h*f.rLines, 1*11);
            //var fRect1 = this.rect(f.x+1*G.margin, f.y-0*G.margin, /*(w+2*G.margin)*/f.w, /*(2*h+G.margin/1)*/f.h+G.margin/2, 2);
            //alert("==>*<==");

            f.r[f.r.length] = fRect1.attr(CSS.familyBox);    //logD("f.r[f.r.length] = "+f.r[f.r.length-1]);
            //fRect.attr({fill: "90-#fff-#000", stroke: "#f00", opacity: 1});
            //-- do-not-delete  fRect.attr({fill: "#f66", stroke: "#f00", opacity: 0.5});
            var text1 = this.text(f.x + 1*G.margin, f.y+1.5*G.margin+0*textLineHeight, ((f.md==''&& f.mp=='') ? ' ' : 'oo') +' '+f.md + ((f.md=='') ? '' : ''))
//           var text1 = this.text(f.x+2*G.margin, f.y+1*G.margin+0*textLineHeight, ((f.md==''&& f.mp=='') ? ' ' : 'oo') +' '+f.md + ((f.md=='') ? '' : ''))
            f.r[f.r.length] = text1.attr(CSS.font).attr(CSS.text);
            var text2 = this.text(f.x + 1*G.margin, f.y+1.5*G.margin+1*textLineHeight, '  '+f.mp+((f.mp=='') ? '' : ''))
            f.r[f.r.length] = text2.attr(CSS.font).attr(CSS.text); //alert('aaa aaa aaa aaa')
            var text3 = this.text(f.x + 1*G.margin, f.y+1.5*G.margin+2*textLineHeight, ((f.dd==''&& f.dp=='') ? ' ' : 'o-o') +''+f.dd + ((f.dd=='') ? '' : ''))
            f.r[f.r.length] = text3.attr(CSS.font).attr(CSS.text);
            var text4 = this.text(f.x + 1*G.margin, f.y+1.5*G.margin+3*textLineHeight, '  '+f.dp+((f.dp=='') ? '' : ''))
            f.r[f.r.length] = text4.attr(CSS.font).attr(CSS.text);
        ////////////////////////////////////////////////           
               
                
            
            // define the family total spouses width
            var totalw = ((f.mother > 0) ? g['p'+f.mother].w : 0) + 
                ((f.father > 0) ? g['p'+f.father].w : 0);
            // reserve space for maybe both spouses                
            updateGlobVars(f, f.x-1*G.margin, f.y-1*G.margin, /*f.w*/(((f.father > 0) && (f.mother > 0)) ? totalw : f.w),
                f.h, 'drawFamily if (skipChildId > 0)');

            G._f = f;
            if ((f.father > 0) && (f.mother > 0)) { 
            //if (('father' in f) && ('mother' in f)) {
                // put mother & father over fam box
                this.drawPerson(f.father, 'leftParent');
                G._f = f; // !!! DO NOT DELETE THE LINE
                this.drawPerson(f.mother, 'rightParent');
            } else if (f.father > 0) {
                this.drawPerson(0+f.father, 'centerParent');
            } else if (f.mother > 0) {
                this.drawPerson(0+f.mother, 'centerParent');
            } else {
                G.diwe_=G.diwe; G.diwe = 'E';
                logE("no father and mother set in Family"); 
                G.diwe=G.diwe_;
            }
            delete G._f;
        } else if (skipSpouseId > 0) { // draw both parents
            //alert("==>*<== draw both parents ");
            var spouseId = ((f.mother > 0) && (skipSpouseId == f.mother)) ?
                ((f.father > 0) ? f.father : '0') :
                ((f.father > 0) && (skipSpouseId == f.father)) ?
                    ((f.mother > 0) ? f.mother : '0') : '0';  
            //logD("spouseId="+spouseId);
            if (spouseId == 0) {
                G._p = g['p'+skipSpouseId]; // po tuo Person paišome Family box'ą   
                // logPerson(skipSpouseId); 
                    
                var p = G._p;
                var x = p.x + p.w/2- w/2 - G.margin;   //logD("p.x p.w "+p.x+" "+p.w);
                var y = p.y + 2*p.h + 0*G.vertGapBetweenGenerations;
                
                f.x = x /*- G.margin*/;
                f.y = y /*- G.margin*/;
            ////////////////////////////////////////////////
        //var rect1 = this.rect(p.x-1*G.margin, p.y-1*G.margin, p.w, p.h, 5).attr(CSS.gender)
                //var nLines = 2
                var textWidth = f.w - 2*G.margin;
                var textLineHeight = f.h /*(f.h - G.margin/2)/nLines*/;

                //var fRect1 = this.rect(f.x-1*G.margin, f.y-0*G.margin, (w+2*G.margin), (2*h+G.margin/1),0);
                var fRect1 = this.rect(f.x - 0*2*G.margin, f.y - 0*G.margin,  f.w + 2*G.margin, f.h*f.rLines, 0*22+5);
                //var fRect1 = this.rect(f.x-1*G.margin, f.y-0*G.margin, /*(w+2*G.margin)*/f.w, /*(2*h+G.margin/1)*/f.h+G.margin/2, 0);
                f.r[f.r.length*0] = fRect1; /*alert(000);*/  // logD("f.r[f.r.length] = "+f.r[f.r.length-1]);
                //fRect.attr({fill: "90-#fff-#000", stroke: "#f00", opacity: 1});
                //-- do-not-delete  fRect.attr({fill: "#f66", stroke: "#f00", opacity: 0.5});

                var text1 = this.text(f.x, f.y+1*G.margin+0*textLineHeight, ((f.md==''&& f.mp=='') ? ' ' : 'oo')+' '+f.md+((f.md=='') ? '' : ''))
                f.r[f.r.length] = text1.attr(CSS.font).attr(CSS.text);
                var text2 = this.text(f.x, f.y+1*G.margin+1*textLineHeight, '  '+f.mp+((f.mp=='') ? '' : ''))
                f.r[f.r.length] = text2.attr(CSS.font).attr(CSS.text); //alert('aaa aaa aaa aaa')
                var text3 = this.text(f.x, f.y+1*G.margin+2*textLineHeight, ((f.dd==''&& f.dp=='') ? ' ' : 'o-o')+''+f.dd+((f.mp=='') ? '' : ''))
                f.r[f.r.length] = text3.attr(CSS.font).attr(CSS.text);
                var text4 = this.text(f.x, f.y+1*G.margin+3*textLineHeight, '  '+f.dp+((f.dp=='') ? '' : ''))
                f.r[f.r.length] = text4.attr(CSS.font).attr(CSS.text);
            ////////////////////////////////////////////////

 
                
                updateGlobVars(f, f.x-1*G.margin, f.y-1*G.margin, f.w, f.h, 'drawFamily (skipSpouseId > 0) (spouseId == 0)');
                delete G._p;
            } else if ((spouseId > 0) && !(g['p'+spouseId].r.length > 0)/*('x' in g['p'+spouseId])*/) { //alert("==>*<==");
                G._p = g['p'+skipSpouseId]; // po tuo Person paišome Family box'ą   
                // logPerson(skipSpouseId); 
                    
                var p = G._p;
                //--var x = p.x + p.w - w/2 - G.margin;   //logD("p.x p.w "+p.x+" "+p.w);
                var x = p.x + p.w - w/2 + 1*G.margin; /*B524-2*/  //logD("p.x p.w "+p.x+" "+p.w);
                //alert('in drawFamily: f.rLines='+f.rLines);
                 var y = p.y + p.h*/*f.rLines*//*6*/getMaxHusbWifeLines(p, f) + 0*G.vertGapBetweenGenerations;
                //var y = p.y + p.h*/*p.rLines*/getMaxHusbWifeLines(p, f) + 0*G.vertGapBetweenGenerations;

                f.x = x /*- 1*G.margin*/;
                f.y = y /*- 1*G.margin*/;
            ////////////////////////////////////////////////
                //var nLines = 2
                var textWidth = f.w - 2*G.margin;
                var textLineHeight = f.h /*(f.h - G.margin/2)/nLines*/;

                //var fRect2 = this.rect(f.x-1*G.margin,f.y-0*G.margin,(w+2*G.margin),(2*h+G.margin/1),0);
 // old         var fRect1 = this.rect(f.x-1*G.margin, f.y-0*G.margin, /*(w+2*G.margin)*/f.w, /*(2*h+G.margin/1)*/f.h+G.margin/2, 2);
                var fRect1 = this.rect(f.x - 1*G.margin, f.y - 0*G.margin,  f.w + 2*G.margin, f.h*f.rLines, 0*33+5);
                fRect1.attr(CSS.familyBox); /*alert(000);*/  //fRect1.attr({fill: "90-#fff-#000", stroke: "#f00", opacity: 1});
                f.r[f.r.length] = fRect1;  // logD("f.r[f.r.length] = "+f.r[f.r.length-1]);
                var text1 = this.text(f.x, f.y+1*G.margin+0*textLineHeight, ((f.md==''&& f.mp=='') ? ' ' : 'oo')+' '+f.md+((f.md=='') ? '' : ''))
//old           var text1 = this.text(f.x, f.y+1*G.margin/1+0*textLineHeight, ((f.md==''&& f.mp=='') ? ' ' : 'oo')+' '+f.md+((f.md=='') ? '' : ''))
                f.r[f.r.length] = text1.attr(CSS.font).attr(CSS.text);
                var text2 = this.text(f.x, f.y+1*G.margin/1+1*textLineHeight, '  '+f.mp+((f.mp=='') ? '' : ''))
                f.r[f.r.length] = text2.attr(CSS.font).attr(CSS.text); //alert('aaa aaa aaa aaa')
                var text3 = this.text(f.x, f.y+1*G.margin/1+2*textLineHeight, ((f.dd==''&& f.dp=='') ? ' ' : 'o-o')+''+f.dd+((f.mp=='') ? '' : ''))
                f.r[f.r.length] = text3.attr(CSS.font).attr(CSS.text);
                var text4 = this.text(f.x, f.y+1*G.margin/1+3*textLineHeight, '  '+f.dp+((f.dp=='') ? '' : ''))
                f.r[f.r.length] = text4.attr(CSS.font).attr(CSS.text);
            ////////////////////////////////////////////////
                updateGlobVars(f, f.x-1*G.margin, f.y-1*G.margin, f.w, f.h, 'drawFamily (skipSpouseId > 0) (spouseId > 0)');
                //-- B120-4/vsh temporary: //updateGlobVars(x-1*G.margin, y-1*G.margin, (w+2*G.margin), (2*h+G.margin/2));
                G._f = f; /* 5B28-6/vsh */

                this.drawPerson(spouseId, 'spouse');
                    // spouse exists and spouse is processed by drawPerson now
                //this.drawPerson(spouseId, 'personOnly');

                delete G._f; /* 5B28-6/vsh */
                delete G._p;
            }
        } else {
            G.diwe_=G.diwe; G.diwe = 'E';
            logE('drawFamily: skipSpouseId==0 familyId skipSpouseId skipChildId ' + familyId+' '+skipSpouseId+' '+skipChildId);
            G.diwe=G.diwe_;
        }
    }
    
//    if ('children' in f) {
//        var childIdsString = ""+f.children;  // !!! ==> ""+...  is IMPORTANT
//        //        logD("childIdsString=|"+childIdsString+"|");
//        var childIds = [];
//        if (childIdsString.indexOf(',') < 0) {
//            childIds[0] = childIdsString; // logD("childIds[0]="+childIds[0]);
//        } else {
//            childIds = childIdsString.split(','); // logD("childIds="+childIds);
//        } // logD("childIds.length="+childIds.length);
//        for (var j = 0; j < childIds.length; j++){
//            // logD(" = " + familyIds[j]);
//           if (!('p'+childIds[j] in g)) continue; //  B130-7: it may happen because of forest show narrowing
//           if ((childIds[j] != skipChildId) && !(g['p'+childIds[j]].r.length > 0)/*('x' in g['p'+childIds[j]])*/) {
//                //logD("childIds[j]="+childIds[j]);  // logFamily(familyId);
//                G._f = f;
//                this.drawPerson(childIds[j], 'child');
//                delete G._f;
//           }
//        }
//    }
    //logD('--] dF familyId skipSpouseId skipChildId w h x y ' + familyId+' '+skipSpouseId+' '+skipChildId+' '+f.w+' '+f.h+' '+f.x+' '+f.y);
    
    if (G.loggedIn && (f.father == G.rootId || f.mother == G.rootId)) {
       
            if (f.father == 0 || f.mother == 0) {

            var xgli = f.x-2*G.margin;
            var ygli = f.y+f.h/4 //G.margin;
                    
            var arrowLeft = paper.drawImage( G.app+'images/goLeft.png', xgli, ygli, 'w', f.h);
            f.r[f.r.length] = arrowLeft;
            f.arrowLeft = arrowLeft; // !!!
        
//            var rect2 = this.rect(xgli, ygli, f.h, f.h, 1);
//            rect2.attr({fill: "ff3", "fill-opacity":"0", stroke: "#fff", "stroke-width":"1"});
//            f.r[f.r.length] = rect2;
            
            arrowLeft.node.onclick = function() { ///*logD*/alert("arrowLeft.node.onclick");
                 arrowLeft.hide();
                 arrowDown.hide();  //if ('arrowDown' in f) f.arrowDown.hide();
                 paper.setActions4FamilySpouse(f.id);
            };
            arrowLeft.node.onmouseover = function() { ///*logD*/alert("arrowLeft.node.onmouseover");
                //arrowLeft.show()
                //rect2.attr({fill: "#ff0", opacity: 1});
                arrowLeft.attr("title", "click mouse to show available actions for the family");
                //rect2.attr({fill: "#111", "fill-opacity":0, stroke: "#0ff", "stroke-width": 2});
            };
            arrowLeft.node.onmouseout = function() { ///*logD*/alert("arrowLeft.node.onmouseout");
                //arrowLeft.hide();
                //rect2.attr({fill: "#fff", opacity: 0.5});
                 //rect2.attr('display', 'none');
                //rect2.attr({fill: "#000", "fill-opacity": 0, stroke: "#fff", "stroke-width": 2});
            };
        }
        
        var xglj = f.x + (f.w-f.h)/2 - G.margin;
        var yglj = f.y+f.h + 0.6*G.margin;
                    
        var arrowDown = paper.drawImage( G.app+'images/goDown.png', xglj, yglj, 'w', f.h);
        f.r[f.r.length] = arrowDown;
        f.arrowDown = arrowDown; // !!!

//        var recta = this.rect(xglj, yglj, f.h, f.h, 1);
//        recta.attr({"fill":"white", "fill-opacity":"0", "stroke":"#fff", "stroke-width":"0"});
//        //recta.attr({"fill":"ff3",   "fill-opacity":"1", "stroke":"#fff", "stroke-width":"0"});
//        f.r[f.r.length] = recta;
        
        arrowDown.node.onclick = function() { ///*logD*/alert("arrowUp.node.onclick");
            if (f.father == 0 || f.mother == 0) arrowLeft.hide();
            arrowDown.hide();
            paper.setActions4FamilyChildren(f.id);
        };
        arrowDown.node.onmouseover = function() { ///*logD*/alert("arrowUp.node.onmouseover");
            //arrowUp.show()
            //recta.attr({fill: "#ff0", opacity: 1});
            arrowDown.attr("title", "click mouse to show available actions for the family children");
            //recta.attr({fill: "#111", "fill-opacity":"0", stroke: "#0ff", "stroke-width": 2});
        };
        arrowDown.node.onmouseout = function() { ///*logD*/alert("arrowUp.node.onmouseout");
            //arrowUp.hide();
            //recta.attr({fill: "#fff", opacity: 0.5});
             //recta.attr('display', 'none');
            //recta.attr({fill: "#000", "fill-opacity": 0, stroke: "#fff", "stroke-width": 2});
        };
        
    }
    G.diwe=G.diwe_;
}


// AC29-3/vsh init
Raphael.fn.drawTestShapes = function() { //-------------------------------------
    //var rec = paper.rect(10, 10, 10, 10);
    //var cir = paper.circle(10, 10, 10);
    //rec.insertAfter(cir); // neveikia!

    // DO NOT delete
     var imageURI =
    //"vsh.jpg";
    //"http://vshmixweb.vytastax.staxapps.net/images/2-James_Gosling.jpg";
    "http://vshmixweb.vytastax.staxapps.net/images/2-James_Strachan.png";
    this.drawImage(imageURI, 5, 5, 'h', 90);

    var c = this.circle(G.globWidth-40, 40, 20);
    c.node.onclick = function() { //logD("c.node.onclick");
        c.attr("fill", "#f00");
         LoadNewPage ();
    }
    // ! neveikia c.node.click = function (event) { logD("c.click"); attr({fill: "blue"}); };
    // ! neveikia c.mouseover = function () { logD("c.mouseover"); c.attr({fill: "green"}); };
    c.node.onmouseover = function() { //logD("c.node.onmouseover");
        c.attr({fill: "green"});
        c.attr("title", "Bandymas, aaaaa bbbbbb sssss ccccc fffff")
    };
    // ! neveikia c.mouseout = function () { logD("c.mouseout"); c.attr({fill: "#eee"}); };
    c.node.onmouseout = function() { //logD("c.node.onmouseout");
        c.attr({fill: "#fff"});
    };

    var rt = this.rect(G.globWidth-80, 20, 20, 20);
    //rt.attr({fill: "315-#fff-#000", opacity: 1});
    rt.attr({fill: "315-#ffffff-#000000", stroke: "#f000000", opacity: 1});
    var rb = this.rect(G.globWidth-80, 40, 20, 20);
    //rb.attr({fill: "45-#fff-#000", opacity: 1});
    rb.attr({fill: "45-#ffffff-#000000", stroke: "#f000000", opacity: 1});

}

//logger = function(message) {
//    //logD(document.getElementById("logger").firstChild.nodeValue);
//    document.getElementById("logger").firstChild.nodeValue = message;
//};

function LoadNewPage () {
   location.assign ( G.app+"rest/person/6");
};


// Development time tool. Defines show Person info via alert
// B106-4/vsh init
function logPerson(personId) {
    var xKey = "p"+personId;
    var p = g[xKey];
    var res = "Person: ";
    s = '';
    for (var i in p) { res = res + ' ' + i +'=' + p[i]; s ='; ' }
    var t = G.diwe; G.diwe="D";
    //logD(res);
    G.diwe=t;
}

// Development time tool.  Defines show Family  info via alert
// B106-4/vsh init
function logFamily(familyId) {
    var xKey = "f"+familyId;
    var f = g[xKey];
    var res = "Family: ";
    s = '';
    for (var i in f) { res = res + ' ' + i +'=' + f[i]; s ='; '  }
    var t = G.diwe; G.diwe="D";
    //logD(res);
    G.diwe=t;
}


function validateInteger( strValue ) {
/************************************************
DESCRIPTION: Validates that a string contains only
    valid integer number. http://rgagnon.com/jsdetails/js-0063.html
PARAMETERS:
   strValue - String to be tested for validity
RETURNS:
   True if valid, otherwise false.
**************************************************/
  var objRegExp  = /(^-?\d\d*$)/;
  //check for integer characters
  return objRegExp.test(strValue);
}

function  validateNumeric( strValue ) {
/*****************************************************************
DESCRIPTION: Validates that a string contains only valid numbers.
    http://rgagnon.com/jsdetails/js-0063.html
PARAMETERS:
   strValue - String to be tested for validity
RETURNS:
   True if valid, otherwise false.
******************************************************************/
  var objRegExp  =  /(^-?\d\d*\.\d*$)|(^-?\d\d*$)|(^-?\.\d\d*$)/;
  //check for numeric characters
  return objRegExp.test(strValue);
}


// B528-6/vsh init
function locateXpendBoxWise(p, /*appPrepDirection,*/ xi, yi) {
/************************************************
DESCRIPTION: finds free enough space for Person box
PARAMETERS:
    p - person object
    //----appPrepDirection - {cLR,cRL} - look for free space in the {right,left}----//
    xi, yi - Family Box Bottom Middle
RETURNS:
   (x, y)
**************************************************/
    //G.diwe_=G.diwe; G.diwe = 'D';
    var gg_wMin = G['g'+p.generation+'_wMin'];
    var gg_wMax = G['g'+p.generation+'_wMax'];
    var gg_hMin = G['g'+p.generation+'_hMin'];
    var gg_hMax = G['g'+p.generation+'_hMax'];
    //logD("locateXpendBox: gg_wMin gg_wMax gg_hMin gg_hMax  " + gg_wMin+" "+gg_wMax+" "+gg_hMin+" "+gg_hMax);
    var xx, yy;
    if (/*appPrepDirection == 'cLR'*/ Math.abs(gg_wMin - xi) > Math.abs(gg_wMax - xi)) {
        if (gg_wMin == 99999) {    
            xx = xi;
            yy = yi;
        } else {
            xx = gg_wMax + G.horizGapBetweenSiblings + 2*G.margin;
            yy = gg_hMin + G.margin;
        }
    } else { // 'cRL'  // B524-2/vsh !!! netestuota
        if (gg_wMin == 99999) {    
            xx = xi;
            yy = yi;
        } else {
            xx = gg_wMin - G.horizGapBetweenSiblings - p.w;
            yy = gg_hMin + G.margin;
        }
    }
    //logD("locateXpendBox: xx yy " +xx+" "+yy);
    //G.diwe=G.diwe_;
    return {x:xx, y:yy};
}



// B120-4/vsh init
function overlayLinearCheck(p, xi, yi) {
/************************************************
DESCRIPTION: checks if 'p' Person can be put in (xi, yi)
PARAMETERS:
    p - Person object
    xi, yi - the box possible top-left corner
RETURNS:
   (true if 'p' Person overlays some other Person in the same generation, otherwise - false)
**************************************************/
    //G.diwe_=G.diwe; G.diwe = 'D';    //G.diwe=G.diwe_;
//    var xx = xi;
//    var yy = yi;
    //logD((G['g'+p.generation]+'').split(','));
    var pgenArr = (G['g'+p.generation]+'').split(',');
    for (var id in pgenArr) {
        //logD('id='+id);
        var pInGener = g['p'+pgenArr[id]];
        if (p.id == pInGener.id) continue;
        //logD('pInGener='+pInGener);
        if (pInGener.r.length > 0)/*('x' in pInGener)*/ { // p Person is drawn now
            if(!overlayCheck(p, pInGener, xi, yi)) {
                //G.diwe=G.diwe_;
                return false;
            }
        }
    }
    //G.diwe=G.diwe_;
    return true;
}


// B120-4/vsh init
function overlayCheck(p, pp, xi, yi) {
/************************************************
DESCRIPTION: checks if p and pp are overlayed
    p - curent person object
    pp - another person object
    xi, yi - testing point
RETURNS:
   (true if p and pp boxes do not overlay, otherwise - false) 
**************************************************/
    G.diwe_=G.diwe; G.diwe = 'D';
    var x = xi;
    var y = yi;
    var corners = [[0+x, 0+y],[x+p.w, 0+y],[x+p.w, y+p.h],[0+x, y+p.h]];
    var test = true; 
    for (var i in corners) {
        //logD('i='+i + ' pp.x='+pp.x + ' pp.w='+pp.w + ' pp.y='+ pp.h + ' pp.y='+pp.h);
        test = ((corners[i][0] < pp.x) || (corners[i][0] > (pp.x+pp.w))) 
            && ((corners[i][1] < pp.y) || (corners[i][1] > (pp.y+pp.h)));
        if (!test) break;
    }   
    G.diwe=G.diwe_;
    return (test);
}


// B528-6/vsh commented out  // B120-4/vsh init
//function locateBox(p, searchDirection, xi, yi) {
////************************************************
////DESCRIPTION: finds free enough space for Person box
////PARAMETERS:
////    p - person object
////    searchDirection - {cLR,cRL} - look for free space in the {right,left}
////    xi, yi - the box pssible top-left corner
////    //w, h - the box width, height
////RETURNS:
////   (x, y)
////**************************************************
//    G.diwe_=G.diwe; G.diwe = 'D';    //G.diwe=G.diwe_;
//    var xx = xi;
//    var yy = yi;
//    //logD((G['g'+p.generation]+'').split(','));
//    var pgenArr = (G['g'+p.generation]+'').split(',');
//    for (var id in pgenArr) {
//        //logD('id='+id);
//        var pInGener = g['p'+pgenArr[id]];
//        //logD('pInGener='+pInGener);
//        if (pInGener.r.length > 0)/*('x' in pInGener)*/ {
//            if(overlayCheck(p, pInGener, xx, yy)) { // no overlay
//                G.diwe=G.diwe_;
//                return {x:xx, y:yy};
//            } else {
//                if (searchDirection == 'cLR') {
//                    xx = pInGener.x + pInGener.w + G.horizGapBetweenSiblings;
//                    yy = 0 + yy;
//                } else { }
//            }
//        }
//    }
//    G.diwe=G.diwe_;
//    return {x:xx, y:yy};
//}


// draws box that contains ALL objects
// B126-3-3/vsh init
Raphael.fn.drawBaseBox = function() {
    G.diwe_=G.diwe; G.diwe = 'D';
    G.g_base_rect = this.rect(G.baseWmin, G.baseHmin, G.baseWmax-G.baseWmin, 
    G.baseHmax-G.baseHmin).attr({fill: "90-#fff-#000"});
    //logD("Click to remove development time generation area rectangle");
    G.g_base_rect.remove();
    G.diwe=G.diwe_;
};


// draws box for every generaton, a box contains all one generation objects
// B120-7/vsh init
Raphael.fn.drawBox4Gener = function() {
    G.diwe_=G.diwe; G.diwe = 'D';
    for (var i = G.gMin; i <= G.gMax; i++) {  
        //logD(i);
        var rt = this.rect(G['g'+i+'_wMin'], G['g'+i+'_hMin'], G['g'+i+'_wMax']-G['g'+i+'_wMin'], 
            G['g'+i+'_hMax']-G['g'+i+'_hMin']);
        rt.attr({fill: "90-#fff-#000"});
        G['g'+i+'_rect'] = rt;
    }
    //logD("Click to remove development time generation area rectangle");
//    for (var i = 0*99+G.gMin; i <= G.gMax; i++) {
//        pauseInMillis(10);
//        G['g'+i+'_rect'].remove();
//    }
    G.diwe=G.diwe_;
};


// draws box for every generaton, a box contains all one genetation onjects
// B126-3/vsh init
Raphael.fn.translteForest = function(deltaX, deltaY) {
    G.diwe_=G.diwe; G.diwe = 'D';
    
    for (var i in g) {
        //logD('i = ' + i);
        var x = g[i]
        for (var j in x.r) {
            x.r[j].translate(deltaX, deltaY)
        }
    }
    
    /*for (var i = G.gMin; i <= G.gMax; i++) {
        logD('generation = ' + i);
        var persons = (G['g'+i]+'').split(',');
        for (var id in persons) {
            //logD('person = ' + id);
            var person = g['p'+persons[id]];
            for (var rp in person.r) {
                person.r[rp].translate(deltaX, deltaY)
            }
            if ('familyId' in person) {
                var fam = g['f'+person.familyId]
                if (!('translated' in fam)) {
                    fam.translated = true
                    for (var rf in fam.r) {
                        fam.r[rf].translate(deltaX, deltaY)
                    }
                }
            }
        }
    }*/
    
    G.diwe=G.diwe_;
};


// initialize a generation objects position global variables -------------------
// AC29-3/vsh init
InitGlobVars = function() {
    for (var i = G.gMin; i <= G.gMax; i++) {
        G['g'+i+'_wMin'] =  99999;
        G['g'+i+'_wMax'] = -99999;
        G['g'+i+'_hMin'] =  99999;
        G['g'+i+'_hMax'] = -99999;
    }
};

// update global variables -----------------------------------------------------
// AC28-2/vsh init
updateGlobVars = function(pf, x, y, width, height, logMsg) {
    G.baseWmin = Math.min(G.baseWmin, x);
    G.baseWmax = Math.max(G.baseWmax, 0 + x + width );
    G.baseHmin = Math.min(G.baseHmin, y);
    G.baseHmax = Math.max(G.baseHmax, 0 + y + height);
    var gg = ('gender' in pf) ? 'g'+pf.generation : 'g'+pf.generation;
    G[gg+'_wMin'] = Math.min(G[gg+'_wMin'], x);
    G[gg+'_wMax'] = Math.max(G[gg+'_wMax'], 0 + x + width );
    G[gg+'_hMin'] = Math.min(G[gg+'_hMin'], y);
    G[gg+'_hMax'] = Math.max(G[gg+'_hMax'], 0 + y + height);
    //showGlobVars(logMsg);
}
// show global variables --------------
// AC29-3/vsh init
showGlobVars = function(logMsg) {
    res = '===> ' + logMsg + ' || ' +
    "baseWmin= "+ G.baseWmin + "; baseWmax= "+ G.baseWmax +
    "; baseHmin= "+ G.baseHmin + "; baseHmax= "+ G.baseHmax + ";"+ " || "; 
    for (var i = 0+G.gMin; i <= 0+G.gMax; i++) {
        res = res + "gener=" + i 
        + " wMin="+G['g'+i+'_wMin'] + " wMax="+G['g'+i+'_wMax'] 
        + " hMin="+G['g'+i+'_hMin'] + " hMax="+G['g'+i+'_hMax'] + " || ";  
     }
    G.diwe_ = G.diwe; G.diwe ='D'
    logD(res);
    G.diwe = G.diwe_;
}

// -----------------------------------------------------------------------------
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// #############################################################################

// ====================================
// Kiekvienoje generacijoje taip surūšiuojami asmenys, kad kairėje forest dalyje būtų
//      artimiausi 'root' asmeniui
// BB08-2/vsh init
Raphael.fn.rangeAll = function() {
    G.diwe_=G.diwe;  //G.diwe = 'D';
    for (var i = G.gMin; i <= G.gMax; i++) {
        //logD('Karta ' + (i));
        G['s'+(i)] = (G['g'+(i)]+'').split(',');
        //logD( 'g'+(i) + '  ' + G['s'+(i)].length + '  ' + G['s'+(i)].toString());
    }
    logD( 's0 rootId' + '  ' + G['s0'].length + '  ' + G['s0'].toString());
    swapIds(G['s'+(0)], G.rootId);
    var pRoot = g['p'+G.rootId];                                            //alert(p.toString());
    if (('familyId' in pRoot)) { // the person is child in family
        var siblings = (g['f'+pRoot.familyId]['children']+'').split(',');   //alert('siblings: ' + siblings.toString());
        for (var s in siblings) {
            swapIds(G['s'+(0)], siblings[s]);                               //alert( 's0 rootId' + '  ' + G['s0'].length + '  ' + G['s0'].toString());
        }
    }
    if (('fd' in pRoot)) { // the person was/is married
        var pFams = (pRoot['fd']+'').split(',');                                //alert('p["fd"] ='+p['fd']); alert('pFams ='+pFams);
        for (var j in pFams) {  // for every person marriage
            var fam = g['f'+pFams[j]];                                      //alert('fam ='+fam);
            var spouse = (pRoot.gender == 'M') ? fam.mother : fam.father;
            if (spouse != 0)  {
                swapIds(G['s'+(0)], spouse);
                var sp = g['p'+spouse];
                if (('familyId' in sp)) { // the spouse is child in family
                    var siblings = (g['f'+sp.familyId]['children']+'').split(',');
                    for (var s in siblings) {
                        swapIds(G['s'+(0)], siblings[s]);
                    }
                }

            }
        }
    }
    logD( 's0 rootId' + '  ' + G['s0'].length + '  ' + G['s0'].toString());

    for (var i =  -1; i >= G.gMin; i--) { //  ancestor generations
        var childrenArr = G['s'+(i+1)];
        for (var c in childrenArr) {                                                 //alert('childrenArr[c] ' + childrenArr[c]);
            if (childrenArr[c] < 0) { // ordered person (child)
                var pc = g['p'+Math.abs(childrenArr[c])];                            //alert(pc.toString());
                if (('familyId' in pc)) { // the person is child in family
                    var fam = g['f'+pc.familyId];                                    //alert('fam ='+fam);
                    //var husb = (pRoot.gender == 'M') ? fam.mother : fam.father;
                    //var spouse = (pRoot.gender == 'M') ? fam.mother : fam.father;
                    if (pRoot.gender == 'M') {
                        if (fam.father != 0) swapIds(G['s'+(i)], fam.father);        //alert( 'M s'+(i) + 'rootId' + '  ' + G['s'+(i)].length + '  ' + G['s'+(i)].toString());
                        if (fam.mother != 0) swapIds(G['s'+(i)], fam.mother);        //alert( 'M s'+(i) + 'rootId' + '  ' + G['s'+(i)].length + '  ' + G['s'+(i)].toString());
                    } else {
                        if (fam.mother != 0) swapIds(G['s'+(i)], fam.mother);        //alert( 'F s'+(i) + 'rootId' + '  ' + G['s'+(i)].length + '  ' + G['s'+(i)].toString());
                        if (fam.father != 0) swapIds(G['s'+(i)], fam.father);        //alert( 'F s'+(i) + 'rootId' + '  ' + G['s'+(i)].length + '  ' + G['s'+(i)].toString());
                    }
//                    var siblings = (g['f'+pc.familyId]['children']+'').split(',');   //alert('siblings: ' + siblings.toString());
//                    for (var s in siblings) {
//                        var p = g['p'+Math.abs(childrenArr[c])];                    //alert(p.toString());
//                        swapIds(G['s'+(i)], siblings[s]);                            //alert( 's0 rootId' + '  ' + G['s0'].length + '  ' + G['s0'].toString());
//                    }
                }
            }
        }
    }

    for (var i = 0; i < 0+1*G.gMax; i++) { //  descendant generations
        var ancestorArr = G['s'+(i)];
        for (var a in ancestorArr) {                                                 //alert('childrenArr[c] ' + childrenArr[c]);
            var pa = g['p'+Math.abs(ancestorArr[a])];
            if (('fd' in pa)) { // the person was/is married
                var paFams = (pa['fd']+'').split(',');                               //alert('pa["fd"] ='+pa['fd']); //alert('paFams ='+paFams);
                for (var j in paFams) {  // for every person marriage
                    var fam = g['f'+paFams[j]];                                      //alert('fam ='+fam);
                    if ('children' in fam) {
                        var siblings = (fam.children+'').split(',');
                        for (var s in siblings) {
                            swapIds(G['s'+(i+1)], siblings[s]);
                        }
                    }
                }
            }
        }
    }

    var logText = '';
    for (var i = G.gMin; i <= G.gMax; i++) {
        logText = logText + ' [' + i + ']=' + G['s'+(i)] + ';  ';
    }
    logI( logText);

    G.diwe=G.diwe_;
};

// ====================================
// Vienos generacijos asmen7 Id'ų masyve, rūšiuotinas id'as ( >0 ) perkeliamas kairiop po jau perkeltųjų ( <0 )
//      ir taip keičiamas id'as ( id-0 )
//      artimiausi 'root' asmeniui
// BB10-4/vsh init
swapIds = function(arr, id) {    //return;
    G.diwe_=G.diwe;  G.diwe = 'I';
    var logTemp = ( 'swapIds id=' + id + '  ' + arr.length + '  ' + arr.toString() + '  ');
    // skip duplicated call
    for (var i in arr) {
        if (arr[i] < 0 && Math.abs(arr[i]) == id) {
            logD( 'duplicated id ' + '  ' + id + '  in ' + arr.toString());
            G.diwe=G.diwe_;
            return;
        }
    }
    // find id index
    var idindex = -1;
    for (var i in arr) {
        if (arr[i] == id) {
            idindex = i;
            logD( 'id idindex' + '  ' + id + '  ' + idindex);
            break;
        }
    }
    if (idindex == -1) {
        logE( 'swapIds id =' + id + ' is missing in array ' +  arr.toString());
        G.diwe=G.diwe_;
        return;
    }

    // perform swap
    for (var i in arr) {
        if (i == 0 && arr[i] > 0) {
            var temp = arr[i];
            arr[i] = 0 - id;
            arr[idindex] = temp;
            break;
        } else {
            if (arr[i] > 0) {
                if (arr[i] == id) {
                    arr[i] = -id;
                    break;
                } else {
                    var temp = arr[i];
                    arr[i] = 0 - id;
                    arr[idindex] = temp;
                    break;
                }
            }
        }
    }
    logD( logTemp +  '  ' + arr.length + '  ' + arr.toString());
    G.diwe=G.diwe_;
};

// ====================================
// draws all generation families and persons
// BA25-2/vsh init
Raphael.fn.drawAll = function() {
    G.diwe_=G.diwe;  G.diwe = 'I';
    for (var i = G.gMin; i <= (0); i++) {
        logD('Karta ' + (i));
        var pgenArr = G['s'+(i)]; //(G['g'+(i)]+'').split(',');
        logD( 's'+(i) + '  ' + G['s'+(i)].length + ';  ' + G['s'+(i)].toString())
        for (var ii in G['s'+(i)]) {
            logD('pgenArr[ii] ' + G['s'+(i)][ii]);
            var pInGener = g['p'+Math.abs(G['s'+(i)][ii])];
            logD('Asmuo ' + pInGener.id);
            if (pInGener.id != G.rootId) {//continue;
                paper.drawPerson(pInGener.id, 'personOnly');
            }
        }
        //for (var j = 0; j < familyIds.length; j++){
    }
    for (var i = 1; i <= G.gMax; i++) {      //alert(9999);
        logD('Karta ' + i);
        var pgenArr = G['s'+i]; //(G['g'+i]+'').split(',');
        logD( 's'+(i) + '  ' + pgenArr.length)
        for (var ii in pgenArr) {
            var pInGener = g['p'+Math.abs(G['s'+(i)][ii])];
            logD('Asmuo ' + pInGener.id);
            paper.drawPerson(pInGener.id, 'personOnly');
        }
    }
/*
    for (var i = 0; i <= (0-G.gMin); i++) {
        logD('Karta ' + (-i));
        var pgenArr = (G['g'+(-i)]+'').split(',');
        logD( 'g'+(-i) + '  ' + pgenArr.length)
        for (var id in pgenArr) {
            var pInGener = g['p'+pgenArr[id]];
            logD('Asmuo ' + pInGener.id);
        }
        //for (var j = 0; j < familyIds.length; j++){
    }
    for (var i = 1; i <= G.gMax; i++) {
        logD('Karta ' + i);
        var pgenArr = (G['g'+i]+'').split(',');
        logD( 'g'+(i) + '  ' + pgenArr.length)
        for (var id in pgenArr) {
            var pInGener = g['p'+pgenArr[id]];
            logD('Asmuo ' + pInGener.id);
        }
    }
*/
    G.diwe=G.diwe_;
};


// BB03-4/vsh vėl naudoju
// B528-6/vsh replaced by locateXpendBoxWise
// B120-4/vsh init
function locateXpendBox(p, appPrepDirection, xi, yi) {
/************************************************
DESCRIPTION: finds free enough space for Person box
PARAMETERS:
    p - person object
    appPrepDirection - {cLR,cRL} - look for free space in the {right,left}
    xi, yi - the box possible top-left corner
RETURNS:
   (x, y)
**************************************************/
/*
//G.vertGapBetweenGenerations = 120;
//G.horizGapBetweenSiblings = 20;
//G.margin = 10; // Family and Person box internal margin
*/
    //G.diwe_=G.diwe; G.diwe = 'D';
    var gg_wMin = G['g'+p.generation+'_wMin'];
    var gg_wMax = G['g'+p.generation+'_wMax'];
    var gg_hMin = G['g'+p.generation+'_hMin'];
    var gg_hMax = G['g'+p.generation+'_hMax'];
    //logD("locateXpendBox: gg_wMin gg_wMax gg_hMin gg_hMax  " + gg_wMin+" "+gg_wMax+" "+gg_hMin+" "+gg_hMax);
    var xx, yy;
    if (appPrepDirection == 'cLR') {
        if (gg_wMin == 99999) {
            xx = xi;
            yy = yi + (p.generation - G.gMin) * G.vertGapBetweenGenerations;
        } else {
            xx = gg_wMax + G.horizGapBetweenSiblings + 2*G.margin;
            yy = gg_hMin + G.margin;
        }
    } else { // 'cRL'  // B524-2/vsh !!! netestuota
        if (gg_wMin == 99999) {
            xx = xi;
            yy = yi;
        } else {
            xx = gg_wMin - G.horizGapBetweenSiblings - p.w;
            yy = gg_hMin + G.margin;
        }
    }
    //logD("locateXpendBox: xx yy " +xx+" "+yy);
    //G.diwe=G.diwe_;
    return {x:xx, y:yy};
}


// Bind persons
// BB07-1/vsh init
Raphael.fn.bindPersons = function() { //==================================================
    G.diwe_=G.diwe; //G.diwe = 'D';    //G.diwe=G.diwe_;
    for (var i in g) {
        if (i.indexOf('p') >= 0) {
            var p = g[i/*pKey*/];
            if ('familyId' in p) { // the Person has parents
                var fKey = 'f'+p.familyId;   //logD(f);
                //if (fKey == 'f1') alert('fKey=' + fKey);
                if (fKey in g) {  // B130-7: opposite may happen because of forest show narrowing
                    //if (fKey == 'f1') alert('fKey in g fKey=' + fKey);
                    var f = g[fKey];   //logD(f);
        // TODO     // nebaigta !!! var spouseId = (('mother' in f) && (personId == f.mother)) ? (('father' in fd) ? f.father : '0') :
                    //    (('father' in fd) && (personId == f.father)) ? (('mother' in fd) ? f.mother : '0') : '0';
                    /* BB07-1/vsh
                    this.drawFamily(p.familyId, 0, personId); // (familyId, skipsSpouseId, skipChilId)
                    */
                    //G.diwe_temp = G.diwe; G.diwe ='D'
                    var fmw = f.x + f.w/2;                                  //logD("f.x f.w "+f.x+" "+f.w);
                    var fmh = f.y + f.h*f.rLines + 0*G.margin/2;            //logD("f.y f.h "+f.y+" "+f.h);

                    //--var plw = p.x + p.w/2 - G.margin;
                    var plw = p.x + p.w/2 + 0*G.margin;

                    var plh = p.y - G.margin;                               //logD('M'+fmw+' '+fmh+'L'+plw+' '+plh);
                    //if (fKey == 'f1') alert('fKey in g fKey=' + fKey + '------- ' + 'M'+fmw+' '+fmh+'L'+plw+' '+plh);
                    var path1 = this.path('M'+fmw+' '+fmh+'L'+plw+' '+plh);  // alert("drawPerson relPosition = " + relPosition);
                    //if (fKey == 'f1') alert('fKey in g fKey=' + fKey + '=======');
                    p.r[p.r.length] = path1;
                    //logD("Raphael.fn.drawPerson: p.r[p.r.length-1] = " +  p.r[p.r.length-1]);
                    //G.diwe = G.diwe_temp;
                }
            }
        }
    }
    G.diwe=G.diwe_;
}


// =============================================================================
 //alert("G.initX = "+G.initX);
 InitGlobVars();

 paper = Raphael(document.getElementById("canvas"), G.globWidth, G.globHeight);
 var scale = 1.5;
 paper.setSize(scale*G.globWidth, scale*G.globHeight); 
 
 paper.rect(0, 0, scale*G.globWidth, scale*G.globHeight).attr({fill: "315-#ffffff-dddddd", stroke: "#fff", opacity: 1});
 
 G.topRect = paper.rect(0, 0, scale*G.globWidth, scale*G.globHeight);
 
 //--paper.drawTestShapes(); // DO NOT DELETE
 
/*
 //========================================
  paper.drawAll();
  sketchPerson();
  sketchFamily();

  //alert("G.initX = "+G.initX);
  G._f = g["f"+getPersonFamilyIdsArr(g["p"+G.rootId])[0]];
  paper.drawPerson(G.rootId, 'init');  //alert("G.initX = "+G.initX);
  //alert("==> after:  paper.drawPerson(G.rootId, 'init')");
  //paper.drawBaseBox();
  //paper.drawBox4Gener();
  //showGlobVars("Before forest translation []... ") 

  G.initX = 5*G.horizGapBetweenSiblings - G.baseWmin;
  G.initY = 5*G.horizGapBetweenSiblings - G.baseHmin;

  //alert("==> before second:  paper.drawPerson(G.rootId, 'init')");
  //paper.drawPerson(G.rootId, 'init');
  paper.translteForest(G.initX, G.initY);
//========================================
*/

 //========================================
  paper.rangeAll();
  //alert("G.initX = "+G.initX + ";   G.initY = "+G.initY);
  sketchPerson();
  sketchFamily();
  paper.drawPerson(G.rootId, 'personOnly');
  paper.drawAll();
  paper.bindPersons();

//========================================


 
 window.onload = function (paper) { /*logE("window.onload");*/ }


