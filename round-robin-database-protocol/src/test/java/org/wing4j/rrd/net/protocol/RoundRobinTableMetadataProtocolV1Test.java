package org.wing4j.rrd.net.protocol;

import org.junit.Assert;
import org.junit.Test;
import org.wing4j.rrd.core.TableStatus;

import java.nio.ByteBuffer;

/**
 * Created by wing4j on 2017/8/4.
 */
public class RoundRobinTableMetadataProtocolV1Test {

    @Test
    public void testConvert() throws Exception {
        RoundRobinTableMetadataProtocolV1 format = new RoundRobinTableMetadataProtocolV1();
        format.setMessageType(MessageType.RESPONSE);
        format.setTableName("table1");
        format.setColumns(new String[]{"request", "response"});
        format.setFileName("D://table1.rrd");
        format.setDataSize(100);
        format.setStatus(TableStatus.NORMAL);
        format.setCode(RspCode.FAIL.getCode());
        format.setDesc(RspCode.FAIL.getDesc());
        ByteBuffer buffer = format.convert();
//        System.out.println(HexUtils.toDisplayString(buffer.array()));
        buffer.flip();
        int size = buffer.getInt();
        int type = buffer.getInt();
        int version = buffer.getInt();
        int messageType = buffer.getInt();
        Assert.assertEquals(MessageType.RESPONSE.getCode(), messageType);
        RoundRobinTableMetadataProtocolV1 format2 = new RoundRobinTableMetadataProtocolV1();
        format2.convert(buffer);
        Assert.assertEquals(RspCode.FAIL.getCode(), format2.getCode());
        Assert.assertEquals(RspCode.FAIL.getDesc(), format2.getDesc());
        Assert.assertEquals("table1", format2.getTableName());
        Assert.assertEquals("request", format2.getColumns()[0]);
        Assert.assertEquals("response", format2.getColumns()[1]);
        Assert.assertEquals(100, format2.getDataSize());
        Assert.assertEquals(TableStatus.NORMAL, format2.getStatus());
    }
}