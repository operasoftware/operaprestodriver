package com.opera.core.systems;

import junit.framework.TestCase;

import org.junit.Test;

import com.opera.core.systems.util.VersionUtil;

public class VersionUtilsTest extends TestCase {

	private final String version = "2.0";
	private final String[] biggerVersions = {"2.0.0", "2.0.1", "2.1", "2.1.0", "3.0", "3.0.1"};
	private final String[] smallerVersions = {"1.0.0", "1.0.1", "1.1", "1.1.0", "1.0", "1.9.9"};

	@Test
	public void testShouldSupportSmallerThan() {
		traverseVersions(biggerVersions, 1, ">");
	}

	@Test
	public void testShouldSupportBiggerThan() {
		traverseVersions(smallerVersions, -1, "<");
	}

	@Test
	public void testShouldSupportEquals() {

		for(int i = 0; i < biggerVersions.length; ++i) {
			int result = VersionUtil.compare(biggerVersions[i], biggerVersions[i]);
			assertTrue("Expected equals on " + biggerVersions[i]  , result == 0 );
		}

		for(int i = 0; i < smallerVersions.length; ++i) {
			int result = VersionUtil.compare(smallerVersions[i], smallerVersions[i]);
			assertTrue("Expected equals on " + smallerVersions[i]  , result == 0 );
		}
	}

	private void traverseVersions(String[] arr, int expected, String sign) {
		for(int i = 0; i < arr.length; ++i) {
			int result = VersionUtil.compare(arr[i], version);
			assertTrue("Expected " + arr[i] + " " + sign + " " + version, result == expected );
		}
	}


}
