package me.mikolaj.trading.command;

import me.mikolaj.trading.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.command.SimpleCommand;

import java.util.ArrayList;
import java.util.List;

public class TradeCommand extends SimpleCommand {
	public TradeCommand() {
		super("wymiana");
		setMinArguments(1);
	}

	@Override
	protected void onCommand() {
		checkConsole();
		final Player first_player = getPlayer(); //gracz, ktory wysyla komende - /wymien <second_player>
		final Player second_player = Bukkit.getPlayer(args[0]);

		//TODO safety checks - lokalizacja obu graczy, czy sa na spawnie
		if (second_player == null) {
			returnTell("&cNie ma takiego gracza!");
		}

		final PlayerCache first_cache = PlayerCache.getCache(first_player);
		final PlayerCache second_cache = PlayerCache.getCache(second_player);

		//if (second_cache.getTradeOffersMap().get(first_player.getUniqueId())) {
		if (first_cache.getTradeOffersMap().get(second_player.getUniqueId())) {
			//to znaczy, ze juz gracz mu wyslal oferte handlu
			//open menu
		} else {
			second_cache.addOfferToTradeMap(first_player.getUniqueId());
			Messenger.info(second_player, "Gracz " + first_player.getName() + " zaproponowal Ci oferte handlu!" +
					". Wpisz /wymiana " + first_player.getName() + " aby otworzyc menu wymiany.");
		}

	}

	@Override
	protected List<String> tabComplete() {
		if (args.length == 1)
			return completeLastWord(PlayerCache.getCache(getPlayer()).getTradeOffersMap().keySet());
		return new ArrayList<>();
	}
}
