package com.adanxing.ad.user.job;

import com.adanxing.ad.api.Enum.DeviceExtEnum;
import com.adanxing.ad.api.proto.AdanxingProto;
import com.adanxing.ad.api.util.ByteUtils;
import com.adanxing.ad.api.util.JSONUtils;
import com.adanxing.ad.device.utils.DeviceExtUtils;
import com.adanxing.ad.user.config.CommonConstant;
import com.adanxing.ad.user.daock1.DeviceInstallAppInfoDao;
import com.adanxing.ad.user.model.DeviceInstallAppModel;
import com.adanxing.ad.user.service.ConfigService;
import com.adanxing.ad.user.service.JedisClusterService;
import com.alibaba.fastjson.TypeReference;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: ad-data
 * @ClassName InstallAppJobHandler
 * @description:
 * @author: chenpeng
 * @doc:https://www.yuque.com/u34568847/titrq8/vx6lhk60oubc2a0y
 * @create: 2025-03-26 14:40
 * @Version 1.0
 **/
@Component
@Slf4j
public class InstallAppJobHandler {

    @Autowired
    ConfigService configService;

    @Autowired
    DeviceInstallAppInfoDao deviceInstallAppInfoDao;
    @Resource(name = "deviceExtNewClusterService")
    JedisClusterService deviceExtNewClusterService;

    @Value("${ad.device.install_app.bit_app_id_convert_map}")
    private String bitAppIdConvertMapStr;

    @Resource(name = "deviceThreadExecutor")
    AsyncTaskExecutor deviceThreadExecutor;



    @XxlJob("deviceInstallAppMergeJob")
    public ReturnT<String> deviceInstallAppMergeJob() throws Exception {
        String param = XxlJobHelper.getJobParam();
        long t1 = System.currentTimeMillis();
        LocalDateTime nowLocalDateTime = LocalDateTime.now();
        String nowTimeStr = nowLocalDateTime.format(DateTimeFormatter.ofPattern(CommonConstant.DATE_TIME_PATTERN));
        log.info("XXL-JOB, deviceInstallApp. nowTimeStr={},param={} start...", nowTimeStr,param);
        //查询request_log中5分钟前到15分钟前（10分钟内的数据）
        int gapTime = configService.getConfigIntValue("ad.device.trust_install_app.gap_time_minute",5);
        LocalDateTime startLocalDateTime = nowLocalDateTime.minusMinutes(5+gapTime);
        LocalDateTime endLocalDateTime = nowLocalDateTime.minusMinutes(5);
        if(startLocalDateTime.getDayOfYear() != endLocalDateTime.getDayOfYear()){
            log.info("XXL-JOB, deviceInstallApp. nowTimeStr={} 跨天了不做任何处理", nowTimeStr);
            return ReturnT.SUCCESS;
        }
        //key:app_id value:bit_app_id
        Map<Integer,Integer> installAppIdConvertMap = new HashMap<>();
        if(StringUtils.isNotBlank(bitAppIdConvertMapStr)){
            installAppIdConvertMap = JSONUtils.parseObject(bitAppIdConvertMapStr,new TypeReference<Map<Integer,Integer>>(){});
        }
        if(MapUtils.isEmpty(installAppIdConvertMap)){
            log.warn("XXL-JOB, deviceInstallApp. nowTimeStr={} appolo配置 ad.device.trust_install_app.app_id_convert_map未配置无法处理 ", nowTimeStr);
            return ReturnT.SUCCESS;
        }
        long t2 = System.currentTimeMillis();
        String startTime = startLocalDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endTime = endLocalDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if(StringUtils.isNotBlank(param)){
            String[] paramArr = param.split(",");
            if(paramArr.length == 2){
                startTime = paramArr[0];
                endTime = paramArr[1];
            }
        }
        String tableName = "request_log_"+endLocalDateTime.format(DateTimeFormatter.ofPattern(CommonConstant.DATE_YYYYMMDD))+"_all";
        String trustInstallAppMediaIds = configService.getConfigValue("ad.device.trust_install_app.media_ids","'10101','10186'");
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("startTime",startTime);
        paramMap.put("endTime",endTime);
        paramMap.put("tableName",tableName);
        paramMap.put("trustInstallAppMediaIds",trustInstallAppMediaIds);
        try{
            //回滚数据
            deviceInstallAppInfoDao.deleteDeviceInstallAppInfo();
            long t21 = System.currentTimeMillis();
            log.info("XXL-JOB, deviceInstallApp. nowTimeStr={},deleteDeviceInstallAppInfo 完成，耗时{}ms", nowTimeStr,t21-t2);
            deviceInstallAppInfoDao.insertDeviceInstallAppInfo(paramMap);
        }catch (Exception e){
            log.error("XXL-JOB, deviceInstallApp. nowTimeStr="+nowTimeStr+",insertDeviceInstallAppInfo 异常",e);
        }
        long t3 = System.currentTimeMillis();
        log.info("XXL-JOB, deviceInstallApp. nowTimeStr={},delete+insertDeviceInstallAppInfo 完成，耗时{}ms", nowTimeStr,t3-t2);
        Map<String,Object> pageParamMap = new HashMap<>();
        pageParamMap.put("startTime",startTime);
        pageParamMap.put("endTime",endTime);
        int pageSize = configService.getConfigIntValue("ad.device.trust_install_app.page_size",100000);
        int batchSize = configService.getConfigIntValue("ad.device.trust_install_app.batch_size",10000);
        int sleepTime = configService.getConfigIntValue("ad.device.trust_install_app.sleep_time_ms",3000);

        pageParamMap.put("pageSize",pageSize);
        int page = 1;
        List<DeviceInstallAppModel> installAppModelList = deviceInstallAppInfoDao.pageDeviceInstallAppInfo(pageParamMap);
        while (CollectionUtils.isNotEmpty(installAppModelList)){
            long t4 = System.currentTimeMillis();
            log.info("XXL-JOB, deviceInstallApp. nowTimeStr={},pageDeviceInstallAppInfo page={},pageSize={} 查询完成，耗时{}ms", nowTimeStr,page,pageSize,t4-t3);
            for (int i = 0; i < installAppModelList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, installAppModelList.size());
                List<DeviceInstallAppModel> batchDeviceInstallAppList = installAppModelList.subList(i, end);
                if(CollectionUtils.isEmpty(batchDeviceInstallAppList)){
                    continue;
                }
                long t5 = System.currentTimeMillis();
                Map<Integer, Integer> finalInstallAppIdConvertMap = installAppIdConvertMap;
                int finalPage = page;
                int finalI = i;
                deviceThreadExecutor.execute(() -> {
//                    log.info("XXL-JOB, deviceInstallApp. threadName={}, nowTimeStr={},fillRedisDeviceInstallAppInfo page={},batch={} 开始执行", Thread.currentThread().getName(),nowTimeStr,finalPage,finalI);
                    fillRedisDeviceInstallAppInfo(batchDeviceInstallAppList, finalInstallAppIdConvertMap, finalPage, finalI);
                });
//                long t6 = System.currentTimeMillis();
//                log.info("XXL-JOB, deviceInstallApp. nowTimeStr={},fillRedisDeviceInstallAppInfo page={},batch={}执行完成，耗时{}ms", nowTimeStr,page,i,t6-t5);
            }
            if(installAppModelList.size() < pageSize){
                break;
            }
            t3 = System.currentTimeMillis();
            //持续批次处理
            DeviceInstallAppModel lastDeviceInstallAppModel =  installAppModelList.get(installAppModelList.size()-1);
            pageParamMap.put("startDid",lastDeviceInstallAppModel.getDid());
            installAppModelList = deviceInstallAppInfoDao.pageDeviceInstallAppInfo(pageParamMap);
            page++;
            //避免持续占用链接
            Thread.sleep(sleepTime);
        }
        log.info("XXL-JOB, deviceInstallApp. nowTimeStr={},param={} end,cost_time={}ms", nowTimeStr,param, System.currentTimeMillis() - t1);
        return ReturnT.SUCCESS;
    }

    /***
     * redis数据聚合
     * 1)批次拉取redis对应设备的记录（若有，30天之内（暂定）的判断为有效数据）
     * 2）如果trust_device_install_app处理结果不为空，则default_device_install_app不做聚合处理。
     * 3）更新redis数据：若是trust_device_install_app不为空，则default_device_install_app置空
     * 4）为了减少对redis的写入，同一设备实时汇总和redis中读取的安装态比较，变化立即更新。
     * @param batchDeviceInstallAppList
     */
    private void fillRedisDeviceInstallAppInfo(List<DeviceInstallAppModel> batchDeviceInstallAppList,
                                               Map<Integer,Integer> installAppIdConvertMap,
                                               int page,
                                               int batch){

        LocalDateTime nowLocalDateTime = LocalDateTime.now();
        int currentLogTime = (int) nowLocalDateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        int beforeMonthDayLogTime = (int) nowLocalDateTime.minusMonths(1).atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        long t11 = System.currentTimeMillis();
        List<DeviceInstallAppModel> filterBatchDeviceInstallAppList =  batchDeviceInstallAppList.stream().map(model -> {
            int trustBitInstallAppIds = 0;
            int defaultBitInstallAppIds = 0;
            if(StringUtils.isNotBlank(model.getTrustInstallAppIds())){
                Set<Integer> trustInstallAppIdSet = Arrays.stream(model.getTrustInstallAppIds().split(",")).map(Integer::valueOf).collect(Collectors.toSet());
                trustBitInstallAppIds = getBitDeviceInstallAppIds(trustInstallAppIdSet,installAppIdConvertMap);
            }
            if(StringUtils.isNotBlank(model.getDefaultInstallAppIds())){
                Set<Integer> defaultInstallAppIdSet = Arrays.stream(model.getDefaultInstallAppIds().split(",")).map(Integer::valueOf).collect(Collectors.toSet());
                defaultBitInstallAppIds = getBitDeviceInstallAppIds(defaultInstallAppIdSet,installAppIdConvertMap);
            }
            model.setTrustBitInstallAppIds(trustBitInstallAppIds);
            model.setDefaultBitInstallAppIds(defaultBitInstallAppIds);
            return model;
        }).filter(model -> model.getTrustBitInstallAppIds() > 0 || model.getDefaultBitInstallAppIds() > 0).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(filterBatchDeviceInstallAppList)){
            log.info("XXL-JOB, deviceInstallApp.threadName={} fillRedisDeviceInstallAppInfo-batchHGet 没有要处理的数据,page={},batch={},cost_time={}ms ",Thread.currentThread().getName(),page,batch,System.currentTimeMillis() - t11);
            return;
        }
        Set<byte[]> batchDeviceExtUserKeyList = filterBatchDeviceInstallAppList.stream()
                .map(model -> DeviceExtUtils.getDeviceExtUserKey(model.getDid(),model.getDidType()).getBytes(StandardCharsets.UTF_8)).collect(Collectors.toSet());
        byte[] redisField = ByteUtils.intToBytes(DeviceExtEnum.DEVICE_INSTALL_APP_NEW_INFO.getCode());
        //批次查询redis
        Map<byte[], byte[]> batchRedisDeviceInstallAppMap = deviceExtNewClusterService.batchHGet(batchDeviceExtUserKeyList,redisField);
        long t12 = System.currentTimeMillis();
//        log.info("XXL-JOB, deviceInstallApp.threadName={} fillRedisDeviceInstallAppInfo-batchHGet 完成 page={},batch={}, size={},cost_time={}ms ",Thread.currentThread().getName(),page,batch,batchDeviceExtUserKeyList.size(), t12 - t11);
        Map<byte[], byte[]> newBatchRedisDeviceInstallAppMap = new HashMap<>();
        for(DeviceInstallAppModel model : filterBatchDeviceInstallAppList){
            byte[] redisKey = DeviceExtUtils.getDeviceExtUserKey(model.getDid(),model.getDidType()).getBytes(StandardCharsets.UTF_8);
            byte[] redisValue = batchRedisDeviceInstallAppMap.get(redisKey);
            AdanxingProto.DeviceInstallNewFeature.Builder deviceInstallFeature = null;
            boolean  newFeatureFlag =  false;
            int trustBitInstallAppIds = model.getTrustBitInstallAppIds();
            int defaultBitInstallAppIds = model.getDefaultBitInstallAppIds();
            if(Objects.isNull(redisValue)){
                newFeatureFlag = true;
            }else{
                try{
                    deviceInstallFeature = AdanxingProto.DeviceInstallNewFeature.parseFrom(redisValue).toBuilder();
                    int redisLogTime = deviceInstallFeature.getLogTime();
                    //超过30天忽略
                    if(redisLogTime < beforeMonthDayLogTime){
                        newFeatureFlag = true;
                    }else{
                        //没有变化不关注
                        if(trustBitInstallAppIds == deviceInstallFeature.getTrustDeviceInstallApp()
                                && defaultBitInstallAppIds == deviceInstallFeature.getDefaultDeviceInstallApp()){
                            continue;
                        }
                        if(trustBitInstallAppIds > 0 || deviceInstallFeature.getTrustDeviceInstallApp() > 0){
                            deviceInstallFeature.setTrustDeviceInstallApp(trustBitInstallAppIds | deviceInstallFeature.getTrustDeviceInstallApp());
                            deviceInstallFeature.setDefaultDeviceInstallApp(0);
                        }else if(defaultBitInstallAppIds > 0){
                            deviceInstallFeature.setDefaultDeviceInstallApp(defaultBitInstallAppIds | deviceInstallFeature.getDefaultDeviceInstallApp());
                        }
                    }
                }catch (Exception e){
                    newFeatureFlag = true;
                    log.error("XXL-JOB, deviceInstallApp.转化AdanxingProto.DeviceInstallNewFeature出现异常,重新生成Feature并写入redis model="+JSONUtils.toJSONString(model),e);
                }
            }
            if(newFeatureFlag){
                deviceInstallFeature = AdanxingProto.DeviceInstallNewFeature.newBuilder();
                deviceInstallFeature.setLogTime(currentLogTime);
                deviceInstallFeature.setTrustDeviceInstallApp(trustBitInstallAppIds);
                if(trustBitInstallAppIds == 0){
                    deviceInstallFeature.setDefaultDeviceInstallApp(defaultBitInstallAppIds);
                }
            }
            newBatchRedisDeviceInstallAppMap.put(redisKey,deviceInstallFeature.build().toByteArray());
        }
        if(MapUtils.isNotEmpty(newBatchRedisDeviceInstallAppMap)){
            deviceExtNewClusterService.batchHSet(newBatchRedisDeviceInstallAppMap,redisField);
        }
        long t13 = System.currentTimeMillis();
//        log.info("XXL-JOB, deviceInstallApp. threadName={},fillRedisDeviceInstallAppInfo-batchHSet 完成 page={},batch={},size={},cost_time={}ms ",Thread.currentThread().getName(),page,batch,newBatchRedisDeviceInstallAppMap.size(), t13 - t12);
        log.info("XXL-JOB, deviceInstallApp. threadName={},fillRedisDeviceInstallAppInfo-batchHGet+batchHSet 完成 page={},batch={},size={},cost_time={}ms ",Thread.currentThread().getName(),page,batch,newBatchRedisDeviceInstallAppMap.size(), t13 - t11);

    }

    /***
     * list转化为bit
     * @param installAppIdSet
     * @param installAppIdConvertMap (key:app_id value:bit_app_id)
     * @return
     */
    private int getBitDeviceInstallAppIds(Set<Integer> installAppIdSet,Map<Integer,Integer> installAppIdConvertMap){
        int bitDeviceInstallAppIds = 0;
        for(Integer installAppId: installAppIdSet){
            Integer bitAppId = installAppIdConvertMap.get(installAppId);
            if(Objects.isNull(bitAppId)){
                continue;
            }
            bitDeviceInstallAppIds |= bitAppId;
        }
        return bitDeviceInstallAppIds;
    }
}
