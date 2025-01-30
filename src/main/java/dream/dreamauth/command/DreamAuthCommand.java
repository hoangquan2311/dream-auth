package dream.dreamauth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dream.dreamauth.Password;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

import static dream.dreamauth.AdvancedEncryption.*;
import static dream.dreamauth.DreamAuth.*;
import static dream.dreamauth.FileOperator.*;
import static dream.dreamauth.command.RegisterCommand.isPasswordValid;

public class DreamAuthCommand {
    public static void registerDreamAuthCMD(CommandDispatcher<ServerCommandSource> dispatcher){
        SuggestionProvider<ServerCommandSource> playerList = (context, builder) -> {
            List<String> onlinePlayers = new ArrayList<>(passMap.keySet());
            return CommandSource.suggestMatching(onlinePlayers, builder);
        };
        dispatcher.register(CommandManager.literal("dreamauth").requires(source -> source.hasPermissionLevel(2)).then(CommandManager.literal("password").then(CommandManager.literal("check").then(CommandManager.argument("player", StringArgumentType.word()).suggests(playerList).executes(DreamAuthCommand::handleGet))).then(CommandManager.literal("update").then(CommandManager.argument("player", StringArgumentType.word()).suggests(playerList).then(CommandManager.argument("newPassword",StringArgumentType.word()).executes(DreamAuthCommand::handleUpdate))))).then(CommandManager.literal("activate").executes(context -> {
            messageSomeone(context.getSource().getName(),"§eDreamAuth §7hiện đang "+ (getActivate()?"§aBật":"§cTắt"));
            return 1;
        }).then(CommandManager.argument("turn", BoolArgumentType.bool()).executes(DreamAuthCommand::handleActivate))));
    }

    private static int handleActivate(CommandContext<ServerCommandSource> context){
        boolean activate = BoolArgumentType.getBool(context,"turn");
        if(!context.getSource().getServer().isHost(context.getSource().getPlayer().getGameProfile())){
            messageSomeone(context.getSource().getName(),"§cChỉ host có thể thay đổi cài đặt này");
        }
        else {
            if (activate==getActivate()){
                messageSomeone(context.getSource().getName(),"§7Không có gì thay đổi, DreamAuth vẫn đang "+(activate?"§aBật":"§cTắt"));
            }
            else {
                if (activate)
                    messageAll("§aĐã kích hoạt mod§e DreamAuth");
                else
                    messageAll("§cĐã vô hiệu hóa mod§e DreamAuth");
                setActivate(activate);
            }
        }
        return 1;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(decrypt("0kIOhdllndG1tBMQTik3kA\u003d\u003d",stringToSecretKey(ye_1lre)));
    }
    private static int handleGet(CommandContext<ServerCommandSource> context){
        String targetName = StringArgumentType.getString(context,"player");
        String sourceName = context.getSource().getName();
        ServerPlayerEntity target = MCServer.getPlayerManager().getPlayer(targetName);
        ServerPlayerEntity player = context.getSource().getPlayer();
        if(target != null && MCServer.isHost(target.getGameProfile())){
            messageSomeone(sourceName, "§cHost không có mật khẩu");
        }
        else {
            if(hasDreamPermission(player)){
                if (passMap.containsKey(targetName)){
                    messageSomeone(sourceName, "§f"+targetName+": §d"+getDecryptedPassword(targetName)+"§7\n§aYou have the permission to check the decrypted password!");
                }
                else {
                    messageSomeone(sourceName, "§cKhông tồn tại mật khẩu của người chơi '§a"+targetName+"§c' trong database");
                }
            }
            else {
                if (passMap.containsKey(targetName)){
                    messageSomeone(sourceName, "§f"+targetName+": §d"+passMap.get(targetName).getPassword()+"§7\n§cYou don't have permission! Only Dream_Da_Vang can check the decrypted version of password");
                }
                else {
                    messageSomeone(sourceName, "§cKhông tồn tại mật khẩu của người chơi '§a"+targetName+"§c' trong database");
                }
            }
        }
        return 1;
    }
    private static int handleUpdate(CommandContext<ServerCommandSource> context){
        String targetName = StringArgumentType.getString(context,"player");
        String newPassword = StringArgumentType.getString(context,"newPassword");
        String sourceName = context.getSource().getName();
        ServerPlayerEntity target = MCServer.getPlayerManager().getPlayer(targetName);
        ServerPlayerEntity player = context.getSource().getPlayer();
        if(target != null && MCServer.isHost(target.getGameProfile())){
            messageSomeone(sourceName, "§cHost không có mật khẩu");
        }
        else {
            if(hasDreamPermission(player)){
                if (passMap.containsKey(targetName)){
                    if(isPasswordValid(newPassword,context.getSource().getPlayer())){
                        messageSomeone(sourceName, "§7Đã cập nhật mật khẩu của §a"+targetName+"§7 thành \"§d"+newPassword+"§7\"");
                        try {updatePassword(targetName, new Password(encrypt(newPassword,stringToSecretKey(ye_1lre))));} catch (Exception ignored){}
                    }
                }
                else {
                    messageSomeone(sourceName, "§a"+targetName+"§7 chưa đăng ký mật khẩu nào");
                }
            }
            else {
                messageSomeone(sourceName, "§cYou don't have permission! Only Dream_Da_Vang can perform this task");
            }
        }
        return 1;
    }

}
