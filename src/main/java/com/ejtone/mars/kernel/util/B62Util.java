package com.ejtone.mars.kernel.util;

import java.util.Stack;

public class B62Util {

	private static String digths = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/**
	 * 将long转为62进制
	 * 
	 * @param id
	 * @return
	 */
	public static String to62( long id ) {
		StringBuffer str = new StringBuffer( "" );
		Stack<Character> s = new Stack<Character>( );
		long num = id;
		while( num != 0 ) {
			s.push( digths.charAt( (int) ( num % 62 ) ) );
			num /= 62;
		}
		while( !s.isEmpty( ) ) {
			str.append( s.pop( ) );
		}
		
		return str.toString( );
	}

	/**
	 * 将64位字符转为10进制
	 * 
	 * @param c
	 * @return
	 */
	public static String to10( String c ) {
		if( c == null || c.isEmpty( ) ) {
			return "-1";
		}
		if( !c.matches( "[0-9a-zA-Z]+" ) ) {
			return "-1";
		}
		char[] charArr2 = c.toCharArray( );
		long l = 0;
		for( int i = 0; i < charArr2.length; i++ ) {
			l += digths.indexOf( charArr2[i] ) * (long) ( Math.pow( 62, ( charArr2.length - i - 1 ) ) );
		}
		return String.valueOf( l );
	}

	/*
	 * @param args
	 */
	public static void main( String[] args ) {
		System.out.println( "62System=" + to62( 123123595999999L ) );
		System.out.println( "10System=" + to10( "ZZZZZZZZ" ) );
	}
}
