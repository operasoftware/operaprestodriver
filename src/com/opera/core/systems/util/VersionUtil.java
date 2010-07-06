package com.opera.core.systems.util;

/**
 * A replacement for a subset of com.sun.xml.ws.util.VersionUtil.
 *
 * @author Jan Vidar Krey (janv@opera.com)
 */
public class VersionUtil {

	/**
	 * Compares the versions given in string format
	 * 
	 * @param version1
	 * @param version2
	 * @return -1, 0 or 1 based upon the comparison results -1 if version1 is
	 *         less than version2 0 if version1 is equal to version2 1 if
	 *         version1 is greater than version2
	 */
    public static int compare(String a, String b)
    {
        int minlen = (a.length() < b.length()) ? a.length() : b.length();
        
        for (int i = 0; i < minlen; i++)
        {
            if (a.charAt(i) < b.charAt(i))
                return -1;
            else if (a.charAt(i) > b.charAt(i))
                return 1;
        }

        // Check for more specific versions, assume the more specific
        // version to be higher.
        // So, "2.0.1" is higher than "2.0", and "2.0.0" is higher than "2.0".
        if (b.length() > a.length())
            return 1;
        if (b.length() < a.length())
            return -1;

        return 0;
    }

}
