package com.example.demo;

import com.browserup.bup.proxy.CaptureType;
import com.browserup.harreader.model.HarEntry;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.codeborne.selenide.Selenide.open;

public class SelenideHarProxyTest {
    @Test
    void test() {
        Configuration.proxyEnabled = true;
        open("https://www.gismeteo.ru");

        var bmp = WebDriverRunner.getSelenideProxy().getProxy();
        bmp.setHarCaptureTypes(CaptureType.getAllContentCaptureTypes());
        bmp.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
        bmp.newHar("pofig");

        List<HarEntry> requests = bmp.getHar().getLog().getEntries();
    }
}
