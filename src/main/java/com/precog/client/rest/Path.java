package com.precog.client.rest;

/**
 * A simple path representation that conforms to the path syntax
 * of the Precog service API. 
 *
 * @author Kris Nuttycombe <kris@precog.com>
 * @author Tom Switzer <switzer@precog.com>
 */
public class Path {
    protected final String path;

    /**
     * Create a new path from the specified string. This path should
     * consist of valid identifiers (alphanumeric strings) delimited
     * by the "/" character.
     * @param path 
     */
    public Path(String path) {
        this.path = path.replaceAll("/+", "/");
    }

    /**
     * Get the value of path.
     *
     * @return the value of path
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Remove the trailing slash from the path, if it has one.
     */
    public Path stripTrailingSlash() {
    	return new Path(path.replaceAll("/$", ""));
    }

    /**
     * Append {@code that} path to this one.
     *
     * @return the concatenation of the 2 paths
     */
    public Path append(Path that) {
        return new Path(path + "/" + that.path);
    }
    
    /**
     * Append {@code that} path to this one. This is equivalent to calling
     * {@code path.append(new Path(that))}.
     *
     * @return the concatenation of the 2 paths
     */
    public Path append(String that) {
    	return this.append(new Path(that));
    }

    /**
     * Return the prefix of this path. May return null if the path is only
     * one element long.
     */
    public Path getPrefix() {
        String[] components = path.split("/");
        if (components.length > 1) {
            StringBuilder prefix = new StringBuilder();
            for (int i = 0; i < components.length - 1; i++) prefix.append(components[i]).append("/");
            return new Path(prefix.toString());
        } else {
            return null;
        }
    }

    public boolean isAbsolute() {
    	return !isRelative();
    }
    
    public boolean isRelative() {
    	return path.length() == 0 || path.charAt(0) == '/';
    }
  
    /** Convert the path to a relative path. */
    public Path relativize() {
    	if (path.length() > 0 && path.charAt(0) == '/') {
    		return new Path(path.substring(1));
    	} else {
    		return this;
    	}
    }
    
    /** Convert the path to an absolute path. */
    public Path absolutize() {
    	if (path.length() > 0 && path.charAt(0) == '/') {
    		return this;
    	} else {
    		return new Path("/" + path);
    	}
    }
  
    public String toString() {
        return path;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
		Path other = (Path) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}
}
