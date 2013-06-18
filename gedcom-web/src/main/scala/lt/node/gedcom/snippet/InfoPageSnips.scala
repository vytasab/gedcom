package lt.node.gedcom.snippet

import _root_.scala.xml.NodeSeq

import _root_.net.liftweb._
import common._
import _root_.net.liftweb.util.Helpers._
import bootstrap.liftweb.InfoXmlMsg

class InfoPageSnips {
  val log = Logger("InfoPageSnips");


  def render() = {
      val msg: NodeSeq = InfoXmlMsg.openOr(<p>Strange: no info message</p>)
      InfoXmlMsg.set(Empty)
      //val msg = S.getSessionAttribute("appInfo") openOr <msg>Strange: no info message</msg>
      //S.unsetSessionAttribute("appInfo")
      "#message" #> msg
  }

}