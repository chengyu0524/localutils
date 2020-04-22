package com.ejtone.mars.kernel.util;

import java.util.ArrayList;
import java.util.List;


public final class AddrUtil {

	private AddrUtil( ) {
		// Empty
	}


	/**
	 * Split a string containing whitespace or comma separated host or IP
	 * addresses and port numbers of the form "host:port host2:port" or
	 * "host:port, host2:port" into a List of InetSocketAddress instances suitable
	 * for instantiating a MemcachedClient.
	 *
	 * Note that colon-delimited IPv6 is also supported. For example: ::1:11211
	 */
	public static List<Addr> getAddresses( String s ) {
		if( s == null ) {
			throw new NullPointerException( "Null host list" );
		}
		if( s.trim( ).equals( "" ) ) {
			throw new IllegalArgumentException( "No hosts in list:  ``" + s + "''" );
		}
		ArrayList<Addr> addrs = new ArrayList<Addr>( );

		for( String hoststuff : s.split( "(?:\\s|,|;)+" ) ) {
			if( hoststuff.equals( "" ) ) {
				continue;
			}
			int finalColon = hoststuff.lastIndexOf( ':' );
			if( finalColon < 1 ) {
				throw new IllegalArgumentException( "Invalid server ``" + hoststuff + "'' in list:  " + s );
			}
			String hostPart = hoststuff.substring( 0, finalColon );
			String portNum = hoststuff.substring( finalColon + 1 );

			addrs.add( new Addr( hostPart, Integer.parseInt( portNum ) ) );
		}
		return addrs;
	}

	public static List<Addr> getAddresses( List<String> servers ) {
		ArrayList<Addr> addrs = new ArrayList<Addr>( servers.size( ) );
		for( String server : servers ) {
			int finalColon = server.lastIndexOf( ':' );
			if( finalColon < 1 ) {
				throw new IllegalArgumentException( "Invalid server ``" + server + "'' in list:  " + server );
			}
			String hostPart = server.substring( 0, finalColon );
			String portNum = server.substring( finalColon + 1 );

			addrs.add( new Addr( hostPart, Integer.parseInt( portNum ) ) );
		}
		return addrs;
	}

	public static List<String> getStringAddresses( String s ) {
		if( s == null ) {
			throw new NullPointerException( "Null host list" );
		}
		if( s.trim( ).equals( "" ) ) {
			throw new IllegalArgumentException( "No hosts in list:  ``" + s + "''" );
		}
		ArrayList<String> addrs = new ArrayList<>( );

		for( String hoststuff : s.split( "(?:\\s|,|;)+" ) ) {
			if( hoststuff.equals( "" ) ) {
				continue;
			}
			int finalColon = hoststuff.lastIndexOf( ':' );
			if( finalColon < 1 ) {
				throw new IllegalArgumentException( "Invalid server ``" + hoststuff + "'' in list:  " + s );
			}
			String hostPart = hoststuff.substring( 0, finalColon );
			String portNum = hoststuff.substring( finalColon + 1 );

			addrs.add( hostPart + ":" + portNum );
		}
		return addrs;

	}

	public static final class Addr {
		public static final String LOCALHOST_STR = "localhost";
		private String host;
		private int port;

		public Addr( String host, int port ) {
			this.host = host;
			this.port = port;
		}

		public String getHost( ) {
			return host;
		}

		public int getPort( ) {
			return port;
		}

		@Override
		public boolean equals( Object obj ) {
			if( obj instanceof Addr ) {
				Addr hp = (Addr) obj;

				String thisHost = convertHost( host );
				String hpHost = convertHost( hp.host );
				return port == hp.port && thisHost.equals( hpHost );
			}
			return false;
		}

		@Override
		public int hashCode( ) {
			return 31 * convertHost( host ).hashCode( ) + port;
		}

		@Override
		public String toString( ) {
			return host + ":" + port;
		}

		private String convertHost( String host ) {
			if( host.equals( "127.0.0.1" ) )
				return LOCALHOST_STR;
			else if( host.equals( "::1" ) )
				return LOCALHOST_STR;

			return host;
		}
	}

}
