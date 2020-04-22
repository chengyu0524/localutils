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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KeyWordMatcher 关键字匹配.
 * 
 * @author cuihq
 */
public class KeyWordMatcher {
	private static final Logger logger = LoggerFactory.getLogger( KeyWordMatcher.class );

	private final Map<Character, KeyWordMatcher> wordMap;
	private boolean complete; // Character是否已经是完整的单词 //如赤裸与赤裸裸
	private static int defaultSize = 1024;

	public KeyWordMatcher( ) {
		this( defaultSize );
	}

	public KeyWordMatcher( int size ) {
		wordMap = new HashMap<Character, KeyWordMatcher>( size );
	}

	public void add( String word ) {
		if( StringUtils.isBlank( word ) ) // 关键字为空,结束
			return;
		addInternal( word.toCharArray( ), 0 );
		return;
	}

	private synchronized void addInternal( char[] wordArray, int startIndex ) { // 加个参数吧,省得生成一堆临时对象

		char c = wordArray[startIndex];
		KeyWordMatcher s = wordMap.get( c ); // 以第一个字符做key,查找map
		if( s == null ) { // 如果没有,则新建一个,放入map中
			s = new KeyWordMatcher( );
			wordMap.put( c, s );
		}
		if( wordArray.length == ++startIndex ) {
			s.complete = true;
			return;
		}
		s.addInternal( wordArray, startIndex );
	}

	public void del( String word ) {
		if( StringUtils.isBlank( word ) ) // 关键字为空,结束
			return;
		delInternal( word.toCharArray( ), 0 );
	}

	private synchronized void delInternal( char[] wordArray, int startIndex ) {

		char c = wordArray[startIndex];

		KeyWordMatcher s = wordMap.get( c ); // 以第一个字符做key,查找map
		if( s == null ) {
			return;
		}
		if( wordArray.length == startIndex + 1 ) { // wordArray查找完毕
			if( s.complete ) { // 这是个完整的单词,将之置为false
				s.complete = false;
				if( s.wordMap.size( ) == 0 ) { // 没有子节点,将此关键字删除
					wordMap.remove( c );
				}
				return;
			} else {
				// 非完整关键字,未匹配上,直接返回
				return;
			}
		}
		s.delInternal( wordArray, startIndex + 1 ); // 删除子串
	}

	public boolean filter( String text ) {
		if( StringUtils.isBlank( text ) ) // 关键字为空,结束
			return false;
		if( wordMap.size( ) == 0 ) {
			return false;
		}

		char[] textArray = text.toCharArray( );
		for( int i = 0; i < textArray.length; i++ ) {
			if( filterInternal( textArray, i ) ) {
				return true;
			}
		}
		return false;

	}

	private boolean filterInternal( char[] textArray, int startIndex ) {

		char c = textArray[startIndex];

		if( logger.isDebugEnabled( ) ) {
			logger.debug( "filter {} in {}", ArrayUtils.subarray( textArray, startIndex, textArray.length ), this );
		}

		KeyWordMatcher s = wordMap.get( c ); // 以第一个字符做key,查找map
		if( s == null ) { // 这个单词没找着,找下一个
			if( logger.isDebugEnabled( ) ) {
				logger.debug( "not found char {} in keyword matcher {}", c, this );
			}
			return false;
		}
		if( logger.isDebugEnabled( ) ) {
			logger.debug( "found char {} in keyword matcher {}", c, this );
		}

		if( s.complete ) { // 已经是完整的单词了,包含
			if( logger.isDebugEnabled( ) ) {
				logger.debug( "matched", c );
			}
			return true;
		}
		if( textArray.length == ++startIndex ) { // 已经是最后一个了
			if( logger.isDebugEnabled( ) ) {
				logger.debug( "not matched", c );
			}
			return false;
		}
		return s.filterInternal( textArray, startIndex );
	}

	public String toString( ) {
		List<String> l = toStringArray( );
		if( l == null ) {
			return "Empty";
		}
		StringBuilder s = new StringBuilder( 1024 );
		s.append( "[ " );
		for( int i = 0, size = l.size( ); i < size; i++ ) {
			s.append( l.get( i ) ).append( ", " );
		}
		s.setLength( s.length( ) - 2 ); // 去掉最后的逗号和空格
		s.append( " ]" );
		return s.toString( );
	}

	private List<String> toStringArray( ) {
		List<String> l = new ArrayList<String>( );
		Set<Character> set = wordMap.keySet( );
		if( set == null ) {
			return l;
		}

		Iterator<Character> iter = set.iterator( );

		while( iter.hasNext( ) ) {
			Character c = iter.next( );

			KeyWordMatcher service = wordMap.get( c );
			if( service.complete )
				l.add( c + "" );
			List<String> slist = service.toStringArray( );
			for( int i = 0, size = slist.size( ); i < size; i++ ) {
				l.add( c + slist.get( i ) );
			}
		}
		return l;
	}

	public int size( ) {
		return wordMap.size( );
	}

	public static void main( String[] args ) {
		KeyWordMatcher m = new KeyWordMatcher( );

		System.out.println( m.filter( "12345678901234567890" ) );
		m.add( "11" );
		System.out.println( m.filter( "12345678901234567890" ) );
		m.add( "25" );
		System.out.println( m.filter( "12345678901234567890" ) );

		m.add( "赤裸" );
		System.out.println( m.filter( "赤裸的" ) );
		m.del( "赤裸" );
		System.out.println( m.filter( "赤裸的" ) );
		m.add( "naked" );
		System.out.println( m.filter( "I'm naked" ) );
		m.add( "na" );
		System.out.println( m.filter( "I'm naked" ) );
		m.del( "na" );
		m.del( "naked" );
		m.del( "赤裸裸" );
		System.out.println( m.filter( "I'm naked" ) );

	}
}
