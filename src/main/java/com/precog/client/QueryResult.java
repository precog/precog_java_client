package com.precog.client;

import java.util.Collections;
import java.util.List;

/**
 * Result of running a query.
 * 
 * If {@link failed()} is {@code true}, then you should assume that the data
 * is invalid (though the partial result may still be of interest to you). A
 * result has failed if either {@link getErrors()} is non-empty or
 * {@link getServerErrors()} is non-empty.
 *
 * @author Tom Switzer <switzer@precog.com>
 */
public class QueryResult {

    private List<String> data;
    private List<MessageReport> errors = Collections.emptyList();
    private List<MessageReport> warnings = Collections.emptyList();
    private List<String> serverErrors = Collections.emptyList();
    
    public QueryResult() {
    }
    
    public QueryResult(List<String> data) {
    	this.data = data;
    }

	public QueryResult(List<String> data, List<MessageReport> errors,
			List<MessageReport> warnings, List<String> serverErrors) {
		super();
		this.data = data;
		this.errors = errors;
		this.warnings = warnings;
		this.serverErrors = serverErrors;
	}

    public boolean failed() {
      return errors.size() > 0 || serverErrors.size() > 0;
    }

    public boolean succeeded() {
      return !failed();
    }

	public List<String> getData() {
		return data;
	}

	public void setData(List<String> data) {
		this.data = data;
	}

	public List<MessageReport> getErrors() {
		return errors;
	}

	public void setErrors(List<MessageReport> errors) {
		this.errors = errors;
	}

	public List<MessageReport> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<MessageReport> warnings) {
		this.warnings = warnings;
	}

	public List<String> getServerErrors() {
		return serverErrors;
	}

	public void setServerErrors(List<String> serverErrors) {
		this.serverErrors = serverErrors;
	}
}
