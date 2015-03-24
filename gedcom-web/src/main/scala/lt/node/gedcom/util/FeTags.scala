package lt.node.gedcom.util


object FeTags extends AnyRef with lt.node.gedcom.util.GedcomMsgsI18n {

//  def getMsg(tag: String): String = this.msgs(tag)(S.locale.getLanguage)
//
//  def tagsByLocale(lang: String): List[(String, String)] = this.msgs.map { kv => (kv._1, kv._2(lang))}.toList
//
//  def tags: List[(String, String)] = this.tagsByLocale(S.locale.getLanguage)

  /*
 FAMILY_EVENT_STRUCTURE: =
   [
   n [ ANUL | CENS | DIV | DIVF ] [Y|<NULL>]  {1:1}     +1 <<EVENT_DETAIL>>  {0:1}
   |
   n [ ENGA | MARR | MARB | MARC ] [Y|<NULL>]  {1:1}    +1 <<EVENT_DETAIL>>  {0:1}
   |
   n [ MARL | MARS ] [Y|<NULL>]  {1:1}``````````````    +1 <<EVENT_DETAIL>>  {0:1}
   |
   n  EVEN          {1:1}                               +1 <<EVENT_DETAIL>>  {0:1}
   ]
  */
  val msgs: Map[String, Map[String, String]] = Map(
    "03ENGA" -> Map( /* ENGAGEMENT */
      "en" -> "Engagement" /* "An event of recording or announcing an agreement between two people to become married"*/,
      "lt" -> "Tapo partneriais"),
    "05MARR" -> Map( /* MARRIAGE */
      "en" -> "Marriage" /* ("A legal, common-law, or customary event of creating a family unit of a man " +
        "and a woman as husband and wife")*/,
      "lt" -> "Vestuvės (Sukūrė šeimą)"),
      //"lt" -> "Vestuvės (Sukūrė šeimą)"),
    "10DIV" -> Map( /* DIVORCE */
      "en" -> "Divorce" /* "An event of dissolving a marriage through civil action"*/,
      "lt" -> "Skyrybos"),
    "15MARB" -> Map( /* MARRIAGE_BANN */
      "en" -> "Official public notice to marry" /* "An event of an official public notice given that two people intend to marry"*/,
      "lt" -> "Sužieduotuvės"),
    "20ANUL" -> Map( /* ANNULMENT */
      "en" -> "Annulment" /* "Declaring a marriage void from the beginning (never existed)"*/,
      "lt" -> "Sužiedotuvės anuliuotos"),
    "25DIVF" -> Map( /* DIVORCE_FILED} */
      "en" -> "Divorce filed" /* "An event of filing for a divorce by a spouse"*/,
      "lt" -> "Skirybų pareiškimas"),
    "30MARC" -> Map( /* MARR_CONTRACT */
      "en" -> "Marriage contract" /* ("An event of recording a formal agreement of marriage, including the prenuptial " +
        "agreement in which marriage partners reach agreement about the property rights " +
        "of one or both, securing property to their children.")*/,
      "lt" -> "Vedybinė sutartis"),
    //"MARS" -> Map( /* MARR_SETTLEMENT */
    //  "en" -> "" /* "An event of creating an agreement between two people contemplating marriage, " +
    //    "at which time they agree to release or modify property rights that would otherwise " +
    //    "arise from the marriage. "*/,
    //  "lt" -> "Vestuvinė sutartis (?)"),
    "35EVEN" -> Map( /* EVENT */
      "en" -> "Event" /* "A noteworthy happening related to an individual, a group, or an organization"*/,
      "lt" -> "Kitoks įvykis"),
    "40MARL" -> Map( /* MARR_LICENSE */
      "en" -> "Marriage license" /* "An event of obtaining a legal license to marry"*/,
      "lt" -> "Leidimas tuoktis (?)",
      "lt?" -> "Leidimas tuoktis taikytas kilmingam luomui seniai Vakaruose")

    //"CENS" -> Map( /* CENSUS */
    //  "en" -> "" /* "he event of the periodic count of the population for a designated locality, such as a national or state Census"*/,
    //  "lt" -> "CENS"),
  )

}

