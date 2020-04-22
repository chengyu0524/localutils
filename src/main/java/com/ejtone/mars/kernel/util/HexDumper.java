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

/**
 * bin类型数据导出为十六进制工具类.
 * 
 * @author cuihq
 *
 */
public class HexDumper {

	/**
	 * The high digits lookup table.
	 */
	private static final byte[] highDigits;

	/**
	 * The low digits lookup table.
	 */
	private static final byte[] lowDigits;

	/**
	 * Initialize lookup tables.
	 */
	static {
		final byte[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

		int i;
		byte[] high = new byte[256];
		byte[] low = new byte[256];

		for( i = 0; i < 256; i++ ) {
			high[i] = digits[i >>> 4];
			low[i] = digits[i & 0x0F];
		}

		highDigits = high;
		lowDigits = low;
	}

	public static String getHexdump( byte[] buffer ) {
		if( buffer == null )
			return "[]";

		int size = buffer.length;

		StringBuilder out = new StringBuilder( size * 3 + 3 );

		out.append( "[" );
		int byteValue;
		for( int i = 0; i < size; i++ ) {
			if( i != 0 ) {
				out.append( " " );
			}
			byteValue = buffer[i] & 0xFF;
			out.append( (char) highDigits[byteValue] );
			out.append( (char) lowDigits[byteValue] );
		}
		out.append( "]" );
		return out.toString( );
	}

	public static void main( String[] args ) {
		String s = HexDumper.getHexdump( new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 99, 88, 127, -128, -1 } );

		System.out.println( s );

	}
}
