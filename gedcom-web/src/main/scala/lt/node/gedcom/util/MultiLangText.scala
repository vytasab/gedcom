package lt.node.gedcom.util

import _root_.scala.xml._

import _root_.net.liftweb.http.S

class MultiLangText(nameInDb: String, mlText: String) extends MultiLang {
  val name = nameInDb
  var mlt = mlText
  var audit: NodeSeq = <f n={name}></f>
  def getAuditRec: NodeSeq = audit
}


object MultiLangText {
  def wrapText(text: String, lang: String): NodeSeq = {
    lang match {
      case "lt" => <lt>{text}</lt>
      case "en" => <en>{text}</en>
      case "de" => <de>{text}</de>
      case "pl" => <pl>{text}</pl>
      case "ru" => <ru>{text}</ru>
      case _ => <xx>{text}</xx>
    }
  }
  def wrapText(text: String): NodeSeq = {
    import net.liftweb.http.S
    this.wrapText(text, S.locale.getLanguage.toLowerCase)
  }

  // C704-3 - ?!? - dbField is considered as DB field name and as DB field contents
  def txt2xml(dbField: String, lang: String): NodeSeq = {
    val xmlField: NodeSeq = dbField match {
      case txt if txt.length == 0 =>
        println("========MultiLangText1 " + dbField)
        <_ d={lang}></_>
      /*case txt =>
        <_ d={lang}>{wrapText(dbField, lang)}</_>*/
      case txt if !txt.startsWith("<_") =>
        println("========MultiLangText2 " + dbField)
        <_ d={lang}>{wrapText(dbField, lang)}</_>
      case txt if txt.startsWith("<_") =>
        println("========MultiLangText3 " + dbField)
        scala.xml.XML.loadString(dbField)
    }
    println("========MultiLangTextX " + xmlField)
    xmlField
  }

  def txt2xml(dbField: String): NodeSeq = {
    import net.liftweb.http.S
    MultiLangText.txt2xml(dbField, S.locale.getLanguage.toLowerCase)
  }

  def hasLang(dbFieldXml: NodeSeq, lang: String): Boolean =
    dbFieldXml match {
      case x if ((x \\ lang).size == 1) =>
        true
      case x if ((x \\ lang).size == 0) =>
        false
      case _ =>
        true
    }

}

trait MultiLang/*Text*/ {

// http://programming-scala.labs.oreilly.com/ch10.html
// http://grahamhackingscala.blogspot.com/2009/11/xml-generation-with-scala.html
// http://stackoverflow.com/questions/2199040/scala-xml-building-adding-children-to-existing-nodes?tab=active#tab-top
// http://www.codecommit.com/blog/scala/working-with-scalas-xml-support  !!!
// http://daily-scala.blogspot.com/2009/12/xml-transformation-1.html
// http://szeiger.de/blog/2009/12/27/a-zipper-for-scala-xml/
// http://scala-programming-language.1934581.n4.nabble.com/Matching-XML-elements-with-a-specific-value-for-an-attribute-td2001793.html
// https://github.com/lift/framework/blob/irc_issue_872_873/core/util/src/test/scala/net/liftweb/util/TimeHelpersSpec.scala

  val name: String
  var mlt: String
  var audit: NodeSeq


  def getLangMsgXml(dbField: String, lang: String): NodeSeq = {
    val dbFieldXml: NodeSeq = MultiLangText.txt2xml(dbField, lang)
    MultiLangText.hasLang(dbFieldXml, lang) match {
      case true => dbFieldXml \\ lang
      case _ =>
        val dl = (dbFieldXml \ "@d").text
        (dbFieldXml \\ dl).text match {
          case txt if txt.size == 0 => MultiLangText.wrapText("", lang)
          case txt => MultiLangText.wrapText("["+dl+"]: " + txt , dl)
        }
    }
  }


  def getLangMsgXml(lang: String): NodeSeq = {
    getLangMsgXml(mlt, lang)
  }


  def getLangMsg(dbField: String, lang: String): String = {
    getLangMsgXml(dbField, lang).text
  }


  def getLangMsg(lang: String): String = {
    getLangMsg(this.mlt, lang)
  }


  def getLangMsg(): String = {
    getLangMsg(S.locale.getLanguage.toLowerCase)
  }



  /**
   * return: reduced 'dbFieldXml' as NodeSeq
   */
  def delLangMsg(dbFieldXml: Node, lang: String): NodeSeq = {
    dbFieldXml match {
      case  Elem(prefix, label, attribs, scope, child@_*) =>
        Elem(prefix, label, attribs, scope, /*child ++ newLangMsg*/delLangMsgInternal(dbFieldXml, lang):_*)
      case _ => sys.error("Can only del children to elements!")
    }
  }
  def delLangMsgInternal(dbFieldXml: Node, lang: String): NodeSeq = {
    val newDbFieldXm: NodeSeq = (dbFieldXml \ "_").filter(_.label != lang)
    newDbFieldXm
  }

  /**
   * return: reduced 'mlt' as NodeSeq
   */
  def delLangMsg(lang: String): NodeSeq = {
    audit = <f n={name}><del>{getLangMsgXml(lang)}</del></f>
    delLangMsg(MultiLangText.txt2xml(mlt, lang).asInstanceOf[Node], lang)
  }


  def addupdLangMsg(dbField: String, msg: String, lang: String): String = {
    val xmlField: NodeSeq = MultiLangText.txt2xml(dbField, lang)
    val oldLangXml: NodeSeq = getLangMsgXml(lang)
    val newLangXml: NodeSeq = // C704-3  MultiLangText.wrapText(msg, lang)
      msg match {
      case m if m.length > 0 => MultiLangText.wrapText(msg, lang)
      case m => NodeSeq.Empty
    }
    val temp: Node =
      if (MultiLangText.hasLang(xmlField, lang)) {
        audit = <f n={name}><old>{oldLangXml}</old><new>{newLangXml}</new></f>
        delLangMsg(xmlField.asInstanceOf[Node], lang).asInstanceOf[Node]
      } else {
        audit = <f n={name}><new>{newLangXml}</new></f>
        xmlField.asInstanceOf[Node]
      }
    addLangMsg(temp, newLangXml/*wrapText(msg, lang)*/.asInstanceOf[Node]).asInstanceOf[Node].toString()
  }


  /**
   * return: updated 'mlt' as String
   */
  def addupdLangMsg(msg: String, lang: String): String = {
    addupdLangMsg(mlt, msg, lang)
  }


  def addLangMsg(n: Node, newLangMsg: Node): Node/*Elem*/ = newLangMsg match {
    case nlm if nlm.text.length > 0 => n match {
      case Elem(prefix, label, attribs, scope, child@_*) =>
        Elem(prefix, label, attribs, scope, child ++ nlm: _*)
      case _ => sys.error("Can only add children to elements!")
    }
    case _ => n
  }
  /* def addLangMsg(n: Node, newLangMsg: Node): Elem = n match {
    case Elem(prefix, label, attribs, scope, child@_*) =>
      Elem(prefix, label, attribs, scope, child ++ newLangMsg: _*)
    case _ => sys.error("Can only add children to elements!")
  } */

}

/*
scala>   var z = <a><b>bb</b><c>cc</c></a>
z: scala.xml.Elem = <a><b>bb</b><c>cc</c></a>
scala> z \ "a"
res8: scala.xml.NodeSeq = NodeSeq()
scala> z \\ "a"
res10: scala.xml.NodeSeq = NodeSeq(<a><b>bb</b><c>cc</c></a>)
scala> z \ "_"
res13: scala.xml.NodeSeq = NodeSeq(<b>bb</b>, <c>cc</c>)
scala> z \\ "_"
res14: scala.xml.NodeSeq = NodeSeq(<a><b>bb</b><c>cc</c></a>, <b>bb</b>, <c>cc</c>)
scala>
*/
