package lt.node.gedcom.util

import net.liftweb.http.S
import org.slf4j.{LoggerFactory, Logger}

object GedcomDateOptions extends AnyRef with lt.node.gedcom.util.GedcomMsgsI18n {

  val log: Logger = LoggerFactory.getLogger("GedcomDateOptions")

  // it is used in a Wizard page title (screenTop)
  val msg4Date: Map[String, String] = Map(
    "en" -> """(\d\d )?(JAN |FEB |MAR |APR |MAY |JUN |JUL |AUG |SEP |OCT |NOV |DEC )?(\d\d\d\d)""",
    "lt" -> """(\d\d\d\d)(-\d\d)?(-\d\d)?"""
  )

  val msgs: Map[String, Map[String, String]] = Map(
    /* Naudojamas datos formato pasirinkimui */
    "01gdt_no_date" -> Map(
      "xx" -> "",
      "en" -> "No date",
      "lt" -> "be datos"),
    "03gdt_exact" -> Map(
      "xx" -> "",
      "en" -> "Date",
      "lt" -> "data"),
    "05gdt_between" -> Map(
      "xx" -> "BET",
      "en" -> "Between",
      //"en" -> "gdt_between",
      "lt" -> "apytikrė: [Tarp ... ir ... ]"),
    "10gdt_before" -> Map(
      "xx" -> "BEF",
      "en" -> "Before",
      //"en" -> "gdt_before",
      "lt" -> "apytikrė: ... Prieš]"),
    "15gdt_after" -> Map(
      "xx" -> "AFT",
      "en" -> "After",
      //"en" -> "gdt_after",
      "lt" -> "apytikrė: [Po ..."),
    "20gdt_about" -> Map(
      "xx" -> "ABT",
      "en" -> "About",
      //"en" -> "gdt_about",
      "lt" -> "apytikrė: APIE ..."),
    "25gdt_from_to" -> Map(
      "xx" -> "FROM_TO",
      "en" -> "From To",
      //"en" -> "gdt_from_to",
      "lt" -> "intervalas: [Nuo ... Iki]"),
    "30gdt_from" -> Map(
      "xx" -> "FROM",
      "en" -> "From",
      //"en" -> "gdt_from",
      "lt" -> "intervalas: [Nuo ..."),
    "35gdt_to" -> Map(
      "xx" -> "TO",
      "en" -> "To",
      //"en" -> "gdt_to",
      "lt" -> "intervalas: ... Iki]"),
    "40gdt_text" -> Map(
      "xx" -> "",
      "en" -> "plain text",
      //"en" -> "gdt_text",
      "lt" -> "data žodžiais"),
    "45gdt_and" -> Map(
      "xx" -> "AND",
      "en" -> "gdt_and",
      "lt" -> "gdt_and")
  )
  // GEDCOM: The date range differs from the date period in that the date range
  //    is an estimate that an event happened on a single date somewhere in the date range specified.
  //lazy val DatePtrnValid = """(\d\d? )?(JAN |FEB |MAR |APR |MAY |JUN |JUL |AUG |SEP |OCT |NOV |DEC )?(\d\d\d\d)""".r

  val DatePtrnYearValid = """(20[0-9][0-9])|(19[0-9][0-9])|(18[0-9][0-9])|(17[0-9][0-9])|(16[0-9][0-9])"""
  // D928-6/vsh old -- lazy val DatePtrnExactGedcomValid = """^((31(?! (FEB|APR|JUN|SEP|NOV)))|((30|29)(?! FEB))|(29(?= FEB (((1[6-9]|[2-9]\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00)))))|(0?[1-9])|1\d|2[0-8]) (JAN|FEB|MAR|MAY|APR|JUL|JUN|AUG|OCT|SEP|NOV|DEC) ((1[6-9]|[2-9]\d)\d{2})$""".r
  val DatePtrnExact__YValid = ("""(""" + DatePtrnYearValid + """)""")
  val DatePtrnExact_MYValid = ("""((JAN|FEB|MAR|MAY|APR|JUL|JUN|AUG|OCT|SEP|NOV|DEC) """ + DatePtrnYearValid + """)""")
  val DatePtrnExactDMYValid = """((31(?! (FEB|APR|JUN|SEP|NOV)))|((30|29)(?! FEB))|(29(?= FEB (((1[6-9]|[2-9]\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00)))))|(0?[1-9])|1\d|2[0-8]) (JAN|FEB|MAR|MAY|APR|JUL|JUN|AUG|OCT|SEP|NOV|DEC) ((1[6-9]|[2-9]\d)\d{2})"""
  lazy val DatePtrnExactGedcomValid = ("""^""" + DatePtrnExactDMYValid + """|""" + DatePtrnExact_MYValid + """|""" + DatePtrnExact__YValid + """$""").r
  //-- dd MMM yyyy (Gedcom format) => http://regexlib.com/Search.aspx?k=date&c=5&m=-1&ps=20&p=4

  //  //lazy val DatePtrnExactValidLt = """^((((19|20)(([02468][048])|([13579][26]))-02-29))|((20[0-9][0-9])|(19[0-9][0-9]))-((((0[1-9])|(1[0-2]))-((0[1-9])|(1[[0-9]])|(2[0-8])))|((((0[13578])|(1[02]))-31)|(((0[1,3-9])|(1[0-2]))-(29|30)))))$""".r
  //    //-- yyyy-mm-dd  => http://datacleaner.org/regex/ISO%20date%20(yyyy-mm-dd)
  // D925-3/vsh old -- lazy val DatePtrnExactValidLt = """^((((16|17|18|19|20)(([02468][048])|([13579][26]))-02-29))|((20[0-9][0-9])|(19[0-9][0-9])|(18[0-9][0-9])|(17[0-9][0-9])|(16[0-9][0-9]))-((((0[1-9])|(1[0-2]))-((0[1-9])|(1\d)|(2[0-8])))|((((0[13578])|(1[02]))-31)|(((0[1,3-9])|(1[0-2]))-(29|30)))))$""".r
  val DatePtrnExactY__Lt = DatePtrnExact__YValid
//val DatePtrnExactYM_Lt_ ="""(((20[0-9][0-9])|(19[0-9][0-9])|(18[0-9][0-9])|(17[0-9][0-9])|(16[0-9][0-9]))-((0[1-9])|(1[0-2])))""".r
  val DatePtrnExactYM_Lt = """((""" + DatePtrnYearValid + """)-((0[1-9])|(1[0-2])))"""
  val DatePtrnExactYMDLt = """((((16|17|18|19|20)(([02468][048])|([13579][26]))-02-29))|((20[0-9][0-9])|(19[0-9][0-9])|(18[0-9][0-9])|(17[0-9][0-9])|(16[0-9][0-9]))-((((0[1-9])|(1[0-2]))-((0[1-9])|(1\d)|(2[0-8])))|((((0[13578])|(1[02]))-31)|(((0[1,3-9])|(1[0-2]))-(29|30)))))"""
  lazy val DatePtrnExactValidLt = ("""^""" + DatePtrnExactYMDLt + """|""" + DatePtrnExactYM_Lt + """|""" + DatePtrnExactY__Lt + """$""").r
  //-- yyyy-mm-dd  => http://regexlib.com/Search.aspx?k=date&c=5&m=-1&ps=20&p=4
  // Based on some of the other patterns on RegExpLib. This is the ISO way of writing dates.



    def dateInitValue: Map[String, Map[String, String]] = Map(
    "gdt_no_date" -> Map( "en" -> "", "lt" -> ""),
    "gdt_exact" -> Map( "en" -> getDateFormatExact, "lt" -> getDateFormatExact),
    "gdt_between" -> Map( // BET AND
      "en" -> (S ? "gd_bet" + " " + getDateFormatApprox + " " + (S ? "gd_and") + " " + getDateFormatApprox),
      "lt" -> (S ? "gd_bet" + " " + getDateFormatApprox + " " + (S ? "gd_and") + " " + getDateFormatApprox)),
    "gdt_before" -> Map( // BEF
      "en" -> (S ? "gd_bef" + " " + getDateFormatApprox),
      "lt" -> (S ? "gd_bef" + " " + getDateFormatApprox)),
    "gdt_after" -> Map( // AFT
      "en" -> (S ? "gd_aft" + " " + getDateFormatApprox),
      "lt" -> (S ? "gd_aft" + " " + getDateFormatApprox)),
    "gdt_about" -> Map( // ABT
      "en" -> (S ? "gd_abt" + " " + getDateFormatApprox),
      "lt" -> (S ? "gd_abt" + " " + getDateFormatApprox)),
    "gdt_from_to" -> Map( // FROM TO
      "en" -> (S ? "gd_from" + " " + getDateFormatApprox + " " + S ? "gd_to" + " " + getDateFormatApprox),
      "lt" -> (S ? "gd_from" + " " + getDateFormatApprox + " " + S ? "gd_to" + " " + getDateFormatApprox)),
    "gdt_from" -> Map( // FROM
      "en" -> (S ? "gd_from" + " " + getDateFormatApprox),
      "lt" -> (S ? "gd_from" + " " + getDateFormatApprox)),
    "gdt_to" -> Map( // TO
      "en" -> (S ? "gd_to" + " " + getDateFormatApprox),
      "lt" -> (S ? "gd_to" + " " + getDateFormatApprox)),
    "gdt_text" -> Map( // text
      "en" -> "(en: ...type_here... )",
      "lt" -> "(lt: ...rašykit žodžiais čia... )")
  )


  def valiDate(gcDateTypedIn: String, dateShape: String): String = {

    val gcDate = gcDateTypedIn.trim.replaceAll("( )+", " ")

    val langXx: String = S.locale.getLanguage

    val nnMMM: Map[String,String] = Map( "01"->"JAN", "02"->"FEB", "03"->"MAR",
      "04"->"APR", "05"->"MAY", "06"->"JUN",
      "07"->"JUL", "08"->"AUG", "09"->"SEP",
      "10"->"OCT", "11"->"NOV", "12"->"DEC" )

//    //lazy val DatePtrnValid = """(\d\d? )?(JAN |FEB |MAR |APR |MAY |JUN |JUL |AUG |SEP |OCT |NOV |DEC )?(\d\d\d\d)""".r
//
////    lazy val DatePtrnExactWsValid = """^(((((0[1-9])|(1\d)|(2[0-8]))\/((0[1-9])|(1[0-2])))|((31\/((0[13578])|(1[02])))|((29|30)\/((0[1,3-9])|(1[0-2])))))\/((20[0-9][0-9])|(19[0-9][0-9])))|((29\/02\/(19|20)(([02468][048])|([13579][26]))))$""".r
////    //-- dd/mm/yyyy  => http://forums.asp.net/t/1410702.aspx/1
//
//    lazy val DatePtrnExactGedcomValid = """^((31(?! (FEB|APR|JUN|SEP|NOV)))|((30|29)(?! FEB))|(29(?= FEB (((1[6-9]|[2-9]\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00)))))|(0?[1-9])|1\d|2[0-8]) (JAN|FEB|MAR|MAY|APR|JUL|JUN|AUG|OCT|SEP|NOV|DEC) ((1[6-9]|[2-9]\d)\d{2})$""".r
//    //-- dd MMM yyyy (Gedcom format) => http://regexlib.com/Search.aspx?k=date&c=5&m=-1&ps=20&p=4
//
////  //lazy val DatePtrnExactValidLt = """^((((19|20)(([02468][048])|([13579][26]))-02-29))|((20[0-9][0-9])|(19[0-9][0-9]))-((((0[1-9])|(1[0-2]))-((0[1-9])|(1[[0-9]])|(2[0-8])))|((((0[13578])|(1[02]))-31)|(((0[1,3-9])|(1[0-2]))-(29|30)))))$""".r
////    //-- yyyy-mm-dd  => http://datacleaner.org/regex/ISO%20date%20(yyyy-mm-dd)
//    lazy val DatePtrnExactValidLt = """^((((19|20)(([02468][048])|([13579][26]))-02-29))|((20[0-9][0-9])|(19[0-9][0-9]))-((((0[1-9])|(1[0-2]))-((0[1-9])|(1\d)|(2[0-8])))|((((0[13578])|(1[02]))-31)|(((0[1,3-9])|(1[0-2]))-(29|30)))))$""".r
//    //-- yyyy-mm-dd  => http://regexlib.com/Search.aspx?k=date&c=5&m=-1&ps=20&p=4
//    // Based on some of the other patterns on RegExpLib. This is the ISO way of writing dates.
//
    def isGdtExact(i18nDateValue: String): String = {
      val i18nDatVal = i18nDateValue //.trim.replaceAll("( )+", " ")
      log.debug("isGdtExact S.locale.getLanguage i18nDateValue i18nDatVal |" + S.locale.getLanguage + "| |"  + i18nDateValue + "| |" + i18nDatVal + "|")
      S.locale.getLanguage match {
        case "lt" =>
          log.debug("isGdtExact lt 1")
          try {
            DatePtrnExactValidLt findPrefixOf /*findFirstIn*/ i18nDatVal match {
              case Some(ymd) =>
                log.debug("isGdtExact DatePtrnExactValidLt i18nDatVal ymd |" + i18nDatVal + "| |" + ymd + "|")
                ""
              case _ =>
                log.debug("isGdtExact lt 2")
                S ? "gdt.badExact"
            }
          } catch {
            case ex: Exception =>
              log.debug("isGdtExact DatePtrnExactValidLt exception: " + ex.toString/* + ";  |" + i18nDatVal + "|" */)
              S ? "gdt.badExact"
          }
        case "en" =>
          try {
            DatePtrnExactGedcomValid findPrefixOf /*findFirstIn*/ i18nDatVal match {
              case Some(dmy) =>
                log.debug("isGdtExact DatePtrnExactValidLt i18nDatVal ymd |" + i18nDatVal + "| |" + dmy + "|")
                ""
              case _ =>
                log.debug("isGdtExact lt 2")
                S ? "gdt.badExact"
            }
          } catch {
            case ex: Exception =>
              log.debug("isGdtExact DatePtrnExactValidEn exception: " + ex.toString/* + ";  |" + i18nDatVal + "|" */)
              S ? "gdt.badExact"
          }
      }
    }

    def isValiDate(i18nDatVal: String): String = {
      log.debug("isValiDate S.locale.getLanguage i18nDatVal isoDatVal|" + S.locale.getLanguage + "| |" + i18nDatVal  + "| |" + GedcomUtil.iso8601Date(i18nDatVal) + "|")
      try {
        GedcomUtil.iso8601Date(i18nDatVal) match {
        case "0000-00-00" =>
          S ? "date.is.invalid"
        case ymd if ymd.size == 10 =>
          DatePtrnExactValidLt findPrefixOf ymd match {
            case Some(x) =>
              log.debug("isValiDate Some(x)|" + x + "|")
              ""
            case None =>
              log.debug("isValiDate None")
              S ? "date.is.invalid"
          }
        case ymd if ymd.size == 7 =>
          """^(1[4-9]|20)\d\d[-](0[1-9]|1[012])$""".r findPrefixOf ymd match {
            case Some(x) => ""
            case None => S ? "date.is.invalid"
          }
        case ymd if ymd.size == 4 =>
          """^(1[4-9]|20)\d\d$""".r findPrefixOf ymd match {
            case Some(x) => ""
            case None => S ? "date.is.invalid"
          }
        }
      } catch {
        case ex: Exception =>
          log.debug("isValiDate exception: " + ex.toString)
          S ? "date.is.invalid"
      }
    }

    def isTwoDates(i18nDateValue: String, option: String): String = {
      val i18nDatVal = i18nDateValue //.trim.replaceAll("( )+", " ")
      var aaa, zzz, xmsg: String = ""
      option match {
        case "between" =>
          aaa = S ? "gd_bet"
          zzz = S ? "gd_and"
          xmsg = "gdt.badBetween"
        case "from_to" =>
          aaa = S ? "gd_from"
          zzz = S ? "gd_to"
          xmsg = "gdt.badFromTo"
      }
      log.debug("isTwoDates S.locale.getLanguage i18nDateValue i18nDatVal |" + S.locale.getLanguage /*+ "| |"  + i18nDateValue*/ + "| |" + i18nDatVal + "|")
      lazy val TwoDatesValidLt = "^" + aaa + "\\s(.*?)\\s" + zzz + "\\s(.*?)$"
      lazy val TwoDatePtrnValidLt = TwoDatesValidLt.r
      log.debug("isTwoDates TwoDatesValidLt |" + TwoDatesValidLt + "|")
      S.locale.getLanguage match {
        case xx if xx=="lt" || xx=="en" =>
          try {
            lazy val TwoDatePtrnValidLt(lowerDate, upperDate) = i18nDatVal
            log.debug("isTwoDates lowerDate upperDate |" + lowerDate + "| |" + upperDate + "|")
            isValiDate(lowerDate) + isValiDate(upperDate) match {
              case "" =>
                GedcomUtil.valiDateLU(lowerDate, upperDate) match {
                  case true => ""
                  case false => S ? xmsg
                }
              case _ => S ? xmsg
            }
          } catch {
            case ex: Exception =>
              log.debug("isTwoDates TwoDatePtrnValidLt exception: " + ex.toString /* + ";  |" + i18nDatVal + "|" */)
              S ? xmsg
          }
        case _ =>
          S ? "gdt.badUnexpected"
      }
    }

    def isOneDate(i18nDateValue: String, option: String): String = {
      val i18nDatVal = i18nDateValue //.trim.replaceAll("( )+", " ")
      var aaa, xmsg: String = ""
      option match {
        case "before" =>
          aaa = S ? "gd_bef"
          xmsg = "date.is.invalid"
        case "after" =>
          aaa = S ? "gd_aft"
          xmsg = "date.is.invalid"
        case "about" =>
          aaa = S ? "gd_abt"
          xmsg = "date.is.invalid"
        case "from" =>
          aaa = S ? "gd_from"
          xmsg = "date.is.invalid"
        case "to" =>
          aaa = S ? "gd_to"
          xmsg = "date.is.invalid"
      }
      log.debug("isOneDate S.locale.getLanguage i18nDateValue i18nDatVal |" + S.locale.getLanguage /*+ "| |"  + i18nDateValue*/ + "| |" + i18nDatVal + "|")
      lazy val OneDateValidLt = "^" + aaa + "\\s(.*?)$"
      lazy val OneDatePtrnValidLt = OneDateValidLt.r
      log.debug("isOneDate OneDateValidLt |" + OneDateValidLt + "|")
      S.locale.getLanguage match {
        case xx if xx=="lt" || xx=="en" =>
          try {
            lazy val OneDatePtrnValidLt(lowerDate) = i18nDatVal
            log.debug("isOneDate lowerDate |" + lowerDate  + "|")
            isValiDate(lowerDate) match {
              case "" => ""
              case _ => S ? xmsg
            }
          } catch {
            case ex: Exception =>
              log.debug("isOneDate OneDatePtrnValidLt exception: " + ex.toString /* + ";  |" + i18nDatVal + "|" */)
              S ? xmsg
          }
        case _ =>
          S ? "gdt.badUnexpected"
      }
    }

    def isGdtText(i18nDateValue: String): String = {
      val i18nDatVal = if (i18nDateValue == " ") "" else i18nDateValue
      log.debug("isGdtText i18nDateValue i18nDatVal |" + i18nDateValue + "| |" + i18nDatVal + "|")
      try {
        lazy val TextDatePtrnOpt = """^\((en|lt|de|pl|ru)\:(.+)\)$""".r
        //    ^\((en|lt|de|pl|ru)\:(.+)\)$  -->  (lt: testas)
        //    ^\(\([a-z]{2}\)\:\|(.+)\|\)$  -->  ((lt):|asasasd|)
        TextDatePtrnOpt findPrefixOf /*findFirstIn*/ i18nDatVal match {
          case Some(ymdtxt) =>
            log.debug("isGdtText  i18nDatVal ymdtxt |" + i18nDatVal + "| |" + ymdtxt + "|")
            val TextDatePtrnOpt(lang, daText) = i18nDatVal
            (lang, daText) match {
              case (null, dt) =>
                log.debug("isGdtText |" + "(null, dt)" + "|")
                S ? "gdt.lang?"
              case (ln, null) =>
                log.debug("isGdtText |" + "(ln, null)" + "|")
                S ? "gdt.badText"
              case (ln, dt) =>
                ln match {
                  case xx if xx == langXx =>
                    log.debug("isGdtText (ln, dt) |" + "xx if xx == langXx" + "|")
                    ""
                  case _ =>
                    log.debug("isGdtText (ln, dt) |" + "_" + "|")
                    S ? "gdt.lang?"
                }
              case _ => ""
            }
          case _ =>
            log.debug("isGdtText lt 2")
            S ? "gdt.badText"
        }
      } catch {
        case ex: Exception =>
          log.debug("isGdtText exception: " + ex.toString/* + ";  |" + i18nDatVal + "|" */)
          S ? "gdt.badText"
      }
    }

    dateShape match {
      case "gdt_no_date" => gcDate match {
        case str if str.size > 0 => S ? "gdt.badNoDate"
        case _ => ""
      }
      case "gdt_exact" => isGdtExact(gcDate)
      case "gdt_between" => isTwoDates(gcDate, "between")  // isGdtBetween(gcDate)  // BET
      case "gdt_before" => isOneDate(gcDate, "before")  // BEF
      case "gdt_after" => isOneDate(gcDate, "after")  // AFT
      case "gdt_about" => isOneDate(gcDate, "about")  // ABT
      case "gdt_from_to" => isTwoDates(gcDate, "from_to")
      case "gdt_from" => isOneDate(gcDate, "from")  // FROM
      case "gdt_to" => isOneDate(gcDate, "to")  // TO
      case "gdt_text" => isGdtText(gcDate)
    }

  }


  def defineDateShape(gcDate: String): String = {
    //lazy val ptrn_gdt_no_date = """xxx""".r
    lazy val ptrn_gdt_exact = DatePtrnExactGedcomValid
    lazy val ptrn_MMMmb_yyyy = """^(JAN |FEB |MAR |APR |MAY |JUN |JUL |AUG |SEP |OCT |NOV |DEC )?(\d\d\d\d)$""".r
    lazy val ptrn_gdt_between = """^BET\s.*?\sAND\s.*?$""".r
    lazy val ptrn_gdt_before = """^BEF\s.*?$""".r
    lazy val ptrn_gdt_after = """^AFT\s.*?$""".r
    lazy val ptrn_gdt_about = """^ABT\s.*?$""".r
    lazy val ptrn_gdt_from_to = """^FROM\s.*?\sTO\s.*?$""".r
    lazy val ptrn_gdt_from = """^FROM\s.*?$""".r
    lazy val ptrn_gdt_to = """^TO\s.*?$""".r
    lazy val ptrn_gdt_text = """^\(.*?\S+.*?\)$""".r    // """^\(([a-z]{2}):\|(.+)\|\)$""".r
    gcDate match {
      case gcd if gcd.size==0 => "gdt_no_date"
      case gcd if ptrn_gdt_exact.findPrefixOf(gcd).isEmpty.unary_!  => "gdt_exact"
      case gcd if ptrn_MMMmb_yyyy.findPrefixOf(gcd).isEmpty.unary_!  => "gdt_about"
      case gcd if ptrn_gdt_between.findPrefixOf(gcd).isEmpty.unary_!  => "gdt_between"
      case gcd if ptrn_gdt_before.findPrefixOf(gcd).isEmpty.unary_!  => "gdt_before"
      case gcd if ptrn_gdt_after.findPrefixOf(gcd).isEmpty.unary_!  => "gdt_after"
      case gcd if ptrn_gdt_about.findPrefixOf(gcd).isEmpty.unary_!  => "gdt_about"
      case gcd if ptrn_gdt_from_to.findPrefixOf(gcd).isEmpty.unary_!  => "gdt_from_to"
      case gcd if ptrn_gdt_from.findPrefixOf(gcd).isEmpty.unary_!  => "gdt_from"
      case gcd if ptrn_gdt_to.findPrefixOf(gcd).isEmpty.unary_!  => "gdt_to"
      case gcd if ptrn_gdt_text.findPrefixOf(gcd).isEmpty.unary_!  => "gdt_text"
      case _ => "gdt_text"
    }

  }


}

