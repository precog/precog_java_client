package com.precog.client;

/**
 * User Account details.
 *
 * @author Gabriel Claramunt <gabriel@precog.com>
 * @author Tom Switzer <switzer@precog.com>
 */
public class AccountInfo {
    private String accountId;
    private String email;
    private String accountCreationDate;
    private String apiKey;
    private String rootPath;
    private AccountPlan plan;

    /**
     * Returns the account ID.
     *
     * The account ID is sometimes used in other requests, such as when
     * ingesting data, to specify the <i>owner</i> of the data being ingested.
     */
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /** Returns the e-mail associated with the account. */
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the date that the account was first created.
     *
     * TODO: This should be a Java Date. Account create date is returned as an
     * ISO8601 string.
     */
    public String getAccountCreationDate() {
        return accountCreationDate;
    }

    public void setAccountCreationDate(String accountCreationDate) {
        this.accountCreationDate = accountCreationDate;
    }

    /** Returns the API key associated with the account. */
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Returns the root path the account has ownership of. Typically, data is
     * ingested to sub-paths of the root path.
     */
    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    /**
     * Returns the plan the account is subscribed too. For example, if you are
     * using a free beta account, then this would be "Free".
     */
    public AccountPlan getPlan() {
        return plan;
    }

    public void setPlan(AccountPlan plan) {
        this.plan = plan;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((accountCreationDate == null) ? 0 : accountCreationDate
						.hashCode());
		result = prime * result
				+ ((accountId == null) ? 0 : accountId.hashCode());
		result = prime * result + ((apiKey == null) ? 0 : apiKey.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((plan == null) ? 0 : plan.hashCode());
		result = prime * result
				+ ((rootPath == null) ? 0 : rootPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccountInfo other = (AccountInfo) obj;
		if (accountCreationDate == null) {
			if (other.accountCreationDate != null)
				return false;
		} else if (!accountCreationDate.equals(other.accountCreationDate))
			return false;
		if (accountId == null) {
			if (other.accountId != null)
				return false;
		} else if (!accountId.equals(other.accountId))
			return false;
		if (apiKey == null) {
			if (other.apiKey != null)
				return false;
		} else if (!apiKey.equals(other.apiKey))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (plan == null) {
			if (other.plan != null)
				return false;
		} else if (!plan.equals(other.plan))
			return false;
		if (rootPath == null) {
			if (other.rootPath != null)
				return false;
		} else if (!rootPath.equals(other.rootPath))
			return false;
		return true;
	}
}
