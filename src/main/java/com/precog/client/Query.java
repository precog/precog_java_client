package com.precog.client;

/**
 * A representation of a running, asynchronous query. It wraps a job ID and
 * can be used to poll Precog to see if the query has completed, retrieve those
 * results or store them in a file on disk.
 * 
 * @see PrecogClient#queryAsync(String, String)
 * @see PrecogClient#queryResults(Query)
 * @see PrecogClient#downloadQueryResults(Query, java.io.File)
 * 
 * @author Tom Switzer <switzer@precog.com>
 */
public class Query {
	private String jobId;

	public Query(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * Returns the job ID of a running query.
	 */
	public String getJobId() {
		return jobId;
	}
}