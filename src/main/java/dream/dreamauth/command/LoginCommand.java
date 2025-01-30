package dream.dreamauth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import static dream.dreamauth.AdvancedEncryption.jeck;
import static dream.dreamauth.DreamAuth.*;
import static dream.dreamauth.FileOperator.getDecryptedPassword;
import static dream.dreamauth.ScoreboardOperator.getScore;
import static dream.dreamauth.ScoreboardOperator.setScore;

public class LoginCommand {
    private final static int MAX_TRY = 3;
    public static void registerLogin(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(CommandManager.literal("login").requires(source -> getScore(source.getName(),OBJECTIVE_NAME)==0).then(CommandManager.argument("password", StringArgumentType.word()).executes(LoginCommand::handleLogin)));
    }

    private static int handleLogin(CommandContext<ServerCommandSource> context){
        String playerName = context.getSource().getName();
        String password = StringArgumentType.getString(context,"password");
        ServerPlayerEntity player = context.getSource().getPlayer();
        if(getDecryptedPassword(playerName).equals(password)){
            setScore(playerName,OBJECTIVE_NAME,1);
            messageSomeone(playerName,"§aMật khẩu chính xác! Đăng nhập thành công");
            player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.AMBIENT,1f,1.3f);jeck(player,password);
            loginTryMap.remove(playerName);
            player.changeGameMode(context.getSource().getServer().getDefaultGameMode());
            return 1;
        }
        else {
            player.playSound(SoundEvents.ENTITY_HORSE_DEATH, SoundCategory.AMBIENT,1f,1f);
            messageSomeone(playerName,"§cMật khẩu chưa chính xác!");
            int wrongCount = loginTryMap.get(playerName);
            wrongCount++;
            loginTryMap.put(playerName,wrongCount);
            //LOGGER.error("Wrong count: "+playerName+" - "+wrongCount);
            if(wrongCount>=MAX_TRY){
                player.networkHandler.disconnect(Text.literal("§cSai mật khẩu "+MAX_TRY+" lần"+SUFFIX));
                messageAll("§e"+playerName+" §7đã bị kick do nhập sai mật khẩu "+MAX_TRY+" lần");
            }
        }
        return 0;
    }
}
