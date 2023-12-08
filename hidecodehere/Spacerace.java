import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.NavigableMap;
import static java.time.format.DateTimeFormatter.ofPattern;


/*
* This is a rough estimation of where the earth currently is in its orbit path.
* It is a text based ascii drawing implemented by reprinting a character array
* over and over to create a slight animation effect. The stars are programed to
* twinkle at a steady rate while also incrementing seconds on the clock to avoid
* thread issues. In progress are more animations and up to date information in the
* text sections.
*
 */
public class Spacerace {
    public String[] artLines;
    private Thread twinklingThread;
    private NavigableMap<Integer, String> degreeArtMap = new TreeMap<>();


    /*****************************************************************************************************
    * Designed for an approximation of earths orbit based on treating it as a 360 degree circle.         *
    * This project is not suitable for the actual location of earth on its ellipse. More advanced        *
    * calculations may be done in the future.                                                            *
    * ***************************************************************************************************/

    public static double calculateEarthPosition(LocalDate date) {
        // Reference date: January 1st, 2000, where Earth is considered to be at 0 degrees
        LocalDate referenceDate = LocalDate.of(2000, 1, 1);
        long daysBetween = ChronoUnit.DAYS.between(referenceDate, date);

        // Earth moves roughly 360/365.25 degrees per day along its orbit
        return (daysBetween * 360.0 / 365.25) % 360;
    }


    /*************************
     *   HEART OF ASCII ART   *
     *************************/
    public Spacerace(){

        degreeArtMap.put(0,   "asciiArt/space010.txt");
        degreeArtMap.put(20,  "asciiArt/space2030.txt");
        degreeArtMap.put(40,  "asciiArt/space4050.txt");
        degreeArtMap.put(60,  "asciiArt/space6070.txt");
        degreeArtMap.put(90,  "asciiArt/space90110.txt");
        degreeArtMap.put(120, "asciiArt/space120130.txt");
        degreeArtMap.put(140, "asciiArt/space140150.txt");
        degreeArtMap.put(160, "asciiArt/space160170.txt");
        degreeArtMap.put(180, "asciiArt/space180190.txt");
        degreeArtMap.put(200, "asciiArt/space200210.txt");
        degreeArtMap.put(220, "asciiArt/space220230.txt");
        degreeArtMap.put(240, "asciiArt/space240260.txt");
        degreeArtMap.put(270, "asciiArt/space270290.txt");
        degreeArtMap.put(300, "asciiArt/space300310.txt");
        degreeArtMap.put(320, "asciiArt/space32030.txt");
        degreeArtMap.put(340, "asciiArt/space34050.txt");
    }

     /*************************************************
     * Takes position calculated and finds the        *
     * corresponding filepath in the Navigable Map    *
     *************************************************/

    public void loadAsciiArtForCurrentPosition(double position) {
        String filePath;
        // Find the appropriate file for the current position
        Integer key = degreeArtMap.floorKey((int) position);
        if (key == null) {
            key = 0; // Default to the first range if position is below the lowest key
        }
        filePath = degreeArtMap.get(key);
        // Load the ASCII art from the file path
        try {
            asciiArt(filePath);
        } catch (IOException e) {
            System.out.println("Error loading ASCII art: " + e.getMessage());
        }
    }

     /********************************************
     * Creates the array from the file path      *
     * specified. Needed for twinkling and clock. *
     ********************************************/

    public void asciiArt(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        artLines = lines.toArray(new String[0]);
    }

     /********************************
     * Starts the twinkling function.*
     ********************************/
    public void startTwinkling() {
        twinklingThread = new Thread(() -> {
            try {
                twinklingEffect();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        twinklingThread.start();
    }

    /************************************************************************************
     * Searches for my target characters which represent stars and slowly replaces them *
     * with different characters to simulate twinkling. The clear screen function is    *
     * called to keep the presentation focused on the current character array.          *
     ***********************************************************************************/


    public void twinklingEffect() throws InterruptedException {
        Random random = new Random();
        char[] starFadeChars = {'*', '+', '.', ' '};
        char[] plusFadeChars = {'┼', '├', '─', ' '}; // Fading characters for '┼'
        int maxFadeIndex = starFadeChars.length - 1;
        int maxPlusFadeIndex = plusFadeChars.length -1;
        String[] originalArt = Arrays.copyOf(artLines, artLines.length);

        DateTimeFormatter mediumClockedIn = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate date = LocalDate.now();
        double position = calculateEarthPosition(date);
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        //infinite loop, the stars will shine until the program is terminated
        while (true) {
            clearScreen();
            for (int j = 0; j < artLines.length; j++) {
                char[] lineChars = artLines[j].toCharArray();
                for (int k = 0; k < lineChars.length; k++) {
                    if (originalArt[j].charAt(k) == '*' && random.nextFloat() < 0.4) {
                        int fadeIndex = random.nextInt(maxFadeIndex + 1);
                        lineChars[k] = starFadeChars[fadeIndex];
                    }
                    else if (originalArt[j].charAt(k) == '┼' && random.nextFloat() < 0.4) {
                        int fadeIndex = random.nextInt(maxPlusFadeIndex + 1);
                        lineChars[k] = plusFadeChars[fadeIndex];
                    }
                }
                artLines[j] = new String(lineChars);
            }
            LocalTime updatedTime = LocalTime.now();
            String updatedFormattedTime = updatedTime.truncatedTo(ChronoUnit.MINUTES).format(mediumClockedIn);
            OVER_WRITE(16, 195, String.valueOf(position), 17, 188, formattedDate, 18, 188, updatedFormattedTime);

            displayArt();
            Thread.sleep(1000); // Adjust for desired speed
            artLines = Arrays.copyOf(originalArt, originalArt.length);
        }
    }


     /***************************************************************
     * Clears the previous string array output to help with format. *
     ***************************************************************/


    private void clearScreen() {
        // Clear the console - this method is platform-dependent.
        // For Unix/Linux/Mac:
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

     /******************************************************************************
     * Finds the positions of two specified markers in the array. Use this to find *
     * and mark locations in the array for desired changes.                        *
     ******************************************************************************/
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



    /******************************************************************************************************************
     * Locations of the markers as well as the desired new string are passed in to overwrite with time, date, coord.  *
     *****************************************************************************************************************/

    public void OVER_WRITE(int row1, int col1, String new_row1, int row2, int col2, String new_row2, int row3, int col3, String new_row3) {
        artLines[row1] = replaceSubstring(artLines[row1], col1, String.valueOf(new_row1));
        artLines[row2] = replaceSubstring(artLines[row2], col2, String.valueOf(new_row2));
        artLines[row3] = replaceSubstring(artLines[row3], col3, String.valueOf(new_row3));
    }

     /********************************************************************************
     * Logic to input actual time, date, and coordinates on their respective markers *
     ********************************************************************************/

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

    /*****************************************
    * Print out every character in the array *
    *****************************************/
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
     * special cases are included for solstices/equinoxes.    *
     *********************************************************/

    public static void main(String[] args) {
        Spacerace spacerace = new Spacerace();
        Scanner scanner = new Scanner(System.in);
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();

        DateTimeFormatter Clocked_in = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter formatter = ofPattern("yyyy-MM-dd"); // Define your desired date format

        String formattedDate = date.format(formatter);
        String formattedTime = time.format(Clocked_in);

        LocalDate winterSolstice = LocalDate.of(date.getYear(), 12, 21);
        String solsticeAsciiPath = "asciiArt/solstice.txt";

        LocalDate vernalEquinox = LocalDate.of(date.getYear(), 3, 20);
        String vernalAsciiPath = "asciiArt/vernaleq.txt";

        LocalDate summerSolstice = LocalDate.of(date.getYear(), 6, 21);
        String summerSolPath = "asciiArt/summersolstice.txt";

        LocalDate autumnEquinox = LocalDate.of(date.getYear(), 9, 22);
        String autumnEqPath = "asciiArt/autumneq.txt";

        double position = calculateEarthPosition(date);

        //Special case handling
        try {
            if(date.equals(winterSolstice)) {
                spacerace.asciiArt(solsticeAsciiPath);
            } else if(date.equals(vernalEquinox)) {
                spacerace.asciiArt(vernalAsciiPath);
            } else if (date.equals(summerSolstice)) {
                spacerace.asciiArt(summerSolPath);
            } else if (date.equals(autumnEquinox)) {
                spacerace.asciiArt(autumnEqPath);
            }else {
                spacerace.loadAsciiArtForCurrentPosition(position);
            }
            //spacerace.findMarkerPositions('!', '=');
            spacerace.OVER_WRITE(16, 195, String.valueOf(position), 17, 188, formattedDate, 18, 188, String.valueOf(formattedTime));
            spacerace.displayArt(); // Display initial art
        } catch (IOException e) {
            System.out.println("Error loading ASCII art: " + e.getMessage());
        }
        spacerace.startTwinkling();
        scanner.close();
    }
}
