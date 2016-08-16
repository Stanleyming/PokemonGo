import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import okhttp3.OkHttpClient;

import java.util.Scanner;

public class GoogleUserInteractionExample {

	/**
	 * Example on how to login with Google by asking a token from the user
	 * @param args stuff
	 */
	public static void main(String[] args) {
		OkHttpClient http = new OkHttpClient();
		try {
			// instanciate a provider, it will give an url
			GoogleUserCredentialProvider provider = new GoogleUserCredentialProvider(http);
			
			// in this url, you will get a code for the google account that is logged
			System.out.println("Please go to " + provider.LOGIN_URL);
			System.out.println("Enter authorisation code:");
			
			// Ask the user to enter it in the standart input
			Scanner sc = new Scanner(System.in);
			String access = sc.nextLine();
			
			// we should be able to login with this token
			provider.login(access);
			System.out.println("Refresh token:" + provider.getRefreshToken());
			
		} catch (LoginFailedException | RemoteServerException e) {
			e.printStackTrace();
		}

	}
}