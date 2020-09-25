package com.example.scheduler;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.example.Util.DateLoader;
import com.example.dto.DistrictVO;
import com.example.service.weatherService;

public class SchedulerFcW {

	private static final Logger logger = LoggerFactory.getLogger(SchedulerFcW.class);
	
	@Inject
	private weatherService service;

	@Scheduled(cron = "0 45 0/1 ?* * * ")  //현재날씨
	public void scheduleNW() throws Exception{

		String now = new DateLoader().DateLader();
		String now_day = now.substring(0, 8);
		String now_time = now.substring(8);
		
		List<DistrictVO> areaList = service.selectarea(); 
		logger.debug("현재 날씨 요청 전 확인 -> 현재 날짜 = " + now_day + "현재 시작 시간 = " + now_time);
		
		service.insertnw_scheduler(now,areaList);
		
		now = new DateLoader().DateLader();
		now_time = now.substring(8);
		logger.debug("★★ ★ ★ ★  현재 날씨 요청 끝 -> 현재 날짜 = " + now_day + "현재 요청끝난 시간 = " + now_time);

	}
	
	@Scheduled(cron = "0 10 0/3 ?* * * ")  //예보날씨
	public void scheduleFW() throws Exception{

		logger.info("scheduler시작 = " );
		String now = new DateLoader().DateLader();
		String now_day = now.substring(0, 8);
		String now_time = now.substring(8);
		logger.debug("현재 날짜 = " + now_day+"현재 시간 = " + now_time);
		List<DistrictVO> areaList = service.selectarea(); 
		service.insertfw(now, areaList);
		logger.info("★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★ ★★★★★★★★★★★★★★★★★★★");

	}
}
