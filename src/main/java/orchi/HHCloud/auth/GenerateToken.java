package orchi.HHCloud.auth;

import java.util.Random;

/**
* Generador de id de tokens
*/
public class GenerateToken {
	private static String valuesToTokens = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-";
	private static int sizeToken = 300;
	private static Random random = new Random();

	public static String newToken() {
		return newToken(sizeToken);
	}

	public static String newToken(int size) {
		return newToken(size,valuesToTokens);
	}
	public static String newToken(int size,String valueToToken) {
		String[] values = valueToToken.split("");
		String token = "";
		int x = 0;
		while (x <= size) {
			int index = random.nextInt(values.length);
			token += values[index];
			x++;
		}
		return token;
	}
}
