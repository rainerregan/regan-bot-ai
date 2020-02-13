package com.rainerregan.reganbot;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
		String pesan 		= messageEvent.getMessage().getText().toLowerCase();
		String[] pesanSplit = pesan.split("");

		//System.out.println(pesan);

		String uid = messageEvent.getSource().getUserId();

		String replyToken = messageEvent.getReplyToken();
		String replyMessage = "Sorry, I don't understand you.";

		if (pesan == "!commands"){
			String reply = "COMMANDS:\n" +
					"1. use '!' for english\n" +
					"2. use '-' for indonesian\n";

			balasChatDenganJawaban(replyToken, reply);
		}
		else if(pesanSplit[0].equals("!")) {

			try {

				String baseUrl = "http://api.brainshop.ai/get?bid=10463&key=HadqGciRQOLAW0XQ&uid=" + uid + "&msg=" + URLEncoder.encode(pesan, "UTF-8");
				JSONObject replyResponse;
				replyResponse = jsonReader.readJsonFromUrl(baseUrl);

				replyMessage = replyResponse.getString("cnt");
				System.out.println(replyResponse);

			} catch (Exception e) {
				System.out.println(e);
			}

			balasChatDenganJawaban(replyToken, replyMessage);
		}
		else if(pesanSplit[0].equals("-")){
			try {
				String idToEn = "id|en";
				String enToId = "en|id";

				String translationUrlIdEn = "https://api.mymemory.translated.net/get?q="+ URLEncoder.encode(pesan, "UTF-8") +"&langpair=";

				//MENTRANSLATE DARI ID KE EN
				JSONObject translatedIdToEnText;
				translatedIdToEnText = jsonReader.readJsonFromUrl(translationUrlIdEn+idToEn).getJSONObject("responseData");

				String pesanDalamEn = translatedIdToEnText.getString("translatedText");

				//test
				System.out.println(pesanDalamEn);

				String baseUrl = "http://api.brainshop.ai/get?bid=10463&key=HadqGciRQOLAW0XQ&uid=" + uid + "&msg=" + URLEncoder.encode(pesanDalamEn, "UTF-8");

				JSONObject replyResponse;
				replyResponse = jsonReader.readJsonFromUrl(baseUrl);

				//replyMessage = replyResponse.getString("cnt");
				System.out.println(replyResponse);

				String translationUrlEnId = "https://api.mymemory.translated.net/get?q="+ URLEncoder.encode(replyResponse.getString("cnt"), "UTF-8") +"&langpair=";

				//MENTRANSLATE KE ID KEMBALI
				JSONObject translatedEnToIdText;
				translatedEnToIdText = jsonReader.readJsonFromUrl(translationUrlEnId+enToId).getJSONObject("responseData");

				replyMessage = translatedEnToIdText.getString("translatedText");

			} catch (Exception e) {
				System.out.println(e);
			}
			balasChatDenganJawaban(replyToken, replyMessage);
		}
		else if (pesan.split(";")[0].equals("remind")){
			String textWaktu 	= pesan.split(";")[1];
			String catatan 		= pesan.split(";")[2];

			/*
			String[] waktuSplit = textWaktu.split("-");
			int jam 			= Integer.parseInt(waktuSplit[0]);
			int menit 			= Integer.parseInt(waktuSplit[1]);
			*/

			//pushMessageKeUser(uid, "TEST");

			String response 	= "ERROR";

			if (!textWaktu.equals("") && !catatan.equals("")) {

				//the Date and time at which you want to execute
				DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
				Date date = null;
				try {
					date = dateFormatter.parse(textWaktu);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				//Now create the time and schedule it
				Timer timer = new Timer();

				response = "Okay, i'll chat you at '" + textWaktu + "'" + "\nNotes : " + catatan + ".";

				String reminderResponse =  "Hey dear, dont forget your reminder : '" + catatan + "'.";

				timer.schedule(new PushMessageScheduled(uid, reminderResponse), date);

				balasChatDenganJawaban(replyToken,response);
			}else{
				response = "I'm sorry, make sure you have entered the right format : remind;hour;minute;notes.";
				balasChatDenganJawaban(replyToken, response);
			}
		}
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

	private class PushMessageScheduled extends TimerTask {

		String uid;
		String message;

		public PushMessageScheduled(String uid, String message) {
			this.uid = uid;
			this.message = message;
		}

		@Override
		public void run() {
			pushMessageKeUser(uid, message);
		}

		private void pushMessageKeUser(String uid, String message){
			TextMessage textMessage = new TextMessage(message);
			PushMessage pushMessage = new PushMessage(
					uid,
					textMessage
			);

			BotApiResponse botApiResponse;
			try {
				botApiResponse = lineMessagingClient.pushMessage(pushMessage).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				return;
			}

			System.out.println(botApiResponse);
		}
	}
}
