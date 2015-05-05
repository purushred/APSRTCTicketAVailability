package com.smart.apsrtcbus.restful;

import com.smart.apsrtcbus.vo.SpecialServiceVO;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

public interface SpecialService {

	@GET("/apsrtc/servicelist")
    List<SpecialServiceVO> getServiceList();

    @POST("/apsrtc/newservice")
    void addNewService(@Body SpecialServiceVO serviceVO, retrofit.Callback<SpecialServiceVO> callback);

    @POST("/apsrtc/updateservice")
    void updateService(@Body SpecialServiceVO serviceVO, retrofit.Callback<SpecialServiceVO> callback);

    @POST("/apsrtc/getservice")
    void getService(@Body Integer uId, retrofit.Callback<SpecialServiceVO> callback);
}