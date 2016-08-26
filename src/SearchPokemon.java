package pokemon;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.util.Log;
import com.pokegoapi.util.MapUtil;

import okhttp3.OkHttpClient;

public class SearchPokemon implements Runnable {

	MapUtil mapUtil = new MapUtil();
	
	final static int width = 10;
	final static int halfWidth = (int)Math.floor(width / 2);
	
	private Thread t;
	private String threadName;
	private Double latitude, longitude;
	private PokemonGo api;
	private List<CatchablePokemon> allCP;
	
	public static void main(String args[]) {
		System.out.println("main - start");
		
		try {
			
			OkHttpClient http = new OkHttpClient();
			PtcCredentialProvider ptc = new PtcCredentialProvider(http, ExampleLoginDetails.LOGIN, ExampleLoginDetails.PASSWORD);
			PokemonGo go = new PokemonGo(new PtcCredentialProvider(http, ExampleLoginDetails.LOGIN,
					ExampleLoginDetails.PASSWORD), http);

			Double latitude = 22.294881;
			Double longitude = 114.166765;
			
			for (int x=-halfWidth; x<=halfWidth; x++) {
				Double newLat = latitude + (x * 0.00025); // each 0.00025 = 25m
				
				SearchPokemon r = new SearchPokemon(go, newLat, longitude);
				
				r.start();
			}
			
		} catch (LoginFailedException | RemoteServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//	    System.out.println("main - end");
	}
	
	public SearchPokemon(PokemonGo go, Double inLatitude, Double inLongitude){
//		 threadName = name;
		latitude = inLatitude;
		longitude = inLongitude;
		api = go;
//		System.out.println("Creating " +  latitude + "," + longitude );
	}
	
	public void run() {
//		System.out.println("Running " +  latitude + "," + longitude );
		try {
			for (int y=-halfWidth; y<=halfWidth; y++) {
				Double newLng = longitude + (y * 0.00025); // each 0.0005 = 50m
				
				System.out.println("Thread["+y+"]: " + latitude + "," + newLng);
				
				api.setLocation(latitude, newLng, 0);
				Map map = new Map(api);
				List<CatchablePokemon> tmpCP = map.getCatchablePokemon();
				System.out.println("tmpCP size: "+tmpCP.size());
				
				if (allCP == null) {
					allCP = new ArrayList<CatchablePokemon>();
				}
				
				if (tmpCP != null) {
					for (CatchablePokemon cp : tmpCP) {
						
						System.out.println("Catchable Pokemon: "+cp.getPokemonId()+"("+cp.getPokemonIdValue()+"), Lat/Lng: "+cp.getLatitude()+", "+cp.getLongitude()+", Distance: "+ Math.round(mapUtil.distFrom(latitude, longitude, cp.getLatitude(), cp.getLongitude())) +"m, Expire: "+convertTime(cp.getExpirationTimestampMs()));
						if (!allCP.contains(cp)) {
							allCP.add(cp);
						}
					}
				}
				
				// Let the thread sleep for a while.
				Thread.sleep(50);
			}
	  } catch (InterruptedException e) {
		  System.out.println("Thread " +  latitude + "," + longitude + " interrupted. [InterruptedException]");
	  } catch (RemoteServerException e) {
		  System.out.println("Thread " +  latitude + "," + longitude + " interrupted. [RemoteServerException]");
	  } catch (LoginFailedException e) {
		  System.out.println("Thread " +  latitude + "," + longitude + " interrupted. [LoginFailedException]");
	  }
	  System.out.println("Thread " +  latitude + "," + longitude + " exiting.");
	}
	
	public void start () {
//		System.out.println("Starting " +  latitude + "," + longitude );
		if (t == null) {
			Runnable r = new SearchPokemon(api, latitude, longitude);
			t = new Thread(r);
			t.start ();
		}
	}
	
	private static String convertTime(long time){
		 Date date = new Date(time);
//		 Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss.SSS");
		 Format format = new SimpleDateFormat("HH:mm:ss");
		 return format.format(date);
	}
}
