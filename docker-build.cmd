docker rmi cob/userservice:development
docker build --no-cache -t cob/userservice:development .
minikube cache add cob/userservice:development
