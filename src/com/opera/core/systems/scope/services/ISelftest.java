package com.opera.core.systems.scope.services;

public interface ISelftest {
	/**
	 * Run the selftest for the specified modules
	 * 
	 * @param modules The names of the modules to test
	 * @return The test output
	 */
	/* XXX: Might be a better idea to return some kind of structured object 
	 * here, instead of a bare string. First order of business is getting this
	 * damn thing working though.
	 */
	public void runSelftests(String... modules);
}
