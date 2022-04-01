SHELL := /bin/bash
IMAGE_TAG = 17.20

.PHONY: clean docker-clean docker-build docker-push

default: docker-build

clean: docker-clean

docker-clean:
	docker rmi dmatej/eclipse-jenkins-glassfish:$(IMAGE_TAG)

docker-build:
	docker build -t dmatej/eclipse-jenkins-glassfish:$(IMAGE_TAG) .

docker-push:
	docker push dmatej/eclipse-jenkins-glassfish:$(IMAGE_TAG)
