package com.ejtone.mars.kernel.util.internal;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Cracklog4e {
	static String key = "b1K49dF4";

	public static void main( String[] args ) {
		byte[] is = "cui hq cuihq@mail.com".getBytes( );
		try {
			Cipher cipher = Cipher.getInstance( "DES" );
			SecretKeySpec secretkeyspec = new SecretKeySpec( key.getBytes( ), "DES" );
			cipher.init( 1, secretkeyspec );
			byte[] is_2_ = cipher.doFinal( is );
			is = is_2_;
			String string_6_ = "";
			for( int i = 0; i < is.length; i++ ) {
				int i_7_ = is[i] & 0xff;
				String string_8_ = Integer.toHexString( i_7_ );
				if( string_8_.length( ) == 1 )
					string_8_ = "0" + string_8_;
				string_6_ += (String) string_8_;
			}
			String string = string_6_;
			String string_33_ = "";
			int i = 0;
			int i_34_ = 4;
			int i_35_ = i + i_34_;
			int i_36_ = 5;
			for( int i_37_ = 0; i_37_ < i_36_; i_37_++ ) {
				string_33_ += string.substring( i, i_35_ );
				i = i_35_;
				i_35_ = i + i_34_;
				if( i_35_ >= string.length( ) )
					break;
				if( i_37_ + 1 < i_36_ )
					string_33_ += "-";
			}

			System.out.println( string_33_ );
		} catch( Exception exception ) {
		}
	}
}
