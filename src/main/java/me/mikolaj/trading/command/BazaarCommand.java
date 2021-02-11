package me.mikolaj.trading.command;

import me.mikolaj.trading.PlayerCache;
import me.mikolaj.trading.utils.BazaarUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.command.SimpleCommand;

import java.util.List;

public class BazaarCommand extends SimpleCommand {
	public BazaarCommand() {
		super("bazar");
		setMinArguments(1);
	}

	@Override
	protected void onCommand() {
		final String action = args[0];
		final Player player = getPlayer();
		final PlayerCache cache = PlayerCache.getCache(player);

		if("dodaj".equals(action)) {
			if(args.length == 1)
				returnTell("&cUzyj komendy poprawnie - /{label} dodaj <ilosc_zlota>");
			final int amount_of_gold = Integer.parseInt(args[1]);
			final ItemStack item = player.getInventory().getItemInMainHand();

			//dodac dokladnie ten item co ma gracz
			if(!item.toString().contains("AIR x 0")) {//czy na pewno to dziala??

				cache.addEverything(item, item.getItemMeta(), amount_of_gold);
				//player.getInventory().remove(player.getItemInHand());
				Messenger.success(player, "Udalo Ci sie dodac " + item.toString() + " za " + amount_of_gold + " zlota");
			}
			else {
				Messenger.error(player, "Musisz miec w rece jakis item, ktory chcesz dodac!");
			}
		}
		else if("otworz".equals(action)) {
			if(!BazaarUtil.isInBazaarRegion(player)) {
				Messenger.error(player, "Musisz znajdowac sie w odpowiednim miejscu!");
				return;
			}

			if(cache.getCounter() == 0) {
				Messenger.error(player, "Musisz dodac chociaz 1 item, aby moc otworzyc bazar!");
				return;
			}

			if(cache.hasBazaar()) {
				Messenger.error(player, "Juz otworzyles bazar! Aby go zamknac, wpisz /bazar zamknij");
				return;
			}
			cache.setHasBazaar(true);
			cache.setBazaarLoc(player.getLocation());
			Messenger.success(player, "Otworzyles bazar!");

			//dzieki bazaar taskowi gracz stoi w miejscu !
		    player.setWalkSpeed(0);
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 100000, 100000));

		}
		else if("zamknij".equals(action)) {
			if(!cache.hasBazaar()) {
				Messenger.error(player, "Nie otworzyles jeszcze bazaru!");
				return;
			}
			player.setWalkSpeed(0.2f);
			player.removePotionEffect(PotionEffectType.JUMP);
			cache.setHasBazaar(false);
			cache.clearContent();
			cache.setBazaarLoc(null);

			Messenger.announce(player, "Zamknales bazar!");
		}

		else if("nazwa".equals(action)) {
			//TODO trzeba dopracowac - zeby nad graczem sie wyswietlalo wszystko ;)
			player.setCustomName("OTWARTE XD");
			player.setCustomNameVisible(true);
			Messenger.success(player, "Ustawiles nazwe");
		}

		else if("lista".equals(action)) {
			if(cache.getCounter() == 0)
				returnTell("&cTwoj bazar jest pusty!");

			Messenger.announce(player, "Lista: ");
			for(int i = 0; i < cache.getCounter(); i++)
				Common.tell(player, BazaarUtil.getItemAndAmountFormated(cache.getContent()[i], cache.getAmount()[i]));
		}
		else if("usun".equals(action)) {
			//TODO zaimplementowac usuwanie itemow
			//moze byc problematyczne, bo trzeba by przesuwac wszystko o jedno miejsce w tablicy, ale to do pomyslenia pozniej
		}
	}

	@Override
	protected List<String> tabComplete() {
		if(args.length == 1) {
			return completeLastWord("dodaj", "otworz", "zamknij", "lista");
		}
		return null;
	}

}
