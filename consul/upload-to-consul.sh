#!/bin/bash

# Ждем немного, чтобы убедиться, что Consul полностью готов
sleep 5

# Изменяем путь к конфигурационным файлам
CONFIG_DIR="/consul/config"
if [ ! -d "$CONFIG_DIR" ]; then
    echo "Ошибка: Директория $CONFIG_DIR не существует"
    exit 1
fi

# Проходим по всем файлам в директории
for file in "$CONFIG_DIR"/*; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        filename_without_ext="${filename%.*}"

        echo "Загрузка файла: $filename"

        # Используем consul-server вместо localhost
        curl -X PUT \
             --data-binary @"$file" \
             http://consul-server:8500/v1/kv/config/"$filename_without_ext"/data

        if [ $? -eq 0 ]; then
            echo "✓ Успешно загружен: $filename_without_ext"
        else
            echo "✗ Ошибка при загрузке: $filename_without_ext"
        fi
    fi
done

echo "Загрузка завершена"