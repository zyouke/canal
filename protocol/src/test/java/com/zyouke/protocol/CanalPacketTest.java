package com.zyouke.protocol;

import com.alibaba.otter.canal.protocol.CanalPacket;
import com.google.protobuf.ByteString;

public class CanalPacketTest {

    private static CanalPacket.Handshake createCanalPacket(){
        CanalPacket.Handshake.Builder builder = CanalPacket.Handshake.newBuilder();
        builder.setCommunicationEncoding("GBk");
        builder.setSeeds(ByteString.copyFromUtf8("3"));
        builder.addSupportedCompressions(CanalPacket.Compression.GZIP);
        return builder.build();
    }

    /**
     * 编码
     * @param handshake
     * @return
     */
    private static byte[] encode(CanalPacket.Handshake handshake){
        return handshake.toByteArray();
    }

    /**
     * 解码
     * @return
     */
    private static CanalPacket.Handshake dencode(byte[] bytes) throws Exception{
        return CanalPacket.Handshake.parseFrom(bytes);
    }

    public static void main(String[] args) throws Exception{
        long s = System.currentTimeMillis();
        CanalPacket.Handshake handshake = createCanalPacket();
        byte[] encodeBytes = encode(handshake);
        CanalPacket.Handshake handshakeToDencode = dencode(encodeBytes);
        System.out.println("--------------" + handshakeToDencode.getCommunicationEncoding());
        System.out.println("--------------" + handshakeToDencode.getSeeds().toString("UTF-8"));
        System.out.println("--------------" + handshakeToDencode.getSupportedCompressions(0).toString());
        System.out.println(System.currentTimeMillis() - s);
    }

}
