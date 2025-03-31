package com.adanxing.ad.user.daock1;


import com.adanxing.ad.user.model.DeviceInstallAppModel;

import java.util.List;
import java.util.Map;

public interface DeviceInstallAppInfoDao {

    int insertDeviceInstallAppInfo(Map<String,Object> paramMap);

    List<DeviceInstallAppModel> pageDeviceInstallAppInfo(Map<String,Object> paramMap);

    int deleteDeviceInstallAppInfo();



}
