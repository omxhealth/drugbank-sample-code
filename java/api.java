import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;

public class api {

    public static final String DRUGBANK_API = "https://api.drugbankplus.com/v1/";
    public static final String DRUGBANK_API_KEY = System.getenv("DB_API_KEY");
    public static final Map<String, String> DRUGBANK_HEADERS = new HashMap<String, String>() {
        {
            put("Authorization", DRUGBANK_API_KEY);
            put("Content-Type", "application/json");
            put("Accept", "application/json");
        }
    };    

    public static void main(String[] args) {

        API_Key_Check();

        ddi_example();

    }

    /**
     * Checks if the dev API key has been set in the environment
     */
    public static void API_Key_Check() {
        if (DRUGBANK_API_KEY == null) {
            System.out.println("please set environment variable DB_API_KEY");
            System.exit(1);
        }
    }

    /**
     * Implemented from https://stackoverflow.com/a/26177982/12471692
     * @param route what to pull from the API
     * @param params the query parameters to the API
     * @return
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public static URL drugbank_url(String route, Map<String, Object> params) throws URISyntaxException,
            MalformedURLException {

        URI oldUri = new URI(DRUGBANK_API + route);
        StringBuilder queries = new StringBuilder();
        
        //add all params to the request
        for (Map.Entry<String, Object> query: params.entrySet()) {
            queries.append( "&" + query.getKey()+"="+query.getValue());
        }

        String newQuery = oldUri.getQuery();
        if (newQuery == null) {
            newQuery = queries.substring(1);
        } else {
            newQuery += queries.toString();
        }

        URI newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(),
            oldUri.getPath(), newQuery, oldUri.getFragment());

        return newUri.toURL();
     
    }

    public static void setHeader(HttpsURLConnection connection) {
        for (Map.Entry<String, String> entry: DRUGBANK_HEADERS.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());;
        }
    }

    public static void drugbank_get(String route, Map<String, Object> params) throws IOException, URISyntaxException {

        int responseCode;
        String readLine = "";
        URL url = drugbank_url(route, params);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        setHeader(connection);

        responseCode = connection.getResponseCode();

        if (responseCode == HttpsURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer response = new StringBuffer();

            //read the call response into the stringbuffer
            while ((readLine = in.readLine()) != null) {
                response.append(readLine);
            }

            in.close();

            JSONObject responseJSON = new JSONObject(response.toString());
            pretty_log(responseJSON);

        } else {
            throw new RuntimeException("Request Failed. Status Code: " + responseCode);
        }

    }

    public static void pretty_log(JSONObject json) {
        System.out.print(json.toString(4));
    }

    /**
     * drug_names request example for tylenol (no params)
     */
    public static void drug_names_example() {
        
        Map<String, Object> params = new HashMap<String, Object>() {
            {
                put("q", "tylenol");
            }
        };

        try {
            drugbank_get("drug_names", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Drug-drug interaction (DDI) example. 
     * Gets interactions by Drugbank ids
     */
    public static void ddi_example() {
        
        String[] drug_ids = new String[]{"DB01598", "DB01597", "DB12377", "DB01004"};

        Map<String, Object> params = new HashMap<String, Object>() {
            {
                put("drugbank_id", String.join(",", drug_ids));
            }
        };

        try {
            drugbank_get("ddi", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
}