package com.rainerregan.reganbot;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.Random;
import java.util.concurrent.ExecutionException;

@LineMessageHandler
@SpringBootApplication
public class ReganbotApplication extends SpringBootServletInitializer {

	JsonReader jsonReader;

	@Autowired
	private LineMessagingClient lineMessagingClient;

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ReganbotApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(ReganbotApplication.class, args);
	}

	@EventMapping
	public void handleTextEvent(MessageEvent<TextMessageContent> messageEvent){
		String pesan = messageEvent.getMessage().getText().toLowerCase();

		System.out.println(pesan);

		String uid = messageEvent.getSource().getUserId();
		String baseUrl = "http://api.brainshop.ai/get?bid=10463&key=HadqGciRQOLAW0XQ&uid="+ uid + "&msg=" + pesan;

		String replyToken = messageEvent.getReplyToken();
		String replyMessage = "ERROR";

		try {
			JSONObject replyResponse;
			replyResponse = jsonReader.readJsonFromUrl(baseUrl);

			replyMessage = replyResponse.getString("cnt");
			System.out.println(replyResponse);

		}catch (Exception e){
			System.out.println(e);
		}

		balasChatDenganJawaban(replyToken, replyMessage);

		/*
		String[] pesanSplit = pesan.split(" ");
		if(pesanSplit[0].equals("apakah")){
			String jawaban = getRandomJawaban();
			String replyToken = messageEvent.getReplyToken();
			balasChatDenganRandomJawaban(replyToken, jawaban + uid);
		}
		 */
	}

	/*
	private String getRandomJawaban(){
		String jawaban = "";
		int random = new Random().nextInt();
		if(random%2==0){
			jawaban = "Ya";
		} else{
			jawaban = "Nggak";
		}
		return jawaban;
	}
	 */
	private void balasChatDenganJawaban(String replyToken, String jawaban){
		TextMessage jawabanDalamBentukTextMessage = new TextMessage(jawaban);
		try {
			lineMessagingClient
					.replyMessage(new ReplyMessage(replyToken, jawabanDalamBentukTextMessage))
					.get();
		} catch (InterruptedException | ExecutionException e) {
			System.out.println("Ada error saat ingin membalas chat");
		}
	}
}
