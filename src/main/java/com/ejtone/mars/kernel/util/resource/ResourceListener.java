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

/**
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 * @param <T>
 */
public interface ResourceListener<T extends Object> {

	/**
	 * listener名称.
	 * 打印信息时会使用，无实际意义
	 * 
	 * @return
	 */
	public String getName( );

	/**
	 * 资源增加.
	 * 
	 * @param res
	 */
	public void add( T res );

	/**
	 * 资源被删除.
	 * 
	 * @param res
	 */
	public void del( T res );

	/**
	 * 资源更新.
	 * 
	 * @param newRes
	 * @param oldRes
	 */
	public void update( T newRes, T oldRes );

	/**
	 * listener是否对该资源有兴趣.
	 * provider在新增资源时会调用listener的该方法，判断是否调用listener的add方法
	 * 
	 * @param res
	 * @return
	 */
	public boolean interest( T res );

	/**
	 * listener是否包含该资源.
	 * provider在修改/删除资源时会调用listener的该方法，判断是否调用listener的update/del方法
	 * 
	 * @param res
	 * @return
	 */
	public boolean contains( T res );
}
