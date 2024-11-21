package com.example.demo;

import com.codeborne.selenide.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.lightbody.bmp.BrowserMobProxyServer;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

import static com.codeborne.selenide.Selenide.open;

@Slf4j
public class TestHar {
    @Test
    @SneakyThrows
    public void test() {
        var proxy = new BrowserMobProxyServer();
        proxy.start(3030);

        var seleniumProxy = new Proxy();
        var proxyStr = "localhost:" + proxy.getPort();
        seleniumProxy.setHttpProxy(proxyStr);
        seleniumProxy.setSslProxy(proxyStr);

        Configuration.browserCapabilities = new DesiredCapabilities();
        Configuration.browserCapabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

        proxy.newHar("yandex");
        open("https://yandex.ru/");

        var harFile = new File("test.har");
        proxy.getHar().writeTo(harFile);
    }
}
