package me.electroid.nicknamer;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Skin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.Random;

/**
 * A basic random identity generating plugin.
 * @author ElectroidFilms
 */
public class NicknamePlugin extends JavaPlugin {

    private static final int MIN_USERNAME_LENGTH = 4;
    private static final int[] OCN_PUNISHMENT_RANGE = {1000, 10000};
    private static final String OCN_PUNISHMENT_PAGE = "http://oc.tc/punishments?page=";

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

    /**
    * A async task to generate and apply the fake skin and username (Never run on main thread).
    */
    public class NicknameTask implements Runnable {

        private Player player;

        private NicknameTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            MinecraftNameGenerator gen = new MinecraftNameGenerator(MIN_USERNAME_LENGTH);
            String name = gen.generate(getSeedFromOcn());
            Skin fakeSkin = getRandomSkin();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.equals(player) && !p.hasPermission("nickname.see") && !p.isOp()) {
                    player.setFakeNameAndSkin(p, name, fakeSkin);
                }
            }
            player.sendMessage(ChatColor.GREEN + "Changed nickname to.. " + ChatColor.WHITE + ChatColor.ITALIC + name);
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("nick")) {
            if (sender.hasPermission("nickname.use")) {
                if (args.length == 1) {
                    sender.sendMessage("Attempting to generate random nickname and skin..");
                    Bukkit.getScheduler().runTaskAsynchronously(this, new NicknameTask((Player) sender));
                } else if (args.length == 2) {
                    Player player = Bukkit.getPlayerExact(args[1], sender);
                    if (player != null) {
                        sender.sendMessage("Attempting to generate random nickname and skin for " + player.getDisplayName(sender) + "..");
                        Bukkit.getScheduler().runTaskAsynchronously(this, new NicknameTask(player));
                    } else {
                        sender.sendMessage(ChatColor.RED + "Could not find the specified player to nick");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid formatting. /nick (player)");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use /nick");
            }
        } else if (cmd.getName().equalsIgnoreCase("clearnick")) {
            if (args.length == 1) {
                ((Player) sender).clearFakeNamesAndSkins();
                sender.sendMessage(ChatColor.GREEN + "Your nickname and fake skin have been cleared.");
            } else if (args.length == 2) {
                if (sender.hasPermission("nickname.clear.others")) {
                    Player player = Bukkit.getPlayerExact(args[1], sender);
                    if (player != null) {
                        player.clearFakeNamesAndSkins();
                        sender.sendMessage(ChatColor.GREEN + player.getDisplayName(sender) + "'s nickname and fake skin have been cleared.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Could not find the specified player to clear their nick");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use /clearnick");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid formatting. /clearnick (player)");
            }
        }
        return true;
    }

    /**
    * Get a banned user from Overcast Network's punishments page.
    * @return The banned username.
    */
    private String getSeedFromOcn() {
        /** Backup username in the event of any errors. */
        String seed = "_creeperNoob";
        try {
            Document doc = Jsoup.connect(OCN_PUNISHMENT_PAGE + randomWithinRange(OCN_PUNISHMENT_RANGE[0], OCN_PUNISHMENT_RANGE[1])).get();
            seed = doc.select("tbody").first().select("td").get(1).select("a").attr("href");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return seed.substring(1, seed.length());
    }

    /**
    * Get a random skin from any players online.
    * @return The random skin.
    */
    private Skin getRandomSkin() {
        Skin skin = Skin.EMPTY;
        int index = random(Bukkit.getOnlinePlayers().size());
        int count = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (index == count) {
                skin = p.getRealSkin();
            }
            count++;
        }
        return skin;
    }

    private int random(int range) {
        if (range != 0)
            return new Random().nextInt(range);
        return 0;
    }

    private int randomWithinRange(int start, int end) {
        return new Random().nextInt(end - start) + start;
    }

}
