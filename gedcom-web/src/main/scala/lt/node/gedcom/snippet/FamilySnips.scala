package lt.node.gedcom.snippet


//{Elem, NodeSeq, Group, Text}

import _root_.net.liftweb._

//import http.{RequestVar, S, SHtml}

import http._
import common._
import net.liftweb.util._
import Helpers._


class FamilySnips {
  val log = Logger("FamilySnips");

  def pageTitle = {
    log.debug("S.getSessionAttribute('role').openOr('')=" + S.getSessionAttribute("role").openOr("--negerai--"))
    log.debug("S.locale.toString=|" + S.locale.toString + "| ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
    val richTitle =
      (if (S.locale.toString.length < 3) S.locale.toString else S.locale.toString.substring(0, 2)) match {
        case "lt" /*_LT*/ =>
          val roleInText = Map[String, String]("cB" -> "vaiką - brolį", "cS" -> "vaiką - seserį",
            "pF" -> "tėvą", "pM" -> "motiną",
            "sH" -> "vyrą", "sW" -> "žmoną",
            "fSpouse" -> "sutuoktinį(ę)", "fSon" -> "sūnų", "fDaughter" -> "dukterį",
            "sF" -> "šeimą"
          )
          <_>Pridėti šeimon {roleInText(S.getSessionAttribute("role").openOr("--"))}</_>.text
        case _ =>
          val roleInText = Map[String, String]("cB" -> "child - brother", "cS" -> "child - sister",
            "pH" -> "father", "pW" -> "mother",
            "sH" -> "husband", "sW" -> "wife",
            "fSpouse" -> "spouse", "fSon" -> "son", "fDaughter" -> "daughter",
            "sF" -> "family"
          )
          <_>Add to family as  {roleInText(S.getSessionAttribute("role").openOr("--"))}</_>.text
      }
    "#richTitle" #> richTitle
  }



}
