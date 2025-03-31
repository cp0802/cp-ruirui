package com.adanxing.ad.device.utils;

import com.adanxing.ad.api.Enum.DeviceIdTypeEnum;
import com.adanxing.ad.api.util.MD5Utils;
import com.adanxing.ad.device.enums.DeviceExtEnum;
import com.adanxing.ad.user.utils.ByteUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class DeviceExtUtils {

    private static final String DEVICE_EXT = "ui_%s_%s";

    public static DeviceExtEnum convertDeviceExtType(byte[] key) {
        return DeviceExtEnum.getByCode(ByteUtils.byteToInt(key, 0));
    }


    public static String getDeviceExtUserKey(String did, int didType) {
        if (Objects.isNull(didType)) {
            return String.format(DEVICE_EXT, 0, did);
        }
        switch (DeviceIdTypeEnum.getByCode(didType)) {
            case IMEI:
                did = MD5Utils.md5Encrypt32Lower(did);
                didType = DeviceIdTypeEnum.IMEI_MD5.getCode();
                break;
            case IMEI_MD5:
                did = StringUtils.lowerCase(did);
                didType = DeviceIdTypeEnum.IMEI_MD5.getCode();
                break;
            case OAID:
                did = MD5Utils.md5Encrypt32Lower(did);
                didType = DeviceIdTypeEnum.OAID_MD5.getCode();
                break;
            case OAID_MD5:
                did = StringUtils.lowerCase(did);
                didType = DeviceIdTypeEnum.OAID_MD5.getCode();
                break;
            case IDFA:
                did = MD5Utils.md5Encrypt32Lower(did);
                didType = DeviceIdTypeEnum.IDFA_MD5.getCode();
                break;
            case IDFA_MD5:
                did = StringUtils.lowerCase(did);
                didType = DeviceIdTypeEnum.IDFA_MD5.getCode();
                break;
            case MAC:
                did = MD5Utils.md5Encrypt32Lower(did.toUpperCase().replaceAll(":", ""));
                didType = DeviceIdTypeEnum.MAC_MD5.getCode();
                break;
            case MAC_MD5:
                did = StringUtils.lowerCase(did);
                didType = DeviceIdTypeEnum.MAC_MD5.getCode();
                break;
            default:
                break;
        }
        return String.format(DEVICE_EXT, didType, did);
    }

}
