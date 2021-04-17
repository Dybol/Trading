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
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.remain.Remain;

import java.util.ArrayList;
import java.util.List;

public class PlayerListener implements Listener {

	//IMP dorobic, ze jak sie wchodzi to gracz moze normalnie chodzic, moze w cachu zapisywac tylko czy ma bazar i na tej podstawie po wejsciu usuwac efekty/??

	@EventHandler
	public void onInteract(final PlayerInteractAtEntityEvent event) {
		if (Settings.Bazaar.ENABLED) {
			if (event.getRightClicked().getType() == EntityType.ARMOR_STAND && BazaarUtil.isInBazaarRegion(event.getRightClicked().getLocation())) {
				event.setCancelled(true);
				System.out.println("anulowaon");
			}
		}

		if (!(event.getRightClicked() instanceof Player))
			return;

		if (!Remain.isInteractEventPrimaryHand(event))
			return;

		//IMP dodac try / catch i wyswietlac blad jak bedzie wprowadzona zla lokalizacja.
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
			System.out.println("Gracz 1: " + player1.getName());
			System.out.println("Gracz 2: " + player2.getName());


			System.out.println("Slot (raw)" + event.getRawSlot());
			System.out.println("Slot (zwykly)" + event.getSlot());

			for (int i = 0; i < 6; i++) {
				if (event.getRawSlot() == i * 9 + 4 && event.getRawSlot() < 54)
					event.setCancelled(true);
			}

			//sprawdzanie, czy oba przyciski sa zolte, jezeli tak to mozna do zielonych przechodzic. Ale przy zoltych nie mozna nic ruszac!!

			final ItemStack paneAt5 = event.getInventory().getItem(5);
			final ItemStack paneAt48 = event.getInventory().getItem(48);

			final ItemStack currItem = event.getCurrentItem();

			final Player clicker = (Player) event.getWhoClicked();

			System.out.println(event.getCurrentItem() + " <----- CURR ITEM");

			//trzeba zrobic anulowanie po kliknieciu na czerwone szklo, ale wczesniej musi wywolac sie metoda zamieniajaca je na zolte ;)
			if (paneAt5 != null && paneAt48 != null && (paneAt5.getType() == Material.RED_STAINED_GLASS_PANE || paneAt48.getType() == Material.RED_STAINED_GLASS_PANE)
					&& event.getRawSlot() != 5 && event.getRawSlot() != 48) {


				//cofamy sie do dwoch czerwonych
				if (paneAt5.getType() == Material.YELLOW_STAINED_GLASS_PANE && event.getClickedInventory() != null && event.getCurrentItem() != null) {
					if (event.getClickedInventory().getViewers().size() == 2) {
						Common.runLater(() -> event.getClickedInventory().setItem(5, new ItemStack(Material.RED_STAINED_GLASS_PANE, 1)));

						//event.getClickedInventory().setItem(5, new ItemStack(Material.RED_STAINED_GLASS_PANE, 1));
					}

				} else if (paneAt48.getType() == Material.YELLOW_STAINED_GLASS_PANE && event.getClickedInventory() != null && event.getCurrentItem() != null) {
					if (event.getClickedInventory().getViewers().size() == 2) {
						Common.runLater(() -> event.getClickedInventory().setItem(48, new ItemStack(Material.RED_STAINED_GLASS_PANE, 1)));

						//event.getClickedInventory().setItem(48, new ItemStack(Material.RED_STAINED_GLASS_PANE, 1));
					}
				}
				//tutaj wywolamy nowa metode. todo nic w niej nie ma waznego, pewnie mozna usunac.
				handleItemManipulation(event);


				if (clicker.equals(player1))
					System.out.println("Walidacja dla " + player1.getName() + validInputForPlayer(true, event.getRawSlot()));
				else
					System.out.println("Walidacja dla " + player2.getName() + validInputForPlayer(false, event.getRawSlot()));


				//to w drugim stadium, po wzietu itemu
				if (currItem == null || event.getSlot() < 54) {
					if (clicker.equals(player1)) {
						if (!validInputForPlayer(true, event.getRawSlot()))
							event.setCancelled(true);

					} else if (clicker.equals(player2)) {
						if (!validInputForPlayer(false, event.getRawSlot()))
							event.setCancelled(true);
					} else {
						System.out.println("hgw czemu zadnego gracza nie znaleziono");
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
					thirdAcceptMeta.setDisplayName(ChatColor.GREEN + "DONE!");
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

				} else {
					System.out.println("blad, eq gracza jest nullem kiedy nie powinno byc (sam koniec wymiany)");
					for (final HumanEntity p : event.getViewers())
						Common.tell(p, "BLAD ://");

				}

				if (event.getClickedInventory() != null)
					for (final ItemStack item : event.getClickedInventory().getStorageContents())
						System.out.println("Item ( z petli ): " + item);

			} else { //anulujemy tak czy inaczej
				event.setCancelled(true);
			}

			//szklo - nie zabieramy go ofc!
			if (event.getRawSlot() == 5 || event.getRawSlot() == 48)
				event.setCancelled(true);
		}

		//System.out.println(event.getInventory().getViewers() + "<-- zwykle inv");
		if (event.getClickedInventory() != null) ;
		//System.out.println(event.getClickedInventory().getViewers() + "<-- klikniete");
	}

	private void handleItemManipulation(final InventoryClickEvent event) {

		//sprawdzamy czy na pewno w dobrym ekwipunku jestesmy
		if (event.getClickedInventory() == null)
			return;

		final ItemStack currItem = event.getCurrentItem();
		if (currItem == null && event.getRawSlot() < 54 && event.getClickedInventory().getViewers().size() == 2)
			System.out.println("Item zostal wlozony, ale hgw czy byl tam wczesniej czy nie");
		else if (currItem == null && event.getRawSlot() >= 54 && event.getClickedInventory().getViewers().size() == 1)
			System.out.println("Item jest w zwyklym eq, ale hgw czy byl juz tam wczesniej czy nie");
	}

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

	private void giveTradeItems(final Player player1, final Player player2, final Inventory inventory) {
		final List<ItemStack> itemsForPlayer1 = new ArrayList<>();
		final List<ItemStack> itemsForPlayer2 = new ArrayList<>();

		int counter = 0;
		for (final ItemStack item : inventory.getStorageContents()) {
			System.out.println("Item " + item + " na slocie nr: " + counter);
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

	@EventHandler
	public void onItemTake(final InventoryMoveItemEvent event) {
		System.out.println("Z move event");
	}

}