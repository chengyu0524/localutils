package com.ejtone.mars.kernel.util;

/**
 * 手机号段匹配.
 * 
 * @author cuihq
 *
 */
public class SectionMatcher {
	/**
	 * Logger for this class
	 */

	public static final int SECTION_START_POINT = 1; // 号段开始位置 //从第1位开始，忽略最前面的1
	private SectionMatcher[] childMatcher = new SectionMatcher[10]; // 号码最大0-9
	private boolean complete;

	/**
	 * 添加一个号段
	 * 
	 * @param section
	 */
	public void add( String section ) {
		if( section == null || SECTION_START_POINT >= section.length( ) ) {
			return;
		}
		addInternal( section.toCharArray( ), SECTION_START_POINT );
	}

	private synchronized void addInternal( char[] charArray, int startPoint ) {
		int c = charArray[startPoint] - '0';
		if( c < 0 || c > 9 ) {
			return;
		}
		SectionMatcher m = childMatcher[c];
		if( m == null ) {
			m = new SectionMatcher( );
			childMatcher[c] = m;
		}
		if( startPoint == charArray.length - 1 ) {
			m.complete = true;
			return;
		}
		m.addInternal( charArray, startPoint + 1 );
	}

	/**
	 * 删除一个号段
	 */

	public void del( String section ) {
		if( section == null || SECTION_START_POINT >= section.length( ) ) {
			return;
		}
		delInternal( section.toCharArray( ), SECTION_START_POINT );
	}

	private synchronized void delInternal( char[] charArray, int startPoint ) {
		int c = charArray[startPoint] - '0';
		if( c < 0 || c > 9 ) {
			return;
		}
		SectionMatcher m = childMatcher[c];
		if( m == null ) {
			return;
		}
		if( startPoint == charArray.length - 1 ) {
			m.complete = false;
			return;
		}
		m.delInternal( charArray, startPoint + 1 );
	}

	public boolean match( String mobile ) {

		if( mobile == null || SECTION_START_POINT >= mobile.length( ) ) {
			return false;
		}
		return match( mobile.toCharArray( ), SECTION_START_POINT );
	}

	private boolean match( char[] charArray, int startPoint ) {

		int c = charArray[startPoint] - '0';
		if( c < 0 || c > 9 ) {
			return false;
		}
		SectionMatcher matcher = childMatcher[c];
		if( matcher == null ) {
			return false;
		}
		if( matcher.complete ) {
			return true;
		}
		if( startPoint == charArray.length - 1 ) {
			return false;
		}
		return matcher.match( charArray, startPoint + 1 );
	}

	public static void main( String[] args ) {

		SectionMatcher m = new SectionMatcher( );

		System.out.println( m.match( "13910570075" ) );

		m.add( "139105700751" );
		System.out.println( m.match( "13910570075" ) );

		m.add( "13910" );
		System.out.println( m.match( "13910570075" ) );

		m.add( "1391" );
		System.out.println( m.match( "13910570075" ) );

		m.del( "13910" );
		System.out.println( m.match( "13910570075" ) );

		m.del( "1391" );
		System.out.println( m.match( "13910570075" ) );

		m.add( "1" );
		System.out.println( m.match( "13910570075" ) );

	}
}
