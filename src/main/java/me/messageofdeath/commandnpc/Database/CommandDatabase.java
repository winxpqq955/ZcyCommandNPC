package me.messageofdeath.commandnpc.Database;

import java.util.ArrayList;

import me.messageofdeath.commandnpc.CommandNPC;
import me.messageofdeath.commandnpc.Database.PluginSettings.PluginSettings;
import me.messageofdeath.commandnpc.NPCDataManager.NPCCommand;
import me.messageofdeath.commandnpc.NPCDataManager.NPCData;
import org.bukkit.Location;

public class CommandDatabase {

	private final CommandNPC instance;
	private YamlDatabase database;
	public CommandDatabase(CommandNPC instance) {
		this.instance = instance;
	}

	public void initDatabase() {
		database = new YamlDatabase(instance, "data");
		database.onStartUp();
	}

	public void loadDatabase() {
		CommandNPC.getQueueSystem().execute(() -> {
			for (String idx : database.getSection("NPCS", new ArrayList<>())) {
				NPCData data = new NPCData(Integer.parseInt(idx));
				for (String commands : database.getStringArray("NPCS." + idx + ".Commands", new ArrayList<>())) {
					String[] args = commands.split("~~~");
					ClickType clickType;
					if (args.length == 13) {//Version 1.9.1
						clickType = this.getClickType(args);
						data.addCommand(new NPCCommand(Integer.parseInt(args[0]), args[1], args[2], args[4], clickType, Boolean.parseBoolean(args[5]),
								Boolean.parseBoolean(args[6]), Boolean.parseBoolean(args[7]), Boolean.parseBoolean(args[11]), Boolean.parseBoolean(args[12]),
								Double.parseDouble(args[8]), Integer.parseInt(args[9]), Integer.parseInt(args[10])));
						continue;
					}else if (args.length == 11) {//Version 1.9.0
						clickType = this.getClickType(args);
						data.addCommand(new NPCCommand(Integer.parseInt(args[0]), args[1], args[2], args[4], clickType, Boolean.parseBoolean(args[5]),
								Boolean.parseBoolean(args[6]), Boolean.parseBoolean(args[7]), false, false, Double.parseDouble(args[8]), Integer.parseInt(args[9]), Integer.parseInt(args[10])));
						continue;
					}else if (args.length == 9) {//Version 1.8.8-1.8.9
						clickType = this.getClickType(args);
						data.addCommand(new NPCCommand(Integer.parseInt(args[0]), args[1], args[2], "", clickType, Boolean.parseBoolean(args[4]),
								Boolean.parseBoolean(args[5]), Boolean.parseBoolean(args[6]), false, false, Double.parseDouble(args[7]), Integer.parseInt(args[8]), 0));
						continue;
					}else if (args.length == 8) {//Version 1.8.7
						clickType = this.getClickType(args);
						data.addCommand(new NPCCommand(Integer.parseInt(args[0]), args[1], args[2], "", clickType, Boolean.parseBoolean(args[4]),
								Boolean.parseBoolean(args[5]), false, false, false, Double.parseDouble(args[6]), Integer.parseInt(args[7]), 0));
						continue;
					}
					args = commands.split("~");
					if (args.length == 7) {// Version 1.8.6
						clickType = this.getClickType(args);
						data.addCommand(new NPCCommand(Integer.parseInt(args[0]), args[1], args[2], "", clickType, Boolean.parseBoolean(args[4]),
								Boolean.parseBoolean(args[5]), false, false, false, Double.parseDouble(args[6]), 0, 0));
					}else if (args.length == 5) {//Version 1.8.5
						String cmd = args[2];
						if (cmd.toLowerCase().contains("-c")) {
							cmd = cmd.replace("-c", "");
						}
						data.addCommand(new NPCCommand(args[0], args[1], "", ClickType.getClickType(PluginSettings.ClickType.getSetting()), Boolean.parseBoolean(cmd),
								Boolean.parseBoolean(args[3]), false, false, false, Double.parseDouble(args[4]), 0, 0));
					}else{
						instance.logError("Loading Command", "CommandDatabase", "loadDatabase()", "Incompatible command! ***Not detrimental*** ID: "+idx+" | Line: " + commands);
					}
				}
				for (String location : database.getStringArray("NPCS." + idx + ".Location", new ArrayList<>())) {
					String[] args = location.split("\\|", 6);
					data.setTp(new Location(CommandNPC.getInstance().getServer().getWorld(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Float.parseFloat(args[4]), Float.parseFloat(args[5])));
				}
				CommandNPC.getCommandManager().addNPCData(data);
			}
			instance.log("Loading commands complete!", true);
		});
	}

	public void deleteNPC(int id) {
		CommandNPC.getQueueSystem().execute(() -> database.set("NPCS." + id, null));
	}

	public void saveDatabase() {
		CommandNPC.getQueueSystem().execute(() -> {
			for (NPCData data : CommandNPC.getCommandManager().getNPCDatas()) {
				ArrayList<String> commands = new ArrayList<>();
				for (NPCCommand command : data.getCommands()) {
					commands.add(command.getID() + "~~~" + command.getCommand() + "~~~" + command.getPermission() + "~~~" + command.getClickType().name() + "~~~" +
							command.getCooldownMessage() + "~~~" + command.inConsole() + "~~~" + command.asOp() + "~~~" + command.isRandom() + "~~~" +
							command.getCost() + "~~~" + command.getDelay() + "~~~" + command.getCooldown() + "~~~" + command.isIgnorePermMsg() + "~~~" + command.isIgnoreMoneyMsg());
				}
				database.set("NPCS." + data.getId() + ".Commands", commands);
				ArrayList<String> location = new ArrayList<>();
				if (data.getTp() != null) {
					location.add(String.format("%s|%s|%s|%s|%s|%s", data.getTp().getWorld().getName(), data.getTp().getX(), data.getTp().getY(), data.getTp().getZ(), data.getTp().getYaw(), data.getTp().getPitch()));
				}
				database.set("NPCS." + data.getId() + ".Location", location);
			}
		});
	}
	
	private ClickType getClickType(String[] args) {
		return args.length > 3 && ClickType.hasClickType(args[3]) ? ClickType.getClickType(args[3]) : ClickType.getClickType(PluginSettings.ClickType.getSetting());
	}
}
