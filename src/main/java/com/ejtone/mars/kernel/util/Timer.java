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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 程序耗时计算工具类.
 * 设置logger属性为debug时启用
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 */
public class Timer {

	private static final Logger logger = LoggerFactory.getLogger( Timer.class );

	private long threshold;

	long clock;

	public Timer( ) {
		this( 0 );
	}

	public Timer( long threshold ) {
		this.threshold = threshold;
		this.clock = System.currentTimeMillis( );
	}

	public void start( ) {
		if( logger.isDebugEnabled( ) ) {
			this.clock = System.currentTimeMillis( );
		}
	}

	public void cal( String tag ) {
		if( logger.isDebugEnabled( ) ) {
			long t = System.currentTimeMillis( );
			long diff = t - this.clock;
			if( diff >= threshold ) {
				logger.debug( "{} cost: {} ms", tag, diff );
			}
			this.clock = t;
		}
	}

	public long cost( ) {
		return System.currentTimeMillis( ) - this.clock;
	}
}
