package lt.node.gedcom.util

object PaTags extends AnyRef with lt.node.gedcom.util.GedcomMsgsI18n {

//  def getMsg(tag: String): String = this.msgs(tag)(S.locale.getLanguage)
//
//  def tagsByLocale(lang: String): List[(String, String)] = this.msgs.map { kv => (kv._1, kv._2(lang))}.toList
//
//  def tags: List[(String, String)] = this.tagsByLocale(S.locale.getLanguage)

  /*
 INDIVIDUAL_ATTRIBUTE_STRUCTURE: =
   [
   n  CAST <CASTE_NAME>   {1:1}                +1 <<EVENT_DETAIL>>  {0:1}
   |
   n  DSCR <PHYSICAL_DESCRIPTION>   {1:1}      +1 <<EVENT_DETAIL>>  {0:1}
   |
   n  EDUC <SCHOLASTIC_ACHIEVEMENT>   {1:1}    +1 <<EVENT_DETAIL>>  {0:1}
   |
   n  IDNO <NATIONAL_ID_NUMBER>   {1:1}*       +1 <<EVENT_DETAIL>>  {0:1}
   |
   n  NATI <NATIONAL_OR_TRIBAL_ORIGIN>   {1:1} +1 <<EVENT_DETAIL>>  {0:1}
   |
   n  NCHI <COUNT_OF_CHILDREN>   {1:1}         +1 <<EVENT_DETAIL>>  {0:1}
   |
   n  NMR <COUNT_OF_MARRIAGES>   {1:1}         +1 <<EVENT_DETAIL>>  {0:1}
   |
   n  OCCU <OCCUPATION>   {1:1}                +1 <<EVENT_DETAIL>>  {0:1}
   |
   n  PROP <POSSESSIONS>   {1:1}               +1 <<EVENT_DETAIL>>  {0:1}
   |
   n  RELI <RELIGIOUS_AFFILIATION>   {1:1}     +1 <<EVENT_DETAIL>>  {0:1}
   |
   n  RESI           {1:1}                     +1 <<EVENT_DETAIL>>  {0:1}
   |
   n  SSN <SOCIAL_SECURITY_NUMBER>   {0:1}     +1 <<EVENT_DETAIL>>  {0:1}
   |
   n  TITL <NOBILITY_TYPE_TITLE>  {1:1}        +1 <<EVENT_DETAIL>>  {0:1}
   ]
  */
  val msgs: Map[String, Map[String, String]] = Map(
    "05DSCR" -> Map( /* PHYSICAL_DESCRIPTION */
      "en" -> "Physical description" /*"The physical characteristics of a person, place, or thing"*/,
      "lt" -> "Fizinis apibūdinimas"),
    "10EDUC" -> Map( /* EDUCATION, SCHOLASTIC_ACHIEVEMENT */
      "en" -> "Education, scholastic achievement" /*"Indicator of a level of education attained."*/,
      "lt" -> "Išsilavinimas"),
    "15NATI" -> Map( /* NATIONALITY, NATIONAL_OR_TRIBAL_ORIGIN */
      "en" -> "Nationality, national or tribal origin" /*"The national heritage of an individual"*/,
      "lt" -> "Tautybė"),
    "20NCHI" -> Map( /* CHILDREN_COUNT, COUNT_OF_CHILDREN */
      "en" -> "Count of children" /*("The number of children that this person is known to be the parent " +
        //"of (all marriages) when subordinate to an individual, or that belong to this family " +
        //"when subordinate to a FAM_RECORD")*/,
      "lt" -> "Vaikų skaičius"),
    "25NMR" -> Map( /* MARRIAGE_COUNT, COUNT_OF_MARRIAGES */
      "en" -> "Count of mariages" /*"The number of times this person has participated in a family as a spouse or parent"*/,
      "lt" -> "Santuokų skaičius"),
    "30OCCU" -> Map( /* OCCUPATION */
      "en" -> "Occupation" /*"The type of work or profession of an individual"*/,
      "lt" -> "Profesija, darbinė veikla"),
    "35PROP" -> Map( /* PROPERTY, POSSESSIONS */
      "en" -> "Property, possesions" /*"Pertaining to possessions such as real estate or other property of interest"*/,
      "lt" -> "Nuosavybė - žemė, namai, ..."),
    "40RELI" -> Map( /* RELIGION, RELIGIOUS_AFFILIATION */
      "en" -> "Religion, religious affilation" /*"A religious denomination to which a person is affiliated or for which a record applies"*/,
      "lt" -> "Išpažįstama religija"),
    "45RESI" -> Map( /* RESIDENCE */
      "en" -> "Residence" /*"The act of dwelling at an address for a period of time"*/,
      "lt" -> "Gyvenamoji vieta"),
    "50TITL" -> Map( /* TITLE, NOBILITY_TYPE_TITLE */
      "en" -> "Title, nobility type title" /*("A description of a specific writing or other work, such as the title " +
        "of a book when used in a source context, or a formal designation used by an individual " +
        "in connection with positions of royalty or other social status, such as Grand Duke")*/,
      "lt" -> "Titulas, kilmė"),
    "55IDNO" -> Map( /* IDENT_NUMBER, NATIONAL_ID_NUMBER */
    "en" -> "Ident number, national ID number" /*"A number assigned to identify a person within some significant external system"*/,
    "lt" -> "Asmens numeris kažkokioje sistemoje")

    //"SSN" -> Map( /* SOCIAL_SECURITY_NUMBER */
    //  "en" -> ("A number assigned by the United States Social Security Administration. " +
    //    "Used for tax identification purposes"),
    //  "lt" -> "SODRA'os numeris"),

  )

}
