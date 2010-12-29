package com.opera.core.systems.scope;

import com.opera.core.systems.model.ICommand;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

//syntax = "scope";
//
//service CookieManager
//{
//    option (version) = "1.0";
//
//    /**
//     * Retrieve a list of cookies for a given domain.
//     *
//     * @param domain Name of domain to look for, e.g. "opera.com"
//     * @param path   Limit cookie list to specified path or subpath.
//     */
//    command GetCookie(GetCookieArg) returns (CookieList) = 1;
//
//    /**
//     * Removes selected cookies or all cookies in a domain.
//     *
//     * @param domain Name of domain to remove cookies from, e.g. "opera.com"
//     * @param path   If specified only removes cookies from specified path or subpath.
//     * @param name   Name of cookie to remove, if unspecified removes all cookies matching domain/path.
//     */
//    command RemoveCookie(RemoveCookieArg) returns (Default) = 2;
//
//    /**
//     * Removes all cookies.
//     */
//    command RemoveAllCookies(Default) returns (Default) = 3;
//
//    command GetCookieSettings(Default) returns (CookieSettings) = 4;
//}
//
// */

public enum CookieManagerCommand implements ICommand {
    GET_COOKIE(1),
    REMOVE_COOKIE(2),
    REMOVE_ALL_COOKIES(3),
    GET_COOKIE_SETTINGS(4),
    DEFAULT(-1);

    private int code;
    private static final Map<Integer, CookieManagerCommand> lookup = new HashMap<Integer, CookieManagerCommand>();

    static {
        for (CookieManagerCommand command : EnumSet.allOf(CookieManagerCommand.class))
            lookup.put(command.getCommandID(), command);
    }

    private CookieManagerCommand(int code) {
        this.code = code;
    }

    public int getCommandID() {
        return code;
    }

    public String getServiceName() {
        return "cookie-manager";
    }

    public static CookieManagerCommand get(int code) {
    	CookieManagerCommand command = lookup.get(code);
        return (command != null) ? command : DEFAULT;
    }

}
