package lt.node.gedcom.util

object GedcomDateOptions extends AnyRef with lt.node.gedcom.util.GedcomMsgsI18n {

  val msgs: Map[String, Map[String, String]] = Map(
    "01gdt_no_date" -> Map(
      "xx" -> "",
      "en" -> "No date",
      "lt" -> "be datos"),
    "03gdt_exact" -> Map(
      "xx" -> "",
      "en" -> "exact date",
      "lt" -> "tiksli"),
//      "lt" -> "tiksli Data"),
    "05gdt_between" -> Map(
      "xx" -> "BET",
      "en" -> "gdt_between",
      "lt" -> "apytikrė: [Tarp ... ir ... ]"),
//      "lt" -> "data: [Tarp ... ir ... ]"),
    "10gdt_before" -> Map(
      "xx" -> "BEF",
      "en" -> "gdt_before",
      "lt" -> "apytikrė: ... Prieš]"),
//      "lt" -> "data: ... Prieš]"),
    "15gdt_after" -> Map(
      "xx" -> "AFT",
      "en" -> "gdt_after",
      "lt" -> "apytikrė: [Po ..."),
//      "lt" -> "data: [Po ..."),
    "20gdt_about" -> Map(
      "xx" -> "ABT",
      "en" -> "gdt_about",
      "lt" -> "apytikrė"),
//      "lt" -> "apytikrė Data"),
    "25gdt_from_to" -> Map(
      "xx" -> "FROM_TO",
      "en" -> "gdt_from_to",
      "lt" -> "intervalas: [Nuo ... Iki]"),
//      "lt" -> "datų intervalas: [Nuo ... Iki]"),
    "30gdt_from" -> Map(
      "xx" -> "FROM",
      "en" -> "gdt_from",
      "lt" -> "intervalas: [Nuo ..."),
//      "lt" -> "datų intervalas: [Nuo ..."),
    "35gdt_to" -> Map(
      "xx" -> "TO",
      "en" -> "gdt_to",
      "lt" -> "intervalas: ... Iki]"),
//      "lt" -> "datų intervalas: ... Iki]"),
    "40gdt_text" -> Map(
      "xx" -> "",
      "en" -> "gdt_text",
      "lt" -> "žodžiais, jei kitaip netinka"),
//      "lt" -> "data Žodžiais, jei kitaip netinka"),
    "45gdt_and" -> Map(
      "xx" -> "AND",
      "en" -> "gdt_and",
      "lt" -> "gdt_and")
//      "lt" -> "gdt_and")
  )
// GEDCOM: The date range differs from the date period in that the date range
//    is an estimate that an event happened on a single date somewhere in the date range specified.

}

