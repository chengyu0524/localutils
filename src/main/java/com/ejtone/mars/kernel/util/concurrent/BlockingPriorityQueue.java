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
package com.ejtone.mars.kernel.util.concurrent;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 优先级队列.
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 * @param <E>
 */
public class BlockingPriorityQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, java.io.Serializable {
	private static final Logger logger = LoggerFactory.getLogger( BlockingPriorityQueue.class );

	private static final long serialVersionUID = 1895807855312277146L;
	private final BlockingQueue<E>[] queues; // 实际的队列
	private final ReentrantLock lock = new ReentrantLock( ); // 非公平锁
	private final Condition notEmpty = lock.newCondition( ); // 总体的非空条件
	private AtomicInteger size = new AtomicInteger( 0 ); // 总队列的大小
	private PriorityInspector priorityInspector;
	private long capacity;

	public static interface PriorityInspector {
		public int getPriority( Object o );

		public int getMaxPriority( );
	}

	@SuppressWarnings( "unchecked" )
	public BlockingPriorityQueue( int limit, PriorityInspector inspector ) {
		if( limit <= 0 || inspector == null || inspector.getMaxPriority( ) < 0 ) {
			throw new IllegalArgumentException( );
		}
		this.capacity = limit;
		this.priorityInspector = inspector;
		int maxPriority = inspector.getMaxPriority( );
		queues = new ArrayBlockingQueue[maxPriority + 1];
		for( int i = 0; i <= maxPriority; i++ ) {
			queues[i] = new ArrayBlockingQueue<E>( limit );
		}
	}

	public long capacity( ) {
		return capacity;
	}

	public boolean add( E e ) {
		return offer( e );
	}

	public boolean offer( E e ) {
		BlockingQueue<E> q = getQueue( e );
		final ReentrantLock lock = this.lock;
		lock.lock( );
		try {
			boolean ok = q.offer( e );
			if( ok && size.getAndIncrement( ) == 0 ) {
				notEmpty.signal( ); // 只有在原先队列为空时，才通知队列非空
			}
			return ok;
		} finally {
			lock.unlock( );
		}
	}

	public boolean offer( E e, long timeout, TimeUnit unit ) throws InterruptedException {

		BlockingQueue<E> q = getQueue( e );
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly( );
		try {
			boolean ok = q.offer( e, timeout, unit );
			if( ok && size.getAndIncrement( ) == 0 ) {
				notEmpty.signal( );
			}
			return ok;
		} finally {
			lock.unlock( );
		}
	}

	public void put( E e ) throws InterruptedException {

		BlockingQueue<E> q = getQueue( e );
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly( );
		try {
			q.put( e );
			if( size.getAndIncrement( ) == 0 ) {
				notEmpty.signal( );
			}
			return;
		} finally {
			lock.unlock( );
		}
	}

	public E poll( ) {
		final ReentrantLock lock = this.lock;
		lock.lock( );
		try {
			if( size.get( ) == 0 ) {
				return null;
			}
			for( int i = queues.length - 1; i >= 0; i-- ) {
				BlockingQueue<E> q = queues[i];
				E e = q.poll( );
				if( e != null ) {
					if( size.getAndDecrement( ) > 1 ) {
						notEmpty.signal( );
					}
					return e;
				}
			}
			return null;
		} finally {
			lock.unlock( );
		}
	}

	public E take( ) throws InterruptedException {
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly( );
		try {
			try {
				while( size.get( ) == 0 ) {
					notEmpty.await( );
				}
			} catch( InterruptedException ie ) {
				notEmpty.signal( ); // propagate to non-interrupted thread
				throw ie;
			}
			E x = poll( );
			assert x != null;
			return x;
		} finally {
			lock.unlock( );
		}
	}

	public E poll( long timeout, TimeUnit unit ) throws InterruptedException {
		long nanos = unit.toNanos( timeout );
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly( );
		try {
			for( ;; ) {
				E x = poll( );
				if( x != null ) {
					return x;
				}
				if( nanos <= 0 )
					return null;
				try {
					nanos = notEmpty.awaitNanos( nanos );
				} catch( InterruptedException ie ) {
					notEmpty.signal( ); // propagate to non-interrupted thread
					throw ie;
				}
			}
		} finally {
			lock.unlock( );
		}
	}

	public E peek( ) {
		final ReentrantLock lock = this.lock;
		lock.lock( );
		try {
			if( size.get( ) == 0 ) {
				return null;
			}
			for( int i = queues.length - 1; i >= 0; i-- ) {
				BlockingQueue<E> q = queues[i];
				E e = q.peek( );
				if( e != null ) {
					return e;
				}
			}
			return null;
		} finally {
			lock.unlock( );
		}
	}

	public int size( ) {
		return size.get( );
	}

	public int remainingCapacity( ) {
		int remain = 0;
		lock.lock( );
		try {
			for( int i = queues.length - 1; i >= 0; i-- ) {
				remain += queues[i].remainingCapacity( );
			}
			return remain;
		} finally {
			lock.unlock( );
		}
	}

	public boolean remove( Object o ) {
		@SuppressWarnings( "unchecked" )
		E e = (E) o;
		lock.lock( );
		try {
			BlockingQueue<E> l = getQueue( e );
			if( l.remove( o ) ) {
				size.decrementAndGet( );
				return true;
			}
			return false;
		} finally {
			lock.unlock( );
		}
	}

	public boolean contains( Object o ) {
		@SuppressWarnings( "unchecked" )
		E e = (E) o;
		lock.lock( );
		try {
			BlockingQueue<E> l = getQueue( e );
			return l.contains( o );
		} finally {
			lock.unlock( );
		}
	}

	public BlockingQueue<E>[] getQueues( ) {
		return queues;
	}

	public MonitorQueueBean[] monitor( ) {
		MonitorQueueBean[] beans = new MonitorQueueBean[queues.length];
		for( int i = 0; i < queues.length; i++ ) {
			beans[i] = new MonitorQueueBean( );
			beans[i].priority = i;
			beans[i].size = queues[i].size( );
			beans[i].capacity = capacity;
		}
		return beans;
	}

	public Object[] toArray( ) {
		final ReentrantLock lock = this.lock;
		lock.lock( );
		try {
			if( size.get( ) == 0 ) {
				return new Object[0];
			} else {
				Object[] a = null;
				for( int i = queues.length - 1; i >= 0; i-- ) {
					ArrayUtils.addAll( a, queues[i].toArray( ) );
				}
				return a;
			}
		} finally {
			lock.unlock( );
		}
	}

	@SuppressWarnings( "unchecked" )
	public <T> T[] toArray( T[] a ) {
		final ReentrantLock lock = this.lock;
		lock.lock( );
		try {
			for( int i = queues.length - 1; i >= 0; i-- ) {
				T[] t = (T[]) java.lang.reflect.Array.newInstance( a.getClass( ).getComponentType( ), 0 );
				ArrayUtils.addAll( a, queues[i].toArray( t ) );
			}
			return a;
		} finally {
			lock.unlock( );
		}
	}

	public String toString( ) {
		final ReentrantLock lock = this.lock;
		lock.lock( );
		try {
			Object[] arr = toArray( );
			if( arr == null || arr.length == 0 )
				return "[]";
			StringBuilder sb = new StringBuilder( );
			sb.append( '[' );
			for( int i = 0, size = arr.length; i < size; i++ ) {
				Object e = arr[i];
				if( i != 0 ) {
					sb.append( ',' ).append( ' ' );
				}
				sb.append( e == this ? "(this Collection)" : e );
			}
			sb.append( "]" );
			return sb.toString( );
		} finally {
			lock.unlock( );
		}
	}

	@Override
	public int drainTo( Collection<? super E> c ) {
		if( c == null )
			throw new NullPointerException( );
		if( c == this )
			throw new IllegalArgumentException( );
		final ReentrantLock lock = this.lock;
		lock.lock( );
		try {
			int n = 0;
			E e;
			while( ( e = poll( ) ) != null ) {
				c.add( e );
				++n;
			}
			return n;
		} finally {
			lock.unlock( );
		}
	}

	@Override
	public int drainTo( Collection<? super E> c, int maxElements ) {
		if( c == null )
			throw new NullPointerException( );
		if( c == this )
			throw new IllegalArgumentException( );
		if( maxElements <= 0 )
			return 0;
		final ReentrantLock lock = this.lock;
		lock.lock( );
		try {
			int n = 0;
			E e;
			while( n < maxElements && ( e = poll( ) ) != null ) {
				c.add( e );
				++n;
			}
			return n;
		} finally {
			lock.unlock( );
		}
	}

	@Override
	public void clear( ) {
		final ReentrantLock lock = this.lock;
		lock.lock( );
		try {
			for( int i = queues.length - 1; i >= 0; i-- ) {
				queues[i].clear( );
			}
			size = new AtomicInteger( 0 );
		} finally {
			lock.unlock( );
		}
	}

	@Override
	public Iterator<E> iterator( ) {
		return new Itr( toArray( ) );
	}

	private BlockingQueue<E> getQueue( E e ) {
		return queues[safe( priorityInspector.getPriority( e ) )];
	}

	private int safe( int priority ) {
		return priority >= queues.length ? queues.length - 1 : priority;
	}

	private void writeObject( java.io.ObjectOutputStream s ) throws java.io.IOException {
		lock.lock( );
		try {
			s.defaultWriteObject( );
		} finally {
			lock.unlock( );
		}
	}

	/**
	 * Snapshot iterator that works off copy of underlying q array.
	 */
	private class Itr implements Iterator<E> {
		final Object[] array; // Array of all elements
		int cursor; // index of next element to return;
		int lastRet; // index of last element, or -1 if no such

		Itr( Object[] array ) {
			lastRet = -1;
			this.array = array;
		}

		public boolean hasNext( ) {
			return cursor < array.length;
		}

		@SuppressWarnings( "unchecked" )
		public E next( ) {
			if( cursor >= array.length )
				throw new NoSuchElementException( );
			lastRet = cursor;
			return (E) array[cursor++];
		}

		public void remove( ) {
			if( lastRet < 0 )
				throw new IllegalStateException( );
			Object x = array[lastRet];
			lastRet = -1;
			// Traverse underlying queue to find == element,
			// not just a .equals element.
			lock.lock( );
			try {
				BlockingPriorityQueue.this.remove( x );
			} finally {
				lock.unlock( );
			}
		}
	}

	public static class MonitorQueueBean {
		private int priority;
		private long size;
		private long capacity;

		public int getPriority( ) {
			return priority;
		}

		public long getSize( ) {
			return size;
		}

		public long getCapacity( ) {
			return capacity;
		}
	}

	public static void main( String[] args ) {
		BlockingPriorityQueue<PriorityObject> queue = new BlockingPriorityQueue<PriorityObject>( 1000, new PriorityInspector( ) {

			@Override
			public int getPriority( Object o ) {
				PriorityObject p = (PriorityObject) o;
				return p.priority;
			}

			@Override
			public int getMaxPriority( ) {
				return 4;
			}
		} );

		for( int i = 0; i < 1005; i++ ) {
			boolean b = queue.offer( new PriorityObject( i % 4 ) );
			if( !b ) {
				logger.error( "false for i = {}", i );
			}
		}
		while( true ) {
			PriorityObject o;
			try {
				o = queue.take( );
			} catch( InterruptedException e ) {
				e.printStackTrace( );
				return;
			}
			if( o == null ) {
				logger.info( "done" );
				break;
			}
			logger.debug( "priority = {}", o.priority );
		}
	}

	private static class PriorityObject {
		private int priority;

		public PriorityObject( int priority ) {
			this.priority = priority;
		}
	}
}
