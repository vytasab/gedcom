package bootstrap.liftweb
import org.slf4j.{LoggerFactory, Logger}

import _root_.net.liftweb._
import http._
//import http.S.?
import sitemap._
import Loc._
/**
 * Created by IntelliJ IDEA.
 * User: padargas
 * Date: 3/25/11
 * Time: 5:54 PM
 * To change this template use File | Settings | File Templates.
 */

object MenuInfo {
  val log: Logger = LoggerFactory.getLogger("Boot");

  val entries: List[Menu] = List(
    Menu(Loc("Home", List("index"), S.?("Home")),
      Menu(Loc("errorPage", List("errorPage"), "", Hidden)),
      Menu(Loc("infoPage", List("infoPage"), "", Hidden))
    ),
    Menu(Loc("gedcom", List("gedcom"), S.?("gedcom")),
      Menu(Loc("personssublist", List("gedcom", "personsSublist"), S.?("person.sublist"))),
      Menu(Loc("forest", List("gedcom", "forest"), S.?("forest"), Hidden)),
      Menu(Loc("restperson1", List("rest", "person", "1"), S.?("Vytautas"))),
      Menu(Loc("restperson2", List("rest", "person", "2"), S.?("Dalia"))),
      Menu(Loc("restperson3", List("rest", "person", "3"), S.?("Andrius"))),
      Menu(Loc("addPerson", List("gedcom", "addAlonePerson"), S.?("add.person") /*, Hidden*/)),
      //Menu(Loc("addeditPE", List("gedcom", "addeditPE"), S.?("add.person.event") /*, Hidden*/)),
      //Menu(Loc("addeditPA", List("gedcom", "addeditPA"), S.?("add.person.attrib") /*, Hidden*/)),
      Menu(Loc("personView", List("gedcom", "personView"), "", Hidden)),

      Menu(Loc("editPe", List("gedcom", "editPe"), "", Hidden)),
      Menu(Loc("deletePe", List("gedcom", "deletePe"), "", Hidden)),
      //Menu(Loc("_deletePe", List("gedcom", "_deletePe"), "", Hidden)),
      //Menu(Loc("_deletePe0", List("_deletePe"), "", Hidden)),
      Menu(Loc("editPa", List("gedcom", "editPa"), "", Hidden)),
      Menu(Loc("deletePa", List("gedcom", "deletePa"), "", Hidden)),
      Menu(Loc("editFe", List("gedcom", "editFe"), "", Hidden)),
      Menu(Loc("deleteFe", List("gedcom", "deleteFe"), "", Hidden)),

      Menu(Loc("personView0", List("gedcom", "personView0"), "", Hidden)),
      Menu(Loc("personView1", List("gedcom", "personView1"), "", Hidden)),
      Menu(Loc("personView2", List("gedcom", "personView2"), "", Hidden)),
      Menu(Loc("personView3", List("gedcom", "personView3"), "", Hidden)),
      Menu(Loc("personView4", List("gedcom", "personView4"), "", Hidden)),
      Menu(Loc("personUpdate", List("gedcom", "personUpdate"), "", Hidden)),
      Menu(Loc("addeditPerson", List("gedcom", "addeditPerson"), "", Hidden)),
      Menu(Loc("personDelete", List("gedcom", "personDelete"), "", Hidden)),
      Menu(Loc("familyChildDelete", List("gedcom", "familyChildDelete"), "", Hidden)),
      Menu(Loc("familyDelete", List("gedcom", "familyDelete"), "", Hidden)),
      Menu(Loc("peWizard", List("gedcom", "peWizard"), "", Hidden)),
      Menu(Loc("bindPerson", List("gedcom", "bindPerson"), S.?("person.list"), Hidden))
    ),
    // TODO localize menu strings
    //    Menu(Loc("UserCred", List("login", "changePassword"),S.?("Credentials"), Hidden)),
    //    Menu(Loc("UserValidation", List("validation"),S.?("UserValidation"), Hidden)),
    //    Menu(Loc("UserLogin", List("login", "index"),S.?("Login"),
    //      If(() => AccessControl.isAuthenticated_?() == false, () => RedirectResponse("/")))),
    //    Menu(Loc("UserLogout", List("logout"),S.?("Logout"),
    //      If(() => AccessControl.isAuthenticated_?(), () => RedirectResponse("/")))),
    //    Menu(Loc("UserReset", List("login", "resetPassword"),S.?("Password Reset"))),
    //    Menu(Loc("UserAdd", List("login", "useradd"),S.?("Add.User"))),

/*  Bc01/vsh deprecated
    Menu(Loc("settings", List("settings"), S.?("settings"))
    ),
*/

    Menu(Loc("addendum", List("addendum"), S.?("Addendum")),
      Menu(Loc("aExport", List("addendum", "export"), S.?("export.gedcom")))
    ),

    Menu(Loc("loginout", List("authent"), S.?("authentication")),
      //Loc("authent", User.basePath, "Authentication"),
      Menu(Loc("UserLogin", List("login", "login"), S.?("log.in"),
        If(() => AccessControl.isAuthenticated_?() == false, () => RedirectResponse("/")))),
      Menu(Loc("UserLogout", List("logout"), S.?("log.out"),
        If(() => AccessControl.isAuthenticated_?(), () => RedirectResponse("/")))),
      Menu(Loc("UserReset", List("login", "lostPassword"),S.?("lost.password"))),
      Menu(Loc("UserChgPsw", List("login", "changePassword"),S.?("change.password")
        /*,If(() => AccessControl.isAuthenticated_?(), () => RedirectResponse("/"))*/
      )),
      Menu(Loc("UserEdit", List("login", "useredit"),S.?("edit.profile"),
        If(() => AccessControl.isAuthenticated_?(), () => RedirectResponse("/")))),
      Menu(Loc("UserAdd", List("login", "useradd"),S.?("sign.up"),
        If(() => AccessControl.isAuthenticated_?() == false, () => RedirectResponse("/")))),
      Menu(Loc("UserValidation", List("validation"), ""/*?("UserValidation")*/, Hidden)),
      Menu(Loc("UserAddEndup", List("login", "addendup"), "", Hidden))
    ),

    Menu(Loc("workshop", List("workshop"), S.?("Workshop")),
      Menu(Loc("wizard", List("bandymai", "wizard"), "Wizard")),
      Menu(Loc("ajax", List("bandymai", "ajax"), "AjaxForm")),
      Menu(Loc("gedcomTree", List("gedcom", "personList"), S.?("person.list"))),
/*    Menu(Loc("library", List("MenuInfo"), S.?("Library")),
        Menu(Loc("Authors", "authors" :: "list" :: Nil, S.?("Author List"))),
        Menu(Loc("Add Author", "authors" :: "add" :: Nil, S.?("Add Author"), Hidden)),
        Menu(Loc("Books", "books" :: "list" :: Nil, S.?("Book List"))),
        Menu(Loc("Add Book", "books" :: "add" :: Nil, S.?("Add Book"), Hidden)),
        Menu(Loc("BookSearch", "books" :: "search" :: Nil, S.?("Book Search")))
      ), */
      //Menu(Loc("Uberscreen", List("bandymai", "uberscreen"),S.?("Uberscreen"))),
      //Menu(Loc("restStatic", List("bandymai", "raphaelworkshop"), "Raphael Workshop")),
      Menu(Loc("LiftScreen", List("bandymai", "liftField"), S.?("LiftField"))),
      Menu(Loc("popupwindow", List("bandymai", "popupwindow"),S.?("PopupWindow"))),
      Menu(Loc("sendMailTest", List("bandymai", "sendMailTest"),S.?("Send Mail Test"))),
      Menu(Loc("autoCompleteSample", List("bandymai", "autoCompleteSample"),S.?("AutoComplete"))),
      Menu(Loc("recursive", List("bandymai", "recursive"),S.?("Recursive"))),
      Menu(Loc("one", List("bandymai", "one"),S.?("Recursive-one"))),
      Menu(Loc("two", List("bandymai", "two"),S.?("Recursive-two"))),
      Menu(Loc("both", List("bandymai", "both"),S.?("Recursive-both")))
    )
  )
}
