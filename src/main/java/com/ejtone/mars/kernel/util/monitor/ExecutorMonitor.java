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
package com.ejtone.mars.kernel.util.monitor;

import com.ejtone.mars.kernel.util.MixUtil;
import com.ejtone.mars.kernel.util.concurrent.BlockingPriorityQueue;
import com.ejtone.mars.kernel.util.concurrent.BlockingPriorityQueue.MonitorQueueBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;

/**
 * 线程池监控
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 */
public class ExecutorMonitor extends AbstractMonitor {
	private static final Logger logger = MixUtil.monLogger;

	private List<NamedExecutor> list = new ArrayList<>( ); // 监控的线程池列表

	public static ExecutorMonitor getInstance( ) {
		return InstanceHolder.instance;
	}

	private ExecutorMonitor( ) {

	}

	public void regist( String name, ThreadPoolExecutor executor ) {
		synchronized( list ) {
			list.add( new NamedExecutor( name, executor ) );
			logger.info( "regist executor {}:{} to monitor", name, System.identityHashCode( executor ) );
		}
	}


	public void unregist( ThreadPoolExecutor executor ) {
		synchronized( list ) {
			NamedExecutor ne = removeExecutor( executor );
			if( ne != null ) {
				logger.info( "unregist executor {}:{} from monitor", ne.name, System.identityHashCode( executor ) );
			}
		}
	}

	private NamedExecutor removeExecutor( ThreadPoolExecutor executor ) {
		Iterator<NamedExecutor> i = list.iterator( );
		while( i.hasNext( ) ) {
			NamedExecutor ne = i.next( );
			if( ne.executor == executor ) {
				i.remove( );
				return ne;
			}
		}
		return null;
	}

	/**
	 * 该方法适用于在spring配置文件中设置要检测的Executor
	 */
	public void setExecutors( Collection<NamedExecutor> c ) {
		if( c == null ) {
			return;
		}
		list.addAll( c );
	}

	private static class NamedExecutor {
		private String name;
		private ThreadPoolExecutor executor;

		public NamedExecutor( String name, ThreadPoolExecutor executor ) {
			this.name = name;
			this.executor = executor;
		}
	}

	private static class InstanceHolder {
		public static final ExecutorMonitor instance = new ExecutorMonitor( );
	}

	private class MonitorTask implements Runnable {

		@Override
		public void run( ) {

			try {
				runTask( );
			} catch( Throwable t ) {
				logger.error( "", t );
			}
		}

		private void runTask( ) {
			Iterator<NamedExecutor> iter = list.iterator( );
			while( iter.hasNext( ) ) {
				NamedExecutor ne = iter.next( );
				ThreadPoolExecutor threadPool = ne.executor;
				String name = ne.name;
				BlockingQueue<Runnable> queue = threadPool.getQueue( );
				if( queue instanceof ArrayBlockingQueue ) {
					ArrayBlockingQueue<Runnable> aq = (ArrayBlockingQueue<Runnable>) queue;

					logger.info( "Monitor Executor {} : core = {}, curr = {}/{}, jobs = {}/{}", name, threadPool.getCorePoolSize( ), threadPool.getPoolSize( ),
							threadPool.getMaximumPoolSize( ), aq.size( ), aq.remainingCapacity( ) + aq.size( ) );
				} else if( queue instanceof BlockingPriorityQueue ) {
					BlockingPriorityQueue<Runnable> bq = (BlockingPriorityQueue<Runnable>) queue;
					MonitorQueueBean[] beans = bq.monitor( );
					StringBuilder sb = new StringBuilder( );
					for( int j = 0; j < beans.length; j++ ) {
						if( j != 0 ) {
							sb.append( ", " );
						}
						sb.append( "q-" ).append( beans[j].getPriority( ) ).append( ":" ).append( beans[j].getSize( ) ).append( "/" )
								.append( beans[j].getCapacity( ) );
					}
					logger.info( "Monitor Executor {} : core = {}, curr = {}/{}, jobs = {}", name, threadPool.getCorePoolSize( ), threadPool.getPoolSize( ),
							threadPool.getMaximumPoolSize( ), sb.toString( ) );
				} else {
					logger.info( "Monitor Executor {} : core = {}, curr = {}/{}, jobs = {}/{}", name, threadPool.getCorePoolSize( ), threadPool.getPoolSize( ),
							threadPool.getMaximumPoolSize( ), queue.size( ), -1 );
				}
			}
		}
	}

	@Override
	protected Runnable getMonitorTask( ) {
		return new MonitorTask( );
	}
}
