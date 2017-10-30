import java.io.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itextpdf.text.DocumentException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("ERROR: wrong number of arguments");
            System.out.println("USAGE: <input_file>");
            System.out.println("EXAMPLE: test.pdf");
            System.exit(1);
        }

        try {
            String inputFile = args[0];
            Pdf pdf = new Pdf(inputFile);

            if (Options.OUTPUT) {
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
                System.out.println(gson.toJson(pdf));
            }
        } catch (IOException|DocumentException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}