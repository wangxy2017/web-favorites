FROM openjdk:17-jdk-alpine
ADD ./target/web-favorites.jar web-favorites.jar
EXPOSE 8888
EXPOSE 8889
ENTRYPOINT [ "sh", "-c", "java -Xms512m -Xmx512m -Xmn256m -Duser.timezone=Asia/Shanghai -jar web-favorites.jar" ]