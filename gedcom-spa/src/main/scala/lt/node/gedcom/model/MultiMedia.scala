package lt.node.gedcom.model

import javax.persistence._
import javax.persistence.Column
import xml.{Unparsed, NodeSeq}

// http://www.javacodegeeks.com/2012/05/load-or-save-image-using-hibernate.html

// MULTIMEDIA_LINK: =
// [          /* embedded form*/
// n  OBJE @<XREF:OBJE>@  {1:1}
// |          /* linked form*/
// n  OBJE           {1:1}
// +1 FORM <MULTIMEDIA_FORMAT>  {1:1}
// +1 TITL <DESCRIPTIVE_TITLE>  {0:1}
// +1 FILE <MULTIMEDIA_FILE_REFERENCE>  {1:1}
// +1 <<NOTE_STRUCTURE>>  {0:M}
// ]

@Entity
@Table(name="multimedia")
class MultiMedia extends MultiLang {
// the table contains ACTUAL and AUDIT records
// ACTUAL records:
//      idRoot == 0;
// deleted (AUDIT) records:
//      idRoot <-- id of actual record;
//      modifier <-- setModifier (user has deleted the record)
// 'title' edit (AUDIT) records:
//      idRoot <-- id of actual record;
//`     blob <-- null
//      title <-- text before editing, i.e. old text
//      modifier <-- setModifier (user has edited the record)
//      the rest fields remain unintialized: mimeType,personmultimedia,familymultimedia,eventdetailmultimedia
/*
( idRoot: Long,
  mimeType: String, title: String,
  blob: Array[Byte],   //TODO CB15-4/vsh: how to store it?:
  personmultimedia: Person, familymultimedia: Family, eventdetailmultimedia: EventDetail,
  submitter: String, modifier: String
)
*/


  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  var id: Long = _

  @Column(unique=false, nullable=true)   // buvo false
  var idRoot: Long = 0L
  // 0:  means this record is active;  otherwise:  contains active record id;
  // when record deleted: id == idRoot;  modifier != null
  // 'title' edition causes new record creation. New record's blob is null, idRoot is equal id of active record

  @Column(name="title", unique=false, nullable=false, length=32000)
  var title: String = ""
  // i18n'able
  // DESCRIPTIVE_TITLE: = {Size=1:248}
  // The title of a work, record, item, or object.

  @Column(name="mimeType", unique=false, nullable=true, length=100)
  var mimeType: String = _
  // the value is suffix of uploaded file
  /* MULTIMEDIA_FORMAT: = {Size=3:4} [ bmp | gif | jpeg | ole | pcx | tiff | wav ]
     Indicates the format of the multimedia data associated with the specific GEDCOM context.
     This allows processors to determine whether they can process the data object.
     Any linked files should contain the data required, in the indicated format, to process the file data.
     Industry standards will emerge in this area and GEDCOM will then narrow its scope.
     CB14-3/vsh:  http://www.webmaster-toolkit.com/mime-types.shtml
                  http://www.w3.org/Protocols/rfc1341/4_Content-Type.html
                  http://en.wikipedia.org/wiki/MIME#Content-Type
  */
  //@Column(name="blob", nullable=true, columnDefinition="mediumblob")

  //@Lob
  //@Column(name="blob", nullable=true, columnDefinition="mediumblob")

  //@Column(name="blob", nullable=true, columnDefinition="longblob")

  //@Lob
  //@Column(name="blob", nullable=true, columnDefinition="longblob")

  //@Lob
  //@Column(name="blob", nullable=true)

  @Lob
  @Column(name="blobas", nullable=true, columnDefinition="LONGBLOB")
  var blobas: Array[Byte]= _
  //-- http://stackoverflow.com/questions/1944660/hibernate-database-specific-columndefinition-values
  //-- http://dev.mysql.com/doc/refman/5.0/en/reserved-words.html

  @ManyToOne(fetch=FetchType.LAZY, optional=true)
  var personmultimedia: Person = _

  @ManyToOne(fetch=FetchType.LAZY, optional=true)
  var familymultimedia: Family = _

  @ManyToOne(fetch=FetchType.LAZY, optional=true)
  var eventdetailmultimedia: EventDetail = _

  @Column(unique=false, nullable=true)     // to nebuvo
  var submitter: String  = _               // buvo = ""

  @Column(unique=false, nullable=true)
  var modifier: String = _

  def setSubmitter(u: User) = this.submitter = u.getSubmitter()

  def setModifier(u: User) = this.modifier = u.getModifier()


  def getClone4TitleEdit(u: User): MultiMediaClone4TitleEdit = MultiMediaClone4TitleEdit(id, title, u.getModifier())


  def toXml(): NodeSeq = {
    <mm id={id.toString}>
      <mimeType>{mimeType}</mimeType>
      <title>{Unparsed(avoidNull(title))}</title>
      <idRoot>{idRoot}</idRoot>
    </mm>
  }
//TODO CB15-4/vsh: how to provide it?: memoize maybe  mblob:Array[Byte],
//TODO CB27-2/vsh: title is multilang specific field

//  override def toString(/*em: EntityManager*/) = "eventdetail:[" + id + "] " + descriptor + "; " + dateValue + "; " +
//    place + "; " + ageAtEvent + "; " + cause + "; " + source + "; " +
//    ""

}


case class MultiMediaClone4TitleEdit ( idRoot: Long, title: String, modifier: String )

/*
case class MultiMediaClone ( idRoot: Long,
  mimeType: String, title: String,
  blob: Array[Byte],   //TODO CB15-4/vsh: how to store it?:
  personmultimedia: Person, familymultimedia: Family, eventdetailmultimedia: EventDetail,
  submitter: String, modifier: String
)
*/
