package com.example.demo;

import com.example.demo.parser.GpuParser;
import com.example.demo.persistance.GpuRecord;
import com.example.demo.persistance.GpuRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotOptions;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@Slf4j
public class DemoApplication {

	private List<GpuParser> gpuParsers;
	private final GpuRecordRepository gpuRecordRepository;
	private final Bot bot;

	@Autowired
	public DemoApplication(List<GpuParser> gpuParsers,
						   GpuRecordRepository gpuRecordRepository,
						   Bot bot) {
		this.gpuParsers = gpuParsers;
		this.gpuRecordRepository = gpuRecordRepository;
		this.bot = bot;
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public BotSession botSession(){
		BotSession botSession = null;
		try {
			TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
			botSession = telegramBotsApi.registerBot(this.bot);
		} catch (TelegramApiException e) {
			log.error("Can't open bot session, e:{}", e.getMessage());
		}
		return botSession;
	}

	@Scheduled(fixedRate = 90000)
	public void parse(){
		for (GpuParser gpuParser: gpuParsers){
			List<Gpu> gpuList = gpuParser.parse();
			String msg = "";
			for(Gpu gpu: gpuList){

				Optional<GpuRecord> gpuRecordOrigin = gpuRecordRepository.findByUrl(gpu.getUrl());

				if(gpuRecordOrigin.isEmpty()){
					String shortName = gpu.getName().replaceAll("Graphics Card.+", "");
					msg += "<a href=\"" + gpu.getUrl() + "\">" + shortName + "</a>\n" + gpu.getPrice() + "\n";
					GpuRecord gpuRecord = new GpuRecord();
					gpuRecord.setUrl(gpu.getUrl());
					gpuRecord.setPrice(gpu.getPrice());
					gpuRecordRepository.save(gpuRecord);
					log.info("Saved new gpu record: " + gpuRecord);
				}
			}

			if (!msg.isEmpty()){
				SendMessage sendMessage = new SendMessage(this.bot.CHAT_ID, msg);
				sendMessage.enableHtml(true);
				this.bot.sendMessage(sendMessage);
			} else {
				log.info("There are not new cards.");
			}
		}
	}

}
