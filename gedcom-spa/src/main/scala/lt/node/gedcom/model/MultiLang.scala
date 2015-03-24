package lt.node.gedcom.model

import _root_.scala.xml._

trait MultiLang {

  def avoidNull(string: String): String = {
    (string != null) match {
      case true if string.startsWith("<_") == true =>
        string
      case true if string.startsWith("<_") == false =>
        <_ d="en"><en>{string}</en></_>.toString()
      case _ =>
        <_ d="en"><en></en></_>.toString()

    }
  }

  def avoidEmpty(string: String): String = {
    (string == null) match {
      case true => """"""
      case _ =>
        (string.length() > 0) match {
          case true if string.startsWith("<_") == true =>
            XML.loadString(string).text.length() match {
              case 0 => """"""
              case _ => string
            }
          case true if string.startsWith("<_") == false =>
            <_ d="en"><en>{string}</en></_>.toString()
          case _ => """"""
        }
    }
  }


  def getLangText(dbField: String, lang: String): String =  {
    val dbFieldXml: NodeSeq = XML.loadString(dbField)
    hasLang(dbFieldXml, lang) match {
      case true => (dbFieldXml \\ lang).text
      case _ => (dbFieldXml \\ (dbFieldXml \ "@d").text).text
    }
  }


  def hasLang(dbFieldXml: NodeSeq, lang: String): Boolean =
    dbFieldXml match {
      case x if ((x \\ lang).size == 1) => true
      case x if ((x \\ lang).size == 0) => false
      case _ => true
    }

}
