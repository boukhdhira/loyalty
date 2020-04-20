# The build stage docker image: Optionally a name can be given to a new build stage by adding AS
# The tag or digest values are optional. If you omit either of them, the builder assumes a latest tag by default
FROM maven:3.6.0-jdk-8 as maven

# copy the project files
COPY ./pom.xml ./pom.xml

# build all dependencies
RUN mvn dependency:go-offline -B

# copy your other files
COPY ./src ./src

# build for release
RUN mvn package -DskipTests

# each FROM instruction clears any state created by previous instructions
# The final base docker image
FROM openjdk:8-jre-alpine

# set deployment directory
WORKDIR /loyalty-project

# copy over the built artifact from the maven image
COPY --from=maven target/loyalty-0.0.1.jar ./

# user 8088 port
EXPOSE 8088
# set the startup command to run the binary
CMD ["java", "-jar", "./loyalty-0.0.1.jar"]