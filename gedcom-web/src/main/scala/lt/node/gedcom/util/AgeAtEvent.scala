package lt.node.gedcom.util

import _root_.net.liftweb._
import http._
import util.Helpers._

import scala.xml._
import scala.xml.transform._

object AgeAtEvent {


  def validateAgeAtEvent(aaeText: String, lang: String): Boolean = {
    aaeText.size match {
      case 0 =>
        true
      case _ =>
        val pattern = java.util.regex.Pattern.compile(regex(lang))
        pattern.matcher(normalizeAAE(aaeText)).matches
    }
  }


  def validateAgeAtEvent(aaeText: String): Boolean = validateAgeAtEvent(aaeText, S.locale.getLanguage)


/*
  def validateAAE(aae: String): List[FieldError] = { //validateAgeAtEvent aae
    (aae.size==0 || AgeAtEvent.validateAgeAtEvent(aae)) match {
      case true => Nil
      case _ => S.?("aae.is.invalid")
    }
  }
*/


  def doGedcomAgeAtEvent(aaeText: String, lang: String): String = {
    var aaei = normalizeAAE(aaeText)
//    println("doGedcomAgeAtEvent: " + msgsByLocale("en").toString)
    lang match {
      case "en" =>
        aaei
      case _ =>
//        println(<_>doGedcomAgeAtEvent |{aaei}|</_>.text)
//        println(<_>doGedcomAgeAtEvent |{msgsByLocale(lang).toString}|</_>.text)
        for (kv <- msgsByLocale(lang); if (kv._2 != msgs(kv._1)("en"))) {
          aaei = aaei.replaceAll(kv._2, msgs(kv._1)("en"))
//          println(<_>|{kv._2}|  |{msgs(kv._1)("en")}|  |{aaei}|</_>.text)
        }
        aaei
    }
  }
  def doGedcomAgeAtEvent(aaeText: String): String = {
    doGedcomAgeAtEvent(aaeText, S.locale.getLanguage)
  }


  def doLocaleAgeAtEvent(aaeText: String, lang: String): String = {
    var aaei = normalizeAAE(aaeText)
//    println("doLocaleAgeAtEvent: " + msgsByLocale("en").toString)
    lang match {
      case "en" =>
        aaei
      case _ =>
        for (kv <- msgsByLocale("en")/*.map(_.swap)*/; if (kv._2 != msgs(kv._1)(lang))) {
          aaei = aaei.replaceAll(kv._2, msgs(kv._1)(lang))
        }
        aaei
    }
  }
  def doLocaleAgeAtEvent(aaeText: String): String = {
    doLocaleAgeAtEvent(aaeText, S.locale.getLanguage)
  }
  def localeAgeAtEventInXml(node: NodeSeq): NodeSeq = {
    //http://stackoverflow.com/questions/970675/scala-modifying-nested-elements-in-xml
    RTaae(node.asInstanceOf[Node]).asInstanceOf[NodeSeq]
  }
//  def localeAgeAtEventInXml(node: NodeSeq): NodeSeq = {
//    //--http://stackoverflow.com/questions/970675/scala-modifying-nested-elements-in-xml
//    def updateElements(seq: Seq[NodeSeq]): Seq[NodeSeq] =
//      for( subNode <- seq ) yield localeAgeAtEventInXml(subNode)
//    node match {
//      case <person>{ ch @ _* }</person> => <person>{ ch.map(updateElements )}</person>
//      case <event>{ ch @ _* }</event> => <event>{ ch.map(updateElements )}</event>
//      case <pe>{ ch @ _* }</pe> => <pe>{ ch.map(updateElements )}</pe>
//      case <attrib>{ ch @ _* }</attrib> => <attrib>{ ch.map(updateElements )}</attrib>
//      case <pa>{ ch @ _* }</pa> => <pa>{ ch.map(updateElements )}</pa>
//      case <families>{ ch @ _* }</families> => <families>{ ch.map(updateElements )}</families>
//      case <family>{ ch @ _* }</family> => <family>{ ch.map(updateElements )}</family>
//      case <fe>{ ch @ _* }</fe> => <fe>{ ch.map(updateElements )}</fe>
//      case <ed>{ ch @ _* }</ed> => <ed>{ ch.map(updateElements )}</ed>
//      case <ageAtEvent>{aaeText}</ageAtEvent> => <ageAtEvent>{this.doLocaleAgeAtEvent(aaeText.toString)}</ageAtEvent>
//      case other @ _ => other
//    }
//  }
//  def localeAgeAtEventInXml(node: NodeSeq): NodeSeq = node match {
//    //--http://stackoverflow.com/questions/970675/scala-modifying-nested-elements-in-xml
//     case <person>{ ch @ _* }</person> => <person>{ ch.map(localeAgeAtEventInXml )}</person>
//     case <event>{ ch @ _* }</event> => <event>{ ch.map(localeAgeAtEventInXml )}</event>
//     case <pe>{ ch @ _* }</pe> => <pe>{ ch.map(localeAgeAtEventInXml )}</pe>
//     case <attrib>{ ch @ _* }</attrib> => <attrib>{ ch.map(localeAgeAtEventInXml )}</attrib>
//     case <pa>{ ch @ _* }</pa> => <pa>{ ch.map(localeAgeAtEventInXml )}</pa>
//     case <families>{ ch @ _* }</families> => <families>{ ch.map(localeAgeAtEventInXml )}</families>
//     case <family>{ ch @ _* }</family> => <family>{ ch.map(localeAgeAtEventInXml )}</family>
//     case <fe>{ ch @ _* }</fe> => <fe>{ ch.map(localeAgeAtEventInXml )}</fe>
//     case <ed>{ ch @ _* }</ed> => <ed>{ ch.map(localeAgeAtEventInXml )}</ed>
//     case <ageAtEvent>{aaeText}</ageAtEvent> => <ageAtEvent>{this.doLocaleAgeAtEvent(aaeText.toString)}</ageAtEvent>
//     case other @ _ => other
//  }




  def msgsByLocale(lang: String): List[(String, String)] = msgs.map {
    kv => (kv._1, kv._2(lang))
  }.toList


  def normalizeAAE(aaeText: String): String = {
    aaeText.trim.replaceAll("  ", " ")
  }


  def getMsg(msgKey: String): String = this.msgs(msgKey)(S.locale.getLanguage)


  // http://download.oracle.com/javase/tutorial/essential/regex/quant.html
  val regex: Map[String, String] = Map(
    "en" -> """^[<|>]?(\d{1,2}y\s+\d{1,2}m\s+\d{1,3}d|\d{1,2}y|\d{1,2}m|\d{1,3}d|\d{1,2}y\s+\d{1,2}m|\d{1,2}y\s+\d{1,3}d|\d{1,2}m\s+\d{1,3}d|CHILD|INFANT|STILLBORN)$""",
    "lt" -> """^[<|>]?(\d{1,2}M\s+\d{1,2}m\s+\d{1,3}d|\d{1,2}M|\d{1,2}m|\d{1,3}d|\d{1,2}M\s+\d{1,2}m|\d{1,2}M\s+\d{1,3}d|\d{1,2}m\s+\d{1,3}d|VAIKAS|KŪDIKIS|NAUJAGIMIS)$"""
  )


  val msgs: Map[String, Map[String, String]] = Map(
    //"regex" -> regex,
    "year" -> Map(
      "en" -> "y",
      "lt" -> "M"
    ),
    "month" -> Map(
      "en" -> "m",
      "lt" -> "m"
    ),
    "day" -> Map(
      "en" -> "d",
      "lt" -> "d"
    ),
    "child" -> Map(
      "en" -> "CHILD",
      "lt" -> "VAIKAS"
    ),
    "infant" -> Map(
      "en" -> "INFANT",
      "lt" -> "KŪDIKIS"
    ),
    "stillborn" -> Map(
      "en" -> "STILLBORN",
      "lt" -> "NAUJAGIMIS"
    )
  )
}


//-- http://stackoverflow.com/questions/970675/scala-modifying-nested-elements-in-xml

object RRaaei extends RewriteRule {
  override def transform(n: Node): Seq[Node] = n match {
    case Elem(prefix, "ageAtEvent", attribs, scope, aaeText)  =>
      Elem(prefix, "ageAtEvent", attribs, scope, new Text(AgeAtEvent.doLocaleAgeAtEvent(aaeText.toString)))
    case other => other
  }
}

object RTaaei extends RuleTransformer(RRaaei)

object RRaae extends RewriteRule {
  override def transform(n: Node): Seq[Node] = n match {
    case aae @ Elem(_, "ed", _, _, _*) => RTaaei(aae)
    case other => other
  }
}

object RTaae extends RuleTransformer(RRaae)
