<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:logging="urn:jboss:domain:logging:1.2"
                xmlns:p="urn:jboss:domain:1.4"
                xmlns:jgroups="urn:jboss:domain:jgroups:1.2"
                xmlns:core="urn:infinispan:server:core:5.3"
                xmlns:endpoint="urn:infinispan:server:endpoint:6.0"
                >
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

    <!-- Parameter declarations with defaults set -->
    <xsl:param name="modifyInfinispan">false</xsl:param>
    <xsl:param name="modifyRelay">false</xsl:param>
    <xsl:param name="modifyMulticastAddress">false</xsl:param>
    <xsl:param name="modifyRemoteDestination">false</xsl:param>
    <xsl:param name="modifyOutboundSocketBindingHotRod">false</xsl:param>
    <xsl:param name="removeRestSecurity">true</xsl:param>
    <xsl:param name="infinispanServerEndpoint">false</xsl:param>
    <xsl:param name="infinispanFile">none</xsl:param>
    <xsl:param name="log.level.infinispan">INFO</xsl:param>
    <xsl:param name="log.level.jgroups">INFO</xsl:param>
    <xsl:param name="log.level.console">INFO</xsl:param>

    <xsl:template match="core:subsystem">
        <xsl:if test="$modifyInfinispan = 'false'">
            <xsl:copy>
                <!-- also copy all subsystem attributes -->
                <xsl:for-each select="@*">
                    <xsl:attribute name="{name(.)}">
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </xsl:for-each>
                <xsl:apply-templates/>
            </xsl:copy>
        </xsl:if>
        <xsl:if test="$modifyInfinispan != 'false'">
            <xsl:copy-of select="document($modifyInfinispan)"/>
        </xsl:if>
    </xsl:template>

    <!-- configure subsystem/logger[@category = 'org.infinispan']/level
    and subsystem/logger[@category = 'org.jgroups']/level -->
    <xsl:template match="logging:subsystem">
        <xsl:copy>
            <xsl:for-each select="@*">
                <xsl:attribute name="{name(.)}">
                    <xsl:value-of select="."/>
                </xsl:attribute>
            </xsl:for-each>

            <xsl:apply-templates select="*[not(self::logging:logger)]"/>

            <xsl:apply-templates select="*[self::logging:logger]"/>

            <xsl:element name="logging:logger">
                <xsl:attribute name="category">org.infinispan</xsl:attribute>
                <xsl:element name="level">
                    <xsl:attribute name="name">
                        <xsl:value-of select="$log.level.infinispan"/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:element>

            <xsl:element name="logging:logger" >
                <xsl:attribute name="category">org.jgroups</xsl:attribute>
                <xsl:element name="level">
                    <xsl:attribute name="name">
                        <xsl:value-of select="$log.level.jgroups"/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:element>
        </xsl:copy>
    </xsl:template>

    <!-- configure subsystem/console-handler[@name = 'CONSOLE']/level -->
    <xsl:template match="logging:subsystem/logging:console-handler[@name = 'CONSOLE']">
        <xsl:copy>
            <!-- copy also all attributes -->
            <xsl:for-each select="@*">
                <xsl:attribute name="{name(.)}">
                    <xsl:value-of select="."/>
                </xsl:attribute>
            </xsl:for-each>

            <xsl:apply-templates/>

            <xsl:element name="level">
                <xsl:attribute name="name">
                    <xsl:value-of select="$log.level.console"/>
                </xsl:attribute>
            </xsl:element>
        </xsl:copy>
    </xsl:template>

    <!-- configure subsystem/periodic-rotating-file-handler[@name = 'FILE']/level -->
    <xsl:template match="logging:subsystem/logging:periodic-rotating-file-handler[@name = 'FILE']">
        <xsl:copy>
            <!-- copy also all attributes -->
            <xsl:for-each select="@*">
                <xsl:attribute name="{name(.)}">
                    <xsl:value-of select="."/>
                </xsl:attribute>
            </xsl:for-each>

            <xsl:apply-templates/>

            <xsl:element name="level">
                <xsl:attribute name="name">
                    <xsl:choose>
                        <xsl:when test="$log.level.infinispan != 'INFO' and $log.level.jgroups = 'INFO'">
                            <xsl:value-of select="$log.level.infinispan"/>
                        </xsl:when>
                        <xsl:when test="$log.level.infinispan = 'INFO' and $log.level.jgroups != 'INFO'">
                            <xsl:value-of select="$log.level.jgroups"/>
                        </xsl:when>
                        <xsl:otherwise>TRACE</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:element>
        </xsl:copy>
    </xsl:template>
    <!-- delete existing configurations for these loggers -->
    <xsl:template match="logging:subsystem/logging:logger[@category = 'org.infinispan']"></xsl:template>
    <xsl:template match="logging:subsystem/logging:logger[@category = 'org.jgroups']"></xsl:template>
    <xsl:template match="logging:subsystem/logging:console-handler[@name = 'CONSOLE']/logging:level"></xsl:template>
    <xsl:template match="logging:subsystem/logging:periodic-rotating-file-handler[@name = 'FILE']/logging:level"></xsl:template>

    <xsl:template match="jgroups:relay">
        <xsl:if test="$modifyRelay = 'false'">
            <xsl:copy>
                <!-- also copy all relay attributes -->
                <xsl:for-each select="@*">
                    <xsl:attribute name="{name(.)}">
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </xsl:for-each>
                <xsl:apply-templates/>
            </xsl:copy>
        </xsl:if>
        <xsl:if test="$modifyRelay != 'false'">
            <xsl:copy-of select="document($modifyRelay)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="p:socket-binding[@name='jgroups-udp']">
        <xsl:if test="$modifyMulticastAddress = 'false'">
            <xsl:copy>
                <!-- also copy all attributes -->
                <xsl:for-each select="@*">
                    <xsl:attribute name="{name(.)}">
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </xsl:for-each>
                <xsl:apply-templates/>
            </xsl:copy>
        </xsl:if>
        <xsl:if test="$modifyMulticastAddress != 'false'">
            <xsl:copy-of select="document($modifyMulticastAddress)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="p:remote-destination[@host='remote-host']">
        <xsl:if test="$modifyRemoteDestination = 'false'">
            <xsl:copy>
                <!-- also copy all attributes -->
                <xsl:for-each select="@*">
                    <xsl:attribute name="{name(.)}">
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </xsl:for-each>
                <xsl:apply-templates/>
            </xsl:copy>
        </xsl:if>
        <xsl:if test="$modifyRemoteDestination != 'false'">
            <xsl:copy-of select="document($modifyRemoteDestination)"/>
        </xsl:if>
    </xsl:template>

    <!-- outbound-socket-binding remote-store-hotrod-server -->
    <!--<xsl:template match="p:remote-destination[@host='remote-host']">-->
    <xsl:template match="p:outbound-socket-binding[@name='remote-store-hotrod-server']">
        <xsl:if test="$modifyOutboundSocketBindingHotRod = 'false'">
            <xsl:copy>
                <!-- also copy all attributes -->
                <xsl:for-each select="@*">
                    <xsl:attribute name="{name(.)}">
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </xsl:for-each>
                <xsl:apply-templates/>
            </xsl:copy>
        </xsl:if>
        <xsl:if test="$modifyOutboundSocketBindingHotRod != 'false'">
            <xsl:copy-of select="document($modifyOutboundSocketBindingHotRod)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="endpoint:subsystem">
        <xsl:if test="$infinispanServerEndpoint = 'false'">
            <xsl:copy>
                <!-- also copy all subsystem attributes -->
                <xsl:for-each select="@*">
                    <xsl:attribute name="{name(.)}">
                        <xsl:value-of select="."/>
                    </xsl:attribute>
                </xsl:for-each>
                <xsl:apply-templates/>
            </xsl:copy>
        </xsl:if>
        <xsl:if test="$infinispanServerEndpoint != 'false'">
            <xsl:copy-of select="document($infinispanServerEndpoint)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="endpoint:subsystem/endpoint:rest-connector">
        <xsl:if test="$removeRestSecurity != 'true'">
            <xsl:copy>
                <xsl:copy-of select="@*"/>
                <xsl:apply-templates/>
            </xsl:copy>
        </xsl:if>
        <xsl:if test="$removeRestSecurity = 'true'">
            <xsl:copy>
                <xsl:copy-of select="@*[not(name() = 'security-domain' or name() = 'auth-method')]"/>
                <xsl:apply-templates/>
            </xsl:copy>
        </xsl:if>
    </xsl:template>

    <!-- matches on the remaining tags and recursively applies templates to their children and copies them to the result  -->
    <xsl:template match="*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>


</xsl:stylesheet>