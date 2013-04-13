package com.precog.client;

/**
 * Result of data ingestion.
 *
 * @author Gabriel Claramunt <gabriel@precog.com>
 */
public class IngestResult {

	private String ingestId;
    private int total;
    private int ingested;
    private int failed;
    private int skipped;
    private String[] errors;

    /**
     * Returns the total number of records seen. This includes both records
     * were successfully ingested and those that failed.
     */
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    /** 
     * Returns the number of records that were ingested successfully.
     */
    public int getIngested() {
        return ingested;
    }

    public void setIngested(int ingested) {
        this.ingested = ingested;
    }

    /** Returns the number of records that failed to be ingested. */
    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    /** Returns the number of records that were skipped. */
    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    /**
     * Returns a list of errors returned by the server. Note that you can have
     * errors, but also have some records be successfully ingested.
     */
    public String[] getErrors() {
        return errors;
    }

    public void setErrors(String[] errors) {
        this.errors = errors;
    }

	public String getIngestId() {
		return ingestId;
	}

	public void setIngestId(String ingestId) {
		this.ingestId = ingestId;
	}
}
