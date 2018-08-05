package orchi.HHCloud.auth;

import java.util.Random;

public class GenerateToken {
	private static String[] valuesToTokens = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"
			.split("");
	private static int sizeToken = 300;
	private static Random random = new Random();

	public static String newToken() {
		return newToken(sizeToken);
	}

	public static String newToken(int size) {
		String token = "";
		int x = 0;
		while (x <= size) {
			int index = random.nextInt(valuesToTokens.length);
			token += valuesToTokens[index];
			x++;
		}
		return token;
	}
}
