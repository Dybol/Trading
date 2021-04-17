package me.mikolaj.trading.command;

import me.mikolaj.trading.PlayerCache;
import me.mikolaj.trading.menu.BazaarDeleteMenu;
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

public class BazaarCommand extends SimpleCommand {
	public BazaarCommand() {
		super("bazar");
		//setMinArguments(1);
	}

	@Override
	protected void onCommand() {
		checkConsole();
		final Player player = getPlayer();
		if (args.length == 0) {
			Messenger.info(player, "Uzyj komendy /bazar info po wiecej informacji");
			return;
		}
		final String action = args[0];
		final PlayerCache cache = PlayerCache.getCache(player);

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

				//dodac dokladnie ten item co ma gracz
				if (!item.toString().contains("AIR x 0")) {//czy na pewno to dziala??
					if (CompMaterial.fromItem(item) == CompMaterial.GOLD_INGOT || CompMaterial.fromItem(item) == CompMaterial.GOLD_BLOCK) {
						Messenger.warn(player, "Nie mozesz handlowac waluta! Item nie został dodany.");
						return;
					}

					cache.addEverything(item, item.getItemMeta(), sellAmount, buyAmount);
					//player.getInventory().remove(player.getItemInHand());
					Messenger.success(player, "Dodales " + item.toString() + ". Sprzedajesz go za " + sellAmount + " zlota, a kupujesz za " + buyAmount);
				} else {
					//to dziala
					Messenger.error(player, "Musisz miec w rece jakis item, ktory chcesz dodac!");
				}
			} catch (final NumberFormatException ex) {
				Messenger.error(player, "Wprowadz odpowiednie wartosci! Poprawne uzycie - /bazar dodaj <cenaSprzedazy> <cenaKupna>");
			}
		}
		else if("otworz".equals(action)) {
			if (!BazaarUtil.isInBazaarRegion(player)) {
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

			//todo dorobione bazary
			if (cache.getBazaarName() != null) {
				final ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation().clone().add(0, 0, 0), EntityType.ARMOR_STAND);
				cache.setBazaarStand(stand);

				stand.setVisible(false);
				stand.setCustomName(Common.colorize(cache.getBazaarName()));
				stand.setCustomNameVisible(true);
				stand.setGravity(false);

			}

			cache.setHasBazaar(true);
			cache.setBazaarLoc(player.getLocation());
			Messenger.success(player, "Otworzyles bazar!");

			//dzieki bazaar taskowi gracz stoi w miejscu !
			player.setWalkSpeed(0);
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 100000, 100000));

		}
		else if("zamknij".equals(action)) {
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

		} else if ("nazwa".equals(action)) {
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

				System.out.println("wywo " + i);
			}

			if (counter > 20) {
				Messenger.error(player, "Nazwa bazaru moze miec maksymalnie 20 znakow!");
				return;
			}

			cache.setBazaarName(s.toString());
			Messenger.success(player, "Ustawiles nazwe");

		} else if ("lista".equals(action)) {
			if (cache.getCounter() == 0)
				returnTell("&cTwoj bazar jest pusty!");

			Messenger.announce(player, "Lista: ");
			for (int i = 0; i < cache.getCounter(); i++)
				Common.tell(player, BazaarUtil.getItemAndAmountFormated(cache.getContent()[i], cache.getSellAmount()[i], cache.getBuyAmount()[i]));

		} else if ("usunitem".equals(action)) {
			if (cache.hasBazaar())
				returnTell("&cNie mozesz usuwac itemow z otwartego bazaru!");
			new BazaarDeleteMenu(player).displayTo(player);
		} else if ("info".equals(action)) {
			Messenger.info(player, "&aDodawanie itemu: &7/bazar dodaj <cenaSprzedazy> <cenaKupna>. Jezeli nie chcesz kupowac / sprzedawac wpisz 0");
			Messenger.info(player, "&aOtwieranie bazaru: &7/bazar otworz. Nie bedziesz sie mogl ruszac, dopoki nie zamkniesz bazaru");
			Messenger.info(player, "&aLista dodanych itemow: &7/bazar lista");
			Messenger.info(player, "&aUstawianie nazwy: &7/bazar nazwa");
			Messenger.info(player, "&aUsuwanie dodanego itemu: &7/bazar usunitem");
			Messenger.info(player, "&aZamykanie bazaru: &7/bazar zamknij");

		} else {
			Messenger.info(player, "Uzyj komendy /bazar info po wiecej informacji");
		}
	}

	@Override
	protected List<String> tabComplete() {
		if (args.length == 1) {
			return completeLastWord("dodaj", "otworz", "zamknij", "lista", "usunitem", "info", "nazwa");
		}
		return new ArrayList<>();
	}

}
