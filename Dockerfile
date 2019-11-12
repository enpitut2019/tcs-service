FROM clojure:openjdk-8-lein
ENV LANG C.UTF-8

ENV APP_HOME /app
RUN mkdir $APP_HOME
WORKDIR $APP_HOME
