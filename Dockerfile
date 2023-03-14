FROM centos:latest
MAINTAINER sapnarsy2612@gmail.com
RUN echo "name=CentOS-$releasever - AppStream" > /etc/yum.repos.d/appstream.repo \
    && echo "baseurl=http://mirror.centos.org/centos/$releasever/appstream/\$basearch/" >> /etc/yum.repos.d/appstream.repo \
    && echo "enabled=1" >> /etc/yum.repos.d/appstream.repo \
    && echo "gpgcheck=0" >> /etc/yum.repos.d/appstream.repo \
    && yum install -y httpd
RUN yum install -y zip
RUN yum install -y unzip
ADD https://www.free-css.com/assets/files/free-css-templates/download/page254/photogenic.zip /var/www/html
WORKDIR /var/www/html
RUN unzip photogenic.zip
RUN cp -rvf photogenic/* .
RUN rm -rf photogenic.zip photogenic
CMD ["/usr/sbin/httpd", "-D", "FOREGROUND"]
EXPOSE 80 22
