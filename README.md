# [English document](https://yolandaqingniu.github.io/)

# 轻牛蓝牙Android SDK

## 快速集成 
### 混淆配置(proguard-rules)
+ -keep class com.qingniu.scale.model.BleScaleData{*;}

##具体操作文档
[具体集成说明文档](https://yolandaqingniu.github.io/)

### Android Studio 线上依赖
* 在你工程的根目录下的 **build.gradle**添加**jitpack**支持
   ```
   allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
   ```
* 在你的module的根目录下的**build.gradle**添加依赖
	```
	<!--这里的版本号，2.24.0 可以指定为任意release版本-->
	<!--如果希望一直使用最新版本可以替换 2.24.0 master-SNAPSHOT -->
	dependencies {
	        ...
	        compile 'com.github.YolandaQingniu:qnscalesdkX:2.24.0'
	}
	```

## 注意事项
- targetSdkVersion 在28及以上，需要先获取定位权限，才能扫描到设备，需要开发者自己申请
- 部分手机上使用蓝牙功能需要开启GPS才能扫描到设备，SDK中会输出GPS未开启的日志，但不会回调错误，开发者可以自主进行限制
- 如果你的项目是多进程的，建议限制在主进程才进行SDK的初始化

## 常见问题
具体使用参考[API文档](https://yolandaqingniu.github.io/)和`Demo`，以下为一些常见问题。

1. 初始化提示appid错误
    + 检查初始化文件和使用的appid是否匹配
    + 检查引入的SDK是否是最新的
2. 扫描设备调用成功，但是一直没有设备回调，且无错误回调
    + 检查所扫描的设备，是否已经被其他人连接
    + 部分手机需要开启GPS才能扫描到设备，请检查手机GPS是否开启
3. 连接设备一直无法成功或者成功后很快就断开连接
    + 检查设备是否被其他人连接了
    + 在系统蓝牙中查看是否当前连接的设备已经被配对,如果已经配对，需要取消配对
    + 部分手机需要先扫描才能连接成功，先扫描设备再进行连接
4. 获取到的指标与和商务洽谈的指标数不同
    + 先检查出现问题的设备，扫描时显示的名称是否正确
    + 心率秤无论是否开放了心率指标，SDK都会下发心率指标
5. 数据或者设备等监听回调，同一时间回调多次
    + 先确定是否，设置了多次监听。当监听不使用时，一定要设置为null
    + 确定是否是穿鞋测量，这个可能导致短时间内，完成多次测量的情况
6. SDK返回无定位权限错误
    + 检查是否对**ACCESS_COARSE_LOCATION**和**ACCESS_FINE_LOCATION**都进行了申请，SDK中对2个权限都进行了校验
    + 是否编译版本26以及以上，如果是，2个权限都需要单独申请(8.0的新特性)
7. SDK返回错误的文件,确认文件位置无异常，确认文件在demo中使用无异常
    + 检查是否有添加so库
    + 检查是否打包的apk文件中含有so库
8. SDK 集成后测量Native方法报错
    + 检查是否集成设置有问题
    + 本地集成不能和在线集成混合使用，只能选择一种方式
    
**`提示`**：遇到无法定位的问题，希望开发者能第一时间提供日志，以便我们尽快找到问题    
