package lt.node.gedcom.snippet

import org.slf4j.{LoggerFactory, Logger}

import _root_.net.liftweb._
import http._
import js.JE.JsObj
import js.jquery.JqJsCmds._
import wizard._
import common._
import _root_.net.liftweb.util.Helpers._
import _root_.net.liftweb.util.FieldError

// B302-3/vsh

import _root_.bootstrap.liftweb.{AccessControl,RequestedURL,ErrorXmlMsg,CurrentUser}
import _root_.lt.node.gedcom.model._
import _root_.lt.node.gedcom.util._ //{GedcomDateOptions,PeTags,PaTags,ToolTips}
//import lt.node.gedcom.util.GedcomUtil

/**
 * google-group: Lift [LiftScreen as class]
 * google-group: Overriding wizard/screen field html?
 * google-group: Lift [Seven Things that distinguish Lift from other web frameworks]
 */


class /*object*/ AddPeWizardRunner {

  def render = "* [onclick]" #> SHtml.ajaxInvoke(() =>
    ModalDialog((<div><lift:PeWizard ajax="true"/><br/></div>),
      JsObj(("top","200px"),("left","300px"),("width","600px"),("height","600px"))))
}
/*  neveikia: ,("align","middle")
      JsObj(("top","40px"),("left","40px"),("width","800px"),("height","800px"))))
    ModalDialog((<div> <lift:PeWizard ajax="true"/></div>), JsObj("width", "800px")))
 */

class PeWizard extends Wizard with Loggable {

  val log: Logger = LoggerFactory.getLogger("PeWizard")

  if (!AccessControl.isAuthenticated_?) S.redirectTo("/")
  RequestedURL(Full(S.referer.openOr("gedcom/personView")))

  log.debug("PeWizard wvBoxCU.is CurrentUser |" + CurrentUser.is + "|")
  private object wvBoxCU extends WizardVar[Box[User]](CurrentUser.is)
  log.debug("PeWizard wvBoxCU.is wvBoxCU |" + wvBoxCU.is + "|")

  private object wvBoxPerson extends WizardVar[Box[Person]](Empty)
  object wvBoxPersonEvent extends WizardVar[Box[PersonEvent]](Empty)
  object wvBoxPersonAttrib extends WizardVar[Box[PersonAttrib]](Empty)
  object wvBoxEventDetail extends WizardVar[Box[EventDetail]](Empty)

  val dateFormat = GedcomDateOptions.msg4Date(S.get("locale").getOrElse("en"))
  log.debug("PeWizard S.get(\"locale\").getOrElse(\"en\") |" + S.get("locale").getOrElse("en") + "|")
  log.debug("PeWizard dateFormat |" + dateFormat + "|")

  personVar.is match {
    case Full(p) =>
      log.debug("PeWizard personVar.is |" + personVar.is + "|")
       wvBoxPerson.set(Full(p))
    case _ =>
      val place = "PeWizard (on top of source)"
      val msg = "No person for " + S.getSessionAttribute("personId").toString/*.openOr("0").toLong*/
      log.error(place+"; "+msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map( "location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
        })
  }

  private object wvEA extends WizardVar[String]("event")
  //private object wvEaTagDat extends WizardVar[(String, String, String)]("event", "", "gdt_exact")
  private object wvEvenDat4Pe extends WizardVar[(String, String)]("", "gdt_exact")
  private object wvAttrDat4Pe extends WizardVar[(String, String)]("", "gdt_exact")
  //private object wvEvenDat4Fa extends WizardVar[(String, String)]("", "gdt_exact")
  private object wvDPAS extends WizardVar[(String, String, String, String, String)]("", "", "", "", "")
  private object wvEDMLT extends WizardVar [(MultiLangText, MultiLangText, MultiLangText,
    MultiLangText, MultiLangText, MultiLangText)] (
    new MultiLangText("descriptor", ""),
    new MultiLangText("dateValue", ""),
    new MultiLangText("place", ""),
    new MultiLangText("cause", ""),
    new MultiLangText("source", ""),
    new MultiLangText("note", "") )

  // for PersonEvent.adoptedBy
  private object wvADOP extends WizardVar[String]("BOTH")

  // for PersonEvent EventDetail.descriptrion
  private object wvEVEN extends WizardVar[String]("")
  //private object wvEVEN extends WizardVar[(String, MultiLangText)]("", new MultiLangText("descriptrion", ""))

  // for PersonEvent EventDetail.cause
  private object wvDEAT extends WizardVar[String]("")
  //private object wvDEAT extends WizardVar[(String, MultiLangText)]("", new MultiLangText("cause", ""))

  // for PersonAttrib.tagValue
  private object wvXXXX extends WizardVar[(String, MultiLangText)]("", new MultiLangText("tagValue", ""))

  /*private object wvDateLabels extends WizardVar[(String, String)]("", "")*/

  val eaCases: List[(String, String)] =
    List(("event", "wiz.event"), ("attrib", "wiz.attribute")).map((kv)=>(kv._1, S ? kv._2))
  val adoptCases: List[(String, String)] =
    List(("BOTH", "wiz.adopt.both"), ("WIFE", "wiz.adopt.wife"), ("HUSB", "wiz.adopt.husb")).map((kv)=>(kv._1, S ? kv._2))


  var actionCUD = "C" //-- MUST be initialized with "C";    C add, U update, D delete
  var updatePePa = "" //-- MUST be initialized with "";     PE PersonEvent, PA PersonAttrib

  getEventAttribData()
  log.debug("PeWizard wvBoxPerson after getEventAttribData |" + wvBoxPerson.get./*get.*/toString + "|")
  log.debug("PeWizard actionCUD updatePePa |" + actionCUD + "|" + updatePePa + "|")


  override protected def calcAjaxOnDone = Unblock

  override def calcFirstScreen = { //  : Box[Screen]
    log.debug("PeWizard calcFirstScreen  []...")
    actionCUD match  {
      case "C" =>
        log.debug("PeWizard calcFirstScreen C")
        Full(addEventOrAttib)
      case "U" => updatePePa match {
        case "PE" =>
          log.debug("PeWizard calcFirstScreen Pe")
          Full(selPeTag)
        case "PA" =>
          log.debug("PeWizard calcFirstScreen Pa")
          Full(selPaTag)
        case _ =>
          log.debug("PeWizard calcFirstScreen _")
          Empty
      }
      case _ =>
        log.debug("PeWizard calcFirstScreen  ...[]")
        Empty
    }
  }


  val addEventOrAttib = new Screen {
    val aoeInit = wvEA.get
    val eoaNew = radio(S ? "wiz.add", eaCases.filter((kv) => kv._1==aoeInit).head._2, eaCases.map( _._2),
      valMinLen(1, S ? "wiz.click.radio"))

    override def nextScreen = {
      wvEA.set(eaCases.find(_._2 == eoaNew.get).get._1)
      eoaNew.get match {
        case xxx if xxx == "" => selPeTag
        case theRest => eaCases.find(_._2 == theRest.toString).get._1 match {
          case "event" => selPeTag
          case _ => selPaTag
        }
      }
    }
  }


  val selPeTag = new Screen {
    val tagInit = wvEvenDat4Pe.get._1 match { // PeTags.tags.map(_._2).head
      case "" => PeTags.tags.map(_._2).head
      case _ => wvEvenDat4Pe.get._1
    }
    val tagNew = select(S ? "add.event", tagInit, PeTags.tags.map(_._2)
      , "size"->"13", "title"->ToolTips.getMsg("age_at_event")
    )
    val dateoptionsInit = GedcomDateOptions.getMsg(wvEvenDat4Pe.get._2)
    val dateoptionsNew = select(S ? "pe.dateShape", dateoptionsInit,
      GedcomDateOptions.tags.filter( _._1 != "gdt_and").map(_._2), "size"->"10")

    override def nextScreen = {
      log.debug("selPeTag tagNew.is |" + tagNew.get + "|")
      wvEvenDat4Pe.set((PeTags.tags.find(_._2 == tagNew.get).get._1, wvEvenDat4Pe.get._2))
      //S.notice(wvEvenDat4Pe.get._2)
      //S.notice(dateoptionsNew.get)
      //wvEvenDat4Pe.set( wvEvenDat4Pe.get._1, dateoptionsNew.get)
      wvEvenDat4Pe.set( wvEvenDat4Pe.get._1, GedcomDateOptions.getKey(dateoptionsNew.get))
      //S.notice(wvEvenDat4Pe.get._2)

      tagNew.get match {
        //case tagInit if tagInit == "" => selPeTag
        case newVal => PeTags.tags.find(_._2 == newVal).get._1 match {
          case "ADOP" => peTagADOP
          case "EVEN" => peTagEVEN
          case "DEAT" => peTagDEAT
          case _ => peTagXXXX
        }
      }
    }
  }


  val selPaTag = new Screen {
    val tagInit2 = wvAttrDat4Pe.get._1 match {
      case "" => //log.debug("selPaTag 'attrib' ")
        PaTags.tags.map(_._2).head
      case _ =>
        log.debug("selPaTag '_' ")
        wvAttrDat4Pe.get._1
    }
    log.debug("selPaTag tagInit2 |" + tagInit2 + "|")
    val tagNew2 = select(S ? "add.attrib", tagInit2, PaTags.tags.map(_._2),
      "size"->"11")

    val dateoptionsInit = GedcomDateOptions.getMsg(wvAttrDat4Pe.get._2)
    val dateoptionsNew = select(S ? "pe.dateValue", dateoptionsInit,
      GedcomDateOptions.tags.filter( _._1 != "gdt_and").map(_._2), "size"->"10")

    override def nextScreen = {
      log.debug("selPaTag tagNew2.is |" + tagNew2.get + "|")
      wvAttrDat4Pe.set((PaTags.tags.find(_._2 == tagNew2.get).get._1, wvAttrDat4Pe.get._2))
      //S.notice(wvAttrDat4Pe.get.toString)

      //wvAttrDat4Pe.set(wvAttrDat4Pe.get._1, dateoptionsNew.get)
      wvAttrDat4Pe.set( wvAttrDat4Pe.get._1, GedcomDateOptions.getKey(dateoptionsNew.get))
      /*
      log.debug("selPeTag tagNew.is |" + tagNew.get + "|")
      wvEvenDat4Pe.set((PeTags.tags.find(_._2 == tagNew.get).get._1, wvEvenDat4Pe.get._2))
//      S.notice(wvEvenDat4Pe.get.toString)
      wvEvenDat4Pe.set( wvEvenDat4Pe.get._1, dateoptionsNew.get)
       */
      log.debug("selPaTag wvAttrDat4Pe " + wvAttrDat4Pe.get.toString)
      log.debug("selPaTag PaTags.tags.find(_._2 == newVal2).get._1 " + PaTags.tags.find(_._2 == tagNew2.get).get._1)
      tagNew2.get match {
        case newVal2 => PaTags.tags.find(_._2 == newVal2).get._1 match {
          case xxxx if xxxx == "RESI" => paTagRESI
          case _ => paTagXXXX
        }
      }
    }
  }

    val peTagADOP = new Screen {
    val adoptInit = S ? adoptCases.find(_._1==wvADOP.get).get._2  //"BOTH"
    val adoptNew = radio(S ? "pe.adoptedBy", adoptInit, adoptCases.map(_._2),
      valMinLen(1, S ? "wiz.click.radio"))
//    //--------------------------------------------------
// D226-2/vsh atidėtas datos veikšmmės formavimo keitimas - viskas viename lauke
//    //    val dateoptionsInit = GedcomDateOptions.getMsg(wvEvenDat4Pe.get._2)
//    //    val dateoptionsNew = select(S ? "pe.dateValue", dateoptionsInit,
//    //      GedcomDateOptions.tags.filter( _._1 != "gdt_and").map(_._2), "size"->"9")
//    val dateInit =  "asasasasasas" //dateInitValue
    //val dateInit =  GedcomDateOptions.dateInitValue(GedcomDateOptions.getKey(wvEvenDat4Pe.get._2))
    //val dateInit =  GedcomDateOptions.dateInitValue(GedcomDateOptions.getKey(wvEvenDat4Pe.get._2))(S.locale.getLanguage)
    //val dateInit =  GedcomDateOptions.dateInitValue(GedcomDateOptions.getKey(wvEvenDat4Pe.get._2))(S.locale.getLanguage)

    //val dateInit = GedcomDateOptions.dateInitValue(wvEvenDat4Pe.get._2)(S.locale.getLanguage)
    //val dateNew = textarea(S ? "pe.dateValue", dateInit, "style"->"display:yes", "class"->"textarea-4date", isValiDate _ )
    val dateInit = wvDPAS._1
    val dateNew = textarea((S ? "pe.dateValue")+ ": " + GedcomDateOptions.dateInitValue(wvEvenDat4Pe.get._2)(S.locale.getLanguage),
      dateInit, "style"->"display:yes", "class"->"textarea-4date", isValiDate _ )

    //      case _ => field(/*S ? wvDateLabels.get._1*/"trys", (wvEvenDat4Pe.get._2), "size"->"11", "maxlength"->"11",
    //        "display"->"yes"  /*isIncompletedate _*/ )
    //    //println("aaaaaaaaaaaaaaaaaaaaaa "+ wvEvenDat4Pe.get._2)
////    val dateNew = field(/*S ? wvDateLabels.get._1*/"trys", dateInit, "size"->"11", "maxlength"->"11",
////        "display"->"yes"  /*isIncompletedate _*/ )
//    val dateNew = (wvEvenDat4Pe.get._2) match {
//      case z if z=="be datos" =>  field(/*S ? wvDateLabels.get._1*/"vienas", (wvEvenDat4Pe.get._2), "size"->"11", "maxlength"->"11",
//        "display"->"none"  /*isIncompletedate _*/ )
//      case "gdt_no_date" =>  field(/*S ? wvDateLabels.get._1*/"vienas", (wvEvenDat4Pe.get._2), "size"->"11", "maxlength"->"11",
//        "display"->"none"  /*isIncompletedate _*/ )
//      case z if z=="gdt_text" => textarea/*field*/("du", "("+(wvEvenDat4Pe.get._2)+")", "style"->"display:yes", "class"->"textarea-small")
//      case "žodžiais, jei kitaip netinka" => textarea/*field*/("du", (wvEvenDat4Pe.get._2), "style"->"display:yes", "class"->"textarea-small")
//      case _ => field(/*S ? wvDateLabels.get._1*/"trys", (wvEvenDat4Pe.get._2), "size"->"11", "maxlength"->"11",
//        "display"->"yes"  /*isIncompletedate _*/ )
//    }
////    val dateNew = GedcomDateOptions.getKey(wvEvenDat4Pe.get._2) match {
////      case "gdt_no_date" =>  field(/*S ? wvDateLabels.get._1*/"vienas", GedcomDateOptions.getKey(wvEvenDat4Pe.get._2), "size"->"11", "maxlength"->"11",
////        "display"->"none"  /*isIncompletedate _*/ )
////      case "gdt_text" => textarea/*field*/("du", GedcomDateOptions.getKey(wvEvenDat4Pe.get._2), "style"->"display:yes", "class"->"textarea-small")
////      case _ => field(/*S ? wvDateLabels.get._1*/"trys", GedcomDateOptions.getKey(wvEvenDat4Pe.get._2), "size"->"11", "maxlength"->"11",
////        "display"->"yes"  /*isIncompletedate _*/ )
////    }
//    //--------------------------------------------------
    val placeInit = wvEDMLT._3.getLangMsg()
    val placeNew = textarea/*field*/(S ? "pe.place", placeInit,
      "style"->"display:yes", "class"->"textarea-small")

    val ageAtEventInit = wvDPAS.get._3
    val ageAtEventNew = textarea/*field*/(S ? "pe.ageAtEvent", ageAtEventInit,
      "title"->ToolTips.getMsg("age_at_event"), "class"->"textarea-small",
      isValidateAAE _ )

    val sourceInit = wvEDMLT._5.getLangMsg()
    val sourceNew = textarea/*field*/(S ? "pe.source", sourceInit,
      "maxlength"->"248", "class"->"textarea-small")

    val noteInit = wvEDMLT._6.getLangMsg()
    val noteNew = textarea/*field*/(S ? "pe.note", noteInit,
      "maxlength"->"248", "class"->"textarea-small")

    override def screenTop = Full(<span><b>{PeTags.getMsg(wvEvenDat4Pe.get._1)}</b></span>)

    def isValidateAAE(aae: String): List[FieldError] = { //validateAgeAtEvent aae
      AgeAtEvent.validateAgeAtEvent(aae) match {
        case true => Nil
        case _ => S.?("aae.is.invalid")
      }
    }

    def isValiDate(s: String): List[FieldError] = {
      GedcomDateOptions.valiDate(s, wvEvenDat4Pe.get._2) match {
        case "" =>
          //S.notice("Nil")
          Nil
        case msg if msg.length > 0 =>
          //S.notice("msg |"+msg+"|")
          msg  // S.?("date.is.invalid")
      }
    }

    override def nextScreen = {
//      wvEvenDat4Pe.set( wvEvenDat4Pe.get._1, dateoptionsNew.get)
      wvADOP.set(adoptNew.get)
      wvDPAS.set(dateNew.get, placeNew.get, ageAtEventNew.get, sourceNew.get, noteNew.get)
      conf/*nextScreen4Date*/
    }
  }


  val peTagEVEN = new Screen {
    val descriptorInit = wvEVEN.get
    val descriptorNew = textarea/*field*/(S ? "pe.descriptor", descriptorInit, "style"->"display:yes", "class"->"textarea-small")/*.toString*/  //D922-7
//    val dateoptionsInit = GedcomDateOptions.getMsg(wvEvenDat4Pe.get._2)
//    val dateoptionsNew = select(S ? "pe.dateValue", dateoptionsInit,
//      GedcomDateOptions.tags.filter( _._1 != "gdt_and").map(_._2), "size"->"9")

    //val dateInit =  GedcomDateOptions.dateInitValue(wvEvenDat4Pe.get._2)(S.locale.getLanguage)
    ////val dateNew = textarea(S ? "pe.dateValue", dateInit, "style"->"display:yes", "class"->"textarea-4date")
    //val dateNew = textarea(S ? "pe.dateValue", dateInit, "style"->"display:yes", "class"->"textarea-4date", isValiDate _ )
    val dateInit = wvDPAS._1
    val dateNew = textarea((S ? "pe.dateValue")+ ": " + GedcomDateOptions.dateInitValue(wvEvenDat4Pe.get._2)(S.locale.getLanguage),
      dateInit, "style"->"display:yes", "class"->"textarea-4date", isValiDate _ )

    val placeInit = wvEDMLT._3.getLangMsg()
    val placeNew = textarea/*field*/(S ? "pe.place", placeInit,
      "style"->"display:yes", "class"->"textarea-small")

    val ageAtEventInit = wvDPAS.get._3
    val ageAtEventNew = textarea/*field*/(S ? "pe.ageAtEvent", ageAtEventInit,
      "title"->ToolTips.getMsg("age_at_event"), "class"->"textarea-small", validateAAE _ )

    val sourceInit = wvEDMLT._5.getLangMsg()
    val sourceNew = textarea/*field*/(S ? "pe.source", sourceInit,
      "style"->"display:none", "maxlength"->"248", "class"->"textarea-small")

    val noteInit = wvEDMLT._6.getLangMsg()
    val noteNew = textarea/*field*/(S ? "pe.note", noteInit,
      "maxlength"->"248", "class"->"textarea-small")

    override def screenTop = Full(<span><b>{PeTags.getMsg(wvEvenDat4Pe.get._1)}</b></span>)

    def validateAAE(aae: String): List[FieldError] = { //validateAgeAtEvent aae
      AgeAtEvent.validateAgeAtEvent(aae) match {
        case true => Nil
        case _ => S.?("aae.is.invalid")
      }
    }

    def isValiDate(s: String): List[FieldError] = {
      GedcomDateOptions.valiDate(s, wvEvenDat4Pe.get._2) match {
        case "" =>
          //S.notice("Nil")
          Nil
        case msg if msg.length > 0 =>
          //S.notice("msg |"+msg+"|")
          msg  // S.?("date.is.invalid")
      }
    }

    override def nextScreen = {
//      wvEvenDat4Pe.set(dateNew, wvEvenDat4Pe.get._2)
      wvEVEN.set(descriptorNew)
      wvDPAS.set( dateNew.get, placeNew.get, ageAtEventNew.get, sourceNew.get, noteNew.get)
      conf/*nextScreen4Date*/
    }
  }


  val peTagDEAT = new Screen {
//    val dateoptionsInit = GedcomDateOptions.getMsg(wvEvenDat4Pe.get._2)
//    val dateoptionsNew = select(S ? "pe.dateValue", dateoptionsInit,
//      GedcomDateOptions.tags.filter( _._1 != "gdt_and").map(_._2), "size"->"9")

    //val dateInit =  GedcomDateOptions.dateInitValue(wvEvenDat4Pe.get._2)(S.locale.getLanguage)
    //val dateNew = textarea(S ? "pe.dateValue", dateInit, "style"->"display:yes", "class"->"textarea-4date", isValiDate _ )
    val dateInit = wvDPAS._1
    val dateNew = textarea((S ? "pe.dateValue")+ ": " + GedcomDateOptions.dateInitValue(wvEvenDat4Pe.get._2)(S.locale.getLanguage),
      dateInit, "style"->"display:yes", "class"->"textarea-4date", isValiDate _ )

    val placeInit = wvEDMLT._3.getLangMsg()
    val placeNew = textarea/*field*/(S ? "pe.place", placeInit/*, 2, 30*/,
      /*"rows"->"3",  "cols"->"30",*/  /*"style"->"width:300px; height:50px; display:yes",*/
      "maxlength"->"120", "class"->"textarea-small")

    val ageAtEventInit = wvDPAS.get._3
    val ageAtEventNew = textarea/*field*/(S ? "pe.ageAtEvent", ageAtEventInit,
      "title"->ToolTips.getMsg("age_at_event"), "class"->"textarea-small", validateAAE _ )

    val sourceInit = wvEDMLT._5.getLangMsg()
    val sourceNew = textarea/*field*/(S ? "pe.source", sourceInit,
      "style"->"display:none", "maxlength"->"248", "class"->"textarea-small")

    val causeInit = wvEDMLT._5.getLangMsg()
    val causeNew = textarea/*field*/(S ? "pe.cause", causeInit,
      "style"->"display:yes", "class"->"textarea-small")

    val noteInit = wvEDMLT._6.getLangMsg()
    val noteNew = textarea/*field*/(S ? "pe.note", noteInit,
      "maxlength"->"248", "class"->"textarea-small")

    override def screenTop = Full(<span><b>{PeTags.getMsg(wvEvenDat4Pe.get._1)}</b></span>)

    def validateAAE(aae: String): List[FieldError] = { //validateAgeAtEvent aae
      AgeAtEvent.validateAgeAtEvent(aae) match {
        case true => Nil
        case _ => S.?("aae.is.invalid")
      }
    }

    def isValiDate(s: String): List[FieldError] = {
      GedcomDateOptions.valiDate(s, wvEvenDat4Pe.get._2) match {
        case "" =>
          //S.notice("Nil")
          Nil
        case msg if msg.length > 0 =>
          //S.notice("msg |"+msg+"|")
          msg  // S.?("date.is.invalid")
      }
    }

    override def nextScreen = {
//      wvEvenDat4Pe.set(wvEvenDat4Pe.get._1, dateoptionsNew.get)
//      wvEvenDat4Pe.set(dateNew, wvEvenDat4Pe.get._2)
      wvDEAT.set(causeNew.get)
      wvDPAS.set(dateNew.get, placeNew.get, ageAtEventNew.get, sourceNew.get, noteNew.get)
      conf/*nextScreen4Date*/
    }
  }


  val peTagXXXX = new Screen {
//    val dateoptionsInit = GedcomDateOptions.getMsg(wvEvenDat4Pe.get._2)
//    val dateoptionsNew = select(S ? "pe.dateValue", dateoptionsInit,
//      GedcomDateOptions.tags.filter( _._1 != "gdt_and").map(_._2)
//      , "size"->"9"
//      , "title"->ToolTips.getMsg("age_at_event")
//    )
    val dateInit = wvDPAS._1
    val dateNew = textarea((S ? "pe.dateValue")+ ": " + GedcomDateOptions.dateInitValue(wvEvenDat4Pe.get._2)(S.locale.getLanguage),
      dateInit, "style"->"display:yes", "class"->"textarea-4date", isValiDate _ )

    val placeInit = wvEDMLT._3.getLangMsg()
    val placeNew = textarea/*field*/(S ? "pe.place", placeInit,
      "style"->"display:yes", "maxlength"->"120", "class"->"textarea-small")

    val ageAtEventInit = wvDPAS.get._3
    val ageAtEventNew = textarea/*field*/(S ? "pe.ageAtEvent", ageAtEventInit,
      "title"->ToolTips.getMsg("age_at_event"), validateAAE _ , "class"->"textarea-small")

    val sourceInit = wvEDMLT._5.getLangMsg()
    val sourceNew = textarea/*field*/(S ? "pe.source", sourceInit,
      "style"->"display:none", "maxlength"->"248", "class"->"textarea-small")

    val noteInit = wvEDMLT._6.getLangMsg()
    val noteNew = textarea/*field*/(S ? "pe.note", noteInit,
      "maxlength"->"248", "class"->"textarea-small")

    override def screenTop = Full(<span><b>{PeTags.getMsg(wvEvenDat4Pe.get._1)}</b></span>)

    def validateAAE(aae: String): List[FieldError] = { //validateAgeAtEvent aae
      AgeAtEvent.validateAgeAtEvent(aae) match {
        case true => Nil
        case _ => S.?("aae.is.invalid")
      }
    }

    def isValiDate(s: String): List[FieldError] = {
      GedcomDateOptions.valiDate(s, wvEvenDat4Pe.get._2) match {
        case "" =>
          //S.notice("Nil")
          Nil
        case msg if msg.length > 0 =>
          //S.notice("msg |"+msg+"|")
          msg  // S.?("date.is.invalid")
      }
    }

    override def nextScreen = {
//      wvEvenDat4Pe.set(dateNew, wvEvenDat4Pe.get._2)
      wvDPAS.set(dateNew.get.trim.replaceAll("( )+", " "),
        placeNew.get.trim.replaceAll("( )+", " "),
        ageAtEventNew.get.trim.replaceAll("( )+", " "),
        sourceNew.get.trim.replaceAll("( )+", " "),
        noteNew.get.trim.replaceAll("( )+", " "))
      conf/*nextScreen4Date*/
    }
  }



    val paTagRESI = new Screen {
//      val dateoptionsInit = GedcomDateOptions.getMsg(wvAttrDat4Pe.get._2)
//      val dateoptionsNew = select(S ? "pe.dateValue", dateoptionsInit,
//        GedcomDateOptions.tags.filter( _._1 != "gdt_and").map(_._2), "size"->"9")

      //val dateInit =  GedcomDateOptions.dateInitValue(wvAttrDat4Pe.get._2)(S.locale.getLanguage)
      //val dateNew = textarea(S ? "pe.dateValue", dateInit, "style"->"display:yes", "class"->"textarea-4date", isValiDate _ )
      val dateInit = wvDPAS._1
      val dateNew = textarea((S ? "pe.dateValue")+ ": " + GedcomDateOptions.dateInitValue(wvAttrDat4Pe.get._2)(S.locale.getLanguage),
        dateInit, "style"->"display:yes", "class"->"textarea-4date", isValiDate _ )

      val placeInit = wvEDMLT._3.getLangMsg()
      val placeNew = textarea/*field*/(S ? "pe.place", placeInit,
        "style"->"display:yes", "maxlength"->"120", "class"->"textarea-small")

      val ageAtEventInit = wvDPAS.get._3
      val ageAtEventNew = textarea/*field*/(S ? "pe.ageAtEvent", ageAtEventInit,
        "title"->ToolTips.getMsg("age_at_event"), "class"->"textarea-small", validateAAE _)

      val sourceInit = wvEDMLT._5.getLangMsg()
      val sourceNew = textarea/*field*/(S ? "pe.source", sourceInit,
        "style"->"display:none", "maxlength"->"248", "class"->"textarea-small")

      val noteInit = wvEDMLT._6.getLangMsg()
      val noteNew = textarea/*field*/(S ? "pe.note", noteInit,
        "maxlength"->"248", "class"->"textarea-small")

      override def screenTop = Full(<span><b>{PaTags.getMsg(wvAttrDat4Pe.get._1)}</b></span>)

      def validateAAE(aae: String): List[FieldError] = { //validateAgeAtEvent aae
        AgeAtEvent.validateAgeAtEvent(aae) match {
          case true => Nil
          case _ => S.?("aae.is.invalid")
        }
      }

      def isValiDate(s: String): List[FieldError] = {
        GedcomDateOptions.valiDate(s, wvAttrDat4Pe.get._2) match {
          case "" =>
            //S.notice("Nil")
            Nil
          case msg if msg.length > 0 =>
            //S.notice("msg |"+msg+"|")
            msg  // S.?("date.is.invalid")
        }
      }

      override def nextScreen = {
//        wvAttrDat4Pe.set(wvAttrDat4Pe.get._1, dateoptionsNew.get)
//        wvAttrDat4Pe.set(dateNew, wvAttrDat4Pe.get._2)
        wvDPAS.set(dateNew.get, placeNew.get, ageAtEventNew.get, sourceNew.get, noteNew.get)
        conf/*nextScreen4Date*/
      }
    }


    val paTagXXXX = new Screen {
      log.debug("paTagXXXX []...")
      val valueInit = wvXXXX.get._1
      val valueNew = textarea/*field*/(S ? "pa.value", valueInit, "class"->"textarea-small")

//      val dateoptionsInit = GedcomDateOptions.getMsg(wvAttrDat4Pe.get._2)
//      val dateoptionsNew = select(S ? "pe.dateValue", dateoptionsInit,
//        GedcomDateOptions.tags.filter( _._1 != "gdt_and").map(_._2), "size"->"9")

      //val dateInit =  GedcomDateOptions.dateInitValue(wvAttrDat4Pe.get._2)(S.locale.getLanguage)
      //val dateNew = textarea(S ? "pe.dateValue", dateInit, "style"->"display:yes", "class"->"textarea-4date", isValiDate _ )
      val dateInit = wvDPAS._1
      val dateNew = textarea((S ? "pe.dateValue")+ ": " + GedcomDateOptions.dateInitValue(wvAttrDat4Pe.get._2)(S.locale.getLanguage),
        dateInit, "style"->"display:yes", "class"->"textarea-4date", isValiDate _ )

      val placeInit = wvEDMLT._3.getLangMsg()
      val placeNew = textarea/*field*/(S ? "pe.place", placeInit,
        "maxlength"->"120", "class"->"textarea-small")

      val ageAtEventInit = wvDPAS.get._3
      val ageAtEventNew = textarea/*field*/(S ? "pe.ageAtEvent", ageAtEventInit,
        "title"->ToolTips.getMsg("age_at_event"), validateAAE _ , "class"->"textarea-small")

      val sourceInit = wvEDMLT._5.getLangMsg()
      val sourceNew = textarea/*field*/(S ? "pe.source", sourceInit,
        "maxlength"->"248", "class"->"textarea-small")

      val noteInit = wvEDMLT._6.getLangMsg()
      val noteNew = textarea/*field*/(S ? "pe.note", noteInit,
        "maxlength"->"248", "class"->"textarea-small")

      log.debug("paTagXXXX ...[]")

      override def screenTop = Full(<span><b>{PaTags.getMsg(wvAttrDat4Pe.get._1)}</b></span>)

      def validateAAE(aae: String): List[FieldError] = { //validateAgeAtEvent aae
        AgeAtEvent.validateAgeAtEvent(aae) match {
          case true => Nil
          case _ => S.?("aae.is.invalid")
        }
      }

      def isValiDate(s: String): List[FieldError] = {
        GedcomDateOptions.valiDate(s, wvAttrDat4Pe.get._2) match {
          case "" =>
            //S.notice("Nil")
            Nil
          case msg if msg.length > 0 =>
            //S.notice("msg |"+msg+"|")
            msg  // S.?("date.is.invalid")
        }
      }

      override def nextScreen = {
//        wvAttrDat4Pe.set(wvAttrDat4Pe.get._1, dateoptionsNew.get)
//        wvAttrDat4Pe.set(dateNew, wvAttrDat4Pe.get._2)
        wvXXXX.set((valueNew/*.toString*/, wvXXXX.get._2))      //D922-7
        wvDPAS.set(dateNew.get, placeNew.get, ageAtEventNew.get, sourceNew.get, noteNew.get)
        conf/*nextScreen4Date*/
      }
    }
  //}


  // D226-2/vsh atidėtas datos reikšmmės formavimo keitimas - viskas viename lauke
  // D316-6/vsh padarytas datos reikšmmės formavimo keitimas - viskas viename lauke
  /*def nextScreen4Date = {
    wvEA.get match {
      case "event" =>
        log.debug("nextScreen4Date |" + wvEvenDat4Pe.get._2 + "|")
        //S.notice("nextScreen4Date |" + wvEvenDat4Pe.get._2 + "|")
        GedcomDateOptions.getKey(wvEvenDat4Pe.get._2) match {
          case "gdt_no_date" =>
            conf
          case "gdt_exact" =>
            //S.notice("nextScreen4Date is " + "gdt_exact")
            ymdDate  // C703-2/vsh instead of:  gdt_exact
          case "gdt_between" =>
            //S.notice("nextScreen4Date is " + "gdt_between")
            wvDateLabels.set(S ? "gd_bet", S ? "gd_and")
            ymdymdDate  //gdt_between
          case "gdt_before" =>
            //S.notice("nextScreen4Date is " + "gdt_before")
            wvDateLabels.set(S ? "gd_bef", wvDateLabels._2)
            ymdDate  //gdt_before
          case "gdt_after" =>
            wvDateLabels.set(S ? "gd_aft", wvDateLabels._2)
            ymdDate  //gdt_after
          case "gdt_about" =>
            wvDateLabels.set(S ? "gd_abt", wvDateLabels._2)
            ymdDate  //gdt_about
          case "gdt_from_to" =>
            wvDateLabels.set(S ? "gd_from", S ? "gd_to")
            ymdymdDate  //gdt_from_to
          case "gdt_from" =>
            wvDateLabels.set(S ? "gd_from", wvDateLabels._2)
            ymdDate  //gdt_from
          case "gdt_to" =>
            wvDateLabels.set(S ? "gd_to", wvDateLabels._2)
            ymdDate  //gdt_to
          case theRest /* includes "gdt_text"*/ =>
            wvDateLabels.set(S ? "gd_txt", wvDateLabels._2)
            gdt_text
        }
      case "attrib" =>
        //S.notice("nextScreen4Date " + wvAttrDat4Pe.get._2)
        GedcomDateOptions.getKey(wvAttrDat4Pe.get._2) match {
          case "gdt_no_date" =>
            conf
          case "gdt_exact" =>
            //S.notice("nextScreen4Date is " + "gdt_exact")
            ymdDate  // C703-2/vsh instead of:  gdt_exact
          case "gdt_between" =>
            //S.notice("nextScreen4Date is " + "gdt_between")
            wvDateLabels.set(S ? "gd_bet", S ? "gd_and")
            ymdymdDate  //gdt_between
          case "gdt_before" =>
            //S.notice("nextScreen4Date is " + "gdt_before")
            wvDateLabels.set(S ? "gd_bef", wvDateLabels._2)
            ymdDate  //gdt_before
          case "gdt_after" =>
            wvDateLabels.set(S ? "gd_aft", wvDateLabels._2)
            ymdDate  //gdt_after
          case "gdt_about" =>
            wvDateLabels.set(S ? "gd_abt", wvDateLabels._2)
            ymdDate  //gdt_about
          case "gdt_from_to" =>
            wvDateLabels.set(S ? "gd_from", S ? "gd_to")
            ymdymdDate  //gdt_from_to
          case "gdt_from" =>
            wvDateLabels.set(S ? "gd_from", wvDateLabels._2)
            ymdDate  //gdt_from
          case "gdt_to" =>
            wvDateLabels.set(S ? "gd_to", wvDateLabels._2)
            ymdDate  //gdt_to
          case theRest /* includes "gdt_text"*/ =>
            wvDateLabels.set(S ? "gd_txt", wvDateLabels._2)
            gdt_text
        }
    }
  }*/


  /*val ymdDate  = new Screen {
    val dateInit = wvDPAS._1
    val dateNew = field(S ? wvDateLabels.get._1, dateInit, "size"->"11", "maxlength"->"11",
      isIncompletedate _
    )
    override def screenTop = Full(<span>{GedcomDateOptions.getMsg(wvEvenDat4Pe.get._2)}: {dateFormat}</span>)
    override def nextScreen = {
      wvDPAS.set(<_>{S ? wvDateLabels.get._1} {dateNew.get.trim}</_>.text.trim,
        wvDPAS.get._2, wvDPAS.get._3, wvDPAS.get._4, wvDPAS.get._5)
      conf
    }
    def isIncompletedate(s: String): List[FieldError] = {
      GedcomUtil.valiDate(s) match {
        case true => Nil
        case _ => S.?("date.is.invalid")
      }
    }
  }*/


/*
  val ymdymdDate = new Screen {
// TODO CB18-7 wrong LT case date after edition
    val dateLowerInit =  wvDPAS._1
// TODO B308-2 possibly extract lower date if any
    val dateLowerNew = field(S ? wvDateLabels.get._1, dateLowerInit, "size"->"11", "maxlength"->"11",
      isIncompletedate _)
    val dateUpperInit =  wvDPAS._1
// TODO B308-2 possibly extract upper date if any
    val dateUpperNew = field(S ? wvDateLabels.get._2, dateUpperInit, "size"->"11", "maxlength"->"11",
      isIncompletedate _, mustRightRelate _ )
    override def screenTop = Full(<span>{GedcomDateOptions.getMsg(wvEvenDat4Pe.get._2)}: {dateFormat} - {dateFormat}</span>)
    override def nextScreen = {
      wvDPAS.set(<_>{S ? wvDateLabels.get._1} {dateLowerNew.get.trim} {S ? wvDateLabels.get._2} {dateUpperNew.get.trim}</_>.text.trim,
        wvDPAS.get._2, wvDPAS.get._3, wvDPAS.get._4, wvDPAS.get._5)
      conf
    }

    def isIncompletedate(s: String): List[FieldError] = {
      GedcomUtil.valiDate(s) match {
        case true => Nil
        case _ => S.?("date.is.invalid")
      }
    }

    // http://stackoverflow.com/questions/237061/using-regular-expressions-to-extract-a-value-in-java
    def mustRightRelate(s: String): List[FieldError] = {
      val isoLowerDate = GedcomUtil.iso8601Date(dateLowerNew.get).toInt
      val isoUpperDate = GedcomUtil.iso8601Date(dateUpperNew.get).toInt
      (isoLowerDate, isoUpperDate) match {
        case (l, u) if l * u == 0 =>  S.?("date.is.invalid")
        case (l, u) if l < u =>  Nil
        case (l, u) =>  S.?("date.is.invalid")
      }
    }

  }
*/


/*
  val gdt_text = new Screen {
    log.debug("Screen=gdt_text: " + GedcomDateOptions.tags.find(_._1 == wvEvenDat4Pe.get._2).get._2)
    val dateInit = ""
    val dateNew = field(S ? "gd_txt", dateInit,
      "size"->"10", "maxlength"->"120", ("style","font:bold,display:none"),
      valMinLen(1, S ? "no.text")
    )
    override def screenTop = Full(<span>{GedcomDateOptions.getMsg(wvEvenDat4Pe.get._2)}</span>)
    override def nextScreen = {
      wvDPAS.set("("+dateNew.get+")", wvDPAS.get._2, wvDPAS.get._3, wvDPAS.get._4, wvDPAS.get._5)
      conf
    }
  }
*/


  val conf = new Screen {
    override def confirmScreen_? = true
    override def nextScreen = Empty
  }


  private object wvPE extends WizardVar[(/*id*/Long,
    /*tag*/String, /*familyId(when BIRT CHR ADOP)*/Long, /*adoptedBy*/String)](0L, "", 0L, "")
  private object wvPA extends WizardVar[(/*id*/Long,
    /*tag*/String, /*tagValue*/String)] (0L, "", "")
  private object wvED extends WizardVar[(/*id*/Long,
    /*descriptor (for EVENT)*/String, /*dateValue*/String, /*place*/String, /*ageAtEvent*/String,
    /*cause for DEAT*/String, /*source*/String, /*note*/String)](0L, "", "", "", "", "", "", "")

// TODO B308-2/vsh sutvarkyti /*familyId(when BIRT CHR ADOP)*/

  def finish() {
    wvEA.get match {
      case "event" =>
        val msg = <_>wvEvenDat4Pe: ({wvEvenDat4Pe.get.toString()})</_>.text + " | " +
          <_>wvDPAS: ({wvDPAS.get.toString()})</_>.text + " | " +
          <_>wvADOP: ({wvADOP.get.toString})</_>.text + " | " +
          <_>wvEVEN: ({wvEVEN.get.toString})</_>.text + " | " +
          <_>wvDEAT: ({wvDEAT.get.toString})</_>.text
        log.debug(msg)
        S.notice(msg)
        wvPE.set(wvPE.get._1, wvEvenDat4Pe.get._1, wvPE.get._3,
          (if (wvEvenDat4Pe.get._1 == "ADOP") wvADOP.get else "") )
        wvED.set(wvED.get._1,
         (if (wvEvenDat4Pe.get._1 == "EVEN") wvEVEN.get else ""),
         wvDPAS.get._1,
         wvDPAS.get._2,
         wvDPAS.get._3,
         (if (wvEvenDat4Pe.get._1 == "DEAT") wvDEAT.get else ""),
         wvDPAS.get._4, wvDPAS.get._5)
        val msg2 = <_>wvPE: ({wvPE.get.toString()})</_>.text + " | " +
          <_>wvED: ({wvED.get.toString()})</_>.text
        log.debug(msg2)
        S.notice(msg2)

        if (true/*validResult*/) {
          actionCUD match {
            case "C" =>
              var pe: PersonEvent = new PersonEvent
              log.debug("PeWizard.finish.C PersonEvent.id |" + pe.id.toString + "|")
              var peClone: Box[PersonEventClone] = Empty
              var ed: EventDetail = new EventDetail
              var edClone: Box[EventDetailClone] = Empty
              pe.personevent = wvBoxPerson.is.open_!
              pe.tag = wvEvenDat4Pe.get._1
              pe.tag match {
                case "ADOP" =>
                  // pe.familyId = 0L
                  pe.adoptedBy = wvADOP.get
                case "EVEN" =>
                  // addupdLangMsg(dbField: String, msg: String, lang: String)
                  ed.descriptor = /*wvEVEN.get*/ wvEDMLT.get._1.addupdLangMsg(/*"descriptor", */wvEVEN.get/*, S.locale.getLanguage.toLowerCase*/)
                case "DEAT" =>
                  ed.cause = /*wvDEAT.get*/  wvEDMLT.get._4.addupdLangMsg(/*"cause", */wvDEAT.get/*, S.locale.getLanguage.toLowerCase*/)
// TODO  C6221-4 no cause value when editting
                case _ =>
              }
              ed.dateValue = GedcomUtil.gedcomizeI18nDate(wvDPAS.get._1)
              ed.place = wvEDMLT.get._3.addupdLangMsg(wvDPAS.get._2)
              ed.ageAtEvent = AgeAtEvent.doGedcomAgeAtEvent(wvDPAS.get._3)
              ed.source = wvEDMLT.get._5.addupdLangMsg(wvDPAS.get._4)
              ed.note = wvEDMLT.get._6.addupdLangMsg(wvDPAS.get._5)

              log.debug("PeWizard.finish wvBoxCU.is CurrentUser |" + CurrentUser.is + "|")
              log.debug("PeWizard.finish wvBoxCU.is wvBoxCU |" + wvBoxCU.is + "|")

              pe.setSubmitter(wvBoxCU.is.open_!)
              pe = Model.merge(pe)

              var pea = new Audit
              pea.setFields(wvBoxCU.is.open_!, "PE", pe.id, "add", pe.getAuditRec(peClone))
              pea = Model.merge(pea)

              ed.personevent = pe
              ed.setSubmitter(wvBoxCU.is.open_!)
              log.debug("PeWizard.finish ed.dateValue |" + ed.dateValue + "|")
              ed = Model.merge(ed)

              var eda = new Audit
              eda.setFields(wvBoxCU.is.open_!, "ED", ed.id, "add", ed.getAuditRec(edClone))
              eda = Model.merge(eda)
              Model.flush()
            case "U" => // ----------------------------------------------------
// TODO B412-2/vsh  ....get.get.id).get ==> ???
              var pe: PersonEvent = Model.find(classOf[PersonEvent], wvBoxPersonEvent.get.get.id).get
              log.debug("PeWizard.finish.U PersonEvent.id |" + pe.id.toString + "|")
              //-- case class PersonEventClone (tag:String, familyId:String, adoptedBy:String,  personevent_id:String)
              val peClone: Box[PersonEventClone] =
                Full(PersonEventClone(pe.tag, pe.familyId.toString, pe.adoptedBy, pe.personevent.id.toString))
              var ed: EventDetail = Model.find(classOf[EventDetail], wvBoxEventDetail.get.get.id).get
              val edClone: Box[EventDetailClone] = Full(EventDetailClone(
                ed.descriptor, ed.dateValue, ed.place, ed.ageAtEvent, ed.cause, ed.source, ed.note,
                (if (ed.personevent==null) "0" else ed.personevent.id.toString),
                (if (ed.personattrib==null) "0" else ed.personattrib.id.toString),
                (if (ed.familyevent==null) "0" else ed.familyevent.id.toString) ))
              pe.personevent = wvBoxPerson.is.open_!
              pe.tag = wvEvenDat4Pe.get._1
              pe.tag match {
                case "ADOP" =>
                  // pe.familyId = 0L
                  pe.adoptedBy = wvADOP.get
                case "EVEN" =>
                  ed.descriptor = wvEDMLT.get._1.addupdLangMsg(wvEVEN.get)
                case "DEAT" =>
                  ed.cause = wvEDMLT.get._4.addupdLangMsg(wvDEAT.get)
                case _ =>
              }
              ed.dateValue = GedcomUtil.gedcomizeI18nDate(wvDPAS.get._1)
              ed.place = wvEDMLT.get._3.addupdLangMsg(wvDPAS.get._2)

              ed.ageAtEvent = AgeAtEvent.doGedcomAgeAtEvent(wvDPAS.get._3)
              ed.source = wvEDMLT.get._5.addupdLangMsg(wvDPAS.get._4)
              ed.note = wvEDMLT.get._6.addupdLangMsg(wvDPAS.get._5)

              log.debug("PeWizard.finish wvBoxCU.is CurrentUser |" + CurrentUser.is + "|")
              log.debug("PeWizard.finish wvBoxCU.is wvBoxCU |" + wvBoxCU.is + "|")

              pe.setSubmitter(wvBoxCU.is.open_!)
              pe = Model.merge(pe)

              var pea = new Audit
              pea.setFields(wvBoxCU.is.open_!, "PE", pe.id, "upd", pe.getAuditRec(peClone))
              pea = Model.merge(pea)

              ed.personevent = pe
              ed.setSubmitter(wvBoxCU.is.open_!)
              ed = Model.merge(ed)

              var eda = new Audit
              eda.setFields(wvBoxCU.is.open_!, "ED", ed.id, "upd", ed.getAuditRec(edClone))
              eda = Model.merge(eda)
              Model.flush()
            case _ =>
          }

        } else {
          val place = "PeWizard finish event"
          val msg = ("Validation is unsuccessful")
          log.error(place+"; "+msg)
          S.redirectTo("/errorPage", () => {
            ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
          })
        }

      case "attrib" =>
        val msg3 = <_>wvAttrDat4Pe: ({wvAttrDat4Pe.get.toString()})</_>.text + " | " +
          <_>wvDPAS: ({wvDPAS.get.toString()})</_>.text
        log.debug(msg3)
        S.notice(msg3)
        wvPA.set((wvPA.get._1, wvAttrDat4Pe.get._1, wvXXXX.get._1))
        wvED.set(wvED.get._1,
         (if (wvAttrDat4Pe.get._1 == "EVEN") wvEVEN.get else ""),  // NONSENSE a bit
         wvDPAS.get._1,
         wvDPAS.get._2,
         wvDPAS.get._3,
         (if (wvAttrDat4Pe.get._1 == "DEAT") wvDEAT.get else ""),  // NONSENSE a bit
         wvDPAS.get._4, wvDPAS.get._5)
        val msg4 = <_>wvPA: ({wvPA.get.toString()})</_>.text + " | " +
          <_>wvED: ({wvED.get.toString()})</_>.text
        log.debug(msg4)
        S.notice(msg4)
        if (true/*validResult*/) {
          actionCUD match {
            case "C" =>
              var pa: PersonAttrib = new PersonAttrib
              var paClone: Box[PersonAttribClone] = Empty
              var ed: EventDetail = new EventDetail
              var edClone: Box[EventDetailClone] = Empty
                pa.personattrib = wvBoxPerson.is.open_!
                pa.tag = wvAttrDat4Pe.get._1
                pa.tag match {
                  case "RESI" =>
                  case aTag =>
                    pa.tagValue = wvXXXX.get._2.addupdLangMsg(wvXXXX.get._1)
                }
              ed.dateValue = GedcomUtil.gedcomizeI18nDate(wvDPAS.get._1)
              ed.place = wvEDMLT.get._3.addupdLangMsg(wvDPAS.get._2)
              ed.ageAtEvent = AgeAtEvent.doGedcomAgeAtEvent(wvDPAS.get._3)
              ed.source = wvEDMLT.get._5.addupdLangMsg(wvDPAS.get._4)    //log.debug("PeWizard.finish wvBoxCU.is CurrentUser |" + CurrentUser.is + "|")
              ed.note = wvEDMLT.get._6.addupdLangMsg(wvDPAS.get._5)      //log.debug("PeWizard.finish wvBoxCU.is CurrentUser |" + CurrentUser.is + "|")
              //log.debug("PeWizard.finish wvBoxCU.is wvBoxCU |" + wvBoxCU.is + "|")

              pa.setSubmitter(wvBoxCU.is.open_!)
              pa = Model.merge(pa)

              var paa = new Audit
              paa.setFields(wvBoxCU.is.open_!, "PA", pa.id, "add", pa.getAuditRec(paClone))
              paa = Model.merge(paa)

              ed.personattrib = pa
              ed.setSubmitter(wvBoxCU.is.open_!)
              ed = Model.merge(ed)

              var eda = new Audit
              eda.setFields(wvBoxCU.is.open_!, "ED", ed.id, "add", ed.getAuditRec(edClone))
              eda = Model.merge(eda)
              Model.flush()
            case "U" =>
              var pa: PersonAttrib = Model.find(classOf[PersonAttrib], wvBoxPersonAttrib.get.get.id).get
              //-- case class PersonAttribClone (tag:String, tagValue:String, personattrib_id:String)
              val paClone: Box[PersonAttribClone] = //Empty
                Full(PersonAttribClone(pa.tag, pa.tagValue, pa.personattrib.id.toString))
              var ed: EventDetail = Model.find(classOf[EventDetail], wvBoxEventDetail.get.get.id).get
              //var edClone: Box[EventDetailClone] = Empty
              val edClone: Box[EventDetailClone] = Full(EventDetailClone(
                ed.descriptor, ed.dateValue, ed.place, ed.ageAtEvent, ed.cause, ed.source, ed.note,
                (if (ed.personevent==null) "0" else ed.personevent.id.toString),
                (if (ed.personattrib==null) "0" else ed.personattrib.id.toString),
                (if (ed.familyevent==null) "0" else ed.familyevent.id.toString) ))
              pa.personattrib = wvBoxPerson.is.open_!
              pa.tag = wvAttrDat4Pe.get._1
              pa.tag match {
                case "RESI" =>
                case aTag =>
                  pa.tagValue = wvXXXX.get._2.addupdLangMsg(wvXXXX.get._1)
              }
              ed.dateValue = GedcomUtil.gedcomizeI18nDate(wvDPAS.get._1)
              ed.place = wvEDMLT.get._3.addupdLangMsg(wvDPAS.get._2)
              ed.ageAtEvent = AgeAtEvent.doGedcomAgeAtEvent(wvDPAS.get._3)
              ed.source = wvEDMLT.get._5.addupdLangMsg(wvDPAS.get._4)            //log.debug("PeWizard.finish wvBoxCU.is CurrentUser |" + CurrentUser.is + "|")
              ed.note = wvEDMLT.get._6.addupdLangMsg(wvDPAS.get._5)              //log.debug("PeWizard.finish wvBoxCU.is CurrentUser |" + CurrentUser.is + "|")
              //log.debug("PeWizard.finish wvBoxCU.is CurrentUser |" + CurrentUser.is + "|")
              //log.debug("PeWizard.finish wvBoxCU.is wvBoxCU |" + wvBoxCU.is + "|")

              pa.setSubmitter(wvBoxCU.is.open_!)
              pa = Model.merge(pa)

              var paa = new Audit
              paa.setFields(wvBoxCU.is.open_!, "PA", pa.id, "upd", pa.getAuditRec(paClone))
              paa = Model.merge(paa)

              ed.personattrib = pa
              ed.setSubmitter(wvBoxCU.is.open_!)
              ed = Model.merge(ed)

              var eda = new Audit
              eda.setFields(wvBoxCU.is.open_!, "ED", ed.id, "upd", ed.getAuditRec(edClone))
              eda = Model.merge(eda)
              Model.flush()
            case _ =>
          }

        } else {
          val place = "PeWizard finish attrib"
          val msg = ("Validation is unsuccessful")
          log.error(place+"; "+msg)
          S.redirectTo("/errorPage", () => {
            ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
          })
        }

      case _ =>
    }
    wvPE.remove()
    wvPA.remove()
    wvED.remove()
    log.debug("/rest/personView/" + wvBoxPerson.get.open_!.id)
    S.redirectTo("/rest/personView/" + wvBoxPerson.get.open_!.id) //})
  }

  //def ajaxRender = "* [onclick]" #> SHtml.ajaxInvoke(() =>
  //  ModalDialog(<div>
  //    <lift:PeWizard ajax="true"/>
  //  </div>))


  def getEventAttribData()/*: Unit =*/ {
    S.getSessionAttribute("personEventId") match {
      case Full(personEventId) =>
        log.debug("PeWizard getEventAttribData personEventId case Full")
        val personEvent: Option[PersonEvent] = Model.find(classOf[PersonEvent], personEventId.toLong)
        personEvent match {
          case Some(x) =>
            log.debug("PeWizard getEventAttribData personEventId case Some(x)")
            val pe = x
            wvBoxPersonEvent.set(Full(pe))
            pe.getEventDetail(Model.getUnderlying)
            wvBoxPerson.set(Full(pe.personevent))
            //wvFE.set((pe.id, pe.tag))
            //  //-- extends WizardVar[(/*id*/Long, /*tag*/String)] (0L, "")
            //log.debug("PeWizard getFamilyData wvFE.get "+wvFE.get.toString())
            pe.eventdetails.size match {
              case 1 =>
                log.debug("PeWizard getEventAttribData personEventId case 1")
                actionCUD = "U" //-- C create, U update, D delete
                updatePePa = "PE"
                wvEA.set("event")
                val ped: EventDetail = pe.eventdetails.iterator.next
                wvBoxEventDetail.set(Full(ped))
                wvEvenDat4Pe.set((PeTags.getMsg(pe.tag), GedcomDateOptions.defineDateShape(ped.dateValue)/*"gdt_exact"*/))
                wvDPAS.set((GedcomUtil.i18nizeGedcomDate(ped.dateValue), ped.place, AgeAtEvent.doLocaleAgeAtEvent(ped.ageAtEvent), ped.source, ped.note))
                wvEDMLT.set ((new MultiLangText("descriptor", ped.descriptor),
                  new MultiLangText("dateValue", GedcomUtil.i18nizeGedcomDate(ped.dateValue)),
                  new MultiLangText("place", ped.place),
                  new MultiLangText("cause", ped.cause),
                  new MultiLangText("source", ped.source),
                  new MultiLangText("note", ped.note) ))

                wvADOP.set(("BOTH"))
                val mltDescr = new MultiLangText("descriptor", ped.descriptor)
                wvEVEN.set(mltDescr.getLangMsg())
                log.debug("PeWizard getEventAttribData wvEVEN.get "+wvEVEN.get.toString)
                val mltCause = new MultiLangText("cause", ped.cause)
                wvDEAT.set(mltDescr.getLangMsg())
                log.debug("PeWizard getEventAttribData wvDEAT.get "+wvDEAT.get.toString)
              case 0 =>
                val place = "PeWizard getEventAttribData"
                val msg = ("PeWizard getEventAttribData: There is no EventDetail for PersonEvent id = " + personEventId)
                log.error(place+"; "+msg)
                S.redirectTo("/errorPage", () => {
                  ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))}
                )
              case n =>
                val place = "PeWizard getEventAttribData"
                val msg = ("PeWizard getEventAttribData: There are more than 1 EventDetail for PersonEvent id = " + personEventId)
                log.error(place+"; "+msg)
                S.redirectTo("/errorPage", () => {
                  ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))}
                )
            }
          case None =>
            val place = "PeWizard getEventAttribData"
            val msg = ("PeWizard getEventAttribData: There is no PersonEvent id = " + personEventId)
            log.error(place+"; "+msg)
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))}
            )
          case _ =>
            val place = "PeWizard getEventAttribData"
            val msg = ("PeWizard getEventAttribData: Case _  for PersonEvent id = " + personEventId)
            log.error(place+"; "+msg)
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))}
            )
        }
      case _ =>
        log.debug("PeWizard getEventAttribData personEventId case _")
        updatePePa = ""
    }
    S.unsetSessionAttribute("personEventId")
    if (updatePePa.size > 0) {
      S.unsetSessionAttribute("personAttribId")
      return
    }
    S.getSessionAttribute("personAttribId") match {
      case Full(personAttribId) =>
        val personAttrib: Option[PersonAttrib] = Model.find(classOf[PersonAttrib], personAttribId.toLong)
        personAttrib match {
          case Some(x) =>
            val pa = x
            wvBoxPersonAttrib.set(Full(pa))
            pa.getAttribDetail(Model.getUnderlying)
            wvBoxPerson.set(Full(pa.personattrib))
            //wvFE.set((pe.id, pe.tag))
            //  //-- extends WizardVar[(/*id*/Long, /*tag*/String)] (0L, "")
            //log.debug("PeWizard getFamilyData wvFE.get "+wvFE.get.toString())
            pa.attribdetails.size match {
              case 1 =>
                actionCUD = "U" //-- C create, U update, D delete
                updatePePa = "PA"
                wvEA.set("attrib")
                val pad: EventDetail = pa.attribdetails.iterator.next
                wvBoxEventDetail.set(Full(pad))
                /*wvEvenDat4Pa*/
                wvAttrDat4Pe.set((PaTags.getMsg(pa.tag), GedcomDateOptions.defineDateShape(pad.dateValue)/*"gdt_exact"*/))  // ("", "gdt_exact")
                wvDPAS.set((GedcomUtil.i18nizeGedcomDate(pad.dateValue), pad.place, AgeAtEvent.doLocaleAgeAtEvent(pad.ageAtEvent), pad.source, pad.note))   // ("", "", "", "")
                wvEDMLT.set ((new MultiLangText("descriptor", pad.descriptor),
                  new MultiLangText("dateValue", GedcomUtil.i18nizeGedcomDate(pad.dateValue)),
                  new MultiLangText("place", pad.place),
                  new MultiLangText("cause", pad.cause),
                  new MultiLangText("source", pad.source),
                  new MultiLangText("note", pad.note) ))
                val mlt = new MultiLangText("tagValue", pa.tagValue)
                wvXXXX.set((mlt.getLangMsg(), mlt))
                log.debug("PeWizard getEventAttribData wvXXXX.get "+wvXXXX.get.toString())
              case 0 =>
                val place = "PeWizard getEventAttribData"
                val msg = ("PeWizard getEventAttribData: There is no EventDetail for PersonAttrib id = " + personAttribId)
                log.error(place+"; "+msg)
                S.redirectTo("/errorPage", () => {
                  ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))}
                )
              case n =>
                val place = "PeWizard getEventAttribData"
                val msg = ("PeWizard getEventAttribData: There are more than 1 EventDetail for PersonAttrib id = " + personAttribId)
                log.error(place+"; "+msg)
                S.redirectTo("/errorPage", () => {
                  ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))}
                )
            }
          case None =>
            val place = "PeWizard getEventAttribData"
            val msg = ("PeWizard getEventAttribData: There is no PersonAttrib id = " + personAttribId)
            log.error(place+"; "+msg)
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))}
            )
          case _ =>
            val place = "PeWizard getEventAttribData"
            val msg = ("PeWizard getEventAttribData: Case _  for PersonAttrib id = " + personAttribId)
            log.error(place+"; "+msg)
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))}
            )
        }
      case _ =>
        updatePePa = ""
    }
    S.unsetSessionAttribute("personAttribId")
  }

// D228-4/vsh tai negali veikti Screen'e i6 principo !!!
//  def displayYesNone(): String = {
//    log.debug("displayYesNone wvEvenDat4Pe.get._2 |" + wvEvenDat4Pe.get._2 + "|")
//    wvEA.get match {
//      case "event" =>
//        wvEvenDat4Pe.get._2 match {
//          case "gdt_no_date" => log.debug("displayYesNone event \"gdt_no_date\" "); "display:none"
//          case _ => log.debug("displayYesNone event \"_\" "); "display:yes"
//        }
//      case "attrib" =>
//        wvAttrDat4Pe.get._2 match {
//          case "gdt_no_date" => "display:none"
//          case _ => "display:yes"
//        }
//      case _ => log.debug("displayYesNone \"_\" \"_\" "); "display:yes"
//    }
//  }

}
