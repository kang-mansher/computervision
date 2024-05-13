FROM korekontrol/ubuntu-java-python3

VOLUME /tmp

WORKDIR /app

RUN useradd -m myuser && \
    chown -R myuser:myuser /app

USER myuser

RUN apt-get update

RUN python3 -m pip config set global.break-system-packages true
RUN python3 -m pip install --upgrade pip

RUN pip install numpy
RUN pip install opencv-python
RUN pip install torch torchvision -f https://download.pytorch.org/whl/torch_stable.html
RUN pip install pillow
RUN pip install ultralytics
RUN pip install transformers

RUN apt-get install -y git

COPY target/*.jar /app/app.jar
COPY ./vision /app/vision

EXPOSE 1025
CMD ["java", "-jar", "/app/app.jar"]