import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
//import java.util.stream.Stream;

public class MyCSVReader {
    public static void main(String[] args) {
        CSVReader csvReader = null; 
        List<EmailData> emailDataList = new ArrayList<>();

        try {
            // Provides the path to the CSV file
            String csvFilePath = "C:\\Users\\ramax\\OneDrive\\Desktop\\Text Processing\\spam_or_not_spam.csv";
            
            // Initializes a CSVReader with OpenCSV
            csvReader = new CSVReaderBuilder(new FileReader(csvFilePath))
                    .withSkipLines(1) // Skips the title row if present in CSV file
                    .build(); // Builds a CSVReader object

            String[] nextCell; // Creates an array to store the next sequential row's contents
            while ((nextCell = csvReader.readNext()) != null) {
                    // Accesses the "email" and "label" columns
                    String email = nextCell[0];
                    int label = Integer.parseInt(nextCell[1]); // Converts the strings in the label column (either "0" or "1") into an integer
                    
                    // Calculates email freatures
                    int emailLength = email.length(); // Calculates email length
                    Map<String, Integer> wordFrequency = calcWordFrequency(email); // Calculates word frequency

                    // Saves computed features
                    emailDataList.add(new EmailData(email, label, emailLength, wordFrequency));
            }

            // Calculates and stores summary statistics
            List<Integer> emailLengths = emailDataList.stream().map(EmailData::getEmailLength).collect(Collectors.toList());
            double meanEmailLength = calcMean(emailLengths);
            double stdDevEmailLength = calcStandardDeviation(emailLengths);

            // Outputs results
            System.out.println("Mean Email Length: " + meanEmailLength);
            System.out.println("Standard Deviation of Email Length: " + stdDevEmailLength);

            // Calculates cosine similarity and saves the results
            calcAndSaveCosineSim(emailDataList);

            // Calculates predictions and saves the results
            // Threhold for classfiying emails as spam or not sm 
            double threshold = 0.7;
            calcAndSavePredictions(emailDataList, threshold);

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            // Handles the validation exception
        } finally {
            try {
                if (csvReader != null) { // Prevents a NullPointerException
                    // Closes the CSV reader when done
                    csvReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }   

    // Function to calculate word frequency
    private static Map<String, Integer> calcWordFrequency(String email) {
        Map<String, Integer> wordFrequency = new HashMap<>();
        
        // Split email content into words based on spaces
        String[] words = email.split(" ");
        for (String word : words) {
            wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
        }
        return wordFrequency;
    }

    // Function to calculate the mean
    private static double calcMean(List<Integer> data) {
        double sum = data.stream().mapToInt(Integer::intValue).sum(); // Uses Java Streams to calculate the sum of all integers in data (Converts integers in 'data' into 'IntStream' by calling 'mapToInt(Integer::intValue)' on it can calculates the sum with '.sum()')
        return sum / data.size(); // Calculates the mean by dividing sum by the number of elements ('data.size()')
    }

    // Function to calcuate the standard deviation
    private static double calcStandardDeviation(List<Integer> data) {
        double mean = calcMean(data); // Calls 'calcMean' function to calculate the mean of 'data'
        double sumOfSquaredDiffs = data.stream() // Uses Java Streams to calculate the sum of squared differences between each data point and mean 
            .mapToDouble(value -> Math.pow(value - mean, 2)) // It converts integers in 'data' into a 'DoubleStream' by calculating how far the value is from the mean and squaring the difference
            .sum();                                // Then, it calculates the sum of these squared differences
        return Math.sqrt(sumOfSquaredDiffs / (data.size() - 1)); // Gets sqrt of sum of squared differences divided by the degrees of freedom ('data.size()'' minus 1)
    }

    // Function to calculate cosine similarity between two word frequeny maps
    private static double calcAndSaveCosineSim(Map<String, Integer> a, Map<String, Integer> b) { // The function has parameters representing word frequency maps 'a' and 'b' and returns 'double', the calculated cosine similarity 
        // Calculates dot product
        double dotProduct = a.entrySet().stream() // Initializes 'dotProduct' and allows entry key-value pairs) from the map 'a' to compare the cosine similarity between both maps
            .filter(e -> b.containsKey(e.getKey())) // Checks if entry 'a' (now 'e') has matching keys in map 'b' for comparison
            .mapToDouble(e -> e.getValue() * b.get(e.getKey())) // Filters entry by calculating the product of both map's frequencies  
            .sum(); // Adds all products to find the dot product between word frequency vecotrs of maps 'a' and 'b'
        // Calculate magntudes, magnitudeA and magnitudeA are of the word frequency vectors for map 'a' and 'b'. 
        // The magnitude of a vector here is the squre root of the sum of the sqaures of frequencies 
        double magnitudeA = Math.sqrt(a.values().stream().mapToDouble(i -> i * i).sum()); // Calculates magnitudeA by extracting map the 'a' values, squares, and gets the sum of the squared values 
        double magnitudeB = Math.sqrt(b.values().stream().mapToDouble(i -> i * i).sum()); // Calculates magnitudeB similar to that of magnitudeA
        // Calculate cosine similarity
        return dotProduct / (magnitudeA * magnitudeB);
    }

    // Function to calculate and save cosine similarity scores
    private static void calcAndSaveCosineSim(List<EmailData> emailDataList) {
        String outputFilePath = "cosine_similarity_score.csv"; // Specifies the name and path for 'FileWriter' to output the file for cosine similarity scores
        try (FileWriter writer = new FileWriter(outputFilePath)) { // Opens a 'FileWriter' called 'writer' for the output file and when 'FileWriter' exits the try block, it stops writing
            // Writes header line for output file 
            writer.write("Email1, Email2, CosineSimilarity\n"); // Writes header line for output file
            // Calculates and saves cosine similarity scores
            for (int c = 0; c < emailDataList.size(); c++) { // Iterates through each email in 'emailDataList' with 'c' as the index 
                for (int s = c + 1; s < emailDataList.size(); s++) { // Interates through remaining emails in 'emailDataList' compare index 'c' to 's' (the index for this loop) 
                    double similarity = calcAndSaveCosineSim(emailDataList.get(c).getWordFrequency(), emailDataList.get(s).getWordFrequency()); // Calculates cosine similarity between the two emails using indices 'c' and 's'
                    writer.write(emailDataList.get(c).getEmail() + "," + similarity + "\n"); // Writes email indices and similarity score in the output file
                }
            }
        } catch (IOException e) { // Catches exceptions when working with the output file and prints the exception for diagnosis
            e.printStackTrace();
        }
    }
*?
    // Functions to calculate and save predictions based on cosine similarity
    private static void calcAndSavePredictions(List<EmailData> emailDataList, double threshold) {
        String outputFilePath = "predictions.txt"; // Specifies the name and path for 'FileWriter' to output the file for predictions
        try (FileWriter writerPred = new FileWriter(outputFilePath)) {
            // Writes header line for output file
            writerPred.write("Email, Prediction\n");
            // Calculates and saves predictions 
            for (int c = 0; c < emailDataList.size(); c++) { // Iterates through each email in 'emailDataList' with 'c' as the index
                double maxSimilarity = 0.0; // Initializes a double to 0.0
                for (int s = 0; s < emailDataList.size(); s++ ) { // Iterates through each email in 'emailDataList' with 'c' as the index
                    if (c != s) { // Checks if indices 'c' and 's' are not equal or exclude self comparison of email
                        // Calculates cosine similarity between current email and another email
                        double similarity = calcAndSaveCosineSim(emailDataList.get(c).getWordFrequency(), emailDataList.get(s).getWordFrequency());
                        // Keeps track of the resulting maxium similarity
                        if (similarity > maxSimilarity) { // Updates 'maxSimilarity' to the highest cosine similarity value
                            maxSimilarity = similarity;   // resulting from the comparison of the current email with others in the dataset
                        }
                    }
                }
                // Classifies based on the threshold by checking if the highest cosine similarity value 
                // compared to other emails in the dataset, is greater than the threshold value
                int prediction;
                if (maxSimilarity > threshold) { // If 'maxSimilarity' is greater than the 'threshold', there's a greater similarity to at least one other email in the dataset 
                    prediction = 1; // Not spam
                } else {
                    prediction = 0; // Spam
                }
                // Write the email and the associating predicted label (0 or 1) to the output file
                writerPred.write(emailDataList.get(c).getEmail() + "," + prediction + "\n");
            }
        } catch (IOException e) { // Catches exceptions when working with the output file and prints the exception for diagnosis
            e.printStackTrace();
        }
    }
}