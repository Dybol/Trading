package me.mikolaj.trading.utils;

import lombok.experimental.UtilityClass;
import me.mikolaj.trading.settings.Settings;
import org.bukkit.entity.Player;
import org.mineacademy.fo.region.Region;

/**
 * Klasa pomocnicza do wymiany
 */
@UtilityClass
public class TradingUtil {

	/**
	 * Miejsce na wymiane
	 */
	private static final Region tradingReg = Settings.Trade.TRADING_EVERYWHERE ? null : new Region(Settings.Trade.PRIMARY, Settings.Trade.SECONDARY);

	/**
	 * Metoda sprawdzajaca czy gracz jest w miejscu na wymiane
	 *
	 * @param player - gracz
	 * @return - czy gracz jest w miejscu na handel
	 */
	public boolean isInBazaarRegion(final Player player) {
		return Settings.Bazaar.BAZAARS_EVERYWHERE || tradingReg.isWithin(player.getLocation());
	}
}
