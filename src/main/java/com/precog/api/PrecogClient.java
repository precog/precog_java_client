package com.precog.api;

import com.precog.api.Request.ContentType;
import com.precog.api.dto.AccountInfo;
import com.precog.api.dto.PrecogServiceConfig;
import com.precog.api.dto.QueryResult;
import com.precog.api.options.IngestOptions;
import com.precog.json.ToJson;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A simple client for storing arbitrary records in the Precog database.
 *
 * @author Kris Nuttycombe <kris@precog.com>
 * @author Tom Switzer <switzer@precog.com>
 */
public class PrecogClient {
	private static final Logger logger = Logger.getLogger(PrecogClient.class.getName());
	
	static final URL fromHost(String host) {
		try {
			return new URL("https", host, 443, "/");
		} catch (MalformedURLException ex) {
            logger.log(Level.SEVERE, "Invalid client URL", ex);
            return null;
        }
	}
	
    /**
     * The deprecated production http service.
     *
     * @deprecated use {@link PRODUCTION_HTTPS}
     */
    @Deprecated
    public static final URL PRODUCTION_HTTP = fromHost("api.precog.com");

    /** The default Precog (production) HTTPS service. */
    public static final URL PRODUCTION_HTTPS = fromHost("api.precog.com");

    /** The default Precog Beta HTTPS service. */
    public static final URL BETA_HTTPS = fromHost("beta.precog.com");

    /** Precog development HTTPS service. */
    public static final URL DEV_HTTPS = fromHost("devapi.precog.com");
	
	
    private final URL service;
    
	private final String apiKey;
	
	private final Gson gson;

    private final Rest rest;

    public static final int API_VERSION = 1;

    private static class Paths {
        public static Path FS = new Path("/fs");
    }

    private static class Services {
        public static String ANALYTICS = "/analytics";
        public static String ACCOUNTS = "/accounts";
        public static String INGEST = "/ingest";
    }

    /**
     * Factory method to create a Precog client from a Heroku addon token
     *
     * @param precogToken Heroku precog addon token
     * @return Precog client
     */
    public static PrecogClient fromHeroku(String precogToken) {
        return new PrecogClient(PrecogServiceConfig.fromToken(precogToken));
    }

    /**
     * A convenience constructor that uses the default production API.
     * Note: during the Precog beta period, you must use the two-argment constructor
     * and provide the specific Service instance for the storage server URL provided
     * with your integration instructions.
     *
     * @param apiKey The string token that permits storage of records at or below the
     *               virtual filesystem path to be used
     */
    public PrecogClient(String apiKey) {
    	this(PrecogClient.PRODUCTION_HTTPS, apiKey, null);
    }

    /**
     * Builds a new client to connect to precog services based on an PrecogServiceConfig.
     *
     * @param ac account token
     */
    public PrecogClient(PrecogServiceConfig ac){
    	this(fromHost(ac.getHost()), ac.getApiKey(), null);
    }

    /**
     * Builds a new client to connect to precog services.
     *
     * @param service service to connect
     * @param apiKey  api key to use
     */
    public PrecogClient(URL service, String apiKey) {
    	this(service, apiKey, null);
    }
    
    /**
     * Builds a new client to connect to precog services.
     * 
     * If a {@code gson} is not null, then the provided {@code Gson} object
     * will be used by {@link store(Path,Object)} to serialize the object to
     * JSON. If {@code gson} is null, then a default {@code Gson} object is
     * used instead.
     * 
     * @param service Precog end-point to use
     * @param apiKey API key used to authenticate with Precog
     * @param gson An optional Gson object to use for JSON serialization
     */
    public PrecogClient(URL service, String apiKey, Gson gson) {
        this.service = service;
        this.apiKey = apiKey;
        this.gson = gson == null ? new Gson() : gson;
        this.rest = new Rest(service, apiKey);
    }
    
    /**
     * Returns the current service (Precog end-point) being used.
     * 
     * @return the Precog service
     */
    public URL getService() {
		return service;
	}


    /**
     * Get the Api Key used by this client to store data.
     *
     * @return the value of apiKey
     */
    public String getApiKey() {
        return apiKey;
    }


    /**
     * Builds a path given a service and path, using the current api version
     *
     * @param service the name of the API service to access (eg. account, ingest,etc)
     * @param path    The path corresponding to the action to be performed
     * @return Path of the form /$service/v$version/$path
     */
    public static Path actionPath(String service, Path path) {
        return new Path(service + "/v" + API_VERSION).append(path);
    }

    /**
     * Creates a new account ID, accessible by the specified email address and
     * password, or returns the existing account ID.
     *
     * @param service  the Precog service to use
     * @param email    user's email
     * @param password user's password
     * @return Json string with the account Id
     * @throws IOException
     * @see Service
     */
    public static String createAccount(URL service, String email, String password) throws IOException {
        Request r = new Request();
        r.setBody("{ \"email\": \"" + email + "\", \"password\": \"" + password + "\" }");
        Rest rest = new Rest(service);
        // Returns Conflict if account exists.
        return rest.request(Rest.Method.POST, actionPath(Services.ACCOUNTS, new Path("accounts/")).getPath(), r);
    }

    /**
     * Creates a new account ID, accessible by the specified email address and
     * password, or returns the existing account ID. This just calls
     * {@link createAccount(Service,String,String)} with a default service of
     * {@link Service#ProductionHttps}.
     *
     * @param email    user's email
     * @param password user's password
     * @return Json string with the account Id
     * @throws IOException
     * @see Service#ProductionHttps
     */
    public static String createAccount(String email, String password) throws IOException {
        return createAccount(PrecogClient.PRODUCTION_HTTPS, email, password);
    }

    /**
     * Retrieves the details about a particular account. This call is the
     * primary mechanism by which you can retrieve your master API key.
     *
     * @param service  the Precog service (end-point) to use
     * @param email     user's email
     * @param password  user's password
     * @param accountId account's id number
     * @return account info
     * @throws IOException
     * @see Service
     */
    public static String describeAccount(URL service, String email, String password, String accountId) throws IOException {
        Request r = new Request();
        Rest.addBaseAuth(r.getHeader(), email, password);
        Rest rest = new Rest(service);
        return rest.request(Rest.Method.GET, actionPath(Services.ACCOUNTS, new Path("accounts/" + accountId)).getPath(), r);
    }


    /**
     * Retrieves the details about a particular account. This call is the
     * primary mechanism by which you can retrieve your master API key.
     * This is equivalent to calling
     * {@link describeAccount(Service,String,String,String)} with a default
     * service of {@link Service#ProductionHttps}.
     *
     * @param email     user's email
     * @param password  user's password
     * @param accountId account's id number
     * @return account info
     * @throws IOException
     */
    public static String describeAccount(String email, String password, String accountId) throws IOException {
        return describeAccount(PrecogClient.PRODUCTION_HTTPS, email, password, accountId);
    }
    
    /**
     * Store the object {@code obj} as a record in Precog. It is serialized by
     * Gson using reflection. If a {@code Gson} object was provided during
     * construction, then it will be used, otherwise the default Gson
     * serialization will be used.
     * 
     * @note Calling this method guarantees the object is stored in the Precog
     *       transaction log.
     * 
     * @param path The path in the virtual file system to store the record
     * @param obj The object to serialize to JSON and store in the VFS
     * @throws IOException
     */
    public <T> void store(Path path, T obj) throws IOException {
    	String json = gson.toJson(obj);
    	store(path, json);
    }

    /**
     * Store the specified record.
     *
     * @param <T>        The type of the record object. This type must be serializable to JSON using a ToJson instance
     *                   for some supertype of the specified type.
     * @param path       The path at which the record should be placed in the virtual file system.
     * @param obj        The record being storeed.
     * @param serializer The function used to serialize the record to a JSON string.
     * @throws IOException
     */
    public <T> void store(Path path, T obj, ToJson<? super T> serializer) throws IOException {
        store(path, serializer.serialize(obj));
    }

    /**
     * Store a raw JSON string at the sep.
     */
    public void store(Path path, String recordJson) throws IOException {
        IngestOptions options = new IngestOptions(ContentType.JSON);
        ingest(path, recordJson, options);
    }

    /**
     * Builds the async/sync data storage path
     *
     * @param async boolean, true to do an async storage call
     * @param path  The path at which the record should be placed in the virtual file system.
     * @return full path
     */
    public Path buildStoragePath(boolean async, Path path) {
        return new Path(async ? "async" : "sync").append(Paths.FS).append(path);
    }

    /**
     * Builds a sync data storage path
     *
     * @param path The path at which the record should be placed in the virtual file system.
     * @return full path
     */
    public Path buildStoragePath(Path path) {
        return buildStoragePath(false, path);
    }


    /**
     * Ingest data in the specified path.
     *
     * Ingest behavior is controlled by the ingest options.
     * <p/>
     * If Async is true,  asynchronously uploads data to the specified path and
     * file name. The method will return almost immediately with an HTTP
     * ACCEPTED response.
     * <p/>
     * If Async is false, synchronously uploads data to the specified path and
     * file name. The method will not return until the data has been committed
     * to the transaction log. Queries may or may not reflect data committed
     * to the transaction log.
     * <p/>
     * The optional owner account ID parameter can be used to disambiguate the
     * account that owns the data, if the API key has multiple write grants to
     * the path with different owners.
     *
     * @param path    The path at which the record should be placed in the virtual file system.
     * @param content content to be ingested
     * @param options Ingestion options
     * @return ingest result
     * @throws IOException
     */
    public String ingest(Path path, String content, IngestOptions options) throws IOException {
        if (content == null || content.equals("")) {
            throw new IllegalArgumentException("argument 'content' must contain a non empty value formatted as described by type");
        }
        Request request = new Request();
        request.getParams().putAll(options.asMap());
        request.setBody(content);
        request.setContentType(options.getDataType());
        return rest.request(Rest.Method.POST, actionPath(Services.INGEST, buildStoragePath(options.isAsync(), path)).getPath(), request);
    }
    
    /**
     * Deletes the specified path.
     *
     * @param path
     * @return
     * @throws IOException
     */
    public String delete(Path path) throws IOException {
        Request request = new Request();
        return rest.request(Rest.Method.DELETE, actionPath(Services.INGEST, buildStoragePath(path)).getPath(), request);
    }

    /**
     * Executes a synchronous query relative to the specified base path. The
     * HTTP connection will remain open for as long as the query is evaluating
     * (potentially minutes).
     *
     * Not recommended for long-running queries, because if the connection is
     * interrupted, there will be no way to retrieve the results of the query.
     *
     * @param path relative storage path to query
     * @param q    quirrel query to excecute
     * @return result as Json string
     * @throws IOException
     */
    public QueryResult query(Path path, String q) throws IOException {
        if (!path.getPrefix().equals(Paths.FS)) {
            path = Paths.FS.append(path);
        }
        Request request = new Request();
        request.getParams().put("q", q);
        request.getParams().put("format", "detailed");
        String response = rest.request(Rest.Method.GET, actionPath(Services.ANALYTICS, path).getPath(), request);
        QueryResult result = gson.fromJson(response, QueryResult.class);
        return result;
    }
}
