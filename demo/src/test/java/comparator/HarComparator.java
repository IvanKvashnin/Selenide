package comparator;

import com.example.demo.AdBlockTest;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.SneakyThrows;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class HarComparator {

    /**
     * Основной метод для запуска сравнения HAR-файлов.
     * Считывает оригинальный HAR и HAR-файлы с включенными блокировщиками рекламы.
     * Сравнивает их содержимое и генерирует HTML-отчет.
     */
    @SneakyThrows
    public static void main(String[] args) {
        String link = "https://www.rbc.ru/"; // URL для тестирования

        // Генерация оригинального HAR-файла с помощью BrowserMobProxy
        File originalHarFile = AdBlockTest.collectHarFile("original.har", link);

        // HAR-файлы, собранные с блокировщиками рекламы
        // HAR-файл МИТМА, запущен с затычкой, потому что локально нельзя запустить прокси на митм
        File newHarFile1 = new File("/Users/kuvshinova/IdeaProjects/Selenide/demo/src/test/java/comparator/new1.har");
        File newHarFile2 = AdBlockTest.collectHarFileWithAdBlock("adblock.har", link);
        File newHarFile3 = AdBlockTest.collectHarFileWithAdGuard("adguard.har", link);

        // Проверка наличия всех необходимых HAR-файлов
        if (!originalHarFile.exists() || !newHarFile1.exists() || !newHarFile2.exists() || !newHarFile3.exists()) {
            System.out.println("Один или несколько HAR-файлов отсутствуют.");
            return;
        }

        // Чтение содержимого HAR-файлов
        String originalHarContent = new String(Files.readAllBytes(Paths.get(originalHarFile.getPath())));
        String newHarContent1 = new String(Files.readAllBytes(Paths.get(newHarFile1.getPath())));
        String newHarContent2 = new String(Files.readAllBytes(Paths.get(newHarFile2.getPath())));
        String newHarContent3 = new String(Files.readAllBytes(Paths.get(newHarFile3.getPath())));

        // Анализ структуры JSON в HAR-файлах
        JSONObject originalHar = new JSONObject(originalHarContent);
        JSONObject newHar1 = new JSONObject(newHarContent1);
        JSONObject newHar2 = new JSONObject(newHarContent2);
        JSONObject newHar3 = new JSONObject(newHarContent3);

        // Сравнение HAR-файлов
        Map<String, Object> result = compareHars(originalHar, newHar1, newHar2, newHar3);

        // Генерация отчета в формате HTML
        generateHtmlReport(result, "report.html");
    }

    /**
     * Сравнение содержимого HAR-файлов.
     *
     * @param originalHar Оригинальный HAR-файл (без блокировщика рекламы)
     * @param newHar1     HAR-файл с MITM
     * @param newHar2     HAR-файл с AdBlock
     * @param newHar3     HAR-файл с AdGuard
     * @return Результат сравнения в виде карты (Map)
     */
    @SneakyThrows
    public static Map<String, Object> compareHars(JSONObject originalHar, JSONObject newHar1, JSONObject newHar2, JSONObject newHar3) {
        // Карта для хранения заблокированных URL
        Map<String, List<String>> blockedUrls = new HashMap<>();
        blockedUrls.put("newHar1", new ArrayList<>());
        blockedUrls.put("newHar2", new ArrayList<>());
        blockedUrls.put("newHar3", new ArrayList<>());

        // Извлечение записей запросов из HAR-файлов
        JSONArray originalEntries = originalHar.getJSONObject("log").getJSONArray("entries");
        JSONArray newEntries1 = newHar1.getJSONObject("log").getJSONArray("entries");
        JSONArray newEntries2 = newHar2.getJSONObject("log").getJSONArray("entries");
        JSONArray newEntries3 = newHar3.getJSONObject("log").getJSONArray("entries");

        // Преобразование запросов в удобные карты
        Map<String, JSONObject> originalEntriesMap = getEntriesMap(originalEntries);
        Map<String, JSONObject> newEntriesMap1 = getEntriesMap(newEntries1);
        Map<String, JSONObject> newEntriesMap2 = getEntriesMap(newEntries2);
        Map<String, JSONObject> newEntriesMap3 = getEntriesMap(newEntries3);

        // Сравнение запросов и определение, какие из них были заблокированы
        for (String url : originalEntriesMap.keySet()) {
            if (!newEntriesMap1.containsKey(url)) {
                blockedUrls.get("newHar1").add(url);
            }
            if (!newEntriesMap2.containsKey(url)) {
                blockedUrls.get("newHar2").add(url);
            }
            if (!newEntriesMap3.containsKey(url)) {
                blockedUrls.get("newHar3").add(url);
            }
        }

        // Упорядочивание всех заблокированных URL и распределение по категориям
        Set<String> allUrls = new HashSet<>(blockedUrls.get("newHar1"));
        allUrls.addAll(blockedUrls.get("newHar2"));
        allUrls.addAll(blockedUrls.get("newHar3"));

        Map<String, List<String>> sortedBlockedUrls = new HashMap<>();
        sortedBlockedUrls.put("newHar1", new ArrayList<>());
        sortedBlockedUrls.put("newHar2", new ArrayList<>());
        sortedBlockedUrls.put("newHar3", new ArrayList<>());

        for (String url : allUrls) {
            if (blockedUrls.get("newHar1").contains(url)) {
                sortedBlockedUrls.get("newHar1").add(url);
            }
            if (blockedUrls.get("newHar2").contains(url)) {
                sortedBlockedUrls.get("newHar2").add(url);
            }
            if (blockedUrls.get("newHar3").contains(url)) {
                sortedBlockedUrls.get("newHar3").add(url);
            }
        }

        // Выявление запросов, заблокированных AdGuard и AdBlock, но пропущенных первым файлом MITM
        List<String> blockedUrlsNotInNewHar1 = getBlockedUrlsNotInNewHar1(sortedBlockedUrls);

        // Возврат собранных результатов
        Map<String, Object> result = new HashMap<>();
        result.put("blockedUrls", sortedBlockedUrls);
        result.put("blockedUrlsNotInNewHar1", blockedUrlsNotInNewHar1);

        return result;
    }

    /**
     * Преобразует массив записей JSON в удобную карту с URL в качестве ключа.
     */
    @SneakyThrows
    private static Map<String, JSONObject> getEntriesMap(JSONArray entries) {
        Map<String, JSONObject> entriesMap = new HashMap<>();
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            JSONObject request = entry.getJSONObject("request");
            String url = request.getString("url");
            entriesMap.put(url, entry);
        }
        return entriesMap;
    }

    /**
     * Возвращает список URL, которые были заблокированы только AdBlock и AdGuard.
     */
    private static List<String> getBlockedUrlsNotInNewHar1(Map<String, List<String>> sortedBlockedUrls) {
        List<String> blockedUrlsNotInNewHar1 = new ArrayList<>();
        Set<String> newHar1Urls = new HashSet<>(sortedBlockedUrls.get("newHar1"));

        for (String url : sortedBlockedUrls.get("newHar2")) {
            if (!newHar1Urls.contains(url)) {
                blockedUrlsNotInNewHar1.add(url);
            }
        }

        for (String url : sortedBlockedUrls.get("newHar3")) {
            if (!newHar1Urls.contains(url)) {
                blockedUrlsNotInNewHar1.add(url);
            }
        }

        return blockedUrlsNotInNewHar1;
    }

    /**
     * Генерация HTML-отчета с использованием шаблона FreeMarker.
     *
     * @param data       Данные о заблокированных URL
     * @param outputFile Путь к выходному HTML-файлу
     */
    public static void generateHtmlReport(Map<String, Object> data, String outputFile) throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setDirectoryForTemplateLoading(new File("/Users/kuvshinova/IdeaProjects/Selenide/demo/src/test/java/templates"));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);

        Template template = cfg.getTemplate("report.ftl");

        Map<String, List<String>> blockedUrls = (Map<String, List<String>>) data.get("blockedUrls");
        List<String> blockedUrlsNotInNewHar1 = (List<String>) data.get("blockedUrlsNotInNewHar1");

        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("blockedUrlsNewHar1", blockedUrls.get("newHar1"));
        dataModel.put("blockedUrlsNewHar2", blockedUrls.get("newHar2"));
        dataModel.put("blockedUrlsNewHar3", blockedUrls.get("newHar3"));
        dataModel.put("blockedUrlsCountNewHar1", blockedUrls.get("newHar1").size());
        dataModel.put("blockedUrlsCountNewHar2", blockedUrls.get("newHar2").size());
        dataModel.put("blockedUrlsCountNewHar3", blockedUrls.get("newHar3").size());
        dataModel.put("blockedUrlsNotInNewHar1", blockedUrlsNotInNewHar1);

        try (FileWriter writer = new FileWriter(new File(outputFile))) {
            template.process(dataModel, writer);
        }
    }
}