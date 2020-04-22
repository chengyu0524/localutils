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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 内存映射文件.
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 */
public class Mapper {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger( Mapper.class );

	private MappedByteBuffer buffer;
	private String name;

	public Mapper( String file, int size ) {

		logger.info( "Mapper(String, int) - file=" + file + ", size=" + size );

		this.name = file;
		RandomAccessFile raf = null;
		FileChannel channel = null;
		buffer = null;
		try {
			raf = new RandomAccessFile( file, "rwd" );
			if( raf.length( ) == 0 ) { // 如果文件长度为0, 则为其分配空间
				byte[] b = new byte[size];
				raf.write( b );
			}
			/*
			 * 映射文件
			 */
			channel = raf.getChannel( );
			buffer = channel.map( FileChannel.MapMode.READ_WRITE, 0, size );
		} catch( Exception e ) {
			logger.error( "new Mapper exception:", e );
		} finally {
			if( channel != null ) {
				try {
					channel.close( );
				} catch( IOException e ) {
				}
			}
			if( raf != null ) {
				try {
					raf.close( );
				} catch( IOException e ) {
				}
			}
		}
	}

	public String getName( ) {
		return name;
	}

	public MappedByteBuffer getBuffer( ) {
		return this.buffer;
	}

}
