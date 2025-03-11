package com.adanxing.ad.device.enums;

import lombok.Getter;

@Getter
public enum DeviceExtEnum {

    CROWD(1, "crowd"),
    MIANZHEN_TAG(2, "mianZhenTag"),
    MEDIA_TAG(3, "mediaTag"),
    QIHANG_DATA(4, "qihangData"),
    TANX_FEATURE(5, "TanxFeature"),
    JD_FEATURE(6, "JDFeature"),
    QIHANG_FEATURE(7, "QihangFeature"),
    MEITUAN_FEATURE(8, "MeituanFeature"),
    IMP_FEATURE_TOTAL(9, "ssp直客和直客总曝光频次"),
    PDD_FEATURE(10, "PddFeature"),
    IMP_FEATURE_WEEK(11, "ssp直客和直客周曝光频次"),
    DOUYU_DEVICE_INFO(12, "斗鱼设备信息"),
    AX_DEVICE_INFO(13, "设备采集信息"),
    RTQ_BLACK(14, "秒针设备黑名单"),
    DEVICE_DEEP_INFO(15, "设备唤起信息"),
    DEVICE_INSTALL_APP_INFO(16, "设备安装态信息"),
    TBCPS_TASK(17, "淘宝CPS-Task"),
    @Deprecated
    OTT_MOB(18, "手机投屏-ott-mob"),
    DEVICE_INFO(19, "设备信息"),
    X_IP_DEVICE(20, "x-ip-device"),
    ;

    private Integer code;
    private String desc;

    DeviceExtEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    public static DeviceExtEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DeviceExtEnum typeEnum : DeviceExtEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }

}
