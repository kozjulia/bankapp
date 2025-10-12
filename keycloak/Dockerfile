FROM alpine:latest

# Установка необходимых пакетов
RUN apk add --no-cache curl jq

# Создаем директории
WORKDIR /app
RUN mkdir -p /app/scripts /app/keycloak/scopes /app/keycloak/clients

# Копируем скрипты и конфигурации
COPY scripts/*.sh /app/scripts/
COPY scopes/* /app/keycloak/scopes/
COPY clients/* /app/keycloak/clients/

# Устанавливаем права на выполнение
RUN chmod +x /app/scripts/*.sh