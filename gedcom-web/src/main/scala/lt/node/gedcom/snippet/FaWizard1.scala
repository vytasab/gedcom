package lt.node.gedcom.snippet


import _root_.net.liftweb._
import http._
import js.JE.JsObj
import js.jquery.JqJsCmds._
import common._
import _root_.net.liftweb.util.Helpers._

// B302-3/vsh

//import _root_.lt.node.gedcom.model._

/**
 * google-group: Lift [Seven Things that distinguish Lift from other web frameworks]
 */


class/*object*/ AddFaWizardRunner1 {
  def render = "* [onclick]" #> SHtml.ajaxInvoke(() =>
    ModalDialog((<div><lift:FaWizard ajax="true"/><br/></div>),
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
  }


}
