package hw5;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import com.google.common.base.CharMatcher;

public class CustomAddressGenerator
{
	/*  @param prefix	string of letters in base58 encoding
	 *  @returns 		a Bitcoin address on mainnet which starts with 1 followed prefix.
     */

	private static final NetworkParameters Net_Params = MainNetParams.get();
	private static final int MAX_ADDRESS_LENGTH = 35;
	private static long attempts;

	public static String get(String prefix)
	{
		if(checkValid(prefix))
		{
			String bitAddr;
			System.out.println("Generating custom bitcoin address with: " + prefix);
			do
			{
				bitAddr = new ECKey().toAddress(Net_Params).toString();
				attempts = attempts + 1;

				if(attempts % 200000 == 0)
				{
					System.out.println("Still working on thread " + Thread.currentThread().getName() + ", with Attempts: " + attempts);
				}
			} while(!(bitAddr.startsWith(prefix)));

			System.out.println("Finishing thread " + Thread.currentThread().getName() + ", with Attempts: " + attempts);
			return bitAddr;
		}

		else
		{
			System.out.println("Can not find valid bitcoin address.");
			return null;
		}
	}

	private static boolean checkValid(final String substring)
	{
		boolean flag = true;
		if(!CharMatcher.JAVA_LETTER_OR_DIGIT.matchesAllOf(substring) || substring.length() > MAX_ADDRESS_LENGTH || CharMatcher.anyOf("OIl0").matchesAnyOf(substring))
		{
			flag = false;
		}
		return flag;
	}

	public static void main(String[] args)
	{
		String address = get("1ZHUX");
		System.out.println("Custom address is: " + address);
	}
}