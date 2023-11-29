import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.NavigableMap;

import static java.time.format.DateTimeFormatter.ofPattern;

public class Spacerace {
    public String[] artLines;
    private volatile boolean keepTwinkling = true;
    private NavigableMap<Integer, String> degreeArtMap = new TreeMap<>();

    public static double calculateEarthPosition(LocalDate date) {
        // Reference date: January 1st, 2000, where Earth is considered to be at 0 degrees
        LocalDate referenceDate = LocalDate.of(2000, 1, 1);

        // Days between reference date and target date
        long daysBetween = ChronoUnit.DAYS.between(referenceDate, date);

        // Approximate orbital position (in a very simplified model, not accurate for real-world usage)
        // Earth moves roughly 360/365.25 degrees per day along its orbit
        double position = (daysBetween * 360.0 / 365.25) % 360;

        return position;
    }


    /*************************
     *   HEART OF ASCII ART   *
     *************************/
    public Spacerace(){

        degreeArtMap.put(0, "/home/riverx/DS1/javaprava/Spaceracer/space010.txt");
        degreeArtMap.put(20, "/home/riverx/DS1/javaprava/Spaceracer/space2030.txt");
        degreeArtMap.put(40, "/home/riverx/DS1/javaprava/Spaceracer/space4050.txt");
        degreeArtMap.put(60, "/home/riverx/DS1/javaprava/Spaceracer/space6070.txt");
        //degreeArtMap.put(90, "/home/riverx/DS1/javaprava/Spaceracer/space2030.txt");
        //degreeArtMap.put(120, "/home/riverx/DS1/javaprava/Spaceracer/space2030.txt");
        //degreeArtMap.put(140, "/home/riverx/DS1/javaprava/Spaceracer/space2030.txt");
        //degreeArtMap.put(180, "/home/riverx/DS1/javaprava/Spaceracer/space2030.txt");
        //degreeArtMap.put(200, "/home/riverx/DS1/javaprava/Spaceracer/space2030.txt");
        //degreeArtMap.put(220, "/home/riverx/DS1/javaprava/Spaceracer/space2030.txt");
        //degreeArtMap.put(240, "/home/riverx/DS1/javaprava/Spaceracer/space2030.txt");
        //degreeArtMap.put(260, "/home/riverx/DS1/javaprava/Spaceracer/space2030.txt");
        //degreeArtMap.put(280, "/home/riverx/DS1/javaprava/Spaceracer/space2030.txt");
        //degreeArtMap.put(300, "/home/riverx/DS1/javaprava/Spaceracer/space2030.txt");
        degreeArtMap.put(320, "/home/riverx/DS1/javaprava/Spaceracer/space32030.txt");
        degreeArtMap.put(340, "/home/riverx/DS1/javaprava/Spaceracer/space34050.txt");
    }

    public void loadAsciiArtForCurrentPosition(double position) {
        String filePath;
        // Check if it's the winter solstice
        if (LocalDate.now().equals(LocalDate.of(LocalDate.now().getYear(), 12, 21))) {
            filePath = degreeArtMap.get(320); // Load solstice art
        } else {
            // Find the appropriate file for the current position
            Integer key = degreeArtMap.floorKey((int) position);
            if (key == null) {
                key = 0; // Default to the first range if position is below the lowest key
            }
            filePath = degreeArtMap.get(key);
        }

        // Load the ASCII art from the file path
        try {
            asciiArt(filePath);
        } catch (IOException e) {
            System.out.println("Error loading ASCII art: " + e.getMessage());
        }
    }

    public void asciiArt(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        artLines = lines.toArray(new String[0]);
    }

    public static void displayAsciiArt(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading ASCII art file: " + e.getMessage());
        }
    }

    public void startTwinkling() {
        Thread twinklingThread = new Thread(() -> {
            try {
                twinklingEffect();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        twinklingThread.start();
    }


    public void twinklingEffect() throws InterruptedException {
        Random random = new Random();
        char[] starFadeChars = {'*', '+', '.', ' '}; // Fading characters for '*'
        char[] plusFadeChars = {'┼', '├', '─', ' '}; // Fading characters for '┼'
        int maxFadeIndex = starFadeChars.length - 1;
        String[] originalArt = Arrays.copyOf(artLines, artLines.length);

        DateTimeFormatter mediumClockedIn = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);
        LocalDate date = LocalDate.now();
        double position = calculateEarthPosition(date);
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        while (keepTwinkling) {
            clearScreen();

            // Update twinkling effect
            for (int j = 0; j < artLines.length; j++) {
                char[] lineChars = artLines[j].toCharArray();
                for (int k = 0; k < lineChars.length; k++) {
                    if (random.nextBoolean()) {
                        int fadeIndex = random.nextInt(maxFadeIndex + 1);
                        if (originalArt[j].charAt(k) == '*') {
                            lineChars[k] = starFadeChars[fadeIndex];
                        } else if (originalArt[j].charAt(k) == '┼') {
                            lineChars[k] = plusFadeChars[fadeIndex];
                        }
                    }
                }
                artLines[j] = new String(lineChars);
            }

            // Update time
            LocalTime updatedTime = LocalTime.now();
            String updatedFormattedTime = updatedTime.format(mediumClockedIn);
            OVER_WRITE(16, 195, String.valueOf(position), 17, 188, formattedDate, 18, 188, updatedFormattedTime);

            displayArt();
            Thread.sleep(1000); // Adjust for desired speed
            artLines = Arrays.copyOf(originalArt, originalArt.length);
        }
    }





    public void stopTwinkling() {
        keepTwinkling = false;
    }


    private void clearScreen() {
        // Clear the console - this method is platform-dependent.
        // For Unix/Linux/Mac:
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Finds the positions of two specified markers in the array
     *
     * @param marker1 The first marker character to find.
     * @param marker2 The second marker character to find.
     */
    public void findMarkerPositions(char marker1, char marker2) {
        for (int i = 0; i < artLines.length; i++) {
            for (int j = 0; j < artLines[i].length(); j++) {
                if (artLines[i].charAt(j) == marker1) {
                    System.out.println("Marker '" + marker1 + "' found at position: (" + i + ", " + j + ")");
                }
                if (artLines[i].charAt(j) == marker2) {
                    System.out.println("Marker '" + marker2 + "' found at position: (" + i + ", " + j + ")");
                }
            }
        }
    }

    public void OVER_WRITE(int row1, int col1, String new_row1, int row2, int col2, String new_row2, int row3, int col3, String new_row3) {
        artLines[row1] = replaceSubstring(artLines[row1], col1, String.valueOf(new_row1));
        artLines[row2] = replaceSubstring(artLines[row2], col2, String.valueOf(new_row2));
        artLines[row3] = replaceSubstring(artLines[row3], col3, String.valueOf(new_row3));
    }

    private String replaceSubstring(String str, int startIndex, String newSubstring) {
        if (startIndex < 0 || startIndex >= str.length()) {
            return str; // Index out of bounds, return the original string
        }
        int endIndex = startIndex + newSubstring.length();
        if (endIndex > str.length()) {
            endIndex = str.length();
        }
        return str.substring(0, startIndex) + newSubstring + str.substring(endIndex);
    }

    public void displayArt() {
        for (String line : artLines) {
            System.out.println(line);
        }
    }


    /********************
    * HEART OF DARKNESS *
    * ******************/
     /*********************************************************
     * The main function handles most of the display logic.   *
     * Try section is implemented to display specific text    *
     * based on degree calculated. Date is presented and      *
     * special cases are included for the solstices and shit. *
     *********************************************************/

    public static void main(String[] args) {
        Spacerace spacerace = new Spacerace();
        Scanner scanner = new Scanner(System.in);
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();

        DateTimeFormatter Clocked_in = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
        DateTimeFormatter formatter = ofPattern("yyyy-MM-dd"); // Define your desired date format

        String formattedDate = date.format(formatter);
        String formattedTime = time.format(Clocked_in);

        LocalDate winterSolstice = LocalDate.of(date.getYear(), 12, 21);
        String solsticeAsciiPath = "/home/riverx/DS1/javaprava/Spaceracer/solstice.txt";

        LocalDate vernalEquinox = LocalDate.of(date.getYear(), 03, 20);
        String vernalAsciiPath = "/home/riverx/DS1/javaprava/Spaceracer/vernaleq.txt";

        double position = calculateEarthPosition(date);
        System.out.println("Approximate position of the Earth on " + date + ": " + position + " degrees");

        spacerace.startTwinkling();
        System.out.println("Press 'x' to stop twinkling...");

        //Implement all special date handling here

        /**********************
        * HERE LIES THE TRIES *
        * ********************/

        try {
            if(date.equals(winterSolstice)) {
                spacerace.asciiArt(solsticeAsciiPath);
            } else if(date.equals(vernalEquinox)){
                spacerace.asciiArt(vernalAsciiPath);
            } else {
                spacerace.loadAsciiArtForCurrentPosition(position);
            }
            //spacerace.findMarkerPositions('!', '=');
            spacerace.OVER_WRITE(16, 195, String.valueOf(position), 17, 188, formattedDate, 18, 188, String.valueOf(formattedTime));
            spacerace.displayArt(); // Display initial art
        } catch (IOException e) {
            System.out.println("Error loading ASCII art: " + e.getMessage());
        }

        spacerace.startTwinkling(); // Start the twinkling effect
        System.out.println("Press 'x' to stop twinkling...");

        while (true) {
            String input = scanner.nextLine();
            if ("x".equalsIgnoreCase(input)) {
                spacerace.stopTwinkling(); // Stop the twinkling effect
                break;
            }
        }

        scanner.close();
    }
}
