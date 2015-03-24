package lt.node.gedcom.snippet

import _root_.net.liftweb._
import http._
import js.JE.JsObj
import js.jquery.JqJsCmds._
import common._
import _root_.net.liftweb.util.Helpers._

// B302-3/vsh
/**
 * google-group: Lift [Seven Things that distinguish Lift from other web frameworks]
 */


class/*object*/ AddFaWizardRunner1 {
  def render = "* [onclick]" #> SHtml.ajaxInvoke(() =>
    ModalDialog((<div><lift:FaWizard1 ajax="true"/><br/></div>),
      JsObj(("top","200px"),("left","300px"),("width","600px"),("height","600px"))))
  /*def render = "* [onclick]" #> SHtml.ajaxInvoke(() =>
    ModalDialog(<div>
      <lift:FaWizard1 ajax="true"/>
    </div>))*/
}


class FaWizard1 extends lt.node.gedcom.snippet.FaWizard /*Wizard with Loggable*/ {

  override def getFamilyData(/*idFamIndex: Int*/): Unit = {
    val idFamIndex: Int = 1
    wvBoxFamily.set(Full(familyReqVar.get.get(idFamIndex).get))
    //S.setSessionAttribute("familyId"/*"familyEventId"*/, familyReqVar.get.get(idFamIndex).get.id.toString)
    // in PersonView.scala:  object familyReqVar extends RequestVar[Map[Int, Family]](Map.empty)
    log.debug("FaWizard1 idFamIndex=%d Family=%s".format(idFamIndex,familyReqVar.get.get(idFamIndex).get.toString() ))
    //super.getFamilyData()
  }
}

class AddFaWizardRunner2 {
  def render = "* [onclick]" #> SHtml.ajaxInvoke(() =>
    ModalDialog((<div><lift:FaWizard2 ajax="true"/><br/></div>),
      JsObj(("top","200px"),("left","300px"),("width","600px"),("height","600px"))))
}


class FaWizard2 extends lt.node.gedcom.snippet.FaWizard {

  override def getFamilyData(): Unit = {
    val idFamIndex: Int = 2
    wvBoxFamily.set(Full(familyReqVar.get.get(idFamIndex).get))
    log.debug("FaWizard2 idFamIndex=%d Family=%s".format(idFamIndex,familyReqVar.get.get(idFamIndex).get.toString() ))
  }
}

class AddFaWizardRunner3 {
  def render = "* [onclick]" #> SHtml.ajaxInvoke(() =>
    ModalDialog((<div><lift:FaWizard3 ajax="true"/><br/></div>),
      JsObj(("top","200px"),("left","300px"),("width","600px"),("height","600px"))))
}


class FaWizard3 extends lt.node.gedcom.snippet.FaWizard {

  override def getFamilyData(): Unit = {
    val idFamIndex: Int = 3
    wvBoxFamily.set(Full(familyReqVar.get.get(idFamIndex).get))
    log.debug("FaWizard3 idFamIndex=%d Family=%s".format(idFamIndex,familyReqVar.get.get(idFamIndex).get.toString() ))
  }
}

class AddFaWizardRunner4 {
  def render = "* [onclick]" #> SHtml.ajaxInvoke(() =>
    ModalDialog((<div><lift:FaWizard4 ajax="true"/><br/></div>),
      JsObj(("top","200px"),("left","300px"),("width","600px"),("height","600px"))))
}


class FaWizard4 extends lt.node.gedcom.snippet.FaWizard {

  override def getFamilyData(): Unit = {
    val idFamIndex: Int = 4
    wvBoxFamily.set(Full(familyReqVar.get.get(idFamIndex).get))
    log.debug("FaWizard4 idFamIndex=%d Family=%s".format(idFamIndex,familyReqVar.get.get(idFamIndex).get.toString() ))
  }
}
