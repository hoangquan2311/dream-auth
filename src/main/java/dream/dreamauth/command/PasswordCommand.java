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

import static dream.dreamauth.AdvancedEncryption.encrypt;
import static dream.dreamauth.AdvancedEncryption.stringToSecretKey;
import static dream.dreamauth.FileOperator.*;
import static dream.dreamauth.DreamAuth.*;
import static dream.dreamauth.ScoreboardOperator.getScore;
import static dream.dreamauth.command.RegisterCommand.isPasswordValid;

public class PasswordCommand {
    public static void registerPasswordCommand(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(CommandManager.literal("password").then(CommandManager.literal("update").then(CommandManager.argument("newPassword", StringArgumentType.word()).executes(PasswordCommand::handleChange))));
    }
    private static int handleChange(CommandContext<ServerCommandSource> context){
        String sourceName = context.getSource().getName();
        String newPassword = StringArgumentType.getString(context, "newPassword");
        ServerPlayerEntity sourcePlayer = context.getSource().getPlayer();
        if(getActivate()){
            if(getScore(sourceName,OBJECTIVE_NAME)==1){
                if(context.getSource().getServer().isHost(sourcePlayer.getGameProfile())){
                    messageSomeone(sourceName, "§cKhông có mật khẩu vì bạn là Host");
                    return 0;
                }
                else {
                    if(isPasswordValid(newPassword,sourcePlayer)){
                        messageSomeone(sourceName, "§7Đã cập nhật mật khẩu thành công: \"§d"+newPassword+"§7\"");
                        sourcePlayer.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.AMBIENT,1f,1.3f);
                        try {updatePassword(sourceName, new Password(encrypt(newPassword,stringToSecretKey(ye_1lre))));} catch (Exception ignored){}
                        return 1;
                    }
                    return 0;
                }
            }
            else {
                messageSomeone(sourceName,"§cBạn cần đăng nhập để dùng lệnh này");
                sourcePlayer.playSound(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.AMBIENT,1f,1f);
                return 0;
            }
        }
        else {
            messageSomeone(sourceName,"§cCập nhật thất bại do DreamAuth đang bị vô hiệu hóa");
            sourcePlayer.playSound(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.AMBIENT,1f,1f);
            return 0;
        }
    }
}
