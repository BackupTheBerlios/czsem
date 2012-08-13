<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE rdf:RDF [
    <!ENTITY owl "http://www.w3.org/2002/07/owl#" >
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#" >
    <!ENTITY owl2xml "http://www.w3.org/2006/12/owl2-xml#" >
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#" >
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#" >
    <!ENTITY pml "http://ufal.mff.cuni.cz/pdt/pml/" >
]>

<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:pml="http://ufal.mff.cuni.cz/pdt/pml/"		
	version="1.0">
	
<xsl:param name="ontology_uri_param"/>

<xsl:variable name="ontology_uri">
	<xsl:choose>
		<xsl:when test="$ontology_uri_param">
			<xsl:value-of select="$ontology_uri_param"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="/node()/@ontology_uri"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:variable>

<xsl:template match="/">
	
	<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	         xmlns:pml="http://ufal.mff.cuni.cz/pdt/pml/">
	    
	    <Ontology xmlns="http://www.w3.org/2002/07/owl#" rdf:about="{$ontology_uri}"/>
		
		<xsl:apply-templates select="//pml:children[@id]|//pml:LM[@id]|//pml:trees/node()[@id]"/>         
	</rdf:RDF>
</xsl:template>

<xsl:template match="pml:children[@id]">
	<xsl:call-template name="create_node">
		<xsl:with-param name="parent_id" select="../@id"/>
	</xsl:call-template>	
</xsl:template>

<xsl:template match="pml:LM[@id]|pml:trees/node()[@id]">
	<xsl:call-template name="create_node">
		<xsl:with-param name="parent_id" select="../../@id"/>
	</xsl:call-template>	
</xsl:template>

<xsl:template name="copy_attribute">
	<xsl:variable name="attr_text" select="normalize-space(text())"/>	
	<xsl:if test="$attr_text">
		<xsl:element name="pml:{name(self::node())}"><xsl:value-of select="$attr_text"/></xsl:element>
<!-- 	<xsl:copy><xsl:value-of select="$attr_text"/></xsl:copy>  -->
	</xsl:if>	
</xsl:template>

<xsl:template name="copy_refernce">
	<xsl:param name="node_name" select="name(self::node())"/>	
	<xsl:variable name="attr_text" select="normalize-space(text())"/>
	<xsl:choose>
		<xsl:when test="$attr_text">
			<xsl:element name="pml:{$node_name}">
				<xsl:call-template name="paste_node_URI_attr">
					<xsl:with-param name="node_id"><xsl:value-of select="$attr_text"/></xsl:with-param>
					<xsl:with-param name="attr_name">rdf:resource</xsl:with-param>
				</xsl:call-template>				
			</xsl:element>
		</xsl:when>
		<xsl:otherwise>
			<xsl:for-each select="pml:LM">
				<xsl:call-template name="copy_refernce">
					<xsl:with-param name="node_name">
						<xsl:value-of select="$node_name"/>
					</xsl:with-param>
				</xsl:call-template>
			</xsl:for-each>
		</xsl:otherwise>
	</xsl:choose>		
</xsl:template>

<xsl:template name="paste_value_of_node_URI">
	<xsl:param name="node_id" select="@id"/>
	<xsl:value-of select="concat($ontology_uri,'#node_', $node_id)"/>
</xsl:template>

<xsl:template name="paste_node_URI_attr">
	<xsl:param name="node_id" select="default_id"/>
	<xsl:param name="attr_name" select="default_attr"/>
		<xsl:attribute name="{$attr_name}">
			<xsl:call-template name="paste_value_of_node_URI">
				<xsl:with-param name="node_id"><xsl:value-of select="$node_id"/></xsl:with-param>
			</xsl:call-template>
		</xsl:attribute>
</xsl:template>


<xsl:template name="create_node">
	<xsl:param name="ch_id" select="@id"/>
	<xsl:param name="parent_id" select="default_id"/>

	<pml:Node>
			<xsl:call-template name="paste_node_URI_attr">
				<xsl:with-param name="node_id"><xsl:value-of select="$ch_id"/></xsl:with-param>
				<xsl:with-param name="attr_name">rdf:about</xsl:with-param>
			</xsl:call-template>
	
			<xsl:if test="$parent_id">
				<pml:hasParent>
					<xsl:call-template name="paste_node_URI_attr">
						<xsl:with-param name="node_id"><xsl:value-of select="$parent_id"/></xsl:with-param>
						<xsl:with-param name="attr_name">rdf:resource</xsl:with-param>
					</xsl:call-template>
				</pml:hasParent>
			</xsl:if>
			
			<xsl:for-each select="child::node()|pml:m/node()|pml:a/node()|pml:gram/node()">
			<!-- 
				<xsl:variable name="thisnode" select="self::node()"/>	
			-->					
				<xsl:variable name="thisname" select="name(self::node())"/>
				<xsl:choose>
					<xsl:when test="substring-after($thisname, '.') = 'rf'">
						<xsl:call-template name="copy_refernce"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="copy_attribute"/>
					</xsl:otherwise>
				</xsl:choose>				
			</xsl:for-each>								
	</pml:Node>
</xsl:template>
</xsl:stylesheet>