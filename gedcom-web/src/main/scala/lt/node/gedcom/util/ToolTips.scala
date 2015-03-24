package lt.node.gedcom.util

import _root_.net.liftweb._
import http._
import util.Helpers._


object ToolTips {

  def getMsg(msgKey: String): String = this.tooltips(msgKey)(S.locale.getLanguage)

  def getMsgText(msgKey: String): String = this.getMsg(msgKey)/*.text*/

  val tooltips: Map[String, Map[String, String]] = Map(
    "age_at_event" -> Map(
      "en" -> """[ < | > | <NULL>]
[ YYy MMm DDDd | YYy | MMm | DDDd | YYy MMm | YYy DDDd | MMm DDDd |
CHILD | INFANT | STILLBORN ] ]""",
      "lt" -> """[ &lt; | &gt;| <tuščia>]
[ ssM ssm sssd | sM | ssm | sssd | ssM ssm | ssM sssd | ssm sssd |
VAIKAS | KŪDIKIS | NAUJAGIMIS ] ], kur ss...  yra skaitmenys"""
    ),
    "xxx" -> Map(
      "en" -> """en""",
      "lt" -> """lt""")
  )

}
