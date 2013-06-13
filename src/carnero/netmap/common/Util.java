package carnero.netmap.common;

import java.io.BufferedReader;
import java.io.IOException;

public class Util {

	/**
	 * Get network level according to type
	 *
	 * @param type
	 * @return
	 */
	public static int getNetworkLevel(int type) {
		for (int i = 0; i < Constants.NET_LEVELS.length; i ++) {
			for (int j = 0; j < Constants.NET_LEVELS[i].length; j ++) {
				if (Constants.NET_LEVELS[i][j] == type) {
					return i;
				}
			}
		}

		return Constants.NET_LEVEL_1;
	}

	public static void readIntoBuffer(BufferedReader br, StringBuffer buffer) throws IOException {
		int bufferSize = 1024*16;
		char[] bytes = new char[bufferSize];
		int bytesRead;
		while ((bytesRead = br.read(bytes)) > 0) {
			if (bytesRead == bufferSize) {
				buffer.append(bytes);
			}
			else {
				buffer.append(bytes, 0, bytesRead);
			}
		}
	}

	/**
	 * Randomly choose some usual user agent
	 *
	 * @return
	 */
	public static String pickUserAgent() {
		final int rnd = (int) Math.round(Math.random() * 6);
		final String idBrowser;

		switch (rnd) {
			case 0:
				idBrowser = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.1 (KHTML, like Gecko) Chrome/5.0.322.2 Safari/533.1";
				break;
			case 2:
				idBrowser = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; MDDC)";
				break;
			case 3:
				idBrowser = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401 Firefox/3.6.3";
				break;
			case 4:
				idBrowser = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_2; en-us) AppleWebKit/531.21.8 (KHTML, like Gecko) Version/4.0.4 Safari/531.21.10";
				break;
			case 5:
				idBrowser = "Mozilla/5.0 (iPod; U; CPU iPhone OS 2_2_1 like Mac OS X; en-us) AppleWebKit/525.18.1 (KHTML, like Gecko) Version/3.1.1 Mobile/5H11a Safari/525.20";
				break;
			case 6:
				idBrowser = "Mozilla/5.0 (Linux; U; Android 1.1; en-gb; dream) AppleWebKit/525.10+ (KHTML, like Gecko) Version/3.0.4 Mobile Safari/523.12.2";
				break;
			case 7:
				idBrowser = "Mozilla/5.0 (X11; U; Linux i686; en-US) AppleWebKit/533.4 (KHTML, like Gecko) Chrome/5.0.375.86 Safari/533.4";
				break;
			default:
				idBrowser = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_2; en-US) AppleWebKit/532.9 (KHTML, like Gecko) Chrome/5.0.307.11 Safari/532.9";
		}

		return idBrowser;
	}
}
