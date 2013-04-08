package com.precog.api.options;

import com.precog.api.Request.ContentType;

import java.util.HashMap;
import java.util.Map;

/**
 * Optional parameters for ingest.
 *
 * @author Gabriel Claramunt <gabriel@precog.com>
 */
public class IngestOptions {

    public static String OWNER_ACCOUNT_ID = "ownerAccountId";
    public static String MODE = "mode";

    private ContentType dataType;
    private String ownerAccountId;
    private boolean async;
    private boolean batch;

    public IngestOptions(ContentType dataType) {
        this.dataType = dataType;
    }

    public Map<String, String> asMap() {
        Map<String, String> map = new HashMap<String, String>();
        if (ownerAccountId != null) {
            map.put(OWNER_ACCOUNT_ID, ownerAccountId);
        }
        if (batch) {
        	map.put(MODE, "batch");
        }
        return map;
    }

    public ContentType getDataType() {
        return dataType;
    }

    public String getOwnerAccountId() {
        return ownerAccountId;
    }

    public void setOwnerAccountId(String ownerAccountId) {
        this.ownerAccountId = ownerAccountId;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

	public boolean isBatch() {
		return batch;
	}

	public void setBatch(boolean batch) {
		this.batch = batch;
	}
}
