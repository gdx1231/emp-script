# 安装 DKIMforJavaMail 到本地仓库

mvn install:install-file -Dfile=DKIMforJavaMail.jar \
	 -DgroupId=DKIMforJavaMail \
	 -DartifactId=DKIMforJavaMail \
	 -Dversion=1 \ 
	 -Dpackaging=jar 