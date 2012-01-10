package lt.node.gedcom.snippet

import _root_.scala.xml.NodeSeq
import _root_.net.liftweb.widgets.menu._

class CmsMenu {

  def render(xhtml: NodeSeq): NodeSeq = {
    MenuWidget(MenuStyle.HORIZONTAL)
    //  MenuWidget(MenuStyle.option), where option = [HORIZONTAL, VERTICAL, NAVBAR]
  }

}