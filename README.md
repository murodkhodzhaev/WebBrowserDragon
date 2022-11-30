# NewBrowserPro

Язык - Kotlin.
Архитектура - MVVM.
БД - Room.
Логи - Timber.
Работа с изображениями - Glide.

[Макеты приложения](https://www.figma.com/file/JZPajqfeQPWmNHEfRQcZ7H/design_db?node-id=0%3A1)

Экраны приложения:
* SplashActivity - стартовый экран приложения
* OnboardingActivity - экран с онбордингами
* HomeActivity - начальный экран приложения, в котором отражаются закладки
* BrowserActivity - основной экран, на котором осуществляется браузинг интернета  
* SettingsActivity - экран настроек
* DownloadsActivity - экран загрузок
* HistoryRecordsActivity - экран истории браузера
* MainActivity - тестовая активити, код из которой дублирует код с Foss Browser

Примеры хороших браузеров:
- [Lightning Browser (на Kotlin)](https://github.com/anthonycr/Lightning-Browser)
- [Foss Browser (на Java)](https://github.com/scoute-dich/browser)

В папке <b>src/main/java/com/newbrowser/pro/utils/other</b> находится Foss Browser, переписанный на Kotlin. Возможно, может пригодиться.

Что ещё не сделано:
- вкладки (создание новой, переключение, инкогнито)
- некоторые настройки (указаны в коде)