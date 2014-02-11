// a family tree drawing
// E121-2/vsh  pe : fa <-=-> 1:n is implemented, code has been cleaned
// B104-2/vsh started from gedcomLoader.js
/* naming conventions for assoc arrays:
G - global parameters,
g - gedcom Entities: g['x'+id] - x ={p-Person, f-family, pe- , pa- , fe- , ed- }}
an Entity contains box params: x - horiz coord, y - vert coord, w - width, h - height
*/
// Global parameters
// G.loggedIn = the value is set in server Lift side (Forest.scala)
G.globWidth =  0*640 + 0*720 + 1*800 + 0 + 0*1280;
G.globHeight = 0*480 + 0*576 + 1*600 + 0 + 0*720;
G.locale = 'lt'; // actual value must be generated in Lift
G.direction = 'BT'; // tree dreawing direction: BT-BottomTop TB-TopBottom LR-LeftRight RL-RightLeft
G.vertGapBetweenGenerations = 200;
G.horizGapBetweenSiblings = 25;
G.margin = 10; // Family and Person box internal margin
G.xDefault = G.yDefault =  G.xyDefault = 100; // default position for every Person box
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
G.topRectCssAttrs = ({fill: "#eeeeee", stroke: "#fff", opacity: 1});

G.shownFam=[];
if (!G.shownFam.indexOf) {
    alert ("Your browser does not support indexOf method!");
}

// Development time tools:  Loging configuration
G.DIWE = '-DIWE'; //'-DIWE';
G.diwe = 'E';
function logD(msg) { if ('-D   '.indexOf(G.diwe.toUpperCase()) > 0) alert('DEBUG ' + msg); }
function logI(msg) { if ('_DI  '.indexOf(G.diwe.toUpperCase()) > 0) alert('INFO ' + msg); }
function logW(msg) { if ('_DIW '.indexOf(G.diwe.toUpperCase()) > 0) alert('WARN ' + msg); }
function logE(msg) { if ('_DIWE'.indexOf(G.diwe.toUpperCase()) > 0) alert('ERROR ' + msg); }

CSS={};
CSS.male = {'fill':'270-#cff-#fff', 'stroke':'#000'};       // 'stroke-width':1
CSS.female = {'fill':'270-#fcf-#fff', 'stroke':'#000'};
CSS.maleOver = {'fill':'90-#6ff-#fff', 'stroke':'#000'};
CSS.femaleOver = {'fill':'90-#f6f-#fff', 'stroke':'#000'};
CSS.maleOut = {'fill':'270-#cff-#fff', 'stroke':'#000'};
CSS.femaleOut = {'fill':'270-#fcf-#fff', 'stroke':'#000'};

As = {};
Aa = {};

// DC28-6/vsh The object allows to reach all objects in the canvas
// JS Person object may be bound to 0..n JS Family objects
// JS Family object is bound to ONLY one family box, so g['f'+f.id].r array contains all the family related objects
// A key structure: pId[.fId[.fId[...]]], where pId is p.id value, fId is optional f.id value
// R[key] value
R={};


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
updateGlobVars = function() {
    G.diwe_=G.diwe;  G.diwe = 'I';
    logD('updateGlobVars key='+arguments[0]+';')
    if (arguments.length == 2) {   //  (key, logMsg) {
        var key = ''+arguments[0]
        var logMsg = arguments[1]
        var person = g['p'+key.split('.')[0]]
        var idsInKey = key.split('.')
        var x = R[key][0].getBBox(false).x
        var y = R[key][0].getBBox(false).y
        var width = R[key][0].getBBox(true).width
        var height = R[key][0].getBBox(true).height
        logD('updateGlobVars key='+key+';  generation='+person.generation+';  x='+x+';  '+'y='+y+';  '+'width='+width+';  '+'height='+height+';')
        //for (var j = 1; j < key.split('.').length; j++){ // the person family ids
        for (var j = 1; j < idsInKey.length; j++){ // the person family ids
            //var spouse = getPersonSpouseInFamily(person, g['f'+j])
            var spouse = getPersonSpouseInFamily(person, g['f'+idsInKey[j]])
            //width = +((Object.keys(spouse).length > 0) ?  spouse.r[0].getBBox(true).width : 0)
            width += ((Object.keys(spouse).length > 0) ?  spouse.r[0].getBBox(true).width : 0)
        }
        G.baseWmin = Math.min(G.baseWmin, x);
        G.baseWmax = Math.max(G.baseWmax, 0 + x + width );
        G.baseHmin = Math.min(G.baseHmin, y);
        G.baseHmax = Math.max(G.baseHmax, 0 + y + height);
        var gg = 'g'+person.generation;
        G[gg+'_wMin'] = Math.min(G[gg+'_wMin'], x);
        G[gg+'_wMax'] = Math.max(G[gg+'_wMax'], 0 + x*1 + width*1 );
        G[gg+'_hMin'] = Math.min(G[gg+'_hMin'], y);
        G[gg+'_hMax'] = Math.max(G[gg+'_hMax'], 0 + y + height);
        //showGlobVars(logMsg);
    } else if (arguments.length == 6) { //  (pf, x, y, width, height, logMsg) {
        var pf = arguments[0]
        var x = arguments[1];  var y = arguments[2]
        var width = arguments[3];  var height = arguments[4]
        var logMsg = arguments[5];
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
    G.diwe=G.diwe_;
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


getPersonSpouseInFamily = function(person, family) { //-----------------------------------------------------------------
// E105-7/vsh init
/************************************************
DESCRIPTION: define 'person' person spouse person in 'family' family
PARAMETERS:
    person - person object
    family - family object
RETURNS:
   spouse person object or empty object {}
**************************************************/
    //logPerson(person.id)
    G.diwe_=G.diwe; G.diwe = 'I';
    var spouseId;
    if(typeof family === "undefined") {
        spouseId = 0
    } else {
        spouseId = ((family.mother > 0) && (person.id == family.mother)) ?
            ((family.father > 0) ? family.father : 0) :
            ((family.father > 0) && (person.id == family.father)) ?
                ((family.mother > 0) ? family.mother : 0) : 0;
    }
    return ( (spouseId > 0) ? g['p'+spouseId] : {} )
    logD("getPersonSpouseInFamily spouseId="+spouseId);
    G.diwe=G.diwe_;
};


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


/**
* Returns a random CSS color rgb code between min and max
*/
function randomColor () {
    var min = 50;
    var max = 250;
    var r = Math.random() * (max - min) + min;
    var g = Math.random() * (max - min) + min;
    var b = Math.random() * (max - min) + min;
    return "rgb("+r+","+g+","+b+")"
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


// Defines a Person box w and h
// B123-7/vsh init
//sketchPerson = function() {
Raphael.fn.sketchPerson = function() { //==================================================
    G.diwe_=G.diwe;  G.diwe = 'I';    //G.diwe=G.diwe_;
    for (var i in g) {
        if (!(i.indexOf('p') >= 0)) continue;
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
        if (!('bd' in p)) p.bd='';
        if (!('bp' in p)) p.bp='';
        if (!('dd' in p)) p.dd='';
        if (!('dp' in p)) p.dp='';
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

        // var rLines = 6-0;  // grubus sprendimas
        //var rLines = 2 + (whBD.w>0 ? 1 : 0) + (whBP.w>0 ? 1 : 0) + (whDD.w>0 ? 1 : 0) + (whDP.w>0 ? 1 : 0);  //alert(nLines);
            // blogai rodė:  DD DP be BD BP
        var rLines = 2 + ((whDD.w+whDP.w)>0 ? 4 : (whBD.w+whBP.w)>0 ? 2 : 0);  //alert(nLines);
            // E117-5/vsh pataisytas rodymas:  DD DP be BD BP
        var textWidth = Math.max(whNameGivn.w, whNameSurn.w, whBD.w, whBP.w, whDD.w, whDP.w) + gWidth;
        var textLineHeight = Math.max(whNameGivn.h, whNameSurn.h, whBD.h, whBP.h, whDD.h, whDP.h) + 0*gHeight;
        p.rLines = rLines;
        p.w = textWidth /*+ 2*G.margin*/;
        p.h = /*rLines**/textLineHeight /*+ G.margin/2*/;
        //logD('--] sP personId w h rLines ' + i+'  '+p.w+' '+p.h+' '+p.rLines)
        paper.drawPersonDefault(p.id)
    }
    for (var i in g) {
        if (!(i.indexOf('p') >= 0)) continue;
        var x = g[i]
        for (var j in x.r) {
            x.r[j].hide();
        }
    }
    G.diwe=G.diwe_;
}

// Draws a Person info
// E101-3/vsh init  made from  Raphael.fn.drawPerson
Raphael.fn.drawPersonDefault = function(personId) { //====================   d_r_a_w__P_e_r_s_o_n__D_e_f_a_u_l_t
    G.diwe_=G.diwe; G.diwe = 'I';
    //logD('[-- dP personId relPosition "' + personId+'" '+relPosition)
    var pKey = "p"+personId;
    var p = g[pKey];

    // E103-5/vsh this is done in server side // DC28-6/vsh  p.r=[];  // clean person objects array

    var gender = (p.gender=="M") ? '♂' : '♀';
    var nLines = p.rLines;

    textWidth = p.w /*- 2*G.margin*/;
    textLineHeight = p.h /*(p.h - G.margin/2)/nLines*/;

    if (G.direction == 'BT') {
        var nLines = p.rLines;    //alert('pe-init: nLines='+nLines);
        //var xptfc = 150; //f.x + f.w/2 - 0*G.margin/* - f.childrenWidth/2 + f.wMean/2*/;  //
        //var yptfc = 50; //f.y + f.h + G.vertGapBetweenGenerations;                       //
        //var realXY = locateXpendBox(p, 'cLR', xptfc, yptfc);
        //var x = realXY.x;
        //var y = realXY.y;
        var x = y = G.xyDefault; // default position for every Person box
        //var x = G.initX - 1*textWidth/2;
        //var y = G.initY - 1*textLineHeight * nLines;
        p.y = y; // - G.margin;

        logD('dP finally: personId nLines ' +pKey+' '+nLines)
        p.x = x - 0*G.margin;

        var rectBorder = (p.id==G.rootId) ? 3 : 1
        CSS.font = {'font-family':'Verdana', 'font-size':'10px'};
        CSS.text = {'fill':'#000000', 'stroke-width':'1', 'text-anchor':'start'};
        //CSS.male = {'fill':'270-#cff-#fff', 'stroke':'#000', 'stroke-width':rectBorder};
        //CSS.female = {'fill':'270-#fcf-#fff', 'stroke':'#000', 'stroke-width':rectBorder};
        //CSS.gender = ((p.gender=='M') ?  CSS.male : CSS.female);

        var rect1 = this.rect(p.x - G.margin, p.y - G.margin, p.w + 2*G.margin,
                p.h*nLines + G.margin, 5).attr((p.gender=='M') ? CSS.male : CSS.female)
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
    } else {
         logE("direction must be 'BT' [BottomTop] only");
    }

    //logD("familyId=" + ('familyId' in p));

    G.diwe=G.diwe_;
    //return (p.r);
}


// Defines a Family box w and h
// B123-7/vsh init
Raphael.fn.sketchFamily = function() { //==================================================
    G.diwe_=G.diwe;  G.diwe = 'I';
    for (var i in g) {
        if (!(i.indexOf('f') >= 0)) continue;
        var f = g[i];
        var whMD = getDimension('oo  '+f.md);
        // D710-3/vsh var whMP = getDimension('    '+f.mp);
        var whDD = getDimension('o-o '+f.dd);
        // D710-3/vsh var whDP = getDimension('    '+f.dp);
        var rLines = Math.max((whMD.w>0 ? 1 : 0) /*+(whMP.w>0 ? 1 : 0)*/ + (whDD.w>0 ? 1 : 0) /*+ (whDP.w>0 ? 1 : 0)*/, 1);
        var textWidth = Math.max(whMD.w/*, whMP.w*/, whDD.w/*, whDP.w*/);
        var textLineHeight = Math.max(whMD.h/*, whMP.h*/, whDD.h/*, whDP.h*/);
        f.rLines = rLines;
        f.w = textWidth /*+ 2*G.margin*/;
        f.h = /*rLines**/textLineHeight /*+ G.margin/2*/; // kad vert jungtys būtų vientisos
        //logD('--] sF fmilyId w h  ' + i+'  '+f.w+' '+f.h)

        if ('children' in f) {
            var childIdsString = ""+f.children;  // !!! ==> ""+...  is IMPORTANT
            var childIds = childIdsString.split(','); // logD("childIds="+childIds);
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
        paper.drawFamilyDefault(f.id)
    }
    for (var i in g) {
        if (!(i.indexOf('f') >= 0)) continue;
        var x = g[i]
        for (var j in x.r) {
            x.r[j].hide();
        }
    }
    G.diwe=G.diwe_;
}

// Draws a Family info
// E107-2/vsh init  made from  Raphael.fn.drawPersonDefault and
Raphael.fn.drawFamilyDefault = function(famId) { //====================   d_r_a_w__F_a_m_i_l_y___D_e_f_a_u_l_t
    G.diwe_=G.diwe; G.diwe = 'I';
    var fKey = "f"+famId;
    var f = fam = g[fKey];
    var nLines = f.rLines;

    textWidth = p.w /*- 2*G.margin*/;
    textLineHeight = p.h /*(p.h - G.margin/2)/nLines*/;

    if (G.direction == 'BT') {
        var x = y = G.xyDefault; // default position for every Family box
        f.x = x
        f.y = y
        var textWidth = f.w - 2*G.margin;
        var textLineHeight = f.h  //(f.h - G.margin/2)/nLines

        var fRect1 = this.rect(f.x - 1*G.margin, f.y - 0*G.margin,  f.w + 2*G.margin, f.h*f.rLines, 0*33+5);
        fRect1.attr(CSS.familyBox); /*alert(000);*/  //fRect1.attr({fill: "90-#fff-#000", stroke: "#f00", opacity: 1});
        f.r[f.r.length] = fRect1;  // logD("f.r[f.r.length] = "+f.r[f.r.length-1]);
        var text1 = this.text(f.x, f.y+1*G.margin+0*textLineHeight, ((f.md==''&& f.mp=='') ? ' ' : 'oo')+' '+f.md+((f.md=='') ? '' : ''))
        f.r[f.r.length] = text1.attr(CSS.font).attr(CSS.text);
        /* D710-3/vsh var text2 = this.text(f.x, f.y+1*G.margin/1+1*textLineHeight, '  '+f.mp+((f.mp=='') ? '' : ''))
        f.r[f.r.length] = text2.attr(CSS.font).attr(CSS.text); //alert('aaa aaa aaa aaa')*/
        var text3 = this.text(f.x, f.y+1*G.margin/1+2*textLineHeight, ((f.dd==''&& f.dp=='') ? ' ' : 'o-o')+''+f.dd+((f.mp=='') ? '' : ''))
        f.r[f.r.length] = text3.attr(CSS.font).attr(CSS.text);
        /* D710-3/vsh var text4 = this.text(f.x, f.y+1*G.margin/1+3*textLineHeight, '  '+f.dp+((f.dp=='') ? '' : ''))
        f.r[f.r.length] = text4.attr(CSS.font).attr(CSS.text);*/
    } else {
         logD("direction must be 'BT' [BottomTop] only");
    }
    G.diwe=G.diwe_;
}


// DC30-1/vsh init rebuilt from  Raphael.fn.drawFamily = function(familyId, skipSpouseId, skipChildId)
Raphael.fn.drawFamilies = function(key) { // key (in R) structure is pId[.fId]{1,n}
    G.diwe_=G.diwe;  G.diwe = 'I';

    CSS.font = {'font-family':'Verdana', 'font-size':'10px'};
    CSS.text = {'fill':'#000000', 'stroke-width':'1', 'text-anchor':'start'};
    CSS.familyBox = {'fill':"#ffffff", 'stroke':"#000", 'opacity':'1.0'};

    var PeFas = (""+key).split('.')  //;alert(PeFas)
    var peId = PeFas[0];
    var peObj = g["p"+peId]
    PeFas.shift() // remove array first element
    var famIds = PeFas               //;alert(famIds)
    var maxPeHeight = getMaxPeHeight(key)

    for (var j = 0; j < famIds.length; j++) {  //alert(j)
        var fKey = "f"+famIds[j];
        var fam = famObj = g[fKey];
        fam.color = randomColor()
        if ('fe' in fam) {
            logE("Raphael.fn.drawFamilies: the function 'if'  branch needs to be written !")
            return;
        }
        var w = fam.w
        var h = fam.h

        var spouse = getPersonSpouseInFamily(peObj, fam)

        var rTemp = [];    //alert('rTemp.length=' + rTemp.length)
        var deltaX = deltaY = 0;
        if (Object.keys(spouse).length == 0) {
            var peBox = R[key][0].getBBox(false);
            var pX = peBox.x, pY = peBox.y, pWidth = peBox.width, pHeight = peBox.height;
            deltaX = pX - G.xyDefault + (pWidth - w)/2
            deltaY = pY - G.xyDefault + /*pHeight*/Math.max(pHeight, maxPeHeight)
        } else if (famIds.length == 1) {
            var peBox = R[key][0].getBBox(false);
            var pX = peBox.x, pY = peBox.y, pWidth = peBox.width, pHeight = peBox.height;
            deltaX = pX - G.xyDefault + pWidth - w/2
            deltaY = pY - G.xyDefault + /*pHeight*/Math.max(pHeight, maxPeHeight)
        } else if (famIds.length > 1) {
            var xBox = R[fKey][0].getBBox(false);
            var pX = xBox.x, pY = xBox.y, pWidth = xBox.width, pHeight = xBox.height;
            deltaX = pX - G.xyDefault + pWidth/2
            deltaY = pY - G.xyDefault + /*pHeight*/Math.max(pHeight, maxPeHeight)
        } else { alert }
        for (var rf in fam.r) {
            rTemp[rTemp.length] = fam.r[rf].clone().show().translate(deltaX, deltaY);
        }
        fam.r = rTemp
    }

// E117-5/vsh komentarintas kodas NEVEIKIA
//    for (var i = 0; i < famIds.length; i++) {
//        var fKey = "f"+famIds[i];
//        var fam = famObj = g[fKey];
//        fam.afc ={};
//        var rect1 = fam.r[0]
//        if (G.loggedIn && (fam.father == G.rootId || fam.mother == G.rootId)) {       //alert(fKey)
//            var xglj = rect1.getBBox(false).x + fam.w/2 + 0.5*G.margin;
//            var yglj = rect1.getBBox(false).y + fam.h + 1.6*G.margin;
//            var arrowDown = paper.drawImage( G.app+'images/goDown.png', xglj, yglj, 'w', fam.h);
//            fam.r[fam.r.length] = arrowDown;
//            fam.arrowDown = arrowDown
//            fam.arrowDown.node.onmouseover = function() {  logI("arrowUp.node.onmouseover fKey="+fKey);
//                //if (fam.father == 0 || fam.mother == 0) arrowLeft.hide();
//                if ('arrowDown' in fam) fam.arrowDown.show();
//                paper.setActions4FamilyChildren(fam.id, rect1.getBBox(false));
//            };
//        }
//    }

    if (G.loggedIn /*&& (fam.father == G.rootId || fam.mother == G.rootId)*/) {       //alert(fKey)
        var fam0 = g["f"+famIds[0]];
        if ((fam0.father == G.rootId || fam0.mother == G.rootId)) {       //alert('fam0.id '+fam0.id)
            fam0.afc ={};
            var x = fam0.r[0].getBBox(false).x + fam0.w/2 + 0.5*G.margin;
            var y = fam0.r[0].getBBox(false).y + fam0.h + 1.6*G.margin;
            fam0.arrowDown = paper.drawImage( G.app+'images/goDown.png', x, y, 'w', fam0.h);
            fam0.r.push(fam0.arrowDown)
            //fam0.arrowDown = arrowDown
            fam0.arrowDown.node.onmouseover = function() {  //logI("arrowUp.node.onmouseover 0 fKey="+fam0.id);
                if ('arrowDown' in fam0) fam0.arrowDown.show();
                paper.setActions4FamilyChildren(fam0.id, fam0.r[0].getBBox(false));
            };
            if (famIds.length > 1) {
                var fam1 = g["f"+famIds[1]];
                if ((fam1.father == G.rootId || fam1.mother == G.rootId)) {       //alert('fam1.id '+fam1.id)
                    fam1.afc ={};
                    x = fam1.r[0].getBBox(false).x + fam1.w/2 + 0.5*G.margin;
                    y = fam1.r[0].getBBox(false).y + fam1.h + 1.6*G.margin;
                    fam1.arrowDown = paper.drawImage( G.app+'images/goDown.png', x, y, 'w', fam1.h);
                    fam1.r.push(fam1.arrowDown)
                    //fam1.arrowDown = arrowDown
                    fam1.arrowDown.node.onmouseover = function() {  //logI("arrowUp.node.onmouseover 1 fKey="+fam1.id);
                        if ('arrowDown' in fam1) fam1.arrowDown.show();
                        paper.setActions4FamilyChildren(fam1.id, fam1.r[0].getBBox(false));
                    };
                    if (famIds.length > 2) {
                        var fam2 = g["f"+famIds[2]];
                        if ((fam2.father == G.rootId || fam2.mother == G.rootId)) {       //alert('fam2.id '+fam2.id)
                            fam2.afc ={};
                            x = fam2.r[0].getBBox(false).x + fam2.w/2 + 0.5*G.margin;
                            y = fam2.r[0].getBBox(false).y + fam2.h + 1.6*G.margin;
                            fam2.arrowDown = paper.drawImage( G.app+'images/goDown.png', x, y, 'w', fam2.h);
                            fam2.r.push(arrowDown)
                            //fam2.arrowDown = arrowDown
                            fam2.arrowDown.node.onmouseover = function() {  //logI("arrowUp.node.onmouseover 2 fKey="+fam2.id);
                                if ('arrowDown' in fam2) fam2.arrowDown.show();
                                paper.setActions4FamilyChildren(fam2.id, fam2.r[0].getBBox(false));
                            };
                            if (famIds.length > 3) {
                                var fam3 = g["f"+famIds[2]];
                                if ((fam3.father == G.rootId || fam3.mother == G.rootId)) {       //alert('fam3.id '+fam3.id)
                                    fam3.afc ={};
                                    x = fam3.r[0].getBBox(false).x + fam3.w/2 + 0.5*G.margin;
                                    y = fam3.r[0].getBBox(false).y + fam3.h + 1.6*G.margin;
                                    fam3.arrowDown = paper.drawImage( G.app+'images/goDown.png', x, y, 'w', fam2.h);
                                    fam3.r.push(arrowDown)
                                    //fam2.arrowDown = arrowDown
                                    fam3.arrowDown.node.onmouseover = function() {  //logI("arrowUp.node.onmouseover 3 fKey="+fam3.id);
                                        if ('arrowDown' in fam3) fam3.arrowDown.show();
                                        paper.setActions4FamilyChildren(fam3.id, fam3.r[0].getBBox(false));
                                    };
                                }
                                if (famIds.length > 1) { logW(localeString('js_person_fams_exceeds_4 ')); return }
                            }

                        }
                    }

                }
            }
        }
    }

    G.diwe=G.diwe_;
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Defines possible set of actions for the Person direct ancestors
    // B110-1/vsh init
    this.setActions4FamilyChildren = function(familyId, bbox) {  //alert("setActions4FamilyChildren "+ familyId)
        G.diwe_=G.diwe; G.diwe = 'I';
        var fKey = "f"+familyId;
        var fam = g[fKey];
        var Afc = fam.afc
        Afc.a = {};
        Afc.xC = bbox.x
        Afc.yC = bbox.y + bbox.height/2
        var aaf = getDimension(localeString('js_add_son'));
        var aam = getDimension(localeString('js_add_daughter'));
        //var aac = getDimension(localeString('js_cancel'));
        Afc.w = Math.max(aaf.w, aam.w/*, aac.w*/) + 20  // .[nnnn]
        Afc.h = Math.max(aaf.h, aam.h/*, aac.h*/)
        var aN = 2
        Afc.x = Afc.xC               // /*p.x*/getDelta(f,'x') - 0.5*G.margin - Afc.w
        Afc.y = Afc.yC - aN*Afc.h/2  //  /*p.y*/getDelta(f,'y')
        Afc.cx = Afc.x;
        Afc.cy = Afc.y;

        paper.setAction4FamilyChildren(familyId, 'js_add_son', G.app+'rest/'+G.rootId+'/addSonToFamily/'+familyId);
        paper.setAction4FamilyChildren(familyId, 'js_add_daughter', G.app+'rest/'+G.rootId+'/addDaughterToFamily/'+familyId);
        //this.setAction4FamilyChildren(familyId, 'js_go_home', G.app+'');
        //this.setAction4FamilyChildren(familyId, 'js_cancel', '');

        // E116-4/vsh   G.topRect.attr({'fill':'#333', 'fill-opacity':'0.5'})

        fam.topAcctionsRect/*G.topAcctionsRect*/ = paper.rect(Afc.xC, Afc.yC-aN*Afc.h/2-G.arm, G.arm+Afc.w+G.arm, G.arm+Afc.h*aN+G.arm, 10).
            attr({'fill':'#ff9', 'fill-opacity':'0.8'});
        for (var i in Afc.a) { Afc.a[i].show(); Afc.a[i].toFront(); }

        fam.topAcctionsRect.node.onmouseover = function() {
            fam.arrowDown.hide()
        };

        fam.topAcctionsRect.node.onmouseout = function(event) {
            // get bounding rect of the paper
            var bnds = event.target.getBoundingClientRect();
            // adjust mouse x/y
            var mx = event.clientX - bnds.left
            var my = event.clientY - bnds.top
            var bx = fam.topAcctionsRect.getBBox(true)  // ---
            logD('mx='+mx +', my='+my +'; bx.x='+(bx.x-bnds.left)+', bx.y='+(bx.y-bnds.top)+', bx.width='+bx.width+', bx.height='+bx.height);
            if ((mx <= 0 || mx >= bx.width) || (my <= 0 || my >= bx.height)) {
                fam.topAcctionsRect.hide();
                fam.arrowDown.show()
                for (var i in Afc.a) { Afc.a[i].hide(); }
            }
        };
        G.diwe=G.diwe_;
    }

    // Visualize an action for the Person direct ancestor
    // B110-1/vsh init
    this.setAction4FamilyChildren = function(familyId, actionName, restUrl) { //====
        var fKey = "f"+familyId;
        var fam = g[fKey];
        var Afc = fam.afc
        var localeActionName = localeString(actionName);
        var actionCode = actionName.replace('.', '');
        var anAction = this.text(Afc.cx+G.arm, Afc.cy, localeActionName+' ['+familyId+']');
        anAction.attr("text-anchor","start");
        Afc.cx = Afc.cx;
        Afc.cy = Afc.cy + Afc.h + G.margin/2;
        Afc.a[actionCode] = anAction;

        // E116-4/vsh old version //var actionBox = this.rect(Afc.cx-G.margin/2, Afc.cy-Afc.h-1.25*G.margin, Afc.w+1*G.margin, Afc.h+1*G.margin/4, 0);
        var actionBox = this.rect(Afc.cx+G.arm-G.margin/2, Afc.cy-Afc.h-1.25*G.margin, Afc.w+1*G.margin, Afc.h+1*G.margin/4, 0)
        Afc.init = {/*"fill":"#fff",*/"fill":"#cff", "stroke":"#000", "opacity":0.5, "stroke-width":2};
        actionBox.attr(Afc.init);
        Afc.a[actionCode+'Box'] = actionBox;

        Afc.a[actionCode+'Box']/*actionBox*/.node.onmouseover = function() { //logD("anAction.node.onmouseover");
            Afc.mouseover = {"fill":"#cf9", "stroke":"#000", "opacity":0.0, "stroke-width":3, "title":"click mouse to " + localeActionName};
            actionBox.attr(Afc.mouseover);
        };

        Afc.a[actionCode+'Box']/*actionBox*/.node.onmouseout = function() { //logD("anAction.node.onmouseout");
            Afc.mouseout = {"fill":"#cff", "stroke":"#000", "opacity":0.5, "stroke-width":1};
            actionBox.attr(Afc.mouseout);
        };
        Afc.a[actionCode+'Box']/*actionBox*/.node.onclick = function() { //logD("anAction.node.onclick");
            fam.topAcctionsRect.remove()
            G.topRect.attr(G.topRectCssAttrs/*{fill: "270-#eeeeee-#dddddd", stroke: "#fff", opacity: 1}*/)
            actionBox.attr("fill","#fff");
            if (restUrl == '') {
                for (var i in Afc.a) { Afc.a[i].remove(); }
            } else {
                location.assign(restUrl);
            }
        };
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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

logger = function(message) {
    //logD(document.getElementById("logger").firstChild.nodeValue);
    document.getElementById("logger").firstChild.nodeValue = message;
};

function LoadNewPage () {
   location.assign ( G.app+"rest/person/6");
};


// Development time tool. Defines show Person info via alert
// B106-4/vsh init
function logPerson(personId) {
    var xKey = "p"+personId;
    var p = g[xKey];
    var res = "Person: ";
    var s = '';
    for (var i in p) { res = res + ' ' + i +'=' + p[i]; s ='; ' }
    var t = G.diwe; G.diwe="D";  logD(res);  G.diwe=t;
}

// Development time tool.  Defines show Family  info via alert
// B106-4/vsh init
function logFamily(familyId) {
    var xKey = "f"+familyId;
    var f = g[xKey];
    var res = "Family: ";
    var s = '';
    for (var i in f) { res = res + ' ' + i +'=' + f[i]; s ='; '  }
    var t = G.diwe; G.diwe="D";  logD(res);  G.diwe=t;
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
    G.diwe_=G.diwe; G.diwe = 'I';
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
    G.diwe_=G.diwe; G.diwe = 'I';
    G.g_base_rect = this.rect(G.baseWmin, G.baseHmin, G.baseWmax-G.baseWmin,
    G.baseHmax-G.baseHmin).attr({fill: "90-#fff-#000"});
    //logD("Click to remove development time generation area rectangle");
    G.g_base_rect.remove();
    G.diwe=G.diwe_;
};


// draws box for every generaton, a box contains all one generation objects
// B120-7/vsh init
Raphael.fn.drawBox4Gener = function() {
    G.diwe_=G.diwe; G.diwe = 'I';
    for (var i = G.gMin; i <= G.gMax; i++) {
        //logD(i);
        var rt = this.rect(G['g'+i+'_wMin'], G['g'+i+'_hMin'], G['g'+i+'_wMax']-G['g'+i+'_wMin'],
            G['g'+i+'_hMax']-G['g'+i+'_hMin']);
        rt.attr({fill: "90-#fff-#000"});
        //rt.attr({fill: "90-#fff-#000", 'stroke-width':11});
        G['g'+i+'_rect'] = rt;
    }
    //logD("Click to remove development time generation area rectangle");
//    for (var i = 0*99+G.gMin; i <= G.gMax; i++) {
//        pauseInMillis(10);
//        G['g'+i+'_rect'].remove();
//    }
    G.diwe=G.diwe_;
};


// E103-5-3/vsh init
/*Raphael.fn.*/getMaxPeHeight = function(key) {
    G.diwe_=G.diwe; G.diwe = 'I';
    var person = p = g['p'+key.split('.')[0]]  //peObj;
    var maxPeHeight = p.r[0].getBBox(true).height
    var idsInKey = key.split('.')
    for (var j = 1; j < idsInKey.length; j++){
        var family =  g['f'+idsInKey[j]]    //faObj;
        /*var spouseId = ((family.mother > 0) && (person.id == family.mother)) ?
            ((family.father > 0) ? family.father : 0) :
            ((family.father > 0) && (person.id == family.father)) ?
                ((family.mother > 0) ? family.mother : 0) : 0;*/
        var spouse = getPersonSpouseInFamily(person, family)
        //maxPeHeight = Math.max(maxPeHeight, g['p'+spouseId].r[0].getBBox(true).height)
        maxPeHeight = Math.max(maxPeHeight, (Object.keys(spouse).length > 0) ?  spouse.r[0].getBBox(true).height : 0)
    }
    G.diwe=G.diwe_;
    return maxPeHeight;
};


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// E103-5-3/vsh init
Raphael.fn.createPeBox = function(key, deltaX, deltaY) {
//************************************************
//DESCRIPTION: Creates (translates) pId person box in right location
//PARAMETERS:
//    key - key structure: pId[.fId[.fId[...]]], where pId is p.id value, fId is optional f.id value
//    deltaX - x-axis shift
//    deltaY - y-axis shift
//RETURNS:
//   rTemp - array of rect objects
//**************************************************
    G.diwe_=G.diwe; G.diwe = 'I';
    G.arm = 25; // Action rect margin
    var maxPeHeight = getMaxPeHeight(key)
    var person = g['p'+key.split('.')[0]]  //peObj;
    var personId = person.id
    var personGender = person.gender

    var rectBorder = (personId==G.rootId) ? 3 : 1
    var rTemp = [];
    for (var rp in person.r) { rTemp[rTemp.length] = person.r[rp].clone().show().translate(deltaX, deltaY); }
    var rect1 = rTemp[0];
    if (rect1.getBBox(true).height < maxPeHeight) {
       var rect2 = this.rect(rect1.getBBox(true).x, rect1.getBBox(true).y,
            rect1.getBBox(true).width, maxPeHeight, 5)/*.attr(CSS.gender)*//*.toBack()*/
        rTemp.shift();
        rTemp.unshift(rect2);
        for (var i = 1; i < rTemp.length; i++)  rTemp[i].toFront();
        rect1.remove()
    }
    rect1 = rTemp[0].attr((personGender=='M') ? CSS.male : CSS.female).attr({'stroke-width':rectBorder})

    rect1.node.onclick = function() { //logI("rect1.node.onclick " + personId);
        rect1.attr("fill", "#fff");
        if (personId == G.rootId) {
            location.assign ( G.app+"rest/personView/" + personId);
        } else
            location.assign ( G.app+"rest/person/" + personId);
    };

    rect1.node.onmouseover = function() { //logI("rect1.node.onmouseover " + personId);
        var rectBorder = (personId==G.rootId) ? 3 : 1
        rect1.attr((personGender=='M') ? CSS.maleOver : CSS.femaleOver).attr({'stroke-width':rectBorder})
        if (personId == G.rootId) {
            rect1.attr("title", localeString("js_go2PeData")/*"click mouse to see all the person data"*/);
        } else {
            rect1.attr("title", localeString("js_go2ChgPe")/*"click mouse to change current person"*/);
        }
    };

    rect1.node.onmouseout = function() { //logI("rect1.node.onmouseout " + personId);
        var rectBorder = (personId==G.rootId) ? 3 : 1
        rect1.attr((personGender=='M') ? CSS.maleOut : CSS.femaleOut).attr({'stroke-width':rectBorder})
    };

    rTemp[0] = rect1;

    if ((G.loggedIn && (personId == G.rootId))) {
        //alert (0+personId + ' =a= ' + 0+G.rootId)
        // D7004-4/vsh start using  (G.loggedIn
        // D207-4/vsh // goLeft, goUp icon shape is (p.h X p.h)
        // goLeft icon drawing  S-i-b-l-i-n-g  S-i-b-l-i-n-g  S-i-b-l-i-n-g
        var xgli = rect1.getBBox(false).x - g['p'+personId].h-0*1*G.margin-1
        var ygli = rect1.getBBox(false).y - 0*1*G.margin
        var arrowLeft = paper.drawImage( G.app+'images/goLeft.png', xgli, ygli, 'w', person.h);
        //rTemp.push(/*p.r[p.r.length] = */arrowLeft)
        person.r[person.r.length] = arrowLeft
        person["arrowLeft"] = arrowLeft

        arrowLeft.node.onmouseover = function() {   //logI("arrowLeft.node.onmouseover");
            if ('arrowLeft' in person) person.arrowLeft.hide();
            if ('arrowUp' in person) person.arrowUp.hide();
            paper.setActions4Sibling(person.id, rect1.getBBox(false));   //alert();
        };

        arrowLeft.node.onmouseout = function() {  //logD("arrowLeft.node.onmouseout");
            // E116-4/vsh  //if ('arrowUp' in person) person.arrowUp.show();
        };

        // goUp icon drawing  A-n-c-e-s-t-o-r  A-n-c-e-s-t-o-r  A-n-c-e-s-t-o-r  A-n-c-e-s-t-o-r
        var family = g['f'+person.familyId];
        if ((!('familyId' in person)) || (!(family.father > 0)) || (!(family.mother > 0))) {
            var xglj = rect1.getBBox(false).x
            var yglj = rect1.getBBox(false).y - person.h - 0.1*G.margin;
            var arrowUp = paper.drawImage( G.app+'images/goUp.png', xglj, yglj, 'w', person.h);
            rTemp.push(arrowUp)
            person["arrowUp"] = arrowUp;
            arrowUp.node.onmouseover = function() { ///*logD*/alert("arrowUp.node.onmouseover");
               if ('arrowLeft' in person) person.arrowLeft.hide();
               if ('arrowUp' in person) person.arrowUp.hide();
               paper.setActions4Ancestor(person.id, rect1.getBBox(false));
            };
            arrowUp.node.onmouseout = function() { ///*logD*/alert("arrowUp.node.onmouseout");
                //if ('arrowLeft' in person) person.arrowLeft.show();
            };
        }
    }
    G.diwe=G.diwe_;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Defines possible set of actions for the Person
    // B106-4/vsh init
    // E112-7/vsh moved inside 'createPeBox' function
    // relPosition: init, leftParent, rightParent, centerParent
    this.setActions4Sibling = function(personId, bbox/*, pboxX, pboxY*/) { //===S-i-b-l-i-n-g=========
        G.diwe_=G.diwe; G.diwe = 'I';

    //logE('setActions4Sibling personId='+personId+';  R keys='   +  Object.keys(R));
    ////logE('setActions4Sibling personId='+personId+';  rTemp[0]=' +  Object.keys(rTemp[0]));

        var pKey = "p"+personId;
        var person = g[pKey];
        As.a = {};
        As.xC = bbox.x - 2*G.margin/*E117-5/vsh  it hides arrowLeft icon*/
        As.yC = bbox.y + bbox.height/2
        //alert (bbox.x + ' ' + bbox.y+ ' ' + bbox.x2+ ' ' + bbox.y2 + ' ' + bbox.width+ ' ' + bbox.height )
        var asb = getDimension(localeString('js_add_brother'));
        var ass = getDimension(localeString('js_add_sister'));
        var aff = getDimension(localeString('js_add_family'));
        var asc = getDimension(localeString('js_cancel'));
        As.w = Math.max(asb.w, ass.w, asc.w, aff.w);
        As.h = Math.max(asb.h, ass.h, asc.h, aff.h);
        var aN = ('familyId' in person) ? 3 : 1
        As.x = As.xC/*-As.w/2*//*+arm */ //  /*rTemp[0].getBBox(false).x*//*pboxX*/bbox.x - As.w - 0.5*G.margin
        As.y = As.yC-aN*As.h/2   //  /*rTemp[0].getBBox(false).y*//*pboxY*/bbox.y // this.getDelta(person,'y')
        As.cx = As.x;
        As.cy = As.y;

        var fId = ('familyId' in person) ? (0+person.familyId) : '0';
        // D704-4/vsh this.setAction4Sibling(personId, 'js_full_info', G.app+'rest/personView/'+personId);  // B217-4/vsh
        //alert (personId + ' =b= ' + G.rootId)
        if (G.loggedIn && (0+personId == 0+G.rootId)) { // D207-4/vsh
            if ('familyId' in person) {
                this.setAction4Sibling(personId, 'js_add_brother', G.app+'rest/'+personId+'/addBrotherToFamily/'+fId);
                this.setAction4Sibling(personId, 'js_add_sister', G.app+'rest/'+personId+'/addSisterToFamily/'+fId);
            }
            this.setAction4Sibling(personId, 'js_add_family', G.app+'rest/'+personId+'/addNewFamily/'+person.gender);
        }

        G.topAcctionsRect = this.rect(As.xC, As.yC-aN*As.h/2-G.arm, G.arm+As.w+G.arm, G.arm+As.h*aN+G.arm, 10).attr({'fill':'#ff9', 'fill-opacity':'0.8'});
        for (var i in As.a) { As.a[i].show(); As.a[i].toFront(); }

         G.topAcctionsRect.node.onmouseover = function() {
            //person.arrowLeft.hide()
            if ('arrowLeft' in person) person.arrowLeft.hide();
        };

         G.topAcctionsRect.node.onmouseout = function(event) {
            // get bounding rect of the paper
            var bnds = event.target.getBoundingClientRect();
            // adjust mouse x/y
            var mx = event.clientX - bnds.left
            var my = event.clientY - bnds.top
            var bx = G.topAcctionsRect.getBBox(true)
            logD('mx='+mx +', my='+my +'; bx.x='+(bx.x-bnds.left)+', bx.y='+(bx.y-bnds.top)+', bx.width='+bx.width+', bx.height='+bx.height);
            //if ((mx <= bx.x-bnds.left || mx >= bx.x-bnds.left+bx.width) || (my <= bx.y-bnds.top || my >= bx.y-bnds.top+bx.height)) {   //alert('ok')
            if ((mx <= 0 || mx >= bx.width) || (my <= 0 || my >= bx.height)) {   //alert('ok')
                G.topAcctionsRect.hide();
                if ('arrowLeft' in person) person.arrowLeft.show();
                if ('arrowUp' in person) person.arrowUp.show();
                for (var i in As.a) { As.a[i].hide(); }
            }
        };
        G.diwe=G.diwe_;
    }


    // Visualize an action for the Person
    // B108-6-4/vsh init
    // E112-7/vsh moved inside 'createPeBox' function
    this.setAction4Sibling = function(personId, actionName, restUrl) { //=====
        var pKey = "p"+personId;
        var person = g[pKey];
        var localeActionName = localeString(actionName);
        var actionCode = actionName.replace('.', '');
        var anAction = this.text(As.cx+G.arm, As.cy, localeActionName)
      //var anAction = this.text(As.cx, As.cy, localeActionName);
        anAction.attr("text-anchor","start");
        //As.cx = As.cx;
        As.cy = As.cy + As.h + G.margin/2;
        As.a[actionCode] = anAction;

        var actionBox = this.rect(As.cx+G.arm-G.margin/2, As.cy-As.h-1.25*G.margin, As.w+1*G.margin, As.h+1*G.margin/4, 0)
        As.init = {"fill":"#cff", "stroke":"#000", "opacity":0.5,  "stroke-width":2};
        actionBox.attr(As.init);
        As.a[actionCode+'Box'] = actionBox;

        actionBox.node.onmouseover = function() { //logD("anAction.node.onmouseover");
            As.mouseover = {"fill":"#cf9", "stroke":"#000", "opacity":0.0, "stroke-width":3, "title":"click mouse to " + localeActionName};
            actionBox.attr(As.mouseover);
        };

        actionBox.node.onmouseout = function() { //logD("anAction.node.onmouseout");
            As.mouseout = {"fill":"#cff", "stroke":"#000", "opacity":0.5, "stroke-width":1};
            actionBox.attr(As.mouseout);
        };

        actionBox.node.onclick = function() { //logD("anAction.node.onclick");
            G.topAcctionsRect.remove()
            G.topRect.attr(G.topRectCssAttrs/*{fill: "270-#eeeeee-#dddddd", stroke: "#fff", opacity: 1}*/)
            actionBox.attr("fill","#fff");
            if (restUrl == '') {
                for (var i in As.a) { As.a[i].remove(); }
                if ('arrowLeft' in person) person.arrowLeft.show();
                if ('arrowUp' in person) person.arrowUp.show();
            } else {
                location.assign(restUrl);
            }
        };
    }

    // Defines possible set of actions for the Person direct ancestors
    // B110-1/vsh init
    // E112-7/vsh moved inside 'createPeBox' function
    // relPosition: init, leftParent, rightParent, centerParent
    this.setActions4Ancestor = function(personId, bbox) { //===A-n-c-e-s-t-o-r========
        G.diwe_=G.diwe; G.diwe = 'I';
        var pKey = "p"+personId;
        var person = g[pKey];
        Aa.a = {};
        Aa.xC = bbox.x
        Aa.yC = bbox.y + bbox.height/2
        var aaf = getDimension(localeString('js_add_husband'));
        var aam = getDimension(localeString('js_add_wife'));
        //var aac = getDimension(localeString('js_cancel'));
        Aa.w = Math.max(aaf.w, aam.w/*, aac.w*/);
        Aa.h = Math.max(aaf.h, aam.h/*, aac.h*/);
        //Aa.x = person.x - 2*G.margin - Aa.w;
        //Aa.y = person.y - person.h;  //Aa.h;
        var aN = ('familyId' in person) ? 1 : 2
        //Aa.x =/*person.x*/getDelta(person,'x') - 0.5*G.margin - Aa.w
        //Aa.y = /*person.y*/getDelta(person,'y')
        Aa.x = Aa.xC
        Aa.y = Aa.yC-aN*Aa.h/2
        Aa.cx = Aa.x;
        Aa.cy = Aa.y;

        if ('familyId' in person) { // the person has parents
            var f = g['f'+person.familyId];
            if (f.father == 0)
                this.setAction4Ancestor(personId, 'js_add_father', G.app+'rest/'+personId+'/addFatherToFamily/'+person.familyId);
            if (f.mother == 0)
                this.setAction4Ancestor(personId, 'js_add_mother', G.app+'rest/'+personId+'/addMotherToFamily/'+person.familyId);
        } else {
            this.setAction4Ancestor(personId, 'js_add_father', G.app+'rest/'+personId+'/addFatherToFamily/'+0);
            this.setAction4Ancestor(personId, 'js_add_mother', G.app+'rest/'+personId+'/addMotherToFamily/'+0);
        }

        //var arm = 50; // Action rect margin
        //G.topAcctionsRect = this.rect(Aa.x-arm, Aa.y-arm, arm+Aa.w+arm, arm+Aa.h*5+arm, 10).attr({'fill':'#ff9', 'fill-opacity':'0.5'});
        G.topAcctionsRect = this.rect(Aa.xC, Aa.yC-aN*Aa.h/2-G.arm, G.arm+Aa.w+G.arm, G.arm+Aa.h*aN+G.arm, 10).attr({'fill':'#ff9', 'fill-opacity':'0.8'});
        for (var i in Aa.a) { Aa.a[i].show(); Aa.a[i].toFront(); }

         G.topAcctionsRect.node.onmouseover = function(/*event*/) {
            person.arrowUp.hide()
        };

         G.topAcctionsRect.node.onmouseout = function(event) {    // person.arrowLeft
            // get bounding rect of the paper
            var bnds = event.target.getBoundingClientRect();
            // adjust mouse x/y
            var mx = event.clientX - bnds.left
            var my = event.clientY - bnds.top
            var bx = G.topAcctionsRect.getBBox(true)
            logD('mx='+mx +', my='+my +'; bx.x='+(bx.x-bnds.left)+', bx.y='+(bx.y-bnds.top)+', bx.width='+bx.width+', bx.height='+bx.height);
            //if ((mx <= bx.x-bnds.left || mx >= bx.x-bnds.left+bx.width) || (my <= bx.y-bnds.top || my >= bx.y-bnds.top+bx.height)) {   //alert('ok')
            if ((mx <= 0 || mx >= bx.width) || (my <= 0 || my >= bx.height)) {   //alert('ok')
                G.topAcctionsRect.hide();
                if ('arrowLeft' in person) person.arrowLeft.show();
                if ('arrowUp' in person) person.arrowUp.show();
                for (var i in Aa.a) { Aa.a[i].hide(); }
            }
        };
        G.diwe=G.diwe_;
    }


    // Visualize an action for the Person direct ancestor
    // B110-1/vsh init
    // E112-7/vsh moved inside 'createPeBox' function
    this.setAction4Ancestor = function(personId, actionName, restUrl) { //====
        var pKey = "p"+personId;
        var person = g[pKey];
        var localeActionName = localeString(actionName);
        var actionCode = actionName.replace('.', '');
        var anAction = this.text(Aa.cx+G.arm, Aa.cy, localeActionName)
        //var anAction = this.text(Aa.cx, Aa.cy, localeActionName);
        anAction.attr("text-anchor","start");
        //Aa.cx = Aa.cx;
        Aa.cy = Aa.cy + Aa.h + G.margin/2;
        Aa.a[actionCode] = anAction;

        var actionBox = this.rect(Aa.cx+G.arm-G.margin/2, Aa.cy-Aa.h-1.25*G.margin, Aa.w+1*G.margin, Aa.h+1*G.margin/4, 0);
        Aa.init = {"fill":"#cff", "stroke":"#000", "opacity":"0.5", "stroke-width":"2"};
        actionBox.attr(Aa.init);
        Aa.a[actionCode+'Box'] = actionBox;

        actionBox.node.onmouseover = function() { //logD("anAction.node.onmouseover");
            Aa.mouseover = {"fill":"#cf9", "stroke":"#000", "opacity":0.0, "stroke-width":"3", "title":"click mouse to " + localeActionName};
            actionBox.attr(Aa.mouseover);
        };

        actionBox.node.onmouseout = function() { //logD("anAction.node.onmouseout");
            Aa.mouseout = {"fill":"#cff", "stroke":"#000", "opacity":0.5, "stroke-width":"1"};
            actionBox.attr(Aa.mouseout);
        };
        actionBox.node.onclick = function() { //logD("anAction.node.onclick");
            G.topAcctionsRect.remove()
            G.topRect.attr(G.topRectCssAttrs/*{fill: "270-#eeeeee-#dddddd", stroke: "#fff", opacity: 1}*/)
            actionBox.attr("fill","#fff");
            if (restUrl == '') {
                for (var i in Aa.a) { Aa.a[i].remove(); }
                if ('arrowUp' in person) person.arrowUp.show();
                if ('arrowLeft' in person) person.arrowLeft.show();
            } else {
                location.assign(restUrl);
            }
        };
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    return rTemp;
};
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


// E103-5-3/vsh init
Raphael.fn.createSpouseBox = function(key, spIndex, deltaX, deltaY) {
//************************************************
//DESCRIPTION: Creates (translates) pId person spouse (key.split('.')[spIndex]) box in right location
//PARAMETERS:
//    key - key structure: pId[.fId[.fId[...]]], where pId is p.id value, fId is optional f.id value
//    spIndex - key.split('.') array element index
//    deltaX - x-axis shift
//    deltaY - y-axis shift
//RETURNS:  true, when spouse really exists, false - otherwise
//**************************************************
    G.diwe_=G.diwe; G.diwe = 'I';
    var maxPeHeight = getMaxPeHeight(key)
    logD("createSpouseBox key="+key);
    var person = /*p = */g['p'+key.split('.')[0]]      //peObj;
    var family =  g['f'+key.split('.')[spIndex]]    //faObj;
    logD("createSpouseBox family.id="+family.id);
    var spouse = getPersonSpouseInFamily(person, family)

    if (Object.keys(spouse).length == 0) {
        //logE('drawFamilies: no spouse for person id='+ person.id + '; in family id=' + family.id);
        return false;
    }
    var spouseId = spouse.id

    logD("createSpouseBox spouseId="+spouseId);
    var rTemp = [];
    for (var rp in g['p'+spouseId].r) {
        rTemp[rTemp.length] = g['p'+spouseId].r[rp].clone().show().translate(deltaX, deltaY);
    }
    var rectBorder = (g['p'+spouseId].id == G.rootId) ? 3 : 1
    var rect1 = rTemp[0];
    if (rect1.getBBox(true).height < maxPeHeight) {
        var rect2 = this.rect(rect1.getBBox(true).x, rect1.getBBox(true).y,
            rect1.getBBox(true).width, maxPeHeight, 5).
            attr((spouse.gender=='M') ? CSS.male : CSS.female).attr({'stroke-width':rectBorder})
        rect1.remove()
        rTemp.shift();
        rTemp.unshift(rect2);
        for (var i = 1; i < rTemp.length; i++)  rTemp[i].toFront();
        rect1 = rTemp[0];
    }

    rect1.node.onclick = function() { //logI("rect1.node.onclick " + spouseId);
        rect1.attr("fill", "#fff");
        location.assign ( G.app+"rest/person/" + g['p'+spouseId].id);
    };

    rect1.node.onmouseover = function() { //logI("rect1.node.onmouseover " + spouseId);
        rect1.attr((spouse.gender=='M') ? CSS.maleOver : CSS.femaleOver).attr({'stroke-width':rectBorder}).
            attr("title", localeString("js_go2ChgPe")/*"click mouse to change current person"*/);
    };

    rect1.node.onmouseout = function() { //logI("rect1.node.onmouseout " + spouseId);
        rect1.attr((spouse.gender=='M') ? CSS.maleOut : CSS.femaleOut).attr({'stroke-width':rectBorder}) //.attr({fill: '270-#fcf-#fff', opacity: 0.5});
    };

    rTemp[0] = rect1;
    R['f'+key.split('.')[spIndex]] = rTemp;
    G.diwe=G.diwe_;
    return true;
};


//
// B126-3/vsh init
Raphael.fn.translteForest = function(deltaX, deltaY) {
    G.diwe_=G.diwe; G.diwe = 'i';
    for (var i in R) {
        logD('i = ' + i);
        var x = R[i]
        for (var j in x) { x[j].translate(deltaX, deltaY) }
    }
    for (var i in g) {
        logD('i = ' + i);
        var x = g[i]
        for (var j in x.r) { x.r[j].translate(deltaX, deltaY)}
    }
    G.diwe=G.diwe_;
};


// -----------------------------------------------------------------------------
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// #############################################################################

// ====================================
// Kiekvienoje generacijoje taip surūšiuojami asmenys, kad kairėje forest dalyje būtų
//      artimiausi 'root' asmeniui
// BB08-2/vsh init
Raphael.fn.rangeAll = function() {
    G.diwe_=G.diwe;  G.diwe = 'I';
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
    logD( logText);

    G.diwe=G.diwe_;
};

// ====================================
// Vienos generacijos asmenų Id'ų masyve, rūšiuotinas id'as ( >0 ) perkeliamas kairiop po jau perkeltųjų ( <0 )
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


// ============================================================================
// Shows one person and his not yet shown families
// DC28-6/vsh init
Raphael.fn.drawPeAndFams = function(personObj) {   //alert('drawPeAndFams')
    G.diwe_=G.diwe;  G.diwe = 'I';
    var p = personObj;
    logD('drawPeAndFamilies Asmuo ' + p.id + '  ' +  p.nameGivn + '  ' + p.nameSurn + '  fd=' + p.fd);
    var key = ''+p.id
    if (('fd' in p)) { // the Person is/was maried at least once
        var familyIds = getPersonFamilyIdsArr(p);
        // build a key for R
        for (var j = 0; j < familyIds.length; j++){
            if (!isFamilyDrawn(p, familyIds[j])) {
                key = key + '.' + familyIds[j]
            }
        }
        logD('drawPeAndFamilies key ' + key);
        var idsInKey = key.split('.')
        var maxPeHeight = getMaxPeHeight(key)
        //if (key.indexOf(".") > 0) { // there are not shown families for the person
        if (idsInKey.length > 1) { // there are not shown families for the person
            //  http://stackoverflow.com/questions/728360/most-elegant-way-to-clone-a-javascript-object
            var realXY = locateBox(p);  //var x = realXY.x;   //var y = realXY.y;
            var deltaX = realXY.x - G.xyDefault;
            var deltaY = realXY.y - G.xyDefault;
            R[key] = paper.createPeBox(key/*p*/, deltaX, deltaY)
            deltaX += /*deltaX +*/ p.r[0].getBBox(true).width;
            for (var j = 1; j < idsInKey.length; j++){
                if (paper.createSpouseBox(key, j/*peObj, faObj*/, deltaX, deltaY))
                    deltaX += R['f'+key.split('.')[j]][0].getBBox(true).width
            }
            this.drawFamilies(key); // key is pId[.fId]{1,n}
            updateGlobVars(key, 'drawPeAndFams');
        }
    } else { // the Person is single
            var realXY = locateBox(p);  //var x = realXY.x;   //var y = realXY.y;
            var deltaX = realXY.x - G.xyDefault;
            var deltaY = realXY.y - G.xyDefault;
            R[key] = paper.createPeBox(key/*p*/, deltaX, deltaY)
            updateGlobVars(key, 'drawPeAndFams');
    }
    G.diwe_=G.diwe;  G.diwe = 'I';
    logD('drawPeAndFamilies R key='+key+';  keys=' +  Object.keys(R));
    G.diwe=G.diwe_;
};


var getPersonFamilyIdsArr = function(person) {
    var familyIds = [];
    if (('fd' in person)) { // the Person is/was maried at least once
        var familyIdsString = ""+person.fd;  // !!! ==> ""+...  is IMPORTANT
        familyIds = familyIdsString.split(",");
    }
    return familyIds;
}


// Checks if family is drawn now
// E107-2/vsh init
function isFamilyDrawn(person, familyId) {
    var fKey = "f"+familyId;
    var f = g[fKey];
    return ((('f'+familyId) in R) ? true : false)
 }


// ====================================
// draws all generation families and persons
// BA25-2/vsh init
Raphael.fn.drawAll = function() {   //alert("drawAll");
    G.diwe_=G.diwe;  G.diwe = 'I';
    paper.drawPeAndFams(g["p"+G.rootId]);
    for (var i = G.gMin; i <= (0); i++) {
        logD('Karta (<=0) ' +  i);
        var pgenArr = G['s'+(i)]; //(G['g'+(i)]+'').split(',');
        logD( 's'+(i) + '  ' + G['s'+(i)].length + ';  ' + G['s'+(i)].toString())
        for (var ii in G['s'+(i)]) {
            logD('pgenArr[ii] ' + G['s'+(i)][ii]);
            var pe = pInGener = g['p'+Math.abs(G['s'+(i)][ii])];
            if (pe.id != G.rootId)  this.drawPeAndFams(pe);
        }
    }
    for (var i = 1; i <= G.gMax; i++) {
        logD('Karta (>0) ' + i);
        var pgenArr = G['s'+i]; //(G['g'+i]+'').split(',');
        logD( 's'+(i) + '  ' + pgenArr.length)
        for (var ii in pgenArr) {
            var pe = pInGener = g['p'+Math.abs(G['s'+(i)][ii])];
            this.drawPeAndFams(pe);
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


// C103-5/vsh init
function locateBox(p) {
/************************************************
DESCRIPTION: finds free enough space for Person box
PARAMETERS:
    p - person object
RETURNS:
   (x, y) of top-left corner
**************************************************/
    var xptfc = 1*75+0*150+0*150; //f.x + f.w/2 - 0*G.margin/* - f.childrenWidth/2 + f.wMean/2*/;  //
    var yptfc = 1*75+0*50+0*50;   //f.y + f.h + G.vertGapBetweenGenerations;                       //
    return locateXpendBox(p, 'cLR', xptfc, yptfc);
}


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
    G.diwe_=G.diwe; G.diwe = 'I';
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
    logD("locateXpendBox: generation xx yy " +p.generation+" "+xx+" "+yy);
    G.diwe=G.diwe_;
    return {x:xx, y:yy};
}


Raphael.fn.getPersonBox = function(peId) { //==================================================
// E110-5/vsh init
/************************************************
DESCRIPTION: finds Person box
PARAMETERS:
    peId - person id
RETURNS:
   Raphael's 'rect' type object
**************************************************/
    G.diwe_=G.diwe; G.diwe = 'i';
    for (var per in R) {
        logD('getPersonBox [1]... per |' + per + '| ')
        if (''+per.indexOf('f') >= 0) { continue; }
        //else {
        logD('getPersonBox [ok] per |' + per + '| ')
        if (per.split('.')[0] == peId) {
            G.diwe=G.diwe_;
            return R[per][0].getBBox(false)
        }
        //}
    }
    logD('getPersonBox [2]... peId |' + peId + '| ')
    for (var per in R) {
        logD('getPersonBox [2]... per |' + per + '| ')
        if (''+per.indexOf('f') >= 0) {
            var fam = g[per];
            if (fam == undefined) logE("getPersonBox fam undefined for family id " + per )
            logD('getPersonBox |' + per + '| ' + fam)
            logD('getPersonBox [2]... peId |' + peId + '| ' + fam.father + '  ' + fam.mother )
            if ((fam.father == peId) || (fam.mother == peId )) {
                G.diwe=G.diwe_;
                return R[per][0].getBBox(false)
            }
        }
    }
    logE('getPersonBox:person ' + peId + ' has no Box !');
    G.diwe=G.diwe_;
}


// Bind persons
// BB07-1/vsh init
Raphael.fn.bindPersons = function() { //==================================================
    G.diwe_=G.diwe; G.diwe = 'i';    //G.diwe=G.diwe_;
    logD('bindPersons R keys ' +  Object.keys(R));
    //logger('bindPersons R keys ' +  Object.keys(R))
    for (var i in g) {
        //logD("bindPersons: i = " +  i)
        if (i.indexOf('p') == -1) continue;
        var p = g[i];
        if ('familyId' in p) { // the Person has parents
            logD('bindPersons Asmuo : i=' +  i+ '  p.id=' + p.id + '  ' +  p.nameGivn + '  ' + p.nameSurn + '  fd=' + p.fd);
            var fKey = 'f'+p.familyId;   //logD(f);
            //if (fKey == 'f1') alert('fKey=' + fKey);
            if (fKey in g) {  // B130-7: opposite may happen because of forest show narrowing
                var f = g[fKey]
                var rColor = f.color
                //var fmw = f.x + f.w/2;                                  //logD("f.x f.w "+f.x+" "+f.w);
                //var fmh = f.y + f.h*f.rLines + 0*G.margin/2;            //logD("f.y f.h "+f.y+" "+f.h);*/
                var fBox = f.r[0].getBBox(false)
                var fmw = fBox.x + fBox.width/2
                var fmh = fBox.y + fBox.height*f.rLines - 3*G.margin/1

                //var plw = p.x + p.w/2 + 0*G.margin;
                //var plh = p.y - G.margin;                               //logD('M'+fmw+' '+fmh+'L'+plw+' '+plh);
                //if (fKey == 'f1') alert('fKey in g fKey=' + fKey + '------- ' + 'M'+fmw+' '+fmh+'L'+plw+' '+plh);
                var box = this.getPersonBox(p.id)
                if (box == undefined) logE("bindPersons box undefined for p.id " + p.id )
                var plw = box.x + box.width/2
                var plh = box.y - 0*G.margin


                logD/*alert*/('path |'+'M'+fmw+' '+fmh+'L'+plw+' '+plh+'|');  // alert("drawPerson relPosition = " + relPosition);
                var path1 = this.path('M'+fmw+' '+fmh+'L'+plw+' '+plh).attr({stroke: rColor})
                path1.attr({stroke: rColor/*randomColor()*/})
                p.r[p.r.length] = path1;
                //logD("bindPersons: p.r[p.r.length-1] = " +  p.r[p.r.length-1]);
                //G.diwe = G.diwe_temp;
            }
        }
    }
    G.diwe=G.diwe_;
//    G.diwe_=G.diwe; //G.diwe = 'D';    //G.diwe=G.diwe_;
//    for (var i in g) {
//        if (i.indexOf('p') >= 0) {
//            var p = g[i/*pKey*/];
//            if ('familyId' in p) { // the Person has parents
//                var fKey = 'f'+p.familyId;   //logD(f);
//                //if (fKey == 'f1') alert('fKey=' + fKey);
//                if (fKey in g) {  // B130-7: opposite may happen because of forest show narrowing
//                    //if (fKey == 'f1') alert('fKey in g fKey=' + fKey);
//                    var f = g[fKey];   //logD(f);
//                    var rColor = f.color; //randomColor()
//                    // TODO nebaigta !!! var spouseId = (('mother' in f) && (personId == f.mother)) ? (('father' in fd) ? f.father : '0') :
//                    //    (('father' in fd) && (personId == f.father)) ? (('mother' in fd) ? f.mother : '0') : '0';
//                    /* BB07-1/vsh
//                    this.drawFamily(p.familyId, 0, personId); // (familyId, skipsSpouseId, skipChilId)
//                    */
//                    //G.diwe_temp = G.diwe; G.diwe ='D'
//                    var fmw = f.x + f.w/2;                                  //logD("f.x f.w "+f.x+" "+f.w);
//                    var fmh = f.y + f.h*f.rLines + 0*G.margin/2;            //logD("f.y f.h "+f.y+" "+f.h);
//
//                    //--var plw = p.x + p.w/2 - G.margin;
//                    var plw = p.x + p.w/2 + 0*G.margin;
//
//                    var plh = p.y - G.margin;                               //logD('M'+fmw+' '+fmh+'L'+plw+' '+plh);
//                    //if (fKey == 'f1') alert('fKey in g fKey=' + fKey + '------- ' + 'M'+fmw+' '+fmh+'L'+plw+' '+plh);
//                    var path1 = this.path('M'+fmw+' '+fmh+'L'+plw+' '+plh);  // alert("drawPerson relPosition = " + relPosition);
//                    //if (fKey == 'f1') alert('fKey in g fKey=' + fKey + '=======');
//                    path1.attr({stroke: rColor/*randomColor()*/})
//                    p.r[p.r.length] = path1;
//                    //logD("Raphael.fn.drawPerson: p.r[p.r.length-1] = " +  p.r[p.r.length-1]);
//                    //G.diwe = G.diwe_temp;
//                }
//            }
//        }
//    }
//    G.diwe=G.diwe_;
}


////======================================================================================================================
////  E115-3/vsh   http://stackoverflow.com/questions/15257059/how-do-i-get-an-event-in-raphaels-paper-coordinates
///*============================================================*/
////var paper = Raphael(10, 50, 320, 200);
//
//var rect = paper.rect(0, 0, 200, 200);
//rect.attr('fill', 'green');
//rect.mousedown(function (event, a, b) {
//    // get bounding rect of the paper
//    var bnds = event.target.getBoundingClientRect();
//    // adjust mouse x/y
//    var mx = event.clientX - bnds.left;
//    var my = event.clientY - bnds.top;
//    // divide x/y by the bounding w/h to get location %s and apply factor by actual paper w/h
//    var fx = mx/bnds.width * rect.attrs.width
//    var fy = my/bnds.height * rect.attrs.height
//    // cleanup output
//    fx = Number(fx).toPrecision(3);
//    fy = Number(fy).toPrecision(3);
//    //$('#here').text('x: ' + fx + ', y: ' + fy);
//    alert('x: ' + fx + ', y: ' + fy);
//});
////rect.mouseout(function (event, a, b) {
//rect.node.onmouseout = function(event/*, a, b*/) {
//    // get bounding rect of the paper
//    var bnds = event.target.getBoundingClientRect();
//    // adjust mouse x/y
//    var mx = event.clientX - bnds.left
//    var my = event.clientY - bnds.top
//    // divide x/y by the bounding w/h to get location %s and apply factor by actual paper w/h
//    var fx = mx/bnds.width * rect.attrs.width
//    var fy = my/bnds.height * rect.attrs.height
//    // cleanup output
//    fx = Number(fx).toPrecision(3);
//    fy = Number(fy).toPrecision(3);
//    //$('#here').text('x: ' + fx + ', y: ' + fy);
//    //alert('mx: ' + mx + ', my: ' + my + '; x: ' + fx + ', y: ' + fy);
//    alert('event.clientX: ' + event.clientX + ', event.clientY: ' + event.clientY + '; bnds.left: ' + bnds.left + ', bnds.top: ' + bnds.top + '; x: ' + fx + ', y: ' + fy);
//};
////paper.setViewBox(5, 5, 10, 10);
//paper.setViewBox(0, 0, 10, 10, true);
////paper.setViewBox(300, 300, 400, 400);
///*============================================================*/

// =====================================================================================================================
// =====================================================================================================================
//alert("G.initX = "+G.initX);
InitGlobVars();

paper = Raphael(document.getElementById("canvas"), G.globWidth, G.globHeight);
var scale = 1.375
paper.setSize(scale*G.globWidth, scale*G.globHeight);
G.topRect = paper.rect(0, 0, scale*G.globWidth, scale*G.globHeight).attr(G.topRectCssAttrs);

//--paper.drawTestShapes(); // DO NOT DELETE

///*
// //========================================
//  paper.drawAll();
//  sketchPerson();
//  sketchFamily();
//
//  //alert("G.initX = "+G.initX);
//  G._f = g["f"+getPersonFamilyIdsArr(g["p"+G.rootId])[0]];
//  paper.drawPerson(G.rootId, 'init');  //alert("G.initX = "+G.initX);
//  //alert("==> after:  paper.drawPerson(G.rootId, 'init')");
//  //paper.drawBaseBox();
//  //paper.drawBox4Gener();
//  //showGlobVars("Before forest translation []... ")
//
//  G.initX = 5*G.horizGapBetweenSiblings - G.baseWmin;
//  G.initY = 5*G.horizGapBetweenSiblings - G.baseHmin;
//
//  //alert("==> before second:  paper.drawPerson(G.rootId, 'init')");
//  //paper.drawPerson(G.rootId, 'init');
//  paper.translteForest(G.initX, G.initY);
////========================================
//*/

//========================================
  paper.rangeAll();
  //alert("G.initX = "+G.initX + ";   G.initY = "+G.initY);
  // e103-5/vsh sketchPerson();

  paper.sketchPerson(); // since e103-5
  paper.sketchFamily();
  //paper.drawBox4Gener();

  //paper.drawPerson(G.rootId, 'personOnly');
  //  var pKey = "p"+G.rootId;    //alert(pKey);
  //  var pe = g["p"+G.rootId];   //alert(p);
  //paper.drawPeAndFams(g["p"+G.rootId]);

  paper.drawAll();
  paper.bindPersons();

  //G.diwe_=G.diwe;  G.diwe = 'D';
  //logD('drawPersonAndFamilies R keys ' +  Object.keys(R));
  //G.diwe=G.diwe_;

  var goXxxW = 30;
  var goXxxH = 30;
  var marginX = 10;
  var marginY = 10;
  var vInitx = marginX;
  var vInity = 10 //G.globHeight/2 + 2*goXxxH;
  var step = 200;
  var vGap = hGap = 10;

  //var go2TopLeft = paper.rect(vInitx+3, vInitx, 20, 20).attr('fill', 'lightgreen').attr("title", localeString("js_goInit"))
  var go2TopLeft = paper.drawImage( G.app+'images/goRefresh.png', vInitx, vInity + 0*(goXxxH + vGap), 'w', 30).attr("title", localeString("js_goInit"));
   go2TopLeft.node.onclick = function() {
     location.assign(G.app+"/gedcom/forest")
   };
   go2TopLeft.node.onmouseover = function() {
     goDown.attr("title", localeString("js_goInit"));
   };

  var goUp = paper.drawImage( G.app+'images/goUp.png', vInitx + 1*(goXxxH + hGap), vInity  + 0*(goXxxH + vGap), 'w', 30);
  goUp.node.onclick = function() {
    paper.translteForest(0, 1*step);
    goUp.attr("title", localeString("js_goUp"));
  };
  goUp.node.onmouseover = function() {
    //paper.translteForest(0, 1*step);
    goUp.attr("title", localeString("js_goUp"));
  };

  var goRight = paper.drawImage( G.app+'images/goRight.png', vInitx, vInity + 1*(goXxxH + vGap), 'w', 30);   // go090.jpg
  goRight.node.onclick = function() {
    paper.translteForest(-1*step, 0);
    goRight.attr("title", localeString("js_goRight"));
  };
  goRight.node.onmouseover = function() {
    //paper.translteForest(-1*step, 0);
    goRight.attr("title", localeString("js_goRight"));
  };

  var goLeft = paper.drawImage( G.app+'images/goLeft.png', vInitx, vInity + 2*(goXxxH + vGap), 'w', 30);
  goLeft.node.onclick = function() {
    paper.translteForest(1*step, 0);
    goLeft.attr("title", localeString("js_goLeft"));
  };
  goLeft.node.onmouseover = function() {
    //paper.translteForest(1*step, 0);
    goLeft.attr("title", localeString("js_goLeft"));
  };

  var goDown = paper.drawImage( G.app+'images/goDown.png', vInitx + 2*(goXxxH + hGap), vInity + 0*(goXxxH + vGap), 'w', 30);
  goDown.node.onclick = function() {
    paper.translteForest(0, -1*step);
    goDown.attr("title", localeString("js_goDown"));
  };
  goDown.node.onmouseover = function() {
    //paper.translteForest(0, -1*step);
    goDown.attr("title", localeString("js_goDown"));
  };

 //logger('Logger Logger Logger Logger Logger  ')

 G.topRect.node.onclick = function(event) {   //alert("!!!!!!")
     // get bounding rect of the paper
     var bnds = event.target.getBoundingClientRect();
     // adjust mouse x/y
     var mx = event.clientX - bnds.left
     var my = event.clientY - bnds.top
     // divide x/y by the bounding w/h to get location %s and apply factor by actual paper w/h
     var fx = mx/bnds.width * G.topRect.attrs.width
     var fy = my/bnds.height * G.topRect.attrs.height
     // cleanup output
     fx = Number(fx).toPrecision(3);
     fy = Number(fy).toPrecision(3);
     paper.translteForest((scale*G.globWidth/2 - fx), (scale*G.globHeight/2 - fy));        // G.globWidth, G.globHeight
     //paper.translteForest(10, 10);        // G.globWidth, G.globHeight
     //alert('event.clientX: ' + event.clientX + ', event.clientY: ' + event.clientY + '; bnds.left: ' + bnds.left + ', bnds.top: ' + bnds.top + '; x: ' + fx + ', y: ' + fy);
 };

//window.onload = function (paper) { logD("window.onload"); }

paper.rect(scale*G.globWidth/2-8, scale*G.globHeight/2-8, 16, 16, 8).attr('fill', 'lightgreen').attr("title", localeString("js_canvas_center"))

//======================================================================================================================
// E115-3/vsh  The statement is necessary for editing functionality impelemtation
// http://stackoverflow.com/questions/15257059/how-do-i-get-an-event-in-raphaels-paper-coordinates
paper.setViewBox(0, 0, 10, 10, true);
/*============================================================*/

