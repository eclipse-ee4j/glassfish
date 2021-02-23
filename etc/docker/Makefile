SHELL := /bin/bash
IMAGE_TAG = tini-jdk-11.0.10

.PHONY: clean docker-clean docker-build docker-push

default: docker-build

clean: docker-clean

docker-clean:
	docker rmi ee4jglassfish/ci:$(IMAGE_TAG)

docker-build:
	docker build -t ee4jglassfish/ci:$(IMAGE_TAG) .

docker-push:
	docker push ee4jglassfish/ci:$(IMAGE_TAG)
