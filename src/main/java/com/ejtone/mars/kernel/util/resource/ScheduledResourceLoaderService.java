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
package com.ejtone.mars.kernel.util.resource;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ejtone.mars.kernel.util.NamedThreadFactory;

public class ScheduledResourceLoaderService {

	private static final ScheduledResourceLoaderService instance = new ScheduledResourceLoaderService( );

	private final ScheduledThreadPoolExecutor executor;

	public static ScheduledResourceLoaderService getInstance( ) {
		return instance;
	}

	private ScheduledResourceLoaderService( ) {
		executor = new ScheduledThreadPoolExecutor( 5, new NamedThreadFactory( "ScheduledResourceExecutor" ) );
	}

	public void scheduleAtFixedRate( final ResourceLoader loader, long initialDelay, long period, TimeUnit unit ) {
		executor.scheduleAtFixedRate( new Runnable( ) {

			@Override
			public void run( ) {
				loader.load( );
			}

		}, initialDelay, period, unit );
	}
}
