package com.db.app;

import static spark.Spark.*;
import static spark.debug.DebugScreen.enableDebugScreen;
import spark.ModelAndView;
import spark.Request;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 */
public class App {

    private static final Logger logger = LogManager.getLogger(App.class);
    protected static String templatesPath = "";
    protected static String authKey = "";
    protected static String apiHost = "";
    
    public static void main(String[] args) {
        Properties props = loadProperties();
        setupServer(props);

        JinjavaEngine engine = new JinjavaEngine(templatesPath);
        engine.setUseCache(false);

        //TODO
        get("/", (req, res) -> new ModelAndView(new HashMap<>(), "index.html"), engine);

        //product concepts page
        get("/product_concepts", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            return new ModelAndView(attributes, "product_concepts.html");
        }, engine);

        get("/api/product_concepts", (req, res) -> {
            Map<String, String> params = setParams(req);
            return api.drugbank_get("product_concepts", params).getData();
        });

        //region
        get("/api/*/product_concepts", (req, res) -> {
            Map<String, String> params = setParams(req);
            return api.drugbank_get(req.splat()[0] + "/product_concepts", params).getData().toString();
        });

        get("/api/product_concepts/*/*", (req, res) -> {
            Map<String, String> params = setParams(req);
            return api.drugbank_get("product_concepts/" + req.splat()[0] + "/" +
            req.splat()[1], params).getData();
        });

        //region
        get("/api/*/product_concepts/*/*", (req, res) -> {
            Map<String, String> params = setParams(req);
            return api.drugbank_get(req.splat()[0] + "/product_concepts/" + 
            req.splat()[1] + "/" + req.splat()[2], params).getData();
        });

        //drug names page
        get("/api/drug_name", (req, res) -> {
            Map<String, String> params = setParams(req);
            return api.drugbank_get("drug_names", params).getData();
        });

        //drug-drug interactions page
        get("/api/ddi", (req, res) -> {
            Map<String, String> params = setParams(req);
            return api.drugbank_get("ddi", params).getData();
        });
       
    }

    /**
     * Grabs the params from the request and returns them as a Map.
     * @param req the request made
     * @return Map of the params
     */
    public static Map<String, String> setParams(Request req) {
        Map<String, String> params = new HashMap<String, String>();

        for (String param : req.queryParams()) {
            params.put(param, req.queryParams(param));
        }

        return params;
    }

    /**
     * Starts up the server based on the value in the props paramater.
     * 
     * @param props Properties
     */
    static void setupServer(Properties props) {
        
        try {
            int port = Integer.parseInt(props.getProperty("port"));
            port(port);
            logger.info("Port: " + port);
        } catch(Exception e) {
            logger.error("Cannot set port value", e);
        }

        try {
            String staticPath = Paths.get(props.getProperty("static")).toString();
            externalStaticFileLocation(staticPath);
            logger.info("Static files path: " + staticPath);
        } catch(Exception e) {
            logger.error("Cannot set static files path", e);
        }

        try {
            templatesPath = props.getProperty("templates");
            logger.info("Templates path: " + templatesPath);
        } catch(Exception e) {
            logger.error("Cannot set templates path", e);
        }

        try {
            apiHost = props.getProperty("api-host");
            logger.info("API Host: " + apiHost);
        } catch(Exception e) {
            logger.error("No host specified", e);
        }

        try {
            authKey = props.getProperty("auth-key");
            logger.info("Authentication: " + authKey);
        } catch(Exception e) {
            logger.error("Cannot find authorization", e);
        }

    }

    /**
     * Load properties from the file "server.properties".
     * 
     * The file must contain:
     *  - port: the port to host the server on
     *  - templates: path to the template resources directory
     *  - static: path to the static resources directory
     *  - api-host: the URL to the Drugbank API including the version to be used
     *    ("https://api.drugbankplus.com/v1/")
     *  - auth-key: authorization key for API access
     * 
     * @return Properties
     */
    static Properties loadProperties() {
        Properties props = new Properties();

        logger.info("Loading server properties...");

        try {
            Reader reader = new FileReader(Paths.get("server.properties").toString());
            props.load(reader);
        } catch(Exception e) {
            logger.error("Cannot read properties file", e);
        }

        return props;

    }

}
