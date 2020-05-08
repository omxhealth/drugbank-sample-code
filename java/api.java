import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.json.*;

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

        //ddi_example();
        adverse_effects_paging_example();
    
    }

    /**
     * Checks if the dev API key has been set in the system environment
     */
    public static void API_Key_Check() {
        if (DRUGBANK_API_KEY == null) {
            System.out.println("please set environment variable DB_API_KEY");
            System.exit(1);
        }
    }

    /**
     * Creates the URL where the API call is to be sent to (no query parameters)
     */
    public static URL drugbank_url(String route) throws MalformedURLException {
        URL url = new URL(DRUGBANK_API + route);
        return url;
    }

    /**
     * Creates the URL where the API call is to be sent to (with query parameters)
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

    /**
     * Sets the header for the connection before establishing it.
     */
    public static void setHeader(HttpsURLConnection connection) {
        for (Map.Entry<String, String> entry: DRUGBANK_HEADERS.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());;
        }
    }

    public static DBResponse drugbank_get(String route) throws IOException, URISyntaxException {
        return drugbank_get(route, null);
    }

    /**
     * Makes a GET request to the DrugBank API with query parameters.
     * @param route: the url route
     * @param params: url query parameters
     * @return JSON object retrieved
     * @throws IOException
     * @throws URISyntaxException
     */
    public static DBResponse drugbank_get(String route, Map<String, Object> params) throws IOException, URISyntaxException {

        int responseCode;
        URL url;
        DBResponse res;
        String readLine = "";

        if (params == null) {
            url = drugbank_url(route);
        } else {
            url = drugbank_url(route, params);
        }
        
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

            Map<String, List<String>> header = connection.getHeaderFields();
            
            if (response.toString().startsWith("[")) {
                JSONArray responseJSON = new JSONArray(response.toString());
                res = new DBResponse(responseJSON, header);
            } else {
                JSONObject responseJSON = new JSONObject(response.toString());
                res = new DBResponse(responseJSON, header);
            }
            
            return res;

        } else {
            throw new RuntimeException("Request Failed. Status Code: " + responseCode);
        }

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
            DBResponse res = drugbank_get("drug_names", params);
            res.prettyPrintData();
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
            DBResponse res = drugbank_get("ddi", params);
            res.prettyPrintData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public static Object adverse_effects_paging_example() {
        
        try {
            DBResponse page1 = drugbank_get("drugs/DB00472/adverse_effects");
            DBResponse page2 = drugbank_get(page1.getNextPageLink());
            return page2.data; 
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }
    
}

class DBResponse {

    Object data;
    Map<String, List<String>> response;
    boolean isObject;

    DBResponse(Object data, Map<String, List<String>> response) {
        
        if (data instanceof JSONObject) {
            this.isObject = true;
        } else if (data instanceof JSONArray) {
            this.isObject = false;
        } else {
            throw new IllegalArgumentException("Data provided is not an instance of a JSONObject or JSONArray");
        }
        
        this.data = data;
        this.response = response;
        
    }

    public boolean isObject() {
        return this.isObject();
    }

    public Map<String, List<String>> getResponse() {
        return response;
    }

    /**
     * Returns the data from the database response.
     * Remember to cast to the correct type (JSONObject or JSONArray)!
     * @return
     */
    public Object getData() {
        return data;
    }

    public String getNextPageLink() {
        String header = this.response.get("Link").toString();

        if (header == null){
            return null;
        } else {
            return header;
        }

    }

    public void printResponse() {
        
        System.out.println("Response Header:");
        
        for (Map.Entry<String, List<String>> entry : this.response.entrySet()) {
            System.out.println("Key : " + entry.getKey() + " , Value : " + entry.getValue());
        }

    }

    public void prettyPrintData() {
        if (isObject){
            System.out.print(((JSONObject) data).toString(4));
        } else {
            System.out.print(((JSONArray) data).toString(4));
        }
        
    }

}
