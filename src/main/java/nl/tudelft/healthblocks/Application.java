package nl.tudelft.healthblocks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main class for the backend (server).
 */
@SpringBootApplication
public class Application {

    /**
     * The main method of the main backend class.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
