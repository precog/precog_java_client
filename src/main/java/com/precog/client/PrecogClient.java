package com.precog.client;

import com.precog.client.rest.HttpException;
import com.precog.client.rest.Method;
import com.precog.client.rest.Path;
import com.precog.client.rest.Request;
import com.precog.client.rest.RequestBuilder;
import com.precog.client.rest.Response;
import com.precog.client.rest.Rest;
import com.precog.json.ToJson;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

/**
 * A simple REST client for storing data in Precog and querying it with Quirrel.
 * <p>
 * This provides methods to upload files to
 * your virtual file system (in Precog), append records/events to the VFS,
 * delete data, and run Quirrel queries on your data. Additionally, you can
 * also create new accounts and get the account details for existing accounts.
 * <p>
 * All methods are blocking, which means that the method returns when the
 * server has replied with the answer.
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
			// Unfortunately, we need to turn this into a runtime exception.
            throw new IllegalArgumentException("Invalid host: cannot construct URL from: https://" + host);
        }
	}	

    /** The Precog (nebula) HTTPS service. */
    public static final URL NEBULA_HTTPS = fromHost("nebula.precog.com");

    /**
     * The Precog Beta HTTPS service. This is also the default Precog service
     * used when one isn't specified. If you signed up for a beta account, this
     * is the services you'll want to use.
     */
    public static final URL BETA_HTTPS = fromHost("beta.precog.com");

    /**
     * Precog development HTTPS service. This is useful for testing t
     */
    public static final URL DEV_HTTPS = fromHost("devapi.precog.com");

    /** Precog API version being used. */
    public static final int API_VERSION = 1;
    
    private static Path FS = new Path("/fs/");

    private static class Paths {
    	private static Path service(String serv) {
    		return new Path(serv + "/v" + API_VERSION + "/");
    	}
    	
        public static Path ANALYTICS = service("analytics");
        public static Path ACCOUNTS = service("accounts");
        public static Path INGEST = service("ingest");
    }
    
    
    private final Gson gson;
    private final URL service;
    private final Rest rest;
    private final String accountId;
    private final Path basePath;
	private final String apiKey;
	

    /**
     * Builds a new client to connect to precog services.
     * 
     * @param service Precog end-point to use
     * @param apiKey API key used to authenticate with Precog
     * @param accountId the account to use as the data owner
     * @param basePath The base path to use for all requests
     * @param gson An optional Gson object to use for JSON serialization
     */
    PrecogClient(URL service, String apiKey, String accountId, String basePath, Gson gson) {
        this.service = service;
        this.apiKey = apiKey;
        this.accountId = accountId;
        this.basePath = basePath == null ? new Path("/") : new Path("/" + basePath);
        this.gson = gson == null ? new Gson() : gson;
        this.rest = new Rest(service);
    }

    /**
     * Builds a new client to connect to precog services.
     *
     * @param service service to connect
     * @param apiKey  api key to use
     * @param accountId the account to use as the data owner
     * @param basePath The base path to use for all requests
     */
    public PrecogClient(URL service, String apiKey, String accountId, String basePath) {
    	this(service, apiKey, accountId, basePath, null);
    }

    /**
     * Builds a new client to connect to precog services. This will use the
     * {@code accountId} as the base path {@link #getBasePath()} for all queries.
     * If this is not desired, you must construct the {@link PrecogClient} with
     * a base path explicitly.
     * <p>
     * This is equivalent to calling:
     * {@code new PrecogClient(service, apiKey, accountId, accountId)}.
     *
     * @param service service to connect
     * @param apiKey  api key to use
     * @param accountId the account to use as the data owner
     */
    public PrecogClient(URL service, String apiKey, String accountId) {
    	this(service, apiKey, accountId, accountId, null);
    }

    /**
     * Builds a new client to connect to precog services. This attempts to
     * determine the data owner (on ingest/append/upload) from the API key.
     * This may fail and cause the related methods to throw an exception. If
     * this is not desired, then use a constructor that let's you use one of
     * the account IDs.
     * <p>
     * The base path in this case is assumed to be {@code "/"}.
     *
     * @param service service to connect
     * @param apiKey  api key to use
     */
    public PrecogClient(URL service, String apiKey) {
    	this(service, apiKey, null, null, null);
    }

    /**
     * Construct a new PrecogClient using the API key of the given account, the
     * account ID of the account as the data owner, and the account root path
     * as the base path.
     * 
     * @param service the Precog end-point to use
     * @param account the account to base this client off of
     */
    public PrecogClient(URL service, AccountInfo account) {
    	this(service, account.getApiKey(), account.getAccountId(), account.getRootPath());
    }
    
    /**
     * A convenience constructor that uses the default beta API.
     * Note: during the Precog beta period, you must use the two-argment constructor
     * and provide the specific Service instance for the storage server URL provided
     * with your integration instructions.
     * <p>
     * This uses the {@link PrecogClient#BETA_HTTPS} (beta.precog.com) service by default.
     *
     * @param apiKey The string token that permits storage of records at or below the
     *               virtual filesystem path to be used
     */
    public PrecogClient(String apiKey, Path basePath) {
    	this(PrecogClient.BETA_HTTPS, apiKey, null);
    }

    /**
     * Factory method to create a Precog client from a Heroku add-on token.
     *
     * @param precogToken Heroku precog addon token
     * @return a {@link PrecogClient} configured from the Heroku token
     */
    public static PrecogClient fromHeroku(String precogToken) {
    	byte[] data=DatatypeConverter.parseBase64Binary(precogToken);
        String decoded= new String(data);
        String[] values=decoded.split(":");
        if (values.length != 6) {
        	throw new IllegalArgumentException("Invalid Heroku token.");
        }
        
        URL service = fromHost(values[2]);
        String accountId = values[3];
        String apiKey = values[4];
        String rootPath = values[5];
        
        return new PrecogClient(service, apiKey, accountId, rootPath);
    }

    /**
     * Builds a new client to connect to precog services.
     * <p>
     * This constructor uses the Precog beta end-point (beta.precog.com). If
     * you wish to use a different end point, use
     * {@link #PrecogClient(URL, String, String, String)}.
     *
     * @param apiKey  api key to use
     * @param accountId the account to use as the data owner
     * @param basePath The base path to use for all requests
     * @see #PrecogClient(URL, String, String, String)
     * @see PrecogClient#BETA_HTTPS
     */
    public PrecogClient(String apiKey, String accountId, String basePath) {
    	this(BETA_HTTPS, apiKey, accountId, basePath, null);
    }

    /**
     * Builds a new client to connect to precog services. This will use the
     * {@code accountId} as the base path {@link #getBasePath()} for all queries.
     * If this is not desired, you must construct the {@link PrecogClient} with
     * a base path explicitly.
     * <p>
     * This constructor uses the Precog beta end-point (beta.precog.com). If
     * you wish to use a different end point, use
     * {@link #PrecogClient(URL, String, String)}.
     *
     * @param apiKey  api key to use
     * @param accountId the account to use as the data owner
     * @see #PrecogClient(URL, String, String)
     * @see PrecogClient#BETA_HTTPS
     */
    public PrecogClient(String apiKey, String accountId) {
    	this(BETA_HTTPS, apiKey, accountId, accountId, null);
    }

    /**
     * Builds a new client to connect to precog services. This attempts to
     * determine the data owner (on ingest/append/upload) from the API key.
     * This may fail and cause the related methods to throw an exception. If
     * this is not desired, then use a constructor that let's you use one of
     * the account IDs.
     * <p>
     * The base path in this case is assumed to be {@code "/"}.
     * <p>
     * This constructor uses the Precog beta end-point (beta.precog.com). If
     * you wish to use a different end point, use
     * {@link #PrecogClient(URL, String)}.
     *
     * @param apiKey  api key to use
     * @see #PrecogClient(URL, String)
     * @see PrecogClient#BETA_HTTPS
     */
    public PrecogClient(String apiKey) {
    	this(BETA_HTTPS, apiKey, null, null, null);
    }

    /**
     * Construct a new PrecogClient using the API key of the given account, the
     * account ID of the account as the data owner, and the account root path
     * as the base path.
     * <p>
     * This constructor uses the Precog beta end-point (beta.precog.com). If
     * you wish to use a different end point, use
     * {@link #PrecogClient(URL, AccountInfo)}.
     *
     * @param account the account to base this client off of
     * @see #PrecogClient(URL, AccountInfo)
     * @see PrecogClient#BETA_HTTPS
     */
    public PrecogClient(AccountInfo account) {
    	this(account.getApiKey(), account.getAccountId(), account.getRootPath());
    }
    
    
    // Getters.
    
    
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
     * Returns the account ID used as the owner of ingested data.
     */
    public String getAccountId() {
    	return accountId;
    }
    
    /**
     * Returns the base path that is prepended to all paths provided in methods.
     */
    public Path getBasePath() {
    	return basePath;
    }
    
    
    // ACCOUNTS
    

    /**
     * Creates a new account ID, accessible by the specified email address and
     * password, or returns the existing account ID. You <b>must</b> provide a
     * service that uses HTTPS to use this service, otherwise an
     * {@code IllegalArgumentException} will be thrown.
     *
     * @param service  the Precog service to use
     * @param email    user's email
     * @param password user's password
     * @return Json string with the account Id
     * @throws HttpException if the server sends an unexpected response
     * @throws IllegalArgumentException if the service is not secure (HTTPS)
     */
    public static AccountInfo createAccount(URL service, String email, String password)
    		throws HttpException {
        Request r = new RequestBuilder(Method.POST, Paths.ACCOUNTS.append("accounts/"))
        	.setBody("{ \"email\": \"" + email + "\", \"password\": \"" + password + "\" }")
        	.setHttpsRequired(true)
        	.build();
        Response response = Rest.execute(service, r);
        Gson gson = new Gson();
        AccountInfo account0 = gson.fromJson(response.asString(), AccountInfo.class);
        return PrecogClient.describeAccount(service, email, password, account0.getAccountId());
    }

    /**
     * Creates a new account ID, accessible by the specified email address and
     * password, or returns the existing account ID. This just calls
     * {@link #createAccount(URL,String,String)} with a default service of
     * {@link PrecogClient#BETA_HTTPS}.
     *
     * @param email    user's email
     * @param password user's password
     * @return Json string with the account Id
     * @throws HttpException if the server sends an unexpected response
     * @throws IllegalArgumentException if the service is not secure (HTTPS)
     */
    public static AccountInfo createAccount(String email, String password)
    		throws HttpException {
        return createAccount(PrecogClient.BETA_HTTPS, email, password);
    }

    /**
     * Retrieves the details about a particular account. This call is the
     * primary mechanism by which you can retrieve your master API key. You
     * <b>must</b> provide a service that uses HTTPS to use this service,
     * otherwise an {@code IllegalArgumentException} will be thrown.
     *
     * @param service  the Precog service (end-point) to use
     * @param email     user's email
     * @param password  user's password
     * @param accountId account's id number
     * @return account info
     * @throws HttpException if the server sends an unexpected response
     * @throws IllegalArgumentException if the service is not secure (HTTPS)
     */
    public static AccountInfo describeAccount(URL service, String email, String password, String accountId)
    		throws HttpException {
    	Request request = new RequestBuilder()
			.setPath(Paths.ACCOUNTS.append("accounts/" + accountId))
			.addBasicAuth(email, password)
			.setHttpsRequired(true)
			.build();
        String json = Rest.execute(service, request).asString();
        Gson gson = new Gson();
        return gson.fromJson(json, AccountInfo.class);
    }

    /**
     * Retrieves the details about a particular account. This call is the
     * primary mechanism by which you can retrieve your master API key.
     * This is equivalent to calling
     * {@link #describeAccount(URL,String,String,String)} with a default
     * service of {@link PrecogClient#BETA_HTTPS}.
     *
     * @param email     user's email
     * @param password  user's password
     * @param accountId account's id number
     * @return account info
     * @throws HttpException if the server sends an unexpected response
     * @throws IllegalArgumentException if the service is not secure (HTTPS)
     */
    public static AccountInfo describeAccount(String email, String password, String accountId)
    		throws HttpException {
        return describeAccount(PrecogClient.BETA_HTTPS, email, password, accountId);
    }
    
    
    // INGEST
    
    
    /**
     * Appends all the events in {@code contents}, a string whose {@link Format}
     * is described by {@code format}, to {@code path} in the virtual
     * file-system.
     * 
     * @param path the path in Precog to ingest the data into
     * @param contents the data to ingest
     * @param format the format of the data
     * @return the results of the ingest
     * @throws HttpException if the server sends an unexpected response
     */
    public AppendResult appendAllFromString(String path, String contents, Format format)
    		throws HttpException {
    	Request request = new RequestBuilder(ingestRequest(path, format))
    		.setBody(contents).build();
    	return gson.fromJson(rest.execute(request).asString(), AppendResult.class);
    }
    
    /**
     * Appends all the events in {@code contents}, a file whose {@link Format}
     * is described by {@code format}, to {@code path} in the virtual
     * file-system.
     * <p>
     * For instance, to ingest a CSV file, you could do something like:
     * <p>
     * <pre>
     * {@code
     * PrecogClient precog = new Precog(myApiKey, myAccountId);
     * File csvFile = new File("/path/to/my.csv");
     * precog.appendAllFromFile(file.getName(), file, CsvFormat.CSV);
     * }
     * </pre>
     * 
     * @param path the path in Precog to ingest the data into
     * @param file the data file to ingest
     * @param format the format of the data
     * @return the results of the ingest
     * @throws HttpException if the server sends an unexpected response
     */
    public AppendResult appendAllFromFile(String path, File file, Format format)
    		throws HttpException {
    	Request request = new RequestBuilder(ingestRequest(path, format))
    		.setBody(file).build();
    	return gson.fromJson(rest.execute(request).asString(), AppendResult.class);
    }

    /**
     * Appends all the events in {@code contents}, an {@code InputStream}
     * whose {@link Format} is described by {@code format}, to {@code path} in
     * the virtual file-system.
     * <p>
     * This will use chunked-encoding for the HTTP stream, so is suitable for
     * large {@code InputStream}s.
     * 
     * @param path the path in Precog to ingest the data into
     * @param in the data as an InputStream to ingest
     * @param format the format of the data
     * @return the results of the ingest
     * @throws HttpException if the server sends an unexpected response
     */
    public AppendResult appendAllFromInputStream(String path, InputStream in, Format format)
    		throws HttpException {
    	Request request = new RequestBuilder(ingestRequest(path, format))
    		.setBody(in).build();
    	return gson.fromJson(rest.execute(request).asString(), AppendResult.class);
    }
    	
    // Builds the bulk of an ingest request -- everything but the body.
    private Request ingestRequest(String path0, Format format) {
    	Path path = Paths.INGEST.append(buildStoragePath(new Path(path0)));
    	RequestBuilder rb = new RequestBuilder(Method.POST, path)
    		.addParam("apiKey", apiKey)
    		.addParam("mode", "batch")
    		.addParam("receipt", "true")
    		.setContentType(format.getContentType());
    	
    	final Request request = accountId == null
    			? rb.build()
    			: rb.addParam("ownerAccountId", accountId).build();
    	
    	return format.accept(new FormatVisitor<Request>() {
			public Request visitJsonFormat(JsonFormat format) {
				return request;
			}

			public Request visitCsvFormat(DelimitedFormat format) {
				return new RequestBuilder(request)
					.addParam("delimiter", "" + format.getDelimiter())
					.addParam("quote", "" + format.getQuote())
					.addParam("escape", "" + format.getEscape())
					.build();
			}
    	});
    }

    /**
     * Store the object {@code obj} as a record in Precog. It is serialized by
     * Gson using reflection. If a {@code Gson} object was provided during
     * construction, then it will be used, otherwise the default Gson
     * serialization will be used.
     * <p>
     * Note: Calling this method guarantees the object is stored in the Precog
     *       transaction log.
     * 
     * @param path The path in the virtual file system to store the record
     * @param obj The object to serialize to JSON and store in the VFS
     * @throws HttpException if there is a network error or unexpected results
     */
    public AppendResult append(String path, Object obj)
    		throws HttpException {
    	String json = gson.toJson(obj);
    	return appendAllFromString(path, json, JsonFormat.JSON_STREAM);
    }
    
    /**
     * Append a collection of records in Precog.
     * 
     * @param path the sub-path to store the records in
     * @param coll the collection of records to store
     * @throws HttpException if the server sends an unexpected response
     */
    public <T> AppendResult appendAll(String path, Iterable<T> coll)
    		throws HttpException {
    	InputStream in = new JsonStream(new JsonIterator<T>(gson, coll.iterator()));
    	return appendAllFromInputStream(path, in, JsonFormat.JSON_STREAM);
    }
    
    /**
     * Store the object {@code obj} as a record in Precog. It is serialized by
     * using {@link ToJson#serialize(Object)} on {@code toJson}.
     * <p>
     * Note: Calling this method guarantees the object is stored in the Precog
     *       transaction log.
     * 
     * @param path The path in the virtual file system to store the record
     * @param obj The object to serialize to JSON and store in the VFS
     * @throws HttpException if the server sends an unexpected response
     */
    public <T> AppendResult append(String path, T obj, ToJson<T> toJson)
    		throws HttpException {
    	String json = toJson.serialize(obj);
    	return appendAllFromString(path, json, JsonFormat.JSON_STREAM);
    }
    
    /**
     * Append a collection of records in Precog.
     * 
     * @param path the sub-path to store the records in
     * @param coll the collection of records to store
     * @throws HttpException if the server sends an unexpected response
     */
    public <T> AppendResult appendAll(String path, Iterable<T> coll, ToJson<T> toJson)
    		throws HttpException {
    	InputStream in = new JsonStream(new JsonIterator<T>(toJson, coll.iterator()));
    	return appendAllFromInputStream(path, in, JsonFormat.JSON_STREAM);
    }
    
    /**
     * Uploads the records in {@code file} to {@code path}. This is equivalent
     * to first <b>deleting the data</b> at the VFS path {@code path}, then
     * calling {@link #appendAllFromFile(String,File,Format)}.
     * 
     * @param path the path in Precog to upload the data to
     * @param file the data file to upload
     * @param format the format of the file's contents
     * @return the results of the data ingest
     * @throws HttpException if the server sends an unexpected response
     */
    public AppendResult uploadFile(String path, File file, Format format)
    		throws HttpException {
    	delete(path);
    	return appendAllFromFile(path, file, format);
    }

    /**
     * Builds a data storage path from a given path.
     *
     * @param path The path at which the record should be placed in the virtual file system.
     * @return full path
     */
    public Path buildStoragePath(Path path) {
        return FS.append(basePath).append(path);
    }
    
    /**
     * Deletes the data stored at the specified path. This does NOT do a
     * recursive delete. It'll only delete the data the path specified, all
     * other data in sub-paths of {@code path} will remain in-tact.
     *
     * @param path the path to delete data from
     * @throws HttpException if the server sends an unexpected response
     */
    public void delete(String path) throws HttpException {
    	Path path0 = Paths.INGEST.append(buildStoragePath(new Path(path)));
    	rest.execute(new RequestBuilder(Method.DELETE, path0)
    		.addParam("apiKey", apiKey).build());
    }
    
    
    // ANALYTICS
    

    /**
     * Executes a synchronous query relative to the specified base path. The
     * HTTP connection will remain open for as long as the query is evaluating
     * (potentially minutes).
     * <p>
     * Not recommended for long-running queries, because if the connection is
     * interrupted, there will be no way to retrieve the results of the query.
     *
     * @param path relative storage path to query
     * @param q    quirrel query to excecute
     * @return result as Json string
     * @throws HttpException if the server sends an unexpected response
     */
    public QueryResult query(String path, String q) throws HttpException {
    	Path path0 = Paths.ANALYTICS.append(buildStoragePath(new Path(path)));
        Request request = new RequestBuilder(path0)
        	.addParam("apiKey", apiKey)
        	.addParam("q", q)
        	.addParam("format", "detailed")
        	.build();
        String json = rest.execute(request).asString();
        QueryResult result = gson.fromJson(json, QueryResult.class);
        result.setGson(gson);
        return result;
    }
    
    /**
     * Executes a synchronous query relative to the specified base path. The
     * HTTP connection will remain open for as long as the query is evaluating
     * (potentially minutes).
     * <p>
     * Not recommended for long-running queries, because if the connection is
     * interrupted, there will be no way to retrieve the results of the query.
     *
     * @param q    quirrel query to excecute
     * @return result as Json string
     * @throws HttpException if the server sends an unexpected response
     */
    public QueryResult query(String q) throws HttpException {
    	return query("", q);
    }
    
    /**
     * Runs an asynchronous query against Precog. An async query is a query
     * that simply returns a Job ID, rather than the query results. You can
     * then periodically poll for the results of the job/query.
     * <p>
     * This does <b>NOT</b> run the query in a new thread. It will still block
     * the current thread until the server responds.
     * <p>
     * An example of using {@link #queryAsync(String)} to poll for results
     * could look like:
     * <p>
     * <pre>
     * {@code
     * PrecogClient precog = ...;
     * Query query = precog.queryAsync("foo/", "min(//bar)"); 
     * QueryResult result = null;
     * while (result == null) {
     *     result = precog.queryResults(query);
     * }
     * Double min = Double.valueOf(result.data.get(0));
     * println("Minimum is: " + min);
     * }
     * </pre>
     * <p>
     * This is ideal for long running queries.
     * 
     * @param path the base path to use in the query
     * @param q the query to execute
     * @return a Job ID that can be used with {@link #queryResults(Query)}
     * @throws HttpException if the server sends an unexpected response
     */
    public Query queryAsync(String path, String q) throws HttpException {
    	Path prefixPath = basePath.append(new Path(path).stripTrailingSlash());
    	Path path0 = Paths.ANALYTICS.append("queries");
    	Request request = new RequestBuilder(Method.POST, path0)
    		.addParam("apiKey", apiKey)
    		.addParam("q", q)
    		.addParam("prefixPath", prefixPath.toString())
    		.build();
    	String json = rest.execute(request).asString();
    	return gson.fromJson(json, Query.class);
    }
    
    /**
     * Runs an asynchronous query against Precog. An async query is a query
     * that simply returns a Job ID, rather than the query results. You can
     * then periodically poll for the results of the job/query.
     * 
     * @see PrecogClient#queryAsync(String, String)
     */
    public Query queryAsync(String q) throws HttpException {
    	return queryAsync("", q);
    }
    
    /**
     * This polls Precog for the completion of an async query. If the query
     * has completed, then a {@link QueryResult} object is returned. Otherwise,
     * {@code null} is returned.
     * 
     * @param query the query, as returned by {@link #queryAsync(String,String)}
     * @return the results if the query completed, {@code null} otherwise
     * @throws HttpException if the server sends an unexpected response
     */
    public QueryResult queryResults(Query query) throws HttpException {
    	Path path = Paths.ANALYTICS.append("queries/").append(query.getJobId());
        Request request = new RequestBuilder(path)
        	.addParam("apiKey", apiKey)
        	.build();
        String json = rest.execute(request).asString();
        if (json != null && json != "") {
        	QueryResult result = gson.fromJson(json, QueryResult.class);
        	result.setGson(gson);
        	return result;
        } else {
        	return null;
        }
    }
    
    /**
     * Downloads the results of a query to a file. This will block until the
     * query has completed and results are ready.
     * <p>
     * This will stream the results to the file, so it is suitable for working
     * with very large result sets.
     * <p>
     * If the query has any errors (or server errors), then {@code false} will
     * be returned. Otherwise, the query results will be downloaded to the File
     * {@code file} and {@code true} will be returned.
     * 
     * @param query the async query to download the results from
     * @param file the file to store the results in
     * @throws IOException if the file already exists or there is an error writing to file
     * @throws HttpException if there are any network problems or the server returns an unexpected result
     */
    public boolean downloadQueryResults(Query query, File file) throws IOException, HttpException {
    	Path path = Paths.ANALYTICS.append("queries/").append(query.getJobId());
        Request request = new RequestBuilder(path)
        	.addParam("apiKey", apiKey)
        	.build();
        
        Response response = rest.execute(request);
        try {
	        while (response.getStatusCode() == 202) {
	        	try {
	        		Thread.sleep(100);
	        		response.getData().close();
	        		response = rest.execute(request);
	        	} catch (InterruptedException ex) {
	        		Thread.currentThread().interrupt();
	        		logger.warning("Thread interrupted, cancelling download of query results.");
	        		return false;
	        	}
	        }
	        
	        if (response.getStatusCode() != 200) {
	        	throw HttpException.unexpectedResponse(response);
	        }
	    	
	    	FileWriter writer0 = new FileWriter(file);
	    	try {
		    	JsonWriter writer = new JsonWriter(writer0);
		    	
		    	writer.beginArray();
		    	
		    	Reader reader0 = new BufferedReader(new InputStreamReader(response.getData()));
		    	JsonReader reader = new JsonReader(reader0);
		    	reader.beginObject();
		    	while (reader.hasNext()) {
		    		String key = reader.nextName();
		    		if (key.equals("data")) {
		    			reader.beginArray();
		    			while (reader.peek() != JsonToken.END_ARRAY) {
		    				writeValue(writer, reader);
		    			}
		    			reader.endArray();
		    			
		    		} else if (key.equals("errors")) {
		    			reader.beginArray();
		    			if (reader.peek() != JsonToken.END_ARRAY) {
		    				return false;
		    			}
		    			reader.endArray();
		    			
		    		} else if (key.equals("serverErrors")) {
		    			reader.beginArray();
		    			if (reader.peek() != JsonToken.END_ARRAY) {
		    				return false;
		    			}
		    			reader.endArray();
		    			
		    		} else {
		    			reader.skipValue();
		    		}
		    	}
		    	reader.endObject();
		    	
		    	writer.endArray();
		    	
	    	} finally {
	    		writer0.close();
	    	}
        } finally {
        	response.getData().close();
        }
        
        return true;
    }
    
    // Pipes a single JSON value from reader -> writer.
    private static void writeValue(JsonWriter writer, JsonReader reader) throws IOException {
    	switch(reader.peek()) {
    		case BEGIN_ARRAY:
    			reader.beginArray();
    			writer.beginArray();
    			while (reader.peek() != JsonToken.END_ARRAY) {
    				writeValue(writer, reader);
    			}
    			writer.endArray();
    			reader.endArray();
    			break;
    			
    		case BEGIN_OBJECT:
    			reader.beginObject();
    			writer.beginObject();
    			while (reader.peek() != JsonToken.END_OBJECT) {
    				writer.name(reader.nextName());
    				writeValue(writer, reader);
    			}
    			writer.endObject();
    			reader.endObject();
    			break;
    			

    		case NULL:
    			reader.nextNull();
    			writer.nullValue();
    			break;
    			
    		case BOOLEAN:
    			writer.value(reader.nextBoolean());
    			break;
    			
    		case NUMBER:
    			try {
    				long value = reader.nextLong();
    				writer.value(value);
    			} catch (NumberFormatException nfe) {
    				double value = reader.nextDouble();
    				writer.value(value);
    			}
    			break;
    			
    		case STRING:
    			writer.value(reader.nextString());
    			break;
    			
    		default:
    			throw new IllegalStateException("Unexpected JSON token found: " + reader.peek());
    	}
    }
    
    private static class JsonIterator<T> implements Iterator<String> {
    	private ToJson<T> toJson;
    	private Iterator<T> iter;

		JsonIterator(ToJson<T> toJson, Iterator<T> iter) {
			this.toJson = toJson;
			this.iter = iter;
		}
		
		JsonIterator(final Gson gson, Iterator<T> iter) {
			this.toJson = new ToJson<T>() {
				public String serialize(T value) {
					return gson.toJson(value);
				}
			};
			this.iter = iter;
		}
		
		public boolean hasNext() {
			return iter.hasNext();
		}

		public String next() {
			return toJson.serialize(iter.next());
		}

		public void remove() {
			iter.remove();			
		}
    }
        
    /**
     * Given an {@code Iterator<T>}, this will create a new-line separated
     * input stream of the JSON serialization of each element in the iterator.
     */
    private static class JsonStream extends InputStream {
    	private static enum Mode {
    		SEPARATE, INSERT;
    	}
    	private static byte[] WS = new byte[] { 10 };
    	
    	private Iterator<String> iter;
    	
    	private Mode mode = Mode.INSERT;
    	private int offset = 0;
    	private byte[] chunk = new byte[0];
    	
    	JsonStream(Iterator<String> iter) {
    		this.iter = iter;
    	}
    	
    	private boolean isEmpty() throws IOException {
    		while (offset >= chunk.length && queueNext()) {
    			// Spin.
    		}
    		return offset >= chunk.length;
    	}
    	
    	private boolean queueNext() throws IOException {
    		if (iter.hasNext()) {
    			if (mode == Mode.SEPARATE) {
    				chunk = WS;
    			} else {
	    			String json = iter.next();
	    			chunk = json.getBytes("UTF-8");
    			}
    			offset = 0;
    			return true;
    		} else {
    			return false;
    		}
    	}

		public int read() throws IOException {
			if (isEmpty()) {
				return -1;
			} else {
				return chunk[offset++];
			}
		}
		
		@Override
		public int read(byte[] bytes) throws IOException {
			return read(bytes, 0, bytes.length);
		}
		
		@Override
		public int read(byte[] bytes, int off, int len) throws IOException {
			if (len == 0) {
				return 0;
			} else if (isEmpty()) {
				return -1;
			} else {
				int pos = off;
				while (len > 0 && !isEmpty()) {
					int bs = Math.min(chunk.length - offset, len);
					System.arraycopy(chunk, offset, bytes, pos, bs);
					offset += bs;
					pos += bs;
					len -= bs;
				}
				return pos - off;
			}
		}
    }
}
