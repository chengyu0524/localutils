package com.ejtone.mars.kernel.util;

public class KV {

	private String k;
	private String v;


	public KV( String k, String v ) {
		this.k = k;
		this.v = v;
	}


	public String getK( ) {
		return k;
	}


	public String getV( ) {
		return v;
	}


	@Override
	public boolean equals( Object obj ) {
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass( ) != obj.getClass( ) )
			return false;
		KV other = (KV) obj;
		if( k == null ) {
			if( other.k != null )
				return false;
		} else if( !k.equals( other.k ) )
			return false;
		if( v == null ) {
			if( other.v != null )
				return false;
		} else if( !v.equals( other.v ) )
			return false;
		return true;
	}


}
