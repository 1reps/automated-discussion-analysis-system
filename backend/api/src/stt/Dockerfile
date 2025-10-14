FROM google/cloud-sdk:slim
RUN apt-get update && apt-get install -y python3 python3-pip ffmpeg git && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY backend/python-core /opt/python-core
RUN python3 -m pip install --no-cache-dir --upgrade pip && \
    python3 -m pip install --no-cache-dir -e /opt/python-core
COPY backend/stt/requirements.txt requirements.txt
RUN python3 -m pip install --no-cache-dir -r requirements.txt
COPY backend/stt /app
ENV PYTHONUNBUFFERED=1
CMD ["python3","-m","uvicorn","stt_main:app","--host","0.0.0.0","--port","8000"]
