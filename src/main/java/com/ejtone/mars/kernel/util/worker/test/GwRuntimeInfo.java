package com.ejtone.mars.kernel.util.worker.test;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 运行时信息
 */
public class GwRuntimeInfo {
	private String gwId; //网关标识
	private int senderId; //该网关所属sender
	private volatile int msgNumInSender =100; //sender中该网关的消息数
	private int maxNumInSender=100; //sender中最多保存该网关的消息数
	//		private int msgNumInCache; //缓存中该网关的消息数
	private int maxNumInCache=1000; //缓存中最多保存该网关的消息数
//	private Lock senderLock = new ReentrantLock( );
	private Lock cacheLock = new ReentrantLock( );
	private LinkedList<WorkerBean> cache = new LinkedList<>( );
	private static AtomicIntegerFieldUpdater<GwRuntimeInfo> updater = AtomicIntegerFieldUpdater.newUpdater( GwRuntimeInfo.class, "msgNumInSender" );



	public String getGwId( ) {
		return gwId;
	}

	public void setGwId( String gwId ) {
		this.gwId = gwId;
	}

	public int getSenderId( ) {
		return senderId;
	}

	public void setSenderId( int senderId ) {
		this.senderId = senderId;
	}

	public int getMsgNumInSender( ) {
		return msgNumInSender;
	}

	public void setMsgNumInSender( int msgNumInSender ) {
		this.msgNumInSender = msgNumInSender;
	}

	public int getMaxNumInSender( ) {
		return maxNumInSender;
	}

	public void setMaxNumInSender( int maxNumInSender ) {
		this.maxNumInSender = maxNumInSender;
	}

	public int getMsgNumInCache( ) {
		return cache.size( );
	}

	//		public void setMsgNumInCache( int msgNumInCache ) {
	//				this.msgNumInCache = msgNumInCache;
	//		}

	public int getMaxNumInCache( ) {
		return maxNumInCache;
	}

	public void setMaxNumInCache( int maxNumInCache ) {
		this.maxNumInCache = maxNumInCache;
	}

	private boolean submit( WorkerBean submit ) {
		return false;
	}

	public void decrMsgNumsInSender( ) {
		//		senderLock.lock( );
		//		try {
		//			msgNumInSender--;
		//		} finally {
		//			senderLock.unlock( );
		//		}
		updater.decrementAndGet( this );
	}

	public boolean incrMsgNumsInSender( ) {
		if( msgNumInSender < maxNumInSender ) { //高并发时该操作会让消息多几条，不会有大影响，也不需要控制这么精密
			updater.incrementAndGet( this );
			return true;
		}
		return false;

		//		senderLock.lock( );
		//		try {
		//			if( msgNumInSender < maxNumInSender ) {
		//				msgNumInSender++;
		//				return true;
		//			}
		//			return false;
		//		} finally {
		//			senderLock.unlock( );
		//		}
	}

	public boolean submitMsgToCache( WorkerBean submit ) {
		cacheLock.lock( );
		try {
			if( cache.size( ) < maxNumInCache ) {
				return cache.add( submit );
			} else {
				return false;
			}
		} finally {
			cacheLock.unlock( );
		}
	}

	public WorkerBean transMsgFromCacheToSender( ) {
		cacheLock.lock( );
		try {
			if( cache.size( ) == 0 ) {
				return null;
			}
			if( incrMsgNumsInSender( ) ) {
				return cache.removeFirst( );
			} else {
				return null;
			}
		} finally {
			cacheLock.unlock( );
		}
	}

	/**
	 * 尝试替换cache中的消息到sender中.
	 * <p>
	 * 如果cache中有消息，那么取出该消息，将submit扔进去.取出的消息后续会扔到sender中
	 *
	 * @param submit
	 * @return
	 */
	public WorkerBean tryReplaceMsgFromCacheToSender( WorkerBean submit ) {
		cacheLock.lock( );
		try {
			if( cache.size( ) == 0 ) {
				return null;
			}
			if( incrMsgNumsInSender( ) ) {
				WorkerBean replaced = cache.removeFirst( );
				cache.add( submit );
				return replaced;
			} else {
				return null;
			}
		} finally {
			cacheLock.unlock( );
		}
	}
}

