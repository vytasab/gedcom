package lt.node.gedcom.util

import _root_.java.text.SimpleDateFormat

import _root_.net.liftweb._
import http._
import common._
import util.Helpers._

//import js.JsCmd
//import js.JE.{Str, Call, AnonFunc}

import _root_.scala._
import scala.List
import xml.{NodeSeq, Group}

import _root_.java.text.MessageFormat

//{ModelMovedTospaSpa,Classifiers,ClassifierAttribs}


object Utilits {
  val log: Logger = Logger("Utilits");

  def fadeOutEffect(xhtml: Group): NodeSeq = {
    LiftRules.noticesAutoFadeOut.default.set((notices: NoticeType.Value) => {
      notices match {
        case NoticeType.Notice => Full((5 seconds, 5 seconds))
        case NoticeType.Warning => Full((5 seconds, 4 seconds))
        case NoticeType.Error => Full((5 seconds, 3 seconds))
        case _ => Empty
      }
    }
    )
    bind("z", xhtml,
      "fadeOutScript" -> (<head>
        <!---->
      </head>)
    )
  };

  val noSlashDate = new SimpleDateFormat("yyyyMMdd");

  val slashDate = new SimpleDateFormat("yyyy/MM/dd");

  val isoDate = new SimpleDateFormat("yyyy-MM-dd");

  val isoDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


  //  def splitEvery[A](as : List[A], n : Int) : List[List[A]] =
  //  as.splitAt(n) match {
  //    case (a, Nil) => a :: Nil
  //    case (a, b)   => a :: splitEvery(b, n)
  //  }

  def getIntParam(name: String, default: Int): Int = {
    try
    {
      S.param(name).map(_.toInt) openOr default
    }
    catch {
      case e => default // Should log something in this case
    }
  };

  //  type DateConverter = String => Date
  //  def parseDate(value : String, converter : DateConverter) : Box[Date] =
  //    try {
  //      Full(converter(value))
  //    } catch {
  //      case e => Empty
  //    }
  //
  //  def getDateParam(name : String, converter : DateConverter) : Box[Date] = {
  //    S.param(name).map(parseDate(_, converter)) openOr Empty
  //  }

  def restoreXmlTags(text: String): String = {
    var txt = text
    val oldNewStr: List[(String, String)] = ("&lt;", "<") :: ("&gt;", ">") :: ("&apos;", "'") :: Nil
    oldNewStr foreach (x => {
      txt = txt.replaceAll(x._1, x._2); /*println (x._1 +  x._2);*/
    })
    //log.error(MessageFormat.format("Util.restoreXmlTags |||{0}||| |||{1}|||", text, txt))
    log.debug(MessageFormat.format("Util.restoreXmlTags |||{0}||| |||{1}|||", text, txt))
    //println(txt)
    txt
  };


  def secs2TimeGapDhms(totSecs: Long): Tuple4[String, String, String, String] =
    if (totSecs <= 0)
      ("", "", "", "")
    else {
      var xgap: Long = totSecs / (24L * 3600L);
      val days = if (xgap > 0) "" + xgap else "";
      xgap = (totSecs % (24L * 3600L)) / 3600L;
      val hours = if (xgap > 0) "" + xgap else "";
      xgap = (totSecs % 3600L) / 60L;
      val mins = if (xgap > 0) "" + xgap else "";
      xgap = totSecs % 60L;
      val secs = if (xgap > 0) "" + xgap else "";
      (days, hours, mins, secs)
    };


  /**
   *   http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance
   */

  def levenshtein(str1: String, str2: String): Int = {
    val lenStr1 = str1.length
    val lenStr2 = str2.length
    //val d: Array[Array[Int]] = new Array /*.ofDim*/ (lenStr1 + 1, lenStr2 + 1)
    val d: Array[Array[Int]] = Array.ofDim(lenStr1 + 1, lenStr2 + 1)
    for (i <- 0 to lenStr1) d(i)(0) = i
    for (j <- 0 to lenStr2) d(0)(j) = j
    for (i <- 1 to lenStr1; /*val*/ j <- 1 to lenStr2) {
      val cost = if (str1(i - 1) == str2(j - 1)) 0 else 1
      d(i)(j) = scala.math. /*Numeric.*/ min(
        scala.math. /*Numeric.*/ min(
          d(i - 1)(j) + 1, // deletion
          d(i)(j - 1) + 1), // insertion
        d(i - 1)(j - 1) + cost // substitution
      )
    }
    d(lenStr1)(lenStr2).toInt
  }

  def gedcomHEAD(rootId: Long, ance: Int, desc: Int): String = {
    this.gedcomHEAD(<_>gedcom_p{rootId}_a{ance}_d{desc}.ged</_>.text)
  }

  def gedcomHEAD(fileName: String): String = {
    val lvlNum = 0
    val txt: StringBuffer = new StringBuffer(<_>{lvlNum} HEAD</_>.text+"\n")
    def txtapp(lvl: Int, tag: String, value: String): Unit =
      txt.append(<_>{lvl} {tag} {value}</_>.text+"\n")
    txtapp(lvlNum+1, "SOUR", "gedcom-web")
    txtapp(lvlNum+2, "VERS", "1.0")
    txtapp(lvlNum+2, "NAME", "gedcom-web")
    txtapp(lvlNum+1, "DEST", "ANY")
    txtapp(lvlNum+1, "DATE", getTimeStr("d MMM yyyy"))
    txtapp(lvlNum+2, "TIME", getTimeStr("HH:mm:ss"))
    txt.append("1 SUBM @B1@\n")
    txtapp(lvlNum+1, "FILE", {fileName})
    txt.append("1 GEDC \n2 VERS 5.5.1 \n2 FORM Lineage-Linked \n1 CHAR UTF-8\n")
    txt.append("0 @B1@ SUBM \n1 NAME padargas\n")
    //txt.append(<_>{levelNumber+1} SOUR gedcom-web\n</_>.toString)
    //txt.append(<_>{levelNumber+2} VERS 1.0-SNAPSHOT\n</_>.toString)
    //txt.append(<_>{levelNumber+2} NAME gedcom-web\n</_>.toString)
    //txt.append(<_>{levelNumber+1} DEST ANY\n</_>.toString)
    //txt.append(<_>{levelNumber+1} DATE {getTimeStr("d MMM yyyy")}\n</_>.toString)
    //txt.append(<_>{levelNumber+2} TINE {getTimeStr("HH:mm:ss")}\n</_>.toString)
    txt.toString
  }

  def gedcomTRLR(): String = "0 TRLR\n"

  def getTimeStr(dateFormat: String): String = {
    import java.util.Date
    import java.text.FieldPosition
    val sdf = new java.text.SimpleDateFormat(dateFormat)
    sdf.format(new Date, new StringBuffer(), new FieldPosition(0)).toString
  }

  def getTimeStr: String = {
    this.getTimeStr("yyyy-MM-dd'T'HH:mm:ss.SSS")
  }

};
