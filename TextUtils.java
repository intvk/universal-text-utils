
import me.clip.placeholderapi.PlaceholderAPI;
import me.intvk.ispleef.Main;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;

public class TextUtils {
    private final Main plugin = Main.getInstance();

    public static String replacePlaceholders(Player player, String message) {
        if (player == null || message == null) {
            return null;
        }

        return PlaceholderAPI.setPlaceholders(player, message);
    }

    public void getMessages(Player player, String key) {
        plugin.getDatabaseManager().getLanguageOperation().getPlayerLanguageAsync(player.getUniqueId(), language -> {
            if (language == null) {
                plugin.getLogger().warning("No language found for player '" + player.getName() + "'.");
                language = "en_US";
            }

            FileConfiguration config = plugin.getMessageReader().readMessageFile(language + ".yml");
            if (config == null) {
                return; // Обработка ошибки, если файл сообщений не найден
            }

            ConfigurationSection messageSection = config.getConfigurationSection(key);
            if (messageSection == null) {
                plugin.getLogger().warning("No message section found for key '" + key + "' in player's messages file.");
                return; // Обработка ошибки, если раздел сообщений не найден
            }

            sendMessageWithOptions(player,
                    messageSection.getString("chat"),
                    messageSection.getString("sound"),
                    messageSection.getString("title.title"),
                    messageSection.getString("title.subtitle"),
                    messageSection.getInt("title.times.fadeIn", 0),
                    messageSection.getInt("title.times.stay", 20),
                    messageSection.getInt("title.times.fadeOut", 0),
                    messageSection.getString("actionbar"));
        });
    }

    public static void sendMessageWithOptions(Player player, String chatMessage, String sound, String title, String subtitle, int fadeIn, int stay, int fadeOut, String actionbarMessage) {
        if (chatMessage != null) {
            sendMessage(player, chatMessage);
        }

        if (sound != null) {
            playSound(player, sound);
        }

        if (title != null && subtitle != null) {
            sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }

        if (actionbarMessage != null) {
            sendActionBar(player, actionbarMessage);
        }
    }

    public static void sendMessage(Player player, String message) {
        String processedMessage = replacePlaceholders(player, message);
        if (processedMessage != null) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(processedMessage));
        }
    }

    public static void sendTitle(final @NonNull Audience player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        fadeIn *= 50;
        stay *= 50;
        fadeOut *= 50;

        String processedTitle = replacePlaceholders((Player) player, title);
        String processedSubTitle = replacePlaceholders((Player) player, subTitle);

        final Title.Times times = Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut));
        final Title showTitle = Title.title(LegacyComponentSerializer.legacyAmpersand().deserialize(processedTitle), LegacyComponentSerializer.legacyAmpersand().deserialize(processedSubTitle), times);

        player.showTitle(showTitle);
    }

    public static void playSound(final Player player, String sound) {
        String processedSound = replacePlaceholders(player, sound);
        if (processedSound != null) {
            Key soundKeyObj = Key.key(processedSound);
            Sound soundObj = Sound.sound(soundKeyObj, Sound.Source.MASTER, 1.0f, 1.0f);
            player.playSound(soundObj);
        }
    }

    public static void sendActionBar(final Player player, String actionBar) {
        String processedActionBar = replacePlaceholders(player, actionBar);
        if (processedActionBar != null) {
            player.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(processedActionBar));
        }
    }
}
