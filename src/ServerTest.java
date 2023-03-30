import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerTest {

    private final String serverAddress = "localhost";
    private final int serverPort = 4321;

    @Test
    public void testGetAllPersons() throws Exception {
        try (
                Socket socket = new Socket(serverAddress, serverPort);
                InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)
        ) {
            JSONObject request = new JSONObject();
            request.put("httpURL", "persons");
            request.put("httpMethod", "get");

            bufferedWriter.write(request.toJSONString());
            bufferedWriter.newLine();
            bufferedWriter.flush();

            String response = bufferedReader.readLine();

            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(response);

            assertEquals("200", jsonResponse.get("httpStatusCode").toString());
        }
    }

    @Test
    public void testAddNewPerson() throws Exception {
        try (
                Socket socket = new Socket(serverAddress, serverPort);
                InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)
        ) {
            JSONObject request = new JSONObject();
            request.put("httpURL", "persons");
            request.put("httpMethod", "post");

            JSONObject personData = new JSONObject();
            personData.put("id", 12345);
            personData.put("name", "Test Person");
            personData.put("age", 30);
            personData.put("favoriteColor", "blue");

            JSONObject data = new JSONObject();
            data.put("p1", personData);
            request.put("data", data);

            bufferedWriter.write(request.toJSONString());
            bufferedWriter.newLine();
            bufferedWriter.flush();

            String response = bufferedReader.readLine();

            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(response);

            assertEquals("200", jsonResponse.get("httpStatusCode").toString());
        }
    }
}
