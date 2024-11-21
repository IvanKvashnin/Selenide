package com.example.demo;

import com.codeborne.selenide.WebDriverRunner;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v121.network.Network;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Selenide.open;
import static java.util.Optional.empty;
import static org.openqa.selenium.devtools.v121.network.Network.*;

@Slf4j
public class HarNoProxy {
    @Test
    @SneakyThrows
    void test() {

        open("https://yandex.ru/");

        var driver = (ChromeDriver) WebDriverRunner.getWebDriver();
        var devTools = driver.getDevTools();
        devTools.createSession();

        List<String> harEntries = new ArrayList<>();

        devTools.send(enable(empty(), empty(), empty()));

        devTools.addListener(requestWillBeSent(), (args1) -> {
            var url = args1.getRequest().getUrl();
            harEntries.add(url);
        });

        devTools.addListener(responseReceived(), (args2) -> {
            var url = args2.getResponse().getUrl();
            int status = args2.getResponse().getStatus();
            harEntries.add(url + " " + status);
            System.out.println(url + " - Status: " + status);
        });

        var gson = new Gson();
        var writer = new BufferedWriter(new FileWriter("output.har"));
        var toWrite = gson.toJson(harEntries);
        writer.write(toWrite);

        Thread.sleep(1000);
        devTools.send(Network.disable());
    }
}
