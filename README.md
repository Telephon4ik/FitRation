# FitRation
**Платформа для управления питанием и фитнес-тренерства**

FitRation — это мобильное приложение для Android, которое помогает пользователям следить за питанием и достигать фитнес-целей с помощью персонализированных планов питания и профессионального сопровождения тренеров.

---

## Возможности

### Для пользователей
- Персонализированные планы питания на основе целей и параметров
- Автоматическая генерация блюд из базы Spoonacular API
- Отметка съеденных блюд для точного учета
- Поиск и привязка тренеров по уникальному ID

### Для тренеров / диетологов
- Управление клиентами
- Комментирование блюд с рекомендациями
- Просмотр питания клиентов за любые даты
- Система заявок от пользователей

---

## Архитектура

### Технологический стек
- **Язык:** Java
- **Минимальная версия Android:** API 26 (Android 8.0)
- **Архитектура:** MVVM (Model-View-ViewModel)
- **База данных:** Firebase Firestore
- **Аутентификация:** Firebase Authentication
- **API:** Spoonacular API для генерации блюд

### Библиотеки
- Material Components — UI
- Glide — загрузка изображений
- MPAndroidChart — графики
- Retrofit / OkHttp — сетевые запросы

---

## Структура проекта

```text
FitRation/
├── app/
│   ├── src/main/java/com/fitration/
│   │   ├── auth/              # Экран авторизации и регистрации
│   │   ├── models/            # Модели данных
│   │   ├── repository/        # Репозитории для работы с данными
│   │   ├── ui/                # Пользовательский интерфейс
│   │   │   ├── coach/         # Интерфейс для тренеров
│   │   │   └── user/          # Интерфейс для пользователей
│   │   ├── utils/             # Утилиты и вспомогательные классы
│   │   └── viewmodels/        # ViewModel'ы для MVVM
│   └── res/                   # Ресурсы приложения
```
## Установка и запуск

### Требования
- Android Studio Arctic Fox или выше
- JDK 17
- API ключ Spoonacular
- Настроенный проект Firebase

### Шаги установки

#### Клонирование репозитория
```bash
git clone https://github.com/yourusername/fitration.git
```
#### Открытие проекта
```bash
cd fitration
# Откройте Android Studio и выберите проект
```
## Настройка Firebase

1. Создайте проект на [Firebase Console](https://console.firebase.google.com/).
2. Добавьте приложение Android.
3. Скачайте `google-services.json` и поместите в `app/`.

## Получение API ключа Spoonacular

1. Зарегистрируйтесь на [Spoonacular](https://spoonacular.com/food-api).
2. Добавьте ключ в `SpoonacularService.java`:

```java
// SpoonacularService.java
private static final String API_KEY = "ваш_ключ_здесь";
```
## Запуск приложения

1. Выберите эмулятор или подключите устройство
2. Нажмите **"Run"** в Android Studio

---

## Экранная демонстрация

### Авторизация

| Экран входа | Регистрация пользователя | Регистрация тренера |
|-------------|-------------------------|-------------------|
| <img width="276" height="491" alt="image" src="https://github.com/user-attachments/assets/8180b76b-90c6-4c07-9ede-fc6b424a6f20" />
 | <img width="1080" height="3752" alt="image" src="https://github.com/user-attachments/assets/20c96d8e-ca94-4480-9281-28497404c27d" />
| <img width="1080" height="2040" alt="image" src="https://github.com/user-attachments/assets/1a58b23d-b8f3-4f66-83d3-df29ac1460ea" />
|

### Для пользователей

| План питания | Профиль пользователя | Отправка заявки |
|-------------|--------------------|----------------|
| <img width="1080" height="2264" alt="image" src="https://github.com/user-attachments/assets/bb2c4aad-d7d1-4eaa-b1ee-8374faf9ea39" />
 | <img width="1080" height="2212" alt="image" src="https://github.com/user-attachments/assets/ebdde4e9-6377-4d6f-8937-f8eafd74d798" />
 | <img width="1080" height="2219" alt="image" src="https://github.com/user-attachments/assets/dae2887b-daef-4a3f-a2f8-f50089bdd953" />
 |

### Для тренеров

| Список клиентов | Питание клиента | Профиль тренера |
|----------------|----------------|----------------|
| <img width="1080" height="2287" alt="image" src="https://github.com/user-attachments/assets/a99ee97a-0f53-4269-901e-b2877cc06290" />
 | <img width="1080" height="2273" alt="image" src="https://github.com/user-attachments/assets/45747e66-a50b-4ebf-9728-b723070b9b07" />
| <img width="1080" height="2272" alt="image" src="https://github.com/user-attachments/assets/13488e33-d67f-4a8e-8888-922cdb7fe6ef" />
 |

---

## Настройка

### Конфигурация Firebase

- **Firebase Authentication**: Включите Email/Password authentication
- **Firebase Firestore**: Создайте базу данных в режиме разработки
- **Правила безопасности**: Настройте правила доступа

### API ключи

```java
// SpoonacularService.java
private static final String API_KEY = "ваш_ключ_здесь";
```
# Настройка окружения

Создайте файл `local.properties` с переменными окружения при необходимости:

```properties
# Опционально для специфических конфигураций
```

---

## Структура базы данных

### Коллекции Firestore

**users/** — Пользователи системы  
├── uid: string        # UID пользователя
├── email: string      # Email
├── name: string       # Имя
├── role: string       # Роль (USER/COACH)
├── publicId: string   # Публичный ID (FR-XXXXXX)
├── coachId: string    # ID привязанного тренера
└── profileData: map   # Данные профиля

**meals/**                 # Блюда
├── userId: string     # ID пользователя
├── date: timestamp    # Дата приема пищи
├── mealType: string   # Тип (breakfast/lunch/dinner/snack)
├── nutrition: map     # Пищевая ценность
└── coachComment: string # Комментарий тренера

**coach_requests/**       # Заявки тренерам
├── coachId: string    # ID тренера
├── userId: string     # ID пользователя
├── status: string     # Статус (PENDING/ACCEPTED/REJECTED)
└── message: string    # Сообщение

---

# Используемые технологии

- **Firebase** — Бэкенд и аутентификация
- **Spoonacular API** — Генерация блюд
- **Material Design** — Дизайн система
- **Glide** — Загрузка изображений

---
