package me.mikolaj.trading.command;

import me.mikolaj.trading.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.command.SimpleCommand;
import org.mineacademy.fo.remain.Remain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//foundation way
public class TradeCommand extends SimpleCommand {

	public TradeCommand() {
		super("wymiana|wymien|trade");
		setDescription("Proponuje wymiane innemu graczowi");
		setUsage("<nick_gracza>");
	}

	@Override
	protected void onCommand() {
		checkConsole();
		final Player first_player = getPlayer();
		if (args.length == 0) {
			//uzycie:
			//PlayerCache.getCache(first_player).restorePlayerSnapshot(first_player);
			//first_player.sendMessage(ChatColor.DARK_BLUE + "Poprawne uzycie: " + getLabel() + " <nick_gracza>");
			Messenger.info(first_player, "&aPoprawne uzycie: " + getLabel() + " <nick_gracza>");
			return;
		}

		if (args.length == 1) {
			//hgw xd
			final Player second_player = Bukkit.getPlayer(args[0]);
			if (second_player == null) {
				Messenger.error(first_player, "Nie ma takiego gracza!");
				return;
			}
			if (first_player.equals(second_player)) {
				Messenger.error(first_player, "Nie mozesz wyslac sobie oferty handlu!");
				return;
			}

			final PlayerCache first_cache = PlayerCache.getCache(first_player);
			final PlayerCache second_cache = PlayerCache.getCache(second_player);

			if (first_cache.getTradeOffersMap().get(second_player.getUniqueId()) != null) {

				//tworzymy kopie ich eq
				first_cache.createInventorySnapshot(first_player);
				second_cache.createInventorySnapshot(second_player);


				final Inventory inv = createTradingMenu(first_player, second_player);

				first_player.openInventory(inv);
				second_player.openInventory(inv);

				first_cache.getTradeOffersMap().remove(second_player.getUniqueId());

				//to znaczy, ze juz gracz mu wyslal oferte handlu
				//open menu
			} else {
				second_cache.addOfferToTradeMap(first_player.getUniqueId());
				Messenger.info(first_player, "Zaproponowales oferte handlu graczowi " + second_player.getName());
				Messenger.info(second_player, "Gracz " + first_player.getName() + " zaproponowal Ci oferte handlu!" +
						". Wpisz /wymiana " + first_player.getName() + " aby otworzyc menu wymiany.");
			}
		}
	}

	private Inventory createTradingMenu(final Player player1, final Player player2) {
		//ten gracz ktory wyslal propozycje wymiany bedzie na poczatku
		final Inventory inv = Bukkit.createInventory(null, 6 * 9, "Wymiana " + player2.getName() + " z " + player1.getName());
		final ItemStack spaceBetweenEq = new ItemStack(Material.IRON_BARS, 1);

		final ItemMeta spaceMeta = spaceBetweenEq.getItemMeta();
		spaceMeta.setDisplayName(" ");
		spaceBetweenEq.setItemMeta(spaceMeta);

		for (int i = 0; i < 6; i++)
			inv.setItem(i * 9 + 4, spaceBetweenEq);

		final ItemStack firstAccept = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);

		final ItemMeta firstAcceptMeta = firstAccept.getItemMeta();
		firstAcceptMeta.setDisplayName(ChatColor.GOLD + "Kontynuuj transakcje");
		firstAccept.setItemMeta(firstAcceptMeta);

		inv.setItem(5, firstAccept);
		inv.setItem(48, firstAccept);

		return inv;

	}

	@Override
	protected List<String> tabComplete() {

		if (args.length == 1) {//IMP zmienic to na nazwe gracza a nie uuid.!
			final List<Player> playerList = new ArrayList<>();
			for (final UUID uuid : PlayerCache.getCache(getPlayer()).getTradeOffersMap().keySet()) {
				playerList.add(Remain.getPlayerByUUID(uuid));
			}
			return playerList.isEmpty() ? new ArrayList<>() : completeLastWord(playerList);
		}
		return new ArrayList<>();
	}

}