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

public interface ResourceInspector<T extends Object> {

	/**
	 * 获取资源标识.
	 * 
	 * @param resource
	 * @return
	 */
	Object getId( T resource );

	/**
	 * 比较两个资源是否相同.
	 * <p>
	 * provider在regist资源时，会使用该方法判断资源是否完全相同。如果不相同，会调用相应listener的update方法
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	boolean equals( T o1, T o2 );

}
