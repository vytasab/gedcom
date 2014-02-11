package lt.node.gedcom.snippet

import org.slf4j.{LoggerFactory, Logger}

import _root_.net.liftweb._
import http._
import js.jquery.JqJsCmds._
import net.liftweb.util.{LiftFlowOfControlException, FieldError}
import wizard._
import common._
import _root_.net.liftweb.util.Helpers._
import _root_.lt.node.gedcom.model._
import bootstrap.liftweb._
import javax.persistence.EntityTransaction
import net.liftweb.common.Full
import scala.Some

//import collection.JavaConversions._
import scala.collection.JavaConverters._

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


// http://stackoverflow.com/questions/9019250/liftscreen-validate-custom-fields
class LoginViaFnSnBd extends Wizard with Loggable {

  val log: Logger = LoggerFactory.getLogger("LoginViaFnSnBd")

  //-- !!! require(AccessControl.isAuthenticated_?())

  object wvFSB extends WizardVar[(String, String, String)] /*("Vytautas","Šabanas","1949-04-20")*/("","","")

  override protected def calcAjaxOnDone = Unblock

  override def calcFirstScreen = { //  : Box[Screen]
    log.debug("LoginViaFnSnBd calcFirstScreen  []...")
    Full(screen4FSB)
  }


  lazy val screen4FSB = new Screen {
    val fName = field(S ? "lfsb.fn", wvFSB._1, "size"->"32", "maxlength"->"48", isValidName _)
    val sName = field(S ? "lfsb.sn", wvFSB._2, "size"->"32", "maxlength"->"48", isValidName _)
    val bDate = field(S ? "lfsb.bd", wvFSB._3, "size"->"11", "maxlength"->"11", isIncompletedate _)

    def isIncompletedate(s: String): List[FieldError] = {
      GedcomUtil.valiDate(s) match {
        case true => Nil
        case _ => S ? "lfsb.bd.err" /*"date.is.invalid"*/
      }
    }

    def isValidName(s: String): List[FieldError] = {
      s match {
        case s if s.length > 1  => Nil
        case _ => S ? "lfsb.fsn.err" /*"wizmm.no.title"*/
      }
    }

    override def nextScreen = {
      wvFSB.set(fName, sName, bDate)
      Empty //--> finish
      //conf  // --> go to confirmation screen
    }

  }


  def finish() {

    def bdIsOk(pe: Person, bDate: String): Boolean  = {
      peEventDate(pe, "BIRT") match {
        case x if x == "" => false
        case y => {
          log.debug("bdIsOk bDate=" + bDate + ";  from db="+ y + ";")
          bDate == y
        }
      }
    }

    def peEventDate(pe: Person, eventName: String): String  = {
      pe.personevents.asScala.filter(x => x.tag == eventName).toList match {
        case Nil => ""
        case list => GedcomUtil.i18nizeGedcomDate(list.head.eventdetails.iterator.next().dateValue)
      }
    }
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

      val personsFnSn = Model.createNamedQuery[Person]("findPersonOrGivnSurn").
        setParams("nameGivn" -> ("%" + wvFSB._1 + "%"),
          "nameSurn" -> ("%" + wvFSB._2 + "%")).getResultList().toList
      val personsFnSnBd = personsFnSn.filter(p => bdIsOk(p, wvFSB._3))
      personsFnSnBd.size match {
        case 0 =>
          val msg = LongMsgs.getMsg("lfsb.res.0") //<p>  |{wvFSB._1}|;  |{wvFSB._2}|;  |{wvFSB._3}|;</p>
          log.warn("infoPage: "+msg.text)
          S.redirectTo("/infoPage", () => InfoXmlMsg.set(Full(<p>{msg}</p>)) )
        case 1 =>
          val msg = S ? "one.person" + "  |" +wvFSB._1 + "|;  |" +wvFSB._2 + "|;  |" +wvFSB._3 + "|;"
          log.debug("infoPage: "+msg)
          log.debug("S.redirectT: |"+(<_>{S.hostAndPath}/rest/person/{personsFnSnBd.head.id.toString}</_>.text)+"|")
          //RequestedURL(Full(<_>/rest/person/{personsFnSnBd.head.id.toString}</_>.text))
          S.redirectTo(<_>/rest/person/{personsFnSnBd.head.id.toString}</_>.text)
        case n =>
          //val msg = S ? "lfsb.res.0" + "  " +wvFSB._1 + "  " +wvFSB._2 + "  " +wvFSB._3
          val msg = LongMsgs.getMsg("lfsb.res.n") //<p>  |{wvFSB._1}|;  |{wvFSB._2}|;  |{wvFSB._3}|;</p>
          log.warn("infoPage: "+msg.text)
          S.redirectTo("/infoPage", () => InfoXmlMsg.set(Full(<p>{msg}</p>)) )
      }
    } catch {
       case rse: LiftFlowOfControlException => throw rse
       case e: Exception => entityTransaction.rollback()
       // TODO D203/vsh išsiaiškinti Transaction veikimą

    }
  }


  //-- development time Screen
  val conf = new Screen {
    log.debug("[conf]...")
    log.debug("fName is |" + wvFSB._1 + "|")
    log.debug("sName is |" + wvFSB._2 + "|")
    log.debug("bDate is |" + wvFSB._3 + "|")
    log.debug("...[conf]")
    override def confirmScreen_? = true
    override def nextScreen = Empty
  }

}

