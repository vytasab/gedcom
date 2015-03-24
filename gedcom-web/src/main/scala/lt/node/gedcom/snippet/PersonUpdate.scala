package lt.node.gedcom.snippet

import _root_.scala._
import _root_.scala.xml.Text

import _root_.net.liftweb._
import http._
import SHtml._
import js._
import JsCmds._
import common._
import _root_.net.liftweb.util.Helpers._

import http.{SessionVar, S, SHtml}


import _root_.bootstrap.liftweb.{ErrorXmlMsg, AccessControl, RequestedURL, CurrentUser}

import _root_.lt.node.gedcom._
import model._  //{Model, Audit, Person, PersonEvent, PersonAttrib, EventDetail, Family}
import _root_.lt.node.gedcom.util.{GedcomDateOptions,PeTags,PaTags,GedcomDate}

class PersonUpdate {

  val log = Logger("PersonUpdate");

  object personVar extends /*Request*/ SessionVar[Box[Person]](Empty)

  object eventVar extends SessionVar[Box[PersonEvent]](Empty)

  object attribVar extends SessionVar[Box[PersonAttrib]](Empty)

  object detailVar extends SessionVar[Box[EventDetail]](Empty)

  object dateSessVar extends SessionVar[Box[GedcomDate]](Empty)

  def listPePa = {

    if (!AccessControl.isAuthenticated_?()) S.redirectTo("/")

    RequestedURL(Full(S.referer.openOr("gedcom/personView")))

    var selectedPeTag: String = ""
    var selectedPaTag: String = ""

    def addPe(): Unit = {
      log.debug(<_>selectedPeTag={selectedPeTag}</_>.text);
      S.redirectTo(<_>/rest/person/{personVar.is.open_!.id}/event/{selectedPeTag}</_>.text)
    }

    def addPa(): Unit = {
      log.debug(<_>selectedPaTag={selectedPaTag}</_>.text);
      S.redirectTo(<_>/rest/person/{personVar.is.open_!.id}/attrib/{selectedPaTag}</_>.text)
    }

    "#pelist" #> FocusOnLoad(SHtml.select(/*("","-- events --")::*/PeTags.tags, Empty, {
      selectedPeTag = _
    }, "size" -> "13", "onchange" -> "selectWhenChanged(this)")) &
      "#submitPe" #> SHtml.submit("SavePeInvisible", addPe) &
      "#palist" #> SHtml.select(/*("", "-- attribs --")::*/ PaTags.tags, Empty, {
        selectedPaTag = _
      }, "size" -> "12", "onchange" -> "selectWhenChanged(this)") &
      "#submitPa" #> SHtml.submit("SavePaInvisible", addPa) &
      "#cancel" #> SHtml.link("/gedcom/personView",
        () => {/*S.redirectTo(RequestedURL.is.openOr("/"))*/}, Text(S ? "return"))

  }


 def addAlonePerson() = {
   S.unsetSessionAttribute("role")
   S.setSessionAttribute("role", "newAlone")
   log.debug("addAlonePerson: S.getSessionAttribute('role').openOr('')= " + S.getSessionAttribute("role").openOr("-negerai-"))
   addEdit()
 }


  def addEdit() = {
    RequestedURL(Full(S.referer.openOr("/")))
    log.debug("addEdit: S.referer= " + S.referer)
    log.debug("addEdit: S.getSessionAttribute('role').openOr('')= " + S.getSessionAttribute("role").openOr("-negerai-"))
    var person: Person = null;
    var personClone: Box[PersonClone] = Empty
    S.getSessionAttribute("role") match {
      case Full("upd") =>
        //val person: Person = Model.find(classOf[Person], S.getSessionAttribute("personId").get.toLong).get
        person = Model.find(classOf[Person], S.getSessionAttribute("personId").get.toLong).get
        assert (person != null, {log.error("addedit: No person for " + S.getSessionAttribute("personId").get)})
        personClone = Full(person.getClone/*.asInstanceOf[Person]*/)
        S.setSessionAttribute("aNameGivn", person.nameGivn)
        S.setSessionAttribute("aNameSurn", person.nameSurn)
        S.setSessionAttribute("aGender", person.gender)
        personVar.set(Full(person))
      case Full("newAlone") =>
        personVar.set(Empty) // it is too early for: new Person
        S.unsetSessionAttribute("aNameGivn")
        S.unsetSessionAttribute("aNameSurn")
        S.unsetSessionAttribute("aGender")
        S.unsetSessionAttribute("gender")
      case Full("pF")|Full("pM") |
           Full("sH")|Full("sW")|Full("sF") |
           Full("cB")|Full("cS") |
           Full("fSon")|Full("fDaughter") =>
        // p = parent, s = spouse, c = child, f = family
        //person = new Person
        personVar.set(Empty) // it is too early for: new Person
        S.setSessionAttribute("aNameGivn", "")
        S.setSessionAttribute("aNameSurn", "")
        S.setSessionAttribute("aGender", S.getSessionAttribute("gender").get)
      case _ =>
    }
//    var person: Person = null
//    //Nothing // = personVar.is.openOr(new Person)
    val mapGender = Map(S.?("male") -> "M", S.?("female") -> "F")
    var aNameGivn = S.getSessionAttribute("aNameGivn") openOr ""
    var aNameSurn = S.getSessionAttribute("aNameSurn") openOr ""
    var aGender = S.getSessionAttribute("aGender") openOr (S.getSessionAttribute("gender") openOr "")
    // Hold a val here so that the "id" closure holds it when we re-enter this method
    //var currentId = 0L

    def doAddEdit(): Unit = {
      log.debug("[doAddEdit]...")
      if (AccessControl.isAuthenticated_?) {// person.setSubmitter(CurrentUser.is.open_!)
        var validResult = true
        S.setSessionAttribute("aNameGivn", aNameGivn)
        S.setSessionAttribute("aNameSurn", aNameSurn)
        S.setSessionAttribute("aGender", aGender)
        if (!validate("givn", aNameGivn)) {
          validResult = false
          S.error("nameGivn_error", S.?("wrong.given.name"))
        }
        if (!validate("surn", aNameSurn)) {
          validResult = false
          S.error("nameSurn_error", S.?("wrong.surn.name"))
        }
        if (!validate("gender", aGender)) {
          validResult = false
          S.error("gender_error", S.?("wrong.gender"))
        }
        if (validResult) {
          S.getSessionAttribute("role") match {
            case Full("upd") =>
              person = personVar.is.openOr(new Person)
              // Hold a val here so that the "id" closure holds it when we re-enter this method
              //currentId = person.id
            case Full("pF")|Full("pM") |
                 Full("sH")|Full("sW")|Full("sF") |
                 Full("cB")|Full("cS") |
                 Full("fSon")|Full("fDaughter")|Full("fSpouse") =>
              person = new Person
            case Full("newAlone") =>
              person = new Person
            case _ =>
              val msg = ("doAddEdit: invalid 'role' = |" +  S.getSessionAttribute("role").openOr("no-role") + "|")
              log.debug(msg)
              S.redirectTo("/errorPage", () => {
                ErrorXmlMsg.set(Some(Map(
                  "location" -> <p>PersonUpdate.addEdit.doAddEdit</p>,
                  "message" -> <p>{msg}</p>))) })
            }
            person.nameGivn = aNameGivn
            person.nameSurn = aNameSurn
            person.gender = aGender
            person.setSubmitter(CurrentUser.is.open_!)
            person = Model.merge(person)
            var audit = new Audit
            audit.setFields(CurrentUser.is.open_!, "Pe", person.id,
              personClone match{case Full(x) => "upd"; case _ => "add";},
              person.getAuditRec(personClone))
            audit = Model.merge(audit)
            Model.flush
            S.notice(person.toString(Model.getUnderlying) + "  " + S.?("person.added.updated"))
            S.getSessionAttribute("role") match {
              case Full("upd") =>
              case Full("pF")|Full("pM") |
                   Full("sH")|Full("sW")|Full("sF") |
                   Full("cB")|Full("cS") |
                   Full("fSon")|Full("fDaughter") =>
                val aPersonSnips = new PersonSnips;
                aPersonSnips.completeQuery(person.id)
              case _ =>
                S.unsetSessionAttribute("aNameGivn")
                S.unsetSessionAttribute("aNameSurn")
                S.unsetSessionAttribute("aGender")
              }
          S.redirectTo(RequestedURL.is.openOr("/"))
          //S.redirectTo("/rest/person/" + person.id)
        } else {
          S.error(S.?("errors.in.form"))
          S.redirectTo("/gedcom/addeditPerson/")
        }
        log.debug("...[doAddEdit]")
      } else {
        val msg = ("addEdit:doAddEdit: You are not logged in")
        log.debug(msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map(
            "location" -> <p>PersonUpdate.addEdit.doAddEdit</p>,
            "message" -> <p>{msg}</p>)))
        })
      }
    }

    def validate(domain: String, text: String): Boolean = {
      domain match {
        case "givn" => aNameGivn.length > 0
        case "surn" => aNameSurn.length > 0
        case "gender" => aGender == "M" || aGender == "F"
        case _ => false
      }
    }

    val initGender4Radio: Box[String] = {
      log.debug("add: initGender4Radio S.getSessionAttribute(\"aGender\") " + S.getSessionAttribute("aGender").openOr("-negerai-"))
      log.debug("add: initGender4Radio mapGender " + mapGender.toString)
      S.getSessionAttribute("aGender") match {
        case Full(x) if x != "" && "MF".contains(x) =>
          val kvMap = mapGender map(_.swap)
          Full(kvMap(x).toString)
        case _ => Empty
      }
    }

    "#title" #> Text(S.getSessionAttribute("role") match {
        case Full("upd") => S ? "edit.person"
        case _ => S ? "add.person" }) &
      "#nameGivn" #> SHtml.text(aNameGivn, aNameGivn = _) &
      "#nameSurn" #> SHtml.text(aNameSurn, aNameSurn = _) &
      "#gender" #> (S.getSessionAttribute("role") match {
        case Full("upd") =>
          SHtml.radio(mapGender.keys.toList, initGender4Radio, {
          //"#gender" #> SHtml.radio(mapGender.keys.toList, S.getSessionAttribute("gender")/*Empty*/, {
          x: String => aGender = mapGender(x)}).toForm
        case Full("pF")|Full("pM") | Full("sH")|Full("sW") =>
          Text(S ? S.getSessionAttribute("gender").get)
        case Full("cB")|Full("cS") | Full("fSon")|Full("fDaughter") =>
          Text(S ? S.getSessionAttribute("gender").get)
        case _ =>
          SHtml.radio(mapGender.keys.toList, initGender4Radio, {
          //"#gender" #> SHtml.radio(mapGender.keys.toList, S.getSessionAttribute("gender")/*Empty*/, {
          x: String => aGender = mapGender(x)}).toForm
      }) &
      "#submit" #> SHtml.submit(S.?("submit"), doAddEdit) &
      "#cancel" #> SHtml.link("index", () => {
        S.unsetSessionAttribute("aNameGivn")
        S.unsetSessionAttribute("aNameSurn")
        S.unsetSessionAttribute("aGender")
        S.redirectTo(RequestedURL.is.openOr("/"))
      },
        Text(S.?("return")))
  }


  /*
     case Req(List("rest", "person", epId, "event", tag_or_id), _, GetRequest) => {
       // N.B. !!! -- tag CANNOT be numeral
       S.setSessionAttribute("personId", epId)
       tag_or_id.matches("\\d+") match {
         case true =>
           S.setSessionAttribute("eventId", tag_or_id)
           S.unsetSessionAttribute("eventTag")
         case _ =>
           S.setSessionAttribute("eventTag", tag_or_id)
           S.unsetSessionAttribute("eventId")
       }
       S.redirectTo("/gedcom/addeditPe")
     }
  */
  def addEvent() = {
    RequestedURL(Full(S.referer.openOr("/")))


////////////////////////////////////////////////////////////////////////////////////////////////////
//
//    def button(in: NodeSeq) =
//      ajaxButton(in,
//        () => S.runTemplate(List("gedcom","gedcomDate")).
//          map(ns => /*http.js.jquery.JqJsCmds*/ModalDialog(ns)) openOr
//          Alert("Couldn't find /gedcom/gedcomDate template"))
//    // the template needs to bind to either server-side behavior
//    // and unblock the UI
//
//    val thisYear = new java.text.SimpleDateFormat("yyyy").format(java.util.Calendar.getInstance.getTime)
//    val initYear = 1800
//    val yyyys: List[(String, String)] = GedcomUtil.yyyys  // "yyyy" :: List.range(initYear, (thisYear.toInt+1))map(i => (i.toString,i.toString))
//    var yyyy = ""
//    var yyyyMM = ""
//    var yyyyMMdd = thisYear.toString
//    val MMs: List[(String, String)] = GedcomUtil.MMs  /* "mm" :: List.range(1, 13)map(i => {
//      val t: String = "0" + i.toString
//      val n: String = t.substring(t.size-2)
//      (n,n)
//    })*/
//    var MM = ""
//    var MMdd = "1"
//    var dds: List[(String, String)] = GedcomUtil.dds  /*"dd" :: List.range(1, 31+1)map(i => {
//      val t: String = "0" + i.toString
//      val n: String = t.substring(t.size-2)
//      (n,n)
//    })*/
//    var dd = ""
//    var yyyyFrom = ""
//    var MMFrom = ""
//    var ddFrom = ""
//    var yyyyTo = ""
//    var MMTo = ""
//    var ddTo = ""
//    var yyyyBef = ""
//    var MMBef = ""
//    var ddBef = ""
//    var yyyyAft = ""
//    var MMAft = ""
//    var ddAft = ""
//    var yyyyBet = ""
//    var MMBet = ""
//    var ddBet = ""
//    var yyyyAnd = ""
//    var MMAnd = ""
//    var ddAnd = ""
//    var yyyyApx = ""
//    var MMApx = ""
//    var ddApx = ""
//    var datext = ""
//
//
//    def setDisplatStyle(option: String): String = setDisplatStyle2(option, "")
//
//    /**
//     * option: "gdt_exact" "gdt_from" "gdt_to" "gdt_from_to" "gdt_before" "gdt_after" "gdt_between" "gdt_about" "gdt_text"
//     * context: "from", "to", ""
//     */
//    def setDisplatStyle2(option: String, context: String): String = {
//      val res = S.getSessionAttribute("GedcomDateCase") match {
//        case Full(x) if x == option && context == "" => "display:yes"
//        case Full(x) if x == option && option == "gdt_from_to" && context == "gdt_from" => "display:yes"
//        case Full(x) if x == option && option == "gdt_from" && context == "gdt_from" => "display:yes"
//        case Full(x) if x == option && option == "gdt_from_to" && context == "gdt_to" => "display:yes"
//        case Full(x) if x == option && option == "gdt_to" && context == "gdt_to" => "display:yes"
//        case _ => "display:none"
//      }
//      //S.notice(option + " " + res + " " + S.getSessionAttribute("GedcomDateCase").toString)
//      res
//    }
//
//    var result = ""
//
//    def retFunc(result: String) = {
//              S.setSessionAttribute("GedcomDate", result)
//              log.debug("retFunc: confirm: submit is pressed to return " + result);
//              S.notice("--> " + result)
//  }
//
//    def confirm = {
//      // nerodo ir tai normalu: S.notice(S.getSessionAttribute("GedcomDateCase").toString)
//      log.debug("S.getSessionAttribute('GedcomDateCase') "+S.getSessionAttribute("GedcomDateCase").toString)
//      "#yyyy" #> ajaxSelect(yyyys, Empty, v => {yyyy = v; Noop}, "style" -> setDisplatStyle("gdt_exact") ) &
//      "#MM" #> ajaxSelect(MMs, Empty, v => {MM = v; Noop}, "style" -> setDisplatStyle("gdt_exact") ) &
//      "#dd" #> ajaxSelect(dds, Empty, v => {dd = v; Noop}, "style" -> setDisplatStyle("gdt_exact") ) &
//      //
//      "#from" #> <span style={setDisplatStyle2(S.getSessionAttribute("GedcomDateCase").get, "gdt_from")}>{S.?("gd_from")}</span> &
//      "#yyyyFrom" #> ajaxSelect(yyyys, Empty, v => {yyyyFrom = v; Noop}, "style" -> setDisplatStyle2(S.getSessionAttribute("GedcomDateCase").get, "gdt_from") ) &
//      "#MMFrom" #> ajaxSelect(MMs, Empty, v => {MMFrom = v; Noop}, "style" -> setDisplatStyle2(S.getSessionAttribute("GedcomDateCase").get, "gdt_from") ) &
//      "#ddFrom" #> ajaxSelect(dds, Empty, v => {ddFrom = v; Noop}, "style" -> setDisplatStyle2(S.getSessionAttribute("GedcomDateCase").get, "gdt_from") ) &
//      //
//      "#to" #> <span style={setDisplatStyle2(S.getSessionAttribute("GedcomDateCase").get, "gdt_to")}>{S.?("gd_to")}</span> &
//      "#yyyyTo" #> ajaxSelect(yyyys, Empty, v => {yyyyTo = v; Noop}, "style" -> setDisplatStyle2(S.getSessionAttribute("GedcomDateCase").get, "gdt_to") ) &
//      "#MMTo" #> ajaxSelect(MMs, Empty, v => {MMTo = v; Noop}, "style" -> setDisplatStyle2(S.getSessionAttribute("GedcomDateCase").get, "gdt_to") ) &
//      "#ddTo" #> ajaxSelect(dds, Empty, v => {ddTo = v; Noop}, "style" -> setDisplatStyle2(S.getSessionAttribute("GedcomDateCase").get, "gdt_to") ) &
//      //
//      "#bef" #> <span style={setDisplatStyle("gdt_before")}>{S.?("gd_bef")}</span> &
//      "#yyyyBef" #> ajaxSelect(yyyys, Empty, v => {yyyyBef = v; Noop}, "style" -> setDisplatStyle("gdt_before") ) &
//      "#MMBef" #> ajaxSelect(MMs, Empty, v => {MMBef = v; Noop}, "style" -> setDisplatStyle("gdt_before") ) &
//      "#ddBef" #> ajaxSelect(dds, Empty, v => {ddBef = v; Noop}, "style" -> setDisplatStyle("gdt_before") ) &
//      //
//      "#aft" #> <span style={setDisplatStyle("gdt_after")}>{S.?("gd_aft")}</span> &
//      "#yyyyAft" #> ajaxSelect(yyyys, Empty, v => {yyyyAft = v; Noop}, "style" -> setDisplatStyle("gdt_after") ) &
//      "#MMAft" #> ajaxSelect(MMs, Empty, v => {MMAft = v; Noop}, "style" -> setDisplatStyle("gdt_after") ) &
//      "#ddAft" #> ajaxSelect(dds, Empty, v => {ddAft = v; Noop}, "style" -> setDisplatStyle("gdt_after") ) &
//      //
//      "#bet" #> <span style={setDisplatStyle("gdt_between")}>{S.?("gd_bet")}</span> &
//      "#yyyyBet" #> ajaxSelect(yyyys, Empty, v => {yyyyBet = v; Noop}, "style" -> setDisplatStyle("gdt_between") ) &
//      "#MMBet" #> ajaxSelect(MMs, Empty, v => {MMBet = v; Noop}, "style" -> setDisplatStyle("gdt_between") ) &
//      "#ddBet" #> ajaxSelect(dds, Empty, v => {ddBet = v; Noop}, "style" -> setDisplatStyle("gdt_between") ) &
//      "#and" #> <span style={setDisplatStyle("gdt_between")}>{S.?("gd_and")}</span> &
//      "#yyyyAnd" #> ajaxSelect(yyyys, Empty, v => {yyyyAnd = v; Noop}, "style" -> setDisplatStyle("gdt_between") ) &
//      "#MMAnd" #> ajaxSelect(MMs, Empty, v => {MMAnd = v; Noop}, "style" -> setDisplatStyle("gdt_between") ) &
//      "#ddAnd" #> ajaxSelect(dds, Empty, v => {ddAnd = v; Noop}, "style" -> setDisplatStyle("gdt_between") ) &
//      //
//      "#apx" #> <span style={setDisplatStyle("gdt_about")}>{S.?("gd_abt")}</span> &
//      "#yyyyApx" #> ajaxSelect(yyyys, Empty, v => {yyyyApx = v; Noop}, "style" -> setDisplatStyle("gdt_about") ) &
//      "#MMApx" #> ajaxSelect(MMs, Empty, v => {MMApx = v; Noop}, "style" -> setDisplatStyle("gdt_about") ) &
//      "#ddApx" #> ajaxSelect(dds, Empty, v => {ddApx = v; Noop}, "style" -> setDisplatStyle("gdt_about") ) &
//      //
//      "#text" #> <span style={setDisplatStyle("gdt_txt")}>{S.?("gd_text")}</span> &
//      "#datext" #> ajaxText( datext, v => {datext = v; S.notice("datext = " + v); Noop}, "style" -> setDisplatStyle("gdt_text") ) &
//      //
//      "#submit" #> ((b: NodeSeq) => ajaxButton(b, () => {
//        val result: String = S.getSessionAttribute("GedcomDateCase") match {
//          case Full("gdt_exact") =>
//              (yyyy, MM, dd) match {
//                case (y,"","") if y != "" =>  <_>{y}</_>.text
//                case (y,m,"") if y != "" && m != ""  => <_>{y}-{m}</_>.text
//                case (y,m,d) if y != "" && m != "" && d != "" => <_>{y}-{m}-{d}</_>.text
//                case _ => ""
//              }
//              //retFunc(result)
//          case Full("gdt_from") =>
//              (yyyyFrom, MMFrom, ddFrom) match {
//                case (y,"","") if y != "" =>  <_>FROM {y}</_>.text
//                case (y,m,"") if y != "" && m != ""  => <_>FROM {y}-{m}</_>.text
//                case (y,m,d) if y != "" && m != "" && d != "" => <_>FROM {y}-{m}-{d}</_>.text
//                case _ => ""
//              }
//          case Full("gdt_to") =>
//              (yyyyTo, MMTo, ddTo) match {
//                case (y,"","") if y != "" =>  <_>TO {y}</_>.text
//                case (y,m,"") if y != "" && m != ""  => <_>TO {y}-{m}</_>.text
//                case (y,m,d) if y != "" && m != "" && d != "" => <_>TO {y}-{m}-{d}</_>.text
//                case _ => ""
//              }
//          case Full("gdt_from_to") =>
//            (yyyyFrom, MMFrom, ddFrom, yyyyTo, MMTo, ddTo) match {
//              case (y,"","",yy,"","") if y != "" &&                       yy != "" && y.toInt*10000 < yy.toInt*10000 =>
//                <_>FROM {y} TO {yy}</_>.text
//              case (y,m,"",yy,"","")  if y != "" && m != "" &&            yy != "" && y.toInt*10000+m.toInt*100 < yy.toInt*10000 =>
//                <_>FROM {y}-{m} TO {yy}</_>.text
//              case (y,m,d,yy,"","")   if y != "" && m != "" && d != "" && yy != "" && y.toInt*10000+m.toInt*100+d.toInt < yy.toInt*10000 =>
//                <_>FROM {y}-{m}-{d} TO {yy}</_>.text
//              case (y,"","",yy,mm,"") if y != "" &&                       yy != "" && mm != "" && y.toInt*10000 < yy.toInt*10000+mm.toInt*100 =>
//                <_>FROM {y} TO {yy}-{mm}-{dd}</_>.text
//              case (y,m,"",yy,mm,"")  if y != "" && m != "" &&            yy != "" && mm != "" && y.toInt*10000+m.toInt*100 < yy.toInt*10000+mm.toInt*100 =>
//                <_>FROM {y}-{m} TO {yy}-{mm}-{dd}</_>.text
//              case (y,m,d,yy,mm,"")   if y != "" && m != "" && d != "" && yy != "" && mm != "" && y.toInt*10000+m.toInt*100+d.toInt < yy.toInt*10000+mm.toInt*100 =>
//                <_>FROM {y}-{m}-{d} TO {yy}-{mm}</_>.text
//              case (y,"","",yy,mm,dd) if y != "" &&                       yy != "" && mm != "" && dd != "" && y.toInt*10000 < yy.toInt*10000+mm.toInt*100+dd.toInt =>
//                <_>FROM {y} TO {yy}-{mm}-{dd}</_>.text
//              case (y,m,"",yy,mm,dd)  if y != "" && m != "" &&            yy != "" && mm != "" && dd != "" && y.toInt*10000+m.toInt*100 < yy.toInt*10000+mm.toInt*100+dd.toInt =>
//                <_>FROM {y}-{m} TO {yy}-{mm}-{dd}</_>.text
//              case (y,m,d,yy,mm,dd)   if y != "" && m != "" && d != "" && yy != "" && mm != "" && dd != "" && y.toInt*10000+m.toInt*100+d.toInt < yy.toInt*10000+mm.toInt*100+dd.toInt =>
//                <_>FROM {y}-{m}-{d} TO {yy}-{mm}-{dd}</_>.text
//              case _ => ""
//            }
//          case Full("gdt_before") =>
//            (yyyyBef, MMBef, ddBef) match {
//              case (y,"","") if y != "" =>  <_>BEF {y}</_>.text
//              case (y,m,"") if y != "" && m != ""  => <_>BEF {y}-{m}</_>.text
//              case (y,m,d) if y != "" && m != "" && d != "" => <_>BEF {y}-{m}-{d}</_>.text
//              case _ => ""
//            }
//          case Full("gdt_after") =>
//            (yyyyAft, MMAft, ddAft) match {
//              case (y,"","") if y != "" =>  <_>AFT {y}</_>.text
//              case (y,m,"") if y != "" && m != ""  => <_>AFT {y}-{m}</_>.text
//              case (y,m,d) if y != "" && m != "" && d != "" => <_>AFT {y}-{m}-{d}</_>.text
//              case _ => ""
//            }
//          case Full("gdt_between") =>
//            (yyyyBet, MMBet, ddBet, yyyyAnd, MMAnd, ddAnd) match {
//              case (y,"","",yy,"","") if y != "" &&                       yy != "" && y.toInt*10000 < yy.toInt*10000 =>
//                <_>BET {y} AND {yy}</_>.text
//              case (y,m,"",yy,"","")  if y != "" && m != "" &&            yy != "" && y.toInt*10000+m.toInt*100 < yy.toInt*10000 =>
//                <_>BET {y}-{m} AND {yy}</_>.text
//              case (y,m,d,yy,"","")   if y != "" && m != "" && d != "" && yy != "" && y.toInt*10000+m.toInt*100+d.toInt < yy.toInt*10000 =>
//                <_>BET {y}-{m}-{d} AND {yy}</_>.text
//              case (y,"","",yy,mm,"") if y != "" &&                       yy != "" && mm != "" && y.toInt*10000 < yy.toInt*10000+mm.toInt*100 =>
//                <_>BET {y} AND {yy}-{mm}-{dd}</_>.text
//              case (y,m,"",yy,mm,"")  if y != "" && m != "" &&            yy != "" && mm != "" && y.toInt*10000+m.toInt*100 < yy.toInt*10000+mm.toInt*100 =>
//                <_>BET {y}-{m} AND {yy}-{mm}-{dd}</_>.text
//              case (y,m,d,yy,mm,"")   if y != "" && m != "" && d != "" && yy != "" && mm != "" && y.toInt*10000+m.toInt*100+d.toInt < yy.toInt*10000+mm.toInt*100 =>
//                <_>BET {y}-{m}-{d} AND {yy}-{mm}</_>.text
//              case (y,"","",yy,mm,dd) if y != "" &&                       yy != "" && mm != "" && dd != "" && y.toInt*10000 < yy.toInt*10000+mm.toInt*100+dd.toInt =>
//                <_>BET {y} AND {yy}-{mm}-{dd}</_>.text
//              case (y,m,"",yy,mm,dd)  if y != "" && m != "" &&            yy != "" && mm != "" && dd != "" && y.toInt*10000+m.toInt*100 < yy.toInt*10000+mm.toInt*100+dd.toInt =>
//                <_>BET {y}-{m} AND {yy}-{mm}-{dd}</_>.text
//              case (y,m,d,yy,mm,dd)   if y != "" && m != "" && d != "" && yy != "" && mm != "" && dd != "" && y.toInt*10000+m.toInt*100+d.toInt < yy.toInt*10000+mm.toInt*100+dd.toInt =>
//                <_>BET {y}-{m}-{d} AND {yy}-{mm}-{dd}</_>.text
//              case _ => ""
//            }
//          case Full("gdt_about") =>
//            (yyyyApx, MMApx, ddApx) match {
//              case (y,"","") if y != "" =>  <_>ABT {y}</_>.text
//              case (y,m,"") if y != "" && m != ""  => <_>ABT {y}-{m}</_>.text
//              case (y,m,d) if y != "" && m != "" && d != "" => <_>ABT {y}-{m}-{d}</_>.text
//              case _ => ""
//            }
//          case Full("gdt_text") => datext
//          case Empty => ""
//          case _ => ""
//        }
//        log.debug("confirm: submit is pressed to return result = |" + result +"|");
//        result match {
//          case r if r.size > 0 => {
//            S.setSessionAttribute("GedcomDate", result)
//            S.notice("--> " + result)
//            Unblock //& Alert("--> " + result)
//          }
//          case _ => {
//            S.setSessionAttribute("GedcomDate", result)
//            S.error(S.?("date.is.invalid"))
//            Noop
//          }
//        }
//        //Unblock //& Alert("--> " + result)
//        })) &
//      "#return" #> ((b: NodeSeq) => <button onclick={Unblock.toJsCmd}>{b}</button>)
//
//    }
//
////////////////////////////////////////////////////////////////////////////////////////////////////


    log.debug("addEvent: S.referer= " + S.referer)
    var aPerson: Person = /*personVar.is.openOr(new Person)*/ new Person
    var aPE: PersonEvent = /*eventVar.is.openOr(new PersonEvent)*/ new PersonEvent
    var aPE_former: PersonEvent = aPE
    var aED: EventDetail = /*detailVar.is.openOr(new EventDetail)*/ new EventDetail
    var aED_former: EventDetail = aED
    //log.debug("addEvent: S.getSessionAttribute('role').openOr('')= " + S.getSessionAttribute("role").openOr("-negerai-"))
    val person: Person = Model.find(classOf[Person], S.getSessionAttribute("personId").get.toLong).get
    personVar.set(Full(person))
    S.getSessionAttribute("eventTag") match {
      case Full(eventTag) =>
        log.debug("addEvent: eventTag = " + eventTag)
        val pe = new PersonEvent
        pe.tag = eventTag
        eventVar(Full(pe))
        detailVar(Full(new EventDetail))
      case _ =>
        S.getSessionAttribute("eventId") match {
          case Full(eventId) =>
            log.debug("addEvent: eventId = " + eventId)
            Model.find(classOf[PersonEvent], S.getSessionAttribute("eventId").get.toLong) match {
              case Some(pe) =>
                eventVar(Full(pe))
                pe.getEventDetail(Model.getUnderlying)
                pe.eventdetails.size match {
                  case 1 =>
                    detailVar(Full(pe.eventdetails.iterator.next))
                  case n: Int =>
                    val msg = ("addEvent: No EventDetail for PersonEvent = " + eventId)
                    log.debug(msg)
                    S.redirectTo("/errorPage", () => {
                      ErrorXmlMsg.set(Some(Map(
                        "location" -> <p>PersonUpdate.addEvent</p>,
                        "message" -> <p>{msg}</p>)))
                    })
                }
              case _ =>
                val msg = ("addEvent: No PersonEvent for person = " + person.id.toString)
                log.debug(msg)
                S.redirectTo("/errorPage", () => {
                  ErrorXmlMsg.set(Some(Map(
                    "location" -> <p>PersonUpdate.addEvent</p>,
                    "message" -> <p>{msg}</p>)))
                })
            }
            personVar.set(Full(person))
          case _ =>
            val msg = ("addEvent: No Session Attributes: 'eventTag' and 'eventId'")
            log.debug(msg)
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map(
                "location" -> <p>PersonUpdate.addEvent</p>,
                "message" -> <p>{msg}</p>)))
            })
        }
    }
    //    var person: Person = null
    //    //Nothing // = personVar.is.openOr(new Person)
    //    val mapGender = Map(S.?("male") -> "M", S.?("female") -> "F")
    //    var aNameGivn = S.getSessionAttribute("aNameGivn") openOr ""
    //    var aNameSurn = S.getSessionAttribute("aNameSurn") openOr ""
    //    var aGender = S.getSessionAttribute("aGender") openOr (S.getSessionAttribute("gender") openOr "")
    //    // Hold a val here so that the "id" closure holds it when we re-enter this method
    //    //var currentId = 0L

    aPE_former = eventVar.is.openOr(new PersonEvent)
    aED_former = detailVar.is.openOr(new EventDetail)

    def perform(): Unit = {
      log.debug("[perform]...")
      aPerson = personVar.is.openOr(new Person)
      var validResult = true
      //S.setSessionAttribute("aNameGivn", aNameGivn)
      //S.setSessionAttribute("aNameSurn", aNameSurn)
      //S.setSessionAttribute("aGender", aGender)
      //if (!validate("givn", aNameGivn)) {
      //  validResult = false
      //  S.error("nameGivn_error", S.?("wrong.given.name"))
      //}
      //if (!validate("surn", aNameSurn)) {
      //  validResult = false
      //  S.error("nameSurn_error", S.?("wrong.surn.name"))
      //}
      //if (!validate("gender", aGender)) {
      //  validResult = false
      //  S.error("gender_error", S.?("wrong.gender"))
      //}
      if (validResult) {
        aPE = eventVar.is.openOr(new PersonEvent)
        aED = detailVar.is.openOr(new EventDetail)
        //        // Hold a val here so that the "id" closure holds it when we re-enter this method
        //        //currentId = person.id
        //        person.nameGivn = aNameGivn
        //        person.nameSurn = aNameSurn
        //        person.gender = aGender

        if (AccessControl.isAuthenticated_?) {
          aPE.setSubmitter(CurrentUser.is.open_!)
          aED.setSubmitter(CurrentUser.is.open_!)
        }

        aPE = Model.mergeAndFlush(aPE)
        aED = Model.mergeAndFlush(aED)
        /*S.notice(person.toString(Model.getUnderlying) + "  " + S.?("person.added.updated"))
        S.unsetSessionAttribute("aNameGivn")
        S.unsetSessionAttribute("aNameSurn")
        S.unsetSessionAttribute("aGender")
        (S.getSessionAttribute("personId").isDefined ||
          S.getSessionAttribute("familyId").isDefined ||
          S.getSessionAttribute("gender").isDefined ||
          S.getSessionAttribute("role").isDefined) match {
          case true =>
            completeQuery(person.id)
          case _ => // for unrelated add action
            S.redirectTo("/rest/person/" + person.id)
        }*/
        //if (S.getSessionAttribute("personId").isDefined) S.unsetSessionAttribute("personId")
        S.unsetSessionAttribute("testastestas")
        S.unsetSessionAttribute("personId")
        S.unsetSessionAttribute("eventTag")
        S.unsetSessionAttribute("eventId")
        S.redirectTo(RequestedURL.is.openOr("/"))
        //S.redirectTo("/rest/person/" + person.id)
      } else {
        S.error(S.?("errors.in.form"))
        S.redirectTo("/gedcom/addeditPerson/")
      }
      log.debug("...[doAdd]")
    }

    def validate(domain: String, text: String): Boolean = {
      domain match {
      //        case "givn" => aNameGivn.length > 0
      //        case "surn" => aNameSurn.length > 0
      //        case "gender" => aGender == "M" || aGender == "F"
        case _ => false
      }
    }

    //    val initGender4Radio: Box[String] = {
    //      S.getSessionAttribute("aGender") match {
    //        case Full(x) if "MF".contains(x) =>
    //          val kvMap = mapGender map {
    //            _.swap
    //          }
    //          Full(kvMap(x).toString)
    //        //Full((mapGender.map{_.swap})(x))
    //        case _ => Empty
    //      }
    //    }

    val tag: String = eventVar.is.get.tag
    val attr4AdoptedBy: String = tag match {
      case "ADOP" => "display:yes"
      case "BIRT" => "display:yes"
      case _ => "display:none"
    }
    val attr4Descriptor: String = tag match {
      case "EVEN" => "display:yes"
      case _ => "display:none"
    }
    val attr4Cause: String = tag match {
      case "DEAT" => "display:yes"
      case _ => "display:none"
    }
    val adoptedByOptions = Map("HUSB" -> S.?("HUSB"), "WIFE" -> S.?("WIFE"), "BOTH" -> S.?("BOTH"))

// TODO start solving L12 of textual fields
    var adoptedBy = ""
    var descriptor = ""
    var dateValue = ""
    var place = ""
    var ageAtEvent = ""
    var cause = ""
    var source = ""

    "#petag" #> <span>{tag}</span> &
      "#adoptedBy_" #> <span style={attr4AdoptedBy}>{S ? "pe.adoptedBy"}</span> &
      "#adoptedBy" #> <span style={attr4AdoptedBy}>{SHtml.radio(adoptedByOptions.keys.toList, Empty, {
        (x) => adoptedBy = adoptedByOptions(x)
      }).toForm}</span> &
      "#descriptor_" #> <span style={attr4Descriptor}>{S ? "pe.descriptor"}</span> &
      "#descriptor" #> SHtml.text(descriptor, descriptor = _, "style" -> attr4Descriptor,
        "size" -> "60", "maxlength" -> "90") &
      //
      "#dateoptions" #> ajaxSelect(GedcomDateOptions.tags.filter( _._1 != "gdt_and"), Empty,
        v => {
          S.notice(v);
//          JsRaw("alert('---------------------------------------')")
//          JsRaw(gedcomJsData + "\n" + "G.loggedIn=" + AccessControl.isAuthenticated_? + ";" +
//            "G.rootId=" + rootId + ";" + GedcomRest.getLocaleStrings() +
//            (if (sbIdGen.toString.length > 0) countGenerationSize(sbIdGen.toString) else ""))
//          ModalGedcomDate.button(<b>{S.?("gedcom.date")}</b>)
          S.setSessionAttribute("GedcomDateCase", v);
          Noop
        },
        "size" -> "9"/*, "onchange" -> "selectWhenSelected()"*/) &
      "#dateValue" #> ModalGedcomDate.button(<b>{S.?("gedcom.date")}</b>) &
//    val mgd = ModalGedcomDate()
//      "#dateoptions" #> FocusOnLoad(SHtml.select(GedcomDateOptions.tags, Empty, {
//        S.notice(_)
//      },
//      "size" -> "9", "onchange" -> "selectWhenChanged(this)")) &
      //
      "#place" #> SHtml.text(place, place = _,
        "size" -> "60", "maxlength" -> "120") &
      /*"#place" #> SHtml.textarea(place, place = _, "cols" -> "60", "rows" -> "2") &*/
      "#ageAtEvent" #> SHtml.text(ageAtEvent, ageAtEvent = _,
        "size" -> "60", "maxlength" -> "12") &
      "#cause_" #> <span style={attr4Cause}>{S ? "cause"}</span> &
      "#cause" #> SHtml.text(cause, cause = _,
        "style" -> attr4Cause, "size" -> "60", "maxlength" -> "90") &
      "#source" #> SHtml.text(source, source = _, "size" -> "60", "maxlength" -> "248") &
      /*"#source" #> SHtml.textarea(source, source = _, "cols" -> "60", "rows" -> "4") &*/
      "#submit" #> SHtml.submit(S.?("submit"), perform) &
      "#cancel" #> SHtml.link("index", () => {
        //S.unsetSessionAttribute("aNameGivn")
        //S.unsetSessionAttribute("aNameSurn")
        //S.unsetSessionAttribute("aGender")
        S.redirectTo(RequestedURL.is.openOr("/"))
      },
        Text(S.?("return")))
// TODO implement AgeatEvent
    /*
    AGE_AT_EVENT: = {Size=1:12}
    [ < | > | <NULL>]
    [ YYy MMm DDDd | YYy | MMm | DDDd |
    YYy MMm | YYy DDDd | MMm DDDd |
    CHILD | INFANT | STILLBORN ]
    ]
    Where :
    > = greater than indicated age
    < = less than indicated age
    y = a label indicating years
    m = a label indicating months
    d = a label indicating days
    YY = number of full years
    MM = number of months
    DDD = number of days
    CHILD = age < 8 years
    INFANT = age < 1 year
    STILLBORN = died just prior, at, or near birth, 0 years
    */

  }

  //
  //
  //  def button(in: NodeSeq) =
  //    ajaxButton(in,
  //      () => {log.debug("addEvent modal button's name =" + in)
  //        S.runTemplate(List("gedcom","gedcomDate")).
  //        map(ns => ModalDialog(ns)) openOr
  //        Alert("Couldn't find /gedcom/gedcomDate template")
  //      })
  //  var name = "[vardas]"
  //  def confirm =
  //    "#name" #> ajaxText(name, { n => println("name = " + n)
  //        Noop }) &
  //      "#confirm_yes" #> ((b: NodeSeq) => ajaxButton(b, () => {
  //        println("Rhode Island Destroyed")
  //        Unblock & Alert("Rhode Island Destroyed")
  //      })) &
  //      "#confirm_no" #> ((b: NodeSeq) => <button onclick={Unblock.toJsCmd}>{b}</button>)
  //  //  def confirm(in: NodeSeq) =
  //  //    bind("confirm", in,
  //  //      "yes" -> ((b: NodeSeq) => ajaxButton(b, () => {
  //  //        println("Rhode Island Destroyed")
  //  //        Unblock & Alert("Rhode Island Destroyed")
  //  //      })),
  //  //      "no" -> ((b: NodeSeq) => <button onclick={Unblock.toJsCmd}>{b}</button>))


  val xsl4Update =
"""<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>
    <xsl:param name="app" select="/" />
    <xsl:template match="/">
        <!--<table>-->
        <!--<tr> <td></td><td></td><td></td><td></td> </tr>-->
        <tr>
        <td><a>
            <xsl:attribute name="href">
                <!--<xsl:value-of select="concat('/gedcom-web/gedcom/personUpdate/',//root/person/@id)" />-->
                <xsl:value-of select="concat($app,'gedcom/personUpdate/',//root/person/@id)" />
            </xsl:attribute>
            <!--<img src="/gedcom-web/images/page_edit.gif"/>-->
            <img>
                <xsl:attribute name="src">
                    <xsl:value-of select="concat($app,'images/page_edit.gif')" />
                </xsl:attribute>
            </img>
            <!--<b><xsl:value-of select="concat(//root/person/nameGivn,' ',//root/person/nameSurn,' ')"/>
            <xsl:apply-templates select="//root/person/gender" />
            </b>-->
        </a>
        </td>
        <td colspan="3">
            <b><xsl:value-of select="concat(//root/person/nameGivn,' ',//root/person/nameSurn,' ')"/>
            <xsl:apply-templates select="//root/person/gender" /></b>
        </td>
        </tr>
        <!--</table>-->
        <xsl:apply-templates select="families" />
    </xsl:template>
    <xsl:template match="families">
        <xsl:apply-templates select="family" />
    </xsl:template>
    <xsl:template match="family">
        <xsl:apply-templates select="spouse/person" />
        <span style="font-size:smaller">
            <xsl:apply-templates select="child/person" />
        </span>
    </xsl:template>
    <xsl:template match="person">
        <span>
            <xsl:attribute name="class">
                <xsl:value-of select="concat(gender,'-style')" />
            </xsl:attribute>
        <a>
            <xsl:attribute name="href">
                <!--<xsl:value-of select="concat('/gedcom-web/rest/personView/',./@id)" />-->
                <xsl:value-of select="concat($app,'rest/personView/',./@id)" />
            </xsl:attribute>
            <span>
                <b><xsl:value-of select="concat(nameGivn,' ',nameSurn,' ')"/>
                <xsl:apply-templates select="gender" />
                </b>
            </span>
        </a>
        </span>
    </xsl:template>
      <!-- \u2640  ♀ Venus;  \u2642  ♂ Mars  -->
    <xsl:template match="gender">
        <xsl:choose>
            <xsl:when test=".='M'">♂</xsl:when>
            <xsl:otherwise>♀</xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>"""


}
