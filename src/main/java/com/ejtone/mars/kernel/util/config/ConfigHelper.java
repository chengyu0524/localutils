package com.ejtone.mars.kernel.util.config;

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
import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.ejtone.mars.kernel.util.MixUtil;

/**
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 */
public class ConfigHelper {

	public static final String configPathName = "configPath";
	public static final String configFileName = "configFile";
	public static final String classPathContextName = "classPathContext";
	public static final String fileSystemContextName = "fileSystemContext";

	private static final boolean webapp =
			getBooleanValue( System.getProperty( "webapp" ), ConfigHelper.class.getClassLoader( ).getResource( "/" ) == null ? false : true );

	private static final String configPath = getValue( System.getProperty( configPathName ),
			webapp ? ConfigHelper.class.getClassLoader( ).getResource( "/" ).getPath( ) : MixUtil.mergeUrl( System.getProperty( "user.dir" ), "conf/" ) );

	private static final String configFile = MixUtil.mergeUrl( configPath, getValue( System.getProperty( configFileName ), "config.xml" ) );

	public static String getConfigPath( ) {
		return configPath;
	}

	public static String getConfigFile( ) {
		return configFile;
	}

	public static String getConfigFile( String configFile ) {
		if( new File( configFile ).isAbsolute( ) ) {
			return configFile;
		} else {
			return MixUtil.mergeUrl( configPath, configFile );
		}
	}

	private static String getValue( String value, String defaultValue ) {
		return StringUtils.isBlank( value ) ? defaultValue : value;
	}

	private static boolean getBooleanValue( String value, boolean defaultValue ) {
		return StringUtils.isBlank( value ) ? defaultValue : "true".equals( value );
	}

}
