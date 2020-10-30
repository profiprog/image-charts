FROM maven:3.6.3-openjdk-8 as build
WORKDIR /workdir
COPY pom.xml .
RUN mvn dependency:resolve
COPY src src
RUN mvn package

FROM tomcat:8.5.59
RUN rm -fr webapps \
    && mkdir webapps \
    && echo "tomcat.util.http.parser.HttpParser.requestTargetAllow=|" \
        >> conf/catalina.properties
COPY --from=build /workdir/target/charts-api.war webapps/ROOT.war
RUN mkdir webapps/ROOT && cd webapps/ROOT && unzip ../ROOT.war
