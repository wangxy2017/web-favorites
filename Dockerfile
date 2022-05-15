FROM openjdk:17-jdk-alpine
ADD ./target/web-favorites.jar web-favorites.jar
EXPOSE 9020
EXPOSE 8888
ENTRYPOINT [ "sh", "-c", "java -Xms512m -Xmx512m -Xmn256m -Duser.timezone=Asia/Shanghai -jar web-favorites.jar" ]