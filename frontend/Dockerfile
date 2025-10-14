FROM node:20-slim
WORKDIR /app
COPY package.json pnpm-lock.yaml ./
RUN corepack enable && corepack prepare pnpm@9.12.3 --activate && pnpm install --frozen-lockfile
COPY . .
EXPOSE 5173
CMD ["pnpm","dev","--host","0.0.0.0"]

