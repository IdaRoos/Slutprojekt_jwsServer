import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Server {
    public static void main(String[] args) {
        Socket socket;
        InputStreamReader inputStreamReader;
        OutputStreamWriter outputStreamWriter;
        BufferedReader bufferedReader;
        BufferedWriter bufferedWriter;
        ServerSocket serverSocket;

        try {

            serverSocket = new ServerSocket(4321);

        } catch (Exception e) {
            System.out.println(e);
            return;
        }

        while (true) {
            try {
                // En "väntemetod", väntar på specifik socket efter trafik
                socket = serverSocket.accept();
                inputStreamReader = new InputStreamReader(socket.getInputStream());
                outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
                bufferedReader = new BufferedReader(inputStreamReader);
                bufferedWriter = new BufferedWriter(outputStreamWriter);

                String message;

                while (true) {

                    message = bufferedReader.readLine();
                    if (message == null) {
                        break; // break the loop if message is null
                    }

                    // Hämta klientens meddelande och skicka den till openUpData() metoden
                    String returnData = openUpData(message);
                    System.out.println(message);

                    //Skicka acknowledgement svar tillbaka till klienten
                    bufferedWriter.write(returnData);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }

//  Stäng kopplingar
                socket.close();
                inputStreamReader.close();
                outputStreamWriter.close();
                bufferedReader.close();
                bufferedWriter.close();


            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    static String openUpData(String message) throws IOException, ParseException {

        // Steg 1. Bygg upp JSON Object baserat på inkommande string
        JSONParser parser = new JSONParser();
        JSONObject jsonOb = (JSONObject) parser.parse(message);
        System.out.println("jsonOb" + jsonOb);

        // Steg 2. läs av URL och HTTP metod för att veta vad klienten vill
        // Hämta värdet av httpUrl
        String url = jsonOb.get("httpURL").toString();
        // Hämta värdet av httpMethod
        String method = jsonOb.get("httpMethod").toString();

        //Steg 2.5 Dela upp URL med .split metod, där det finns en slash vill vi dela upp url:en i mindre delar
        String[] urls = url.split("/");


        // Steg 3.Använd en SwitchCase för att kolla vilken data som ska användas
        switch (urls[0]) {
            case "persons": {
                if (method.equalsIgnoreCase("get")) {
                    //Skapa JSONReturn objektet
                    JSONObject jsonReturn = new JSONObject();

                    // Hämta data från JSON fil
                    String data = parser.parse(new FileReader("data/data.json")).toString();
                    if (urls.length > 1) {
                        String id = urls[1];
                        System.out.println("Requested ID: " + id);
                        JSONObject jsonData = (JSONObject) parser.parse(data);
                        System.out.println("jsonData: " + jsonData);
                        JSONObject personData = (JSONObject) jsonData.get("p" + id);
                        System.out.println("personData: " + personData);

                        if (personData != null) {
                            // Returnera data för en specifik person
                            jsonReturn.put("data", personData);
                            jsonReturn.put("httpStatusCode", 200);

                        } else {
                            // Person med efterfrågat ID finns inte
                            jsonReturn.put("data", "Person not found");
                            jsonReturn.put("httpStatusCode", 404);
                        }
                        return jsonReturn.toJSONString();


                    } else {
                        // Vill hämta data om personer
                        //Skapa JSONReturn objektet
                        jsonReturn = new JSONObject();

                        // Hämta data från JSON fil
                        jsonReturn.put("data", parser.parse(new FileReader("data/data.json")).toString());
                        // Konverterar jsonObjekt till strängvärde med hjälp av "toJSONString()"

                        // Inkluderat HTTP status code
                        jsonReturn.put("httpStatusCode", 200);

                        // Returnera JSON String
                        return jsonReturn.toJSONString();
                    }

                } else if (method.equalsIgnoreCase("post")) {

                    JSONObject data = (JSONObject) jsonOb.get("data");

                    //Denna kod kommer först att hämta "p1" objektet från "data", och sedan hämta "id", "name", "age" och "favoriteColor" från "p1" objektet.
                    JSONObject personData = data.get("p1") != null ? (JSONObject) data.get("p1") : null;
                    long id = personData != null && personData.get("id") != null ? Long.parseLong(personData.get("id").toString()) : 0L;
                    String name = personData != null && personData.get("name") != null ? personData.get("name").toString() : "";
                    long age = personData != null && personData.get("age") != null ? Long.parseLong(personData.get("age").toString()) : 0L;
                    String favoriteColor = personData != null && personData.get("favoriteColor") != null ? personData.get("favoriteColor").toString() : "";


                    // Create a new person object with the new values
                    JSONObject newPerson = new JSONObject();
                    newPerson.put("id", id);
                    newPerson.put("name", name);
                    newPerson.put("age", age);
                    newPerson.put("favoriteColor", favoriteColor);
                    System.out.println(newPerson);

                    try (FileReader fileReader = new FileReader("data/data.json")) {
                        // Read the JSON file into a JSON object
                        JSONParser jsonParser = new JSONParser();
                        JSONObject jsonFile = (JSONObject) jsonParser.parse(fileReader);

                        // Add the new person object to the jsonFile
                        String newPersonKey = "p" + (jsonFile.size() + 1);
                        jsonFile.put(newPersonKey, newPerson);

                        // Write jsonFile to data.json
                        try (FileWriter fileWriter = new FileWriter("data/data.json")) {
                            fileWriter.write(jsonFile.toJSONString());
                            fileWriter.flush();
                        }

                        // Create the JSONReturn object
                        JSONObject jsonReturn = new JSONObject();
                        jsonReturn.put("httpStatusCode", 200);

                        // Return the JSON string
                        return jsonReturn.toJSONString();
                    }
                }
            }
        }
        return "Message recieved";
    }
}
