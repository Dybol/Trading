package me.mikolaj.trading;

import me.mikolaj.trading.command.BazaarCommand;
import me.mikolaj.trading.command.TradeCommand;
import me.mikolaj.trading.event.PlayerListener;
import me.mikolaj.trading.settings.Localization;
import me.mikolaj.trading.settings.Settings;
import me.mikolaj.trading.task.BazaarTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.settings.YamlStaticConfig;

import java.util.Arrays;
import java.util.List;

public class TradingPlugin extends SimplePlugin {
	@Override
	protected void onPluginStart() {
		Common.log("laduje");
		registerCommand(new BazaarCommand());
		//registerCommand(new TradeTestCommand());
		registerCommand(new TradeCommand());
//		getCommand("wymiana").setExecutor(new TradeTestCommand());
//		//todo w ten sposob mozna zrobic tab complete'a !!
//		getCommand("wymiana").setTabCompleter((sender, command, alias, args) -> {
//			if (!(sender instanceof Player))
//				return new ArrayList<>();
//
//			return new ArrayList<>();
//		});

		registerEvents(new PlayerListener());

		new BazaarTask().runTaskTimer(this, 0, 10);
	}

	@Override
	protected void onPluginStop() {
		for (final Player player : Bukkit.getOnlinePlayers()) {
			System.out.println("wywolano mimo zamykania");
			final PlayerCache cache = PlayerCache.getCache(player);
			player.setWalkSpeed(0.2f);
			player.removePotionEffect(PotionEffectType.JUMP);
			cache.setHasBazaar(false);
			cache.clearContent();
			cache.setBazaarLoc(null);
		}
	}

	@Override
	public List<Class<? extends YamlStaticConfig>> getSettings() {
		return Arrays.asList(Settings.class, Localization.class);
	}
}
