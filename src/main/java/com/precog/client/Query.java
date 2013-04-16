package com.precog.client;

/**
 * This represents an asynchronous query. It wraps a job ID and can be used to
 * poll Precog to see if the query has completed.
 * 
 * @see PrecogClient#queryAsync(String, String)
 * @see PrecogClient#queryResults(Query)
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

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
}