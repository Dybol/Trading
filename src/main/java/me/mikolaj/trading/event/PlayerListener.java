package me.mikolaj.trading.event;

import me.mikolaj.trading.PlayerCache;
import me.mikolaj.trading.menu.BazaarMainMenu;
import me.mikolaj.trading.settings.Settings;
import me.mikolaj.trading.utils.BazaarUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.remain.Remain;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasa odpowiadajaca za interakacje gracza ze swiatem
 */
public class PlayerListener implements Listener {


	/**
	 * Metoda wywolywana podczas interakcji gracza z jakakolwiek jednostka
	 *
	 * @param event - wydarzenie tej interakcji
	 */
	@EventHandler
	public void onInteract(final PlayerInteractAtEntityEvent event) {
		if (Settings.Bazaar.ENABLED) {
			if (event.getRightClicked().getType() == EntityType.ARMOR_STAND && BazaarUtil.isInBazaarRegion(event.getRightClicked().getLocation()))
				event.setCancelled(true);
		}

		if (!(event.getRightClicked() instanceof Player))
			return;

		if (!Remain.isInteractEventPrimaryHand(event))
			return;

		if (Settings.Bazaar.ENABLED) {

			final Player clickedPlayer = (Player) event.getRightClicked();

			if (!BazaarUtil.isInBazaarRegion(clickedPlayer)
					|| !BazaarUtil.isInBazaarRegion(event.getPlayer()))
				return;

			if (PlayerCache.getCache(clickedPlayer).hasBazaar()) {
				new BazaarMainMenu(clickedPlayer).displayTo(event.getPlayer());
			}
		}
	}

	/**
	 * Metoda wywolywana podczas zamkniecia przez gracza ekwipunku
	 * <p>
	 * To tutaj w razie naglego zamkniecia menu z wymiana miedzy graczami
	 * Zwracamy ich itemy ze stanu przed wymiana
	 *
	 * @param event - wydarzenie zamykania ekwipunku
	 */
	@EventHandler
	public void onInventoryClose(final InventoryCloseEvent event) {
		if (event.getView().getTitle().contains(event.getPlayer().getName()) && !event.getView().getTitle().equals("Crafting")) {
			if (event.getViewers().isEmpty())
				return;
			if (event.getViewers().contains((event.getPlayer())) && event.getViewers().size() == 1)
				return;
			// pewnie bedzie trzeba sprawdzic, co jak kilka tradow jest na raz i jeden zamyka.. problem z tylume
			// najlepiej dorobic tytul wymiana z <nick_gracza>
			for (final HumanEntity humanEntity : event.getViewers()) {
				final Player loop_player = (Player) humanEntity;
				final PlayerCache loopPlayerCache = PlayerCache.getCache(loop_player);
				if (!loopPlayerCache.isInTradeEnd()) { //potrzebne, bo jak na koncu dajemy graczom itemy i zamykamy eq to aktywuje sie ten backup
					Common.runLater(() -> loopPlayerCache.restorePlayerSnapshot(loop_player));

					Common.runLater(loop_player::closeInventory);
				}
			}
		}
	}

	/**
	 * Metoda wywolywana podczas klikniecia na cokolwiek w ekwipunku
	 * <p>
	 * To tutaj tworzymy system dzialania wymiany miedzy graczami
	 *
	 * @param event - wydarzenie interakcji w ekwipunku
	 */
	@EventHandler
	public void onInventoryClick(final InventoryClickEvent event) {
		final String title = event.getView().getTitle();
		if (title.contains(event.getWhoClicked().getName()) && !title.equals("Crafting")) {
			if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
				event.setCancelled(true);
				return;
			}
			final String[] args = title.split(" ");
			final Player player1 = Bukkit.getPlayer(args[1]);
			final Player player2 = Bukkit.getPlayer(args[3]);
			if (player1 == null || player2 == null)
				return;
			//gracz 1 moze uzywac tylko przycisku na slocie 48

			for (int i = 0; i < 6; i++) {
				if (event.getRawSlot() == i * 9 + 4 && event.getRawSlot() < 54)
					event.setCancelled(true);
			}

			//sprawdzanie, czy oba przyciski sa zolte, jezeli tak to mozna do zielonych przechodzic. Ale przy zoltych nie mozna nic ruszac!!

			final ItemStack paneAt5 = event.getInventory().getItem(5);
			final ItemStack paneAt48 = event.getInventory().getItem(48);

			final ItemStack currItem = event.getCurrentItem();

			final Player clicker = (Player) event.getWhoClicked();

			//trzeba zrobic anulowanie po kliknieciu na czerwone szklo, ale wczesniej musi wywolac sie metoda zamieniajaca je na zolte
			if (paneAt5 != null && paneAt48 != null && (paneAt5.getType() == Material.RED_STAINED_GLASS_PANE || paneAt48.getType() == Material.RED_STAINED_GLASS_PANE)
					&& event.getRawSlot() != 5 && event.getRawSlot() != 48) {

				//cofamy sie do dwoch czerwonych
				if (paneAt5.getType() == Material.YELLOW_STAINED_GLASS_PANE && event.getClickedInventory() != null && event.getCurrentItem() != null) {
					if (event.getClickedInventory().getViewers().size() == 2) {
						Common.runLater(() -> event.getClickedInventory().setItem(5, new ItemStack(Material.RED_STAINED_GLASS_PANE, 1)));

					}

				} else if (paneAt48.getType() == Material.YELLOW_STAINED_GLASS_PANE && event.getClickedInventory() != null && event.getCurrentItem() != null) {
					if (event.getClickedInventory().getViewers().size() == 2) {
						Common.runLater(() -> event.getClickedInventory().setItem(48, new ItemStack(Material.RED_STAINED_GLASS_PANE, 1)));

					}
				}

				//to w drugim stadium, po wzietu itemu
				if (currItem == null || event.getSlot() < 54) {
					if (clicker.equals(player1)) {
						if (!validInputForPlayer(true, event.getRawSlot()))
							event.setCancelled(true);

					} else if (clicker.equals(player2)) {
						if (!validInputForPlayer(false, event.getRawSlot()))
							event.setCancelled(true);
					} else {
						return;
					}
				}

			} else if (currItem != null && currItem.getType() == Material.RED_STAINED_GLASS_PANE &&
					(event.getRawSlot() == 5 || event.getRawSlot() == 48)) {

				//gracz kliknal nie swoj przycisk
				if ((event.getRawSlot() == 5 && clicker.equals(player1)) || event.getRawSlot() == 48 && clicker.equals(player2)) {
					event.setCancelled(true);
					return;
				}

				//wywolujemy inne metody
				if (event.getClickedInventory() != null) {

					final ItemStack secondAcceptItem = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1);

					final ItemMeta secondAcceptMeta = secondAcceptItem.getItemMeta();
					secondAcceptMeta.setDisplayName(ChatColor.GREEN + "Akceptuj");
					secondAcceptItem.setItemMeta(secondAcceptMeta);


					if (event.getRawSlot() == 5)
						event.getClickedInventory().setItem(5, secondAcceptItem);
					else
						event.getClickedInventory().setItem(48, secondAcceptItem);

				}

				event.setCancelled(true);

			}

			//drugi etap
			else if (paneAt5 != null && paneAt5.equals(paneAt48) && paneAt5.getType() == Material.YELLOW_STAINED_GLASS_PANE
					&& currItem != null && currItem.getType() == Material.YELLOW_STAINED_GLASS_PANE) {
				//niewazne gddzie by bylo, to anulujemy!

				//gracz kliknal nie swoj przycisk
				if ((event.getRawSlot() == 5 && clicker.equals(player1)) || event.getRawSlot() == 48 && clicker.equals(player2)) {
					event.setCancelled(true);
					return;
				}

				if (event.getClickedInventory() != null) {

					final ItemStack thirdAcceptItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);

					final ItemMeta thirdAcceptMeta = thirdAcceptItem.getItemMeta();
					thirdAcceptMeta.setDisplayName(ChatColor.GREEN + "Wszystko zrobione!");
					thirdAcceptItem.setItemMeta(thirdAcceptMeta);


					if (event.getRawSlot() == 5)
						event.getClickedInventory().setItem(5, thirdAcceptItem);
					else if (event.getRawSlot() == 48)
						event.getClickedInventory().setItem(48, thirdAcceptItem);
					// jezeli oba sa zielone, to dajemy itemy dwom stronom

				}
				event.setCancelled(true);
				//jezeli juz wszystko jest ok - ostatni moment, brakuje jednego panelu - zolty i zielony w tym momencie
			} else if (paneAt5 != null && paneAt48 != null &&
					((paneAt5.getType() == Material.GREEN_STAINED_GLASS_PANE && paneAt48.getType() == Material.YELLOW_STAINED_GLASS_PANE)
							|| paneAt5.getType() == Material.YELLOW_STAINED_GLASS_PANE && paneAt48.getType() == Material.GREEN_STAINED_GLASS_PANE)
					&& event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.GREEN_STAINED_GLASS_PANE &&
					(event.getRawSlot() == 5 || event.getRawSlot() == 48)) {

				if ((event.getRawSlot() == 5 && clicker.equals(player1)) || event.getRawSlot() == 48 && clicker.equals(player2)) {
					event.setCancelled(true);
					return;
				}

				for (final HumanEntity viewer : event.getViewers()) {

					if (viewer instanceof Player) {
						final PlayerCache cache = PlayerCache.getCache((Player) viewer);
						cache.setIsInTradeEnd(true);
						Messenger.info(viewer, "&aWymiana udana!");
						Common.runLater(viewer::closeInventory);
					}
				}
				if (event.getClickedInventory() != null) {
					giveTradeItems(player1, player2, event.getClickedInventory());
					Common.runLater(20, () -> {
						PlayerCache.getCache(player1).setIsInTradeEnd(false);
						PlayerCache.getCache(player2).setIsInTradeEnd(false);
					});

				}

			} else { //anulujemy tak czy inaczej
				event.setCancelled(true);
			}

			//szklo - nie zabieramy go oczywiscie
			if (event.getRawSlot() == 5 || event.getRawSlot() == 48)
				event.setCancelled(true);
		}
	}


	/**
	 * Metoda sprawdzajaca, czy gracz wlozyl item w odpowiednie miejsce
	 *
	 * @param player1 - czy gracz jest pierwszym (rozdzielenie graczy na 1 i 2 w celu latwego sprawdzania czy wszystko jest ok)
	 * @param rawSlot - slot na ktory mial zostac wlozony item
	 * @return - true, jezeli wszystko poszlo ok, false w przeciwnym przypadku
	 */
	private boolean validInputForPlayer(final boolean player1, final int rawSlot) {
		if (rawSlot < 0 || rawSlot >= 54)
			return true;

		final int modulo = rawSlot % 9;

		if (player1) {
			return modulo < 4 && rawSlot != 48;
		} else {
			return modulo > 4 && rawSlot != 5;
		}
	}

	/**
	 * Metoda wywolywana podczas sukcesywnej wymiany - kiedy itemy maja zostac zamienione miedzy graczami
	 *
	 * @param player1   - gracz pierwszy
	 * @param player2   - gracz drugi
	 * @param inventory - ekwipunek w ktorym byla wymiana, tzw menu
	 */
	private void giveTradeItems(final Player player1, final Player player2, final Inventory inventory) {
		final List<ItemStack> itemsForPlayer1 = new ArrayList<>();
		final List<ItemStack> itemsForPlayer2 = new ArrayList<>();

		int counter = 0;
		for (final ItemStack item : inventory.getStorageContents()) {

			if (item != null && counter % 9 != 4 && counter != 5 && counter != 48) {
				if (counter % 9 < 4)
					itemsForPlayer2.add(item);
				else
					itemsForPlayer1.add(item);
			}
			counter++;
		}
		for (final ItemStack item : itemsForPlayer1)
			player1.getInventory().addItem(item);
		for (final ItemStack item : itemsForPlayer2)
			player2.getInventory().addItem(item);

	}
}