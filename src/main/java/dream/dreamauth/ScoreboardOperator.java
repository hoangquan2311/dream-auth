package dream.dreamauth;

import net.minecraft.scoreboard.*;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import static dream.dreamauth.DreamAuth.MCServer;

public class ScoreboardOperator {


    public static void checkScoreboard(MinecraftServer server, String objName){
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(objName);
        if(objective == null){
            scoreboard.addObjective(objName, ScoreboardCriterion.DUMMY, Text.literal(objName), ScoreboardCriterion.RenderType.INTEGER, false, BlankNumberFormat.INSTANCE);
        }
    }
    public static int getScore(String name, String objName){
        Scoreboard scoreboard = MCServer.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(objName);
        if (objective != null) {
            ScoreHolder holder = ScoreHolder.fromName(name);
            if(scoreboard.getScoreHolderObjectives(holder).containsKey(objective))
                return scoreboard.getScore(holder,objective).getScore();
        }
        return -1;
    }

    public static void setScore(String name, String objName, int score){
        ServerScoreboard scoreboard = MCServer.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(objName);
        if (objective != null) {
            scoreboard.getOrCreateScore(ScoreHolder.fromName(name),objective).setScore(score);
        }
    }

    public static void addScore(String name, String objective, int score){
        setScore(name,objective,getScore(name,objective)+score);
    }

    public static void removeScore(String name, String objective, int score){
        setScore(name,objective,getScore(name,objective)-score);
    }

    public static void resetScore(String name, String objName){
        Scoreboard scoreboard = MCServer.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(objName);
        if (objective != null){
            scoreboard.removeScore(ScoreHolder.fromName(name),objective);
        }
    }
}
