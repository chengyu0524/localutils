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
package com.ejtone.mars.kernel.util.flowctrl;

/**
 * 流量控制接口.
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 */
public interface FlowControler {

	/**
	 * 暂停线程，直到1个请求允许通过.
	 * 
	 * @throws InterruptedException
	 */
	public void control( ) throws InterruptedException;

	/**
	 * 暂停线程，直到number个请求允许通过.
	 * 
	 * @throws InterruptedException
	 */
	public void control( int number ) throws InterruptedException;

	/**
	 * 判断请求是否允许通过.
	 * 
	 * @return
	 */
	public boolean accept( );

	/**
	 * 判断number个请求是否允许通过.
	 * 
	 * @param number
	 * @return
	 */
	public boolean accept( int number );
}
