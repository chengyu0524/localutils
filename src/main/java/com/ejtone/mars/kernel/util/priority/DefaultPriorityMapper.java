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
public class DefaultPriorityMapper implements PriorityMapper {

	private int[] mapper = { 0, 1, 2, 3, 3, 3, 3, 3, 3, 3 };
	private boolean orderAsc = true; // 升序排列
	private int maxPriority = mapper[mapper.length - 1]; // 最大优先级值

	public DefaultPriorityMapper( ) {}

	/**
	 * 将输入的优先级参数转换为系统的优先级
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public int convert( int intValue ) {
		if( intValue < 0 ) { // 小于0的值总是返回最低优先级
			return 0;
		} else if( intValue >= mapper.length ) { // 超过最大可用值时，根据升序还是降序返回
			return orderAsc ? maxPriority : 0;
		}
		return mapper[intValue];
	}

	@Override
	public int safe( int priority ) {
		return priority < 0 ? 0 : ( priority > maxPriority ? maxPriority : priority );
	}

	@Override
	public int maxPriority( ) {
		return maxPriority;
	}

	public void setStringMapper( String mapperString ) throws Exception {
		setMapper( toIntArray( mapperString ) );
	}

	public void setMapper( int[] mapper ) throws Exception {
		if( mapper.length == 0 || !validate( mapper ) ) {
			throw new Exception( "优先级配置不合法" );
		}
		this.maxPriority = this.orderAsc ? mapper[mapper.length - 1] : mapper[0];
		this.mapper = mapper;
	}

	private static final int[] toIntArray( String s ) {
		String[] stringArray = s.split( "," );
		int[] intArray = new int[stringArray.length];
		for( int i = 0, size = stringArray.length; i < size; i++ ) {
			intArray[i] = Integer.parseInt( stringArray[i] );
		}
		return intArray;
	}

	/**
	 * 校验优先级字符串是否合法
	 * 优先级字符串第一个值或最后一个值必须为0，第一个值为0时，数据为升序排列，否则为降序
	 * 两个相邻值的差不能大于1
	 * 
	 * @param priority
	 * @return
	 */
	private final boolean validate( int[] priority ) {
		int lastIndex = priority.length - 1;

		if( priority[0] > priority[lastIndex] ) { // 值越大优先级越低
			this.orderAsc = false; // 降序
			if( priority[lastIndex] != 0 )
				return false;
			for( int i = 0, size = lastIndex; i < size; i++ ) {
				int d = priority[i] - priority[i + 1];
				if( d != 0 && d != 1 ) { // 相邻的值差不能为1
					return false;
				}
			}
		} else { // 值越大优先级越大
			if( priority[0] != 0 )
				return false;
			for( int i = 0, size = lastIndex; i < size; i++ ) {
				int d = priority[i + 1] - priority[i];
				if( d != 0 && d != 1 ) {
					return false;
				}
			}
		}
		return true;
	}

}
