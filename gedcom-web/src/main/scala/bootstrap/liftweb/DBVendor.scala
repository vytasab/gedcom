package bootstrap.liftweb


import _root_.java.text.MessageFormat
import _root_.java.sql.{Connection, DriverManager}

import org.slf4j.{LoggerFactory, Logger}

import _root_.net.liftweb._
import mapper._
import common._
import util._

/**
 * Created by IntelliJ IDEA.
 * User: padargas
 * Date: 3/25/11
 * Time: 5:48 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Database connection calculation
 */

object DBVendor extends ConnectionManager {
  val log: Logger = LoggerFactory.getLogger("Boot;DBVendor");
  private var pool: List[Connection] = Nil
  private var poolSize = 0
  private val maxPoolSize = 4

  private def createOne: Box[Connection] =
    try
    {
      val driverName: String = Props.get("db.driver") openOr "test---Driver"
      //"org.h2.Driver"
      //"org.apache.derby.jdbc.EmbeddedDriver"

      val dbUrl: String = Props.get("db.url") openOr "test---jdbc:xxx"
      //"jdbc:h2:tcp://192.168.1.5:9092/C:/H2/vshmix"
      //"jdbc:derby:lift_example;create=true"

      Class.forName(driverName)

      log.debug(MessageFormat.format("Boot Props.get db.driver |{0}|,db.url |{1}|",
        Props.get("db.driver"), Props.get("db.url")))
      log.debug(MessageFormat.format("Boot Props.get db.user |{0}|,db.password |{1}|",
        Props.get("db.user"), Props.get("db.password")))

      println("DBVendor Props.get(\"__app\") = " + Props.get("__app").openOr("/gedcom/") + "|");
      println("DBVendor Props.get(\"db.driver\") = " + Props.get("db.driver").openOr("test---Driver") + "|");


      val dm = (Props.get("db.user"), Props.get("db.password")) match {
        case (Full(user), Full(pwd)) => DriverManager.getConnection(dbUrl, user, pwd)
        case _ => DriverManager.getConnection(dbUrl)
      }
      Full(dm)
    } catch {
      case e: Exception => e.printStackTrace; Empty
    }

  def newConnection(name: ConnectionIdentifier): Box[Connection] =
    synchronized{
      pool match {
        case Nil if poolSize < maxPoolSize =>
          val ret = createOne
          poolSize = poolSize + 1
          ret.foreach(c => pool = c :: pool)
          ret

        case Nil => wait(1000L); newConnection(name)
        case x :: xs => try
        {
          x.setAutoCommit(false)
          Full(x)
        } catch {
          case e => try
          {
            pool = xs
            poolSize = poolSize - 1
            x.close
            newConnection(name)
          } catch {
            case e => newConnection(name)
          }
        }
      }
    }

  def releaseConnection(conn: Connection): Unit = synchronized{
    pool = conn :: pool
    notify
  }

}
