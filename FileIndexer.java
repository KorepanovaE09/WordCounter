import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.*;

public class FileIndexer {

    public static ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> index = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        System.out.println("🚀 СТАРТ ПРОГРАММЫ");

        String rootPath = "D:\\ПОЛИТЕХ\\мага\\2\\WordCounter\\data";

        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            Files.walk(Paths.get(rootPath))
                    .filter(Files::isRegularFile)
                    .forEach(path -> executor.submit(() -> processFile(path.toString())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdown();

        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("📊 ИТОГ В ПАМЯТИ");
        // printIndex();

        System.out.println("🏁 КОНЕЦ ПРОГРАММЫ");
    }

    public static void processFile(String filePath) {

        System.out.println("📄 Обрабатываю: " + filePath);

        ConcurrentHashMap<String, Integer> localMap = new ConcurrentHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.toLowerCase();
                line = line.replaceAll("[^a-zа-я0-9 ]", " ");

                String[] words = line.split("\\s+");

                for (String word : words) {
                    if (!word.isEmpty()) {
                        localMap.merge(word, 1, Integer::sum);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("❌ Ошибка файла: " + filePath);
            e.printStackTrace();
        }

        index.put(filePath, localMap);

        System.out.println("💾 Сохраняю в БД: " + filePath);

        saveToDatabase(filePath, localMap);
    }

    public static void saveToDatabase(String filePath, Map<String, Integer> wordMap) {

        System.out.println("🔌 Подключение к БД...");

        try (Connection conn = DB.getConnection()) {

            System.out.println("✅ Подключение успешно");

            // вставка файла (гарантированно возвращает id)
            PreparedStatement psFile = conn.prepareStatement(
                    "INSERT INTO files(file_path) VALUES (?) " +
                            "ON CONFLICT (file_path) DO UPDATE SET file_path = EXCLUDED.file_path " +
                            "RETURNING id"
            );

            psFile.setString(1, filePath);

            ResultSet rs = psFile.executeQuery();

            if (!rs.next()) {
                throw new RuntimeException("❌ Не удалось получить file_id");
            }

            int fileId = rs.getInt(1);

            System.out.println("🆔 fileId = " + fileId);

            PreparedStatement psWord = conn.prepareStatement(
                    "INSERT INTO word_counts(file_id, word, count) VALUES (?, ?, ?)"
            );

            int count = 0;

            for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {
                psWord.setInt(1, fileId);
                psWord.setString(2, entry.getKey());
                psWord.setInt(3, entry.getValue());
                psWord.addBatch();
                count++;
            }

            psWord.executeBatch();

            System.out.println("📥 Сохранено слов: " + count);

        } catch (Exception e) {
            System.out.println("❌ ОШИБКА БД для файла: " + filePath);
            e.printStackTrace();
        }
    }

    public static void printIndex() {

        System.out.println("📊 ЧАСТОТНОСТЬ СЛОВ В ФАЙЛАХ");

        for (Map.Entry<String, ConcurrentHashMap<String, Integer>> fileEntry : index.entrySet()) {

            String fileName = Paths.get(fileEntry.getKey()).getFileName().toString();

            System.out.println("\n📄 Файл: " + fileName);

            fileEntry.getValue().entrySet().stream()
                    .sorted((a, b) -> b.getValue() - a.getValue())
                    .forEach(e ->
                            System.out.println("   " + e.getKey() + " : " + e.getValue())
                    );
        }
    }
}