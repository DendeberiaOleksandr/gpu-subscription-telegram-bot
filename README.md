# [gpu-subscription-telegram-bot](https://github.com/DendeberiaOleksandr/gpu-subscription-telegram-bot/tree/master)

This application is [Telegram Bot](https://core.telegram.org/bots/api) that receives subscriptions on Graphic Cards from users through Telegram Client. For manage subscriptions use commands:

| Command  | Description |
| ------------- | ------------- |
| /s  | Displays all subscriptions with id and GPU name  |
| /s -d {id}  | Delete subscription  |
| /s -a {GPU.name} | Add new subscription |

Using scheduling application also parses info from websites basing on subscriptions. By default scheduling is hardcoded and works every 90s. 
