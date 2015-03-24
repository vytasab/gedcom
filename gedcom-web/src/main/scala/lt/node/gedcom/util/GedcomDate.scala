package lt.node.gedcom.util

import _root_.net.liftweb.http.S
import java.text.SimpleDateFormat
import org.slf4j.{LoggerFactory, Logger}
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

  val nnMMM: Map[String,String] = Map( "01"->"JAN", "02"->"FEB", "03"->"MAR",
    "04"->"APR", "05"->"MAY", "06"->"JUN",
    "07"->"JUL", "08"->"AUG", "09"->"SEP",
    "10"->"OCT", "11"->"NOV", "12"->"DEC" )

  lazy val MMMnn: Map[String,String] = nnMMM.map(_.swap)  //.get(mmm).get

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

  /**
   * Transforms GEDCOM format date  to localized format.
   */
  def i18nizeGedcomDate(gedcomDateValue: String): String = {
    log.debug("i18nizeGedcomDate================================================|")

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
          new SimpleDateFormat( """yyyy-MM""").format(new SimpleDateFormat( """MMM yyyy""", Locale.ENGLISH).parse(gedcomDateValue))
          //nnMMM(mm) + " " + yy
        case (dd, null, yy) =>
          log.debug("DatePtrnOpt(dd, null, yy)")
          "Err: |" + gedcomDateValue + "|"
        case (dd, mm, yy) =>
          log.debug("DatePtrnOpt(dd, mm, yy) |" + gedcomDateValue + "|" )
          new SimpleDateFormat( "yyyy-MM-dd").format(new SimpleDateFormat( "d MMM yyyy", Locale.ENGLISH).parse(gedcomDateValue))
          //dd + " " + nnMMM(mm) + " " + yy
        //-- http://stackoverflow.com/questions/6154772/java-unparseable-date?rq=1
      }
    }

    def dateDe(gedcomDateValue: String): String = { " " }
    def datePl(gedcomDateValue: String): String = { " " }
    def dateRu(gedcomDateValue: String): String = { " " }

    lang match {
      case "lt" =>
        gedcomDateValue match {
          //case "" => ""
          case _ =>
            try {
              val DatePtrnOpt(d, m, y) = gedcomDateValue
              log.debug(gedcomDateValue + "==> DatePtrnOpt(d,m,y)" + (if (d != null) d else "null") + (if (m != null) m else "null") + (if (y != null) y else "null"))
              dateLt(gedcomDateValue)
            } catch {
              case ex: Exception =>
                log.debug("DatePtrnOpt(null, null, null): " + ex.toString)
                lazy val DatePeriodPtrnOpt = """^(FROM .+?)?( )?(TO .+?)?$""".r
                try {
                  log.debug("|" + gedcomDateValue + "|==>")
                  val DatePeriodPtrnOpt(from, s, to) = gedcomDateValue
                  log.debug(" DatePeriodPtrnOpt(from,s,to)" +
                    (if (from != null) from else "null") + (if (s != null) s else "null") + (if (to != null) to else "null"))
                  (from, s, to) match {
                    //case (null, null, null) => "Err: /" + gedcomDateValue + "/"
                    case (f, null, null) =>
                      log.debug("NUO |" + f.substring(5) + "|")
                      "NUO " + dateLt(f.substring(5))
                    case (null, null, t) =>
                      log.debug("IKI")
                      "IKI " + dateLt(t.substring(3))
                    case (f, ss, t) =>
                      log.debug("NUO IKI")
                      "NUO " + dateLt(f.substring(5)) + " IKI " + dateLt(t.substring(3))
                    case _ /*(null, null, null)*/ =>
                      throw new Exception("i18nizeGedcomDate DatePeriodPtrnOpt(from,s,to)")
                  }
                } catch {
                  case ex: Exception =>
                    log.debug("DatePeriodPtrnOpt(null, null, null): " + ex.toString)
                    lazy val DateRangeBAPtrn = """^(BET .+?)( AND .+?)?$""".r
                    try {
                      log.debug("|" + gedcomDateValue + "|==>")
                      val DateRangeBAPtrn(bet, and) = gedcomDateValue
                      log.debug(" DateRangeBAPtrn(bet, and)" + (if (bet != null) bet else "null") + (if (and != null) and else "null"))
                      (bet, and) match {
                        //case (null, null) => "Err: -/" + gedcomDateValue + "/-"
                        //case (b, null) => "Err: =/" + gedcomDateValue + "/="
                        //case (null, a) => "Err: +/" + gedcomDateValue + "/+"
                        case (b, a) =>
                          log.debug("BET AND")
                          "TARP " + dateLt(b.substring(4)) + " IR " + dateLt(a.substring(5))
                        case _  =>
                          throw new Exception("i18nizeGedcomDate DateRangeBAPtrn(bet, and)")
                      }
                    } catch {
                      case ex: Exception =>
                        log.debug("DateRangeBAPtrn(null, null): " + ex.toString)
                        lazy val DateOtherPtrn = """^(BEF .+?)?(AFT .+?)?(ABT .+?)?$""".r
                        try {
                          log.debug("|" + gedcomDateValue + "|==>")
                          val DateOtherPtrn(bef, aft, abt) = gedcomDateValue
                          log.debug(" DateOtherPtrn(bef, aft, abt)" + (if (bef != null) bef else "null") + (if (aft != null) aft else "null") + (if (abt != null) abt else "null"))
                          (bef, aft, abt) match {
                            case (ok, null, null) => "PRIEŠ " + dateLt(ok.substring(4))
                            case (null, ok, null) => "PO " + dateLt(ok.substring(4))
                            case (null, null, ok) => "APIE " + dateLt(ok.substring(4))
                            case _  =>
                              throw new Exception("i18nizeGedcomDate DateOtherPtrn(bef, aft, abt)")
                          }
                        } catch {
                          case ex: Exception =>
                            log.warn("i18nizeGedcomDate LT final Exception DateOtherPtrn(null, null, null): |" + gedcomDateValue + "|")
                            gedcomDateValue match {
                              case xxx if xxx.size == 0 => ""
                              case " "  => ""
                              case _ =>
                                lazy val TextDatePtrnOpt = """^\((en|lt|de|pl|ru)\:(.+)\)$""".r
                                //    ^\((en|lt|de|pl|ru)\:(.+)\)$  -->  (lt: testas)
                                //    ^\(\([a-z]{2}\)\:\|(.+)\|\)$  -->  ((lt):|asasasd|)
                                TextDatePtrnOpt findPrefixOf /*findFirstIn*/ gedcomDateValue match {
                                  case Some(ymdtxt) =>
                                    gedcomDateValue
                                  case _ =>
                                    log.warn("DateOtherPtrn(null, null, null): " + ex.toString)
                                    "[lt]: " + gedcomDateValue
                                }
                            }
                            /*lazy val TextDatePtrnOpt = """^\((en|lt|de|pl|ru)\:(.+)\)$""".r
                            //    ^\((en|lt|de|pl|ru)\:(.+)\)$  -->  (lt: testas)
                            //    ^\(\([a-z]{2}\)\:\|(.+)\|\)$  -->  ((lt):|asasasd|)
                            TextDatePtrnOpt findPrefixOf /*findFirstIn*/ gedcomDateValue match {
                              case Some(ymdtxt) =>
                                gedcomDateValue
                              case _ =>
                                log.warn("DateOtherPtrn(null, null, null): " + ex.toString)
                                "[en]: " + gedcomDateValue
                            }*/
                        }
                    }
                }
            }
        }
      //case "de" => gedcomDateValue
      //case "pl" => gedcomDateValue
      //case "ru" => gedcomDateValue
      case "en" =>
        log.debug("en: gedcomDateValue: " + gedcomDateValue)
        val en2gedcom: Map[String, String] = Map("ABOUT" -> "ABT", "AFTER" -> "AFT ", "BEFORE" -> "BEF ", "BETWEEN" -> "BET")
        var dv: String = gedcomDateValue
        en2gedcom.keySet.foreach(k => dv = dv.replaceFirst(en2gedcom(k), k))
        log.debug("en: gedcomDateValue: " + dv)
        dv
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
  def gedcomizeI18nDate(i18nDatValu: String /*, lang: String*/): String = {
    log.debug("gedcomizeI18nDate================================================|")

    val i18nDateValue = i18nDatValu.trim.replaceAll("( )+", " ")

    val lang: String = S.get("locale").getOrElse("en")

    //lazy val DatePtrnOpt = """(\d\d? )?(JAN |FEB |MAR |APR |MAY |JUN |JUL |AUG |SEP |OCT |NOV |DEC )?(\d\d\d\d)""".r
    lazy val DatePtrnOptLt = """^(\d\d\d\d)(-\d\d)?(-\d\d)?$""".r   //"""^(\d\d\d\d)( \d\d?)?( \d\d?)?$""".r

    def dateLt(i18nDateValue: String): String = {
      val DatePtrnOptLt(yy, mm, dd) = i18nDateValue.trim
      (yy, mm, dd) match {
        case (yy, null, null) =>
          log.debug("DatePtrnOptLt(yy, null, null)")
          yy
        case (yy, mm, null) =>
          log.debug("DatePtrnOptLt(yy, mm, null)")
          //new SimpleDateFormat( """MMM yyyy""").format(new SimpleDateFormat( """yyyy-MM""").parse(i18nDateValue)).toUpperCase
          nnMMM(mm.substring(1)) + " " + yy
        case (yy, null, dd) =>
          log.debug("DatePtrnOptLt(yy, null, dd)")
          "Err: " + i18nDateValue
        case (yy, mm, dd) =>
          log.debug("DatePtrnOptLt(yy, mm, dd)")
          //ew SimpleDateFormat( """d MMM yyyy""").format(new SimpleDateFormat( """yyyy-MM-dd""").parse(i18nDateValue)).toUpperCase
          dd.substring(1) + " " + nnMMM(mm.substring(1)) + " " + yy
      }
    }

    lang match {
      case "lt" =>
        try {
          val DatePtrnOptLt(y, m, d) = i18nDateValue
          log.debug(i18nDateValue + "==> DatePtrnOptLt(y,m,d)" + (if (y != null) y else "null") + (if (m != null) m else "null") + (if (d != null) d else "null"))
          dateLt(i18nDateValue)
        } catch {
          case ex: Exception =>
            log.debug("DatePtrnOptLt(null, null, null): " + ex.toString)
            lazy val DatePeriodPtrnOpt = """^(NUO \d{4}.*?)?(\s+?)?(IKI \d{4}.*?)?$""".r  // """^(NUO .+?)?( )?(IKI .+?)?$""".r
            //-- tested by: http://www.regexplanet.com/advanced/java/index.html
            /*  NUO 1949-04-20 IKI 1949-04-20     NUO 1949-04-20 kkk IKI 1949-04-20    NUO 1949-04-20 kkk IKI 1949-04-20
             kkk IKI 1949-04-20    IKI 1949-04-20    1949-04-20    NUO 1949-04-20    NUO
             */
            try {
              log.debug("|" + i18nDateValue + "|==>")
              val DatePeriodPtrnOpt(from, s, to) = i18nDateValue
              log.debug(" DatePeriodPtrnOpt(from,s,to)" +
                (if (from != null) from else "null") + (if (s != null) s else "null") + (if (to != null) to else "null"))
              (from, s, to) match {
                case (f, null, null) =>
                  log.debug("FROM |" + f.substring("NUO ".size) + "|")
                  "FROM " + dateLt(f.substring("NUO ".size))
                case (null, null, t) =>
                  log.debug("TO")
                  "TO " + dateLt(t.substring("IKI ".size))
                case (f, ss, t) =>
                  log.debug("FROM TO")
                  "FROM " + dateLt(f.substring("NUO ".size)) + " TO " + dateLt(t.substring("IKI ".size))
                case _ /*(null, null, null)*/ =>
                  throw new Exception("gedcomizeI18nDate DatePeriodPtrnOpt(from,s,to)")
              }
            } catch {
              case ex: Exception =>
                log.debug("DatePeriodPtrnOpt(null, null, null): " + ex.toString)
                lazy val DateRangeBAPtrn = """^(TARP \d{4}.*?)( IR \d{4}.*?)?$""".r
                try {
                  log.debug("|" + i18nDateValue + "|==>")
                  val DateRangeBAPtrn(bet, and) = i18nDateValue
                  log.debug(" DateRangeBAPtrn(bet, and)" + (if (bet != null) bet else "null") + (if (and != null) and else "null"))
                  (bet, and) match {
                    //case (null, null) => "Err: " + i18nDateValue
                    //case (b, null) => "Err: " + i18nDateValue
                    //case (null, a) => "Err: " + i18nDateValue
                    case (b, a) =>
                      log.debug("BET AND")
                      "BET " + dateLt(b.substring("TARP ".size)) + " AND " + dateLt(a.substring(" IR ".size))
                    case _ =>
                      throw new Exception("gedcomizeI18nDate DateRangeBAPtrn(bet, and)")

                  }
                } catch {
                  case ex: Exception =>
                    log.debug("DateRangeBAPtrn(null, null): " + ex.toString)
                    //lazy val DateOtherPtrn = """(BEF .+?)?(AFT .+?)?(ABT .+?)?""".r
                    lazy val DateOtherPtrn = """^(PRIEŠ \d{4}.*?)?(PO \d{4}.*?)?(APIE \d{4}.*?)?$""".r
                    try {
                      log.debug("|" + i18nDateValue + "|==>")
                      val DateOtherPtrn(bef, aft, abt) = i18nDateValue
                      log.debug(" DateOtherPtrn(bef, aft, abt)" + (if (bef != null) bef else "null") + (if (aft != null) aft else "null") + (if (abt != null) abt else "null"))
                      (bef, aft, abt) match {
                        case (ok, null, null) => "BEF " + dateLt(ok.substring("PRIEŠ ".size+0*6))
                        case (null, ok, null) => "AFT " + dateLt(ok.substring("PO ".size+0*3))
                        case (null, null, ok) => "ABT " + dateLt(ok.substring("APIE ".size+0*5))
                        case _ =>
                          throw new Exception("gedcomizeI18nDate DateOtherPtrn(bef, aft, abt)")
                      }
                    } catch {
                      case ex: Exception =>
                        log.warn("gedcomizeI18nDate LT final Exception DateOtherPtrn(null, null, null): |" + i18nDateValue + "|")
                        i18nDateValue match {
                          case xxx if xxx.size == 0 => ""
                          case " "  => ""
                          case _ =>
                            lazy val TextDatePtrnOpt = """^\((en|lt|de|pl|ru)\:(.+)\)$""".r
                            //    ^\((en|lt|de|pl|ru)\:(.+)\)$  -->  (lt: testas)
                            //    ^\(\([a-z]{2}\)\:\|(.+)\|\)$  -->  ((lt):|asasasd|)
                            TextDatePtrnOpt findPrefixOf /*findFirstIn*/ i18nDateValue match {
                              case Some(ymdtxt) =>
                                i18nDateValue
                              case _ =>
                                log.warn("DateOtherPtrn(null, null, null): " + ex.toString)
                                "[lt]: " + i18nDateValue
                            }
                        }

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


  def iso8601Date(date: String): String = {
    //val dateRawCheck = java.util.regex.Pattern.compile("^\\d{4}\\s+\\d{2}\\s+\\d{2}$")
    //val pYyyyMmDd = java.util.regex.Pattern.compile("^(16|17|18|19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])$")
    //val pYyyyMm = java.util.regex.Pattern.compile("^(16|17|18|19|20)\\d\\d[- /.](0[1-9]|1[012])$")
    //val pnYyyy = java.util.regex.Pattern.compile("^(16|17|18|19|20)\\d\\d$")
    //val pYyyyMmDd_ = java.util.regex.Pattern.compile("^(\\d+)[- /.](\\d+)[- /.](\\d+)$")
    //val pYyyyMm_ = java.util.regex.Pattern.compile("^(\\d+)[- /.](\\d+)$")
    //val pYyyy_ = java.util.regex.Pattern.compile("^(\\d+)$")
    log.debug("iso8601Date date |"+date+"|")

    //val nnMMM: Map[String,String] = Map( "01"->"JAN", "02"->"FEB", "03"->"MAR",
    //  "04"->"APR", "05"->"MAY", "06"->"JUN",
    //  "07"->"JUL", "08"->"AUG", "09"->"SEP",
    //  "10"->"OCT", "11"->"NOV", "12"->"DEC" )
    //lazy val MMMnn: Map[String,String] = nnMMM.map(_.swap)  //.get(mmm).get

    S.get("locale").getOrElse("en") match {
      case "en" =>
        try {
          //lazyvalDatePtrnOpt = """(\d\d? )?(JAN |FEB |MAR |APR |MAY |JUN |JUL |AUG |SEP |OCT |NOV |DEC )?(\d\d\d\d)""".r
          lazy val DatePtrnOpt = """(\d\d )?(JAN |FEB |MAR |APR |MAY |JUN |JUL |AUG |SEP |OCT |NOV |DEC )?(\d\d\d\d)""".r
          val DatePtrnOpt(d, m, y) = date
          (d, m, y) match {
            case (null, null, yy) =>
              log.debug("DatePtrnOpt(null, null, yy)")
              //new SimpleDateFormat( """yyyy-MM-dd""").format(new SimpleDateFormat( """yyyy""").parse(date))
              yy
            case (null, mm, yy) =>
              log.debug("DatePtrnOpt(null, mm, yy)")
              //new SimpleDateFormat( """yyyy-MM-dd""").format(new SimpleDateFormat( """MMM yyyy""").parse(date))
              MMMnn.get(mm.trim) match {
                case Some(mx) => yy + "-" + mx
                case None => "0000-00-00"
              }
            case (dd, null, yy) =>
              log.debug("DatePtrnOpt(dd, null, yy): Error|" + date + "|" )
              "0000-00-00"
            case (dd, mm, yy) =>
              log.debug("DatePtrnOpt(dd, mm, yy)")
              //new SimpleDateFormat( """yyyy-MM-dd""").format(new SimpleDateFormat( """d MMM yyyy""").parse(date))
              MMMnn.get(mm.trim) match {
                case Some(mx) => yy + "-" + mx + "-" + dd.trim
                case None => "0000-00-00"
              }
          }
        } catch {
          case ex: Exception =>
            log.error("iso8601Date [en]: ): " + ex.toString)
            "0000-00-00"
        }
      case "lt" =>
        try {
          lazy val DatePtrnOptLt = """^(\d\d\d\d)(-\d\d)?(-\d\d)?$""".r  // """(\d\d\d\d)(-\d\d?)?(-\d\d?)?""".r
          val DatePtrnOptLt(yy, mm, dd) = date
          (yy, mm, dd) match {
            case (yy, null, null) =>
              log.debug("DatePtrnOptLt(yy, null, null)")
              //new SimpleDateFormat( """yyyy-MM-dd""").format(new SimpleDateFormat( """yyyy""").parse(date))
              date
            case (yy, mm, null) =>
              log.debug("DatePtrnOptLt(yy, mm, null)")
              //new SimpleDateFormat( """yyyy-MM-dd""").format(new SimpleDateFormat( """yyyy-MM""").parse(date))
              date
            case (yy, null, dd) =>
              log.debug("DatePtrnOptLt(yy, null, dd)")
              "0000-00-00" // "Err: " + date
            case (yy, mm, dd) =>
              log.debug("DatePtrnOptLt(yy, mm, dd)")
              //new SimpleDateFormat( """yyyy-MM-dd""").format(new SimpleDateFormat( """yyyy-MM-dd""").parse(date))
              date
          }
        } catch {
          case ex: Exception =>
            log.error("iso8601Date [lt]: ): " + ex.toString)
            "0000-00-00"
        }
      case _ =>
        log.error("iso8601Date S.get(\"locale\") ["+ S.get("locale") + "]")
        "0000-00-00"
    }
  }


  def valiDateLU(lowerDate: String, upperDate: String): Boolean = {
    val isoLowerDate = iso8601Date(lowerDate)
    val isoUpperDate = iso8601Date(upperDate)
    valiDate(lowerDate) && valiDate(upperDate) &&  (isoLowerDate < isoUpperDate)
    }


  def valiDate(date: String): Boolean = {
    iso8601Date(date) match {
      case "0000-00-00" => false
      case _ => true
    }
  }

}
