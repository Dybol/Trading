package me.mikolaj.trading.event;

import me.mikolaj.trading.PlayerCache;
import me.mikolaj.trading.menu.TestBazaar;
import me.mikolaj.trading.settings.Settings;
import me.mikolaj.trading.utils.BazaarUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.mineacademy.fo.remain.Remain;

public class PlayerListener implements Listener {

	//dzieki temu chyba nie bedzie sie tworzyla nowa instancja, mozna potem z ciekawosci zmierzyc czas czy to cos zmienia

	@EventHandler
	public void onInteract(final PlayerInteractAtEntityEvent event) {
		if (!(event.getRightClicked() instanceof Player))
			return;

		if (!Remain.isInteractEventPrimaryHand(event))
			return;

		//TODO dodac try / catch i wyswietlac blad jak bedzie wprowadzona zla lokalizacja.
		if(Settings.Bazaar.ENABLED) {
			final Player clickedPlayer = (Player) event.getRightClicked();

			if(!BazaarUtil.isInBazaarRegion(clickedPlayer)
					|| !BazaarUtil.isInBazaarRegion(event.getPlayer()))
				return;

			if (PlayerCache.getCache(clickedPlayer).hasBazaar()) {
				new TestBazaar(clickedPlayer).displayTo(event.getPlayer());
			}
		}
	}
}
