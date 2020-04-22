/**
 * Copyright [2000-2100] [cuihq]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ejtone.mars.kernel.util.app;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.ejtone.mars.kernel.util.MixUtil;
import com.ejtone.mars.kernel.util.NamedThreadFactory;
import com.ejtone.mars.kernel.util.config.Config.SpringBeanConfigFile;
import com.ejtone.mars.kernel.util.config.ConfigUtils;
import com.ejtone.mars.kernel.util.lifecycle.AbstractLifeCycle;
import com.ejtone.mars.kernel.util.monitor.ExecutorMonitor;
import org.aspectj.util.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.Lifecycle;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class App extends AbstractLifeCycle implements ApplicationContextAware {

	private static final Logger logger = LoggerFactory.getLogger( App.class );

	private static final App instance = new App( );
	private int corePoolSize = 2;
	private boolean loadSpringContext = true;
	private ApplicationContext springContext;
	private final String resourcePath = ConfigUtils.getConfigPath( );
	private final List<Runnable> hooks = new ArrayList<>( );
	private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool( corePoolSize, new NamedThreadFactory( "pubpool" ) );

	public static App getInstance( ) {
		return instance;
	}

	private App( ) {
		if( scheduledExecutor instanceof ThreadPoolExecutor ) {
			ExecutorMonitor.getInstance( ).regist( "APP", (ThreadPoolExecutor) scheduledExecutor );
		}
	}

	public void setCorePoolSize( int corePoolSize ) {
		this.corePoolSize = corePoolSize;
	}

	public void setLoadSpringContext( boolean loadSpringContext ) {
		this.loadSpringContext = loadSpringContext;
	}

	@Override public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
		this.springContext = applicationContext;
	}

	public ScheduledExecutorService getScheduledExecutor( ) {
		return scheduledExecutor;
	}

	public Object getBean( String name ) {
		if( springContext == null ) {
			return null;
		} else {
			return springContext.getBean( name );
		}
	}

	public ApplicationContext getApplicationContext( ) {
		return springContext;
	}

	public synchronized App addShutdownHook( Runnable hook ) {
		hooks.add( hook );
		return this;
	}

	public long getStartTime( ) {
		return ManagementFactory.getRuntimeMXBean( ).getStartTime( );
	}

	public long getUpTime( ) {
		return ManagementFactory.getRuntimeMXBean( ).getUptime( );
	}

	private String[] getXmlFiles( ) {

		List<SpringBeanConfigFile> list = ConfigUtils.getSpringBeanConfigFileList( );
		List<String> fileList = new ArrayList<>( list.size( ) );
		for( SpringBeanConfigFile f : list ) {
			if( f.isClassPathFile( ) ) {
				fileList.add( "classpath:" + f.getFilename( ) );
			} else {
				if( new File( f.getFilename( ) ).isAbsolute( ) ) { // 绝对路径，以此为准
					fileList.add( "file:" + f.getFilename( ) );
				} else { // 相对路径，前面加上配置文件路径
					fileList.add( "file:" + MixUtil.mergeUrl( ConfigUtils.getConfigPath( ), f.getFilename( ) ) );
				}
			}
		}
		return fileList.toArray( new String[0] );
	}

	public void initLog4j( ) {
		File file = new File( resourcePath + "/log4j.xml" );
		if( file.exists( ) ) {
			initLog4j( file );
		} else {
			file = new File( resourcePath + "/log4j.properties" );
			if( file.exists( ) ) {
				initLog4j( file );
			} else {
				logger.info( "not found log4j configuration file" );
			}
		}
	}

	private void initLog4j( File file ) {
		String className = "org.apache.log4j.PropertyConfigurator";
		try {
			Class<?> clazz = Class.forName( className );
			Reflection.invokestaticN( clazz, "configureAndWatch", new String[]{ file.getAbsolutePath( ) } );
		} catch( ClassNotFoundException e ) {
			return;
		}
	}

	public void initLogback( ) {
		File file = new File( resourcePath + "/logback.xml" );
		if( !file.exists( ) ) {
			logger.info( "not found loback configuration file" );
			return;
		}
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory( );
		try {
			JoranConfigurator configurator = new JoranConfigurator( );
			configurator.setContext( lc );
			lc.reset( );
			configurator.doConfigure( resourcePath + "/logback.xml" );
		} catch( JoranException e ) {
			logger.error( "", e );
		}
	}

	public void initSpringContext( ) {
		String[] xmlFiles = getXmlFiles( );
		if( xmlFiles != null && xmlFiles.length > 0 ) {
			springContext = new FileSystemXmlApplicationContext( xmlFiles, true, springContext );
		}
	}

	@Override protected void doStart( ) throws Exception {
		logger.info( "context starting up" );


		Thread.setDefaultUncaughtExceptionHandler( new Thread.UncaughtExceptionHandler( ) {
			@Override public void uncaughtException( Thread t, Throwable e ) {
				logger.error( "uncaughtException, thread = {}, e = ", t.getName( ), e );
			}
		} );

		initLogback( );
		initLog4j( );
		if( loadSpringContext ) {
			initSpringContext( );
		}

		Runtime.getRuntime( ).addShutdownHook( new Thread( ) {

			@Override public void run( ) {
				try {
					instance.stop( );
				} catch( Exception e ) {
					logger.error( "", e );
				}
			}
		} );
	}

	@Override protected void doStop( ) throws Exception {
		logger.info( "context stopping" );
		for( int i = hooks.size( ) - 1; i >= 0; i-- ) { //
			try {
				hooks.get( i ).run( );
			} catch( Throwable e ) {
			}
		}
		if( springContext != null ) {
			( (Lifecycle) springContext ).stop( );
		}
	}

	public void halt( ) {

		logger.info( "system is halting" );
		Executors.newSingleThreadExecutor( ).submit( new Runnable( ) {
			@Override public void run( ) {
				Runtime.getRuntime( ).exit( 0 );
			}
		} );
	}

	public static void main( String[] args ) {

		try {
			App.getInstance( ).start( );
		} catch( Exception e ) {
			logger.error( "", e );
			Runtime.getRuntime( ).exit( -1 );
		}
	}
}
