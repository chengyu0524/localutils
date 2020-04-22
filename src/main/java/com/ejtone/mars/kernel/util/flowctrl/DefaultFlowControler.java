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
package com.ejtone.mars.kernel.util.flowctrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.ejtone.mars.kernel.util.Timer;

/**
 * 流量控制器 每个流量控制器与一个通道相关联,用于对通道进行流量控制
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 */
public class DefaultFlowControler implements FlowControler {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger( DefaultFlowControler.class );

	/**
	 * 每秒发送的最大值
	 * 小于0不控制
	 * 等于0功能待定,不要用.目前是不控制
	 */
	private int limit = -1;

	private int size = limit; // 数组的大小

	/**
	 * 当前位置保存最老节点
	 */
	private int current;

	private long period = TimeUnit.MILLISECONDS.toNanos( 100 ); /* 间隔的时间片段 */

	/**
	 * 一个limit大小的数组,用于存放消息发送时的时间 数组内容循环存放,到达limit后从0重新开始 控制时,取出current位置的发送时间
	 * 如果与当前时间差额大于period,说明period内未发送到limit条消息 否则说明到达流量限制.线程停顿difftime后重新判断
	 */
	private long controler[];

	private ReentrantLock lock = new ReentrantLock( true ); // 使用公平锁，防止某个线程等待太久
	private Condition condition = lock.newCondition( );
	private Semaphore sem = new Semaphore( 1 );

	public DefaultFlowControler( ) {

	}

	/**
	 * @param limit
	 *        100毫秒内发送的最大条数
	 */
	public DefaultFlowControler( int limit ) {
		this( limit, 100 );
	}

	/**
	 * @param limit
	 *        period内发送的最大条数
	 * @param period
	 *        一个时间段,毫秒为单位
	 */
	public DefaultFlowControler( int limit, int period ) {
		init( limit, period );
	}

	public long getFlowLimit( ) {
		return limit;
	}

	public long getFlowPeriod( ) {
		return period;
	}

	private void init( int limit, int period ) {
		lock.lock( );
		try {
			if( limit > 0 ) {
				this.period = TimeUnit.MILLISECONDS.toNanos( period );
				this.current = 0;
				this.controler = new long[limit];
				this.size = limit;
			}
			this.limit = limit;
		} finally {
			lock.unlock( );
		}
	}

	public void setLimit( int limit ) {
		lock.lock( );
		try {
			if( limit > 0 ) {
				if( limit > size ) { // 如果新的数据较大，则新建一个数组，将当前数据拷贝过去
					this.current = 0;
					long[] controler = new long[limit];
					if( this.controler != null ) {
						System.arraycopy( this.controler, 0, controler, 0, this.limit );
					}
					this.controler = controler;
					this.size = limit;
				} else if( this.current >= limit ) { // 如果当前位置已经超过边界了，将current置为0
					this.current = 0;
				}
				this.limit = limit;
			}
		} finally {
			lock.unlock( );
		}
	}

	public void update( int limit, int period ) {
		if( this.period == period ) {
			this.setLimit( limit );
		} else {
			this.init( limit, period );
		}
	}

	/**
	 * 试图通过number个号码的流控,如果不能通过,则等待直到通过
	 * 等待时锁定资源,其他线程的流控都需要等待
	 * 
	 * @param number
	 */
	public void control( int number ) throws InterruptedException {

		// lock.lock( ); // 先得到锁
		sem.acquire( );
		try {
			for( int i = 0; i < number; i++ ) {
				control( ); // 等待通过
			}
		} finally {
			// lock.unlock( );
			sem.release( );
		}
	}

	/**
	 * 流量控制 如果未通过则一直休眠,直到通过
	 */
	public void control( ) throws InterruptedException {

		long sleeptime;
		lock.lock( );
		try {
			while( true ) {
				if( ( sleeptime = tryAccept( 0 ) ) == 0 ) {
					return;
				}
				condition.awaitNanos( sleeptime );
			}
		} finally {
			lock.unlock( );
		}
	}

	/**
	 * 流控
	 * 
	 * @return 全部能通过则返回true,否则返回false
	 */
	public boolean accept( int number ) {
		return tryAccept( 0, number ) == 0 ? true : false;
	}

	/**
	 * 流量控制
	 * 
	 * @return 通过返回true,否则返回false
	 */
	public boolean accept( ) {
		return ( tryAccept( 0 ) == 0 ) ? true : false;
	}

	/**
	 * 判断发送number条信息是否超限,并对超限的个条数进行限制,使之一段时间内的发送的消息都超限
	 * ec在解包后会使用包内的手机号码数调用本函数
	 * ec发送来的数据以包为单位进行流控,如果包内的手机号码超过1条,则进行判断,如果手机号码数超过了流控上限,则使ec在一段时间内无法发送消息
	 * 
	 * @param number
	 */
	public void limit( int number ) {
		if( limit <= 0 ) {
			return;
		}

		int count = number - 1; // 减一去掉通过流控的那个数据包
		if( count <= 0 ) {
			return;
		}

		lock.lock( );
		try {
			long currtime = System.nanoTime( );
			for( ; count > 0; count-- ) { // 试图过count次流控
				if( tryAccept( currtime ) != 0 ) { // 流控超限了 //超限后对剩下的消息数进行惩罚
					long passtime = getPassTime( count ); // 计算出要全部通过剩下的count条消息需要到的时间

					for( int i = 0; i < limit; i++ ) { // 将所有的controler的时间设为passtime,这样在passtime到之前,没有消息可以通过
						controler[i] = passtime; // 不能简单的将current设为passtime,否则其他的计算passtime会出问题
					}
					break;
				}
			}
		} finally {
			lock.unlock( );
		}
	}

	/**
	 * 流量控制
	 * 
	 * @return 如果通过返回0,否则返回需要休息的秒数
	 */
	protected long tryAccept( long currtime ) {

		if( limit <= 0 )
			return 0;

		long difftime;
		lock.lock( );
		try {
			if( currtime == 0 ) {
				currtime = System.nanoTime( );
			}
			if( ( difftime = currtime - controler[current] ) >= period ) {
				controler[current] = currtime;
				current = ( current + 1 ) % limit;
				return 0;
			}
			return period - difftime;
		} finally {
			lock.unlock( );
		}
	}

	protected long tryAccept( long currtime, int number ) {
		if( limit <= 0 || number <= 0 )
			return 0;
		long difftime;

		lock.lock( );
		try {
			if( currtime == 0 ) {
				currtime = System.nanoTime( );
			}

			int position = ( current + number - 1 ) % limit; // 找到测试的位置
			if( ( difftime = currtime - controler[position] ) >= period ) {
				for( int i = 0; i < number; i++ ) {/* 将相应位置置为当前时间 */
					controler[( current + i ) % limit] = currtime;
				}
				current = ( position + 1 ) % limit; // 最旧的节点
				return 0;
			}
			// /**
			// * 未通过，打印一下信息
			// */
			// logger.info( "current = {}, position = {}, difftime = {}", currtime, controler[position],
			// difftime );
			// for( int i = 0; i < limit; i++ ) {
			// logger.info( "current + {} = {}", i, controler[( current + i ) % limit] );
			// }

		} finally {
			lock.unlock( );
		}
		return period - difftime;
	}

	protected long getPassTime( int number ) {
		if( limit <= 0 )
			return 0;

		int div = number / limit;
		int rem = number % limit;

		int position = ( current + rem - 1 ) % limit;

		return controler[position] + div * period;
	}

	protected static void TestControl( ) {

		DefaultFlowControler c = new DefaultFlowControler( 2, 1000 );

		Timer timer = new Timer( );
		for( int i = 0; i < 30; i++ ) {
			try {
				c.control( 1 );
			} catch( InterruptedException e ) {
				e.printStackTrace( );
			}
			// try {
			// //Thread.sleep( 5 ); //15034/733 15045/734
			// //Thread.sleep( 1 ); //3045/155 3015/150
			// Thread.sleep( 2 ); //cost 6100ms/305true 6081/303 6015/300 6016/300
			// } catch( InterruptedException e ) {
			// }
		}
		// System.out.println( cnt );
		timer.cal( "" );
	}

	protected static void TestMultiControl( ) {

		final DefaultFlowControler c = new DefaultFlowControler( 2, 1000 );

		for( int j = 0; j < 5; j++ ) {
			new Thread( new Runnable( ) {

				public void run( ) {
					int cnt = 0;
					Timer timer = new Timer( );
					for( int i = 0; i < 10; i++ ) {
						try {
							c.control( );
						} catch( InterruptedException e ) {
							e.printStackTrace( );
						}
						cnt++;
					}
					timer.cal( "" + cnt );
				}
			} ).start( );

		}
	}

	protected static void TestAccept( ) {

		DefaultFlowControler c = new DefaultFlowControler( 5, 100 );
		int cnt = 0;
		Timer timer = new Timer( );
		for( int i = 0; i < 300; i++ ) {
			if( c.accept( 10 ) ) {
				cnt++;
			}
			try {
				// Thread.sleep( 5 ); //15034/733 15045/734
				// Thread.sleep( 1 ); //3045/155 3015/150
				Thread.sleep( 2 ); // cost 6100ms/305true 6081/303 6015/300 6016/300
			} catch( InterruptedException e ) {
			}
		}
		System.out.println( cnt );
		timer.cal( "" );
	}

	protected static void TestMultiAccept( ) {

		final DefaultFlowControler c = new DefaultFlowControler( 5, 100 );

		for( int j = 0; j < 5; j++ ) {
			new Thread( new Runnable( ) {

				public void run( ) {
					int cnt = 0;
					Timer timer = new Timer( );
					for( int i = 0; i < 3000; i++ ) {
						if( c.accept( ) ) {
							// System.out.println( Thread.currentThread( ).getName( ) + "-" + true );
							cnt++;
						}
						try {
							/**
							 * sleep 2
							 * 6009/48 6009/44 6026/71 6026/75 6026/64
							 * 6008/48 6007/58 6016/59 6019/65 6021/70
							 */
							Thread.sleep( 2 ); // 105true
						} catch( InterruptedException e ) {
						}
					}
					timer.cal( "" + cnt );
				}
			} ).start( );

		}
	}

	protected static void TestMultiControlerAccept( ) {

		final DefaultFlowControler c = new DefaultFlowControler( 10, 100 );

		for( int j = 0; j < 5; j++ ) {
			new Thread( new Runnable( ) {

				public void run( ) {

					Timer timer = new Timer( );
					for( int i = 0; i < 100; i++ ) {
						try {
							c.control( 3 );
						} catch( InterruptedException e ) {
							e.printStackTrace( );
						} // max 14992, 14983, 14998
						// try {
						// /**
						// * sleep 2
						// *
						// * 6009/48 6009/44 6026/71 6026/75 6026/64
						// * 6008/48 6007/58 6016/59 6019/65 6021/70
						// */
						// Thread.sleep( 2 ); //105true
						// } catch( InterruptedException e ) {
						// }
					}
					timer.cal( "" );
				}
			} ).start( );

		}
	}

	protected static void TestUpdate( ) {
		DefaultFlowControler f = new DefaultFlowControler( 5, 1000 );
		f.setLimit( 30 );
		f.setLimit( 10 );
	}

	protected static void TestControlAndAccept( ) {

		final DefaultFlowControler f1 = new DefaultFlowControler( 15, 1000 );
		final DefaultFlowControler f2 = new DefaultFlowControler( 30, 2000 );

		final ThreadPoolExecutor t1 = new ThreadPoolExecutor( 10, 10, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>( 10000 ) );
		final ThreadPoolExecutor t2 = new ThreadPoolExecutor( 10, 10, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>( 10000 ) );
		for( int i = 0; i < 1000; i++ ) {
			t1.execute( new Runnable( ) {

				@Override
				public void run( ) {
					try {
						f1.control( 15 );
						logger.info( "11111111111111111111{}", System.currentTimeMillis( ) );
						t2.execute( new Runnable( ) {

							@Override
							public void run( ) {
								logger.info( "222222222222222222{}", System.currentTimeMillis( ) );
								boolean accept = f2.accept( 15 );
								if( accept ) {
									logger.info( "good" );

								} else {
									logger.error( "overlimit!!!!!!!!!!!" );

								}
							}
						} );
					} catch( InterruptedException e ) {
						// TODO Auto-generated catch block
						e.printStackTrace( );
					}
				}
			} );
		}

	}

	public static void main( String[] args ) {
		// TestControl( );
		// TestMultiControl( );
		// TestAccept( );
		// TestMultiAccept( );
		// TestMultiControlerAccept( );
		// TestUpdate( );
		TestControlAndAccept( );
	}

}
