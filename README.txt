Pong game - Java Swing frontend + Spring Boot backend + MySQL

ARCHITECTURE
  Game (Swing)  --HTTP/JSON-->  Spring Boot REST API  --JPA-->  MySQL (pong_db.matches)

SETUP (once)
1. Install JDK 17+ and MySQL (MySQL server must be running).
2. Open backend/src/main/resources/application.properties and set
   spring.datasource.password to your MySQL password.
   (The database pong_db and the matches table are created automatically.
    backend/schema.sql is optional and does the same thing manually.)

RUN - you need TWO terminals.

Terminal 1 - start the backend (keep it running), from the backend folder:
  Windows (PowerShell):  cd backend
                         .\mvnw.cmd spring-boot:run
  Mac/Linux:             cd backend
                         ./mvnw spring-boot:run
  (First run downloads Maven + dependencies, so it needs internet and takes a minute.
   Wait for "Started PongApplication". It listens on http://localhost:8080)

Terminal 2 - start the game, from the project root folder (the one with src/ and backend/):
  javac src/*.java -d out
  java -cp out Main

API
  POST http://localhost:8080/api/matches   body: {"player1Score":5,"player2Score":3,"winner":"Player 1"}
  GET  http://localhost:8080/api/matches   returns the last 10 matches (newest first)

FILES
  src/Main.java        menus (main menu, mode select) and game loop
  src/Draw.java        game logic, drawing, pause menu
  src/ApiClient.java   sends/reads matches to the backend over HTTP
  backend/             Spring Boot project
    pom.xml                                   dependencies (web, data-jpa, mysql)
    src/main/resources/application.properties DB connection settings
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
