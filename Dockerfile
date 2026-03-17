# Stage 1: Build fat JAR
FROM amazoncorretto:25 AS builder

WORKDIR /app

RUN yum install -y findutils

# Copy Gradle wrapper and build files first for better layer caching
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon

# Copy source and build fat JAR
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# Stage 2: Lambda custom runtime
FROM public.ecr.aws/lambda/provided:al2023

# Install Amazon Corretto 25 JDK
RUN dnf install -y java-25-amazon-corretto-devel

# Install Xvfb and X11 libraries needed for Chrome without a real display
RUN dnf install -y \
    wget \
    unzip \
    alsa-lib \
    atk \
    at-spi2-atk \
    cups-libs \
    gtk3 \
    libXcomposite \
    libXcursor \
    libXdamage \
    libXext \
    libXi \
    libXrandr \
    libXScrnSaver \
    libXtst \
    nss \
    nspr \
    pango \
    libXt

# Install Google Chrome stable and matching ChromeDriver
RUN wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_x86_64.rpm \
    && rpm -ivh --nodeps google-chrome-stable_current_x86_64.rpm \
    && rm -f google-chrome-stable_current_x86_64.rpm \
    && CHROMEDRIVER_URL=$(curl -s https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions-with-downloads.json \
        | python3 -c "import json,sys; d=json.load(sys.stdin); print(next(x['url'] for x in d['channels']['Stable']['downloads']['chromedriver'] if x['platform']=='linux64'))") \
    && wget -q "$CHROMEDRIVER_URL" -O /tmp/chromedriver.zip \
    && unzip /tmp/chromedriver.zip -d /tmp/ \
    && mv /tmp/chromedriver-linux64/chromedriver /usr/bin/chromedriver \
    && chmod +x /usr/bin/chromedriver \
    && rm -rf /tmp/chromedriver*

# Copy the fat JAR from builder
COPY --from=builder /app/build/libs/*.jar /app/app.jar

WORKDIR /app

ENV JAVA_HOME=/usr/lib/jvm/java-25-amazon-corretto
ENV PATH=${JAVA_HOME}/bin:${PATH}
# Lambda custom runtime bootstrap
COPY lambda/bootstrap /var/runtime/bootstrap
RUN chmod +x /var/runtime/bootstrap

CMD ["handler"]
