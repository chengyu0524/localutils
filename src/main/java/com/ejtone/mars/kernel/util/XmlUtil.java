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

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

/**
 * XML工具类.
 * 
 * @author cuihq
 *
 */
public class XmlUtil {

	public static final String REG_INVALID_CHAR_FOR_XML1_0 = "[^\u0009 \r \n \uE000-\uFFFD \u10000-\u10FFFF]";
	public static final String REG_INVALID_CHAR_FOR_XML1_1 = "[^\u0001-\uD7FF \uE000-\uFFFD \u10000-\u10FFFF]";


	public static String wrapXmlText( String xml ) {
		return "<![CDATA[" + xml + "]]>";
	}

	public static String cleanInvalidXmlChars( String xml ) {
		return xml.replaceAll( REG_INVALID_CHAR_FOR_XML1_0, "" );
	}

	public static int intValue( Element parent, String qname ) {
		return intValue( parent, qname, 0 );
	}

	public static int intValue( Element parent, String qname, int defaultValue ) {
		String text = parent.elementText( qname );
		if( StringUtils.isBlank( text ) ) {
			return defaultValue;
		}
		return Integer.parseInt( text );
	}

	public static long longValue( Element parent, String qname ) {
		return longValue( parent, qname, 0L );
	}

	public static long longValue( Element parent, String qname, long defaultValue ) {
		String text = parent.elementText( qname );
		if( StringUtils.isBlank( text ) ) {
			return defaultValue;
		}
		return Long.parseLong( text );
	}

	public static boolean booleanValue( Element parent, String qname ) {
		return booleanValue( parent, qname, false );
	}

	public static boolean booleanValue( Element parent, String qname, boolean defaultValue ) {
		String text = parent.elementText( qname );
		if( StringUtils.isBlank( text ) ) {
			return defaultValue;
		}
		return Boolean.parseBoolean( text );
	}

	public static void main( String[] args ) {
		System.out.println( "aaa\n999\uffff".replaceAll( REG_INVALID_CHAR_FOR_XML1_0, "8" ) );
	}
}
