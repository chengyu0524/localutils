package com.ejtone.mars.kernel.util.resource;

import java.util.List;

public class DelegateResourceProvider<T> implements ResourceProvider<T> {

	private ResourceProvider<T> delegate;

	public DelegateResourceProvider( ) {}


	public ResourceProvider<T> getDelegate( ) {
		return delegate;
	}

	public void setDelegate( ResourceProvider<T> delegate ) {
		this.delegate = delegate;
	}


	@Override
	public T get( Object id ) {
		return delegate.get( id );
	}

	@Override
	public void regist( T res ) {
		delegate.regist( res );
	}

	@Override
	public void remove( T res ) {
		delegate.remove( res );
	}

	@Override
	public void removeByKey( Object key ) {
		delegate.removeByKey( key );
	}

	@Override
	public List<T> getAll( ) {
		return delegate.getAll( );
	}

	@Override
	public int size( ) {
		return delegate.size( );
	}

	@Override
	public void registListener( ResourceListener<T> listener ) {
		delegate.registListener( listener );
	}

	@Override
	public void removeListener( ResourceListener<T> listener ) {
		delegate.removeListener( listener );
	}

	@Override
	public void removeAllListeners( ) {
		delegate.removeAllListeners( );
	}



}
