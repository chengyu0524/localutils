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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * 
 * @author cuihq[cui2005@gmail.com]
 *
 * @param <T>
 */
public abstract class AbstractResourceProvider<T extends Object> extends AbstractResourceLoader implements ResourceProvider<T> {

	private static final Logger logger = LoggerFactory.getLogger( AbstractResourceProvider.class );

	protected ResourceInspector<T> inspector;
	private Map<Object, T> map = new HashMap<Object, T>( );

	private List<ResourceListener<T>> list = new ArrayList<ResourceListener<T>>( );

	public AbstractResourceProvider( ResourceInspector<T> inspector ) {
		this.inspector = inspector;
	}

	@Override
	public T get( Object id ) {
		return map.get( id );
	}

	@SuppressWarnings( "unchecked" )
	protected Class<T> getTClass( ) {
		return (Class<T>) ( (ParameterizedType) getClass( ).getGenericSuperclass( ) ).getActualTypeArguments( )[0];
	}

	@Override
	public synchronized void regist( T res ) {
		T old = map.get( inspector.getId( res ) );
		if( old != null ) {
			if( inspector.equals( old, res ) ) {
				logger.debug( "resource {} no change", inspector.getId( res ) );
			} else {
				map.put( inspector.getId( res ), res );
				logger.info( "update resource {}", inspector.getId( res ) );
				resourceUpdate( res, old );
			}
		} else {
			map.put( inspector.getId( res ), res );
			logger.info( "regist resource {}", inspector.getId( res ) );
			resourceAdd( res );
		}
	}

	@Override
	public synchronized void remove( T res ) {
		T old = map.remove( inspector.getId( res ) );
		if( old != null ) {
			logger.info( "unregist resource {}", inspector.getId( res ) );
			resourceDel( old );
		}
	}

	@Override
	public synchronized void removeByKey( Object key ) {
		T old = map.remove( key );
		if( old != null ) {
			logger.info( "unregist resource {}", key );
			resourceDel( old );
		}
	}

	@Override
	public synchronized List<T> getAll( ) {
		List<T> l = new ArrayList<>( map.values( ) );
		return l;
	}

	@Override
	public synchronized int size( ) {
		return map.size( );
	}

	@Override
	public synchronized void registListener( ResourceListener<T> listener ) {

		/*
		 * 添加listener
		 */
		if( !registInternal( listener ) ) {
			return;
		}

		/*
		 * 将所有资源发送给listener
		 */
		Iterator<T> i = map.values( ).iterator( );
		while( i.hasNext( ) ) {
			T r = i.next( );
			try {
				if( listener.interest( r ) ) {
					listener.add( r );
				}
			} catch( Exception e ) {
				logger.error( "add resource {} to listener {} exception", inspector.getId( r ), listener.getName( ) );
				logger.error( "exception info : ", e );
			}
		}
	}

	@Override
	public synchronized void removeListener( ResourceListener<T> listener ) {
		Iterator<ResourceListener<T>> i = list.iterator( );
		while( i.hasNext( ) ) {
			ResourceListener<T> l = i.next( );
			if( l.equals( listener ) ) {
				i.remove( );
				logger.info( "listener {} unregisted", listener.getName( ) );
				/*
				 * 通知listener所有资源删除
				 */
				// Iterator<T> vi = map.values( ).iterator( );
				// while( vi.hasNext( ) ) {
				// T r = vi.next( );
				// try {
				// if( listener.interest( r ) ) {
				// listener.del( r );
				// }
				// } catch( Exception e ) {
				// logger.error( "remove resource {} from listener {} exception", inspector.getId( r ),
				// listener.getName( ) );
				// logger.error( "exception info : ", e );
				// }
				// }
				return;
			}
		}
	}

	@Override
	public synchronized void removeAllListeners( ) {
		list = new ArrayList<ResourceListener<T>>( );
	}

	@Override
	protected void doLoad( ) {
		try {
			Collection<T> c = doLoad0( );
			if( c != null ) {
				updateResource( c );
			}
		} catch( Exception e ) {
			logger.error( "", e );
		}
	}

	protected Collection<T> doLoad0( ) throws Exception {
		return null;

	};

	/**
	 * 更新所有resource.
	 * <p>
	 * doLoad方法在加载完成该资源后，需要调用该方法更新所有资源.
	 * 
	 * @param c
	 */
	protected synchronized void updateResource( Collection<T> c ) {

		/*
		 * 生成新的mapper,遍历c,将数据导入mapper中
		 */
		Map<Object, T> newer = new HashMap<Object, T>( );
		Iterator<T> i = c.iterator( );
		while( i.hasNext( ) ) {
			T t = i.next( );
			newer.put( inspector.getId( t ), t );
		}

		/*
		 * 使新的数据生效
		 */
		Map<Object, T> older = map;
		map = newer;

		/*
		 * 重新遍历c,将c中的数据从older中删除
		 * 如果原先有该数据，则比较是否更新，如有更新，通知各个listener
		 * 如果原先无数据，说明该数据为新加数据，通知各个listener
		 * 遍历完成后，older中剩余的数据为被删除的数据，通知各个listener
		 */
		i = c.iterator( );
		while( i.hasNext( ) ) {
			T t = i.next( );
			Object key = inspector.getId( t );
			T o = older.remove( key );
			if( o == null ) { // 原map中不存在该res,为新加
				logger.info( "regist resource {}", inspector.getId( t ) );
				resourceAdd( t );
			} else { // 原map中存在该res,判断是否为更新
				if( !inspector.equals( t, o ) ) {
					logger.info( "update resource {}", inspector.getId( t ) );
					resourceUpdate( t, o );
				} else {
					logger.debug( "resource {} no change", inspector.getId( t ) );
				}
			}
		}
		/*
		 * 遍历结束,older中剩余的res为已删除的res
		 */
		i = older.values( ).iterator( );
		while( i.hasNext( ) ) {
			resourceDel( i.next( ) );
		}
	}

	protected void resourceAdd( T res ) {
		Iterator<ResourceListener<T>> i = list.iterator( );
		while( i.hasNext( ) ) {
			ResourceListener<T> r = i.next( );
			try {
				if( r.interest( res ) ) {
					r.add( res );
				}
			} catch( Throwable e ) {
				logger.error( "add resource {} to listener {} exception", inspector.getId( res ), r.getName( ) );
				logger.error( "exception info : ", e );
			}
		}
	}

	protected void resourceUpdate( T res, T old ) {
		Iterator<ResourceListener<T>> i = list.iterator( );
		while( i.hasNext( ) ) {
			ResourceListener<T> r = i.next( );
			try {
				if( r.contains( old ) ) {// 如果原先包含该资源则更新
					r.update( res, old );
				}else if( r.interest( res )){// 如果原先不包含，则询问是否对新资源感兴趣
						r.add(res);
				}
//
//				else if( StringUtils.equals( r.getName( ), "AgentTcpClientFactory" ) ) {
//					r.update( res, old );
//				}
			} catch( Throwable e ) {
				logger.error( "update resource {} to listener {} exception", inspector.getId( res ), r.getName( ) );
				logger.error( "exception info : ", e );
			}
		}
	}

	protected void resourceDel( T res ) {
		Iterator<ResourceListener<T>> i = list.iterator( );
		while( i.hasNext( ) ) {
			ResourceListener<T> r = i.next( );
			try {
				if( r.contains( res ) ) {
					r.del( res );
				}
			} catch( Throwable e ) {
				logger.error( "delete resource {} from listener {} exception", inspector.getId( res ), r.getName( ) );
				logger.error( "exception info : ", e );
			}
		}
	}

	private synchronized boolean registInternal( ResourceListener<T> listener ) {
		Iterator<ResourceListener<T>> i = list.iterator( );
		while( i.hasNext( ) ) {
			ResourceListener<T> r = i.next( );
			if( r.equals( listener ) ) {
				logger.warn( "listener {} already registed", listener.getName( ) );
				return false;
			}
		}
		list.add( listener );
		logger.info( "listener {} registed", listener.getName( ) );
		return true;
	}
}
