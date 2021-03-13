package me.mikolaj.trading;

import me.mikolaj.trading.command.BazaarCommand;
import me.mikolaj.trading.command.TradeTestCommand;
import me.mikolaj.trading.event.PlayerListener;
import me.mikolaj.trading.settings.Localization;
import me.mikolaj.trading.settings.Settings;
import me.mikolaj.trading.task.BazaarTask;
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
		//registerCommand(new TradeCommand());
		getCommand("wymiana").setExecutor(new TradeTestCommand());

		registerEvents(new PlayerListener());

		new BazaarTask().runTaskTimer(this, 0, 10);
	}

	@Override
	public List<Class<? extends YamlStaticConfig>> getSettings() {
		return Arrays.asList(Settings.class, Localization.class);
	}
}
