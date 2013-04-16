package com.precog.client;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.precog.client.text.TextTag;

/**
 * Result of the execution of a query (result set, errors, warnings, etc).
 * 
 * If {@link #failed()} is {@code true}, then you should assume that the data
 * is invalid (though the partial result may still be of interest to you). A
 * result has failed if either {@link #getErrors()} is non-empty or
 * {@link #getServerErrors()} is non-empty.
 *
 * @author Tom Switzer <switzer@precog.com>
 */
public class QueryResult implements Iterable<String> {

	private Gson gson;
    private List<String> data;
    private List<TextTag> errors = Collections.emptyList();
    private List<TextTag> warnings = Collections.emptyList();
    private List<String> serverErrors = Collections.emptyList();
    
    QueryResult() {
    }
    
	QueryResult(Gson gson, List<String> data, List<TextTag> errors,
			List<TextTag> warnings, List<String> serverErrors) {
		this.data = data;
		this.errors = errors;
		this.warnings = warnings;
		this.serverErrors = serverErrors;
	}

	/**
	 * Returns {@code true} if the query did not execute successfully. This
	 * means that either there was an error with query itself (so
	 * {@link #getErrors()} is non-empty), or there was a server error while
	 * executing the query (so {@link #getServerErrors()} is non-empty).
	 */
    public boolean failed() {
      return errors.size() > 0 || serverErrors.size() > 0;
    }

    /**
     * Returns {@code true} if the query executed successfully, otherwise
     * returns {@code false}. 
     */
    public boolean succeeded() {
      return !failed();
    }
    
    void setGson(Gson gson) {
    	this.gson = gson;
    }

    /**
     * Returns the list of errors found in the query. If there are no errors,
     * then an empty list is returned.
     */
	public List<TextTag> getErrors() {
		return errors;
	}

	/**
	 * Returns the list of warnings for the query. If there are no warnings,
	 * then an empty list is returned.
	 */
	public List<TextTag> getWarnings() {
		return warnings;
	}

	/**
	 * Returns the list of server errors encountered while executing the query.
	 * If there are no errors, then an empty list is returned. If the list is
	 * non-empty, then that means there was a server error.
	 */
	public List<String> getServerErrors() {
		return serverErrors;
	}
	
	/**
	 * Returns the result in position {@code index}, deserializing it (using
	 * reflection) to the class of {@code klass}.
	 * 
	 * @param index the index of the result in the set of results
	 * @param klass the Class to deserialize the result to
	 * @return an instance of {@code T}
	 */
	public <T> T get(int index, Class<T> klass) {
		return gson.fromJson(get(index), klass);
	}

	/**
	 * Returns the result at position {@code index} in the result set.
	 * 
	 * @param index the index of the result in the set of results
	 * @return the result as JSON
	 */
	public String get(int index) {
		return this.data.get(index);
	}

	/** Returns the number of results returned by the query. */
	public int size() {
		return this.data.size();
	}

	/** Returns an iterator over the JSON result set. */
	public Iterator<String> iterator() {
		return data.iterator();
	}
}
