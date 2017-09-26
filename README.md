# 说明
这是一个安卓应用程序检查、下载、更新模块
# 集成
首先，在工程目录下的build.gradle中添加jitpack.io仓库地址
<pre>
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
</pre>
然后，在你的应用模块目录下添加updater远程依赖
<pre>
dependencies {
        compile 'com.github.dida-logistics-mobile:updater:1.0.0'
}
</pre>
