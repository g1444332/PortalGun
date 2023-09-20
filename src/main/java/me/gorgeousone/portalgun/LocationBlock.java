package me.gorgeousone.portalgun;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import net.milkbowl.vault.economy.Economy;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class LocationBlock implements Listener {

    private static final int DETECTION_RANGE = 5;
    private static final int SPECIAL_RADIUS = 5;

    private static final Location TARGET_LOCATION = new Location(Bukkit.getWorld("world"), -91, 36, -178);
    private static final Location SPECIAL_LOCATION = new Location(Bukkit.getWorld("world"), -87, 30, -194);
    private static final List<String> playerName = new ArrayList<>();
    private static final List<String> jump = new ArrayList<>();

    private static Economy econ = null;

    private final BlockHandler blockHandler;
    private final Plugin plugin;

    public LocationBlock(BlockHandler blockHandler, Plugin plugin) {
        this.plugin = plugin;
        this.blockHandler = blockHandler;
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking() && isWithinRange(player) && playerName.contains(player.getDisplayName())) {
            jump.add(player.getDisplayName());
            playerName.remove(player.getDisplayName());
            blockHandler.dropLiftedBlock(player);

            player.removePotionEffect(PotionEffectType.SLOW);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 4 * 20, 255, true));

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Вы заработали 3 тугриков"));
                RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                econ = rsp.getProvider();
                EconomyResponse r = econ.depositPlayer(player, 3);
                jump.remove(player.getDisplayName());
            }, 20 * 5);

            new BukkitRunnable() {
                int timer = 0;
                int duration = 3; // Длительность сообщения в секундах

                @Override
                public void run() {
                    timer++;

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Выгружаем бочку.."));

                    if (timer >= duration) {
                        cancel(); // Остановка задачи после указанной длительности
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L); // Запуск задачи каждую секунду (20 тиков)
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (playerName.contains(player.getDisplayName())) {
            event.setCancelled(true);
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (isWithinRange(player, TARGET_LOCATION, DETECTION_RANGE)) {
                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock != null && clickedBlock.getType() == Material.BARREL && !playerName.contains(player.getDisplayName())) {
                    playerName.add(player.getDisplayName());
                    event.setCancelled(true);
                    jump.add(player.getDisplayName());

                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 4 * 20, 255, true));

                    BukkitScheduler scheduler = getServer().getScheduler();
                    scheduler.runTaskLater(plugin, () -> {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000000, 3, true));
                        jump.remove(player.getDisplayName());
                    }, 4 * 21); // Здесь 4 * 20 - это задержка в тиках (4 секунды)

                    String заголовок = ChatColor.GOLD + "Вы взяли бочку";
                    String субзаголовок = "отнесите её в трюм";

                    int задержка = 60; // Задержка в тиках перед отображением заголовка
                    int продолжительность = 20 * 3; // Продолжительность отображения заголовка в тиках (5 секунд)

                    player.sendTitle(заголовок, субзаголовок, задержка, продолжительность, задержка);
                    blockHandler.liftBlock(player, clickedBlock);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getLocation().getBlock().getRelative(0, -1, 0).getType().isSolid() && jump.contains(player.getDisplayName())) {
            player.setVelocity(player.getVelocity().setY(0));
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        blockHandler.dropLiftedBlock(player);
        player.removePotionEffect(PotionEffectType.SLOW);
        playerName.remove(player.getDisplayName());
    }

    private boolean isWithinRange(Player player, Location targetLocation, int specialRadius) {
        return player.getLocation().distance(targetLocation) <= specialRadius || player.getLocation().getBlock().getLocation().equals(targetLocation);
    }

    private boolean isWithinRange(Player player) {
        return player.getLocation().distance(SPECIAL_LOCATION) <= SPECIAL_RADIUS;
    }
}