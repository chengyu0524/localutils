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
package com.ejtone.mars.kernel.util.selector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 * @param <T>
 */
public class RollingSelector<T extends Object> implements Selector<T> {
	private static final Logger logger = LoggerFactory.getLogger( RollingSelector.class );

	private final List<T> list;
	private long pos = 0;
	private final ReentrantLock lock = new ReentrantLock( );
	private final Condition available = lock.newCondition( );

	public RollingSelector( ) {
		this.list = new ArrayList<T>( );
	}

	/**
	 * 
	 * @param object
	 * @return
	 * 				true : 添加了实例<br>
	 *         false: 更新了实例<br>
	 */
	public boolean add( T object ) {

		if( object == null ) {
			throw new NullPointerException( );
		}
		lock.lock( );
		try {
			boolean add = addOrUpdate( object );
			available.signalAll( );
			return add;
		} finally {
			lock.unlock( );
		}
	}

	/**
	 * 
	 * @param o
	 * @return
	 */
	public boolean remove( T o ) {
		lock.lock( );
		try {
			logger.info( "remove resource {} from selector", o );
			// Utils.printStackTrace();
			return list.remove( o );
		} finally {
			lock.unlock( );
		}
	}

	public List<T> removeAll( ) {
		lock.lock( );
		try {
			List<T> l = new ArrayList<>( list );
			list.clear( );
			return l;
		} finally {
			lock.unlock( );
		}
	}

	public T next( ) {
		lock.lock( );
		try {
			int size = list.size( );
			if( size == 0 ) { // 无数据
				return null;
			}
			int index = (int) ( pos++ % size );
			return list.get( index );
		} finally {
			lock.unlock( );
		}
	}

	public T waitNext( ) throws InterruptedException {
		lock.lock( );
		try {
			while( true ) {
				T r = next( );
				if( r != null ) {
					return r;
				}
				awaitAvailable( );
				continue;
			}
		} finally {
			lock.unlock( );
		}
	}

	public T waitNext( long timeout ) throws InterruptedException {

		lock.lock( );
		try {
			while( true ) {
				T r = next( );
				if( r != null ) {
					return r;
				}
				if( timeout > 0 ) {
					awaitAvailable( timeout );
					continue;
				}
				if( timeout == 0 ) {
					return null;
				} else {
					awaitAvailable( );
					continue;
				}
			}
		} finally {
			lock.unlock( );
		}
	}

	public List<T> getAll( ) {
		lock.lock( );
		try {
			return new ArrayList<T>( this.list );
		} finally {
			lock.unlock( );
		}
	}

	public int size( ) {
		return list.size( );
	}

	public void awaitAvailable( ) throws InterruptedException {
		if( list.size( ) > 0 ) {
			return;
		}
		lock.lock( );
		try {
			available.await( );
		} finally {
			lock.unlock( );
		}
	}

	public void awaitAvailable( long timeout ) throws InterruptedException {
		if( list.size( ) > 0 ) {
			return;
		}
		lock.lock( );
		try {
			available.await( timeout, TimeUnit.MILLISECONDS );
		} finally {
			lock.unlock( );
		}
	}

	/**
	 * add返回true
	 * update返回false
	 * 
	 * @param o
	 * @return
	 */
	private boolean addOrUpdate( T o ) {
		lock.lock( );
		try {
			Iterator<T> i = list.iterator( );
			while( i.hasNext( ) ) {
				T old = i.next( );
				if( old == o ) { // 同一个实例，不需要做什么
					return false;
				} else if( old.equals( o ) ) { // 相等但不是同一个实例
					logger.info( "update resource {} to {}", old, o );
					i.remove( ); // 删除&add
					list.add( o );
					return false;
				}
			}
			logger.debug( "add resource {} to selector", o );
			list.add( o );
			return true;
		} finally {
			lock.unlock( );
		}
	}
}
