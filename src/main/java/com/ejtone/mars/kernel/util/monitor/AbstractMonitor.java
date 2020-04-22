package com.ejtone.mars.kernel.util.monitor;

import com.ejtone.mars.kernel.util.MixUtil;
import com.ejtone.mars.kernel.util.NamedThreadFactory;
import com.ejtone.mars.kernel.util.lifecycle.AbstractLifeCycle;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

public abstract class AbstractMonitor extends AbstractLifeCycle {
	
	protected static Logger logger = MixUtil.monLogger;
	
	// 所有的监控共享这个线程池
	protected static ScheduledExecutorService executor = Executors.newScheduledThreadPool( 2, new NamedThreadFactory( "monitor" ) );

	private int monitorRateMs = 10000; // 监控时间间隔，毫秒为单位

	private ScheduledFuture<?> monFuture;

	public void setMonitorRateMs( int monitorRateMs ) {
		this.monitorRateMs = monitorRateMs;
	}

	@Override
	protected void doStart( ) throws Exception {
		super.doStart( );
		monFuture = executor.scheduleAtFixedRate( getMonitorTask( ), monitorRateMs, monitorRateMs, TimeUnit.MILLISECONDS );
	}

	@Override
	protected void doStop( ) throws Exception {
		if( monFuture != null ) {
			monFuture.cancel( false );
			monFuture = null;
		}
		super.doStop( );
	}

	protected abstract Runnable getMonitorTask( );
}
