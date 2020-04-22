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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期&时间工具类.
 * 
 * @author cuihq
 *
 */
public class DateTimeUtil {

	public static final String dateFormat = "yyyyMMdd";
	public static final String timeFormat = "HHmmss";
	public static final String dateTimeFormat = "yyyyMMddHHmmss";
	public static final String dateTimeMsFormat = "yyyyMMddHHmmssSSS";


	public static String getDateTime( ) {
		return new SimpleDateFormat( dateTimeFormat ).format( Calendar.getInstance( ).getTime( ) );
	}

	public static String getDate( ) {
		return new SimpleDateFormat( dateFormat ).format( Calendar.getInstance( ).getTime( ) );
	}

	public static String getTime( ) {
		return new SimpleDateFormat( timeFormat ).format( Calendar.getInstance( ).getTime( ) );
	}

	public static String getDate( int nday ) {
		Date d = Calendar.getInstance( ).getTime( );
		return new SimpleDateFormat( dateFormat ).format( getDate( d, nday ) );
	}

	public static String getDate( String date, int nday ) {
		try {
			SimpleDateFormat f = new SimpleDateFormat( dateFormat );
			Date d = f.parse( date );
			return f.format( getDate( d, nday ) );
		} catch( ParseException e ) {
			return null;
		}
	}

	public static Date getDate( Date d, int nday ) {
		Calendar c = Calendar.getInstance( );
		c.setTime( d );
		c.add( Calendar.DAY_OF_YEAR, nday );
		return c.getTime( );
	}

	public static String getDate( String format ) {
		SimpleDateFormat sf = new SimpleDateFormat( format );
		return sf.format( Calendar.getInstance( ).getTime( ) );
	}

	public static String getDate( Date date, String format ) {
		return new SimpleDateFormat( format ).format( date );
	}

	public static Date parseDate( String date, String format ) {
		SimpleDateFormat sf = new SimpleDateFormat( format );
		try {
			return sf.parse( date );
		} catch( ParseException e ) {
			return null;
		}
	}

	public static DateAndTime getDateAndTime( ) {
		return new DateAndTime( );
	}

	public static DateAndTime getDateAndTime( Date date ) {
		return new DateAndTime( date );
	}

	public static class DateAndTime {

		private String date;
		private String time;

		public DateAndTime( ) {
			this( Calendar.getInstance( ).getTime( ) );
		}

		public DateAndTime( Date d ) {
			String s = new SimpleDateFormat( dateTimeFormat ).format( d );
			date = s.substring( 0, 8 );
			time = s.substring( 8, 14 );
		}

		public String getDate( ) {
			return date;
		}

		public String getTime( ) {
			return time;
		}
	}

	public static void main( String[] args ) throws ParseException {
		SimpleDateFormat f = new SimpleDateFormat( dateTimeFormat );
		Date d = f.parse( "20131107021217" );
		Calendar c = Calendar.getInstance( );
		c.setTime( d );
		c.add( Calendar.SECOND, 303165 );
		System.out.println( DateTimeUtil.getDate( c.getTime( ), dateTimeFormat ) );

	}
}
