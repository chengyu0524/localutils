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
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 */
public class XmlConfigLoader {
	private static final Logger logger = LoggerFactory.getLogger( XmlConfigLoader.class );

	public static Config load( String configFile ) {
		return load( new Config( ), new File( configFile ), true );
	}

	private static Config load( Config config, File configFile, boolean superFile ) {

		if( !configFile.exists( ) ) {
			logger.error( "file {} not exist, ignore", configFile.getPath( ) );
			return config;
		}

		try {
			Document document = new SAXReader( ).read( configFile );
			Element root = document.getRootElement( );

			Iterator<?> i = root.elementIterator( );
			while( i.hasNext( ) ) {
				readElement( config, "", (Element) i.next( ), false );
			}
			return config;
		} catch( DocumentException e ) {
			logger.error( "load file {} exception : {}", configFile.getPath( ), e.toString( ) );
			return config;
		}
	}

	private static void readProperty( Config config, String parent, Element e, boolean splice ) {

		String pn = e.attributeValue( "name" );
		String pv = e.attributeValue( "value" );
		if( StringUtils.isBlank( pn ) ) {
			logger.error( "not found name in property" );
			return;
		} else if( pv == null ) {
			pv = "";
		}

		if( splice ) {
			pn = parent + pn;
		}
		config.setProperty( pn, pv );
		return;
	}

	private static void readImport( Config config, String parent, Element e ) {
		String filename = e.attributeValue( "src" );
		if( StringUtils.isBlank( filename ) ) {
			logger.error( "not found import src, ignore" );
			return;
		}
		if( filename.endsWith( ".xml" ) ) {
			load( config, new File( ConfigHelper.getConfigFile( filename ) ), false );
		} else {
			PropConfigLoader.loadFiles( config.getProperties( ), new File( ConfigHelper.getConfigFile( filename ) ) );
		}
		return;
	}

	private static void readSpringContext( Config config, Element e ) {
		Iterator<?> i = e.elementIterator( );

		while( i.hasNext( ) ) {
			Element el = (Element) i.next( );

			String file = el.attributeValue( "name" );
			if( StringUtils.isBlank( file ) ) {
				logger.error( "not found spring file name, ignore" );
				return;
			}
			if( el.getName( ).equals( "classpath" ) ) {
				config.addSpringBeanConfigFile( true, file );
			} else if( el.getName( ).equals( "file" ) ) {
				config.addSpringBeanConfigFile( false, file );
			} else {
				logger.error( "illegal element name : {}", el.getName( ) );
			}
		}
		return;
	}

	private static void readElement( Config config, String parent, Element e, boolean splice ) {
		String name = e.getName( );
		switch( name ) {
			case "property" :
				readProperty( config, parent, e, splice );
				return;
			case "import" :
				readImport( config, parent, e );
				return;
			case "spring-context" :
				readSpringContext( config, e );
				return;
		}

		logger.debug( "reading element : {} ", name );

		String s = e.attributeValue( "splice" );
		if( StringUtils.isBlank( s ) || !s.equals( "true" ) ) {
			splice = false;
		} else {
			splice = true;
		}
		Iterator<?> i = e.elementIterator( );
		while( i.hasNext( ) ) {
			readElement( config, name, (Element) i.next( ), splice );
		}
		logger.debug( "reading element : {} end", name );
		return;
	}

	public static void main( String[] args ) {
		Config config = XmlConfigLoader.load( "./conf/config.xml" );
		System.out.println( JSON.toJSONString( config ) );
	}
}
