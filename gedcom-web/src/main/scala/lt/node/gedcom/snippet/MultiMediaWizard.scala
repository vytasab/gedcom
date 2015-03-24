package lt.node.gedcom.snippet

import org.slf4j.{LoggerFactory, Logger}

import _root_.net.liftweb._
import http._
import js.JE.JsObj
import js.jquery.JqJsCmds._
import util.FieldError
import wizard._
import common._
import _root_.net.liftweb.util.Helpers._
import _root_.lt.node.gedcom.model._
import bootstrap.liftweb.{ErrorXmlMsg, AccessControl, CurrentUser}
import javax.persistence.EntityTransaction

//import _root_.bootstrap.liftweb.CurrentUser
//import _root_.bootstrap.liftweb._

// B302-3/vsh

import _root_.lt.node.gedcom.util._ //{GedcomDateOptions,PeTags,PaTags,ToolTips}

///**
// * Created with IntelliJ IDEA.
// * User: vsh
// * Date: 12/3/12
// * Time: 9:37 PM
// * To change this template use File | Settings | File Templates.
// */
//class /*object*/ AddMultiMediaWizardRunner {
//  /*def render = "#aaa" #> SHtml.ajaxInvoke(() =>
//    ModalDialog((<div><lift:MultiMediaWizard ajax="true"/><br/></div>),
//      JsObj(("top","200px"),("left","300px"),("width","600px"),("height","600px"))))*/
//  def render = "* [onclick]" #> SHtml.ajaxInvoke(() =>
//    ModalDialog((<div><lift:MultiMediaWizard ajax="true"/><br/></div>),
//      JsObj(("top","200px"),("left","300px"),("width","600px"),("height","600px"))))
//}
//
//class/*object*/ MultiMediaWizardRunner {
//  def render = "* [onclick]" #> SHtml.ajaxInvoke(() =>
//    ModalDialog(<div><lift:MultiMediaWizard ajax="true"/></div>))
//}


// https://groups.google.com/forum/?fromgroups=#!search/file$20upload$20wizard$20lift/liftweb/gPwd0noM2Lg/_g2Df0d9Qn4J
// http://grokbase.com/t/gg/liftweb/129jpker9z/lift-upload-field-on-a-lift-screen
// http://stackoverflow.com/questions/9019250/liftscreen-validate-custom-fields
// https://www.assembla.com/wiki/show/liftweb/Add_custom_field_types_to_LiftScreen
class MultiMediaWizard extends Wizard with Loggable {

  val log: Logger = LoggerFactory.getLogger("MultiMediaWizard")
// E216-7/vsh  23:49:16.331 [http-nio-8443-exec-7] ...
//  WARN  o.h.util.JDBCExceptionReporter - SQL Error: 0, SQLState: S1000
//  ERROR o.h.util.JDBCExceptionReporter - Packet for query is too large (1790988 > 1048576).
//        You can change this value on the server by setting the max_allowed_packet' variable.
//  ERROR o.h.e.d.AbstractFlushingEventListener - Could not synchronize database state with session
// org.hibernate.exception.GenericJDBCException: Could not execute JDBC batch update
  val maxFileSize = 1048576
  require(AccessControl.isAuthenticated_?())
  object wvInt extends WizardVar[(Box[FileParamHolder], String, MultiLangText)] (Empty, "", new MultiLangText("title", ""))
  object wvDb extends WizardVar[(Box[FileParamHolder], String, String)] (Empty, "", "")
  object wvMmOld extends WizardVar[Option[MultiMedia]] (None)

  private object editCase extends WizardVar[String]("title")
  val mimes: List[String] = List("image/gif", "image/png", "image/jpeg")
  val editCases: List[(String, String)] =
    List(("mm", "wizmm.mm"), ("title", "wizmm.title"), ("mm_title", "wizmm.mmTitle")).map((kv)=>(kv._1, S ? kv._2))

  override protected def calcAjaxOnDone = Unblock

  override def calcFirstScreen = { //  : Box[Screen]
    log.debug("MultiMediaWizard calcFirstScreen  []...")
    var actionCUD = S.getSessionAttribute("mmActionCUD").getOrElse("?") //--    C add, U update, D delete
    log.debug("MultiMediaWizard mmActionCUD = |" + actionCUD + "|")
    actionCUD match  {
      case "C" =>
        log.debug("MultiMediaWizard calcFirstScreen C")
        Full(newMmScreen)
      case "U" =>
        log.debug("MultiMediaWizard calcFirstScreen U")
        Full(updateOptionScreen)
      case _ =>
        log.debug("MultiMediaWizard calcFirstScreen  ...[]")
        Empty
    }
  }


  lazy val newMmScreen = new Screen {

    override protected def hasUploadField = true

    val file = makeField[Array[Byte], Nothing](
      S ? "wizmm.file", new Array[Byte](0),
      field => SHtml.fileUpload( fph => { wvInt.set((Full(fph),fph.mimeType, wvInt.get._3)) } ),
      NothingOtherValueInitializer, isValidSize _, isValidMime _ )

    //val titleNew = textarea/*field*/(S ? "pe.note", wvInt._3.getLangMsg(), 3, 30, isValidTitle _)
    val titleNew = textarea/*field*/(S ? "pe.note", wvInt._3.getLangMsg(),
      "class"->"textarea-small", isValidTitle _)


    override def nextScreen = {
      wvDb.set(wvInt.get._1, wvInt.get._2, wvInt.get._3.addupdLangMsg(titleNew.get))
      //Empty //--> finish
      conf
    }

    def isValidSize(s: Array[Byte]): List[FieldError] = wvInt._1 match {
      case Full(x) if x.length < maxFileSize => Nil
      case Full(x) => S.?("wizmm.file.toBig") + ": " + x.length
      //case _ => "???"
    }
    def isValidMime(s: Array[Byte]): List[FieldError] = wvInt._1 match {
      case Full(x) if mimes.exists(m => m == x.mimeType) => Nil
      /*case Full(x) if x.mimeType == "image/gif" => Nil
      case Full(x) if x.mimeType == "image/png" => Nil
      case Full(x) if x.mimeType == "image/jpeg" => Nil*/
      case _ => S.?("wizmm.false.mime") + ": " + wvInt._1.toString //.open_!.mimeType
    }
    def isValidTitle(s: String): List[FieldError] = {
      s match {
        case s if s.length > 0  => Nil
        case _ => S.?("wizmm.no.title")
      }
    }

  }



  lazy val updateOptionScreen = new Screen {
    val editCaseInit = editCase.get
    lazy val editCaseNew = radio(S ? "wizmm.choose",
      editCases.filter((kv) => kv._1==editCaseInit).head._2, editCases.map( _._2)/*, valMinLen(1, S ? "wiz.click.radio")*/)
      S.getSessionAttribute("idMm") match {
        case mm if mm.isEmpty => None
        case _ => wvMmOld.set(Model.find(classOf[MultiMedia], S.getSessionAttribute("idMm").get.toLong))
      }
    // --^ person.xsl <xsl:template match="mm" mode="full"> assures mmId refres to active record
    override def nextScreen = {
      editCase.set(editCases.find(_._2 == editCaseNew.get).get._1)
      log.debug("updateOptionScreen editCaseNew.get: " + editCaseNew.get)
      log.debug("updateOptionScreen editCase.get: " + editCase.get)
      editCase.get match {
          case "mm" => editMm
          case "title" => editTitle
          case "mm_title" => editMmTitle
          case _ =>
            val place = "MultiMediaWizard.updateOptionScreen.nextScreen"
            val msg = "An 'editCase.get' is unavailable |"+ editCase.get + "|"
            log.debug(place+": "+msg)
            S.redirectTo("/errorPage", () =>
              ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
            )
      }
    }
  }



  lazy val editMmTitle = new Screen {

    override protected def hasUploadField = true

    log.debug("editMmTitle: MultiMedia for id="+ S.getSessionAttribute("idMm").toString)
    val file = makeField[Array[Byte], Nothing](
      S ? "wizmm.file", new Array[Byte](0),
      field => SHtml.fileUpload( fph => { wvInt.set((Full(fph),fph.mimeType, wvInt.get._3)) } ),
        NothingOtherValueInitializer, isValidSize _, isValidMime _ )
    var mm: MultiMedia = null
    wvMmOld.get match {
      case Some(mmr) =>
        mm = mmr
        log.debug("editMmTitle  Some(mmr) " + new MultiLangText("title", mm.title).getLangMsg() /*mm.toString*/)
      case _ =>
        val place = "MultiMediaWizard.editMmTitle"
        val msg = "No MultiMedia for id=" + S.getSessionAttribute("idMm").toString
        log.debug(place+": "+msg)
        S.redirectTo("/errorPage", () =>
          ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
        )
    }

    val titleNew = textarea(S ? "pe.note", new MultiLangText("title", mm.title).getLangMsg(),
      isValidTitle _, "class"->"textarea-small")

    override def nextScreen = {
      log.debug("editMmTitle screen nextScreen wvInt._2 ==>" + wvInt._2 + "<====")
      wvDb.set(wvInt.get._1, wvInt.get._2, wvInt.get._3.addupdLangMsg(titleNew.get))
      //Empty //--> finish
      conf
    }

    def isValidSize(s: Array[Byte]): List[FieldError] = wvInt._1 match {
      case Full(x) if x.length < maxFileSize => Nil
      case _ => S.?("wizmm.file.toBig") + ": " + wvInt._1.toString //.open_!.mimeType
      //case _ => "???"
    }
    def isValidMime(s: Array[Byte]): List[FieldError] = wvInt._1 match {
      case Full(x) if mimes.exists(/*m => m*/ _ == x.mimeType) => Nil
      case _ => S.?("wizmm.false.mime") + ": " + wvInt._1.toString //.open_!.mimeType
    }
    def isValidTitle(s: String): List[FieldError] = {
      s match {
        case s if s.length > 0  => Nil
        case _ => S ? "wizmm.no.title"
      }
    }

  }



  lazy val editMm = new Screen {

    override protected def hasUploadField = true

    log.debug("editMm: MultiMedia for id="+ S.getSessionAttribute("idMm").toString)
    val file = makeField[Array[Byte], Nothing](
      S ? "wizmm.file", new Array[Byte](0),
      field => SHtml.fileUpload( fph => { wvInt.set((Full(fph),fph.mimeType, wvInt.get._3)) } ),
        NothingOtherValueInitializer, isValidSize _ , isValidMime _ )
    var mm: MultiMedia = null
    wvMmOld.get match {
    case Some(mmr) =>
        mm = mmr
        log.debug("editMm  Some(mmr) " + new MultiLangText("title", mm.title).getLangMsg() /*mm.toString*/)
      case _ =>
        val place = "MultiMediaWizard.editMm"
        val msg = "No MultiMedia for id=" + S.getSessionAttribute("idMm").toString
        log.debug(place+": "+msg)
        S.redirectTo("/errorPage", () =>
          ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
        )
        /*S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map(
            "location" -> <p>{place}</p>,
            "message" -> <p>{msg}</p>)))
        })*/
    }

    def isValidSize(s: Array[Byte]): List[FieldError] = wvInt._1 match {
      case Full(x) if x.length < maxFileSize => Nil
      case Full(x) => S.?("wizmm.file.toBig") + ": " + x.length
      //case _ => "???"
    }
    override def nextScreen = {
      log.debug("editMm screen nextScreen  wvInt._2 ==>" + wvInt._2 + "<====")
      wvDb.set(wvInt.get._1, wvInt.get._2, ""/*wvInt.get._3.*//*.addupdLangMsg(titleNew.get)*/)
      //Empty //--> finish
      conf
    }
    def isValidMime(s: Array[Byte]): List[FieldError] = wvInt._1 match {
      //case Full(x) if (mimes.exists(m => m == x.mimeType)) => Nil
      case Full(x) if (mimes.exists(_ == x.mimeType)) => Nil
      case _ => S.?("wizmm.false.mime") + ": " + wvInt._1.toString //.open_!.mimeType
    }

  }



  lazy val editTitle = new Screen {

    override protected def hasUploadField = true

    log.debug("editTitle: MultiMedia for id="+ S.getSessionAttribute("idMm").toString)
    var mm: MultiMedia = null
    wvMmOld.get match {
    case Some(mmr) =>
        mm = mmr
        log.debug("editTitle  Some(mmr) " + new MultiLangText("title", mm.title).getLangMsg() /*mm.toString*/)
      case _ =>
        val place = "MultiMediaWizard.editTitle"
        val msg = "No MultiMedia for id=" + S.getSessionAttribute("idMm").toString
        log.debug(place+": "+msg)
        S.redirectTo("/errorPage", () =>
          ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
        )
    }

    //val titleNew = textarea/*field*/(S ? "pe.note", new MultiLangText("title", mm.title).getLangMsg(), isValidTitle _)
    val titleNew = textarea(S ? "pe.note", new MultiLangText("title", mm.title).getLangMsg(),
      isValidTitle _, "class"->"textarea-small")

    override def nextScreen = {
      log.debug("editTitle screen nextScreen wvInt._3 ==>" + wvInt._3 + "<====")
      wvDb.set(wvInt.get._1, wvInt.get._2, wvInt.get._3.addupdLangMsg(titleNew.get))
      //Empty //--> finish
      conf
    }

    def isValidTitle(s: String): List[FieldError] = {
      s match {
        case s if s.length > 0 => Nil
        case _ => S ? "wizmm.no.title"
      }
    }

  }


  def finish() {
    log.debug("[finish]...")
    val entityTransaction: EntityTransaction = Model.getTransaction()
    try {
      /*S.notice("Thanks for uploading a file")
      log.debug("Thanks for uploading a file")
      //S.notice("Thanks for uploading a file of " + wvInt._1.open_!.length + " bytes")
      S.notice("MIME is |" + wvDb._2 + "|")
      log.debug("MIME is |" + wvDb._2 + "|")
      S.notice("title is |" + wvDb._3/*.getLangMsg()*/ + "|")
      log.debug("title is |" + wvDb._3/*.getLangMsg()*/ + "|")*/
      val actionCUD = S.getSessionAttribute("mmActionCUD").getOrElse("?") //--    C add, U update, D delete
      actionCUD match  {
        case "C" =>
          require(AccessControl.isAuthenticated_?())
          val msg = "Uploading a file" + " | " +
            <_>MIME: ({wvDb._2})</_>.text + " | " +
            "title: ("+{wvDb._3}+")"
          log.debug(msg)
          S.notice(msg)
          var mm = new MultiMedia
          Model.persist(mm)
          log.debug("MultiMedia.id |" + mm.id.toString + "|")
          mm.idRoot = 0L
          mm.mimeType = wvDb._2
          mm.title = wvDb._3
          mm.blobas = wvDb._1.get.file
          S.getSessionAttribute("role").open_! match {
            case "Pe" =>
              val person: Person = Model.find(classOf[Person], S.getSessionAttribute("personId").get.toLong).get
              mm.personmultimedia = person
              S.unsetSessionAttribute("personId")
            case "PE" =>
              val pe: PersonEvent = Model.find(classOf[PersonEvent], S.getSessionAttribute("idParentED").get.toLong).get
              val ed: EventDetail = pe.eventdetails.iterator.next()
              mm.eventdetailmultimedia = ed
              S.unsetSessionAttribute("idParentED")
            case "PA" =>
              val pa: PersonAttrib = Model.find(classOf[PersonAttrib], S.getSessionAttribute("idParentED").get.toLong).get
              val ed: EventDetail = pa.attribdetails.iterator.next()
              mm.eventdetailmultimedia = ed
              S.unsetSessionAttribute("idParentED")
            /*case "Fa" =>
              val family: Family = Model.find(classOf[Family], S.getSessionAttribute("familyId").get.toLong).get
              mm.familymultimedia = family
              S.unsetSessionAttribute("familyId")*/
            case "FE" =>
              val fe: FamilyEvent = Model.find(classOf[FamilyEvent], S.getSessionAttribute("idParentED").get.toLong).get
              val ed: EventDetail = fe.familydetails.iterator.next()
              mm.eventdetailmultimedia = ed
              S.unsetSessionAttribute("idParentED")
          }
          mm.setSubmitter(CurrentUser.open_!)
          mm = Model.merge(mm)
          logMmRecord(mm)
          Model.flush()
          S.unsetSessionAttribute("role")

        case "U" =>
          require(AccessControl.isAuthenticated_?())
          var mmOld = wvMmOld.get.get
          var mmNew = new MultiMedia
          Model.persist(mmNew)
          log.debug("U MultiMedia.id |" + mmNew.id.toString + "|")
          mmNew.idRoot = 0L
          mmNew.personmultimedia = mmOld.personmultimedia
          mmNew.familymultimedia= mmOld.familymultimedia
          mmNew.eventdetailmultimedia = mmOld.eventdetailmultimedia
          mmNew.setSubmitter(CurrentUser.open_!)
          editCase.get match {
            case "mm" => //edit Mm
              val msg = "U: uploading MM only" + " | " + <_>MIME:({wvDb._2})</_>.text
              log.debug(msg)
              S.notice(msg)
              mmNew.blobas = wvDb._1.get.file
              mmNew.mimeType = wvDb._2
              mmNew.title = mmOld.title
              mmNew = Model.merge(mmNew)
              logMmRecord(mmNew)
              //--v create audit record from former one
              mmOld.idRoot = mmNew.id
              mmOld.setModifier(CurrentUser.open_!)
              mmOld = Model.merge(mmOld)
            case "title" => //edit title
              val msg = "U: editing title only: ("+{wvDb._3}+")"
              log.debug(msg)
              S.notice(msg)
              mmNew.blobas = mmOld.blobas
              mmNew.mimeType = mmOld.mimeType
              mmNew.title = wvDb._3
              mmNew = Model.merge(mmNew)
              logMmRecord(mmNew)
              //--v create audit record from former one
              mmOld.idRoot = mmNew.id
              mmOld.blobas = null
              mmOld.setModifier(CurrentUser.open_!)
              mmOld = Model.merge(mmOld)
            case "mm_title" => //edit MmTitle
              val msg = "U: uploading MM and title" + " | " + <_>MIME:({wvDb._2})</_>.text + " | " + "title:("+{wvDb._3}+")"
              log.debug(msg)
              S.notice(msg)
              mmNew.blobas = wvDb._1.get.file
              mmNew.mimeType = wvDb._2
              mmNew.title = wvDb._3
              mmNew = Model.merge(mmNew)
              logMmRecord(mmNew)
              //--v create audit record from former one
              mmOld.idRoot = mmNew.id
              mmOld.setSubmitter(CurrentUser.open_!)
              mmOld = Model.merge(mmOld)
            case _ =>
              val place = "MultiMediaWizard finish"
              val msg = "An editCase is unexpected |"+ editCase + "|"
              log.error(place+": "+msg)
              S.redirectTo("/errorPage", () =>
                ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
              )
          }
          Model.flush()
          entityTransaction.commit()
        case _ =>
          val place = "MultiMediaWizard finish"
          val msg = "An actionCUD is unexpected |"+ actionCUD + "|"
          log.error(place+": "+msg)
          S.redirectTo("/errorPage", () =>
            ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
          )
      }
      S.unsetSessionAttribute("mmActionCUD")
    } catch {
      case e: Exception => // TODO D203/vsh išsiaiškinti Transaction veikimą
        entityTransaction.rollback()
    }

    def logMmRecord(mmRec: MultiMedia) {
      val msge = "MultiMedia: | " + <_>idRoot: ({mmRec.idRoot})</_>.text + " | " +
        <_>MIME: ({mmRec.mimeType})</_>.text + " | " + <_>title: ({mmRec.title})</_>.text + " | " +
        <_>blobas.length: ({mmRec.blobas.length})</_>.text + " | " +
        // D315-5/vsh cause: org.hibernate.LazyInitializationException: could not initialize proxy - no Session
        //<_>person: ({if (mmRec.personmultimedia.isInstanceOf[Person]) mmRec.personmultimedia.id else "-"})</_>.text + " | " +
        //<_>family: ({if (mmRec.familymultimedia.isInstanceOf[Family]) mmRec.familymultimedia.id else "-"})</_>.text + " | " +
        //<_>eventdetail: ({if (mmRec.eventdetailmultimedia.isInstanceOf[EventDetail]) mmRec.eventdetailmultimedia.id else "-"})</_>.text + " | " +
        <_>submitter: ({if (mmRec.submitter.eq(null)) "-" else mmRec.submitter})</_>.text + " | " +
        <_>modifier: ({if (mmRec.modifier.eq(null)) "-" else mmRec.modifier})</_>.text + " | "
      log.debug(msge)
    }
  }


  //-- development time Screen
  val conf = new Screen {
    log.debug("[conf]...")
    log.debug("MIME is |" + wvDb._2 + "|")
    log.debug("title is |" + wvDb._3 + "|")
    log.debug("...[conf]")
    override def confirmScreen_? = true
    override def nextScreen = Empty
  }

}

