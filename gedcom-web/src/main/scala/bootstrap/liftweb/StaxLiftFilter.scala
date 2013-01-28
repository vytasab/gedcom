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

  class StaxLiftFilter extends LiftFilter {
    override def init(config: FilterConfig){
      //System.setProperty("run.mode", "production")
      System.setProperty("run.mode", "development")
      super.init(config)
    }
  }


