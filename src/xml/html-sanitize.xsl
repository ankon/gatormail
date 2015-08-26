<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="html" omit-xml-declaration="no" indent="yes"
		encoding="iso-8859-1"
		doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
		doctype-system="http://www.w3.org/TR/html4/loose.dtd"/>
<!--
		doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
-->
<!--
	The following is a list of ELEMENT tags to filter because of the potential to screw up document encoding or for some other reason.
-->

	<xsl:template match="META">
		<DISABLED-META>
		<xsl:apply-templates select="* | @* | text()"/>
		</DISABLED-META>
	</xsl:template>

	<xsl:template match="BODY">
		<DISABLED-BODY>
		<xsl:apply-templates select="* | @* | text()"/>
		</DISABLED-BODY>
	</xsl:template>

	<xsl:template match="SCRIPT">
		<DISABLED-SCRIPT>
		<xsl:comment>
		<xsl:text>
The contents of this script have been filtered by GatorMail.
</xsl:text>
		</xsl:comment>
<!--		<xsl:apply-templates select="* | @* | text()"/>-->
		</DISABLED-SCRIPT>
	</xsl:template>

<!--
	The following is a list of attribute tags to filter because of the potential to scripting that might intercept the GL Cookie.
-->
	<xsl:template match="@*[starts-with(name(.), 'on')]">
		<xsl:attribute name="{concat('disabled-', name(.))}">
		<xsl:value-of select="."/>
		</xsl:attribute>
	</xsl:template>

<!--
	The following is a special attribute tag, because it contains an link that might contain scripting.
-->
	<xsl:template match="@href[contains(translate( ., 'SCRIPT', 'script'), 'script:')]">
		<xsl:attribute name="{name(.)}">
		<xsl:value-of select="concat('disabled-', translate(., ' ', ''))"/>
		</xsl:attribute>
	</xsl:template>

<!--
	The following is a default catch and pass through all ELEMENTS, attributes, and text.
-->
	<xsl:template match="/ | * | @*">
		<xsl:copy>
		<xsl:apply-templates select="* | @* | text()" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
