<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:security="urn:jboss:domain:security:1.2">
    <xsl:output method="xml" indent="yes"/>

    <!-- New security-domain definition -->
    <xsl:variable name="newDigestSecurityDomainDefinition">
       <security:security-domain name="digest_auth" cache-type="infinispan">
          <security:authentication>
             <security:login-module code="UsersRoles" flag="required">
                <security:module-option name="hashAlgorithm" value="MD5"/>
                <security:module-option name="hashEncoding" value="rfc2617"/>
                <security:module-option name="hashUserPassword" value="false"/>
                <security:module-option name="hashStorePassword" value="true"/>
                <security:module-option name="passwordIsA1Hash" value="true" />
                <security:module-option name="storeDigestCallback" value="org.jboss.security.auth.callback.RFC2617Digest"/>
                <security:module-option name="usersProperties">
                   <xsl:attribute name="value">
                      <xsl:text disable-output-escaping="no">${jboss.server.config.dir}/application-users.properties</xsl:text>
                   </xsl:attribute>
                </security:module-option>
                <security:module-option name="rolesProperties">
                   <xsl:attribute name="value">
                      <xsl:text disable-output-escaping="no">${jboss.server.config.dir}/application-roles.properties</xsl:text>
                   </xsl:attribute>
                </security:module-option>
             </security:login-module>
          </security:authentication>
       </security:security-domain>
    </xsl:variable>
    
    <!-- Add another security domain -->
    <xsl:template match="//security:subsystem/security:security-domains/security:security-domain[position()=last()]">
       <xsl:copy>
           <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
       <xsl:copy-of select="$newDigestSecurityDomainDefinition"/>
    </xsl:template>
    
    <!-- Copy everything else. -->
    <xsl:template match="@*|node()">
       <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
       </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

