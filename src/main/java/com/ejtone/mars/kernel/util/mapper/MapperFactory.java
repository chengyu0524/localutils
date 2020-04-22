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
package com.ejtone.mars.kernel.util.mapper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.ejtone.mars.kernel.util.MixUtil;
import com.ejtone.mars.kernel.util.config.ConfigUtils;

/**
 * 内存映射文件工厂类.
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 */
public class MapperFactory {

	private static String defaultMapperPath = MixUtil.mergeUrl( ConfigUtils.getString( "dataPath", "./data" ), "mapper" );
	private static Map<String, Mapper> map = new HashMap<String, Mapper>( );

	public static Mapper getMapper( String filename, int size ) {
		return getMapper( defaultMapperPath, filename, size );
	}

	public synchronized static Mapper getMapper( String pathname, String filename, int size ) {
		String file;
		if( pathname.endsWith( "/" ) )
			file = pathname + filename;
		else
			file = pathname + "/" + filename;
		Mapper mapper = map.get( file );
		if( mapper == null ) {
			new File( pathname ).mkdirs( );
			mapper = new Mapper( file, size );
			if( mapper != null ) {
				map.put( file, mapper );
			}
		}
		return mapper;
	}

	public synchronized static void close( Mapper mapper ) {
		map.remove( mapper.getName( ) );
	}

	public synchronized static void closeAll( ) {
		map.clear( );
	}
}
