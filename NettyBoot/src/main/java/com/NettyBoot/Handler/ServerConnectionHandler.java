package com.NettyBoot.Handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.NettyBoot.Common.IniFile;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;


public class ServerConnectionHandler extends ChannelInboundHandlerAdapter {
	
	/** 
	 * 2020-09-23 jslee
	 * 클라이언트에서 동적으로 message를 전송하여야 하므로 변수선언 후 getter / setter 함수선언
	 * */
	
	/** Logger */
	private Logger logger = LogManager.getLogger(ServerConnectionHandler.class);
	
	/** 파일감지 후 서버에 보낼 전문변수 설정, DirWatchRunnable.java에서 대상파일 필터링 후 값 전달한다 jslee */
	private ByteBuf message;
	
	private String msg;
	
	boolean sendstat = false;
	
	int i = 1;
	
	int clientId = 0;
	
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public ByteBuf getMessage() {
		return message;
	}

	public void setMessage(ByteBuf message) {
		this.message = message;
	}

	public void setClientId(int id) {
		// TODO Auto-generated method stub
		this.clientId = id;
	}
	
	/**
     * SyncClient.java 에서 서버로의 연결이 만들어지면 channelActive 메소드가 호출 된다. jslee
     */
	@Override
    public void channelActive(final ChannelHandlerContext ctx) { // (1)
		
		//logger.info("server connections Success!!");	
		/**
		 * 2020-09-25 
		 * jslee
		 * 
		 * 서버가 연결되면, 감지된 전문 메시지를 서버에 전송한다.
		 * */
		//ctx.writeAndFlush(message);
		
		sendMessage(ctx, this.msg);
		i++;
    }
	
	/** Server 접속 종료시 실행됨 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.close();
	}
       
	/** Server 에서 패킷 데이터를 수신 받았을 때 실행된다. jslee */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		
		/** 서버로부터 수신받은 전문 메시지를 ByteBuf형태로 변환한다. jslee */
		//ByteBuf in = (ByteBuf) msg;
		
		/** 로그찍기용 메시지 형 변환 jslee */
		//String message = in.toString(Charset.forName("utf-8"));
		
	    try {
	    	
			//ctx.writeAndFlush(MessageToByteBuf(message, ctx));
	    } finally {
	        ReferenceCountUtil.release(msg); // (2)
	        //logger.info("서버 Listener 종료 요청");
	    }
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		
		sendstat = true;
		
		IniFile ini = IniFile.getInstance();
		
		int sendCnt = Integer.parseInt(ini.getIni("Client", "SEND_CNT"));
		
		if(i <= sendCnt) {
			sendMessage(ctx, this.msg);
			i++;
		} else {
			System.out.println("[" + clientId + "] Send Complete");
			ctx.close();
		}
	}
	
	public void sendMessage(ChannelHandlerContext ctx, String msg) {

		ByteBuf bbmsg;
		clientLog("S", msg);
		byte[] str = new byte[msg.length()];
		
		// 예제로 사용할 바이트 배열을 만듭니다.
		str =  msg.getBytes();
		
		bbmsg = Unpooled.wrappedBuffer(str, 0, str.length);
		
		// 메시지를 쓴 후 플러쉬합니다.
		ctx.writeAndFlush(bbmsg);
    }
	
	public void clientLog(String type, String msg) {

		if(type.equals("R")) {
			
			logger.info("[" + this.clientId + "][RECV] :: " + msg);			
		} else if(type.equals("S")) {
			
			logger.info("[" + this.clientId + "][SEND] :: " + msg);
		} else {
			
			logger.info("[" + this.clientId + "] :: " + msg);
		}
	}
}
