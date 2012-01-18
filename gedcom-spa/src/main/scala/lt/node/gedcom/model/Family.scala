package lt.node.gedcom.model

import javax.persistence._

import scala.collection.JavaConversions._
import _root_.scala.xml.NodeSeq

import _root_.net.liftweb._
import common._

/**
 * Family
 *
 * http://tadtech.blogspot.com/2007/09/hibernate-association-mappings-in.html
 *
FAM_RECORD: =
n @<XREF:FAM>@   FAM            { 1:1 }
+1 <<FAMILY_EVENT_STRUCTURE>>           { 0:M }
+2 HUSB               { 0:1 }
+3 AGE <AGE_AT_EVENT>           { 1:1 }
+2 WIFE               { 0:1 }
+3 AGE <AGE_AT_EVENT>           { 1:1 }
+1 HUSB @<XREF:INDI>@           { 0:1 }                 +
+1 WIFE @<XREF:INDI>@           { 0:1 }                 +
+1 CHIL @<XREF:INDI>@           { 0:M }
+1 NCHI <COUNT_OF_CHILDREN>           { 0:1 }
+1 SUBM @<XREF:SUBM>@           { 0:M }
+1 <<LDS_SPOUSE_SEALING>>           { 0:M }
+1 <<SOURCE_CITATION>>           { 0:M }
+2 <<NOTE_STRUCTURE>>           { 0:M }
+2 <<MULTIMEDIA_LINK>>           { 0:M }
+1 <<MULTIMEDIA_LINK>>           { 0:M }
+1 <<NOTE_STRUCTURE>>           { 0:M }
+1 REFN <USER_REFERENCE_NUMBER>           { 0:M }
+2 TYPE <USER_REFERENCE_TYPE>           { 0:1 }
+1 RIN <AUTOMATED_RECORD_ID>           { 0:1 }
+1 <<CHANGE_DATE>>           { 0:1 }
 */

@Entity
@Table(name = "family")
class Family /*extends Loggable*/  {

  //val log: Logger = LoggerFactory.getLogger("Family");

  //@Column(name = "familyId")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  // @OneToOne
  // @JoinColumn(name = "id")
  var husbandId: Long = 0L
  // Person = _

  // @OneToOne
  // @JoinColumn(name = "id")
  var wifeId: Long = 0L
  // Person = _

  /**/
  @Column(unique = false, nullable = true)
  var countOfChildren: Int = 0
  /**/

  @OneToMany(mappedBy = "family", targetEntity = classOf[Person], cascade = Array(CascadeType.REMOVE))
  var children: java.util.Set[Person] = new java.util.HashSet[Person]()

  @OneToMany(mappedBy = "familyevent", targetEntity = classOf[FamilyEvent], cascade = Array(CascadeType.REMOVE))
  var familyevents: java.util.Set[FamilyEvent] = new java.util.HashSet[FamilyEvent]()

  //@ManyToOne(fetch = FetchType.LAZY, optional = true)
  var submitter = ""

  def getChildren(em: EntityManager) = {
    // AC05-7/vsh workaround for wrong 'childern' setting
    val retrievedPerson: java.util.List[Person] = em.createNamedQuery("findPersonByFamily").
      setParameter("family", this).
      getResultList().asInstanceOf[java.util.List[Person]]
    this.children = new java.util.HashSet[Person](retrievedPerson)
  }

  def getFamilyEvents(em: EntityManager) = {
    // AC05-7/vsh workaround for wrong 'familyevents' setting
    val retrievedEntity: java.util.List[FamilyEvent] = em.createNamedQuery("findFamilyEventByFamily").
      setParameter("family", this).
      getResultList().asInstanceOf[java.util.List[FamilyEvent]]
    this.familyevents = new java.util.HashSet[FamilyEvent](retrievedEntity)
  }

  def toString(em: EntityManager) = {
    println("husbandId " + this.husbandId)
    println("wifeId " + this.wifeId)
    val res = "family:[" + this.id + "] + " +
      " o-> " + (if (this.husbandId == 0) " " else (em.find(classOf[Person], this.husbandId).nameGivn + "; ")) +
      " o-+ " + (if (this.wifeId == 0) " " else (em.find(classOf[Person], this.wifeId).nameGivn + "; ")) +
      " children # " + children.size +
      " events # " + familyevents.size
    res
  }

  def getClone(): FamilyClone = {
    FamilyClone(husbandId.toString, wifeId.toString, countOfChildren.toString)
  }

  def getAuditXml(old: Box[FamilyClone]): NodeSeq = {
    old match {
      case Full(x) =>
        AuditHelper.checkChanges(List(
          ("husbandId", "", husbandId.toString, x.husbandId),
          ("wifeId", "", wifeId.toString, x.wifeId),
          ("countOfChildren", "", countOfChildren.toString, x.countOfChildren)))
      case Empty =>
        AuditHelper.checkAddFields(List(
          ("husbandId", "", husbandId.toString),
          ("wifeId", "", wifeId.toString),
          ("countOfChildren", "", countOfChildren.toString)))
      case _ =>
        NodeSeq.Empty
    }
  }
  def getAuditRec(old: Box[FamilyClone]): String = {
    getAuditXml(old).toString
  }


  def toXml(em: EntityManager): NodeSeq = {
      this.getChildren(em)
      this.getFamilyEvents(em)
      <family id={id.toString}>
        <wife>
          {em.find(classOf[Person], this.wifeId) match {
            case s: Person => s.toXml(em)
            case _ =>
          }}
        </wife>
        <husband>
          {em.find(classOf[Person], this.husbandId) match {
            case s: Person => s.toXml(em)
            case _ =>
          }}
        </husband>
        {for (c <- this.children.toList) yield
        <child>
          {c.toXml(em)}
        </child>}
        {for (e <- this.familyevents.toList) yield
        <event>
          {e.toXml(em)}
        </event>}
      </family>
  }

  def setSubmitter(u: User) = this.submitter = u.getSubmitter()


  def toGedcom(em: EntityManager, levelNumber: Int, lang: String): String = {
    this.getChildren(em)
    this.getFamilyEvents(em)
    val txt: StringBuffer = new StringBuffer(<_>{levelNumber} @{id.toString}@ FAM</_>.text+"\n")
    this.husbandId match {
      case id if id > 0 => txt.append(<_>{levelNumber} HUSB {this.husbandId}</_>.text+"\n")
      case _ => ""
    }
    this.wifeId match {
      case id if id > 0 => txt.append(<_>{levelNumber} WIFE {this.wifeId}</_>.text+"\n")
      case _ => ""
    }
    this.countOfChildren match {
      case n if n > 0 => txt.append(<_>{levelNumber} NCHI {this.countOfChildren}</_>.text+"\n")
      case _ => ""
    }
    for (c <- this.children.toList)
      txt.append(<_>{levelNumber} CHIL @{c.id}@</_>.text+"\n")
    for (a <- this.familyevents.toList)
      txt.append(a.toGedcom(em, levelNumber, lang)) // <INDIVIDUAL_ATTRIBUTE_STRUCTURE>
    txt.toString
  }


}

case class FamilyClone(husbandId:String, wifeId:String, countOfChildren:String)
