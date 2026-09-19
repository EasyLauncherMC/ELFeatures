[easylauncher]: https://easylauncher.org/
[githubRepo]: https://github.com/EasyLauncherMC/ELFeatures

[supportedVersionsImg]: https://img.shields.io/badge/%D0%B2%D0%B5%D1%80%D1%81%D0%B8%D1%8F%20%D0%B8%D0%B3%D1%80%D1%8B-1.0%20%D0%B8%20%D0%BD%D0%BE%D0%B2%D0%B5%D0%B5-3BAF18?style=for-the-badge

[latestReleaseImg]: https://img.shields.io/github/v/release/EasyLauncherMC/ELFeatures?color=3BAF18&label=%D0%B2%D0%B5%D1%80%D1%81%D0%B8%D1%8F%20%D0%BC%D0%BE%D0%B4%D0%B0&logo=github&sort=semver&style=for-the-badge
[latestRelease]: https://github.com/EasyLauncherMC/ELFeatures/releases/latest

<div align="center">

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="/.github/banner/dark.png">
  <source media="(prefers-color-scheme: light)" srcset="/.github/banner/light.png">
  <img alt="ELFeatures" src="/.github/banner/light.png" width="100%">
</picture>

![supportedVersionsImg] [![latestReleaseImg]][latestRelease]

</div>

---

## О проекте

ELFeatures — клиентский мод для Minecraft, добавляющий поддержку функций [EasyLauncher][easylauncher]:

- **HD-скины и плащи** от EasyX — отображаются на любом сервере без дополнительных настроек.
- **Статистика игрового времени** для страницы аккаунта в лаунчере *(в разработке)*.
- **QuickPlay** на поддерживаемых версиях — запуск игры сразу в мир или на сервер.
- **Исправления совместимости** для корректной работы на различных платформах.

> [!CAUTION]
> Не пытайтесь самостоятельно установить мод в папку `mods/` или ещё каким-либо способом!
> EasyLauncher сам на своей стороне незаметно встраивает мод в игру при запуске, и присутствие его в `mods/` будет только мешать.
> Также мод требует дополнительных параметров со стороны нашего лаунчера, поэтому не должен запускаться на других лаунчерах.

> [!IMPORTANT]
> Официально мы не публикуем собранный мод в свободном доступе или на площадках — он предназначен для внутреннего использования только нашим лаунчером.
> Загружая мод со сторонних источников и используя его в своих целях, вы берете на себя полную ответственность за свои действия и сохранность своих игровых данных.

## Поддерживаемые платформы

| Платформа |      Статус       |   Версии игры    |
|:---------:|:-----------------:|:----------------:|
|  Fabric   | ✅ Поддерживается |  `1.14` и новее  |
|   Forge   | ✅ Поддерживается | `1.5.2` и новее  |
| NeoForge  | ✅ Поддерживается | `1.20.2` и новее |
|   Quilt   | ✅ Поддерживается | `1.14.4` и новее |
| OptiFine  | ✅ Поддерживается | `1.7.2` и новее  |
|  Vanilla  | ✅ Поддерживается |  `1.0` и новее   |

## License

This project is open-source and licensed under the [MIT license](/LICENSE).
