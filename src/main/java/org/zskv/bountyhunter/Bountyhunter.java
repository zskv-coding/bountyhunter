package org.zskv.bountyhunter;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Bountyhunter extends JavaPlugin implements Listener {

    private final Map<UUID, Integer> bounties = new HashMap<>();
    private File bountyFile;
    private FileConfiguration bountyConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupBountyFile();
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("bounty").setExecutor(new BountyCommand(this));
        createBountyCraftRecipe();
        getLogger().info("BountyHunter plugin enabled!");
    }

    @Override
    public void onDisable() {
        saveBounties();
        getLogger().info("BountyHunter plugin disabled!");
    }

    private void setupBountyFile() {
        bountyFile = new File(getDataFolder(), "bounties.yml");

        if (!bountyFile.exists()) {
            saveResource("bounties.yml", false);
        }

        bountyConfig = YamlConfiguration.loadConfiguration(bountyFile);

        if (bountyConfig.contains("bounties")) {
            ConfigurationSection bountiesSection = bountyConfig.getConfigurationSection("bounties");

            for (String key : bountiesSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    int bountyAmount = bountiesSection.getInt(key + ".amount");

                    bounties.put(uuid, bountyAmount);
                } catch (IllegalArgumentException e) {
                    getLogger().warning("Invalid UUID in bounties.yml: " + key);
                }
            }
        }
    }

    private void saveBounties() {
        if (bountyConfig == null || bountyFile == null) {
            getLogger().warning("Bounty config is not loaded, skipping save.");
            return;
        }

        bountyConfig.set("bounties", null); // Clear old data

        for (Map.Entry<UUID, Integer> entry : bounties.entrySet()) {
            String path = "bounties." + entry.getKey().toString() + ".amount";
            bountyConfig.set(path, entry.getValue());
        }

        try {
            bountyConfig.save(bountyFile);
        } catch (IOException e) {
            getLogger().severe("Could not save bounties.yml!");
            e.printStackTrace();
        }
    }

    private void createBountyCraftRecipe() {
        ItemStack bountyStar = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = bountyStar.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bBounty Star");
            meta.setLore(List.of("§7Redeem to restore bounty!"));
            bountyStar.setItemMeta(meta);
        }

        NamespacedKey key = new NamespacedKey(this, "bounty_star");
        ShapedRecipe recipe = new ShapedRecipe(key, bountyStar);

        recipe.shape("EHE", "NTN", "ENE");
        recipe.setIngredient('E', Material.ECHO_SHARD);
        recipe.setIngredient('H', Material.HEART_OF_THE_SEA);
        recipe.setIngredient('N', Material.NAUTILUS_SHELL);
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();

        if (killer != null && killer != player) {
            adjustBounty(player, -1000);
            adjustBounty(killer, 1000);
        } else {
            adjustBounty(player, -1000);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack result = event.getRecipe().getResult();
        if (result.getType() == Material.NETHER_STAR &&
                result.hasItemMeta() && result.getItemMeta().getDisplayName().equals("§bBounty Star")) {

            int currentBounty = bounties.getOrDefault(player.getUniqueId(), 5000);
            if (currentBounty <= 1000) {
                adjustBounty(player, 1000);
                player.sendMessage("§aYou crafted a Bounty Star and gained §e1000§a bounty!");
            } else {
                event.setCancelled(true);
                player.sendMessage("§cYou can only craft a Bounty Star if you have 1000 bounty or less!");
            }
        }
    }

    private void adjustBounty(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        int current = bounties.getOrDefault(uuid, 5000);
        int newBounty = Math.max(0, current + amount);

        bounties.put(uuid, newBounty);
        updateBountyDisplay(player);

        // Effects
        if (amount > 0) {
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 20, 1, 1, 1, 0.5);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        } else {
            player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation(), 20, 1, 1, 1, 0.5);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0f, 0.5f);
        }

        if (newBounty == 0) {
            player.kickPlayer("You have lost all your bounty and have been banned. You are worthless.");
            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), "No bounty left! Worthless.", null, null);
        }
    }

    private void updateBountyDisplay(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("bounty", Criteria.DUMMY, "§b§lYour Bounty");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score score = objective.getScore(player.getName());
        score.setScore(bounties.getOrDefault(player.getUniqueId(), 5000));

        player.setScoreboard(board);
    }

    public int getBounty(UUID uuid) {
        return bounties.getOrDefault(uuid, 5000);
    }
}
