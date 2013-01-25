package lt.node.gedcom.util

import _root_.net.liftweb.http.S
import java.text.{ParsePosition, SimpleDateFormat}
import org.slf4j.{LoggerFactory, Logger}
import lt.node.gedcom.model.{EventDetail, PersonEvent, Person}
import net.liftweb.util.FieldError
import java.util.Locale

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

  val log: Logger = LoggerFactory.getLogger("GedcomUtil")
  // log.debug(<_>xIsNotYetInJS={xIds.get.toString} xId={xId} result={result}</_>.text)

  val thisYear = new java.text.SimpleDateFormat("yyyy").format(java.util.Calendar.getInstance.getTime)
  val initYear = 1800
  val yyyys: List[(String, String)] = "yyyy" :: List.range(initYear, (thisYear.toInt + 1)) map (i => (i.toString, i.toString))

  val MMs: List[(String, String)] = "mm" :: List.range(1, 13) map (i => {
    val t = "0" + i.toString
    val n = t.substring(t.size - 2)
    (n, n)
  })

  var dds: List[(String, String)] = "dd" :: List.range(1, 31 + 1) map (i => {
    val t = "0" + i.toString
    val n = t.substring(t.size - 2)
    (n, n)
  })

  //  lazy val ymdI18n: Map[String, String] = Map(
  //    "gd_abt" /*"Data apie"*/ -> "gdt_about",
  //    "gd_aft" /*"Data po"*/ -> "gdt_after",
  //    "gd_and" /*"ir"*/ -> "gdt_and",
  //    "gd_bef" /*"Data prieš"*/ -> "gdt_before",
  //    "gd_bet" /*"Data tarp"*/ -> "gdt_between",
  //    "gd_from"/*"Data nuo"*/ -> "gdt_from",
  //    "gd_to"  /*"Data iki"*/ -> "gdt_to",
  //    "gd_txt" /*"Data tekstu"*/ -> ""
  //   )


  //  /**
  //   * Transforms localized date to GEDCOM format. Date format is yyyy [mm [dd]]
  //   */
  //  def doGedcomDate(dateI18nValue: String, dateOptionI18nValue: String): String = {
  //    log.debug(<_>dateI18nValue=|{dateI18nValue}| dateOptionI18nValue=|{dateOptionI18nValue}| </_>.text)
  //    val dateOptionKey = GedcomDateOptions.getKey(dateOptionI18nValue)
  //    //log.debug(<_>dateOptionKey=|{dateOptionKey}| </_>.text)
  //    dateOptionKey match {
  //      case "gdt_no_date" =>
  //        ""
  //      case "gdt_exact" =>
  //        dateI18nValue
  //      case "gdt_between" =>
  //        dateI18nValue.replaceFirst(S.?("gd_bet"), GedcomDateOptions.getMsg(ymdI18n("gd_bet"), "xx")).
  //          replaceFirst(S.?("gd_and"), GedcomDateOptions.getMsg(ymdI18n("gd_and"), "xx"))
  ////        "xx" -> "BET",
  ////        "en" -> "gdt_between",
  ////        "lt" -> "apytikrė: [Tarp ... ir ... ]"),
  //      case "gdt_before" =>
  //        dateI18nValue.replaceFirst(S.?("gd_bef"), GedcomDateOptions.getMsg(ymdI18n("gd_bef"), "xx"))
  ////        "xx" -> "BEFORE",
  ////        "en" -> "gdt_before",
  ////        "lt" -> "apytikrė: ... Prieš]"),
  //      case "gdt_after" =>
  //        dateI18nValue.replaceFirst(S.?("gd_aft"), GedcomDateOptions.getMsg(ymdI18n("gd_aft"), "xx"))
  ////        "xx" -> "AFTER",
  ////        "en" -> "gdt_after",
  ////        "lt" -> "apytikrė: [Po ..."),
  //      case "gdt_about" =>
  //        dateI18nValue.replaceFirst(S.?("gd_abt"), GedcomDateOptions.getMsg(ymdI18n("gd_abt"), "xx"))
  ////        "xx" -> "ABOUT",
  ////        "en" -> "gdt_about",
  ////        "lt" -> "apytikrė"),
  //      case "gdt_from_to" =>
  //        dateI18nValue.replaceFirst(S.?("gd_from"), GedcomDateOptions.getMsg(ymdI18n("gd_from"), "xx")).
  //          replaceFirst(S.?("gd_to"), GedcomDateOptions.getMsg(ymdI18n("gd_to"), "xx"))
  ////        "xx" -> "FROM_TO",
  ////        "en" -> "gdt_from_to",
  ////        "lt" -> "intervalas: [Nuo ... Iki]"),
  //      case "gdt_from" =>
  //        dateI18nValue.replaceFirst(S.?("gd_from"), GedcomDateOptions.getMsg(ymdI18n("gd_from"), "xx"))
  ////        "xx" -> "FROM",
  ////        "en" -> "gdt_from",
  ////        "lt" -> "intervalas: [Nuo ..."),
  //      case "gdt_to" =>
  //        dateI18nValue.replaceFirst(S.?("gd_to"), GedcomDateOptions.getMsg(ymdI18n("gd_to"), "xx"))
  ////        "xx" -> "TO",
  ////        "en" -> "gdt_to",
  ////        "lt" -> "intervalas: ... Iki]"),
  //      case "gdt_text" =>
  //        dateI18nValue
  //    }
  //    //log.debug(<_>dds: y={y.toString}; m={m.toString} </_>.text)
  //  }


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
    ("--", "--") :: List.range(1, m match {
      case d31: Int if List(1, 3, 5, 7, 8, 10, 12).contains(d31) => 31 + 1
      case d30: Int if List(4, 6, 9, 11).contains(d30) => 30 + 1
      case d29: Int if (d29 == 2) && (((y % 4) == 0) || ((y % 100) == 0)) => 29 + 1
      case d28: Int if d28 == 2 => 28 + 1
      case _ => 0
    }) map (i => (i.toString, i.toString))
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

  /**
   * Transforms GEDCOM format date  to localized format.
   */
  def i18nizeGedcomDate(gedcomDateValue: String): String = {

    val lang: String = S.get("locale").getOrElse("en")
    lazy val DatePtrnOpt = """(\d\d? )?(JAN |FEB |MAR |APR |MAY |JUN |JUL |AUG |SEP |OCT |NOV |DEC )?(\d\d\d\d)""".r

    def dateLt(gedcomDateValue: String): String = {
      val DatePtrnOpt(d, m, y) = gedcomDateValue
      (d, m, y) match {
        case (null, null, yy) =>
          log.debug("DatePtrnOpt(null, null, yy)")
          yy
        case (null, mm, yy) =>
          log.debug("DatePtrnOpt(null, mm, yy)")
          new SimpleDateFormat( """yyyy-MM""").format(new SimpleDateFormat( """MMM yyyy""").parse(gedcomDateValue))
        case (dd, null, yy) =>
          log.debug("DatePtrnOpt(dd, null, yy)")
          "Err: |" + gedcomDateValue + "|"
        case (dd, mm, yy) =>
          log.debug("DatePtrnOpt(dd, mm, yy) |" + gedcomDateValue + "|" )
          new SimpleDateFormat( "yyyy-MM-dd").format(new SimpleDateFormat( "d MMM yyyy", Locale.ENGLISH).parse(gedcomDateValue))
         //-- http://stackoverflow.com/questions/6154772/java-unparseable-date?rq=1
      }
    }

    log.debug("i18nizeGedcomDate================================================|")
    lang match {
      case "lt" =>
        gedcomDateValue match {
          case "" => ""
          case _ =>
            try {
              val DatePtrnOpt(d, m, y) = gedcomDateValue
              log.debug(gedcomDateValue + "==> DatePtrnOpt(d,m,y)" + (if (d != null) d else "null") + (if (m != null) m else "null") + (if (y != null) y else "null"))
              dateLt(gedcomDateValue)
            } catch {
              case ex: Exception =>
                log.debug("DatePtrnOpt(null, null, null): " + ex.toString)
                lazy val DatePeriodPtrnOpt = """(FROM .+?)?( )?(TO .+?)?""".r
                try {
                  log.debug("|" + gedcomDateValue + "|==>")
                  val DatePeriodPtrnOpt(from, s, to) = gedcomDateValue
                  log.debug(" DatePeriodPtrnOpt(from,s,to)" +
                    (if (from != null) from else "null") + (if (s != null) s else "null") + (if (to != null) to else "null"))
                  (from, s, to) match {
                    case (null, null, null) => "Err: /" + gedcomDateValue + "/"
                    case (f, null, null) =>
                      log.debug("NUO |" + f.substring(5) + "|")
                      "NUO " + dateLt(f.substring(5))
                    case (null, null, t) =>
                      log.debug("IKI")
                      "IKI " + dateLt(t.substring(3))
                    case (f, ss, t) =>
                      log.debug("NUO IKI")
                      "NUO " + dateLt(f.substring(5)) + " IKI " + dateLt(t.substring(3))
                  }
                } catch {
                  case ex: Exception =>
                    log.debug("DatePeriodPtrnOpt(null, null, null): " + ex.toString)
                    lazy val DateRangeBAPtrn = """(BET .+?)( AND .+?)?""".r
                    try {
                      log.debug("|" + gedcomDateValue + "|==>")
                      val DateRangeBAPtrn(bet, and) = gedcomDateValue
                      log.debug(" DateRangeBAPtrn(bet, and)" + (if (bet != null) bet else "null") + (if (and != null) and else "null"))
                      (bet, and) match {
                        case (null, null) => "Err: -/" + gedcomDateValue + "/-"
                        case (b, null) => "Err: =/" + gedcomDateValue + "/="
                        case (null, a) => "Err: +/" + gedcomDateValue + "/+"
                        case (b, a) =>
                          log.debug("BET AND")
                          "TARP " + dateLt(b.substring(4)) + " IR " + dateLt(a.substring(5))
                      }
                    } catch {
                      case ex: Exception =>
                        log.debug("DateRangeBAPtrn(null, null): " + ex.toString)
                        lazy val DateOtherPtrn = """(BEF .+?)?(AFT .+?)?(ABT .+?)?""".r
                        try {
                          log.debug("|" + gedcomDateValue + "|==>")
                          val DateOtherPtrn(bef, aft, abt) = gedcomDateValue
                          log.debug(" DateOtherPtrn(bef, aft, abt)" + (if (bef != null) bef else "null") + (if (aft != null) aft else "null") + (if (abt != null) abt else "null"))
                          (bef, aft, abt) match {
                            case (ok, null, null) => "PRIEŠ " + dateLt(ok.substring(4))
                            case (null, ok, null) => "PO " + dateLt(ok.substring(4))
                            case (null, null, ok) => "APIE " + dateLt(ok.substring(4))
                          }
                        } catch {
                          case ex: Exception =>
                            log.warn("DateOtherPtrn(null, null, null): " + ex.toString)
                            "[en]: " + gedcomDateValue
                        }
                    }
                }
            }
        }
      case "en" =>
        log.debug("en: gedcomDateValue: " + gedcomDateValue)
        val en2gedcom: Map[String, String] = Map("ABOUT" -> "ABT", "AFTER" -> "AFT ", "BEFORE" -> "BEF ", "BETWEEN" -> "BET")
        var dv: String = gedcomDateValue
        en2gedcom.keySet.foreach(k => dv = dv.replaceFirst(en2gedcom(k), k))
        log.debug("en: gedcomDateValue: " + dv)
        dv
      //case "en" => gedcomDateValue
      //case "de" => gedcomDateValue
      //case "pl" => gedcomDateValue
      //case "ru" => gedcomDateValue
      case _ =>
        log.debug("===|" + "_ or |" + lang + "|")
        gedcomDateValue //  default case "en"
    }
  }

  def i18nizeXmlDateValues(xmlStr: String): String = {
    var xml = xmlStr.replaceAll("\\n", "").replace("<dateValue>", "<_x_>").replace("</dateValue>", "</_x_>")
    //log.debug ("i18nizeXmlDateValues init |" + xml + "|")
    val regexp = """<_x_>.*?</_x_>""".r
    var re: Option[String] = regexp findFirstIn xml
    while (re != None) {
      //log.debug ("i18nizeXmlDateValues re.get |" + re.get + "|")
      xml = regexp replaceFirstIn(xml, "<dateValue>" + i18nizeGedcomDate(re.get.substring(5, re.get.length - 6)) + "</dateValue>")
      //log.debug ("i18nizeXmlDateValues ... |" + xml + "|")
      re = regexp findFirstIn xml
    }
    xml
  }

  /**
   * Transforms localized I18n date to GEDCOM format.
   */
  def gedcomizeI18nDate(i18nDateValue: String /*, lang: String*/): String = {

    val lang: String = S.get("locale").getOrElse("en")

    //lazy val DatePtrnOpt = """(\d\d? )?(JAN |FEB |MAR |APR |MAY |JUN |JUL |AUG |SEP |OCT |NOV |DEC )?(\d\d\d\d)""".r
    lazy val DatePtrnOptLt = """(\d\d\d\d)( \d\d?)?( \d\d?)?""".r

    def dateLt(i18nDateValue: String): String = {
      val DatePtrnOptLt(yy, mm, dd) = i18nDateValue
      (yy, mm, dd) match {
        case (yy, null, null) =>
          log.debug("DatePtrnOptLt(yy, null, null)")
          yy
        case (yy, mm, null) =>
          log.debug("DatePtrnOptLt(yy, mm, null)")
          new SimpleDateFormat( """MMM yyyy""").format(new SimpleDateFormat( """yyyy mm""").parse(i18nDateValue)).toUpperCase
        case (yy, null, dd) =>
          log.debug("DatePtrnOptLt(yy, null, dd)")
          "Err: " + i18nDateValue
        case (yy, mm, dd) =>
          log.debug("DatePtrnOptLt(yy, mm, dd)")
          new SimpleDateFormat( """d MMM yyyy""").format(new SimpleDateFormat( """yyyy MM dd""").parse(i18nDateValue)).toUpperCase
      }
    }

    log.debug("gedcomizeI18nDate================================================|")
    lang match {
      case "lt" =>
        try {
          val DatePtrnOptLt(y, m, d) = i18nDateValue
          log.debug(i18nDateValue + "==> DatePtrnOptLt(y,m,d)" + (if (y != null) y else "null") + (if (m != null) m else "null") + (if (d != null) d else "null"))
          dateLt(i18nDateValue)
        } catch {
          case ex: Exception =>
            log.debug("DatePtrnOptLt(null, null, null): " + ex.toString)
            lazy val DatePeriodPtrnOpt = """(NUO .+?)?( )?(IKI .+?)?""".r
            try {
              log.debug("|" + i18nDateValue + "|==>")
              val DatePeriodPtrnOpt(from, s, to) = i18nDateValue
              log.debug(" DatePeriodPtrnOpt(from,s,to)" +
                (if (from != null) from else "null") + (if (s != null) s else "null") + (if (to != null) to else "null"))
              (from, s, to) match {
                case (null, null, null) => "Err: " + i18nDateValue
                case (f, null, null) =>
                  log.debug("FROM |" + f.substring(5) + "|")
                  "FROM " + dateLt(f.substring(5))
                case (null, null, t) =>
                  log.debug("TO")
                  "TO " + dateLt(t.substring(3))
                case (f, ss, t) =>
                  log.debug("FROM TO")
                  "FROM " + dateLt(f.substring(4)) + " TO " + dateLt(t.substring(4))
              }
            } catch {
              case ex: Exception =>
                log.debug("DatePeriodPtrnOpt(null, null, null): " + ex.toString)
                lazy val DateRangeBAPtrn = """(TARP .+?)( IR .+?)?""".r
                try {
                  log.debug("|" + i18nDateValue + "|==>")
                  val DateRangeBAPtrn(bet, and) = i18nDateValue
                  log.debug(" DateRangeBAPtrn(bet, and)" + (if (bet != null) bet else "null") + (if (and != null) and else "null"))
                  (bet, and) match {
                    case (null, null) => "Err: " + i18nDateValue
                    case (b, null) => "Err: " + i18nDateValue
                    case (null, a) => "Err: " + i18nDateValue
                    case (b, a) =>
                      log.debug("BET AND")
                      "BET " + dateLt(b.substring(3)) + " AND " + dateLt(a.substring(4))
                  }
                } catch {
                  case ex: Exception =>
                    log.debug("DateRangeBAPtrn(null, null): " + ex.toString)
                    //lazy val DateOtherPtrn = """(BEF .+?)?(AFT .+?)?(ABT .+?)?""".r
                    lazy val DateOtherPtrn = """(PRIEŠ .+?)?(PO .+?)?(APIE .+?)?""".r
                    try {
                      log.debug("|" + i18nDateValue + "|==>")
                      val DateOtherPtrn(bef, aft, abt) = i18nDateValue
                      log.debug(" DateOtherPtrn(bef, aft, abt)" + (if (bef != null) bef else "null") + (if (aft != null) aft else "null") + (if (abt != null) abt else "null"))
                      (bef, aft, abt) match {
                        case (ok, null, null) => "BEF " + dateLt(ok.substring(6))
                        case (null, ok, null) => "AFT " + dateLt(ok.substring(3))
                        case (null, null, ok) => "APIE " + dateLt(ok.substring(5))
                      }
                    } catch {
                      case ex: Exception =>
                        log.warn("DateOtherPtrn(null, null, null): " + ex.toString)
                        "[lt]: " + i18nDateValue
                    }
                }
            }

        }
      case "en" =>
        val en2gedcom: Map[String, String] = Map("ABOUT" -> "ABT", "AFTER" -> "AFT ", "BEFORE" -> "BEF ", "BETWEEN" -> "BET")
        var dv: String = i18nDateValue
        en2gedcom.keySet.foreach(k => dv = dv.replaceFirst(k, en2gedcom(k)))
        dv
      //case "de" => gedcomDateValue
      //case "pl" => gedcomDateValue
      //case "ru" => gedcomDateValue
      case _ =>
        log.debug("===|" + "_ or en")
        i18nDateValue //  default case "en"
    }
  }


  def iso8601Date(date: String): String /*List[FieldError]*/ = {
    //val dateRawCheck = java.util.regex.Pattern.compile("^\\d{4}\\s+\\d{2}\\s+\\d{2}$")
    //val pYyyyMmDd = java.util.regex.Pattern.compile("^(16|17|18|19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])$")
    //val pYyyyMm = java.util.regex.Pattern.compile("^(16|17|18|19|20)\\d\\d[- /.](0[1-9]|1[012])$")
    //val pnYyyy = java.util.regex.Pattern.compile("^(16|17|18|19|20)\\d\\d$")
    //val pYyyyMmDd_ = java.util.regex.Pattern.compile("^(\\d+)[- /.](\\d+)[- /.](\\d+)$")
    //val pYyyyMm_ = java.util.regex.Pattern.compile("^(\\d+)[- /.](\\d+)$")
    //val pYyyy_ = java.util.regex.Pattern.compile("^(\\d+)$")

    S.get("locale").getOrElse("en") match {
      case "en" =>
        try {
          lazy val DatePtrnOpt = """(\d\d? )?(JAN |FEB |MAR |APR |MAY |JUN |JUL |AUG |SEP |OCT |NOV |DEC )?(\d\d\d\d)""".r
          val DatePtrnOpt(d, m, y) = date
          (d, m, y) match {
            case (null, null, yy) =>
              log.debug("DatePtrnOpt(null, null, yy)")
              new SimpleDateFormat( """yyyyMMdd""").format(new SimpleDateFormat( """yyyy""").parse(date))
            case (null, mm, yy) =>
              log.debug("DatePtrnOpt(null, mm, yy)")
              new SimpleDateFormat( """yyyyMMdd""").format(new SimpleDateFormat( """MMM yyyy""").parse(date))
            case (dd, null, yy) =>
              log.debug("DatePtrnOpt(dd, null, yy)")
              "00000000" // "Err: |" + date + "|"
            case (dd, mm, yy) =>
              log.debug("DatePtrnOpt(dd, mm, yy)")
              new SimpleDateFormat( """yyyyMMdd""").format(new SimpleDateFormat( """d MMM yyyy""").parse(date))
          }
        } catch {
          case ex: Exception =>
            log.debug("valiDate): " + ex.toString)
            "00000000"
        }
      case "lt" =>
        try {
          lazy val DatePtrnOptLt = """(\d\d\d\d)( \d\d?)?( \d\d?)?""".r
          val DatePtrnOptLt(yy, mm, dd) = date
          (yy, mm, dd) match {
            case (yy, null, null) =>
              log.debug("DatePtrnOptLt(yy, null, null)")
              new SimpleDateFormat( """yyyyMMdd""").format(new SimpleDateFormat( """yyyy""").parse(date))
            case (yy, mm, null) =>
              log.debug("DatePtrnOptLt(yy, mm, null)")
              new SimpleDateFormat( """yyyyMMdd""").format(new SimpleDateFormat( """yyyy mm""").parse(date))
            case (yy, null, dd) =>
              log.debug("DatePtrnOptLt(yy, null, dd)")
              "00000000" // "Err: " + date
            case (yy, mm, dd) =>
              log.debug("DatePtrnOptLt(yy, mm, dd)")
              new SimpleDateFormat( """yyyyMMdd""").format(new SimpleDateFormat( """yyyy MM dd""").parse(date))
          }
        } catch {
          case ex: Exception =>
            log.debug("valiDate): " + ex.toString)
            "00000000"
        }
      case _ => "00000000"
    }
  }


  /*def valiDateLU(lowerDate: String, upperDate: String): Boolean = {
    val isoLowerDate = iso8601Date(lowerDate)
    val isoUpperDate = iso8601Date(upperDate)
    }*/


  def valiDate(date: String): Boolean /*List[FieldError]*/ = {

    iso8601Date(date) match {
      case "00000000" => false //S.?("date.is.invalid")
      case _ => true // Nil
    }

    //    S.get("locale").getOrElse("en") match {
    //      case "en" =>
    //        { try {
    //          lazy val DatePtrnOpt = """(\d\d? )?(JAN |FEB |MAR |APR |MAY |JUN |JUL |AUG |SEP |OCT |NOV |DEC )?(\d\d\d\d)""".r
    //          val DatePtrnOpt(d, m, y) = date
    //          (d, m, y) match {
    //            case (null, null, yy) =>
    //              log.debug ("DatePtrnOpt(null, null, yy)")
    //              new SimpleDateFormat("""yyyyMMdd""").format(new SimpleDateFormat("""yyyy""").parse(date))
    //            case (null, mm, yy) =>
    //              log.debug ("DatePtrnOpt(null, mm, yy)")
    //              new SimpleDateFormat("""yyyyMMdd""").format(new SimpleDateFormat("""MMM yyyy""").parse(date))
    //            case (dd, null, yy) =>
    //              log.debug ("DatePtrnOpt(dd, null, yy)")
    //              "00000000" // "Err: |" + date + "|"
    //            case (dd, mm, yy) =>
    //              log.debug ("DatePtrnOpt(dd, mm, yy)")
    //              new SimpleDateFormat("""yyyyMMdd""").format(new SimpleDateFormat("""d MMM yyyy""").parse(date))
    //          }
    //        } catch {
    //            case ex: Exception =>
    //              log.debug("valiDate): " + ex.toString)
    //              "00000000"
    //        } } match {
    //            //case "00000000" => List(S.?("date.is.invalid"))
    //            case "00000000" => false//S.?("date.is.invalid")
    //            case _ => true  // Nil
    //        }
    //        //}
    //      case "lt" =>
    //        ( try {
    //          lazy val DatePtrnOptLt = """(\d\d\d\d)( \d\d?)?( \d\d?)?""".r
    //          val DatePtrnOptLt(yy, mm, dd) = date
    //          (yy, mm, dd) match {
    //            case (yy, null, null) =>
    //              log.debug ("DatePtrnOptLt(yy, null, null)")
    //              new SimpleDateFormat("""yyyyMMdd""").format(new SimpleDateFormat("""yyyy""").parse(date))
    //            case (yy, mm, null) =>
    //              log.debug ("DatePtrnOptLt(yy, mm, null)")
    //              new SimpleDateFormat("""yyyyMMdd""").format(new SimpleDateFormat("""yyyy mm""").parse(date))
    //            case (yy, null, dd) =>
    //              log.debug ("DatePtrnOptLt(yy, null, dd)")
    //              "00000000" // "Err: " + date
    //            case (yy, mm, dd) =>
    //              log.debug ("DatePtrnOptLt(yy, mm, dd)")
    //              new SimpleDateFormat("""yyyyMMdd""").format(new SimpleDateFormat("""yyyy MM dd""").parse(date))
    //          }
    //        } catch {
    //          case ex: Exception =>
    //            log.debug("valiDate): " + ex.toString)
    //            "00000000"
    //        } ) match {
    //          //case "00000000" => List(S.?("date.is.invalid"))
    //          case "00000000" => false // S.?("date.is.invalid")
    //          case _ => true // Nil
    //        }
    //      case _ => true // Nil
    //    }

  }

}
