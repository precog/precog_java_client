package com.precog.client;

import com.google.gson.annotations.SerializedName;
import com.precog.json.gson.RawJson;

public class TestData {
	public final int testInt;
    
	public final String testStr;
    
    @SerializedName("~raw")
    public final RawJson testRaw;


    public TestData(int testInt, String testStr, RawJson testRaw) {
        this.testInt = testInt;
        this.testStr = testStr;
        this.testRaw = testRaw;
    }


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + testInt;
		result = prime * result + ((testRaw == null) ? 0 : testRaw.hashCode());
		result = prime * result + ((testStr == null) ? 0 : testStr.hashCode());
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
		TestData other = (TestData) obj;
		if (testInt != other.testInt)
			return false;
		if (testRaw == null) {
			if (other.testRaw != null)
				return false;
		} else if (!testRaw.equals(other.testRaw))
			return false;
		if (testStr == null) {
			if (other.testStr != null)
				return false;
		} else if (!testStr.equals(other.testStr))
			return false;
		return true;
	}
}
