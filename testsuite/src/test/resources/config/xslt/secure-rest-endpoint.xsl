<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:endpoint="urn:infinispan:server:endpoint:6.0">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

    <xsl:param name="security.domain" select="'other'"/>
    <xsl:param name="security.mode" select="'WRITE'"/>
    <xsl:param name="auth.method" select="'BASIC'"/>
    <xsl:param name="cache.container" select="'${connector.cache.container}'"/>

    <!-- New rest-connector definition -->
    <xsl:variable name="newRESTEndpointDefinition">
       <rest-connector virtual-server="default-host">
          <xsl:attribute name="cache-container">
             <xsl:value-of select="$cache.container" />
          </xsl:attribute>
          <xsl:attribute name="security-domain">
             <xsl:value-of select="$security.domain" />
          </xsl:attribute>
          <xsl:attribute name="auth-method">
             <xsl:value-of select="$auth.method" />
          </xsl:attribute>
          <xsl:attribute name="security-mode">
             <xsl:value-of select="$security.mode" />
          </xsl:attribute>
       </rest-connector>
    </xsl:variable>
    
    <!-- Replace rest-connector element with new one - secured -->
    <xsl:template match="//endpoint:subsystem/endpoint:rest-connector">
       <xsl:copy-of select="$newRESTEndpointDefinition"/>
    </xsl:template>

    <xsl:template match="//endpoint:subsystem/endpoint:hotrod-connector/@cache-container">
        <xsl:attribute name="cache-container">
            <xsl:value-of select="$cache.container"/>
        </xsl:attribute>
    </xsl:template>

    <xsl:template match="//endpoint:subsystem/endpoint:memcached-connector/@cache-container">
        <xsl:attribute name="cache-container">
            <xsl:value-of select="$cache.container"/>
        </xsl:attribute>
    </xsl:template>

    <xsl:template match="//endpoint:subsystem/endpoint:websocket-connector/@cache-container">
        <xsl:attribute name="cache-container">
            <xsl:value-of select="$cache.container"/>
        </xsl:attribute>
    </xsl:template>

    <!-- Copy everything else. -->
    <xsl:template match="@*|node()">
       <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

