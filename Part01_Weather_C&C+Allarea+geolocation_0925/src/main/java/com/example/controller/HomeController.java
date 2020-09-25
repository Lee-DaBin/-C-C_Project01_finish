package com.example.controller;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.Util.DateLoader;
import com.example.dto.DistrictVO;
import com.example.dto.FcastWeatherVO;
import com.example.dto.NowWeatherVO;
import com.example.service.MainpageSErviceImpl;
import com.example.service.MainpageService;
import com.example.service.areaService;
import com.example.service.areaServiceImpl;
import com.example.service.weatherService;

@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Inject
	private weatherService service;
	private MainpageService mainService;
	private areaService areaService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) throws Exception {

		logger.info("home");
		logger.info("Welcome home! The client locale is {}.");

		String now = new DateLoader().DateLader(); // 현재 날짜 시간
		logger.info("현재시간" + now);

		// 메인화면 구성
		logger.info("=== 1) 메인화면 구성");
		mainService = new MainpageSErviceImpl();
		mainService.execute(model, now);

		return "home";
	}

	// 현재 지역 현재날씨 ajax
	@RequestMapping(value = "/nowWeather", method = RequestMethod.POST, produces = "application/text; charset=utf8") // 한글인코딩해서넘기기
	public @ResponseBody String ajax_nowWeather(@RequestBody String ID, Model model) throws Exception {

		List<DistrictVO> areaList = service.selectarea(); // 지역테이블 준비
		String now = new DateLoader().DateLader();
		String areaID = ID.replaceAll("[^0-9]", ""); // 숫자만가져오기

		areaID = areaID.substring(0, 5); // 시도로 사용할꺼니까 앞에 4자리만 필요 , 광역시면 2자리
		String district_ID = areaID + "00000";
		((org.slf4j.Logger) logger).debug("=== 1) 현재 받아온 지역 ID = " + district_ID);

		// 현재날씨 요청 및 출력
		logger.info("=== 2) 현재날씨 시작");
		service.insertnw(now, district_ID, areaList);
		List<NowWeatherVO> nwList = service.selectnw(now, district_ID);
		String nowWeather = service.NWjsonObject_view(nwList, areaList, now);

		return nowWeather;

	}

	// 현재 지역 예보날씨 ajax
	@RequestMapping(value = "/now_area_fcWeatherList", method = RequestMethod.POST, produces = "application/text; charset=utf8") // 한글인코딩해서넘기기
	public @ResponseBody String now_area_fcWeatherList(@RequestBody String ID, Model model) throws Exception {

		List<DistrictVO> areaList = service.selectarea(); // 지역테이블 준비

		String now = new DateLoader().DateLader();
		String nowday = now.substring(0, 8);
		String areaID = ID.replaceAll("[^0-9]", ""); // 숫자만가져오기

		areaID = areaID.substring(0, 5); // 시도로 사용할꺼니까 앞에 4자리만 필요 , 광역시면 2자리
		String district_ID = areaID + "00000";
		((org.slf4j.Logger) logger).debug("=== 1) nowarea_fcWeatherList 현재 받아온 지역 ID = " + district_ID);

		// 선택한 지역이름,예보날씨 보내기
		List<FcastWeatherVO> fcDtos = service.selectfw(nowday, areaList, district_ID); // 지역테이블 준비
		String Sfw = service.FWjsonObject_view(fcDtos, areaList, now);
		((org.slf4j.Logger) logger).debug("nowarea_fcWeatherList 자바스크립트 보낼꺼 확인" + Sfw);
		return Sfw;
	}

	// 선택한 지역 중심
	@RequestMapping(value = "/area_centroid", method = RequestMethod.POST, produces = "application/text; charset=utf8") // 한글인코딩해서넘기기
	public @ResponseBody String area_centroid(@RequestBody String area_code) throws Exception {

		List<DistrictVO> areaList = service.selectarea(); // 지역테이블 준비
		String area_code_step01 = area_code.replaceAll("[^0-9]", "") + "00000000";
		((org.slf4j.Logger) logger).debug("=== 1) 현재 받아온 지역코드 = " + area_code_step01);

		// 선택지역 X,Y 찾기
		areaService = new areaServiceImpl();
		String area_centroid = areaService.select_centroid(area_code_step01, areaList);

		return area_centroid;
	}
	
	//전국날시 버튼 클릭시 광역시도별 현재날씨 요청및 가져오기
		@RequestMapping(value = "/step01_nw_weather", method = RequestMethod.POST, produces = "application/text; charset=utf8") // 한글인코딩해서넘기기
		public @ResponseBody String select_nw_step01(@RequestBody String text) throws Exception {
			((org.slf4j.Logger) logger).debug("=== 1 광역시도별 현재날씨 요청) "+text);
			List<DistrictVO> areaList = service.selectarea(); // 지역테이블 준비
			String now = new DateLoader().DateLader();
			List<NowWeatherVO> nwList = service.select_nw_step01(now);
			
			JSONObject step01NWObject = new JSONObject();// 최종 완성될 JSonObject
			JSONArray areaArray = new JSONArray(); // 지역이름 json정보 array
			
			String areaname_01;
			for (int a = 0; a < areaList.size(); a++) {
					
				for (int i = 0; i < nwList.size(); i++) {

					if (nwList.get(i).getDistrict_ID().equals(areaList.get(a).getDistrict_ID())) {
						areaname_01 = areaList.get(a).getDistrict_name_step1();

						logger.info("지역명/" + areaname_01);
						logger.info("===========================" + areaname_01 + "===========================");

						JSONObject areaNWInfo = new JSONObject(); // 지역정보및 현재날씨 정보가 들어갈 Jsonobject

						areaArray = new JSONArray(); // 지역이름 json정보 array
						areaNWInfo.put("district_ID", nwList.get(i).getDistrict_ID());
						areaNWInfo.put("nowTime", nwList.get(i).getNowTime());
						areaNWInfo.put("nowDate", nwList.get(i).getNowDate());
						areaNWInfo.put("district_name_step1", areaname_01);
						areaNWInfo.put("NTH", nwList.get(i).getNTH());
						areaNWInfo.put("SKY", nwList.get(i).getSKY());
						areaNWInfo.put("X", areaList.get(a).getX());
						areaNWInfo.put("Y", areaList.get(a).getY());
						logger.info("확인 areaArray리스트 들어가기전에 확인 ==== " + areaNWInfo);
						areaArray.add(areaNWInfo);
						logger.info("확인 FWjsonObject 들어가기전에 확인 ==== " + areaArray);

						step01NWObject.put(areaname_01, areaArray);
						logger.info("FWjsonObject에  잘 들어갔는지 확인 ==== " + areaArray);
					}
					
					
				}
				
			}
			
			String step01NW = step01NWObject.toJSONString();
			
			return step01NW;

	}
		
	// 선택한 지역 현재날씨
	@RequestMapping(value = "/select_area_NowWeather", method = RequestMethod.POST, produces = "application/text; charset=utf8") // 한글인코딩해서넘기기
	public @ResponseBody String select_area_NowWeather(@RequestBody String area_code) throws Exception {

		List<DistrictVO> areaList = service.selectarea(); // 지역테이블 준비
		String area_code_step01 = area_code.replaceAll("[^0-9]", "");
		logger.debug("=== 1) 현재 받아온 지역광역시도코드 = " + area_code_step01);

		// 선택한 지역별 광역시도 지역리스트
		List<DistrictVO> select_step01_area = areaService.select_step01_area(area_code_step01, areaList); // 지역테이블 준비

		// 광역시도별 지역리스트 현재날씨 요청
		String now = new DateLoader().DateLader();

		List<NowWeatherVO> select_area_nwList = service.select_step01_nw(now, area_code_step01);

		String area_nwList = service.NWareajsonObject_view(select_area_nwList, select_step01_area, now);
		logger.debug("광역시도별 자바스크립트 보낼꺼 확인" + area_nwList);

		return area_nwList;
	}

	// 선택한 지역 예보날씨 default
	@RequestMapping(value = "/select_area_fcWeatherList", method = RequestMethod.POST, produces = "application/text; charset=utf8") // 한글인코딩해서넘기기
	public @ResponseBody String select_area_fcWeatherList(@RequestBody String ID, Model model) throws Exception {

		List<DistrictVO> areaList = service.selectarea(); // 지역테이블 준비

		String now = new DateLoader().DateLader();
		String nowday = now.substring(0, 8);
		String areaID = ID.replaceAll("[^0-9]", ""); // 숫자만가져오기
		String district_ID = areaID + "00000000";
		logger.debug("=== 1) nowarea_fcWeatherList 현재 받아온 지역 ID = " + district_ID);

		// 선택한 지역이름,예보날씨 보내기
		List<FcastWeatherVO> fcDtos = service.selectfw(nowday, areaList, district_ID); // 지역테이블 준비
		String Sfw = service.FWjsonObject_view(fcDtos, areaList, now);
		logger.debug("nowarea_fcWeatherList 자바스크립트 보낼꺼 확인" + Sfw);
		return Sfw;
	}

	// 선택한 지역 예보날씨
	@RequestMapping(value = "/select_area_fcWeatherList_step02", method = RequestMethod.POST, produces = "application/text; charset=utf8") // 한글인코딩해서넘기기
	public @ResponseBody String select_area_fcWeatherList_step02(@RequestBody String ID, Model model) throws Exception {

		List<DistrictVO> areaList = service.selectarea(); // 지역테이블 준비

		String now = new DateLoader().DateLader();
		String nowday = now.substring(0, 8);
		String areaID = ID.replaceAll("[^0-9]", ""); // 숫자만가져오기
		logger.debug("=== 1) nowarea_fcWeatherList 현재 받아온 지역 ID = " + areaID);

		// 선택한 지역이름,예보날씨 보내기
		List<FcastWeatherVO> fcDtos = service.selectfw(nowday, areaList, areaID); // 지역테이블 준비
		String Sfw = service.FWjsonObject_view(fcDtos, areaList, now);
		logger.debug("nowarea_fcWeatherList 자바스크립트 보낼꺼 확인" + Sfw);
		return Sfw;
	}

}
