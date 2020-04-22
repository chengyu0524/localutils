package com.ejtone.mars.kernel.util.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DelegateListenerProvider<T extends Object> implements ListenerProvider<T> {

	private List<T> listeners = new ArrayList<>( );
	private Lock lock = new ReentrantLock( );
	private ListenerInspector<T> inspector;

	public DelegateListenerProvider( ListenerInspector<T> inspector ) {
		this.inspector = inspector;
	}

	public void notifyListeners( Object... args ) {
		lock.lock( );
		try {
			Iterator<T> i = listeners.iterator( );
			while( i.hasNext( ) ) {
				inspector.notify( i.next( ), args );
			}
		} finally {
			lock.unlock( );
		}
	}

	public void nofityListener( T listener, Object... args ) {
		inspector.notify( listener, args );
	}

	public void setListeners( Collection<T> c ) {
		lock.lock( );
		try {
			Iterator<T> i = c.iterator( );
			while( i.hasNext( ) ) {
				listeners.add( i.next( ) );
			}
		} finally {
			lock.unlock( );
		}
	}

	@Override
	public void registListener( T listener ) {
		lock.lock( );
		try {
			listeners.add( listener );
		} finally {
			lock.unlock( );
		}
	}

	@Override
	public void removeListener( T listener ) {
		lock.lock( );
		try {
			listeners.remove( listener );
		} finally {
			lock.unlock( );
		}
	}

	public static interface ListenerInspector<T extends Object> {
		public void notify( T listener, Object... args );
	}

}
