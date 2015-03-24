package lt.node.gedcom.util

//import util.Helpers._

object PeTags extends AnyRef with lt.node.gedcom.util.GedcomMsgsI18n {

//  def getMsg(key: String): String = {
//    tags.find(_._1 == key).orElse(Full((key,key))).get._2
//  }
//
//  def getKey(value: String): String = tags.find(_._2 == value).get._1
//
//  def tagsByLocale(lang: String): List[(String, String)] = this.msgs.map { kv => (kv._1, kv._2(lang))}.toList
//
//  def tags: List[(String, String)] = this.tagsByLocale(S.locale.getLanguage).
//    sortWith((a,b) => {a._1.substring(0,2).toInt < b._1.substring(0,2).toInt}).
//    map((kv) => {(kv._1.substring(2), kv._2)})
//  //-----   .filter( _._1 != "gdt_and")  // .filter( (kv) => {kv._1 != "gdt_and"})

// def getMsg(tag: String): String = this.msgs(tag)(S.locale.getLanguage)
//def getMsg(tag: String): String = this.msgs(tag)(S.locale.getLanguage)

//  def tagsByLocale(lang: String): List[(String, String)] = this.msgs.map { kv => (kv._1, kv._2(lang))}.toList
//def tagsByLocale(lang: String): List[(String, String)] = this.msgs.map { kv => (kv._1, kv._2(lang))}.toList

//  def tags: List[(String, String)] = this.tagsByLocale(S.locale.getLanguage).
//    sortWith((a,b) => {a._1.substring(0,2).toInt < b._1.substring(0,2).toInt}).
//    map((kv) => {(kv._1.substring(2), kv._2)})
//def tags: List[(String, String)] = this.tagsByLocale(S.locale.getLanguage)
// //.keySet map(o => ())

  /*
 INDIVIDUAL_EVENT_STRUCTURE: =
   [
   n[ BIRT | CHR ] [Y|<NULL>]  {1:1}                         +1 <<EVENT_DETAIL>>  {0:1}      +1 FAMC @<XREF:FAM>@  {0:1}
       |
   n  [ DEAT | BURI | CREM ] [Y|<NULL>]   {1:1}              +1 <<EVENT_DETAIL>>  {0:1}
     |
   n  ADOP [Y|<NULL>]  {1:1}     +1 <<EVENT_DETAIL>>  {0:1}  +1 FAMC @<XREF:FAM>@  {0:1}     +2 ADOP <ADOPTED_BY_WHICH_PARENT>  {0:1}
                       |
   n  [ BAPM | BARM | BASM | BLES ] [Y|<NULL>]  {1:1}        +1 <<EVENT_DETAIL>>  {0:1}
     |
   n  [ CHRA | CONF | FCOM | ORDN ] [Y|<NULL>]  {1:1}        +1 <<EVENT_DETAIL>>  {0:1}
     |
   n  [ NATU | EMIG | IMMI ] [Y|<NULL>]  {1:1}               +1 <<EVENT_DETAIL>>  {0:1}
     |
   n  [ CENS | PROB | WILL] [Y|<NULL>]  {1:1}                +1 <<EVENT_DETAIL>>  {0:1}
     |
   n  [ GRAD | RETI ] [Y|<NULL>]  {1:1}                      +1 <<EVENT_DETAIL>>  {0:1}
     |
   n  EVEN          {1:1}                                    +1 <<EVENT_DETAIL>>  {0:1}
     ]
  */
  val msgs: Map[String, Map[String, String]] = Map(
// TODO aet up tag sequence
    "01BIRT" -> Map( /* BIRTH */
      "en" -> "Birth event.",
      "lt" -> "Gimė"),
    "02ADOP" -> Map(
      "en" -> "Adopted",
      "lt" -> "Įvaikino"),
    "03CHR" -> Map( /* CHRISTENING */
      "en" -> "Christening" /*"The religious event (not LDS) of baptizing and/or naming a child."*/,
      "lt" -> "Krikštijo"),
    //"[ BAPM | BARM | BASM | BLES ]" -> Map(
    //  "en" -> "",
    //  "lt" -> ""
    //),
    //"[ CHRA | CONF | FCOM | ORDN ]" -> Map(
    //  "en" -> "",
    //  "lt" -> ""
    //),
    "04GRAD" -> Map( /* GRADUATION */
      "en" -> "Graduation" /*"An event of awarding educational diplomas or degrees to individuals"*/,
      "lt" -> "Įgijo mokslą"),
    "05IMMI" -> Map( /* IMMIGRATION */
      "en" -> "Immigration"  /*"An event of entering into a new locality with the intent of residing there"*/,
      "lt" -> "Imigravo"),
    "06EMIG" -> Map( /* EMIGRATION */
      "en" -> "Emigration" /*"An event of leaving one's homeland with the intent of residing elsewhere"*/,
      "lt" -> "Emigravo"),
    "07NATU" -> Map( /* NATURALIZATION */
      "en" -> "Naturalization" /*"The event of obtaining citizenship"*/,
      "lt" -> "Gavo pilietybę"),
    //"CENS" -> Map( /* CENSUS */
    //  "en" -> "he event of the periodic count of the population for a designated locality, such as a national or state Census",
    //  "lt" -> "CENS"),
    //"PROB" -> Map( /* PROBATE */
    //  "en" -> "An event of judicial determination of the validity of a will. May indicate several related court activities over several dates",
    //  "lt" -> "Surašytas testamentas"),
    "08WILL" -> Map( /* WILL */
      "en" -> "Will" /*"A legal document treated as an event, by which a person disposes of his or her estate, to take effect after death. The event date is the date the will was signed while the person was alive. (See also PROBate.)"*/,
      "lt" -> "Patvirtinto testamentą"),
    "09RETI" -> Map( /* RETIREMENT  */
      "en" -> "Retirement" /*"An event of exiting an occupational relationship with an employer after a qualifying time period"*/,
      "lt" -> "Išėjo pensijon"),
    "10EVEN" -> Map( /* EVENT */
      "en" ->  "Event" /*"A noteworthy happening related to an individual, a group, or an organization"*/,
      "lt" -> "Įvykis"),
    "11DEAT" -> Map( /* DEATH */
      "en" ->  "Death" /*"The event when mortal life terminates"*/,
      "lt" -> "Mirė"),
    "12BURI" -> Map( /* BURIAL */
      "en" ->  "Burial" /*"The event of the proper disposing of the mortal remains of a deceased person"*/,
      "lt" -> "Palaidojo"),
    "13CREM" -> Map( /* CREMATION */
      "en" -> "Cremation" /*"Disposal of the remains of a person's body by fire"*/,
      "lt" -> "Kremavo")
    //,"xxx" -> Map("en" -> "en", "lt" -> "lt")
  )
}
