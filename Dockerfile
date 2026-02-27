# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# FROM gebish/ci:v13
FROM eclipse-temurin:11-jdk

RUN wget https://launchpad.net/~xtradeb/+archive/ubuntu/apps/+files/xtradeb-apt-source_0.4_all.deb
RUN dpkg -i xtradeb-apt-source_0.4_all.deb
RUN rm *.deb

RUN apt-get update && \
    apt-get install -y \
    git \
    xvfb \
    curl \
    bzip2 \
    libgtk-3-dev \
    libdbus-glib-1-2 \
    wget \
    gnupg \
    firefox \
    firefox-geckodriver \
    chromium \
    chromium-driver \
    ca-certificates \
    gosu && \
    apt-get clean

RUN useradd -u 1001 -m circleci

WORKDIR /home/circleci

ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# Set up Xvfb to start automatically
ENV DISPLAY=:99

ENV CI=true

# Create an entrypoint script
COPY --chmod=755 docker-entrypoint.sh /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["bash"]
