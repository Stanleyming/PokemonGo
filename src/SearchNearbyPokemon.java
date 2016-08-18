package pokemon;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.NearbyPokemon;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.google.common.geometry.S2CellId;
import com.pokegoapi.google.common.geometry.S2LatLng;
import com.pokegoapi.google.common.geometry.S2Polyline;
import com.pokegoapi.util.Log;
import com.pokegoapi.util.MapUtil;

import POGOProtos.Map.Pokemon.MapPokemonOuterClass;
import POGOProtos.Map.Pokemon.NearbyPokemonOuterClass;
import okhttp3.OkHttpClient;

public class SearchNearbyPokemon {
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Start Run!\n");
		
		try {
			
			OkHttpClient http = new OkHttpClient();
			MapUtil mapUtil = new MapUtil();
			PokemonGo go = new PokemonGo(new PtcCredentialProvider(http, ExampleLoginDetails.LOGIN,
					ExampleLoginDetails.PASSWORD), http);
			Map map = go.getMap();
			int width = 7;
			Double latitude = 22.277;
			Double longitude = 114.161;
			map.setDefaultWidth(1);
			
			// set location
			go.setLocation(latitude, longitude, 0);
//			MapObjects mapObj = map.getMapObjects(latitude, longitude);
//			System.out.println(mapObj.toString());
			
			
			// Phase 1
//			Collection<NearbyPokemonOuterClass.NearbyPokemon> nearbyPokemons = mapObj.getNearbyPokemons();
//			Collection<MapPokemonOuterClass.MapPokemon> catchablePokemons = mapObj.getCatchablePokemons();
			
//			System.out.println("NearbyPokemons = "+nearbyPokemons.toString());
//			System.out.println("CatchablePokemons = "+catchablePokemons.toString());
			
//			if (nearbyPokemons != null) {
//				Iterator it = nearbyPokemons.iterator(); 
//		        while(it.hasNext()){
//		        	NearbyPokemonOuterClass.NearbyPokemon nearbyPokemon = (POGOProtos.Map.Pokemon.NearbyPokemonOuterClass.NearbyPokemon) it.next();
//		        	
//		        	System.out.println("pokemon_id: "+nearbyPokemon.getPokemonId());
//		        }
//			}
//			if (catchablePokemons != null) {
//				Iterator it = catchablePokemons.iterator(); 
//		        while(it.hasNext()){
//		        	MapPokemonOuterClass.MapPokemon catchablePokemon = (MapPokemonOuterClass.MapPokemon) it.next();
//		        	
//		        	System.out.println("catchablePokemon_id: "+catchablePokemon.getPokemonId()+", Latitude: "+catchablePokemon.getLatitude()+", Longitude: "+catchablePokemon.getLongitude()+", Distance: "+ Math.round(mapUtil.distFrom(latitude, longitude, catchablePokemon.getLatitude(), catchablePokemon.getLongitude())*100) +"m, Expire: "+convertTime(catchablePokemon.getExpirationTimestampMs()));
//		        }
//			}

			List<CatchablePokemon> catchablePokemon = (List<CatchablePokemon>) go.getMap().getCatchablePokemon();
			System.out.println("Pokemon in area:" + catchablePokemon.size());
			
			for (CatchablePokemon cp : catchablePokemon) {
				System.out.println("catchablePokemon_id: "+cp.getPokemonId()+", Latitude: "+cp.getLatitude()+", Longitude: "+cp.getLongitude()+", Distance: "+ Math.round(mapUtil.distFrom(latitude, longitude, cp.getLatitude(), cp.getLongitude())) +"m, Expire: "+convertTime(cp.getExpirationTimestampMs()));
//				System.out.println("catchablePokemon_id: "+cp.getPokemonId()+", Distance: "+ Math.round(mapUtil.distFrom(latitude, longitude, cp.getLatitude(), cp.getLongitude())*100) +"m, Expire: "+convertTime(cp.getExpirationTimestampMs()));
			}
			
			
			
			// Phase 2
			List<CatchablePokemon> allCP = new ArrayList<CatchablePokemon>();
			int halfWidth = (int)Math.floor(width / 2);
			for (int x = -halfWidth; x <= halfWidth; x++) {
				for (int y = -halfWidth; y <= halfWidth; y++) {
					Double newLat = latitude + (x * 0.001);
					Double newLng = longitude + (y * 0.001);
					System.out.println("Lat: "+newLat+", Lng: "+newLng);
					
					go.setLocation(newLat, newLng, 0);
					List<CatchablePokemon> tmpCP = go.getMap().getCatchablePokemon();
					
					for (CatchablePokemon cp : tmpCP) {
						System.out.println(cp.getPokemonId());
						if (!allCP.contains(cp)) {
							allCP.add(cp);
						}
					}
				}
			}
			
			System.out.println("Phase 2");
			for (CatchablePokemon cp : allCP) {
				System.out.println("catchablePokemon_id: "+cp.getPokemonId()+", Latitude: "+cp.getLatitude()+", Longitude: "+cp.getLongitude()+", Distance: "+ Math.round(mapUtil.distFrom(latitude, longitude, cp.getLatitude(), cp.getLongitude())) +"m, Expire: "+convertTime(cp.getExpirationTimestampMs()));
			}
			
		} catch (LoginFailedException | RemoteServerException e) {
			// failed to login, invalid credentials, auth issue or server issue.
			Log.e("Main", "Failed to login. Invalid credentials or server issue: ", e);
		}
		
		System.out.println("\nFinish!!");
	}
	
	private static String convertTime(long time){
	    Date date = new Date(time);
//	    Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss.SSS");
	    Format format = new SimpleDateFormat("HH:mm:ss");
	    return format.format(date);
	}
}
