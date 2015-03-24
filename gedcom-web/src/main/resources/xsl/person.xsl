<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        >
    <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>
    <xsl:param name="userIs" select="'guest'"/>
    <xsl:param name="locTexts4XSL" select="'No_value_for_param=locTexts4XSL'"/>
    <xsl:param name="lang" select="lt"/>
    <xsl:param name="mode" select="'mini'"/>
    <xsl:param name="peId" select="0"/>
    <xsl:param name="paId" select="0"/>
    <xsl:param name="feId" select="0"/>
    <xsl:param name="mmId" select="0"/>
    <xsl:param name="personId" select="0"/>
    <xsl:param name="childId" select="0"/>
    <xsl:param name="familyId" select="0"/>
    <xsl:param name="app" select="'/'"/>


    <xsl:template name="locstr">
        <xsl:param name="k"></xsl:param>
        <xsl:param name="l">lt</xsl:param>
        <!--<xsl:value-of select="concat('locTexts4XSL=|',$locTexts4XSL,'|')"/>-->
        <xsl:choose>
            <xsl:when test="$l='lt'"><xsl:value-of select="document($locTexts4XSL)//n[@k=$k]/lt"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="document($locTexts4XSL)//n[@k=$k]/en"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="/">
        <!--<xsl:value-of select="concat('|/| $lang=|',$lang,'| $mode=|',$mode,'|')"/>-->
        <!--<xsl:value-of select="concat(' $peId=|',$peId,'| $paId=|',$paId,'| $feId=|',$feId,'|')"/>-->
        <!--<xsl:value-of select="concat(' $personId=|',$personId,'| $childId=|',$childId,'| $familyId=|',$familyId,'| $app=|',$app,'|')"/>-->
        <xsl:choose> <!-- the sequence is important !!! -->
            <xsl:when test="$peId != 0 or $paId != 0 or $feId != 0">
                <xsl:apply-templates select="person" mode="onepepafe"/> <!-- PersonView.delete{Pe,Pa,Fe} -->
            </xsl:when>
            <xsl:when test="$mmId != 0">
                <xsl:apply-templates select="person" mode="onemm"/>
            </xsl:when>
            <xsl:when test="$mode='spouses'"> <!-- PersonView.familyDelete -->
                <xsl:apply-templates select="//family" mode="spouses"/>
            </xsl:when>
            <xsl:when test="$personId != 0 and $mode != 'parentsChildren' ">
                <xsl:apply-templates select="/family" mode="fullInfo"/>
            </xsl:when>
            <xsl:when test="$mode='mini'"> <!-- PersonView.renderParent -->
                <xsl:apply-templates select="person" mode="mini"/>
            </xsl:when>
            <xsl:when test="$mode='noFams'"> <!-- PersonView.{renderPerson,deletePerson} -->
                <xsl:apply-templates select="person" mode="noFams"/>
            </xsl:when>
            <xsl:when test="$mode='parentsChildren'"> <!-- PersonView.familyChildDelete -->
                <xsl:apply-templates select="//family" mode="parentsChildren"/>
            </xsl:when>
            <xsl:otherwise> <!-- PersonView.{getFamDataHtml,} -->
                <xsl:apply-templates select="/family" mode="fullInfo"/>
                <!--Error Klaida-->
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template match="person" mode="onepepafe">
        <xsl:if test="$userIs!='guest'"></xsl:if>
        <span>
                <!--<xsl:attribute name="class">
                    <xsl:value-of select="concat(gender,'-style')"/>
                </xsl:attribute>-->
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="concat($app,'rest/personView/',./@id)"/>
                    </xsl:attribute>
                    <span>
                        <b>
                            <xsl:value-of select="concat(nameGivn,' ',nameSurn,' ')"/>
                            <xsl:apply-templates select="gender"/>
                        </b>
                    </span>
                </a>
                <a>
                    <xsl:attribute name="href">
                        <xsl:value-of select="concat($app,'rest/person/',./@id)"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:call-template name="locstr">
                            <xsl:with-param name="k">back.to.forest</xsl:with-param>
                            <xsl:with-param name="l" select="$lang"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <img>
                        <xsl:attribute name="src">
                            <xsl:value-of select="concat($app,'images/page_tree.gif')"/>
                        </xsl:attribute>
                    </img>
                </a>
            </span>
        <br/>
        <br/>
        <xsl:apply-templates select="event" mode="PE"/>
        <xsl:apply-templates select="attrib"/>
        <xsl:if test="$feId != 0">
            <xsl:apply-templates select="families/family" mode="fullInfo"/>
        </xsl:if>
        <br/>
        <br/>
    </xsl:template>


    <xsl:template match="person" mode="onemm">
        <xsl:if test="$userIs!='guest'"></xsl:if>
        <span>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="concat($app,'rest/personView/',./@id)"/>
                </xsl:attribute>
                <span>
                    <b>
                        <xsl:value-of select="concat(nameGivn,' ',nameSurn,' ')"/>
                        <xsl:apply-templates select="gender"/>
                    </b>
                </span>
            </a>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="concat($app,'rest/person/',./@id)"/>
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:call-template name="locstr">
                        <xsl:with-param name="k">back.to.forest</xsl:with-param>
                        <xsl:with-param name="l" select="$lang"/>
                    </xsl:call-template>
                </xsl:attribute>
                <img>
                    <xsl:attribute name="src">
                        <xsl:value-of select="concat($app,'images/page_tree.gif')"/>
                    </xsl:attribute>
                </img>
            </a>
        </span>
        <xsl:if test="$userIs!='guest'">
            <br/>
            <br/>
            <xsl:call-template name="mm4del"/>
            <!--<xsl:apply-templates select="mm" mode="mmonly"/>-->
            <br/>
            <br/>
        </xsl:if>
    </xsl:template>


    <xsl:template match="person" mode="mini">
        <xsl:if test="$userIs!='guest'"></xsl:if>
        <span>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="concat($app,'rest/personView/',./@id)"/>
                </xsl:attribute>
                <span>
                    <b>
                        <xsl:value-of select="concat(nameGivn,' ',nameSurn,' ')"/>
                        <xsl:apply-templates select="gender"/>
                    </b>
                </span>
            </a>
        </span>
    </xsl:template>


    <xsl:template match="person" mode="noFams">
            <div class="container">
                <xsl:if test="$userIs!='guest'">
                    <div class="span-2">
                        <a>
                            <xsl:attribute name="href">
                                <!--<xsl:value-of select="concat($app,'rest/editFe/',fe/@id)"/>-->
                                <xsl:value-of select="concat($app,'gedcom/personUpdate/',./@id)"/>
                            </xsl:attribute>
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">edit.person</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <img>
                                <xsl:attribute name="src">
                                    <xsl:value-of select="concat($app,'images/','page_edit.gif')"/>
                                </xsl:attribute>
                            </img>
                        </a>
                        <xsl:choose>
                            <xsl:when test="count(//event)=0 and count(//attrib)=0">
                                <a>
                                    <xsl:attribute name="href">
                                        <!--<xsl:value-of select="concat($app,'rest/deleteFe/',fe/@id)"/>-->
                                        <xsl:value-of select="concat($app,'gedcom/personDelete/',./@id)"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:call-template name="locstr">
                                            <xsl:with-param name="k">delete.person</xsl:with-param>
                                            <xsl:with-param name="l" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:attribute>
                                    <img>
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="concat($app,'images/','page_delete.gif')"/>
                                        </xsl:attribute>
                                    </img>
                                </a>
                            </xsl:when>
                            <xsl:otherwise>
                                <img>
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="concat($app,'images/','page_deny.gif')"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                        <xsl:call-template name="locstr">
                                            <xsl:with-param name="k">pre.delete.person</xsl:with-param>
                                            <xsl:with-param name="l" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:attribute>
                                </img>
                            </xsl:otherwise>
                        </xsl:choose>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="concat($app,'rest/addMultiMedia/Pe/',./@id)"/>
                            </xsl:attribute>
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">add.multimedia</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <img>
                                <xsl:attribute name="src">
                                    <xsl:value-of select="concat($app,'images/image_new.gif')"/>
                                </xsl:attribute>
                            </img>
                        </a>
                    </div>
                </xsl:if>
                <div class="span-7 colborder">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="concat($app,'rest/personView/',./@id)"/>
                        </xsl:attribute>
                    <!--<a>-->
                        <!--<xsl:attribute name="href">-->
                            <!--<xsl:value-of select="concat($app,'rest/personView/',./@id)"/>-->
                        <!--</xsl:attribute>-->
                        <b><u>
                            <span style="font-size:larger">
                                <xsl:value-of select="concat(nameGivn,' ',nameSurn,' ')"/>
                                <xsl:apply-templates select="gender"/>
                            </span>
                        </u></b>
                    </a>
                    <!--</a>-->&#160;&#160;&#160;
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="concat($app,'rest/person/',./@id)"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:call-template name="locstr">
                                <xsl:with-param name="k">back.to.forest</xsl:with-param>
                                <xsl:with-param name="l" select="$lang"/>
                            </xsl:call-template>
                        </xsl:attribute>
                        <img>
                            <xsl:attribute name="src">
                                <xsl:value-of select="concat($app,'images/page_tree.gif')"/>
                            </xsl:attribute>
                        </img>
                    </a>
                </div>
                <div class="prepend-7 ">
                    <xsl:apply-templates select="mm" mode="full"/>
                </div>
            </div>
        <xsl:apply-templates select="event" mode="PE"/>
        <xsl:apply-templates select="attrib"/>
    </xsl:template>


    <xsl:template match="event" mode="PE">
        <!--<xsl:value-of select="concat('|',$peId,'|',$paId,'|',$feId,'||',_/pe/@id,'||')"  />-->
        <xsl:if test="($peId = 0 and $paId = 0 and $feId = 0) or ($peId = number(pe/@id))">
            <div class="container">
                <xsl:if test="$userIs!='guest'">
                    <xsl:if test="$peId = 0 and $paId = 0 and $feId = 0">
                        <div class="span-2">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($app,'rest/editPe/',pe/@id)"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">edit.person.event</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <img>
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="concat($app,'images/','page_edit.gif')"/>
                                    </xsl:attribute>
                                </img>
                            </a>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($app,'rest/deletePe/',pe/@id)"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">delete.person.event</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <img>
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="concat($app,'images/','page_delete.gif')"/>
                                    </xsl:attribute>
                                </img>
                            </a>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($app,'rest/addMultiMedia/PE/',pe/@id)"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">add.multimedia</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <img>
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="concat($app,'images/image_new.gif')"/>
                                    </xsl:attribute>
                                </img>
                            </a>
                            <!--case _ => <span>
                              <button class="lift:AddPeWizardRunner.render">
                                <lift:loc>wiz.add.pepa</lift:loc>
                                <img src="/images/page_new.gif" />
                              </button>
                              <br/><br/>
                            </span>-->
                            <!--<span>
                                <button>
                                    <xsl:attribute name="class">
                                        <xsl:value-of select="concat('','lift:AddMultiMediaWizardRunner.render')"/>
                                    </xsl:attribute>
                                    <img>
                                        <xsl:attribute name="src">
                                            <xsl:value-of select="concat($app,'images/image_new.gif')"/>
                                        </xsl:attribute>
                                    </img>
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">wiz.add.pepa</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </button>
                                <br/><br/>
                            </span>-->
                        </div>
                    </xsl:if>
                </xsl:if>
                <div class="span-4 colborder">
                    <!--<span class="span-4 colborder ed_tag" title="loc_tag;  Įvykio tipas">-->
                    <span class="span-4 colborder ed_tag">
                        <xsl:value-of select="concat('pe',pe/@tag,'_ ')"/>
                    </span>
                    <br/>
                    <xsl:if test="(pe/@tag = 'EVEN')">
                        <span class="span-4 colborder ed_tag">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.type</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:variable name="text">
                                    <xsl:call-template name="MultiLangText">
                                        <xsl:with-param name="mlt" select="pe/ed/descriptor"/>
                                        <xsl:with-param name="language" select="$lang"/>
                                    </xsl:call-template>
                            </xsl:variable>
                            <xsl:value-of select="concat(': ',$text,' ')"/>
                            <!--<xsl:value-of select="concat(': ',pe/ed/descriptor,' ')"/>-->
                        <!--<xsl:attribute name="title">   D119-6/vsh: Attribute 'title' outside of element.
                            <xsl:call-template name="locstr">
                                <xsl:with-param name="k">event.type</xsl:with-param>
                                <xsl:with-param name="l" select="$lang"/>
                            </xsl:call-template>
                        </xsl:attribute>-->
                        </span>
                    </xsl:if>
                    <xsl:if test="string-length(pe/ed/dateValue) > 0">
                        <!--<span class="span-4 colborder ed_dateValue" title="loc_dateVale;  Įvykio data">-->
                        <span class="span-4 colborder ed_dateValue">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.date</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:value-of select="concat(pe/ed/dateValue,' ')"/>
                        </span>
                        <br/>
                    </xsl:if>
                </div>
                    <div class="span-15">
                        <xsl:if test="(pe/@tag != 'EVEN')">
                            <xsl:if test="string-length(pe/ed/descriptor) > 0">
                                <span class="span-15 ed_descriptor">
                                    <xsl:attribute name="title">
                                        <xsl:call-template name="locstr">
                                            <xsl:with-param name="k">event.type</xsl:with-param>
                                            <xsl:with-param name="l" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:attribute>
                                    <xsl:variable name="text">
                                        <xsl:call-template name="MultiLangText">
                                            <xsl:with-param name="mlt" select="pe/ed/descriptor"/>
                                            <xsl:with-param name="language" select="$lang"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <xsl:value-of select="concat($text,' ')"/>
                                    <!--<xsl:value-of select="concat(pe/ed/descriptor,'  ')"/>-->
                                </span>
                            </xsl:if>
                        </xsl:if>
                        <xsl:if test="string-length(pe/ed/place) > 0">
                            <span class="span-15 ed_place">
                                <xsl:attribute name="title">
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">event.place</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <xsl:variable name="text">
                                    <xsl:call-template name="MultiLangText">
                                        <xsl:with-param name="mlt" select="pe/ed/place"/>
                                        <xsl:with-param name="language" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:value-of select="concat($text,' ')"/>
                                <!--<xsl:value-of select="concat(pe/ed/place,' ')"/>-->
                            </span>
                            <br/>
                        </xsl:if>
                        <xsl:if test="string-length(pe/ed/ageAtEvent) > 0">
                            <span class="span-15 ed_ageAtEvent">
                                <xsl:attribute name="title">
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">event.atAge</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <xsl:value-of select="concat(pe/ed/ageAtEvent,' ')"/>
                            </span>
                            <br/>
                        </xsl:if>
                        <xsl:if test="string-length(pe/ed/cause) > 0">
                            <span class="span-15 ed_cause">
                                <xsl:attribute name="title">
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">event.cause</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <xsl:variable name="text">
                                    <xsl:call-template name="MultiLangText">
                                        <xsl:with-param name="mlt" select="pe/ed/cause"/>
                                        <xsl:with-param name="language" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:value-of select="concat($text,' ')"/>
                                <!--<xsl:value-of select="concat(pe/ed/cause,' ')"/>-->
                            </span>
                            <br/>
                        </xsl:if>
                        <xsl:if test="string-length(pe/ed/source) > 0">
                            <span class="span-15 ed_source">
                                <xsl:attribute name="title">
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">event.source</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <xsl:variable name="text">
                                    <xsl:call-template name="MultiLangText">
                                        <xsl:with-param name="mlt" select="pe/ed/source"/>
                                        <xsl:with-param name="language" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:value-of select="concat($text,' ')"/>
                                <!--<xsl:value-of select="concat(pe/ed/source,' ')"/>-->
                            </span>
                            <br/>
                        </xsl:if>
                        <xsl:if test="string-length(pe/ed/note) > 0">
                            <span class="span-15 ed_note">
                                <xsl:attribute name="title">
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">event.note</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <xsl:variable name="text">
                                    <xsl:call-template name="MultiLangText">
                                        <xsl:with-param name="mlt" select="pe/ed/note"/>
                                        <xsl:with-param name="language" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:value-of select="concat($text,' ')"/>
                                <!--<xsl:value-of select="concat(pe/ed/note,' ')"/>-->
                            </span>
                            <br/>
                        </xsl:if>
                        <xsl:apply-templates select="pe/ed/mm" mode="full"/>    <!--<p>PEPEPEPEPEPEPEPEPE</p>-->
                    </div>
                </div>
    </xsl:if>
    <!--<hr/>-->
    </xsl:template>


    <xsl:template match="attrib">
        <!--<xsl:value-of select="concat('|',$peId,'|',$paId,'|',$feId,'||',_/pa/@id,'||')" />-->
        <xsl:if test="($peId = 0 and $paId = 0 and $feId = 0) or ($paId = number(pa/@id))">
            <div class="container">
                <xsl:if test="$peId = 0 and $paId = 0 and $feId = 0">
                    <xsl:if test="$userIs!='guest'">
                        <div class="span-2">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($app,'rest/editPa/',pa/@id)"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">edit.person.attrib</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <img>
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="concat($app,'images/','page_edit.gif')"/>
                                    </xsl:attribute>
                                </img>
                            </a>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($app,'rest/deletePa/',pa/@id)"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">delete.person.attrib</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <img>
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="concat($app,'images/','page_delete.gif')"/>
                                    </xsl:attribute>
                                </img>
                            </a>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($app,'rest/addMultiMedia/PA/',pa/@id)"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">add.multimedia</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <img>
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="concat($app,'images/image_new.gif')"/>
                                    </xsl:attribute>
                                </img>
                            </a>
                        </div>
                    </xsl:if>
                </xsl:if>
                <div class="span-4 colborder">
                    <span class="ed_tag">
                        <xsl:value-of select="concat('pa',pa/@tag,'_ ')"/>
                    </span>
                    <xsl:if test="string-length(pa/ed/dateValue) > 0">
                        <br/>
                        <span class="ed_dateValue">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.date</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:value-of select="concat(pa/ed/dateValue,' ')"/>
                        </span>
                    </xsl:if>
                </div>
                <div class="span-15">
                    <span class="span-15 ed_descriptor">
                        <xsl:attribute name="title">
                            <xsl:call-template name="locstr">
                                <xsl:with-param name="k">attrib.value</xsl:with-param>
                                <xsl:with-param name="l" select="$lang"/>
                            </xsl:call-template>
                        </xsl:attribute>
                        <xsl:variable name="text">
                            <xsl:call-template name="MultiLangText">
                                <xsl:with-param name="mlt" select="pa/tagValue"/>
                                <xsl:with-param name="language" select="$lang"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:value-of select="concat($text,' ')"/>
                    </span>
                    <br/>
                    <xsl:if test="string-length(pa/ed/descriptor) > 0">
                        <span class="span-15 ed_descriptor">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.type</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:variable name="text">
                                <xsl:call-template name="MultiLangText">
                                    <xsl:with-param name="mlt" select="pa/ed/descriptor"/>
                                    <xsl:with-param name="language" select="$lang"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:value-of select="concat($text,' ')"/>
                        </span>
                        <br/>
                    </xsl:if>
<!--
                    <xsl:if test="string-length(pa/ed/dateValue) > 0">
                        <span class="span-15 ed_dateValue">
                            <xsl:value-of select="concat(pa/ed/dateValue,' ')"/>
                        </span>
                        <br/>
                    </xsl:if>
-->
                    <xsl:if test="string-length(pa/ed/place) > 0">
                        <span class="span-15 ed_place">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.place</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:variable name="text">
                                <xsl:call-template name="MultiLangText">
                                    <xsl:with-param name="mlt" select="pa/ed/place"/>
                                    <xsl:with-param name="language" select="$lang"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:value-of select="concat($text,' ')"/>
                            <!--<xsl:value-of select="concat(pa/ed/place,' ')"/>-->
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="string-length(pa/ed/ageAtEvent) > 0">
                        <span class="span-15 ed_ageAtEvent">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.atAge</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:value-of select="concat(pa/ed/ageAtEvent,' ')"/>
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="string-length(pa/ed/cause) > 0">
                        <span class="span-15 ed_cause">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.cause</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:variable name="text">
                                <xsl:call-template name="MultiLangText">
                                    <xsl:with-param name="mlt" select="pa/ed/cause"/>
                                    <xsl:with-param name="language" select="$lang"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:value-of select="concat($text,' ')"/>
                            <!--<xsl:value-of select="concat(pa/ed/cause,' ')"/>-->
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="string-length(pa/ed/source) > 0">
                        <span class="span-15 ed_source">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.source</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:variable name="text">
                                <xsl:call-template name="MultiLangText">
                                    <xsl:with-param name="mlt" select="pa/ed/source"/>
                                    <xsl:with-param name="language" select="$lang"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:value-of select="concat($text,' ')"/>
                            <!--<xsl:value-of select="concat(pa/ed/source,' ')"/>-->
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="string-length(pa/ed/note) > 0">
                        <span class="span-15 ed_note">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.note</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:variable name="text">
                                <xsl:call-template name="MultiLangText">
                                    <xsl:with-param name="mlt" select="pa/ed/note"/>
                                    <xsl:with-param name="language" select="$lang"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:value-of select="concat($text,' ')"/>
                            <!--<xsl:value-of select="concat(pa/ed/note,' ')"/>-->
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:apply-templates select="pa/ed/mm" mode="full"/>   <!--<p>PAPAPAPAPAPAPAPAPAPAPA</p>-->
                </div>
            </div>
        </xsl:if>
        <!--<hr/>-->
    </xsl:template>


    <xsl:template match="family" mode="spouses">
        <!--<xsl:value-of select="concat('|family spouses| $lang=|',$lang,'| $mode=|',$mode,'|')"/>-->
        <!--<xsl:value-of select="concat(' $peId=|',$peId,'| $paId=|',$paId,'| $feId=|',$feId,'|')"/>-->
        <!--<xsl:value-of select="concat(' $personId=|',$personId,'| $childId=|',$childId,'| $familyId=|',$familyId,'| $app=|',$app,'|')"/>-->
        <xsl:if test="$userIs!='guest'"></xsl:if>
        <div class="container">
            <!--<xsl:apply-templates select="wife/person|husband/person" mode="spouse" />-->
            <span class="column span-1">
                <img>
                    <xsl:attribute name="src">
                        <xsl:value-of select="concat($app,'images/','family_MF.gif')"/>
                    </xsl:attribute>
                    <xsl:attribute name="style">
                        <xsl:value-of select="concat('vertical-align:','bottom',';height:','20px')"/>
                    </xsl:attribute>
                    <!--<xsl:attribute name="title">
                        <xsl:value-of select="concat('','Delete all events and attribs of the person')"/>
                    </xsl:attribute>-->
                    <!--<xsl:attribute name="title">
                        <xsl:call-template name="locstr">
                            <xsl:with-param name="k"></xsl:with-param>
                            <xsl:with-param name="l" select="$lang"/>
                        </xsl:call-template>
                    </xsl:attribute>-->
                </img>
            </span>
            <span class="column span-8">
                <div>
                    <xsl:apply-templates select="//family[@id=$familyId]/husband/person" mode="spouse"/>
                </div>
                <div>
                    <xsl:apply-templates select="//family[@id=$familyId]/wife/person" mode="spouse"/>
                </div>
            </span>
            <!--<span class="column span-8" style="font-size:smaller">-->
                <!--&lt;!&ndash;<xsl:apply-templates select="//family/child/person[@id=$childId]" mode="child"/>&ndash;&gt;-->
                <!--<xsl:apply-templates select="//family[@id=$familyId]/child/person[@id=$childId]" mode="child"/>-->
            <!--</span>-->
        </div>
    </xsl:template>


    <xsl:template match="family" mode="parentsChildren">
        <!--<xsl:value-of select="concat('|family parentsChildren| $lang=|',$lang,'| $mode=|',$mode,'|')"/>-->
        <!--<xsl:value-of select="concat(' $peId=|',$peId,'| $paId=|',$paId,'| $feId=|',$feId,'|')"/>-->
        <!--<xsl:value-of select="concat(' $personId=|',$personId,'| $childId=|',$childId,'| $familyId=|',$familyId,'| $app=|',$app,'|')"/>-->
        <xsl:if test="$userIs!='guest'">
        </xsl:if>
        <div class="container">
            <!--<xsl:apply-templates select="wife/person|husband/person" mode="spouse" />-->
            <span class="column span-1">
                <img>
                    <xsl:attribute name="src">
                        <xsl:value-of select="concat($app,'images/','family_MF.gif')"/>
                    </xsl:attribute>
                    <xsl:attribute name="style">
                        <xsl:value-of select="concat('vertical-align:','bottom',';height:','20px')"/>
                    </xsl:attribute>
                    <!--<xsl:attribute name="title">
                        <xsl:value-of select="concat('','Delete all events and attribs of the person')"/>
                    </xsl:attribute>-->
                    <!--<xsl:attribute name="title">
                        <xsl:call-template name="locstr">
                            <xsl:with-param name="k"></xsl:with-param>
                            <xsl:with-param name="l" select="$lang"/>
                        </xsl:call-template>
                    </xsl:attribute>-->
            </img>
            </span>
            <span class="column span-7">
                <div>
                    <xsl:apply-templates select="husband/person" mode="spouse"/>
                </div>
                <div>
                    <xsl:apply-templates select="wife/person" mode="spouse"/>
                </div>
            </span>
            <span class="column span-8" style="font-size:smaller">
                <xsl:apply-templates select="//family/child/person[@id=$childId]" mode="child"/>
            </span>
        </div>
    </xsl:template>


    <xsl:template match="family" mode="fullInfo">
        <xsl:if test="$userIs!='guest'"></xsl:if>
        <div class="container">
            <div class="span-2">
                <xsl:choose>
                    <xsl:when test="count(child) > 0">
                        &#160;
                    </xsl:when>
                    <xsl:otherwise>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="concat($app,'gedcom/familyDelete/',//person/@id,'/',./@id)"/>
                            </xsl:attribute>
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">delete.person.family</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <img>
                                <xsl:attribute name="src">
                                    <xsl:value-of select="concat($app,'images/','page_delete.gif')"/>
                                </xsl:attribute>
                            </img>
                        </a>
                    </xsl:otherwise>
                </xsl:choose>

            </div>
            <div class="span-20">
                <xsl:apply-templates select="wife/person|husband/person" mode="spouse"/>
                <xsl:value-of select="concat('&#160;',' ')"/>
                <xsl:value-of select="concat('&#160;',' ')"/>
                <span style="font-size:smaller">
                    <xsl:apply-templates select="child/person" mode="child"/>
                </span>
                <xsl:if test="$feId != 0">
                    <br/>
                    <br/>
                </xsl:if>
            </div>
        </div>
        <xsl:apply-templates select="event" mode="FA"/>
    </xsl:template>


    <xsl:template match="person" mode="spouse">
        <!--<xsl:value-of select="concat('|',$personId,'|',../../../../@id,'|',./@id,'|')" />-->
        <!--xsl:value-of select="concat(' ===|',..,'|=== ')"/-->
        <!--<xsl:if test="(./@id != $personId)">-->
        <xsl:if test="$userIs!='guest'"></xsl:if>
        <span>
            <!--<xsl:attribute name="class">
                <xsl:value-of select="concat(gender,'-style')" />
            </xsl:attribute>-->
            <xsl:choose>
                <xsl:when test="$personId!=0 and $childId!=0">
                    <b>
                        <xsl:value-of select="concat(nameGivn,' ',nameSurn,' ')"/>
                        <xsl:apply-templates select="gender"/>
                    </b>
                </xsl:when>
                <xsl:when test="$familyId!=0">
                    <b>
                        <xsl:value-of select="concat(nameGivn,' ',nameSurn,' ')"/>
                        <xsl:apply-templates select="gender"/>
                    </b>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:if test="(./@id != ../../../../@id) or ($personId != ./@id) and ($feId = 0)">
                    <!--<xsl:if test="(./@id != ../../../../@id)">-->
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="concat($app,'rest/personView/',./@id)"/>
                            </xsl:attribute>
                            <span>
                                <b>
                                    <xsl:value-of select="concat(nameGivn,' ',nameSurn,' ')"/>
                                    <xsl:apply-templates select="gender"/>
                                </b>
                            </span>
                        </a>
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
            <!--<xsl:value-of select="concat(' &#160;','&#160; ')"/>-->
        </span>
    </xsl:template>


    <xsl:template match="person" mode="child">
        <!--xsl:value-of select="concat(' ===|',..,'|=== ')"/-->
        <!--<xsl:if test="./@id != ../../../../@id">-->
        <!--<xsl:attribute name="class">
            <xsl:value-of select="concat(gender,'-style')" />
        </xsl:attribute>-->
        <xsl:if test="$userIs!='guest'"></xsl:if>
        <span>
            <xsl:choose>
                <xsl:when test="$personId!=0 and $childId!=0">
                    <b>
                        <xsl:value-of select="concat(nameGivn,' ',nameSurn,' ')"/>
                        <xsl:apply-templates select="gender"/>
                    </b>
                </xsl:when>
                <xsl:otherwise>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="concat($app,'rest/personView/',./@id)"/>
                        </xsl:attribute>
                        <span>
                            <b>
                                <xsl:value-of select="concat(nameGivn,' ',nameSurn,' ')"/>
                                <xsl:apply-templates select="gender"/>
                            </b>
                        </span>
                    </a>
                    <xsl:if test="$feId = 0">
                        <xsl:if test="$userIs!='guest'">
                            <a>
                                <xsl:attribute name="href">
                                    <!--<xsl:value-of select="concat($app,'rest/deleteFe/',fe/@id)"/>-->
                                    <!--<xsl:value-of select="concat($app,'gedcom/familyChildDelete/',//person/@id,'/',./@id)"/>-->
                                    <xsl:value-of select="concat($app,'gedcom/familyChildDelete/',$personId,'/',./@id)"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">remove.child</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <img>
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="concat($app,'images/','page_delete.gif')"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="style">
                                        <xsl:value-of select="concat('vertical-align:','text-bottom')"/>
                                    </xsl:attribute>
                                </img>
                            </a>
                        </xsl:if>
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="concat('&#160;','&#160;','&#160;','&#160;')"/>
        </span>
        <!--</xsl:if>-->
    </xsl:template>


    <xsl:template match="event" mode="FA">
        <!--<xsl:value-of select="concat('|',$peId,'|',$paId,'|',$feId,'^',fe/@id,'^')"/>-->
        <xsl:if test="$userIs!='guest'"></xsl:if>
        <xsl:if test="($peId = 0 and $paId = 0 and $feId = 0) or ($feId = number(fe/@id))">
            <div class="container">
                <xsl:if test="$peId = 0 and $paId = 0 and $feId = 0">
                    <xsl:if test="$userIs!='guest'">
                        <div class="span-2">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($app,'rest/editFe/',fe/@id)"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">edit.family.event</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <img>
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="concat($app,'images/','page_edit.gif')"/>
                                    </xsl:attribute>
                                </img>
                            </a>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($app,'rest/deleteFe/',fe/@id)"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">delete.family.event</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <img>
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="concat($app,'images/','page_delete.gif')"/>
                                    </xsl:attribute>
                                </img>
                            </a>
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="concat($app,'rest/addMultiMedia/FE/',fe/@id)"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:call-template name="locstr">
                                        <xsl:with-param name="k">add.multimedia</xsl:with-param>
                                        <xsl:with-param name="l" select="$lang"/>
                                    </xsl:call-template>
                                </xsl:attribute>
                                <img>
                                    <xsl:attribute name="src">
                                        <xsl:value-of select="concat($app,'images/image_new.gif')"/>
                                    </xsl:attribute>
                                </img>
                            </a>
                        </div>
                    </xsl:if>
                </xsl:if>
                <div class="span-4 colborder">
                    <span class="span-4 colborder ed_tag" title="loc_tag;  Įvykio tipas">
                        <xsl:value-of select="concat('fe',fe/@tag,'_ ')"/>
                    </span>
                    <xsl:if test="string-length(fe/ed/dateValue) > 0">
                        <span class="span-4 colborder ed_dateValue" title="loc_dateVale;  Įvykio data">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.date</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:value-of select="concat(fe/ed/dateValue,' ')"/>
                        </span>
                        <br/>
                    </xsl:if>
                </div>
                <div class="span-15">
                    <xsl:if test="string-length(fe/ed/descriptor) > 0">
                        <span class="span-15 ed_descriptor">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.type</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:variable name="text">
                                <xsl:call-template name="MultiLangText">
                                    <xsl:with-param name="mlt" select="fe/ed/descriptor"/>
                                    <xsl:with-param name="language" select="$lang"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:value-of select="concat($text,' ')"/>
                            <!--<xsl:value-of select="concat(fe/ed/descriptor,'  ')"/>-->
                        </span>
                    </xsl:if>
                    <xsl:if test="string-length(fe/ed/place) > 0">
                        <span class="span-15 ed_place">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.place</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:variable name="text">
                                <xsl:call-template name="MultiLangText">
                                    <xsl:with-param name="mlt" select="fe/ed/place"/>
                                    <xsl:with-param name="language" select="$lang"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:value-of select="concat($text,' ')"/>
                            <!--<xsl:value-of select="concat(fe/ed/place,' ')"/>-->
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="string-length(fe/ed/ageAtEvent) > 0">
                        <span class="span-15 ed_ageAtEvent">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.atAge</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:value-of select="concat(fe/ed/ageAtEvent,' ')"/>
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="string-length(fe/ed/cause) > 0">
                        <span class="span-15 ed_cause">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.cause</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:variable name="text">
                                <xsl:call-template name="MultiLangText">
                                    <xsl:with-param name="mlt" select="fe/ed/cause"/>
                                    <xsl:with-param name="language" select="$lang"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:value-of select="concat($text,' ')"/>
                            <!--<xsl:value-of select="concat(fe/ed/cause,' ')"/>-->
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="string-length(fe/ed/source) > 0">
                        <span class="span-15 ed_source">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.source</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:variable name="text">
                                <xsl:call-template name="MultiLangText">
                                    <xsl:with-param name="mlt" select="fe/ed/source"/>
                                    <xsl:with-param name="language" select="$lang"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:value-of select="concat($text,' ')"/>
                            <!--<xsl:value-of select="concat(fe/ed/source,' ')"/>-->
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="string-length(fe/ed/note) > 0">
                        <span class="span-15 ed_note">
                            <xsl:attribute name="title">
                                <xsl:call-template name="locstr">
                                    <xsl:with-param name="k">event.note</xsl:with-param>
                                    <xsl:with-param name="l" select="$lang"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:variable name="text">
                                <xsl:call-template name="MultiLangText">
                                    <xsl:with-param name="mlt" select="fe/ed/note"/>
                                    <xsl:with-param name="language" select="$lang"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:value-of select="concat($text,' ')"/>
                            <!--<xsl:value-of select="concat(fe/ed/note,' ')"/>-->
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:apply-templates select="fe/ed/mm" mode="full"/> <!--<p>FAFAFAFAFAFAFAFAFAFAFAFA</p>-->
                </div>
            </div>
        </xsl:if>
        <!--<hr/>-->
    </xsl:template>


    <!-- \u2640  ♀ Venus;  \u2642  ♂ Mars  -->
    <xsl:template match="gender">
        <xsl:choose>
            <!--<xsl:when test=".='M'">♂</xsl:when>-->
            <xsl:when test=".='M'">
                <img>
                    <xsl:attribute name="src">
                        <xsl:value-of select="concat($app,'images/gender_M.gif')"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:call-template name="locstr">
                            <xsl:with-param name="k">male</xsl:with-param>
                            <xsl:with-param name="l" select="$lang"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="style">
                        <xsl:value-of select="concat('vertical-align:','text-bottom',';height:','16px')"/>
                    </xsl:attribute>
                </img>
            </xsl:when>
            <!--<xsl:otherwise>♀</xsl:otherwise>-->
            <xsl:otherwise>
                <img>
                    <xsl:attribute name="src">
                        <xsl:value-of select="concat($app,'images/gender_F.gif')"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:call-template name="locstr">
                            <xsl:with-param name="k">female</xsl:with-param>
                            <xsl:with-param name="l" select="$lang"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="style">
                        <xsl:value-of select="concat('vertical-align:','text-bottom',';height:','16px')"/>
                    </xsl:attribute>
                </img>
            </xsl:otherwise>
        </xsl:choose>
        <!--[<lift:loc>add.edit.person</lift:loc>]-->
    </xsl:template>


    <xsl:template match="mm" mode="full">
        <!--<xsl:value-of select="idRoot"/>|-->
        <!--<xsl:value-of select="idRoot='0'"/>-->
        <xsl:if test="idRoot='0'"> <!-- idRoot='0' ==> active record -->
            <span class="span-15">
                <!--<table style="cellspacing:0px; cellpadding:0px; border-collapse:collapse; width:100%"><tr>-->
                <table style="border:0px solid black; padding:0px; border-collapse:collapse; width:100%"><tr>
                <xsl:if test="$userIs!='guest'">
                    <td style="border:0px solid black; width:10%">
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="concat($app,'rest/editMultiMedia/',@id)"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:call-template name="locstr">
                                <xsl:with-param name="k">edit.multimedia</xsl:with-param>
                                <xsl:with-param name="l" select="$lang"/>
                            </xsl:call-template>
                        </xsl:attribute>
                        <img>
                            <xsl:attribute name="src">
                                <xsl:value-of select="concat($app,'images/','image_edit.gif')"/>
                            </xsl:attribute>
                        </img>
                    </a>
                    <a>
                        <xsl:attribute name="href">
                            <xsl:value-of select="concat($app,'rest/deleteMultiMedia/',@id)"/>
                        </xsl:attribute>
                        <xsl:attribute name="title">
                            <xsl:call-template name="locstr">
                                <xsl:with-param name="k">delete.multimedia</xsl:with-param>
                                <xsl:with-param name="l" select="$lang"/>
                            </xsl:call-template>
                        </xsl:attribute>
                        <img>
                            <xsl:attribute name="src">
                                <xsl:value-of select="concat($app,'images/','image_delete.gif')"/>
                            </xsl:attribute>
                        </img>
                    </a>
                    </td>
                </xsl:if>
               <td style="border:1px groove #cff; width:1%">
               <img class="expando" height="75px">
                    <xsl:attribute name="src">
                        <xsl:value-of select="concat($app,'images/',@id)"/>
                    </xsl:attribute>
                </img>
               </td>
                <td> <!--<td style="border:0px groove black;">-->
                <xsl:call-template name="MultiLangText">
                    <xsl:with-param name="mlt" select="title"/>
                    <xsl:with-param name="language" select="$lang"/>
                </xsl:call-template>
                </td>
                </tr></table>
            </span>
        </xsl:if>
    </xsl:template>


    <!--<xsl:template match="mm" mode="mmonly">
        <span class="span-2">
            ||<xsl:value-of select="concat($app,'images/',$mmId)"/>||
            &lt;!&ndash;||<xsl:value-of select="concat($app,'images/',/person//mm[@id=$mmId])"/>||&ndash;&gt;
            <img width="50px" height="50px">
                <xsl:attribute name="src">
                    <xsl:value-of select="concat($app,'images/',/person//mm[@id=$mmId])"/>
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:call-template name="MultiLangText">
                        <xsl:with-param name="mlt" select="title"/>
                        <xsl:with-param name="language" select="$lang"/>
                    </xsl:call-template>
                </xsl:attribute>
            </img>
        </span>
    </xsl:template>-->


    <xsl:template name="mm4del">
        <table style="background:#FFC"><tr><td>
        <span class="span-4">
            <img width="50px" height="50px">
                <xsl:attribute name="src">
                    <xsl:value-of select="concat($app,'images/',$mmId)"/>
                </xsl:attribute>
                <xsl:attribute name="title">
                    <xsl:value-of select="concat('',/person//mm[@id=$mmId]/title//text())"/>
                    <xsl:call-template name="MultiLangText">
                        <xsl:with-param name="mlt" select="title"/>
                        <xsl:with-param name="language" select="$lang"/>
                    </xsl:call-template>
                </xsl:attribute>
            </img>
            <br/>
           <!--||<xsl:value-of select="concat('',/person//mm[@id=$mmId]/title/_/lt/text())"/>||-->
           <xsl:value-of select="concat('',/person//mm[@id=$mmId]/title//text())"/>
           <!--||<xsl:value-of select="concat('',/person//mm[@id=$mmId]/title)"/>||-->
           <br/>
        </span>
        </td></tr></table>
    </xsl:template>


    <xsl:template name="MultiLangText">
        <xsl:param name="language" select="$lang"/>
        <xsl:param name="mlt"></xsl:param>
        <!--<xsl:variable name="dfltLang" select="$mlt/_/@d" />-->
        <!--<xsl:value-of select="concat('-|',$dfltLang,'|-')" />-->
        <!--<xsl:value-of select="concat('_|',$mlt/_/@d,'|_')" />-->
        <xsl:choose>
            <xsl:when test="$language='lt' and $mlt/_/lt">
                <xsl:value-of select="$mlt/_/lt"/>
            </xsl:when>
            <xsl:when test="$language='en' and $mlt/_/en">
                <xsl:value-of select="$mlt/_/en"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="$mlt/_/*">
                    <xsl:if test="name()=$mlt/_/@d">
                        <xsl:value-of select="."/>
                    </xsl:if>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--<xsl:if test="$userIs!='guest'">
    </xsl:if>-->
    <!--  <xsl:apply-templates select="pe/ed/mm" mode="full"/>  -->
</xsl:stylesheet>