ARG FROM_IMAGE=usgs/node:12

FROM ${FROM_IMAGE}

ENV LANG="en_US.UTF-8"

USER root

RUN yum install -y git glibc-langpack-en java-11-openjdk-devel which \
    && yum clean all

USER usgs-user
