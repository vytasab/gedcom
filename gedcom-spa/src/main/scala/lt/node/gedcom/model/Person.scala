package lt.node.gedcom.model

import javax.persistence._

//import org.slf4j.{LoggerFactory, Logger}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.xml.NodeSeq

import _root_.net.liftweb._
import http.S

import common._

//import model._

/**
 * TstPerson
 *
INDIVIDUAL_RECORD:=
n @XREF:INDI@ INDI          { 1:1 }
+1 RESN <RESTRICTION_NOTICE>          { 0:1 } p.52              ?
+1 <<PERSONAL_NAME_STRUCTURE>>          { 0:M } p.34
+1 SEX <SEX_VALUE>          { 0:1 } p.53                         +
+1 <<INDIVIDUAL_EVENT_STRUCTURE>>          { 0:M } p.31
+1 <<INDIVIDUAL_ATTRIBUTE_STRUCTURE>>          { 0:M } p.30
+1 <<LDS_INDIVIDUAL_ORDINANCE>>          { 0:M } p.32
+1 <<CHILD_TO_FAMILY_LINK>>          { 0:M } p.29
+1 <<SPOUSE_TO_FAMILY_LINK>>          { 0:M } p.35
+1 SUBM @<XREF:SUBM>@          { 0:M } p.55
+1 <<ASSOCIATION_STRUCTURE>>          { 0:M } p.29
+1 ALIA @<XREF:INDI>@          { 0:M } p.55
+1 ANCI @<XREF:SUBM>@          { 0:M } p.55
+1 DESI @<XREF:SUBM>@          { 0:M } p.55
+1 <<SOURCE_CITATION>>          { 0:M } p.34
+1 <<MULTIMEDIA_LINK>>          { 0:M } p.33,26
+1 <<NOTE_STRUCTURE>>          { 0:M } p.33
+1 RFN <PERMANENT_RECORD_FILE_NUMBER>          { 0:1 } p.50
+1 AFN <ANCESTRAL_FILE_NUMBER>          { 0:1 } p.38
+1 REFN <USER_REFERENCE_NUMBER>          { 0:M } p.55
+2 TYPE <USER_REFERENCE_TYPE>          { 0:1 } p.55
+1 RIN <AUTOMATED_RECORD_ID>          { 0:1 } p.38
+1 <<CHANGE_DATE>> *
 */



@Entity
@Table(name = "person")
class Person  {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column(unique = false, nullable = false, length = 250)
  var nameGivn: String = ""

  @Column(unique = false, nullable = false, length = 250)
  var nameSurn: String = ""

  @Column(unique = false, nullable = false, length = 1)
  var gender: String = ""
  // values: M  F

  @OneToMany(mappedBy = "personevent", targetEntity = classOf[PersonEvent], cascade = Array(CascadeType.REMOVE))
  var personevents: java.util.Set[PersonEvent] = new java.util.HashSet[PersonEvent]()

  @OneToMany(mappedBy = "personattrib", targetEntity = classOf[PersonAttrib], cascade = Array(CascadeType.REMOVE))
  var personattribs: java.util.Set[PersonAttrib] = new java.util.HashSet[PersonAttrib]()

  @OneToMany(mappedBy = "personmultimedia", targetEntity = classOf[MultiMedia], cascade = Array(CascadeType.REMOVE))
  var personmultimedias: java.util.Set[MultiMedia] = new java.util.HashSet[MultiMedia]()

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  var family: Family = _
  // Is child in the family;  look at 'children' in Family

  //@ManyToOne(fetch = FetchType.LAZY, optional = true)
  var submitter = ""


  def getPersonEvents(em: EntityManager) = {
    // AC05-7/vsh workaround for wrong 'personevents' setting
    val retrievedPersonEvent: java.util.List[PersonEvent] = em.createNamedQuery("findPersonEventByPerson").
      setParameter("person", this).
      getResultList().asInstanceOf[java.util.List[PersonEvent]]
    this.personevents = new java.util.HashSet[PersonEvent](retrievedPersonEvent)
  }


  def getPersonAttribs(em: EntityManager) = {
    // AC08-3/vsh workaround for wrong 'personattribs' setting
    val retrievedPersonAttrib: java.util.List[PersonAttrib] = em.createNamedQuery("findPersonAttribByPerson").
      setParameter("person", this).
      getResultList().asInstanceOf[java.util.List[PersonAttrib]]
    this.personattribs = new java.util.HashSet[PersonAttrib](retrievedPersonAttrib)
  }


  def getPersonMultiMedias(em: EntityManager) = {
    val retrievedPersonMultiMedia: java.util.List[MultiMedia] = em.createNamedQuery("findMultiMediaByPerson").
      setParameter("person", this).
      getResultList().asInstanceOf[java.util.List[MultiMedia]]
    this.personmultimedias = new java.util.HashSet[MultiMedia](retrievedPersonMultiMedia)
  }


  /**
   * This person is father or mother in these families
   */
  def families(em: EntityManager): List[Family] = {
    val retrievedFamily: java.util.List[Family] = this.gender match {
      case "M" => em.createNamedQuery("findFamilyByHusbandId").
        setParameter("husbandId", this.id).
        getResultList().asInstanceOf[java.util.List[Family]]
      case "F" => em.createNamedQuery("findFamilyByWifeId").
        setParameter("wifeId", this.id).
        getResultList().asInstanceOf[java.util.List[Family]]
      case _ => Nil
    }
    retrievedFamily.toList
  }


  def toString(em: EntityManager) = "person:[" + id + "] " + nameGivn + " " + nameSurn + " " + gender +
    " events # " + personevents.size +
    " attribs # " + personattribs.size +
    " families # " + families(em).size +
    ""


// B318-4/vsh does not work: http://robust-it.co.uk/clone
  /*def getClone(): PersonC = {
    import com.rits.cloning._
    // //import uk.com.robust-it.cloning.Cloner
    Cloner cloner = new Cloner();
	  cloner.deepClone(this);
	}*/

  def getClone(): PersonClone = {
    PersonClone(nameGivn, nameSurn, gender, (if (family==null) "0" else family.id.toString))
  }

  def getAuditXml(old: Box[PersonClone]): NodeSeq = {
    old match {
      case Full(x) =>
        AuditHelper.checkChanges(List(
          ("nameGivn", "", nameGivn, x.nameGivn),
          ("nameSurn", "", nameSurn, x.nameSurn),
          ("gender", "", gender, x.gender),
          ("family_id", "", (if (family==null) "0" else family.id.toString), x.family_id)))
      case Empty =>
        AuditHelper.checkAddFields(List(
          ("nameGivn", "", nameGivn),
          ("nameSurn", "", nameSurn),
          ("gender", "", gender),
          ("family_id", "", (if (family==null) "0" else family.id.toString))))
      case _ =>
        NodeSeq.Empty
    }
  }
  def getAuditRec(old: Box[PersonClone]): String = {
    getAuditXml(old).toString
  }


  def toXmlGeneral(em: EntityManager, withFamilies: Boolean): NodeSeq = {
    this.getPersonEvents(em)
    this.getPersonAttribs(em)
    <person id={id.toString}>
      <nameGivn>{nameGivn}</nameGivn> <nameSurn>{nameSurn}</nameSurn> <gender>{gender}</gender>
      {(this.personmultimedias.asScala.toList).map{m: MultiMedia => {m.toXml}}.toSeq}
      {for (e <- this.personevents.toList) yield
    <event>
        {e.toXml(em)}
      </event>}
      {for (e <- this.personattribs.toList) yield
      <attrib>
        {e.toXml(em)}
      </attrib>}
      {if (withFamilies) this.toXmlFamilies(em) }
    </person>
// TODO CB15-4/vsh: add MultiMedia
    //this.toXmlFamilies(em) // ? recursive call
  }


  def toXml(em: EntityManager): NodeSeq = {
    this.getPersonEvents(em)
    this.getPersonAttribs(em)
    this.toXmlGeneral(em, false)
    /*<person id={id.toString}>
      <nameGivn>{nameGivn}</nameGivn> <nameSurn>{nameSurn}</nameSurn> <gender>{gender}</gender>
      {for (e <- this.personevents.toList) yield
      <event>
        {e.toXml(em)}
      </event>}
      {for (e <- this.personattribs.toList) yield
      <attrib>
        {e.toXml(em)}
      </attrib>}
    </person>*/
    //this.toXmlFamilies(em) // ? recursive call
  }


  def toXmlFamilies(em: EntityManager): NodeSeq = {
    val families: List[Family] = this.families(em)
    <families lang={S.locale.getLanguage}>
      {for (f <- families /*.sort(_<_)*/ ) yield
        f.toXml(em)
      }
    </families>
  }


  def toXml4Update(em: EntityManager): NodeSeq = {
    val families: List[Family] = this.families(em)
    this.getPersonEvents(em)
    this.getPersonAttribs(em)
    val personEvents: List[PersonEvent] = this.personevents.toList
    val personAttribs: List[PersonAttrib] = this.personattribs.toList
    <_ lang={S.locale.getLanguage}>
      <root>{this.toXml(em)}</root>
      <pes>
        {for (pe <- personEvents /*.sort(_<_)*/ ) yield
          pe.toXml(em)
        }
      </pes>
      <pas>
        {for (pa <- personAttribs /*.sort(_<_)*/ ) yield
          pa.toXml(em)
        }
      </pas>
      this.toXmlFamilies(em)
    </_>
  }

  //  def toXml(em: EntityManager) = <person></person>:[" + id + "] " + nameGivn + " " + nameSurn + " " + gender +
  //    " events # " + personevents.size +
  //    " attribs # " + personattribs.size +
  //    " families # " + families(em).size +
  //    ""
  //

  def setSubmitter(u: User) = this.submitter = u.getSubmitter()


  def toGedcom(em: EntityManager, levelNumber: Int, lang: String): String = {
    this.getPersonEvents(em)
    this.getPersonAttribs(em)
    val txt: StringBuffer = new StringBuffer(<_>{levelNumber} @I{id.toString}@ INDI</_>.text+"\n")
    txt.append(<_>1 NAME {nameGivn} /{nameSurn}/</_>.text+"\n")
    txt.append(<_>1 SEX {gender}</_>.text+"\n")
    for (e <- this.personevents.toList)
      txt.append(e.toGedcom(em, levelNumber+1, lang))  // <INDIVIDUAL_EVENT_STRUCTURE>
    for (a <- this.personattribs.toList)
      txt.append(a.toGedcom(em, levelNumber+1, lang)) // <INDIVIDUAL_ATTRIBUTE_STRUCTURE>
    for (f <- this.families(em))
      txt.append(<_>{levelNumber+1} FAMS @F{f.id}@</_>.text+"\n")  // <SPOUSE_TO_FAMILY_LINK>
    txt.toString
  }


}

case class PersonClone(nameGivn:String, nameSurn:String, gender:String, family_id:String)
