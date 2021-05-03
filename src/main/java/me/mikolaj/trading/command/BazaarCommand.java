package me.mikolaj.trading.command;

import me.mikolaj.trading.PlayerCache;
import me.mikolaj.trading.menu.BazaarDeleteMenu;
import me.mikolaj.trading.settings.Settings;
import me.mikolaj.trading.utils.BazaarUtil;
import me.mikolaj.trading.utils.Constans;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.command.SimpleCommand;
import org.mineacademy.fo.remain.CompMaterial;

import java.util.ArrayList;
import java.util.List;

/**
 * Komendy zwiazane z bazarami
 */
public class BazaarCommand extends SimpleCommand {

	/**
	 * Tworzenie nazwy komendy
	 */
	public BazaarCommand() {
		super("bazar");
	}

	/**
	 * Metoda wywolywana po wpisaniu komendy
	 */
	@Override
	protected void onCommand() {
		checkConsole();

		final Player player = getPlayer();

		//brak argumentow
		if (args.length == 0) {
			Messenger.info(player, "Uzyj komendy /bazar info po wiecej informacji");
			return;
		}

		final String action = args[0];
		final PlayerCache cache = PlayerCache.getCache(player);

		// ------------------- w zaleznosci od pierwszego argumentu wywolujemy rozne akcje -------------------

		//akcje wywolywane podczas dodania itemu
		if ("dodaj".equals(action)) {
			if (args.length == 1 || args.length == 2)
				returnTell("&cUzyj komendy poprawnie - /bazar dodaj <cenaSprzedazy> <cenaKupna>");

			if (cache.getCounter() == Constans.MAX_ITEMS_IN_BAZAAR) {
				returnTell("&cDodales maksymalna ilosc przedmiotow do bazaru!");
			}

			if (cache.hasBazaar()) {
				returnTell("&cZamknij bazar, zeby moc dodawac itemy!");
			}

			try {
				final int sellAmount = Integer.parseInt(args[1]);
				final int buyAmount = Integer.parseInt(args[2]);
				if (sellAmount == 0 && buyAmount == 0) {
					Messenger.error(player, "Wprowadz chociaz jedna niezerowa wartosc!");
					return;
				}
				final ItemStack item = player.getInventory().getItemInMainHand();

				if (!item.toString().contains("AIR x 0")) {
					if (CompMaterial.fromItem(item) == CompMaterial.GOLD_INGOT || CompMaterial.fromItem(item) == CompMaterial.GOLD_BLOCK) {
						Messenger.warn(player, "Nie mozesz handlowac waluta! Item nie został dodany.");
						return;
					}

					cache.addEverything(item, item.getItemMeta(), sellAmount, buyAmount);
					Messenger.success(player, "Dodales " + item.toString() + ". Sprzedajesz go za " + sellAmount + " zlota, a kupujesz za " + buyAmount);
				} else {
					Messenger.error(player, "Musisz miec w rece jakis item, ktory chcesz dodac!");
				}
			} catch (final NumberFormatException ex) {
				Messenger.error(player, "Wprowadz odpowiednie wartosci! Poprawne uzycie - /bazar dodaj <cenaSprzedazy> <cenaKupna>");
			}
		}

		//akcje wywolywanie po otworzeniu bazaru
		else if ("otworz".equals(action)) {
			if (!BazaarUtil.isInBazaarRegion(player) && !Settings.Bazaar.BAZAARS_EVERYWHERE) {
				Messenger.error(player, "Musisz znajdowac sie w odpowiednim miejscu!");
				return;
			}

			if (cache.getCounter() == 0) {
				Messenger.error(player, "Musisz dodac chociaz 1 item, aby moc otworzyc bazar!");
				return;
			}

			if (cache.hasBazaar()) {
				Messenger.error(player, "Juz otworzyles bazar! Aby go zamknac, wpisz /bazar zamknij");
				return;
			}

			if (cache.getBazaarName() != null) {
				final ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation().clone().add(0, 0.2, 0), EntityType.ARMOR_STAND);
				cache.setBazaarStand(stand);

				stand.setVisible(false);
				stand.setCustomName(Common.colorize(cache.getBazaarName()));
				stand.setCustomNameVisible(true);
				stand.setGravity(false);

			}

			cache.setHasBazaar(true);
			cache.setBazaarLoc(player.getLocation());
			Messenger.success(player, "Otworzyles bazar!");

			player.setWalkSpeed(0);
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 100000, 100000));

		}

		//akcje wywolywanie po zamknieciu bazaru
		else if ("zamknij".equals(action)) {
			if (!cache.hasBazaar()) {
				Messenger.error(player, "Nie otworzyles jeszcze bazaru!");
				return;
			}

			if (cache.getBazaarStand() != null) {
				cache.getBazaarStand().remove();
				cache.setBazaarStand(null);
				cache.setBazaarName(null);
			}

			player.setWalkSpeed(0.2f);
			player.removePotionEffect(PotionEffectType.JUMP);
			cache.setHasBazaar(false);
			cache.clearContent();
			cache.setBazaarLoc(null);

			Messenger.announce(player, "Zamknales bazar!");
		}

		//akcje wywolywanie po ustawieniu nazwy bazaru
		else if ("nazwa".equals(action)) {
			if (args.length == 1) {
				Messenger.error(player, "Wprowadz nazwe bazaru!");
				return;
			}

			final StringBuilder s = new StringBuilder();

			int counter = 0;
			for (int i = 1; i < args.length; i++) {
				counter += args[i].length();
				s.append(args[i]);
				if (i != args.length - 1)
					s.append(" ");
			}

			if (counter > 20) {
				Messenger.error(player, "Nazwa bazaru moze miec maksymalnie 20 znakow!");
				return;
			}

			cache.setBazaarName(s.toString());
			Messenger.success(player, "Ustawiles nazwe");
		}

		//akcje wywolywanie po wylistowaniu wszystkich itemow z bazaru
		else if ("lista".equals(action)) {
			if (cache.getCounter() == 0)
				returnTell("&cTwoj bazar jest pusty!");

			Messenger.announce(player, "Lista: ");
			for (int i = 0; i < cache.getCounter(); i++)
				Common.tell(player, BazaarUtil.getItemAndAmountFormated(cache.getContent()[i], cache.getSellAmount()[i], cache.getBuyAmount()[i]));

		}
		//akcje wywolywanie po checi usuniecia itemu
		else if ("usunitem".equals(action)) {
			if (cache.hasBazaar())
				returnTell("&cNie mozesz usuwac itemow z otwartego bazaru!");
			new BazaarDeleteMenu(player).displayTo(player);
		}

		//akcje wywolywanie po uzyskaniu informacji
		else if ("info".equals(action)) {
			Messenger.info(player, "&aDodawanie itemu: &7/bazar dodaj <cenaSprzedazy> <cenaKupna>. Jezeli nie chcesz kupowac / sprzedawac wpisz 0");
			Messenger.info(player, "&aOtwieranie bazaru: &7/bazar otworz. Nie bedziesz sie mogl ruszac, dopoki nie zamkniesz bazaru");
			Messenger.info(player, "&aLista dodanych itemow: &7/bazar lista");
			Messenger.info(player, "&aUstawianie nazwy: &7/bazar nazwa");
			Messenger.info(player, "&aUsuwanie dodanego itemu: &7/bazar usunitem");
			Messenger.info(player, "&aZamykanie bazaru: &7/bazar zamknij");
		}

		//wprowadzono nieistniejaca opcje
		else {
			Messenger.info(player, "Uzyj komendy /bazar info po wiecej informacji");
		}
	}

	/**
	 * Metoda sluzaca za proponowanie argumentow po wpisaniu komendy
	 *
	 * @return - lista slow, ktore maja zostac podpowiedziane
	 */
	@Override
	protected List<String> tabComplete() {
		if (args.length == 1) {
			return completeLastWord("dodaj", "otworz", "zamknij", "lista", "usunitem", "info", "nazwa");
		}
		return new ArrayList<>();
	}

}
