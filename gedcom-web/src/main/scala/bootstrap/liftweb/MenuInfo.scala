package bootstrap.liftweb
import org.slf4j.{LoggerFactory, Logger}

import net.liftweb.http._
import net.liftweb.common.Loggable
import net.liftweb.sitemap.Loc.If

//import http.S.?
import net.liftweb.sitemap._
import Loc._
/**
 * Created by IntelliJ IDEA.
 * User: padargas
 * Date: 3/25/11
 * Time: 5:54 PM
 * To change this template use File | Settings | File Templates.
 */

object MenuInfo  extends Loggable {
  val log/*: Logger*/ = LoggerFactory.getLogger("MenuInfo")
  log.debug("MenuInfo: []... ... ...")

  // CB05-1/vsh DPP: The 'S ? "Home"' etc. parameters to Menu() are call-by-name which means
  // that they are not evaluated during Boot.scala, but are evaluated when the menu is displayed.

  val entries: List[Menu] = List(
    Menu(Loc("Home", List("index"), S ? "Home"),
      Menu(Loc("errorPage", List("errorPage"), "", Hidden)),
      Menu(Loc("infoPage", List("infoPage"), "", Hidden))
    ),
    Menu(Loc("gedcom", List("topMenu"), S ? "gedcom",
      If(() => AccessControl.isHttps_?(), () => RedirectResponse(/*"/"*/AccessControl.toGo("/")))),
      Menu(Loc("personssublist", List("gedcom", "personsSublist"), S.?("person.sublist"),
        If(() => AccessControl.isAuthenticated_?(), ()=>RedirectResponse(/*"/"*/AccessControl.toGo("/"))))),
    //Menu(Loc("richPeListByXn", List("gedcom", "richPeListByXn"), S.?("person.richPeListByXn"),
      Menu(Loc("richPeListByXn", List("gedcom", "richPeListByXn"), S.?("sf.name.pe.list"),
        If(() => AccessControl.isAuthenticated_?() || System.getProperty("run.mode")=="development"/* || AccessControl.isDeveloper_?()*/, ()=>RedirectResponse("/")))),
      Menu(Loc("richPersonList", List("gedcom", "richPersonList"), S.?("person.richPersonList"), Hidden )),
      Menu(Loc("treeBranch", List("loginFSB"), S ? "fam.tree.branch",
        If(() => !AccessControl.isAuthenticated_?(), ()=>RedirectResponse(/*"/"*/AccessControl.toGo("/"))))),
      Menu(Loc("forest", List("gedcom", "forest"), S.?("forest"), Hidden)),
      Menu(Loc("addPerson", List("gedcom"/*, "addeditPerson"*/, "addAlonePerson"), S.?("add.person"),
        If(()=>AccessControl.isAuthenticated_?(), ()=>RedirectResponse(/*"/"*/AccessControl.toGo("/"))))),
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
      Menu(Loc("bindPerson", List("gedcom", "bindPerson"), S.?("person.list"), Hidden)),

    //Menu(Loc("addMultiMediaPe", List("gedcom", "addMultiMedia"), "", Hidden)),
    //Menu(Loc("addMultiMediaFa", List("gedcom", "addMultiMedia"), "", Hidden)),
      Menu(Loc("addMultiMediaED",  List("gedcom", "addMultiMedia"), "", Hidden)),
    //Menu(Loc("addMultiMediaEDi", List("gedcom", "addMultiMediaInt"), "", Hidden)),
      Menu(Loc("editMultiMedia",   List("gedcom", "editMultiMedia"), "", Hidden)),
      Menu(Loc("deleteMultiMedia", List("gedcom", "deleteMultiMedia"), "", Hidden))
    ),
    Menu(Loc("addendum", List("export"), S.?("Addendum"),
      If(() => AccessControl.isHttps_?() && AccessControl.isAuthenticated_?(),
        () => RedirectResponse(/*"/"*/AccessControl.toGo("/")))),
    //Menu(Loc("addendum", List("addendum", "export"), S.?("Addendum")),
    //Menu(Loc("addendum", List("topMenu"), S.?("Addendum")),
      Menu(Loc("aExportAll", List("addendum", "doExportAll"), S.?("export.all"),
        If(() => AccessControl.isDeveloper_?(), () => RedirectResponse(/*"/"*/AccessControl.toGo("/"))))),
      Menu(Loc("aExportPart", List("addendum", "exportPart"), S.?("export.part"))),
      //Menu(Loc("bExportAll", List("addendum", "doExportAll"), "", Hidden)),
      Menu(Loc("bExportPart", List("addendum", "doExportPart"), "", Hidden))
    ),
/*
    // Build SiteMap
    def sitemap = SiteMap(
      Menu.i("Home") / "index", // the simple way to declare a menu
      Menu.i("My Pages") / "user" / "index" >> EarlyResponse(loggedIn), //Show the page but redirect if user is not logged in
      RegisterLogin.menu, //the register/login snippet menu part

      // more complex because this menu allows anything in the
      // /static path to be visible
      Menu(Loc("Static", Link(List("static"), true, "/static/index"),
        "Static Content")))
 */

    Menu(Loc("loginout", List("authent"), S.?("authentication"),
      If(() => AccessControl.isHttps_?(), () => RedirectResponse(/*"/"*/AccessControl.toGo("/")))),
    //Menu(Loc("loginout", List("topMenu"), S ? "authentication"),
      //Loc("authent", User.basePath, "Authentication"),
      Menu(Loc("UserLogin", "login"::"login"::Nil, S.?("log.in"),
        If(() => !AccessControl.isAuthenticated_?(), () => RedirectResponse(/*"/"*/AccessControl.toGo("/"))))),
      Menu(Loc("UserLogout", List("logout"), S.?("log.out"),
        If(() => AccessControl.isAuthenticated_?(), () => RedirectResponse(/*"/"*/AccessControl.toGo("/"))))),
      Menu(Loc("UserReset", List("login", "lostPassword"), S.?("lost.password"),
        If(() => !AccessControl.isAuthenticated_?(), () => RedirectResponse(/*"/"*/AccessControl.toGo("/"))))),
      Menu(Loc("UserChgPsw", List("login", "changePassword"), S.?("change.password"),
        If(() => AccessControl.isAuthenticated_?(), () => RedirectResponse(/*"/"*/AccessControl.toGo("/"))))),
      Menu(Loc("UserNewPsw", List("login", "newPassword"), S.?("password.new"),
        If(() => !AccessControl.isAuthenticated_?(), () => RedirectResponse(/*"/"*/AccessControl.toGo("/"))))),
      Menu(Loc("UserEdit", List("login", "useredit"),S.?("edit.profile"),
        If(() => AccessControl.isAuthenticated_?(), () => RedirectResponse(/*"/"*/AccessControl.toGo("/"))))),
      Menu(Loc("UserAdd", List("login", "useradd"),S.?("sign.up"),
        If(() => !AccessControl.isAuthenticated_?(), () => RedirectResponse(/*"/"*/AccessControl.toGo("/"))))),
      Menu(Loc("UserValidation", List("validation"), ""/*?("UserValidation")*/, Hidden)),
      Menu(Loc("UserAddEndup", List("login", "addendup"), "", Hidden))
    ),
    Menu(Loc("workshop", List("workshop"), S.?("Workshop"),
      If(() => AccessControl.isHttps_?() && AccessControl.isDeveloper_?(), () => RedirectResponse(/*"/"*/AccessControl.toGo("/")))),
      Menu(Loc("sendMailTestWorkshop", List("bandymai", "sendMailTest"),S.?("Send Mail Test"))),
      Menu(Loc("wizard", List("bandymai", "wizard"), "Wizard")),
      Menu(Loc("ajax", List("bandymai", "ajax"), "AjaxForm")),
      Menu(Loc("gedcomTree", List("gedcom", "personList"), S.?("person.list"))),
      //Menu(Loc("Uberscreen", List("bandymai", "uberscreen"),S.?("Uberscreen"))),
      //Menu(Loc("restStatic", List("bandymai", "raphaelworkshop"), "Raphael Workshop")),
      Menu(Loc("LiftScreen", List("bandymai", "liftField"), S.?("LiftField"))),
      Menu(Loc("popupwindow", List("bandymai", "popupwindow"),S.?("PopupWindow"))),
      //--Menu(Loc("sendMailTest", List("bandymai", "sendMailTest"),S.?("Send Mail Test"))),
      Menu(Loc("autoCompleteSample", List("bandymai", "autoCompleteSample"),S.?("AutoComplete"))),
      Menu(Loc("recursive", List("bandymai", "recursive"),S.?("Recursive"))),
      Menu(Loc("one", List("bandymai", "one"),S.?("Recursive-one"))),
      Menu(Loc("two", List("bandymai", "two"),S.?("Recursive-two"))),
      Menu(Loc("both", List("bandymai", "both"),S.?("Recursive-both")))
    )
  )

}
