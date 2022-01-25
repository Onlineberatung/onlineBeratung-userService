docker rmi cob/userservice:development10
minikube cache delete cob/userservice:development10
mvn clean package -Dmaven.test.skip
docker build --no-cache -t cob/userservice:development11 .
minikube cache add cob/userservice:development11
minikube cache reload cob/userservice:development11
