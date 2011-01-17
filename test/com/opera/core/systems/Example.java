package com.opera.core.systems;

// run from the same directory with
// javac -classpath lib/junit-4.8.2.jar:lib/commons-jxpath-1.3.jar:lib/webdriver-common.jar:lib/commons-exec-1.1.jar:lib/winp-1.14.jar:lib/commons-io-2.0.jar:lib/protobuf-java-2.3.0.jar:./src:. Example.java
// java -classpath lib/junit-4.8.2.jar:lib/commons-jxpath-1.3.jar:lib/webdriver-common.jar:lib/commons-exec-1.1.jar:lib/winp-1.14.jar:lib/commons-io-2.0.jar:lib/protobuf-java-2.3.0.jar:./src:. Example

import org.openqa.selenium.WebElement;

import com.opera.core.systems.settings.*;

class Example {

  public static void main(String[] args) {
    System.out.println(">> Init settings");
    OperaDriverSettings settings = new OperaDriverSettings();

    System.out.println(">> Set binary paths");
    settings.setOperaBinaryLocation("/home/stuart/operas/lingogi/opera-ppp-mainline-build_4359-buildsetid_26276-core_2_8-lingogi/profiles/release_desktop/lingogi_release_desktop");
    settings.setOperaLauncherBinary("/home/stuart/bin/opera-launcher");

    System.out.println(">> Set binary arguments");
    settings.setOperaBinaryArguments("-url http://localhost/");

    System.out.println(">> Set run opera launcher");
    settings.setRunOperaLauncherFromOperaDriver(true);

    System.out.println(">> Init driver");
    OperaDriver driver = new OperaDriver(settings);

    System.out.println(">> Navigate to page");
    driver.get("http://example.com/");

    System.out.println(">> Current URL: "+ driver.getCurrentUrl());

    WebElement el = driver.findElementByTagName("p");
    System.out.println(el.getText());

    System.out.println(">> Exit");
    driver.shutdown();
    System.exit(0);
  }

}
