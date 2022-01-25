docker rmi cob/userservice:development11
minikube cache delete cob/userservice:development11
mvn clean package -Dmaven.test.skip
docker build --no-cache -t cob/userservice:development12 .
minikube cache add cob/userservice:development12
minikube cache reload cob/userservice:development12
