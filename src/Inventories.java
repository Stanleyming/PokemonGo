import POGOProtos.Enums.PokemonFamilyIdOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass.PokemonId;
import POGOProtos.Inventory.EggIncubatorOuterClass;
import POGOProtos.Inventory.InventoryItemDataOuterClass;
import POGOProtos.Inventory.InventoryItemOuterClass;
import POGOProtos.Inventory.Item.ItemDataOuterClass.ItemData;
import POGOProtos.Inventory.Item.ItemIdOuterClass.ItemId;
import POGOProtos.Networking.Requests.Messages.GetInventoryMessageOuterClass.GetInventoryMessage;
import POGOProtos.Networking.Requests.RequestTypeOuterClass;
import POGOProtos.Networking.Responses.GetInventoryResponseOuterClass.GetInventoryResponse;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.CandyJar;
import com.pokegoapi.api.inventory.EggIncubator;
import com.pokegoapi.api.inventory.Hatchery;
import com.pokegoapi.api.inventory.Item;
import com.pokegoapi.api.inventory.ItemBag;
import com.pokegoapi.api.inventory.PokeBank;
import com.pokegoapi.api.inventory.Pokedex;
import com.pokegoapi.api.inventory.Stats;
import com.pokegoapi.api.pokemon.EggPokemon;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.main.ServerRequest;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


public class Inventories {

	private final PokemonGo api;
	@Getter
	private ItemBag itemBag;
	@Getter
	private PokeBank pokebank;
	@Getter
	private CandyJar candyjar;
	@Getter
	private Pokedex pokedex;
	@Getter
	private List<EggIncubator> incubators;
	@Getter
	private Hatchery hatchery;

	private long lastInventoryUpdate = 0;

	/**
	 * Creates Inventories and initializes content.
	 *
	 * @param api PokemonGo api
	 * @throws LoginFailedException  the login failed exception
	 * @throws RemoteServerException the remote server exception
	 */
	public Inventories(PokemonGo api) throws LoginFailedException, RemoteServerException {
		this.api = api;
		itemBag = new ItemBag(api);
		pokebank = new PokeBank(api);
		candyjar = new CandyJar(api);
		pokedex = new Pokedex(api);
		incubators = new ArrayList<>();
		hatchery = new Hatchery(api);
		updateInventories();
	}

	/**
	 * Updates the inventories with latest data.
	 *
	 * @throws LoginFailedException  the login failed exception
	 * @throws RemoteServerException the remote server exception
	 */
	public void updateInventories() throws LoginFailedException, RemoteServerException {
		updateInventories(false);
	}

	/**
	 * Updates the inventories with the latest data.
	 *
	 * @param forceUpdate For a full update if true
	 * @throws LoginFailedException  the login failed exception
	 * @throws RemoteServerException the remote server exception
	 */
	public void updateInventories(boolean forceUpdate) throws LoginFailedException, RemoteServerException {
		if (forceUpdate) {
			lastInventoryUpdate = 0;
			itemBag.reset(api);
			pokebank.reset(api);
			candyjar.reset(api);
			pokedex.reset(api);
			incubators = new ArrayList<>();
			hatchery.reset(api);
		}
		GetInventoryMessage invReqMsg = GetInventoryMessage.newBuilder()
				.setLastTimestampMs(lastInventoryUpdate)
				.build();
		ServerRequest inventoryRequest = new ServerRequest(RequestTypeOuterClass.RequestType.GET_INVENTORY, invReqMsg);
		api.getRequestHandler().sendServerRequests(inventoryRequest);

		GetInventoryResponse response = null;
		try {
			response = GetInventoryResponse.parseFrom(inventoryRequest.getData());
		} catch (InvalidProtocolBufferException e) {
			throw new RemoteServerException(e);
		}

		for (InventoryItemOuterClass.InventoryItem inventoryItem
				: response.getInventoryDelta().getInventoryItemsList()) {
			InventoryItemDataOuterClass.InventoryItemData itemData = inventoryItem.getInventoryItemData();

			// hatchery
			if (itemData.getPokemonData().getPokemonId() == PokemonId.MISSINGNO && itemData.getPokemonData().getIsEgg()) {
				hatchery.addEgg(new EggPokemon(itemData.getPokemonData()));
			}

			// pokebank
			if (itemData.getPokemonData().getPokemonId() != PokemonId.MISSINGNO) {
				pokebank.addPokemon(new Pokemon(api, inventoryItem.getInventoryItemData().getPokemonData()));
			}

			// items
			if (itemData.getItem().getItemId() != ItemId.UNRECOGNIZED
					&& itemData.getItem().getItemId() != ItemId.ITEM_UNKNOWN) {
				ItemData item = itemData.getItem();
				itemBag.addItem(new Item(item));
			}

			// candyjar
			if (itemData.getCandy().getFamilyId() != PokemonFamilyIdOuterClass.PokemonFamilyId.UNRECOGNIZED
					&& itemData.getCandy().getFamilyId() != PokemonFamilyIdOuterClass.PokemonFamilyId.FAMILY_UNSET) {
				candyjar.setCandy(
						itemData.getCandy().getFamilyId(),
						itemData.getCandy().getCandy()
				);
			}
			// player stats
			if (itemData.hasPlayerStats()) {
				api.getPlayerProfile().setStats(new Stats(itemData.getPlayerStats()));
			}

			// pokedex
			if (itemData.hasPokedexEntry()) {
				pokedex.add(itemData.getPokedexEntry());
			}

			if (itemData.hasEggIncubators()) {
				for (EggIncubatorOuterClass.EggIncubator incubator : itemData.getEggIncubators().getEggIncubatorList()) {
					incubators.add(new EggIncubator(api, incubator));
				}
			}

			lastInventoryUpdate = api.currentTimeMillis();
		}
	}
}