package com.ejtone.mars.kernel.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

	public static String toJsonString( Object o ) {
		return JSON.toJSONString( o, SerializerFeature.IgnoreNonFieldGetter );
	}

	public static <T> T fromJsonString( String text, Class<T> clazz ) {
		return JSON.parseObject( text, clazz );
	}

	@SuppressWarnings( "unchecked" )
	public static <T> List<T> fromJsonStringToArray( String text, Class<T> clazz ) {
		JSONArray array = JSON.parseArray( text );
		final List<T> list = new ArrayList<>( );
		for( int i = 0; i < array.size( ); i++ ) {
			Object value = array.get( i );
			if( value instanceof JSONObject ) {
				list.add( fromJsonString( ( (JSONObject) value ).toJSONString( ), clazz ) );
			} else {
				list.add( (T) value );
			}
		}
		return list;
	}

}
