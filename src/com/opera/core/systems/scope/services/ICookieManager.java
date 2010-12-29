package com.opera.core.systems.scope.services;

import java.util.Set;

import org.openqa.selenium.Cookie;

//TODO Document
public interface ICookieManager {
	Set<Cookie> getCookie(String domain, String path);

	void removeCookie(String domain, String path, String name);

	void removeAllCookies();

	int getMaxCookies();

	int getMaxCookiesPerDomain();

	int getMaxCookieLength();

	void init();

}
