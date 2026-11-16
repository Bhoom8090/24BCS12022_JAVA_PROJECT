Pong game - Java Swing frontend + Spring Boot backend + H2 (default) or MySQL (optional)

ARCHITECTURE
  Game (Swing)  --HTTP/JSON-->  Spring Boot REST API  --JPA-->  H2 file DB (default, local)  or  MySQL (optional)

SETUP (once)
1. Install JDK 17+. That's it - the backend uses an embedded H2 database by default,
   so no database server or config edits are needed. Matches are stored locally in
   backend/data/pongdb.mv.db (created automatically; git-ignored).
   An H2 console is available at http://localhost:8081/h2-console
   (JDBC URL: jdbc:h2:file:./data/pongdb, user: sa, empty password).
2. Optional - MySQL instead of H2:
   - Install MySQL and set your password in backend/src/main/resources/application-mysql.properties
     (or via MYSQL_URL / MYSQL_USER / MYSQL_PASSWORD environment variables).
   - Start the backend with the mysql profile (see RUN below).
   - The database pong_db and the matches table are created automatically.
     backend/schema.sql is optional and does the same thing manually (MySQL only).

RUN - you need TWO terminals.

Terminal 1 - start the backend (keep it running), from the backend folder:
  Windows (PowerShell):  cd backend
                         .\mvnw.cmd spring-boot:run
  Mac/Linux:             cd backend
                         ./mvnw spring-boot:run
  (First run downloads Maven + dependencies, so it needs internet and takes a minute.
   Wait for "Started PongApplication". It listens on http://localhost:8081)
   To use MySQL instead of H2, add the mysql profile:
     Windows (PowerShell):  .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
     Mac/Linux:             ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql

Terminal 2 - start the game, from the project root folder (the one with src/ and backend/):
  javac src/*.java -d out
  java -cp out Main

API
  POST http://localhost:8081/api/matches   body: {"player1Score":5,"player2Score":3,"winner":"Player 1"}
  GET  http://localhost:8081/api/matches   returns the last 10 matches (newest first)

FILES
  src/Main.java        menus (main menu, mode select) and game loop
  src/Draw.java        game logic, drawing, pause menu
  src/ApiClient.java   sends/reads matches to the backend over HTTP
  backend/             Spring Boot project
    pom.xml                                   dependencies (web, data-jpa, h2 + mysql)
    src/main/resources/application.properties        default DB (embedded H2, local file)
    src/main/resources/application-mysql.properties  optional MySQL settings
    src/main/java/com/pong/
      PongApplication.java   starts the app
      Match.java             entity -> table "matches"
      MatchRepository.java   Spring Data JPA repository
      MatchController.java   REST endpoints
      MatchRequest.java      request body for POST

GAME
Start -> choose Normal (first to 5) or Time (10 min, higher score wins).
Controls: P1 = W/S, P2 = Up/Down.
ESC during a game = pause menu (Resume / Quit). Pick with W/S or Up/Down + ENTER, or the mouse.
After a match: ENTER = play again, ESC = main menu.
Finished matches are saved through the backend. The History button reads them back.
The game still runs without the backend, but nothing is saved or shown in History.
