package pos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Polarity {

    private Map<String, Double> dictionary;

    static String pathToSWN = "C:\\Users\\Shiv\\Desktop\\SWN\\home\\swn\\www\\admin\\dump\\SentiWordNet_3.0.0_20130122.txt";

    public Polarity() {
        dictionary = new HashMap<>();

        HashMap<String, HashMap<Integer, Double>> tempDictionary = new HashMap<>();

        try (BufferedReader csv = new BufferedReader(new FileReader(pathToSWN))) {
            int lineNumber = 0;

            String line;
            while ((line = csv.readLine()) != null) {
                lineNumber++;

                if (!line.trim().startsWith("#")) {
                    String[] data = line.split("\t");
                    String wordTypeMarker = data[0];

                    if (data.length != 6) {
                        throw new IllegalArgumentException(
                                "Incorrect tabulation format in file, line: "
                                + lineNumber);
                    }

                    Double synsetScore = Double.parseDouble(data[2])- Double.parseDouble(data[3]);

                    String[] synTermsSplit = data[4].split(" ");

                    for (String synTermSplit : synTermsSplit) {
                        String[] synTermAndRank = synTermSplit.split("#");
                        String synTerm = synTermAndRank[0] + "#"
                                + wordTypeMarker;

                        int synTermRank = Integer.parseInt(synTermAndRank[1]);
                        if (!tempDictionary.containsKey(synTerm)) {
                            tempDictionary.put(synTerm,
                                    new HashMap<Integer, Double>());
                        }

                        tempDictionary.get(synTerm).put(synTermRank,
                                synsetScore);
                    }
                }
            }

            for (Map.Entry<String, HashMap<Integer, Double>> entry : tempDictionary
                    .entrySet()) {
                String word = entry.getKey();
                Map<Integer, Double> synSetScoreMap = entry.getValue();

                double score = 0.0;
                double sum = 0.0;
                for (Map.Entry<Integer, Double> setScore : synSetScoreMap
                        .entrySet()) {
                    score += setScore.getValue() / (double) setScore.getKey();
                    sum += 1.0 / (double) setScore.getKey();
                }
                
                score /= sum;

                dictionary.put(word, score);
            }
            
        } catch (IOException | IllegalArgumentException e) {
        }
    }

    public double extract(String word, String pos) {
       // System.out.println(word + " " + pos);
        double score = 0.0;
        try {
            score = dictionary.get(word + "#" + pos);
        } catch (NullPointerException e) {

        }
        return score;
    }

    public double getPolarityScore(String word, String tag) throws IOException {
        //  Polarity polarity = new Polarity();
        word = word.toLowerCase();
        word = word.replaceAll("[^a-z]", "");
        return this.extract(word, tag);
    }

    public static void main(String[] args) throws IOException {
        Polarity sentiwordnet = new Polarity();
        System.out.println("very#r " + sentiwordnet.extract("not", "r"));
        System.out.println("good#a " + sentiwordnet.extract("good", "a"));
        System.out.println("worst#a " + sentiwordnet.extract("worst", "a"));
        System.out.println("blue#n " + sentiwordnet.extract("blue", "n"));
    }

}
