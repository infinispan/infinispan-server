<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:web="urn:jboss:domain:web:1.1"
                xmlns:security="urn:jboss:domain:security:1.2">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

    <!-- New security-domain definition -->
    <xsl:variable name="newClientCertSecurityDomainDefinition">
       <security:security-domain name="client_cert_auth" cache-type="infinispan">
          <security:authentication>
             <security:login-module code="CertificateRoles" flag="required">
                <security:module-option name="securityDomain" value="client_cert_auth"/>
                <security:module-option name="rolesProperties">
                  <xsl:attribute name="value">
                     <xsl:text disable-output-escaping="no">${jboss.server.config.dir}/roles.properties</xsl:text>
                  </xsl:attribute>
                </security:module-option>
             </security:login-module>
          </security:authentication>
          <security:jsse truststore-password="changeit" client-auth="true">
             <xsl:attribute name="truststore-url">
                <xsl:text disable-output-escaping="no">${jboss.server.config.dir}/jsse.keystore</xsl:text>
             </xsl:attribute>
          </security:jsse>
       </security:security-domain>
    </xsl:variable>
    
    <!-- New connector definition -->
    <xsl:variable name="newHttpsConnector">
       <web:connector name="https" protocol="HTTP/1.1" scheme="https" socket-binding="https" enable-lookups="false" secure="true">
          <web:ssl name="myssl" 
               keystore-type="JKS"
               password="changeit"
               key-alias="test"
               truststore-type="JKS"
               verify-client="want">
               <xsl:attribute name="certificate-key-file">
                  <xsl:text disable-output-escaping="no">${jboss.server.config.dir}/server.keystore</xsl:text>
               </xsl:attribute>
               <xsl:attribute name="ca-certificate-file">
                  <xsl:text disable-output-escaping="no">${jboss.server.config.dir}/server.keystore</xsl:text>
               </xsl:attribute>
          </web:ssl>
       </web:connector>
    </xsl:variable>
    
    <!-- Add another security domain -->
    <xsl:template match="//security:subsystem/security:security-domains/security:security-domain[position()=last()]">
       <xsl:copy>
           <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
       <xsl:copy-of select="$newClientCertSecurityDomainDefinition"/>
    </xsl:template>
    
    <!-- Add another connector -->
    <xsl:template match="//web:subsystem/web:connector[position()=last()]">
       <xsl:copy>
           <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
       <xsl:copy-of select="$newHttpsConnector"/>
    </xsl:template>

    <!-- Copy everything else. -->
    <xsl:template match="@*|node()">
       <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

