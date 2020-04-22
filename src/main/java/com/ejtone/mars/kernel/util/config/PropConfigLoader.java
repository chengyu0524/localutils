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
package com.ejtone.mars.kernel.util.config;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 */
public class PropConfigLoader {
	private static final Logger logger = LoggerFactory.getLogger( PropConfigLoader.class );

	public static Properties load( String configFile ) {
		return loadProperties( new File( configFile ) );
	}

	public static Properties loadProperties( File... configFiles ) {
		Properties prop = new Properties( );
		loadFiles( prop, configFiles );
		return prop;
	}

	public static void loadFiles( Properties prop, File... configFiles ) {

		InputStreamReader reader = null;
		for( File configFile : configFiles ) {
			if( !configFile.exists( ) ) {
				logger.error( "file {} not exist, ignore", configFile );
				continue;
			}
			try {
				reader = new InputStreamReader( new FileInputStream( configFile ), "UTF-8" );
				prop.load( reader );
			} catch( Exception e ) {
				logger.error( "", e );
			} finally {
				IOUtils.closeQuietly( reader );
				reader = null;
			}
		}
		return;
	}

	public static Properties loadPath( String path ) {
		Properties prop = new Properties( );

		if( path == null ) {
			return prop;
		}
		try {
			File[] array = new File( path ).listFiles( new FileFilter( ) {
				public boolean accept( File pathname ) {
					String filename = pathname.getName( ).toLowerCase( );
					if( filename.endsWith( ".properties" ) && !filename.equals( "log4j.properties" ) ) {
						return true;
					} else {
						return false;
					}
				}
			} );

			if( array == null || array.length == 0 ) {
				return prop;
			}
			loadFiles( prop, array );
			return prop;
		} catch( Exception ex ) {
			logger.error( "", ex );
			return prop;
		}
	}
}
