Build:
  * svn checkout http://adobe-assignment.googlecode.com/svn/trunk/
  * mvn clean install `-DskipTests`

File server:
  * cu extensie connection keep-alive
  * java -jar file-web-server/target/file-web-server.jar -r `[path\to\some\folder]` -ka -p `[port]` -w `[pool_size]`
  * ka - pt connection keep alive
  * Eg: java -jar file-web-server/target/file-web-server.jar -ka -r D:\work -p 9999 -w 20

Aplicatie web:
  * java -jar web-app/target/web-app.jar
  * test in browser: http://localhost:12345/
  * html+js - web-app/src/main/resources/iframe-drag-drop.html

https://code.google.com/p/adobe-assignment/source/browse/#svn%2Ftrunk