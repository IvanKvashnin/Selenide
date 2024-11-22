package com.example.demo;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.logging.LoggingPreferences;

import java.io.BufferedWriter;
import java.io.FileWriter;

import static com.codeborne.selenide.Selenide.open;
import static com.google.gson.JsonParser.parseString;
import static java.util.logging.Level.ALL;
import static org.openqa.selenium.logging.LogType.PERFORMANCE;

@Slf4j
public class SelenideNetworkLogsTest {
    @Test
    @SneakyThrows
    void test() {
        var logPrefs = new LoggingPreferences();
        logPrefs.enable(PERFORMANCE, ALL);

        Configuration.browserCapabilities.setCapability("goog:loggingPrefs", logPrefs);

        open("https://yandex.ru/");
        logNetwork();
    }

    @SneakyThrows
    public void logNetwork() {
        var logs = WebDriverRunner.getWebDriver().manage().logs().get(PERFORMANCE);
        var logsBrowser = new StringBuilder();
        var writer = new BufferedWriter(new FileWriter("networkLog.har"));
        logs.forEach(log -> logsBrowser.append(parseString(log.getMessage())));
        writer.write(logsBrowser.toString());
    }
}
