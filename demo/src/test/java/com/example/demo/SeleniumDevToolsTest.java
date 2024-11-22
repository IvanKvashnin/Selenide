package com.example.demo;

import com.codeborne.selenide.WebDriverRunner;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.v121.network.Network;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selenide.open;
import static java.util.Optional.empty;
import static org.awaitility.Awaitility.await;
import static org.openqa.selenium.devtools.v121.network.Network.disable;
import static org.openqa.selenium.devtools.v121.network.Network.enable;

@Slf4j
public class SeleniumDevToolsTest {
    @Test
    @SneakyThrows
    void test() {
        open("https://yandex.ru/");
        var driver = (ChromeDriver) WebDriverRunner.getWebDriver();
        var devTools = driver.getDevTools();

        devTools.createSession();
        devTools.send(enable(empty(), empty(), empty()));

        var entries = new ArrayList<>();

        devTools.addListener(Network.responseReceived(), (handler) -> {
            await().until(() -> handler != null);
            var url = handler.getResponse().getUrl();
            var status = handler.getResponse().getStatus();
            entries.add(Map.of("response", Map.of(
                    "url", url,
                    "status", status)));
        });

        TimeUnit.SECONDS.sleep(3);
        devTools.send(disable());

        var stringHar = Map.of("entries", entries);

        var writer = new BufferedWriter(new FileWriter("output.har"));
        var jsonHar = new Gson().toJson(stringHar);
        writer.write(jsonHar);
    }
}
