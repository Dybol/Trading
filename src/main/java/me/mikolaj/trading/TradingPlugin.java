package me.mikolaj.trading;

import me.mikolaj.trading.command.BazaarCommand;
import me.mikolaj.trading.command.TradeCommand;
import me.mikolaj.trading.event.PlayerListener;
import me.mikolaj.trading.settings.Settings;
import me.mikolaj.trading.task.BazaarTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.settings.YamlStaticConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Klasa główna
 */
public class TradingPlugin extends SimplePlugin {

	/**
	 * Metoda wywoływana przy włączaniu serwera
	 */
	@Override
	protected void onPluginStart() {
		if (Settings.Bazaar.ENABLED)
			registerCommand(new BazaarCommand());
		if (Settings.Trade.ENABLED)
			registerCommand(new TradeCommand());

		registerEvents(new PlayerListener());

		new BazaarTask().runTaskTimer(this, 0, 10);
	}

	/**
	 * Metoda wywoływana przy wyłączaniu serwera
	 */
	@Override
	protected void onPluginStop() {
		for (final Player player : Bukkit.getOnlinePlayers()) {

			final PlayerCache cache = PlayerCache.getCache(player);
			player.setWalkSpeed(0.2f);
			player.removePotionEffect(PotionEffectType.JUMP);
			cache.setHasBazaar(false);
			cache.clearContent();
			cache.setBazaarLoc(null);

			cache.getBazaarStand().remove();
			cache.setBazaarStand(null);
			cache.setBazaarName(null);
		}
	}

	/**
	 * Metoda odpowiadająca za rejestrowanie ustawień
	 *
	 * @return - Lista klas z ustawieniami
	 */
	@Override
	public List<Class<? extends YamlStaticConfig>> getSettings() {
		return Arrays.asList(Settings.class);
	}
}
