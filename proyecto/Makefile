#! /usr/bin/env make -f
SUBDIRS = frontend backend backend-grpc backend-rest backend-rest-externo

all: $(SUBDIRS)

run-devel:
	docker-compose -f docker-compose-devel.yml up

containers: $(SUBDIRS)
	for i in $(SUBDIRS); do \
		$(MAKE) -C $$i $@; \
	done

frontend:
	$(MAKE) -C $@

backend:
	$(MAKE) -C $@

backend-grpc:
	$(MAKE) -C $@

backend-rest:
	$(MAKE) -C $@

clean:
	for i in $(SUBDIRS); do \
		$(MAKE) -C $$i $@; \
	done

maven:
	docker run \
		   -v $$(pwd):/var/maven \
		   -v ~/.m2:/var/maven/.m2 -t \
		   -w /var/maven \
		   --rm -u $$(id -u):$$(id -g) \
		   -e MAVEN_CONFIG=/var/maven/.m2 maven:3-eclipse-temurin-17 \
		   mvn -B -e -C -T 4 -Duser.home=/var/maven -DskipTests clean install


.PHONY: all run-devel containers frontend backend backend-grpc backend-rest clean
