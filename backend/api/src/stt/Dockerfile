FROM python:3.11-slim
RUN apt-get update && apt-get install -y ffmpeg git && rm -rf /var/lib/apt/lists/*
WORKDIR /app
# Build context is repo root in docker-compose.dev.yml
COPY backend/api/src/python-core /opt/python-core
RUN python -m pip install --no-cache-dir --upgrade pip && \
    python -m pip install --no-cache-dir -e /opt/python-core
COPY backend/api/src/stt/requirements.txt requirements.txt
RUN python -m pip install --no-cache-dir -r requirements.txt
COPY backend/api/src/stt /app
ENV PYTHONUNBUFFERED=1
CMD ["python","-m","uvicorn","stt_main:app","--host","0.0.0.0","--port","8000"]
