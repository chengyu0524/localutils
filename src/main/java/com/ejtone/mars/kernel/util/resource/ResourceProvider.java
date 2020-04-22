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
package com.ejtone.mars.kernel.util.resource;

import java.util.List;

/**
 * 资源提供者.
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 * @param <T>
 */
public interface ResourceProvider<T extends Object> {
	/**
	 * 根据ID查找资源
	 * 
	 * @param id
	 * @return
	 */
	public T get( Object id );

	/**
	 * 注册资源.
	 * 
	 * @param res
	 */
	public void regist( T res );

	/**
	 * 删除资源.
	 * 
	 * @param res
	 */
	public void remove( T res );

	/**
	 * 删除资源.
	 * 
	 * @param res
	 */
	public void removeByKey( Object key );

	/**
	 * 获取所有资源的拷贝.
	 * 
	 * @return
	 */
	public List<T> getAll( );

	public int size( );

	/**
	 * 注册资源监听者.
	 * 
	 * @param listener
	 */
	public void registListener( ResourceListener<T> listener );

	/**
	 * 删除资源监听者.
	 * 
	 * @param listener
	 */
	public void removeListener( ResourceListener<T> listener );

	/**
	 * 删除所有资源监听者.
	 */
	public void removeAllListeners( );

}
