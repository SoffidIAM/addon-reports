<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:zul="http://www.zkoss.org/2005/zul">

	<xsl:template match="/zul:zk/zul:zscript" priority="3">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
		</xsl:copy>
		<zul:zscript>
			boolean canViewReports = es.caib.seycon.ng.utils.Security.isUserInRole("seu:report:show/*");
		</zul:zscript>
	</xsl:template>
	
	<xsl:template match="zul:tree/zul:treechildren/zul:treeitem[1]/zul:treechildren" priority="3">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
			<zul:treeitem>
				<zul:treerow>
					<zul:apptreecell langlabel="report.menu" 
								pagina="addon/report/report.zul">
							<xsl:attribute name="if">${canViewReports}</xsl:attribute>
					</zul:apptreecell>
				</zul:treerow>
			</zul:treeitem>
		</xsl:copy>
			
	</xsl:template>
 

	<xsl:template match="node()|@*" priority="2">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>