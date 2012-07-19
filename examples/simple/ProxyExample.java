import com.opera.core.systems.OperaDriver;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

class ProxyExample {

    public static void main(String... args) throws Exception {
        // creating proxy settings
        Proxy proxy = new Proxy();
        proxy.setHttpProxy("localhost:48000");
        proxy.setFtpProxy("localhost:21");
        proxy.setSslProxy("localhost:443");

        // creating capabilities
        DesiredCapabilities cap = new DesiredCapabilities();
        cap.setCapability("opera.proxy", proxy);

        WebDriver wd = new OperaDriver(cap);

        wd.get("http://www.opera.com");

        Thread.sleep(10000);
        wd.quit();
    }

}
