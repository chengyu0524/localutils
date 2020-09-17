package com.ejtone.mars.kernel.util.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ejtone.mars.kernel.util.app.App;
import com.ejtone.mars.kernel.util.config.ConfigUtils;
import com.ejtone.mars.kernel.util.lifecycle.AbstractLifeCycle;

public class McLPSenderWorker extends AbstractLifeCycle {


	private static final Logger logger = LoggerFactory.getLogger( McLPSenderWorker.class );
	public static final int delayInterval = ConfigUtils.getInt( "agent_send_delay_interval", 1000 ); // 延时间隔
	public static final int retryInterval = ConfigUtils.getInt( "agent_send_retry_interval", 100 ); // 重试间隔

	private int senderId;
	private BlockingQueue<WorkerBean> sendQ = new LinkedBlockingQueue<>( ); // 发送队列
	private BlockingQueue<DelaySubmit> retryQ = new LinkedBlockingQueue<>( ); // 重试队列
	// private long lastRetryTime = 0; //最后一次重试的时间 //重试需要间隔
	private Thread workThread;

	public McLPSenderWorker( int senderId ) {
		this.senderId = senderId;
	}

	public void submit( WorkerBean req ) {
		sendQ.offer( req );
	}

	@Override
	protected void doStart( ) throws Exception {
		super.doStart( );

		workThread = new Thread( new SenderTask( ), "gw-sender-" + senderId );
		workThread.start( );

	}

	@Override
	protected void doStop( ) throws Exception {
		super.doStop( );
		if( workThread != null ) {
			workThread.interrupt( );
		}
	}


	public final class SenderTask implements Runnable {


		private DelaySubmit templateSubmit;

		public SenderTask( ) {}

		@Override
		public void run( ) {

			while( true ) {
				if( !App.getInstance( ).isRunning( ) ) { // 如果系统已停止，则退出
					break;
				}

				WorkerBean req = recvMsg( );
				if( req == null ) {
					continue;
				}

				processSubmit( req );
			}
		}

		// 获取要下发的submit消息
		private WorkerBean recvMsg( ) {

			try {
				return sendQ.poll( retryInterval, TimeUnit.MILLISECONDS );// 尝试从sendQ中获取消息，如果没消息停顿一会儿
			} catch( InterruptedException e ) {
				return null;
			}
		}

		// 数据重试操作
		private void processSubmit( WorkerBean req ) {
			//数据该往哪发往哪发
			
		}


		private void trySaveToMsgQWithDelay( WorkerBean req, String marsId ) {
			//扔消息队列 
			//WorkerBean req 消息实体
			//marsId哪一台机器处理不过来就扔到哪一台机器的消息队列 他等会在处理
		}

	}


	public static final class DelaySubmit {
		private WorkerBean submit;
		private long delayTo;

		public DelaySubmit( WorkerBean submit, long delayTo ) {
			this.submit = submit;
			this.delayTo = delayTo;
		}

		public WorkerBean getSubmit( ) {
			return submit;
		}

		public long getDelayTo( ) {
			return delayTo;
		}
	}
}
