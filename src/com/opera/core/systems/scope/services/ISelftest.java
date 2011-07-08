package com.opera.core.systems.scope.services;

import java.util.List;

public interface ISelftest {
	/**
	 * Run the selftests for the specified modules.
	 *
	 * @param modules The names of the modules to test
	 * @return The test output
	 */
	public void runSelftests(List<String> modules);

	public class SelftestResult {
		private String tag;
		private String description;
		private ResultType result;
		private String skipWhy;

		public SelftestResult(String tag, String description, ResultType result) {
			this(tag, description, result, null);
		}

		public SelftestResult(String tag, String description, ResultType result, String reason) {
			if(reason != null && result != ResultType.SKIP)
				throw new IllegalArgumentException("SKIP reason not allowed with non-SKIP result.");

			this.tag = tag;
			this.description = description;
			this.result = result;
			skipWhy = reason;
		}

		public String getTag()         { return tag; }
		public String getDescription() { return description; }
		public ResultType getResult()  { return result; }
		public String getSkipWhy()     { return skipWhy; }
	}

	public enum ResultType {
		PASS, FAIL, SKIP
	}
}
