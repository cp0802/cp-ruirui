package com.adanxing.ad.device.utils;

import com.adanxing.ad.device.enums.DeviceExtEnum;
import com.adanxing.ad.user.utils.ByteUtils;

public class DeviceExtUtils {

    public static DeviceExtEnum convertDeviceExtType(byte[] key) {
        return DeviceExtEnum.getByCode(ByteUtils.byteToInt(key, 0));
    }



}
