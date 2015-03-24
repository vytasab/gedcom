package lt.node.gedcom.snippet


import _root_.net.liftweb._
import net.liftweb.common.{Full, Loggable, Logger}
import http._
import net.liftweb.util._
import Helpers._
import http.js.JE.JsRaw

import lt.node.gedcom.rest._
import bootstrap.liftweb.AccessControl

class Forest extends Loggable {

  val log = Logger("Forest")

  def render = {
    GedcomRest.emptyPids
    GedcomRest.emptyFids
    val sb: StringBuffer = new StringBuffer("")
    val sbIdGen: StringBuffer = new StringBuffer("")

    //val rootId = S.getSessionAttribute("personId").openOr("1").trim.toLong
    val rootId = S.getSessionAttribute("personId") match {
      case Full(personId) => personId.trim.toLong
      case _ => 0; S.redirectTo("/loginFSB")
    }
    GedcomRest.getPersonJS(
      (S.get("ancestNum").getOrElse("1").toInt,
        S.get("descendNum").getOrElse("1").toInt,
        if (S.get("showSiblings").getOrElse("1") == "1") true else false),
      rootId, 0, sb, sbIdGen)
    //    GedcomRest.getPersonJS((2, true), rootId, 0, sb, sbIdGen)
    //    GedcomRest.getPersonJS((2, false), rootId, 0, sb, sbIdGen) // B130-7 testas - gerai
    log.debug("Forest GedcomRest.getPersonJS rootId = " + rootId + " --------------------");
    //log.debug("Forest GedcomRest.getPersonJS Props.get(\"__app\") = " + Props.get("__app").openOr("/__app/") + "|");
    //log.debug("Forest GedcomRest.getPersonJS Props.get(\"db.driver\") = " + Props.get("db.driver").openOr("test---Driver") + "|");


    val sbf: StringBuffer = new StringBuffer("")
    //GedcomRest.getFamilyJS(S.getSessionAttribute("personId").open_!.toLong, sbf)
    var gedcomJsData = "G={};g={};" +
      (<_>G.app='{Props.get("__app").openOr("/gedcom-web/")}';</_>).text + sb + sbf
    //println(sbIdGen.toString + "|" + arrIdGen.length + "|" + arrIdGen(0) + "|" + arrIdGen(1) + "|")
    //ScriptRenderer.ajaxScript.r(JsRaw(gedcomJsData))
    // google-lift: [url encoded javascrip]
    // does not compile:  Script(JsRaw(gedcomJsData))

    def countGenerationSize(text: String): String = {
      val counts = scala.collection.mutable.Map.empty[Int, Int] //"personId,generation [...]"
      log.debug("Forest.countGenerationSize() = |" + text + "|")
      var gMin = 999;
      var gMax = -999;
      for (xIdGen <- text.split("\\s+")) {
        val anIdGen = xIdGen.split(",+")
        log.debug("xIdGen = " + xIdGen)
        log.debug(" anIdGen = " + xIdGen.split(",+")(0).toString)
        val oldCount =
          if (counts.contains(anIdGen(1).toInt)) counts(anIdGen(1).toInt)
          else 0
        counts += (anIdGen(1).toInt -> (oldCount + 1))
        gMin = /*Math*/math.min(gMin, anIdGen(1).toInt)
        gMax = /*Math*/math.max(gMax, anIdGen(1).toInt)
      }
      val sb: StringBuffer = new StringBuffer("\n" + (<_>G.gSize={counts.size};G.gMin={gMin};G.gMax={gMax};</_>.text))
      for (x <- counts.keys) {
        val xx = if (x == 0) '0' else x.toString
        val sbIds: StringBuffer = new StringBuffer("G['g" + {
          xx
        } + "']=['")
        var sep = ""
        for (y <- text.split(" +")) {
          val z = y.split(",+");
          if (z(1).toInt == x.toInt) {
            sbIds.append(sep + z(0))
            sep = ","
          }
        }
        sb.append(sbIds.append("'];"))

      }
      sb.toString
    }

    val jsRawStr: String = (gedcomJsData + "\n" + "G.loggedIn=" + AccessControl.isAuthenticated_? + ";" +
      "G.rootId=" + rootId + ";" + GedcomRest.getLocaleStrings() +
      (if (sbIdGen.toString.length > 0) countGenerationSize(sbIdGen.toString) else ""))
    log.debug("JS: ||||| " + JsRaw(jsRawStr).toString + " |||||")
    //
    // B301-2/vsh buvo ok iki 2.3-M1_2.8.1 JsRaw(jsRawStr)
    "#jsraw" #> <script type="text/javascript">{jsRawStr}</script>
    //
  }

  /**
   * Google-group: Lift [script tags, embedding raw javascript]
   */
}
