package com.example.demo;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;

import static com.codeborne.selenide.Selenide.open;

@Slf4j
public class AdBlockTest {

    // Пути к расширениям
    private static final String ADGUARD_EXTENSION_PATH = "C:\\Users\\kovsh\\IdeaProjects\\Selenide\\demo\\src\\test\\java\\com\\example\\demo\\AdguardBrowserExtension-5.0.185";
    private static final String ADBLOCK_PLUS_EXTENSION_PATH = "C:\\Users\\kovsh\\IdeaProjects\\Selenide\\demo\\src\\test\\java\\com\\example\\demo\\adblockpluschrome-3.10.2";

    /**
     * Конфигурация ChromeOptions с расширением
     *
     * @param extensionPath Путь к папке с расширением
     * @return ChromeOptions, настроенный с расширением
     */
    private static ChromeOptions configureChromeWithExtension(String extensionPath) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--load-extension=" + extensionPath);
        return options;
    }

    /**
     * Настройка Proxy через BrowserMobProxy и привязка к браузеру.
     *
     * @return Активный экземпляр BrowserMobProxy
     */
    private static BrowserMobProxy setupProxy() {
        BrowserMobProxy proxy = new BrowserMobProxyServer();
        proxy.start(0); // Использовать любой свободный порт

        // Конфигурация прокси для Selenium
        Proxy seleniumProxy = new Proxy();
        String proxyStr = "localhost:" + proxy.getPort();
        seleniumProxy.setHttpProxy(proxyStr);
        seleniumProxy.setSslProxy(proxyStr);

        // Добавление прокси в капабилити браузера
        Configuration.browserCapabilities = new DesiredCapabilities();
        Configuration.browserCapabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

        return proxy;
    }

    /**
     * Сбор HAR-файла при работе с браузером
     *
     * @param fileName      Название сохраняемого HAR-файла
     * @param url           URL для открытия в браузере
     * @param chromeOptions Настройки Chrome (включая расширения)
     * @return HAR-файл с сетевыми данными
     */
    @SneakyThrows
    private static File collectHarFile(String fileName, String url, ChromeOptions chromeOptions) {
        BrowserMobProxy proxy = null;
        File harFile = new File(fileName);

        try {
            proxy = setupProxy();

            // Добавляем ChromeOptions к текущим настройкам браузера
            Configuration.browserCapabilities.merge(chromeOptions);

            // Настройка записи HAR
            proxy.newHar("capture-har");

            // Открытие указанного сайта
            open(url);

            // Сохраняем HAR-файл
            proxy.getHar().writeTo(harFile);
            System.out.println("HAR-файл сохранён: " + harFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Не удалось сохранить HAR-файл: " + e.getMessage());
        } finally {
            if (proxy != null) {
                proxy.stop(); // Остановка прокси
            }
            Selenide.closeWebDriver(); // Закрытие браузера
        }

        return harFile;
    }

    /**
     * Сбор HAR-файла оригинального
     */
    @SneakyThrows
    public static File collectHarFile(String fileName, String url) {
        ChromeOptions options = new ChromeOptions();
        return collectHarFile(fileName, url, options);
    }

    /**
     * Сбор HAR-файла с использованием расширения AdGuard.
     */
    @SneakyThrows
    public static File collectHarFileWithAdGuard(String fileName, String url) {
        ChromeOptions optionsWithAdGuard = configureChromeWithExtension(ADGUARD_EXTENSION_PATH);
        return collectHarFile(fileName, url, optionsWithAdGuard);
    }

    /**
     * Сбор HAR-файла с использованием расширения AdBlock Plus.
     */
    @SneakyThrows
    public static File collectHarFileWithAdBlock(String fileName, String url) {
        ChromeOptions optionsWithAdBlock = configureChromeWithExtension(ADBLOCK_PLUS_EXTENSION_PATH);
        return collectHarFile(fileName, url, optionsWithAdBlock);
    }

    /**
     * Основной метод для тестирования разных расширений.
     */
//    public static void main(String[] args) {
//        String url = "https://www.w3schools.com/python/default.asp"; // Замените своим тестируемым сайтом
//
//        // Сбор HAR с AdGuard
//        collectHarFileWithAdGuard("har_file_with_adguard.har", url);
//
//        // Сбор HAR с AdBlock Plus
//        collectHarFileWithAdBlock("har_file_with_adblock.har", url);
//    }
}