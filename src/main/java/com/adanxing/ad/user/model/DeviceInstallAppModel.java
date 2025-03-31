package com.adanxing.ad.user.model;

import lombok.Data;

import java.util.Date;

/**
 * @program: ad-data
 * @ClassName DeviceInstallAppInfo
 * @description:
 * @author: chenpeng
 * @doc:
 * @create: 2025-03-26 17:30
 * @Version 1.0
 **/
@Data
public class DeviceInstallAppModel {

    private Date logTime;

    private String did;

    private Integer didType;

    private String trustInstallAppIds;

    private String defaultInstallAppIds;

    int trustBitInstallAppIds = 0;

    int defaultBitInstallAppIds = 0;

}
