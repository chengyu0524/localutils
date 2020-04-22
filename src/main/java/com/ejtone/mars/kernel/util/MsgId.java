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

import com.ejtone.mars.kernel.util.config.ConfigUtils;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消息标识.
 * 使用前请先为nodeId赋值
 * 
 * 采用64位（8字节）的整数：<br>
 * 
 * 时间（格式为MMDDHHMMSS，即月日时分秒）：bit64~bit39，其中<br>
 * <li>bit64~bit61：月份的二进制表示；</li>
 * <li>bit60~bit56：日的二进制表示；</li>
 * <li>bit55~bit51：小时的二进制表示；</li>
 * <li>
 * bit50~bit45：分的二进制表示；</li>
 * <li>bit44~bit39：秒的二进制表示；</li> 节点代码：bit38~bit17，把节点的代码转换为整数填写到该字段中<br>
 * 序列号：bit16~bit1，顺序增加，步长为1，循环使用。<br>
 * 各部分如不能填满，左补零，右对齐。
 * 
 * @author cuihq
 *
 */
public final class MsgId {

	private int nodeId = ConfigUtils.getInt( "mars_id", 1 ) + ConfigUtils.getInt( "mars_id_offset", 0 );
	private AtomicInteger seq = new AtomicInteger( );

	public static final MsgId getInstance( ) {
		return InstanceHolder.instance;
	}

	private MsgId( ) {

	}

	public void setNodeId( int nodeId ) {
		this.nodeId = nodeId;
	}

	/**
	 * 新生成一个long型的msgId.
	 * 
	 * @return
	 */
	public long newMsgId( ) {
		return buildMsgId( );
	}

	/**
	 * 生成一个字符串形式的msgId.
	 * 
	 * @return
	 */
	public String newStringMsgId( ) {
		return buildStringMsgId( );
	}
	
	private static long startTime = DateTimeUtil.parseDate( "20160501", "yyyyMMdd" ).getTime( ); // 以16年5月1日做为系统的起点

	/**
	 * 将22位msgid格式化为18位.
	 * <p>
	 * 22位规则：MMddHHmmss(10位)+7位marsId+5位序列号<br>
	 * 18位规则如下：<br>
	 * 将10位时间转换为8位，转换规则为将时间转换为相应的秒，减去基本值（20160501），得到一个最大8位的long（1157天，比原msgid一年不重复多些）<br>
	 * 将7位marsId转换为5位，去除头两位。marsId的配置最大就是5位，不会产生影响<br>
	 * 序列号不变<br>
	 * 
	 * 
	 * @param msgId
	 * @return
	 */
	public static String format18( String msgId ) {

		if( msgId.length( ) != 22 ) {
			throw new IllegalArgumentException( "msgId长度有误" );
		}

		Calendar now = Calendar.getInstance( ); // 现在的时间
		Calendar submitDate = Calendar.getInstance( ); // submit的时间
		submitDate.setTime( DateTimeUtil.parseDate( msgId.substring( 0, 10 ), "MMddHHmmss" ) );
		submitDate.set( Calendar.YEAR, now.get( Calendar.YEAR ) );

		if( submitDate.getTimeInMillis( ) > now.getTimeInMillis( ) ) {// 跨年了，倒回去
			submitDate.set( Calendar.YEAR, now.get( Calendar.YEAR ) - 1 );
		}

		long time = ( ( submitDate.getTimeInMillis( ) - startTime ) / 1000 ) % 99999999L;
		String s1 = String.format( "%08d", time );
		String s2 = msgId.substring( 12 );

		return s1 + s2;

	}

	/**
	 * 将long型的msgId转换为string形式.
	 * 
	 * @param msgId
	 * @return
	 */
	public static final String format( long msgId ) {

		long mm = msgId, dd = msgId, HH = msgId, MM = msgId, SS = msgId, gw = msgId, sq = msgId;

		mm >>>= 60;
		dd = dd & 0x0F80000000000000l;
		dd >>>= 55;
		HH = HH & 0x007C000000000000l;
		HH >>>= 50;
		MM = MM & 0x0003F00000000000l;
		MM >>>= 44;
		SS = SS & 0x00000FC000000000l;
		SS >>>= 38;
		gw = gw & 0x0000003FFFFF0000l;
		gw >>>= 16;
		sq = sq & 0x000000000000FFFFl;

		return String.format( "%1$02d%2$02d%3$02d%4$02d%5$02d%6$07d%7$05d", mm, dd, HH, MM, SS, gw, sq );

	}

	/**
	 * 将string形式的msgId转换为long型.
	 * 
	 * @param msgId
	 * @return
	 */
	public static final long format( String msgId ) {
		if( msgId == null || !msgId.matches( "\\d{22}" ) ) {
			return 0;
		}

		long mm = Long.parseLong( msgId.substring( 0, 2 ) );
		long dd = Long.parseLong( msgId.substring( 2, 4 ) );
		long HH = Long.parseLong( msgId.substring( 4, 6 ) );
		long MM = Long.parseLong( msgId.substring( 6, 8 ) );
		long SS = Long.parseLong( msgId.substring( 8, 10 ) );
		long gw = Long.parseLong( msgId.substring( 10, 17 ) );
		long sq = Long.parseLong( msgId.substring( 17, 22 ) );

		return mm << 60 | dd << 55 | HH << 50 | MM << 44 | SS << 38 | gw << 16 | sq;
	}

	private long buildMsgId( ) {
		Calendar c = Calendar.getInstance( );
		long month = c.get( 2 ) + 1;
		long day = c.get( 5 );
		long hour = c.get( 11 );
		long minute = c.get( 12 );
		long second = c.get( 13 );
		return month << 60 | day << 55 | hour << 50 | minute << 44 | second << 38 | nodeId << 16 | ( ( seq.incrementAndGet( ) % 65535 ) & 0xffff );

	}

	private String buildStringMsgId( ) {
		Calendar c = Calendar.getInstance( );
		long month = c.get( 2 ) + 1;
		long day = c.get( 5 );
		long hour = c.get( 11 );
		long minute = c.get( 12 );
		long second = c.get( 13 );
		return String.format( "%1$02d%2$02d%3$02d%4$02d%5$02d%6$07d%7$05d", month, day, hour, minute, second, nodeId,
				( ( seq.incrementAndGet( ) % 65535 ) & 0xffff ) );
	}


	private static class InstanceHolder {
		public static final MsgId instance = new MsgId( );
	}

	public static void main( String[] args ) {

//		byte[] a = { (byte) 0xc1, 0x2e, (byte) 0xc7, (byte) 0xc0, 0x27, 0x17, 0x00, 0x3f };
//
//		ByteBuffer b = ByteBuffer.wrap( a );
//		
//		long msgid = b.getLong();
////		long msgid = Long.parseLong( "c12ec7c02717003f", 16 );
//		// long msgid = Long.parseLong( "1ce27c0c727100f3",16);[]
//		String s = MsgId.format( msgid );
//		System.out.println( s );
//
//		// long msgid;
//		// for( int i = 0; i < 1; i++ ) {
//		// System.out.println( msgid = MsgId.getInstance( ).newMsgId( ) );
//		// System.out.println( MsgId.format( msgid ) );
//		// System.out.println( MsgId.format( MsgId.format( msgid ) ) );
//		// }
		
		
		System.out.println( "20170221000907364".substring( 0, 14 ) );


	}
}
