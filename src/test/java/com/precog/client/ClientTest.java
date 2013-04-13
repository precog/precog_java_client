package com.precog.client;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import com.precog.client.AccountInfo;
import com.precog.client.CsvFormat;
import com.precog.client.IngestResult;
import com.precog.client.JsonFormat;
import com.precog.client.PrecogClient;
import com.precog.client.QueryResult;
import com.precog.client.rest.Path;
import com.precog.json.RawStringToJson;
import com.precog.json.ToJson;
import com.precog.json.gson.GsonFromJson;
import com.precog.json.gson.GsonToJson;
import com.precog.json.gson.RawJson;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Unit test for basic client.
 */
public class ClientTest {
    
    private static String generateEmail() {
    	return "java-test-" + UUID.randomUUID().toString() + "@precog.com";
    }
    
    private static Path generatePath() {
    	return new Path(UUID.randomUUID().toString());
    }

    public static String email = generateEmail();
    public static String password = "password";
    public static String testAccountId;
    public static String testApiKey;
    public static PrecogClient testClient;

    private static class TestData {
        public final int testInt;
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

        String result = PrecogClient.createAccount(svc, email, password);
        AccountInfo res = GsonFromJson.of(new TypeToken<AccountInfo>() {
        }).deserialize(result);
        testAccountId = res.getAccountId();

        result = PrecogClient.describeAccount(svc, email, password, testAccountId);
        res = GsonFromJson.of(new TypeToken<AccountInfo>() {
        }).deserialize(result);
        testApiKey = res.getApiKey();

        testClient = new PrecogClient(svc, testApiKey, testAccountId);
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

    @Test
    public void testStore() throws IOException {
        RawJson testJson = new RawJson("{\"test\":[{\"v\": 1}, {\"v\": 2}]}");
        TestData testData = new TestData(42, "Hello\" World", testJson);
        testClient.append("/test/", testData);
    }

    @Test
    public void testStoreStrToJson() throws IOException {
        ToJson<String> toJson = new RawStringToJson();
        String data = "{\"test\":[{\"v\": 1}, {\"v\": 2}]}";
        testClient.append("/test/", data, toJson);
    }

    @Test
    public void testStoreRawJsonString() throws IOException {
        String rawJson = "{\"test\":[{\"v\": 1}, {\"v\": 2}]}";
        testClient.appendAllFromString("/test/", rawJson, JsonFormat.JSON_STREAM);
    }

    @Test
    public void testStoreRawUTF8() throws IOException {
        String rawJson = "{\"test\":[{\"������������������������������\": 1}, {\"v\": 2}]}";
        testClient.appendAllFromString("/test/", rawJson, JsonFormat.JSON_STREAM);
    }

    @Test
    public void testIngestCSV() throws IOException {
    	String csv = "a,b,c\n1,2,3\n\n";
        IngestResult result = testClient.appendAllFromString("/test/", csv, CsvFormat.CSV);
        assertEquals(2, result.getIngested());
    }

    @Test
    public void testRawJson() throws IOException {
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
    public void testAppendAllFromCollection() throws IOException {
    	ArrayList<TestData> ts = new ArrayList<TestData>();
    	ts.add(new TestData(1, "asdf", new RawJson("asdf")));
    	ts.add(new TestData(2, "qwerty", new RawJson("[1,2,3]")));
    	ts.add(new TestData(3, "zxcv", new RawJson("1111")));
    	testClient.appendAll("/test/appendAll", ts);
    	
    	int count = 0;
    	int i = 10;
    	while (count == 0 && i > 0) {
    		try {
    			Thread.sleep(500);
    		} catch (InterruptedException ex) {
    			// Don't really care...
    		}
    		QueryResult result = testClient.query("/test/", "count(//appendAll)");
    		count = Integer.valueOf(result.getData().get(0));
    		i -= 1;
    	}
    	assertEquals(3, count);
    }

    @Test
    public void testOddCharacters() throws IOException {
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
    public void testCreateAccount() throws IOException {
        String result = PrecogClient.createAccount(getService(), generateEmail(), password);
        assertNotNull(result);
        AccountInfo res = GsonFromJson.of(new TypeToken<AccountInfo>() {
        }).deserialize(result);
        String accountId = res.getAccountId();
        assertNotNull(accountId);
        assertNotSame(testAccountId, accountId);
    }

    @Test
    public void testDescribeAccount() throws IOException {
        String result = PrecogClient.describeAccount(testClient.getService(), email, password, testAccountId);
        assertNotNull(result);
        AccountInfo res = GsonFromJson.of(new TypeToken<AccountInfo>() {
        }).deserialize(result);
        assertEquals(testAccountId, res.getAccountId());
    }

    @Test
    public void testQuery() throws IOException {
    	QueryResult result = testClient.query("", "count(//non-existant)");
        assertNotNull(result);
        assertEquals("0", result.getData().get(0));
    }

    @Test
    public void testFromHeroku() throws UnsupportedEncodingException {
        String user="user";
        String password="password";
        String host= "beta.host.com";
        String accountId="12345";
        String apiKey="AAAAA-BBBBB-CCCCCC-DDDDD";
        String rootPath ="/00001234/";
        String values=user+":"+password+":"+host+":"+accountId+":"+apiKey+":"+ rootPath;
        String token= DatatypeConverter.printBase64Binary(values.getBytes("UTF-8"));
        PrecogClient precogApi=PrecogClient.fromHeroku(token);
        assertNotNull(precogApi);
    }

}
