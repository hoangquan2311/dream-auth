package dream.dreamauth;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static dream.dreamauth.AdvancedEncryption.*;
import static dream.dreamauth.DreamAuth.ye_1lre;

public class FileOperator {
    public static final Logger LOGGER = LoggerFactory.getLogger("DreamAuth");
    public static Map<String, Password> passMap = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File PASS_FILE;
    public static Map<String, Password> checkOldFile(MinecraftServer server){
        Path filePath = server.getSavePath(WorldSavePath.ROOT).resolve("dreamauth-password.json");
        if(Files.exists(filePath)){
            File oldFile = Paths.get(filePath.toUri()).toFile();
            Map<String, Password> oldPass;
            try (FileReader reader = new FileReader(oldFile)) {
                Type type = new TypeToken<Map<String, Password>>() {}.getType();
                oldPass = GSON.fromJson(reader, type);
                if (oldPass != null) {
                    return oldPass;
                }
            } catch (IOException e) {
                LOGGER.warn(LOGGER.getName() + ": Failed to load old password file");
            }
        }
        return null;
    }
    public static void loadFile(MinecraftServer server){
        Path filePath = server.getSavePath(WorldSavePath.ROOT).resolve("dreamauth.json");
        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath);
                PASS_FILE = Paths.get(filePath.toUri()).toFile();
                try {
                    Map<String, Password> oldPass = checkOldFile(server);
                    if (oldPass != null){
                        deleteOldFile(server);
                        for (String key : oldPass.keySet()){
                            String oldPlainPass = oldPass.get(key).getPassword();
                            String newCipherPass = encrypt(oldPlainPass,stringToSecretKey(ye_1lre));
                            oldPass.get(key).setPassword(newCipherPass);
                        }
                        passMap.putAll(oldPass);
                        syncWithFile();
                    }
                    else {
                        passMap = new HashMap<>();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            deleteOldFile(server);
            PASS_FILE = Paths.get(filePath.toUri()).toFile();
            try (FileReader reader = new FileReader(PASS_FILE)) {
                Type type = new TypeToken<Map<String, Password>>() {}.getType();
                passMap = GSON.fromJson(reader, type);
                if (passMap == null) {
                    passMap = new HashMap<>();
                }
            } catch (IOException e) {
                LOGGER.warn(LOGGER.getName() + ": Failed to load password from JSON file.");
            }
        }
    }
    public static String getDecryptedPassword(String name) {
        String password = "";
        try {password = decrypt(passMap.get(name).getPassword(),stringToSecretKey(ye_1lre));}
        catch (Exception ignored){}
        return password;
    }
    public static void updatePassword(String name, Password password) {
        passMap.put(name,password);
        syncWithFile();
    }
    private static void syncWithFile(){
        try (FileWriter writer = new FileWriter(PASS_FILE)) {
            GSON.toJson(passMap, writer);
        } catch (IOException e) {
            LOGGER.warn("Failed to sync with JSON file.");
        }
    }
    private static void deleteOldFile(MinecraftServer server){
        Path filePath = server.getSavePath(WorldSavePath.ROOT).resolve("dreamauth-password.json");
        if(Files.exists(filePath)) {
            File oldFile = Paths.get(filePath.toUri()).toFile();
            LOGGER.info(LOGGER.getName()+" Delete old file state: "+ oldFile.delete());
        }
    }
}
