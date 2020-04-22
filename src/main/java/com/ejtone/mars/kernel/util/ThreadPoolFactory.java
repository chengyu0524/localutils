package com.ejtone.mars.kernel.util;

import com.ejtone.mars.kernel.util.monitor.ExecutorMonitor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.FactoryBean;

public class ThreadPoolFactory implements FactoryBean<ThreadPoolExecutor> {

	private String name;
	private int minThreads;
	private int maxThreads;
	private int maxJobs;

	public void setName( String name ) {
		this.name = name;
	}

	public void setMinThreads( int minThreads ) {
		this.minThreads = minThreads;
	}

	public void setMaxThreads( int maxThreads ) {
		this.maxThreads = maxThreads;
	}

	public void setMaxJobs( int maxJobs ) {
		this.maxJobs = maxJobs;
	}

	@Override
	public ThreadPoolExecutor getObject( ) throws Exception {
		ThreadPoolExecutor executor = new ThreadPoolExecutor( minThreads, maxThreads, 0L, TimeUnit.HOURS, new ArrayBlockingQueue<Runnable>( maxJobs ) );
		executor.prestartAllCoreThreads( );
		ExecutorMonitor.getInstance( ).regist( name, executor );
		return executor;
	}

	@Override
	public Class<?> getObjectType( ) {
		return ThreadPoolExecutor.class;
	}

	@Override
	public boolean isSingleton( ) {
		return false;
	}

}
