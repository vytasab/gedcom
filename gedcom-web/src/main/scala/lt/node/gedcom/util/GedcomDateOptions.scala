package lt.node.gedcom.util

object GedcomDateOptions extends AnyRef with lt.node.gedcom.util.GedcomMsgsI18n {

  // it is used in a Wizard page title (screenTop)
  val msg4Date: Map[String, String] = Map(
    "en" -> "[[dd] MMM] yyyy",
    "lt" -> "yyyy [MM [dd]]"
  )

  val msgs: Map[String, Map[String, String]] = Map(
    "01gdt_no_date" -> Map(
      "xx" -> "",
      "en" -> "No date",
      "lt" -> "be datos"),
    "03gdt_exact" -> Map(
      "xx" -> "",
      "en" -> "Exact date",
      "lt" -> "tiksli"),
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
      "lt" -> "apytikrė"),
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
      "en" -> " plain text",
      //"en" -> "gdt_text",
      "lt" -> "žodžiais, jei kitaip netinka"),
    "45gdt_and" -> Map(
      "xx" -> "AND",
      "en" -> "gdt_and",
      "lt" -> "gdt_and")
  )
  // GEDCOM: The date range differs from the date period in that the date range
  //    is an estimate that an event happened on a single date somewhere in the date range specified.

}

