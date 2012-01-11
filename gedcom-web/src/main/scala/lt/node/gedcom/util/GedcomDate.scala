package lt.node.gedcom.util

import _root_.net.liftweb.http.S

abstract class GedcomDate
case class YMD(yyyy: String, MM: String, dd: String) extends GedcomDate
case class Exact(ymd: YMD) extends GedcomDate
case class Between(bet: YMD, and: YMD) extends GedcomDate
case class Bef(ymd: YMD) extends GedcomDate
case class Aft(ymd: YMD) extends GedcomDate
case class Abt(ymd: YMD) extends GedcomDate
case class FromTo(from: YMD, to: YMD) extends GedcomDate
case class From(ymd: YMD) extends GedcomDate
case class To(ymd: YMD) extends GedcomDate
case class Datext(text: String) extends GedcomDate


object GedcomUtil {

  val thisYear = new java.text.SimpleDateFormat("yyyy").format(java.util.Calendar.getInstance.getTime)
  val initYear = 1800
  val yyyys: List[(String, String)] = "yyyy" :: List.range(initYear, (thisYear.toInt+1))map(i => (i.toString,i.toString))

  val MMs: List[(String, String)] = "mm" :: List.range(1, 13)map(i => {
    val t = "0" + i.toString
    val n = t.substring(t.size-2)
    (n,n)
  })

  var dds: List[(String, String)] = "dd" :: List.range(1, 31+1)map(i => {
    val t = "0" + i.toString
    val n = t.substring(t.size-2)
    (n,n)
  })

  lazy val ymdI18n: Map[String, String] = Map(
    "gd_abt" /*"Data apie"*/ -> "gdt_about",
    "gd_aft" /*"Data po"*/ -> "gdt_after",
    "gd_and" /*"ir"*/ -> "gdt_and",
    "gd_bef" /*"Data prieš"*/ -> "gdt_before",
    "gd_bet" /*"Data tarp"*/ -> "gdt_between",
    "gd_from"/*"Data nuo"*/ -> "gdt_from",
    "gd_to"  /*"Data iki"*/ -> "gdt_to",
    "gd_txt" /*"Data tekstu"*/ -> ""
   )


  /**
   * Transforms localized date to GEDCOM format. Date format is yyyy [mm [dd]]
   */
  def doGedcomDate(dateI18nValue: String, dateOptionI18nValue: String): String = {
    //println(<_>dateI18nValue=|{dateI18nValue}| dateOptionI18nValue=|{dateOptionI18nValue}| </_>.text)
    val dateOptionKey = GedcomDateOptions.getKey(dateOptionI18nValue)
    //println(<_>dateOptionKey=|{dateOptionKey}| </_>.text)
    dateOptionKey match {
      case "gdt_no_date" =>
        ""
      case "gdt_exact" =>
        dateI18nValue
      case "gdt_between" =>
        dateI18nValue.replaceFirst(S.?("gd_bet"), GedcomDateOptions.getMsg(ymdI18n("gd_bet"), "xx")).
          replaceFirst(S.?("gd_and"), GedcomDateOptions.getMsg(ymdI18n("gd_and"), "xx"))
//        "xx" -> "BET",
//        "en" -> "gdt_between",
//        "lt" -> "apytikrė: [Tarp ... ir ... ]"),
      case "gdt_before" =>
        dateI18nValue.replaceFirst(S.?("gd_bef"), GedcomDateOptions.getMsg(ymdI18n("gd_bef"), "xx"))
//        "xx" -> "BEFORE",
//        "en" -> "gdt_before",
//        "lt" -> "apytikrė: ... Prieš]"),
      case "gdt_after" =>
        dateI18nValue.replaceFirst(S.?("gd_aft"), GedcomDateOptions.getMsg(ymdI18n("gd_aft"), "xx"))
//        "xx" -> "AFTER",
//        "en" -> "gdt_after",
//        "lt" -> "apytikrė: [Po ..."),
      case "gdt_about" =>
        dateI18nValue.replaceFirst(S.?("gd_abt"), GedcomDateOptions.getMsg(ymdI18n("gd_abt"), "xx"))
//        "xx" -> "ABOUT",
//        "en" -> "gdt_about",
//        "lt" -> "apytikrė"),
      case "gdt_from_to" =>
        dateI18nValue.replaceFirst(S.?("gd_from"), GedcomDateOptions.getMsg(ymdI18n("gd_from"), "xx")).
          replaceFirst(S.?("gd_to"), GedcomDateOptions.getMsg(ymdI18n("gd_to"), "xx"))
//        "xx" -> "FROM_TO",
//        "en" -> "gdt_from_to",
//        "lt" -> "intervalas: [Nuo ... Iki]"),
      case "gdt_from" =>
        dateI18nValue.replaceFirst(S.?("gd_from"), GedcomDateOptions.getMsg(ymdI18n("gd_from"), "xx"))
//        "xx" -> "FROM",
//        "en" -> "gdt_from",
//        "lt" -> "intervalas: [Nuo ..."),
      case "gdt_to" =>
        dateI18nValue.replaceFirst(S.?("gd_to"), GedcomDateOptions.getMsg(ymdI18n("gd_to"), "xx"))
//        "xx" -> "TO",
//        "en" -> "gdt_to",
//        "lt" -> "intervalas: ... Iki]"),
      case "gdt_text" =>
        dateI18nValue
    }
    //log.debug(<_>dds: y={y.toString}; m={m.toString} </_>.text)
  }


  /**
   * Transforms GEDCOM date to localized format. Date format is yyyy [mm [dd]]
   */
/*
  def doLocalizedDate(gedcomDate: String): String = {
    import net.liftweb.http.S
    import java.util.StringTokenizer

    val sb: StringBuilder = new StringBuilder("")
    val st: StringTokenizer = new StringTokenizer(gedcomDate)
    while (st.hasMoreTokens()) {
      st.nextToken() match {
        case "BET" => sb.append(S.?("gd_bet")).append(" ")
        case "BEF" => sb.append(S.?("gd_bef")).append(" ")
        case "AFT" => sb.append(S.?("gd_aft")).append(" ")
        case "ABT" => sb.append(S.?("gd_abt")).append(" ")
        case "FROM" => sb.append(S.?("gd_from")).append(" ")
        case "TO" => sb.append(S.?("gd_to")).append(" ")
        case "AND" => sb.append(S.?("gd_and")).append(" ")
        case string => sb.append(string).append(" ")
      }
    }
    sb.toString.trim
  }
*/


  /**
   * Prepares the days list according yyyy and MM for a form selection element
   */
  def ddsFunc(y: Int, m: Int): List[(String, String)] = {
    //log.debug(<_>dds: y={y.toString}; m={m.toString} </_>.text)
    ("--","--") :: List.range(1, m match {
      case d31: Int if List(1,3,5,7,8,10,12).contains(d31) => 31+1
      case d30: Int if List(4,6,9,11).contains(d30) => 30+1
      case d29: Int if (d29 == 2) && (((y % 4)==0) || ((y % 100)==0)) => 29+1
      case d28: Int if d28 == 2 => 28+1
      case _ => 0
  })map(i => (i.toString,i.toString))
  }

/*
  var yyyy = "";   var yyyyMM = "";  var yyyyMMdd = thisYear.toString
  var MM = "";  var MMdd = "1";  var dd = ""
  var yyyyFrom = "";  var MMFrom = "";  var ddFrom = ""
  var yyyyTo = "";  var MMTo = "";  var ddTo = ""
  var yyyyBef = "";  var MMBef = "";  var ddBef = ""
  var yyyyAft = "";  var MMAft = "";  var ddAft = ""
  var yyyyBet = "";  var MMBet = "";  var ddBet = ""
  var yyyyAnd = "";  var MMAnd = "";  var ddAnd = ""
  var yyyyApx = "";  var MMApx = "";  var ddApx = ""
  var datext = ""
*/
}