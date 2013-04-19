package com.precog.client;

/**
 * User account plan. This defines the type of plan a user is currently
 * subscribed to. For instance, if you signed up for a beta account, then your
 * AccountPlan would like be {@link #FREE}.
 *
 * @author Tom Switzer <switzer@precog.com>
 * 
 * @see AccountInfo#getPlan()
 */
public class AccountPlan {
	public static AccountPlan FREE = new AccountPlan("Free");


    private String type;

    public AccountPlan(String type) {
        this.setType(type);
    }

    /**
     * Returns the plan type (eg. "Free").
     */
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		AccountPlan other = (AccountPlan) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
