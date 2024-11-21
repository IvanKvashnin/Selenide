package com.example.demo;

import com.codeborne.selenide.WebDriverRunner;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.v121.network.Network;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Selenide.open;
import static java.util.Optional.empty;
import static org.openqa.selenium.devtools.v121.network.Network.*;

@Slf4j
public class HarNoProxy {
    @Test
    void test() {
        open("https://yandex.ru/");

        var driver = (ChromeDriver) WebDriverRunner.getWebDriver();
        var devTools = driver.getDevTools();
        devTools.createSession();

        List<Map<String, Object>> entries = new ArrayList<>();

        devTools.send(enable(empty(), empty(), empty()));

        devTools.addListener(requestWillBeSent(), (args1) -> {
            var url = args1.getRequest().getUrl();
            var requestEntry = new HashMap<String, Object>();
            requestEntry.put("url", url);
            requestEntry.put("method", args1.getRequest().getMethod());
            requestEntry.put("time", System.currentTimeMillis());
            entries.add(requestEntry);
        });

        devTools.addListener(responseReceived(), (args2) -> {
            var url = args2.getResponse().getUrl();
            int status = args2.getResponse().getStatus();
            var responseEntry = new HashMap<String, Object>();
            responseEntry.put("url", url);
            responseEntry.put("status", status);
            responseEntry.put("time", System.currentTimeMillis());
            entries.add(responseEntry);
            System.out.println(url + " - Status: " + status);
        });


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted", e);
        } finally {
            devTools.send(Network.disable());
        }

        // Сохранение в файл в формате HAR
        Map<String, Object> har = new HashMap<>();
        har.put("log", new HashMap<String, Object>() {{
            put("version", "1.2");
            put("creator", new HashMap<String, String>() {{
                put("name", "Vlad");
                put("version", "4.0");
            }});
            put("entries", entries);
        }});

        // Сохранение в файл
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.har"))) {
            var gson = new Gson();
            var toWrite = gson.toJson(har);
            writer.write(toWrite);
        } catch (IOException e) {
            log.error("Error writing to file", e);
        }
    }
}
