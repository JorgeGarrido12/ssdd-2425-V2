FROM tomcat:10.1-jdk17

# Cambia "ServiceExterno.war" si usas otro <finalName> en el pom.xml
COPY target/ServiceExterno.war /usr/local/tomcat/webapps/ServiceExterno.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
