package me.mikolaj.trading.command;

import me.mikolaj.trading.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.mineacademy.fo.Messenger;

//old way - testing
public class TradeTestCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull final CommandSender commandSender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {

		if (commandSender instanceof Player) {
			final Player first_player = (Player) commandSender;
			if (args.length == 0) {
				//uzycie:
				//PlayerCache.getCache(first_player).restorePlayerSnapshot(first_player);
				first_player.sendMessage(ChatColor.DARK_BLUE + "Poprawne uzycie: " + label + "<nick_gracza>");
				Messenger.info(first_player, "&aPoprawne uzycie: " + label + " <nick_gracza>");
				return false;
			}

			if (args.length == 1) {
				//hgw xd
				final Player second_player = Bukkit.getPlayer(args[0]);
				if (second_player == null) {
					first_player.sendMessage(ChatColor.RED + "Nie ma takiego gracza!");
					return false;
				}
				if (first_player.equals(second_player)) {
					first_player.sendMessage(ChatColor.RED + "Nie mozesz wyslac sobie oferty handlu!");
					return false;
				}

				final PlayerCache first_cache = PlayerCache.getCache(first_player);
				final PlayerCache second_cache = PlayerCache.getCache(second_player);

				if (first_cache.getTradeOffersMap().get(second_player.getUniqueId()) != null) {
					//final Menu menu = new TradingMenu(first_player, second_player);
					//todo
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

			return true;
		}
		return false;
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
	
}