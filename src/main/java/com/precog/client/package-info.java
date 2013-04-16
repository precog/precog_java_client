/**
 * A Java client for working with Precog. This main class in this package is
 * {@link com.precog.client.PrecogClient}, which provides a convenient way to:
 * <p>
 * <ul>
 *  <li>Append records/events to a path in Precog
 *  <li>Upload files (CSV, JSON, etc.) to Precog
 *  <li>Run quirrel queries against your data
 * </ul>
 * <p>
 * Here is a short example, demonstrating the use of {@link com.precog.client.PrecogClient}
 * to insert and query data in Precog.
 * <p>
 * <pre>
 * {@code
 * 
 * import com.precog.client.*
 * 
 * class Example {
 *     static class Person {
 *         String name;
 *         int age;
 *     
 *         public Person(String name, int age) {
 *         	   this.name = name;
 *             this.age = age;
 *         }
 *     }
 * 
 *     public static void main(String[] args) {
 *     
 *         // Create a new account. You would probably use an existing account.
 *         
 *         AccountInfo account = PrecogClient.createAccount("bob@example.com", "password");
 *         
 *         // Construct a PrecogClient from an account. This configures the client
 *         // to use the account's API key to access Precog, the account ID as the
 *         // owner of data that is uplaoded/appended, annd the account's root path
 *         // as the base-path of all queries & uploads/appends.
 *           
 *         PrecogClient client = new PrecogClient(account);
 *         
 *         // We create some sample data to upload. The appendAll method let's us
 *         // append lists, sets, etc. of arbitrary Java objects. These are serialized
 *         // using reflection (with Google's GSON library). In this case, a Person
 *         // will be serialized to { "name": "...", "age": # }.
 *         
 *         List<Person> people = Arrays.asList(
 *                 new Person("Tom", 42),
 *                 new Person("Joanne", 73),
 *                 new Person("George", 22));
 *         IngestResult ingest = client.appendAll("people/", people);
 *         
 *         // Whenever we add data to Precog, we get back an IngestResult. This
 *         // provides access to not only the data (the rows of JSON returned), but
 *         // also statistics on the ingest (such as # of rows successfully ingested)
 *         // and any errors that may have occured while trying to ingest the data.
 *         // Here, we give up if we encountered any errors. This may happen if the
 *         // JSON isn't valid, for instance.
 * 
 *         if (!ingest.isSuccess()) {
 *             System.out.println("Failed to add all people:");
 *             for (String error : ingest.getErrors()) {
 *                 System.out.println("\t" + error);
 *             }
 *             System.exit(1);
 *         }
 *         
 *         // Data isn't guaranteed to be immediately visible in queries after it
 *         // has been ingested. It will eventually be made available.
 *         // Nonetheless, let's run some queries against our people data set.
 *         
 *         QueryResult result = client.query("count(//people)");
 *         System.out.println("# of people: " + result.get(0, Long.class));
 *         
 *         // We can also run what we call an asynchronous query. What this means
 *         // is that instead of waiting for the results of the query, Precog will
 *         // run the query in the background and respond to the client with a query
 *         // ID that can be used to determine the status and get the results of the
 *         // query when it completes. Here we will run an async query, then repeatedly
 *         // poll Precog for the results in a while loop.
 *         
 *         Query query = client.queryAsync("mean((//people).age)");
 *         result = null;
 *         while (result == null) {
 *         	   result = client.queryResults(query);
 *         }
 *         System.out.println("Mean age: " + result.get(0, Double.class));
 *     }
 * }
 * 
 * }
 * </pre>
 */
package com.precog.client;
