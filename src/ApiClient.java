import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/api/matches";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    public static void saveMatch(int p1, int p2, String winner) {
        String json = "{\"player1Score\":" + p1 + ",\"player2Score\":" + p2
                + ",\"winner\":\"" + winner.replace("\"", "") + "\"}";
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL))
                    .timeout(Duration.ofSeconds(3))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.err.println("Could not save match (is the backend running?): " + e.getMessage());
        }
    }

    public static String getHistory() {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(BASE_URL))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                return "Could not load history: server returned " + res.statusCode();
            }
            StringBuilder sb = new StringBuilder();
            Matcher m = Pattern.compile("\\{[^{}]*\\}").matcher(res.body());
            while (m.find()) {
                String o = m.group();
                String when = field(o, "playedAt").replace('T', ' ');
                if (when.length() > 19) when = when.substring(0, 19);
                sb.append("P1 ").append(field(o, "player1Score")).append(" - ")
                  .append(field(o, "player2Score")).append(" P2  |  Winner: ")
                  .append(field(o, "winner")).append("  |  ").append(when).append("\n");
            }
            return sb.length() == 0 ? "No matches yet." : sb.toString();
        } catch (Exception e) {
            return "Could not load history. Start the Spring Boot backend first.\n(" + e.getMessage() + ")";
        }
    }

    private static String field(String obj, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"?([^\",}]*)\"?").matcher(obj);
        return m.find() ? m.group(1) : "";
    }
}
