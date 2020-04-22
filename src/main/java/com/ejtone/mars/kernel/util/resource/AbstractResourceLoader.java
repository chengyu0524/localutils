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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 */
public abstract class AbstractResourceLoader implements ResourceLoader {

	protected AtomicBoolean loading = new AtomicBoolean( false );

	/**
	 * 加载全部资源
	 */
	@Override
	public void load( ) {
		if( loading.compareAndSet( false, true ) ) {
			try {
				doLoad( );
				return;
			} finally {
				loading.set( false );
			}
		}
	}

	protected abstract void doLoad( );
}
