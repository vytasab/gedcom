package lt.node.gedcom.snippet

import org.slf4j.{LoggerFactory, Logger}

import _root_.net.liftweb._
import http._
import js.jquery.JqJsCmds._
import util.FieldError
import wizard._
import common._
import _root_.net.liftweb.util.Helpers._
import _root_.lt.node.gedcom.model._
import bootstrap.liftweb.{AccessControl, CurrentUser}

//import _root_.bootstrap.liftweb.CurrentUser
//import _root_.bootstrap.liftweb._

// B302-3/vsh

import _root_.lt.node.gedcom.util._ //{GedcomDateOptions,PeTags,PaTags,ToolTips}

/**
 * Created with IntelliJ IDEA.
 * User: vsh
 * Date: 12/3/12
 * Time: 9:37 PM
 * To change this template use File | Settings | File Templates.
 */

class/*object*/ MultiMediaWizardRunner {
  def render = "* [onclick]" #> SHtml.ajaxInvoke(() =>
    ModalDialog(<div><lift:MultiMediaWizard ajax="true"/></div>))
}


// https://groups.google.com/forum/?fromgroups=#!search/file$20upload$20wizard$20lift/liftweb/gPwd0noM2Lg/_g2Df0d9Qn4J
// http://grokbase.com/t/gg/liftweb/129jpker9z/lift-upload-field-on-a-lift-screen
// http://stackoverflow.com/questions/9019250/liftscreen-validate-custom-fields
// https://www.assembla.com/wiki/show/liftweb/Add_custom_field_types_to_LiftScreen
object  MultiMediaWizard extends Wizard with Loggable {

  val log: Logger = LoggerFactory.getLogger("MultiMediaWizard")
  require(AccessControl.isAuthenticated_?())
  object wvInt extends WizardVar[(Box[FileParamHolder], String, MultiLangText)] (Empty, "", new MultiLangText("title", ""))
  object wvDb extends WizardVar[(Box[FileParamHolder], String, String)] (Empty, "", "")

  private object /*wvEA*/editCase extends WizardVar[String]("title")
  val editCases: List[(String, String)] =
    List(("mm", "wizmm.mm"), ("title", "wizmm.title"), ("mm_title", "wizmm.mmTitle")).map((kv)=>(kv._1, S ? kv._2))
  // log.debug("MultiMediaWizard editCases = " + editCases.toString())
  //  val editCases: List[(String, String)] =
//    List(("event", "wiz.event"), ("attrib", "wiz.attribute")).map((kv)=>(kv._1, S ? kv._2))

  //  getEventAttribData()
//  log.debug("PeWizard wvBoxPerson after getEventAttribData |" + wvBoxPerson.get./*get.*/toString + "|")
//  log.debug("PeWizard actionCUD updatePePa |" + actionCUD + "|" + updatePePa + "|")
//  override protected def calcAjaxOnDone = Unblock
//  override def calcFirstScreen = { //  : Box[Screen]
//    log.debug("PeWizard calcFirstScreen  []...")
//    actionCUD match  {
//      case "C" =>
//        log.debug("PeWizard calcFirstScreen C")
//        Full(addEventOrAttib)
//      case "U" => updatePePa match {
//        case "PE" =>
//          log.debug("PeWizard calcFirstScreen Pe")
//          Full(selPeTag)
//        case "PA" =>
//          log.debug("PeWizard calcFirstScreen Pa")
//          Full(selPaTag)
//        case _ =>
//          log.debug("PeWizard calcFirstScreen _")
//          Empty
//      }
//      case _ =>
//        log.debug("PeWizard calcFirstScreen  ...[]")
//        Empty
//    }
//  }

  log.debug("MultiMediaWizard mmActionCUD = " + S.getSessionAttribute("mmActionCUD").getOrElse("?") )
  var actionCUD = S.getSessionAttribute("mmActionCUD").getOrElse("?") //--    C add, U update, D delete
  S.unsetSessionAttribute("mmActionCUD")

  override def calcFirstScreen = { //  : Box[Screen]
    log.debug("MultiMediaWizard calcFirstScreen  []...")
    actionCUD match  {
      case "C" =>
        log.debug("MultiMediaWizard calcFirstScreen C")
        Full(uploadScreen)
      case "U" =>
        log.debug("MultiMediaWizard calcFirstScreen U")
        Full(updateOptionScreen)
      /*case "U" => updatePePa match {
        case "PE" =>
          log.debug("MultiMediaWizard calcFirstScreen Pe")
          Full(selPeTag)
        case "PA" =>
          log.debug("MultiMediaWizard calcFirstScreen Pa")
          Full(selPaTag)
        case _ =>
          log.debug("MultiMediaWizard calcFirstScreen _")
          Empty
      }*/
      case _ =>
        log.debug("MultiMediaWizard calcFirstScreen  ...[]")
        Empty
    }
  }


  val updateOptionScreen = new Screen {
    val editCaseInit = editCase.get
    val editCaseNew = radio(S ? "wizmm.choose",
      editCases.filter((kv) => kv._1==editCaseInit).head._2, editCases.map( _._2)/*, valMinLen(1, S ? "wiz.click.radio")*/)

    override def nextScreen = {
      //wvEA.set(eaCases.find(_._2 == eoaNew.get).get._1)
      editCase.set(editCases.find(_._2 == editCaseNew.get).get._1)
      editCaseNew.get match {
        //case xxx if xxx == "" => editTitle
        case theRest => editCases.find(_._2 == theRest.toString).get._1 match {
          //case "mm" => editMm
          //case "title" => editTitle
          case _ => editMmTitle
        }
      }
    }
  }


  val editMmTitle = new Screen {

    override protected def hasUploadField = true

    val file = makeField[Array[Byte], Nothing](
      /*"File"*/S ? "file'as", new Array[Byte](0),
      field => SHtml.fileUpload( fph => { wvInt.set((Full(fph),fph.mimeType, wvInt.get._3)) } ),
        NothingOtherValueInitializer, isValidMime _ )

    //val titleInit = wvInt._3.getLangMsg()
    val titleNew = field(S ? "pe.note", wvInt._3.getLangMsg(), isValidTitle _)


    override def nextScreen = {
      wvInt.set(wvInt.get._1, wvInt.get._2, wvInt.get._3)
      log.debug("nextScreen  [titleScreen]...")
      log.debug("uploadScreen nextScreen ==>" + wvInt._2 + "<====")
      wvDb.set(wvInt.get._1, wvInt.get._2, wvInt.get._3.addupdLangMsg(titleNew.get))
      Empty //conf //titleScreen
    }

    def isValidMime(s: Array[Byte]): List[FieldError] = {
      // !!! there is some strange validation implementation
      wvInt._1 match {
        case Full(x) if x.mimeType == "image/gif" => Nil
        case Full(x) if x.mimeType == "image/png" => Nil
        case Full(x) if x.mimeType == "image/jpeg" => Nil
        case _ => S.?("mime type is not supported") + ": " + wvInt._1.open_!.mimeType
      }
    }
    def isValidTitle(s: String): List[FieldError] = {
      s match {
        case s if s.length > 0  => Nil
        case _ => S.?("there.is.no.title")
        // TODO  S.?("there.is.no.title")
      }
    }

  }
//  val uploadScreen = new Screen {
//
//    override protected def hasUploadField = true
//
//    val file = makeField[Array[Byte], Nothing](
//      /*"File"*/S ? "file'as", new Array[Byte](0),
//      field => SHtml.fileUpload( fph => { wvInt.set((Full(fph),fph.mimeType, wvInt.get._3)) } ),
//        NothingOtherValueInitializer, isValidMime _ )
//
//    //val titleInit = wvInt._3.getLangMsg()
//    val titleNew = field(S ? "pe.note", wvInt._3.getLangMsg(), isValidTitle _)
//
//
//    override def nextScreen = {
//      wvInt.set(wvInt.get._1, wvInt.get._2, wvInt.get._3)
//      log.debug("nextScreen  [titleScreen]...")
//      log.debug("uploadScreen nextScreen ==>" + wvInt._2 + "<====")
//      wvDb.set(wvInt.get._1, wvInt.get._2, wvInt.get._3.addupdLangMsg(titleNew.get))
//      Empty //conf //titleScreen
//    }
//
//    def isValidMime(s: Array[Byte]): List[FieldError] = {
//      // !!! there is some strange validation implementation
//      wvInt._1 match {
//        case Full(x) if x.mimeType == "image/gif" => Nil
//        case Full(x) if x.mimeType == "image/png" => Nil
//        case Full(x) if x.mimeType == "image/jpeg" => Nil
//        case _ => S.?("mime type is not supported") + ": " + wvInt._1.open_!.mimeType
//      }
//    }
//    def isValidTitle(s: String): List[FieldError] = {
//      s match {
//        case s if s.length > 0  => Nil
//        case _ => S.?("there.is.no.title")
//        // TODO  S.?("there.is.no.title")
//      }
//    }
//
//  }

//  val uploadScreen = new Screen {
//
//    override protected def hasUploadField = true
//
//    val file = makeField[Array[Byte], Nothing](
//      /*"File"*/S ? "file'as", new Array[Byte](0),
//      field => SHtml.fileUpload( fph => { wvInt.set((Full(fph),fph.mimeType, wvInt.get._3)) } ),
//        NothingOtherValueInitializer, isValidMime _ )
//
//    //val titleInit = wvInt._3.getLangMsg()
//    val titleNew = field(S ? "pe.note", wvInt._3.getLangMsg(), isValidTitle _)
//
//
//    override def nextScreen = {
//      wvInt.set(wvInt.get._1, wvInt.get._2, wvInt.get._3)
//      log.debug("nextScreen  [titleScreen]...")
//      log.debug("uploadScreen nextScreen ==>" + wvInt._2 + "<====")
//      wvDb.set(wvInt.get._1, wvInt.get._2, wvInt.get._3.addupdLangMsg(titleNew.get))
//      Empty //conf //titleScreen
//    }
//
//    def isValidMime(s: Array[Byte]): List[FieldError] = {
//      // !!! there is some strange validation implementation
//      wvInt._1 match {
//        case Full(x) if x.mimeType == "image/gif" => Nil
//        case Full(x) if x.mimeType == "image/png" => Nil
//        case Full(x) if x.mimeType == "image/jpeg" => Nil
//        case _ => S.?("mime type is not supported") + ": " + wvInt._1.open_!.mimeType
//      }
//    }
//    def isValidTitle(s: String): List[FieldError] = {
//      s match {
//        case s if s.length > 0  => Nil
//        case _ => S.?("there.is.no.title")
//        // TODO  S.?("there.is.no.title")
//      }
//    }
//
//  }



  val uploadScreen = new Screen {

    override protected def hasUploadField = true

    val file = makeField[Array[Byte], Nothing](
      /*"File"*/S ? "file'as", new Array[Byte](0),
      field => SHtml.fileUpload( fph => { wvInt.set((Full(fph),fph.mimeType, wvInt.get._3)) } ),
        NothingOtherValueInitializer, isValidMime _ )

    //val titleInit = wvInt._3.getLangMsg()
    val titleNew = field(S ? "pe.note", wvInt._3.getLangMsg(), isValidTitle _)


    override def nextScreen = {
      wvInt.set(wvInt.get._1, wvInt.get._2, wvInt.get._3)
      log.debug("nextScreen  [titleScreen]...")
      log.debug("uploadScreen nextScreen ==>" + wvInt._2 + "<====")
      wvDb.set(wvInt.get._1, wvInt.get._2, wvInt.get._3.addupdLangMsg(titleNew.get))
      Empty //conf //titleScreen
    }

    def isValidMime(s: Array[Byte]): List[FieldError] = {
      // !!! there is some strange validation implementation
      wvInt._1 match {
        case Full(x) if x.mimeType == "image/gif" => Nil
        case Full(x) if x.mimeType == "image/png" => Nil
        case Full(x) if x.mimeType == "image/jpeg" => Nil
        case _ => S.?("mime type is not supported") + ": " + wvInt._1.open_!.mimeType
      }
    }
    def isValidTitle(s: String): List[FieldError] = {
      s match {
        case s if s.length > 0  => Nil
        case _ => S.?("there.is.no.title")
        // TODO  S.?("there.is.no.title")
      }
    }

  }


//  val titleScreen = new Screen {
//    val titleInit = wvInt._3.getLangMsg()
//    val titleNew = field(S ? "pe.note", titleInit, isIncompletedate _)
//    /*wvInt.set((wvInt.get._1, wvInt.get._2,
//      //wvInt.get._3.addupdLangMsg(titleNew.toString)
//      new MultiLangText("title", titleNew.get)
//      )
//    )*/
//
//    /*val uploadedFile = File.createTempFile(wvInt.is.map(v => v.fileName).toString + (new Date()).getTime(), ".tmp")
//    FileUtils.writeByteArrayToFile(uploadedFile, uploadScreen.file.get)
//    val importSummary = // fetch the metadata
//    val name = field("Name", importSummary.name,
//      trim, valMinLen(1,"Name too short"),
//      valMaxLen(1000,"That's a long name"))
//    val files = field("Import %s files?".format(importSummary.files.size), true)
//    val children = field("Import %s children?".format(importSummary.children.size), true)*/
//
//    override def nextScreen = {
//      log.debug("nextScreen [conf]...|" + titleNew.get + "|")
//      /*wvInt.set(wvInt.get._1, wvInt.get._2, wvInt.get._3
//        //wvInt.get._3.addupdLangMsg(titleNew.get)
//        //new MultiLangText("title", titleNew.get)
//        //new MultiLangText("title", wvInt.get._3.)
//        )*/
//
//      ///wvDb.set((wvInt.get._1.open_!.file, wvInt.get._2, wvDb.get._3.addupdLangMsg(wvInt.get._3)
//        //wvInt.get._3.addupdLangMsg(titleNew.get)
//        //new MultiLangText("title", titleNew.get)
//        //new MultiLangText("title", wvInt.get._3.)
//
//      //  wvDb.set(wvInt.get._1, wvInt.get._2, wvInt.get._3)  // /*, titleNew.get*/
//        wvDb.set(wvInt.get._1, wvInt.get._2, wvInt.get._3.addupdLangMsg(titleNew.get))
//      conf
//    }
//
//    def isIncompletedate(s: String): List[FieldError] = {
//      s match {
//        case s if s.length > 0  => Nil
//        case _ => S.?("there.is.no.title")
//// TODO  S.?("there.is.no.title")
//      }
//    }
//
//  }


  val conf = new Screen {
    log.debug("[conf]...")
    log.debug("MIME is |" + wvDb._2 + "|")
    log.debug("title is |" + wvDb._3 + "|")
    log.debug("...[conf]")
    override def confirmScreen_? = false //true
    override def nextScreen = Empty
  }


  def finish() {
    log.debug("[finish]...")
    /*S.notice("Thanks for uploading a file")
    log.debug("Thanks for uploading a file")
    //S.notice("Thanks for uploading a file of " + wvInt._1.open_!.length + " bytes")
    S.notice("MIME is |" + wvDb._2 + "|")
    log.debug("MIME is |" + wvDb._2 + "|")
    S.notice("title is |" + wvDb._3/*.getLangMsg()*/ + "|")
    log.debug("title is |" + wvDb._3/*.getLangMsg()*/ + "|")*/
    val msg = <_>Uploading a file</_>.text + " | " +
      <_>MIME: ({wvDb._2})</_>.text + " | " +
      "title: ("+{wvDb._3}+")"
    log.debug(msg)
    S.notice(msg)
    var mm = new MultiMedia
    log.debug("MultiMedia.id |" + mm.id.toString + "|")
    mm.idRoot = 0L
    mm.mimeType = wvDb._2
    mm.title = wvDb._3
    mm.blobas = wvDb._1.get.file
    S.getSessionAttribute("role").open_! match {
      case "Pe" =>
        var person: Person = Model.find(classOf[Person], S.getSessionAttribute("personId").get.toLong).get
        mm.personmultimedia = person
        S.unsetSessionAttribute("personId")
      case "PE" =>
        var pe: PersonEvent = Model.find(classOf[PersonEvent], S.getSessionAttribute("idParentED").get.toLong).get
        val ed: EventDetail = pe.eventdetails.iterator.next()
        mm.eventdetailmultimedia = ed
        S.unsetSessionAttribute("idParentED")
      case "PA" =>
        var pa: PersonAttrib = Model.find(classOf[PersonAttrib], S.getSessionAttribute("idParentED").get.toLong).get
        val ed: EventDetail = pa.attribdetails.iterator.next()
        mm.eventdetailmultimedia = ed
        S.unsetSessionAttribute("idParentED")
      case "FE" =>
        var fe: FamilyEvent = Model.find(classOf[FamilyEvent], S.getSessionAttribute("idParentED").get.toLong).get
        val ed: EventDetail = fe.familydetails.iterator.next()
        mm.eventdetailmultimedia = ed
        S.unsetSessionAttribute("idParentED")
      case "Fa" =>
        var family: Family = Model.find(classOf[Family], S.getSessionAttribute("familyId").get.toLong).get
        mm.familymultimedia = family
        S.unsetSessionAttribute("familyId")
    }
    require(AccessControl.isAuthenticated_?())
    mm.setSubmitter(CurrentUser.open_!)
    val msge = <_>MultiMedia: </_>.text + " | " +
      <_>idRoot: ({mm.idRoot})</_>.text + " | " +
      <_>MIME: ({mm.mimeType})</_>.text + " | " +
      <_>title: ({mm.title})</_>.text + " | " +
      <_>blobas.length: ({mm.blobas.length})</_>.text + " | " +
      <_>person: ( ({if (mm.personmultimedia.eq(null)) "-" else mm.personmultimedia.id})</_>.text + " | " +
      <_>family: ( ({if (mm.familymultimedia.eq(null)) "-" else mm.familymultimedia.id})</_>.text + " | " +
      <_>eventdetail: ( ({if (mm.eventdetailmultimedia.eq(null)) "-" else mm.eventdetailmultimedia.id})</_>.text + " | " +
      <_>submitter: ({mm.submitter})</_>.text + " | " +
      <_>modifier: ({mm.modifier})</_>.text + " | " + ""
    log.debug(msge)

    mm = Model.merge(mm)
    Model.flush()
    S.unsetSessionAttribute("role")
  }

}
