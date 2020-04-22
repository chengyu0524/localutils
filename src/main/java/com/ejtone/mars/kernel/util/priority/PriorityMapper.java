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
package com.ejtone.mars.kernel.util.priority;

/**
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 */
public interface PriorityMapper {
	/**
	 * 将输入的优先级值转换为系统的优先级.
	 * 
	 * @param value
	 * @return
	 */
	public int convert( int value );

	/**
	 * 安全使用优先级值，防止越界.
	 * 
	 * @param priority
	 * @return
	 */
	public int safe( int priority );

	/**
	 * 优先级的最大值.
	 * 
	 * @return
	 */
	public int maxPriority( );
}
