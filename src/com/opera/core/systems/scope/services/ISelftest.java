package com.opera.core.systems.scope.services;

public interface ISelftest {
	/**
	 * Run the selftests for the specified modules.
	 *
	 * @param modules The names of the modules to test
	 * @return The test output
	 */
	public void runSelftests(String... modules);
}
