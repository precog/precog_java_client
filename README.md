Precog Client
=============

A Java client for working with Precog's REST API.

Getting Started
---------------

The easiest way to use Precog's Java client is by adding it as a dependency in
your Maven project:

    <dependencies>
      ...
      <dependency>
        <groupId>com.precog</groupId>
        <artifactId>precog-java-client</artifactId>
        <version>1.0.0</version>
      </dependency>
      ...
    </dependencies>

### Quick Start

A good first step is simply to create a PrecogClient instance. This is the main
class through which we can access Precog.

    PrecogClient precog = new PrecogClient(myApiKey, myAccountId);

We don't specify an *end-point* to use here. By default, the Precog client
will use the Precog Beta service as its end-point. If you are using one of our
production offerings, then you'll need to provide the appropriate end-point.
If you signed up for a Beta account, then just replace `myApiKey` and
`myAccountId` with your Precog API key and account ID respectively.

Now we'll want to store some data in Precog. Let's say we have a CSV file,
`my-data.csv`, that looks like this:

    Order ID,Customer ID,Product ID,Quantity,Price,TaxRate
    1234,5678,9101112,2,3.99,0.13
    ...

We'll upload the file to Precog to `my-data`.

    File csv = new File("my-data.csv");
    precog.uploadFile("my-data", csv, Formats.CSV);

Data uploaded/stored in Precog isn't immediately available. Instead, we
guarantee that your data will eventually be made available. Usually this is
nearly instantaneous, however it is something to keep in mind.

So, now we want to query the data. Let's look at some example queries.

Say we want to calculate the total sales of all our data, including tax.
We formulate this as a Quirrel query, then execute it with our Precog client.

    String totals =
        "data := //my-data                                        \n" +
        "data with {                                              \n" +
          "total: data.Quantity * data.Price * (1 + data.TaxRate) \n" +
        "}";
    
    QueryResult result = precog.query(totals);

Query results are stored in a `QueryResult` object. The object itself is an
`Iterable<String>`. Each element of the result is simply the JSON encoding of
the result.

    for (String json : result) {
        System.out.println(json);
    }

Although we print the results out, we don't actually know for sure that the
query succeeded. So, we probably want to check. Quirrel errors (eg. syntax
errors) are reported as `TextTag`s. This is a simple class that let's us
accurately identify the exact location of the error in the query. So, errors
and warnings are reported as a list of `TextTag`s.

    if (result.failed()) {
        System.out.println("Query failed!");
        for (TextTag tag : result.getErrors()) {
            System.out.println("Error: " + tag.toString());
        }
    }

    for (TextTag tag : result.getWarnings()) {
        System.out.println("Warning: " + tag.toString());
    }

The PrecogClient also let's submit Query's for execution, without actually
requiring the results right away. This is used for long-running queries, so
we don't have to wait around for the results. Precog calls these *async*
queries. When an async query is run, a `Query` object is returned. This is a
handle that let's us periodically poll Precog to see if the query has finished.

Let's use async queries to find our best customer.

    String bestCustomer =
       "salesByCustomer := solve 'customer                   \n" +
       "  { customer: 'customer,                             \n" +
       "    sales: sum(order.Quantity * order.Price) }       \n" +
       "bestCustomer := salesByCustomer where                \n" +
       "  salesByCustomer.sales = max(salesByCustomer.sales) \n" +
       "bestCustomer";

   Query query = precog.asyncQuery(bestCustomer);

Now that we have a handle on the results, we can do 1 of 2 things. We can get
the results directly and store them in memory, using `queryResults`. In this
case, if the results aren't ready yet, then `queryResults` returns `null`. So,
that indicates we need to wait a bit and try again. However, we can also just
store the results directly into a file on disk using `downloadQueryResults`.
In this case, we will block until the results are ready, then feed them to a
file on disk. This is useful for queries where we expect large results, as we
don't need to store everything in memory.

    File outFile = new File("best-customer.json");
    assert(precog.downloadQueryResults(query, outFile) == true);

License
-------

Copyright 2013 Reportgrid, Inc.

Licensed under the MIT License: [http://opensource.org/licenses/MIT]
