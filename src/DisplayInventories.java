import POGOProtos.Enums.PokemonIdOuterClass;
import POGOProtos.Networking.Responses.ReleasePokemonResponseOuterClass;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.util.Log;
import com.pokegoapi.util.PokeNames;

import okhttp3.OkHttpClient;

import java.util.List;
import java.util.Locale;


public class DisplayInventories {
	
	public static void main(String[] args) {
		OkHttpClient http = new OkHttpClient();
		try {
			// check readme for other example
			PokemonGo go = new PokemonGo(new PtcCredentialProvider(http, ExampleLoginDetails.LOGIN,
					ExampleLoginDetails.PASSWORD), http);

			List<Pokemon> pokeList = go.getInventories().getPokebank().getPokemons();
			
			if (pokeList != null && pokeList.size() > 0) {
				for (int i=0; i<pokeList.size(); i++) {
					Pokemon pokemon = pokeList.get(i);
					
					System.out.println("Name: " + PokeNames.getDisplayName(pokemon.getPokemonId().getNumber(), Locale.ENGLISH));
					System.out.println("CP: " + pokemon.getCp());
					System.out.println("HP: " + pokemon.getStamina());
					System.out.println("IVRatio: " + pokemon.getIvRatio());
					System.out.println("ATK: " + pokemon.getIndividualAttack());
					System.out.println("DEF: " + pokemon.getIndividualDefense());
					System.out.println("STA: " + pokemon.getIndividualStamina());
					System.out.println("");
				}
			}
			
		} catch (LoginFailedException | RemoteServerException e) {
			// failed to login, invalid credentials, auth issue or server issue.
			Log.e("Main", "Failed to login. Invalid credentials or server issue: ", e);
		}
	}
}
