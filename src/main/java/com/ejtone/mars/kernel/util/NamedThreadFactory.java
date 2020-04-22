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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 命名线程工厂.
 * 
 * @author cuihq
 *
 */
public class NamedThreadFactory implements ThreadFactory {
	
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger( NamedThreadFactory.class );

	private final ThreadGroup group;
	private final AtomicInteger counter;
	private final String namePrefix;
	private static Map<String, AtomicInteger> map = new ConcurrentHashMap<>( );

	public NamedThreadFactory( String name ) {
		super( );
		this.counter = getCounter( name );
		this.group = new ThreadGroup( name );
		this.namePrefix = name;
	}

	public Thread newThread( Runnable runnable ) {
		StringBuffer buffer = new StringBuffer( );
		buffer.append( this.namePrefix );
		buffer.append( '-' );
		buffer.append( this.counter.getAndIncrement( ) );
		Thread t = new Thread( group, runnable, buffer.toString( ), 0 );
		t.setDaemon( false );
		t.setPriority( Thread.NORM_PRIORITY );
		//t.setUncaughtExceptionHandler( new DefaultUncaughtExceptionHandler( this.namePrefix ) );
		return t;
	}

	private static AtomicInteger getCounter( String name ) {
		AtomicInteger counter = map.get( name );
		if( counter == null ) {
			synchronized( map ) {
				counter = map.get( name );
				if( counter == null ) {
					counter = new AtomicInteger( );
					map.put( name, counter );
				}
			}
		}
		return counter;
	}
	
	/**
	 * 线程未捕获异常处理
	 * <p>
	 * 如果线程池中线程需要对捕获异常处理(打印日志),创始化线程池时,需要指定线程工厂为"NamedThreadFactory".
	 * @author mh
	 */
	@SuppressWarnings( "unused" )
	private static class DefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {

		@SuppressWarnings( "unused" )
		private String name;

		@SuppressWarnings( "unused" )
		public DefaultUncaughtExceptionHandler( ) {
			this.name = "";
		}

		public DefaultUncaughtExceptionHandler( String name ) {
			this.name = name;
		}

		@Override
		public void uncaughtException( Thread t, Throwable e ) {
			if( e instanceof UncaughtException ) {
				logger.error( "e.clz={}, e.msg={}, data={}", e.getCause( ).getClass( ).getSimpleName( ), e.getMessage( ),
						( (UncaughtException) e ).getData( ) );

			} else {
				logger.error( "e.clz={}, e.msg={}, e.stack={}", e.getCause( ).getClass( ).getSimpleName( ), e.getMessage( ), e.getStackTrace( ) );
			}
		}
	}
	
	/**
	 * 封装了业务数据的RuntimeException
	 * <p>
	 * 如果日志需要记录业务数据,需要捕获异常,将业务数据封装之后抛出.不需要业务数据,代码不用捕获
	 * @author mh
	 */
	public static class UncaughtException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		// 异常时的业务数据
		private String data;

		public UncaughtException( ) {}

		public UncaughtException( String message, Throwable cause, String data ) {
			super( message, cause );
			this.data = data;
		}

		public String getData( ) {
			return data;
		}
	}
}
