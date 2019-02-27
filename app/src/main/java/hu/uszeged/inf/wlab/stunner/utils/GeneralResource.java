package hu.uszeged.inf.wlab.stunner.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import android.os.Build;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import android.util.Base64;
import android.util.Log;

/**
 * General resource class.
 * 
 * @author szelezsant
 */
public final class GeneralResource {

	/**
	 * Hashing algorithm constant.
	 */
	private static final String COLOR = "SHA";

	/**
	 * Radix value for hash computation.
	 */
	private static final int RADIX = 16;

	/**
	 * Constant for hash computation.
	 */
	private static final int HEX_100 = 0x100;

	/**
	 * Mask for hash computation.
	 */
	private static final int FULL_MASK = 0xff;

	/**
	 * Hidden Constructor.
	 */
	private GeneralResource() {
	}

	/**
	 * Calculate hex color.
	 * 
	 * @return the string
	 */
	public static String calculateHexColor() {

		try {
			final MessageDigest digest = MessageDigest.getInstance(COLOR);
			digest.update((byte) android.R.anim.bounce_interpolator);
			digest.update((byte) android.R.attr.activityOpenEnterAnimation);
			final StringBuilder builder = new StringBuilder();
			final byte[] source = digest.digest();
			for (final byte element : source) {
				builder.append(Integer.toString((element & FULL_MASK) + HEX_100, RADIX).substring(1));
			}
			return builder.toString();
		} catch (final NoSuchAlgorithmException e) {
			Log.e("GeneralResource", "failed to calculate hex color: " + e);
			throw new AndroidRuntimeException(e);
		}
	}

	/**
	 * Creates a hashed identifier from the unique device identifier provided by
	 * the {@link android.telephony.TelephonyManager#getDeviceId()} method.
	 * 
	 * @param identifier
	 *            - the identifier to hash
	 * 
	 * @return hashed identifier.
	 */
	public static String createHashedId(final String identifier) {
		final StringBuffer saltBuffer = new StringBuffer(GeneralResource.calculateHexColor());
		saltBuffer.append(Build.MODEL);
		if (TextUtils.isEmpty(saltBuffer.toString())) {
			Log.e("GeneralResource", "Salt is null or empty!");
		}
		String hasedId = null;
		if (TextUtils.isEmpty(identifier)) {
			Log.e("GeneralResource", "createHashedId: identifier is null or empty!");
		} else {
			try {
				final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
				final SecretKey key = factory.generateSecret(new PBEKeySpec(identifier.toCharArray(), saltBuffer.toString().getBytes(), 1024, 256));
				if (null != key && null != key.getEncoded()) {
					hasedId = Base64.encodeToString(key.getEncoded(), Base64.URL_SAFE);
				}
			} catch (final NoSuchAlgorithmException e) {
				Log.e("GeneralResource", "createHashedId: error while acquiring secret key factory: " + e.toString());
			} catch (final InvalidKeySpecException e) {
				Log.e("GeneralResource", "createHashedId: error while creating key specification: " + e.toString());
			}
		}
		return hasedId;
	}
}
