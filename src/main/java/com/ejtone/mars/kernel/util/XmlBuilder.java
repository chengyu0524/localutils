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
package com.ejtone.mars.kernel.util;

import org.apache.commons.lang3.StringEscapeUtils;


/**
 * 简单的xml拼装工具类.
 * 
 * @author cuihq
 *
 */
public class XmlBuilder {
	private boolean withDeclaration;
	private boolean withCharset;
	private String charset;
	private boolean useFullEmptyTag;
	private String root;
	private StringBuilder sb = new StringBuilder( );

	XmlBuilder( boolean withDeclaration, boolean withCharset, String charset, boolean useFullEmptyTag, String root ) {
		this.withDeclaration = withDeclaration;
		this.withCharset = withCharset;
		this.charset = charset;
		this.useFullEmptyTag = useFullEmptyTag;
		this.root = root;
	}

	public XmlBuilder startDocument( ) {

		if( withDeclaration ) {
			if( withCharset ) {
				sb.append( "<?xml version=\"1.0\" encoding=\"" ).append( charset ).append( "\" ?>" );
			} else {
				sb.append( "<?xml version=\"1.0\" ?>" );
			}
		}
		sb.append( "<" ).append( root ).append( ">" );
		return this;
	}

	public XmlBuilder endDocument( ) {
		sb.append( "</" ).append( root ).append( ">" );
		return this;
	}


	public XmlBuilder startTag( String tag ) {
		sb.append( "<" ).append( tag ).append( ">" );
		return this;
	}

	public XmlBuilder endTag( String tag ) {
		sb.append( "</" ).append( tag ).append( ">" );
		return this;
	}

	/**
	 * 如果value中可能存在xml需要转义的字符，使用该方法添加tag.
	 * 
	 * @param tag
	 * @param value
	 * @return
	 */
	public XmlBuilder addTagSafely( String tag, String value ) {
		if( value == null ) {
			return addTag( tag, value );
		} else {
			return addTag( tag, StringEscapeUtils.escapeXml( value ) );
		}
	}

	public String getCharset( ) {
		return charset;
	}

	public String toXmlString( ) {
		return sb.toString( );
	}

	public byte[] toXmlByteArray( ) {
		return CharsetUtil.getBytesUnchecked( sb.toString( ), charset );
	}

	/**
	 * 如果value中确定不会存在xml需要转义的字符，使用该方法添加tag.
	 * 
	 * @param tag
	 * @param value
	 * @return
	 */
	public XmlBuilder addTag( String tag, String value ) {
		if( value == null ) {
			if( useFullEmptyTag ) {
				sb.append( "<" ).append( tag ).append( ">" );
				sb.append( "</" ).append( tag ).append( ">" );
			} else {
				sb.append( "<" ).append( tag ).append( " />" );
			}
			return this;
		}
		sb.append( "<" ).append( tag ).append( ">" );
		sb.append( value );
		sb.append( "</" ).append( tag ).append( ">" );
		return this;
	}

	// private void ifNewLine() {
	// if (this.useNewLine) {
	// sb.append("\n");
	// }
	// }

	public static class Builder {
		private boolean withDeclaration = true;
		private boolean withCharset = true;
		private String charset = CharsetUtil.UTF_8;
		private boolean useFullEmptyTag = true; // true时使用<a></a> //false时使用<a/>形式
		private String root = "root";

		private Builder( ) {

		}

		public static Builder custom( ) {
			return new Builder( );
		}

		public XmlBuilder build( ) {
			return new XmlBuilder( withDeclaration, withCharset, charset, useFullEmptyTag, root );
		}

		public Builder setWithDeclaration( boolean withDeclaration ) {
			this.withDeclaration = withDeclaration;
			return this;
		}

		public Builder setWithCharset( boolean withCharset ) {
			this.withCharset = withCharset;
			return this;
		}

		public Builder setCharset( String charset ) {
			this.charset = charset;
			return this;
		}

		public Builder setUseFullEmptyTag( boolean useFullEmptyTag ) {
			this.useFullEmptyTag = useFullEmptyTag;
			return this;
		}

		public Builder setRoot( String name ) {
			this.root = name;
			return this;
		}
	}
}
