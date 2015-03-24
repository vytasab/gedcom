package lt.node.gedcom.snippet

import _root_.lt.node.gedcom.util.UberScreen
import _root_.net.liftweb.wizard.Wizard
import _root_.net.liftweb._
import mapper._
import http.S
import http.LiftScreen

// http://lift.la/lifts-screen

object BandymasUberScreen extends UberScreen {
  val flavor = field(S ? "Paprastas field", "aaa")
  val ilgasTekstas = textarea("Pastaba", "pastaba", 5, 80)
  println("-- " + ilgasTekstas.toString + " --")
  val visosLokales = (java.util.Locale.getAvailableLocales.
      toList.sortWith(_.getDisplayName < _.getDisplayName).
      map(lo => (lo.toString, lo.getDisplayName))).toSeq
  val lokale = select[String]("Lokalė", "", visosLokales)
  val tinezona = select[String]("Timezone", "", MappedTimeZone.timeZoneList.toSeq)

// B301-2  override protected def redirectBack() {
//    S.seeOther("/books/list")
//  }

  override def finish() {
    S.notice("I like " + flavor.is + " too!")
    S.notice("notice " + ilgasTekstas.is)
    S.notice("lokale " + lokale.is.open_!)
    S.notice("tinezona " + tinezona.is.open_!)
    //    RedirectTo("/books/list")
  }

}

  class Kazkokia extends UberScreen  {

  // ---------------------------------------------------------------------------------------------------------------------
  val MyWizard = new Wizard {

    object completeInfo extends WizardVar(false)

    def finish() {
      S.notice("Thank you for registering your pet")
      completeInfo.set(true)
    }

    val nameAndAge = new Screen {
      val name = field(S ? "First Name", "",
        valMinLen(2, S ?? "Name Too Short"))
      val age = field(S ? "Age", 0,
        minVal(5, S ?? "Too young"),
        maxVal(120, S ?? "You should be dead"))

      override def nextScreen = if (age.is < 18) parentName else favoritePet
    }
    val parentName = new Screen {
      val parentName = field(S ? "Mom or Dad's name", "",
        valMinLen(2, S ?? "Name Too Short"),
        valMaxLen(40, S ?? "Name Too Long"))
    }
    val favoritePet = new Screen {
      val petName = field(S ? "Pet's name", "",
        valMinLen(2, S ?? "Name Too Short"),
        valMaxLen(40, S ?? "Name Too Long"))
    }
    // ---------------------------------------------------------------------------------------------------------------------
  }

  object AskAboutIceCream1 extends LiftScreen {
    val flavor = field(S ? "What's your favorite Ice cream flavor", "aaa")

    def finish() {
      S.notice("I like " + flavor.is + " too!")
    }
  }

  object AskAboutIceCream2 extends LiftScreen {
    val flavor = field(S ? "What's your favorite Ice cream flavor", "",
      trim,
      valMinLen(3, "Name too short"),
      valMaxLen(10, "That's a long name"))

    def finish() {
      S.notice("Man patinka " + flavor.is + " irgi!")
    }
  }

  object AskAboutIceCream3 extends LiftScreen {
    val flavor = field(S ? "What's your favorite Ice cream flavor", "",
      trim, valMinLen(2, S ? "Name too short"),
      valMaxLen(40, S ? "That's a long name"))
    val sauce = field(S ? "Like chocalate sauce?", false)

    def finish() {
      if (sauce) {
        S.notice(flavor.is + " tastes especially good with chocolate sauce!")
      }
      else S.notice("I like " + flavor.is + " too!")
    }
  }

  object AskAboutIceCream4 extends LiftScreen {
    val flavor = field(S ? "What's your favorite Ice cream flavor", "",
      trim, valMinLen(2, S ? "Name too short"),
      valMaxLen(40, S ? "That's a long name"))
    val sauce = field(S ? "Like chocalate sauce?", false)

    override def validations = notTooMuchChocolate _ :: super.validations

    def notTooMuchChocolate(): Errors = {
      if (sauce && flavor.toLowerCase.contains("chocolate")) "That's a lot of chocolate"
      else Nil
    }

    def finish() {
      if (sauce) {
        S.notice(flavor.is + " tastes especially good with chocolate sauce!")
      }
      else S.notice("I like " + flavor.is + " too!")
    }
  }

}