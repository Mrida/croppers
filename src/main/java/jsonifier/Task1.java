package jsonifier;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Task1 {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Folder should be specified as param");
            return;
        }

        File dir = new File(args[0]);
        String DESTINATION_SUBDIR = args.length > 1 ? args[1] : "updated";

        File outputDir = new File(Paths.get(dir.getAbsolutePath(), DESTINATION_SUBDIR).toUri());

        System.out.println("choose 1 for Centurion, 2 for Legendary and 3 for Raging. Choose 5 for SHA-256 or 6 to read addresses from excel file, Or choose 9 for reorder.");

        final Scanner scanner = new Scanner(System.in);
        final int res = scanner.nextInt();

        final String pantherType;
        if (res == 1) {
            pantherType = "CENTURION";
            if (!dir.isDirectory()) {
                System.out.println("input should be a directory");
                return;
            }
        } else if (res == 2) {
            pantherType = "Legendary";
            if (!dir.isDirectory()) {
                System.out.println("input should be a directory");
                return;
            }
        } else if (res == 3) {
            pantherType = "Raging";
            if (!dir.isDirectory()) {
                System.out.println("input should be a directory");
                return;
            }
        } else if (res == 5) {
            if (!dir.isDirectory()) {
                System.out.println("input should be a directory");
                return;
            }
            System.out.println("The provenance hash is: " + hash(dir));
            return;
        }else if (res == 6) {
            if (dir.isDirectory()) {
                System.out.println("input should be a file");
                return;
            }
            System.out.println("The output array is: " + whitelist(dir));
            return;
        } else if (res == 9) {
            if (!dir.isDirectory()) {
                System.out.println("input should be a directory");
                return;
            }
            reorder(dir, outputDir);
            return;
        } else {
            throw new RuntimeException("Input not allowed");
        }

        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        for (File file : dir.listFiles()) {
            if (!file.isDirectory()) {
                if (isJson(file)) {

                    //read
                    final JSONObject element = elements(file);

                    //manipulate
                    element.remove("Type");
                    element.remove("date");
                    final JSONArray attributes = ((JSONArray) element.get("attributes"));
                    JSONObject trait = new JSONObject();
                    trait.put("trait_type", "RANK");
                    trait.put("value", pantherType);
                    attributes.add(trait);

                    for (Object attribute : attributes) {
                        JSONObject aTrait = (JSONObject) attribute;
                        if (aTrait.get("value").equals("Empty")) {
                            aTrait.put("value", "None");
                        }
                    }

                    //write
                    final Path dest = java.nio.file.Files.createFile(Paths.get(dir.getAbsolutePath(), DESTINATION_SUBDIR, file.getName()));
                    final FileWriter destFile = new FileWriter(new File(dest.toUri()));
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    JsonElement je = JsonParser.parseString(element.toJSONString());
                    String prettyJsonString = gson.toJson(je);
                    destFile.write(prettyJsonString);
                    destFile.flush();

                } else {
                    System.out.println(file.getName() + " was skipped as it is not an image");
                }
            }
        }

    }

    private static String whitelist(File csvFile) throws IOException {
        List<String> addresses = java.nio.file.Files.readAllLines(Paths.get(csvFile.getAbsolutePath())).stream().flatMap(x -> Arrays.stream(x.split(",,,,,,,,,,,,,,"))).filter(x -> x.startsWith("0x")).collect(
            Collectors.toList());
        System.out.println("You have " + addresses.size() + " whitelisted addresses");
        return "[" + addresses.stream().map(x-> "\"" + x + "\"").collect(Collectors.joining(",")) + "]";
    }

    private static String hash(File dir) {
        final String hashes = Arrays.stream(dir.listFiles()).filter(x -> !x.isDirectory()).map(x -> {
            try {
                String hash = Files.hash(new File(x.getAbsolutePath()), Hashing.sha256()).toString();
                System.out.println("hash for " + x.getName() + " is " + hash);
                return hash;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.joining(""));

        return Hashing.sha256().hashString(hashes, StandardCharsets.UTF_8).toString();
    }

    private static void reorder(File file, File outputDir) throws IOException {
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        final List<File> collect = Arrays.stream(file.listFiles()).filter(x -> !x.isDirectory()).sorted(

            (f1, f2) -> {
                try {
                    int i1 = Integer.parseInt(f1.getName().substring(0, f1.getName().lastIndexOf(".")));
                    int i2 = Integer.parseInt(f2.getName().substring(0, f2.getName().lastIndexOf(".")));
                    return i1 - i2;
                } catch (NumberFormatException e) {
                    throw new AssertionError(e);
                }
            }

                                                                                                       ).collect(Collectors.toList());
        int i = 1;
        for (File f : collect) {
            Path copied = Paths.get(outputDir.getAbsolutePath(), i++ + ".json");
            java.nio.file.Files.copy(Paths.get(f.getAbsolutePath()), copied, new StandardCopyOption[] { StandardCopyOption.REPLACE_EXISTING });
        }
    }

    static JSONObject elements(File file) throws Exception {
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(file)) {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            return (JSONObject) obj;
        }

    }

    private static boolean isJson(File file) {
        final String substring = file.getName().substring(file.getName().lastIndexOf('.') + 1);
        return substring.equalsIgnoreCase("json") || substring.equalsIgnoreCase("txt");
    }

}
