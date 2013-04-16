package com.precog.client;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import com.precog.client.AccountInfo;
import com.precog.client.CsvFormat;
import com.precog.client.IngestResult;
import com.precog.client.JsonFormat;
import com.precog.client.PrecogClient;
import com.precog.client.QueryResult;
import com.precog.client.rest.HttpException;
import com.precog.client.rest.Path;
import com.precog.json.RawStringToJson;
import com.precog.json.ToJson;
import com.precog.json.gson.GsonFromJson;
import com.precog.json.gson.GsonToJson;
import com.precog.json.gson.RawJson;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Unit test for basic client.
 */
public class ClientTest {

	// Maximum # of times to poll for successful ingests.
    private static final int MAX_TRIES = 25;
    
    private static String generateEmail() {
    	return "java-test-" + UUID.randomUUID().toString() + "@precog.com";
    }
    
    private static Path generatePath() {
    	return new Path("/test/" + UUID.randomUUID().toString());
    }

    public static String email = generateEmail();
    public static String password = "password";
    public static String testAccountId;
    public static String testApiKey;
    public static PrecogClient client;
    public static Gson gson = new Gson();

    private static class TestData {
        @SuppressWarnings("unused")
		public final int testInt;
        
        @SuppressWarnings("unused")
		public final String testStr;
        
        @SerializedName("~raw")
        public final RawJson testRaw;


        public TestData(int testInt, String testStr, RawJson testRaw) {
            this.testInt = testInt;
            this.testStr = testStr;
            this.testRaw = testRaw;
        }
    }

    @BeforeClass
    public static void beforeAll() throws Exception {
        URL svc = getService();
        AccountInfo account = PrecogClient.createAccount(svc, email, password);
        testAccountId = account.getAccountId();
        testApiKey = account.getApiKey();
        client = new PrecogClient(svc, account);
    }

    /**
     * Builds the service under test.
     * Defaults to devapi.precog.com but a different one can be specified by setting the Java property "host"
     * (to allow run the unit tests against a different server)
     * @return  Service
     */
    private static URL getService() {
        String host = System.getProperty("host");
        if (host == null) {
        	return PrecogClient.DEV_HTTPS;
        } else {
        	return PrecogClient.fromHost(host);
        }
    }
    
    public void expectCount(Path path0, int countExpected) throws IOException, HttpException {
    	String path = path0.relativize().toString();
    	int count = 0;
    	int tries = 0;
    	while (count != countExpected && tries < MAX_TRIES) {
    		if (tries == 0) {
	    		try {
	    			Thread.sleep(500);
	    		} catch (InterruptedException ex) {
	    			// Meh.
	    		}
    		}
    		
    		tries += 1;
    		QueryResult result = client.query("", "count(//" + path + ")");
    		count = Integer.valueOf(result.getData().get(0));
    	}
    	assertEquals(countExpected, count);
    }

    @Test
    public void testAppend() throws IOException, HttpException {
    	Path path = generatePath();
        RawJson testJson = new RawJson("{\"test\":[{\"v\": 1}, {\"v\": 2}]}");
        TestData testData = new TestData(42, "Hello\" World", testJson);
        IngestResult result = client.append(path.toString(), testData);
        assertEquals(1, result.getIngested());
        expectCount(path, 1);
    }

    @Test
    public void testAppendWithToJson() throws IOException, HttpException {
        ToJson<String> toJson = new RawStringToJson();
        Path path = generatePath();
        String data = "{\"test\":[{\"v\": 1}, {\"v\": 2}]}";
        IngestResult result = client.append(path.toString(), data, toJson);
        assertEquals(1, result.getIngested());
        expectCount(path, 1);
        QueryResult qresult = client.query("count(//" + path.relativize() + ")");
        assertFalse(qresult.getData().get(0).startsWith("\""));
    }

    @Test
    public void testAppendAllFromStringWithJsonStream() throws IOException, HttpException {
    	Path path = generatePath();
        String rawJson = "{\"test\":[{\"v\": 1}, {\"v\": 2}]} {\"test\":[{\"v\": 2}, {\"v\": 3}]}";
        IngestResult result = client.appendAllFromString(path.toString(), rawJson, JsonFormat.JSON_STREAM);
        assertEquals(2, result.getIngested());
        expectCount(path, 2);
    }

    @Test
    public void testAppendAllFromStringWithJsonArray() throws IOException, HttpException {
    	Path path = generatePath();
        String rawJson = "[{\"test\":[{\"v\": 1}, {\"v\": 2}]},{\"test\":[{\"v\": 2}, {\"v\": 3}]}]";
        IngestResult result = client.appendAllFromString(path.toString(), rawJson, JsonFormat.JSON);
        assertEquals(2, result.getIngested());
        expectCount(path, 2);
    }

    @Test
    public void testAppendAllFromStringWithCSV() throws IOException, HttpException {
    	Path path = generatePath();
    	String csv = "a,b,c\n1,2,3\n\n,,tom\n\n";
        IngestResult result = client.appendAllFromString(path.toString(), csv, CsvFormat.CSV);
        assertEquals(4, result.getIngested());
        expectCount(path, 4);
    }

    @Test
    public void testAppendRawUTF8() throws IOException, HttpException {
    	Path path = generatePath();
        String rawJson = "{\"test\":[{\"������������������������������\": 1}, {\"v\": 2}]}";
        client.appendAllFromString(path.toString(), rawJson, JsonFormat.JSON_STREAM);
        expectCount(path, 1);
    }

    @Test
    public void testRawJson() throws IOException, HttpException {
        ToJson<Object> toJson = new GsonToJson();

        String testString = "{\"test\":[{\"v\":1},{\"v\":2}]}";
        RawJson testJson = new RawJson(testString);
        TestData testData = new TestData(42, "Hello\" World", testJson);

        String expected = new StringBuilder("{")
                .append("\"testInt\":").append(42).append(",")
                .append("\"testStr\":\"Hello\\\" World\",")
                .append("\"~raw\":").append(testString)
                .append("}")
                .toString();

        assertEquals(expected, toJson.serialize(testData));
    }
    
    @Test
    public void testAppendAllFromCollection() throws IOException, HttpException {
    	Path path = generatePath();
    	ArrayList<TestData> ts = new ArrayList<TestData>();
    	ts.add(new TestData(1, "asdf", new RawJson("asdf")));
    	ts.add(new TestData(2, "qwerty", new RawJson("[1,2,3]")));
    	ts.add(new TestData(3, "zxcv", new RawJson("1111")));
    	IngestResult result = client.appendAll(path.toString(), ts);
    	assertEquals(3, result.getIngested());
    	expectCount(path, 3);
    }
    
    @Test
    public void testDelete() throws IOException, HttpException {
    	Path path = generatePath();
    	TestData data = new TestData(1, "abc", new RawJson("[1,2,3]"));
    	IngestResult result = client.append(path.toString(), data);
    	assertEquals(1, result.getIngested());
    	expectCount(path, 1);
    	client.delete(path.toString());
    	expectCount(path, 0);
    }

    @Test
    public void testOddCharacters() throws IOException, HttpException {
        ToJson<Object> toJson = new GsonToJson();
        TestData testData = new TestData(1, "���", new RawJson(""));

        String expected = new StringBuilder("{")
                .append("\"testInt\":").append(1).append(",")
                .append("\"testStr\":\"���\"")
                .append("}")
                .toString();

        String result = toJson.serialize(testData);

        assertEquals(expected, result);
    }


    @Test
    public void testCreateAccount() throws IOException, HttpException {
    	String email = generateEmail();
        AccountInfo account = PrecogClient.createAccount(getService(), email, password);
        assertNotNull(account);
        assertNotSame(testAccountId, account.getAccountId());
        assertNotNull(account.getApiKey());
        assertEquals(email, account.getEmail());
    }

    @Test
    public void testDescribeAccount() throws IOException, HttpException {
    	String email = generateEmail();
        AccountInfo account0 = PrecogClient.createAccount(getService(), email, password);
        AccountInfo account1 = PrecogClient.describeAccount(getService(), email, password, account0.getAccountId());
        assertEquals(account0, account1);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testCreateAccountRequiresHttps() throws IOException, HttpException {
    	String email = generateEmail();
    	URL url0 = getService();
    	URL url = new URL("http", url0.getHost(), url0.getPort(), url0.getPath());
    	PrecogClient.createAccount(url, email, password);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testDescribeAccountRequiresHttps() throws IOException, HttpException {
    	String email = generateEmail();
    	URL url0 = getService();
    	URL url = new URL("http", url0.getHost(), url0.getPort(), url0.getPath());
    	AccountInfo account = PrecogClient.createAccount(url0, email, password);
    	PrecogClient.describeAccount(url, email, password, account.getAccountId());
    }

    @Test
    public void testQuery() throws IOException, HttpException {
    	QueryResult result = client.query("", "count(//non-existant)");
        assertNotNull(result);
        assertEquals("0", result.getData().get(0));
    }
    
    @Test
    public void testQueryAsync() throws IOException, HttpException {
    	Path path = generatePath();
    	List<TestData> data = Arrays.asList(
    			new TestData(1, "", null),
    			new TestData(2, "", null),
    			new TestData(3, "", null));
    	client.appendAll(path.toString(), data);
    	expectCount(path, 3);
    	Query query = client.queryAsync("max((//" + path.relativize() + ").testInt)");
    	QueryResult result = null;
    	while (result == null) {
    		result = client.queryResults(query);
    	}
    	double max = Double.valueOf(result.getData().get(0));
    	assertEquals(3.0, max, 0.0);
    }

    @Test
    public void testFromHeroku() throws UnsupportedEncodingException {
        String user = "user";
        String password = "password";
        String host = "beta.host.com";
        String accountId = "12345";
        String apiKey = "AAAAA-BBBBB-CCCCCC-DDDDD";
        String rootPath = "/00001234/";
        String values = user+":"+password+":"+host+":"+accountId+":"+apiKey+":"+ rootPath;
        String token = DatatypeConverter.printBase64Binary(values.getBytes("UTF-8"));
        
        PrecogClient client0 = PrecogClient.fromHeroku(token);
        
        assertTrue(client0.getService().toString().contains(host));
        assertEquals(accountId, client0.getAccountId());
        assertEquals(apiKey, client0.getApiKey());
        assertEquals(new Path(rootPath), client0.getBasePath());
    }
}
