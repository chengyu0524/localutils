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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ejtone.mars.kernel.util.lifecycle.LifeCycle;


/**
 * 工具类大杂烩，不好分类的都写这里吧.
 * 
 * @author cuihq
 *
 */
public class MixUtil {

	private static final Logger logger = LoggerFactory.getLogger( MixUtil.class );
	public static final String __LINE_SEPARATOR = System.getProperty( "line.separator", "\n" );
	public static final Logger datLogger = LoggerFactory.getLogger( "$$dat.logger" );
	public static final Logger monLogger = LoggerFactory.getLogger( "$$mon.logger" );
	public static final Logger rmqLogger = LoggerFactory.getLogger( "RocketmqCommon" );
	public static final String INET_ADDR_ANY = "0.0.0.0";

	public static String[] chars = new String[]{ "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
			"u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
			"N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "-", "_" };

	public static int roundUpToPowerOf2( int number ) {
		return ( number > 1 ) ? Integer.highestOneBit( ( number - 1 ) << 1 ) : 1;
	}

	/**
	 * 合并URL
	 * 
	 * @param baseUrl
	 * @param addUrl
	 * @return
	 */
	public static String mergeUrl( String baseUrl, String addUrl ) {

		baseUrl = StringUtils.stripEnd( baseUrl == null ? "" : baseUrl, "/" );
		addUrl = StringUtils.stripStart( addUrl == null ? "" : addUrl, "/" );

		return baseUrl + '/' + addUrl;
	}

	/**
	 * 根据文件名获取文件后缀
	 * 
	 * @param filename
	 * @return
	 */
	public static String fileSuffix( String filename ) {
		if( StringUtils.isBlank( filename ) ) {
			return "";
		}
		String[] s = filename.split( "\\." );

		return s.length <= 1 ? "" : s[s.length - 1];
	}

	/**
	 * 获取上一级目录路径
	 * 
	 * @param url
	 * @return
	 */
	public static String parentPath( String url ) {

		if( StringUtils.isBlank( url ) || url.equals( "/" ) ) {
			return "/";
		}

		url = StringUtils.stripEnd( url, "/" );

		int i = url.lastIndexOf( "/" );
		if( i != -1 ) {
			return url.substring( 0, i ) + "/";
		} else {
			return "/";
		}
	}

	public static byte[] readFully( InputStream inputStream ) throws IOException {
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream( );
			int read = 0;
			byte[] bytes = new byte[1024];

			while( ( read = inputStream.read( bytes ) ) != -1 ) {
				out.write( bytes, 0, read );
			}
			out.flush( );
			return out.toByteArray( );
		} finally {
			MixUtil.safeClose( out );
		}
	}
	
	public static String replaceUnderlineAndfirstToUpper( String str ) {

		if( str == null || str.length( ) == 0 ) {
			return "";
		}
		String[] arr = str.split( "_" );
		StringBuilder sb = new StringBuilder( str.length( ) );
		for( int i = 0; i < arr.length; i++ ) {
			if( arr[i] == null || arr[i].length( ) == 0 ) {
				continue;
			}
			sb.append( Character.toTitleCase( arr[i].charAt( 0 ) ) ).append( arr[i].substring( 1 ) );
		}
		return sb.toString( );
	}

	public static void sleep( long millis ) {
		try {
			Thread.sleep( millis );
		} catch( InterruptedException e ) {
		}
	}

	public static void warnTimeout( long currVal ) {
		warnTimeout( currVal, 5000 );
	}

	public static void warnTimeout( long currVal, long warnVal ) {
		if( currVal < warnVal ) {
			logger.warn( "timeout value {} < {}ms, make sure it's your want", currVal, warnVal );
		}
	}

	public static void printStackTrace( ) {
		printStackTrace( logger );
	}

	public static void printStackTrace( Logger logger ) {
		StackTraceElement[] elements = Thread.currentThread( ).getStackTrace( );
		for( StackTraceElement element : elements ) {
			logger.info( "{}", element.toString( ) );
		}
	}

	public static String getStackTrace( ) {
		StringBuilder sb = new StringBuilder( 1024 );
		StackTraceElement[] elements = Thread.currentThread( ).getStackTrace( );
		for( StackTraceElement element : elements ) {
			sb.append( element.toString( ) ).append( "\n" );
		}
		return sb.toString( );
	}

	public static void safeClose( Closeable o ) {
		if( o == null ) {
			return;
		}
		try {
			o.close( );
		} catch( Throwable t ) {
			logger.error( "", t );
		}
	}

	public static void safeStop( LifeCycle lifeCycle ) {
		if( lifeCycle == null ) {
			return;
		}
		try {
			lifeCycle.stop( );
		} catch( Throwable t ) {
			logger.error( "", t );
		}
	}

	public static final boolean mkdirs( String dir ) {
		File f = new File( dir );
		return f.mkdirs( );
	}

	public static String listToString( List<?> list ) {
		return JsonUtil.toJsonString( list );
	}

	public static String arrayToString( Object[] array ) {
		if( array == null || array.length == 0 ) {
			return "[]";
		}

		StringBuilder sb = new StringBuilder( );
		sb.append( "[" );
		for( int i = 0; i < array.length; i++ ) {
			if( i != 0 ) {
				sb.append( "," );
			}
			sb.append( array[i] );
		}
		sb.append( "]" );
		return sb.toString( );
	}

	public static boolean objectEquals( Object obj1, Object obj2 ) {
		return obj1 == null ? obj2 == null : obj1.equals( obj2 );
	}

	/**
	 * 比较两个list中的元素.
	 * 
	 * @param list1
	 * @param list2
	 * @return 返回结果是一个有2个元素的list，第一个标识list2相对list1增加的元素，第二个标识删除的元素
	 */
	public static <T> List<List<T>> comp( List<T> list1, List<T> list2 ) {
		List<List<T>> result = new ArrayList<>( 2 );
		if( list1 == null || list1.size( ) == 0 ) { // list1为空，list2则全部为新增
			result.add( new ArrayList<>( list2 ) );
			result.add( new ArrayList<T>( ) );
			return result;
		}

		if( list2 == null || list2.size( ) == 0 ) { // list2为空，则list1的内容全部被删除
			result.add( new ArrayList<T>( ) );
			result.add( new ArrayList<>( list1 ) );
			return result;
		}

		List<T> listAdd = new ArrayList<>( );
		List<T> listRemoved = new ArrayList<>( list1 );
		for( T t : list2 ) {
			if( !listRemoved.remove( t ) ) { // 返回true，说明list2中包含t,返回false表明t是新增加的元素
				listAdd.add( t );
			}
		}
		// 剩下的元素是list2中不包含的元素
		result.add( listAdd );
		result.add( listRemoved );
		return result;
	}

	/**
	 * 比较两个Bean中所有元素是否全等，是返回True，有一个不等则返回False<br/>
	 * 参数的位置和结果无关
	 * 
	 * @param obj1 第一个Bean
	 * @param obj2 第二个Bean
	 * @return 是否全等的boolean值
	 */
	public static boolean beanEquals( Object obj1, Object obj2 ) {

		if( ( null == obj1 && null != obj2 ) || ( null != obj1 && null == obj2 ) ) {
			return false;
		} else if( null == obj1 && null == obj2 ) {
			return true;
		} else if( obj1.equals( obj2 ) ) {
			return true;
		}

		Field[] fieldArr1 = obj1.getClass( ).getDeclaredFields( );
		Field[] fieldArr2 = obj2.getClass( ).getDeclaredFields( );
		if( fieldArr1.length != fieldArr2.length ) {
			return false;
		} else {
			Field field1, field2;
			String typeName1, typeName2;
			Object tmpObj1, tmpObj2;
			for( int i = 0; i < fieldArr1.length; i++ ) {
				field1 = fieldArr1[i];
				field2 = fieldArr2[i];
				field1.setAccessible( true );
				field2.setAccessible( true );
				typeName1 = field1.getType( ).getName( );
				typeName2 = field2.getType( ).getName( );
				if( !StringUtils.equals( typeName1, typeName2 ) ) {
					return false;
				}
				try {
					tmpObj1 = field1.get( obj1 );
					tmpObj2 = field2.get( obj2 );
				} catch( Exception e ) {
					return false;
				}
				if( ( null == tmpObj1 && null != tmpObj2 ) || ( null != tmpObj1 && null == tmpObj2 ) ) {
					return false;
				} else if( null == tmpObj1 && null == tmpObj2 ) {
					continue;
				}
				if( String.class.getName( ).equals( typeName1 ) ) {
					if( !( (String) tmpObj1 ).equals( (String) tmpObj2 ) ) {
						return false;
					}
				} else if( boolean.class.getName( ).equals( typeName1 ) ) {
					if( !( (Boolean) tmpObj1 ).equals( (Boolean) tmpObj2 ) ) {
						return false;
					}
				} else if( double.class.getName( ).equals( typeName1 ) ) {
					if( !( (Double) tmpObj1 ).equals( (Double) tmpObj2 ) ) {
						return false;
					}
				} else if( float.class.getName( ).equals( typeName1 ) ) {
					if( !( (Float) tmpObj1 ).equals( (Float) tmpObj2 ) ) {
						return false;
					}
				} else if( int.class.getName( ).equals( typeName1 ) ) {
					if( !( (Integer) tmpObj1 ).equals( (Integer) tmpObj2 ) ) {
						return false;
					}
				} else if( long.class.getName( ).equals( typeName1 ) ) {
					if( !( (Long) tmpObj1 ).equals( (Long) tmpObj2 ) ) {
						return false;
					}
				} else if( char.class.getName( ).equals( typeName1 ) ) {
					if( !( (Character) tmpObj1 ).equals( (Character) tmpObj2 ) ) {
						return false;
					}
				} else {
					if( !( tmpObj1.toString( ).equals( tmpObj2.toString( ) ) ) ) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public static String getLocalAddress( ) {
		// try {
		// InetAddress addr = InetAddress.getLocalHost( );
		// return addr.getHostAddress( ).toString( );// 获得本机IP
		// } catch( UnknownHostException e ) {
		// return "127.0.0.1";
		// }

		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			// 获得所有网络接口
			netInterfaces = NetworkInterface.getNetworkInterfaces( );
			while( netInterfaces.hasMoreElements( ) ) {
				NetworkInterface ni = netInterfaces.nextElement( );
				if( ni.getName( ).equals( "en0" ) || ni.getName( ).equals( "eth0" ) ) {
					Enumeration<InetAddress> ips = ni.getInetAddresses( );
					while( ips.hasMoreElements( ) ) {
						InetAddress i = ips.nextElement( );
						if( i instanceof Inet6Address ) {
							continue;
						}
						return i.getHostAddress( );
					}
				}
			}
		} catch( SocketException e ) {
		}
		return "00:00:00:00:00:00";
	}

	public static String getMac( ) {
		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			// 获得所有网络接口
			netInterfaces = NetworkInterface.getNetworkInterfaces( );
			while( netInterfaces.hasMoreElements( ) ) {
				NetworkInterface ni = netInterfaces.nextElement( );
				if( ni.getName( ).equals( "en0" ) || ni.getName( ).equals( "eth0" ) ) {
					byte[] macs = ni.getHardwareAddress( );
					// 该interface不存在HardwareAddress，继续下一次循环
					if( macs == null ) {
						continue;
					}
					return String.format( "%02x:%02x:%02x:%02x:%02x:%02x", macs[0] & 0xff, macs[1] & 0xff, macs[2] & 0xff, macs[3] & 0xff, macs[4] & 0xff,
							macs[5] & 0xff );
				}
			}
		} catch( SocketException e ) {
		}
		return "00:00:00:00:00:00";
	}

	public static void getAllMacAdress( ) {
		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			// 获得所有网络接口
			netInterfaces = NetworkInterface.getNetworkInterfaces( );
			while( netInterfaces.hasMoreElements( ) ) {
				NetworkInterface ni = netInterfaces.nextElement( );
				System.out.println( "DisplayName: " + ni.getDisplayName( ) );
				System.out.println( "Name: " + ni.getName( ) );
				Enumeration<InetAddress> ips = ni.getInetAddresses( );
				while( ips.hasMoreElements( ) ) {
					InetAddress i = ips.nextElement( );
					if( i instanceof Inet6Address ) {
						System.out.println( "IPv6: " + i.getHostAddress( ) );
					} else {
						System.out.println( "IPv4: " + i.getHostAddress( ) );
					}
				}
				byte[] macs = ni.getHardwareAddress( );
				// 该interface不存在HardwareAddress，继续下一次循环
				if( macs == null ) {
					continue;
				}
				String mac = String.format( "%02x:%02x:%02x:%02x:%02x:%02x", macs[0] & 0xff, macs[1] & 0xff, macs[2] & 0xff, macs[3] & 0xff, macs[4] & 0xff,
						macs[5] & 0xff );
				System.out.println( "MAC:" + mac );
			}
		} catch( SocketException e ) {
			e.printStackTrace( );
		}
	}

	public static String getObjectIdentity( Object o ) {
		return o.getClass( ).getSimpleName( ) + "@" + Integer.toHexString( System.identityHashCode( o ) );
	}

	public static void checkTimeout( long currVal ) {
		checkTimeout( currVal, 5000 );
	}

	public static void checkTimeout( long currVal, long warnVal ) {
		if( currVal < warnVal ) {
			logger.warn( "timeout value {} < {}ms,make sure it's your want", currVal, warnVal );
		}
	}

	public static String shortUUID( ) {
		StringBuffer shortBuffer = new StringBuffer( );
		String uuid = uuid( );
		// 每3个十六进制字符转换成为2个字符
		for( int i = 0; i < 10; i++ ) {
			String str = uuid.substring( i * 3, i * 3 + 3 );
			// 转成十六进制
			int x = Integer.parseInt( str, 16 );
			// 除64得到前面6个二进制数的
			shortBuffer.append( chars[x / 0x40] );
			// 对64求余得到后面6个二进制数1
			shortBuffer.append( chars[x % 0x40] );
		}
		// 加上后面两个没有改动的
		shortBuffer.append( uuid.charAt( 30 ) );
		shortBuffer.append( uuid.charAt( 31 ) );
		return shortBuffer.toString( );
	}

	public static String uuid( ) {
		UUID uuid = UUID.randomUUID( );
		long mostSigBits = uuid.getMostSignificantBits( );
		long leastSigBits = uuid.getLeastSignificantBits( );

		return ( digits( mostSigBits >> 32, 8 ) + digits( mostSigBits >> 16, 4 ) + digits( mostSigBits, 4 ) + digits( leastSigBits >> 48, 4 )
				+ digits( leastSigBits, 12 ) );
	}

	private static String digits( long val, int digits ) {
		long hi = 1L << ( digits * 4 );
		return Long.toHexString( hi | ( val & ( hi - 1 ) ) ).substring( 1 );
	}

	public static String nullToEmpty( String s ) {
		return s == null ? "" : s;
	}

	/**
	 * 将点分十进制形式的IP地址转换为十进制整数
	 * 
	 * @param strIp
	 * @return
	 */
	public static long ipToLong( String strIp ) {
		long[] ip = new long[4];
		// 先找到IP地址字符串中.的位置
		int position1 = strIp.indexOf( "." );
		int position2 = strIp.indexOf( ".", position1 + 1 );
		int position3 = strIp.indexOf( ".", position2 + 1 );
		// 将每个.之间的字符串转换成整型
		ip[0] = Long.parseLong( strIp.substring( 0, position1 ) );
		ip[1] = Long.parseLong( strIp.substring( position1 + 1, position2 ) );
		ip[2] = Long.parseLong( strIp.substring( position2 + 1, position3 ) );
		ip[3] = Long.parseLong( strIp.substring( position3 + 1 ) );
		return ( ip[0] << 24 ) + ( ip[1] << 16 ) + ( ip[2] << 8 ) + ip[3];
	}

	/**
	 * 将十进制整数转换为点分十进制形式的IP地址
	 * 
	 * @param longIp
	 * @return
	 */
	public static String longToIP( long longIp ) {
		StringBuffer sb = new StringBuffer( "" );
		// 直接右移24位
		sb.append( String.valueOf( ( longIp >>> 24 ) ) );
		sb.append( "." );
		// 将高8位置0，然后右移16位
		sb.append( String.valueOf( ( longIp & 0x00FFFFFF ) >>> 16 ) );
		sb.append( "." );
		// 将高16位置0，然后右移8位
		sb.append( String.valueOf( ( longIp & 0x0000FFFF ) >>> 8 ) );
		sb.append( "." );
		// 将高24位置0
		sb.append( String.valueOf( ( longIp & 0x000000FF ) ) );
		return sb.toString( );
	}

	public static void main( String[] args ) {
		// System.out.println(replaceUnderlineAndfirstToUpper(null));
		// System.out.println(replaceUnderlineAndfirstToUpper(""));
		// System.out.println(replaceUnderlineAndfirstToUpper("____"));
		// System.out.println(replaceUnderlineAndfirstToUpper("_a_abc__Dc"));
		// System.out.println(replaceUnderlineAndfirstToUpper("_a_b__Dc"));
		// System.out.println(replaceUnderlineAndfirstToUpper("_a_Db__c"));
		System.out.println( getLocalAddress( ) );
		System.out.println( getMac( ) );
		getAllMacAdress( );
	}
}
