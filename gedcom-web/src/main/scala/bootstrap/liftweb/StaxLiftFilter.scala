package bootstrap.liftweb

/**
 * Created with IntelliJ IDEA.
 * User: padargas
 * Date: 7/31/12
 * Time: 6:36 AM
 * To change this template use File | Settings | File Templates.
 */

import _root_.net.liftweb.http.LiftFilter
import _root_.javax.servlet._
import net.liftweb.util.Props
import net.liftweb.util.Props.RunModes

class StaxLiftFilter extends LiftFilter {
    override def init(config: FilterConfig){
      println("StaxLiftFilter: Props.fileName |" + Props.fileName + "|")
      println("StaxLiftFilter: Props.get(\"run.mode\").get |" + Props.get("run.mode").get + "|")
      println("StaxLiftFilter: Props.mode |" + Props.mode.toString + "|")
      //System.setProperty("run.mode", "production")
      //System.setProperty("run.mode", "development")
      System.setProperty("run.mode", Props.get("run.mode").get)
      /*Props.get("run.mode").get match {
        case "production" =>
          Props.mode = RunModes.Production
        case _ =>
      }*/
      println("StaxLiftFilter: System.getProperty(\"run.mode\") = " + System.getProperty("run.mode"))
      super.init(config)
    }
  }


