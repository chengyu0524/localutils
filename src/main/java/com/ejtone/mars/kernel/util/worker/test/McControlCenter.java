package com.ejtone.mars.kernel.util.worker.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ejtone.mars.kernel.util.JsonUtil;
import com.ejtone.mars.kernel.util.MixUtil;
import com.ejtone.mars.kernel.util.NamedThreadFactory;
import com.ejtone.mars.kernel.util.config.ConfigUtils;
import com.ejtone.mars.kernel.util.lifecycle.AbstractLifeCycle;

/**
 * 控制中心
 */
public class McControlCenter extends AbstractLifeCycle {

	private static final Logger logger = LoggerFactory.getLogger( McControlCenter.class );

	public static final int multipleMsgNumsInSender = ConfigUtils.getInt( "multiple_msgnums_sender", 1 );
	public static final int multipleMsgNumsInCache = ConfigUtils.getInt( "multiple_msgnums_cache", 10 );
	public static final int lpSenderNums = ConfigUtils.getInt( "lp_sender_nums", 32 );


	// 如果某个sender中的消息数超过该值，则打印该sender中消息的分布情况
	public static final int monMsgNumsInSenderThreadhold = ConfigUtils.getInt( "mon_msgnums_sender_threadhold", 1024 );
	// 如果缓存中某个网关消息的个数超过该值，则打印缓存中该网关消息的个数
	public static final int monMsgNumsInCacheThreadhold = ConfigUtils.getInt( "mon_msgnums_cache_threadhold", 50 );

	private static final int cacheTaskInterval = ConfigUtils.getInt( "center_cache_task_interval", 1000 ); // 缓存扫描间隔
	private static final int monitorTaskInterval = ConfigUtils.getInt( "center_monitor_task_interval", 3000 ); // 监控扫描间隔


	private Map<String/* gwId */, GwRuntimeInfo> gwMap = new ConcurrentHashMap<>( );
	private McLPSenderWorker[] workers = new McLPSenderWorker[lpSenderNums];
	// 2018年4月1日修改 增加线程池名字
	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool( 2, new NamedThreadFactory( "controler-task" ) );

	public static final McControlCenter getInstance( ) {
		return InstanceHolder.isntance;
	}


	@Override
	protected void doStart( ) throws Exception {
		super.doStart( );
		int count = workers.length;
		for( int i = 0; i < count; i++ ) {
			workers[i] = new McLPSenderWorker( i );
			workers[i].start( );
		}

		executorService.scheduleWithFixedDelay( new CacheTask( ), cacheTaskInterval, cacheTaskInterval, TimeUnit.MILLISECONDS );
		executorService.scheduleWithFixedDelay( new MonitorTask( ), monitorTaskInterval, monitorTaskInterval, TimeUnit.MILLISECONDS );
	}

	@Override
	protected void doStop( ) throws Exception {
		super.doStop( );
		executorService.shutdown( );
		for( int i = 0, size = workers.length; i < size; i++ ) {
			if( workers[i] != null ) {
				MixUtil.safeStop( workers[i] );
			}
		}
	}

	public void decrMsgNumsInSender( String gwId ) {
		GwRuntimeInfo r = gwMap.get( gwId );
		if( r == null ) {
			logger.error( "not found runtime info for gw {}", gwId );
			return;
		}
		r.decrMsgNumsInSender( );
	}

	public boolean submit( WorkerBean submit ) {
		String gwId = submit.getId();
		GwRuntimeInfo gwinfo = gwMap.get( gwId );
		if( gwinfo == null ) {
			logger.error( "not found gwRuntimeInfo {}", gwId );
			return false;
		}

		// 首先尝试替换cache中消息
		WorkerBean reqplaced = gwinfo.tryReplaceMsgFromCacheToSender( submit );
		if( reqplaced != null ) { // 有消息可替换，直接将替换的消息扔到sender中
			if( logger.isDebugEnabled() ){
				logger.debug( "替换缓存信息下发：{} 原消息先放入缓存 {}", JsonUtil.toJsonString( reqplaced ),JsonUtil.toJsonString( submit ) );
			}
			workers[getSenderId( gwinfo.getSenderId( ) )].submit( reqplaced );
			return true;
		}
		// 没有消息可替换，走原先的逻辑
		if( gwinfo.incrMsgNumsInSender( ) ) {
			workers[getSenderId( gwinfo.getSenderId( ) )].submit( submit );
			return true;
		} else {
			return gwinfo.submitMsgToCache( submit );
		}
	}


	public int getSenderId( int senderId ) {
		return senderId % lpSenderNums;
	}


	// 缓存任务，在Sender有空间的时候尝试从缓存中获取消息并放入Sender
	public final class CacheTask implements Runnable {

		@Override
		public void run( ) {

			Iterator<GwRuntimeInfo> it = gwMap.values( ).iterator( );
			while( it.hasNext( ) ) {
				GwRuntimeInfo inf = it.next( );
				int index = getSenderId( inf.getSenderId( ) );
				for( int i = 0, size = inf.getMaxNumInSender( ); i < size; i++ ) { // 一次循环从cache中获取有限的消息
					WorkerBean req = inf.transMsgFromCacheToSender( );
					if( req == null ) {
						break;// 缓存中没有消息或者sender中没有空间
					} else {
						workers[index].submit( req );
					}
				}
			}
		}
	}



	public final class MonitorTask implements Runnable {

		@Override
		public void run( ) {

			CenterStatInfo stat = new CenterStatInfo( lpSenderNums );

			Iterator<GwRuntimeInfo> it = gwMap.values( ).iterator( );
			while( it.hasNext( ) ) {
				GwRuntimeInfo inf = it.next( );
				int index = getSenderId( inf.getSenderId( ) );

				SenderStatInfo senderStatInfo = stat.senders[index];
				senderStatInfo.msgNums += inf.getMsgNumInSender( );
				senderStatInfo.gwMap.put( inf.getGwId( ), new GwStatInfo( inf.getGwId( ), inf.getMsgNumInSender( ) ) );

				stat.cache.msgNums += inf.getMsgNumInCache( );
				stat.cache.list.add( new GwStatInfo( inf.getGwId( ), inf.getMsgNumInCache( ) ) );
			}

			monCenterStatInfo( stat );

		}

		private void monCenterStatInfo( CenterStatInfo stat ) {

			StringBuilder sb = new StringBuilder( );

			// 打印Sender统计信息
			for( SenderStatInfo info : stat.senders ) {
				sb.append( "sender[" ).append( info.senderId ).append( "]=" ).append( info.msgNums );
				if( info.msgNums > monMsgNumsInSenderThreadhold ) { // 消息个数超过打印阈值，打印消息分布情况
					sb.append( "{" );
					Iterator<GwStatInfo> it = info.gwMap.values( ).iterator( );
					while( it.hasNext( ) ) {
						GwStatInfo gwStatInfo = it.next( );
						sb.append( gwStatInfo.gwId ).append( "=" ).append( gwStatInfo.msgNums ).append( "," );
					}
					sb.append( "}" );
				}
				sb.append( ";" );
			}
			logger.info( "MsgInSenders:{}", sb.toString( ) );


			// 打印Cache统计信息
			sb = new StringBuilder( );
			sb.append( stat.cache.msgNums );

			sb.append( "{" );
			for( GwStatInfo gwStatInfo : stat.cache.list ) {
				if( gwStatInfo.msgNums > monMsgNumsInCacheThreadhold ) {
					sb.append( gwStatInfo.gwId ).append( "=" ).append( gwStatInfo.msgNums ).append( "," );
				}
			}
			sb.append( "}" );

			logger.info( "MsgInCaches:{}", sb.toString( ) );
		}
	}



	private static final class CenterStatInfo {

		private SenderStatInfo[] senders;

		private CacheStatInfo cache;

		public CenterStatInfo( int senderNums ) {
			senders = new SenderStatInfo[senderNums];
			if( senders.length > 0 ) {
				for( int i = 0; i < senders.length; i++ ) {
					senders[i] = new SenderStatInfo( i );
				}
			}
			// 2018年4月1日修改
			cache = new CacheStatInfo( );
		}
	}


	private static final class CacheStatInfo {
		private int msgNums;
		private List<GwStatInfo> list = new ArrayList<>( );
	}


	private static final class SenderStatInfo {
		private int senderId;
		private int msgNums;
		private Map<String/* gwId */, GwStatInfo> gwMap = new HashMap<>( );

		public SenderStatInfo( int senderId ) {
			this.senderId = senderId;
		}

	}


	private static final class GwStatInfo {
		private String gwId;
		private int msgNums;

		public GwStatInfo( String gwId, int msgNums ) {
			this.gwId = gwId;
			this.msgNums = msgNums;
		}
	}


	private static final class InstanceHolder {
		public static final McControlCenter isntance = new McControlCenter( );
	}
}
