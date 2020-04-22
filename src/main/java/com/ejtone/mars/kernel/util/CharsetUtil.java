/**
 * Copyright [2000-2100] [cuihq]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ejtone.mars.kernel.util;

import java.io.UnsupportedEncodingException;

/**
 * 常用字符集.
 * 
 * @author cuihq
 *
 */
public class CharsetUtil {
	public static final String ISO_8859_1 = "ISO-8859-1";
	public static final String US_ASCII = "US-ASCII";
	public static final String UTF_16 = "UTF-16";
	public static final String UTF_16BE = "UTF-16BE";
	public static final String UTF_16LE = "UTF-16LE";
	public static final String UTF_8 = "UTF-8";
	public static final String GBK = "GBK";

	public static byte[] getBytesIso8859_1( String string ) {
		return CharsetUtil.getBytesUnchecked( string, ISO_8859_1 );
	}

	public static byte[] getBytesUsAscii( String string ) {
		return CharsetUtil.getBytesUnchecked( string, US_ASCII );
	}

	public static byte[] getBytesUtf16( String string ) {
		return CharsetUtil.getBytesUnchecked( string, UTF_16 );
	}

	public static byte[] getBytesUtf16Be( String string ) {
		return CharsetUtil.getBytesUnchecked( string, UTF_16BE );
	}

	public static byte[] getBytesUtf16Le( String string ) {
		return CharsetUtil.getBytesUnchecked( string, UTF_16LE );
	}

	public static byte[] getBytesUtf8( String string ) {
		return CharsetUtil.getBytesUnchecked( string, UTF_8 );
	}

	public static byte[] getBytesGbk( String string ) {
		return CharsetUtil.getBytesUnchecked( string, GBK );
	}

	public static byte[] getBytesUnchecked( String string, String charsetName ) {
		if( string == null ) {
			return null;
		}
		try {
			return string.getBytes( charsetName );
		} catch( UnsupportedEncodingException e ) {
			throw newIllegalStateException( charsetName, e );
		}
	}

	private static IllegalStateException newIllegalStateException( String charsetName, UnsupportedEncodingException e ) {
		return new IllegalStateException( charsetName + ": " + e );
	}

	public static String newString( byte[] bytes, String charsetName ) {
		if( bytes == null ) {
			return null;
		}
		try {
			return new String( bytes, charsetName );
		} catch( UnsupportedEncodingException e ) {
			throw newIllegalStateException( charsetName, e );
		}
	}

	public static String newStringIso8859_1( byte[] bytes ) {
		return newString( bytes, CharsetUtil.ISO_8859_1 );
	}

	public static String newStringUsAscii( byte[] bytes ) {
		return newString( bytes, US_ASCII );
	}

	public static String newStringUtf16( byte[] bytes ) {
		return newString( bytes, UTF_16 );
	}

	public static String newStringUtf16Be( byte[] bytes ) {
		return newString( bytes, UTF_16BE );
	}

	public static String newStringUtf16Le( byte[] bytes ) {
		return newString( bytes, UTF_16LE );
	}

	public static String newStringUtf8( byte[] bytes ) {
		return newString( bytes, UTF_8 );
	}

	public static String newStringGBK( byte[] bytes ) {
		return newString( bytes, GBK );
	}
}
