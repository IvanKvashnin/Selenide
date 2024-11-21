//package com.example.demo;
//
//import io.github.bonigarcia.wdm.WebDriverManager;
//import net.lightbody.bmp.BrowserMobProxyServer;
//import net.lightbody.bmp.client.ClientUtil;
//import org.junit.jupiter.api.Test;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.io.File;
//
//@SpringBootTest
//class DemoApplicationTests {
//
//	@Test
//	void contextLoads() {
//        WebDriverManager.chromedriver().setup();
//
//        var proxy = new BrowserMobProxyServer();
//        proxy.start(0);
//
//        var seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
//
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--proxy-server=" + seleniumProxy);
//        var driver = new ChromeDriver(options);
//
//        try {
//            proxy.newHar("octopus");
//            driver.get("https://octopus.com/");
//            Thread.sleep(5000); // Подождите 5 секунд
//
//            // Сохраняем HAR файл
//            var harFile = new File("test.har");
//            proxy.getHar().writeTo(harFile);
//            System.out.println("HAR файл сохранен как: " + harFile.getAbsolutePath());
//	}
//
//}
