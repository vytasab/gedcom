package lt.node.gedcom.snippet

import org.slf4j.{LoggerFactory, Logger}

import _root_.net.liftweb._
import common._
import http._
import SHtml._
import js._
import JsCmds._
import js.jquery._
import JqJsCmds._
import util._
import Helpers._

import _root_.scala.xml.NodeSeq

//import _root_.lt.node.gedcom.util.{GedcomDate}

// google-gr: Lift [Converting to CSS-based binding]

///*object*/ class ModalGedcomDate/*(gedcomDate: Box[GedcomDate])*/ extends Loggable {
/*class*/
object ModalGedcomDate extends Loggable {

  val log: Logger = LoggerFactory.getLogger("ModalGedcomDate");

//  log.debug("ModalGedcomDate gedcomDate = " + gedcomDate.toString)

  def button(in: NodeSeq) =
    ajaxButton(in,
      () => S.runTemplate(List("gedcom","gedcomDate")).
        map(ns => /*http.js.jquery.JqJsCmds*/ModalDialog(ns)) openOr
        Alert("Couldn't find /gedcom/gedcomDate template"))
  // the template needs to bind to either server-side behavior
  // and unblock the UI

  val thisYear = new java.text.SimpleDateFormat("yyyy").format(java.util.Calendar.getInstance.getTime)
  val initYear = 1800
  val yyyys: List[(String, String)] = "yyyy" :: List.range(initYear, (thisYear.toInt+1))map(i => (i.toString,i.toString))
  var yyyy = ""
  var yyyyMM = ""
  var yyyyMMdd = thisYear.toString
  val MMs: List[(String, String)] = "mm" :: List.range(1, 13)map(i => {
    val t = "0" + i.toString
    val n = t.substring(t.size-2)
    (n,n)
  })
  var MM = ""
  var MMdd = "1"
  var dds: List[(String, String)] = "dd" :: List.range(1, 31+1)map(i => {
    val t = "0" + i.toString
    val n = t.substring(t.size-2)
    (n,n)
  })
  var dd = ""
  var yyyyFrom = ""
  var MMFrom = ""
  var ddFrom = ""
  var yyyyTo = ""
  var MMTo = ""
  var ddTo = ""
  var yyyyBef = ""
  var MMBef = ""
  var ddBef = ""
  var yyyyAft = ""
  var MMAft = ""
  var ddAft = ""
  var yyyyBet = ""
  var MMBet = ""
  var ddBet = ""
  var yyyyAnd = ""
  var MMAnd = ""
  var ddAnd = ""
  var yyyyApx = ""
  var MMApx = ""
  var ddApx = ""
  var datext = ""


  def setDisplatStyle(option: String): String = setDisplatStyle(option, "")

  /**
   * option: "gdt_exact" "gdt_from" "gdt_to" "gdt_from_to" "gdt_before" "gdt_after" "gdt_between" "gdt_about" "gdt_text"
   * context: "from", "to", ""
   */
  def setDisplatStyle(option: String, context: String): String = {
    val res = S.getSessionAttribute("GedcomDateCase") match {
      case Full(x) if x == option && context == "" => "display:yes"
      case Full(x) if x == option && option == "gdt_from_to" && context == "gdt_from" => "display:yes"
      case Full(x) if x == option && option == "gdt_from" && context == "gdt_from" => "display:yes"
      case Full(x) if x == option && option == "gdt_from_to" && context == "gdt_to" => "display:yes"
      case Full(x) if x == option && option == "gdt_to" && context == "gdt_to" => "display:yes"
      case _ => "display:none"
    }
    //S.notice(option + " " + res + " " + S.getSessionAttribute("GedcomDateCase").toString)
    res
  }

  var result = ""

  def retFunc(result: String) = {
            S.setSessionAttribute("GedcomDate", result)
            log.debug("retFunc: confirm: submit is pressed to return " + result);
            S.notice("--> " + result)
}

  def confirm = {
    // nerodo ir tai normalu: S.notice(S.getSessionAttribute("GedcomDateCase").toString)
    log.debug("S.getSessionAttribute('GedcomDateCase') "+S.getSessionAttribute("GedcomDateCase").toString)
    "#yyyy" #> ajaxSelect(yyyys, Empty, v => {yyyy = v; Noop}, "style" -> setDisplatStyle("gdt_exact") ) &
    "#MM" #> ajaxSelect(MMs, Empty, v => {MM = v; Noop}, "style" -> setDisplatStyle("gdt_exact") ) &
    "#dd" #> ajaxSelect(dds, Empty, v => {dd = v; Noop}, "style" -> setDisplatStyle("gdt_exact") ) &
    //
    "#from" #> <span style={setDisplatStyle(S.getSessionAttribute("GedcomDateCase").get, "gdt_from")}>{S.?("gd_from")}</span> &
    "#yyyyFrom" #> ajaxSelect(yyyys, Empty, v => {yyyyFrom = v; Noop}, "style" -> setDisplatStyle(S.getSessionAttribute("GedcomDateCase").get, "gdt_from") ) &
    "#MMFrom" #> ajaxSelect(MMs, Empty, v => {MMFrom = v; Noop}, "style" -> setDisplatStyle(S.getSessionAttribute("GedcomDateCase").get, "gdt_from") ) &
    "#ddFrom" #> ajaxSelect(dds, Empty, v => {ddFrom = v; Noop}, "style" -> setDisplatStyle(S.getSessionAttribute("GedcomDateCase").get, "gdt_from") ) &
    //
    "#to" #> <span style={setDisplatStyle(S.getSessionAttribute("GedcomDateCase").get, "gdt_to")}>{S.?("gd_to")}</span> &
    "#yyyyTo" #> ajaxSelect(yyyys, Empty, v => {yyyyTo = v; Noop}, "style" -> setDisplatStyle(S.getSessionAttribute("GedcomDateCase").get, "gdt_to") ) &
    "#MMTo" #> ajaxSelect(MMs, Empty, v => {MMTo = v; Noop}, "style" -> setDisplatStyle(S.getSessionAttribute("GedcomDateCase").get, "gdt_to") ) &
    "#ddTo" #> ajaxSelect(dds, Empty, v => {ddTo = v; Noop}, "style" -> setDisplatStyle(S.getSessionAttribute("GedcomDateCase").get, "gdt_to") ) &
    //
    "#bef" #> <span style={setDisplatStyle("gdt_before")}>{S.?("gd_bef")}</span> &
    "#yyyyBef" #> ajaxSelect(yyyys, Empty, v => {yyyyBef = v; Noop}, "style" -> setDisplatStyle("gdt_before") ) &
    "#MMBef" #> ajaxSelect(MMs, Empty, v => {MMBef = v; Noop}, "style" -> setDisplatStyle("gdt_before") ) &
    "#ddBef" #> ajaxSelect(dds, Empty, v => {ddBef = v; Noop}, "style" -> setDisplatStyle("gdt_before") ) &
    //
    "#aft" #> <span style={setDisplatStyle("gdt_after")}>{S.?("gd_aft")}</span> &
    "#yyyyAft" #> ajaxSelect(yyyys, Empty, v => {yyyyAft = v; Noop}, "style" -> setDisplatStyle("gdt_after") ) &
    "#MMAft" #> ajaxSelect(MMs, Empty, v => {MMAft = v; Noop}, "style" -> setDisplatStyle("gdt_after") ) &
    "#ddAft" #> ajaxSelect(dds, Empty, v => {ddAft = v; Noop}, "style" -> setDisplatStyle("gdt_after") ) &
    //
    "#bet" #> <span style={setDisplatStyle("gdt_between")}>{S.?("gd_bet")}</span> &
    "#yyyyBet" #> ajaxSelect(yyyys, Empty, v => {yyyyBet = v; Noop}, "style" -> setDisplatStyle("gdt_between") ) &
    "#MMBet" #> ajaxSelect(MMs, Empty, v => {MMBet = v; Noop}, "style" -> setDisplatStyle("gdt_between") ) &
    "#ddBet" #> ajaxSelect(dds, Empty, v => {ddBet = v; Noop}, "style" -> setDisplatStyle("gdt_between") ) &
    "#and" #> <span style={setDisplatStyle("gdt_between")}>{S.?("gd_and")}</span> &
    "#yyyyAnd" #> ajaxSelect(yyyys, Empty, v => {yyyyAnd = v; Noop}, "style" -> setDisplatStyle("gdt_between") ) &
    "#MMAnd" #> ajaxSelect(MMs, Empty, v => {MMAnd = v; Noop}, "style" -> setDisplatStyle("gdt_between") ) &
    "#ddAnd" #> ajaxSelect(dds, Empty, v => {ddAnd = v; Noop}, "style" -> setDisplatStyle("gdt_between") ) &
    //
    "#apx" #> <span style={setDisplatStyle("gdt_about")}>{S.?("gd_abt")}</span> &
    "#yyyyApx" #> ajaxSelect(yyyys, Empty, v => {yyyyApx = v; Noop}, "style" -> setDisplatStyle("gdt_about") ) &
    "#MMApx" #> ajaxSelect(MMs, Empty, v => {MMApx = v; Noop}, "style" -> setDisplatStyle("gdt_about") ) &
    "#ddApx" #> ajaxSelect(dds, Empty, v => {ddApx = v; Noop}, "style" -> setDisplatStyle("gdt_about") ) &
    //
    "#text" #> <span style={setDisplatStyle("gdt_txt")}>{S.?("gd_text")}</span> &
    "#datext" #> ajaxText( datext, v => {datext = v; S.notice("datext = " + v); Noop}, "style" -> setDisplatStyle("gdt_text") ) &
    //
    "#submit" #> ((b: NodeSeq) => ajaxButton(b, () => {
      val result: String = S.getSessionAttribute("GedcomDateCase") match {
        case Full("gdt_exact") =>
            (yyyy, MM, dd) match {
              case (y,"","") if y != "" =>  <_>{y}</_>.text
              case (y,m,"") if y != "" && m != ""  => <_>{y}-{m}</_>.text
              case (y,m,d) if y != "" && m != "" && d != "" => <_>{y}-{m}-{d}</_>.text
              case _ => ""
            }
            //retFunc(result)
        case Full("gdt_from") =>
            (yyyyFrom, MMFrom, ddFrom) match {
              case (y,"","") if y != "" =>  <_>FROM {y}</_>.text
              case (y,m,"") if y != "" && m != ""  => <_>FROM {y}-{m}</_>.text
              case (y,m,d) if y != "" && m != "" && d != "" => <_>FROM {y}-{m}-{d}</_>.text
              case _ => ""
            }
        case Full("gdt_to") =>
            (yyyyTo, MMTo, ddTo) match {
              case (y,"","") if y != "" =>  <_>TO {y}</_>.text
              case (y,m,"") if y != "" && m != ""  => <_>TO {y}-{m}</_>.text
              case (y,m,d) if y != "" && m != "" && d != "" => <_>TO {y}-{m}-{d}</_>.text
              case _ => ""
            }
        case Full("gdt_from_to") =>
          (yyyyFrom, MMFrom, ddFrom, yyyyTo, MMTo, ddTo) match {
            case (y,"","",yy,"","") if y != "" &&                       yy != "" && y.toInt*10000 < yy.toInt*10000 =>
              <_>FROM {y} TO {yy}</_>.text
            case (y,m,"",yy,"","")  if y != "" && m != "" &&            yy != "" && y.toInt*10000+m.toInt*100 < yy.toInt*10000 =>
              <_>FROM {y}-{m} TO {yy}</_>.text
            case (y,m,d,yy,"","")   if y != "" && m != "" && d != "" && yy != "" && y.toInt*10000+m.toInt*100+d.toInt < yy.toInt*10000 =>
              <_>FROM {y}-{m}-{d} TO {yy}</_>.text
            case (y,"","",yy,mm,"") if y != "" &&                       yy != "" && mm != "" && y.toInt*10000 < yy.toInt*10000+mm.toInt*100 =>
              <_>FROM {y} TO {yy}-{mm}-{dd}</_>.text
            case (y,m,"",yy,mm,"")  if y != "" && m != "" &&            yy != "" && mm != "" && y.toInt*10000+m.toInt*100 < yy.toInt*10000+mm.toInt*100 =>
              <_>FROM {y}-{m} TO {yy}-{mm}-{dd}</_>.text
            case (y,m,d,yy,mm,"")   if y != "" && m != "" && d != "" && yy != "" && mm != "" && y.toInt*10000+m.toInt*100+d.toInt < yy.toInt*10000+mm.toInt*100 =>
              <_>FROM {y}-{m}-{d} TO {yy}-{mm}</_>.text
            case (y,"","",yy,mm,dd) if y != "" &&                       yy != "" && mm != "" && dd != "" && y.toInt*10000 < yy.toInt*10000+mm.toInt*100+dd.toInt =>
              <_>FROM {y} TO {yy}-{mm}-{dd}</_>.text
            case (y,m,"",yy,mm,dd)  if y != "" && m != "" &&            yy != "" && mm != "" && dd != "" && y.toInt*10000+m.toInt*100 < yy.toInt*10000+mm.toInt*100+dd.toInt =>
              <_>FROM {y}-{m} TO {yy}-{mm}-{dd}</_>.text
            case (y,m,d,yy,mm,dd)   if y != "" && m != "" && d != "" && yy != "" && mm != "" && dd != "" && y.toInt*10000+m.toInt*100+d.toInt < yy.toInt*10000+mm.toInt*100+dd.toInt =>
              <_>FROM {y}-{m}-{d} TO {yy}-{mm}-{dd}</_>.text
            case _ => ""
          }
        case Full("gdt_before") =>
          (yyyyBef, MMBef, ddBef) match {
            case (y,"","") if y != "" =>  <_>BEF {y}</_>.text
            case (y,m,"") if y != "" && m != ""  => <_>BEF {y}-{m}</_>.text
            case (y,m,d) if y != "" && m != "" && d != "" => <_>BEF {y}-{m}-{d}</_>.text
            case _ => ""
          }
        case Full("gdt_after") =>
          (yyyyAft, MMAft, ddAft) match {
            case (y,"","") if y != "" =>  <_>AFT {y}</_>.text
            case (y,m,"") if y != "" && m != ""  => <_>AFT {y}-{m}</_>.text
            case (y,m,d) if y != "" && m != "" && d != "" => <_>AFT {y}-{m}-{d}</_>.text
            case _ => ""
          }
        case Full("gdt_between") =>
          (yyyyBet, MMBet, ddBet, yyyyAnd, MMAnd, ddAnd) match {
            case (y,"","",yy,"","") if y != "" &&                       yy != "" && y.toInt*10000 < yy.toInt*10000 =>
              <_>BET {y} AND {yy}</_>.text
            case (y,m,"",yy,"","")  if y != "" && m != "" &&            yy != "" && y.toInt*10000+m.toInt*100 < yy.toInt*10000 =>
              <_>BET {y}-{m} AND {yy}</_>.text
            case (y,m,d,yy,"","")   if y != "" && m != "" && d != "" && yy != "" && y.toInt*10000+m.toInt*100+d.toInt < yy.toInt*10000 =>
              <_>BET {y}-{m}-{d} AND {yy}</_>.text
            case (y,"","",yy,mm,"") if y != "" &&                       yy != "" && mm != "" && y.toInt*10000 < yy.toInt*10000+mm.toInt*100 =>
              <_>BET {y} AND {yy}-{mm}-{dd}</_>.text
            case (y,m,"",yy,mm,"")  if y != "" && m != "" &&            yy != "" && mm != "" && y.toInt*10000+m.toInt*100 < yy.toInt*10000+mm.toInt*100 =>
              <_>BET {y}-{m} AND {yy}-{mm}-{dd}</_>.text
            case (y,m,d,yy,mm,"")   if y != "" && m != "" && d != "" && yy != "" && mm != "" && y.toInt*10000+m.toInt*100+d.toInt < yy.toInt*10000+mm.toInt*100 =>
              <_>BET {y}-{m}-{d} AND {yy}-{mm}</_>.text
            case (y,"","",yy,mm,dd) if y != "" &&                       yy != "" && mm != "" && dd != "" && y.toInt*10000 < yy.toInt*10000+mm.toInt*100+dd.toInt =>
              <_>BET {y} AND {yy}-{mm}-{dd}</_>.text
            case (y,m,"",yy,mm,dd)  if y != "" && m != "" &&            yy != "" && mm != "" && dd != "" && y.toInt*10000+m.toInt*100 < yy.toInt*10000+mm.toInt*100+dd.toInt =>
              <_>BET {y}-{m} AND {yy}-{mm}-{dd}</_>.text
            case (y,m,d,yy,mm,dd)   if y != "" && m != "" && d != "" && yy != "" && mm != "" && dd != "" && y.toInt*10000+m.toInt*100+d.toInt < yy.toInt*10000+mm.toInt*100+dd.toInt =>
              <_>BET {y}-{m}-{d} AND {yy}-{mm}-{dd}</_>.text
            case _ => ""
          }
        case Full("gdt_about") =>
          (yyyyApx, MMApx, ddApx) match {
            case (y,"","") if y != "" =>  <_>ABT {y}</_>.text
            case (y,m,"") if y != "" && m != ""  => <_>ABT {y}-{m}</_>.text
            case (y,m,d) if y != "" && m != "" && d != "" => <_>ABT {y}-{m}-{d}</_>.text
            case _ => ""
          }
        case Full("gdt_text") => datext
        case Empty => ""
        case _ => ""
      }
      log.debug("confirm: submit is pressed to return result = |" + result +"|");
      result match {
        case r if r.size > 0 => {
          S.setSessionAttribute("GedcomDate", result)
          S.notice("--> " + result)
          Unblock //& Alert("--> " + result)
        }
        case _ => {
          S.setSessionAttribute("GedcomDate", result)
          S.error(S.?("date.is.invalid"))
          Noop
        }
      }
      //Unblock //& Alert("--> " + result)
      })) &
    "#return" #> ((b: NodeSeq) => <button onclick={Unblock.toJsCmd}>{b}</button>)

  }

}



//  def confirm(in: NodeSeq) =
//    bind("confirm", in,
//      "yes" -> ((b: NodeSeq) => ajaxButton(b, () => {
//        println("Rhode Island Destroyed")
//        Unblock & Alert("Rhode Island Destroyed")
//      })),
//      "no" -> ((b: NodeSeq) => <button onclick={Unblock.toJsCmd}>{b}</button>))

//  var dds = ddsFunc(yyyyMMdd.toInt, MMdd.toInt)
//  def ddsFunc(y: Int, m: Int): List[(String, String)] = {
//    log.debug(<_>dds: y={y.toString}; m={m.toString} </_>.text)
//    ("--","--") :: List.range(1, m match {
//      case d31: Int if List(1,3,5,7,8,10,12).contains(d31) => 31+1
//      case d30: Int if List(4,6,9,11).contains(d30) => 30+1
//      case d29: Int if (d29 == 2) && (((y % 4)==0) || ((y % 100)==0)) => 29+1
//      case d28: Int if d28 == 2 => 28+1
//      case _ => 0
//  })map(i => (i.toString,i.toString))
//  }

// google-gr: Lift [Form validation: notice on conversion failure?] how to validate Date
/*
trait MappedDateParseErrorHandler[T] {
  self : MappedDate[T] =>

  def proxyParse(s : String) : Date = {
    LiftRules.dateTimeConverter().parseDate(s) match {
      case f @ Failure(...) => handleDateParseError(f)
      case e @ Empty => handleDateParseError(e ?~ "Unknown parse failure")
      case Full(date) => this.set(date)
    }
    // Return whatever the current value is
    this.is
  }

  def handleDateParseError(error : Failure) : Unit // Implement this in your
field

  override def setFromAny(f : Any) : Date = f match {
    case s : String => proxyParse(s)
    case (s : String) :: _ => proxyParse(s)
    case other => super.setFromAny(f)
  }

}
*/
