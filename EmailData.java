public class EmailData { // Holds email-related data and gives a constructor to initialize private fields 
    private String email;
    private int label;
    private int emailLength;
    private Map<String, Integer> wordFrequency; // Map that stores word frequencies. Each word in the email is a key and the value is the word's frequency in the email 

    public EmailData(String email, int label, int emailLength, Map<String, Integer> wordFrequency) { // Initializes 'EmailData' object with the values for email, label, email length, and word frequencies 
        this.email = email; // These are the corresponding instance varaibles the parameter is is intialized to
        this.label = label;
        this.emailLength = emailLength;
        this.wordFrequency = wordFrequency;
    }
    // Getter Methods
    public String getEmail() {
        return email;
    }

    public int getLabel() {
        return label;
    }

    public int getEmailLength() {
        return emailLength;
    }

    public Map<String, Integer> getWordFrequency() {
        return wordFrequency;
    }
}