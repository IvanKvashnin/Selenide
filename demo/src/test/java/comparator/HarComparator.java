package comparator;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HarComparator {

    @SneakyThrows
    public static void main(String[] args) {
        File originalHarFile = new File("/Users/kuvshinova/IdeaProjects/Selenide/demo/src/test/java/comparator/original.har");
        File newHarFile1 = new File("/Users/kuvshinova/IdeaProjects/Selenide/demo/src/test/java/comparator/new1.har");
        File newHarFile2 = new File("/Users/kuvshinova/IdeaProjects/Selenide/demo/src/test/java/comparator/new2.har");
        File newHarFile3 = new File("/Users/kuvshinova/IdeaProjects/Selenide/demo/src/test/java/comparator/new3.har");

        if (!originalHarFile.exists() || !newHarFile1.exists() || !newHarFile2.exists() || !newHarFile3.exists()) {
            System.out.println("One or more HAR files do not exist.");
            return;
        }

        String originalHarContent = new String(Files.readAllBytes(Paths.get(originalHarFile.getPath())));
        String newHarContent1 = new String(Files.readAllBytes(Paths.get(newHarFile1.getPath())));
        String newHarContent2 = new String(Files.readAllBytes(Paths.get(newHarFile2.getPath())));
        String newHarContent3 = new String(Files.readAllBytes(Paths.get(newHarFile3.getPath())));

        JSONObject originalHar = new JSONObject(originalHarContent);
        JSONObject newHar1 = new JSONObject(newHarContent1);
        JSONObject newHar2 = new JSONObject(newHarContent2);
        JSONObject newHar3 = new JSONObject(newHarContent3);

        Map<String, List<String>> blockedUrls = compareHars(originalHar, newHar1, newHar2, newHar3);

        generateHtmlReport(blockedUrls, "report.html");
    }

    @SneakyThrows
    public static Map<String, List<String>> compareHars(JSONObject originalHar, JSONObject newHar1, JSONObject newHar2, JSONObject newHar3) {
        Map<String, List<String>> blockedUrls = new HashMap<>();
        blockedUrls.put("newHar1", new ArrayList<>());
        blockedUrls.put("newHar2", new ArrayList<>());
        blockedUrls.put("newHar3", new ArrayList<>());

        JSONArray originalEntries = originalHar.getJSONObject("log").getJSONArray("entries");
        JSONArray newEntries1 = newHar1.getJSONObject("log").getJSONArray("entries");
        JSONArray newEntries2 = newHar2.getJSONObject("log").getJSONArray("entries");
        JSONArray newEntries3 = newHar3.getJSONObject("log").getJSONArray("entries");

        Map<String, JSONObject> originalEntriesMap = getEntriesMap(originalEntries);
        Map<String, JSONObject> newEntriesMap1 = getEntriesMap(newEntries1);
        Map<String, JSONObject> newEntriesMap2 = getEntriesMap(newEntries2);
        Map<String, JSONObject> newEntriesMap3 = getEntriesMap(newEntries3);

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

        // Объединение всех URL
        Set<String> allUrls = new HashSet<>(blockedUrls.get("newHar1"));
        allUrls.addAll(blockedUrls.get("newHar2"));
        allUrls.addAll(blockedUrls.get("newHar3"));

        // Сортировка URL
        List<String> sortedUrls = new ArrayList<>(allUrls);
        Collections.sort(sortedUrls);

        // Распределение URL по спискам
        Map<String, List<String>> sortedBlockedUrls = new HashMap<>();
        sortedBlockedUrls.put("newHar1", new ArrayList<>());
        sortedBlockedUrls.put("newHar2", new ArrayList<>());
        sortedBlockedUrls.put("newHar3", new ArrayList<>());

        for (String url : sortedUrls) {
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

        return sortedBlockedUrls;
    }


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

    public static void generateHtmlReport(Map<String, List<String>> blockedUrls, String outputFile) throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setDirectoryForTemplateLoading(new File("/Users/kuvshinova/IdeaProjects/Selenide/demo/src/test/java/templates"));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);

        Template template = cfg.getTemplate("report.ftl");

        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("blockedUrlsNewHar1", blockedUrls.get("newHar1"));
        dataModel.put("blockedUrlsNewHar2", blockedUrls.get("newHar2"));
        dataModel.put("blockedUrlsNewHar3", blockedUrls.get("newHar3"));
        dataModel.put("blockedUrlsCountNewHar1", blockedUrls.get("newHar1").size());
        dataModel.put("blockedUrlsCountNewHar2", blockedUrls.get("newHar2").size());
        dataModel.put("blockedUrlsCountNewHar3", blockedUrls.get("newHar3").size());

        try (FileWriter writer = new FileWriter(new File(outputFile))) {
            template.process(dataModel, writer);
        }
    }
}