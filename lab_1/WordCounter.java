package lab_1;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WordCounter {
    public static ConcurrentHashMap<String, Integer> wordCount = new ConcurrentHashMap<>();

    public static void main(String[] args){
        String filePath = "test.txt";

        processFile(filePath);

        printResult();
    }

    public static void processFile(String filePath) {
        try{
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;

        while ((line = reader.readLine()) != null){
            line = line.toLowerCase();
            line = line.replaceAll("[^a-zа-я0-9 ]", "");
            String[] words = line.split("\\s+");

            for (String word: words){
                if (!word.isEmpty()){
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
        }

        reader.close();
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла");
            e.printStackTrace();
        }
    }

    public static void printResult() {
        System.out.println("Результат подсчета слов: ");
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()){
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }    
}