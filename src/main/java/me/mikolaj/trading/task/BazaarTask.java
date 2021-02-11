package me.mikolaj.trading.task;

import me.mikolaj.trading.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.mineacademy.fo.Messenger;

public class BazaarTask extends BukkitRunnable {
	@Override
	public void run() {
		for(final Player player: Bukkit.getOnlinePlayers()) {
			final PlayerCache cache = PlayerCache.getCache(player);
			if(cache.hasBazaar()) {
				if(player.getLocation().getX() != cache.getBazaarLoc().getX() || player.getLocation().getZ() != cache.getBazaarLoc().getZ()) {
					player.teleport(cache.getBazaarLoc());
					Messenger.warn(player, "Aby moc sie ruszac, musisz najpierw zamknac bazar!");
				}
			}
		}
	}
}
