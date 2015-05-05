package com.smart.apsrtcbus.utilities;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smart.apsrtcbus.vo.SearchResultVO;
import com.smart.apsrtcbus.vo.StationVO;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class AppUtils {

    public static final String SITE_URL = "http://apsrtconline.in/oprs-web/";

    public static final String STATION_INFO_URL = "http://192.168.0.4:8080/apsrtc/";

//    public static final String STATION_INFO_URL = "http://apsrtc-reddy.rhcloud.com/";

    // searchType=0 for onward and 1 for return journey
    public static final String SEARCH_URL = SITE_URL + "forward/booking/avail/services.do?adultMale=1&childMale=0&";

    public static final String MAIN_SEARCH_URL = SITE_URL + "avail/services.do?";

    /**
     * To check whether internet is enabled.
     *
     * @param cm - Connectivity Manager class
     * @return true: network connected. false: network not connected.
     */
    public static boolean isNetworkOnline(ConnectivityManager cm) {
        boolean status = false;
        try {
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
                    status = true;
            }
        } catch (Exception e) {
            Log.e("Network Error", e.getMessage());
            return false;
        }
        return status;
    }

    public static List<StationVO> getBusStationList(String data) {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<List<StationVO>>() {
        }.getType();
        List<StationVO> list = gson.fromJson(data, collectionType);
        return list;
    }

    public static ArrayList<SearchResultVO> parseHTMLUsingJSoup(String response) {

        ArrayList<SearchResultVO> resultVOList = new ArrayList<>();

        Document doc = org.jsoup.Jsoup.parse(response);
        Elements rows = doc.select(".result-grid").select(".rSetForward");

        for (Element row : rows) {
            SearchResultVO resultVO = new SearchResultVO();
            resultVO.setServiceNo(row.select(".srvceNO").text());
            resultVO.setServiceName(row.select(".col1 > p ").text());
            resultVO.setDeparture(row.select(".StrtTm").text());
            resultVO.setArrival(row.select(".ArvTm").text());
            resultVO.setType(row.select(".col3-left > span").text().split(Pattern.quote("("))[0]);
            // resultVO.setViaPlace(row.select(".col3-left > p").text().split(":")[1]);
            resultVO.setDepotName(row.select(".col3-right").text().split(":")[1]);
            resultVO.setAdultFare(row.select(".TickRate").text());
            //resultVO.setAdultFare(row.select(".TickRate").text());
            resultVO.setAvailableSeats(row.select(".availCs").text().trim().split(" ")[0].trim());

            resultVOList.add(resultVO);
        }
        return resultVOList;
    }

    /**
     * This method is to increment/decrement a date by 1 day.
     *
     * @param operation - -1 for decrement and 1 for increment.
     * @param dateStr   : Input date string which is to be incremented/decremented.
     * @return New date string will be returned.
     */
    public static String getNewDate(int operation, String dateStr) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        try {
            Date date = formatter.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            switch (operation) {
                case -1:
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    break;
                case 1:
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    break;
            }

            Date newDate = cal.getTime();
            return formatter.format(newDate);

        } catch (ParseException e) {
            Log.e("Error", e.getMessage());
        }
        return null;
    }

    public static String getFormattedDate(String dateStr) {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        Date date = null;
        try {
            date = format.parse(dateStr);
        } catch (ParseException e) {
            Log.e("Error", e.getLocalizedMessage());
        }

        format.applyPattern("EEE dd MMM, yyyy");
        return format.format(date);
    }
}