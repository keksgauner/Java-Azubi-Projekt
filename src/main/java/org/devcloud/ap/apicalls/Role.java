package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.database.PgRole;
import org.devcloud.ap.utils.JSONCreator;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

public class Role {
    private static final Logger logger = LoggerFactory.getLogger(Role.class);

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/role/roles", new Roles());
    }

    private Role() {}

    private final static String error = "error";

    private static void addResponseHeaders(HttpExchange httpExchange) {
        httpExchange.getResponseHeaders().add("Content-Type", "application/json");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    }

    private static void writeResponse(HttpExchange httpExchange, String response, int statusCode) throws IOException {
        httpExchange.sendResponseHeaders(statusCode, response.length());

        OutputStream outputStream = httpExchange.getResponseBody();
        for(char write : response.toCharArray())
            outputStream.write(write);
        outputStream.close();
    }

    private static JSONCreator getJSONCreator(int statusCode) {
        return new JSONCreator().addKeys("statuscode").addValue(statusCode);
    }

    private static void debugRequest(URI requestURI) {
        logger.debug("{} - was requested", requestURI);
    }

    private static class Roles implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);


            Session session = Azubiprojekt.getSqlPostgres().openSession();


            Query query = session.createQuery("FROM PgRole", PgRole.class);
            List<PgRole> pgRoles = query.list();
            logger.debug("Lese Role records...");

            HashMap<Integer, HashMap<String, String>> roles = new HashMap<>();
            for (PgRole rawRoles : pgRoles) {
                HashMap<String, String> infos = new HashMap<>();

                infos.put("name", rawRoles.getRolename());
                infos.put("color", rawRoles.getRolecolor());
                roles.put(rawRoles.getRoleid(), infos);
            }

            String response = getJSONCreator(201)
                    .addKeys("response", "roles")
                    .addValue("Du hast erfolgreich die Roles abgefragt!", new JSONObject(roles).toString()).toString();

            writeResponse(httpExchange, response, 201);
        }
    }
}
