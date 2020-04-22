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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PropertyPlaceholderHelper;

import com.ejtone.mars.kernel.util.config.Config.SpringBeanConfigFile;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * 配置文件工具类.
 * <p>
 * 考虑到配置的复杂性，程序默认项目为web工程，读取项目resources目录下config.xml文件.<br>
 * 如果是普通项目，需在启动时设置-Dwebapp=false,此时会使用项目conf目录下的配置文件<br>
 * 如需手工指定配置路径，需在启动时设置-DconfigPath=...<br>
 * 如需手工指定配置文件，需在启动时设置-DconfigFile=...<br>
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 */
public class ConfigUtils extends ConfigHelper {

	private static final Logger logger = LoggerFactory.getLogger( ConfigUtils.class );

	private static Config config;
	private static final PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper( "${", "}" );

	static {
		load( );
	}

	public static Properties getAll( ) {
		return config.getProperties( );
	}

	public static String getProperty( String key ) {
		return config.getProperty( key );
	}

	public static String getString( String key, String defaultValue ) {
		String value = getProperty( key );
		return value == null ? defaultValue : value;
	}

	public static boolean getBoolean( String key, boolean defaultValue ) {
		String value = getProperty( key );
		return value == null ? defaultValue : Boolean.parseBoolean( value );
	}

	public static int getInt( String key, int defaultValue ) {
		String value = getProperty( key );
		return value == null ? defaultValue : Integer.parseInt( value );
	}

	public static long getLong( String key, long defaultValue ) {
		String value = getProperty( key );
		return value == null ? defaultValue : Long.parseLong( value );
	}

	public static List<SpringBeanConfigFile> getSpringBeanConfigFileList( ) {
		return config.getSpringBeanConfigFileList( );
	}

	private static synchronized void load( ) {

		config = XmlConfigLoader.load( getConfigFile( ) );
		config.setProperty( configPathName, getConfigPath( ) ); // 将配置文件路径及文件做为属性存放
		config.setProperty( configFileName, getConfigFile( ) );

		replacePlaceholder( config.getProperties( ) );
		debugConfig( ); // 打印日志
	}

	private static void debugConfig( ) {
		Iterator<Entry<Object, Object>> i = config.getProperties( ).entrySet( ).iterator( );
		while( i.hasNext( ) ) {
			Entry<Object, Object> e = i.next( );
			logger.info( "{} = {}", e.getKey( ), e.getValue( ) );
		}
	}

	private static void replacePlaceholder( Properties prop ) {
		Iterator<Entry<Object, Object>> i = prop.entrySet( ).iterator( );
		while( i.hasNext( ) ) {
			Entry<Object, Object> e = i.next( );
			prop.put( e.getKey( ), helper.replacePlaceholders( (String) e.getValue( ), prop ) );
		}
	}

	public static void main( String[] args ) {
		System.out.println( "dataPath = " + ConfigUtils.getProperty( "dataPath" ) );
	}
}
