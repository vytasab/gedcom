package lt.node.gedcom.snippet

import org.slf4j.{LoggerFactory, Logger}

import _root_.net.liftweb._
import http._
import js.jquery.JqJsCmds._
import wizard._
import common._
import _root_.net.liftweb.util.Helpers._
import _root_.net.liftweb.util.FieldError

// B302-3/vsh

import _root_.bootstrap.liftweb.{AccessControl,RequestedURL,ErrorXmlMsg,CurrentUser}
import _root_.lt.node.gedcom.model._
import _root_.lt.node.gedcom.util._

/**
 * google-group: Lift [Seven Things that distinguish Lift from other web frameworks]
 */


class AddFaWizardRunner {
  /*def render = "* [onclick]" #> SHtml.ajaxInvoke(() =>
    ModalDialog((<div><lift:FaWizard ajax="true"/><br/></div>),
    JsObj(("top","200px"),("left","300px"),("width","600px"),("height","600px"))))*/
}

class FaWizard extends Wizard with Loggable {

  val log: Logger = LoggerFactory.getLogger("FaWizard");

  if (!AccessControl.isAuthenticated_?()) S.redirectTo("/")

  RequestedURL(Full(S.referer.openOr("gedcom/personView")))

  log.debug("FaWizard wvBoxCU.is CurrentUser |" + CurrentUser.is + "|")
  private object wvBoxCU extends WizardVar[Box[User]](CurrentUser.is)
  log.debug("FaWizard wvBoxCU.is wvBoxCU |" + wvBoxCU.is + "|")
  /*private*/ object wvBoxFamily extends WizardVar[Box[Family]](Empty)
  /*private*/ object wvBoxFamilyEvent extends WizardVar[Box[FamilyEvent]](Empty)
  /*private*/ object wvBoxEventDetail extends WizardVar[Box[EventDetail]](Empty)
  private object wvEvenDat4Fa extends WizardVar[(String, String)]("", "gdt_exact")
  private object wvDPAS extends WizardVar[(String, String, String, String, String)]("", "", "", "", "")
  private object wvEDMLT extends WizardVar [(MultiLangText, MultiLangText, MultiLangText,
    MultiLangText, MultiLangText, MultiLangText)] (
    new MultiLangText("descriptor", ""),
    new MultiLangText("dateValue", ""),
    new MultiLangText("place", ""),
    new MultiLangText("cause", ""),
    new MultiLangText("source", ""),
    new MultiLangText("note", "") )
  private object wvEVEN extends WizardVar[String]("")
  // for FamilyEvent EventDetail.cause

// TODO B411-1/vsh  wwFE wvED  ar reikalingi ?
  private object wvFE extends WizardVar[(/*id*/Long, /*tag*/String)] (0L, "")
  private object wvED extends WizardVar[(/*id*/Long,
    /*descriptor (for EVENT)*/String, /*dateValue*/String, /*place*/String, /*ageAtEvent*/String,
    /*cause for DEAT*/String, /*note*/String, /*source*/String)](0L, "", "", "", "", "", "", "")
  private object wvRootPersonId extends WizardVar[String](personVar.get.get.id.toString)
  val dateFormat = GedcomDateOptions.msg4Date(S.get("locale").getOrElse("en"))


  var actionCUD = "C" //-- MUST be initialized with "C";    C add, U update, D delete

  getFamilyData()
  log.debug("FaWizard wvBoxFamily after getFamilyData |" + wvBoxFamily.toString + "|")

  override protected def calcAjaxOnDone = Unblock

  override def calcFirstScreen = { //  : Box[Screen]
    log.debug("FaWizard calcFirstScreen  []...")
    actionCUD match  {
      case "C" =>
        log.debug("FaWizard calcFirstScreen C")
        Full(selFeTag)
      case "U" =>
        log.debug("FaWizard calcFirstScreen U")
        Full(selFeTag)
      case _ =>
        log.debug("FaWizard calcFirstScreen  ...[]")
        Empty
    }
  }

  val selFeTag = new Screen {
    val tagInit = wvEvenDat4Fa.get._1 match {
      case "" => FeTags.tags.map(_._2).head
      case _ => wvEvenDat4Fa.get._1
    }
    val tagNew = select(S ? "add.event", tagInit, FeTags.tags.map(_._2)
      , "size"->"9"
      , "title"->ToolTips.getMsg("age_at_event")
    )
    //val dateoptionsInit = GedcomDateOptions.getMsg(wvEvenDat4Pe.get._2)
    //val dateoptionsNew = select(S ? "pe.dateShape", dateoptionsInit,
    //  GedcomDateOptions.tags.filter( _._1 != "gdt_and").map(_._2), "size"->"10")
    val dateoptionsInit = GedcomDateOptions.getMsg(wvEvenDat4Fa.get._2)
    val dateoptionsNew = select(S ? "pe.dateShape", dateoptionsInit,
      GedcomDateOptions.tags.filter( _._1 != "gdt_and").map(_._2), "size" -> "10")


    override def nextScreen = {
      log.debug("selFeTag tagNew.get |" + tagNew.get + "|")
      log.debug("selFeTag FeTags.tags |" + FeTags.tags.toString() + "|")
      log.debug("selFeTag FeTags.tags.find(_._2 == tagNew.get).get._1 |" + FeTags.tags.find(_._2 == tagNew.get).get._1 + "|")
      wvEvenDat4Fa.set(wvEvenDat4Fa.get._1, GedcomDateOptions.getKey(dateoptionsNew.get)/*dateoptionsNew.get*/)
      wvEvenDat4Fa.set((FeTags.tags.find(_._2 == tagNew.get).get._1, wvEvenDat4Fa.get._2))
    //wvEvenDat4Pe.set((PeTags.tags.find(_._2 == tagNew.get).get._1, wvEvenDat4Pe.get._2))


      //      S.notice(wvEvenDat4Fa.get.toString())
      tagNew.get/*.is*/ match {
        //case tagInit if tagInit == "" => selPeTag
        case newVal => FeTags.tags.find(_._2 == newVal).get._1 match {
          case "EVEN" => feTagEVEN
          case _ => feTagXXXX
        }
      }
    }
  }


  val feTagEVEN = new Screen {
    val descriptorInit = wvEVEN.get
    //val descriptorNew = field(S ? "pe.descriptor", descriptorInit, "style" -> "display:yes").toString
    //val descriptorInit = wvEVEN.get
    val descriptorNew = textarea/*field*/(S ? "pe.descriptor", descriptorInit,
      "style"->"display:yes", "class"->"textarea-small")

    val dateInit = wvDPAS._1 //GedcomDateOptions.dateInitValue(wvEvenDat4Fa.get._2)(S.locale.getLanguage)
    val dateNew = textarea((S ? "pe.dateValue")+ ": " + GedcomDateOptions.dateInitValue(wvEvenDat4Fa.get._2)(S.locale.getLanguage),
        dateInit, "style"->"display:yes", "class"->"textarea-4date", isValiDate _ )
    //val dateoptionsInit = GedcomDateOptions.getMsg(wvEvenDat4Fa.get._2)
    //val dateoptionsNew = select(S ? "pe.dateValue", dateoptionsInit, GedcomDateOptions.tags.filter( _._1 != "gdt_and").map(_._2), "size" -> "9")

    val placeInit = wvEDMLT._3.getLangMsg()
    //val placeNew = field(S ? "pe.place", placeInit, "style" -> "display:yes")
    val placeNew = textarea/*field*/(S ? "pe.place", placeInit,
      "style"->"display:yes", "maxlength"->"120", "class"->"textarea-small")

    val ageAtEventInit = wvDPAS.get._3
    //val ageAtEventNew = field(S ? "pe.ageAtEvent", ageAtEventInit,
    //  "title"->ToolTips.getMsg("age_at_event"),
    //  validateAAE _ )
    val ageAtEventNew = textarea(S ? "pe.ageAtEvent", ageAtEventInit,
      "title"->ToolTips.getMsg("age_at_event"), "class"->"textarea-small", validateAAE _)

    val sourceInit = wvEDMLT._5.getLangMsg()
    //val sourceNew = field(S ? "pe.source", sourceInit, "style" -> "display:none")
    val sourceNew = textarea(S ? "pe.source", sourceInit,
      "style"->"display:none", "maxlength"->"248", "class"->"textarea-small")

    val noteInit = wvEDMLT._6.getLangMsg()
    //val noteNew = field(S ? "pe.note", noteInit)
    val noteNew = textarea(S ? "pe.note", noteInit,
      "maxlength"->"248", "class"->"textarea-small")

    override def screenTop = Full(<span><b>{FeTags.getMsg(wvEvenDat4Fa.get._1)}</b></span>)

    def validateAAE(aae: String): List[FieldError] = { //validateAgeAtEvent aae
      AgeAtEvent.validateAgeAtEvent(aae) match {
        case true => Nil
        case _ => S.?("aae.is.invalid")
      }
    }

    def isValiDate(s: String): List[FieldError] = {
      GedcomDateOptions.valiDate(s, wvEvenDat4Fa.get._2) match {
        case "" =>
          //S.notice("Nil")
          Nil
        case msg if msg.length > 0 =>
          //S.notice("msg |"+msg+"|")
          msg  // S.?("date.is.invalid")
      }
    }

    override def nextScreen = {

      //wvEvenDat4Fa.set(wvEvenDat4Fa.get._1, dateoptionsNew.get)

      wvEVEN.set(descriptorNew)
      wvDPAS.set( /*wvDPAS.get._1*/dateNew, placeNew.get, ageAtEventNew.get, sourceNew.get, noteNew.get)
      conf/*nextScreen4Date*/
    }
  }


  val feTagXXXX = new Screen {

    val dateInit = wvDPAS._1
    val dateNew = textarea((S ? "pe.dateValue")+ ": " + GedcomDateOptions.dateInitValue(wvEvenDat4Fa.get._2)(S.locale.getLanguage),
      dateInit, "style"->"display:yes", "class"->"textarea-4date", isValiDate _ )
    //val dateoptionsInit = GedcomDateOptions.getMsg(wvEvenDat4Fa.get._2)
    //val dateoptionsNew = select(S ? "pe.dateValue", dateoptionsInit, GedcomDateOptions.tags.filter( _._1 != "gdt_and").map(_._2), "size" -> "9")

    val placeInit = wvEDMLT._3.getLangMsg()
    //val placeNew = field(S ? "pe.place", placeInit, "size"->"50", "maxlength"->"55")
      // B320-7/vsh nereaguoja į (2, 50):   textarea(S ? "pe.place", placeInit, 2, 50)
      // textarea(S ? "pe.place", placeInit)
    val placeNew = textarea/*field*/(S ? "pe.place", placeInit,
        "style"->"display:yes", "maxlength"->"120", "class"->"textarea-small")

    val ageAtEventInit = wvDPAS.get._3
    //val ageAtEventNew = field(S ? "pe.ageAtEvent", ageAtEventInit, "title"->ToolTips.getMsg("age_at_event"), validateAAE _ )
    val ageAtEventNew = textarea(S ? "pe.ageAtEvent", ageAtEventInit,
      "title"->ToolTips.getMsg("age_at_event"), "class"->"textarea-small", validateAAE _)

    val sourceInit = wvEDMLT._5.getLangMsg()
    //val sourceNew = field(S ? "pe.source", sourceInit, "style" -> "display:none")
    val sourceNew = textarea(S ? "pe.source", sourceInit,
      "style"->"display:none", "maxlength"->"248", "class"->"textarea-small")

    val noteInit = wvEDMLT._6.getLangMsg()
    //val noteNew = field(S ? "pe.note", noteInit)
    val noteNew = textarea(S ? "pe.note", noteInit,
      "maxlength"->"248", "class"->"textarea-small")

    override def screenTop = Full(<span><b>{FeTags.getMsg(wvEvenDat4Fa.get._1)}</b></span>)

    def validateAAE(aae: String): List[FieldError] = { //validateAgeAtEvent aae
      AgeAtEvent.validateAgeAtEvent(aae) match {
        case true => Nil
        case _ => S.?("aae.is.invalid")
      }
    }

    def isValiDate(s: String): List[FieldError] = {
      GedcomDateOptions.valiDate(s, wvEvenDat4Fa.get._2) match {
        case "" =>
          //S.notice("Nil")
          Nil
        case msg if msg.length > 0 =>
          //S.notice("msg |"+msg+"|")
          msg  // S.?("date.is.invalid")
      }
    }

    override def nextScreen = {
      //wvEvenDat4Fa.set(wvEvenDat4Fa.get._1, dateoptionsNew.get)
      wvDPAS.set( /*wvDPAS.get._1*/dateNew, placeNew.get, ageAtEventNew.get, sourceNew.get, noteNew.get)
      conf/*nextScreen4Date*/
    }
  }

  /*private object wvDateLabels extends WizardVar[(String, String)]("", "")*/

  /*def nextScreen4Date = {
        //S.notice("nextScreen4Date " + wvEvenDat4Fa.get._2)
        GedcomDateOptions.getKey(wvEvenDat4Fa.get._2) match {
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
  }*/


  /*val ymdDate  = new Screen {
    val dateInit = wvDPAS._1  // "yyyy [[MM [dd]]"
    val dateNew = field(S ? wvDateLabels.get._1, dateInit, "size" -> "11", "maxlength" -> "11", isIncompletedate _ )
    override def screenTop = Full(<span>{GedcomDateOptions.getMsg(wvEvenDat4Fa.get._2)}: {dateFormat}</span>)
    //override def screenBottom = Full(<span>{wvEvenDat4Fa.get._2}</span>)
    override def nextScreen = {
      wvDPAS.set(<_>{S ? wvDateLabels.get._1} {dateNew.get.trim}</_>.text.trim,
        wvDPAS.get._2, wvDPAS.get._3, wvDPAS.get._4, wvDPAS.get._5)
      conf
    }
    def isIncompletedate(s: String): List[FieldError] = {
      log.debug("FaWizard ymdDate s |" + s + "|")
      GedcomUtil.valiDate(s) match {
        case true => Nil
        case _ => S.?("date.is.invalid")
      }
    }
  }*/


  /*val ymdymdDate = new Screen {
    val dateLowerInit =  wvDPAS._1
    val dateLowerNew = field(S ? wvDateLabels.get._1, dateLowerInit, "size" -> "11", "maxlength" -> "11",
      isIncompletedate _)
    val dateUpperInit =  wvDPAS._1
    val dateUpperNew = field(S ? wvDateLabels.get._2, dateUpperInit, "size" -> "11", "maxlength" -> "11",
      isIncompletedate _, mustRightRelate _
    )
    override def screenTop = Full(<span>{GedcomDateOptions.getMsg(wvEvenDat4Fa.get._2)}: {dateFormat} - {dateFormat}</span>)
    //override def screenBottom = Full(<span>{wvEvenDat4Fa.get._2}</span>)
    override def nextScreen = {
      wvDPAS.set(<_>{S ? wvDateLabels.get._1} {dateLowerNew.get.trim} {S ? wvDateLabels.get._2} {dateUpperNew.get.trim}</_>.text.trim,
        wvDPAS.get._2, wvDPAS.get._3, wvDPAS.get._4, wvDPAS.get._5)
      conf
    }

    def isIncompletedate(s: String): List[FieldError] = {
      log.debug("FaWizard ymdymdDate s |" + s + "|")
      GedcomUtil.valiDate(s) match {
        case true => Nil
        case _ => S.?("date.is.invalid")
      }
    }*/

    /* http://stackoverflow.com/questions/237061/using-regular-expressions-to-extract-a-value-in-java */
    /*def mustRightRelate(s: String): List[FieldError] = {
      val isoLowerDate = GedcomUtil.iso8601Date(dateLowerNew.get).toInt
      val isoUpperDate = GedcomUtil.iso8601Date(dateUpperNew.get).toInt
      (isoLowerDate, isoUpperDate) match {
        case (l, u) if l * u == 0 =>  S.?("date.is.invalid")
        case (l, u) if l < u =>  Nil
        case (l, u) =>  S.?("date.is.invalid")
      }
    }

  }*/


  /*val gdt_text = new Screen {
    log.debug("Screen=gdt_text: " + GedcomDateOptions.tags.find(_._1 == wvEvenDat4Fa.get._2).get._2)
    val dateInit = ""
    val dateNew = field(S ? "gd_txt", dateInit,
      "size" -> "10", "maxlength" -> "120", ("style","font:bold,display:none"),
      valMinLen(1, S ? "no.text")
    )
    override def screenTop = Full(<span>{GedcomDateOptions.getMsg(wvEvenDat4Fa.get._2)}</span>)
    override def nextScreen = {
      wvDPAS.set(dateNew.get, wvDPAS.get._2, wvDPAS.get._3, wvDPAS.get._4, wvDPAS.get._5)
      conf
    }
  }*/


  val conf = new Screen {
    override def confirmScreen_? = true
    override def nextScreen = Empty
  }

// TODO B308-2/vsh sutvarkyti /*familyId(when BIRT CHR ADOP)*/

  def finish() {
    val msg = <_>wvEvenDat4Fa: (
      {wvEvenDat4Fa.get.toString()}
      )</_>.text + " | " +
      <_>wvDPAS: (
        {wvDPAS.get.toString()}
        )</_>.text + " | " +
      <_>wvEVEN: (
        {wvEVEN.get.toString()}
        )</_>.text + " | "
    log.debug(msg)
    S.notice(msg)
    wvED.set(wvED.get._1,
      (if (wvEvenDat4Fa.get._1 == "EVEN") wvEVEN.get else ""),
      wvDPAS.get._1,
      wvDPAS.get._2,
      wvDPAS.get._3,
      (if (wvEvenDat4Fa.get._1 == "DEAT") "" /*wvDEAT.get*/ else ""),
      wvDPAS.get._4, wvDPAS.get._5)
    val msg2 = <_>wvFE: (
      {wvFE.get.toString()}
      )</_>.text + " | " +
      <_>wvED: (
        {wvED.get.toString()}
        )</_>.text
    log.debug(msg2)
    S.notice(msg2)

    if (true /*validResult*/ ) {
      actionCUD match {
        case "C" =>
          var fe: FamilyEvent = new FamilyEvent
          log.debug("FaWizard.finish FamilyEvent.id |" + fe.id.toString() + "|")
          var feClone: Box[FamilyEventClone] = Empty
          var ed: EventDetail = new EventDetail
          var edClone: Box[EventDetailClone] = Empty
          fe.familyevent = wvBoxFamily.is.open_! // DC19-4/vsh  error occurs here
          fe.tag = wvEvenDat4Fa.get._1
          fe.tag match {
            case "EVEN" =>
              ed.descriptor = /*wvEVEN.get*/ wvEDMLT.get._1.addupdLangMsg(/*"descriptor", */ wvEVEN.get /*, S.locale.getLanguage.toLowerCase*/)
            case _ =>
          }
          // C626-2 ed.dateValue = /*wvDPAS.get._1*/  GedcomUtil.doGedcomDate(wvDPAS.get._1, wvEvenDat4Fa.get._2)
          ed.dateValue = GedcomUtil.gedcomizeI18nDate(wvDPAS.get._1)
          ed.place = wvEDMLT.get._3.addupdLangMsg(wvDPAS.get._2)
          ed.ageAtEvent = wvDPAS.get._3
          ed.source = wvEDMLT.get._5.addupdLangMsg(wvDPAS.get._4)
          ed.note = wvEDMLT.get._6.addupdLangMsg(wvDPAS.get._5)

          log.debug("FaWizard.finish wvBoxCU.is CurrentUser |" + CurrentUser.is + "|")
          log.debug("FaWizard.finish wvBoxCU.is wvBoxCU |" + wvBoxCU.is + "|")

          fe.setSubmitter(wvBoxCU.is.open_!)
          fe = Model.merge(fe)

          var fea = new Audit
          fea.setFields(wvBoxCU.is.open_!, "FE", fe.id, "add", fe.getAuditRec(feClone))
          fea = Model.merge(fea)

          ed.familyevent = fe
          ed.setSubmitter(wvBoxCU.is.open_!)
          ed = Model.merge(ed)

          var eda = new Audit
          eda.setFields(wvBoxCU.is.open_!, "ED", ed.id, "add", ed.getAuditRec(edClone))
          eda = Model.merge(eda)
          Model.flush

        case "U" =>
          // TODO B411-1/vsh  ....get.get.id).get ==> ???
          //log.debug("FaWizard.finish wvBoxFamilyEvent.get.get.familyevent |" + wvBoxFamilyEvent.get.get.familyevent.toString() + "|")
          //              var family: Family = Model.find(classOf[Family], wvBoxFamilyEvent.get.get.familyevent).get  ///*new FamilyEvent*/ wvBoxFamilyEvent.get.get
          //              log.debug("FaWizard.finish Family.id |" + family.id.toString() + "|")
          // TODO B411-1/vsh  ....get.get.id).get ==> ???
          var fe: FamilyEvent = Model.find(classOf[FamilyEvent], wvBoxFamilyEvent.get.get.id).get
          log.debug("FaWizard.finish FamilyEvent.id |" + fe.id.toString() + "|")
          val feClone: Box[FamilyEventClone] =
            Full(FamilyEventClone(fe.tag, fe.familyevent.id.toString()))

          //  TODO DC19-4/vsh Ne visai tvarkingas sprendimas, nes U atveju po taisymo neišlieka root person reikšmė
          //    reikėtų GedcomRest'e FE taisymo atveju nurodyti papikdimu S parametru root person Id'ą
          Math.random() match {
            case x if x < 0.5 && fe.familyevent.wifeId > 0 =>
              wvRootPersonId.set(fe.familyevent.wifeId.toString)
            case x if x < 0.5 && fe.familyevent.wifeId == 0 =>
              wvRootPersonId.set(fe.familyevent.husbandId.toString)
            case x if fe.familyevent.husbandId > 0 =>
              wvRootPersonId.set(fe.familyevent.husbandId.toString)
            case _ =>
              wvRootPersonId.set(fe.familyevent.wifeId.toString)
          }

          val family: Family = Model.find(classOf[Family], fe.familyevent.id).get //val family: Family = fe.familyevent
          log.debug("FaWizard.finish Family.id |" + family.id.toString() + "|")

          // TODO B411-1/vsh  ....get.get.id).get ==> ???
          var ed: EventDetail = Model.find(classOf[EventDetail], wvBoxEventDetail.get.get.id).get
          val edClone: Box[EventDetailClone] = Full(EventDetailClone(
            ed.descriptor, ed.dateValue, ed.place, ed.ageAtEvent, ed.cause, ed.source, ed.note,
            (if (ed.personevent == null) "0" else ed.personevent.id.toString),
            (if (ed.personattrib == null) "0" else ed.personattrib.id.toString),
            (if (ed.familyevent == null) "0" else ed.familyevent.id.toString)))
          fe.familyevent = family // wvBoxFamily.is.open_!
          fe.tag = wvEvenDat4Fa.get._1
          fe.tag match {
            case "EVEN" =>
              ed.descriptor = /*wvEVEN.get*/ wvEDMLT.get._1.addupdLangMsg(/*"descriptor", */ wvEVEN.get /*, S.locale.getLanguage.toLowerCase*/)
            case _ =>
          }
          // C626-2 ed.dateValue = /*wvDPAS.get._1*/  GedcomUtil.doGedcomDate(wvDPAS.get._1, wvEvenDat4Fa.get._2)
          ed.dateValue = GedcomUtil.gedcomizeI18nDate(wvDPAS.get._1)
          ed.place = wvEDMLT.get._3.addupdLangMsg(wvDPAS.get._2)
          ed.ageAtEvent = wvDPAS.get._3
          ed.source = wvEDMLT.get._5.addupdLangMsg(wvDPAS.get._4)
          ed.note = wvEDMLT.get._6.addupdLangMsg(wvDPAS.get._5)

          log.debug("FaWizard.finish wvBoxCU.is CurrentUser |" + CurrentUser.is + "|")
          log.debug("FaWizard.finish wvBoxCU.is wvBoxCU |" + wvBoxCU.is + "|")

          fe.setSubmitter(wvBoxCU.is.open_!)
          fe = Model.merge(fe)

          var fea = new Audit
          fea.setFields(wvBoxCU.is.open_!, "FE", fe.id, "upd", fe.getAuditRec(feClone))
          fea = Model.merge(fea)

          ed.familyevent = fe
          ed.setSubmitter(wvBoxCU.is.open_!)
          ed = Model.merge(ed)

          var eda = new Audit
          eda.setFields(wvBoxCU.is.open_!, "ED", ed.id, "upd", ed.getAuditRec(edClone))
          eda = Model.merge(eda)
          Model.flush()
        case _ =>
      }

    } else {
      val place = "FaWizard finish event"
      val msg = ("Validation is unsuccessful")
      log.error(place + "; " + msg)
      S.redirectTo("/errorPage", () => {
        ErrorXmlMsg.set(Some(Map(
          "location" -> <p>
            {place}
          </p>,
          "message" -> <p>
            {msg}
          </p>)))
      })
    }
    wvED.remove()
    wvBoxFamily.remove()
    wvBoxFamilyEvent.remove()
    wvBoxEventDetail.remove()
    wvEvenDat4Fa.remove()
    wvDPAS.remove()
    wvEDMLT.remove()
    wvEVEN.remove()
    wvFE.remove()
    log.debug("/rest/personView/" + wvRootPersonId.get/*personVar.get.get.id.toString*/ /*wvBoxPerson.get.open_!.id*/)
    S.redirectTo("/rest/personView/" + wvRootPersonId.get/*personVar.get.get.id.toString*/ /*wvBoxPerson.get.open_!.id*/) //})
  }

  def getFamilyData() {
    log.debug("FaWizard getFamilyData")
    S.getSessionAttribute("familyEventId") match {
      case Full(familyEventId) =>
        val familyEvent: Option[FamilyEvent] = Model.find(classOf[FamilyEvent], familyEventId.toLong)
        familyEvent match {
          case Some(x) =>
            val fe = x
            wvBoxFamilyEvent.set(Full(fe))
            fe.getEventDetail(Model.getUnderlying)
            wvBoxFamily.set(Full(fe.familyevent))
            wvFE.set((fe.id, fe.tag))
              //-- extends WizardVar[(/*id*/Long, /*tag*/String)] (0L, "")
            log.debug("FaWizard getFamilyData wvFE.get "+wvFE.get.toString())
            fe.familydetails.size match {
              case 1 =>
                actionCUD = "U" //-- C create, U update, D delete
                val fed: EventDetail = fe.familydetails.iterator.next
                wvBoxEventDetail.set(Full(fed))
                wvED.set((fed.id, fed.descriptor, GedcomUtil.i18nizeGedcomDate(fed.dateValue), fed.place, fed.ageAtEvent, fed.cause, fed.source, fed.note))
                wvEvenDat4Fa.set((FeTags.getMsg(fe.tag), GedcomDateOptions.defineDateShape(fed.dateValue)/*"gdt_exact"*/))
                wvDPAS.set((GedcomUtil.i18nizeGedcomDate(fed.dateValue), fed.place, fed.ageAtEvent, fed.source, fed.note))
                wvEDMLT.set ((new MultiLangText("descriptor", fed.descriptor),
                  new MultiLangText("dateValue", GedcomUtil.i18nizeGedcomDate(fed.dateValue)),
                  new MultiLangText("place", fed.place),
                  new MultiLangText("cause", fed.cause),
                  new MultiLangText("source", fed.source),
                  new MultiLangText("note", fed.note) ))
                wvEVEN.set(fed.descriptor)   // ("")
                log.debug("FaWizard getFamilyData wvED.get "+wvED.get.toString())
              case 0 =>
                val place = "FaWizard getFamilyData"
                val msg = ("FaWizard getFamilyData: There is no EventDetail for FamilyEvent id = " + familyEventId)
                log.error(place+"; "+msg)
                S.redirectTo("/errorPage", () => {
                  ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
                })
              case n =>
                val place = "FaWizard getFamilyData"
                val msg = ("FaWizard getFamilyData: There are more than 1 EventDetail for FamilyEvent id = " + familyEventId)
                log.error(place+"; "+msg)
                S.redirectTo("/errorPage", () => {
                  ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
                })
            }
          case None =>
            val place = "FaWizard getFamilyData"
            val msg = ("FaWizard getFamilyData: There is no FamilyEvent id = " + familyEventId)
            log.error(place+"; "+msg)
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
            })
          case _ =>
            val place = "FaWizard getFamilyData"
            val msg = ("FaWizard getFamilyData: Case _  for FamilyEvent id = " + familyEventId)
            log.error(place+"; "+msg)
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
            })
        }
      case _ =>
        val place = "FaWizard getFamilyData"
        val msg = ("(case _): 'familyEventId' is undefined")
        log.error(place+"; "+msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
        })
    }
    S.unsetSessionAttribute("familyEventId")
  }

}
