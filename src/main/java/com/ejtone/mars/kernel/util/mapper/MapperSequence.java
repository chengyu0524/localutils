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
package com.ejtone.mars.kernel.util.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 使用内存映射文件保存序列号
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 */
public class MapperSequence {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger( MapperSequence.class );

	private Mapper mapper;
	private long minValue;
	private long maxValue;
	private long curValue = 1;
	private long requireNumber; // 每次获取的序列号数量
	private int lockNumber; // 锁的数目 //多个数目的锁有利于提高吞吐量
	private SequenceValue[] values;
	private ReentrantLock[] locks;
	private AtomicBoolean initialized = new AtomicBoolean( );

	public MapperSequence( String name, long minValue, long maxValue ) {
		this( name, minValue, maxValue, 1024, 16 );
	}

	public MapperSequence( String name, long minValue, long maxValue, int requireNumber, int lockNumber ) {

		this.minValue = minValue;
		this.maxValue = maxValue;
		this.requireNumber = requireNumber;
		this.lockNumber = lockNumber;
		if( this.maxValue < this.minValue ) {
			this.maxValue = this.minValue;
		}

		Mapper mapper = MapperFactory.getMapper( name, Long.SIZE / 8 );
		if( mapper == null ) {
			throw new RuntimeException( "create mapper " + name + " failed" );
		} else {
			this.setMapper( mapper );
		}

		values = new SequenceValue[lockNumber];
		locks = new ReentrantLock[lockNumber];

		for( int i = 0; i < lockNumber; i++ ) {
			values[i] = new SequenceValue( );
			locks[i] = new ReentrantLock( );
		}
	}

	public void init( ) {
		if( initialized.compareAndSet( false, true ) ) {
			for( int i = 0; i < lockNumber; i++ ) {
				acquire( values[i] );
			}
		}
	}

	public Long next( ) {
		int i = (int) ( Thread.currentThread( ).getId( ) % lockNumber );
		Lock l = locks[i];
		l.lock( );
		try {
			return values[i].value( );
		} finally {
			l.unlock( );
		}
	}

	private synchronized void acquire( SequenceValue value ) {
		long max;
		if( curValue >= maxValue - requireNumber + 1 ) {
			max = maxValue;
			value.setCurValue( curValue );
			value.setMaxValue( maxValue );
			curValue = minValue;
		} else {
			max = curValue + requireNumber - 1;
			value.setCurValue( curValue );
			value.setMaxValue( max );
			curValue = max + 1;
		}

		if( mapper != null && mapper.getBuffer( ) != null ) {
			mapper.getBuffer( ).putLong( 0, curValue );
			mapper.getBuffer( ).force( );
		}
	}


	private void setMapper( Mapper mapper ) {
		this.mapper = mapper;
		long curValue = mapper.getBuffer( ).getLong( 0 );
		if( curValue < this.minValue )
			curValue = this.minValue;
		logger.info( "sequence initialized, curvalue = {}", curValue );
		this.curValue = curValue;
	}

	private class SequenceValue {
		private long maxValue = -1;
		private long curValue;

		public void setMaxValue( long maxValue ) {
			this.maxValue = maxValue;
		}

		public void setCurValue( long curValue ) {
			this.curValue = curValue;
		}

		public long value( ) {

			if( curValue > maxValue ) {
				acquire( this );
			}
			return curValue++;
		}
	}

}
