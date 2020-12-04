import com.fasterxml.jackson.databind.ObjectMapper;
import exception.SlicePageContentFetcherException;
import model.SliceDetail;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlicePageContentFetcher {

    private final static String URL = "https://www.factornews.com/xhr/loadmoreslices/";
    
    public static List<SliceDetail> fetch(Integer page) throws SlicePageContentFetcherException {

        HttpURLConnection connection = initConnection();
        addRequestParameters(connection, page);
        String pageContent = getNextPageContentString(connection);
        List<SliceDetail> slicePage = convertPageContentToJson(pageContent);
        closeConnection(connection);

        return slicePage;
    }

    private static HttpURLConnection initConnection() throws SlicePageContentFetcherException {

        HttpURLConnection connection = null;

        try {
            URL url = new URL(URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
        } catch (IOException e) {
            throw new SlicePageContentFetcherException("Erreur lors de l'initialisation de la connexion HTTP", e);
        }

        return connection;
    }

    private static void addRequestParameters(HttpURLConnection connection, Integer page) throws SlicePageContentFetcherException {

        Map<String, String> parameters = new HashMap<>();
        parameters.put("loadpage", page.toString());

        connection.setDoOutput(true);
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new SlicePageContentFetcherException("Erreur lors de l'ajout des paramètres à la requête", e);
        }
    }

    private static String getNextPageContentString(HttpURLConnection connection) throws SlicePageContentFetcherException {

        BufferedReader in;
        StringBuffer content = new StringBuffer();

        try {
            int status = connection.getResponseCode();

            if (status != 200) {
                throw new SlicePageContentFetcherException("Erreur lors de l'exécution de la requête vers Slice, code d'erreur HTTP: " + status);
            }

            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();

        } catch (IOException e) {
            throw new SlicePageContentFetcherException("Erreur lors de l'exécution de la requête vers Slice", e);
        }

        return content.toString();
    }

    private static List<SliceDetail> convertPageContentToJson(String content) throws SlicePageContentFetcherException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(content, mapper.getTypeFactory().constructCollectionType(List.class, SliceDetail.class));
        } catch (IOException e) {
            throw new SlicePageContentFetcherException("Erreur lors de la conversion de la réponse JSON", e);
        }
    }

    private static void closeConnection(HttpURLConnection connection) {
        connection.disconnect();
    }
}
