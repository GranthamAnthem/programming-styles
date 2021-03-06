import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.util.*;

public class Seventeen {

    public static void main(String[] args) throws IOException {

        // error if build and run from compiler
        if (args.length < 1) {
            System.exit(1);
        }
        // if build and run from terminal
        // java Main.java pride-and-prejudice.txt
        else {
            new WordFrequencyController(args[0]).run();
        }

        System.out.println("Enter a class to inspect: ");
        Scanner in =  new Scanner(System.in);
        String name = in.nextLine();
        System.out.println("Getting information about " + name);

        Class cls = null;
        try {
            cls = Class.forName(name);
        } catch (Exception e) {
            System.out.println("No such class: " + name);
        }

        if (cls != null) {

            Class superClass = cls.getSuperclass();
            System.out.println("SuperClass name: " + superClass.getName());

            Class[] interfaces = cls.getInterfaces();
            for (Class iface : interfaces)
                System.out.println("Interface name: " + iface.getName());

            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields) {
                System.out.println("Filend name: " + f.getName() + "\tType: " + f.getType());
            }

            Method[] methods = cls.getMethods();
            for (Method m : methods)
                System.out.println("Method name: " + m.getName());
        }
    }
}

/*                                  */
/*                                  */
/*          Abstract Classes        */
/*                                  */
/*                                  */

interface IDataStorage {

    // retrieve words from file
    Object getWords();
}

interface IStopWordFilter {

    // check if word is a stop word
    boolean checkStopWord(String word);
}

interface IWordFrequency {
    // count words with frequencies
    void countFrequencies(String word);
    // sort the words in descending order
    LinkedHashMap<String, Integer> reverseSortTopResults();
}

interface IWFController {
    // launch program
    void run();
}


/*                                  */
/*                                  */
/*          Concrete Classes        */
/*                                  */
/*                                  */

class WordFrequencyController implements IWFController {
    private DataStoreManager storeManager;
    private StopWordManager stopWordManager;
    private WordFrequencyManager wordFrequencyManager;

    public WordFrequencyController(String filePath) throws IOException {
        this.storeManager = new DataStoreManager(filePath);
        this.stopWordManager = new StopWordManager();
        this. wordFrequencyManager = new WordFrequencyManager();
    }

    public void run() {

        Class storeManagerClass = storeManager.getClass();
        Class stopWordManagerClass = stopWordManager.getClass();
        Class wordFrequencyManagerClass = wordFrequencyManager.getClass();

        try {

            Method getWordsMethod = storeManagerClass.getDeclaredMethod("getWords");

            Method checkStopWord = stopWordManagerClass.getDeclaredMethod("checkStopWord", String.class);

            Method countFrequenciesMethod = wordFrequencyManagerClass.getDeclaredMethod("countFrequencies", String.class);

            Method sortFrequenciesMethod = wordFrequencyManagerClass.getDeclaredMethod("reverseSortTopResults");

            for (String word: (List<String>) getWordsMethod.invoke(storeManager)) {
                if(!(Boolean) checkStopWord.invoke(stopWordManager, word)) {
                    countFrequenciesMethod.invoke(wordFrequencyManager, word);
                }
            }

            int count = 0;
            for (String key : ((LinkedHashMap<String, Integer>) sortFrequenciesMethod.invoke(wordFrequencyManager)).keySet()) {
                String keys = key;
                Integer value = ((LinkedHashMap<String, Integer>) sortFrequenciesMethod.invoke(wordFrequencyManager)).get(key);
                System.out.println(keys + " - " + value);
                count++;
                if (count >= 25)
                    break;
            }

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}



class DataStoreManager implements IDataStorage {
    private List<String> fileData;
    private List<String> words;

    public DataStoreManager(String filePath) throws IOException {
        fileData = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line = "";
        while ((line = br.readLine()) != null) {
            fileData.add(line);
        }
        br.close();

        words = new ArrayList<>();
        for(int i = 0; i < fileData.size(); i++) {
            String[] w = fileData.get(i).toLowerCase().split("[^a-zA-Z]");
            for(String temp: w) {
                words.add(temp);
            }
        }
    }

    @Override
    public List<String> getWords() {
        return this.words;
    }
}

class StopWordManager implements IStopWordFilter{
    private Set<String> stopWords;

    public StopWordManager() throws IOException {
        this.stopWords = new HashSet<>();

        BufferedReader br = new BufferedReader(new FileReader("../stop-words.txt"));
        String line = "";
        while((line = br.readLine()) != null) {
            String[] stopWords = line.split(",");
            for(String words : stopWords) {
                this.stopWords.add(words);
            }
        }
        br.close();
    }

    @Override
    public boolean checkStopWord(String word) {
        return this.stopWords.contains(word);
    }
}

class WordFrequencyManager implements IWordFrequency{
    private HashMap<String, Integer> countMap;
    private LinkedHashMap<String, Integer> sortHighestCount;

    public WordFrequencyManager() {
        this.countMap = new HashMap<>();
    }

    @Override
    public void countFrequencies(String word) {
        if (word.length() >= 2) {
            if (!(countMap.containsKey(word)))
                countMap.put(word, 1);
            else
                countMap.put(word, countMap.get(word) +1);
        }
    }

    @Override
    public LinkedHashMap<String, Integer> reverseSortTopResults() {
        sortHighestCount = new LinkedHashMap<>();
        countMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortHighestCount.put(x.getKey(), x.getValue()));
        return sortHighestCount;
    }
}
