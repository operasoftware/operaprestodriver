package com.opera.core.systems.test;

import com.opera.core.systems.OperaDriver;

public class Test {
	public static void main(String[] args) {
		OperaDriver driver = new OperaDriver();
		driver.get("http://t/core/features/spatnav/adhoc/after-hidden_visibility-block.html");
		driver.operaAction("Highlight next URL");
		Object result = driver.executeScript("document.activeElement.text;");
		System.out.println(result);
	}
}
