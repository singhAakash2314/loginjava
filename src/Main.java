import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port 8000");
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String path = t.getRequestURI().getPath();
            if (path.equals("/") || path.equals("/index.html")) {
                sendFileResponse(t, "src/index.html"); // Adjusted file path
            } else if (path.equals("/login") && t.getRequestMethod().equals("POST")) {
                handleLogin(t);
            } else {
                sendResponse(t, "Not Found", 404);
            }
        }

        private void handleLogin(HttpExchange t) throws IOException {
            // Add your PostgreSQL database connection and validation logic here
            // Example logic:
            String response;
            String method = t.getRequestMethod();
            if (method.equals("POST")) {
                InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.readLine();
                String[] inputs = formData.split("&");
                String username = inputs[0].split("=")[1];
                String password = inputs[1].split("=")[1];
                // Add your PostgreSQL database connection and validation logic here
                if (isValidLogin(username, password)) {

                    response = "Login Successful";
                } else {
                    System.out.println(username);
                    System.out.println(password);
                    response = "Invalid username or password";
                }
            } else {
                response = "Invalid Method";
            }
            sendResponse(t, response, 200);
        }

        private boolean isValidLogin(String username, String password) {
            // Add your PostgreSQL database connection and validation logic here
            // Example connection code:
            try {
                // Add your PostgreSQL database connection logic here
                // For example:

                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/javadb", "postgres", "root");
                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
                ResultSet rs = stmt.executeQuery(sql);
                 if (rs.next()) {
                     rs.close();
                     stmt.close();
                     conn.close();
                     return true;
                 }
                 rs.close();
                 stmt.close();
                 conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false; // For the sake of example, always return false
        }

        private void sendFileResponse(HttpExchange t, String fileName) throws IOException {
            File file = new File(fileName);
            if (file.exists()) {
                byte[] bytes = Files.readAllBytes(Paths.get(fileName));
                t.sendResponseHeaders(200, bytes.length);
                OutputStream os = t.getResponseBody();
                os.write(bytes);
                os.close();
            } else {
                sendResponse(t, "File Not Found", 404);
            }
        }

        private void sendResponse(HttpExchange t, String response, int statusCode) throws IOException {
            t.sendResponseHeaders(statusCode, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}