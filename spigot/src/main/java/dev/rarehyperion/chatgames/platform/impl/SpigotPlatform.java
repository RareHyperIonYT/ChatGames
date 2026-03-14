package pl.twojserwer.itemevent;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ItemEventPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private final Map<Material, Integer> requirements = new HashMap<>();
    private final Map<Material, Integer> progress = new HashMap<>();
    private List<String> rewards;

    private BossBar bossBar;
    private boolean eventActive = false;
    private long nextActionTime = 0L;

    private FileConfiguration dataConfig;
    private File dataFile;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigData();
        
        BossBar.Overlay bbOverlay = BossBar.Overlay.valueOf(getConfig().getString("bossbar.overlay", "PROGRESS"));
        bossBar = BossBar.bossBar(Component.empty(), 0.0f, BossBar.Color.GREEN, bbOverlay);

        loadDataFile();

        if (getCommand("itemevent") != null) {
            getCommand("itemevent").setExecutor(this);
        }
        getServer().getPluginManager().registerEvents(this, this);

        // Zegar odświeżania odliczania do konca eventu (bossbar odświeża progres bloku już natychmiastowo)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            checkTime();
            updateBossBar(); 
            saveData();
        }, 0L, 1200L);

        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.addViewer(p);
        }

        getLogger().info("Community Event załadowany pomyślnie!");
    }

    @Override
    public void onDisable() {
        saveData();
    }

    private void loadConfigData() {
        requirements.clear();
        ConfigurationSection reqSection = getConfig().getConfigurationSection("requirements");
        if (reqSection != null) {
            for (String key : reqSection.getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if (mat != null) {
                    requirements.put(mat, reqSection.getInt(key));
                }
            }
        }
        rewards = getConfig().getStringList("rewards");
    }

    private void loadDataFile() {
        dataFile = new File(getDataFolder(), "progress.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        eventActive = dataConfig.getBoolean("event-active", false);
        nextActionTime = dataConfig.getLong("next-action-time", 0L);
        
        progress.clear();
        ConfigurationSection progSection = dataConfig.getConfigurationSection("progress");
        if (progSection != null) {
            for (String key : progSection.getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if (mat != null) {
                    progress.put(mat, progSection.getInt(key));
                }
            }
        }

        if (nextActionTime == 0L) {
            // Pierwsze uruchomienie - zaczynamy odliczanie do planowanego startu eventu
            eventActive = false;
            nextActionTime = calculateNextTargetTime();
            saveData();
        } else {
            checkTime(); 
        }

        updateBossBar();
    }

    private void saveData() {
        dataConfig.set("event-active", eventActive);
        dataConfig.set("next-action-time", nextActionTime);
        dataConfig.set("progress", null);
        for (Map.Entry<Material, Integer> entry : progress.entrySet()) {
            dataConfig.set("progress." + entry.getKey().name(), entry.getValue());
        }
        try { dataConfig.save(dataFile); } catch (IOException ignored) {}
    }

    private long calculateNextTargetTime() {
        String dayStr = getConfig().getString("schedule.day", "FRIDAY");
        int hour = getConfig().getInt("schedule.hour", 16);
        
        DayOfWeek day;
        try {
            day = DayOfWeek.valueOf(dayStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            day = DayOfWeek.FRIDAY;
            getLogger().warning("Nieprawidłowy dzień tygodnia w configu! Użyto FRIDAY.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.with(TemporalAdjusters.nextOrSame(day)).withHour(hour).withMinute(0).withSecond(0).withNano(0);
        
        if (now.isAfter(target) || now.isEqual(target)) {
            target = now.with(TemporalAdjusters.next(day)).withHour(hour).withMinute(0).withSecond(0).withNano(0);
        }
        return target.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private void checkTime() {
        if (System.currentTimeMillis() >= nextActionTime) {
            if (eventActive) {
                // Skoro minęły 24h trwania eventu, a oni nie uzbierali -> porażka
                Bukkit.broadcast(color(getConfig().getString("messages.event-failed")));
                eventActive = false;
                nextActionTime = calculateNextTargetTime(); // Czekamy na kolejny tydzień (np. piątek)
                progress.clear();
            } else {
                // Czas oczekiwania minął -> startujemy event na równe 24h
                progress.clear();
                eventActive = true;
                nextActionTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24);
                Bukkit.broadcast(color(getConfig().getString("messages.event-started")));
            }
            saveData();
            updateBossBar();
        }
    }

    private void updateBossBar() {
        long timeLeft = nextActionTime - System.currentTimeMillis();
        if (timeLeft < 0) timeLeft = 0;

        String timeString = formatTime(timeLeft);

        if (eventActive) {
            double totalRequired = 0;
            double totalProgress = 0;
            StringBuilder itemsBuilder = new StringBuilder();

            for (Map.Entry<Material, Integer> entry : requirements.entrySet()) {
                int req = entry.getValue();
                int currentProg = progress.getOrDefault(entry.getKey(), 0);
                
                totalRequired += req;
                totalProgress += Math.min(currentProg, req);

                if (itemsBuilder.length() > 0) {
                    itemsBuilder.append(" &8| ");
                }
                
                // Formatujemy nazwę przedmiotu (np. IRON_INGOT na Iron ingot)
                String matName = entry.getKey().name().replace("_", " ");
                matName = matName.substring(0, 1).toUpperCase() + matName.substring(1).toLowerCase();
                
                itemsBuilder.append("&e").append(matName).append(": &a").append(currentProg).append("&8/&c").append(req);
            }

            // Pasek na bieżąco powiększający się w zależności od procentowego ukończenia wszystkich elementów
            float percent = (float) (totalRequired == 0 ? 0 : totalProgress / totalRequired);
            bossBar.progress(Math.max(0.0f, Math.min(1.0f, percent)));
            bossBar.color(BossBar.Color.valueOf(getConfig().getString("bossbar.active-color", "GREEN")));

            // Podstawiamy zmienną %items% aby pokazać listę na BossBar
            String titleTemplate = getConfig().getString("bossbar.active-title", "&aCel: %items% &7(&b%time%&7)");
            
            // Jeśli ktoś zapomniał dodać %items% w configu, wymusimy jej pokazanie dla bezpieczeństwa
            if (!titleTemplate.contains("%items%")) {
                titleTemplate = titleTemplate + " &8[&7" + "%items%" + "&8]";
            }

            String title = titleTemplate
                    .replace("%progress%", String.format("%.1f", percent * 100))
                    .replace("%items%", itemsBuilder.toString())
                    .replace("%time%", timeString);
                    
            bossBar.name(color(title));
        } else {
            // Stan oczekiwania na kolejny event
            bossBar.progress(1.0f);
            bossBar.color(BossBar.Color.valueOf(getConfig().getString("bossbar.waiting-color", "BLUE")));
            String title = getConfig().getString("bossbar.waiting-title", "&7Kolejny cel za: &b%time%")
                    .replace("%time%", timeString);
            bossBar.name(color(title));
        }
    }

    private void addProgress(Material mat, int amount) {
        if (!eventActive) return;

        int current = progress.getOrDefault(mat, 0);
        int required = requirements.getOrDefault(mat, 0);
        
        if (current < required) {
            progress.put(mat, Math.min(current + amount, required));
            // Natychmiastowa aktualizacja paska i tekstu od razu po wydobyciu/scraftowaniu bloku!
            updateBossBar(); 
            checkWinCondition();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!eventActive) return;
        if (event.getBlock().hasMetadata("placed_by_player")) return;

        Material mat = event.getBlock().getType();
        if (requirements.containsKey(mat)) {
            addProgress(mat, 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        event.getBlock().setMetadata("placed_by_player", new FixedMetadataValue(this, true));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!eventActive || event.getRecipe() == null) return;
        ItemStack result = event.getRecipe().getResult();
        Material mat = result.getType();
        
        if (requirements.containsKey(mat)) {
            int amount = result.getAmount();
            if (event.isShiftClick()) {
                int maxCrafts = Integer.MAX_VALUE;
                for (ItemStack item : event.getInventory().getMatrix()) {
                    if (item != null && item.getType() != Material.AIR) {
                        maxCrafts = Math.min(maxCrafts, item.getAmount());
                    }
                }
                if (maxCrafts != Integer.MAX_VALUE && maxCrafts > 0) {
                    amount = maxCrafts * result.getAmount();
                }
            }
            addProgress(mat, amount);
        }
    }

    private void checkWinCondition() {
        for (Map.Entry<Material, Integer> entry : requirements.entrySet()) {
            if (progress.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return; // Czegoś jeszcze brakuje
            }
        }

        // Wszystkie cele osiągnięte w czasie poniżej 24h!
        eventActive = false; 
        nextActionTime = calculateNextTargetTime(); // Przechodzimy w stan oczekiwania do kolejnego wyznaczonego dnia
        Bukkit.broadcast(color(getConfig().getString("messages.event-won")));

        for (String cmd : rewards) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        saveData();
        updateBossBar();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!eventActive) {
            long timeLeft = nextActionTime - System.currentTimeMillis();
            sender.sendMessage(color(getConfig().getString("messages.waiting-info", "&cObecnie nie trwa żaden event. Następny za: &b%time%")
                    .replace("%time%", formatTime(timeLeft))));
            return true;
        }

        sender.sendMessage(color(getConfig().getString("messages.info-header", "&e--- Postęp Celu Społeczności ---")));
        String format = getConfig().getString("messages.info-format", "&7- &b%item%: &a%progress% &8/ &c%required%");
        
        for (Map.Entry<Material, Integer> entry : requirements.entrySet()) {
            int current = progress.getOrDefault(entry.getKey(), 0);
            sender.sendMessage(color(format
                    .replace("%item%", entry.getKey().name())
                    .replace("%progress%", String.valueOf(current))
                    .replace("%required%", String.valueOf(entry.getValue()))));
        }
        sender.sendMessage(color("&7Wykopuj lub craftuj przedmioty, aby pomóc w evencie!"));
        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        bossBar.addViewer(event.getPlayer());
    }

    private String formatTime(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        return String.format("%dd %dh %dm", days, hours, minutes);
    }

    private Component color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
