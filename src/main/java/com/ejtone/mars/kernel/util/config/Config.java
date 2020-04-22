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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 */
public class Config {

	private Properties props = new Properties( );
	private List<SpringBeanConfigFile> springBeanConfigFileList = new ArrayList<>( );

	public String getProperty( String key ) {
		return props.getProperty( key );
	}

	public Properties getProperties( ) {
		return props;
	}

	public String setProperty( String key, String value ) {
		return (String) props.setProperty( key, value );
	}

	public void setProperties( String key, String value ) {
		this.props.setProperty( key, value );
	}

	public List<SpringBeanConfigFile> getSpringBeanConfigFileList( ) {
		return this.springBeanConfigFileList;
	}

	public void addSpringBeanConfigFile( boolean isClassPathFile, String filename ) {
		springBeanConfigFileList.add( new SpringBeanConfigFile( isClassPathFile, filename ) );
	}

	public static class SpringBeanConfigFile {
		boolean isClassPathFile;
		String filename;

		SpringBeanConfigFile( boolean isClassPathFile, String filename ) {
			this.isClassPathFile = isClassPathFile;
			this.filename = filename;
		}

		public boolean isClassPathFile( ) {
			return isClassPathFile;
		}

		public String getFilename( ) {
			return filename;
		}
	}
}
